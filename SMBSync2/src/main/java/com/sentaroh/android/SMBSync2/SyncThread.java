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
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;

import com.sentaroh.android.Utilities.MiscUtil;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.android.Utilities.SafManager;
import com.sentaroh.android.Utilities.StringUtil;
import com.sentaroh.android.Utilities.ZipFileListItem;
import com.sentaroh.jcifs.JcifsAuth;
import com.sentaroh.jcifs.JcifsException;
import com.sentaroh.jcifs.JcifsFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_COPY;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_MOVE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_RESP_CANCEL;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_RESP_NOALL;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_RESP_YESALL;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_DAY;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_MONTH;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_YEAR;
import static com.sentaroh.android.SMBSync2.Constants.SYNC_FILE_TYPE_AUDIO;
import static com.sentaroh.android.SMBSync2.Constants.SYNC_FILE_TYPE_IMAGE;
import static com.sentaroh.android.SMBSync2.Constants.SYNC_FILE_TYPE_VIDEO;
import static com.sentaroh.android.SMBSync2.Constants.WHOLE_DIRECTORY_FILTER_PREFIX;

public class SyncThread extends Thread {

    private GlobalParameters mGp = null;

    private NotifyEvent mNotifyToService = null;

    public final static int SYNC_RETRY_INTERVAL = 30;

    class SyncThreadWorkArea {
        public GlobalParameters gp = null;

        public Context context=null;
//        public ArrayList<TwoWaySyncFileInfoItem> currSyncFileInfoList = new ArrayList<TwoWaySyncFileInfoItem>();
//        public ArrayList<TwoWaySyncFileInfoItem> newSyncFileInfoList = new ArrayList<TwoWaySyncFileInfoItem>();

        public ArrayList<FileLastModifiedTimeEntry> currLastModifiedList = new ArrayList<FileLastModifiedTimeEntry>();
        public ArrayList<FileLastModifiedTimeEntry> newLastModifiedList = new ArrayList<FileLastModifiedTimeEntry>();

        public ArrayList<Pattern[]> dirIncludeFilterArrayList = new ArrayList<Pattern[]>();
        public ArrayList<Pattern[]> dirExcludeFilterArrayList = new ArrayList<Pattern[]>();
        public ArrayList<Pattern> dirExcludeFilterPatternList = new ArrayList<Pattern>();
        public Pattern fileFilterInclude, fileFilterExclude;
        //		public Pattern dirFilterInclude,dirFilterExclude;
        public ArrayList<Pattern> dirIncludeFilterPatternList = new ArrayList<Pattern>();

        public ArrayList<Pattern> wholeDirIncludeFilterPatternList = new ArrayList<Pattern>();
        public ArrayList<Pattern> wholeDirExcludeFilterPatternList = new ArrayList<Pattern>();

        public ArrayList<AdapterFilterList.FilterListItem>matchFromBeginDirIncludeFilterList=new ArrayList<AdapterFilterList.FilterListItem>();
        public ArrayList<AdapterFilterList.FilterListItem>matchFromBeginDirExcludeFilterList=new ArrayList<AdapterFilterList.FilterListItem>();

        public ArrayList<AdapterFilterList.FilterListItem>matchAnyDirExcludeFilterList=new ArrayList<AdapterFilterList.FilterListItem>();

        public final boolean ALL_COPY = false;

        public long totalTransferByte = 0, totalTransferTime = 0;
        public int totalCopyCount, totalDeleteCount, totalIgnoreCount = 0, totalRetryCount = 0;

        public boolean lastModifiedIsFunctional = true;

        public JcifsAuth masterAuth=null;
        public String masterSmbAddress=null;
        public JcifsAuth targetAuth=null;
        public String targetSmbAddress=null;

        public int jcifsNtStatusCode=0;

        public CommonUtilities util = null;

//        public MediaScannerConnection mediaScanner = null;

        public PrintWriter syncHistoryWriter = null;

        public int syncDifferentFileAllowableTime = 0;
        public int offsetOfDaylightSavingTime=0;
        public int syncTaskRetryCount = 0;
        public int syncTaskRetryCountOriginal = 0;

        public boolean localFileLastModListModified = false;

        public int confirmCopyResult = 0, confirmDeleteResult = 0, confirmMoveResult = 0, confirmArchiveResult=0;

        public ArrayList<String> smbFileList = null;
//		public StringBuilder strBldMaster=new StringBuilder(256);
//		public StringBuilder strBldTarget=new StringBuilder(256);

        public String exception_msg_area = "";

        public String msgs_mirror_task_file_copying = null;

        public String msgs_mirror_task_file_replaced = null,
                msgs_mirror_task_file_copied = null,
                msgs_mirror_task_file_moved = null,
                msgs_mirror_task_file_archived = null;

        public SyncTaskItem currentSTI = null;

        public boolean replaceKeywordRequiredAtWhileSync=false;

        public ArrayList<ZipFileListItem> zipFileList = new ArrayList<ZipFileListItem>();
        public String zipFileNameEncoding = "";
        public boolean zipFileCopyBackRequired = false;
        public String zipWorkFileName = null;

        public SafFile lastWriteSafFile=null;
        public File lastWriteFile=null;

        public SimpleDateFormat sdfLocalTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        public SimpleDateFormat sdfUTCTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    }

    private SyncThreadWorkArea mStwa = new SyncThreadWorkArea();

    public SyncThread(Context c, GlobalParameters gp, NotifyEvent ne) {
//        Thread.dumpStack();
        mGp = gp;
        mNotifyToService = ne;
        mStwa.util = new CommonUtilities(c, "SyncThread", mGp, null);
        mStwa.gp = mGp;
        mStwa.context=c;

        mGp.safMgr.setDebugEnabled(mGp.settingDebugLevel > 1);
        mGp.safMgr.loadSafFile();
        mGp.initJcifsOption(c);
//        prepareMediaScanner();

        mStwa.msgs_mirror_task_file_copying = c.getString(R.string.msgs_mirror_task_file_copying);
        mStwa.msgs_mirror_task_file_replaced = c.getString(R.string.msgs_mirror_task_file_replaced);
        mStwa.msgs_mirror_task_file_copied = c.getString(R.string.msgs_mirror_task_file_copied);
        mStwa.msgs_mirror_task_file_moved = c.getString(R.string.msgs_mirror_task_file_moved);
        mStwa.msgs_mirror_task_file_archived = c.getString(R.string.msgs_mirror_task_file_archived);

        mStwa.sdfUTCTime.setTimeZone(TimeZone.getTimeZone("UTC"));
//        mStwa.zipWorkFileName = gp.appContext.getCacheDir().toString() + "/zip_work_file";

        printSafDebugInfo();

//        listStorageInfo();
    }

    private void printSafDebugInfo() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mStwa.context);
        String sd_uuid_list = prefs.getString(SafManager.SDCARD_UUID_KEY, "");
        String usb_uuid_list = prefs.getString(SafManager.USB_UUID_KEY, "");

        mStwa.util.addDebugMsg(1, "I", "SafFile SafManager=" + mGp.safMgr +
                ", sdcard uuid_list=" + sd_uuid_list+", usb uuid_list="+usb_uuid_list);
    }

    private void listStorageInfo() {
        if (mGp.settingDebugLevel>0) {
            ArrayList<String>sil= CommonUtilities.listSystemInfo(mStwa.context, mGp);
            for(String item:sil) mStwa.util.addDebugMsg(1, "I", item);
        }
    }

    @Override
    public void run() {
//        mStwa.util.setLogId(""+Thread.currentThread().getId());
        if (!mGp.syncThreadActive) {
            mGp.syncThreadActive = true;
            defaultUEH = Thread.currentThread().getUncaughtExceptionHandler();
            Thread.currentThread().setUncaughtExceptionHandler(unCaughtExceptionHandler);

            listStorageInfo();

//			showMsg(stwa,false, "","I","","",mStwa.context.getString(R.string.msgs_mirror_task_started));
            NotificationUtil.setNotificationIcon(mGp, mStwa.util, R.drawable.ic_48_smbsync_run_anim, R.drawable.ic_48_smbsync_run);

            loadLocalFileLastModList();

//            waitMediaScannerConnected();

            mGp.syncThreadCtrl.initThreadCtrl();

            SyncRequestItem sri = mGp.syncRequestQueue.poll();
            boolean sync_error_detected = false;
            int sync_result = 0;
            boolean wifi_off_after_end = false;

            reconnectWifi();

            while (sri != null && sync_result == 0) {
                mStwa.util.addLogMsg("I", String.format(mStwa.context.getString(R.string.msgs_mirror_sync_request_started), sri.request_id_display));
                mGp.syncThreadRequestID = sri.request_id;
                mGp.syncThreadRequestIDdisplay = sri.request_id_display;
                mStwa.util.addDebugMsg(1, "I", "Sync request option : Requestor=" + mGp.syncThreadRequestID +
                        ", WiFi on=" + sri.wifi_on_before_sync_start +
                        ", WiFi delay=" + sri.start_delay_time_after_wifi_on + ", WiFi off=" + sri.wifi_off_after_sync_ended+", OverrideCharge="+sri.overrideSyncOptionCharge);

                boolean wifi_on_issued=performWiFiOnIfRequired(sri);

                mStwa.currentSTI = sri.sync_task_list.poll();

                long start_time = 0;
                while ((sync_result == 0 || sync_result == SyncTaskItem.SYNC_STATUS_WARNING) && mStwa.currentSTI != null) {
                    start_time = System.currentTimeMillis();
                    listSyncOption(mStwa.currentSTI);
                    setSyncTaskRunning(true);
                    showMsg(mStwa, false, mStwa.currentSTI.getSyncTaskName(), "I", "", "", mStwa.context.getString(R.string.msgs_mirror_task_started));

                    initSyncParms(mStwa.currentSTI);

                    String wifi_msg = "";
                    if (mStwa.currentSTI.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) ||
                            mStwa.currentSTI.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                        wifi_msg=isWifiConditionSatisfied(mStwa.currentSTI);
                    } else {
                        mStwa.util.addDebugMsg(1, "I", "WiFi ciondition check bypassed because SMB folder does not used.");
                    }

                    if (wifi_msg.equals("")) {//Continue
                        boolean charge_status=mStwa.currentSTI.isSyncOptionSyncWhenCharging();
                        if (sri.overrideSyncOptionCharge.equals(ScheduleItem.OVERRIDE_SYNC_OPTION_ENABLED)) {
                            charge_status=true;
                            if (mStwa.currentSTI.isSyncOptionSyncWhenCharging()!=charge_status) {
                                mStwa.util.addDebugMsg(1, "I", "Charge staus option was enabled by schedule option.");
                            }
                        } else if (sri.overrideSyncOptionCharge.equals(ScheduleItem.OVERRIDE_SYNC_OPTION_DISABLED)) {
                            charge_status=false;
                            if (mStwa.currentSTI.isSyncOptionSyncWhenCharging()!=charge_status) {
                                mStwa.util.addDebugMsg(1, "I", "Charge staus option was disabled by schedule option.");
                            }
                        }
                        if ((charge_status && CommonUtilities.isCharging(mStwa.context)) || !charge_status) {
                            sync_result = compileFilter(mStwa.currentSTI, mStwa.currentSTI.getFileFilter(), mStwa.currentSTI.getDirFilter());
                            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                sync_result = checkStorageAccess(mStwa.currentSTI);

                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS)
                                    sync_result = performSync(mStwa.currentSTI);
                            }
                        } else {
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                            String be = mStwa.context.getString(R.string.msgs_mirror_sync_cancelled_battery_option_not_satisfied);
                            showMsg(mStwa, true, mStwa.currentSTI.getSyncTaskName(), "E", "", "", be);
                            mGp.syncThreadCtrl.setThreadMessage(be);
                        }
                    } else {//Error
                        if (wifi_msg.equals(mStwa.context.getString(R.string.msgs_mirror_sync_skipped_wifi_ap_conn_other))) {
//                                sync_result=SyncTaskItem.SYNC_STATUS_SUCCESS;
                            sync_result = SyncTaskItem.SYNC_STATUS_WARNING;
                            showMsg(mStwa, true, mStwa.currentSTI.getSyncTaskName(), "W", "", "", wifi_msg);
                            mGp.syncThreadCtrl.setThreadMessage(wifi_msg);
                        } else {
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                            showMsg(mStwa, true, mStwa.currentSTI.getSyncTaskName()+": ", "E", "", "", wifi_msg);
                            mGp.syncThreadCtrl.setThreadMessage(wifi_msg);
                        }
                    }

                    saveLocalFileLastModList();

                    CommonUtilities.saveMsgList(mGp);

                    postProcessSyncResult(mStwa.currentSTI, sync_result, (System.currentTimeMillis() - start_time));

                    mStwa.currentSTI = sri.sync_task_list.poll();
                    if ((mStwa.currentSTI != null || mGp.syncRequestQueue.size() > 0) &&
                            mGp.settingErrorOption && sync_result == SyncHistoryItem.SYNC_STATUS_ERROR) {
                        showMsg(mStwa, false, mStwa.currentSTI.getSyncTaskName(), "W", "", "",
                                mStwa.context.getString(R.string.msgs_mirror_task_result_error_skipped));
                        sync_error_detected = true;
                        sync_result = SyncTaskItem.SYNC_STATUS_SUCCESS;
                    }
                }
                if (sri.wifi_off_after_sync_ended && wifi_on_issued) wifi_off_after_end = true;

                String prev_req_id_display=sri.request_id_display;
                if (sync_result==SyncTaskItem.SYNC_STATUS_CANCEL || sync_result==SyncTaskItem.SYNC_STATUS_ERROR) {
                    //Put not executed sync task
                    SyncTaskItem sti=mStwa.currentSTI;
                    ArrayList<String> task_list=new ArrayList<String>();
                    ArrayList<String> sched_list=new ArrayList<String>();
                    ArrayList<String> req_list_display=new ArrayList<String>();
                    while(sti!=null) {
                        task_list.add(sti.getSyncTaskName());
                        sched_list.add(sri.schedule_name);
                        req_list_display.add(sri.request_id_display);
                        sti=sri.sync_task_list.poll();
                    }
                    sri = mGp.syncRequestQueue.poll();
                    while(sri!=null) {
                        sti=sri.sync_task_list.poll();
                        while(sti!=null) {
                            task_list.add(sti.getSyncTaskName());
                            sched_list.add(sri.schedule_name);
                            req_list_display.add(sri.request_id_display);
                            sti=sri.sync_task_list.poll();
                        }
                        sri = mGp.syncRequestQueue.poll();
                    }
                    if (task_list.size()>0) {
                        mStwa.util.addLogMsg("W",
                                mStwa.context.getString(R.string.msgs_svc_received_start_sync_task_request_accepted_but_not_executed_title));
                        for(int i=0;i<task_list.size();i++) {
                            if (sched_list.get(i).equals("")) mStwa.util.addLogMsg("W", "  "+String.format(mStwa.context.getString(R.string.msgs_svc_received_start_sync_task_request_accepted_but_not_executed_msg), task_list.get(i), req_list_display.get(i)));
                            else mStwa.util.addLogMsg("W", "  "+String.format(mStwa.context.getString(R.string.msgs_svc_received_start_sync_task_request_accepted_but_not_executed_msg_schedule), sched_list.get(i), task_list.get(i), req_list_display.get(i)));
                        }

                    }

                    mStwa.util.addLogMsg("I", String.format(mStwa.context.getString(R.string.msgs_mirror_sync_request_ended), prev_req_id_display));
                } else {
                    //Continue sync
                    mStwa.util.addLogMsg("I", String.format(mStwa.context.getString(R.string.msgs_mirror_sync_request_ended), prev_req_id_display));
                    sri = mGp.syncRequestQueue.poll();
                }
            }

            if (wifi_off_after_end) if (isWifiOn()) {
                mStwa.util.addDebugMsg(1, "I", "WiFi off issued");
                setWifiOff();
            }

            if (sync_error_detected) {
                showMsg(mStwa, false, "", "W", "", "",
                        mStwa.context.getString(R.string.msgs_mirror_task_sync_request_error_detected));
            }

            saveLocalFileLastModList();

            NotificationUtil.setNotificationIcon(mGp, mStwa.util, R.drawable.ic_48_smbsync_wait, R.drawable.ic_48_smbsync_wait);
            NotificationUtil.reShowOngoingMsg(mGp, mStwa.util);

            mGp.syncThreadRequestID = "";
            mGp.syncThreadRequestIDdisplay = "";
            mGp.syncThreadActive = false;

