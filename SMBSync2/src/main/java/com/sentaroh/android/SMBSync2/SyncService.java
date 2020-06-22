package com.sentaroh.android.SMBSync2;

/*
The MIT License (MIT)
Copyright (c) 2011-2018 Sentaroh

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

*/

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.util.Log;

import com.sentaroh.android.SMBSync2.Log.LogUtil;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;

import static com.sentaroh.android.SMBSync2.Constants.QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE;
import static com.sentaroh.android.SMBSync2.Constants.QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_ALL;
import static com.sentaroh.android.SMBSync2.Constants.QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_AUTO;
import static com.sentaroh.android.SMBSync2.Constants.QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_MANUAL;
import static com.sentaroh.android.SMBSync2.Constants.QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_TEST;
import static com.sentaroh.android.SMBSync2.Constants.QUERY_SYNC_TASK_INTENT;
import static com.sentaroh.android.SMBSync2.Constants.REPLY_SYNC_TASK_EXTRA_PARM_SYNC_ARRAY;
import static com.sentaroh.android.SMBSync2.Constants.REPLY_SYNC_TASK_EXTRA_PARM_SYNC_COUNT;
import static com.sentaroh.android.SMBSync2.Constants.REPLY_SYNC_TASK_INTENT;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_AUTO_SYNC_INTENT;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_EXTRA_PARM_SYNC_PROFILE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ERROR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_SUCCESS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_ERROR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_SUCCESS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SERVICE_HEART_BEAT;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_START_SYNC_INTENT;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SYNC_ENDED;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SYNC_REQUEST_ACTIVITY;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SYNC_REQUEST_EXTERNAL;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SYNC_REQUEST_SCHEDULE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SYNC_REQUEST_SHORTCUT;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SYNC_STARTED;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ERROR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_SUCCESS;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_INTENT_TIMER_EXPIRED;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_NAME_KEY;

@SuppressLint("Wakelock")
public class SyncService extends Service {
    private GlobalParameters mGp = null;

    private CommonUtilities mUtil = null;

    private WifiManager mWifiMgr = null;

    private Context mContext = null;

    private WifiReceiver mWifiReceiver = new WifiReceiver();

