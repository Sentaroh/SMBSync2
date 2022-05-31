package com.sentaroh.android.SMBSync2;

/*
The MIT License (MIT)
Copyright (c) 2011 Sentaroh

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

import com.sentaroh.jcifs.JcifsAuth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Created by sentaroh on 2018/03/21.
 */
class SyncTaskItem implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;
    private String syncTaskName = "";
    private String syncTasｋGroup = "";
    private boolean syncTaskEnabled = true;
    private boolean isChecked = false;

    private boolean syncOptionSyncTestMode = false;

    private int syncTaskPosition = 0;

    public final static String SYNC_TASK_TYPE_MIRROR = "M"; // index 0 (Indexes position from SYNC_TASK_TYPE_LIST not used for now)
    public final static String SYNC_TASK_TYPE_COPY = "C"; // index 1
    public final static String SYNC_TASK_TYPE_MOVE = "X"; // index 2
    public final static String SYNC_TASK_TYPE_SYNC = "S"; // index 3
    public final static String SYNC_TASK_TYPE_ARCHIVE = "A"; // index 4
    public final static String SYNC_TASK_TYPE_DEFAULT = SYNC_TASK_TYPE_COPY;
    public final static String SYNC_TASK_TYPE_DEFAULT_DESCRIPTION = "COPY";
    public final static String[] SYNC_TASK_TYPE_LIST=new String[]{SYNC_TASK_TYPE_MIRROR, SYNC_TASK_TYPE_COPY, SYNC_TASK_TYPE_MOVE, SYNC_TASK_TYPE_SYNC, SYNC_TASK_TYPE_ARCHIVE};
    private String syncTaskType = SYNC_TASK_TYPE_DEFAULT;

    public final static String SYNC_TASK_TWO_WAY_OPTION_ASK_USER = "0";
    public final static String SYNC_TASK_TWO_WAY_OPTION_COPY_NEWER = "1";
    public final static String SYNC_TASK_TWO_WAY_OPTION_COPY_OLDER = "2";
    public final static String SYNC_TASK_TWO_WAY_OPTION_SKIP_SYNC_FILE = "3";
    public final static String SYNC_TASK_TWO_WAY_OPTION_DEFAULT = SYNC_TASK_TWO_WAY_OPTION_ASK_USER;
    public final static String[] SYNC_TASK_TWO_WAY_OPTION_LIST=new String[]{
            SYNC_TASK_TWO_WAY_OPTION_ASK_USER, SYNC_TASK_TWO_WAY_OPTION_COPY_NEWER, SYNC_TASK_TWO_WAY_OPTION_COPY_OLDER, SYNC_TASK_TWO_WAY_OPTION_SKIP_SYNC_FILE};
    public final static String SYNC_TASK_TWO_WAY_CONFLICT_FILE_SUFFIX=".smbsync2_conflict";
    private String syncTwoWayConflictOption =SYNC_TASK_TWO_WAY_OPTION_DEFAULT;
    private boolean syncTwoWayConflictKeepConflictFile = false;

    public final static String SYNC_FOLDER_TYPE_INTERNAL = "INT";
    public final static String SYNC_FOLDER_TYPE_SDCARD = "EXT";
    public final static String SYNC_FOLDER_TYPE_USB="USB";
    public final static String SYNC_FOLDER_TYPE_SMB = "SMB";
    public final static String SYNC_FOLDER_TYPE_ZIP = "ZIP";
    public final static String SYNC_FOLDER_TYPE_DEFAULT = SYNC_FOLDER_TYPE_INTERNAL;
    public final static String[] SYNC_FOLDER_TYPE_LIST=new String[]{SYNC_FOLDER_TYPE_INTERNAL, SYNC_FOLDER_TYPE_SDCARD, SYNC_FOLDER_TYPE_USB, SYNC_FOLDER_TYPE_ZIP, SYNC_FOLDER_TYPE_SMB};
    private String syncTaskMasterFolderType = SYNC_FOLDER_TYPE_DEFAULT;

    private String syncTaskMasterFolderDirName = "";
    private String syncTaskMasterLocalMountPoint = "";
    private String syncTaskMasterFolderSmbShareName = "";
    private String syncTaskMasterFolderSmbIpAddress = "";
    private String syncTaskMasterFolderSmbHostName = "";
    private String syncTaskMasterFolderSmbPortNumber = SYNC_FOLDER_SMB_PORT_DEFAULT;
    private String syncTaskMasterFolderSmbUserName = "";
    private String syncTaskMasterFolderSmbPassword = "";
    private String syncTaskMasterFolderSmbDomain = "";
    public final static String SYNC_FOLDER_SMB_PORT_DEFAULT = "";//default is not specified
    public final static String SYNC_FOLDER_SMB_PORT_DEFAULT_DESCRIPTION = "Autodetect SMB port";
    public final static String SYNC_FOLDER_SMB_PROTOCOL_SYSTEM = "0";
//    public final static String SYNC_FOLDER_SMB_PROTOCOL_SMB1 = "1";
//    public final static String SYNC_FOLDER_SMB_PROTOCOL_SMB201 = "2";
//    public final static String SYNC_FOLDER_SMB_PROTOCOL_SMB211 = "3";
//    public final static String SYNC_FOLDER_SMB_PROTOCOL_SMB212 = "4";
    public final static String SYNC_FOLDER_SMB_PROTOCOL_SMB1 = String.valueOf(JcifsAuth.JCIFS_FILE_SMB1); //1
    public final static String SYNC_FOLDER_SMB_PROTOCOL_SMB201 = String.valueOf(JcifsAuth.JCIFS_FILE_SMB201); //2
    public final static String SYNC_FOLDER_SMB_PROTOCOL_SMB211 = String.valueOf(JcifsAuth.JCIFS_FILE_SMB211); //3
    public final static String SYNC_FOLDER_SMB_PROTOCOL_SMB212 = String.valueOf(JcifsAuth.JCIFS_FILE_SMB212); //4
    public final static String SYNC_FOLDER_SMB_PROTOCOL_SMB214 = String.valueOf(JcifsAuth.JCIFS_FILE_SMB214); //5
    public final static String SYNC_FOLDER_SMB_PROTOCOL_SMB_LATEST = String.valueOf(JcifsAuth.JCIFS_FILE_SMB_LATEST); //6
    public final static String SYNC_FOLDER_SMB_PROTOCOL_DEFAULT = SYNC_FOLDER_SMB_PROTOCOL_SMB_LATEST;
    public final static String[] SYNC_FOLDER_SMB_PROTOCOL_LIST=new String[]{
            SYNC_FOLDER_SMB_PROTOCOL_SMB1, SYNC_FOLDER_SMB_PROTOCOL_SMB201, SYNC_FOLDER_SMB_PROTOCOL_SMB211,
            SYNC_FOLDER_SMB_PROTOCOL_SMB212, SYNC_FOLDER_SMB_PROTOCOL_SMB214, SYNC_FOLDER_SMB_PROTOCOL_SMB_LATEST};

    public final static String SYNC_FOLDER_SMB_PROTOCOL_SMB_LATEST_VERSION = "2.17"; // used from SMB protocol selection drop menu and for SYNC_FOLDER_SMB_PROTOCOL_DEFAULT_DESCRIPTION
    public final static String SYNC_FOLDER_SMB_PROTOCOL_DEFAULT_DESCRIPTION = "SMB" + SYNC_FOLDER_SMB_PROTOCOL_SMB_LATEST_VERSION; //used to display error message when importing a synctask with invalid SMB protocol
    private String syncTaskMasterFolderSmbProtocol = SYNC_FOLDER_SMB_PROTOCOL_DEFAULT;
    private boolean syncTaskMasterFolderSmbIpcSigningEnforced = true;

    private boolean syncTaskMasterFolderSmbUseSmb2Negotiation = false;

    private String syncTaskMasterFolderRemovableStorageID = "";