//            mStwa.mediaScanner.disconnect();

            mNotifyToService.notifyToListener(true, new Object[]{sync_result});
        }
        System.gc();
    }

    private boolean performWiFiOnIfRequired(SyncRequestItem sri) {
        boolean wifi_on_issued=false;
        if (Build.VERSION.SDK_INT<=28) {//Android 5/6/7/8/9
            if (sri.wifi_on_before_sync_start) {
                if (!isWifiOn()) {
                    wifi_on_issued=true;
                    setWifiOn();
                    if (sri.start_delay_time_after_wifi_on > 0) {
                        mStwa.util.addLogMsg("I", String.format(mStwa.context.getString(R.string.msgs_mirror_sync_start_was_delayed), sri.start_delay_time_after_wifi_on));
                        SystemClock.sleep(1000 * sri.start_delay_time_after_wifi_on);
                        if (!isWifiOn()) {
                            mStwa.util.addLogMsg("E",mStwa.context.getString(R.string.msgs_mirror_sync_wifi_can_not_enabled));
                        }
                    }
                    mStwa.util.addDebugMsg(1, "I", "WiFi IP Addr="+CommonUtilities.getIfIpAddress("wlan0"));
                }
            }
        } else {
            mStwa.util.addDebugMsg(1, "I", "WiFi On ignored");
        }
        return wifi_on_issued;
    }

    private void setSyncTaskRunning(boolean running) {
        SyncTaskItem c_sti = SyncTaskUtil.getSyncTaskByName(mGp.syncTaskList, mStwa.currentSTI.getSyncTaskName());

        c_sti.setSyncTaskRunning(running);

        if (running) openSyncResultLog(c_sti);
        else closeSyncResultLog();

        refreshSyncTaskListAdapter();
    }

    private void listSyncOption(SyncTaskItem sti) {
        mStwa.util.addDebugMsg(1, "I", "Sync Task : Type=" + sti.getSyncTaskType());
        String mst_uid="";
//        if (mGp.settingSecurityReinitSmbAccountPasswordValue) mst_uid=sti.getMasterSmbUserName().equals("")?"":"????????";
//        else mst_uid=sti.getMasterSmbUserName();
        mst_uid=sti.getMasterSmbUserName().equals("")?"":"????????";
        mStwa.util.addDebugMsg(1, "I", "   Master Type=" + sti.getMasterFolderType() +
                ", Addr=" + sti.getMasterSmbAddr() +
                ", Hostname=" + sti.getMasterSmbHostName() +
                ", Port=" + sti.getMasterSmbPort() +
                ", SmbShare=" + sti.getMasterSmbShareName() +
                ", UserID=" + mst_uid +
                ", Directory=" + sti.getMasterDirectoryName() +
                ", SMB Protocol=" + sti.getMasterSmbProtocol() +
                ", SMB IPC signing enforced=" + sti.isMasterSmbIpcSigningEnforced() +
                ", SMB Use SMB2 Negotiation=" + sti.isMasterSmbUseSmb2Negotiation() +
                ", RemovableID=" + sti.getMasterRemovableStorageID() +
                ", MountPoint=" + sti.getMasterLocalMountPoint() +
                "");
        String tgt_uid="";
//        if (mGp.settingSecurityReinitSmbAccountPasswordValue) tgt_uid=sti.getTargetSmbUserName().equals("")?"":"????????";
//        else tgt_uid=sti.getTargetSmbUserName();
        tgt_uid=sti.getTargetSmbUserName().equals("")?"":"????????";
        mStwa.util.addDebugMsg(1, "I", "   Target Type=" + sti.getTargetFolderType() +
                        ", Addr=" + sti.getTargetSmbAddr() +
                        ", Hostname=" + sti.getTargetSmbHostName() +
                        ", Port=" + sti.getTargetSmbPort() +
                        ", SmbShare=" + sti.getTargetSmbShareName() +
                        ", UserID=" + tgt_uid +
                        ", Directory=" + sti.getTargetDirectoryName() +
                        ", SMB Protocol=" + sti.getTargetSmbProtocol() +
                        ", SMB IPC signing enforced=" + sti.isTargetSmbIpcSigningEnforced() +
                        ", SMB Use SMB2 Negotiation=" + sti.isTargetSmbUseSmb2Negotiation() +
                        ", RemovableID=" + sti.getTargetRemovableStorageID() +
                        ", MountPoint=" + sti.getTargetLocalMountPoint() +
                        ", UseTakenDateTime=" + sti.isTargetUseTakenDateTimeToDirectoryNameKeyword(),
                "");
        mStwa.util.addDebugMsg(1, "I", "   File filter Audio=" + sti.isSyncFileTypeAudio() +
                ", Image=" + sti.isSyncFileTypeImage() +
                ", Video=" + sti.isSyncFileTypeVideo() +
                "");
        mStwa.util.addDebugMsg(1, "I", "   Confirm=" + sti.isSyncConfirmOverrideOrDelete() ,
                ", LastModSmbsync2=" + sti.isSyncDetectLastModifiedBySmbsync() ,
                ", UseLastMod=" + sti.isSyncOptionDifferentFileByTime() ,
                ", UseFileSize=" + sti.isSyncOptionDifferentFileBySize() ,
                ", UseFileSizeGreaterThanTagetFile=" + sti.isSyncDifferentFileSizeGreaterThanTagetFile() ,
                ", DoNotResetFileLastMod=" + sti.isSyncDoNotResetFileLastModified() ,
                ", SyncEmptyDir=" + sti.isSyncOptionSyncEmptyDirectory() ,
                ", SyncHiddenDir=" + sti.isSyncOptionSyncHiddenDirectory() ,
                ", SyncProcessOverride=" + sti.isSyncOverrideCopyMoveFile() ,
                ", ProcessRootDirFile=" + sti.isSyncProcessRootDirFile() ,
                ", SyncSubDir=" + sti.isSyncOptionSyncSubDirectory() ,
                ", AutoSync=" + sti.isSyncTaskAuto() ,
                ", TestMode=" + sti.isSyncTestMode() ,
//                ", UseTempName=" + sti.isSyncUseFileCopyByTempName() ,
                ", UseSmallBuffer=" + sti.isSyncOptionUseSmallIoBuffer() ,
                ", AllowableTime=" + sti.getSyncOptionDifferentFileAllowableTime() ,
                ", RetryCount=" + sti.getSyncOptionRetryCount() ,
                ", WiFi Status Option=" + sti.getSyncOptionWifiStatusOption(),
                ", UseExtendedDirectoryFilter1=" + sti.isSyncOptionUseExtendedDirectoryFilter1() ,
                ", SkipIfConnectAnotherWifiSsid=" + sti.isSyncOptionTaskSkipIfConnectAnotherWifiSsid() ,
                ", SyncOnlyCharging=" + sti.isSyncOptionSyncWhenCharging() ,
                ", DeleteFirst=" + sti.isSyncOptionDeleteFirstWhenMirror() ,

                ", IgnoreDstDifference=" + sti.isSyncOptionIgnoreDstDifference(),
                ", OffsetOfDst=" + sti.getSyncOptionOffsetOfDst(),
                ", NeverOverwriteTargetFileIfItIsNewerThanTheMasterFile="+sti.isSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile(),
                ", IgnoreUnusableCharacterUsedDirectoryFileName="+sti.isSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters(),
                ", DoNotUseRenameWhenSmbFileWrite=" + sti.isSyncOptionDoNotUseRenameWhenSmbFileWrite() ,
                "");
        mStwa.util.addDebugMsg(1, "I", "   SMB1 Option, LM Compatiibility=" + mGp.settingsSmbLmCompatibility +
                ", Use extended security=" + mGp.settingsSmbUseExtendedSecurity +
                ", Client reponse timeout=" + mGp.settingsSmbClientResponseTimeout +
                ", Disable plain text passwords=" + mGp.settingsSmbDisablePlainTextPasswords +
                "");
    }

    private void initSyncParms(SyncTaskItem sti) {
        String mst_dom=null, mst_user=null, mst_pass=null;
        mst_dom=sti.getMasterSmbDomain().equals("")?null:sti.getMasterSmbDomain();
        mst_user=sti.getMasterSmbUserName().equals("")?null:sti.getMasterSmbUserName();
        mst_pass=sti.getMasterSmbPassword().equals("")?null:sti.getMasterSmbPassword();
        int mst_smb_level = Integer.parseInt(sti.getMasterSmbProtocol());
        if (sti.getMasterSmbProtocol().equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1)) {
            mStwa.masterAuth=new JcifsAuth(JcifsAuth.JCIFS_FILE_SMB1, mst_dom, mst_user, mst_pass);
        } else {
            mStwa.masterAuth=new JcifsAuth(mst_smb_level, mst_dom, mst_user, mst_pass, sti.isMasterSmbIpcSigningEnforced(), sti.isMasterSmbUseSmb2Negotiation());
        }

        String tgt_dom=null, tgt_user=null, tgt_pass=null;
        tgt_dom=sti.getTargetSmbDomain().equals("")?null:sti.getTargetSmbDomain();
        tgt_user=sti.getTargetSmbUserName().equals("")?null:sti.getTargetSmbUserName();
        tgt_pass=sti.getTargetSmbPassword().equals("")?null:sti.getTargetSmbPassword();
        int tgt_smb_level = Integer.parseInt(sti.getTargetSmbProtocol());
        if (sti.getTargetSmbProtocol().equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1)) {
            mStwa.targetAuth=new JcifsAuth(JcifsAuth.JCIFS_FILE_SMB1, tgt_dom, tgt_user, tgt_pass);
        } else {
            mStwa.targetAuth=new JcifsAuth(tgt_smb_level, tgt_dom, tgt_user, tgt_pass, sti.isTargetSmbIpcSigningEnforced(), sti.isTargetSmbUseSmb2Negotiation());
        }

        mStwa.syncTaskRetryCount = mStwa.syncTaskRetryCountOriginal = Integer.parseInt(sti.getSyncOptionRetryCount()) + 1;
        mStwa.syncDifferentFileAllowableTime = sti.getSyncOptionDifferentFileAllowableTime() * 1000;//Convert to milisec
        mStwa.offsetOfDaylightSavingTime=sti.getSyncOptionOffsetOfDst()*60*1000;//Convert to milisec

        mStwa.totalTransferByte = mStwa.totalTransferTime = 0;
        mStwa.totalCopyCount = mStwa.totalDeleteCount = mStwa.totalIgnoreCount = mStwa.totalRetryCount = 0;

        if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            if (sti.isSyncDetectLastModifiedBySmbsync()) mStwa.lastModifiedIsFunctional = false;
            else
                mStwa.lastModifiedIsFunctional = isSetLastModifiedFunctional(mStwa.gp.internalRootDirectory);
//		} else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
//			mStwa.lastModifiedIsFunctional=false;
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            mStwa.lastModifiedIsFunctional = true;
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            if (Build.VERSION.SDK_INT>=24) {
                mStwa.lastModifiedIsFunctional = isSetLastModifiedFunctional(mStwa.gp.safMgr.getSdcardRootPath());
            } else {
                mStwa.lastModifiedIsFunctional = false;
            }
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
            if (Build.VERSION.SDK_INT>=24) {
                mStwa.lastModifiedIsFunctional = isSetLastModifiedFunctional(mStwa.gp.safMgr.getUsbRootPath());
            } else {
                mStwa.lastModifiedIsFunctional = false;
            }
        } else mStwa.lastModifiedIsFunctional = false;
        mStwa.util.addDebugMsg(1, "I", "lastModifiedIsFunctional=" + mStwa.lastModifiedIsFunctional);
    }

    private void postProcessSyncResult(SyncTaskItem sti, int sync_result, long et) {
        long msecs = et;
        long hr = TimeUnit.MILLISECONDS.toHours(msecs); msecs -= TimeUnit.HOURS.toMillis(hr);
        long min = TimeUnit.MILLISECONDS.toMinutes(msecs); msecs -= TimeUnit.MINUTES.toMillis(min);
        long sec = TimeUnit.MILLISECONDS.toSeconds(msecs); msecs -= TimeUnit.SECONDS.toMillis(sec);
        long ms = TimeUnit.MILLISECONDS.toMillis(msecs);

        String sync_et = null;
        if (hr != 0) sync_et = String.format("%02d:%02d:%02d", hr, min, sec);
        else if (min != 0) sync_et = String.format("%d min %d.%03d sec", min, sec, ms);
        else sync_et = String.format("%d.%03d sec", sec, ms);

        String error_msg = "";
        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR || sync_result == SyncTaskItem.SYNC_STATUS_WARNING) {
            error_msg = mGp.syncThreadCtrl.getThreadMessage();
        }
//		if (!error_msg.equals("")) {
//			if (mStwa.syncHistoryWriter!=null) {
//				String print_msg="";
//				print_msg=mStwa.util.buildPrintMsg("E", sti.getSyncTaskName(),": ",error_msg);
//				mStwa.syncHistoryWriter.println(print_msg);
//			}
//		}
        String transfer_rate = calTransferRate(mStwa.totalTransferByte, mStwa.totalTransferTime);
        addHistoryList(sti, sync_result,
                mStwa.totalCopyCount, mStwa.totalDeleteCount, mStwa.totalIgnoreCount, mStwa.totalRetryCount,
                error_msg, sync_et, transfer_rate);
//		if (!error_msg.equals("")) showMsg(mStca, false,sti.getSyncTaskName(),"E", "","",error_msg);

        showMsg(mStwa, true, sti.getSyncTaskName(), "I", "", "",
                String.format(mStwa.context.getString(R.string.msgs_mirror_task_no_of_copy),
                        mStwa.totalCopyCount, mStwa.totalDeleteCount, mStwa.totalIgnoreCount, sync_et));
        showMsg(mStwa, true, sti.getSyncTaskName(), "I", "", "",
                String.format(mStwa.context.getString(R.string.msgs_mirror_task_avg_rate),
                        transfer_rate));

        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
            showMsg(mStwa, false, sti.getSyncTaskName(), "I", "", "", mStwa.context.getString(R.string.msgs_mirror_task_result_ok));
        } else if (sync_result == SyncTaskItem.SYNC_STATUS_WARNING) {
            showMsg(mStwa, false, sti.getSyncTaskName(), "I", "", "", mStwa.context.getString(R.string.msgs_mirror_task_result_ok));
        } else if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL) {
            showMsg(mStwa, false, sti.getSyncTaskName(), "I", "", "", mStwa.context.getString(R.string.msgs_mirror_task_result_cancel));
        } else if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
            showMsg(mStwa, false, sti.getSyncTaskName(), "E", "", "",
                    mStwa.context.getString(R.string.msgs_mirror_task_result_error_ended));
        }

        setSyncTaskRunning(false);
        SyncTaskUtil.saveSyncTaskListToFile(mGp, mStwa.context, mStwa.util, false, "", "", mGp.syncTaskList, false);

    }

    private void loadLocalFileLastModList() {
        mStwa.localFileLastModListModified = false;
        NotifyEvent ntfy = new NotifyEvent(mStwa.context);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
                String en = (String) o[0];
                mStwa.util.addLogMsg("W", "Duplicate local file last modified entry was ignored, name=" + en);
            }
        });
        FileLastModifiedTime.loadLastModifiedList(mGp.settingMgtFileDir, mStwa.currLastModifiedList, mStwa.newLastModifiedList, ntfy);
    }

    private void saveLocalFileLastModList() {
        if (mStwa.localFileLastModListModified) {
            long b_time = System.currentTimeMillis();
            mStwa.localFileLastModListModified = false;
            FileLastModifiedTime.saveLastModifiedList(mGp.settingMgtFileDir, mStwa.currLastModifiedList, mStwa.newLastModifiedList);
            mStwa.util.addDebugMsg(1, "I", "saveLastModifiedList elapsed time=" + (System.currentTimeMillis() - b_time));
        }
    }

    private int checkStorageAccess(SyncTaskItem sti) {
        int sync_result = 0;
        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD) ||
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                String e_msg = "";
                if (mGp.safMgr.hasExternalSdcardPath()) {
                    e_msg = mStwa.context.getString(R.string.msgs_mirror_external_sdcard_select_required);
                } else {
                    e_msg = mStwa.context.getString(R.string.msgs_mirror_external_sdcard_not_mounted);
                }
                showMsg(mStwa, true, sti.getSyncTaskName(), "E", "", "", e_msg);
                mGp.syncThreadCtrl.setThreadMessage(e_msg);
                return sync_result;
            } else if (mGp.safMgr.getSdcardRootSafFile() == null) {
                sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                showMsg(mStwa, true, sti.getSyncTaskName(), "E", "", "",
                        mStwa.context.getString(R.string.msgs_mirror_external_sdcard_select_required));
                mGp.syncThreadCtrl.setThreadMessage(
                        mStwa.context.getString(R.string.msgs_mirror_external_sdcard_select_required));
                return sync_result;
            }
        }
        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB) ||
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
            if (mGp.safMgr.getUsbRootPath().equals(SafManager.UNKNOWN_USB_DIRECTORY)) {
                sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                String e_msg = "";
                e_msg = mStwa.context.getString(R.string.msgs_mirror_usb_storage_not_mounted);
                showMsg(mStwa, true, sti.getSyncTaskName(), "E", "", "", e_msg);
                mGp.syncThreadCtrl.setThreadMessage(e_msg);
                return sync_result;
            } else if (mGp.safMgr.getUsbRootSafFile() == null) {
                sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                showMsg(mStwa, true, sti.getSyncTaskName(), "E", "", "",
                        mStwa.context.getString(R.string.msgs_mirror_usb_storage_not_mounted));
                mGp.syncThreadCtrl.setThreadMessage(
                        mStwa.context.getString(R.string.msgs_mirror_usb_storage_not_mounted));
                return sync_result;
            }
        }
        if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP) &&
                sti.isTargetZipUseExternalSdcard()) {
            if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                String e_msg = "";
                if (mGp.safMgr.hasExternalSdcardPath()) {
                    e_msg = mStwa.context.getString(R.string.msgs_mirror_external_sdcard_select_required);
                } else {
                    e_msg = mStwa.context.getString(R.string.msgs_mirror_external_sdcard_not_mounted);
                }
                showMsg(mStwa, true, sti.getSyncTaskName(), "E", "", "", e_msg);
                mGp.syncThreadCtrl.setThreadMessage(e_msg);
                return sync_result;
            } else if (mGp.safMgr.getSdcardRootSafFile() == null) {
                sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                showMsg(mStwa, true, sti.getSyncTaskName(), "E", "", "",
                        mStwa.context.getString(R.string.msgs_mirror_external_sdcard_select_required));
                mGp.syncThreadCtrl.setThreadMessage(
                        mStwa.context.getString(R.string.msgs_mirror_external_sdcard_select_required));
                return sync_result;
            }
        }

        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            String addr=null;
//            if (sti.getMasterSmbHostName().equals("") && !sti.getMasterSmbHostName().equals("")) {
            if (!sti.getMasterSmbHostName().equals("")) {
                addr = CommonUtilities.resolveHostName(mGp, mStwa.util, mStwa.masterAuth.getSmbLevel(), sti.getMasterSmbHostName());
                if (addr == null) {
                    String msg = mStwa.context.getString(R.string.msgs_mirror_remote_name_not_found) +
                            sti.getMasterSmbHostName();
                    mStwa.util.addLogMsg("E", "", msg);
                    mGp.syncThreadCtrl.setThreadMessage(msg);
                    sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                    return sync_result;
                }
            } else {
                addr=sti.getMasterSmbAddr();
            }
            InetAddress ia=CommonUtilities.getInetAddress(addr);
            if ((ia instanceof  Inet6Address)) mStwa.masterSmbAddress="["+CommonUtilities.addScopeidToIpv6Address(addr)+"]";
            else mStwa.masterSmbAddress=addr;
            if (sti.getMasterSmbPort().equals("")) {
                if (!isIpaddressConnectable(addr, 445) && !isIpaddressConnectable(addr, 139)) {
                    sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                    showMsg(mStwa, true, sti.getSyncTaskName(), "E", "", "",
                            String.format(mStwa.context.getString(R.string.msgs_mirror_remote_addr_not_connected), addr));
                    mGp.syncThreadCtrl.setThreadMessage(
                            String.format(mStwa.context.getString(R.string.msgs_mirror_remote_addr_not_connected), addr));
                    return sync_result;
                }
            } else {
                int port = Integer.parseInt(sti.getMasterSmbPort());
                if (!isIpaddressConnectable(addr, port)) {
                    sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                    showMsg(mStwa, true, sti.getSyncTaskName(), "E", "", "",
                            String.format(mStwa.context.getString(R.string.msgs_mirror_remote_addr_not_connected_with_port), addr, port));
                    mGp.syncThreadCtrl.setThreadMessage(
                            String.format(mStwa.context.getString(R.string.msgs_mirror_remote_addr_not_connected_with_port), addr, port));
                    return sync_result;
                }
            }
        }
        if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            String addr=null;
            if (!sti.getTargetSmbHostName().equals("")) {
                addr = CommonUtilities.resolveHostName(mGp, mStwa.util, mStwa.targetAuth.getSmbLevel(), sti.getTargetSmbHostName());
                if (addr == null) {
                    String msg = mStwa.context.getString(R.string.msgs_mirror_remote_name_not_found) + sti.getTargetSmbHostName();
                    mStwa.util.addLogMsg("E", "", msg);
                    mGp.syncThreadCtrl.setThreadMessage(msg);
                    sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                    return sync_result;
                }
            } else {
                addr=sti.getTargetSmbAddr();
            }
            if (CommonUtilities.isIpAddressV6(addr)) mStwa.targetSmbAddress="["+CommonUtilities.addScopeidToIpv6Address(addr)+"]";
            else mStwa.targetSmbAddress=addr;
            if (sti.getTargetSmbPort().equals("")) {
                if (!isIpaddressConnectable(addr, 445) && !isIpaddressConnectable(addr, 139)) {
                    sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                    showMsg(mStwa, true, sti.getSyncTaskName(), "E", "", "",
                            String.format(mStwa.context.getString(R.string.msgs_mirror_remote_addr_not_connected), addr));
                    mGp.syncThreadCtrl.setThreadMessage(
                            String.format(mStwa.context.getString(R.string.msgs_mirror_remote_addr_not_connected), addr));
                    return sync_result;
                }
            } else {
                int port = Integer.parseInt(sti.getTargetSmbPort());
                if (!isIpaddressConnectable(addr, port)) {
                    sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                    showMsg(mStwa, true, sti.getSyncTaskName(), "E", "", "",
                            String.format(mStwa.context.getString(R.string.msgs_mirror_remote_addr_not_connected_with_port), addr, port));
                    mGp.syncThreadCtrl.setThreadMessage(
                            String.format(mStwa.context.getString(R.string.msgs_mirror_remote_addr_not_connected_with_port), addr, port));
                    return sync_result;
                }
            }
        }

        return sync_result;
    }

    private boolean isIpaddressConnectable(String addr, int port) {
//        int cnt = 3;
        boolean result = false;
        result = isIpAddressAndPortConnected(addr, port, 3500);//1000);
//        while (cnt > 0) {
//            result = isIpAddressAndPortConnected(addr, port, 3500);//1000);
//            if (result) break;
//            cnt--;
//        }
        return result;
    }

    final public boolean isIpAddressAndPortConnected(String address, int port, int timeout) {
        boolean reachable = false;
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(address, port)), timeout);
//            OutputStream os=socket.getOutputStream();
//            os.write(mNbtData);
//            os.flush();
//            os.close();
            reachable = true;
            socket.close();
        } catch (IOException e) {
//            ArrayList<String> el=new ArrayList<String>();
//            el.add(e.getMessage());
//            if (e.getCause()!=null) el.add(e.getCause().getMessage());
//            for(String em:el) {
//                if (em.startsWith("isConnected failed: ECONNREFUSED (Connection refused)")) return true;
//            }
            mStwa.util.addDebugMsg(1, "I", e.getMessage());
            for (StackTraceElement ste:e.getStackTrace()) mStwa.util.addDebugMsg(1, "I", ste.toString());
        } catch (Exception e) {
//            e.printStackTrace();
            mStwa.util.addDebugMsg(1, "I", e.getMessage());
            for (StackTraceElement ste:e.getStackTrace()) mStwa.util.addDebugMsg(1, "I", ste.toString());
        }
        return reachable;
    }

    // Default uncaught exception handler variable
    private UncaughtExceptionHandler defaultUEH;

    private UncaughtExceptionHandler unCaughtExceptionHandler =
            new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Thread.currentThread().setUncaughtExceptionHandler(defaultUEH);
                    NotificationUtil.setNotificationIcon(mGp, mStwa.util, R.drawable.ic_48_smbsync_wait, R.drawable.ic_48_smbsync_wait);
                    ex.printStackTrace();
                    StackTraceElement[] st = ex.getStackTrace();
                    String st_msg = "";
                    for (int i = 0; i < st.length; i++) {
                        st_msg += "\n at " + st[i].getClassName() + "." +
                                st[i].getMethodName() + "(" + st[i].getFileName() +
                                ":" + st[i].getLineNumber() + ")";
                    }
                    mGp.syncThreadCtrl.setThreadResultError();
                    String end_msg = ex.toString() + st_msg;
                    if (mStwa.gp.safMgr != null) {
                        String saf_msg=mStwa.gp.safMgr.getLastErrorMessage();
                        if (saf_msg.length()>0) end_msg += "\n\nSafManager Messages\n" + saf_msg;

                        end_msg += "\n" + "getSdcardRootPath=" + mGp.safMgr.getSdcardRootPath();
                        end_msg += "\n" + "getUsbRootPath=" + mGp.safMgr.getUsbRootPath();

//                        File[] fl = ContextCompat.getExternalFilesDirs(mStwa.context, null);
                        File[] fl = mStwa.context.getExternalFilesDirs(null);
                        if (fl != null) {
                            for (File f : fl) {
                                if (f != null) end_msg += "\n" + "ExternalFilesDirs=" + f.getPath();
                            }
                        }
                        if (mGp.safMgr.getSdcardRootSafFile() != null)
                            end_msg += "\n" + "getSdcardSafFile name=" + mGp.safMgr.getSdcardRootSafFile().getName();

                        if (mGp.safMgr.getUsbRootSafFile() != null)
                            end_msg += "\n" + "getUsbSafFile name=" + mGp.safMgr.getUsbRootSafFile().getName();
                    }

                    mGp.syncThreadCtrl.setThreadMessage(end_msg);
                    showMsg(mStwa, true, "", "E", "", "", end_msg);
                    showMsg(mStwa, false, "", "E", "", "",
                            mStwa.context.getString(R.string.msgs_mirror_task_result_error_ended));

                    if (mStwa.currentSTI != null) {
                        addHistoryList(mStwa.currentSTI, SyncHistoryItem.SYNC_STATUS_ERROR,
                                mStwa.totalCopyCount, mStwa.totalDeleteCount, mStwa.totalIgnoreCount, mStwa.totalRetryCount,
                                end_msg, "", "");
//        			mUtil.saveHistoryList(mGp.syncHistoryList);
                        setSyncTaskRunning(false);
                    }
                    mGp.syncThreadCtrl.setDisabled();
                    mGp.syncThreadRequestID = "";
                    mGp.syncThreadRequestIDdisplay = "";

                    mGp.syncThreadActive = false;
                    mGp.dialogWindowShowed = false;
                    mGp.syncRequestQueue.clear();

                    SyncTaskUtil.saveSyncTaskListToFile(mGp, mStwa.context, mStwa.util, false, "", "", mGp.syncTaskList, false);

                    mNotifyToService.notifyToListener(false, null);
                    // re-throw critical exception further to the os (important)