    private SleepReceiver mSleepReceiver = new SleepReceiver();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new GlobalParameters().setNewLocale(base, true));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = SyncService.this;
        mGp= GlobalWorkArea.getGlobalParameters(mContext);
        mGp.safMgr.loadSafFile();
        mUtil = new CommonUtilities(mContext, "Service", mGp, null);
        mUtil.addDebugMsg(1, "I", "onCreate entered"+", settingScreenThemeLanguage="+mGp.settingScreenThemeLanguage);

        NotificationUtil.initNotification(mGp, mUtil, mContext);
        NotificationUtil.clearNotification(mGp, mUtil);

        mUtil.addLogMsg("I", mContext.getString(R.string.msgs_smbsync_main_start) +
                " API=" + Build.VERSION.SDK_INT +
                ", Version " + getApplVersionName());

        if (mGp.syncTaskList == null)
            mGp.syncTaskList = SyncTaskUtil.createSyncTaskList(mContext, mGp, mUtil, false);

        if (mGp.syncHistoryList == null)
            mGp.syncHistoryList = mUtil.loadHistoryList();

        mWifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        initWifiStatus();

        IntentFilter int_filter = new IntentFilter();
        int_filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        int_filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        int_filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        int_filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        int_filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        int_filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, int_filter);

        int_filter = new IntentFilter();
        int_filter.addAction(Intent.ACTION_SCREEN_OFF);
        int_filter.addAction(Intent.ACTION_SCREEN_ON);
        int_filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mSleepReceiver, int_filter);
    }

    private String getApplVersionName() {
        String result = "Unknown";
        try {
            String packegeName = getPackageName();
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packegeName, PackageManager.GET_META_DATA);
            result = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            mUtil.addDebugMsg(1, "I", "SMBSync2 package can not be found");
        }
        return result;
    }

    private boolean mHeartBeatActive = false;

    private void setHeartBeat() {
        if (Build.VERSION.SDK_INT >= 21) {
            if (Build.VERSION.SDK_INT >= 23) {
                String packageName = mContext.getPackageName();
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                if (pm.isIgnoringBatteryOptimizations(packageName)) {
                    mUtil.addDebugMsg(1, "I", "HeartBeat cancelled because ignoring battery optimizations enabled");
                    return;
                }
            }
            mHeartBeatActive = true;
            long time = System.currentTimeMillis() + 1000 * 300;
            Intent in = new Intent();
            in.setAction(SMBSYNC2_SERVICE_HEART_BEAT);
            PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= 23)
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
            else am.set(AlarmManager.RTC_WAKEUP, time, pi);
        }
    }

    private void cancelHeartBeat() {
        mHeartBeatActive = false;
        Intent in = new Intent();
        in.setAction(SMBSYNC2_SERVICE_HEART_BEAT);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        WakeLock wl = ((PowerManager) getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        , "SMBSync-Service-1");
        wl.acquire(1000);
        String action = "";
        if (intent != null) if (intent.getAction() != null) action = intent.getAction();
        if (action.equals(SCHEDULER_INTENT_TIMER_EXPIRED)) {
            mUtil.addDebugMsg(1, "I", "onStartCommand entered, action=" + action);
            if (mGp.settingScheduleSyncEnabled) startSyncByScheduler(intent);
            else {
                mUtil.addDebugMsg(1,"I","Schedule sync request is ignored because scheuler is disabled");
            }
        } else if (action.equals(SMBSYNC2_START_SYNC_INTENT)) {
            mUtil.addDebugMsg(1, "I", "onStartCommand entered, action=" + action);
            startSyncByAnotherAppl(intent);
        } else if (action.equals(SMBSYNC2_AUTO_SYNC_INTENT)) {
            mUtil.addDebugMsg(1, "I", "onStartCommand entered, action=" + action);
            startSyncByShortcut(intent);
        } else if (action.equals(QUERY_SYNC_TASK_INTENT)) {
            mUtil.addDebugMsg(1, "I", "onStartCommand entered, action=" + action);
            processQuerySyncTask(intent);
        } else if (action.equals(SMBSYNC2_SERVICE_HEART_BEAT)) {
//			mUtil.addDebugMsg(0,"I","onStartCommand entered, action="+action);
            if (mHeartBeatActive) setHeartBeat();
        } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED) ||
                action.equals(Intent.ACTION_MEDIA_EJECT)) {
            String path = intent.getDataString();
            mUtil.addDebugMsg(1, "I", "onStartCommand entered, action=" + action+", Path="+path);
            final String sdcard = mGp.safMgr.getSdcardRootPath();
            final String usb_flash = mGp.safMgr.getUsbRootPath();
            Thread th = new Thread() {
                @Override
                public void run() {
                    mGp.refreshMediaDir(mContext);
                    mUtil.addDebugMsg(1, "I", "Media directory, SDCARD=" + mGp.safMgr.getSdcardRootPath() );
                    mUtil.addDebugMsg(1, "I", "Media directory, USB=" + mGp.safMgr.getUsbRootPath());
                    if (mGp.callbackStub != null && (!sdcard.equals(mGp.safMgr.getSdcardRootPath()) || !usb_flash.equals(mGp.safMgr.getUsbRootPath()))) {
                        try {
                            mGp.callbackStub.cbMediaStatusChanged();
                        } catch (RemoteException e) {
                            mUtil.addDebugMsg(1, "I", "Media directory refresh callback failed, error="+e.getMessage());
                        }
                    }
                }
            };
            th.start();
        } else {
            mUtil.addDebugMsg(2, "I", "onStartCommand entered, action=" + action);
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        setActivityForeground();
        return mSvcClientStub;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered, qs=" + mGp.syncRequestQueue.size());
        if (isServiceToBeStopped()) stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        unregisterReceiver(mWifiReceiver);
        unregisterReceiver(mSleepReceiver);
//        unregisterReceiver(mUsbReceiver);
        stopForeground(true);
        if (mGp.notificationLastShowedMessage != null && !mGp.notificationLastShowedMessage.equals("")) {
            showSyncEndNotificationMessage();
            mGp.notificationLastShowedMessage = null;
        }
        cancelHeartBeat();
        mGp.releaseWakeLock(mUtil);
        mUtil.addLogMsgFromUI("I",mContext.getString(R.string.msgs_terminate_application));
        LogUtil.closeLog(mContext, mGp);
        NotificationUtil.setNotificationEnabled(mGp, true);
        CommonUtilities.saveMsgList(mGp);
        if (mGp.activityRestartRequired) {
            mGp.activityRestartRequired = false;
            mGp.clearParms();
            System.gc();

            Handler hndl = new Handler();
            hndl.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(mContext, ActivityMain.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }, 200);
        } else {
            if (mGp.settingExitClean) {
                Handler hndl = new Handler();
                hndl.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }, 100);
            } else {
                mGp.clearParms();
                System.gc();
            }
        }
    }

    private boolean isServiceToBeStopped() {
        boolean result = false;
        if (mGp.callbackStub == null) {
            synchronized (mGp.syncRequestQueue) {
                result = !(mGp.syncRequestQueue.size() > 0 || mGp.syncThreadActive);
            }
        }
        return result;
    }

    private ScheduleItem getScheduleInformation(ArrayList<ScheduleItem> sl, String name) {
        for (ScheduleItem si : sl) {
            if (si.scheduleName.equals(name))
                return si;
        }
        return null;
    }

    private void processQuerySyncTask(Intent in) {
        String reply_list="", sep="";
        String task_type=QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_AUTO;
        if (in.getStringExtra(QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE)!=null) task_type=in.getStringExtra(QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE);
        mUtil.addDebugMsg(1,"I","extra="+in.getExtras()+", str="+in.getStringExtra(QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE));
        int reply_count=0;
        if (mGp.syncTaskList.size()>0) {
            for(int i=0;i<mGp.syncTaskList.size();i++) {
                SyncTaskItem sti=mGp.syncTaskList.get(i);
                if (task_type.toLowerCase().equals(QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_TEST.toLowerCase())) {
                    if (sti.isSyncTestMode()) {
                        reply_list+=sep+sti.getSyncTaskName();
                        sep=",";
                        reply_count++;
                    }
                } else if (task_type.toLowerCase().equals(QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_AUTO.toLowerCase())) {
                    if (sti.isSyncTaskAuto()) {
                        reply_list+=sep+sti.getSyncTaskName();
                        sep=",";
                        reply_count++;
                    }
                } else if (task_type.toLowerCase().equals(QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_MANUAL.toLowerCase())) {
                    if (!sti.isSyncTaskAuto()) {
                        reply_list+=sep+sti.getSyncTaskName();
                        sep=",";
                        reply_count++;
                    }
                } else if (task_type.toLowerCase().equals(QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_ALL.toLowerCase())) {
                    reply_list+=sep+sti.getSyncTaskName();
                    sep=",";
                    reply_count++;
                }
            }
        }
        Intent reply=new Intent(REPLY_SYNC_TASK_INTENT);
        reply.putExtra(REPLY_SYNC_TASK_EXTRA_PARM_SYNC_COUNT, reply_count);
        reply.putExtra(REPLY_SYNC_TASK_EXTRA_PARM_SYNC_ARRAY, reply_list);
        mContext.sendBroadcast(reply);
    }

    private void startSyncByScheduler(Intent in) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

        mUtil.addLogMsg("I", mContext.getString(R.string.msgs_svc_received_start_request_from_scheduler));

        ArrayList<ScheduleItem> scheduleInfoList = ScheduleUtil.loadScheduleData(mContext, mGp);
        if (in.getExtras().containsKey(SCHEDULER_SCHEDULE_NAME_KEY)) {
            String schedule_name_list = in.getStringExtra(SCHEDULER_SCHEDULE_NAME_KEY);

            mUtil.addDebugMsg(1,"I", "Schedule information, name=" + schedule_name_list);

            String[] schedule_list=schedule_name_list.split(",");

            for(String schedule_name:schedule_list) {
                mUtil.addDebugMsg(1, "I", "Schedule start, name=" + schedule_name);
                ScheduleItem si = getScheduleInformation(scheduleInfoList, schedule_name);
                if (si!=null) {
                    if (si.syncAutoSyncTask) {
                        queueAutoSyncTask(SMBSYNC2_SYNC_REQUEST_SCHEDULE, si);
                    } else {
                        if (si.syncTaskList != null && si.syncTaskList.length() > 0) {
                            String[] pl = si.syncTaskList.split(",");
                            String n_tl = "", sep = "";
                            for (int i = 0; i < pl.length; i++) {
                                if (getSyncTask(pl[i]) != null) {
                                    n_tl += sep + pl[i];
                                    sep = ",";
                                } else {
                                    mUtil.addLogMsg("W",
                                            mContext.getString(R.string.msgs_svc_received_start_request_from_scheduler_task_not_found) + pl[i]);
                                }
                            }
                            if (!n_tl.equals("")) {
                                String[] n_pl = n_tl.split(",");
                                queueSpecificSyncTask(n_pl, SMBSYNC2_SYNC_REQUEST_SCHEDULE, si);
                            } else {
                                mUtil.addLogMsg("E", mContext.getString(R.string.msgs_svc_received_start_request_from_scheduler_no_task_list));
                            }
                        } else {
                            mUtil.addLogMsg("E", mContext.getString(R.string.msgs_svc_received_start_request_from_scheduler_no_task_list));
                        }
                    }
                } else {
                    mUtil.addLogMsg("W","Specified schedule name was not found, name=",schedule_name);
                }
            }
            if (!mGp.syncThreadActive) {
                sendStartNotificationIntent();
                startSyncThread();
            }

        }
