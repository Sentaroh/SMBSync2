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

import com.sentaroh.android.Utilities.ThemeUtil;

public class Constants {

	public static final String APPLICATION_TAG="SMBSync2";
	public static final String PACKAGE_NAME="com.sentaroh.android."+APPLICATION_TAG;
	public static final String APP_SPECIFIC_DIRECTORY="Android/data/com.sentaroh.android."+APPLICATION_TAG;
	public static long SERIALIZABLE_NUMBER=1L;
	public static final String LOG_FILE_NAME=APPLICATION_TAG+"_log";

	public static final String DEFAULT_PREFS_FILENAME="default_preferences";

    public final static int SYNC_IO_AREA_SIZE = 1024 * 1024 * 4;
    public final static int GENERAL_IO_AREA_SIZE = 1024 * 1024;

	public final static boolean BUILD_FOR_AMAZON=false;
	
	public static final String SMBSYNC2_CONFIRM_REQUEST_COPY="Copy";
	public static final String SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE="DeleteFile";
	public static final String SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR="DeleteDir";
	public static final String SMBSYNC2_CONFIRM_REQUEST_MOVE="Move";
    public static final String SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE ="Archive";
    public static final String SMBSYNC2_CONFIRM_REQUEST_CONFLICT_FILE ="Conflict";
	public static final int SMBSYNC2_CONFIRM_RESP_YES = 1;
	public static final int SMBSYNC2_CONFIRM_RESP_YESALL = 2;
	public static final int SMBSYNC2_CONFIRM_RESP_NO = -1;
	public static final int SMBSYNC2_CONFIRM_RESP_NOALL = -2;
	public static final int SMBSYNC2_CONFIRM_RESP_CANCEL = -10;

    public static final int SMBSYNC2_CONFIRM_CONFLICT_RESP_SELECT_A = 21;
    public static final int SMBSYNC2_CONFIRM_CONFLICT_RESP_SELECT_B = 22;
    public static final int SMBSYNC2_CONFIRM_CONFLICT_RESP_NO = -21;
    public static final int SMBSYNC2_CONFIRM_CONFLICT_RESP_CANCEL = -30;

    public static final String SMBSYNC2_KEY_STORE_ALIAS = "SMBSync2";
    public static final String SMBSYNC2_KEY_STORE_AES_ALIAS = "SMBSync2_AES";

	public final static String SMBSYNC2_PROFILE_FILE_NAME_V1="profile_v1.txt";
	public final static String SMBSYNC2_PROFILE_FILE_NAME_V2="profile_v2.txt";
	public final static String SMBSYNC2_PROFILE_FILE_NAME_V3="profile_v3.txt";
	public final static String SMBSYNC2_PROFILE_FILE_NAME_V4="profile_v4.txt";
    public final static String SMBSYNC2_PROFILE_FILE_NAME_V5="profile_v5.txt";
    public final static String SMBSYNC2_PROFILE_FILE_NAME_V6="profile_v6.txt";
    public final static String SMBSYNC2_PROFILE_FILE_NAME_V7="profile_v7.txt";
    public final static String SMBSYNC2_PROFILE_FILE_NAME_V8="profile_v8.txt";
    public final static String SMBSYNC2_PROFILE_FILE_NAME_V9="profile_v9.txt";

	public final static String SMBSYNC2_PROF_VER1="PROF 1";
	public final static String SMBSYNC2_PROF_VER2="PROF 2";
	public final static String SMBSYNC2_PROF_VER3="PROF 3";
	public final static String SMBSYNC2_PROF_VER4="PROF 4";
    public final static String SMBSYNC2_PROF_VER5="PROF 5";
    public final static String SMBSYNC2_PROF_VER6="PROF 6";
    public final static String SMBSYNC2_PROF_VER7="PROF 7";
    public final static String SMBSYNC2_PROF_VER8="PROF 8";
    public final static String SMBSYNC2_PROF_VER9="PROF 9";
	public final static String SMBSYNC2_PROF_ENC="ENC";
	public final static String SMBSYNC2_PROF_DEC="DEC";

    public final static String SMBSYNC2_PROF_DECRYPT_FAILED="<decrypt failed>";
    public final static String SMBSYNC2_PROF_ENCRYPT_FAILED="<encrypt failed>";
	
	public final static String CURRENT_SMBSYNC2_PROFILE_FILE_NAME=SMBSYNC2_PROFILE_FILE_NAME_V9;
	public final static String CURRENT_SMBSYNC2_PROFILE_VERSION=SMBSYNC2_PROF_VER9;
	
	public final static String SMBSYNC2_SERIALIZABLE_FILE_NAME="serial.txt";
	public final static String SMBSYNC2_LOCAL_FILE_LAST_MODIFIED_NAME_V1="local_file_last_modified_V1";
	public final static String SMBSYNC2_LOCAL_FILE_LAST_MODIFIED_WAS_FORCE_LASTEST="*";

