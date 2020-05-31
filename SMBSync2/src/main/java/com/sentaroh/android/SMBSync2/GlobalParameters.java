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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.LocaleList;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sentaroh.android.SMBSync2.Log.LogUtil;
import com.sentaroh.android.Utilities.CommonGlobalParms;
import com.sentaroh.android.Utilities.SafManager;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.ThreadCtrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.LoggerWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import jcifs.util.LogStream;

import static com.sentaroh.android.SMBSync2.Constants.APPLICATION_TAG;
import static com.sentaroh.android.SMBSync2.Constants.LOG_FILE_NAME;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_BLACK;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_LANGUAGE_INIT;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_LIGHT;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_STANDARD;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_ENABLED_KEY;

public class GlobalParameters extends CommonGlobalParms {
    public boolean debuggable = false;

    public boolean activityIsFinished = true;
    public boolean logCatActive=false;

    public boolean externalStorageIsMounted = false;
    public boolean externalStorageAccessIsPermitted = false;
    public String internalRootDirectory = "/";
    public String applicationRootDirectory = "/";

    public String profilePassword = "";
    public final String profileKeyPrefix = "*SMBSync2*";
    public final String profileKeyPrefixOld = "*SMBSync*";

    private final static String GRANT_COARSE_LOCATION_REQUIRED_KEY="settings_sync_grant_coarse_location_required";

    public ArrayBlockingQueue<SyncRequestItem> syncRequestQueue = new ArrayBlockingQueue<SyncRequestItem>(1000);

    public ThreadCtrl syncThreadConfirm = new ThreadCtrl();
    public ThreadCtrl syncThreadCtrl = new ThreadCtrl();

    public boolean activityIsBackground = true;
    public boolean syncThreadEnabled = true;
    public boolean syncThreadActive = false;
    public boolean syncThreadConfirmWait = false;
    public String syncThreadRequestID = "";
    public String syncThreadRequestIDdisplay = "";

    public ISvcCallback callbackStub = null;

//	public boolean activityIsBackgroud=false;

    public boolean sampleProfileCreateRequired = false;

    public boolean wifiIsActive = false;
    public String wifiSsid = "";

//    public boolean themeIsLight = true;
    public String settingScreenTheme =SMBSYNC2_SCREEN_THEME_STANDARD;
    public int applicationTheme = -1;
    public ThemeColorList themeColorList = null;

    public static String settingScreenThemeLanguage = SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM;//holds language code (fr, en... or "0" for system default)
    public static String settingScreenThemeLanguageValue = SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM;//language value (array index) in listPreferences: "0" for system default, "1" for english...
    public static String onStartSettingScreenThemeLanguageValue = SMBSYNC2_SCREEN_THEME_LANGUAGE_INIT;//on first App start, it will be assigned the active language value
    private static final boolean LANGUAGE_LOCALE_USE_NEW_API = true;//set false to force using deprecated setLocale() and not createConfigurationContext() in attachBaseContext

//	public boolean scheduleSyncEnabled=false;
//	public ArrayList<ScheduleItem> scheduleInfoList =new ArrayList<ScheduleItem>();

    //	Settings parameter
    public boolean settingExitClean = true;
    public boolean settingSyncMessageUseStandardTextView =false;
    public int settingDebugLevel = 0;
//    public boolean settingUseLightTheme = false;
    public int settingLogMaxFileCount = 5;
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
    public boolean settingSupressLocationServiceWarning =false;
    public boolean settingSuppressShortcutWarning = true;
    public boolean settingFixDeviceOrientationToPortrait = false;
    public boolean settingForceDeviceTabletViewInLandscape = false;

    public String settingSecurityApplicationPasswordHashValue = "";
//    public boolean settingSecurityApplicationPassword = false;
    public boolean settingSecurityApplicationPasswordUseAppStartup = false;
    public boolean settingSecurityApplicationPasswordUseEditTask = false;
    public boolean settingSecurityApplicationPasswordUseExport = false;
    public boolean settingSecurityReinitSmbAccountPasswordValue = false;

    public boolean appPasswordAuthValidated=false;
    public long appPasswordAuthLastTime=0L;

