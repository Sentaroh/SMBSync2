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
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.sentaroh.android.Utilities.Dialog.MessageDialogAppFragment;
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
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_BLACK;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_LIGHT;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_STANDARD;
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

    private static String mCurrentScreenTheme=SMBSYNC2_SCREEN_THEME_STANDARD;

    private CommonUtilities mUtil = null;
//    private CommonDialog mCommonDlg = null;

//	private GlobalParameters mGp=null;

    @Override
    protected boolean isValidFragment(String fragmentName) {
        // 使用できる Fragment か確認する

        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = getApplicationContext();
//        mGp = (GlobalParameters) getApplicationContext();//getApplication();
        mGp= GlobalWorkArea.getGlobalParameters(mContext);
        SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        mCurrentScreenTheme=shared_pref.getString(getString(R.string.settings_screen_theme), SMBSYNC2_SCREEN_THEME_STANDARD);
        if (mCurrentScreenTheme.equals(SMBSYNC2_SCREEN_THEME_STANDARD)) setTheme(R.style.Main);
        else if (mCurrentScreenTheme.equals(SMBSYNC2_SCREEN_THEME_LIGHT)) setTheme(R.style.MainLight);
        else if (mCurrentScreenTheme.equals(SMBSYNC2_SCREEN_THEME_BLACK)) setTheme(R.style.MainBlack);

//        setTheme(mGp.applicationTheme);
        super.onCreate(savedInstanceState);

        mPrefActivity = ActivitySettings.this;
        if (mUtil == null) mUtil = new CommonUtilities(mPrefActivity, "SettingsActivity", mGp, null);
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

    @Override
    public void onResume() {
        super.onResume();
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
//		setTitle(R.string.settings_main_title);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        loadHeadersFromResource(R.xml.settings_frag, target);
    }

    @Override
    public boolean onIsMultiPane() {
        mContext =getApplicationContext();
//        mGp = (GlobalParameters) getApplication();
//        mGp = (GlobalParameters) getApplicationContext();//getApplication();
        mGp= GlobalWorkArea.getGlobalParameters(mContext);

//    	mPrefActivity=this;
        mUtil = new CommonUtilities(mPrefActivity, "SettingsActivity", mGp, null);
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        return isTablet(mContext);

    }

    public static boolean isTablet(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final int pixels = Math.min(metrics.heightPixels, metrics.widthPixels);
        boolean sz_mp=pixels >= 1200;
        int orientation = context.getResources().getConfiguration().orientation;
        boolean sc_or= orientation == Configuration.ORIENTATION_LANDSCAPE;

        return sz_mp||sc_or;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
    }

    @Override
    final public void onStop() {
        super.onStop();
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
    }

    @Override
    final public void onDestroy() {
        super.onDestroy();
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
    }

    private static void checkSettingValue(CommonUtilities ut, SharedPreferences shared_pref, String key_string, FragmentManager fm) {
        if (!checkSyncSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext))
            if (!checkUiSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext, fm))
                if (!checkLogSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext))
                    if (!checkSmbSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext))
                        if (!checkMiscSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext, fm))
                            if (!checkSecuritySettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext, fm))
                            checkOtherSettings(ut, mPrefFrag.findPreference(key_string), shared_pref, key_string, mContext);
    }

    @SuppressLint("NewApi")
    private static boolean checkSyncSettings(CommonUtilities ut, Preference pref_key,
                                             SharedPreferences shared_pref, String key_string, Context c) {
        boolean isChecked = false;
        if (key_string.equals(c.getString(R.string.settings_error_option))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_force_screen_on_while_sync))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_wifi_lock))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_sync_history_log))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_suppress_warning_location_service_disabled))) {
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

    private static boolean checkUiSettings(
            CommonUtilities ut, Preference pref_key, SharedPreferences shared_pref, String key_string, Context c, FragmentManager fm) {
        boolean isChecked = false;

        if (key_string.equals(c.getString(R.string.settings_playback_ringtone_when_sync_ended))) {
            isChecked = true;
            Preference rv = mPrefFrag.findPreference(c.getString(R.string.settings_playback_ringtone_volume));
            if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS)) {
                pref_key.setSummary(c.getString(R.string.settings_playback_ringtone_when_sync_ended_summary_always));
                rv.setEnabled(true);
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_RINGTONE_NOTIFICATION_ERROR)) {
                pref_key.setSummary(c.getString(R.string.settings_playback_ringtone_when_sync_ended_summary_error));
                rv.setEnabled(true);
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_RINGTONE_NOTIFICATION_SUCCESS)) {
                pref_key.setSummary(c.getString(R.string.settings_playback_ringtone_when_sync_ended_summary_success));
                rv.setEnabled(true);
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_RINGTONE_NOTIFICATION_NO)) {
                pref_key.setSummary(c.getString(R.string.settings_playback_ringtone_when_sync_ended_summary_no));
                rv.setEnabled(false);
            }
        } else if (key_string.equals(c.getString(R.string.settings_vibrate_when_sync_ended))) {
            isChecked = true;
            if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS)) {
                pref_key.setSummary(c.getString(R.string.settings_vibrate_when_sync_ended_summary_always));
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ERROR)) {
                pref_key.setSummary(c.getString(R.string.settings_vibrate_when_sync_ended_summary_error));
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_SUCCESS)) {
                pref_key.setSummary(c.getString(R.string.settings_vibrate_when_sync_ended_summary_success));
            } else if (shared_pref.getString(key_string, "0").equals(SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_NO)) {
                pref_key.setSummary(c.getString(R.string.settings_vibrate_when_sync_ended_summary_no));
            }
        } else if (key_string.equals(c.getString(R.string.settings_notification_message_when_sync_ended))) {
            isChecked = true;
            if (shared_pref.getString(key_string, "1").equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS)) {
                pref_key.setSummary(c.getString(R.string.settings_notification_message_when_sync_ended_summary_always));
            } else if (shared_pref.getString(key_string, "1").equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ERROR)) {
                pref_key.setSummary(c.getString(R.string.settings_notification_message_when_sync_ended_summary_error));
            } else if (shared_pref.getString(key_string, "1").equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_SUCCESS)) {
                pref_key.setSummary(c.getString(R.string.settings_notification_message_when_sync_ended_summary_success));
            } else if (shared_pref.getString(key_string, "1").equals(SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_NO)) {
                pref_key.setSummary(c.getString(R.string.settings_notification_message_when_sync_ended_summary_no));
            }
        } else if (key_string.equals(c.getString(R.string.settings_suppress_warning_app_specific_dir))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_playback_ringtone_volume))) {
            isChecked = true;
            int vol = shared_pref.getInt(key_string, 100);
            pref_key.setSummary(
                    String.format(c.getString(R.string.settings_playback_ringtone_volume_summary), vol));
            if (mInitVolume != vol) playBackDefaultNotification(c, fm, vol);
        } else if (key_string.equals(c.getString(R.string.settings_screen_theme))) {
            isChecked = true;
            String tid=shared_pref.getString(key_string, "0");
            String[] wl_label = c.getResources().getStringArray(R.array.settings_screen_theme_list_entries);
            String sum_msg = wl_label[Integer.parseInt(tid)];
            pref_key.setSummary(sum_msg);
            if (!mCurrentScreenTheme.equals(tid)) {
                if (tid.equals(SMBSYNC2_SCREEN_THEME_STANDARD)) mPrefActivity.setTheme(R.style.Main);
                else if (tid.equals(SMBSYNC2_SCREEN_THEME_LIGHT)) mPrefActivity.setTheme(R.style.MainLight);
                else if (tid.equals(SMBSYNC2_SCREEN_THEME_BLACK)) mPrefActivity.setTheme(R.style.MainBlack);
                mCurrentScreenTheme=tid;
                mPrefActivity.finish();
                Intent intent = new Intent(mPrefActivity, ActivitySettings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                mPrefActivity.startActivity(intent);
            }
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

    private static void playBackDefaultNotification(Context c, FragmentManager fm, int vol) {
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
                MessageDialogAppFragment cd=MessageDialogAppFragment.newInstance(
                        false, "E",mContext.getString(R.string.settings_playback_ringtone_volume_disabled),"");
                cd.showDialog(fm, cd, null);
            }
        }
    }

    private static boolean checkMiscSettings(CommonUtilities ut,
                                             Preference pref_key, SharedPreferences shared_pref, String key_string, Context c, final FragmentManager fm) {
        boolean isChecked = false;

        if (key_string.equals(c.getString(R.string.settings_exit_clean))) {
            isChecked = true;
        } else if (key_string.equals(c.getString(R.string.settings_sync_message_use_standard_text_view))) {
            isChecked = true;
        }
        return isChecked;
    }

    private static boolean checkSecuritySettings(CommonUtilities ut,
                                             Preference pref_key, SharedPreferences shared_pref, String key_string, Context c, final FragmentManager fm) {
        boolean isChecked = false;

        String hv=ApplicationPasswordUtil.getApplicationPasswordHashValue(shared_pref);

        if (key_string.equals(c.getString(R.string.settings_security_application_password))) {
            isChecked = true;
            String contents_string="";
            if (hv.equals("")) {
                contents_string=mContext.getString(R.string.settings_security_application_password_not_created);
            } else  {
                contents_string="-"+mContext.getString(R.string.settings_security_application_password_created);
                if (shared_pref.getBoolean(mContext.getString(R.string.settings_security_use_auth_timeout), true))
                    contents_string+="\n-"+mContext.getString(R.string.settings_security_use_auth_timeout_title);
                if (shared_pref.getBoolean(mContext.getString(R.string.settings_security_application_password_use_app_startup), false))
                    contents_string+="\n-"+mContext.getString(R.string.settings_security_application_password_use_app_startup_title);
                if (shared_pref.getBoolean(mContext.getString(R.string.settings_security_application_password_use_edit_task), false))
                    contents_string+="\n-"+mContext.getString(R.string.settings_security_application_password_use_edit_task_title);
                if (shared_pref.getBoolean(mContext.getString(R.string.settings_security_application_password_use_export_task), false))
                    contents_string+="\n-"+mContext.getString(R.string.settings_security_application_password_use_export_task_title);
                if (shared_pref.getBoolean(mContext.getString(R.string.settings_security_init_smb_account_password), false))
                    contents_string+="\n-"+mContext.getString(R.string.settings_security_init_smb_account_password_title);
            }
            pref_key.setSummary(contents_string);
        }

        return isChecked;
    }

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
//        } else if (key_string.equals("settings_smb_set_default_value")) {
//            isChecked = true;
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
                        checkSettingValue(mUtil, shared_pref, key_string, getFragmentManager());
                    }
                };
        private CommonUtilities mUtil = null;

        @SuppressLint("NewApi")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag = SettingsSync.this;
            mUtil = new CommonUtilities(mContext, "SettingsSync", mGp, null);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_sync);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

            if (!LocalMountPoint.isExternalStorageAvailable()) {
                if (findPreference(getString(R.string.settings_mgt_dir))!=null) findPreference(getString(R.string.settings_mgt_dir)).setEnabled(false);
            }

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_error_option), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_wifi_lock), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_sync_history_log), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_no_compress_file_type), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_mgt_dir), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_force_screen_on_while_sync), getFragmentManager());
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
                        checkSettingValue(mUtil, shared_pref, key_string, getFragmentManager());
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag = SettingsLog.this;
            mUtil = new CommonUtilities(mContext, "SettingsLog", mGp, null);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_log);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_log_option), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_put_logcat_option), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_log_file_max_count), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_log_level), getFragmentManager());
        }

        @Override
        public void onStart() {
            super.onStart();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(listenerAfterHc);
            getActivity().setTitle(R.string.settings_log_title);
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

    public static class SettingsMisc extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                        checkSettingValue(mUtil, shared_pref, key_string, getFragmentManager());
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag = SettingsMisc.this;
            mUtil = new CommonUtilities(mContext, "SettingsMisc", mGp, null);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_misc);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

