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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.Toast;

import com.sentaroh.android.Utilities.LocalMountPoint;

import java.util.List;

import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ERROR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_NO;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_SUCCESS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_ERROR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_NO;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_SUCCESS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ERROR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_NO;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_SUCCESS;

public class ActivitySettings extends PreferenceActivity {
    private static Context mContext = null;
    private static PreferenceFragment mPrefFrag = null;

    @SuppressWarnings("unused")
    private static ActivitySettings mPrefActivity = null;

    private static GlobalParameters mGp = null;

    private CommonUtilities mUtil = null;

//	private GlobalParameters mGp=null;

    @Override
    protected boolean isValidFragment(String fragmentName) {
        // 使用できる Fragment か確認する

        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = this;
//        mGp = (GlobalParameters) getApplicationContext();//getApplication();
        mGp= GlobalWorkArea.getGlobalParameters(mContext);

        setTheme(mGp.applicationTheme);
        super.onCreate(savedInstanceState);
        mPrefActivity = this;
        if (mUtil == null) mUtil = new CommonUtilities(this, "SettingsActivity", mGp);
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        if (mGp.settingFixDeviceOrientationToPortrait)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
    }

    ;

    @Override
    public void onResume() {
        super.onResume();
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
//		setTitle(R.string.settings_main_title);
    }

    ;

    @Override
    public void onBuildHeaders(List<Header> target) {
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        loadHeadersFromResource(R.xml.settings_frag, target);
    }

    @Override
    public boolean onIsMultiPane() {
        mContext = this;
//        mGp = (GlobalParameters) getApplication();
//        mGp = (GlobalParameters) getApplicationContext();//getApplication();
        mGp= GlobalWorkArea.getGlobalParameters(mContext);

//    	mPrefActivity=this;
        mUtil = new CommonUtilities(this, "SettingsActivity", mGp);
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        return true;
    }

    ;

    @Override
    protected void onPause() {
        super.onPause();
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
    }

    ;

    @Override
    final public void onStop() {
        super.onStop();
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
    }

    ;

    @Override
    final public void onDestroy() {
        super.onDestroy();
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
    }

    ;