//	private boolean syncTaskMasterFolderUseInternalUsbFolder=false;

    private String syncTaskTargetFolderType = SYNC_FOLDER_TYPE_INTERNAL;
    private String syncTaskTargetFolderDirName = "";
    private String syncTaskTargetLocalMountPoint = "";
    private String syncTaskTargetFolderSmbShareName = "";
    private String syncTaskTargetFolderSmbIpAddress = "";
    private String syncTaskTargetFolderSmbHostName = "";
    private String syncTaskTargetFolderSmbPortNumber = "";
    private String syncTaskTargetFolderSmbUserName = "";
    private String syncTaskTargetFolderSmbPassword = "";
    private String syncTaskTargetFolderSmbDomain = "";
    private String syncTaskTargetFolderSmbProtocol = SYNC_FOLDER_SMB_PROTOCOL_DEFAULT;
    private boolean syncTaskTargetFolderSmbIpcSigningEnforced = true;

    private boolean syncTaskTargetFolderSmbUseSmb2Negotiation = false;

    private String syncTaskTargetFolderRemovableStorageID = "";

    private String syncTaskTargetZipFileName = "";
    public final static String ZIP_OPTION_COMP_LEVEL_FASTEST = "FASTEST"; //0
    public final static String ZIP_OPTION_COMP_LEVEL_FAST = "FAST"; //1
    public final static String ZIP_OPTION_COMP_LEVEL_NORMAL = "NORMAL"; //2
    public final static String ZIP_OPTION_COMP_LEVEL_MAXIMUM = "MAXIMUM"; //3
    public final static String ZIP_OPTION_COMP_LEVEL_ULTRA = "ULTRA"; //4
    public final static String ZIP_OPTION_COMP_LEVEL_DEFAULT = ZIP_OPTION_COMP_LEVEL_NORMAL;
    public final static String[] ZIP_OPTION_COMP_LEVEL_LIST=new String[]{ZIP_OPTION_COMP_LEVEL_FASTEST, ZIP_OPTION_COMP_LEVEL_FAST, ZIP_OPTION_COMP_LEVEL_NORMAL, ZIP_OPTION_COMP_LEVEL_MAXIMUM, ZIP_OPTION_COMP_LEVEL_ULTRA};
    public final static int ZIP_OPTION_COMP_LEVEL_DEFAULT_ITEM_INDEX = 2;
    public final static String ZIP_OPTION_COMP_METHOD_STORE = "STORE";
    public final static String ZIP_OPTION_COMP_METHOD_DEFLATE = "DEFLATE";
    public final static String ZIP_OPTION_COMP_METHOD_DEFAULT = ZIP_OPTION_COMP_METHOD_DEFLATE;
    public final static String[] ZIP_OPTION_COMP_METGOD_LIST=new String[]{ZIP_OPTION_COMP_METHOD_STORE, ZIP_OPTION_COMP_METHOD_DEFLATE};
    public final static String ZIP_OPTION_ENCRYPT_NONE = "NONE"; //0
    public final static String ZIP_OPTION_ENCRYPT_STANDARD = "STANDARD"; //1
    public final static String ZIP_OPTION_ENCRYPT_AES128 = "AES128"; //2
    public final static String ZIP_OPTION_ENCRYPT_AES256 = "AES256"; //3
    public final static String ZIP_OPTION_ENCRYPT_DEFAULT = ZIP_OPTION_ENCRYPT_NONE;
    public final static int ZIP_OPTION_ENCRYPT_DEFAULT_ITEM_INDEX = 0;
    public final static String[] ZIP_OPTION_ENCRYPT_LIST=new String[]{ZIP_OPTION_ENCRYPT_NONE, ZIP_OPTION_ENCRYPT_STANDARD, ZIP_OPTION_ENCRYPT_AES128, ZIP_OPTION_ENCRYPT_AES256};
    private String syncTaskTargetZipCompOptionCompLevel = ZIP_OPTION_COMP_LEVEL_DEFAULT;
    private String syncTaskTargetZipCompOptionCompMethod = ZIP_OPTION_COMP_METHOD_DEFAULT;
    private String syncTaskTargetZipCompOptionEncrypt = ZIP_OPTION_ENCRYPT_DEFAULT;
    private String syncTaskTargetZipCompOptionPassword = "";
    private String syncTaskTargetZipCompOptionEncoding = "UTF-8";
    //	private boolean syncTaskTargetZipUseInternalUsbFolder=false;
    private boolean syncTaskTargetZipUseSdcard = false;
    private boolean syncTaskTargetZipUseUsb = false;