    public boolean settingPreventSyncStartDelay = true;
    public boolean settingScreenOnIfScreenOnAtStartOfSync = false;

    public boolean settingExportedProfileEncryptRequired = true;

    public boolean settingGrantCoarseLocationRequired = true;

    public String settingNotificationMessageWhenSyncEnded = SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS;
    public String settingVibrateWhenSyncEnded = SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS;
    public String settingRingtoneWhenSyncEnded = SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS;

    public int settingNotificationVolume = 100;

    public boolean settingScheduleSyncEnabled=true;

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
//    public Bitmap notificationLargeIcon = null;

    public ArrayList<SyncMessageItem> msgList = null; //new ArrayList<SyncMessageItem>();
    public boolean freezeMessageViewScroll = false;
    public AdapterSyncMessage msgListAdapter = null;
    public ListView msgListView = null;

    public ArrayList<SyncHistoryItem> syncHistoryList = null;
    public AdapterSyncHistory syncHistoryAdapter = null;
    public ListView syncHistoryListView = null;

    public ArrayList<ScheduleItem> syncTabScheduleList = null;
    public AdapterScheduleList syncTabScheduleAdapter = null;
    public ListView syncTabScheduleListView = null;
    public TextView syncTabMessage=null;

    public ArrayList<SyncTaskItem> syncTaskList = null;
    public AdapterSyncTask syncTaskAdapter = null;
    public ListView syncTaskListView = null;
    public TextView syncTaskEmptyMessage=null;

    public TextView scheduleInfoView = null;
    public String scheduleInfoText = "";
    public TextView scheduleErrorView = null;
    public String scheduleErrorText = "";

    public boolean dialogWindowShowed = false;
    public String progressSpinSyncprofText = "", progressSpinMsgText = "";

    public boolean confirmDialogShowed = false;
    public String confirmDialogFilePathPairA = "";
    public long confirmDialogFileLengthPairA = 0L, confirmDialogFileLastModPairA = 0L;
    public String confirmDialogFilePathPairB = "";
    public long confirmDialogFileLengthPairB = 0L, confirmDialogFileLastModPairB = 0L;
    public String confirmDialogMethod = "";
    public String confirmDialogMessage = "";

    public LinearLayout confirmView = null;
    public LinearLayout confirmOverrideView = null;
    public LinearLayout confirmConflictView = null;
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

    public TextView confirmDialogConflictFilePathA=null;
    public TextView confirmDialogConflictFileLengthA=null;
    public TextView confirmDialogConflictFileLastModA=null;
    public TextView confirmDialogConflictFilePathB=null;
    public TextView confirmDialogConflictFileLengthB=null;
    public TextView confirmDialogConflictFileLastModB=null;

    public Button confirmDialogConflictButtonSelectA=null;
    public OnClickListener confirmDialogConflictButtonSelectAListener = null;
    public Button confirmDialogConflictButtonSelectB=null;
    public OnClickListener confirmDialogConflictButtonSelectBListener = null;
    public Button confirmDialogConflictButtonSyncIgnoreFile=null;
    public OnClickListener confirmDialogConflictButtonSyncIgnoreFileListener = null;
    public Button confirmDialogConflictButtonCancelSyncTask=null;
    public OnClickListener confirmDialogConflictButtonCancelSyncTaskListener = null;


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

    public SafManager safMgr = null;

    private static LogStream logStream=null;//JCIFS logStream
    private static Logger slf4jLog = LoggerFactory.getLogger(GlobalParameters.class);

    public GlobalParameters() {
    }

//    @SuppressLint("Wakelock")
//    @SuppressWarnings("deprecation")
//    @Override
//    public void onCreate() {
//        super.onCreate();
////		Log.v("","onCreate dir="+getFilesDir().toString());
//        c = this.getApplicationContext();
//        uiHandler = new Handler();
//        debuggable = isDebuggable();
//
//        mDimWakeLock = ((PowerManager) c.getSystemService(Context.POWER_SERVICE))
//                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SMBSync2-thread-dim");
//        mPartialWakeLock = ((PowerManager) c.getSystemService(Context.POWER_SERVICE))
//                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SMBSync2-thread-partial");
//        WifiManager wm = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
//        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "SMBSync2-thread");
//
//        internalRootDirectory = Environment.getExternalStorageDirectory().toString();//getInternalStorageRootDirectory();
//        applicationRootDirectory = getFilesDir().toString();
//
//        initStorageStatus();
//
//        initSettingsParms();
//        loadSettingsParms();
//        setLogParms(this);
//
////        scheduleInfoList = ScheduleUtil.loadScheduleData(this);
//    }