	public final static String SMBSYNC2_START_SYNC_INTENT="com.sentaroh.android."+APPLICATION_TAG+".ACTION_START_SYNC";
	public final static String SMBSYNC2_EXTRA_PARM_SYNC_PROFILE="SyncProfile";
    public final static String START_SYNC_EXTRA_PARM_TASK_NAME ="TaskName";
    public final static String START_SYNC_EXTRA_PARM_SYNC_RESULT ="SYNC_RESULT";
    public final static String START_SYNC_EXTRA_PARM_SYNC_RESULT_SUCCESS ="SUCCESS";
    public final static String START_SYNC_EXTRA_PARM_SYNC_RESULT_ERROR ="ERROR";
    public final static String START_SYNC_EXTRA_PARM_SYNC_RESULT_WARNING ="WARNING";
    public final static String START_SYNC_EXTRA_PARM_SYNC_RESULT_CANCEL ="CANCEL";
    public final static String START_SYNC_EXTRA_PARM_SYNC_RESULT_NOT_FOUND ="NOT_FOUND";
    public final static String START_SYNC_EXTRA_PARM_SYNC_RESULT_TASK_NAME ="TASK_NAME";
	public final static String SMBSYNC2_AUTO_SYNC_INTENT="com.sentaroh.android."+APPLICATION_TAG+".ACTION_AUTO_SYNC";

    public final static String QUERY_SYNC_TASK_INTENT ="com.sentaroh.android."+APPLICATION_TAG+".ACTION_QUERY_SYNC_TASK";
    public final static String QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE ="TaskType";
    public final static String QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_ALL ="All";
    public final static String QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_AUTO ="Auto";
    public final static String QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_MANUAL ="Manual";
    public final static String QUERY_SYNC_TASK_EXTRA_PARM_TASK_TYPE_TEST ="Test";
    public final static String REPLY_SYNC_TASK_INTENT ="com.sentaroh.android."+APPLICATION_TAG+".ACTION_REPLY_SYNC_TASK";
    public final static String REPLY_SYNC_TASK_EXTRA_PARM_SYNC_COUNT ="SYNC_COUNT";
    public final static String REPLY_SYNC_TASK_EXTRA_PARM_SYNC_ARRAY ="SYNC_LIST";

	public final static String SMBSYNC2_SERVICE_HEART_BEAT="com.sentaroh.android."+APPLICATION_TAG+".ACTION_SERVICE_HEART_BEAT";

	public final static String SMBSYNC2_SYNC_STARTED="com.sentaroh.android."+APPLICATION_TAG+".ACTION_SYNC_STARTED";
	public final static String SMBSYNC2_SYNC_ENDED="com.sentaroh.android."+APPLICATION_TAG+".ACTION_SYNC_ENDED";

    public final static String SMBSYNC2_SERVICE_MEDIA_STATUS_CAHNGED="com.sentaroh.android."+APPLICATION_TAG+".ACTION_MEDIA_STATUS_CHANGED";


    public final static String SMBSYNC2_SYNC_REQUEST_ACTIVITY="ACTIVITY";
	public final static String SMBSYNC2_SYNC_REQUEST_EXTERNAL="EXTERNAL";
	public final static String SMBSYNC2_SYNC_REQUEST_SHORTCUT="SHORTCUT";
	public final static String SMBSYNC2_SYNC_REQUEST_SCHEDULE="SCHEDULE";
	
	public final static String SMBSYNC2_PROF_TYPE_SYNC="S";
	public final static String SMBSYNC2_PROF_TYPE_SETTINGS="T";
	
	public final static String SMBSYNC2_UNLOAD_SETTINGS_TYPE_STRING="S";
	public final static String SMBSYNC2_UNLOAD_SETTINGS_TYPE_BOOLEAN="B";
	public final static String SMBSYNC2_UNLOAD_SETTINGS_TYPE_INT="I";
	public final static String SMBSYNC2_UNLOAD_SETTINGS_TYPE_LONG="L";
	
	public final static String SMBSYNC2_PROF_FILTER_INCLUDE="I";
	public final static String SMBSYNC2_PROF_FILTER_EXCLUDE="E";
    final public static String SMBSYNC2_PROF_FILTER_FILE="FILE_FILTER";
    final public static String SMBSYNC2_PROF_FILTER_DIR="DIR_FILTER";
    final public static String WHOLE_DIRECTORY_FILTER_PREFIX_V1="\\\\";//only in v1 old filter, match pattern of whole path: filter==\\cache matches */cache/*, doesn't support */cache/data/* !
    final public static String WHOLE_DIRECTORY_FILTER_PREFIX_V2="\\";