//	private boolean syncTaskTargetFolderUseInternalUsbFolder=false;

    private boolean syncFileTypeAudio = false;
    private boolean syncFileTypeImage = false;
    private boolean syncFileTypeVideo = false;

    private ArrayList<String> syncFileFilter = new ArrayList<String>();
    private ArrayList<String> syncDirFilter = new ArrayList<String>();

    private boolean syncOptionRootDirFileToBeProcessed = true;
    private boolean syncOptionProcessOverrideCopyMove = true;
    private boolean syncOptionConfirmOverrideDelete = true;
    private boolean syncOptionForceLastModifiedUseSmbsync = false;
    private boolean syncOptionNotUsedLastModifiedForRemote = false;

    public final static String NETWORK_ERROR_RETRY_COUNT="3";
    public final static String NETWORK_ERROR_RETRY_COUNT_DEFAULT=NETWORK_ERROR_RETRY_COUNT;
    public final static String[] NETWORK_ERROR_RETRY_COUNT_LIST=new String[]{NETWORK_ERROR_RETRY_COUNT_DEFAULT};
    private String syncOptionRetryCount = NETWORK_ERROR_RETRY_COUNT_DEFAULT;
    private boolean syncOptionSyncEmptyDir = true;
    private boolean syncTaskTargetUseTakenDateTimeForDirectoryNameKeyword = false;
    private boolean syncOptionSyncHiddenFile = true;
    private boolean syncOptionSyncHiddenDir = true;
    private boolean syncOptionSyncSubDir = true;

    private boolean syncOptionUseSmallIoBuffer = false;

    private boolean syncOptionDeterminChangedFileBySize = true;
    private boolean syncOptionDeterminChangedFileByTime = true;
    public final static int SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_0=0;
    public final static int SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_1=1;
    public final static int SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_2=3;//For FAY32/VFAT/FAT
    public final static int SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_3=5;
    public final static int SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_4=10;//For compatibility
    public final static int SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_DEFAULT=SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_2;//For compatibility
    public final static int SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_LIST_DEFAULT_ITEM_INDEX = 2;//For compatibility
    public final static int[] SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_LIST =new int[]{
            SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_0, SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_1, SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_2,
            SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_3, SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_4};
    private int syncOptionDeterminChangedFileByTimeValue = SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_DEFAULT;//Seconds

    private boolean syncOptionDoNotUseRenameWhenSmbFileWrite = false;

    private boolean syncOptionUseExtendedDirectoryFilter1 = false;

    private boolean syncOptionDeleteFirstWhenMirror = false;
    private boolean SyncOptionEnsureTargetIsExactMirror = false;

    public final static String SYNC_WIFI_STATUS_WIFI_OFF = "0"; // list index 0
    public final static String SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP = "1"; // list index 1
    public final static String SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_AP = "2"; // list index 2 (deprecated)
    public final static String SYNC_WIFI_STATUS_WIFI_CONNECT_PRIVATE_ADDR = "3"; // list index 3
    public final static String SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_ADDR = "4"; // list index 4
    public final static String SYNC_WIFI_STATUS_WIFI_DEFAULT = SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP;
    public final static String SYNC_WIFI_STATUS_WIFI_DEFAULT_DESCRIPTION = "Conn any AP";
    public final static String[] SYNC_WIFI_STATUS_WIFI_LIST = new String[]{SYNC_WIFI_STATUS_WIFI_OFF , SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP,
            SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_AP, SYNC_WIFI_STATUS_WIFI_CONNECT_PRIVATE_ADDR, SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_ADDR};
    private String syncOptionWifiStatus = SYNC_WIFI_STATUS_WIFI_DEFAULT;
    private boolean syncTaskSkipIfConnectAnotherWifiSsid = false;

    private boolean syncOptionSyncOnlyCharging = false;

    private boolean syncOptionSyncAllowGlobalIpAddress=false;

    static public final int SYNC_STATUS_SUCCESS = SyncHistoryItem.SYNC_STATUS_SUCCESS;
    static public final int SYNC_STATUS_CANCEL = SyncHistoryItem.SYNC_STATUS_CANCEL;
    static public final int SYNC_STATUS_ERROR = SyncHistoryItem.SYNC_STATUS_ERROR;
    static public final int SYNC_STATUS_WARNING = SyncHistoryItem.SYNC_STATUS_WARNING;
    static public final int SYNC_STATUS_DEFAULT = SYNC_STATUS_SUCCESS;
    public final static String SYNC_STATUS_DEFAULT_DESCRIPTION = "Success";
    public final static int[] SYNC_STATUS_LIST = new int[]{SYNC_STATUS_SUCCESS, SYNC_STATUS_CANCEL, SYNC_STATUS_ERROR, SYNC_STATUS_WARNING};
    private String syncLastSyncTime = "";
    private int syncLastSyncResult = SYNC_STATUS_DEFAULT;

    //Not save variables
    private boolean syncTaskIsRunning = false;

    public SyncTaskItem(String stn, boolean pfa, boolean ic) {
        syncTaskName = stn;
        syncTaskEnabled = pfa;
        isChecked = ic;
        initOffsetOfDst();
    }

    public SyncTaskItem() {
        initOffsetOfDst();
    }

    public void initOffsetOfDst() {
        if (TimeZone.getDefault().useDaylightTime()) setSyncOptionOffsetOfDst(TimeZone.getDefault().getDSTSavings()/(60*1000));
        else setSyncOptionOffsetOfDst(SYNC_OPTION_OFFSET_OF_DST_DEFAULT);
    }

    public String getSyncTaskName() {return syncTaskName;}

    public String getSyncTaskType() {return syncTaskType;}

    public void setSyncTaskType(String p) {syncTaskType = p;}

    public void setSyncTaskAuto(boolean p) {syncTaskEnabled = p;}

    public boolean isSyncTaskAuto() {return syncTaskEnabled;}

    public String getSyncTaskGroup() {return syncTasｋGroup;}

    public void setSyncTaskGroup(String grp_name) {syncTasｋGroup = grp_name;}

    public void setSyncTwoWayConflictFileRule(String p) {syncTwoWayConflictOption = p;}
    public String getSyncTwoWayConflictFileRule() {return syncTwoWayConflictOption;}

    public void setSyncTwoWayKeepConflictFile(boolean keep_file) {syncTwoWayConflictKeepConflictFile=keep_file;}
    public boolean isSyncTwoWayKeepConflictFile() {return syncTwoWayConflictKeepConflictFile;}

    public String getMasterSmbUserName() {return syncTaskMasterFolderSmbUserName;}

    public String getMasterSmbPassword() {return syncTaskMasterFolderSmbPassword;}

    public String getMasterSmbShareName() {return syncTaskMasterFolderSmbShareName;}

    public String getMasterDirectoryName() {return syncTaskMasterFolderDirName;}

    public String getMasterLocalMountPoint() {return syncTaskMasterLocalMountPoint;}

    public String getMasterSmbAddr() {return syncTaskMasterFolderSmbIpAddress;}

    public String getMasterSmbPort() {return syncTaskMasterFolderSmbPortNumber;}

    public String getMasterSmbHostName() {return syncTaskMasterFolderSmbHostName;}

    public String getMasterSmbDomain() {return syncTaskMasterFolderSmbDomain;}

    public String getMasterSmbProtocol() {return syncTaskMasterFolderSmbProtocol;}

    public void setMasterSmbProtocol(String proto) {syncTaskMasterFolderSmbProtocol=proto;}

    public String getMasterRemovableStorageID() {return syncTaskMasterFolderRemovableStorageID;}

    public boolean isMasterSmbIpcSigningEnforced() {return syncTaskMasterFolderSmbIpcSigningEnforced;}
    public void setMasterSmbIpcSigningEnforced(boolean enforced) {syncTaskMasterFolderSmbIpcSigningEnforced=enforced;}

    public boolean isMasterSmbUseSmb2Negotiation() {return syncTaskMasterFolderSmbUseSmb2Negotiation;}
    public void setMasterSmbUseSmb2Negotiation(boolean smb2) {syncTaskMasterFolderSmbUseSmb2Negotiation=smb2;}

    public String getMasterFolderType() {return syncTaskMasterFolderType;}

    public String getTargetSmbUserName() {return syncTaskTargetFolderSmbUserName;}

    public String getTargetSmbPassword() {return syncTaskTargetFolderSmbPassword;}

    public String getTargetSmbShareName() {return syncTaskTargetFolderSmbShareName;}

    public String getTargetDirectoryName() {return syncTaskTargetFolderDirName;}

    public String getTargetLocalMountPoint() {return syncTaskTargetLocalMountPoint;}

    public String getTargetSmbAddr() {return syncTaskTargetFolderSmbIpAddress;}

    public String getTargetSmbPort() {return syncTaskTargetFolderSmbPortNumber;}

    public String getTargetSmbHostName() {return syncTaskTargetFolderSmbHostName;}

    public String getTargetSmbDomain() {return syncTaskTargetFolderSmbDomain;}

    public String getTargetSmbProtocol() {return syncTaskTargetFolderSmbProtocol;}

    public void setTargetSmbProtocol(String proto) {syncTaskTargetFolderSmbProtocol=proto;}

    public boolean isTargetSmbIpcSigningEnforced() {return syncTaskTargetFolderSmbIpcSigningEnforced;}
    public void setTargetSmbIpcSigningEnforced(boolean enforced) {syncTaskTargetFolderSmbIpcSigningEnforced=enforced;}

    public boolean isTargetSmbUseSmb2Negotiation() {return syncTaskTargetFolderSmbUseSmb2Negotiation;}
    public void setTargetSmbUseSmb2Negotiation(boolean smb2) {syncTaskTargetFolderSmbUseSmb2Negotiation=smb2;}

    public String getTargetRemovableStorageID() {return syncTaskTargetFolderRemovableStorageID;}

    public String getTargetZipOutputFileName() {return syncTaskTargetZipFileName;}

    public String getTargetFolderType() {return syncTaskTargetFolderType;}

    public String getTargetZipCompressionLevel() {return syncTaskTargetZipCompOptionCompLevel;}
    public void setTargetZipCompressionLevel(String p) {syncTaskTargetZipCompOptionCompLevel = p;}

    public String getTargetZipCompressionMethod() {return syncTaskTargetZipCompOptionCompMethod;}
    public void setTargetZipCompressionMethod(String p) {syncTaskTargetZipCompOptionCompMethod = p;}

    public String getTargetZipEncryptMethod() {return syncTaskTargetZipCompOptionEncrypt;}
    public void setTargetZipEncryptMethod(String p) {syncTaskTargetZipCompOptionEncrypt = p;}

    public String getTargetZipPassword() {return syncTaskTargetZipCompOptionPassword;}
    public void setTargetZipPassword(String p) {syncTaskTargetZipCompOptionPassword = p;}

    public boolean isTargetZipUseExternalSdcard() {return syncTaskTargetZipUseSdcard;}
    public void setTargetZipUseExternalSdcard(boolean p) {syncTaskTargetZipUseSdcard = p;}

    public boolean isTargetZipUseUsb() {return syncTaskTargetZipUseUsb;}
    public void setTargetZipUseUsb(boolean p) {syncTaskTargetZipUseUsb = p;}

    public String getTargetZipFileNameEncoding() {return syncTaskTargetZipCompOptionEncoding;}

    public void setTargetZipFileNameEncoding(String p) {syncTaskTargetZipCompOptionEncoding = p;}

    public final static String PICTURE_ARCHIVE_RENAME_KEYWORD_DATE = "%DATE%";
    public final static String PICTURE_ARCHIVE_RENAME_KEYWORD_TIME = "%TIME%";
    public final static String PICTURE_ARCHIVE_RENAME_KEYWORD_YYYYMMDD = "%YYYYMMDD%";
    public final static String PICTURE_ARCHIVE_RENAME_KEYWORD_HHMMSS = "%HHMMSS%";
    public final static String PICTURE_ARCHIVE_RENAME_KEYWORD_ORIGINAL_NAME = "%ORIGINAL-NAME%";
    public final static String PICTURE_ARCHIVE_RENAME_KEYWORD_YEAR = "%YEAR%";
    public final static String PICTURE_ARCHIVE_RENAME_KEYWORD_MONTH = "%MONTH%";
    public final static String PICTURE_ARCHIVE_RENAME_KEYWORD_DAY = "%DAY%";
    public final static String[] PICTURE_ARCHIVE_RENAME_KEYWORD_LIST = new String[]{PICTURE_ARCHIVE_RENAME_KEYWORD_DATE,
            PICTURE_ARCHIVE_RENAME_KEYWORD_TIME, PICTURE_ARCHIVE_RENAME_KEYWORD_ORIGINAL_NAME, PICTURE_ARCHIVE_RENAME_KEYWORD_YEAR,
            PICTURE_ARCHIVE_RENAME_KEYWORD_MONTH, PICTURE_ARCHIVE_RENAME_KEYWORD_DAY};
    private String syncTaskArchiveRenameFileTemplate = "DSC_%DATE%";
    private String syncTaskArchiveSaveDirectoryTemplate = PICTURE_ARCHIVE_RENAME_KEYWORD_YEAR;

    private boolean syncTaskArchiveRenameWhenArchive = true;

    public final static int PICTURE_ARCHIVE_RETAIN_FOR_A_0_DAYS = 0;
    public final static int PICTURE_ARCHIVE_RETAIN_FOR_A_7_DAYS = 1;
    public final static int PICTURE_ARCHIVE_RETAIN_FOR_A_30_DAYS = 2;
    public final static int PICTURE_ARCHIVE_RETAIN_FOR_A_60_DAYS = 3;
    public final static int PICTURE_ARCHIVE_RETAIN_FOR_A_90_DAYS = 4;
    public final static int PICTURE_ARCHIVE_RETAIN_FOR_A_180_DAYS = 5;
    public final static int PICTURE_ARCHIVE_RETAIN_FOR_A_1_YEARS = 6;
    public final static int PICTURE_ARCHIVE_RETAIN_FOR_A_DEFAULT = PICTURE_ARCHIVE_RETAIN_FOR_A_180_DAYS;
    public final static String PICTURE_ARCHIVE_RETAIN_FOR_A_DEFAULT_DESCRIPTION = "180 Days";
    public final static int[] PICTURE_ARCHIVE_RETAIN_FOR_A_LIST = new int[]{PICTURE_ARCHIVE_RETAIN_FOR_A_0_DAYS,
            PICTURE_ARCHIVE_RETAIN_FOR_A_7_DAYS, PICTURE_ARCHIVE_RETAIN_FOR_A_30_DAYS, PICTURE_ARCHIVE_RETAIN_FOR_A_60_DAYS,
            PICTURE_ARCHIVE_RETAIN_FOR_A_90_DAYS, PICTURE_ARCHIVE_RETAIN_FOR_A_180_DAYS, PICTURE_ARCHIVE_RETAIN_FOR_A_1_YEARS};
    private int syncTaskArchiveRetentionPeriod = PICTURE_ARCHIVE_RETAIN_FOR_A_DEFAULT;

    private boolean syncTaskArchiveCreateDirectory = false;

    private boolean syncTaskArchiveEnable = true;

    public final static int PICTURE_ARCHIVE_SUFFIX_DIGIT_NOT_USED = 0;
    public final static int PICTURE_ARCHIVE_SUFFIX_DIGIT_2_DIGIT = 2;
    public final static int PICTURE_ARCHIVE_SUFFIX_DIGIT_3_DIGIT = 3;
    public final static int PICTURE_ARCHIVE_SUFFIX_DIGIT_4_DIGIT = 4;
    public final static int PICTURE_ARCHIVE_SUFFIX_DIGIT_5_DIGIT = 5;
    public final static int PICTURE_ARCHIVE_SUFFIX_DIGIT_6_DIGIT = 6;
    public final static int PICTURE_ARCHIVE_SUFFIX_DIGIT_DEFAULT = PICTURE_ARCHIVE_SUFFIX_DIGIT_4_DIGIT ;
    public final static int[] PICTURE_ARCHIVE_SUFFIX_DIGIT_LIST = new int[]{PICTURE_ARCHIVE_SUFFIX_DIGIT_NOT_USED,
            PICTURE_ARCHIVE_SUFFIX_DIGIT_2_DIGIT, PICTURE_ARCHIVE_SUFFIX_DIGIT_3_DIGIT,
            PICTURE_ARCHIVE_SUFFIX_DIGIT_4_DIGIT, PICTURE_ARCHIVE_SUFFIX_DIGIT_5_DIGIT, PICTURE_ARCHIVE_SUFFIX_DIGIT_6_DIGIT};
    private String syncTaskArchiveSuffixDigit = String.valueOf(PICTURE_ARCHIVE_SUFFIX_DIGIT_DEFAULT);

    public String getArchiveSuffixOption() {return syncTaskArchiveSuffixDigit;}
    public void setArchiveSuffixOption(String digit) {syncTaskArchiveSuffixDigit =digit;}

    public String getArchiveRenameFileTemplate() {return syncTaskArchiveRenameFileTemplate;}
    public void setArchiveRenameFileTemplate(String template) {syncTaskArchiveRenameFileTemplate =template;}

    public String getArchiveCreateDirectoryTemplate() {return syncTaskArchiveSaveDirectoryTemplate;}
    public void setArchiveCreateDirectoryTemplate(String template) {syncTaskArchiveSaveDirectoryTemplate=template;}

    public boolean isArchiveUseRename() {return syncTaskArchiveRenameWhenArchive;}
    public void setArchiveUseRename(boolean rename) {syncTaskArchiveRenameWhenArchive =rename;}
    public int getArchiveRetentionPeriod() {return syncTaskArchiveRetentionPeriod;}
    public void setArchiveRetentionPeriod(int period) {syncTaskArchiveRetentionPeriod =period;}
    public boolean isArchiveCreateDirectory() {return syncTaskArchiveCreateDirectory;}
    public void setArchiveCreateDirectory(boolean cretae) {syncTaskArchiveCreateDirectory =cretae;}
    public boolean isArchiveEnabled() {return syncTaskArchiveEnable;}// Not used
    public void setArchiveEnabled(boolean enabled) {syncTaskArchiveEnable =enabled;}// Not used

    private boolean syncOptionDeterminChangedFileSizeGreaterThanTargetFile = false;
    public boolean isSyncDifferentFileSizeGreaterThanTagetFile() {return syncOptionDeterminChangedFileSizeGreaterThanTargetFile;}
    public void setSyncDifferentFileSizeGreaterThanTagetFile(boolean p) {syncOptionDeterminChangedFileSizeGreaterThanTargetFile = p;}

    public ArrayList<String> getFileFilter() {return syncFileFilter;}

    public ArrayList<String> getDirFilter() {return syncDirFilter;}

    public boolean isSyncProcessRootDirFile() {return syncOptionRootDirFileToBeProcessed;}

    public void setSyncProcessRootDirFile(boolean p) {syncOptionRootDirFileToBeProcessed = p;}

    public boolean isSyncOverrideCopyMoveFile() {return syncOptionProcessOverrideCopyMove;}

    public void setSyncOverrideCopyMoveFile(boolean p) {syncOptionProcessOverrideCopyMove = p;}

    public boolean isSyncConfirmOverrideOrDelete() {return syncOptionConfirmOverrideDelete;}

    public void setSyncConfirmOverrideOrDelete(boolean p) {syncOptionConfirmOverrideDelete = p;}

    public boolean isSyncDetectLastModifiedBySmbsync() {return syncOptionForceLastModifiedUseSmbsync;}

    public void setSyncDetectLastModidiedBySmbsync(boolean p) {syncOptionForceLastModifiedUseSmbsync = p;}

    public boolean isChecked() {return isChecked;}

    public void setChecked(boolean p) {isChecked = p;}

    public boolean isSyncDoNotResetFileLastModified() {return syncOptionNotUsedLastModifiedForRemote;}

    public void setSyncDoNotResetFileLastModified(boolean p) {syncOptionNotUsedLastModifiedForRemote = p;}

    public void setSyncTaskName(String p) {syncTaskName = p;}

    public void setMasterSmbUserName(String p) {syncTaskMasterFolderSmbUserName = p;}

    public void setMasterSmbPassword(String p) {syncTaskMasterFolderSmbPassword = p;}

    public void setMasterSmbShareName(String p) {syncTaskMasterFolderSmbShareName = p;}

    public void setMasterDirectoryName(String p) {syncTaskMasterFolderDirName = p;}

    public void setMasterLocalMountPoint(String mp) {syncTaskMasterLocalMountPoint = mp;}

    public void setMasterSmbAddr(String p) {syncTaskMasterFolderSmbIpAddress = p;}
    public void setMasterSmbPort(String p) {syncTaskMasterFolderSmbPortNumber = p;}

    public void setMasterSmbHostName(String p) {syncTaskMasterFolderSmbHostName = p;}
    public void setMasterSmbDomain(String p) {syncTaskMasterFolderSmbDomain = p;}

    public void setMasterRemovableStorageID(String p) {syncTaskMasterFolderRemovableStorageID = p;}
    public void setMasterFolderType(String p) {syncTaskMasterFolderType = p;}

    public void setTargetZipOutputFileName(String p) {syncTaskTargetZipFileName = p;}
    public void setTargetSmbUserName(String p) {syncTaskTargetFolderSmbUserName = p;}

    public void setTargetSmbPassword(String p) {syncTaskTargetFolderSmbPassword = p;}
    public void setTargetSmbShareName(String p) {syncTaskTargetFolderSmbShareName = p;}

    public void setTargetDirectoryName(String p) {syncTaskTargetFolderDirName = p;}
    public void setTargetLocalMountPoint(String mp) {syncTaskTargetLocalMountPoint = mp;}

    public void setTargetSmbAddr(String p) {syncTaskTargetFolderSmbIpAddress = p;}
    public void setTargetSmbPort(String p) {syncTaskTargetFolderSmbPortNumber = p;}

    public void setTargetSmbHostname(String p) {syncTaskTargetFolderSmbHostName = p;}
    public void setTargetSmbDomain(String p) {syncTaskTargetFolderSmbDomain = p;}

    public void setTargetRemovableStorageID(String p) {syncTaskTargetFolderRemovableStorageID = p;}
    public void setTargetFolderType(String p) {syncTaskTargetFolderType = p;}

    public boolean isTargetUseTakenDateTimeToDirectoryNameKeyword() {return syncTaskTargetUseTakenDateTimeForDirectoryNameKeyword;}
    public void setTargetUseTakenDateTimeToDirectoryNameKeyword(boolean p) {syncTaskTargetUseTakenDateTimeForDirectoryNameKeyword = p;}

    public boolean isSyncFileTypeAudio() {return syncFileTypeAudio;}
    public void setSyncFileTypeAudio(boolean p) {syncFileTypeAudio = p;}

    public boolean isSyncFileTypeImage() {return syncFileTypeImage;}
    public void setSyncFileTypeImage(boolean p) {syncFileTypeImage = p;}

    public boolean isSyncFileTypeVideo() {return syncFileTypeVideo;}
    public void setSyncFileTypeVideo(boolean p) {syncFileTypeVideo = p;}

    public void setFileFilter(ArrayList<String> p) {syncFileFilter = p;}
    public void setDirFilter(ArrayList<String> p) {syncDirFilter = p;}

    private boolean syncSyncOptionIgnoreFileSize0ByteFile = false;
    public boolean isSyncOptionIgnoreFileSize0ByteFile() {return syncSyncOptionIgnoreFileSize0ByteFile;}
    public void setSyncOptionIgnoreFileSize0ByteFile(boolean p) {syncSyncOptionIgnoreFileSize0ByteFile = p;}

    private int syncSyncOptionMaxFileNameLength = 255;
    public int getSyncOptionMaxFileNameLength() {return syncSyncOptionMaxFileNameLength;}
    public void setSyncOptionMaxFileNameLength(int p) {
        syncSyncOptionMaxFileNameLength = p;}

    static final public String FILTER_FILE_SIZE_VALUE_DEFAULT= "1";
    private String syncFilterFileSizeValue = FILTER_FILE_SIZE_VALUE_DEFAULT;
    public String getSyncFilterFileSizeValue() {return syncFilterFileSizeValue;}
    public void setSyncFilterFileSizeValue(String p) {syncFilterFileSizeValue = p;}

    public static final String FILTER_FILE_SIZE_TYPE_NONE="NONE";
    public static final String FILTER_FILE_SIZE_TYPE_GREATER_THAN ="GT";
    public static final String FILTER_FILE_SIZE_TYPE_LESS_THAN ="LT";
    public static final String FILTER_FILE_SIZE_TYPE_DEFAULT=FILTER_FILE_SIZE_TYPE_NONE;
    private String syncFilterFileSizeType = FILTER_FILE_SIZE_TYPE_DEFAULT;
    public String getSyncFilterFileSizeType() {return syncFilterFileSizeType;}
    public void setSyncFilterFileSizeType(String p) {syncFilterFileSizeType = p;}
    static final public String[] syncFilterFileSizeTypeValueArray=new String[]{FILTER_FILE_SIZE_TYPE_NONE, FILTER_FILE_SIZE_TYPE_LESS_THAN, FILTER_FILE_SIZE_TYPE_GREATER_THAN};
    static public String getSyncFilterFileSizeTypeByIndex(int index) {
        if (index>=0 && index<=2) return syncFilterFileSizeTypeValueArray[index];
        else return FILTER_FILE_SIZE_TYPE_NONE;
    }

    public static final String FILTER_FILE_SIZE_UNIT_BYTE="BYTE";
    public static final String FILTER_FILE_SIZE_UNIT_KIB="KiB";
    public static final String FILTER_FILE_SIZE_UNIT_MIB="MiB";
    public static final String FILTER_FILE_SIZE_UNIT_GIB="GiB";
    public static final String FILTER_FILE_SIZE_UNIT_DEFAULT=FILTER_FILE_SIZE_UNIT_BYTE;
    private String syncFilterFileSizeUnit = FILTER_FILE_SIZE_UNIT_DEFAULT;
    public String getSyncFilterFileSizeUnit() {return syncFilterFileSizeUnit;}
    public void setSyncFilterFileSizeUnit(String p) {syncFilterFileSizeUnit = p;}
    static final public String[] syncFilterFileSizeUnitValueArray=new String[]{FILTER_FILE_SIZE_UNIT_BYTE, FILTER_FILE_SIZE_UNIT_KIB, FILTER_FILE_SIZE_UNIT_MIB, FILTER_FILE_SIZE_UNIT_GIB};
    static public String getSyncFilterFileSizeUnitByIndex(int index) {
        if (index>=0 && index<=3) return syncFilterFileSizeUnitValueArray[index];
        else return FILTER_FILE_SIZE_TYPE_NONE;
    }

    public static final String FILTER_FILE_DATE_VALUE_DEFAULT = "1";
    private String syncFilterFileDateValue = FILTER_FILE_DATE_VALUE_DEFAULT;
    public String getSyncFilterFileDateValue() {return syncFilterFileDateValue;}
    public void setSyncFilterFileDateValue(String p) {syncFilterFileDateValue = p;}

    public static final String FILTER_FILE_DATE_TYPE_NONE="NONE";
    public static final String FILTER_FILE_DATE_TYPE_OLDER_THAN ="OLDER";
    public static final String FILTER_FILE_DATE_TYPE_NEWER_THAN ="NEWER";
    public static final String FILTER_FILE_DATE_TYPE_AFTER_SYNC_BEGIN_DAY ="SYNC_BEGIN_DAY";
    public static final String FILTER_FILE_DATE_TYPE_DEFAULT=FILTER_FILE_DATE_TYPE_NONE;
    private String syncFilterFileDateType = FILTER_FILE_DATE_TYPE_DEFAULT;
    public String getSyncFilterFileDateType() {return syncFilterFileDateType;}
    public void setSyncFilterFileDateType(String p) {syncFilterFileDateType = p;}
    static final public String[] syncFilterFileDateTypeValueArray=new String[]{FILTER_FILE_DATE_TYPE_NONE, FILTER_FILE_DATE_TYPE_OLDER_THAN, FILTER_FILE_DATE_TYPE_NEWER_THAN, FILTER_FILE_DATE_TYPE_AFTER_SYNC_BEGIN_DAY};
    static public String getSyncFilterFileDateTypeByIndex(int index) {
        if (index>=0 && index<=3) return syncFilterFileDateTypeValueArray[index];
        else return FILTER_FILE_SIZE_TYPE_NONE;
    }

    public String getSyncOptionRetryCount() {return syncOptionRetryCount;}
    public void setSyncOptionRetryCount(String p) {syncOptionRetryCount = p;}

    public boolean isSyncOptionSyncEmptyDirectory() {return syncOptionSyncEmptyDir;}
    public void setSyncOptionSyncEmptyDirectory(boolean p) {syncOptionSyncEmptyDir = p;}

    public boolean isSyncOptionSyncHiddenFile() {return syncOptionSyncHiddenFile;}
    public void setSyncOptionSyncHiddenFile(boolean p) {syncOptionSyncHiddenFile = p;}

    public boolean isSyncOptionSyncHiddenDirectory() {return syncOptionSyncHiddenDir;}
    public void setSyncOptionSyncHiddenDirectory(boolean p) {syncOptionSyncHiddenDir = p;}

    public boolean isSyncOptionSyncSubDirectory() {return syncOptionSyncSubDir;}
    public void setSyncOptionSyncSubDirectory(boolean p) {syncOptionSyncSubDir = p;}

    public boolean isSyncOptionUseSmallIoBuffer() {return syncOptionUseSmallIoBuffer;}
    public void setSyncOptionUseSmallIoBuffer(boolean p) {syncOptionUseSmallIoBuffer = p;}

    public boolean isSyncTestMode() {return syncOptionSyncTestMode;}
    public void setSyncTestMode(boolean p) {syncOptionSyncTestMode = p;}

    public boolean isSyncOptionDifferentFileBySize() {return syncOptionDeterminChangedFileBySize;}
    public void setSyncOptionDifferentFileBySize(boolean p) {syncOptionDeterminChangedFileBySize = p;}

    public boolean isSyncOptionDifferentFileByTime() {return syncOptionDeterminChangedFileByTime;}
    public void setSyncOptionDifferentFileByTime(boolean p) {syncOptionDeterminChangedFileByTime = p;}

    public int getSyncOptionDifferentFileAllowableTime() {return syncOptionDeterminChangedFileByTimeValue;}
    public void setSyncOptionDifferentFileAllowableTime(int p) {syncOptionDeterminChangedFileByTimeValue = p;}

