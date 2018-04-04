package com.sentaroh.android.SMBSync2;

/*
The MIT License (MIT)
Copyright (c) 2011-2013 Sentaroh

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
import static com.sentaroh.android.SMBSync2.Log.LogConstants.*;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import com.sentaroh.android.Utilities.CommonGlobalParms;
import com.sentaroh.android.Utilities.SafFileManager;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.ThreadCtrl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GlobalParameters extends CommonGlobalParms {
    public Context appContext = null;

    public boolean debuggable = false;

    public boolean activityIsFinished = true;

    public boolean externalStorageIsMounted = false;
    public boolean externalStorageAccessIsPermitted = false;
    public String internalRootDirectory = "/";
    public String applicationRootDirectory = "/";

    public String profilePassword = "";
    public final String profileKeyPrefix = "*SMBSync2*";
    public final String profileKeyPrefixOld = "*SMBSync*";

    public ArrayBlockingQueue<SyncRequestItem> syncRequestQueue = new ArrayBlockingQueue<SyncRequestItem>(1000);

    public ThreadCtrl syncThreadConfirm = new ThreadCtrl();
    public ThreadCtrl syncThreadControl = new ThreadCtrl();

    public boolean activityIsBackground = true;
    public boolean syncThreadEnabled = true;
    public boolean syncThreadActive = false;
    public boolean syncThreadConfirmWait = false;
    public String syncThreadRequestID = "";

    public ISvcCallback callbackStub = null;

//	public boolean activityIsBackgroud=false;

    public boolean sampleProfileCreateRequired = false;

    public boolean wifiIsActive = false;
    public String wifiSsid = "";

    public WakeLock dimScreenWakelock = null;

    public boolean themeIsLight = true;
    public int applicationTheme = -1;
    public ThemeColorList themeColorList = null;

//	public boolean scheduleSyncEnabled=false;
//	public ArrayList<ScheduleItem> scheduleInfoList =new ArrayList<ScheduleItem>();

    //	Settings parameter
    public boolean settingExitClean = false;
    public int settingDebugLevel = 0;
    public boolean settingUseLightTheme = false;
    public int settingLogMaxFileCount = 10;
    public String settingMgtFileDir = "", settingLogMsgFilename = LOG_FILE_NAME;
    public boolean settingLogOption = false;
    public int settingLogFileMaxSize = 1024 * 1024 * 20;
    public boolean settingPutLogcatOption = false;

    public boolean settingWriteSyncResultLog = true;

    public boolean settingErrorOption = false;
    public boolean settingWifiLockRequired = true;

    public String settingNoCompressFileType = DEFAULT_NOCOMPRESS_FILE_TYPE;
    static final public String DEFAULT_NOCOMPRESS_FILE_TYPE =
            "aac;apk;avi;gif;ico;gz;jar;jpe;jpeg;jpg;m3u;m4a;m4u;mov;movie;mp2;mp3;mpe;mpeg;mpg;mpga;png;qt;ra;ram;svg;tgz;wmv;zip;";

    public boolean settingSupressAppSpecifiDirWarning = false;
    public boolean settingSuppressShortcutWarning = true;
    public boolean settingFixDeviceOrientationToPortrait = false;

    public boolean settingScreenOnWhileSync = true;

    public boolean settingExportedProfileEncryptRequired = true;

    public String settingNotificationMessageWhenSyncEnded = SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS;
    public String settingVibrateWhenSyncEnded = SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS;
    public String settingRingtoneWhenSyncEnded = SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS;

    public int settingNotificationVolume = 100;

    public Handler uiHandler = null;

    public NotificationManager notificationManager = null;
    public boolean notificationEnabled = true;
    public int notificationSmallIcon = R.drawable.ic_48_smbsync_wait;
    public Notification notification = null;
    public Builder notificationBuilder = null;
    public BigTextStyle notificationBigTextStyle = null;
    public Intent notificationIntent = null;
    public PendingIntent notificationPendingIntent = null;
    public String notificationLastShowedMessage = null, notificationLastShowedTitle = "";
    public long notificationLastShowedWhen = 0;
    public String notificationAppName = "";
    //	public boolean notiifcationEnabled=false;
    public long notificationNextShowedTime = 0;
    public Bitmap notificationLargeIcon = null;

    public ArrayList<SyncMessageItem> msgList = new ArrayList<SyncMessageItem>();
    public boolean freezeMessageViewScroll = false;
    public AdapterSyncMessage msgListAdapter = null;
    public ListView msgListView = null;

    public ArrayList<SyncHistoryItem> syncHistoryList = null;
    public AdapterSyncHistory syncHistoryAdapter = null;
    public ListView syncHistoryListView = null;

    public ArrayList<SyncTaskItem> syncTaskList = null;
    public AdapterSyncTask syncTaskAdapter = null;
    public ListView syncTaskListView = null;

    public TextView scheduleInfoView = null;
    public String scheduleInfoText = "";

    public boolean dialogWindowShowed = false;
    public String progressSpinSyncprofText = "", progressSpinMsgText = "";

    public boolean confirmDialogShowed = false;
    public String confirmDialogFilePath = "";
    public String confirmDialogMethod = "";

    public LinearLayout confirmView = null;
    public TextView confirmMsg = null;
    public Button confirmCancel = null;
    public OnClickListener confirmCancelListener = null;
    public Button confirmYes = null;
    public OnClickListener confirmYesListener = null;
    public Button confirmNo = null;
    public OnClickListener confirmNoListener = null;
    public Button confirmYesAll = null;
    public OnClickListener confirmYesAllListener = null;
    public Button confirmNoAll = null;
    public OnClickListener confirmNoAllListener = null;

    public LinearLayout progressBarView = null;
    public TextView progressBarMsg = null;
    public ProgressBar progressBarPb = null;
    public Button progressBarCancel = null;
    public OnClickListener progressBarCancelListener = null;
    public Button progressBarImmed = null;
    public OnClickListener progressBarImmedListener = null;

    public LinearLayout progressSpinView = null;
    public TextView progressSpinSyncprof = null;
    public TextView progressSpinMsg = null;
    public Button progressSpinCancel = null;
    public OnClickListener progressSpinCancelListener = null;

    public SafFileManager safMgr = null;

    public GlobalParameters() {
//		Log.v("","constructed");
    }

    ;

    @SuppressLint("Wakelock")
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
//		Log.v("","onCreate dir="+getFilesDir().toString());
        appContext = this.getApplicationContext();
        uiHandler = new Handler();
        debuggable = isDebuggable();

        mDimWakeLock = ((PowerManager) appContext.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SMBSync2-thread-dim");
        mPartialWakeLock = ((PowerManager) appContext.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SMBSync2-thread-partial");
        WifiManager wm = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "SMBSync2-thread");

        internalRootDirectory = Environment.getExternalStorageDirectory().toString();//getInternalStorageRootDirectory();
        applicationRootDirectory = getFilesDir().toString();

        initStorageStatus();

        initSettingsParms();
        loadSettingsParms();
        setLogParms(this);

//        scheduleInfoList = ScheduleUtil.loadScheduleData(this);
    }

    ;

    public void clearParms() {
        synchronized (msgList) {
            msgList = new ArrayList<SyncMessageItem>();
            msgListAdapter = null;
        }
    }

    ;

    @SuppressLint("NewApi")
    public void initStorageStatus() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            externalStorageIsMounted = false;
        } else {
            externalStorageIsMounted = true;
        }

        if (Build.VERSION.SDK_INT >= 23) {
            externalStorageAccessIsPermitted =
                    (appContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        } else {
            externalStorageAccessIsPermitted = true;
        }

        refreshMediaDir();
    }

    ;

    public void refreshMediaDir() {
        if (safMgr == null) {
            safMgr = new SafFileManager(appContext, settingDebugLevel > 1);
        } else {
            safMgr.setDebugEnabled(settingDebugLevel > 1);
            safMgr.loadSafFileList();
        }
    }

    ;

    public void setLogParms(GlobalParameters gp) {
        setDebugLevel(gp.settingDebugLevel);
        setLogcatEnabled(gp.settingPutLogcatOption);
        setLogLimitSize(2 * 1024 * 1024);
        setLogMaxFileCount(gp.settingLogMaxFileCount);
        setLogEnabled(gp.settingLogOption);
        setLogDirName(gp.settingMgtFileDir);
        setLogFileName(gp.settingLogMsgFilename);
        setApplicationTag(APPLICATION_TAG);
        setLogIntent(BROADCAST_LOG_RESET,
                BROADCAST_LOG_DELETE,
                BROADCAST_LOG_FLUSH,
                BROADCAST_LOG_ROTATE,
                BROADCAST_LOG_SEND,
                BROADCAST_LOG_CLOSE);

    }

    public void initSettingsParms() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        if (prefs.getString(appContext.getString(R.string.settings_mgt_dir), "-1").equals("-1")) {
            Editor pe = prefs.edit();

            sampleProfileCreateRequired = true;

            pe.putString(appContext.getString(R.string.settings_mgt_dir), internalRootDirectory + "/" + APPLICATION_TAG);

            pe.putBoolean(appContext.getString(R.string.settings_exit_clean), true);

            pe.putString(appContext.getString(R.string.settings_smb_lm_compatibility), "0");
            pe.putBoolean(appContext.getString(R.string.settings_smb_use_extended_security), false);
            pe.putString(appContext.getString(R.string.settings_smb_rcv_buf_size), "66576");
            pe.putString(appContext.getString(R.string.settings_smb_snd_buf_size), "66576");
            pe.putString(appContext.getString(R.string.settings_smb_listSize), "65535");
            pe.putString(appContext.getString(R.string.settings_smb_maxBuffers), "100");

            pe.commit();
        }
        if (!prefs.contains(appContext.getString(R.string.settings_dim_screen_on_while_sync)))
            prefs.edit().putBoolean(appContext.getString(R.string.settings_dim_screen_on_while_sync), true).commit();
        if (!prefs.contains(appContext.getString(R.string.settings_notification_message_when_sync_ended)))
            prefs.edit().putString(appContext.getString(R.string.settings_notification_message_when_sync_ended),
                    SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS).commit();
        if (!prefs.contains(appContext.getString(R.string.settings_playback_ringtone_volume)))
            prefs.edit().putInt(appContext.getString(R.string.settings_playback_ringtone_volume), 100).commit();

        if (!prefs.contains(appContext.getString(R.string.settings_sync_history_log)))
            prefs.edit().putBoolean(appContext.getString(R.string.settings_sync_history_log), true).commit();

    }

    public void setSettingOptionLogEnabled(boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        prefs.edit().putBoolean(appContext.getString(R.string.settings_log_option), enabled).commit();
        if (settingDebugLevel==0 && enabled) {
            prefs.edit().putString(appContext.getString(R.string.settings_log_level), "1").commit();
        }
    }

    public void loadSettingsParms() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        settingDebugLevel = Integer.parseInt(prefs.getString(appContext.getString(R.string.settings_log_level), "0"));
        settingLogMaxFileCount = Integer.valueOf(prefs.getString(appContext.getString(R.string.settings_log_file_max_count), "10"));
        settingMgtFileDir = prefs.getString(appContext.getString(R.string.settings_mgt_dir), internalRootDirectory + "/" + APPLICATION_TAG);
        settingLogOption = prefs.getBoolean(appContext.getString(R.string.settings_log_option), false);
        settingPutLogcatOption = prefs.getBoolean(appContext.getString(R.string.settings_put_logcat_option), false);
        settingErrorOption = prefs.getBoolean(appContext.getString(R.string.settings_error_option), false);
        settingWifiLockRequired = prefs.getBoolean(appContext.getString(R.string.settings_wifi_lock), false);

        if (prefs.getString(appContext.getString(R.string.settings_no_compress_file_type), "").equals("")) {
            Editor ed = prefs.edit();
            ed.putString(appContext.getString(R.string.settings_no_compress_file_type), DEFAULT_NOCOMPRESS_FILE_TYPE);
            ed.commit();
        }
        settingNoCompressFileType = prefs.getString(appContext.getString(R.string.settings_no_compress_file_type), DEFAULT_NOCOMPRESS_FILE_TYPE);

        settingNotificationMessageWhenSyncEnded = prefs.getString(appContext.getString(R.string.settings_notification_message_when_sync_ended), "1");
        settingRingtoneWhenSyncEnded = prefs.getString(appContext.getString(R.string.settings_playback_ringtone_when_sync_ended), "0");
        settingVibrateWhenSyncEnded = prefs.getString(appContext.getString(R.string.settings_vibrate_when_sync_ended), "0");
        settingExportedProfileEncryptRequired = prefs.getBoolean(appContext.getString(R.string.settings_exported_profile_encryption), true);
        settingSupressAppSpecifiDirWarning = prefs.getBoolean(appContext.getString(R.string.settings_suppress_warning_app_specific_dir), false);

        themeIsLight = settingUseLightTheme = prefs.getBoolean(appContext.getString(R.string.settings_use_light_theme), false);
        if (settingUseLightTheme) {
            applicationTheme = R.style.MainLight;
        } else {
            applicationTheme = R.style.Main;
        }
        settingFixDeviceOrientationToPortrait = prefs.getBoolean(appContext.getString(R.string.settings_device_orientation_portrait), false);

        settingScreenOnWhileSync = prefs.getBoolean(appContext.getString(R.string.settings_dim_screen_on_while_sync), true);

        settingNotificationVolume = prefs.getInt(appContext.getString(R.string.settings_playback_ringtone_volume), 100);

        settingWriteSyncResultLog = prefs.getBoolean(appContext.getString(R.string.settings_sync_history_log), true);
    }

    public String settingsSmbRcvBufSize = "16644",
            settingsSmbSndBufSize = "16644", settingsSmbListSize = "130170",
            settingsSmbMaxBuffers = "",
            settingsSmbLmCompatibility = "0", settingsSmbUseExtendedSecurity = "true";

    final public void initJcifsOption() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);

        settingsSmbRcvBufSize = prefs.getString(appContext.getString(R.string.settings_smb_rcv_buf_size), "66576");
        settingsSmbSndBufSize = prefs.getString(appContext.getString(R.string.settings_smb_snd_buf_size), "66576");
        settingsSmbListSize = prefs.getString(appContext.getString(R.string.settings_smb_listSize), "");
        settingsSmbMaxBuffers = prefs.getString(appContext.getString(R.string.settings_smb_maxBuffers), "100");

        settingsSmbLmCompatibility = prefs.getString(appContext.getString(R.string.settings_smb_lm_compatibility), "0");
        boolean ues = prefs.getBoolean(appContext.getString(R.string.settings_smb_use_extended_security), false);

        settingsSmbUseExtendedSecurity = "";
        if (ues) settingsSmbUseExtendedSecurity = "true";
        else settingsSmbUseExtendedSecurity = "false";

        System.setProperty("jcifs.netbios.retryTimeout", "3000");
//
//        System.setProperty("jcifs.smb.lmCompatibility", settingsSmbLmCompatibility);
//        System.setProperty("jcifs.smb.client.useExtendedSecurity", settingsSmbUseExtendedSecurity);
//
//        if (!settingsSmbRcvBufSize.equals(""))
//            System.setProperty("jcifs.smb.client.rcv_buf_size", settingsSmbRcvBufSize);//60416 120832
//        if (!settingsSmbSndBufSize.equals(""))
//            System.setProperty("jcifs.smb.client.snd_buf_size", settingsSmbSndBufSize);//16644 120832
//
//        if (!settingsSmbListSize.equals(""))
//            System.setProperty("jcifs.smb.client.listSize", settingsSmbListSize); //65536 1300
//        if (!settingsSmbMaxBuffers.equals(""))
//            System.setProperty("jcifs.smb.maxBuffers", settingsSmbMaxBuffers);//16 100

    }

    private boolean isDebuggable() {
        boolean result = false;
        PackageManager manager = getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = manager.getApplicationInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            result = false;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE)
            result = true;
//        Log.v("","debuggable="+result);
        return result;
    }

    ;

    public WakeLock mDimWakeLock = null;
    public WakeLock mPartialWakeLock = null;
    public WifiLock mWifiLock = null;

    public void releaseWakeLock(SyncUtil util) {
        if (mDimWakeLock.isHeld()) {
            mDimWakeLock.release();
            util.addDebugMsg(1, "I", "Dim wakelock released");
        }
        if (mPartialWakeLock.isHeld()) {
            mPartialWakeLock.release();
            util.addDebugMsg(1, "I", "Partial wakelock released");
        }
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
            util.addDebugMsg(1, "I", "Wifilock released");
        }
    }

    ;

    public void acquireWakeLock(SyncUtil util) {
        if (settingWifiLockRequired) {
            if (!mWifiLock.isHeld()) {
                mWifiLock.acquire();
                util.addDebugMsg(1, "I", "Wifilock acquired");
            }
        }

        if (settingScreenOnWhileSync && isScreenOn(appContext, util)) {// && !activityIsBackground) {
            if (!mDimWakeLock.isHeld()) {
                mDimWakeLock.acquire();
                util.addDebugMsg(1, "I", "Dim wakelock acquired");
            }
        } else {
            if (!mPartialWakeLock.isHeld()) {
                mPartialWakeLock.acquire();
                util.addDebugMsg(1, "I", "Partial wakelock acquired");
            }
        }
    }

    ;

    @SuppressLint("NewApi")
    static public boolean isScreenOn(Context context, SyncUtil util) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= 23) {
            util.addDebugMsg(1, "I", "isDeviceIdleMode()=" + pm.isDeviceIdleMode() +
                    ", isPowerSaveMode()=" + pm.isPowerSaveMode() + ", isInteractive()=" + pm.isInteractive());
        } else {
            util.addDebugMsg(1, "I", "isPowerSaveMode()=" + pm.isPowerSaveMode() + ", isInteractive()=" + pm.isInteractive());
        }
        return pm.isInteractive();
    }

    ;

}