    final public static String[] SMBSYNC2_PROF_FILTER_FILE_INVALID_CHARS=new String[]{"\"", ":", ">", "<", "|", "//", "**", "\\"};
    final public static String[] SMBSYNC2_PROF_FILTER_DIR_INVALID_CHARS=new String[] {"\"", ":", ">", "<", "|", "//", "**"};
    final public static String[] SYNC_TASK_NAME_UNUSABLE_CHARACTER=new String[]{","};
    final public static int SYNC_TASK_NAME_MAX_LENGTH=64;

    final public static String SYNC_TASK_LIST_SEPARATOR=",";

    public static final String SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_NO = "0";
	public static final String SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS = "1";
	public static final String SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_SUCCESS = "2";
	public static final String SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ERROR = "3";
	
	public static final String SMBSYNC2_RINGTONE_NOTIFICATION_NO = "0";
	public static final String SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS = "1";
	public static final String SMBSYNC2_RINGTONE_NOTIFICATION_SUCCESS = "2";
	public static final String SMBSYNC2_RINGTONE_NOTIFICATION_ERROR = "3";

	public static final String SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_NO = "0";
	public static final String SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS = "1";
	public static final String SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_SUCCESS = "2";
	public static final String SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ERROR = "3";

    public static final String SMBSYNC2_SCREEN_THEME_STANDARD = String.valueOf(ThemeUtil.THEME_DEFAULT);
    public static final String SMBSYNC2_SCREEN_THEME_LIGHT = String.valueOf(ThemeUtil.THEME_LIGHT);
    public static final String SMBSYNC2_SCREEN_THEME_BLACK = String.valueOf(ThemeUtil.THEME_BLACK);

    public static final String SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM = "0";
    public static final String SMBSYNC2_SCREEN_THEME_LANGUAGE_INIT = "-99";//ensure onStartSettingScreenThemeLanguageValue is assigned language value only at first app start and when language change by user

    public static final String SMBSYNC2_PROFILE_RETRY_COUNT="3";
	
	public static final int ACTIVITY_REQUEST_CODE_SDCARD_STORAGE_ACCESS=40;
    public static final int ACTIVITY_REQUEST_CODE_USB_STORAGE_ACCESS=50;

	public static final String SMBSYNC2_REPLACEABLE_KEYWORD_YEAR="%YEAR%";
    public static final String SMBSYNC2_REPLACEABLE_KEYWORD_MONTH="%MONTH%";
    public static final String SMBSYNC2_REPLACEABLE_KEYWORD_DAY="%DAY%";
    public static final String SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR="%DAY-OF-YEAR%";

    public static final String SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY ="%WEEKDAY%";
    public static final String SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY_LONG ="%WEEKDAY_LONG%";
    public static final String SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_NUMBER ="%WEEKNO%";

//    *.aac, *.aif, *.aifc, *.aiff, *.kar, *.m3u, *.m4a, *.mid, *.midi, *.mp2, *.mp3, *.mpga, *.ra, *.ram, *.wav
//    *.bmp, *.cgm, *.djv, *.djvu, *.gif, *.ico, *.ief, *.jpe, *.jpeg, *.jpg, *.pbm, *.pgm, *.png, *.pnm, *.ppm, *.ras, *.rgb, *.svg, *.tif, *.tiff, *.wbmp, *.xbm, *.xpm, *.xwd
//    *.avi, *.m4u, *.mov, *.movie, *.mpe, *.mpeg, *.mpg, *.mxu, *.qt, *.wmv

	public static final String[] SYNC_FILE_TYPE_AUDIO=
			new String[]{"*.aac","*.aif", "*.aifc", "*.aiff", "*.flac", "*.kar", "*.m3u", "*.m4a", "*.mid", "*.midi", "*.mp2",
							"*.mp3", "*.mpga", "*.ogg", "*.ra", "*.ram", "*.wav"};
	public static final String[] SYNC_FILE_TYPE_IMAGE=
			new String[]{"*.bmp", "*.cgm", "*.djv", "*.djvu", "*.gif", "*.ico", "*.ief", "*.jpe", "*.jpeg", "*.jpg", "*.pbm",
						"*.pgm", "*.png", "*.pnm", "*.ppm", "*.ras", "*.rgb", "*.svg", "*.tif", "*.tiff", "*.wbmp", "*.xbm", 
						"*.xpm", "*.xwd"};
	public static final String[] SYNC_FILE_TYPE_VIDEO=
			new String[]{"*.avi", "*.m4u", "*.mov", "*.mp4", "*.movie", "*.mpe", "*.mpeg", "*.mpg", "*.mxu", "*.qt", "*.wmv"};

    public static final String[] ARCHIVE_FILE_TYPE=
            new String[]{"gif", "jpg", "jpeg", "jpe", "png", "mp4", "mov"};

}
