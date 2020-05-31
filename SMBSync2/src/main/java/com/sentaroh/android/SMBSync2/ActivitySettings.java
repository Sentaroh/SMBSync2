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
import android.util.Log;

import com.sentaroh.android.Utilities.Dialog.MessageDialogAppFragment;
import com.sentaroh.android.Utilities.LocalMountPoint;

import java.util.List;
import java.util.Locale;

import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ALWAYS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_ERROR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_NO;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_NOTIFICATION_MESSAGE_WHEN_SYNC_ENDED_SUCCESS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_ALWAYS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_ERROR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_NO;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_RINGTONE_NOTIFICATION_SUCCESS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_BLACK;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_LIGHT;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_SCREEN_THEME_STANDARD;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ALWAYS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_ERROR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_NO;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_VIBRATE_WHEN_SYNC_ENDED_SUCCESS;

public class ActivitySettings extends PreferenceActivity {
    private static GlobalParameters mGp = null;

    private static String mCurrentScreenTheme=SMBSYNC2_SCREEN_THEME_STANDARD;
    private static String mCurrentThemeLangaue=SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM;

    private CommonUtilities mUtil = null;

    @Override
    protected boolean isValidFragment(String fragmentName) {
        // 使用できる Fragment か確認する

        return true;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new GlobalParameters().setNewLocale(base, false));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Context c = this;
        mGp= GlobalWorkArea.getGlobalParameters(c);
        SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(c);
        mCurrentScreenTheme=shared_pref.getString(getString(R.string.settings_screen_theme), SMBSYNC2_SCREEN_THEME_STANDARD);
        if (mCurrentScreenTheme.equals(SMBSYNC2_SCREEN_THEME_STANDARD)) setTheme(R.style.Main);
        else if (mCurrentScreenTheme.equals(SMBSYNC2_SCREEN_THEME_LIGHT)) setTheme(R.style.MainLight);
        else if (mCurrentScreenTheme.equals(SMBSYNC2_SCREEN_THEME_BLACK)) setTheme(R.style.MainBlack);

        mCurrentThemeLangaue=shared_pref.getString(getString(R.string.settings_screen_theme_language), SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM);

        super.onCreate(savedInstanceState);

        if (mUtil == null) mUtil = new CommonUtilities(ActivitySettings.this, "SettingsActivity", mGp, null);
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
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        loadHeadersFromResource(R.xml.settings_frag, target);
    }

    @Override
    public boolean onIsMultiPane() {
        mGp= GlobalWorkArea.getGlobalParameters(getApplicationContext());

        mUtil = new CommonUtilities(ActivitySettings.this, "SettingsActivity", mGp, null);
        if (mGp.settingDebugLevel > 0)
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        return isTablet(getApplicationContext(), mUtil);

    }

    public static boolean isTablet(Context context, CommonUtilities cu) {
        int multiPaneDP=540;
        String lang_code=Locale.getDefault().getLanguage();
        if (lang_code.equals("en")) multiPaneDP=500;
        else if (lang_code.equals("fr")) multiPaneDP=540;
        else if (lang_code.equals("ja")) multiPaneDP=500;
        else if (lang_code.equals("ru")) multiPaneDP=1000;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float x_px = (float) Math.min(metrics.heightPixels, metrics.widthPixels);
        final float y_px = (float) Math.max(metrics.heightPixels, metrics.widthPixels);
        boolean portrait_mp = (x_px/metrics.density) >= multiPaneDP;
        boolean land_mp = (y_px/metrics.density) >= multiPaneDP;

        int orientation = context.getResources().getConfiguration().orientation;
        boolean sc_land_mp = (land_mp || mGp.settingForceDeviceTabletViewInLandscape) && orientation == Configuration.ORIENTATION_LANDSCAPE; //screen is in landscape orientation and either width size >= multiPaneDP or user forced MultiPanel view in landscape

        return portrait_mp||sc_land_mp; //use MultiPane display in portrait if width >= multiPaneDP or in landscape if largest screen side >= multiPaneDP
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

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mGp.setNewLocale(this, false);
        //this.recreate();//needed only in Legacy mode if language is different from System Default
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
                        false, "E", c.getString(R.string.settings_playback_ringtone_volume_disabled),"");
                cd.showDialog(fm, cd, null);
            }
        }
    }

    public static class SettingsSync extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                        checkSettingValue(mUtil, shared_pref, key_string, getContext());
                    }
                };
        private CommonUtilities mUtil = null;

        @SuppressLint("NewApi")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUtil = new CommonUtilities(getContext(), "SettingsSync", mGp, null);
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_sync);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(getContext());

            if (!LocalMountPoint.isExternalStorageAvailable()) {
                if (findPreference(getString(R.string.settings_mgt_dir))!=null) findPreference(getString(R.string.settings_mgt_dir)).setEnabled(false);
            }

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_error_option), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_wifi_lock), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_sync_history_log), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_no_compress_file_type), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_mgt_dir), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_force_screen_on_while_sync), getContext());
        }

        private boolean checkSettingValue(CommonUtilities ut, SharedPreferences shared_pref, String key_string, Context c) {
            Preference pref_key=findPreference(key_string);
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
                        checkSettingValue(mUtil, shared_pref, key_string, getContext());
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUtil = new CommonUtilities(getContext(), "SettingsLog", mGp, null);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_log);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(getContext());

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_log_option), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_put_logcat_option), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_log_file_max_count), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_log_level), getContext());
        }

        private boolean checkSettingValue(CommonUtilities ut, SharedPreferences shared_pref, String key_string, Context c) {
            Preference pref_key=findPreference(key_string);
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
                        checkSettingValue(mUtil, shared_pref, key_string, getContext());
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUtil = new CommonUtilities(getContext(), "SettingsMisc", mGp, null);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_misc);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(getContext());

