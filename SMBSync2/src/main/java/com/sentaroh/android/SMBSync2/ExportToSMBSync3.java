package com.sentaroh.android.SMBSync2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sentaroh.android.Utilities.EncryptUtil;
import com.sentaroh.android.Utilities.SafManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static com.sentaroh.android.SMBSync2.GlobalParameters.DEFAULT_NOCOMPRESS_FILE_TYPE;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_ENABLED_KEY;

public class ExportToSMBSync3 {
    private static Logger log = LoggerFactory.getLogger(ExportToSMBSync3.class);

    private final static String SYNC_TASK_CONFIG_FILE_IDENTIFIER = "<!--SMBSync3 Configuration file(Do not change this file)-->";
    private final static String SYNC_TASK_XML_TAG_FILTER_ITEM = "filter";
    private final static String SYNC_TASK_XML_TAG_FILTER_INCLUDE = "include";
    private final static String SYNC_TASK_XML_TAG_FILTER_VALUE = "value";
    private final static String SYNC_TASK_XML_TAG_CONFIG = "config_list";
    private final static String SYNC_TASK_XML_TAG_CONFIG_VERSION = "version";
    private final static String SYNC_TASK_XML_TAG_TASK = "task";
    private final static String SYNC_TASK_XML_TAG_OPTION = "option";
    private final static String SYNC_TASK_XML_TAG_MASTER = "master";
    private final static String SYNC_TASK_XML_TAG_TARGET = "target";

//    private static final String SYNC_TASK_XML_TAG_ARCHIVE_CREATE_DIRECTORY_TEMPLATE = "archive_create_directory_template";
//    private static final String SYNC_TASK_XML_TAG_ARCHIVE_CREATE_DIRECTORY = "archive_create_directory";
//    private static final String SYNC_TASK_XML_TAG_ARCHIVE_ENABLED = "archive_enabled";
    private static final String SYNC_TASK_XML_TAG_ARCHIVE_RENAME_FILE_TEMPLATE = "archive_rename_file_template";
    private static final String SYNC_TASK_XML_TAG_ARCHIVE_RETENTION_PERIOD = "archive_retention_period";
    private static final String SYNC_TASK_XML_TAG_ARCHIVE_SUFFIX_OPTION = "archive_suffix_option";
//    private static final String SYNC_TASK_XML_TAG_ARCHIVE_USE_RENAME = "archive_use_rename";

    private static final String SYNC_TASK_XML_TAG_FILTER_DIRECTORY = "filter_directory";
    private static final String SYNC_TASK_XML_TAG_FILTER_FILE = "filter_file";
    private static final String SYNC_TASK_XML_TAG_FILTER_FILE_PRESET_AUDIO = "filter_file_preset_audio";
    private static final String SYNC_TASK_XML_TAG_FILTER_FILE_PRESET_IMAGE = "filter_file_preset_image";
    private static final String SYNC_TASK_XML_TAG_FILTER_FILE_PRESET_VIDEO = "filter_file_preset_video";
    private static final String SYNC_TASK_XML_TAG_FILTER_IPADDR = "filter_ipaddr";
    private static final String SYNC_TASK_XML_TAG_FILTER_SSID = "filter_ssid";

    private static final String SYNC_TASK_XML_TAG_FOLDER_DIRECTORY = "directory";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_DOMAIN = "smb_server_domain_name";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_ACCOUNT_NAME = "smb_server_account_name";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_ACCOUNT_PASSWORD = "smb_server_account_password";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_ENCRYPTED_ACCOUNT_NAME = "smb_server_encrypted_account_name";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_ENCRYPTED_ACCOUNT_PASSWORD = "smb_server_encrypted_account_password";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_ADDR = "smb_server_addr";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_IPC_SIGNIN_ENFORCED = "smb_server_ipc_signin_enforced";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_NAME = "smb_server_name";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_PORT = "smb_server_port";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_PROTOCOL = "smb_server_protocol";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_SHARE_NAME = "smb_server_share_name";
    private static final String SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_USE_SMB2_NEGO = "smb_server_use_smb2_nego";
    private static final String SYNC_TASK_XML_TAG_FOLDER_UUID = "uuid";
    private static final String SYNC_TASK_XML_TAG_FOLDER_TYPE = "type";
    private static final String SYNC_TASK_XML_TAG_FOLDER_ZIP_COMP_LEVEL = "zip_comp_level";
    private static final String SYNC_TASK_XML_TAG_FOLDER_ZIP_COMP_METHOD = "zip_comp_method";
    private static final String SYNC_TASK_XML_TAG_FOLDER_ZIP_ENCRYPT_METHOD = "zip_encrypt_method";
    private static final String SYNC_TASK_XML_TAG_FOLDER_ZIP_FILE_NAME_ENCODING = "zip_file_name_encoding";
    private static final String SYNC_TASK_XML_TAG_FOLDER_ZIP_OUTPUT_FILE_NAME = "zip_output_file_name";
    private static final String SYNC_TASK_XML_TAG_FOLDER_ZIP_OUTPUT_FILE_PASSWORD = "zip_output_file_password";
    private static final String SYNC_TASK_XML_TAG_FOLDER_ZIP_ENCRYPTED_OUTPUT_FILE_PASSWORD = "zip_encrypted_output_file_password";