//                defaultUEH.uncaughtException(thread, ex);
                }
            };

    private void refreshSyncTaskListAdapter() {
        mGp.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mGp.syncTaskAdapter != null) {
                    int run_task = -1;
                    for (int i = 0; i < mGp.syncTaskList.size(); i++)
                        if (mGp.syncTaskList.get(i).isSyncTaskRunning()) run_task = i;
                    mGp.syncTaskAdapter.notifyDataSetChanged();
                    mGp.syncTaskListView.setSelection(run_task);
                }
            }
        });
    }

    private static boolean isreplaceKeywordRequiredAtWhileSync(String fp) {
        boolean result=false;

        if (fp.indexOf(SMBSYNC2_REPLACEABLE_KEYWORD_YEAR)>=0) result=true;
        if (fp.indexOf(SMBSYNC2_REPLACEABLE_KEYWORD_MONTH)>=0) result=true;
        if (fp.indexOf(SMBSYNC2_REPLACEABLE_KEYWORD_DAY)>=0) result=true;
        if (fp.indexOf(SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR)>=0) result=true;

        return result;
    }

    private int performSync(SyncTaskItem sti) {
        int sync_result = 0;
        long time_millis = System.currentTimeMillis();
        String from, to, to_temp;
        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            from = buildStorageDir(sti.getMasterLocalMountPoint(), sti.getMasterDirectoryName());
            to_temp = buildStorageDir(sti.getTargetLocalMountPoint(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync Internal-To-Internal From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyInternalToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveInternalToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorInternalToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveInternalToInternal(mStwa, sti, from, to);
//            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_SYNC)) {
//                sync_result = TwoWaySyncFile.syncTwowayInternalToInternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
            from = buildStorageDir(sti.getMasterLocalMountPoint(), sti.getMasterDirectoryName());
            to_temp = sti.getTargetLocalMountPoint() + sti.getTargetZipOutputFileName();

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync Internal-To-ZIP From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                String to_fn=replaceKeywordValue(sti.getTargetZipOutputFileName(), time_millis);
                sync_result = SyncThreadSyncZip.syncCopyInternalToInternalZip(mStwa, sti, from, to_fn);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                String to_fn=replaceKeywordValue(sti.getTargetZipOutputFileName(), time_millis);
                sync_result = SyncThreadSyncZip.syncMoveInternalToInternalZip(mStwa, sti, from, to_fn);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                String to_fn=replaceKeywordValue(sti.getTargetZipOutputFileName(), time_millis);
                sync_result = SyncThreadSyncZip.syncMirrorInternalToInternalZip(mStwa, sti, from, to_fn);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                showMsg(mStwa, false, sti.getSyncTaskName(), "W", "", "" ,mStwa.context.getString(R.string.msgs_sync_folder_archive_zip_folder_not_supported_error));
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            from = buildStorageDir(sti.getMasterLocalMountPoint(), sti.getMasterDirectoryName());
            to_temp = buildStorageDir(mGp.safMgr.getSdcardRootPath(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync Internal-To-SDCARD From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyInternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveInternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorInternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveInternalToExternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
            from = buildStorageDir(sti.getMasterLocalMountPoint(), sti.getMasterDirectoryName());
            to_temp = buildStorageDir(mGp.safMgr.getUsbRootPath(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync Internal-To-USB From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyInternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveInternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorInternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveInternalToExternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            //Internal to SMB
            from = buildStorageDir(sti.getMasterLocalMountPoint(), sti.getMasterDirectoryName());
            to_temp = buildSmbHostUrl(mStwa.targetSmbAddress,
                    sti.getTargetSmbPort(), sti.getTargetSmbShareName(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);

            mStwa.util.addDebugMsg(1, "I", "Sync Internal-To-SMB From=" + from + ", To=" + to);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyInternalToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveInternalToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorInternalToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveInternalToSmb(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            //External to Internal
            from = buildStorageDir(mGp.safMgr.getSdcardRootPath(), sti.getMasterDirectoryName());
            to_temp = buildStorageDir(sti.getTargetLocalMountPoint(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);

            mStwa.util.addDebugMsg(1, "I", "Sync SDCARD-To-Internal From=" + from + ", To=" + to);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyExternalToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveExternalToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorExternalToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveExternalToInternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            //External to Internal
            from = buildStorageDir(mGp.safMgr.getUsbRootPath(), sti.getMasterDirectoryName());
            to_temp = buildStorageDir(sti.getTargetLocalMountPoint(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync USB-To-Internal From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyExternalToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveExternalToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorExternalToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveExternalToInternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            //External to External
            from = buildStorageDir(mGp.safMgr.getSdcardRootPath(), sti.getMasterDirectoryName());
            to_temp = buildStorageDir(mGp.safMgr.getSdcardRootPath(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);

            mStwa.util.addDebugMsg(1, "I", "Sync SDCARD-To-SDCARD From=" + from + ", To=" + to);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveExternalToExternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
            //External to External
            from = buildStorageDir(mGp.safMgr.getSdcardRootPath(), sti.getMasterDirectoryName());
            to_temp = buildStorageDir(mGp.safMgr.getUsbRootPath(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);

            mStwa.util.addDebugMsg(1, "I", "Sync SDCARD-To-USB From=" + from + ", To=" + to);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveExternalToExternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
            //External to External
            from = buildStorageDir(mGp.safMgr.getUsbRootPath(), sti.getMasterDirectoryName());
            to_temp = buildStorageDir(mGp.safMgr.getUsbRootPath(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync USB-To-USB From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveExternalToExternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            //External to External
            from = buildStorageDir(mGp.safMgr.getUsbRootPath(), sti.getMasterDirectoryName());
            to_temp = buildStorageDir(mGp.safMgr.getSdcardRootPath(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync USB-To-SDCARD From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorExternalToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveExternalToExternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            //External to SMB
            from = buildStorageDir(mGp.safMgr.getSdcardRootPath(), sti.getMasterDirectoryName());

            to_temp = buildSmbHostUrl(mStwa.targetSmbAddress,
                    sti.getTargetSmbPort(), sti.getTargetSmbShareName(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync SDCARD-To-SMB From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyExternalToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveExternalToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorExternalToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveExternalToSmb(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            //External to SMB
            from = buildStorageDir(mGp.safMgr.getUsbRootPath(), sti.getMasterDirectoryName());

            to_temp = buildSmbHostUrl(sti.getTargetSmbAddr(),
                    sti.getTargetSmbPort(), sti.getTargetSmbShareName(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync USB-To-SMB From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopyExternalToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveExternalToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorExternalToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveExternalToSmb(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            //External to Internal
            from = buildSmbHostUrl(mStwa.masterSmbAddress,
                    sti.getMasterSmbPort(), sti.getMasterSmbShareName(), sti.getMasterDirectoryName()) + "/";

            to_temp = buildStorageDir(sti.getTargetLocalMountPoint(), sti.getTargetDirectoryName());

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync SMB-To-Internal From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopySmbToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveSmbToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorSmbToInternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveSmbToInternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            //External to External
            to_temp = buildStorageDir(mGp.safMgr.getSdcardRootPath(), sti.getTargetDirectoryName());

            from = buildSmbHostUrl(mStwa.masterSmbAddress,
                    sti.getMasterSmbPort(), sti.getMasterSmbShareName(), sti.getMasterDirectoryName()) + "/";

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync SMB-To-SDCARD From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopySmbToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveSmbToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorSmbToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveSmbToExternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
            //External to External
            to_temp = buildStorageDir(mGp.safMgr.getUsbRootPath(), sti.getTargetDirectoryName());

            from = buildSmbHostUrl(mStwa.masterSmbAddress,
                    sti.getMasterSmbPort(), sti.getMasterSmbShareName(), sti.getMasterDirectoryName()) + "/";

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync SMB-To-USB From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopySmbToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveSmbToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorSmbToExternal(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveSmbToExternal(mStwa, sti, from, to);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            //External to External
            to_temp = buildSmbHostUrl(mStwa.targetSmbAddress,
                    sti.getTargetSmbPort(), sti.getTargetSmbShareName(), sti.getTargetDirectoryName()) + "/";

            from = buildSmbHostUrl(mStwa.masterSmbAddress,
                    sti.getMasterSmbPort(), sti.getMasterSmbShareName(), sti.getMasterDirectoryName()) + "/";

            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) to = to_temp;//replaceKeywordValue(to_temp, time_millis);
            else to = replaceKeywordValue(to_temp, time_millis);
            mStwa.replaceKeywordRequiredAtWhileSync=isreplaceKeywordRequiredAtWhileSync(to);

            mStwa.util.addDebugMsg(1, "I", "Sync SMB-To-SMB From=" + from + ", To=" + to);

            if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) {
                sync_result = SyncThreadSyncFile.syncCopySmbToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
                sync_result = SyncThreadSyncFile.syncMoveSmbToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) {
                sync_result = SyncThreadSyncFile.syncMirrorSmbToSmb(mStwa, sti, from, to);
            } else if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
                sync_result = SyncThreadArchiveFile.syncArchiveSmbToSmb(mStwa, sti, from, to);
            }
        }
        return sync_result;
    }

    static public String replaceKeywordValue(String replaceable_string, Long time_millis) {
        String c_date = StringUtil.convDateTimeTo_YearMonthDayHourMin(time_millis);
        String c_date_yyyy = c_date.substring(0, 4);
        String c_date_mm = c_date.substring(5, 7);
        String c_date_dd = c_date.substring(8, 10);
        SimpleDateFormat sdf = new SimpleDateFormat("DDD");
        Date date = new Date();
        date.setTime(time_millis);
        String day_of_year = sdf.format(date);

        String to_temp = null;
        to_temp = replaceable_string.replaceAll(SMBSYNC2_REPLACEABLE_KEYWORD_YEAR, c_date_yyyy)
                .replaceAll(SMBSYNC2_REPLACEABLE_KEYWORD_MONTH, c_date_mm)
                .replaceAll(SMBSYNC2_REPLACEABLE_KEYWORD_DAY, c_date_dd)
                .replaceAll(SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR, day_of_year);
//        Log.v("","org="+replaceable_string+", after="+to_temp);
        return to_temp;
    }

    private String buildStorageDir(String base, String dir) {
        if (dir.equals("")) return base;
        else {
            if (dir.startsWith("/")) return base + dir;
            else return base + "/" + dir;
        }
    }

    private String buildSmbHostUrl(String addr, String port, String share, String dir) {
        String result = "";
        String smb_host = "smb://";
//        if (!addr.equals("")) smb_host = smb_host + addr;
//        else smb_host = smb_host + hostname;
        smb_host = smb_host + addr;
        if (!port.equals("")) smb_host = smb_host + ":" + port;
        smb_host = smb_host + "/" + share;
        if (!dir.equals("")) {
            if (dir.startsWith("/")) result = smb_host + dir;
            else result = smb_host + "/" + dir;
        } else {
            result = smb_host;
        }
        return result;
    }

    public static String removeInvalidCharForFileDirName(String in_str) {
        String out = in_str.replaceAll(":", "")
                .replaceAll("\\\\", "")
                .replaceAll("\\*", "")
                .replaceAll("\\?", "")
                .replaceAll("\"", "")
                .replaceAll("<", "")
                .replaceAll(">", "")
                .replaceAll("|", "");
        return out;
    }

    public static String hasInvalidCharForFileDirName(String in_str) {
        if (in_str.contains(":")) return ":";
        if (in_str.contains("\\")) return "\\";
        if (in_str.contains("*")) return "*";
        if (in_str.contains("?")) return "?";
        if (in_str.contains("\"")) return "\"";
        if (in_str.contains("<")) return "<";
        if (in_str.contains(">")) return ">";
        if (in_str.contains("|")) return "|";
        if (in_str.contains("\n")) return "CR";
        if (in_str.contains("\t")) return "TAB";
        String printable=in_str.replaceAll("\\p{C}", "");
        if (in_str.length()!=printable.length()) return "UNPRINTABLE";
        return "";
    }

    public static boolean isValidFileDirectoryName(SyncThreadWorkArea stwa, SyncTaskItem sti, String in_str) {
        String invalid_char = hasInvalidCharForFileDirName(in_str);
        if (!invalid_char.equals("")) {
            String basename = in_str.substring(in_str.lastIndexOf("/") + 1, in_str.length());
            if (sti.isSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters()) {
                showMsg(stwa, false, stwa.currentSTI.getSyncTaskName(), "I", in_str, basename,
                        String.format(stwa.context.getString(R.string.msgs_mirror_invalid_file_directory_name_character_skipped), invalid_char),
                        stwa.context.getString(R.string.msgs_mirror_task_file_ignored));
            } else {
                showMsg(stwa, false, stwa.currentSTI.getSyncTaskName(), "E", in_str, basename,
                        String.format(stwa.context.getString(R.string.msgs_mirror_invalid_file_directory_name_character_error), invalid_char),
                        stwa.context.getString(R.string.msgs_mirror_task_file_failed));
            }
            return false;
        }
        return true;
    }

//    private CIFSContext setSmbAuth(BaseContext bc, String domain, String user, String pass) {
//        String tuser = null, tpass = null;
//        if (user.length() != 0) tuser = user;
//        if (pass.length() != 0) tpass = pass;
//
//        NtlmPasswordAuthentication creds = new NtlmPasswordAuthentication(bc, "", tuser, tpass);
//        CIFSContext smb_auth = bc.withCredentials(creds);
//
//        return smb_auth;
//    }

    final public static boolean createDirectoryToInternalStorage(SyncThreadWorkArea stwa, SyncTaskItem sti, String dir) {
        boolean result = false;
        File lf = new File(dir);
        if (!lf.exists()) {
            if (!sti.isSyncTestMode()) {
                result = lf.mkdirs();
                if (result && stwa.gp.settingDebugLevel >= 1)
                    stwa.util.addDebugMsg(1, "I", "createDirectoryToInternalStorage directory created, dir=" + dir);
            } else {
                if (stwa.gp.settingDebugLevel >= 1)
                    stwa.util.addDebugMsg(1, "I", "createDirectoryToInternalStorage directory created, dir=" + dir);
            }
        }
        return result;
    }

    final public static boolean createDirectoryToExternalStorage(SyncThreadWorkArea stwa, SyncTaskItem sti, String dir) {
        boolean result = false;
        if (!sti.isSyncTestMode()) {
            File lf = new File(dir);
            boolean i_exists = lf.exists();
            if (!i_exists) {
                SafFile new_saf = null;
                if (dir.startsWith(stwa.gp.safMgr.getSdcardRootPath())) stwa.gp.safMgr.createSdcardItem(dir, true);
                else stwa.gp.safMgr.createUsbItem(dir, true);
                result = (new_saf != null) ? true : false;
                if (result && !i_exists && stwa.gp.settingDebugLevel >= 1)
                    stwa.util.addDebugMsg(1, "I", "createDirectoryToExternalStorage directory created, dir=" + dir);
                stwa.util.addDebugMsg(2, "I", "createDirectoryToExternalStorage result=" + result + ", exists=" + i_exists + ", new_saf=" + new_saf);
            } else {
                stwa.util.addDebugMsg(2, "I", "createDirectoryToExternalStorage directory exists, Directory=" + dir);
            }
        } else {
            if (stwa.gp.settingDebugLevel >= 1)
                stwa.util.addDebugMsg(1, "I", "createDirectoryToExternalStorage directory created, dir=" + dir);
        }
        return result;
    }

    final public static void createDirectoryToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, String dir,
                                                  JcifsAuth auth) throws MalformedURLException, JcifsException {
        try {
            JcifsFile sf = new JcifsFile(dir, auth);
            if (!sti.isSyncTestMode()) {
                if (!sf.exists()) {
                    sf.mkdirs();
                    if (stwa.gp.settingDebugLevel >= 1)
                        stwa.util.addDebugMsg(1, "I", "createDirectoryToSmb directory created, dir=" + dir);
                }
            } else {
                if (!sf.exists()) {
                    if (stwa.gp.settingDebugLevel >= 1)
                        stwa.util.addDebugMsg(1, "I", "createDirectoryToSmb directory created, dir=" + dir);
                }
            }
        } catch(JcifsException e) {
            showMsg(stwa, false, sti.getSyncTaskName(), "E", dir, "","SMB create error, "+e.getMessage());
            throw(e);
        }
    }

    static final public void deleteTempMediaStoreItem(SyncThreadWorkArea stwa, File temp_file) {
        if (Build.VERSION.SDK_INT==26 || Build.VERSION.SDK_INT==27) {
            String tfp=temp_file.getPath();
            File temp_file_for_rename=new File(tfp+"x");
            temp_file.renameTo(temp_file_for_rename);
            int dc_image=0, dc_audio=0, dc_video=0, dc_files=0;
            ContentResolver cr = stwa.context.getContentResolver();
            dc_image=cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{tfp} );
            dc_audio=cr.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA + "=?", new String[]{tfp} );
            dc_video=cr.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA + "=?", new String[]{tfp} );
            dc_files=cr.delete(MediaStore.Files.getContentUri("external"), MediaStore.Video.Media.DATA + "=?", new String[]{tfp} );
            temp_file_for_rename.renameTo(temp_file);
            stwa.util.addDebugMsg(1,"I","deleTempMediaStoreItem Temp file name=",tfp,
                    ", delete count image="+dc_image, ", audio="+dc_audio,", video="+dc_video,", files="+dc_files);
        }
    }

    static public void deleteExternalStorageItem(SyncThreadWorkArea stwa, boolean del_dir, SyncTaskItem sti, String tmp_target) {
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", "deleteExternalStorageItem entered, del=" + tmp_target);
        if (tmp_target.startsWith(stwa.gp.safMgr.getSdcardRootPath())) {
            if (!tmp_target.equals(stwa.gp.safMgr.getSdcardRootPath())) {
                File lf_tmp = new File(tmp_target);
                if (lf_tmp.exists()) {
                    deleteExternalStorageFile(stwa, sti, tmp_target, lf_tmp);
                }
            }
        } else if (tmp_target.startsWith(stwa.gp.safMgr.getUsbRootPath())) {
            if (!tmp_target.equals(stwa.gp.safMgr.getUsbRootPath())) {
                File lf_tmp = new File(tmp_target);
                if (lf_tmp.exists()) {
                    deleteExternalStorageFile(stwa, sti, tmp_target, lf_tmp);
                }
            }
        }
    }

    static public void deleteExternalStorageFile(SyncThreadWorkArea stwa, SyncTaskItem sti, String fp, File df) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "deleteExternalStorageFile entered, del=" + fp);
//		File df=new File(fp);
        if (df.isDirectory()) {
            File[] fl = df.listFiles();
            if (fl != null && fl.length > 0) {
                for (File c_item : fl) {
                    if (c_item.isDirectory()) {
                        deleteExternalStorageFile(stwa, sti, fp + "/" + c_item.getName(), c_item);
//						stwa.totalDeleteCount++;
//						SafFile sf=SafUtil.getSafDocumentFileByPath(stwa.safCA, c_item.getPath(), true);
//						if (!sti.isSyncTestMode()) sf.delete();
//						showMsg(stwa,false, sti.getSyncTaskName(), "I", fp+"/"+c_item.getName(), c_item.getName(),
//								"", stwa.context.getString(R.string.msgs_mirror_task_dir_deleted));
                    } else {
                        stwa.totalDeleteCount++;
                        SafFile sf = null;
                        if (c_item.getPath().startsWith(stwa.gp.safMgr.getSdcardRootPath())) sf=stwa.gp.safMgr.createSdcardItem(c_item.getPath(), false);
                        else sf=stwa.gp.safMgr.createUsbItem(c_item.getPath(), false);
                        if (!sti.isSyncTestMode()) {
                            sf.delete();
                            scanMediaFile(stwa, fp);
                        }
                        showMsg(stwa, false, sti.getSyncTaskName(), "I", fp + "/" + c_item.getName(), c_item.getName(),
                                "", stwa.context.getString(R.string.msgs_mirror_task_file_deleted));
                    }
                    if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                        break;
                    }
                }
                stwa.totalDeleteCount++;
                SafFile sf = null;
                if (df.getPath().startsWith(stwa.gp.safMgr.getSdcardRootPath())) sf=stwa.gp.safMgr.createSdcardItem(df.getPath(), false);
                else sf=stwa.gp.safMgr.createUsbItem(df.getPath(), false);
                if (!sti.isSyncTestMode()) {
                    sf.delete();
                    scanMediaFile(stwa, fp);
                }
                showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, df.getName(),
                        "", stwa.context.getString(R.string.msgs_mirror_task_dir_deleted));
            } else {
                SafFile sf = null;
                if (df.getPath().startsWith(stwa.gp.safMgr.getSdcardRootPath())) sf=stwa.gp.safMgr.createSdcardItem(df.getPath(), false);
                else sf=stwa.gp.safMgr.createUsbItem(df.getPath(), false);
                if (!sti.isSyncTestMode()) {
                    sf.delete();
                    scanMediaFile(stwa, fp);
                }
                stwa.totalDeleteCount++;
                showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, df.getName(),
                        "", stwa.context.getString(R.string.msgs_mirror_task_dir_deleted));
            }
        } else {
            SafFile sf = null;
            if (df.getPath().startsWith(stwa.gp.safMgr.getSdcardRootPath())) sf=stwa.gp.safMgr.createSdcardItem(df.getPath(), false);
            else sf=stwa.gp.safMgr.createUsbItem(df.getPath(), false);
            if (!sti.isSyncTestMode()) sf.delete();
            stwa.totalDeleteCount++;
            showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, df.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_task_file_deleted));

        }
    }

    static public void deleteSmbItem(SyncThreadWorkArea stwa, boolean del_dir, SyncTaskItem sti,
                                     String to_base, String tmp_target, JcifsAuth auth) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", "deleteSmbItem entered, del=" + tmp_target);
        if (!tmp_target.equals(to_base)) {
            try {
                JcifsFile lf_tmp = new JcifsFile(tmp_target, auth);
                if (lf_tmp.exists()) {
                    deleteSmbFile(stwa, sti, tmp_target, lf_tmp);
                }
            } catch(JcifsException e) {
                showMsg(stwa, false, sti.getSyncTaskName(), "E", tmp_target, "","SMB delete error, "+e.getMessage());
                throw(e);
            } catch(IOException e) {
                showMsg(stwa, false, sti.getSyncTaskName(), "E", tmp_target, "","SMB delete error, "+e.getMessage());
                throw(e);
            }
        }
    }

    static public void deleteSmbFile(SyncThreadWorkArea stwa, SyncTaskItem sti, String fp, JcifsFile hf) throws JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "deleteSmbFile entered, del=" + fp);
//		JcifsFile hf=new JcifsFile(fp, stwa.ntlmPasswordAuth);
        if (hf.isDirectory()) {
            JcifsFile[] fl = hf.listFiles();
            if (fl != null && fl.length > 0) {
                for (JcifsFile c_item : fl) {
                    if (c_item.isDirectory()) {
                        deleteSmbFile(stwa, sti, fp + c_item.getName(), c_item);
                    } else {
                        stwa.totalDeleteCount++;
                        if (!sti.isSyncTestMode()) c_item.delete();
                        showMsg(stwa, false, sti.getSyncTaskName(), "I", fp + c_item.getName(), c_item.getName(),
                                "", stwa.context.getString(R.string.msgs_mirror_task_file_deleted));
                    }
                    if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                        break;
                    }
                }
                stwa.totalDeleteCount++;
                if (!sti.isSyncTestMode()) hf.delete();
                showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, hf.getName(),
                        "", stwa.context.getString(R.string.msgs_mirror_task_dir_deleted));
            } else {
                if (!sti.isSyncTestMode()) hf.delete();
                stwa.totalDeleteCount++;
                showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, hf.getName(),
                        "", stwa.context.getString(R.string.msgs_mirror_task_dir_deleted));
            }
        } else {
            if (!sti.isSyncTestMode()) hf.delete();
            stwa.totalDeleteCount++;
            showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, hf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_task_file_deleted));

        }
    }

    static public void deleteInternalStorageItem(SyncThreadWorkArea stwa, boolean del_dir, SyncTaskItem sti, String tmp_target) {
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", "deleteInternalStorageItem entered, del=" + tmp_target);

        if (!tmp_target.equals(stwa.gp.internalRootDirectory)) {
            File lf_tmp = new File(tmp_target);
            if (lf_tmp.exists()) {
                deleteInternalStorageFile(stwa, sti, tmp_target, lf_tmp);
            }
        }
    }

    static public void deleteInternalStorageFile(SyncThreadWorkArea stwa, SyncTaskItem sti, String fp, File lf) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "deleteInternalStorageFile entered, del=" + fp);