//        mUtil.addLogMsg("I", mContext.getString(R.string.msgs_svc_received_start_request_from_scheduler));
        if (isServiceToBeStopped()) stopSelf();
    }

    private void startSyncByAnotherAppl(Intent in) {
        mUtil.addLogMsg("I", mContext.getString(R.string.msgs_svc_received_start_request_from_external));
        Bundle bundle = in.getExtras();
        if (bundle != null) {
            if (bundle.containsKey(SMBSYNC2_EXTRA_PARM_SYNC_PROFILE)) {
                if (bundle.get(SMBSYNC2_EXTRA_PARM_SYNC_PROFILE).getClass().getSimpleName().equals("String")) {
                    String t_sp = bundle.getString(SMBSYNC2_EXTRA_PARM_SYNC_PROFILE);
                    String[] sp = t_sp.split(",");
                    ArrayList<String> pl = new ArrayList<String>();
                    for (int i = 0; i < sp.length; i++) {
                        if (SyncTaskUtil.getSyncTaskByName(mGp.syncTaskList, sp[i]) != null) {
                            pl.add(sp[i]);
                        } else {
                            mUtil.addLogMsg("W",
                                    mContext.getString(R.string.msgs_svc_received_start_request_from_external_task_not_found) + sp[i]);
                            NotificationUtil.showOngoingMsg(mGp, mUtil, 0,
                                    mContext.getString(R.string.msgs_svc_received_start_request_from_external_task_not_found) + sp[i]);
                        }
                    }
                    if (pl.size() > 0) {
                        String[] nspl = new String[pl.size()];
                        for (int i = 0; i < pl.size(); i++) nspl[i] = pl.get(i);
                        queueSpecificSyncTask(nspl, SMBSYNC2_SYNC_REQUEST_EXTERNAL);
//                        if (!mGp.syncThreadActive) {
//                            sendStartNotificationIntent();
//                            startSyncThread();
//                        }
                    } else {
                        mUtil.addLogMsg("W",
                                mContext.getString(R.string.msgs_svc_received_start_request_from_external_no_task_list));
                        NotificationUtil.showOngoingMsg(mGp, mUtil, 0,
                                mContext.getString(R.string.msgs_svc_received_start_request_from_external_no_task_list));
                    }
                } else {
                    NotificationUtil.showOngoingMsg(mGp, mUtil, 0,
                            mContext.getString(R.string.msgs_extra_data_sync_profile_type_error));
                    mUtil.addLogMsg("W", mContext.getString(R.string.msgs_extra_data_sync_profile_type_error));
                }
            } else {
                mUtil.addLogMsg("W",
                        mContext.getString(R.string.msgs_svc_received_start_request_from_external_no_task_specified));
                NotificationUtil.showOngoingMsg(mGp, mUtil, 0,
                        mContext.getString(R.string.msgs_svc_received_start_request_from_external_no_task_specified));
            }
        } else {
            mUtil.addLogMsg("I", mContext.getString(R.string.msgs_svc_received_start_request_from_external_auto_task));
            queueAutoSyncTask(SMBSYNC2_SYNC_REQUEST_EXTERNAL);
            if (!mGp.syncThreadActive) {
                sendStartNotificationIntent();
                startSyncThread();
            }
        }
        if (isServiceToBeStopped()) stopSelf();
    }

    private void startSyncByShortcut(Intent in) {
        mUtil.addLogMsg("I", mContext.getString(R.string.msgs_svc_received_start_request_from_shortcut));
        queueAutoSyncTask(SMBSYNC2_SYNC_REQUEST_SHORTCUT);
        if (!mGp.syncThreadActive) {
            sendStartNotificationIntent();
            startSyncThread();
        }
        if (isServiceToBeStopped()) stopSelf();
    }

    final private ISvcClient.Stub mSvcClientStub = new ISvcClient.Stub() {
        @Override
        public void setCallBack(ISvcCallback callback)
                throws RemoteException {
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            mGp.callbackStub = callback;
        }

        @Override
        public void removeCallBack(ISvcCallback callback)
                throws RemoteException {
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            mGp.callbackStub = null;
        }

        @Override
        public void aidlConfirmReply(int confirmed)
                throws RemoteException {
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered, confirmed=" + confirmed);
            synchronized (mGp.syncThreadConfirm) {
                mGp.syncThreadConfirm.setExtraDataInt(confirmed);
                mGp.syncThreadConfirm.notify();
            }
        }

        @Override
        public void aidlStopService() throws RemoteException {
            stopSelf();
        }

        @Override
        public void aidlStartSpecificSyncTask(String[] job_name) throws RemoteException {
//            Thread.dumpStack();
            queueSpecificSyncTask(job_name, SMBSYNC2_SYNC_REQUEST_ACTIVITY);
//            if (!mGp.syncThreadActive) {
//                sendStartNotificationIntent();
//                startSyncThread();
//            }
        }

        @Override
        public void aidlStartAutoSyncTask() throws RemoteException {
            queueAutoSyncTask(SMBSYNC2_SYNC_REQUEST_ACTIVITY);
            if (!mGp.syncThreadActive) {
                sendStartNotificationIntent();
                startSyncThread();
            }
        }

        @Override
        public void aidlCancelSyncTask() throws RemoteException {
            cancelSyncTask();
        }

        @Override
        public void aidlReloadTaskList() throws RemoteException {

        }

        @Override
        public void aidlSetActivityInBackground() throws RemoteException {
            setActivityBackground();
        }

        @Override
        public void aidlSetActivityInForeground() throws RemoteException {
            setActivityForeground();
        }

    };

    private void setActivityForeground() {
        mGp.activityIsBackground = false;
        NotificationUtil.setNotificationEnabled(mGp, false);
        stopForeground(true);
        NotificationUtil.clearNotification(mGp, mUtil);
    }

    private void setActivityBackground() {
        mGp.activityIsBackground = true;
        NotificationUtil.setNotificationEnabled(mGp, true);
        if (mGp.syncThreadActive) startForeground(R.string.app_name, mGp.notification);
    }

    private void cancelSyncTask() {
        synchronized (mGp.syncThreadCtrl) {
            mGp.syncThreadCtrl.setDisabled();
            mGp.syncThreadCtrl.notify();
        }
    }

    private SyncTaskItem getSyncTask(String job_name) {
        for (SyncTaskItem sji : mGp.syncTaskList) {
            if (sji.getSyncTaskName().equals(job_name)) {
                return sji;
            }
        }
        return null;
    }

    private boolean isSyncTaskAlreadyScheduled(ArrayBlockingQueue<SyncRequestItem>srq, String task_name) {
        boolean result=false;
        for(SyncRequestItem sri:srq) {
            for(SyncTaskItem sti:sri.sync_task_list) {
                if (sti.getSyncTaskName().equals(task_name)) {
                    result=true;
                    break;
                }
            }
            if (result) break;
        }
        return result;
    }

    private void queueSpecificSyncTask(String job_name[], String req_id, ScheduleItem si) {
        SyncRequestItem sri = new SyncRequestItem();
        sri.schedule_name=si.scheduleName;
        sri.wifi_off_after_sync_ended = si.syncWifiOffAfterEnd;
        sri.wifi_on_before_sync_start = si.syncWifiOnBeforeStart;
        sri.start_delay_time_after_wifi_on = si.syncDelayAfterWifiOn;
        sri.overrideSyncOptionCharge=si.syncOverrideOptionCharge;

        sri.request_id = req_id;
        if (sri.request_id.equals(SMBSYNC2_SYNC_REQUEST_ACTIVITY)) sri.request_id_display=mContext.getString(R.string.msgs_svc_received_start_sync_task_request_intent_activity);
        else if (sri.request_id.equals(SMBSYNC2_SYNC_REQUEST_EXTERNAL)) sri.request_id_display=mContext.getString(R.string.msgs_svc_received_start_sync_task_request_intent_external);
        else if (sri.request_id.equals(SMBSYNC2_SYNC_REQUEST_SHORTCUT)) sri.request_id_display=mContext.getString(R.string.msgs_svc_received_start_sync_task_request_intent_shortcut);
        else if (sri.request_id.equals(SMBSYNC2_SYNC_REQUEST_SCHEDULE)) sri.request_id_display=mContext.getString(R.string.msgs_svc_received_start_sync_task_request_intent_schedule);
        else sri.request_id_display="";

        if (job_name != null && job_name.length > 0) {
            for (int i = 0; i < job_name.length; i++) {
                if (getSyncTask(job_name[i]) != null) {
                    if (!getSyncTask(job_name[i]).isSyncTaskError()) {
                        if (isSyncTaskAlreadyScheduled(mGp.syncRequestQueue, job_name[i])) {
                            if (si.scheduleName.equals("")) {
                                mUtil.addLogMsg("W", String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_already_task_queued),
                                        job_name[i], sri.request_id_display));
                            } else {
                                mUtil.addLogMsg("W", String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_already_task_queued_schedule),
                                        sri.schedule_name, job_name[i], sri.request_id_display));
                            }
                        } else {
                            sri.sync_task_list.add(getSyncTask(job_name[i]).clone());
                            if (si.scheduleName.equals("")) {
                                mUtil.addLogMsg("I", String.format(mContext.getString(R.string.msgs_svc_received_start_sync_task_request_accepted),
                                        job_name[i], sri.request_id_display));
                            } else {
                                mUtil.addLogMsg("I", String.format(mContext.getString(R.string.msgs_svc_received_start_sync_task_request_accepted_schedule),
                                        sri.schedule_name, job_name[i], sri.request_id_display));
                            }
                        }
                    } else {
                        mUtil.addLogMsg("W",
                                String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_task_is_error),
                                        job_name[0], sri.request_id_display));
                    }
                } else {
                    mUtil.addLogMsg("W", mContext.getString(R.string.msgs_main_sync_selected_task_not_found) + job_name[i]);
                }
            }
            if (sri.sync_task_list.size() > 0) {
                mGp.syncRequestQueue.add(sri);
            } else {
//                mUtil.addLogMsg("E", mContext.getString(R.string.msgs_main_sync_specified_sync_task_not_scheduled));
            }
        } else {
//            mUtil.addLogMsg("E", mContext.getString(R.string.msgs_main_sync_specified_sync_task_not_scheduled));
        }
    }

    private void queueSpecificSyncTask(String job_name[], String req_id) {
        ScheduleItem si=new ScheduleItem();
        si.scheduleName="";
        si.syncWifiOnBeforeStart=false;
        si.syncDelayAfterWifiOn=0;
        si.syncWifiOffAfterEnd=false;
        si.syncOverrideOptionCharge=ScheduleItem.OVERRIDE_SYNC_OPTION_DO_NOT_CHANGE;
        queueSpecificSyncTask(job_name, req_id, si);
        if (!mGp.syncThreadActive) {
            sendStartNotificationIntent();
            startSyncThread();
        }
    }

    private void queueAutoSyncTask(String req_id, ScheduleItem si) {
        int cnt = 0;
        SyncRequestItem sri = new SyncRequestItem();
        sri.schedule_name=si.scheduleName;
        sri.wifi_off_after_sync_ended = si.syncWifiOffAfterEnd;
        sri.wifi_on_before_sync_start = si.syncWifiOnBeforeStart;
        sri.start_delay_time_after_wifi_on = si.syncDelayAfterWifiOn;
        sri.overrideSyncOptionCharge=si.syncOverrideOptionCharge;

        sri.request_id = req_id;
        if (sri.request_id.equals(SMBSYNC2_SYNC_REQUEST_ACTIVITY)) sri.request_id_display=mContext.getString(R.string.msgs_svc_received_start_sync_task_request_intent_activity);
        else if (sri.request_id.equals(SMBSYNC2_SYNC_REQUEST_EXTERNAL)) sri.request_id_display=mContext.getString(R.string.msgs_svc_received_start_sync_task_request_intent_external);
        else if (sri.request_id.equals(SMBSYNC2_SYNC_REQUEST_SHORTCUT)) sri.request_id_display=mContext.getString(R.string.msgs_svc_received_start_sync_task_request_intent_shortcut);
        else if (sri.request_id.equals(SMBSYNC2_SYNC_REQUEST_SCHEDULE)) sri.request_id_display=mContext.getString(R.string.msgs_svc_received_start_sync_task_request_intent_schedule);
        else sri.request_id_display="";

        synchronized (mGp.syncRequestQueue) {
            for (SyncTaskItem sji : mGp.syncTaskList) {
                if (sji.isSyncTaskAuto() && !sji.isSyncTestMode()) {
                    String[] job_name=new String[]{sji.getSyncTaskName()};
                    if (!sji.isSyncTaskError()) {
                        if (isSyncTaskAlreadyScheduled(mGp.syncRequestQueue, job_name[0])) {
                            mUtil.addLogMsg("W",
                                    String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_already_task_queued_schedule),
                                            sri.schedule_name, job_name[0], sri.request_id_display));
                        } else {
                            cnt++;
                            mUtil.addLogMsg("I",
                                    String.format(mContext.getString(R.string.msgs_svc_received_start_sync_task_request_accepted_schedule),
                                            sri.schedule_name, job_name[0], sri.request_id_display));
                            sri.sync_task_list.add(sji.clone());
                        }
                    } else {
                        mUtil.addLogMsg("W",
                                String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_task_is_error),
                                        job_name[0], sri.request_id_display));
                    }
                }
            }
            if (cnt == 0) {
                mUtil.addLogMsg("E", mContext.getString(R.string.msgs_active_sync_prof_not_found));
                NotificationUtil.showOngoingMsg(mGp, mUtil, System.currentTimeMillis(),
                        mContext.getString(R.string.msgs_active_sync_prof_not_found));
            } else {
                mGp.syncRequestQueue.add(sri);
            }
        }
    }

    private void queueAutoSyncTask(String req_id) {
        ScheduleItem si=new ScheduleItem();
        si.scheduleName="";
        si.syncWifiOnBeforeStart=false;
        si.syncDelayAfterWifiOn=0;
        si.syncWifiOffAfterEnd=false;
        si.syncOverrideOptionCharge=ScheduleItem.OVERRIDE_SYNC_OPTION_DO_NOT_CHANGE;
        queueAutoSyncTask(req_id, si);
    }

    private void sendEndNotificationIntent(int sync_result) {
        Intent in = new Intent(SMBSYNC2_SYNC_ENDED);
        String rc="";
        if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) rc="SUCCESS";
        else if (sync_result==SyncTaskItem.SYNC_STATUS_ERROR) rc="ERROR";
        else if (sync_result==SyncTaskItem.SYNC_STATUS_CANCEL) rc="CANCEL";
        else if (sync_result==SyncTaskItem.SYNC_STATUS_WARNING) rc="WARNING";
        in.putExtra("SYNC_RESULT",rc);
        sendBroadcast(in, null);
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " Send end boradcast intent");
    }

    private void sendStartNotificationIntent() {
        Intent in = new Intent(SMBSYNC2_SYNC_STARTED);
        sendBroadcast(in, null);
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " Send start boradcast intent");
    }

    private void startSyncThread() {
//		final Handler hndl=new Handler();
//        Thread.dumpStack();
        if (!mGp.syncThreadEnabled) {
            mUtil.addLogMsg("W", mContext.getString(R.string.msgs_svc_can_not_start_sync_task_disabled));
            return;
        }
        if (NotificationUtil.isNotificationEnabled(mGp))
            startForeground(R.string.app_name, mGp.notification);
        if (mGp.syncRequestQueue.size() > 0) {
            mGp.acquireWakeLock(mContext, mUtil);
            NotifyEvent ntfy = new NotifyEvent(mContext);
            ntfy.setListener(new NotifyEventListener() {
                @Override
                public void positiveResponse(Context c, Object[] o) {
                    mSyncThreadResult = (int) o[0];
                    cancelHeartBeat();
                    mGp.releaseWakeLock(mUtil);
                    hideDialogWindow();
                    synchronized (mGp.syncRequestQueue) {
                        if (mGp.syncRequestQueue.size() > 0) {
                            showSyncEndNotificationMessage();
                            startSyncThread();
                        } else {
                            sendEndNotificationIntent(mSyncThreadResult);
                            if (mGp.callbackStub == null) {
                                stopSelf();
                            } else {
                                stopForeground(true);
                                showSyncEndNotificationMessage();
                                mGp.notificationLastShowedMessage = "";
                            }
                        }
                    }
                }

                @Override
                public void negativeResponse(Context c, Object[] o) {
                    mSyncThreadResult = SyncTaskItem.SYNC_STATUS_ERROR;
                    cancelHeartBeat();
                    mGp.releaseWakeLock(mUtil);
                    hideDialogWindow();
                    synchronized (mGp.syncRequestQueue) {
                        sendEndNotificationIntent(mSyncThreadResult);
                        mGp.syncRequestQueue.clear();
                        if (mGp.callbackStub == null) {
                            stopSelf();
                        } else {
                            stopForeground(true);
                            showSyncEndNotificationMessage();
                            mGp.notificationLastShowedMessage = "";
                        }
                    }
                }
            });

            showDialogWindow();

            Thread tm = new SyncThread(mContext, mGp, ntfy);
            tm.setName("SyncThread");
            tm.setPriority(Thread.MIN_PRIORITY);
            tm.start();

            cancelHeartBeat();
            setHeartBeat();
        } else {
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " task has not started, queued task does not exist");
            stopForeground(true);
        }
    }

    private int mSyncThreadResult = 0;

    private void showSyncEndNotificationMessage() {
        boolean sound=false, vibration=false;
        if (mSyncThreadResult == SyncTaskItem.SYNC_STATUS_SUCCESS) {
            if (mGp.settingRingtoneWhenSyncEnded.equals(SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS) ||
                    mGp.settingRingtoneWhenSyncEnded.equals(SMBSYNC2_RINGTONE_NOTIFICATION_SUCCESS)) sound=true;
            if (mGp.settingVibrateWhenSyncEnded.equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS) ||
                    mGp.settingVibrateWhenSyncEnded.equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_SUCCESS)) vibration=true;
        } else if (mSyncThreadResult == SyncTaskItem.SYNC_STATUS_CANCEL) {
            if (mGp.settingRingtoneWhenSyncEnded.equals(SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS)) sound=true;
            if (mGp.settingVibrateWhenSyncEnded.equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS)) vibration=true;
        } else if (mSyncThreadResult == SyncTaskItem.SYNC_STATUS_ERROR) {
            if (mGp.settingRingtoneWhenSyncEnded.equals(SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS) ||
                    mGp.settingRingtoneWhenSyncEnded.equals(SMBSYNC2_RINGTONE_NOTIFICATION_ERROR)) sound=true;
            if (mGp.settingVibrateWhenSyncEnded.equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS) ||
                    mGp.settingVibrateWhenSyncEnded.equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ERROR)) vibration=true;
        }
        boolean is_notice_message_showed=false;
        if (mGp.activityIsBackground) {
            if (mSyncThreadResult == SyncTaskItem.SYNC_STATUS_SUCCESS || mSyncThreadResult == SyncTaskItem.SYNC_STATUS_CANCEL) {
                if (mGp.settingNotificationMessageWhenSyncEnded.equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS) ||
                        mGp.settingNotificationMessageWhenSyncEnded.equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_SUCCESS)) {
                    NotificationUtil.showNoticeMsg(mContext, mGp, mUtil, mGp.notificationLastShowedMessage, sound, vibration);
                    is_notice_message_showed=true;
                }
            } else if (mSyncThreadResult == SyncTaskItem.SYNC_STATUS_ERROR) {
                if (mGp.settingNotificationMessageWhenSyncEnded.equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS) ||
                        mGp.settingNotificationMessageWhenSyncEnded.equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ERROR)) {
                    NotificationUtil.showNoticeMsg(mContext, mGp, mUtil, mGp.notificationLastShowedMessage, sound, vibration);
                    is_notice_message_showed=true;
                }
            }
        }