    synchronized public void initGlobalParamter(Context c) {
//		Log.v("","onCreate dir="+getFilesDir().toString());
        uiHandler = new Handler();
        debuggable = isDebuggable(c);

        mDimWakeLock = ((PowerManager) c.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SMBSync2-thread-dim");
        forceDimScreenWakelock = ((PowerManager) c.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SMBSync2-thread-force-dim");
        mPartialWakeLock = ((PowerManager) c.getSystemService(Context.POWER_SERVICE))
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SMBSync2-thread-partial");
        WifiManager wm = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "SMBSync2-thread");

        try {
            String fp=c.getExternalFilesDirs(null)[0].getPath();
            internalRootDirectory = fp.substring(0, fp.indexOf("/Android/data"));
        } catch(Exception ex) {
            internalRootDirectory = Environment.getExternalStorageDirectory().toString();
        }
        applicationRootDirectory = c.getFilesDir().toString();

        final LogUtil jcifs_ng_lu = new LogUtil(c, "SLF4J", this);
        final LogUtil jcifs_old_lu = new LogUtil(c, "JCIFS-V1", this);

        PrintStream ps= null;
        OutputStream os=new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                byte[] buff = ByteBuffer.allocate(4).putInt(b).array();
//                String msg=new String(buff,"UTF-8");
//                if (!msg.equals("\n")) jcifs_old_lu.addDebugMsg(0,"I",msg);
//                Log.v("SMBSync2",StringUtil.getHexString(buff,0,4));
            }
            @Override
            public void write(byte[] buff) throws IOException {
                if (buff.length==1 && buff[0]!=0x0a) {
                } else {
                    String msg=new String(buff,"UTF-8");
                    if (!msg.equals("\n") && msg.replaceAll(" ","").length()>0) jcifs_old_lu.addDebugMsg(0,"I",msg);
//                    Log.v("SMBSync2",StringUtil.getHexString(buff,0,buff.length));
                }
            }
            @Override
            public void write(byte[] buff, int buff_offset, int buff_length) throws IOException {
                if (buff_length==1 && buff[buff_offset]!=0x0a) {
                } else {
                    String msg=new String(buff,buff_offset,buff_length, "UTF-8");
                    if (!msg.equals("\n") && msg.replaceAll(" ","").length()>0) jcifs_old_lu.addDebugMsg(0,"I",msg);
//                    Log.v("SMBSync2",StringUtil.getHexString(buff,buff_offset,buff_length));
                }
            }
        };
        ps=new PrintStream(os);
        LogStream.setInstance(ps);
        logStream=LogStream.getInstance();//Initial create JCIFS logStream object

        JcifsNgLogWriter jcifs_ng_lw=new JcifsNgLogWriter(jcifs_ng_lu);
        slf4jLog.setWriter(jcifs_ng_lw);

        initStorageStatus(c);

        initSettingsParms(c);
        loadSettingsParms(c);
        setLogParms(c, this);