    private static void checkSettingValue(CommonUtilities ut, SharedPreferences shared_pref, String key_string) {
        if (!checkSyncSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext))
            if (!checkUiSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext))
                if (!checkLogSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext))
                    if (!checkSmbSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext))
                        if (!checkMiscSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext))
                            checkOtherSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext);
    }

    ;

    @SuppressLint("NewApi")
    private static boolean checkSyncSettings(CommonUtilities ut, Preference pref_key,
                                             SharedPreferences shared_pref, String key_string, Context c) {
        boolean isChecked = false;
        if (key_string.equals(c.getString(R.string.settings_error_option))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_wifi_lock))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_sync_history_log))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_no_compress_file_type))) {
            isChecked = true;
            if (shared_pref.getString(key_string, "").equals("")) {
                shared_pref.edit().putString(key_string, GlobalParameters.DEFAULT_NOCOMPRESS_FILE_TYPE).commit();
            }
            pref_key.setSummary(shared_pref.getString(key_string, GlobalParameters.DEFAULT_NOCOMPRESS_FILE_TYPE));
        }
        return isChecked;
    }

    private static boolean checkUiSettings(CommonUtilities ut, Preference pref_key,
                                           SharedPreferences shared_pref, String key_string, Context c) {
        boolean isChecked = false;

        if (key_string.equals(c.getString(R.string.settings_playback_ringtone_when_sync_ended))) {
            isChecked = true;
            Preference rv = mPrefFrag.findPreference(c.getString(R.string.settings_playback_ringtone_volume));
            if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_playback_ringtone_when_sync_ended_summary_always));
                rv.setEnabled(true);
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_RINGTONE_NOTIFICATION_ERROR)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_playback_ringtone_when_sync_ended_summary_error));
                rv.setEnabled(true);
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_RINGTONE_NOTIFICATION_SUCCESS)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_playback_ringtone_when_sync_ended_summary_success));
                rv.setEnabled(true);
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_RINGTONE_NOTIFICATION_NO)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_playback_ringtone_when_sync_ended_summary_no));
                rv.setEnabled(false);
            }
        } else if (key_string.equals(c.getString(R.string.settings_vibrate_when_sync_ended))) {
            isChecked = true;
            if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_vibrate_when_sync_ended_summary_always));
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ERROR)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_vibrate_when_sync_ended_summary_error));
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_SUCCESS)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_vibrate_when_sync_ended_summary_success));
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_NO)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_vibrate_when_sync_ended_summary_no));
            }
        } else if (key_string.equals(c.getString(R.string.settings_notification_message_when_sync_ended))) {
            isChecked = true;
            if (shared_pref.getString(key_string, "1").equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_notification_message_when_sync_ended_summary_always));
            } else if (shared_pref.getString(key_string, "1").equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ERROR)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_notification_message_when_sync_ended_summary_error));
            } else if (shared_pref.getString(key_string, "1").equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_SUCCESS)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_notification_message_when_sync_ended_summary_success));
            } else if (shared_pref.getString(key_string, "1").equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_NO)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_notification_message_when_sync_ended_summary_no));
            }
        } else if (key_string.equals(c.getString(R.string.settings_suppress_warning_app_specific_dir))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_playback_ringtone_volume))) {
            isChecked = true;
            int vol = shared_pref.getInt(key_string, 100);
            pref_key.setSummary(
                    String.format(c.getString(R.string.settings_playback_ringtone_volume_summary), vol));
            if (mInitVolume != vol) playBackDefaultNotification(vol);
        } else if (key_string.equals(c.getString(R.string.settings_use_light_theme))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_device_orientation_portrait))) {
            isChecked = true;
//			boolean orientation=shared_pref.getBoolean(key_string,false);
//            if (orientation) mPrefActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            else mPrefActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else if (key_string.equals(c.getString(R.string.settings_dim_screen_on_while_sync))) {
            isChecked = true;
        }

        return isChecked;
    }

    ;

    private static void playBackDefaultNotification(int vol) {
        float volume = (float) vol / 100.0f;
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (uri != null) {
            final MediaPlayer player = MediaPlayer.create(mGp.appContext, uri);
            if (player != null) {
                player.setVolume(volume, volume);
                if (player != null) {
                    Thread th = new Thread() {
                        @Override
                        public void run() {
                            int dur = player.getDuration();
                            player.start();
                            SystemClock.sleep(dur + 10);
                            player.stop();
                            player.reset();
                            player.release();
                        }
                    };
                    th.setPriority(Thread.MAX_PRIORITY);
                    th.start();
                }
            } else {
                Toast toast = Toast.makeText(mContext,
                        mContext.getString(R.string.settings_playback_ringtone_volume_disabled), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 200);
                toast.show();
            }
        }
    }

    ;


    private static boolean checkMiscSettings(CommonUtilities ut,
                                             Preference pref_key, SharedPreferences shared_pref, String key_string, Context c) {
        boolean isChecked = false;

        if (key_string.equals(c.getString(R.string.settings_exit_clean))) {
            isChecked = true;
            if (shared_pref.getBoolean(key_string, true)) {
                pref_key
                        .setSummary(c.getString(R.string.settings_exit_clean_summary_ena));
            } else {
                pref_key
                        .setSummary(c.getString(R.string.settings_exit_clean_summary_dis));
            }
        }

        return isChecked;
    }

    ;

    private static boolean checkLogSettings(CommonUtilities ut,
                                            Preference pref_key, SharedPreferences shared_pref, String key_string, Context c) {
        boolean isChecked = false;

        if (key_string.equals(c.getString(R.string.settings_log_option))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_put_logcat_option))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_log_level))) {
            isChecked = true;
            String[] wl_label = c.getResources().getStringArray(R.array.settings_log_level_list_entries);
            String sum_msg = wl_label[Integer.parseInt(shared_pref.getString(key_string, "0"))];
            pref_key.setSummary(sum_msg);
        } else if (key_string.equals(c.getString(R.string.settings_log_file_max_count))) {
            isChecked = true;
            pref_key.setSummary(String.format(c.getString(R.string.settings_log_file_max_count_summary),
                    shared_pref.getString(key_string, "10")));
        }

        return isChecked;
    }

    private static boolean checkSmbSettings(CommonUtilities ut,
                                            Preference pref_key, SharedPreferences shared_pref, String key_string, Context c) {
        boolean isChecked = false;
        if (key_string.equals(c.getString(R.string.settings_smb_use_extended_security))) {
            isChecked = true;
        } else if (key_string.equals("settings_smb_set_default_value")) {
            if (shared_pref.getBoolean("settings_smb_set_default_value",false)) {
                shared_pref.edit().putBoolean("settings_smb_set_default_value",false).commit();
                shared_pref.edit().putBoolean(c.getString(R.string.settings_smb_use_extended_security),true).commit();
                shared_pref.edit().putBoolean(c.getString(R.string.settings_smb_disable_plain_text_passwords),false).commit();
                shared_pref.edit().putString(c.getString(R.string.settings_smb_lm_compatibility),"3").commit();
                shared_pref.edit().putString(c.getString(R.string.settings_smb_client_response_timeout),"30000").commit();

                mPrefFrag.findPreference("settings_smb_set_default_value").setEnabled(false);
                mPrefFrag.findPreference(c.getString(R.string.settings_smb_use_extended_security).toString()).setEnabled(false);
                mPrefFrag.findPreference(c.getString(R.string.settings_smb_disable_plain_text_passwords).toString()).setEnabled(false);
                mPrefFrag.findPreference(c.getString(R.string.settings_smb_lm_compatibility).toString()).setEnabled(false);
                mPrefFrag.findPreference(c.getString(R.string.settings_smb_client_response_timeout).toString()).setEnabled(false);
            }
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_smb_disable_plain_text_passwords))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_smb_lm_compatibility))) {
            String lmc=shared_pref.getString(c.getString(R.string.settings_smb_lm_compatibility),"3");
            if (lmc.equals("3") || lmc.equals("4")) {
                mPrefFrag.findPreference(c.getString(R.string.settings_smb_use_extended_security).toString()).setEnabled(false);
            } else {
                mPrefFrag.findPreference(c.getString(R.string.settings_smb_use_extended_security).toString()).setEnabled(true);
            }
            pref_key.setSummary(lmc);
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_smb_client_response_timeout))) {
            pref_key.setSummary(shared_pref.getString(key_string, "30000")+" Millis");
            isChecked = true;
        }

        return isChecked;
    }

    private static boolean checkOtherSettings(CommonUtilities ut,
                                              Preference pref_key, SharedPreferences shared_pref, String key_string, Context c) {
        boolean isChecked = true;
        if (pref_key != null) {
            pref_key.setSummary(
                    c.getString(R.string.settings_default_current_setting) +
                            shared_pref.getString(key_string, "0"));
        } else {
            if (mGp.settingDebugLevel > 0)
                ut.addDebugMsg(1, "I", "checkOtherSettings Key not found key=" + key_string);
        }
        return isChecked;
    }

    public static class SettingsSync extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                        checkSettingValue(mUtil, shared_pref, key_string);
                    }
                };
        private CommonUtilities mUtil = null;

        @SuppressLint("NewApi")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag = this;
            mUtil = new CommonUtilities(mContext, "SettingsSync", mGp);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_sync);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_error_option));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_wifi_lock));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_sync_history_log));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_no_compress_file_type));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_mgt_dir));
        }

        @Override
        public void onStart() {
            super.onStart();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(listenerAfterHc);
            getActivity().setTitle(R.string.settings_sync_title);
        }

        @Override
        public void onStop() {
            super.onStop();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(listenerAfterHc);
        }

    }

    public static class SettingsLog extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                        checkSettingValue(mUtil, shared_pref, key_string);
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag = this;
            mUtil = new CommonUtilities(mContext, "SettingsLog", mGp);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_log);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

            if (!LocalMountPoint.isExternalStorageAvailable()) {
                findPreference(getString(R.string.settings_mgt_dir).toString())
                        .setEnabled(false);
            }

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_log_option));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_put_logcat_option));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_log_file_max_count));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_log_level));
        }

        ;

        @Override
        public void onStart() {
            super.onStart();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(listenerAfterHc);
            getActivity().setTitle(R.string.settings_log_title);
        }

        ;

        @Override
        public void onStop() {
            super.onStop();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(listenerAfterHc);
        }

        ;
    }

    ;

    public static class SettingsMisc extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                        checkSettingValue(mUtil, shared_pref, key_string);
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag = this;
            mUtil = new CommonUtilities(mContext, "SettingsMisc", mGp);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_misc);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

            shared_pref.edit().putBoolean(getString(R.string.settings_exit_clean), true).commit();
            findPreference(getString(R.string.settings_exit_clean).toString()).setEnabled(false);
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_exit_clean));
        }

        ;

        @Override
        public void onStart() {
            super.onStart();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(listenerAfterHc);
            getActivity().setTitle(R.string.settings_misc_title);
        }

        ;

        @Override
        public void onStop() {
            super.onStop();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(listenerAfterHc);
        }

        ;
    }

    public static class SettingsSmb extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                        checkSettingValue(mUtil, shared_pref, key_string);
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag = this;
            mUtil = new CommonUtilities(mContext, "SettingsSmb", mGp);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_smb);


            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_use_extended_security));