    private static final String SYNC_TASK_XML_TAG_OPTION_ALLOW_GLOBAL_IP_ADDRESS = "allow_global_ip_address";
    private static final String SYNC_TASK_XML_TAG_OPTION_ALLOWABLE_TIME_FOR_DIFFERENT_FILE = "allowable_time_for_different_file";
    private static final String SYNC_TASK_XML_TAG_OPTION_CONFIIRM_OVERRIDE_OR_DELETE = "confirm_override_or_delete";
    private static final String SYNC_TASK_XML_TAG_OPTION_CONFIRM_NOT_EXIST_EXIF_DATE = "confirm_not_exist_exif_date";
    private static final String SYNC_TASK_XML_TAG_OPTION_DELETE_EMPTY_MASTER_DIRECTORY_WHEN_MOVE = "delete_empty_master_directory_when_move";
    private static final String SYNC_TASK_XML_TAG_OPTION_DELETE_FIRST_WHEN_MIRROR = "delete_first_when_mirror";
    private static final String SYNC_TASK_XML_TAG_OPTION_DETECT_DIFFERENT_FILE_BY_SIZE = "detect_differeent_file_by_size";
    private static final String SYNC_TASK_XML_TAG_OPTION_DETECT_DIFFERENT_FILE_BY_SIZE_IF_GT_MASTER = "detect_differeent_file_by_size_if_gt_master";
    private static final String SYNC_TASK_XML_TAG_OPTION_DETECT_DIFFERENT_FILE_BY_TIME = "detect_differeent_file_by_time";
    private static final String SYNC_TASK_XML_TAG_OPTION_DO_NOT_OVERRIDE_WHEN_TARGET_FILE_IS_NEWER_THAN_MASTER = "do_not_override_when_target_file_is_newer_than_master";
    private static final String SYNC_TASK_XML_TAG_OPTION_DO_NOT_RESET_FILE_LAST_MODIFIED_TIME = "do_not_reset_file_last_modified_time";
    private static final String SYNC_TASK_XML_TAG_OPTION_IGNORE_FILE_DIRECTORY_THAT_CONTAIN_UNUSABLE_CHARACTER = "ignore_file_directory_that_contain_unusable_character";
    private static final String SYNC_TASK_XML_TAG_OPTION_NETWORK_ERROR_RETRY_COUNT = "network_error_retry_count";
    private static final String SYNC_TASK_XML_TAG_OPTION_OVERRIDE_FILE_WHEN_COPY_OR_MOVE = "override_file_when_copy_or_move";
    private static final String SYNC_TASK_XML_TAG_OPTION_PERFORM_SYNC_WHEN_CHARGING = "perform_sync_when_charging";
    private static final String SYNC_TASK_XML_TAG_OPTION_PROCESS_ROOT_DIRECTORY_FILE = "process_root_directory_file";
    private static final String SYNC_TASK_XML_TAG_OPTION_SKIP_TASK_IF_ANOTHER_SSID_CONNECTED = "skip_task_if_another_ssid_connected";
    private static final String SYNC_TASK_XML_TAG_OPTION_SMB_TARGET_FOLDER_USE_SMALL_BUFFER = "smb_target_folder_use_small_buffer";
    private static final String SYNC_TASK_XML_TAG_OPTION_SYNC_EMPTY_DIRECTORY = "sync_empty_directory";
    private static final String SYNC_TASK_XML_TAG_OPTION_SYNC_HIDDEN_DIRECTORY = "sync_hidden_directory";
    private static final String SYNC_TASK_XML_TAG_OPTION_SYNC_HIDDEN_FILE = "sync_hidden_file";
    private static final String SYNC_TASK_XML_TAG_OPTION_SYNC_SUB_DIRECTORY = "sync_sub_directory";
    private static final String SYNC_TASK_XML_TAG_OPTION_TARGET_USE_TAKEN_DATE_DIRECTORY_NAME_KEYWORD = "sync_target_use_taken_date_directory_name_keyword";
    private static final String SYNC_TASK_XML_TAG_OPTION_TARGET_USE_TAKEN_DATE_FILE_NAME_KEYWORD = "sync_target_use_taken_date_file_name_keyword";
    private static final String SYNC_TASK_XML_TAG_OPTION_TWO_WAY_CONFLICT_FILE_RULE = "two_way_conflict_file_rule";
    private static final String SYNC_TASK_XML_TAG_OPTION_TWO_WAY_KEEP_CONFLICT_FILE = "two_way_keep_conflict_file";
    private static final String SYNC_TASK_XML_TAG_OPTION_WIFI_STATUS = "wifi_status";

