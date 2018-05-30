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

import static com.sentaroh.android.SMBSync2.Constants.*;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.*;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

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
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;

import com.sentaroh.android.SMBSync2.Log.LogUtil;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;

@SuppressLint("Wakelock")
public class SyncService extends Service {
    private GlobalParameters mGp = null;

    private SyncUtil mUtil = null;

    private WifiManager mWifiMgr = null;

    private Context mContext = null;

    private WifiReceiver mWifiReceiver = new WifiReceiver();

    private SleepReceiver mSleepReceiver = new SleepReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mGp= GlobalWorkArea.getGlobalParameters(mContext);
        mGp.safMgr.loadSafFile();

        NotificationUtil.initNotification(mGp);
        NotificationUtil.clearNotification(mGp);
        mUtil = new SyncUtil(getApplicationContext(), "Service", mGp);

        mUtil.addDebugMsg(1, "I", "onCreate entered");

        if (mGp.msgList.size() == 0) {
            mUtil.addLogMsg("I", mContext.getString(R.string.msgs_smbsync_main_start) +
                    " API=" + Build.VERSION.SDK_INT +
                    ", Version " + getApplVersionName());
        }

        if (mGp.syncTaskList == null)
            mGp.syncTaskList = SyncTaskUtil.createSyncTaskList(mContext, mGp, mUtil);