//            findPreference(getString(R.string.settings_smb_use_extended_security).toString()).setEnabled(false);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_lm_compatibility));
//            findPreference(getString(R.string.settings_smb_lm_compatibility).toString()).setEnabled(false);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_client_response_timeout));
//            findPreference(getString(R.string.settings_smb_client_reponse_timeout).toString()).setEnabled(false);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_disable_plain_text_passwords));
            checkSettingValue(mUtil, shared_pref, "settings_smb_set_default_value");
        }

        @Override
        public void onStart() {
            super.onStart();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(listenerAfterHc);
            getActivity().setTitle(R.string.settings_smb_title);
        }

        ;

        @Override
        public void onStop() {
            super.onStop();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(listenerAfterHc);
        }

        ;
    }

    ;

    private static int mInitVolume = 100;

    public static class SettingsUi extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                        checkSettingValue(mUtil, shared_pref, key_string);
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag = this;
            mUtil = new CommonUtilities(mContext, "SettingsUi", mGp);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_ui);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

            mInitVolume = shared_pref.getInt(getString(R.string.settings_playback_ringtone_volume), 100);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_notification_message_when_sync_ended));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_playback_ringtone_when_sync_ended));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_playback_ringtone_volume));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_vibrate_when_sync_ended));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_use_light_theme));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_device_orientation_portrait));
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_dim_screen_on_while_sync));

        }

        ;

        @Override
        public void onStart() {
            super.onStart();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(listenerAfterHc);
            getActivity().setTitle(R.string.settings_ui_title);
        }

        ;

        @Override
        public void onStop() {
            super.onStop();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(listenerAfterHc);
        }

        ;
    }

    ;
}