        if (msgList == null) {
            msgList=CommonUtilities.loadMsgList(this);
        }

//        scheduleInfoList = ScheduleUtil.loadScheduleData(this);
    }

    public void clearParms() {
//        synchronized (msgList) {
//            msgList = new ArrayList<SyncMessageItem>();
//            msgListAdapter = null;
//        }
    }

    @SuppressLint("NewApi")
    public void initStorageStatus(Context c) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            externalStorageIsMounted = false;
        } else {
            externalStorageIsMounted = true;
        }

        if (Build.VERSION.SDK_INT >= 23) {
            externalStorageAccessIsPermitted =
                    (c.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        } else {
            externalStorageAccessIsPermitted = true;
        }

        refreshMediaDir(c);
    }

    public void refreshMediaDir(Context c) {
        if (safMgr == null) {
            safMgr = new SafManager(c, settingDebugLevel > 1);
        } else {
            safMgr.setDebugEnabled(settingDebugLevel > 1);
            safMgr.loadSafFile();
        }
    }

    public void setLogParms(Context c, GlobalParameters gp) {
        setDebugLevel(gp.settingDebugLevel);
        setLogcatEnabled(gp.settingPutLogcatOption);
        setLogLimitSize(10 * 1024 * 1024);
        setLogMaxFileCount(gp.settingLogMaxFileCount);
        setLogEnabled(gp.settingLogOption);

        setLogDirName(c.getExternalFilesDir(null)+"/log/");//gp.settingMgtFileDir);
//        setLogDirName(gp.settingMgtFileDir);
        setLogFileName(gp.settingLogMsgFilename);
        setApplicationTag(APPLICATION_TAG);
    }

    public void initSettingsParms(Context c) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        if (prefs.getString(c.getString(R.string.settings_mgt_dir), "-1").equals("-1")) {
            Editor pe = prefs.edit();

            sampleProfileCreateRequired = true;

            pe.putString(c.getString(R.string.settings_mgt_dir), internalRootDirectory + "/" + APPLICATION_TAG);

            pe.putBoolean(c.getString(R.string.settings_exit_clean), true);

            pe.putString(c.getString(R.string.settings_smb_lm_compatibility), "3");
            pe.putBoolean(c.getString(R.string.settings_smb_use_extended_security), true);
            pe.putString(c.getString(R.string.settings_smb_client_response_timeout), "30000");
            pe.putBoolean(c.getString(R.string.settings_smb_disable_plain_text_passwords),false);

///*For debug*/pe.putString(c.getString(R.string.settings_log_level), "1").commit();
///*For debug*/pe.putBoolean(c.getString(R.string.settings_log_option), true).commit();

            pe.commit();
        }

        if (!prefs.contains(c.getString(R.string.settings_screen_theme)))
            prefs.edit().putString(c.getString(R.string.settings_screen_theme), SMBSYNC2_SCREEN_THEME_STANDARD).commit();

        if (!prefs.contains(c.getString(R.string.settings_screen_theme_language)))
            prefs.edit().putString(c.getString(R.string.settings_screen_theme_language), SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM).commit();

        if (!prefs.contains(c.getString(R.string.settings_dim_screen_on_while_sync)))
            prefs.edit().putBoolean(c.getString(R.string.settings_dim_screen_on_while_sync), true).commit();
        if (!prefs.contains(c.getString(R.string.settings_device_orientation_landscape_tablet)))
            prefs.edit().putBoolean(c.getString(R.string.settings_device_orientation_landscape_tablet), false).commit();
        if (!prefs.contains(c.getString(R.string.settings_notification_message_when_sync_ended)))
            prefs.edit().putString(c.getString(R.string.settings_notification_message_when_sync_ended),
                    SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS).commit();
        if (!prefs.contains(c.getString(R.string.settings_playback_ringtone_volume)))
            prefs.edit().putInt(c.getString(R.string.settings_playback_ringtone_volume), 100).commit();

        if (!prefs.contains(c.getString(R.string.settings_sync_history_log)))
            prefs.edit().putBoolean(c.getString(R.string.settings_sync_history_log), true).commit();

        if (!prefs.contains(GRANT_COARSE_LOCATION_REQUIRED_KEY))
            prefs.edit().putBoolean(GRANT_COARSE_LOCATION_REQUIRED_KEY, true).commit();

        if (!prefs.contains(c.getString(R.string.settings_security_application_password)))
            prefs.edit().putBoolean(c.getString(R.string.settings_security_application_password), false).commit();

        if (!prefs.contains(c.getString(R.string.settings_security_application_password_use_app_startup)))
            prefs.edit().putBoolean(c.getString(R.string.settings_security_application_password_use_app_startup), false).commit();

        if (!prefs.contains(c.getString(R.string.settings_security_application_password_use_edit_task)))
            prefs.edit().putBoolean(c.getString(R.string.settings_security_application_password_use_edit_task), false).commit();

        if (!prefs.contains(c.getString(R.string.settings_security_application_password_use_export_task)))
            prefs.edit().putBoolean(c.getString(R.string.settings_security_application_password_use_export_task), false).commit();

        if (!prefs.contains(c.getString(R.string.settings_security_init_smb_account_password)))
            prefs.edit().putBoolean(c.getString(R.string.settings_security_init_smb_account_password), false).commit();

        if (!prefs.contains(c.getString(R.string.settings_wifi_lock)))
            prefs.edit().putBoolean(c.getString(R.string.settings_wifi_lock), true).commit();
    }

    public void setSettingOptionLogEnabled(Context c, boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putBoolean(c.getString(R.string.settings_log_option), enabled).commit();
        if (settingDebugLevel == 0 && enabled) {
            prefs.edit().putString(c.getString(R.string.settings_log_level), "1").commit();
        }
    }

    public void setSettingGrantCoarseLocationRequired(Context c, boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putBoolean(GRANT_COARSE_LOCATION_REQUIRED_KEY, enabled).commit();
        settingGrantCoarseLocationRequired=enabled;
    }

    public void loadSettingsParms(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        settingDebugLevel = Integer.parseInt(prefs.getString(c.getString(R.string.settings_log_level), "0"));
        slf4jLog.setAppendTime(false);
        if (settingDebugLevel==0) {
            LogStream.setLevel(1);
            slf4jLog.setLogOption(false, true, false, false, false);
        } else if (settingDebugLevel==1) {
            LogStream.setLevel(2);
            slf4jLog.setLogOption(false, true, true, false, false);
        } else if (settingDebugLevel==2) {
            LogStream.setLevel(5);
            slf4jLog.setLogOption(true, true, true, true, true);
        } else if (settingDebugLevel==3) {
            LogStream.setLevel(5);
            slf4jLog.setLogOption(true, true, true, true, true);
        }
        settingExitClean=prefs.getBoolean(c.getString(R.string.settings_exit_clean), true);

        settingLogMaxFileCount = Integer.valueOf(prefs.getString(c.getString(R.string.settings_log_file_max_count), "5"));
        settingMgtFileDir = prefs.getString(c.getString(R.string.settings_mgt_dir), internalRootDirectory + "/" + APPLICATION_TAG);
        settingLogOption = prefs.getBoolean(c.getString(R.string.settings_log_option), false);
        settingPutLogcatOption = prefs.getBoolean(c.getString(R.string.settings_put_logcat_option), false);
        settingErrorOption = prefs.getBoolean(c.getString(R.string.settings_error_option), false);
        settingWifiLockRequired = prefs.getBoolean(c.getString(R.string.settings_wifi_lock), true);

        if (prefs.getString(c.getString(R.string.settings_no_compress_file_type), "").equals("")) {
            Editor ed = prefs.edit();
            ed.putString(c.getString(R.string.settings_no_compress_file_type), DEFAULT_NOCOMPRESS_FILE_TYPE);
            ed.commit();
        }
        settingNoCompressFileType = prefs.getString(c.getString(R.string.settings_no_compress_file_type), DEFAULT_NOCOMPRESS_FILE_TYPE);

        settingNotificationMessageWhenSyncEnded = prefs.getString(c.getString(R.string.settings_notification_message_when_sync_ended), "1");
        settingRingtoneWhenSyncEnded = prefs.getString(c.getString(R.string.settings_playback_ringtone_when_sync_ended), "0");
        settingVibrateWhenSyncEnded = prefs.getString(c.getString(R.string.settings_vibrate_when_sync_ended), "0");
        settingExportedProfileEncryptRequired = prefs.getBoolean(c.getString(R.string.settings_exported_profile_encryption), true);
        settingSupressAppSpecifiDirWarning = prefs.getBoolean(c.getString(R.string.settings_suppress_warning_app_specific_dir), false);
        settingSupressLocationServiceWarning = prefs.getBoolean(c.getString(R.string.settings_suppress_warning_location_service_disabled), false);

        settingScreenTheme =prefs.getString(c.getString(R.string.settings_screen_theme), SMBSYNC2_SCREEN_THEME_STANDARD);
        if (prefs.contains("settings_use_light_theme")) {
            boolean themeIsLight = prefs.getBoolean("settings_use_light_theme", false);
            if (themeIsLight) {
                prefs.edit().remove("settings_use_light_theme").commit();
                settingScreenTheme=SMBSYNC2_SCREEN_THEME_LIGHT;
                prefs.edit().putString(c.getString(R.string.settings_screen_theme), SMBSYNC2_SCREEN_THEME_LIGHT).commit();
            }
        }
        if (settingScreenTheme.equals(SMBSYNC2_SCREEN_THEME_LIGHT)) applicationTheme = R.style.MainLight;
        else if (settingScreenTheme.equals(SMBSYNC2_SCREEN_THEME_BLACK)) applicationTheme = R.style.MainBlack;
        else applicationTheme = R.style.Main;
//        if (settingUseLightTheme) {
//            applicationTheme = R.style.MainLight;
//        } else {
//            applicationTheme = R.style.MainBlack;
//        }
        loadLanguagePreference(c);

        settingForceDeviceTabletViewInLandscape = prefs.getBoolean(c.getString(R.string.settings_device_orientation_landscape_tablet), false);

        settingFixDeviceOrientationToPortrait = prefs.getBoolean(c.getString(R.string.settings_device_orientation_portrait), false);

        settingPreventSyncStartDelay = prefs.getBoolean(c.getString(R.string.settings_dim_screen_on_while_sync), true);

        settingNotificationVolume = prefs.getInt(c.getString(R.string.settings_playback_ringtone_volume), 100);

        settingWriteSyncResultLog = prefs.getBoolean(c.getString(R.string.settings_sync_history_log), true);

        settingGrantCoarseLocationRequired = prefs.getBoolean(GRANT_COARSE_LOCATION_REQUIRED_KEY, true);

        settingScreenOnIfScreenOnAtStartOfSync =prefs.getBoolean(c.getString(R.string.settings_force_screen_on_while_sync), false);

        settingSyncMessageUseStandardTextView =prefs.getBoolean(c.getString(R.string.settings_sync_message_use_standard_text_view), false);

        settingSecurityApplicationPasswordHashValue =ApplicationPasswordUtil.getApplicationPasswordHashValue(prefs);
        settingSecurityApplicationPasswordUseAppStartup = prefs.getBoolean(c.getString(R.string.settings_security_application_password_use_app_startup), false);
        settingSecurityApplicationPasswordUseEditTask = prefs.getBoolean(c.getString(R.string.settings_security_application_password_use_edit_task), false);
        settingSecurityApplicationPasswordUseExport = prefs.getBoolean(c.getString(R.string.settings_security_application_password_use_export_task), false);
        settingSecurityReinitSmbAccountPasswordValue = prefs.getBoolean(c.getString(R.string.settings_security_init_smb_account_password), false);

        settingScheduleSyncEnabled=prefs.getBoolean(SCHEDULER_ENABLED_KEY, true);

    }

    public void setScheduleEnabled(Context c, boolean enabled) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        settingScheduleSyncEnabled=enabled;
        prefs.edit().putBoolean(SCHEDULER_ENABLED_KEY, enabled).commit();
    }

    //+ To use createConfigurationContext() non deprecated method:
    //  - set LANGUAGE_LOCALE_USE_NEW_API to true
    //  - use as wrapper in attachBaseContext() for all activities and App or extend all App/Activities from a base Activity Class with attachBaseContext()
    //  - for App: implement also in onConfigurationChanged(), not needed in AppCompatActivity class
    //  - in ActivityMain, when wrapping language in attachBaseContext(), we must int language by calling initLanguagePreference(context)
    //    because preferences manager is init later in onCreate()
    //+ To use updateConfiguration() deprecated method on new API:
    //  - set LANGUAGE_LOCALE_USE_NEW_API to false
    //  - no need to use wrapper in attachBaseContext()
    //  - call "mGp.setNewLocale(this, false)" in onCreate() and onConfigurationChanged() of all activities and App, not needed in Activity fragments
    //  - SyncTaskEditor.java onCreate() and onConfigurationChanged() must be also edited
    //  - all activities and App in AndroidManifest must have: android:configChanges="locale|orientation|screenSize|keyboardHidden|layoutDirection"
    public Context setNewLocale(Context c, boolean init) {
        if (init) loadLanguagePreference(c);
        return updateLanguageResources(c, settingScreenThemeLanguage);
    }

    // wrap language layout in the base context for all activities
    private Context updateLanguageResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (LANGUAGE_LOCALE_USE_NEW_API) {// use createConfigurationContext() when updateConfiguration() is deprecated
            if (Build.VERSION.SDK_INT >= 24) {//we can ignore and use "config.setLocale(locale)" like Build.VERSION.SDK_INT >= 21 if we target above API 24 (API 24 only bug)
                setLocaleForApi24(config, locale);
                context = context.createConfigurationContext(config);
            } else if (Build.VERSION.SDK_INT >= 21) {
                config.setLocale(locale);
                context = context.createConfigurationContext(config);
            } else {
                config.locale = locale;
                res.updateConfiguration(config, res.getDisplayMetrics());
            }
        } else {// ignore deprecation and always use updateConfiguration()
            if (Build.VERSION.SDK_INT >= 24) {
                setLocaleForApi24(config, locale);
            } else if (Build.VERSION.SDK_INT >= 21) {
                config.setLocale(locale);
            } else {
                config.locale = locale;
            }
            res.updateConfiguration(config, res.getDisplayMetrics());
        }

        return context;
    }

    //workaround a bug issue in Android N, but not needed after N
    @RequiresApi(api = 24)
    private void setLocaleForApi24(Configuration config, Locale target) {
        Set<Locale> set = new LinkedHashSet<>();
        // bring the target locale to the front of the list
        set.add(target);

        LocaleList all = LocaleList.getDefault();
        for (int i = 0; i < all.size(); i++) {
            // append other locales supported by the user
            set.add(all.get(i));
        }

        Locale[] locales = set.toArray(new Locale[0]);
        config.setLocales(new LocaleList(locales));
    }

    //load language list value from listPreferences. New languages can be added with Excel template without any change in code.
    //Language list entries must contain "(language_code)" string, exp "English (en)"
    //"English" will be the preferences menu description and "en" the language code
    //settingScreenThemeLanguageValue and settingScreenThemeLanguage are updated only here by loadLanguagePreference()
    //loadLanguagePreference() is called on mainActivity start, any mainActivity result, SyncReceiver, and any early init to GlobalParameters
    //onStartSettingScreenThemeLanguageValue() is assigned only once at app start the value of current active language (settingScreenThemeLanguage)
    //on language change by user or through import settings, settingScreenThemeLanguage no longer equals onStartSettingScreenThemeLanguageValue ->
    //it will cause Pref activitiy to finish() and mainActivity to end with a Warning Dialog in checkThemeLanguageChanged()
    public void loadLanguagePreference(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        settingScreenThemeLanguageValue = prefs.getString(c.getString(R.string.settings_screen_theme_language), SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM);
        String[] lang_entries = c.getResources().getStringArray(R.array.settings_screen_theme_language_list_entries);
        if (settingScreenThemeLanguageValue.equals(SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM)) {
            settingScreenThemeLanguage = SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM;
        } else {
            settingScreenThemeLanguage = lang_entries[Integer.parseInt(settingScreenThemeLanguageValue)].split("[\\(\\)]")[1]; // language entries are in the format "description (languageCode)"
        }
        if (onStartSettingScreenThemeLanguageValue.equals(SMBSYNC2_SCREEN_THEME_LANGUAGE_INIT))
            onStartSettingScreenThemeLanguageValue=settingScreenThemeLanguageValue;
    }

    public boolean isScreenThemeIsLight() {
        boolean result=false;
        if (settingScreenTheme.equals(SMBSYNC2_SCREEN_THEME_LIGHT)) result = true;
        return result;
    }

    public String settingsSmbLmCompatibility = "3", settingsSmbUseExtendedSecurity = "true", settingsSmbClientResponseTimeout = "30000";
    public String settingsSmbDisablePlainTextPasswords="false";

    final public void initJcifsOption(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        settingsSmbLmCompatibility = prefs.getString(c.getString(R.string.settings_smb_lm_compatibility), "3");
        boolean ues = prefs.getBoolean(c.getString(R.string.settings_smb_use_extended_security), true);
        boolean dpp=prefs.getBoolean(c.getString(R.string.settings_smb_disable_plain_text_passwords),false);
        settingsSmbClientResponseTimeout = prefs.getString(c.getString(R.string.settings_smb_client_response_timeout), "30000");

        if (settingsSmbLmCompatibility.equals("3") || settingsSmbLmCompatibility.equals("4")) {
            if (!ues) {
                ues = true;
//                prefs.edit().putBoolean(c.getString(R.string.settings_smb_use_extended_security), true).commit();
            }
        }

        settingsSmbUseExtendedSecurity = ues ? "true" : "false";
        settingsSmbDisablePlainTextPasswords=dpp ? "true" : "false";

        System.setProperty("jcifs.smb.client.attrExpirationPeriod", "0");
        System.setProperty("jcifs.netbios.retryTimeout", "3000");
//        System.setProperty("jcifs.smb.client.listSize", "1000");
        System.setProperty("jcifs.smb.lmCompatibility", settingsSmbLmCompatibility);
        System.setProperty("jcifs.smb.client.useExtendedSecurity", settingsSmbUseExtendedSecurity);
        System.setProperty("jcifs.smb.client.responseTimeout", settingsSmbClientResponseTimeout);
        System.setProperty("jcifs.smb.client.disablePlainTextPasswords",settingsSmbDisablePlainTextPasswords);

//        System.setProperty("jcifs.smb.client.snd_buf_size","61440");
//        System.setProperty("jcifs.smb.client.tcpNoDelay","true");
//        System.setProperty("jcifs.smb.maxBuffers","100");
    }

    private boolean isDebuggable(Context c) {
        boolean result = false;
        PackageManager manager = c.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = manager.getApplicationInfo(c.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            result = false;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE)
            result = true;
//        Log.v("","debuggable="+result);
        return result;
    }

    public WakeLock mDimWakeLock = null;
    public WakeLock forceDimScreenWakelock = null;

    public WakeLock mPartialWakeLock = null;
    public WifiLock mWifiLock = null;

    public void releaseWakeLock(CommonUtilities util) {
        if (forceDimScreenWakelock.isHeld()) {
            forceDimScreenWakelock.release();
            util.addDebugMsg(1, "I", "ForceDim wakelock released");
        }

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

    public void acquireWakeLock(Context c, CommonUtilities util) {
        if (settingWifiLockRequired) {
            if (!mWifiLock.isHeld()) {
                mWifiLock.acquire();
                util.addDebugMsg(1, "I", "Wifilock acquired");
            }
        }

        if (settingScreenOnIfScreenOnAtStartOfSync || (settingPreventSyncStartDelay && isScreenOn(c, util))) {// && !activityIsBackground) {
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

    @SuppressLint("NewApi")
    static public boolean isScreenOn(Context context, CommonUtilities util) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= 23) {
            util.addDebugMsg(1, "I", "isDeviceIdleMode()=" + pm.isDeviceIdleMode() +
                    ", isPowerSaveMode()=" + pm.isPowerSaveMode() + ", isInteractive()=" + pm.isInteractive());
        } else {
            util.addDebugMsg(1, "I", "isPowerSaveMode()=" + pm.isPowerSaveMode() + ", isInteractive()=" + pm.isInteractive());
        }
        return pm.isInteractive();
    }

    class JcifsNgLogWriter extends LoggerWriter {
        private LogUtil mLu =null;
        public JcifsNgLogWriter(LogUtil lu) {
            mLu =lu;
        }
        @Override
        public void write(String msg) {
            mLu.addDebugMsg(1,"I", msg);
        }
    }

}