//		File lf=new File(fp);
//		Log.v("","name="+fp+", dir="+lf.isDirectory()+", file="+lf.isFile());
        if (lf.isDirectory()) {
            File[] fl = lf.listFiles();
            if (fl != null && fl.length > 0) {
                for (File c_item : fl) {
                    if (lf.isDirectory()) {
                        deleteInternalStorageFile(stwa, sti, fp + "/" + c_item.getName(), c_item);
                    } else {
                        stwa.totalDeleteCount++;
                        if (!sti.isSyncTestMode()) {
                            c_item.delete();
                            scanMediaFile(stwa, fp);
                        }
                        showMsg(stwa, false, sti.getSyncTaskName(), "I", fp + "/" + c_item.getName(), c_item.getName(),
                                "", stwa.context.getString(R.string.msgs_mirror_task_file_deleted));
                    }
                    if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                        break;
                    }
                }
                stwa.totalDeleteCount++;
                if (!sti.isSyncTestMode()) {
                    lf.delete();
                    scanMediaFile(stwa, fp);
                }
                showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, lf.getName(),
                        "", stwa.context.getString(R.string.msgs_mirror_task_dir_deleted));
            } else {
                if (!sti.isSyncTestMode()) {
                    lf.delete();
                    scanMediaFile(stwa, fp);
                }
                stwa.totalDeleteCount++;
                showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, lf.getName(),
                        "", stwa.context.getString(R.string.msgs_mirror_task_dir_deleted));
            }
        } else {
            if (!sti.isSyncTestMode()) {
                lf.delete();
                scanMediaFile(stwa, fp);
            }
            stwa.totalDeleteCount++;
            showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, lf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_task_file_deleted));

        }
    }

    private void reconnectWifi() {
        boolean wifi_reconnect_required = false;
        try {
            ContentResolver contentResolver = mStwa.context.getContentResolver();
            int policy = Settings.System.getInt(contentResolver, Settings.Global.WIFI_SLEEP_POLICY);
            switch (policy) {
                case Settings.Global.WIFI_SLEEP_POLICY_DEFAULT:
                    // スリープ中のWiFi接続を維持しない
                    wifi_reconnect_required = true;
                    break;
                case Settings.Global.WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED:
                    // スリープ中のWiFi接続を電源接続時にのみ維持する
                    wifi_reconnect_required = true;
                    break;
                case Settings.Global.WIFI_SLEEP_POLICY_NEVER:
                    // スリープ中のWiFi接続を常に維持する
                    break;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (isWifiOn()) {
            getWifiConnectedAP();
            WifiManager wm = (WifiManager) mStwa.context.getSystemService(Context.WIFI_SERVICE);
            SupplicantState ss = wm.getConnectionInfo().getSupplicantState();
            mStwa.util.addDebugMsg(1, "I", "reconnectWifi ss=" + ss.toString());
            if (!GlobalParameters.isScreenOn(mStwa.context, mStwa.util) && wifi_reconnect_required
            ) {// && !ss.equals(SupplicantState.COMPLETED)) {
                @SuppressWarnings("deprecation")
                WakeLock wl = ((PowerManager) mStwa.context.getSystemService(Context.POWER_SERVICE))
                        .newWakeLock(PowerManager.FULL_WAKE_LOCK
                                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                , "SMBSync2-thread-reconnect");
                long wt = 10 * 1000;
                wl.acquire(5000);
                mStwa.util.addDebugMsg(1, "I", "reconnectWifi reconnect issued");
                long to = 0;
                while (!wm.getConnectionInfo().getSupplicantState().equals("")) {
                    to += 100;
                    if (wt < to) break;
                    SystemClock.sleep(100);
                    mStwa.util.addDebugMsg(1, "I", "reconnectWifi ssw=" + wm.getConnectionInfo().getSupplicantState().toString());
                }
            }

            ss = wm.getConnectionInfo().getSupplicantState();
            mStwa.util.addDebugMsg(1, "I", "reconnectWifi ss=" + ss.toString());
            getWifiConnectedAP();
        }

    }

    private String isWifiConditionSatisfied(SyncTaskItem sti) {
        String result = "";
        String if_addr=CommonUtilities.getIfIpAddress(mStwa.util);
        if (sti.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_PRIVATE_ADDR)) {
            if (!isPrivateAddress(if_addr)) result=mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_wifi_connect_not_local_addr);
        } else if (sti.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_ADDR)) {
            ArrayList<String> wl = sti.getSyncOptionWifiConnectedAddressWhiteList();
            ArrayList<Pattern> inc = new ArrayList<Pattern>();
            int flags = Pattern.CASE_INSENSITIVE;
            for (String al : wl) {
                if (al.startsWith("I")) {
                    String prefix = "", suffix = "";
                    if (al.substring(1).endsWith("*")) suffix = "$";
                    inc.add(Pattern.compile(prefix + MiscUtil.convertRegExp(al.substring(1)) + suffix, flags));
                    mStwa.util.addDebugMsg(1, "I", "isWifiConditionSatisfied Address include added=" + inc.get(inc.size() - 1).toString());
                }
            }
            if (!if_addr.equals("")) {
                if (inc.size() > 0) {
                    Matcher mt;
                    boolean found = false;
                    for (Pattern pat : inc) {
                        mt = pat.matcher(if_addr);
                        if (mt.find()) {
                            found = true;
                            mStwa.util.addDebugMsg(1, "I", "isWifiConditionSatisfied Address include matched=" + pat.toString());
                            break;
                        }
                    }
                    if (!found) {
                        if (sti.isSyncOptionTaskSkipIfConnectAnotherWifiSsid()) {
                            result = mStwa.context.getString(R.string.msgs_mirror_sync_skipped_wifi_address_conn_other);
                        } else {
                            result = mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_wifi_address_conn_other);
                        }
                    }
                }
            } else {
                result = mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_wifi_ap_not_connected);
            }
        } else {
            if (sti.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF)) {
                //NOP
            } else {
                if (!isWifiOn()) {
                    result = mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_wifi_is_off);
                } else {
                    if (sti.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP)) {
                        if (!isConnectedToAnyWifiAP()) result = mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_wifi_ap_not_connected);
                    } else if (sti.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_AP)) {
                        if (!isConnectedToAnyWifiAP()) result = mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_wifi_ap_not_connected);
                        else {
                            ArrayList<String> wl = sti.getSyncOptionWifiConnectedAccessPointWhiteList();
                            ArrayList<Pattern> inc = new ArrayList<Pattern>();
                            int flags = Pattern.CASE_INSENSITIVE;
                            for (String apl : wl) {
                                if (apl.startsWith("I")) {
                                    String prefix = "", suffix = "";
                                    if (apl.substring(1).endsWith("*")) suffix = "$";
                                    inc.add(Pattern.compile(prefix + MiscUtil.convertRegExp(apl.substring(1)) + suffix, flags));
                                    mStwa.util.addDebugMsg(1, "I", "isWifiConditionSatisfied AP include added=" + inc.get(inc.size() - 1).toString());
                                }
                            }
                            if (!getWifiConnectedAP().equals("")) {
                                if (inc.size() > 0) {
                                    Matcher mt;
                                    boolean found = false;
                                    for (Pattern pat : inc) {
                                        if (Build.VERSION.SDK_INT>=27) {
                                            mt = pat.matcher(getWifiConnectedAP());
                                        } else {
                                            mt = pat.matcher(mGp.wifiSsid);
                                        }
                                        if (mt.find()) {
                                            found = true;
                                            mStwa.util.addDebugMsg(1, "I", "isWifiConditionSatisfied AP include matched=" + pat.toString());
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        if (sti.isSyncOptionTaskSkipIfConnectAnotherWifiSsid()) {
                                            result = mStwa.context.getString(R.string.msgs_mirror_sync_skipped_wifi_ap_conn_other);
                                        } else {
                                            result = mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_wifi_ap_conn_other);
                                        }
                                    }
                                }
                            } else {
                                result=getWiFiAPNameErrorReason(sti);
                            }
                        }
                    }
                }
            }
        }
        if (result.equals("")) {
            if (!sti.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF)) {
                if (if_addr.equals("")) {//IP Addressが必要だがIP Addressが取得できない
                    result=mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_ip_address_not_obtained);
                }
            }
            if (result.equals("")) {
                if (!isPrivateAddress(if_addr)) {
                    if (!sti.isSyncOptionSyncAllowGlobalIpAddress())
                        result=String.format(mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_ip_address_is_global), if_addr);
                }
            }
        }

        mStwa.util.addDebugMsg(1, "I", "isWifiConditionSatisfied exited, " + "option=" + sti.getSyncOptionWifiStatusOption() + ", result=" + result);
        return result;
    }

    private String getWiFiAPNameErrorReason(SyncTaskItem sti) {
        String result="";
        if (Build.VERSION.SDK_INT>=27) {
            if (!CommonUtilities.isLocationServiceEnabled(mStwa.context, mStwa.gp)) {
                result=mStwa.context.getString(R.string.msgs_main_location_error_location_service_is_disabled);
                return result;
            }
        }
        WifiManager wm = (WifiManager) mStwa.context.getSystemService(Context.WIFI_SERVICE);
        if (wm.isWifiEnabled()) {
            String ssid_name=wm.getConnectionInfo().getSSID();
            if (ssid_name!=null) {
                if (Build.VERSION.SDK_INT==27 || Build.VERSION.SDK_INT==28) {//Android 8.1 && 9
                    if (mStwa.context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                        result=mStwa.context.getString(R.string.msgs_main_location_error_location_permission_not_granted);
                    }
                } else if (Build.VERSION.SDK_INT>=29) {//Android 10 以上
                    if (mStwa.context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                        result=mStwa.context.getString(R.string.msgs_main_location_error_location_permission_not_granted);
                    } else if (mStwa.context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
                        result=mStwa.context.getString(R.string.msgs_main_location_error_background_location_permission_not_granted);
                    }
                } else {
                    result = mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_wifi_ap_not_connected)+" ssid="+ssid_name;
                }
            } else {
                //SSID is null
                result = mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_wifi_ap_not_connected)+" ssid="+ssid_name;
            }
        } else {
            //WIFI Off
            result = mStwa.context.getString(R.string.msgs_mirror_sync_can_not_start_wifi_is_off);
        }
        return result;
    }

    private boolean isPrivateAddress(String if_addr) {
        if (if_addr.startsWith("10.")) return true;
        else if (if_addr.startsWith("192.168.")) return true;
        else if (if_addr.startsWith("172.16.")) return true;
        else if (if_addr.startsWith("172.17.")) return true;
        else if (if_addr.startsWith("172.18.")) return true;
        else if (if_addr.startsWith("172.19.")) return true;
        else if (if_addr.startsWith("172.20.")) return true;
        else if (if_addr.startsWith("172.21.")) return true;
        else if (if_addr.startsWith("172.22.")) return true;
        else if (if_addr.startsWith("172.23.")) return true;
        else if (if_addr.startsWith("172.24.")) return true;
        else if (if_addr.startsWith("172.25.")) return true;
        else if (if_addr.startsWith("172.26.")) return true;
        else if (if_addr.startsWith("172.27.")) return true;
        else if (if_addr.startsWith("172.28.")) return true;
        else if (if_addr.startsWith("172.29.")) return true;
        else if (if_addr.startsWith("172.30.")) return true;
        else if (if_addr.startsWith("172.31.")) return true;
        return false;
    }

    private String getWifiConnectedAP() {
        String result = "";
        WifiManager wm = (WifiManager) mStwa.context.getSystemService(Context.WIFI_SERVICE);
        if (isWifiOn()) {
            result = CommonUtilities.getWifiSsidName(wm);
            mStwa.util.addDebugMsg(1, "I", "getWifiConnectedAP SSID=" + result);
        } else {
            mStwa.util.addDebugMsg(1, "I", "getWifiConnectedAP WiFi is not enabled.");
        }
        return result;
    }

    private boolean isConnectedToAnyWifiAP() {
        boolean isConnected = false;
        boolean isWiFi = false;
        boolean result=true;
        ConnectivityManager cm =(ConnectivityManager)mStwa.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork!=null) {
            String network=activeNetwork.getExtraInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        }
        if (!isWiFi || !isConnected) {//getWifiConnectedAP().equals("")) {
            result=false;
        }

        return result;
    }

    private boolean isWifiOn() {
        boolean result = false;
        WifiManager wm = (WifiManager) mStwa.context.getSystemService(Context.WIFI_SERVICE);
        result = wm.isWifiEnabled();
        return result;
    }

    private boolean setWifiOn() {
        boolean result = false;
        WifiManager wm = (WifiManager) mStwa.context.getSystemService(Context.WIFI_SERVICE);
        result = wm.setWifiEnabled(true);
        return result;
    }

    private boolean setWifiOff() {
        boolean result = false;
        WifiManager wm = (WifiManager) mStwa.context.getSystemService(Context.WIFI_SERVICE);
        result = wm.setWifiEnabled(false);
        return result;
    }

    static public void showProgressMsg(final SyncThreadWorkArea stwa, final String task_name, final String msg) {
        NotificationUtil.showOngoingMsg(stwa.gp, stwa.util, 0, task_name, msg);
        stwa.gp.progressSpinSyncprofText = task_name;
        stwa.gp.progressSpinMsgText = msg;
        if (stwa.gp.dialogWindowShowed && stwa.gp.progressSpinSyncprof != null) {
            stwa.gp.uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (stwa.gp.progressSpinSyncprof != null && !stwa.gp.activityIsBackground) {
                        stwa.gp.progressSpinSyncprof.setText(stwa.gp.progressSpinSyncprofText);
                        ;
                        stwa.gp.progressSpinMsg.setText(stwa.gp.progressSpinMsgText);
                    }
                }
            });
        }
    }

    static public void showMsg(final SyncThreadWorkArea stwa, boolean log_only,
                               final String task_name, final String cat,
                               final String full_path, final String file_name, final String msg) {
        showMsg(stwa, log_only, task_name, cat, full_path, file_name, msg, "");
   }

    //file_name: used only for ongoing message (dialog progress in top of screen) to display filename and file operation during sync
    //file_name: used for ongoing notification, extra trailing space added as separator from following message
    //full_path: file/dir name + complete path, displayed in messages TAB
    //msg: message, last element, do not end with new line
    //type: displayed aligned at right edge in dedicated column, last element, do not end with new line
    //msg or type shouldn't be both empty: no crash or error though, only adds extra trailing space
    //showOngoingMsg(): System notification area progress message when app in background
    static public void showMsg(final SyncThreadWorkArea stwa, boolean log_only,
                               final String task_name, final String cat,
                               final String full_path, final String file_name, final String msg, final String type) {
        String notif_msg = "";
        String[] notif_msg_list = {file_name, msg, type};
        for (String msg_part : notif_msg_list) {
            if (!msg_part.equals("")) {
                if (notif_msg.equals("")) notif_msg = msg_part;
                else notif_msg = notif_msg.concat(" ").concat(msg_part);
            }
        }

        //ongoing progress message in top of screen during sync, not saved to Messages tab
        stwa.gp.progressSpinSyncprofText = task_name;//title of in app progress notification
        stwa.gp.progressSpinMsgText = notif_msg;//text of in app progress notification

        if (!log_only) {//set onscreen notification progress
            NotificationUtil.showOngoingMsg(stwa.gp, stwa.util, System.currentTimeMillis(), task_name, file_name, msg, type);//system notification if app in background
            if (stwa.gp.dialogWindowShowed && stwa.gp.progressSpinSyncprof != null) {
                stwa.gp.uiHandler.post(new Runnable() {//in app progress notification
                    @Override
                    public void run() {
                        if (stwa.gp.progressSpinSyncprof != null && !stwa.gp.activityIsBackground) {
                            stwa.gp.progressSpinSyncprof.setText(stwa.gp.progressSpinSyncprofText);
                            stwa.gp.progressSpinMsg.setText(stwa.gp.progressSpinMsgText);
                        }
                    }
                });
            }
        }
        //write to Messages tab: [cat + [space] + full_path + "space" + message] on left column, [type] on right column
        //addLogMsg() displays message on the tabb with fields: cat, type, String... msg: separate different msg fields by " " or a separator for redability
        //argument "type" in addLogMsg() is separate field, displayed in separate column on right of msg, aligned on right border, color themed (file operations...)
        //print_msg: text file displayed when we click the sync event in History tab
        //print_msg: remove trailing new line in full_path for readability of the text log file
        //buildPrintMsg(): prints a text file with print_msg, not used for messages display on tab. Text file shown when we click History SyncTask event
        //fields: cat, String... msg: separator " " added between msg fields for redability

        //display message in GUI Messages TAB
        stwa.util.addLogMsg(false, true, true, false, cat, task_name, type, full_path, msg);

        //buildPrintMsg(): print the sync log text file, shown if we click the Sync event in History TAB
        String lm = "";
        String[] print_msg_list = {task_name+":", full_path, msg, type};
        for (String msg_part : print_msg_list) {
            if (!msg_part.equals("")) {
                if (lm.equals("")) lm = msg_part;
                else lm = lm.concat(" ").concat(msg_part);
            }
        }

        if (stwa.gp.settingWriteSyncResultLog && stwa.syncHistoryWriter != null) {
            String print_msg = stwa.util.buildPrintMsg(cat, lm);
            stwa.syncHistoryWriter.println(print_msg);
        }
    }

    static public void showArchiveMsg(final SyncThreadWorkArea stwa, boolean log_only,
                                      final String task_name, final String cat,
                                      final String full_path, final String archive_path, final String from_file_name, final String to_file_name,
                                      final String msg, final String type) {
        String notif_msg = "";
        String[] notif_msg_list = {from_file_name, "-->", to_file_name, msg, type};
        for (String msg_part : notif_msg_list) {
            if (!msg_part.equals("")) {
                if (notif_msg.equals("")) notif_msg = msg_part;
                else notif_msg = notif_msg.concat(" ").concat(msg_part);
            }
        }

        //ongoing progress message in top of screen during sync, not saved to Messages tab
        stwa.gp.progressSpinSyncprofText = task_name;//title of in app progress notification
        stwa.gp.progressSpinMsgText = notif_msg;//text of in app progress notification

        if (!log_only) {//set onscreen notification progress
            NotificationUtil.showOngoingMsg(stwa.gp, stwa.util, System.currentTimeMillis(), task_name, from_file_name, msg, type);//system notification if app in background
            if (stwa.gp.dialogWindowShowed && stwa.gp.progressSpinSyncprof != null) {
                stwa.gp.uiHandler.post(new Runnable() {//in app progress notification
                    @Override
                    public void run() {
                        if (stwa.gp.progressSpinSyncprof != null && !stwa.gp.activityIsBackground) {
                            stwa.gp.progressSpinSyncprof.setText(stwa.gp.progressSpinSyncprofText);
                            stwa.gp.progressSpinMsg.setText(stwa.gp.progressSpinMsgText);
                        }
                    }
                });
            }
        }

        //display message in GUI Messages TAB
        String printed_path = full_path + " --> " + archive_path;
        stwa.util.addLogMsg(false, true, true, false, cat, task_name, type, printed_path, msg);

        //buildPrintMsg(): print the sync log text file, shown if we click the Sync event in History TAB
        String lm = "";
        String[] print_msg_list = {task_name, printed_path, msg, type};
        for (String msg_part : print_msg_list) {
            if (!msg_part.equals("")) {
                if (lm.equals("")) lm = msg_part;
                else lm = lm.concat(" ").concat(msg_part);
            }
        }

        if (stwa.gp.settingWriteSyncResultLog && stwa.syncHistoryWriter != null) {
            String print_msg = stwa.util.buildPrintMsg(cat, lm);
            stwa.syncHistoryWriter.println(print_msg);
        }
    }

    public static void printStackTraceElement(SyncThreadWorkArea stwa, StackTraceElement[] ste) {
        String print_msg = "";
        for (int i = 0; i < ste.length; i++) {
            stwa.util.addLogMsg("E", "", ste[i].toString());
            if (stwa.syncHistoryWriter != null) {
                print_msg = stwa.util.buildPrintMsg("E", ste[i].toString());
                stwa.syncHistoryWriter.println(print_msg);
            }
        }
    }

    static final public boolean sendConfirmRequest(SyncThreadWorkArea stwa, SyncTaskItem sti, String type, String url) {
        boolean result = true;
        int rc = 0;
        stwa.util.addDebugMsg(2, "I", "sendConfirmRequest entered type=" , type ,
                ", Override="+sti.isSyncOverrideCopyMoveFile(), ", Confirm=" + sti.isSyncConfirmOverrideOrDelete(),
                ", fp=", url);
        if (sti.isSyncConfirmOverrideOrDelete()) {
            boolean ignore_confirm = true;
            if (type.equals(SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR) || type.equals(SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE)) {
                if (stwa.confirmDeleteResult == SMBSYNC2_CONFIRM_RESP_YESALL) result = true;
                else if (stwa.confirmDeleteResult == SMBSYNC2_CONFIRM_RESP_NOALL) result = false;
                else ignore_confirm = false;
            } else if (type.equals(SMBSYNC2_CONFIRM_REQUEST_COPY)) {
                if (stwa.confirmCopyResult == SMBSYNC2_CONFIRM_RESP_YESALL) result = true;
                else if (stwa.confirmCopyResult == SMBSYNC2_CONFIRM_RESP_NOALL) result = false;
                else ignore_confirm = false;
            } else if (type.equals(SMBSYNC2_CONFIRM_REQUEST_MOVE)) {
                if (stwa.confirmMoveResult == SMBSYNC2_CONFIRM_RESP_YESALL) result = true;
                else if (stwa.confirmMoveResult == SMBSYNC2_CONFIRM_RESP_NOALL) result = false;
                else ignore_confirm = false;
            }
            if (!ignore_confirm) {
                try {
                    String msg = "";
                    if (type.equals(SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR)) {
                        msg = stwa.context.getString(R.string.msgs_mirror_confirm_please_check_confirm_msg_delete_dir);
                    } else if (type.equals(SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE)) {
                        msg = stwa.context.getString(R.string.msgs_mirror_confirm_please_check_confirm_msg_delete_file);
                    } else if (type.equals(SMBSYNC2_CONFIRM_REQUEST_COPY)) {
                        msg = stwa.context.getString(R.string.msgs_mirror_confirm_please_check_confirm_msg_copy);
                    } else if (type.equals(SMBSYNC2_CONFIRM_REQUEST_MOVE)) {
                        msg = stwa.context.getString(R.string.msgs_mirror_confirm_please_check_confirm_msg_move);
                    }
                    NotificationUtil.showOngoingMsg(stwa.gp, stwa.util, 0, msg);
                    stwa.gp.confirmDialogShowed = true;
                    stwa.gp.confirmDialogFilePathPairA = url;
                    stwa.gp.confirmDialogMethod = type;
                    stwa.gp.syncThreadConfirm.initThreadCtrl();
                    stwa.gp.releaseWakeLock(stwa.util);
                    if (stwa.gp.callbackStub != null) {
                        stwa.gp.callbackStub.cbShowConfirmDialog(type, "", url, 0,0,null,0,0);
                    }
                    synchronized (stwa.gp.syncThreadConfirm) {
                        stwa.gp.syncThreadConfirmWait = true;
                        stwa.gp.syncThreadConfirm.wait();//Posted by SMBSyncService#aidlConfirmResponse()
                        stwa.gp.syncThreadConfirmWait = false;
                    }
                    stwa.gp.acquireWakeLock(stwa.context, stwa.util);
                    if (type.equals(SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR) || type.equals(SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE)) {
                        rc = stwa.confirmDeleteResult = stwa.gp.syncThreadConfirm.getExtraDataInt();
                        if (stwa.confirmDeleteResult > 0) result = true;
                        else result = false;
                        if (stwa.confirmDeleteResult == SMBSYNC2_CONFIRM_RESP_CANCEL)
                            stwa.gp.syncThreadCtrl.setDisabled();
                    } else if (type.equals(SMBSYNC2_CONFIRM_REQUEST_COPY)) {
                        rc = stwa.confirmCopyResult = stwa.gp.syncThreadConfirm.getExtraDataInt();
                        if (stwa.confirmCopyResult > 0) result = true;
                        else result = false;
                        if (stwa.confirmCopyResult == SMBSYNC2_CONFIRM_RESP_CANCEL)
                            stwa.gp.syncThreadCtrl.setDisabled();
                    } else if (type.equals(SMBSYNC2_CONFIRM_REQUEST_MOVE)) {
                        rc = stwa.confirmMoveResult = stwa.gp.syncThreadConfirm.getExtraDataInt();
                        if (stwa.confirmMoveResult > 0) result = true;
                        else result = false;
                        if (stwa.confirmMoveResult == SMBSYNC2_CONFIRM_RESP_CANCEL)
                            stwa.gp.syncThreadCtrl.setDisabled();
                    }
                } catch (RemoteException e) {
                    stwa.util.addLogMsg("E", "", "RemoteException occured");
                    printStackTraceElement(stwa, e.getStackTrace());
                } catch (InterruptedException e) {
                    stwa.util.addLogMsg("E", "", "InterruptedException occured");
                    printStackTraceElement(stwa, e.getStackTrace());
                }
            }
        }
        stwa.util.addDebugMsg(2, "I", "sendConfirmRequest result=" + result, ", rc=" + rc);

        return result;
    }

    static final public boolean sendArchiveConfirmRequest(SyncThreadWorkArea stwa, SyncTaskItem sti, String type, String url) {
        boolean result = true;
        int rc = 0;
        stwa.util.addDebugMsg(2, "I", "sendArchiveConfirmRequest entered type=" , type ,
                ", fp=", url);
        boolean ignore_confirm = true;
        if (type.equals(SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE)) {
            if (stwa.confirmArchiveResult == SMBSYNC2_CONFIRM_RESP_YESALL) result = true;
            else if (stwa.confirmArchiveResult == SMBSYNC2_CONFIRM_RESP_NOALL) result = false;
            else ignore_confirm = false;
        }
        if (!ignore_confirm) {
            try {
                String msg = "";
                if (type.equals(SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE)) {
                    msg = stwa.context.getString(R.string.msgs_mirror_confirm_please_check_confirm_msg_archive);
                }
                NotificationUtil.showOngoingMsg(stwa.gp, stwa.util, 0, msg);
                stwa.gp.confirmDialogShowed = true;
                stwa.gp.confirmDialogFilePathPairA = url;
                stwa.gp.confirmDialogMethod = type;
                stwa.gp.syncThreadConfirm.initThreadCtrl();
                stwa.gp.releaseWakeLock(stwa.util);
                if (stwa.gp.callbackStub != null) {
                    stwa.gp.callbackStub.cbShowConfirmDialog(type, "", url, 0,0,null,0,0);
                }
                synchronized (stwa.gp.syncThreadConfirm) {
                    stwa.gp.syncThreadConfirmWait = true;
                    stwa.gp.syncThreadConfirm.wait();//Posted by SMBSyncService#aidlConfirmResponse()
                    stwa.gp.syncThreadConfirmWait = false;
                }
                stwa.gp.acquireWakeLock(stwa.context, stwa.util);
                if (type.equals(SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE)) {
                    rc = stwa.confirmArchiveResult = stwa.gp.syncThreadConfirm.getExtraDataInt();
                    if (stwa.confirmArchiveResult > 0) result = true;
                    else result = false;
                    if (stwa.confirmArchiveResult == SMBSYNC2_CONFIRM_RESP_CANCEL)
                        stwa.gp.syncThreadCtrl.setDisabled();
                }
            } catch (RemoteException e) {
                stwa.util.addLogMsg("E", "", "RemoteException occured");
                printStackTraceElement(stwa, e.getStackTrace());
            } catch (InterruptedException e) {
                stwa.util.addLogMsg("E", "", "InterruptedException occured");
                printStackTraceElement(stwa, e.getStackTrace());
            }
        }
        stwa.util.addDebugMsg(2, "I", "sendArchiveConfirmRequest result=" + result, ", rc=" + rc);

        return result;
    }

    static final public boolean isLocalFileLastModifiedFileItemExists(SyncThreadWorkArea stwa,
                                                                      SyncTaskItem sti,
                                                                      ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
                                                                      ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
                                                                      String fp) {
        FileLastModifiedTimeEntry item = FileLastModifiedTime.isFileItemExists(
                curr_last_modified_list, new_last_modified_list, fp);
        boolean result=false;
        if (item!=null) result=true;
        if (stwa.gp.settingDebugLevel >= 3)
            stwa.util.addDebugMsg(3, "I", "isLocalFileLastModifiedWasDifferent result=" + result + ", item=" + fp);
        return result;
    }

    static final public FileLastModifiedTimeEntry getLocalFileLastModifiedFileItemExists(SyncThreadWorkArea stwa,
                                                                                         SyncTaskItem sti,
                                                                                         ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
                                                                                         ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
                                                                                         String fp) {
        FileLastModifiedTimeEntry item = FileLastModifiedTime.isFileItemExists(
                curr_last_modified_list, new_last_modified_list, fp);
        if (stwa.gp.settingDebugLevel >= 3)
            stwa.util.addDebugMsg(3, "I", "isLocalFileLastModifiedWasDifferent result=" + item + ", item=" + fp);
        return item;
    }

    static final public boolean isLocalFileLastModifiedWasDifferent(SyncThreadWorkArea stwa,
                                                                    SyncTaskItem sti,
                                                                    ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
                                                                    ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
                                                                    String fp, long l_lm, long r_lm) {
        boolean result = FileLastModifiedTime.isCurrentListWasDifferent(
                curr_last_modified_list, new_last_modified_list,
                fp, l_lm, r_lm, stwa.syncDifferentFileAllowableTime,
                sti.isSyncOptionIgnoreDstDifference(), stwa.offsetOfDaylightSavingTime);
        if (stwa.gp.settingDebugLevel >= 3)
            stwa.util.addDebugMsg(3, "I", "isLocalFileLastModifiedWasDifferent result=" + result + ", item=" + fp);
        return result;
    }

    static final public void deleteLocalFileLastModifiedEntry(SyncThreadWorkArea stwa,
                                                              ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
                                                              ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
                                                              String fp) {
        if (FileLastModifiedTime.deleteLastModifiedItem(curr_last_modified_list, new_last_modified_list, fp)) stwa.localFileLastModListModified = true;
        if (stwa.gp.settingDebugLevel >= 3)
            stwa.util.addDebugMsg(3, "I", "deleteLocalFileLastModifiedEntry entry=" + fp);

    }

    static final public boolean updateLocalFileLastModifiedList(SyncThreadWorkArea stwa,
                                                                ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
                                                                ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
                                                                String to_dir, long l_lm, long r_lm) {
        if (stwa.lastModifiedIsFunctional) return false;
        stwa.localFileLastModListModified = true;
        return FileLastModifiedTime.updateLastModifiedList(
                curr_last_modified_list, new_last_modified_list, to_dir, l_lm, r_lm);
    }

    static final public void addLastModifiedItem(SyncThreadWorkArea stwa,
                                                 ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
                                                 ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
                                                 String to_dir, long l_lm, long r_lm) {
        FileLastModifiedTime.addLastModifiedItem(
                curr_last_modified_list, new_last_modified_list, to_dir, l_lm, r_lm);
        if (stwa.gp.settingDebugLevel >= 3)
            stwa.util.addDebugMsg(3, "I", "addLastModifiedItem entry=" + to_dir);
    }

    final private boolean isSetLastModifiedFunctional(String lmp) {
        boolean result =
                FileLastModifiedTime.isSetLastModifiedFunctional(lmp);
        if (mStwa.gp.settingDebugLevel >= 1)
            mStwa.util.addDebugMsg(1, "I", "isSetLastModifiedFunctional result=" + result + ", Directory=" + lmp);
        return result;
    }

    static final public boolean isFileChanged(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String fp, File tf,//Target (local storage)
                                              JcifsFile mhf, boolean ac) //Master (remote smb)
            throws JcifsException {
        long hf_time = 0, hf_length = 0;
        boolean hf_exists = mhf.exists();

        if (hf_exists) {
            hf_time = mhf.getLastModified();
            hf_length = mhf.length();
        }
        return isFileChangedDetailCompare(stwa, sti, fp, tf, hf_exists, hf_time, hf_length, ac);
    }

    static final public boolean isFileChanged(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String fp, File tf, //Target
                                              File mf, boolean ac)//Master
            throws JcifsException {
        long tf_time = 0, tf_length = 0;
        boolean tf_exists = mf.exists();

        if (tf_exists) {
            tf_time = mf.lastModified();
            tf_length = mf.length();
        }
        return isFileChangedDetailCompare(stwa, sti, fp, tf, tf_exists, tf_time, tf_length, ac);
    }

    static final public boolean isFileChanged(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String fp, JcifsFile tf,//Target
                                              JcifsFile mf, boolean ac)//Master
            throws JcifsException {

        long mf_time = 0, mf_length = 0;
        boolean mf_exists = tf.exists();

        if (mf_exists) {
            mf_time = tf.getLastModified();
            mf_length = tf.length();
        }

        return isFileChangedDetailCompare(stwa, sti, fp,
                mf_exists, mf_time, mf_length, tf.getPath(),
                mf.exists(), mf.getLastModified(), mf.length(), ac);

    }

    static final public boolean checkMasterFileNewerThanTargetFile(SyncThreadWorkArea stwa, SyncTaskItem sti, String fp,
                                                                   long master_time, long target_time) {
        boolean result=true;
        if (sti.isSyncOptionDifferentFileByTime() && sti.isSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile()) {
            if (stwa.lastModifiedIsFunctional) {//Use lastModified
                if (master_time>target_time) {
                    result=true;
                } else {
                    result=false;
                    stwa.totalIgnoreCount++;
                    String fn=fp.substring(fp.lastIndexOf("/"));
                    showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, fn,
                            stwa.context.getString(R.string.msgs_profile_sync_task_sync_option_ignore_never_overwrite_target_file_if_it_is_newer_than_the_master_file_option_enabled),
                            stwa.context.getString(R.string.msgs_mirror_task_file_ignored));
                }
            } else {
                FileLastModifiedTimeEntry flme=getLocalFileLastModifiedFileItemExists(stwa, sti, stwa.currLastModifiedList, stwa.newLastModifiedList, fp);
                if (flme==null) {
                    result=true;
                } else {
                    if (target_time==flme.getRemoteFileLastModified()) {
                        if (master_time>target_time) {
                            result=true;
                        } else {
                            result=false;
                            stwa.totalIgnoreCount++;
                            String fn=fp.substring(fp.lastIndexOf("/"));
                            showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, fn,
                                    stwa.context.getString(R.string.msgs_profile_sync_task_sync_option_ignore_never_overwrite_target_file_if_it_is_newer_than_the_master_file_option_enabled),
                                    stwa.context.getString(R.string.msgs_mirror_task_file_ignored));
                        }
                    } else {
                        if (master_time>target_time) {
                            result=true;
                        } else {
                            result=false;
                            stwa.totalIgnoreCount++;
                            String fn=fp.substring(fp.lastIndexOf("/"));
                            showMsg(stwa, false, sti.getSyncTaskName(), "I", fp, fn,
                                    stwa.context.getString(R.string.msgs_profile_sync_task_sync_option_ignore_never_overwrite_target_file_if_it_is_newer_than_the_master_file_option_enabled),
                                    stwa.context.getString(R.string.msgs_mirror_task_file_ignored));
                        }
                    }
                }
            }
        }
        return result;
    }

    static final private boolean isFileChangedDetailCompare(SyncThreadWorkArea stwa, SyncTaskItem sti, String fp,
                                                            File tf,//Target
                                                            boolean mf_exists, long mf_time, long mf_length,//Master
                                                            boolean ac) throws JcifsException {
        long tf_time = 0, tf_length = 0;
        boolean tf_exists=false;

//        if (tf.getPath().startsWith(stwa.gp.safMgr.getSdcardRootPath())) {
//            SafFile sf=stwa.gp.safMgr.findSdcardItem(tf.getPath());
//            if (sf!=null) {
//                tf_exists=true;
//                tf_time = sf.lastModified();
//                tf_length = sf.length();
//            }
//        } else {
//            tf_exists=tf.exists();
//            if (tf_exists) {
//                tf_time = tf.lastModified();
//                tf_length = tf.length();
//            }
//        }
        tf_exists=tf.exists();
        if (tf_exists) {
            tf_time = tf.lastModified();
            tf_length = tf.length();
        }

        return isFileChangedDetailCompare(stwa, sti, fp,
                tf_exists, tf_time, tf_length, tf.getPath(),//Target
                mf_exists, mf_time, mf_length, ac);//Master
    }

    static final private boolean isFileChangedDetailCompare(SyncThreadWorkArea stwa, SyncTaskItem sti, String fp,
                                                            boolean tf_exists, long tf_time, long tf_length, String tf_path,//Target
                                                            boolean mf_exists, long mf_time, long mf_length, boolean ac) {//Master
        boolean diff = false;
        boolean orphan_file = false;

        long time_diff = Math.abs((mf_time - tf_time));
        long length_diff = Math.abs((mf_length - tf_length));
        //String str=Long.toString(length_diff); showMsg(stwa, false, "length_diff=", "I", "", "", str);
        if (ac) { // boolean ALL_COPY
            diff = true;
        } else if (mf_exists != tf_exists) {
            orphan_file = true;
            diff = true;
        } else if (sti.isSyncOptionDifferentFileBySize() && length_diff > 0) {
            if (sti.isSyncDifferentFileSizeGreaterThanTagetFile()) {
                if (mf_length>tf_length) {
                    diff = true;
                }
            } else {
                diff = true;
            }
            if (diff && !stwa.lastModifiedIsFunctional) {//Update SMBSync2 Filelist
                if (tf_exists) {
                    updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, tf_path, tf_time, mf_time);
                } else {
                    boolean updated = updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, tf_path, tf_time, mf_time);
                    if (!updated) addLastModifiedItem(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, tf_path, tf_time, mf_time);
                }
            }
        } else if (sti.isSyncOptionDifferentFileByTime()) {//Check lastModified(). Compare by size_diff is disabled or length_diff == 0 --> compare same size files by time
            if (stwa.lastModifiedIsFunctional) {//Use lastModified
                if (time_diff > stwa.syncDifferentFileAllowableTime) { //LastModified was changed
                    if (sti.isSyncOptionIgnoreDstDifference()) {
                        if (Math.abs(time_diff-stwa.offsetOfDaylightSavingTime)<=stwa.syncDifferentFileAllowableTime) {
                            diff=false;
                        } else {
                            diff=true;
                        }
                    } else {
                        diff = true;
                    }
                } else {
                    diff = false;
                }
            } else {//Use SMBSync2 Filelist
                boolean found=isLocalFileLastModifiedFileItemExists(stwa, sti, stwa.currLastModifiedList, stwa.newLastModifiedList, tf_path);
                if (!found) {
                    if (time_diff > stwa.syncDifferentFileAllowableTime) {
                        if (sti.isSyncOptionIgnoreDstDifference()) {
                            if (Math.abs(time_diff-stwa.offsetOfDaylightSavingTime)<=stwa.syncDifferentFileAllowableTime) {
                                diff=false;
                            } else {
                                diff=true;
                            }
                        } else {
                            diff = true;
                        }
                    } else {
                        diff = false;
                    }
                    addLastModifiedItem(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, tf_path, tf_time, mf_time );
                } else {
                    diff = isLocalFileLastModifiedWasDifferent(stwa, sti,
                            stwa.currLastModifiedList,
                            stwa.newLastModifiedList,
                            tf_path, tf_time, mf_time);
                }
                stwa.util.addDebugMsg(3, "I", "isFileChangedDetailCompare FilItem Exists="+found);
            }
        } else if (!(sti.isSyncOptionDifferentFileBySize() && length_diff == 0)) { //length_diff == 0 or both compare by time_diff and size_diff are disabled --> if files are same size and compare by size was enabled, they are same, else:
            diff = true; //neither "compare by time" nor "compare by size" are enabled: always overwrite traget, do not update or use SMBSync2 List
        }
        if (stwa.gp.settingDebugLevel >= 3) {
            stwa.util.addDebugMsg(3, "I", "isFileChangedDetailCompare");
            if (tf_exists) stwa.util.addDebugMsg(3, "I", "Target file length=" + tf_length +
                    ", last modified(ms)=" + tf_time +
                    ", date=" + StringUtil.convDateTimeTo_YearMonthDayHourMinSec((tf_time / 1000) * 1000));
            else stwa.util.addDebugMsg(3, "I", "Target file was not exists");
            if (mf_exists) stwa.util.addDebugMsg(3, "I", "Master file length=" + mf_length +
                    ", last modified(ms)=" + mf_time +
                    ", date=" + StringUtil.convDateTimeTo_YearMonthDayHourMinSec((mf_time / 1000) * 1000));
            else stwa.util.addDebugMsg(3, "I", "Master file was not exists");
            stwa.util.addDebugMsg(3, "I", "allcopy=" + ac + ",orphan_file=" + orphan_file +
                    ",time_diff=" + time_diff + ",length_diff=" + length_diff + ", diff=" + diff);
        } else {
            stwa.util.addDebugMsg(1, "I", "isFileChanged fp="+fp+ ", orphan_file=" + orphan_file +
                    ", time_diff=" + time_diff + ", length_diff=" + length_diff + ", diff=" + diff+", target_time="+tf_time+", master_time="+mf_time);
        }
        if (stwa.gp.settingDebugLevel >= 1) {
            String lt_target=stwa.sdfLocalTime.format(tf_time);
            String lt_master=stwa.sdfLocalTime.format(mf_time);
            String ut_target=stwa.sdfUTCTime.format(tf_time);
            String ut_master=stwa.sdfUTCTime.format(mf_time);
            stwa.util.addDebugMsg(1, "I", "isFileChanged Local time target="+lt_target+", master="+lt_master);
            stwa.util.addDebugMsg(1, "I", "isFileChanged UTC        target="+ut_target+", master="+ut_master);
        }
        return diff;
    }

    static final public boolean isFileChangedForLocalToRemote(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                              String fp, File lf, JcifsFile hf, boolean ac) throws JcifsException {
        boolean diff = false;
        long hf_time = 0, hf_length = 0;
        boolean hf_exists = hf.exists();//Target

        if (hf_exists) {
            hf_time = hf.getLastModified();
            hf_length = hf.length();
        }
        long lf_time = 0, lf_length = 0;
        boolean lf_exists = lf.exists();//Master
        boolean orphan_file = false;

        if (lf_exists) {
            lf_time = lf.lastModified();
            lf_length = lf.length();
        }
        long time_diff = Math.abs((hf_time - lf_time));
        long length_diff = Math.abs((hf_length - lf_length));

        if (ac) { // boolean ALL_COPY
            diff = true;
        } else if (hf_exists != lf_exists) {
            orphan_file = true;
            diff = true;
        } else if (sti.isSyncOptionDifferentFileBySize() && length_diff > 0) {
            if (sti.isSyncDifferentFileSizeGreaterThanTagetFile()) {
                if (lf_length>hf_length) {
                    diff = true;
                }
            } else {
                diff = true;
            }
        } else if (sti.isSyncOptionDifferentFileByTime()) {//Check lastModified()
            if ((time_diff > stwa.syncDifferentFileAllowableTime)) { //LastModified was changed
                if (sti.isSyncOptionIgnoreDstDifference()) {
                    if (Math.abs(time_diff-stwa.offsetOfDaylightSavingTime)<=stwa.syncDifferentFileAllowableTime) { //difference is exactly dst_offset +/- user set tolerance (msec)
                        diff=false;
                    } else {
                        diff=true;
                    }
                } else {
                    diff = true;
                }
            } else {
                diff = false;
            }
        } else if (!(sti.isSyncOptionDifferentFileBySize() && length_diff == 0)) { //length_diff == 0 or both compare by time_diff and size_diff are disabled --> if files are same size and compare by size was enabled, they are same, else:
            diff = true; //neither "compare by time" nor "compare by size" are enabled: always overwrite traget
        }
        if (stwa.gp.settingDebugLevel >= 3) {
            stwa.util.addDebugMsg(3, "I", "isFileChangedForLocalToRemote");
            if (hf_exists) stwa.util.addDebugMsg(3, "I", "Remote file length=" + hf_length +
                    ", last modified(ms)=" + hf_time +
                    ", date=" + StringUtil.convDateTimeTo_YearMonthDayHourMinSec((hf_time / 1000) * 1000));
            else stwa.util.addDebugMsg(3, "I", "Remote file was not exists");
            if (lf_exists) stwa.util.addDebugMsg(3, "I", "Local  file length=" + lf_length +
                    ", last modified(ms)=" + lf_time +
                    ", date=" + StringUtil.convDateTimeTo_YearMonthDayHourMinSec((lf_time / 1000) * 1000));
            else stwa.util.addDebugMsg(3, "I", "Local  file was not exists");
            stwa.util.addDebugMsg(3, "I", "allcopy=" + ac + ",orphan_file=" + orphan_file +
                    ",time_diff=" + time_diff +//", time_zone_diff="+time_diff_tz1+
                    ",length_diff=" + length_diff + ", diff=" + diff);
        } else {
            stwa.util.addDebugMsg(1, "I", "isFileChangedForLocalToRemote fp="+fp+ ", orphan_file=" + orphan_file +
                    ", time_diff=" + time_diff + ", length_diff=" + length_diff + ", diff=" + diff+", target_time="+hf_time+", master_time="+lf_time);
        }
        if (stwa.gp.settingDebugLevel >= 1) {
            String lt_target=stwa.sdfLocalTime.format(lf_time);
            String lt_master=stwa.sdfLocalTime.format(hf_time);
            String ut_target=stwa.sdfUTCTime.format(lf_time);
            String ut_master=stwa.sdfUTCTime.format(lf_time);
            stwa.util.addDebugMsg(1, "I", "isFileChangedForLocalToRemote Local time target="+lt_target+", master="+lt_master);
            stwa.util.addDebugMsg(1, "I", "isFileChangedForLocalToRemote UTC        target="+ut_target+", master="+ut_master);
        }

        return diff;
    }

    static public boolean isRetryRequiredError(int sc) {
        if (sc==0 || sc==0xc000006d || sc==0xc0000043) return false;
        else return true;
    }

    static public SafFile createSafFile(SyncThreadWorkArea stwa, SyncTaskItem sti, String fp) {
        return createSafFile(stwa, sti, fp, false);
    }

    static public SafFile createSafFile(SyncThreadWorkArea stwa, SyncTaskItem sti, String fp, boolean isDirectory) {
        SafFile t_df =null;
        if (fp.startsWith(stwa.gp.safMgr.getSdcardRootPath())) {
            t_df = stwa.gp.safMgr.createSdcardItem(fp, isDirectory);
            if (t_df == null) {
                String saf_name = "";
                SafFile sf = stwa.gp.safMgr.getSdcardRootSafFile();
                if (sf != null) saf_name = sf.getName();
                stwa.util.addLogMsg("E", "SDCARD file create error. path=" + fp + ", SafFile=" + saf_name +
                        ", sdcard=" + stwa.gp.safMgr.getSdcardRootPath());
                stwa.util.addLogMsg("E", "SafManager msg="+stwa.gp.safMgr.getLastErrorMessage() );
                return null;
            }
        } else {
            t_df = stwa.gp.safMgr.createUsbItem(fp, isDirectory);
            if (t_df == null) {
                String saf_name = "";
                SafFile sf = stwa.gp.safMgr.getUsbRootSafFile();
                if (sf != null) saf_name = sf.getName();
                stwa.util.addLogMsg("E", "USB file create error. path=" + fp + ", SafFile=" + saf_name +
                        ", usb=" + stwa.gp.safMgr.getUsbRootPath());
                stwa.util.addLogMsg("E", "SafManager msg="+stwa.gp.safMgr.getLastErrorMessage() );
                return null;
            }
        }
        return t_df;
    }

    static public boolean isHiddenDirectory(SyncThreadWorkArea stwa, SyncTaskItem sti, File lf) {
        boolean result = false;
        if (sti.isSyncOptionSyncHiddenDirectory()) result = false;
        else {
            if (lf.getName().substring(0, 1).equals(".")) result = true;
        }
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "isHiddenDirectory(Local) result=" + result + ", Name=" + lf.getName());
        return result;
    }

    static public boolean isHiddenDirectory(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile hf) throws JcifsException {
        boolean result = false;
        if (sti.isSyncOptionSyncHiddenDirectory()) result = false;
        else {
            if (hf.isHidden()) result = true;
        }
        if (stwa.gp.settingDebugLevel >= 2) {
            String name = hf.getName().replace("/", "");
            stwa.util.addDebugMsg(2, "I", "isHiddenDirectory(Remote) result=" + result + ", Name=" + name);
        }
        return result;
    }

    static public boolean isHiddenFile(SyncThreadWorkArea stwa, SyncTaskItem sti, File lf) {
        boolean result = false;
        if (sti.isSyncOptionSyncHiddenFile()) result = false;
        else {
            if (lf.getName().substring(0, 1).equals(".")) result = true;
        }
        if (stwa.gp.settingDebugLevel >= 2) {
            stwa.util.addDebugMsg(2, "I", "isHiddenFile(Local) result=" + result + ", Name=" + lf.getName());
        }
        return result;
    }

    static public boolean isHiddenFile(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile hf) throws JcifsException {
        boolean result = false;
        if (sti.isSyncOptionSyncHiddenFile()) result = false;
        else {
            if (hf.isHidden()) result = true;
        }
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "isHiddenFile(Remote) result=" + result + ", Name=" + hf.getName().replace("/", ""));
        return result;
    }

    static final public boolean isFileSelected(SyncThreadWorkArea stwa, SyncTaskItem sti, String url) {
        boolean filtered = false;
        Matcher mt;

        if (!sti.isSyncProcessRootDirFile()) {//「root直下のファイルは処理するオプションが無効
            String tmp_d = "", tmp_url = url;
            if (url.startsWith("/")) tmp_url = url.substring(1);

            if (sti.getMasterDirectoryName().equals("")) {
                if (tmp_url.substring(tmp_url.length()).equals("/"))
                    tmp_d = tmp_url.substring(0, tmp_url.length() - 1);
                else tmp_d = tmp_url;
            } else {
                if (tmp_url.substring(tmp_url.length()).equals("/"))
                    tmp_d = tmp_url.replace(sti.getMasterDirectoryName() + "/", "");
                else tmp_d = tmp_url.replace(sti.getMasterDirectoryName(), "");
            }

            if (tmp_d.indexOf("/") < 0) {
                //root直下なので処理しない
                if (stwa.gp.settingDebugLevel >= 2)
                    stwa.util.addDebugMsg(2, "I", "isFileSelected not filtered, " +
                            "because Master Dir not processed was effective");
                return false;
            }
        }

        String temp_fid = url.substring(url.lastIndexOf("/") + 1, url.length());
        if (stwa.fileFilterInclude == null) {
            // nothing filter
            filtered = true;
        } else {
            mt = stwa.fileFilterInclude.matcher(temp_fid);
            if (mt.find()) filtered = true;
            if (stwa.gp.settingDebugLevel >= 2)
                stwa.util.addDebugMsg(2, "I", "isFileSelected Include result:" + filtered);
        }
        if (stwa.fileFilterExclude == null) {
            //nop
        } else {
            mt = stwa.fileFilterExclude.matcher(temp_fid);
            if (mt.find()) filtered = false;
            if (stwa.gp.settingDebugLevel >= 2)
                stwa.util.addDebugMsg(2, "I", "isFileSelected Exclude result:" + filtered);
        }
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "isFileSelected result:" + filtered);
        return filtered;
    }

    static final public boolean isDirectorySelectedByFileName(SyncThreadWorkArea stwa, String f_dir_name) {

        String n_fp = "";
        String t_dir = f_dir_name;
        String n_dir = "";
        if (f_dir_name.startsWith("/")) t_dir = f_dir_name.substring(1);
        if (t_dir.endsWith("/")) n_fp = t_dir.substring(0, t_dir.length());
        else n_fp = t_dir;

        if (n_fp.lastIndexOf("/") > 0) n_dir = n_fp.substring(0, n_fp.lastIndexOf("/"));
        boolean result = isDirectorySelectedByDirectoryName(stwa, n_dir);
        return result;
    }

    static final private boolean isDirectorySelectedByDirectoryName(SyncThreadWorkArea stwa, String f_dir) {
        if (stwa.currentSTI.isSyncOptionFixDirectoryFilterBug()) return isDirectorySelectedByDirectoryNameVer2(stwa, f_dir);
        else return isDirectorySelectedByDirectoryNameVer1(stwa, f_dir);
    }

    static final private boolean isDirectorySelectedByDirectoryNameVer2(SyncThreadWorkArea stwa, String f_dir) {
        boolean include = false, sel_include=false, exclude_begin=false, exclude_any=false;
        Matcher mt;

        String t_dir = f_dir;
        String n_dir = removeRedundantSeparator(f_dir, "/", true, true);
        String[] filter_dir_array=n_dir.split("/");

        if (n_dir.equals("")) {
            //not filtered
            include = true;
        } else {
            if (stwa.gp.settingDebugLevel >= 2) {
                stwa.util.addDebugMsg(2, "I", "isDirectorySelectedByDirectoryNameVer2 dir=" + n_dir);
            }

            if (stwa.matchFromBeginDirIncludeFilterList.size()==0) {
                // nothing filter
                include = true;
            } else {
                for (int i = 0; i < stwa.matchFromBeginDirIncludeFilterList.size(); i++) {
                    Pattern pattern=Pattern.compile("^"+ MiscUtil.convertRegExp(stwa.matchFromBeginDirIncludeFilterList.get(i).getFilter()+"/"));
                    mt = pattern.matcher(n_dir+"/");
                    if (mt.find()) {
                        include = true;
                        sel_include=true;
                        break;
                    }
                }
            }
            if (stwa.matchFromBeginDirExcludeFilterList.size()==0  && stwa.matchAnyDirExcludeFilterList.size()==0) {
                //Nop
            } else {
                if (stwa.matchFromBeginDirExcludeFilterList.size()!=0) {
                    for (AdapterFilterList.FilterListItem fli:stwa.matchFromBeginDirExcludeFilterList) {
                        String[] exc_filter_array=fli.getFilter().split("/");
                        if (exc_filter_array.length<=filter_dir_array.length) {
                            boolean matched=true;
                            for(int i=0;i<exc_filter_array.length;i++) {
                                Pattern exc_pattern=Pattern.compile("^"+ MiscUtil.convertRegExp(exc_filter_array[i])+"$");
                                Matcher exc_mt=exc_pattern.matcher(filter_dir_array[i]);
                                if (!exc_mt.find()) {
                                    matched=false;
                                    break;
                                }
                            }
                            if (matched) {
                                exclude_begin=true;
                                include = false;
                                break;
                            }
                        }
                    }
                } else {
                    String[] dir_array=n_dir.split("/");
                    for (AdapterFilterList.FilterListItem fli:stwa.matchAnyDirExcludeFilterList) {
                        String[] exc_filter_array=fli.getFilter().split("/");
                        String exc_key="/";
                        String dir_key="/";
                        if (dir_array.length>=exc_filter_array.length) {
                            for(int i=0;i<Math.min(dir_array.length, exc_filter_array.length);i++) {
                                exc_key+=exc_filter_array[i]+"/";
                                dir_key+=dir_array[i]+"/";
                            }
                            Pattern pattern=Pattern.compile("^"+ MiscUtil.convertRegExp(exc_key)+"$");
                            mt=pattern.matcher(dir_key);
                            boolean excluded=false;
                            if (mt.find()) {
                                include=false;
                                excluded=true;
                                exclude_any=true;
                                break;
                            }
                            if (excluded) break;
                        }
                    }
                }
            }
            if (stwa.gp.settingDebugLevel >= 2)
                stwa.util.addDebugMsg(2, "I", "isDirectorySelectedByDirectoryNameVer2 "+
                        "sel_include="+sel_include+", exclude_begin="+exclude_begin+", exclude_any="+exclude_any+
                        ", result=" + include+", dir="+n_dir);
        }
        return include;
    }


    static final private boolean isDirectorySelectedByDirectoryNameVer1(SyncThreadWorkArea stwa, String f_dir) {
        boolean filtered = false;
        Matcher mt;

        String t_dir = f_dir;
        String n_dir = "";
        if (f_dir.startsWith("/")) t_dir = f_dir.substring(1);
        if (!t_dir.endsWith("/")) n_dir = t_dir + "/";
        else n_dir = t_dir;

        if (n_dir.equals("/")) {
            //not filtered
            filtered = true;
        } else {
            if (stwa.gp.settingDebugLevel >= 2) {
                stwa.util.addDebugMsg(2, "I", "isDirectorySelectedByDirectoryNameVer1 dir=" + n_dir);
            }

            Pattern[] inc_matched_pattern_array = new Pattern[0];
            String matched_inc_dir="";
            String matched_exc_dir="";
            if (stwa.dirIncludeFilterPatternList.size()==0 && stwa.wholeDirIncludeFilterPatternList.size()==0) {
                // nothing filter
                filtered = true;
            } else {
                for (int i = 0; i < stwa.dirIncludeFilterPatternList.size(); i++) {
                    mt = stwa.dirIncludeFilterPatternList.get(i).matcher(n_dir);
                    if (mt.find()) {
                        inc_matched_pattern_array = stwa.dirIncludeFilterArrayList.get(i);
                        String filter = "";
                        for (int j = 0; j < inc_matched_pattern_array.length; j++) {
                            filter += inc_matched_pattern_array[j].toString() + "/";
                        }
                        filtered = true;
                        break;
                    }
                }
                if (!filtered) {
                    for(int i=0;i<stwa.wholeDirIncludeFilterPatternList.size();i++) {
                        mt = stwa.wholeDirIncludeFilterPatternList.get(i).matcher(n_dir);
                        if (mt.find()) {
                            matched_inc_dir=n_dir.substring(0,mt.end());
                            filtered = true;
                            break;
                        }
                    }
                }
                if (stwa.gp.settingDebugLevel >= 2)
                    stwa.util.addDebugMsg(2, "I", "isDirectorySelectedByDirectoryNameVer1 Include result:" + filtered);
            }
            if (stwa.dirExcludeFilterPatternList.size()==0  && stwa.wholeDirExcludeFilterPatternList.size()==0) {
                //nop
            } else {
                if (stwa.dirExcludeFilterPatternList.size()!=0) {
                    for (int i = 0; i < stwa.dirExcludeFilterPatternList.size(); i++) {
                        mt = stwa.dirExcludeFilterPatternList.get(i).matcher(n_dir);
                        if (mt.find()) {
                            if (stwa.currentSTI.isSyncOptionUseExtendedDirectoryFilter1()) {
                                Pattern[] exc = new Pattern[0];
                                if (stwa.dirExcludeFilterArrayList.size() > i) {
                                    exc = stwa.dirExcludeFilterArrayList.get(i);
                                }
                                String filter = "";
                                for (int j = 0; j < exc.length; j++) {
                                    filter += exc[j].toString() + "/";
                                }
                                if (inc_matched_pattern_array.length > exc.length) {
                                    //Selected this entry
                                } else {
                                    filtered = false;
                                }
                            } else {
                                filtered = false;
                            }
                        }
                    }
                } else {
                    String[] dir_array=n_dir.split("/");
                    String sep="";
                    matched_exc_dir="";
                    for (int i = 0; i < stwa.wholeDirExcludeFilterPatternList.size(); i++) {
                        Pattern pattern = stwa.wholeDirExcludeFilterPatternList.get(i);
                        for(String dir_item:dir_array) {
                            mt=pattern.matcher(dir_item);
                            matched_exc_dir+=sep+dir_item;
                            sep="/";
                            if (mt.find()) {
                                filtered=true;
                                break;
                            }
                        }
                        if (filtered) break;
                    }
                    int ll=matched_exc_dir.length();
                }
                if (stwa.gp.settingDebugLevel >= 2)
                    stwa.util.addDebugMsg(2, "I", "isDirectorySelectedByDirectoryNameVer1 Exclude result:" + filtered);
            }
            if (stwa.gp.settingDebugLevel >= 2)
                stwa.util.addDebugMsg(2, "I", "isDirectorySelectedByDirectoryNameVer1 result:" + filtered);
        }
        return filtered;
    }

    static final public boolean isDirectoryToBeProcessed(SyncThreadWorkArea stwa, String abs_dir) {
        if (stwa.currentSTI.isSyncOptionFixDirectoryFilterBug()) return isDirectoryToBeProcessedVer2(stwa, abs_dir);
        else return isDirectoryToBeProcessedVer1(stwa, abs_dir);
    }

    static final public boolean isDirectoryToBeProcessedVer2(SyncThreadWorkArea stwa, String abs_dir) {
        boolean inc = false, exc = false, result = false, inc_specified=false, exc_specified=false;

        String filter_dir = removeRedundantSeparator(abs_dir, "/", true, true);
        String[] filter_dir_array=filter_dir.split("/");
        if (filter_dir.length() != 0) {
            if (stwa.matchFromBeginDirIncludeFilterList.size()==0) inc = true;
            else {
                if (stwa.matchFromBeginDirIncludeFilterList.size()>0) {
                    inc_specified=true;
                    for(AdapterFilterList.FilterListItem fli:stwa.matchFromBeginDirIncludeFilterList) {
                        String[] inc_filter_array=fli.getFilter().split("/");
                        boolean found=false;
                        String inc_filter="", inc_dir="";
                        for(int i=0;i<Math.min(filter_dir_array.length, inc_filter_array.length);i++) {
                            inc_filter+=inc_filter_array[i]+"/";
                            inc_dir+=filter_dir_array[i]+"/";
                        }
                        Pattern pattern=Pattern.compile("^"+ MiscUtil.convertRegExp(inc_filter)+"$");
                        Matcher mt=pattern.matcher(inc_dir);
                        if (mt.find()) {
                            inc=true;
                            break;
                        }
                    }
                }
            }
            if (!inc_specified) inc=true;

            if (stwa.matchFromBeginDirExcludeFilterList.size()==0 && stwa.matchAnyDirExcludeFilterList.size()==0) exc = false;
            else {
                exc = false;
                if (stwa.matchFromBeginDirExcludeFilterList.size()!=0) {
                    exc_specified=true;
                    for(AdapterFilterList.FilterListItem fli:stwa.matchFromBeginDirExcludeFilterList) {
                        String[] exc_filter_array=fli.getFilter().split("/");
                        if (exc_filter_array.length<=filter_dir_array.length) {
                            boolean matched=true;
                            for(int i=0;i<exc_filter_array.length;i++) {
                                Pattern exc_pattern=Pattern.compile("^"+ MiscUtil.convertRegExp(exc_filter_array[i])+"$");
                                Matcher exc_mt=exc_pattern.matcher(filter_dir_array[i]);
                                if (!exc_mt.find()) {
                                    matched=false;
                                    break;
                                }
                            }
                            if (matched) {
                                if (stwa.matchFromBeginDirIncludeFilterList.size()>0) {
                                    boolean found=false;
                                    for(AdapterFilterList.FilterListItem inc_filter:stwa.matchFromBeginDirIncludeFilterList) {
                                        String[] inc_filter_array=inc_filter.getFilter().split("/");
                                        for(int i=0;i<Math.min(filter_dir_array.length, exc_filter_array.length);i++) {
                                            Pattern inc_pattern=Pattern.compile("^"+ MiscUtil.convertRegExp(inc_filter_array[i])+"$");
                                            Matcher inc_mt=inc_pattern.matcher(filter_dir_array[i]);
                                            if (inc_mt.find()) {
                                                if (exc_filter_array.length<inc_filter_array.length) {
                                                    found=true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (found) {
                                        exc=false;
                                        break;
                                    } else {
                                        exc=true;
                                        break;
                                    }
                                } else {
                                    exc=true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (stwa.matchAnyDirExcludeFilterList.size()!=0 && !exc) {
                    exc_specified=true;
                    boolean found=false;
                    for(AdapterFilterList.FilterListItem fli:stwa.matchAnyDirExcludeFilterList) {
                        String[] exc_filter_array=fli.getFilter().split("/");
                        for(String exc_dir:filter_dir_array) {
                            for(String exc_filter:exc_filter_array) {
                                Pattern exc_pattern=Pattern.compile("^"+ MiscUtil.convertRegExp(exc_filter)+"$");
                                Matcher exc_mt=exc_pattern.matcher(exc_dir);
                                if (exc_mt.find()) {
                                    found=true;
                                    if (stwa.matchFromBeginDirIncludeFilterList.size()>0) {
                                        boolean found_inc=false;
                                        for(AdapterFilterList.FilterListItem inc_filter:stwa.matchFromBeginDirIncludeFilterList) {
                                            String[] inc_filter_array=inc_filter.getFilter().split("/");
                                            for(int i=0;i<Math.min(filter_dir_array.length, exc_filter_array.length);i++) {
                                                Pattern inc_pattern=Pattern.compile("^"+ MiscUtil.convertRegExp(inc_filter_array[i])+"$");
                                                Matcher inc_mt=inc_pattern.matcher(filter_dir_array[i]);
                                                if (inc_mt.find()) {
                                                    if (exc_filter_array.length<inc_filter_array.length) {
                                                        found_inc=true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (found_inc) {
                                            exc=false;
                                            break;
                                        } else {
                                            exc=true;
                                            break;
                                        }
                                    } else {
                                        exc=true;
                                        break;
                                    }
                                }
                            }
                            if (found) break;
                        }
                        if (found) break;
                    }
                }
            }

            if (exc) result = false;
            else if (inc) result = true;
            else  if (!inc && !exc) result=false;
            else result = false;
        } else {
            result = true;
            inc = exc = false;
        }
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "isDirectoryToBeProcessedVer2" +
                    " include=" + inc + ", exclude=" + exc + ", result=" + result + ", dir=" + abs_dir);
        return result;
    }


    static final public boolean isDirectoryToBeProcessedVer1(SyncThreadWorkArea stwa, String abs_dir) {
        boolean inc = false, exc = false, result = false;

        String filter_dir = "";
        Pattern[] matched_inc_array = null;
        String matched_inc_dir="", matched_exc_dir="";
        Pattern[] matched_exc_array = null;
        if (abs_dir.length() != 0) {
            if (stwa.dirIncludeFilterArrayList.size()>0 || stwa.dirExcludeFilterPatternList.size()>0 ||
                    stwa.wholeDirIncludeFilterPatternList.size()>0 || stwa.wholeDirExcludeFilterPatternList.size()>0) {
                if (abs_dir.endsWith("/")) filter_dir = abs_dir.substring(0, abs_dir.length() - 1);
                else filter_dir = abs_dir;
            }
            if (stwa.dirIncludeFilterArrayList.size()==0 && stwa.wholeDirIncludeFilterPatternList.size()==0) inc = true;
            else {
                String[] dir_array = null;
                if (filter_dir.startsWith("/")) dir_array = filter_dir.substring(1).split("/");
                else dir_array = filter_dir.split("/");
                for (int i = 0; i < stwa.dirIncludeFilterArrayList.size(); i++) {
                    Pattern[] pattern_array = stwa.dirIncludeFilterArrayList.get(i);
                    boolean found = true;
                    for (int j = 0; j < Math.min(dir_array.length, pattern_array.length); j++) {
                        Matcher mt = pattern_array[j].matcher(dir_array[j]);
                        matched_inc_dir+="/"+dir_array[j];
                        if (dir_array[j].length() != 0) {
                            found = mt.find();
                            if (!found) {
                                break;
                            }
                        }
                    }
                    if (found) {
                        inc = true;
                        matched_inc_array = pattern_array;
                        break;
                    }
                }
                if (!inc) {
                    for (int i = 0; i < stwa.wholeDirIncludeFilterPatternList.size(); i++) {
                        Pattern pattern = stwa.wholeDirIncludeFilterPatternList.get(i);
                        Matcher mt=pattern.matcher(filter_dir);
                        if (mt.find()) {
                            inc=true;
                            matched_inc_dir=filter_dir.substring(0, mt.end());
                            break;
                        }
                    }
                }
            }
            if (stwa.dirExcludeFilterPatternList.size()==0 && stwa.wholeDirExcludeFilterPatternList.size()==0) exc = false;
            else {
                exc = false;
                if (stwa.dirExcludeFilterPatternList.size()!=0) {
                    for (int i = 0; i < stwa.dirExcludeFilterPatternList.size(); i++) {
                        Pattern filter_pattern = stwa.dirExcludeFilterPatternList.get(i);
                        Matcher mt = filter_pattern.matcher(filter_dir);
                        if (mt.find()) {
                            if (stwa.currentSTI.isSyncOptionUseExtendedDirectoryFilter1()) {
                                if (matched_inc_array != null) {
                                    if (matched_inc_array.length > stwa.dirExcludeFilterArrayList.get(i).length) {
                                    } else {
                                        exc = true;
                                        break;
                                    }
                                } else {
                                    //add whole directory exclude process
                                    exc = true;
                                    break;
                                }
                            } else {
                                exc = true;
                                break;
                            }
                        }
                        if (exc) break;
                    }
                } else {
                    String[] dir_array=filter_dir.split("/");
                    String sep="";
                    matched_exc_dir="";
                    for (int i = 0; i < stwa.wholeDirExcludeFilterPatternList.size(); i++) {
                        Pattern pattern = stwa.wholeDirExcludeFilterPatternList.get(i);
                        for(String dir_item:dir_array) {
                            Matcher mt=pattern.matcher(dir_item);
                            matched_exc_dir+=sep+dir_item;
                            sep="/";
                            if (mt.find()) {
                                exc=true;
                                break;
                            }
                        }
                        if (exc) break;
                    }
                    int ll=matched_exc_dir.length();
                }
            }

            if (exc) result = false;
            else if (inc) result = true;
            else result = false;
        } else {
            result = true;
            inc = exc = false;
        }
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "isDirectoryToBeProcessedVer1" +
                    " include=" + inc + ", exclude=" + exc + ", result=" + result + ", dir=" + abs_dir);
        return result;
    }

    private void addPresetFileFilter(ArrayList<String> ff, String[] preset_ff) {
        for (String add_str : preset_ff) {
            boolean found = false;
            for (String ff_str : ff) {
                if (ff_str.substring(1).equals(add_str)) {
                    found = true;
                    break;
                }
            }
            if (!found) ff.add("I" + add_str);
            else if (mStwa.gp.settingDebugLevel >= 1)
                mStwa.util.addDebugMsg(1, "I", "addPresetFileFilter" + " Duplicate file filter=" + add_str);
        }
    }

    final private int compileFilter(SyncTaskItem sti, ArrayList<String> s_ff, ArrayList<String> s_df) {
        if (sti.isSyncOptionFixDirectoryFilterBug()) return compileFilterVer2(sti, s_ff, s_df);
        else return compileFilterVer1(sti, s_ff, s_df);
    }

    static private String removeRedundantSeparator(String input, String separator, boolean remove_start, boolean remove_end) {
        String out=input;
        while(out.indexOf(separator+separator)>=0) {
            out=out.replaceAll(separator+separator, separator);
        }
        if (remove_start) {
            out=out.startsWith(separator)?out.substring(1):out;
        }
        if (remove_end) {
            out=out.endsWith(separator)?out.substring(0, out.length()-1):out;
        }
        return out;
    }

    final private int compileFilterVer2(SyncTaskItem sti, ArrayList<String> s_ff, ArrayList<String> s_df) {
        ArrayList<String> ff = new ArrayList<String>();
        ff.addAll(s_ff);
        if (sti.isSyncFileTypeAudio()) addPresetFileFilter(ff, SYNC_FILE_TYPE_AUDIO);
        if (sti.isSyncFileTypeImage()) addPresetFileFilter(ff, SYNC_FILE_TYPE_IMAGE);
        if (sti.isSyncFileTypeVideo()) addPresetFileFilter(ff, SYNC_FILE_TYPE_VIDEO);
        Collections.sort(ff);

        mStwa.matchFromBeginDirIncludeFilterList.clear();
        mStwa.matchFromBeginDirExcludeFilterList.clear();
        mStwa.matchAnyDirExcludeFilterList.clear();
        for (String filter:s_df) {
            String filter_inc_exc=filter.substring(0, 1);
            String filter_value=filter.substring(1);
            if (filter_value.startsWith(WHOLE_DIRECTORY_FILTER_PREFIX)) {
                String remove_filter_prefix=removeRedundantSeparator(filter_value.substring(2, filter_value.length()), "\\", true, true);
                String remove_directory_separator=removeRedundantSeparator(remove_filter_prefix, "/", true, true);
                AdapterFilterList.FilterListItem fli=new AdapterFilterList.FilterListItem(remove_directory_separator, filter_inc_exc.equals("I"));
                if (fli.isInclude()) {
                    String be = mStwa.context.getString(R.string.msgs_mirror_whole_directory_filter_error,
                            WHOLE_DIRECTORY_FILTER_PREFIX+fli.getFilter());
                    showMsg(mStwa, true, mStwa.currentSTI.getSyncTaskName(), "E", "", "", be);
                    mGp.syncThreadCtrl.setThreadMessage(be);
                    return SyncTaskItem.SYNC_STATUS_ERROR;
                }
                mStwa.matchAnyDirExcludeFilterList.add(fli);
            } else {
                if (filter_value.indexOf(";")>=0) {
                    String reformat_filter=removeRedundantSeparator(filter_value, ";", true, true);
                    String[] filter_parts=reformat_filter.split(";");
                    if (filter_parts.length>0) {
                        for(String filter_item:filter_parts) {
                            String new_filter=removeRedundantSeparator(filter_item, "/", true, true);
                            AdapterFilterList.FilterListItem fli=new AdapterFilterList.FilterListItem(new_filter, filter_inc_exc.equals("I"));
                            if (fli.isInclude()) mStwa.matchFromBeginDirIncludeFilterList.add(fli);
                            else mStwa.matchFromBeginDirExcludeFilterList.add(fli);
                        }
                    }
                } else {
                    String new_filter=removeRedundantSeparator(filter_value, "/", true, true);
                    AdapterFilterList.FilterListItem fli=new AdapterFilterList.FilterListItem(new_filter, filter_inc_exc.equals("I"));
                    if (fli.isInclude()) mStwa.matchFromBeginDirIncludeFilterList.add(fli);
                    else mStwa.matchFromBeginDirExcludeFilterList.add(fli);
                }
            }
        }
        Collections.sort(mStwa.matchFromBeginDirIncludeFilterList, new Comparator<AdapterFilterList.FilterListItem>() {
            @Override
            public int compare(AdapterFilterList.FilterListItem s, AdapterFilterList.FilterListItem t1) {
                return t1.getFilter().compareTo(s.getFilter());
            }
        });

        Collections.sort(mStwa.matchFromBeginDirExcludeFilterList, new Comparator<AdapterFilterList.FilterListItem>() {
            @Override
            public int compare(AdapterFilterList.FilterListItem s, AdapterFilterList.FilterListItem t1) {
                return t1.getFilter().compareTo(s.getFilter());
            }
        });

        Collections.sort(mStwa.matchAnyDirExcludeFilterList, new Comparator<AdapterFilterList.FilterListItem>() {
            @Override
            public int compare(AdapterFilterList.FilterListItem s, AdapterFilterList.FilterListItem t1) {
                return t1.getFilter().compareTo(s.getFilter());
            }
        });

        String all_inc="", all_exc="";
        for(AdapterFilterList.FilterListItem fli:mStwa.matchFromBeginDirIncludeFilterList) all_inc+=fli.getFilter()+";";
        for(AdapterFilterList.FilterListItem fli:mStwa.matchFromBeginDirExcludeFilterList) all_exc+=fli.getFilter()+";";
        mStwa.util.addDebugMsg(1, "I", "compileFilterVer2" + " Directory include=" + all_inc);
        mStwa.util.addDebugMsg(1, "I", "compileFilterVer2" + " Directory exclude=" + all_exc);

        all_inc=all_exc="";
        for(AdapterFilterList.FilterListItem fli:mStwa.matchAnyDirExcludeFilterList) all_exc+=fli.getFilter()+";";
        mStwa.util.addDebugMsg(1, "I", "compileFilterVer2" + " Whole Directory exclude=" + all_exc);

        int flags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
        String ffinc = "", ffexc = "";
        if (ff.size() != 0) {
            String prefix, filter, cni = "", cne = "";
            for (int j = 0; j < ff.size(); j++) {
                prefix = ff.get(j).substring(0, 1);
                filter = ff.get(j).substring(1, ff.get(j).length());
                String rem_filter=removeRedundantSeparator(filter, ";", true, true);
                if (prefix.equals("I")) {
                    ffinc = ffinc + cni + "^"+ MiscUtil.convertRegExp(rem_filter)+"$";
                    cni = "|";
                } else {
                    ffexc = ffexc + cne + "^"+ MiscUtil.convertRegExp(rem_filter)+"$";
                    cne = "|";
                }
            }
        }

        mStwa.fileFilterInclude = mStwa.fileFilterExclude = null;
        if (ffinc.length() != 0) mStwa.fileFilterInclude = Pattern.compile(ffinc, flags);
        if (ffexc.length() != 0) mStwa.fileFilterExclude = Pattern.compile(ffexc, flags);

        if (mStwa.gp.settingDebugLevel >= 1)
            mStwa.util.addDebugMsg(1, "I", "compileFilterVer2" + " File include=" + ffinc + ", exclude=" + ffexc);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    final private int compileFilterVer1(SyncTaskItem sti, ArrayList<String> s_ff, ArrayList<String> s_df) {
        ArrayList<String> ff = new ArrayList<String>();
        ff.addAll(s_ff);
        if (sti.isSyncFileTypeAudio()) addPresetFileFilter(ff, SYNC_FILE_TYPE_AUDIO);
        if (sti.isSyncFileTypeImage()) addPresetFileFilter(ff, SYNC_FILE_TYPE_IMAGE);
        if (sti.isSyncFileTypeVideo()) addPresetFileFilter(ff, SYNC_FILE_TYPE_VIDEO);
        Collections.sort(ff);

        ArrayList<String> discreet_df = new ArrayList<String>();
        ArrayList<String> whole_df = new ArrayList<String>();
        for (String filter:s_df) {
            String filter_name=filter.substring(1);
            if (filter_name.startsWith(WHOLE_DIRECTORY_FILTER_PREFIX)) {
                whole_df.add(filter);
            } else {
                discreet_df.add(filter);
            }
        }
        Collections.sort(discreet_df, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return t1.substring(1).compareTo(s.substring(1));
            }
        });
        Collections.sort(whole_df, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return t1.substring(1).compareTo(s.substring(1));
            }
        });

        int flags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
        String ffinc = "", ffexc = "", dfinc = "", dfexc = "";
        String whole_dfinc = "", whole_dfexc = "";
        if (ff.size() != 0) {
            String prefix, filter, cni = "", cne = "";
            for (int j = 0; j < ff.size(); j++) {
                prefix = ff.get(j).substring(0, 1);
                filter = ff.get(j).substring(1, ff.get(j).length());
                String rem_filter=filter;
                while(rem_filter.indexOf(";;")>=0) rem_filter=rem_filter.replaceAll(";;",";");
                if (rem_filter.endsWith(";")) rem_filter=rem_filter.substring(0,rem_filter.length()-1);
                if (prefix.equals("I")) {
//                    ffinc = ffinc + cni + MiscUtil.convertRegExp("^"+filter+"$");
                    ffinc = ffinc + cni + "^"+ MiscUtil.convertRegExp(rem_filter)+"$";
                    cni = "|";
                } else {
//                    ffexc = ffexc + cne + MiscUtil.convertRegExp("^"+filter+"$");
                    ffexc = ffexc + cne + "^"+ MiscUtil.convertRegExp(rem_filter)+"$";
                    cne = "|";
                }
            }
        }
        mStwa.dirIncludeFilterArrayList.clear();
        mStwa.dirExcludeFilterArrayList.clear();
        mStwa.dirIncludeFilterPatternList.clear();
        mStwa.dirExcludeFilterPatternList.clear();
        if (discreet_df.size() != 0) {
            String prefix, filter, cni = "", cne = "";
            String all_inc = "", all_exc = "";
            for (int j = 0; j < discreet_df.size(); j++) {
                prefix = discreet_df.get(j).substring(0, 1);
                filter = discreet_df.get(j).substring(1, discreet_df.get(j).length());
                createDirFilterArrayListVer1(prefix, filter);
                String pre_str = "", suf_str = "/";
                String rem_filter=filter;
                while(rem_filter.indexOf(";;")>=0) rem_filter=rem_filter.replaceAll(";;",";");
                if (rem_filter.endsWith(";")) rem_filter=rem_filter.substring(0,rem_filter.length()-1);
                if (!rem_filter.startsWith("*")) pre_str = "^";
                if (prefix.equals("I")) {
                    dfinc = pre_str + MiscUtil.convertRegExp(rem_filter);
                    mStwa.dirIncludeFilterPatternList.add(Pattern.compile("(" + dfinc + ")", flags));
                    all_inc += dfinc + ";";
                } else {
                    dfexc = pre_str + MiscUtil.convertRegExp(rem_filter);
                    mStwa.dirExcludeFilterPatternList.add(Pattern.compile("(" + dfexc + ")", flags));
                    all_exc += dfexc + ";";
                }
            }
            mStwa.util.addDebugMsg(1, "I", "compileFilterVer1" + " Directory include=" + all_inc);
            mStwa.util.addDebugMsg(1, "I", "compileFilterVer1" + " Directory exclude=" + all_exc);
        }

        mStwa.wholeDirIncludeFilterPatternList.clear();
        mStwa.wholeDirExcludeFilterPatternList.clear();
        if (whole_df.size() != 0) {
            String prefix, filter, cni = "", cne = "";
            String all_inc = "", all_exc = "";
            for (int j = 0; j < whole_df.size(); j++) {
                prefix = whole_df.get(j).substring(0, 1);
                filter = whole_df.get(j).substring(1);
                String pre_str = "^";
                String suf_str = "$";
                String dir_name="";
                if (filter.startsWith(WHOLE_DIRECTORY_FILTER_PREFIX)) {
                    dir_name=filter.replace(WHOLE_DIRECTORY_FILTER_PREFIX, "");
                    String rem_dir_name=dir_name;
                    while(rem_dir_name.indexOf(";;")>=0) rem_dir_name=rem_dir_name.replaceAll(";;",";");
                    if (rem_dir_name.endsWith(";")) rem_dir_name=rem_dir_name.substring(0,rem_dir_name.length()-1);
                    if (prefix.equals("I")) {
                        whole_dfinc = pre_str + MiscUtil.convertRegExp(rem_dir_name)+suf_str;
                        mStwa.wholeDirIncludeFilterPatternList.add(Pattern.compile("(" + whole_dfinc + ")", flags));
                        all_inc += whole_dfinc + ";";
                    } else {
                        whole_dfexc = pre_str + MiscUtil.convertRegExp(rem_dir_name)+suf_str;
                        mStwa.wholeDirExcludeFilterPatternList.add(Pattern.compile("(" + whole_dfexc + ")", flags));
                        all_exc += whole_dfexc + ";";
                    }
                }
            }
            mStwa.util.addDebugMsg(1, "I", "compileFilterVer1" + " Whole Directory include=" + all_inc);
            mStwa.util.addDebugMsg(1, "I", "compileFilterVer1" + " Whole Directory exclude=" + all_exc);
        }

        mStwa.fileFilterInclude = mStwa.fileFilterExclude = null;
        if (ffinc.length() != 0)
            mStwa.fileFilterInclude = Pattern.compile("(" + ffinc + ")", flags);
        if (ffexc.length() != 0)
            mStwa.fileFilterExclude = Pattern.compile("(" + ffexc + ")", flags);

        if (mStwa.gp.settingDebugLevel >= 1)
            mStwa.util.addDebugMsg(1, "I", "compileFilterVer1" + " File include=" + ffinc + ", exclude=" + ffexc);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

//    final private void createDirFilterArrayListVer2(String prefix, String filter) {
//        int flags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
//        String[] filter_array = null;
//        if (filter.startsWith("/")) filter_array = filter.replaceFirst("/", "").split("/");
//        else filter_array = filter.split("/");
//
//        Pattern[] pattern_array = new Pattern[filter_array.length];
//
//        for (int k = 0; k < filter_array.length; k++) {
//            String filter_string=filter_array[k];
//            while(filter_string.indexOf(";;")>=0) filter_string=filter_string.replaceAll(";;",";");
//            if (filter_string.endsWith(";")) filter_string=filter_string.substring(0,filter_string.length()-1);
//            pattern_array[k] =
//                    Pattern.compile("^" + MiscUtil.convertRegExp(filter_string) + "$", flags);
//        }
//
//        if (prefix.equals("I")) {
//            mStwa.dirIncludeFilterArrayList.add(pattern_array);
//            String array_item = "";
//            for (int i = 0; i < pattern_array.length; i++) array_item += pattern_array[i] + "/";
//            mStwa.util.addDebugMsg(1, "I", "createDirFilterArrayListVer2" + " Directory include=" + array_item);
//
//        } else {
//            mStwa.dirExcludeFilterArrayList.add(pattern_array);
//            String array_item = "";
//            for (int i = 0; i < pattern_array.length; i++) array_item += pattern_array[i] + "/";
//            mStwa.util.addDebugMsg(1, "I", "createDirFilterArrayListVer2" + " Directory exclude=" + array_item);
//        }
//    }

    final private void createDirFilterArrayListVer1(String prefix, String filter) {
        int flags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE;
        String[] filter_array = null;
        if (filter.startsWith("/")) filter_array = filter.replaceFirst("/", "").split("/");
        else filter_array = filter.split("/");

        Pattern[] pattern_array = new Pattern[filter_array.length];

        for (int k = 0; k < filter_array.length; k++) {
            String filter_string=filter_array[k];
            while(filter_string.indexOf(";;")>=0) filter_string=filter_string.replaceAll(";;",";");
            if (filter_string.endsWith(";")) filter_string=filter_string.substring(0,filter_string.length()-1);
            pattern_array[k] =
                    Pattern.compile("^" + MiscUtil.convertRegExp(filter_string) + "$", flags);
        }

        if (prefix.equals("I")) {
            mStwa.dirIncludeFilterArrayList.add(pattern_array);
            String array_item = "";
            for (int i = 0; i < pattern_array.length; i++) array_item += pattern_array[i] + "/";
            mStwa.util.addDebugMsg(1, "I", "createDirFilterArrayListVer1" + " Directory include=" + array_item);

        } else {
            mStwa.dirExcludeFilterArrayList.add(pattern_array);
            String array_item = "";
            for (int i = 0; i < pattern_array.length; i++) array_item += pattern_array[i] + "/";
            mStwa.util.addDebugMsg(1, "I", "createDirFilterArrayListVer1" + " Directory exclude=" + array_item);
        }
    }

//    private void waitMediaScannerConnected() {
//        int cnt = 100;
//        while (!mStwa.mediaScanner.isConnected() && cnt > 0) {
//            SystemClock.sleep(100);
//            cnt--;
//        }
//    }

//    private void prepareMediaScanner() {
//        mStwa.mediaScanner = new MediaScannerConnection(mStwa.context, new MediaScannerConnectionClient() {
//            @Override
//            public void onMediaScannerConnected() {
//                if (mGp.settingDebugLevel >= 1)
//                    mStwa.util.addDebugMsg(1, "I", "MediaScanner connected.");
//            }
//            @Override
//            public void onScanCompleted(final String fp, final Uri uri) {
//                if (mGp.settingDebugLevel >= 2)
//                    mStwa.util.addDebugMsg(2, "I", "MediaScanner scan completed. fn=", fp, ", Uri=" + uri);
//            }
//        });
//        mStwa.mediaScanner.connect();
//    }

    @SuppressLint("DefaultLocale")
    static final public void scanMediaFile(SyncThreadWorkArea stwa, String fp) {
        try {
            MediaScannerConnection.scanFile(stwa.context, new String[]{fp}, null, null);
        } catch(Exception e) {
            stwa.util.addLogMsg("W","Media scanner scan failed, fp="+fp);
            stwa.util.addLogMsg("W",e.getMessage());
        }
//        stwa.util.addDebugMsg(2, "I", "MediaScanner scan request issued. fn=", fp);
//        if (!stwa.mediaScanner.isConnected()) {
//            stwa.util.addLogMsg("W", fp, "Media scanner not not invoked, because mdeia scanner was not connected.");
//            return;
//        }
//        stwa.mediaScanner.scanFile(fp, null);
    }

    private String mSyncHistroryResultFilepath = "";

    final private void openSyncResultLog(SyncTaskItem sti) {
        if (!mStwa.gp.settingWriteSyncResultLog) return;
        mSyncHistroryResultFilepath = mStwa.util.createSyncResultFilePath(sti.getSyncTaskName());
        if (mStwa.syncHistoryWriter != null) closeSyncResultLog();
        File lf = new File(mGp.settingMgtFileDir + "");
        try {
            FileWriter fos = new FileWriter(mSyncHistroryResultFilepath);
            BufferedWriter bow = new BufferedWriter(fos, 1024 * 256);
            mStwa.syncHistoryWriter = new PrintWriter(bow, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeSyncResultLog() {
        if (mStwa.syncHistoryWriter != null) {
            final PrintWriter pw = mStwa.syncHistoryWriter;
            Thread th = new Thread() {
                @Override
                public void run() {
                    pw.flush();
                    pw.close();
                }
            };
            th.start();
            mStwa.syncHistoryWriter = null;
        }
    }

    private void notifySyncResult() {
//        if (mNotificationSound) playBackDefaultNotification(wait_value1);
//        if (mNotificationVibrate) vibrateDefaultPattern(wait_value2);
    }


    final private void addHistoryList(SyncTaskItem sti,
                                      int status, int copy_cnt, int del_cnt, int ignore_cnt,
                                      int retry_cnt, String error_msg, String sync_elapsed_time, String sync_transfer_speed) {
        String date_time = StringUtil.convDateTimeTo_YearMonthDayHourMinSec(System.currentTimeMillis());
        String date = date_time.substring(0, 10);
        String time = date_time.substring(11);
        final SyncHistoryItem hli = new SyncHistoryItem();
        hli.sync_date = date;
        hli.sync_time = time;
        hli.sync_elapsed_time = sync_elapsed_time;
        hli.sync_prof = sti.getSyncTaskName();
        hli.sync_transfer_speed = sync_transfer_speed;
        hli.sync_status = status;
        hli.sync_test_mode = sti.isSyncTestMode();

        hli.sync_result_no_of_copied = copy_cnt;
        hli.sync_result_no_of_deleted = del_cnt;
        hli.sync_result_no_of_ignored = ignore_cnt;
        hli.sync_result_no_of_retry = retry_cnt;
        //hli.sync_req = mGp.syncThreadRequestID;
        hli.sync_req = mGp.syncThreadRequestIDdisplay; //for history, we save the translated ID to display
        hli.sync_error_text = error_msg;
//		if (!mGp.currentLogFilePath.equals("")) hli.isLogFileAvailable=true;
//		hli.sync_log_file_path=mGp.currentLogFilePath;
        hli.sync_result_file_path = mSyncHistroryResultFilepath;
//		Log.v("","before");
//		Log.v("","after");
        SyncTaskItem pfli = SyncTaskUtil.getSyncTaskByName(mGp.syncTaskList, sti.getSyncTaskName());
        if (pfli != null) {
            pfli.setLastSyncTime(date + " " + time);
            pfli.setLastSyncResult(status);
        }
        if (mGp.syncHistoryAdapter != null) {
            mGp.uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mGp.syncHistoryList.add(0, hli);
                    mGp.syncHistoryAdapter.notifyDataSetChanged();
                    mStwa.util.saveHistoryList(mGp.syncHistoryList);
                }
            });
        } else {
            mGp.syncHistoryList.add(0, hli);
            mStwa.util.saveHistoryList(mGp.syncHistoryList);
        }
    }

    static final public String calTransferRate(long tb, long tt) {
        String tfs = null;
        String units = null;
        BigDecimal bd_tr;
//		Log.v("","byte="+tb+", time="+tt);

        if (tb == 0) return "0 Bytes/sec";

        if (tt == 0) return "N/A"; // elapsed time 0 msec

        BigDecimal dfs = new BigDecimal(tb * 1.000000);
        BigDecimal dft1 = new BigDecimal(tt * 1.000);
        BigDecimal dft2 = new BigDecimal(1000.000);
        BigDecimal dft = new BigDecimal("0.000000");
        dft = dft1.divide(dft2); // convert elapsed time from msec to sec
        BigDecimal bd_tr1 = dfs.divide(dft, 6, BigDecimal.ROUND_HALF_UP); // transfer speed in bytes /sec

        if (bd_tr1.compareTo(new BigDecimal(1048576)) >= 0) {//  MB/sec (transfer speed >= 1024 * 1024 bytes)
            units = " MBytes/sec";
            BigDecimal bd_tr2 = new BigDecimal(1024 * 1024 * 1.000000);
            bd_tr = bd_tr1.divide(bd_tr2, 2, BigDecimal.ROUND_HALF_UP);
        } else if (bd_tr1.compareTo(new BigDecimal(1024)) >= 0) { // KB/sec (transfer speed >= 1024 bytes)
            units = " KBytes/sec";
            BigDecimal bd_tr2 = new BigDecimal(1024 * 1.000000);
            bd_tr = bd_tr1.divide(bd_tr2, 1, BigDecimal.ROUND_HALF_UP);
        } else { // Bytes/sec
            units = " Bytes/sec";
            bd_tr = bd_tr1.setScale(0, RoundingMode.HALF_UP);
        }

        // proper formatting and grouping
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        formatSymbols.setDecimalSeparator('.');
        formatSymbols.setGroupingSeparator(' ');
        DecimalFormat formatter = new DecimalFormat("###,###.###", formatSymbols);
        tfs = formatter.format(bd_tr) + units;

        return tfs;
    }

}