//            shared_pref.edit().putBoolean(getString(R.string.settings_exit_clean), true).commit();
//            findPreference(getString(R.string.settings_exit_clean).toString()).setEnabled(false);
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_exit_clean), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_sync_message_use_standard_text_view), getFragmentManager());
        }

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
                        checkSettingValue(mUtil, shared_pref, key_string, getFragmentManager());
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag = SettingsSmb.this;
            mUtil = new CommonUtilities(mContext, "SettingsSmb", mGp, null);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_smb);


            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_use_extended_security), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_lm_compatibility), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_client_response_timeout), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_disable_plain_text_passwords), getFragmentManager());

            Preference button = (Preference)getPreferenceManager().findPreference("settings_smb_set_default_value");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    shared_pref.edit().putBoolean(mContext.getString(R.string.settings_smb_use_extended_security),true).commit();
                    shared_pref.edit().putBoolean(mContext.getString(R.string.settings_smb_disable_plain_text_passwords),false).commit();
                    shared_pref.edit().putString(mContext.getString(R.string.settings_smb_lm_compatibility),"3").commit();
                    shared_pref.edit().putString(mContext.getString(R.string.settings_smb_client_response_timeout),"30000").commit();
                    return false;
                }
            });

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

        @Override
        public void onStop() {
            super.onStop();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(listenerAfterHc);
        }

    }

    private static int mInitVolume = 100;

    public static class SettingsUi extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                        checkSettingValue(mUtil, shared_pref, key_string, getFragmentManager());
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag = SettingsUi.this;
            mUtil = new CommonUtilities(mContext, "SettingsUi", mGp, null);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_ui);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

            mInitVolume = shared_pref.getInt(getString(R.string.settings_playback_ringtone_volume), 100);

            mCurrentScreenTheme=shared_pref.getString(getString(R.string.settings_screen_theme), SMBSYNC2_SCREEN_THEME_STANDARD);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_notification_message_when_sync_ended), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_playback_ringtone_when_sync_ended), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_playback_ringtone_volume), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_vibrate_when_sync_ended), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_screen_theme), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_device_orientation_portrait), getFragmentManager());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_dim_screen_on_while_sync), getFragmentManager());

        }

        @Override
        public void onStart() {
            super.onStart();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(listenerAfterHc);
            getActivity().setTitle(R.string.settings_ui_title);
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

    public static class SettingsSecurity extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                        checkSettingValue(mUtil, shared_pref, key_string, getFragmentManager());
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == 0) {
                if (resultCode == RESULT_OK) {
                    SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    checkSettingValue(mUtil, shared_pref, getString(R.string.settings_security_application_password), getFragmentManager());
                }
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefFrag = SettingsSecurity.this;
            mUtil = new CommonUtilities(mContext, "SettingsSecurity", mGp, null);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_security);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(mContext);

            mInitVolume = shared_pref.getInt(getString(R.string.settings_playback_ringtone_volume), 100);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_security_application_password), getFragmentManager());

            Preference button = (Preference)getPreferenceManager().findPreference(getString(R.string.settings_security_application_password));
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent in=new Intent(mContext, ActivityPasswordSettings.class);
                    startActivityForResult(in, 0);
                    return false;
                }
            });
        }

        @Override
        public void onStart() {
            super.onStart();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(listenerAfterHc);
            getActivity().setTitle(R.string.settings_ui_title);
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

}