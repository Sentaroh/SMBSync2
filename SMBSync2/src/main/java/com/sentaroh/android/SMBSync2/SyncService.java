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

//	private final String SERVICE_STATUS_FILE_NAME="service_status_file";


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mGp= GlobalWorkArea.getGlobalParameters(mContext);
//        mGp = (GlobalParameters) getApplicationContext();//getApplication();
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

        NotificationUtil.setNotificationIcon(mGp, R.drawable.ic_48_smbsync_wait, R.drawable.ic_48_smbsync_wait);
//		NotificationUtil.showOngoingMsg(mGp, 0, mContext.getString(R.string.msgs_notification_smbsync_active));
//		startForeground(R.string.app_name, mGp.notification);

        mWifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        initWifiStatus();

//        initUsbDeviceList();

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

//	private boolean isSvcStatusFileExists() {
//		String ofp=mGp.applicationRootDirectory+"/"+SERVICE_STATUS_FILE_NAME;
//		File lf=new File(ofp);
////		Log.v("","exists="+lf.exists());
//		return lf.exists();
//	};
//
//	private void deleteSvcStatusFileExists() {
//		String ofp=mGp.applicationRootDirectory+"/"+SERVICE_STATUS_FILE_NAME;
//		File lf=new File(ofp);
//		lf.delete();
////		Log.v("","deleted"+lf.exists());
//	};
//
//	private void createSvcStatusFile() {
//		String ofp=mGp.applicationRootDirectory+"/"+SERVICE_STATUS_FILE_NAME;
//		File lf=new File(mGp.applicationRootDirectory);
//		if (!lf.exists()) lf.mkdir();
//		lf=new File(ofp);
//		if (!lf.exists()) {
//			try {
//				lf.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
////		Log.v("","create="+lf.exists());
//	};

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