        if (mGp.syncHistoryList == null)
            mGp.syncHistoryList = mUtil.loadHistoryList();

//        NotificationUtil.setNotificationIcon(mGp, R.drawable.ic_48_smbsync_wait, R.drawable.ic_48_smbsync_wait);

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
            startSyncByScheduler(intent);
        } else if (action.equals(SMBSYNC2_START_SYNC_INTENT)) {
            mUtil.addDebugMsg(1, "I", "onStartCommand entered, action=" + action);
            startSyncByAnotherAppl(intent);
        } else if (action.equals(SMBSYNC2_AUTO_SYNC_INTENT)) {
            mUtil.addDebugMsg(1, "I", "onStartCommand entered, action=" + action);
            startSyncByShortcut(intent);
        } else if (action.equals(SMBSYNC2_SERVICE_HEART_BEAT)) {
//			mUtil.addDebugMsg(0,"I","onStartCommand entered, action="+action);
            if (mHeartBeatActive) setHeartBeat();
        } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED) ||
                action.equals(Intent.ACTION_MEDIA_EJECT)) {
            mUtil.addDebugMsg(1, "I", "onStartCommand entered, action=" + action);
            final String sdcard = mGp.safMgr.getSdcardRootPath();
            Thread th = new Thread() {
                @Override
                public void run() {
                    int count = 10;
                    while (count > 0) {
                        mGp.refreshMediaDir();
                        if (//!usb.equals(mGp.safMgr.getUsbFileSystemDirectory()) ||
                                !sdcard.equals(mGp.safMgr.getSdcardRootPath())) {
                            mUtil.addDebugMsg(1, "I", "New media directory, sdcard=" + mGp.safMgr.getSdcardRootPath() );
                            if (mGp.callbackStub != null) {
                                try {
                                    mGp.callbackStub.cbMediaStatusChanged();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                        SystemClock.sleep(500);
                        count--;
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
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered,action=" + intent.getAction());
        setActivityForeground();
        return mSvcClientStub;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered, qs=" + mGp.syncRequestQueue.size());
        if (isServiceToBeStopped()) stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
        unregisterReceiver(mWifiReceiver);
        unregisterReceiver(mSleepReceiver);
        stopForeground(true);
        if (mGp.notificationLastShowedMessage != null && !mGp.notificationLastShowedMessage.equals("")) {
            showSyncEndNotificationMessage();
            mGp.notificationLastShowedMessage = null;
        }
        cancelHeartBeat();
        mGp.releaseWakeLock(mUtil);
        LogUtil.closeLog(mContext, mGp);
        NotificationUtil.setNotificationEnabled(mGp, true);
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

    private void startSyncByScheduler(Intent in) {
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");

        mUtil.addLogMsg("I", mContext.getString(R.string.msgs_svc_received_start_request_from_scheduler));

        ArrayList<ScheduleItem> scheduleInfoList = ScheduleUtil.loadScheduleData(mGp);
        if (in.getExtras().containsKey(SCHEDULER_SCHEDULE_NAME_KEY)) {
            String schedule_name_list = in.getStringExtra(SCHEDULER_SCHEDULE_NAME_KEY);

            mUtil.addDebugMsg(1,"I", "Schedule information, name=" + schedule_name_list);

            String[] schedule_list=schedule_name_list.split(",");

            for(String schedule_name:schedule_list) {
                mUtil.addLogMsg("I", "Schedule start, name=" + schedule_name);
                ScheduleItem si = getScheduleInformation(scheduleInfoList, schedule_name);
                if (si!=null) {
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
                            queueSpecificSyncTask(n_pl, SMBSYNC2_SYNC_REQUEST_SCHEDULE,
                                    si.syncWifiOnBeforeStart, si.syncDelayAfterWifiOn, si.syncWifiOffAfterEnd);
                        } else {
                            mUtil.addLogMsg("E", mContext.getString(R.string.msgs_svc_received_start_request_from_scheduler_no_task_list));
                        }
                    } else {
                        queueAutoSyncTask(SMBSYNC2_SYNC_REQUEST_SCHEDULE,
                                si.syncWifiOnBeforeStart, si.syncDelayAfterWifiOn, si.syncWifiOffAfterEnd);
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
        mUtil.addLogMsg("I", mContext.getString(R.string.msgs_svc_received_start_request_from_scheduler));
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
                            NotificationUtil.showOngoingMsg(mGp, 0,
                                    mContext.getString(R.string.msgs_svc_received_start_request_from_external_task_not_found) + sp[i]);
                        }
                    }
                    if (pl.size() > 0) {
                        String[] nspl = new String[pl.size()];
                        for (int i = 0; i < pl.size(); i++) nspl[i] = pl.get(i);
                        queueSpecificSyncTask(nspl, SMBSYNC2_SYNC_REQUEST_EXTERNAL);
                        if (!mGp.syncThreadActive) {
                            sendStartNotificationIntent();
                            startSyncThread();
                        }
                    } else {
                        mUtil.addLogMsg("W",
                                mContext.getString(R.string.msgs_svc_received_start_request_from_external_no_task_list));
                        NotificationUtil.showOngoingMsg(mGp, 0,
                                mContext.getString(R.string.msgs_svc_received_start_request_from_external_no_task_list));
                    }
                } else {
                    NotificationUtil.showOngoingMsg(mGp, 0,
                            mContext.getString(R.string.msgs_extra_data_sync_profile_type_error));
                    mUtil.addLogMsg("W", mContext.getString(R.string.msgs_extra_data_sync_profile_type_error));
                }
            } else {
                mUtil.addLogMsg("W",
                        mContext.getString(R.string.msgs_svc_received_start_request_from_external_no_task_specified));
                NotificationUtil.showOngoingMsg(mGp, 0,
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
            mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
            mGp.callbackStub = callback;
        }

        @Override
        public void removeCallBack(ISvcCallback callback)
                throws RemoteException {
            mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
            mGp.callbackStub = null;
        }

        @Override
        public void aidlConfirmReply(int confirmed)
                throws RemoteException {
            mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered, confirmed=" + confirmed);
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
            queueSpecificSyncTask(job_name, SMBSYNC2_SYNC_REQUEST_ACTIVITY);
            if (!mGp.syncThreadActive) {
                sendStartNotificationIntent();
                startSyncThread();
            }
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
        NotificationUtil.clearNotification(mGp);
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

    private void queueSpecificSyncTask(String job_name[], String req_id, boolean wifi_on, int delay_time, boolean wifi_off) {
        SyncRequestItem sri = new SyncRequestItem();
        sri.request_id = req_id;
        sri.wifi_off_after_sync_ended = wifi_off;
        sri.wifi_on_before_sync_start = wifi_on;
        sri.start_delay_time_after_wifi_on = delay_time;
        if (job_name != null && job_name.length > 0) {
            for (int i = 0; i < job_name.length; i++) {
                if (getSyncTask(job_name[i]) != null) {
                    if (isSyncTaskAlreadyScheduled(mGp.syncRequestQueue, job_name[i])) {
                        mUtil.addLogMsg("W", "Sync task was ignored because Sync task was already queued, Sync task=" + job_name[i] + ", Requestor=" + req_id);
                    } else {
                        sri.sync_task_list.add(getSyncTask(job_name[i]).clone());
                        mUtil.addLogMsg("I", "Sync task was queued, Sync task=" + job_name[i] + ", Requestor=" + req_id);
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
        queueSpecificSyncTask(job_name, req_id, false, 0, false);
        if (!mGp.syncThreadActive) {
            sendStartNotificationIntent();
            startSyncThread();
        }
    }

    private void queueAutoSyncTask(String req_id,
                                   boolean wifi_on, int delay_time, boolean wifi_off) {
        int cnt = 0;
        SyncRequestItem sri = new SyncRequestItem();
        sri.request_id = req_id;
        sri.wifi_off_after_sync_ended = wifi_off;
        sri.wifi_on_before_sync_start = wifi_on;
        sri.start_delay_time_after_wifi_on = delay_time;
        synchronized (mGp.syncRequestQueue) {
            for (SyncTaskItem sji : mGp.syncTaskList) {
                if (sji.isSyncTaskAuto() && !sji.isSyncTestMode()) {
                    String[] job_name=new String[]{sji.getSyncTaskName()};
                    if (isSyncTaskAlreadyScheduled(mGp.syncRequestQueue, job_name[0])) {
                        mUtil.addLogMsg("W", "Sync task was ignored because Sync task was already queued, job=" + job_name[0] + ", Requestor=" + req_id);
                    } else {
                        cnt++;
                        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " job was queued, job=" + sji.getSyncTaskName() + ", Requestor=" + req_id);
                        sri.sync_task_list.add(sji.clone());
                    }
                }
            }
            if (cnt == 0) {
                mUtil.addLogMsg("E", mContext.getString(R.string.msgs_active_sync_prof_not_found));
                NotificationUtil.showOngoingMsg(mGp, System.currentTimeMillis(),
                        mContext.getString(R.string.msgs_active_sync_prof_not_found));
            } else {
                mGp.syncRequestQueue.add(sri);
            }
        }
    }

    private void queueAutoSyncTask(String req_id) {
        queueAutoSyncTask(req_id, false, 0, false);
    }

    private void sendEndNotificationIntent() {
        Intent in = new Intent(SMBSYNC2_SYNC_ENDED);
        sendBroadcast(in, null);
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " Send end boradcast intent");
    }

    private void sendStartNotificationIntent() {
        Intent in = new Intent(SMBSYNC2_SYNC_STARTED);
        sendBroadcast(in, null);
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " Send start boradcast intent");
    }

    private void startSyncThread() {
//		final Handler hndl=new Handler();
        if (!mGp.syncThreadEnabled) {
            mUtil.addLogMsg("W", mContext.getString(R.string.msgs_svc_can_not_start_sync_task_disabled));
            return;
        }
        if (mGp.syncRequestQueue.size() > 0) {
            if (NotificationUtil.isNotificationEnabled(mGp))
                startForeground(R.string.app_name, mGp.notification);
            mGp.acquireWakeLock(mUtil);
            NotifyEvent ntfy = new NotifyEvent(this);
            ntfy.setListener(new NotifyEventListener() {
                @Override
                public void positiveResponse(Context c, Object[] o) {
                    mSyncThreadResult = (int) o[0];
                    cancelHeartBeat();
                    mGp.releaseWakeLock(mUtil);
                    hideDialogWindow();
                    synchronized (mGp.syncRequestQueue) {
                        if (mGp.syncRequestQueue.size() > 0) {
                            startSyncThread();
                        } else {
                            sendEndNotificationIntent();
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
                        sendEndNotificationIntent();
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

            Thread tm = new SyncThread(mGp, ntfy);
            tm.setName("SyncThread");
            tm.setPriority(Thread.MIN_PRIORITY);
            tm.start();

            cancelHeartBeat();
            setHeartBeat();
        } else {
            mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " task has not started, queued task does not exist");
        }
    }

    private int mSyncThreadResult = 0;

    private void showSyncEndNotificationMessage() {
//		Log.v("","statue="+mSyncThreadResult+", option="+mGp.settingNotificationMessageWhenSyncEnded);
        if (mSyncThreadResult == SyncTaskItem.SYNC_STATUS_SUCCESS) {
            if (mGp.settingNotificationMessageWhenSyncEnded.equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS) ||
                    mGp.settingNotificationMessageWhenSyncEnded.equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_SUCCESS)) {
                NotificationUtil.showNoticeMsg(mContext, mGp, mGp.notificationLastShowedMessage);
            }
        } else if (mSyncThreadResult == SyncTaskItem.SYNC_STATUS_ERROR) {
            if (mGp.settingNotificationMessageWhenSyncEnded.equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS) ||
                    mGp.settingNotificationMessageWhenSyncEnded.equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ERROR)) {
                NotificationUtil.showNoticeMsg(mContext, mGp, mGp.notificationLastShowedMessage);
            }
        }
    }

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
                        mUtil.addDebugMsg(1, "I", "WIFI receiver, Connected WIFI Access point ssid=" + wssid);
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
                            mUtil.addDebugMsg(1, "I", "WIFI receiver, Disconnected WIFI Access point ssid=" + mGp.wifiSsid);
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
            } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                if (mGp.settingScreenOnWhileSync && mGp.syncThreadActive && !mGp.syncThreadConfirmWait) {
                    if (!mGp.mDimWakeLock.isHeld()) {
                        mGp.mDimWakeLock.acquire();
                        mUtil.addDebugMsg(1, "I", "Sleep receiver, Dim wake lock acquired");
                    }
                }
            }
        }
    }

}