//    public boolean isSyncUseFileCopyByTempNamex() {return syncOptionUseFileCopyByTempName;}
//    public void setSyncUseFileCopyByTempNamex(boolean p) {syncOptionUseFileCopyByTempName = p;}
    public boolean isSyncOptionUseExtendedDirectoryFilter1() {return syncOptionUseExtendedDirectoryFilter1;}
    public void setSyncOptionUseExtendedDirectoryFilter1(boolean p) {syncOptionUseExtendedDirectoryFilter1 = p;}

    private boolean syncOptionUseDirectoryFilterV2=false;
    public boolean isSyncOptionUseDirectoryFilterV2() {return syncOptionUseDirectoryFilterV2;}
    public void setsyncOptionUseDirectoryFilterV2(boolean p) {syncOptionUseDirectoryFilterV2 = p;}

    public String getSyncOptionWifiStatusOption() {return syncOptionWifiStatus;}
    public void setSyncOptionWifiStatusOption(String p) {syncOptionWifiStatus = p;}

    private ArrayList<String> syncOptionWifiConnectedAccessPointWhiteList = new ArrayList<String>();
    public ArrayList<String> getSyncOptionWifiConnectedAccessPointWhiteList() {return syncOptionWifiConnectedAccessPointWhiteList;}
    public void setSyncOptionWifiConnectedAccessPointWhiteList(ArrayList<String> p) {syncOptionWifiConnectedAccessPointWhiteList = p;}

    private ArrayList<String> syncOptionWifiConnectedAddressWhiteList = new ArrayList<String>();
    public ArrayList<String> getSyncOptionWifiConnectedAddressWhiteList() {return syncOptionWifiConnectedAddressWhiteList;}
    public void setSyncOptionWifiConnectedAddressWhiteList(ArrayList<String> p) {syncOptionWifiConnectedAddressWhiteList = p;}

    public boolean isSyncOptionTaskSkipIfConnectAnotherWifiSsid() {return syncTaskSkipIfConnectAnotherWifiSsid;}
    public void setSyncOptionTaskSkipIfConnectAnotherWifiSsid(boolean skip) {syncTaskSkipIfConnectAnotherWifiSsid = skip;}

    public void setSyncOptionSyncWhenCharging(boolean charging) {syncOptionSyncOnlyCharging = charging;}
    public boolean isSyncOptionSyncWhenCharging() {return syncOptionSyncOnlyCharging;}

    public void setSyncOptionDeleteFirstWhenMirror(boolean first) {syncOptionDeleteFirstWhenMirror = first;}
    public boolean isSyncOptionDeleteFirstWhenMirror() {return syncOptionDeleteFirstWhenMirror;}

    public void setSyncOptionEnsureTargetIsExactMirror(boolean first) {SyncOptionEnsureTargetIsExactMirror = first;}
    public boolean isSyncOptionEnsureTargetIsExactMirror() {return SyncOptionEnsureTargetIsExactMirror;}

    private boolean syncOptionConfirmNotExistsExifDate = true;
    public void setSyncOptionConfirmNotExistsExifDate(boolean enabled) {syncOptionConfirmNotExistsExifDate=enabled;}
    public boolean isSyncOptionConfirmNotExistsExifDate() {return syncOptionConfirmNotExistsExifDate;}

    private boolean syncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile = false;
    public void setSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile(boolean enabled) {syncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile=enabled;}
    public boolean isSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile() {return syncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile;}

    private boolean syncOptionIgnoreDstDifference = false;
    public void setSyncOptionIgnoreDstDifference(boolean enabled) {syncOptionIgnoreDstDifference =enabled;}
    public boolean isSyncOptionIgnoreDstDifference() {return syncOptionIgnoreDstDifference;}

    public static final int SYNC_OPTION_OFFSET_OF_DST_10_MIN=10; // index 0
    public static final int SYNC_OPTION_OFFSET_OF_DST_20_MIN=20; // index 1
    public static final int SYNC_OPTION_OFFSET_OF_DST_30_MIN=30; // index 2
    public static final int SYNC_OPTION_OFFSET_OF_DST_40_MIN=40; // index 3
    public static final int SYNC_OPTION_OFFSET_OF_DST_50_MIN=50; // index 4
    public static final int SYNC_OPTION_OFFSET_OF_DST_60_MIN=60; // index 5
    public static final int SYNC_OPTION_OFFSET_OF_DST_70_MIN=70; // index 6
    public static final int SYNC_OPTION_OFFSET_OF_DST_80_MIN=80; // index 7
    public static final int SYNC_OPTION_OFFSET_OF_DST_90_MIN=90; // index 8
    public static final int SYNC_OPTION_OFFSET_OF_DST_100_MIN=100; // index
    public static final int SYNC_OPTION_OFFSET_OF_DST_110_MIN=110; // index 10
    public static final int SYNC_OPTION_OFFSET_OF_DST_120_MIN=120; // index 11
    public static final int SYNC_OPTION_OFFSET_OF_DST_LIST_DEFAULT_ITEM_INDEX=5; // default list index
    public static final int[] SYNC_OPTION_OFFSET_OF_DST_LIST=new int[]{SYNC_OPTION_OFFSET_OF_DST_10_MIN, SYNC_OPTION_OFFSET_OF_DST_20_MIN,
            SYNC_OPTION_OFFSET_OF_DST_30_MIN, SYNC_OPTION_OFFSET_OF_DST_40_MIN, SYNC_OPTION_OFFSET_OF_DST_50_MIN, SYNC_OPTION_OFFSET_OF_DST_60_MIN,
            SYNC_OPTION_OFFSET_OF_DST_70_MIN, SYNC_OPTION_OFFSET_OF_DST_80_MIN, SYNC_OPTION_OFFSET_OF_DST_90_MIN, SYNC_OPTION_OFFSET_OF_DST_100_MIN,
            SYNC_OPTION_OFFSET_OF_DST_110_MIN, SYNC_OPTION_OFFSET_OF_DST_120_MIN};

    public static final int SYNC_OPTION_OFFSET_OF_DST_DEFAULT=SYNC_OPTION_OFFSET_OF_DST_60_MIN;
    private int syncOptionOffsetOfDst = SYNC_OPTION_OFFSET_OF_DST_DEFAULT;
    public void setSyncOptionOffsetOfDst(int offset) {syncOptionOffsetOfDst =offset;}
    public int getSyncOptionOffsetOfDst() {return syncOptionOffsetOfDst;}

    private boolean syncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters = false;
    public void setSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters(boolean enabled) {syncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters=enabled;}
    public boolean isSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters() {return syncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters;}

    public boolean isSyncOptionDoNotUseRenameWhenSmbFileWrite() {return syncOptionDoNotUseRenameWhenSmbFileWrite;}
    public void setSyncOptionDoNotUseRenameWhenSmbFileWrite(boolean p) {syncOptionDoNotUseRenameWhenSmbFileWrite = p;}

    public boolean isSyncOptionSyncAllowGlobalIpAddress() {return syncOptionSyncAllowGlobalIpAddress;}
    public void setSyncOptionSyncAllowGlobalIpAddress(boolean p) {syncOptionSyncAllowGlobalIpAddress = p;}

    private boolean syncOptionMoveOnlyRemoveMasterDirectoryIfEmpty =false;
    public boolean isSyncOptionMoveOnlyRemoveMasterDirectoryIfEmpty() {return syncOptionMoveOnlyRemoveMasterDirectoryIfEmpty;}
    public void setSyncOptionMoveOnlyRemoveMasterDirectoryIfEmpty(boolean p) {syncOptionMoveOnlyRemoveMasterDirectoryIfEmpty = p;}

    static public final int SYNC_FOLDER_ERROR_NO_ERROR =0;
    static public final int SYNC_FOLDER_ERROR_ACCOUNT_NAME =1;
    static public final int SYNC_FOLDER_ERROR_ACCOUNT_PASSWORD =2;
    static public final int SYNC_FOLDER_ERROR_ZIP_PASSWORD =4;
    private int syncMasterFolderError =SyncTaskItem.SYNC_FOLDER_ERROR_NO_ERROR;
    private int syncTargetFolderError =SyncTaskItem.SYNC_FOLDER_ERROR_NO_ERROR;
    private boolean isSyncTaskNameError =false;
    private boolean isSyncTaskWifiOptionError =false;
    public boolean isSyncTaskError() {return (isSyncTaskNameError || isSyncTaskWifiOptionError || (syncMasterFolderError+syncTargetFolderError)!=SYNC_FOLDER_ERROR_NO_ERROR);}
    public int getMasterFolderError() {return syncMasterFolderError;}
    public void setMasterFolderError(int error_code) {syncMasterFolderError = error_code;}
    public int getTargetFolderError() {return syncTargetFolderError;}
    public void setTargetFolderError(int error_code) {syncTargetFolderError = error_code;}
    public boolean getSyncTaskNameError() {return isSyncTaskNameError;}
    public void setSyncTaskNameError(boolean is_error) {isSyncTaskNameError = is_error;}

    public boolean getSyncTaskWifiOptionError() {return isSyncTaskWifiOptionError;}
    public void setSyncTaskWifiOptionError(boolean is_error) {isSyncTaskWifiOptionError = is_error;}

    public void setLastSyncTime(String p) {syncLastSyncTime = p;}
    public void setLastSyncResult(int p) {syncLastSyncResult = p;}

    public String getLastSyncTime() {return syncLastSyncTime;}
    public int getLastSyncResult() {return syncLastSyncResult;}

    public void setSyncTaskRunning(boolean p) {syncTaskIsRunning = p;}
    public boolean isSyncTaskRunning() {return syncTaskIsRunning;}

    public int getSyncTaskPosition() {return syncTaskPosition;}
    public void setSyncTaskPosition(int p) {syncTaskPosition = p;}

    @Override
    public SyncTaskItem clone() {
        SyncTaskItem npfli = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            oos.flush();
            oos.close();

            baos.flush();
            byte[] ba_buff = baos.toByteArray();
            baos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(ba_buff);
            ObjectInputStream ois = new ObjectInputStream(bais);

            npfli = (SyncTaskItem) ois.readObject();
            ois.close();
            bais.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return npfli;
    }

    public boolean isSame(SyncTaskItem sti) {
        boolean result = false;
        if ((syncTaskName.equals(sti.getSyncTaskName()) &&
                (syncTasｋGroup.equals(sti.getSyncTaskGroup())) &&
                (syncTaskEnabled==sti.isSyncTaskAuto()) &&
                (syncOptionSyncTestMode==sti.isSyncTestMode()) &&
                (syncTaskType.equals(sti.getSyncTaskType())) &&
                (syncMasterFolderError==sti.getMasterFolderError()) &&
                (syncTargetFolderError==sti.getTargetFolderError()) &&
                //(isSyncTaskNameError==sti.getSyncTaskNameError()) &&
                (isSyncTaskWifiOptionError==sti.getSyncTaskWifiOptionError()) &&
                (syncTwoWayConflictOption.equals(sti.getSyncTwoWayConflictFileRule())) &&
                (syncTwoWayConflictKeepConflictFile==sti.isSyncTwoWayKeepConflictFile() ))&&
                (syncTaskMasterFolderType.equals(sti.getMasterFolderType())) &&
                (syncTaskMasterFolderDirName.equals(sti.getMasterDirectoryName())) &&
                (syncTaskMasterLocalMountPoint.equals(sti.getMasterLocalMountPoint())) &&
                (syncTaskMasterFolderSmbShareName.equals(sti.getMasterSmbShareName())) &&
                (syncTaskMasterFolderSmbIpAddress.equals(sti.getMasterSmbAddr())) &&
                (syncTaskMasterFolderSmbHostName.equals(sti.getMasterSmbHostName())) &&
                (syncTaskMasterFolderSmbPortNumber.equals(sti.getMasterSmbPort())) &&
                (syncTaskMasterFolderSmbUserName.equals(sti.getMasterSmbUserName())) &&
                (syncTaskMasterFolderSmbPassword.equals(sti.getMasterSmbPassword())) &&
                (syncTaskMasterFolderSmbDomain.equals(sti.getMasterSmbDomain())) &&
                (syncTaskMasterFolderSmbProtocol.equals(sti.getMasterSmbProtocol())) &&
                (syncTaskMasterFolderSmbIpcSigningEnforced==sti.isMasterSmbIpcSigningEnforced()) &&
                (syncTaskMasterFolderSmbUseSmb2Negotiation==sti.isMasterSmbUseSmb2Negotiation()) &&
                (syncTaskMasterFolderRemovableStorageID.equals(sti.getMasterRemovableStorageID()))) {
//                Log.v("","step1");
            if ((syncTaskTargetFolderType.equals(sti.getTargetFolderType())) &&
                    (syncTaskTargetFolderDirName.equals(sti.getTargetDirectoryName())) &&
                    (syncTaskTargetLocalMountPoint.equals(sti.getTargetLocalMountPoint())) &&
                    (syncTaskTargetFolderSmbShareName.equals(sti.getTargetSmbShareName())) &&
                    (syncTaskTargetFolderSmbIpAddress.equals(sti.getTargetSmbAddr())) &&
                    (syncTaskTargetFolderSmbHostName.equals(sti.getTargetSmbHostName())) &&
                    (syncTaskTargetFolderSmbPortNumber.equals(sti.getTargetSmbPort())) &&
                    (syncTaskTargetFolderSmbUserName.equals(sti.getTargetSmbUserName())) &&
                    (syncTaskTargetFolderSmbPassword.equals(sti.getTargetSmbPassword())) &&
                    (syncTaskTargetFolderSmbDomain.equals(sti.getTargetSmbDomain())) &&
                    (syncTaskTargetFolderSmbProtocol.equals(sti.getTargetSmbProtocol())) &&
                    (syncTaskTargetFolderSmbIpcSigningEnforced==sti.isTargetSmbIpcSigningEnforced()) &&
                    (syncTaskTargetFolderSmbUseSmb2Negotiation==sti.isTargetSmbUseSmb2Negotiation()) &&
                    (syncTaskTargetFolderRemovableStorageID.equals(sti.getTargetRemovableStorageID()))) {
//                Log.v("","step2");
                if ((syncTaskTargetZipFileName.equals(sti.getTargetZipOutputFileName())) &&
                        (syncTaskTargetZipCompOptionCompLevel.equals(sti.getTargetZipCompressionLevel())) &&
                        (syncTaskTargetZipCompOptionCompMethod.equals(sti.getTargetZipCompressionMethod())) &&
                        (syncTaskTargetZipCompOptionEncrypt.equals(sti.getTargetZipEncryptMethod())) &&
                        (syncTaskTargetZipCompOptionPassword.equals(sti.getTargetZipPassword())) &&
                        (syncTaskTargetZipCompOptionEncoding.equals(sti.getTargetZipFileNameEncoding())) &&
                        (syncTaskTargetZipUseSdcard ==sti.isTargetZipUseExternalSdcard()) &&
                        (syncTaskTargetZipUseUsb ==sti.isTargetZipUseUsb()) &&
                        (syncTaskArchiveRenameWhenArchive ==sti.isArchiveUseRename()) &&
                        (syncTaskArchiveRenameFileTemplate.equals(sti.getArchiveRenameFileTemplate())) &&
                        (syncTaskArchiveRetentionPeriod ==sti.getArchiveRetentionPeriod()) &&
                        (syncTaskArchiveCreateDirectory ==sti.isArchiveCreateDirectory()) &&
                        (syncTaskArchiveSaveDirectoryTemplate.equals(sti.getArchiveCreateDirectoryTemplate())) &&
                        (syncTaskArchiveSuffixDigit.equals(sti.getArchiveSuffixOption())) &&
                        (syncTaskArchiveEnable ==sti.isArchiveEnabled()) &&
                        (syncFileTypeAudio==sti.isSyncFileTypeAudio()) &&
                        (syncFileTypeImage==sti.isSyncFileTypeImage()) &&
                        (syncFileTypeVideo==sti.isSyncFileTypeVideo()) &&

                        (syncSyncOptionIgnoreFileSize0ByteFile ==sti.isSyncOptionIgnoreFileSize0ByteFile()) &&

                        (syncSyncOptionMaxFileNameLength == sti.getSyncOptionMaxFileNameLength()) &&

                        (syncFilterFileSizeValue.equals(sti.getSyncFilterFileSizeValue())) &&
                        (syncFilterFileSizeType.equals(sti.getSyncFilterFileSizeType())) &&
                        (syncFilterFileSizeUnit.equals(sti.getSyncFilterFileSizeUnit())) &&
                        (syncFilterFileDateValue.equals(sti.getSyncFilterFileDateValue())) &&
                        (syncFilterFileDateType.equals(sti.getSyncFilterFileDateType())) &&

                        (syncOptionRootDirFileToBeProcessed==sti.isSyncProcessRootDirFile()) &&
                        (syncOptionProcessOverrideCopyMove==sti.isSyncOverrideCopyMoveFile()) &&
                        (syncOptionConfirmOverrideDelete==sti.isSyncConfirmOverrideOrDelete()) &&
                        (syncOptionForceLastModifiedUseSmbsync==sti.isSyncDetectLastModifiedBySmbsync()) &&
                        (syncOptionNotUsedLastModifiedForRemote==sti.isSyncDoNotResetFileLastModified()) &&
                        (syncOptionDeterminChangedFileSizeGreaterThanTargetFile==sti.isSyncDifferentFileSizeGreaterThanTagetFile()) &&
                        (syncOptionRetryCount.equals(sti.getSyncOptionRetryCount())) &&
                        (syncOptionSyncEmptyDir==sti.isSyncOptionSyncEmptyDirectory()) &&
                        (syncTaskTargetUseTakenDateTimeForDirectoryNameKeyword == sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) &&
                        (syncOptionSyncHiddenFile==sti.isSyncOptionSyncHiddenFile()) &&
                        (syncOptionSyncHiddenDir==sti.isSyncOptionSyncHiddenDirectory()) &&
                        (syncOptionSyncSubDir==sti.isSyncOptionSyncSubDirectory()) &&
                        (syncOptionUseSmallIoBuffer==sti.isSyncOptionUseSmallIoBuffer()) &&
                        (syncOptionDeterminChangedFileBySize==sti.isSyncOptionDifferentFileBySize()) &&
                        (syncOptionDeterminChangedFileByTime==sti.isSyncOptionDifferentFileByTime()) &&
                        (syncOptionDeterminChangedFileByTimeValue == sti.getSyncOptionDifferentFileAllowableTime()) &&
//                        (syncOptionUseFileCopyByTempName==sti.isSyncUseFileCopyByTempName()) &&
                        (syncOptionDeleteFirstWhenMirror==sti.isSyncOptionDeleteFirstWhenMirror()) &&
                        (SyncOptionEnsureTargetIsExactMirror==sti.isSyncOptionEnsureTargetIsExactMirror()) &&
                        (syncOptionConfirmNotExistsExifDate==sti.isSyncOptionConfirmNotExistsExifDate()) &&

                        (syncOptionUseExtendedDirectoryFilter1==sti.isSyncOptionUseExtendedDirectoryFilter1()) &&
                        (syncOptionUseDirectoryFilterV2==sti.isSyncOptionUseDirectoryFilterV2()) &&

                        (syncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile==sti.isSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile()) &&

                        (syncOptionIgnoreDstDifference==sti.isSyncOptionIgnoreDstDifference()) &&

                        (syncOptionOffsetOfDst==sti.getSyncOptionOffsetOfDst()) &&

                        (syncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters==sti.isSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters()) &&

                        (syncOptionDoNotUseRenameWhenSmbFileWrite==sti.isSyncOptionDoNotUseRenameWhenSmbFileWrite()) &&

                        (syncOptionWifiStatus.equals(sti.getSyncOptionWifiStatusOption())) &&

                        (syncTaskSkipIfConnectAnotherWifiSsid==sti.isSyncOptionTaskSkipIfConnectAnotherWifiSsid()) &&

                        (syncOptionSyncAllowGlobalIpAddress==sti.isSyncOptionSyncAllowGlobalIpAddress()) &&

                        (syncOptionMoveOnlyRemoveMasterDirectoryIfEmpty ==sti.isSyncOptionMoveOnlyRemoveMasterDirectoryIfEmpty()) &&

                        (syncOptionSyncOnlyCharging==sti.isSyncOptionSyncWhenCharging())) {

                    String ff_cmp1 = "";
                    for (String item : syncFileFilter) ff_cmp1 += item;

                    String ff_cmp2 = "";
                    for (String item : sti.getFileFilter()) ff_cmp2 += item;

                    String df_cmp1 = "";
                    for (String item : syncDirFilter) df_cmp1 += item;

                    String df_cmp2 = "";
                    for (String item : sti.getDirFilter()) df_cmp2 += item;

                    String wap_cmp1 = "";
                    for (String item : syncOptionWifiConnectedAccessPointWhiteList) wap_cmp1 += item;

                    String wap_cmp2 = "";
                    for (String item : sti.getSyncOptionWifiConnectedAccessPointWhiteList()) wap_cmp2 += item;

                    String wad_cmp1 = "";
                    for (String item : syncOptionWifiConnectedAddressWhiteList) wad_cmp1 += item;

                    String wad_cmp2 = "";
                    for (String item : sti.getSyncOptionWifiConnectedAddressWhiteList()) wad_cmp2 += item;

                    if ((ff_cmp1.equals(ff_cmp2)) &&
                            (df_cmp1.equals(df_cmp2)) &&
                            (wap_cmp1.equals(wap_cmp2)) &&
                            (wad_cmp1.equals(wad_cmp2))
                            ) {
                        result = true;
                    }
                }
            }


        }
        return result;
    }
}