    private static final String SYNC_TASK_XML_TAG_TASK_AUTO_TASK = "auto";
    private static final String SYNC_TASK_XML_TAG_TASK_ERROR_MASTER = "error_master";
    private static final String SYNC_TASK_XML_TAG_TASK_ERROR_TARGET = "error_target";
    private static final String SYNC_TASK_XML_TAG_TASK_GROUP_NAME = "group";
    private static final String SYNC_TASK_XML_TAG_TASK_LAST_SYNC_RESULT = "last_sync_result";
    private static final String SYNC_TASK_XML_TAG_TASK_LAST_SYNC_TIME = "last_sync_time";
    private static final String SYNC_TASK_XML_TAG_TASK_NAME = "name";
    private static final String SYNC_TASK_XML_TAG_TASK_POSITION = "position";
    private static final String SYNC_TASK_XML_TAG_TASK_TEST_MODE = "test";
    private static final String SYNC_TASK_XML_TAG_TASK_TYPE = "type";

    private final static String SYNC_TASK_XML_TAG_SETTINGS = "settings";

    private static final String SETTINGS_XML_TAG_SYNC_ERROR_HANDLING = "sync_error_handling";
    private static final String SETTINGS_XML_TAG_SYNC_WIFI_LOCKING = "sync_wifi_locking";
    private static final String SETTINGS_XML_TAG_SYNC_KEEP_HISTORY = "sync_keep_history";
    private static final String SETTINGS_XML_TAG_SYNC_PREVENT_DELAY_OF_START_SYNC_DURING_SLEEP = "sync_prevent_start_delay";
    private static final String SETTINGS_XML_TAG_SYNC_DO_NOT_WARN_WHEN_LOCATION_SERVICE_DISABLED = "sync_do_not_warn_location_service_disabled";
    private static final String SETTINGS_XML_TAG_SYNC_DO_NOT_COMPRESS_FILE_TYPES = "sync_do_not_compress_file_types";

    private static final String SETTINGS_XML_TAG_UI_NOTIFICATION_MESSAGES = "ui_notification_messages";
    private static final String SETTINGS_XML_TAG_UI_NOTIFICATION_SOUNDS = "ui_notification_sounds";
    private static final String SETTINGS_XML_TAG_UI_NOTIFICATION_SOUND_VOLUME = "ui_notification_sound_volume";
    private static final String SETTINGS_XML_TAG_UI_NOTIFICATION_VIBRATION = "ui_notification_vibration";
    private static final String SETTINGS_XML_TAG_UI_FORCE_PORTRAIT_ORIENTATION = "ui_force_portrait_orientation";
    private static final String SETTINGS_XML_TAG_UI_SCREEN_THEME = "ui_screen_theme";

    private static final String SETTINGS_XML_TAG_SMB1_LM_COMPATIBILITY_LEVEL = "smb1_lm_compatibility_level";
    private static final String SETTINGS_XML_TAG_SMB1_USE_EXTENDED_SECURITY = "smb1_use_extended_security";
    private static final String SETTINGS_XML_TAG_SMB1_DISABLE_PLAIN_TEXT_PASSWORD = "smb1_disable_plain_text_password";
    private static final String SETTINGS_XML_TAG_SMB1_CLIENT_RESPONSE_TIEOUT = "smb1_client_response_timeout";

    private static final String SETTINGS_XML_TAG_SECURITY_PASSWORD_VALID_TIME_IS_30MIN = "security_password_valid_time_is_30min";
    private static final String SETTINGS_XML_TAG_SECURITY_PASSWORD_AUTH_WHEN_LAUNCH_APPLICATION = "security_password_auth_when_launch_app";
    private static final String SETTINGS_XML_TAG_SECURITY_PASSWORD_AUTH_WHEN_EDIT_TASK = "security_password_auth_when_edit_task";
    private static final String SETTINGS_XML_TAG_SECURITY_PASSWORD_AUTH_WHEN_EXPORT_TASK = "security_password_auth_when_export_task";
    private static final String SETTINGS_XML_TAG_SECURITY_HIDE_SMB_ACCOUNT_PASSWORD_WHEN_EDIT_TASK = "security_hide_smb_account_password_when_edit_task";

    private static final String SETTINGS_XML_TAG_MISC_EXIT_CLEANLY = "misc_exit_cleanly";
    private static final String SETTINGS_XML_TAG_MISC_SYNC_MESSAGE_SHOW_WITH_WORDWRAP = "misc_sync_message_show_with_wordwrap";

    private static final String SETTINGS_XML_TAG_SCHEDULE_ENABLED = "schedule_enabled";

    private final static String SYNC_TASK_XML_TAG_SCHEDULE = "schedule";

    private static final String SCHEDULE_XML_TAG_NAME = "name";
    private static final String SCHEDULE_XML_TAG_ENABLED = "enabled";
    private static final String SCHEDULE_XML_TAG_TYPE = "type";
    private static final String SCHEDULE_XML_TAG_DAY = "day";
    private static final String SCHEDULE_XML_TAG_HOUR = "hour";
    private static final String SCHEDULE_XML_TAG_MIN = "min";
    private static final String SCHEDULE_XML_TAG_DAY_OF_THE_WEEK = "day_of_the_week";
    private static final String SCHEDULE_XML_TAG_EXECUTE_AUTO_TASK = "execute_auto_task";
    private static final String SCHEDULE_XML_TAG_EXECUTE_TASK_LIST = "execute_task_list";
    private static final String SCHEDULE_XML_TAG_CHANGE_CHARGING_OPTION = "change_charging_option";
    private static final String SCHEDULE_XML_TAG_LAST_EXEC_TIME = "last_exec_time";
    private static final String SCHEDULE_XML_TAG_POSITION = "position";
    private static final String SCHEDULE_XML_TAG_GROUP_LIST = "group_list";
    private static final String SCHEDULE_XML_TAG_WIFI_ON_BEFORE_SYNC_BEGIN = "wifi_on_before_sync_begin";
    private static final String SCHEDULE_XML_TAG_WIFI_OFF_AFTER_SYNC_END = "wifi_off_after_sync_end";
    private static final String SCHEDULE_XML_TAG_WIFI_ON_DELAY_TIME = "wifi_on_delay_time";