//            shared_pref.edit().putBoolean(getString(R.string.settings_exit_clean), true).commit();
//            findPreference(getString(R.string.settings_exit_clean).toString()).setEnabled(false);
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_exit_clean), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_sync_message_use_standard_text_view), getContext());
        }

        private boolean checkSettingValue(CommonUtilities ut, SharedPreferences shared_pref, String key_string, Context c) {
            Preference pref_key=findPreference(key_string);
            boolean isChecked = false;
            if (key_string.equals(c.getString(R.string.settings_exit_clean))) {
                isChecked = true;
            } else if (key_string.equals(c.getString(R.string.settings_sync_message_use_standard_text_view))) {
                isChecked = true;
            }
            return isChecked;
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

        @Override
        public void onStop() {
            super.onStop();
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(listenerAfterHc);
        }
    }

    public static class SettingsSmb extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listenerAfterHc =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences shared_pref, String key_string) {
                        checkSettingValue(mUtil, shared_pref, key_string, getContext());
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUtil = new CommonUtilities(getContext(), "SettingsSmb", mGp, null);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_smb);


            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(getContext());

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_use_extended_security), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_lm_compatibility), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_client_response_timeout), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_smb_disable_plain_text_passwords), getContext());

            Preference button = (Preference)getPreferenceManager().findPreference("settings_smb_set_default_value");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    shared_pref.edit().putBoolean(getContext().getString(R.string.settings_smb_use_extended_security),true).commit();
                    shared_pref.edit().putBoolean(getContext().getString(R.string.settings_smb_disable_plain_text_passwords),false).commit();
                    shared_pref.edit().putString(getContext().getString(R.string.settings_smb_lm_compatibility),"3").commit();
                    shared_pref.edit().putString(getContext().getString(R.string.settings_smb_client_response_timeout),"30000").commit();
                    return false;
                }
            });

        }

        private boolean checkSettingValue(CommonUtilities ut, SharedPreferences shared_pref, String key_string, Context c) {
            Preference pref_key=findPreference(key_string);
            boolean isChecked = false;
            if (key_string.equals(c.getString(R.string.settings_smb_use_extended_security))) {
                isChecked = true;
            } else if (key_string.equals(c.getString(R.string.settings_smb_disable_plain_text_passwords))) {
                isChecked = true;
            } else if (key_string.equals(c.getString(R.string.settings_smb_lm_compatibility))) {
                String lmc=shared_pref.getString(c.getString(R.string.settings_smb_lm_compatibility),"3");
                if (lmc.equals("3") || lmc.equals("4")) {
                    findPreference(c.getString(R.string.settings_smb_use_extended_security).toString()).setEnabled(false);
                } else {
                    findPreference(c.getString(R.string.settings_smb_use_extended_security).toString()).setEnabled(true);
                }
                pref_key.setSummary(lmc);
                isChecked = true;
            } else if (key_string.equals(c.getString(R.string.settings_smb_client_response_timeout))) {
                pref_key.setSummary(shared_pref.getString(key_string, "30000")+" Millis");
                isChecked = true;
            }
            return isChecked;
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
                        checkSettingValue(mUtil, shared_pref, key_string, getContext());
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUtil = new CommonUtilities(getContext(), "SettingsUi", mGp, null);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_ui);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(getContext());

            mInitVolume = shared_pref.getInt(getString(R.string.settings_playback_ringtone_volume), 100);

            mCurrentScreenTheme=shared_pref.getString(getString(R.string.settings_screen_theme), SMBSYNC2_SCREEN_THEME_STANDARD);
            mCurrentThemeLangaue=shared_pref.getString(getString(R.string.settings_screen_theme_language), SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_notification_message_when_sync_ended), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_playback_ringtone_when_sync_ended), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_playback_ringtone_volume), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_vibrate_when_sync_ended), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_screen_theme), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_device_orientation_portrait), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_device_orientation_landscape_tablet), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_screen_theme_language), getContext());
            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_dim_screen_on_while_sync), getContext());

        }

        private boolean checkSettingValue(CommonUtilities ut, SharedPreferences shared_pref, String key_string, Context c) {
            mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
            Preference pref_key=findPreference(key_string);
            boolean isChecked = false;
            if (key_string.equals(c.getString(R.string.settings_playback_ringtone_when_sync_ended))) {
                isChecked = true;
                Preference rv = findPreference(c.getString(R.string.settings_playback_ringtone_volume));
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
                if (mInitVolume != vol) playBackDefaultNotification(c, getFragmentManager(), vol);
            } else if (key_string.equals(c.getString(R.string.settings_screen_theme))) {
                isChecked = true;
                String tid=shared_pref.getString(key_string, "0");
                String[] wl_label = c.getResources().getStringArray(R.array.settings_screen_theme_list_entries);
                String sum_msg = wl_label[Integer.parseInt(tid)];
                pref_key.setSummary(sum_msg);
                if (!mCurrentScreenTheme.equals(tid)) {
                    if (tid.equals(SMBSYNC2_SCREEN_THEME_STANDARD)) getActivity().setTheme(R.style.Main);
                    else if (tid.equals(SMBSYNC2_SCREEN_THEME_LIGHT)) getActivity().setTheme(R.style.MainLight);
                    else if (tid.equals(SMBSYNC2_SCREEN_THEME_BLACK)) getActivity().setTheme(R.style.MainBlack);
                    mCurrentScreenTheme=tid;
                    getActivity().finish();
                    Intent intent = new Intent(getActivity(), ActivitySettings.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(intent);
                }
            } else if (key_string.equals(c.getString(R.string.settings_screen_theme_language))) {
                isChecked = true;
                String lang_value=shared_pref.getString(key_string, SMBSYNC2_SCREEN_THEME_LANGUAGE_SYSTEM);
                String[] lang_msgs = c.getResources().getStringArray(R.array.settings_screen_theme_language_list_entries);
                String sum_msg = lang_msgs[Integer.parseInt(lang_value)];
                pref_key.setSummary(sum_msg);
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " lang_val="+lang_value+", settings="+mGp.onStartSettingScreenThemeLanguageValue);
//                if (!lang_value.equals(mGp.onStartSettingScreenThemeLanguageValue)) {//OnSharedPreferenceChangeListener() from preference fragment: language value was changed by user
                if (!lang_value.equals(mCurrentThemeLangaue)) {
                    getActivity().finish();//finish current preferences activity. Will trigger checkThemeLanguageChanged() to force restart app from main activity
                    mGp.setNewLocale(getActivity(), true);
                    Intent intent = new Intent(getActivity(), ActivitySettings.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(intent);
                }
            } else if (key_string.equals(c.getString(R.string.settings_device_orientation_portrait))) {
                isChecked = true;
                boolean forcePortrait=shared_pref.getBoolean(key_string,false);
//            if (forcePortrait) mPrefActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            else mPrefActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                Preference tabletLand = findPreference(c.getString(R.string.settings_device_orientation_landscape_tablet));
                if (forcePortrait) tabletLand.setEnabled(false);
                else tabletLand.setEnabled(true);
            } else if (key_string.equals(c.getString(R.string.settings_device_orientation_landscape_tablet))) {
                isChecked = true;
            } else if (key_string.equals(c.getString(R.string.settings_dim_screen_on_while_sync))) {
                isChecked = true;
            }
            return isChecked;
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
                        checkSettingValue(mUtil, shared_pref, key_string, getContext());
                    }
                };
        private CommonUtilities mUtil = null;

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == 0) {
                if (resultCode == RESULT_OK) {
                    SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(getContext());
                    checkSettingValue(mUtil, shared_pref, getString(R.string.settings_security_application_password), getContext());
                }
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mUtil = new CommonUtilities(getContext(), "SettingsSecurity", mGp, null);
            if (mGp.settingDebugLevel > 0)
                mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

            addPreferencesFromResource(R.xml.settings_frag_security);

            SharedPreferences shared_pref = PreferenceManager.getDefaultSharedPreferences(getContext());

            mInitVolume = shared_pref.getInt(getString(R.string.settings_playback_ringtone_volume), 100);

            checkSettingValue(mUtil, shared_pref, getString(R.string.settings_security_application_password), getContext());

            Preference button = (Preference)getPreferenceManager().findPreference(getString(R.string.settings_security_application_password));
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent in=new Intent(getContext(), ActivityPasswordSettings.class);
                    startActivityForResult(in, 0);
                    return false;
                }
            });
        }

        private boolean checkSettingValue(CommonUtilities ut, SharedPreferences shared_pref, String key_string, Context c) {
            Preference pref_key=findPreference(key_string);
            boolean isChecked = false;
            String hv=ApplicationPasswordUtil.getApplicationPasswordHashValue(shared_pref);

            if (key_string.equals(c.getString(R.string.settings_security_application_password))) {
                isChecked = true;
                String contents_string="";
                if (hv.equals("")) {
                    contents_string=c.getString(R.string.settings_security_application_password_not_created);
                } else  {
                    contents_string="-"+c.getString(R.string.settings_security_application_password_created);
                    if (shared_pref.getBoolean(c.getString(R.string.settings_security_use_auth_timeout), true))
                        contents_string+="\n-"+c.getString(R.string.settings_security_use_auth_timeout_title);
                    if (shared_pref.getBoolean(c.getString(R.string.settings_security_application_password_use_app_startup), false))
                        contents_string+="\n-"+c.getString(R.string.settings_security_application_password_use_app_startup_title);
                    if (shared_pref.getBoolean(c.getString(R.string.settings_security_application_password_use_edit_task), false))
                        contents_string+="\n-"+c.getString(R.string.settings_security_application_password_use_edit_task_title);
                    if (shared_pref.getBoolean(c.getString(R.string.settings_security_application_password_use_export_task), false))
                        contents_string+="\n-"+c.getString(R.string.settings_security_application_password_use_export_task_title);
                    if (shared_pref.getBoolean(c.getString(R.string.settings_security_init_smb_account_password), false))
                        contents_string+="\n-"+c.getString(R.string.settings_security_init_smb_account_password_title);
                }
                pref_key.setSummary(contents_string);
            }

            return isChecked;
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