//        if (mGp.callbackStub != null && sound) playBackDefaultNotification();
//        if (mGp.callbackStub != null && vibration) vibrateDefaultPattern();
        if (!is_notice_message_showed) {
            Intent na=new Intent(mContext, ActivityNotification.class);
            na.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            na.putExtra("SOUND", sound);
            na.putExtra("SOUND_VOLUME", mGp.settingNotificationVolume);
            na.putExtra("VIBRATE", vibration);
            startActivity(na);
        }
    }

//    private void playBackDefaultNotification() {
//        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        if (uri != null) {
//            final MediaPlayer player = MediaPlayer.create(mGp.appContext, uri);
//            if (player != null) {
//                float vol = (float) mGp.settingNotificationVolume / 100.0f;
//                player.setVolume(vol, vol);
//                if (player != null) {
//                    final Thread th = new Thread() {
//                        @Override
//                        public void run() {
//                            int dur = player.getDuration();
//                            player.start();
//                            SystemClock.sleep(dur + 10);
//                            player.stop();
//                            player.reset();
//                            player.release();
//                        }
//                    };
//                    th.setPriority(Thread.MAX_PRIORITY);
//                    th.start();
//                }
//            } else {
//                mUtil.addLogMsg("I", "Default notification can not playback, because default playback is not initialized.");
//            }
//        }
//    }
//
//    private void vibrateDefaultPattern() {
//        Thread th = new Thread() {
//            @SuppressWarnings("deprecation")
//            @Override
//            public void run() {
//                Vibrator vibrator = (Vibrator) mGp.appContext.getSystemService(Context.VIBRATOR_SERVICE);
//                vibrator.vibrate(new long[]{0, 300, 200, 300}, -1);
//            }
//        };
//        th.setPriority(Thread.MAX_PRIORITY);
//        th.start();
//    }

    private void showDialogWindow() {
        mGp.dialogWindowShowed = true;
        try {
            if (mGp.callbackStub != null) mGp.callbackStub.cbThreadStarted();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void hideDialogWindow() {
        mGp.dialogWindowShowed = false;
        try {
            if (mGp.callbackStub != null) mGp.callbackStub.cbThreadEnded();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initWifiStatus() {
        mGp.wifiIsActive = mWifiMgr.isWifiEnabled();
        if (mGp.wifiIsActive) {
            mGp.wifiSsid = mUtil.getConnectedWifiSsid();
        }
        mUtil.addDebugMsg(1, "I", "Wi-Fi Status, Active=" + mGp.wifiIsActive + ", SSID=" + mGp.wifiSsid);
    }

    final private class WifiReceiver extends BroadcastReceiver {
        @Override
        final public void onReceive(Context c, Intent in) {
            String tssid =null;
            try {
                tssid=mWifiMgr.getConnectionInfo().getSSID();
            } catch(Exception e){
                mUtil.addLogMsg("W", "WIFI receiver, getSSID() failed. msg="+e.getMessage());
            }
            String wssid = "";
            String ss = "";
            try {
                ss=mWifiMgr.getConnectionInfo().getSupplicantState().toString();
//                mUtil.addDebugMsg(1,"I","ss="+ss+", IP addr="+CommonUtilities.getIfIpAddress("wlan0"));
//                mUtil.addDebugMsg(1,"I","ss="+ss+", IP addr="+String.format("%8x",mWifiMgr.getConnectionInfo().getIpAddress()));
            } catch(Exception e){
                mUtil.addLogMsg("W", "WIFI receiver, getSupplicantState() failed. msg="+e.getMessage());
            }
            if (tssid == null || tssid.equals("<unknown ssid>")) wssid = "";
            else wssid = tssid.replaceAll("\"", "");
            if (wssid.equals("0x")) wssid = "";

            boolean new_wifi_enabled = mWifiMgr.isWifiEnabled();
            if (!new_wifi_enabled && mGp.wifiIsActive) {
                mUtil.addDebugMsg(1, "I", "WIFI receiver, WIFI Off");
                mGp.wifiSsid = "";
                mGp.wifiIsActive = false;
            } else {
                if (ss.equals("COMPLETED") || ss.equals("ASSOCIATING") || ss.equals("ASSOCIATED")) {
                    if (mGp.wifiSsid.equals("") && !wssid.equals("")) {
                        mUtil.addDebugMsg(1, "I", "WIFI receiver, Connected WIFI Access point SSID=" + wssid);
                        mGp.wifiSsid = wssid;
                        mGp.wifiIsActive = true;
                    }
                } else if (ss.equals("INACTIVE") ||
                        ss.equals("DISCONNECTED") ||
                        ss.equals("UNINITIALIZED") ||
                        ss.equals("INTERFACE_DISABLED") ||
                        ss.equals("SCANNING")) {
                    if (mGp.wifiIsActive) {
                        if (!mGp.wifiSsid.equals("")) {
                            mUtil.addDebugMsg(1, "I", "WIFI receiver, Disconnected WIFI Access point SSID=" + mGp.wifiSsid);
                            mGp.wifiSsid = "";
                            mGp.wifiIsActive = true;
                        }
                    } else {
                        if (new_wifi_enabled) {
                            mUtil.addDebugMsg(1, "I", "WIFI receiver, WIFI On");
                            mGp.wifiSsid = "";
                            mGp.wifiIsActive = true;
                        }
                    }
                }
            }
        }
    }

    final private class SleepReceiver extends BroadcastReceiver {
        @SuppressLint({"Wakelock", "NewApi"})
        @Override
        final public void onReceive(Context c, Intent in) {
            String action = in.getAction();
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
//                Log.v("Sync","force="+mGp.settingScreenOnIfScreenOnAtStartOfSync+", thread="+mGp.syncThreadActive+", wait="+mGp.syncThreadConfirmWait);
                if (mGp.settingScreenOnIfScreenOnAtStartOfSync && mGp.syncThreadActive && !mGp.syncThreadConfirmWait) {
                    if (mGp.forceDimScreenWakelock.isHeld()) mGp.forceDimScreenWakelock.release();
                    mGp.forceDimScreenWakelock.acquire();
                    mUtil.addDebugMsg(1, "I", "Sleep receiver, ForceDim wake lock acquired");
                }
            } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                if (mGp.settingPreventSyncStartDelay && mGp.syncThreadActive && !mGp.syncThreadConfirmWait) {
                    if (!mGp.mDimWakeLock.isHeld()) {
                        mGp.mDimWakeLock.acquire();
                        mUtil.addDebugMsg(1, "I", "Sleep receiver, Dim wake lock acquired");
                    }
                }
            }
        }
    }

}