//	WakeLock wake_lock=null;

    private boolean mHeartBeatActive = false;

    @SuppressLint("NewApi")
    private void setHeartBeat() {
        if (Build.VERSION.SDK_INT >= 21) {
            if (Build.VERSION.SDK_INT >= 23) {
                Intent intent = new Intent();
                String packageName = mContext.getPackageName();
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                if (pm.isIgnoringBatteryOptimizations(packageName)) {
                    mUtil.addDebugMsg(1, "I", "HeartBeat cancelled because ignoring battery optimizations enabled");
                    return;
                }
            }
//            mUtil.addDebugMsg(1,"I", "HeartBeat started");
            mHeartBeatActive = true;
//			Thread.dumpStack();
            long time = System.currentTimeMillis() + 1000 * 300;
//			Intent in = new Intent(mContext, SyncService.class);
            Intent in = new Intent();
            in.setAction(SMBSYNC2_SERVICE_HEART_BEAT);
            PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
//			PendingIntent pi = PendingIntent.getService(mContext, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= 23)
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
            else am.set(AlarmManager.RTC_WAKEUP, time, pi);
        }
    }

    private void cancelHeartBeat() {
        mHeartBeatActive = false;
//		Thread.dumpStack();
//		Intent in = new Intent(mContext, SyncService.class);
        Intent in = new Intent();
        in.setAction(SMBSYNC2_SERVICE_HEART_BEAT);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
//		PendingIntent pi = PendingIntent.getService(mContext, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
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
//        } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED) ||
//                action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
//            mUtil.addDebugMsg(1, "I", "onStartCommand entered, action=" + action);
//            initUsbDeviceList();
        } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED) ||
                action.equals(Intent.ACTION_MEDIA_EJECT)) {
            mUtil.addDebugMsg(1, "I", "onStartCommand entered, action=" + action);
            final String sdcard = mGp.safMgr.getSdcardDirectory();
//            final String usb = mGp.safMgr.getUsbFileSystemDirectory();
            Thread th = new Thread() {
                @Override
                public void run() {
                    int count = 10;
                    while (count > 0) {
                        mGp.refreshMediaDir();
                        if (//!usb.equals(mGp.safMgr.getUsbFileSystemDirectory()) ||
                                !sdcard.equals(mGp.safMgr.getSdcardDirectory())) {
                            mUtil.addDebugMsg(1, "I", "New media directory, sdcard=" + mGp.safMgr.getSdcardDirectory() +
                                    ", usb=" + mGp.safMgr.getUsbFileSystemDirectory());
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
//		wl.release();
//		if (isServiceToBeStopped()) stopSelf();
        return START_STICKY;
    }

//    private void initUsbDeviceList() {
//        mGp.safMgr.loadSafFileList();
////		UsbManager um = (UsbManager) getSystemService(Context.USB_SERVICE);
////		if (um!=null) {
//////			RemovableStorageUtil rsu=new RemovableStorageUtil(mContext,true);
////			HashMap<String, UsbDevice> dl = um.getDeviceList();
////			Iterator<UsbDevice> deviceIterator = dl.values().iterator();
////			while(deviceIterator.hasNext()){
////			    UsbDevice device = deviceIterator.next();
////				mUtil.addDebugMsg(1,"I", "id="+device.getDeviceId()+", name="+device.getDeviceName());
////			}
////		}
//    }

    @Override
    public IBinder onBind(Intent intent) {
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered,action=" + intent.getAction());
        setActivityForeground();
//		if (arg0.getAction().equals("MessageConnection")) 
        return mSvcClientStub;
//		else return svcInterface;
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
//			mGp=null;
            System.gc();
        }
    }

    private boolean isServiceToBeStopped() {
//		Log.v("","size="+mGp.syncRequestQueue.size()+", active="+mGp.syncThreadActive+", cb="+mGp.callbackStub);
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
            String schedule_names = in.getStringExtra(SCHEDULER_SCHEDULE_NAME_KEY);

            mUtil.addLogMsg("I", "Schedule information, name=" + schedule_names);

            String[] schedule_list=schedule_names.split(",");

            for(String schedule_name:schedule_list) {
                ScheduleItem si = getScheduleInformation(scheduleInfoList, schedule_name);

                if (si.syncTaskList != null && si.syncTaskList.length() > 0) {
                    String[] pl = si.syncTaskList.split(",");
                    String n_tl = "", sep = "";
                    for (int i = 0; i < pl.length; i++) {
                        if (getSyncTask(pl[i]) != null) {
                            n_tl += sep + pl[i];
                            sep = ",";
//					Log.v("","tl="+n_tl);
                        } else {
                            mUtil.addLogMsg("W",
                                    mContext.getString(R.string.msgs_svc_received_start_request_from_scheduler_task_not_found) + pl[i]);
                        }
                    }
                    if (!n_tl.equals("")) {
//                        if (isDuplicateRequest(mGp, SMBSYNC2_SYNC_REQUEST_SCHEDULE)) {
//                            mUtil.addLogMsg("W",
//                                    String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_duplicate_request),
//                                            SMBSYNC2_SYNC_REQUEST_SCHEDULE));
//                        } else {
//                            String[] n_pl = n_tl.split(",");
//                            queueSpecificSyncTask(n_pl, SMBSYNC2_SYNC_REQUEST_SCHEDULE,
//                                    si.syncWifiOnBeforeStart, si.syncDelayAfterWifiOn, si.syncWifiOffAfterEnd);
//                        }
                        String[] n_pl = n_tl.split(",");
                        queueSpecificSyncTask(n_pl, SMBSYNC2_SYNC_REQUEST_SCHEDULE,
                                si.syncWifiOnBeforeStart, si.syncDelayAfterWifiOn, si.syncWifiOffAfterEnd);
                    } else {
                        mUtil.addLogMsg("E", mContext.getString(R.string.msgs_svc_received_start_request_from_scheduler_no_task_list));
                    }
                } else {
//                    if (isDuplicateRequest(mGp, SMBSYNC2_SYNC_REQUEST_SCHEDULE)) {
//                        mUtil.addLogMsg("W",
//                                String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_duplicate_request),
//                                        SMBSYNC2_SYNC_REQUEST_SCHEDULE));
//                    } else {
//                        queueAutoSyncTask(SMBSYNC2_SYNC_REQUEST_SCHEDULE,
//                                si.syncWifiOnBeforeStart, si.syncDelayAfterWifiOn, si.syncWifiOffAfterEnd);
//                    }
                    queueAutoSyncTask(SMBSYNC2_SYNC_REQUEST_SCHEDULE,
                            si.syncWifiOnBeforeStart, si.syncDelayAfterWifiOn, si.syncWifiOffAfterEnd);
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
//                        if (isDuplicateRequest(mGp, SMBSYNC2_SYNC_REQUEST_EXTERNAL)) {
//                            mUtil.addLogMsg("W",
//                                    String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_duplicate_request),
//                                            SMBSYNC2_SYNC_REQUEST_EXTERNAL));
//                        } else {
//                            queueSpecificSyncTask(nspl, SMBSYNC2_SYNC_REQUEST_EXTERNAL);
//                            if (!mGp.syncThreadActive) {
//                                sendStartNotificationIntent();
//                                startSyncThread();
//                            }
//                        }
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
//            if (isDuplicateRequest(mGp, SMBSYNC2_SYNC_REQUEST_EXTERNAL)) {
//                mUtil.addLogMsg("W",
//                        String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_duplicate_request),
//                                SMBSYNC2_SYNC_REQUEST_EXTERNAL));
//            } else {
//                mUtil.addLogMsg("I", mContext.getString(R.string.msgs_svc_received_start_request_from_external_auto_task));
//                queueAutoSyncTask(SMBSYNC2_SYNC_REQUEST_EXTERNAL);
//                if (!mGp.syncThreadActive) {
//                    sendStartNotificationIntent();
//                    startSyncThread();
//                }
//            }
            mUtil.addLogMsg("I", mContext.getString(R.string.msgs_svc_received_start_request_from_external_auto_task));
            queueAutoSyncTask(SMBSYNC2_SYNC_REQUEST_EXTERNAL);
            if (!mGp.syncThreadActive) {
                sendStartNotificationIntent();
                startSyncThread();
            }
        }
        if (isServiceToBeStopped()) stopSelf();
    }

//    static public boolean isDuplicateRequest(GlobalParameters mGp, String req_id) {
//        boolean result = false;
//        synchronized (mGp.syncRequestQueue) {
//            if (mGp.syncRequestQueue.size() > 0) {
//                Iterator<SyncRequestItem> sr_o = mGp.syncRequestQueue.iterator();
//                SyncRequestItem sr = null;
//                while ((sr = sr_o.next()) != null) {
////					Log.v("","id="+sr.request_id+", rid="+req_id);
//                    if (sr.request_id.equals(req_id)) {
//                        result = true;
//                        break;
//                    }
//                    if (!sr_o.hasNext()) break;
//                }
//            } else {
//                if (mGp.syncThreadActive && mGp.syncThreadRequestID.equals(req_id)) {
//                    result = true;
//                }
//            }
//        }
//        return result;
//    }

    private void startSyncByShortcut(Intent in) {
        mUtil.addLogMsg("I", mContext.getString(R.string.msgs_svc_received_start_request_from_shortcut));
//        if (isDuplicateRequest(mGp, SMBSYNC2_SYNC_REQUEST_SHORTCUT)) {
//            mUtil.addLogMsg("W",
//                    String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_duplicate_request),
//                            SMBSYNC2_SYNC_REQUEST_SHORTCUT));
//        } else {
//            queueAutoSyncTask(SMBSYNC2_SYNC_REQUEST_SHORTCUT);
//            if (!mGp.syncThreadActive) {
//                sendStartNotificationIntent();
//                startSyncThread();
//            }
//        }
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
        synchronized (mGp.syncThreadControl) {
            mGp.syncThreadControl.setDisabled();
            mGp.syncThreadControl.notify();
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

    private void queueSpecificSyncTask(String job_name[], String req_id,
                                       boolean wifi_on, int delay_time, boolean wifi_off) {
        SyncRequestItem sri = new SyncRequestItem();
        sri.request_id = req_id;
        sri.wifi_off_after_sync_ended = wifi_off;
        sri.wifi_on_before_sync_start = wifi_on;
        sri.start_delay_time_after_wifi_on = delay_time;
        if (job_name != null && job_name.length > 0) {
            for (int i = 0; i < job_name.length; i++) {
                if (getSyncTask(job_name[i]) != null) {
                    if (isSyncTaskAlreadyScheduled(mGp.syncRequestQueue, job_name[i])) {
                        mUtil.addLogMsg("W", "Sync task was ignored because Sync task was already queued, job=" + job_name[i] + ", Requestor=" + req_id);
                    } else {
                        sri.sync_task_list.add(getSyncTask(job_name[i]).clone());
                        mUtil.addDebugMsg(1, "I", "queueSpecificSyncTask Sync task was queued, job=" + job_name[i] + ", Requestor=" + req_id);
                    }
                } else {
                    mUtil.addLogMsg("W", mContext.getString(R.string.msgs_main_sync_selected_task_not_found) + job_name[i]);
                }
            }
            if (sri.sync_task_list.size() > 0) {
                mGp.syncRequestQueue.add(sri);
//                if (!mGp.syncThreadActive) {
//                    sendStartNotificationIntent();
//                    startSyncThread();
//                }
            } else {
                mUtil.addLogMsg("E", mContext.getString(R.string.msgs_main_sync_select_prof_no_active_profile));
            }
        } else {
            mUtil.addLogMsg("E", mContext.getString(R.string.msgs_main_sync_select_prof_no_active_profile));
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
//	    		NotificationUtil.setNotificationIcon(mGp, R.drawable.ic_48_smbsync_run_anim, R.drawable.ic_48_smbsync_run);
                NotificationUtil.showOngoingMsg(mGp, System.currentTimeMillis(),
                        mContext.getString(R.string.msgs_active_sync_prof_not_found));
            } else {
                mGp.syncRequestQueue.add(sri);
//                if (!mGp.syncThreadActive) {
//                    sendStartNotificationIntent();
//                    startSyncThread();
//                }
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
            String tssid = mWifiMgr.getConnectionInfo().getSSID();
            String wssid = "";
            String ss = mWifiMgr.getConnectionInfo().getSupplicantState().toString();
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