    public static boolean saveConfigListToExportFile(Context c, String fp,
                         ArrayList<SyncTaskItem> sync_task_list, ArrayList<ScheduleItem> schedule_list) {
        try {
            File sf=new File(fp);
            String config_data=null;
            config_data= buildConfigData(c, sync_task_list, schedule_list, ENCRYPT_MODE_NO_ENCRYPT, null);
            if (config_data!=null) {
                try {
                    if (sf!=null) {
                        OutputStream os=new FileOutputStream(sf);
                        os.write((config_data+"\n"+SYNC_TASK_CONFIG_FILE_IDENTIFIER+"\n").getBytes());
                        os.flush();
                        os.close();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            log.error("saveXmlSyncTaskListToExport failed.", e);
            return false;
        }
    }

    private static final int ENCRYPT_MODE_NO_ENCRYPT=0;
    private static final int ENCRYPT_MODE_ENCRYPT_SOME_PRIVATE_DATA =1;
    private static final int ENCRYPT_MODE_ENCRYPT_WHOLE_LIST=2;
    private static String buildConfigData(Context c,
                                          ArrayList<SyncTaskItem> sync_task_list, ArrayList<ScheduleItem> schedule_list,
                                          int enc_mode, EncryptUtil.CipherParms cp_enc) {
        log.debug("buildConfigData enc_mode="+enc_mode+", cp_enc="+cp_enc);
        boolean result=true;
        String config_data=null;
        try {
            try {
                DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dbuilder = dbfactory.newDocumentBuilder();

                Document main_document = dbuilder.newDocument();
                Element config_tag = main_document.createElement(SYNC_TASK_XML_TAG_CONFIG);
                config_tag.setAttribute(SYNC_TASK_XML_TAG_CONFIG_VERSION, "1.0.1");

                for(SyncTaskItem item:sync_task_list) {
                    Element task_tag = buildXmlTaskElement(c, main_document, item);
                    config_tag.appendChild(task_tag);

                    Element master_tag = buildXmlMasterElement(c, main_document, item, enc_mode, cp_enc);
                    task_tag.appendChild(master_tag);

                    Element target_tag = buildXmlTargetElement(c, main_document, item, enc_mode, cp_enc);
                    task_tag.appendChild(target_tag);

                }

                for(ScheduleItem item:schedule_list) {
                    Element schedule_tag = buildXmlScheduleElement(c, main_document, item);
                    config_tag.appendChild(schedule_tag);
                }

                Element setting_tag = buildXmlSettingsElement(c, main_document);
                config_tag.appendChild(setting_tag);

                main_document.appendChild(config_tag);

                TransformerFactory tffactory = TransformerFactory.newInstance();
                Transformer transformer = tffactory.newTransformer();
                StringWriter sw=new StringWriter();
                transformer.transform(new DOMSource(main_document), new StreamResult(sw));
                sw.flush();
                sw.close();
                config_data=sw.toString();
            }catch (TransformerConfigurationException e) {
                e.printStackTrace();
                log.error("buildConfigData failed.", e);
                result=false;
            } catch (TransformerException e) {
                e.printStackTrace();
                log.error("buildConfigData failed.", e);
                result=false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.error("buildConfigData failed.", e);
            result=false;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("buildConfigData failed.", e);
            result=false;
        }
        return config_data;
    }

    private static Element buildXmlScheduleElement(Context c, Document main_document, ScheduleItem item) {
        Element schedule_tag = main_document.createElement(SYNC_TASK_XML_TAG_SCHEDULE);
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_NAME, item.scheduleName);
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_ENABLED, item.scheduleEnabled?"true":"false");
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_TYPE, item.scheduleType);
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_DAY, item.scheduleDay);
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_HOUR, item.scheduleHours);
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_MIN, item.scheduleMinutes);
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_DAY_OF_THE_WEEK, item.scheduleDayOfTheWeek);
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_EXECUTE_AUTO_TASK, item.syncAutoSyncTask?"true":"false");
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_EXECUTE_TASK_LIST, item.syncTaskList);
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_CHANGE_CHARGING_OPTION, item.syncOverrideOptionCharge);
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_LAST_EXEC_TIME, String.valueOf(item.scheduleLastExecTime));
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_POSITION, String.valueOf(item.schedulePosition));
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_GROUP_LIST, item.syncGroupList);
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_WIFI_ON_BEFORE_SYNC_BEGIN, item.syncWifiOnBeforeStart?"true":"false");
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_WIFI_OFF_AFTER_SYNC_END, item.syncWifiOffAfterEnd?"true":"false");
        schedule_tag.setAttribute(SCHEDULE_XML_TAG_WIFI_ON_DELAY_TIME, String.valueOf(item.syncDelayAfterWifiOn));

        return schedule_tag;
    }

    private static Element buildXmlSettingsElement(Context c, Document main_document) {
        Element setting_tag = main_document.createElement(SYNC_TASK_XML_TAG_SETTINGS);

        setting_tag.setAttribute(SETTINGS_XML_TAG_SYNC_ERROR_HANDLING,
                getSettingParameterBoolean(c,R.string.settings_error_option, false));
        setting_tag.setAttribute(SETTINGS_XML_TAG_SYNC_WIFI_LOCKING,
                getSettingParameterBoolean(c,R.string.settings_wifi_lock, false));
        setting_tag.setAttribute(SETTINGS_XML_TAG_SYNC_KEEP_HISTORY,
                getSettingParameterBoolean(c,R.string.settings_sync_history_log, false));
        setting_tag.setAttribute(SETTINGS_XML_TAG_SYNC_PREVENT_DELAY_OF_START_SYNC_DURING_SLEEP,
                getSettingParameterBoolean(c,R.string.settings_force_screen_on_while_sync, false));
        setting_tag.setAttribute(SETTINGS_XML_TAG_SYNC_DO_NOT_WARN_WHEN_LOCATION_SERVICE_DISABLED,
                getSettingParameterBoolean(c,R.string.settings_suppress_warning_location_service_disabled, false));
        setting_tag.setAttribute(SETTINGS_XML_TAG_SYNC_DO_NOT_COMPRESS_FILE_TYPES,
                getSettingParameterString(c,R.string.settings_no_compress_file_type, DEFAULT_NOCOMPRESS_FILE_TYPE));

        setting_tag.setAttribute(SETTINGS_XML_TAG_UI_NOTIFICATION_MESSAGES,
                getSettingParameterString(c,R.string.settings_notification_message_when_sync_ended, ""));//gp.settingNotificationMessageWhenSyncEnded);
        setting_tag.setAttribute(SETTINGS_XML_TAG_UI_NOTIFICATION_SOUNDS,
                getSettingParameterString(c,R.string.settings_playback_ringtone_when_sync_ended, ""));//gp.settingNotificationSoundWhenSyncEnded);
        setting_tag.setAttribute(SETTINGS_XML_TAG_UI_NOTIFICATION_SOUND_VOLUME,
                getSettingParameterInt(c,R.string.settings_playback_ringtone_volume, 100));//String.valueOf(gp.settingNotificationVolume));
        setting_tag.setAttribute(SETTINGS_XML_TAG_UI_NOTIFICATION_VIBRATION,
                getSettingParameterString(c,R.string.settings_vibrate_when_sync_ended, ""));//gp.settingNotificationVibrateWhenSyncEnded);
        setting_tag.setAttribute(SETTINGS_XML_TAG_UI_FORCE_PORTRAIT_ORIENTATION,
                getSettingParameterBoolean(c,R.string.settings_device_orientation_portrait, false));//gp.settingFixDeviceOrientationToPortrait?"true":"false");
        setting_tag.setAttribute(SETTINGS_XML_TAG_UI_SCREEN_THEME,
                getSettingParameterString(c,R.string.settings_screen_theme, ""));//gp.settingScreenTheme);

        setting_tag.setAttribute(SETTINGS_XML_TAG_SMB1_LM_COMPATIBILITY_LEVEL,
                getSettingParameterString(c,R.string.settings_smb_lm_compatibility, ""));//gp.settingsSmbLmCompatibility);
        setting_tag.setAttribute(SETTINGS_XML_TAG_SMB1_USE_EXTENDED_SECURITY,
                getSettingParameterBoolean(c,R.string.settings_smb_use_extended_security, true));//gp.settingsSmbUseExtendedSecurity);
        setting_tag.setAttribute(SETTINGS_XML_TAG_SMB1_DISABLE_PLAIN_TEXT_PASSWORD,
                getSettingParameterBoolean(c,R.string.settings_smb_disable_plain_text_passwords, false));//gp.settingsSmbDisablePlainTextPasswords);
        setting_tag.setAttribute(SETTINGS_XML_TAG_SMB1_CLIENT_RESPONSE_TIEOUT,
                getSettingParameterString(c,R.string.settings_smb_client_response_timeout, ""));//gp.settingsSmbClientResponseTimeout);

        setting_tag.setAttribute(SETTINGS_XML_TAG_SECURITY_PASSWORD_AUTH_WHEN_LAUNCH_APPLICATION,
                getSettingParameterBoolean(c,R.string.settings_security_application_password_use_app_startup, false));//gp.settingSecurityApplicationPasswordUseAppStartup?"true":"false");
        setting_tag.setAttribute(SETTINGS_XML_TAG_SECURITY_PASSWORD_AUTH_WHEN_EDIT_TASK,
                getSettingParameterBoolean(c,R.string.settings_security_application_password_use_edit_task, false));//gp.settingSecurityApplicationPasswordUseEditTask?"true":"false");
        setting_tag.setAttribute(SETTINGS_XML_TAG_SECURITY_PASSWORD_AUTH_WHEN_EXPORT_TASK,
                getSettingParameterBoolean(c,R.string.settings_security_application_password_use_export_task, false));//gp.settingSecurityApplicationPasswordUseExport?"true":"false");
        setting_tag.setAttribute(SETTINGS_XML_TAG_SECURITY_HIDE_SMB_ACCOUNT_PASSWORD_WHEN_EDIT_TASK,
                getSettingParameterBoolean(c,R.string.settings_security_init_smb_account_password, false));//gp.settingSecurityReinitSmbAccountPasswordValue?"true":"false");

        setting_tag.setAttribute(SETTINGS_XML_TAG_MISC_EXIT_CLEANLY,
                getSettingParameterBoolean(c,R.string.settings_exit_clean, false));//gp.settingExitClean?"true":"false");
        setting_tag.setAttribute(SETTINGS_XML_TAG_MISC_SYNC_MESSAGE_SHOW_WITH_WORDWRAP,
                getSettingParameterBoolean(c,R.string.settings_sync_message_use_standard_text_view, false));//gp.settingSyncMessageUseStandardTextView?"true":"false");

        setting_tag.setAttribute(SETTINGS_XML_TAG_SCHEDULE_ENABLED, getSettingParameterBoolean(c, SCHEDULER_ENABLED_KEY, true));

        return setting_tag;
    }

    static private String getSettingParameterBoolean(Context c, int res_id, boolean default_value) {
        return getSettingParameterBoolean(c, c.getString(res_id), default_value);
    }
    static private String getSettingParameterBoolean(Context c, String key, boolean default_value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return "B"+(prefs.getBoolean(key, default_value)?"1":"0");
    }

    static private String getSettingParameterString(Context c, int res_id, String default_value) {
        return getSettingParameterString(c, c.getString(res_id), default_value);
    }
    static private String getSettingParameterString(Context c, String key, String default_value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return "S"+prefs.getString(key, default_value);
    }

    static private String getSettingParameterInt(Context c, int res_id, int default_value) {
        return getSettingParameterInt(c, c.getString(res_id), default_value);
    }
    static private String getSettingParameterInt(Context c, String key, int default_value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return "I"+prefs.getInt(key, default_value);
    }

    static private String getSettingParameterLong(Context c, int res_id, long default_value) {
        return getSettingParameterLong(c, c.getString(res_id), default_value);
    }
    static private String getSettingParameterLong(Context c, String key, long default_value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return "I"+prefs.getLong(key, default_value);
    }

    private static Element buildXmlTaskElement(Context c, Document main_document, SyncTaskItem item) {
        Element task_tag = main_document.createElement(SYNC_TASK_XML_TAG_TASK);
        task_tag.setAttribute(SYNC_TASK_XML_TAG_TASK_AUTO_TASK, item.isSyncTaskAuto()?"true":"false");
        task_tag.setAttribute(SYNC_TASK_XML_TAG_TASK_TEST_MODE, item.isSyncTestMode()?"true":"false");
        task_tag.setAttribute(SYNC_TASK_XML_TAG_TASK_POSITION, String.valueOf(item.getSyncTaskPosition()));
        task_tag.setAttribute(SYNC_TASK_XML_TAG_TASK_GROUP_NAME, item.getSyncTaskGroup());
        task_tag.setAttribute(SYNC_TASK_XML_TAG_TASK_LAST_SYNC_RESULT, String.valueOf(item.getLastSyncResult()));
        task_tag.setAttribute(SYNC_TASK_XML_TAG_TASK_LAST_SYNC_TIME, item.getLastSyncTime());
        task_tag.setAttribute(SYNC_TASK_XML_TAG_TASK_ERROR_MASTER, String.valueOf(item.getMasterFolderError()));
        task_tag.setAttribute(SYNC_TASK_XML_TAG_TASK_ERROR_TARGET, String.valueOf(item.getTargetFolderError()));

        task_tag.setAttribute(SYNC_TASK_XML_TAG_TASK_TYPE, item.getSyncTaskType());
        task_tag.setAttribute(SYNC_TASK_XML_TAG_TASK_NAME, item.getSyncTaskName());

        buildXmlOptionElement(c, main_document, task_tag, item);

        if (item.getDirFilter().size()>0)
            buildXmlFilterElement(c, main_document, task_tag, SYNC_TASK_XML_TAG_FILTER_DIRECTORY, item.getDirFilter());
        if (item.getFileFilter().size()>0)
            buildXmlFilterElement(c, main_document, task_tag, SYNC_TASK_XML_TAG_FILTER_FILE, item.getFileFilter());
        if (item.getSyncOptionWifiConnectedAccessPointWhiteList().size()>0)
            buildXmlFilterElement(c, main_document, task_tag, SYNC_TASK_XML_TAG_FILTER_SSID, item.getSyncOptionWifiConnectedAccessPointWhiteList());
        if (item.getSyncOptionWifiConnectedAddressWhiteList().size()>0)
            buildXmlFilterElement(c, main_document, task_tag, SYNC_TASK_XML_TAG_FILTER_IPADDR, item.getSyncOptionWifiConnectedAddressWhiteList());

        return task_tag;
    }

    private static void buildXmlOptionElement(Context c, Document main_document, Element task_tag, SyncTaskItem item) {
        Element option_tag = main_document.createElement(SYNC_TASK_XML_TAG_OPTION);

        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_ALLOW_GLOBAL_IP_ADDRESS, item.isSyncOptionSyncAllowGlobalIpAddress()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_CONFIIRM_OVERRIDE_OR_DELETE, item.isSyncConfirmOverrideOrDelete()?"true":"false");

        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_CONFIRM_NOT_EXIST_EXIF_DATE, item.isSyncOptionConfirmNotExistsExifDate()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_DELETE_EMPTY_MASTER_DIRECTORY_WHEN_MOVE, item.isSyncOptionMoveOnlyRemoveMasterDirectoryIfEmpty()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_DELETE_FIRST_WHEN_MIRROR, item.isSyncOptionDeleteFirstWhenMirror()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_DETECT_DIFFERENT_FILE_BY_SIZE, item.isSyncOptionDifferentFileBySize()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_DETECT_DIFFERENT_FILE_BY_SIZE_IF_GT_MASTER, item.isSyncDifferentFileSizeGreaterThanTagetFile()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_DETECT_DIFFERENT_FILE_BY_TIME, item.isSyncOptionDifferentFileByTime()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_DO_NOT_OVERRIDE_WHEN_TARGET_FILE_IS_NEWER_THAN_MASTER, item.isSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_DO_NOT_RESET_FILE_LAST_MODIFIED_TIME, item.isSyncDoNotResetFileLastModified()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_ALLOWABLE_TIME_FOR_DIFFERENT_FILE, String.valueOf(item.getSyncOptionDifferentFileAllowableTime()));
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_IGNORE_FILE_DIRECTORY_THAT_CONTAIN_UNUSABLE_CHARACTER, item.isSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters()?"true":"false");

        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_OVERRIDE_FILE_WHEN_COPY_OR_MOVE, item.isSyncOverrideCopyMoveFile()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_PERFORM_SYNC_WHEN_CHARGING, item.isSyncOptionSyncWhenCharging()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_PROCESS_ROOT_DIRECTORY_FILE, item.isSyncProcessRootDirFile()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_NETWORK_ERROR_RETRY_COUNT, item.getSyncOptionRetryCount());
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_SKIP_TASK_IF_ANOTHER_SSID_CONNECTED, item.isSyncOptionTaskSkipIfConnectAnotherWifiSsid()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_SMB_TARGET_FOLDER_USE_SMALL_BUFFER, item.isSyncOptionUseSmallIoBuffer()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_SYNC_EMPTY_DIRECTORY, item.isSyncOptionSyncEmptyDirectory()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_SYNC_HIDDEN_DIRECTORY, item.isSyncOptionSyncHiddenDirectory()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_SYNC_HIDDEN_FILE, item.isSyncOptionSyncHiddenFile()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_SYNC_SUB_DIRECTORY, item.isSyncOptionUseSmallIoBuffer()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_TARGET_USE_TAKEN_DATE_DIRECTORY_NAME_KEYWORD, item.isTargetUseTakenDateTimeToDirectoryNameKeyword()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_TARGET_USE_TAKEN_DATE_FILE_NAME_KEYWORD, item.isTargetUseTakenDateTimeToDirectoryNameKeyword()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_TWO_WAY_CONFLICT_FILE_RULE, item.getSyncTwoWayConflictFileRule());
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_TWO_WAY_KEEP_CONFLICT_FILE, item.isSyncTwoWayKeepConflictFile()?"true":"false");
        option_tag.setAttribute(SYNC_TASK_XML_TAG_OPTION_WIFI_STATUS, item.getSyncOptionWifiStatusOption());

        task_tag.appendChild(option_tag);
    }

    private static void buildXmlFilterElement(Context c, Document main_document, Element task_tag, String filter_type, ArrayList<String> filter_list) {
        if (filter_list.size()>0) {
            Element user_file_filter_tag = main_document.createElement(filter_type);
            for (String ff_item:filter_list) {
                Element user_file_filter_entry_tag = main_document.createElement(SYNC_TASK_XML_TAG_FILTER_ITEM);
                user_file_filter_entry_tag.setAttribute(SYNC_TASK_XML_TAG_FILTER_INCLUDE,ff_item.startsWith("I")?"true":"false");
                user_file_filter_entry_tag.setAttribute(SYNC_TASK_XML_TAG_FILTER_VALUE, ff_item.substring(1));
                user_file_filter_tag.appendChild(user_file_filter_entry_tag);
            }
            task_tag.appendChild(user_file_filter_tag);
        }
    }

    private final static String SYNC3_FOLDER_TYPE_LOCAL = "LOCAL";
    private final static String SYNC3_FOLDER_TYPE_SMB = "SMB";
    private final static String SYNC3_FOLDER_TYPE_ZIP = "ZIP";

    static private String convertFolderType(String folder_type) {
        if (folder_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            return SYNC3_FOLDER_TYPE_LOCAL;
        } else if (folder_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            return SYNC3_FOLDER_TYPE_SMB;
        } else if (folder_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            return SYNC3_FOLDER_TYPE_LOCAL;
        } else if (folder_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
            return SYNC3_FOLDER_TYPE_LOCAL;
        } else if (folder_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
            return SYNC3_FOLDER_TYPE_ZIP;
        }
        return "UNKNOWN";
    }

    private static Element buildXmlMasterElement(Context c, Document main_document, SyncTaskItem item, int enc_mode, EncryptUtil.CipherParms cp_int) {
        Element master_tag = main_document.createElement(SYNC_TASK_XML_TAG_MASTER);
        master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_TYPE, convertFolderType(item.getMasterFolderType()));
        master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_DIRECTORY, item.getMasterDirectoryName());

        if (item.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_DOMAIN, item.getMasterSmbDomain());
            if (!item.getMasterSmbUserName().equals("")) {
                master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_ENCRYPTED_ACCOUNT_NAME, "invalid");
            }
            if (!item.getMasterSmbPassword().equals("")) {
                master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_ENCRYPTED_ACCOUNT_PASSWORD, "invalid");
            }
            master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_NAME, item.getMasterSmbHostName());
            master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_ADDR, item.getMasterSmbAddr());
            master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_IPC_SIGNIN_ENFORCED, item.isMasterSmbIpcSigningEnforced()?"true":"false");
            master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_PORT, item.getMasterSmbPort());
            master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_PROTOCOL, item.getMasterSmbProtocol());
            master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_SHARE_NAME, item.getMasterSmbShareName());
            master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_USE_SMB2_NEGO, item.isMasterSmbUseSmb2Negotiation()?"true":"false");
        } else {
            master_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_UUID, getUuidFromFolderType(c, item));
        }

        return master_tag;
    }

    static private String getUuidFromFolderType(Context c, SyncTaskItem sti) {
        SafManager mgr=new SafManager(c, false);
        String uuid="";
        if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            uuid="primary";
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            String fp=mgr.getSdcardRootPath();
            if (fp.equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) uuid="UNKN-OWN0";
            String[] fp_array=fp.split("/");
            uuid=fp_array[2];
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
            String fp=mgr.getUsbRootPath();
            if (fp.equals(SafManager.UNKNOWN_USB_DIRECTORY)) uuid="UNKN-OWNX";
            String[] fp_array=fp.split("/");
            uuid=fp_array[2];
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
            if (sti.isTargetZipUseExternalSdcard()) {
                String fp=mgr.getSdcardRootPath();
                if (fp.equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) uuid="UNKN-OWNX";
                String[] fp_array=fp.split("/");
                uuid=fp_array[2];
            } else {
                uuid="primary";
            }
        }
        return uuid;
    }

    private static Element buildXmlTargetElement(Context c, Document main_document, SyncTaskItem item, int enc_mode, EncryptUtil.CipherParms cp_int) {
        Element target_tag = main_document.createElement(SYNC_TASK_XML_TAG_TARGET);
        target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_TYPE, convertFolderType(item.getTargetFolderType()));
        target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_DIRECTORY, item.getTargetDirectoryName());

        if (item.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_DOMAIN, item.getTargetSmbDomain());
            if (!item.getTargetSmbUserName().equals("")) {
                target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_ENCRYPTED_ACCOUNT_NAME, "invalid");
            }
            if (!item.getTargetSmbPassword().equals("")) {
                target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_ENCRYPTED_ACCOUNT_PASSWORD, "invalid");
            }
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_ADDR, item.getTargetSmbAddr());
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_NAME, item.getTargetSmbHostName());
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_IPC_SIGNIN_ENFORCED, item.isTargetSmbIpcSigningEnforced()?"true":"false");
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_PORT, item.getTargetSmbPort());
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_PROTOCOL, item.getTargetSmbProtocol());
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_SHARE_NAME, item.getTargetSmbShareName());
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_SMB_SERVER_USE_SMB2_NEGO, item.isTargetSmbUseSmb2Negotiation()?"true":"false");
        } else {
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_UUID, getUuidFromFolderType(c, item));

            target_tag.setAttribute(SYNC_TASK_XML_TAG_ARCHIVE_RENAME_FILE_TEMPLATE, item.getArchiveRenameFileTemplate());
            target_tag.setAttribute(SYNC_TASK_XML_TAG_ARCHIVE_RETENTION_PERIOD, String.valueOf(item.getArchiveRetentionPeriod()));
            target_tag.setAttribute(SYNC_TASK_XML_TAG_ARCHIVE_SUFFIX_OPTION, item.getArchiveSuffixOption());

            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_ZIP_COMP_LEVEL, item.getTargetZipCompressionLevel());
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_ZIP_COMP_METHOD, item.getTargetZipCompressionMethod());
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_ZIP_ENCRYPT_METHOD, item.getTargetZipEncryptMethod());
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_ZIP_FILE_NAME_ENCODING, item.getTargetZipFileNameEncoding());
            target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_ZIP_OUTPUT_FILE_NAME, item.getTargetZipOutputFileName());
            if (!item.getTargetZipPassword().equals("")) {
                target_tag.setAttribute(SYNC_TASK_XML_TAG_FOLDER_ZIP_ENCRYPTED_OUTPUT_FILE_PASSWORD, "invalid");
            }
        }

        return target_tag;
    }

}
