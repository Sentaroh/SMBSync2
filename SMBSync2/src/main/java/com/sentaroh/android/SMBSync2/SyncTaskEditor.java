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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.LocalMountPoint;
import com.sentaroh.android.Utilities.MiscUtil;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities.SafManager;
import com.sentaroh.android.Utilities.StringUtil;
import com.sentaroh.android.Utilities.Widget.CustomSpinnerAdapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.KeyEvent.KEYCODE_BACK;
import static com.sentaroh.android.SMBSync2.Constants.APPLICATION_TAG;
import static com.sentaroh.android.SMBSync2.Constants.APP_SPECIFIC_DIRECTORY;
import static com.sentaroh.android.SMBSync2.Constants.ARCHIVE_FILE_TYPE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_DECRYPT_FAILED;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_ENCRYPT_FAILED;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_DIR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_FILE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_INCLUDE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_DAY;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_MONTH;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY_LONG;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_NUMBER;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_YEAR;
import static com.sentaroh.android.SMBSync2.Constants.SYNC_FILE_TYPE_AUDIO;
import static com.sentaroh.android.SMBSync2.Constants.SYNC_FILE_TYPE_IMAGE;
import static com.sentaroh.android.SMBSync2.Constants.SYNC_FILE_TYPE_VIDEO;
import static com.sentaroh.android.SMBSync2.Constants.WHOLE_DIRECTORY_FILTER_PREFIX_V1;
import static com.sentaroh.android.SMBSync2.Constants.WHOLE_DIRECTORY_FILTER_PREFIX_V2;

public class SyncTaskEditor extends DialogFragment {
    private final static String SUB_APPLICATION_TAG = "SyncTask ";

    private Dialog mDialog = null;
    private boolean mTerminateRequired = true;
    private Context mContext = null;
    private SyncTaskEditor mFragment = null;
    private GlobalParameters mGp = null;
    private SyncTaskUtil mTaskUtil = null;
    private CommonUtilities mUtil = null;
    private CommonDialog mCommonDlg = null;

    private FragmentManager mFragMgr = null;

    public static SyncTaskEditor newInstance() {
        SyncTaskEditor frag = new SyncTaskEditor();
        Bundle bundle = new Bundle();
        frag.setArguments(bundle);
        return frag;
    }

    public SyncTaskEditor() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
//		if(outState.isEmpty()){
//	        outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
//	    }
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        // Ignore orientation change to keep activity from restarting
        super.onConfigurationChanged(newConfig);
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

        reInitViewWidget();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        View view = super.onCreateView(inflater, container, savedInstanceState);
        CommonDialog.setDlgBoxSizeLimit(mDialog, true);
        setHasOptionsMenu(true);//force refresh options menu state (needed to update Top Sync button Toast info message when changing task from Manual to Auto)
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (mContext == null) mContext = this.getActivity();
        mFragment = this;
        mFragMgr = this.getFragmentManager();
        if (mUtil == null) mUtil = new CommonUtilities(mContext, "SyncTaskEditor", mGp, getFragmentManager());
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        mCommonDlg = new CommonDialog(mContext, getActivity().getSupportFragmentManager());
        if (mTerminateRequired) {
            this.dismiss();
        }
    }

    @Override
    final public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
    }

    @Override
    final public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) mContext = this.getActivity();
        mGp=GlobalWorkArea.getGlobalParameters(mContext);
        if (mUtil == null) mUtil = new CommonUtilities(mContext, "SyncTaskEditor", mGp, getFragmentManager());
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
    }

    @Override
    final public void onDetach() {
        super.onDetach();
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
    }

    @Override
    final public void onStart() {
        CommonDialog.setDlgBoxSizeLimit(mDialog, true);
        super.onStart();
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        if (mTerminateRequired) mDialog.cancel();
    }

    @Override
    final public void onStop() {
        super.onStop();
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
    }

    @Override
    public void onDestroyView() {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onCancel(DialogInterface di) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        if (!mTerminateRequired) {
            final Button btnCancel = (Button) mDialog.findViewById(R.id.edit_profile_sync_dlg_btn_cancel);
            btnCancel.performClick();
        }
        mFragment.dismiss();
        super.onCancel(di);
    }

    @Override
    public void onDismiss(DialogInterface di) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        super.onDismiss(di);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

        mDialog = new Dialog(getActivity(), mGp.applicationTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!mTerminateRequired) {
            initViewWidget();
            editSyncTask(mOpType, mCurrentSyncTaskItem);
        }
        return mDialog;
    }

    class SavedViewContents {
        CharSequence prof_name_et;
        int prof_name_et_spos;
        int prof_name_et_epos;
        boolean cb_active;

        boolean sync_task_edit_ok_button_enabled =false;
        boolean sync_task_swap_mater_target_button_enabled =false;

        public int sync_opt = -1;
        public boolean sync_process_root_dir_file, sync_conf_required, sync_use_smbsync_last_mod, sync_do_not_reset_remote_file,
                sync_retry, sync_empty_dir, sync_hidden_dir, sync_hidden_file, sync_sub_dir, sync_use_ext_dir_fileter, sync_delete_first, sync_exact_mirror;
        public boolean sync_UseRemoteSmallIoArea;
        public int sync_master_pos = -1, sync_target_pos = -1;

        public String sync_master_foder_info = "";
        public Drawable sync_master_foder_icon=null;
        public String sync_target_foder_info = "";
        public Drawable sync_target_foder_icon=null;

        public String sync_file_filter_info = "";
        public String sync_dir_filter_info = "";

        public boolean sync_process_override;
        public boolean sync_copy_by_rename;

        public int sync_wifi_option = -1;

        public boolean sync_test_mode;
        public boolean sync_diff_use_file_size;
        public boolean sync_diff_use_last_mod;

        public boolean sync_show_special_option;

        public int sync_diff_last_mod_value = -1;

        public boolean sync_diff_file_size_gt_target=false;

        public boolean sync_when_cahrging=false;
        public boolean skip_if_ssid_invalid=false;
        public boolean allow_global_ip_addr=false;
        public boolean do_not_use_rename_when_smb_file_write=false;
        public boolean never_overwrite_target_file_newer_than_the_master_file=false;
        public boolean ignore_dst_difference=false;
        public int dst_offset_value=-1;
        public boolean ignore_unusable_character_used_directory_file_name=false;
        public boolean sync_remove_master_if_empty=false;
//        public boolean keep_conflict_file=false;
        public boolean specific_file_type=false;
        public boolean specific_file_type_audio=false;
        public boolean specific_file_type_image=false;
        public boolean specific_file_type_video=false;
        public boolean specific_diretory=false;
    }

    private SavedViewContents saveViewContents() {
        SavedViewContents sv = new SavedViewContents();

        final EditText et_sync_main_task_name = (EditText) mDialog.findViewById(R.id.edit_sync_task_task_name);
        final CheckedTextView ctv_auto = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_ctv_auto);
        final Spinner spinnerSyncOption = (Spinner) mDialog.findViewById(R.id.edit_sync_task_sync_type);

		final Button swap_master_target = (Button)mDialog.findViewById(R.id.edit_sync_task_change_master_and_target_btn);
//		final Button master_folder_edit_btn=(Button) mDialog.findViewById(R.id.edit_sync_task_edit_master_btn);
        final Button master_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_master_folder_info_btn);
//		final Button target_folder_edit_btn=(Button) mDialog.findViewById(R.id.edit_sync_task_edit_target_btn);
        final Button target_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_target_folder_info_btn);

        final Button sync_task_edit_btn_ok = (Button) mDialog.findViewById(R.id.edit_profile_sync_dlg_btn_ok);

        final Button dir_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_dir_filter_btn);
        final Button file_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_file_filter_btn);

        final CheckedTextView ctvTestMode = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_test_mode);

        final CheckedTextView ct_specific_file_type = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_specific);
//        final LinearLayout ll_specific_file_type_view = (LinearLayout) mDialog.findViewById(R.id.sync_filter_file_type_detail_view);
        final CheckedTextView ct_specific_file_type_audio = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_audio);
        final CheckedTextView ct_specific_file_type_image = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_image);
        final CheckedTextView ct_specific_file_type_video = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_video);

        final CheckedTextView ct_specific_directory = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_sub_directory_specific);
//        final LinearLayout ll_specific_directory_view = (LinearLayout) mDialog.findViewById(R.id.sync_filter_sub_directory_detail_view);

        final CheckedTextView ctv_task_sync_when_cahrging =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_start_when_charging);
        final CheckedTextView ctvProcessRootDirFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_master_root_dir_file);
        final CheckedTextView ctvConfirmOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_confirm_override_delete_file);
        final Spinner spinnerSyncWifiStatus = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_wifi_status);
        final CheckedTextView ctv_task_skip_if_ssid_invalid =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ap_list_task_skip_if_ssid_invalid);
        final CheckedTextView ctv_sync_allow_global_ip_addr =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_sync_allow_global_ip_address);
        final CheckedTextView ctvShowSpecialOption = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_show_special_option);
        final CheckedTextView ctvSyncSubDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_sub_dir);
        final CheckedTextView ctvSyncEmptyDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_empty_directory);
        final CheckedTextView ctvSyncHiddenDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_directory);
        final CheckedTextView ctvSyncHiddenFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_file);
        final CheckedTextView ctvProcessOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_process_override_delete_file);
        final CheckedTextView ctUseExtendedDirectoryFilter1 = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_extended_filter1);
        final CheckedTextView ctUseDirectoryFilterV2 = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_directory_filter_v2);
        final CheckedTextView ctvDeleteFirst = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_delete_first_when_mirror);
        final CheckedTextView ctvEnsureTargetExactMirror = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_ensure_target_is_exact_mirror);
        final CheckedTextView ctvRetry = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_retry_if_error_occured);
        final CheckedTextView ctvSyncUseRemoteSmallIoArea = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_remote_small_io_area);
        final CheckedTextView ctv_edit_sync_tak_option_do_not_use_rename_when_smb_file_write =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_do_mot_use_rename_when_smb_file_write);
        final CheckedTextView ctvDoNotResetRemoteFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_do_mot_reset_file_last_mod_time);
        final CheckedTextView ctvUseSmbsyncLastMod = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_use_smbsync_last_mod_time);
        final CheckedTextView ctvDiffUseFileSize = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_file_size);
        final CheckedTextView ctvDeterminChangedFileSizeGtTarget = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_file_size_greater_than_target);
        final CheckedTextView ctDeterminChangedFileByTime = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_last_mod_time);
        final Spinner spinnerSyncDiffTimeValue = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_diff_file_determin_time_value);
        final CheckedTextView ctv_never_overwrite_target_file_newer_than_the_master_file =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_never_overwrite_target_file_if_it_is_newer_than_the_master_file);
        final CheckedTextView ctv_ignore_dst_difference =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_ignore_dst_difference);
        final Spinner spinnerSyncDstOffsetValue =
                (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_offset_daylight_saving_time_value);
        final CheckedTextView ctv_edit_sync_task_option_ignore_unusable_character_used_directory_file_name =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ignore_unusable_character_used_directory_file_name);
        final CheckedTextView ctv_sync_remove_master_if_empty =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_remove_directory_if_empty_when_move);

        sv.sync_task_edit_ok_button_enabled =sync_task_edit_btn_ok.isEnabled();
        sv.sync_task_swap_mater_target_button_enabled=swap_master_target.isEnabled();

        sv.prof_name_et = et_sync_main_task_name.getText();
        sv.prof_name_et_spos = et_sync_main_task_name.getSelectionStart();
        sv.prof_name_et_epos = et_sync_main_task_name.getSelectionEnd();
        sv.cb_active = ctv_auto.isChecked();
        sv.sync_opt = spinnerSyncOption.getSelectedItemPosition();

        sv.sync_master_foder_info = master_folder_info.getText().toString();
        sv.sync_master_foder_icon=master_folder_info.getCompoundDrawables()[0];
        sv.sync_target_foder_info = target_folder_info.getText().toString();
        sv.sync_target_foder_icon=target_folder_info.getCompoundDrawables()[0];

        sv.sync_file_filter_info = file_filter_btn.getText().toString();
        sv.sync_dir_filter_info = dir_filter_btn.getText().toString();

        sv.sync_process_root_dir_file = ctvProcessRootDirFile.isChecked();
        sv.sync_sub_dir = ctvSyncSubDir.isChecked();
        sv.sync_empty_dir = ctvSyncEmptyDir.isChecked();
        sv.sync_hidden_dir = ctvSyncHiddenDir.isChecked();
        sv.sync_hidden_file = ctvSyncHiddenFile.isChecked();
        sv.sync_process_override=ctvProcessOverride.isChecked();
        sv.sync_conf_required = ctvConfirmOverride.isChecked();
        sv.sync_delete_first = ctvDeleteFirst.isChecked();
        sv.sync_exact_mirror = ctvEnsureTargetExactMirror.isChecked();

        sv.sync_wifi_option = spinnerSyncWifiStatus.getSelectedItemPosition();

        sv.sync_show_special_option = ctvShowSpecialOption.isChecked();

        sv.sync_do_not_reset_remote_file = ctvDoNotResetRemoteFile.isChecked();
        sv.sync_use_smbsync_last_mod = ctvUseSmbsyncLastMod.isChecked();
        sv.sync_retry = ctvRetry.isChecked();
        sv.sync_UseRemoteSmallIoArea = ctvSyncUseRemoteSmallIoArea.isChecked();

        sv.sync_test_mode = ctvTestMode.isChecked();
        sv.sync_diff_use_file_size = ctvDiffUseFileSize.isChecked();
        sv.sync_diff_file_size_gt_target=ctvDeterminChangedFileSizeGtTarget.isChecked();
        sv.sync_diff_use_last_mod = ctDeterminChangedFileByTime.isChecked();

        sv.sync_diff_last_mod_value = spinnerSyncDiffTimeValue.getSelectedItemPosition();

        sv.sync_use_ext_dir_fileter = ctUseExtendedDirectoryFilter1.isChecked();

        sv.sync_when_cahrging=ctv_task_sync_when_cahrging.isChecked();
        sv.skip_if_ssid_invalid=ctv_task_skip_if_ssid_invalid.isChecked();
        sv.allow_global_ip_addr=ctv_sync_allow_global_ip_addr.isChecked();
        sv.do_not_use_rename_when_smb_file_write=ctv_edit_sync_tak_option_do_not_use_rename_when_smb_file_write.isChecked();
        sv.never_overwrite_target_file_newer_than_the_master_file=ctv_never_overwrite_target_file_newer_than_the_master_file.isChecked();
        sv.ignore_dst_difference=ctv_ignore_dst_difference.isChecked();
        sv.dst_offset_value=spinnerSyncDstOffsetValue.getSelectedItemPosition();
        sv.ignore_unusable_character_used_directory_file_name=ctv_edit_sync_task_option_ignore_unusable_character_used_directory_file_name.isChecked();
        sv.sync_remove_master_if_empty=ctv_sync_remove_master_if_empty.isChecked();

        sv.specific_file_type=ct_specific_file_type.isChecked();
        sv.specific_file_type_audio=ct_specific_file_type_audio.isChecked();
        sv.specific_file_type_image=ct_specific_file_type_image.isChecked();
        sv.specific_file_type_video=ct_specific_file_type_video.isChecked();
        sv.specific_diretory=ct_specific_directory.isChecked();

        return sv;
    }

    private void performClickNoSound(View v) {
        v.setSoundEffectsEnabled(false);
        v.performClick();
        v.setSoundEffectsEnabled(true);
    }

    private void restoreViewContents(final SavedViewContents sv) {
        final EditText et_sync_main_task_name = (EditText) mDialog.findViewById(R.id.edit_sync_task_task_name);
        final CheckedTextView ctv_auto = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_ctv_auto);
        final Spinner spinnerSyncOption = (Spinner) mDialog.findViewById(R.id.edit_sync_task_sync_type);

		final Button swap_master_target = (Button)mDialog.findViewById(R.id.edit_sync_task_change_master_and_target_btn);
//		final Button master_folder_edit_btn=(Button) mDialog.findViewById(R.id.edit_sync_task_edit_master_btn);
        final Button master_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_master_folder_info_btn);
//		final Button target_folder_edit_btn=(Button) mDialog.findViewById(R.id.edit_sync_task_edit_target_btn);
        final Button target_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_target_folder_info_btn);

        final Button btn_ok = (Button) mDialog.findViewById(R.id.edit_profile_sync_dlg_btn_ok);

        final Button dir_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_dir_filter_btn);
        final Button file_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_file_filter_btn);

        final LinearLayout ll_special_option_view = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_special_option_view);

        final CheckedTextView ctvTestMode = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_test_mode);

        final CheckedTextView ct_specific_file_type = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_specific);
        final LinearLayout ll_specific_file_type_view = (LinearLayout) mDialog.findViewById(R.id.sync_filter_file_type_detail_view);
        final CheckedTextView ct_specific_file_type_audio = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_audio);
        final CheckedTextView ct_specific_file_type_image = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_image);
        final CheckedTextView ct_specific_file_type_video = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_video);

        final CheckedTextView ct_specific_directory = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_sub_directory_specific);
        final LinearLayout ll_specific_directory_view = (LinearLayout) mDialog.findViewById(R.id.sync_filter_sub_directory_detail_view);

        final CheckedTextView ctv_task_sync_when_cahrging =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_start_when_charging);
        final CheckedTextView ctvProcessRootDirFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_master_root_dir_file);
        final CheckedTextView ctvConfirmOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_confirm_override_delete_file);
        final Spinner spinnerSyncWifiStatus = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_wifi_status);
        final CheckedTextView ctv_task_skip_if_ssid_invalid =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ap_list_task_skip_if_ssid_invalid);
        final CheckedTextView ctv_sync_allow_global_ip_addr =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_sync_allow_global_ip_address);
        final CheckedTextView ctvShowSpecialOption = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_show_special_option);
        final CheckedTextView ctvSyncSubDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_sub_dir);
        final CheckedTextView ctvSyncEmptyDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_empty_directory);
        final CheckedTextView ctvSyncHiddenDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_directory);
        final CheckedTextView ctvSyncHiddenFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_file);
        final CheckedTextView ctvProcessOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_process_override_delete_file);
        final CheckedTextView ctUseExtendedDirectoryFilter1 = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_extended_filter1);
        final CheckedTextView ctUseDirectoryFilterV2 = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_directory_filter_v2);
        final CheckedTextView ctvDeleteFirst = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_delete_first_when_mirror);
        final CheckedTextView ctvEnsureTargetExactMirror = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_ensure_target_is_exact_mirror);
        final CheckedTextView ctvRetry = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_retry_if_error_occured);
        final CheckedTextView ctvSyncUseRemoteSmallIoArea = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_remote_small_io_area);
        final CheckedTextView ctv_edit_sync_tak_option_do_not_use_rename_when_smb_file_write =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_do_mot_use_rename_when_smb_file_write);
        final CheckedTextView ctvDoNotResetRemoteFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_do_mot_reset_file_last_mod_time);
        final CheckedTextView ctvUseSmbsyncLastMod = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_use_smbsync_last_mod_time);
        final CheckedTextView ctvDiffUseFileSize = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_file_size);
        final CheckedTextView ctvDeterminChangedFileSizeGtTarget = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_file_size_greater_than_target);
        final CheckedTextView ctDeterminChangedFileByTime = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_last_mod_time);
        final Spinner spinnerSyncDiffTimeValue = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_diff_file_determin_time_value);
        final CheckedTextView ctv_never_overwrite_target_file_newer_than_the_master_file =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_never_overwrite_target_file_if_it_is_newer_than_the_master_file);
        final CheckedTextView ctv_ignore_dst_difference =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_ignore_dst_difference);
        final Spinner spinnerSyncDstOffsetValue =
                (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_offset_daylight_saving_time_value);
        final CheckedTextView ctv_edit_sync_task_option_ignore_unusable_character_used_directory_file_name =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ignore_unusable_character_used_directory_file_name);
        final CheckedTextView ctv_sync_remove_master_if_empty =
                (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_remove_directory_if_empty_when_move);

        Handler hndl1 = new Handler();
        hndl1.postDelayed(new Runnable() {
            @Override
            public void run() {
                et_sync_main_task_name.setText(sv.prof_name_et);
                ctvTestMode.setChecked(sv.sync_test_mode);
                if (sv.sync_test_mode) CommonDialog.setViewEnabled(getActivity(), ctv_auto, false);
                ctv_auto.setChecked(sv.cb_active);
                CommonDialog.setViewEnabled(getActivity(), spinnerSyncOption, false);
                spinnerSyncOption.setSelection(sv.sync_opt);

                CommonDialog.setViewEnabled(getActivity(), swap_master_target, sv.sync_task_swap_mater_target_button_enabled);

                master_folder_info.setText(sv.sync_master_foder_info);
                master_folder_info.setCompoundDrawablePadding(32);
                master_folder_info.setCompoundDrawablesWithIntrinsicBounds(sv.sync_master_foder_icon, null, null, null);

                target_folder_info.setText(sv.sync_target_foder_info);
                target_folder_info.setCompoundDrawablePadding(32);
                target_folder_info.setCompoundDrawablesWithIntrinsicBounds(sv.sync_target_foder_icon, null, null, null);

                file_filter_btn.setText(sv.sync_file_filter_info);
                dir_filter_btn.setText(sv.sync_dir_filter_info);

                ctvProcessRootDirFile.setChecked(sv.sync_process_root_dir_file);
                ctvSyncSubDir.setChecked(sv.sync_sub_dir);
                if (sv.sync_process_root_dir_file) ctvSyncSubDir.setEnabled(true);
                else ctvSyncSubDir.setEnabled(false);

                ctvSyncEmptyDir.setChecked(sv.sync_empty_dir);
                ctvSyncHiddenDir.setChecked(sv.sync_hidden_dir);
                ctvSyncHiddenFile.setChecked(sv.sync_hidden_file);
                ctvProcessOverride.setChecked(sv.sync_process_override);
                ctvConfirmOverride.setChecked(sv.sync_conf_required);
                ctvDeleteFirst.setChecked(sv.sync_delete_first);
                ctvEnsureTargetExactMirror.setChecked(sv.sync_exact_mirror);

                CommonDialog.setViewEnabled(getActivity(), spinnerSyncWifiStatus, false);
                spinnerSyncWifiStatus.setSelection(sv.sync_wifi_option);

                ctvShowSpecialOption.setChecked(false);
                if (sv.sync_show_special_option) {
                    performClickNoSound(ctvShowSpecialOption);
                }

                ctvDoNotResetRemoteFile.setChecked(sv.sync_do_not_reset_remote_file);
                ctvUseSmbsyncLastMod.setChecked(sv.sync_use_smbsync_last_mod);
                ctvRetry.setChecked(sv.sync_retry);
                ctvSyncUseRemoteSmallIoArea.setChecked(sv.sync_UseRemoteSmallIoArea);

                ctvDiffUseFileSize.setChecked(sv.sync_diff_use_file_size);
                ctvDeterminChangedFileSizeGtTarget.setChecked(sv.sync_diff_file_size_gt_target);
                CommonDialog.setViewEnabled(getActivity(), ctvDeterminChangedFileSizeGtTarget, sv.sync_diff_use_file_size);

                ctDeterminChangedFileByTime.setChecked(sv.sync_diff_use_last_mod);

                CommonDialog.setViewEnabled(getActivity(), spinnerSyncDiffTimeValue, false);
                spinnerSyncDiffTimeValue.setSelection(sv.sync_diff_last_mod_value);

                ctUseExtendedDirectoryFilter1.setChecked(sv.sync_use_ext_dir_fileter);

                ctv_task_sync_when_cahrging.setChecked(sv.sync_when_cahrging);
                ctv_task_skip_if_ssid_invalid.setChecked(sv.skip_if_ssid_invalid);
                ctv_sync_allow_global_ip_addr.setChecked(sv.allow_global_ip_addr);
                ctv_edit_sync_tak_option_do_not_use_rename_when_smb_file_write.setChecked(sv.do_not_use_rename_when_smb_file_write);
                ctv_never_overwrite_target_file_newer_than_the_master_file.setChecked(sv.never_overwrite_target_file_newer_than_the_master_file);
                ctv_ignore_dst_difference.setChecked(false);
                if (sv.ignore_dst_difference) {
                    performClickNoSound(ctv_ignore_dst_difference);
                }
                spinnerSyncDstOffsetValue.setSelection(sv.dst_offset_value);
                ctv_edit_sync_task_option_ignore_unusable_character_used_directory_file_name.setChecked(sv.ignore_unusable_character_used_directory_file_name);
                ctv_sync_remove_master_if_empty.setChecked(sv.sync_remove_master_if_empty);

                ct_specific_file_type.setChecked(false);
                ct_specific_file_type_audio.setChecked(sv.specific_file_type_audio);
                ct_specific_file_type_image.setChecked(sv.specific_file_type_image);
                ct_specific_file_type_video.setChecked(sv.specific_file_type_video);
                if (sv.specific_file_type || sv.specific_file_type_audio || sv.specific_file_type_image || sv.specific_file_type_video ||
                        (!sv.sync_file_filter_info.equals("") && !sv.sync_file_filter_info.equals(mContext.getString(R.string.msgs_profile_sync_task_dlg_file_filter_not_specified)))) {
                    performClickNoSound(ct_specific_file_type);
                }

                ct_specific_directory.setChecked(false);
                if (sv.specific_diretory ||
                        (!sv.sync_dir_filter_info.equals("") && !sv.sync_dir_filter_info.equals(mContext.getString(R.string.msgs_profile_sync_task_dlg_dir_filter_not_specified)))) {
                    performClickNoSound(ct_specific_directory);
                }

                Handler hndl2 = new Handler();
                hndl2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CommonDialog.setViewEnabled(getActivity(), btn_ok, sv.sync_task_edit_ok_button_enabled);
                        CommonDialog.setViewEnabled(getActivity(), spinnerSyncOption, true);
                        CommonDialog.setViewEnabled(getActivity(), spinnerSyncWifiStatus, true);
                        CommonDialog.setViewEnabled(getActivity(), spinnerSyncDiffTimeValue, true);
                    }
                }, 500);
            }
        }, 50);
    }

    public void reInitViewWidget() {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        if (!mTerminateRequired) {
            SavedViewContents sv = null;
            sv = saveViewContents();
            initViewWidget();
            restoreViewContents(sv);
            restoreEditSyncFolderContents();
            CommonDialog.setDlgBoxSizeLimit(mDialog, true);
        }
    }

    private void restoreEditSyncFolderContents() {
        if (mEditFolderDialog!=null) {
            SyncFolderEditValue new_sfev=buildSyncFolderEditValue(mEditFolderDialog, mEditFolderSfev);
            mEditFolderSfev=new_sfev;
            final Button btn_sync_folder_ok = (Button) mEditFolderDialog.findViewById(R.id.edit_profile_remote_btn_ok);
            final TextView dlg_msg = (TextView) mEditFolderDialog.findViewById(R.id.edit_sync_folder_dlg_msg);
            final boolean ok_button_enabled=btn_sync_folder_ok.isEnabled();
//            final int msg_visibility=dlg_msg.getVisibility();
//            final CharSequence msg_text=dlg_msg.getText();
            editSyncFolder(true, mEditFolderSti, mEditFolderSfev, mEditFolderNotify);
            Handler hndl=new Handler();
            hndl.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mEditFolderDialog!=null) {
                        final Button new_btn_sync_folder_ok = (Button) mEditFolderDialog.findViewById(R.id.edit_profile_remote_btn_ok);
                        final TextView new_dlg_msg = (TextView) mEditFolderDialog.findViewById(R.id.edit_sync_folder_dlg_msg);
                        setSyncFolderOkButtonEnabled(new_btn_sync_folder_ok, ok_button_enabled);
                    }
                }
            }, 500);
        }
    }

    public void initViewWidget() {
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");

    }

    private String mOpType = "";
    private SyncTaskItem mCurrentSyncTaskItem;
    private NotifyEvent mNotifyComplete = null;

    public void showDialog(FragmentManager fm, Fragment frag,
                           final String op_type,
                           final SyncTaskItem pli,
                           SyncTaskUtil pm,
                           CommonUtilities ut,
                           CommonDialog cd,
                           GlobalParameters gp,
                           NotifyEvent ntfy) {
        mGp = gp;
        if (mGp.settingDebugLevel > 0) Log.v(APPLICATION_TAG, SUB_APPLICATION_TAG + "showDialog");
        mTerminateRequired = false;
//    	mFragmentMgr=fm;
        mOpType = op_type;
        mCurrentSyncTaskItem = pli;
        mTaskUtil = pm;
//    	mUtil=ut;
//    	mCommonDlg=cd;
        mNotifyComplete = ntfy;
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(frag, null);
        ft.commitAllowingStateLoss();
//	    show(fm,APPLICATION_TAG);
    }

    private void setDialogMsg(final TextView tv, final String msg) {
//    	Log.v("","t="+tv.getText().toString()+", m="+msg);
        if (msg.equals("")) {
            tv.setVisibility(TextView.GONE);
            tv.setText("");
        } else {
            tv.setVisibility(TextView.VISIBLE);
            tv.setText(msg);
        }
    }

    private static String removeInvalidCharForFileDirName(String in_str) {
        String out = in_str.replaceAll(":", "")
                .replaceAll("\\\\", "")
                .replaceAll("\\*", "")
                .replaceAll("\\?", "")
                .replaceAll("\"", "")
                .replaceAll("<", "")
                .replaceAll(">", "")
                .replaceAll("\\|", "");
        return out;
    }

    private static String removeInvalidCharForFileName(String in_str) {
        String out = in_str.replaceAll(":", "")
                .replaceAll("\\\\", "")
                .replaceAll("\\*", "")
                .replaceAll("\\?", "")
                .replaceAll("\"", "")
                .replaceAll("<", "")
                .replaceAll(">", "")
                .replaceAll("/", "")
                .replaceAll("\\|", "");
        return out;
    }

    private void setSyncFolderSmbListener(final Dialog dialog, final SyncTaskItem sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final Spinner sp_sync_folder_smb_proto = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_protocol);
        setSpinnerSyncFolderSmbProto(sti, sp_sync_folder_smb_proto, sfev.folder_smb_protocol);

        final Button btn_search_host = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_search_remote_host);
        final Button btn_sync_folder_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_smb_directory_btn);

        final EditText et_sync_folder_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_directory_name);
        et_sync_folder_dir_name.setText(sfev.folder_directory);

        final EditText et_remote_host = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_server);
        if (!sfev.folder_remote_addr.equals("")) et_remote_host.setText(sfev.folder_remote_addr);
        else et_remote_host.setText(sfev.folder_remote_host);

        //		final LinearLayout ll_sync_folder_port = (LinearLayout)dialog.findViewById(R.id.edit_sync_folder_dlg_port_option_view);
        final CheckedTextView ctv_sync_folder_use_port = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_remote_port_number);
        CommonUtilities.setCheckedTextView(ctv_sync_folder_use_port);
        final EditText et_sync_folder_port = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_port);
        if (!sfev.folder_remote_port.equals("")) {
            ctv_sync_folder_use_port.setChecked(true);
            CommonDialog.setViewEnabled(getActivity(), et_sync_folder_port, true);
            et_sync_folder_port.setText(sfev.folder_remote_port);
        } else {
            if (sfev.folder_remote_use_port_number) ctv_sync_folder_use_port.setChecked(true);
            else ctv_sync_folder_use_port.setChecked(false);
            CommonDialog.setViewEnabled(getActivity(), et_sync_folder_port, false);
        }
        ctv_sync_folder_use_port.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctv_sync_folder_use_port.isChecked();
                ctv_sync_folder_use_port.setChecked(isChecked);
                CommonDialog.setViewEnabled(getActivity(), et_sync_folder_port, isChecked);
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        final LinearLayout ll_dir_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_directory_view);

        final Button btn_dir_view_keyword_insert_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_year);
        final Button btn_dir_view_keyword_insert_month = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_month);
        final Button btn_dir_view_keyword_insert_day = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day);
        final Button btn_dir_view_keyword_insert_day_of_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day_of_year);

        final Button btn_dir_view_keyword_insert_week_number = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_week_number);
        final Button btn_dir_view_keyword_insert_week_day = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_weekday);
        final Button btn_dir_view_keyword_insert_week_day_long = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_weekday_long);

        final CheckedTextView ctv_smb_use_taken_date_time_for_directory_keyword = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_use_taken_date_time_for_directory_keyword);
        if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR) || sfev.folder_master) {
            ctv_smb_use_taken_date_time_for_directory_keyword.setVisibility(CheckedTextView.GONE);
        } else {
            ctv_smb_use_taken_date_time_for_directory_keyword.setVisibility(CheckedTextView.VISIBLE);
        }
        ctv_smb_use_taken_date_time_for_directory_keyword.setChecked(sfev.folder_use_taken_date_time_for_directory_keyword);
        ctv_smb_use_taken_date_time_for_directory_keyword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });

        final CheckedTextView ctv_sync_folder_smb_ipc_enforced = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_smb_ipc_signing_enforced);
        ctv_sync_folder_smb_ipc_enforced.setChecked(sfev.folder_smb_ipc_enforced);

        ctv_sync_folder_smb_ipc_enforced.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctv_sync_folder_smb_ipc_enforced.isChecked();
                ctv_sync_folder_smb_ipc_enforced.setChecked(isChecked);
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        final CheckedTextView ctv_sync_folder_smb_use_smb2_negotiation = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_smb_use_smb2_negotiation);
        ctv_sync_folder_smb_use_smb2_negotiation.setChecked(sfev.folder_smb_use_smb2_negotiation);

        ctv_sync_folder_smb_use_smb2_negotiation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctv_sync_folder_smb_use_smb2_negotiation.isChecked();
                ctv_sync_folder_smb_use_smb2_negotiation.setChecked(isChecked);
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        final CheckedTextView ctv_sync_folder_use_pswd = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_user_pass);

        final EditText et_sync_folder_domain = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_domain);
        et_sync_folder_domain.setVisibility(EditText.GONE);
        final EditText et_sync_folder_user = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_user);
        final EditText et_sync_folder_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_pass);
        if (!sfev.folder_remote_user.equals("") || !sfev.folder_remote_pswd.equals("") || sfev.folder_error_code!=SyncTaskItem.SYNC_FOLDER_ERROR_NO_ERROR) {
            ctv_sync_folder_use_pswd.setChecked(true);
            CommonDialog.setViewEnabled(getActivity(), et_sync_folder_user, true);
            et_sync_folder_user.setText(sfev.folder_remote_user);
            CommonDialog.setViewEnabled(getActivity(), et_sync_folder_pswd, true);
            et_sync_folder_pswd.setText(sfev.folder_remote_pswd);
        } else {
            if (sfev.folder_remote_use_account_name_password) ctv_sync_folder_use_pswd.setChecked(true);
            else ctv_sync_folder_use_pswd.setChecked(false);
            CommonDialog.setViewEnabled(getActivity(), et_sync_folder_user, false);
            CommonDialog.setViewEnabled(getActivity(), et_sync_folder_pswd, false);
        }
        if (mGp.settingSecurityReinitSmbAccountPasswordValue && !mGp.settingSecurityApplicationPasswordHashValue.equals("")) {
            et_sync_folder_user.setText("");
            et_sync_folder_pswd.setText("");
        }
        sfev.folder_remote_use_pswd=ctv_sync_folder_use_pswd.isChecked();
        ctv_sync_folder_use_pswd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctv_sync_folder_use_pswd.isChecked();
                ctv_sync_folder_use_pswd.setChecked(isChecked);
                CommonDialog.setViewEnabled(getActivity(), et_sync_folder_user, isChecked);
                CommonDialog.setViewEnabled(getActivity(), et_sync_folder_pswd, isChecked);
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        final CheckedTextView ctv_show_password = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_show_password);
//        ctv_show_password.setVisibility(CheckedTextView.VISIBLE);
        ctv_show_password.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckedTextView ctv=(CheckedTextView)view;
                ctv.setChecked(!ctv.isChecked());
                if (!ctv.isChecked()) et_sync_folder_pswd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                else et_sync_folder_pswd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });

        final Button btn_sync_folder_list_share = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_share_btn);
        final EditText et_sync_folder_share_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_share_name);
        et_sync_folder_share_name.setText(sfev.folder_remote_share);

        et_remote_host.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                checkSyncFolderValidation(dialog, sfev);
            }
        });
        et_sync_folder_port.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        et_sync_folder_user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                checkSyncFolderValidation(dialog, sfev);
            }
        });
        et_sync_folder_pswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                checkSyncFolderValidation(dialog, sfev);
            }
        });
        et_sync_folder_share_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        sp_sync_folder_smb_proto.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSyncFolderViewVisibility(dialog, sfev.folder_master, sfev);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btn_search_host.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTaskUtil.invokeScanSmbServerDlg(dialog);
            }
        });

        btn_sync_folder_list_share.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTaskUtil.invokeSelectSmbShareDlg(dialog);
            }
        });

        et_sync_folder_dir_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    for(int i = editable.length()-1; i >= 0; i--){
                        if(editable.charAt(i) == '\n'){
                            editable.delete(i, i + 1);
                            return;
                        }
                    }

                    String new_name = removeInvalidCharForFileDirName(editable.toString());
                    if (editable.length() != new_name.length()) {
                        //remove invalid char
                        et_sync_folder_dir_name.setText(new_name);
                        if (new_name.length() > 0)
                            et_sync_folder_dir_name.setSelection(new_name.length());
                        mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_dir_name_has_invalid_char)
                                , "", null);

                    } else {
                        if (new_name.startsWith("/")) {
                            new_name=new_name.substring(1);
                            et_sync_folder_dir_name.setText(new_name);
                            mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_file_directory_name_first_char_has_invalid_char), "", null);
                        }
                    }
                    setSyncFolderArchiveFileImage(dialog, sti, new_name, true);
                }
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });

        btn_sync_folder_list_dir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sel = sp_sync_folder_type.getSelectedItem().toString();
                mTaskUtil.selectRemoteDirectoryDlg(dialog, !sfev.folder_master);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_YEAR);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_month, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_MONTH);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day_of_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR);

        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_number, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_NUMBER);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_day, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_day_long, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY_LONG);

        setWeekDayButtonLabel(btn_dir_view_keyword_insert_week_day, btn_dir_view_keyword_insert_week_day_long,
                mContext.getString(R.string.msg_edit_sync_folder_keyword_insert_week_day),
                mContext.getString(R.string.msg_edit_sync_folder_keyword_insert_week_day_long));

    }

    private void setSyncFolderInternalListener(final Dialog dialog, final SyncTaskItem sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final Button btn_sync_folder_ok = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_ok);

        final LinearLayout ll_sync_folder_mp = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_local_mount_point_selector_view);
        final Spinner sp_sync_folder_mp = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_local_mount_point_selector);
        setSpinnerSyncFolderMountPoint(sti, sp_sync_folder_mp, sfev.folder_mountpoint, !sfev.folder_master);

        final Button btn_sync_folder_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_internal_directory_btn);

        final EditText et_sync_folder_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_directory_name);
        et_sync_folder_dir_name.setText(sfev.folder_directory);

//        final Button btn_sdcard_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_select_document_tree);

        final LinearLayout ll_dir_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_directory_view);

        final Button btn_dir_view_keyword_insert_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_year);
        final Button btn_dir_view_keyword_insert_month = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_month);
        final Button btn_dir_view_keyword_insert_day = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day);
        final Button btn_dir_view_keyword_insert_day_of_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day_of_year);

        final Button btn_dir_view_keyword_insert_week_number = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_week_number);
        final Button btn_dir_view_keyword_insert_week_day = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_weekday);
        final Button btn_dir_view_keyword_insert_week_day_long = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_weekday_long);

        final CheckedTextView ctv_internal_use_taken_date_time_for_directory_keyword = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_use_taken_date_time_for_directory_keyword);
        if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR) || sfev.folder_master) {
            ctv_internal_use_taken_date_time_for_directory_keyword.setVisibility(CheckedTextView.GONE);
        } else {
            ctv_internal_use_taken_date_time_for_directory_keyword.setVisibility(CheckedTextView.VISIBLE);
        }
        ctv_internal_use_taken_date_time_for_directory_keyword.setChecked(sfev.folder_use_taken_date_time_for_directory_keyword);
        ctv_internal_use_taken_date_time_for_directory_keyword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });

        et_sync_folder_dir_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    for(int i = editable.length()-1; i >= 0; i--){
                        if(editable.charAt(i) == '\n'){
                            editable.delete(i, i + 1);
                            return;
                        }
                    }
                    checkSyncFolderValidation(dialog, sfev);

                    String new_name = removeInvalidCharForFileDirName(editable.toString());
                    if (editable.length() != new_name.length()) {
                        //remove invalid char
                        final EditText et_sync_folder_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_directory_name);
                        et_sync_folder_dir_name.setText(new_name);
                        if (new_name.length() > 0)
                            et_sync_folder_dir_name.setSelection(new_name.length());
//                        Toast.makeText(mContext, mContext.getString(R.string.msgs_profile_sync_task_dlg_dir_name_has_invalid_char), Toast.LENGTH_SHORT).show();
                        mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_dir_name_has_invalid_char)
                                , "", null);
                    } else {
                        if (new_name.startsWith("/")) {
                            new_name=new_name.substring(1);
                            et_sync_folder_dir_name.setText(new_name);
                            mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_file_directory_name_first_char_has_invalid_char), "", null);
                        }
                    }
                    setSyncFolderArchiveFileImage(dialog, sti, new_name, true);
                }
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });

        sp_sync_folder_mp.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        btn_sync_folder_list_dir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sel = sp_sync_folder_type.getSelectedItem().toString();

                String url = "";
                if (sp_sync_folder_mp.getSelectedItem()==null) url=mGp.internalRootDirectory;
                else url=sp_sync_folder_mp.getSelectedItem().toString();
                String p_dir = et_sync_folder_dir_name.getText().toString();

                NotifyEvent ntfy = new NotifyEvent(mContext);
                //Listen setRemoteShare response
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context arg0, Object[] arg1) {
                        final EditText et_sync_folder_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_directory_name);
                        String dir_tmp=(String)arg1[1]+"/"+(String)arg1[2];
                        String dir = formatSyncFolderDirectoryName(dir_tmp);
                        et_sync_folder_dir_name.setText(dir);
                    }

                    @Override
                    public void negativeResponse(Context arg0, Object[] arg1) {
                        setDialogMsg(dlg_msg, "");
                    }
                });
                if (sfev.folder_master) mCommonDlg.fileSelectorDirOnlyHideMP(false, url, "/", mContext.getString(R.string.msgs_select_local_dir), ntfy);
                else mCommonDlg.fileSelectorDirOnlyWithCreateHideMP(false, url, "/", mContext.getString(R.string.msgs_select_local_dir), ntfy);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_YEAR);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_month, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_MONTH);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day_of_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR);

        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_number, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_NUMBER);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_day, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_day_long, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY_LONG);

        setWeekDayButtonLabel(btn_dir_view_keyword_insert_week_day, btn_dir_view_keyword_insert_week_day_long,
                mContext.getString(R.string.msg_edit_sync_folder_keyword_insert_week_day),
                mContext.getString(R.string.msg_edit_sync_folder_keyword_insert_week_day_long));

    }

    private String formatSyncFolderDirectoryName(String in) {//remove redundant "//" in path name
        String out=in;
        String remove_redundant_sep= mUtil.removeRedundantDirectorySeparator(in);
        if (remove_redundant_sep.equals("/")) out="";
        else {
            if (out.startsWith("/")) out=remove_redundant_sep.substring(1);
            if (out.endsWith("/")) out=out.substring(0, out.length() - 1);
        }
        return out;
    }

    private void setSyncFolderSdcardListener(final Dialog dialog, final SyncTaskItem sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final Button btn_sync_folder_ok = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_ok);

        final Button btn_sync_folder_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_sdcard_directory_btn);
        if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_list_dir, false);

        final EditText et_sync_folder_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_directory_name);
        et_sync_folder_dir_name.setText(sfev.folder_directory);
        final Button btn_sdcard_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_select_document_tree);

        final LinearLayout ll_dir_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_directory_view);

        final Button btn_dir_view_keyword_insert_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_year);
        final Button btn_dir_view_keyword_insert_month = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_month);
        final Button btn_dir_view_keyword_insert_day = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day);
        final Button btn_dir_view_keyword_insert_day_of_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day_of_year);

        final Button btn_dir_view_keyword_insert_week_number = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_week_number);
        final Button btn_dir_view_keyword_insert_week_day = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_weekday);
        final Button btn_dir_view_keyword_insert_week_day_long = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_weekday_long);

        final CheckedTextView ctv_sdcard_use_taken_date_time_for_directory_keyword = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_use_taken_date_time_for_directory_keyword);
        if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR) || sfev.folder_master) {
            ctv_sdcard_use_taken_date_time_for_directory_keyword.setVisibility(CheckedTextView.GONE);
        } else {
            ctv_sdcard_use_taken_date_time_for_directory_keyword.setVisibility(CheckedTextView.VISIBLE);
        }
        ctv_sdcard_use_taken_date_time_for_directory_keyword.setChecked(sfev.folder_use_taken_date_time_for_directory_keyword);
        ctv_sdcard_use_taken_date_time_for_directory_keyword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });

        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_YEAR);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_month, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_MONTH);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day_of_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR);

        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_number, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_NUMBER);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_day, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_day_long, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY_LONG);

        setWeekDayButtonLabel(btn_dir_view_keyword_insert_week_day, btn_dir_view_keyword_insert_week_day_long,
                mContext.getString(R.string.msg_edit_sync_folder_keyword_insert_week_day),
                mContext.getString(R.string.msg_edit_sync_folder_keyword_insert_week_day_long));

        et_sync_folder_dir_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    for(int i = editable.length()-1; i >= 0; i--){
                        if(editable.charAt(i) == '\n'){
                            editable.delete(i, i + 1);
                            return;
                        }
                    }

                    String new_name = removeInvalidCharForFileDirName(editable.toString());
                    if (editable.length() != new_name.length()) {
                        //remove invalid char
                        et_sync_folder_dir_name.setText(new_name);
                        if (new_name.length() > 0)
                            et_sync_folder_dir_name.setSelection(new_name.length());
                        mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_dir_name_has_invalid_char)
                                , "", null);
                    } else {
                        if (new_name.startsWith("/")) {
                            new_name=new_name.substring(1);
                            et_sync_folder_dir_name.setText(new_name);
                            mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_file_directory_name_first_char_has_invalid_char), "", null);
                        }
                    }
                    setSyncFolderArchiveFileImage(dialog, sti, new_name, true);
                }
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });
        btn_sync_folder_list_dir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sel = sp_sync_folder_type.getSelectedItem().toString();
                String url = mGp.safMgr.getSdcardRootPath();
                String p_dir = et_sync_folder_dir_name.getText().toString();

                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context arg0, Object[] arg1) {
                        final EditText et_sync_folder_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_directory_name);
                        if (((String)arg1[1]).length()>0) {
                            String dir_tmp=(String)arg1[1]+"/"+(String)arg1[2];
                            String dir = formatSyncFolderDirectoryName(dir_tmp);
                            et_sync_folder_dir_name.setText(dir);
                        } else {
                            et_sync_folder_dir_name.setText("");
                        }
                    }

                    @Override
                    public void negativeResponse(Context arg0, Object[] arg1) {
                        setDialogMsg(dlg_msg, "");
                    }
                });
                mCommonDlg.fileSelectorDirOnlyWithCreateHideMP(false, url, "", mContext.getString(R.string.msgs_select_sdcard_dir), ntfy);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });

//        if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) btn_sdcard_select_sdcard.setVisibility(Button.VISIBLE);
//        else btn_sdcard_select_sdcard.setVisibility(Button.GONE);
        btn_sdcard_select_sdcard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                            if (Build.VERSION.SDK_INT>=30) {
                                dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted));
                                dlg_msg.setVisibility(TextView.VISIBLE);
                                btn_sdcard_select_sdcard.setVisibility(Button.GONE);
                            } else {
                                dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_press_select_btn));
                                dlg_msg.setVisibility(TextView.VISIBLE);
                                CommonDialog.setViewEnabled(getActivity(), btn_sdcard_select_sdcard, true);
                                btn_sdcard_select_sdcard.setVisibility(Button.VISIBLE);
                                CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_list_dir, false);
                            }

                        } else {
                            CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_list_dir, true);
                            dlg_msg.setVisibility(TextView.GONE);
                            dlg_msg.setText("");
                            CommonDialog.setViewEnabled(getActivity(), btn_sdcard_select_sdcard, true);
                            btn_sdcard_select_sdcard.setVisibility(Button.GONE);
                            checkSyncFolderValidation(dialog, sfev);
                            setSyncFolderOkButtonEnabled(btn_sync_folder_ok, true);
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                ((ActivityMain) getActivity()).invokeSdcardSelector(ntfy);
            }
        });

    }

    private void setSyncFolderUsbListener(final Dialog dialog, final SyncTaskItem sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final Button btn_sync_folder_ok = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_ok);

        final Button btn_sync_folder_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_usb_directory_btn);
        if (mGp.safMgr.getUsbRootPath().equals(SafManager.UNKNOWN_USB_DIRECTORY)) CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_list_dir, false);

        final EditText et_sync_folder_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_usb_directory_name);
        et_sync_folder_dir_name.setText(sfev.folder_directory);
        final Button btn_select_usb = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_usb_select_document_tree);

        final LinearLayout ll_dir_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_usb_directory_view);

        final Button btn_dir_view_keyword_insert_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_year);
        final Button btn_dir_view_keyword_insert_month = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_month);
        final Button btn_dir_view_keyword_insert_day = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day);
        final Button btn_dir_view_keyword_insert_day_of_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day_of_year);

        final Button btn_dir_view_keyword_insert_week_number = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_week_number);
        final Button btn_dir_view_keyword_insert_week_day = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_weekday);
        final Button btn_dir_view_keyword_insert_week_day_long = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_weekday_long);

        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_YEAR);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_month, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_MONTH);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day_of_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR);

        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_number, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_NUMBER);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_day, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_week_day_long, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY_LONG);

        setWeekDayButtonLabel(btn_dir_view_keyword_insert_week_day, btn_dir_view_keyword_insert_week_day_long,
                mContext.getString(R.string.msg_edit_sync_folder_keyword_insert_week_day),
                mContext.getString(R.string.msg_edit_sync_folder_keyword_insert_week_day_long));

        final CheckedTextView ctv_usb_use_taken_date_time_for_directory_keyword = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_usb_use_taken_date_time_for_directory_keyword);
        if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR) || sfev.folder_master) {
            ctv_usb_use_taken_date_time_for_directory_keyword.setVisibility(CheckedTextView.GONE);
        } else {
            ctv_usb_use_taken_date_time_for_directory_keyword.setVisibility(CheckedTextView.VISIBLE);
        }
        ctv_usb_use_taken_date_time_for_directory_keyword.setChecked(sfev.folder_use_taken_date_time_for_directory_keyword);
        ctv_usb_use_taken_date_time_for_directory_keyword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });

        et_sync_folder_dir_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    for(int i = editable.length()-1; i >= 0; i--){
                        if(editable.charAt(i) == '\n'){
                            editable.delete(i, i + 1);
                            return;
                        }
                    }

                    String new_name = removeInvalidCharForFileDirName(editable.toString());
                    if (editable.length() != new_name.length()) {
                        //remove invalid char
                        et_sync_folder_dir_name.setText(new_name);
                        if (new_name.length() > 0)
                            et_sync_folder_dir_name.setSelection(new_name.length());
                        mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_dir_name_has_invalid_char)
                                , "", null);
                    } else {
                        if (new_name.startsWith("/")) {
                            new_name=new_name.substring(1);
                            et_sync_folder_dir_name.setText(new_name);
                            mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_file_directory_name_first_char_has_invalid_char), "", null);
                        }
                    }
                    setSyncFolderArchiveFileImage(dialog, sti, new_name, true);
                }
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });
        btn_sync_folder_list_dir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sel = sp_sync_folder_type.getSelectedItem().toString();
                String url = mGp.safMgr.getUsbRootPath();
                String p_dir = et_sync_folder_dir_name.getText().toString();

                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context arg0, Object[] arg1) {
                        final EditText et_sync_folder_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_usb_directory_name);
                        if (((String)arg1[1]).length()>0) {
                            String dir_tmp=(String)arg1[1]+"/"+(String)arg1[2];
                            String dir = formatSyncFolderDirectoryName(dir_tmp);
                            et_sync_folder_dir_name.setText(dir);
                        } else {
                            et_sync_folder_dir_name.setText("");
                        }
                    }

                    @Override
                    public void negativeResponse(Context arg0, Object[] arg1) {
                        setDialogMsg(dlg_msg, "");
                    }
                });
                mCommonDlg.fileSelectorDirOnlyWithCreateHideMP(false, url, "", mContext.getString(R.string.msgs_select_usb_dir), ntfy);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });

        btn_select_usb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isUsbMountPointExists(mContext, mUtil, getUsbDeviceUuid(mContext, mUtil))) {
                    mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_usb_mount_point_not_exists), "", null);
                } else {
                    NotifyEvent ntfy = new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context c, Object[] o) {
                            if (mGp.safMgr.getUsbRootPath().equals(SafManager.UNKNOWN_USB_DIRECTORY)) {
                                dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_usb_not_auth_press_select_btn));
                                dlg_msg.setVisibility(TextView.VISIBLE);
                                CommonDialog.setViewEnabled(getActivity(), btn_select_usb, true);
                                btn_select_usb.setVisibility(Button.VISIBLE);
                                CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_list_dir, false);
                            } else {
                                CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_list_dir, true);
                                dlg_msg.setVisibility(TextView.GONE);
                                dlg_msg.setText("");
                                CommonDialog.setViewEnabled(getActivity(), btn_select_usb, true);
                                btn_select_usb.setVisibility(Button.GONE);
                                checkSyncFolderValidation(dialog, sfev);
                                setSyncFolderOkButtonEnabled(btn_sync_folder_ok, true);
                            }
                        }

                        @Override
                        public void negativeResponse(Context c, Object[] o) {
                        }
                    });
                    ((ActivityMain) getActivity()).invokeUsbSelector(ntfy);
                }
            }
        });

    }

    private void setWeekDayButtonLabel(Button short_button, Button long_button, String short_label, String long_label) {
        String s_sunday=SyncThread.replaceKeywordValue(SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY, (new Date("2020/07/26").getTime()));
        String s_monday=SyncThread.replaceKeywordValue(SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY, (new Date("2020/07/27").getTime()));
        String l_sunday=SyncThread.replaceKeywordValue(SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY_LONG, (new Date("2020/07/26").getTime()));
        String l_monday=SyncThread.replaceKeywordValue(SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY_LONG, (new Date("2020/07/27").getTime()));

        String new_short_label=String.format(short_label, s_sunday, s_monday);
        String new_long_label=String.format(long_label, l_sunday, l_monday);

        short_button.setText(new_short_label);
        long_button.setText(new_long_label);

    }

    private void setSyncFolderArchiveListener(final Dialog dialog, final SyncTaskItem n_sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {

        final LinearLayout archive_option_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_option_view);
//        archive_option_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        if (n_sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
            if (sfev.folder_master) archive_option_view.setVisibility(LinearLayout.GONE);
            else archive_option_view.setVisibility(LinearLayout.VISIBLE);
        } else {
            archive_option_view.setVisibility(LinearLayout.GONE);
        }

        TextView dlg_file_type = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_file_type);
        String file_type_list="", file_type_sep="";
        for(String item:ARCHIVE_FILE_TYPE) {
            file_type_list+=file_type_sep+item;
            file_type_sep=", ";
        }
        dlg_file_type.setText(file_type_list);

        final Spinner sp_sync_retain_period = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_retention_period);
        final Spinner sp_sync_suffix_option = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_suffix_option);
        setSpinnerSyncTaskArchiveSuffixSeq(sp_sync_suffix_option, sfev.archive_append_seq_number);
        setSpinnerSyncTaskPictureRetainPeriod(sp_sync_retain_period, sfev.archive_retention_period);

        final CheckedTextView ctvConfirmExifDate = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_confirm_exif_date);
        final CheckedTextView ctvRenameWhenArchive = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_rename_when_archive);

        final Button btn_date = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_file_date);
        final Button btn_time = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_file_time);
        final Button btn_yyyymmdd = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_file_yyyymmdd);
        final Button btn_hhmmss = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_file_hhmmss);
        final Button btn_original_name = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_file_original_name);
        final LinearLayout template_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_template_view);
        final EditText et_file_template = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_file_name_template);
        et_file_template.setText(sfev.archive_file_name_template);
        final TextView tv_template = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_new_name);
//        tv_template.setTextColor(mGp.themeColorList.text_color_primary);

        final CheckedTextView ctvCreateDircetory = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_use_archive_directory);
        final LinearLayout dirTemplateView = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_directory_template_btn_view);
        final Button btn_dir_year = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_directory_year);
        final Button btn_dir_month = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_directory_month);
        final Button btn_dir_day = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_directory_day);
        final EditText et_dir_template = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_directory_name_template);
        et_dir_template.setText(sfev.archive_directory_name_template);

        ctvConfirmExifDate.setChecked(sfev.archive_confirm_exif_date);
        ctvConfirmExifDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctvConfirmExifDate.toggle();
                boolean isChecked=ctvConfirmExifDate.isChecked();
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        ctvRenameWhenArchive.setChecked(sfev.archive_rename_when_archive);

        if (!sfev.archive_rename_when_archive) template_view.setVisibility(LinearLayout.GONE);
        else template_view.setVisibility(LinearLayout.VISIBLE);
        ctvRenameWhenArchive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctvRenameWhenArchive.toggle();
                boolean isChecked=ctvRenameWhenArchive.isChecked();
                if (isChecked) template_view.setVisibility(LinearLayout.VISIBLE);
                else template_view.setVisibility(LinearLayout.GONE);
                tv_template.setText(getSyncTaskArchiveTemplateNewName(isChecked, sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        ctvCreateDircetory.setChecked(sfev.archive_create_directory);
        if (ctvCreateDircetory.isChecked()) dirTemplateView.setVisibility(LinearLayout.VISIBLE);
        else dirTemplateView.setVisibility(LinearLayout.GONE);
        ctvCreateDircetory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctvCreateDircetory.toggle();
                boolean isChecked=ctvCreateDircetory.isChecked();
                if (isChecked) dirTemplateView.setVisibility(LinearLayout.VISIBLE);
                else dirTemplateView.setVisibility(LinearLayout.GONE);
                tv_template.setText(getSyncTaskArchiveTemplateNewName(isChecked, sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        sp_sync_retain_period.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        sp_sync_suffix_option.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        btn_date.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String kw_text=SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_DATE;
                if (et_file_template.getSelectionStart() == et_file_template.getSelectionEnd()) et_file_template.getText().insert(et_file_template.getSelectionStart(), kw_text);
                else et_file_template.getText().replace(et_file_template.getSelectionStart(), et_file_template.getSelectionEnd(), kw_text);
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        btn_time.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String kw_text=SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_TIME;
                if (et_file_template.getSelectionStart() == et_file_template.getSelectionEnd()) et_file_template.getText().insert(et_file_template.getSelectionStart(), kw_text);
                else et_file_template.getText().replace(et_file_template.getSelectionStart(), et_file_template.getSelectionEnd(), kw_text);
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        btn_yyyymmdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String kw_text=SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_YYYYMMDD;
                if (et_file_template.getSelectionStart() == et_file_template.getSelectionEnd()) et_file_template.getText().insert(et_file_template.getSelectionStart(), kw_text);
                else et_file_template.getText().replace(et_file_template.getSelectionStart(), et_file_template.getSelectionEnd(), kw_text);
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        btn_hhmmss.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String kw_text=SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_HHMMSS;
                if (et_file_template.getSelectionStart() == et_file_template.getSelectionEnd()) et_file_template.getText().insert(et_file_template.getSelectionStart(), kw_text);
                else et_file_template.getText().replace(et_file_template.getSelectionStart(), et_file_template.getSelectionEnd(), kw_text);
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        btn_original_name.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String kw_text=SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_ORIGINAL_NAME;
                if (et_file_template.getSelectionStart() == et_file_template.getSelectionEnd()) et_file_template.getText().insert(et_file_template.getSelectionStart(), kw_text);
                else et_file_template.getText().replace(et_file_template.getSelectionStart(), et_file_template.getSelectionEnd(), kw_text);
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        et_file_template.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length()>0) {
                    for(int i = editable.length()-1; i >= 0; i--){
                        if(editable.charAt(i) == '\n'){
                            editable.delete(i, i + 1);
                            return;
                        }
                    }
                }

                String new_temp=removeInvalidCharForFileDirName(editable.toString()).replaceAll("/","");;
                if (new_temp.length()!=editable.length()) {
                    et_file_template.setText(new_temp);
                    et_file_template.setSelection(new_temp.length());
                    mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_file_name_has_invalid_char)
                            , "", null);

                }
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        btn_dir_year.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String kw_text=SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_YEAR;
                if (et_dir_template.getSelectionStart() == et_dir_template.getSelectionEnd()) et_dir_template.getText().insert(et_dir_template.getSelectionStart(), kw_text);
                else et_dir_template.getText().replace(et_dir_template.getSelectionStart(), et_dir_template.getSelectionEnd(), kw_text);
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        btn_dir_month.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String kw_text=SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_MONTH;
                if (et_dir_template.getSelectionStart() == et_dir_template.getSelectionEnd()) et_dir_template.getText().insert(et_dir_template.getSelectionStart(), kw_text);
                else et_dir_template.getText().replace(et_dir_template.getSelectionStart(), et_dir_template.getSelectionEnd(), kw_text);
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        btn_dir_day.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String kw_text=SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_DAY;
                if (et_dir_template.getSelectionStart() == et_dir_template.getSelectionEnd()) et_dir_template.getText().insert(et_dir_template.getSelectionStart(), kw_text);
                else et_dir_template.getText().replace(et_dir_template.getSelectionStart(), et_dir_template.getSelectionEnd(), kw_text);
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        et_dir_template.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                String new_temp=removeInvalidCharForFileDirName(editable.toString()).replaceAll("/","");
                if (new_temp.length()!=editable.length()) {
                    et_dir_template.setText(new_temp);
                    et_dir_template.setSelection(new_temp.length());
                    mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_dir_name_has_invalid_char)
                            , "", null);

                }
                tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                        et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));
                checkArchiveOkButtonEnabled(n_sti, dialog);
            }
        });

        tv_template.setText(getSyncTaskArchiveTemplateNewName(ctvCreateDircetory.isChecked(), sp_sync_suffix_option.getSelectedItemPosition(),
                et_file_template.getText().toString(), et_dir_template.getText().toString(), sfev.folder_directory, n_sti));

        dialog.show();
    }

    private void setSyncFolderArchiveFileImage(final Dialog dialog, SyncTaskItem n_sti, final String target_dir, boolean create_directory) {
        final Spinner sp_sync_retain_period = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_retention_period);
        final Spinner sp_sync_suffix_option = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_suffix_option);

        final CheckedTextView ctvConfirmExifDate = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_confirm_exif_date);
        final CheckedTextView ctvRenameWhenArchive = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_rename_when_archive);

        final Button btn_date = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_file_date);
        final Button btn_time = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_file_time);
        final Button btn_original_name = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_file_original_name);
        final LinearLayout template_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_template_view);
        final EditText et_file_template = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_file_name_template);
        final TextView tv_template = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_new_name);

        final CheckedTextView ctvCreateDircetory = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_use_archive_directory);
        final LinearLayout dirTemplateView = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_directory_template_btn_view);
        final Button btn_dir_year = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_directory_year);
        final Button btn_dir_month = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_directory_month);
        final Button btn_dir_day = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_btn_directory_day);
        final EditText et_dir_template = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_directory_name_template);

        tv_template.setText(getSyncTaskArchiveTemplateNewName(create_directory, sp_sync_suffix_option.getSelectedItemPosition(),
                et_file_template.getText().toString(), et_dir_template.getText().toString(), target_dir, n_sti));

    }

    private void setSyncFolderZipListener(final Dialog dialog, final SyncTaskItem sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final Button btn_sync_folder_ok = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_ok);

        final Button btn_sdcard_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_select_document_tree);

        final LinearLayout ll_zip_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_view);

        final Button btn_zip_view_keyword_insert_year = (Button) ll_zip_view.findViewById(R.id.edit_sync_folder_keyword_insert_year);
        final Button btn_zip_view_keyword_insert_month = (Button) ll_zip_view.findViewById(R.id.edit_sync_folder_keyword_insert_month);
        final Button btn_zip_view_keyword_insert_day = (Button) ll_zip_view.findViewById(R.id.edit_sync_folder_keyword_insert_day);
        final Button btn_zip_view_keyword_insert_day_of_year = (Button) ll_zip_view.findViewById(R.id.edit_sync_folder_keyword_insert_day_of_year);

        final CheckedTextView ctv_zip_file_save_sdcard = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_use_sdcard);

        final Button btn_zip_filelist = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_filelist_btn);
        final EditText et_zip_file = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_name);
        final TextView tv_zip_dir = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_dir_name);

        setSyncFolderkeywordButtonListener(dialog, sfev, btn_zip_view_keyword_insert_year, et_zip_file, SMBSYNC2_REPLACEABLE_KEYWORD_YEAR);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_zip_view_keyword_insert_month, et_zip_file, SMBSYNC2_REPLACEABLE_KEYWORD_MONTH);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_zip_view_keyword_insert_day, et_zip_file, SMBSYNC2_REPLACEABLE_KEYWORD_DAY);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_zip_view_keyword_insert_day_of_year, et_zip_file, SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR);

        String zip_dir="", zip_file="";
        if (sfev.zip_file_name.lastIndexOf("/")>1) {
            //Directory付
            zip_dir=sfev.zip_file_name.substring(0,sfev.zip_file_name.lastIndexOf("/"));
            zip_file=sfev.zip_file_name.substring(sfev.zip_file_name.lastIndexOf("/")+1);
        } else {
            zip_dir="/";
            if (sfev.zip_file_name.length()!=0) zip_file=sfev.zip_file_name.substring(1);
        }
        if (!sfev.zip_file_use_sdcard) {
            tv_zip_dir.setText(zip_dir.replace(mGp.internalRootDirectory, ""));
            et_zip_file.setText(zip_file);
        } else {
            tv_zip_dir.setText(zip_dir.replace(mGp.safMgr.getSdcardRootPath(), ""));
            et_zip_file.setText(zip_file);
        }

        final Spinner sp_comp_level = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_comp_level);
        setSpinnerSyncFolderZipCompressionLevel(sp_comp_level, sfev.zip_comp_level);

        final Spinner sp_zip_enc_method=(Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_method);
        setSpinnerSyncFolderZipEncryptMethod(sp_zip_enc_method, sfev.zip_enc_method);
        final LinearLayout ll_zip_password_view=(LinearLayout)dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_password_view);
        final EditText et_zip_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_password);
        final EditText et_zip_conf_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_confirm);
//        final Button btn_zip_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_select_document_tree);


        if (sfev.zip_enc_method.equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_NONE)) {
            ll_zip_password_view.setVisibility(LinearLayout.GONE);
        } else {
            ll_zip_password_view.setVisibility(LinearLayout.VISIBLE);
        }
        et_zip_pswd.setText(sfev.zip_file_password);
        et_zip_conf_pswd.setText(sfev.zip_file_confirm_password);
        sp_zip_enc_method.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String zip_enc_method=(String)sp_zip_enc_method.getSelectedItem();
                if (!zip_enc_method.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_encrypt_none)))
                    ll_zip_password_view.setVisibility(LinearLayout.VISIBLE);
                else ll_zip_password_view.setVisibility(LinearLayout.GONE);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
                if (dlg_msg.getText().length()==0) checkSyncFolderValidation(dialog, sfev);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btn_sdcard_select_sdcard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                            if (Build.VERSION.SDK_INT>=30) {
                                dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted));
                                dlg_msg.setVisibility(TextView.VISIBLE);
                                btn_sdcard_select_sdcard.setVisibility(Button.GONE);
                            } else {
                                dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_press_select_btn));
                                dlg_msg.setVisibility(TextView.VISIBLE);
                                CommonDialog.setViewEnabled(getActivity(), btn_sdcard_select_sdcard, true);
                                CommonDialog.setViewEnabled(getActivity(), btn_zip_filelist, false);
                                btn_sdcard_select_sdcard.setVisibility(Button.VISIBLE);
                            }
                        } else {
                            CommonDialog.setViewEnabled(getActivity(), btn_zip_filelist, true);
                            dlg_msg.setVisibility(TextView.GONE);
                            dlg_msg.setText("");
                            CommonDialog.setViewEnabled(getActivity(), btn_sdcard_select_sdcard, true);
                            checkSyncFolderValidation(dialog, sfev);
                            setSyncFolderOkButtonEnabled(btn_sync_folder_ok, true);
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                ((ActivityMain) getActivity()).invokeSdcardSelector(ntfy);
            }
        });

        sp_comp_level.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ctv_zip_file_save_sdcard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctv_zip_file_save_sdcard.isChecked();
                ctv_zip_file_save_sdcard.setChecked(isChecked);
                setSyncFolderViewVisibility(dialog, sfev.folder_master, sfev);
//				checkSyncFolderValidation(dialog);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });
        ctv_zip_file_save_sdcard.setChecked(false);
        if (sfev.zip_file_use_sdcard) ctv_zip_file_save_sdcard.performClick();

        btn_zip_filelist.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        final CheckedTextView ctv_zip_file_save_sdcard = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_use_sdcard);
                        final EditText et_zip_file = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_name);
                        final TextView tv_zip_dir = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_dir_name);
                        String zip_dir=((String)o[1]).equals("")?"/":((String)o[1]);
                        String zip_file=(String) o[2];
                        if (!ctv_zip_file_save_sdcard.isChecked()) {
                            tv_zip_dir.setText(zip_dir.replace(mGp.internalRootDirectory, ""));
                            et_zip_file.setText(zip_file.startsWith("/")?zip_file.substring(1):zip_file);
                        } else {
                            tv_zip_dir.setText(zip_dir.replace(mGp.safMgr.getSdcardRootPath(), ""));
                            et_zip_file.setText(zip_file.startsWith("/")?zip_file.substring(1):zip_file);
                        }
                        setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                String title = mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_select_file_title);
                if (!ctv_zip_file_save_sdcard.isChecked()) mCommonDlg.fileSelectorFileOnlyWithCreateHideMP(true, mGp.internalRootDirectory, "", "", title, ntfy);
                else mCommonDlg.fileSelectorFileOnlyWithCreateHideMP(true, mGp.safMgr.getSdcardRootPath(), "", "", title, ntfy);
            }
        });
        et_zip_file.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    for(int i = s.length()-1; i >= 0; i--){
                        if(s.charAt(i) == '\n'){
                            s.delete(i, i + 1);
                            return;
                        }
                    }
                    String new_name = removeInvalidCharForFileName(s.toString());
                    if (s.length() != new_name.length()) {
                        //remove invalid char
                        et_zip_file.setText(new_name);
                        if (new_name.length() > 0) et_zip_file.setSelection(new_name.length());
                        mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_file_name_has_invalid_char)
                                , "", null);
                    } else {
                        if (new_name.startsWith("/")) {
                            new_name=new_name.substring(1);
                            et_zip_file.setText(new_name);
                            mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_file_directory_name_first_char_has_invalid_char), "", null);
                        }
                    }
                }
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        et_zip_pswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) CommonDialog.setViewEnabled(getActivity(), et_zip_conf_pswd, true);
                else CommonDialog.setViewEnabled(getActivity(), et_zip_conf_pswd, false);
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        if (et_zip_pswd.getText().length() > 0) CommonDialog.setViewEnabled(getActivity(), et_zip_conf_pswd, true);
        else CommonDialog.setViewEnabled(getActivity(), et_zip_conf_pswd, false);

        et_zip_conf_pswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                checkSyncFolderValidation(dialog, sfev);
            }
        });
    }

    private Dialog mEditFolderDialog=null;
    private SyncFolderEditValue mEditFolderSfev=null;
    private SyncTaskItem mEditFolderSti=null;
    private NotifyEvent mEditFolderNotify=null;
    private void editSyncFolder(boolean rebuild, SyncTaskItem sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {
        mUtil.addDebugMsg(1, "I", "editSyncFolder entered");
        Dialog t_dialog = null;
        if (rebuild) {
            t_dialog = mEditFolderDialog;
            t_dialog.setContentView(R.layout.edit_sync_folder_dlg);
        } else {
            t_dialog = new Dialog(this.getActivity(), mGp.applicationTheme);
            mEditFolderDialog=t_dialog;
            mEditFolderSfev=sfev;
            mEditFolderSti=sti;
            mEditFolderNotify=ntfy;
            t_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            t_dialog.setCanceledOnTouchOutside(false);
            t_dialog.setContentView(R.layout.edit_sync_folder_dlg);
        }
        final Dialog dialog = t_dialog;
        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        TextView dlg_title = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_title);
        dlg_title.setBackgroundColor(mGp.themeColorList.title_background_color);
        dlg_title.setTextColor(mGp.themeColorList.title_text_color);
        dlg_title.setText(sfev.folder_title);

        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);

        final Spinner spinnerSyncType = (Spinner) mDialog.findViewById(R.id.edit_sync_task_sync_type);

        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
 //       setSpinnerSyncFolderType(sti, sp_sync_folder_type, sfev.folder_type, sfev.folder_master);
        setSpinnerSyncFolderTypeWithUsb(sti, sp_sync_folder_type, sfev.folder_type, sfev.folder_master);

        if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
            if (sfev.folder_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                mUtil.showCommonDialog(false, "W",
                        mContext.getString(R.string.msgs_sync_folder_archive_zip_folder_not_supported), "", null);
            }
        }

        setSyncFolderSmbListener(dialog, sti, sfev, ntfy);
        setSyncFolderInternalListener(dialog, sti, sfev, ntfy);
        setSyncFolderSdcardListener(dialog, sti, sfev, ntfy);
        setSyncFolderUsbListener(dialog, sti, sfev, ntfy);
        setSyncFolderArchiveListener(dialog, sti, sfev, ntfy);
        setSyncFolderZipListener(dialog, sti, sfev, ntfy);

        setSyncFolderFieldHelpListener(dialog, sfev.folder_type);

        final Button btn_sync_folder_cancel = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_cancel);
        final Button btn_sync_folder_ok = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_ok);

        setSyncFolderViewVisibility(dialog, sfev.folder_master, sfev);

        sp_sync_folder_type.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSyncFolderViewVisibility(dialog, sfev.folder_master, sfev);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

//        btn_sync_folder_ok.setEnabled(false);
        setSyncFolderOkButtonEnabled(btn_sync_folder_ok, false);
        btn_sync_folder_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SyncFolderEditValue nsfev = buildSyncFolderEditValue(dialog, sfev);
                nsfev.folder_error_code=SyncTaskItem.SYNC_FOLDER_ERROR_NO_ERROR;
                ntfy.notifyToListener(true, new Object[]{nsfev});
                dialog.dismiss();
                mEditFolderDialog=null;
            }
        });

        btn_sync_folder_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_sync_folder_ok.isEnabled()) {
                    NotifyEvent ntfy = new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            dialog.dismiss();
                            mEditFolderDialog=null;
                        }
                        @Override
                        public void negativeResponse(Context context, Object[] objects) {}
                    });
                    mUtil.showCommonDialog(true, "W",
                            mContext.getString(R.string.msgs_schedule_confirm_title_nosave),
                            mContext.getString(R.string.msgs_profile_sync_folder_dlg_confirm_msg_nosave), ntfy);
                } else {
                    ntfy.notifyToListener(false, null);
                    dialog.dismiss();
                    mEditFolderDialog=null;
                }
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int kc, KeyEvent keyEvent) {
                switch (kc) {
                    case KEYCODE_BACK:
                        if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                            btn_sync_folder_cancel.performClick();
                        }
                        return true;
                    // break;
                    default:
                        // break;
                }

                return false;
            }
        });

        dialog.show();
    }

    private void setSyncFolderkeywordButtonListener(final Dialog dialog, final SyncFolderEditValue sfev,
                                                    final Button btn, final EditText et, final String key_word) {
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et.getSelectionStart() == et.getSelectionEnd())
                    et.getText().insert(et.getSelectionStart(), key_word);
                else et.getText().replace(et.getSelectionStart(), et.getSelectionEnd(), key_word);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });
    }

    private SyncFolderEditValue buildSyncFolderEditValue(Dialog dialog, SyncFolderEditValue org_sfev) {
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final Spinner sp_sync_folder_smb_proto = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_protocol);
        final LinearLayout ll_sync_folder_mp = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_local_mount_point_selector_view);
        final Spinner sp_sync_folder_mp = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_local_mount_point_selector);
        final Button btn_search_host = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_search_remote_host);

        final EditText et_remote_host = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_server);
        final CheckedTextView ctv_sync_folder_smb_ipc_enforced = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_smb_ipc_signing_enforced);
        final CheckedTextView ctv_sync_folder_smb_use_smb2_negotiation = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_smb_use_smb2_negotiation);
        final CheckedTextView ctv_sync_folder_use_port = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_remote_port_number);
        final EditText et_sync_folder_port = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_port);
        final CheckedTextView ctv_sync_folder_use_pswd = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_user_pass);
        final EditText et_sync_folder_domain = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_domain);
        final EditText et_sync_folder_user = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_user);
        final EditText et_sync_folder_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_pass);

        final Button btn_sync_folder_list_share = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_share_btn);
        final EditText et_sync_folder_share_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_share_name);

        final EditText et_sync_folder_internal_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_directory_name);
        final EditText et_sync_folder_smb_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_directory_name);
        final EditText et_sync_folder_sdcard_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_directory_name);
        final EditText et_sync_folder_usb_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_usb_directory_name);


        final LinearLayout ll_dir_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_directory_view);
        final LinearLayout ll_zip_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_view);

        final CheckedTextView ctv_zip_file_save_sdcard = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_use_sdcard);

        final TextView tv_zip_dir = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_dir_name);
        final EditText et_zip_file = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_name);
        final Spinner sp_comp_level = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_comp_level);
        final Spinner sp_zip_enc_method=(Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_method);
        final EditText et_zip_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_password);
        final EditText et_zip_conf_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_confirm);

        final CheckedTextView ctv_internal_use_taken_date_time_for_directory_keyword = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_use_taken_date_time_for_directory_keyword);
        final CheckedTextView ctv_sdcard_use_taken_date_time_for_directory_keyword = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_use_taken_date_time_for_directory_keyword);
        final CheckedTextView ctv_smb_use_taken_date_time_for_directory_keyword = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_use_taken_date_time_for_directory_keyword);
        final CheckedTextView ctv_usb_use_taken_date_time_for_directory_keyword = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_usb_use_taken_date_time_for_directory_keyword);

        SyncFolderEditValue nsfev = org_sfev.clone();
        String sel = sp_sync_folder_type.getSelectedItem().toString();
        if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal))) {//Internal
            String dir= mUtil.removeRedundantDirectorySeparator(et_sync_folder_internal_dir_name.getText().toString().trim());
            nsfev.folder_directory = dir.startsWith("/")?dir.substring(1):dir;
            if (sp_sync_folder_mp.getSelectedItem()==null) nsfev.folder_mountpoint = mGp.internalRootDirectory;
            else nsfev.folder_mountpoint = sp_sync_folder_mp.getSelectedItem().toString().trim();
            nsfev.folder_use_taken_date_time_for_directory_keyword=ctv_internal_use_taken_date_time_for_directory_keyword.isChecked();
            nsfev.folder_type = SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL;
            buildSyncFolderEditValueForArchive(dialog, nsfev);
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard))) {//sdcard
            String dir= mUtil.removeRedundantDirectorySeparator(et_sync_folder_sdcard_dir_name.getText().toString().trim());
            nsfev.folder_directory = dir.startsWith("/")?dir.substring(1):dir;
            nsfev.folder_use_taken_date_time_for_directory_keyword=ctv_sdcard_use_taken_date_time_for_directory_keyword.isChecked();
            nsfev.folder_type = SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD;
            buildSyncFolderEditValueForArchive(dialog, nsfev);
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb))) {//USB Storage
            nsfev.folder_use_taken_date_time_for_directory_keyword=ctv_usb_use_taken_date_time_for_directory_keyword.isChecked();
            String dir= mUtil.removeRedundantDirectorySeparator(et_sync_folder_usb_dir_name.getText().toString().trim());
            nsfev.folder_directory = dir.startsWith("/")?dir.substring(1):dir;
            nsfev.folder_type = SyncTaskItem.SYNC_FOLDER_TYPE_USB;
            buildSyncFolderEditValueForArchive(dialog, nsfev);
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_zip))) {//ZIP
            nsfev.zip_file_use_sdcard = ctv_zip_file_save_sdcard.isChecked();
            String cl = sp_comp_level.getSelectedItem().toString();
            if (cl.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_fastest))) nsfev.zip_comp_level = SyncTaskItem.ZIP_OPTION_COMP_LEVEL_FASTEST;
            else if (cl.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_fast))) nsfev.zip_comp_level = SyncTaskItem.ZIP_OPTION_COMP_LEVEL_FAST;
            else if (cl.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_normal))) nsfev.zip_comp_level = SyncTaskItem.ZIP_OPTION_COMP_LEVEL_NORMAL;
            else if (cl.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_maximum))) nsfev.zip_comp_level = SyncTaskItem.ZIP_OPTION_COMP_LEVEL_MAXIMUM;
            else if (cl.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_ultra))) nsfev.zip_comp_level = SyncTaskItem.ZIP_OPTION_COMP_LEVEL_ULTRA;

            String zip_enc_method = sp_zip_enc_method.getSelectedItem().toString();
            if (zip_enc_method.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_encrypt_none))) nsfev.zip_enc_method = SyncTaskItem.ZIP_OPTION_ENCRYPT_NONE;
            else if (zip_enc_method.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_encrypt_zip_crypto))) nsfev.zip_enc_method = SyncTaskItem.ZIP_OPTION_ENCRYPT_STANDARD;
            else if (zip_enc_method.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_encrypt_aes128))) nsfev.zip_enc_method = SyncTaskItem.ZIP_OPTION_ENCRYPT_AES128;
            else if (zip_enc_method.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_encrypt_aes256))) nsfev.zip_enc_method = SyncTaskItem.ZIP_OPTION_ENCRYPT_AES256;

            if (tv_zip_dir.getText().toString().trim().equals("/")) nsfev.zip_file_name="/"+et_zip_file.getText().toString().trim();
            else nsfev.zip_file_name=tv_zip_dir.getText().toString().trim()+"/"+et_zip_file.getText().toString().trim();
            if (!nsfev.zip_enc_method.equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_NONE)) {
                nsfev.zip_file_password = et_zip_pswd.getText().toString();
                nsfev.zip_file_confirm_password=et_zip_conf_pswd.getText().toString();
            } else {
                nsfev.zip_file_password = "";
            }
            nsfev.folder_type = SyncTaskItem.SYNC_FOLDER_TYPE_ZIP;
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb))) {//smb
            nsfev.folder_use_taken_date_time_for_directory_keyword=ctv_smb_use_taken_date_time_for_directory_keyword.isChecked();
            String dir= mUtil.removeRedundantDirectorySeparator(et_sync_folder_smb_dir_name.getText().toString().trim());
            nsfev.folder_directory = dir.startsWith("/")?dir.substring(1):dir;
            nsfev.folder_type = SyncTaskItem.SYNC_FOLDER_TYPE_SMB;
            buildSyncFolderEditValueForArchive(dialog, nsfev);
            String host=et_remote_host.getText().toString();
            if (CommonUtilities.isIpAddressV6(host) || CommonUtilities.isIpAddressV4(host)) {
                nsfev.folder_remote_addr = host;
                nsfev.folder_remote_host="";
            } else {
                nsfev.folder_remote_host = host;
                nsfev.folder_remote_addr="";
            }
            nsfev.folder_remote_domain = et_sync_folder_domain.getText().toString().trim();
            if (ctv_sync_folder_use_port.isChecked())
                nsfev.folder_remote_port = et_sync_folder_port.getText().toString();
            else nsfev.folder_remote_port = "";
            if (ctv_sync_folder_use_pswd.isChecked()) {
                nsfev.folder_remote_user = et_sync_folder_user.getText().toString().trim();
                nsfev.folder_remote_pswd = et_sync_folder_pswd.getText().toString();
            } else {
                nsfev.folder_remote_user = "";
                nsfev.folder_remote_pswd = "";
            }
            nsfev.folder_remote_use_port_number=ctv_sync_folder_use_port.isChecked();
            nsfev.folder_remote_use_account_name_password=ctv_sync_folder_use_pswd.isChecked();
            nsfev.folder_remote_use_pswd =ctv_sync_folder_use_pswd.isChecked();
            nsfev.folder_remote_share = et_sync_folder_share_name.getText().toString().trim();
            nsfev.folder_smb_protocol=getSmbSelectedProtocol(sp_sync_folder_smb_proto);;
            nsfev.folder_smb_ipc_enforced=ctv_sync_folder_smb_ipc_enforced.isChecked();
            nsfev.folder_smb_use_smb2_negotiation=ctv_sync_folder_smb_use_smb2_negotiation.isChecked();
        }
        return nsfev;
    }

    private void buildSyncFolderEditValueForArchive(Dialog dialog, SyncFolderEditValue nsfev) {
        final Spinner sp_sync_retain_period = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_retention_period);
        final Spinner sp_sync_suffix_option = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_suffix_option);
        final CheckedTextView ctvConfirmExifDate = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_confirm_exif_date);
        final CheckedTextView ctvRenameWhenArchive = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_rename_when_archive);
        final EditText et_file_template = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_file_name_template);
        final CheckedTextView ctvCreateDircetory = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_use_archive_directory);
        final EditText et_dir_template = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_directory_name_template);
        nsfev.archive_retention_period = sp_sync_retain_period.getSelectedItemPosition();
        nsfev.archive_append_seq_number = getArchiveSuffixOptionFromSpinner(sp_sync_suffix_option);
        nsfev.archive_file_name_template = et_file_template.getText().toString();
        nsfev.archive_directory_name_template = et_dir_template.getText().toString();
        nsfev.archive_create_directory = ctvCreateDircetory.isChecked();
        nsfev.archive_confirm_exif_date = ctvConfirmExifDate.isChecked();
        nsfev.archive_rename_when_archive = ctvRenameWhenArchive.isChecked();
    }

    private void setSyncFolderOkButtonEnabled(Button ok_btn, boolean enabled) {
        CommonDialog.setViewEnabled(getActivity(), ok_btn, enabled);
    }

    private void setSyncFolderSmbListDirectoryButtonEnabled(Dialog dialog, boolean enabled) {
        final Button btn_sync_folder_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_smb_directory_btn);
        CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_list_dir, enabled);
//        mUtil.addDebugMsg(1,"I", "button enabled="+enabled);
//        Thread.dumpStack();
    }

    private void setSyncFolderViewVisibility(final Dialog dialog, final boolean master, SyncFolderEditValue org_sfev) {
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final LinearLayout ll_sync_folder_mp = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_local_mount_point_selector_view);
        final Spinner sp_sync_folder_mp = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_local_mount_point_selector);
        final Spinner sp_sync_folder_smb_proto = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_protocol);
        final CheckedTextView ctv_sync_folder_smb_ipc_enforced = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_smb_ipc_signing_enforced);
        final CheckedTextView ctv_sync_folder_smb_use_smb2_negotiation = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_smb_use_smb2_negotiation);
        final LinearLayout ll_sync_folder_smb_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_view);
        final LinearLayout ll_sync_folder_internal_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_view);
        final LinearLayout ll_sync_folder_sdcard_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_view);
        final LinearLayout ll_sync_folder_usb_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_usb_view);
        final LinearLayout ll_sync_folder_zip_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_view);

        final Button btn_sync_folder_list_dirX = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_internal_directory_btn);
        final Button btn_sdcard_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_select_document_tree);
        final Button btn_usb_select_usb = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_usb_select_document_tree);

        final LinearLayout ll_internal_keyword_view = (LinearLayout) ll_sync_folder_internal_view.findViewById(R.id.edit_sync_folder_dlg_internal_dir_keyword_view);
        final LinearLayout ll_smb_keyword_view = (LinearLayout) ll_sync_folder_smb_view.findViewById(R.id.edit_sync_folder_dlg_smb_dir_keyword_view);
        final LinearLayout ll_sdcard_keyword_view = (LinearLayout) ll_sync_folder_sdcard_view.findViewById(R.id.edit_sync_folder_dlg_sdcard_dir_keyword_view);
        final LinearLayout ll_usb_keyword_view = (LinearLayout) ll_sync_folder_usb_view.findViewById(R.id.edit_sync_folder_dlg_usb_dir_keyword_view);
        final LinearLayout ll_zip_keyword_view = (LinearLayout) ll_sync_folder_zip_view.findViewById(R.id.edit_sync_folder_keyword_view);

        final CheckedTextView ctv_zip_file_save_sdcard = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_use_sdcard);
        final Button btn_zip_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_select_document_tree);

//		ctv_sync_folder_use_usb_folder.setVisibility(CheckedTextView.GONE);
        setSyncFolderSmbListDirectoryButtonEnabled(dialog, true);
        String sel = sp_sync_folder_type.getSelectedItem().toString();
        btn_sdcard_select_sdcard.setVisibility(Button.GONE);

        if (master) {
            ll_internal_keyword_view.setVisibility(LinearLayout.VISIBLE);
            ll_sdcard_keyword_view.setVisibility(LinearLayout.VISIBLE);
            ll_usb_keyword_view.setVisibility(LinearLayout.VISIBLE);
            ll_smb_keyword_view.setVisibility(LinearLayout.VISIBLE);
            ll_zip_keyword_view.setVisibility(LinearLayout.GONE);
        } else {
            ll_internal_keyword_view.setVisibility(LinearLayout.VISIBLE);
            ll_sdcard_keyword_view.setVisibility(LinearLayout.VISIBLE);
            ll_usb_keyword_view.setVisibility(LinearLayout.VISIBLE);
            ll_smb_keyword_view.setVisibility(LinearLayout.VISIBLE);
            ll_zip_keyword_view.setVisibility(LinearLayout.VISIBLE);
        }
        if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb))) {
            ll_sync_folder_internal_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_smb_view.setVisibility(LinearLayout.VISIBLE);
            ll_sync_folder_sdcard_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_usb_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_zip_view.setVisibility(LinearLayout.GONE);
            if (getSmbSelectedProtocol(sp_sync_folder_smb_proto).equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1)) {
                ctv_sync_folder_smb_ipc_enforced.setEnabled(false);
                ctv_sync_folder_smb_use_smb2_negotiation.setEnabled(false);
            } else {
                ctv_sync_folder_smb_ipc_enforced.setEnabled(true);
                if (getSmbSelectedProtocol(sp_sync_folder_smb_proto).equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB212) ||
                        getSmbSelectedProtocol(sp_sync_folder_smb_proto).equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB214)) ctv_sync_folder_smb_use_smb2_negotiation.setEnabled(true);
                else ctv_sync_folder_smb_use_smb2_negotiation.setEnabled(false);
            }
            checkSyncFolderValidation(dialog, org_sfev);
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_SMB);
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal))) {
            ll_sync_folder_internal_view.setVisibility(LinearLayout.VISIBLE);
            ll_sync_folder_smb_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_sdcard_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_usb_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_zip_view.setVisibility(LinearLayout.GONE);
            checkSyncFolderValidation(dialog, org_sfev);
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard))) {
            ll_sync_folder_internal_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_smb_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_sdcard_view.setVisibility(LinearLayout.VISIBLE);
            ll_sync_folder_usb_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_zip_view.setVisibility(LinearLayout.GONE);
            checkSyncFolderValidation(dialog, org_sfev);
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD);

            ll_sync_folder_mp.setVisibility(LinearLayout.GONE);
            if (isSdcardDeviceExists(mContext,mUtil)) {
                if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                    if (Build.VERSION.SDK_INT>=30) {
                        dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted));
                        dlg_msg.setVisibility(TextView.VISIBLE);
                        btn_sdcard_select_sdcard.setVisibility(Button.GONE);
                    } else {
                        dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_press_select_btn));
                        dlg_msg.setVisibility(TextView.VISIBLE);
                        CommonDialog.setViewEnabled(getActivity(), btn_sdcard_select_sdcard, true);
                        btn_sdcard_select_sdcard.setVisibility(Button.VISIBLE);
                    }
                    setSyncFolderSmbListDirectoryButtonEnabled(dialog, false);
                } else {
                    setSyncFolderSmbListDirectoryButtonEnabled(dialog, true);
                    dlg_msg.setVisibility(TextView.GONE);
                    dlg_msg.setText("");
                    CommonDialog.setViewEnabled(getActivity(), btn_sdcard_select_sdcard, false);
                    btn_sdcard_select_sdcard.setVisibility(Button.GONE);
                    checkSyncFolderValidation(dialog, org_sfev);
                }
            } else {
                setSyncFolderSmbListDirectoryButtonEnabled(dialog, false);
                dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted));
                dlg_msg.setVisibility(TextView.VISIBLE);
                CommonDialog.setViewEnabled(getActivity(), btn_sdcard_select_sdcard, false);
                btn_sdcard_select_sdcard.setVisibility(Button.GONE);
            }
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD);
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb))) {
            ll_sync_folder_internal_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_smb_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_sdcard_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_usb_view.setVisibility(LinearLayout.VISIBLE);
            ll_sync_folder_zip_view.setVisibility(LinearLayout.GONE);
            checkSyncFolderValidation(dialog, org_sfev);
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD);

            ll_sync_folder_mp.setVisibility(LinearLayout.GONE);
            if (!isUsbDeviceExists(mContext, mUtil)) {
                dlg_msg.setText(mContext.getString(R.string.msgs_main_external_usb_drive_not_found_msg));
                dlg_msg.setVisibility(TextView.VISIBLE);
                btn_usb_select_usb.setVisibility(Button.GONE);
                setSyncFolderSmbListDirectoryButtonEnabled(dialog, false);
            } else {
                if (!isUsbMountPointExists(mContext, mUtil, getUsbDeviceUuid(mContext, mUtil))) {
                    dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_usb_mount_point_not_exists));
                    dlg_msg.setVisibility(TextView.VISIBLE);
                    btn_usb_select_usb.setVisibility(Button.GONE);
                    setSyncFolderSmbListDirectoryButtonEnabled(dialog, false);
                } else {
                    if (mGp.safMgr.getUsbRootPath().equals(SafManager.UNKNOWN_USB_DIRECTORY)) {
                        dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_usb_not_auth_press_select_btn));
                        dlg_msg.setVisibility(TextView.VISIBLE);
                        CommonDialog.setViewEnabled(getActivity(), btn_usb_select_usb, true);
                        btn_usb_select_usb.setVisibility(Button.VISIBLE);
                        setSyncFolderSmbListDirectoryButtonEnabled(dialog, false);
                    } else {
                        setSyncFolderSmbListDirectoryButtonEnabled(dialog, true);
                        dlg_msg.setVisibility(TextView.GONE);
                        dlg_msg.setText("");
                        CommonDialog.setViewEnabled(getActivity(), btn_usb_select_usb, true);
                        btn_usb_select_usb.setVisibility(Button.GONE);
                        checkSyncFolderValidation(dialog, org_sfev);
                    }
                }
            }
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_USB);
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_zip))) {
            ll_sync_folder_internal_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_smb_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_sdcard_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_usb_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_zip_view.setVisibility(LinearLayout.VISIBLE);
            checkSyncFolderValidation(dialog, org_sfev);
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_ZIP);

            if (ctv_zip_file_save_sdcard.isChecked() && Build.VERSION.SDK_INT<=29) {
                ll_sync_folder_mp.setVisibility(Spinner.GONE);
                btn_zip_select_sdcard.setVisibility(Button.VISIBLE);
                if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                    if (Build.VERSION.SDK_INT>=30) {
                        dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted));
                        dlg_msg.setVisibility(TextView.VISIBLE);
                        btn_sdcard_select_sdcard.setVisibility(Button.GONE);
                    } else {
                        dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_press_select_btn));
                        dlg_msg.setVisibility(TextView.VISIBLE);
                        CommonDialog.setViewEnabled(getActivity(), btn_zip_select_sdcard, true);
                        btn_sdcard_select_sdcard.setVisibility(Button.VISIBLE);
                    }
                    setSyncFolderSmbListDirectoryButtonEnabled(dialog, false);
                } else {
                    setSyncFolderSmbListDirectoryButtonEnabled(dialog, true);
                    dlg_msg.setVisibility(TextView.GONE);
                    dlg_msg.setText("");
                    CommonDialog.setViewEnabled(getActivity(), btn_zip_select_sdcard, true);
                    checkSyncFolderValidation(dialog, org_sfev);
                }
            } else {
                btn_zip_select_sdcard.setVisibility(Button.GONE);
                checkSyncFolderValidation(dialog, org_sfev);
            }
        }
    }

    static public boolean isSdcardDeviceExists(Context c, CommonUtilities cu) {
        boolean result=false;
        if (Build.VERSION.SDK_INT>=24) {
            StorageVolume sv=getSdcardStorageVolume(c, cu);
            if (sv!=null) result=true;
        } else {
            result=isStorageVolumeExistsApi23(c, cu, "SD");
        }
        return result;
    }

    static public boolean isUsbDeviceExists(Context c, CommonUtilities cu) {
        boolean result=false;
        if (Build.VERSION.SDK_INT>=24) {
            StorageVolume sv=getStorageVolume(c, cu, "USB");
            if (sv!=null) result=true;
        } else {
            result=isStorageVolumeExistsApi23(c, cu, "USB");
        }
        return result;
    }

    static public String getUsbDeviceUuid(Context c, CommonUtilities cu) {
        String result=null;
        if (Build.VERSION.SDK_INT>=24) {
            StorageVolume sv=getStorageVolume(c, cu, "USB");
            if (sv!=null) result=sv.getUuid();
        } else {
            result=getStorageVolumeUuidApi23(c, cu, "USB");
        }
        return result;
    }

    static public boolean isUsbMountPointExists(Context c, CommonUtilities cu, String uuid) {
        boolean result=false;
        File usb_mp=new File("/storage/"+uuid);
        result=usb_mp.exists();
        return result;
    }

    static private boolean isStorageVolumeExistsApi23(Context c, CommonUtilities cu, String type) {
        boolean exists=false;
        if (getStorageVolumeUuidApi23(c, cu, type)!=null) exists=true;
        return exists;
    }

    static private String getStorageVolumeUuidApi23(Context c, CommonUtilities cu, String type) {
        String result=null;
        try {
            StorageManager sm = (StorageManager) c.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList = sm.getClass().getDeclaredMethod("getVolumeList");
            Object[] volumeList = (Object[]) getVolumeList.invoke(sm);
            for (Object volume : volumeList) {
	            Method isRemovable = volume.getClass().getDeclaredMethod("isRemovable");
	            boolean removable=(boolean)isRemovable.invoke(volume);
                Method isPrimary = volume.getClass().getDeclaredMethod("isPrimary");
                boolean primary=(boolean)isPrimary.invoke(volume);
                Method getUuid = volume.getClass().getDeclaredMethod("getUuid");
                String uuid=(String)getUuid.invoke(volume);
//                Method getId = volume.getClass().getDeclaredMethod("getId");
                Method getPath = volume.getClass().getDeclaredMethod("getPath");
                String path = (String) getPath.invoke(volume);
                Method toString = volume.getClass().getDeclaredMethod("toString");
                String desc=(String)toString.invoke(volume);
//                cu.addDebugMsg(1,"I","isStorageVolumeExistsApi23 uuid="+uuid+", desc="+desc+", isRemovable="+(boolean)isRemovable.invoke(volume)+
//                        ", isPrimary="+(boolean)isPrimary.invoke(volume)+", Id="+(String)getId.invoke(volume)+
//                        ", Path="+(String)getPath.invoke(volume));
                cu.addDebugMsg(1,"I","getStorageVolumeUuidApi23 uuid="+uuid+", desc="+desc+", type="+type+", isRemovable="+removable+", path="+path);
                if (type.contains("SD")) {
                    if (!desc.toLowerCase().contains("USB".toLowerCase()) && removable && !primary) {
                        result=uuid;
                        break;
                    }
                } else {
                    if (desc.contains(type)) {
                        result=uuid;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            cu.addDebugMsg(1,"I","getStorageVolumeUuidApi23 error="+e.getMessage());
        }
        cu.addDebugMsg(1,"I","getStorageVolumeUuidApi23 exit, uuid="+result);
        return result;
    }

    static public StorageVolume getStorageVolume(Context c, CommonUtilities cu, String type) {
        StorageManager sm = (StorageManager) c.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> vol_list=sm.getStorageVolumes();
        StorageVolume sv=null;
        String uuid="";
        GlobalParameters gp=GlobalWorkArea.getGlobalParameters(c);
        if (type.toLowerCase().equals("usb")) {
            for(StorageVolume sv_item:vol_list) {
                cu.addDebugMsg(1,"I","getStorageVolume uuid="+sv_item.getUuid()+", desc="+sv_item.getDescription(c)+
                        ", primary="+sv_item.isPrimary()+", removable="+sv_item.isRemovable());
                if (sv_item.getDescription(c).toLowerCase().contains(type.toLowerCase()) && sv_item.isRemovable() && !sv_item.isPrimary()) {
                    sv=sv_item;
                    uuid=sv_item.getUuid();
                    break;
                } else {
                    if (gp.forceUsbUuidList.size()>0) {
                        for(String item:gp.forceUsbUuidList) {
                            if (item.equals(sv_item.getUuid())) {
                                sv=sv_item;
                                uuid=item;
                                break;
                            }
                        }
                        if (sv!=null) break;
                    }
                }
            }
        } else if (type.toLowerCase().equals("sd")) {
            for(StorageVolume sv_item:vol_list) {
                cu.addDebugMsg(1,"I","getStorageVolume uuid="+sv_item.getUuid()+", desc="+sv_item.getDescription(c)+
                        ", primary="+sv_item.isPrimary()+", removable="+sv_item.isRemovable());
                if (sv_item.getDescription(c).toLowerCase().contains(type.toLowerCase()) && sv_item.isRemovable() && !sv_item.isPrimary()) {
                    if (gp.forceUsbUuidList.size()>0) {
                        boolean found=false;
                        for(String usb_item:gp.forceUsbUuidList) {
                            if (usb_item.equals(sv_item.getUuid())) {
                                found=true;
                                break;
                            }
                        }
                        if (!found) {
                            sv=sv_item;
                            uuid=sv_item.getUuid();
                            break;
                        }
                    } else {
                        sv=sv_item;
                        uuid=sv_item.getUuid();
                        break;
                    }
                }
            }
        }
        cu.addDebugMsg(1,"I","getStorageVolume exit, uuid="+uuid);
        return sv;
    }

    static public StorageVolume getSdcardStorageVolume(Context c, CommonUtilities cu) {
        StorageManager sm = (StorageManager) c.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> vol_list=sm.getStorageVolumes();
        StorageVolume sv=null;
        String uuid="";
        for(StorageVolume item:vol_list) {
            cu.addDebugMsg(1,"I","getSdcardStorageVolume(1) uuid="+item.getUuid()+", desc="+item.getDescription(c)+
                    ", primary="+item.isPrimary()+", removable="+item.isRemovable());
            if (item.getDescription(c).contains("SD")  && item.isRemovable() && !item.isPrimary()) {
                sv=item;
                uuid=item.getUuid();
                break;
            }
        }
        if (sv==null) {
            for(StorageVolume item:vol_list) {
                cu.addDebugMsg(1,"I","getSdcardStorageVolume(2) uuid="+item.getUuid()+", desc="+item.getDescription(c)+
                        ", primary="+item.isPrimary()+", removable="+item.isRemovable());
                if (!item.isPrimary() && item.isRemovable() && !item.getDescription(c).contains("USB")) {
                    sv=item;
                    uuid=item.getUuid();
                    break;
                }
            }
        }
        cu.addDebugMsg(1,"I","getSdcardStorageVolume exit, uuid="+uuid);
        return sv;
    }

    private boolean checkSyncFolderValidation(Dialog dialog, SyncFolderEditValue org_sfev) {
        boolean result = true;
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);
        setDialogMsg(dlg_msg, "");

        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);

        final EditText et_remote_host = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_server);
        final CheckedTextView ctv_sync_folder_use_port = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_remote_port_number);
        final EditText et_sync_folder_port = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_port);

        final CheckedTextView ctv_sync_folder_use_pswd = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_user_pass);
        final EditText et_sync_folder_user = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_user);
        final EditText et_sync_folder_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_pass);

        final EditText et_sync_folder_share_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_share_name);

        final Button btn_sync_folder_ok = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_ok);

//		final LinearLayout ll_sync_folder_smb_view = (LinearLayout)dialog.findViewById(R.id.edit_sync_folder_dlg_smb_host_view);
//		final Button btn_search_host = (Button)dialog.findViewById(R.id.edit_sync_folder_dlg_search_remote_host);
//		final EditText et_sync_folder_domain = (EditText)dialog.findViewById(R.id.edit_sync_folder_dlg_remote_domain);
        final Button btn_sync_folder_smb_list_share = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_share_btn);
        final Button btn_sync_folder_local_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_internal_directory_btn);
        final Button btn_sync_folder_smb_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_smb_directory_btn);
//		final EditText et_sync_folder_dir_name = (EditText)dialog.findViewById(R.id.edit_sync_folder_dlg_directory_name);
//		final Button btn_sdcard_select_sdcard = (Button)dialog.findViewById(R.id.edit_sync_folder_dlg_show_select_document_tree);
//		final CheckedTextView ctv_sync_folder_use_usb_folder = (CheckedTextView)dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_usb_folder);
//		final Button btn_sync_folder_cancel = (Button)dialog.findViewById(R.id.edit_profile_remote_btn_cancel);

//		final LinearLayout ll_dir_view = (LinearLayout)dialog.findViewById(R.id.edit_sync_folder_dlg_directory_view);
//		final LinearLayout ll_zip_view = (LinearLayout)dialog.findViewById(R.id.edit_sync_folder_dlg_zip_view);

//		final CheckedTextView ctv_sync_folder_zip_file_name_time_stamp = (CheckedTextView)dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_time_stamp);
        final Button btn_zip_filelist = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_filelist_btn);
        final EditText et_sync_folder_zip_file_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_name);
        final CheckedTextView ctv_zip_file_save_sdcard = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_use_sdcard);
//		final Button btn_zip_select_sdcard = (Button)dialog.findViewById(R.id.edit_sync_folder_dlg_zip_select_document_tree);
//		final RadioGroup rg_zip_enc_type = (RadioGroup)dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rg);
        final Spinner sp_zip_enc_method=(Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_method);
        final EditText et_zip_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_password);
        final EditText et_zip_conf_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_confirm);

        String sel = sp_sync_folder_type.getSelectedItem().toString();
        if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb))) {
            String remote_host = et_remote_host.getText().toString().trim();

            String sync_folder_port = et_sync_folder_port.getText().toString().trim();

            String sync_folder_user = et_sync_folder_user.getText().toString().trim();
            String sync_folder_pswd = et_sync_folder_pswd.getText().toString();

            String folder_share_name = et_sync_folder_share_name.getText().toString().trim();

            if (remote_host.equals("")) {
                result = false;
                setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_main_sync_profile_dlg_specify_host_address_or_name));
                CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_smb_list_share, false);
            } else {
                if (ctv_sync_folder_use_port.isChecked() && sync_folder_port.equals("")) {
                    result = false;
                    CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_smb_list_share, false);
                    setSyncFolderSmbListDirectoryButtonEnabled(dialog, false);
                    setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_main_sync_profile_dlg_specify_host_port_number));
                } else {
                    if (ctv_sync_folder_use_pswd.isChecked()) {
                        if (sync_folder_user.equals("") && sync_folder_pswd.equals("")) {
                            result = false;
                            CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_smb_list_share, false);
                            setSyncFolderSmbListDirectoryButtonEnabled(dialog, false);
                            setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_main_sync_profile_dlg_specify_host_userid_pswd));
                        } else {
                            if (sync_folder_user.equals(SMBSYNC2_PROF_DECRYPT_FAILED) || sync_folder_user.equals(SMBSYNC2_PROF_ENCRYPT_FAILED)) {
                                setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_import_failed_account_name));
                                result = false;
                            } else if (sync_folder_pswd.equals(SMBSYNC2_PROF_DECRYPT_FAILED) || sync_folder_pswd.equals(SMBSYNC2_PROF_ENCRYPT_FAILED)) {
                                setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_import_failed_account_password));
                                result = false;
                            }
                            if (result) {
                                if (!sync_folder_user.equals("") && sync_folder_pswd.equals("")) {
                                    if (sync_folder_pswd.equals("")) {
                                        result = false;
                                        setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_main_sync_profile_dlg_specify_host_pswd));
                                        btn_sync_folder_smb_list_share.setEnabled(false);
                                        setSyncFolderSmbListDirectoryButtonEnabled(dialog, false);
                                    }
                                }
                            }
                        }
                    }
                    if (result && folder_share_name.equals("")) {
                        setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_main_sync_profile_dlg_specify_host_share_name));
                        CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_smb_list_share, true);
                        CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_smb_list_dir, false);
                        result = false;
                    } else {
                        if (result) {
                            CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_smb_list_share, true);
                            CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_smb_list_dir, true);
                        } else {
                            CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_smb_list_share, false);
                            CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_smb_list_dir, false);
                        }
                    }
                }
            }

            boolean enabled = true;
            if (ctv_sync_folder_use_pswd.isChecked()) {
//                if (et_sync_folder_pswd.getText().toString().equals(""))
//                    enabled = false;
            }
            if (ctv_sync_folder_use_port.isChecked()) {
                if (et_sync_folder_port.getText().toString().equals("")) enabled = false;
            }
            if (et_remote_host.getText().toString().equals("")) enabled = false;

//            CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_smb_list_share, enabled);
//            setSyncFolderSmbListDirectoryButtonEnabled(dialog, enabled);
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_zip))) {
            result = false;
            if (ctv_zip_file_save_sdcard.isChecked() && Build.VERSION.SDK_INT<=29) {
                if (mGp.safMgr.getSdcardRootSafFile() == null) {
                    CommonDialog.setViewEnabled(getActivity(), btn_zip_filelist, false);
                } else {
                    CommonDialog.setViewEnabled(getActivity(), btn_zip_filelist, true);
                }
            } else {
                CommonDialog.setViewEnabled(getActivity(), btn_zip_filelist, true);
            }

            if (et_sync_folder_zip_file_name.getText().length() > 0) {
                String zip_enc_method=(String)sp_zip_enc_method.getSelectedItem();
                if (zip_enc_method.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_encrypt_none))) {
                    result = true;
                } else {
                    if (et_zip_pswd.getText().length() > 0) {
                        if (et_zip_pswd.getText().toString().equals(SMBSYNC2_PROF_DECRYPT_FAILED) || et_zip_pswd.getText().toString().equals(SMBSYNC2_PROF_ENCRYPT_FAILED)) {
                            setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_import_failed_zip_password));
                            result = false;
                        } else {
                            if (et_zip_pswd.getText().toString().equals(et_zip_conf_pswd.getText().toString())) {
                                result = true;
                            } else {
                                setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_diff_zip_password));
                            }
                        }
                    } else {
                        setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_specify_zip_password));
                    }
                }
            } else {
                setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_specify_zip_file_name));
            }
        }
        if (result) {
            setSyncFolderOkButtonEnabledIfFolderChanged(dialog, org_sfev);
        } else {
            setSyncFolderOkButtonEnabled(btn_sync_folder_ok, false);
        }

        return result;
    }

    private void setSyncFolderOkButtonEnabledIfFolderChanged(Dialog dialog, SyncFolderEditValue org_sfev) {
        final Button btn_sync_folder_ok = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_ok);
        SyncFolderEditValue nsfev = buildSyncFolderEditValue(dialog, org_sfev);
        boolean changed = nsfev.isSame(org_sfev);
        if (!changed) setSyncFolderOkButtonEnabled(btn_sync_folder_ok, true);
        else setSyncFolderOkButtonEnabled(btn_sync_folder_ok, false);
    }

    private void confirmUseAppSpecificDir(SyncTaskItem sti, String dir, final NotifyEvent p_ntfy) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (!prefs.getBoolean(mContext.getString(R.string.settings_suppress_warning_app_specific_dir), false)) {
            if (dir.startsWith(APP_SPECIFIC_DIRECTORY)) {
                final Dialog dialog = new Dialog(getActivity(), mGp.applicationTheme);//, android.R.style.Theme_Black);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.show_warning_message_dlg);

                final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.show_warning_message_dlg_title_view);
                final TextView title = (TextView) dialog.findViewById(R.id.show_warning_message_dlg_title);
                title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
                title.setText(mContext.getString(R.string.msgs_main_app_specific_dir_used_title));
                title.setTextColor(mGp.themeColorList.title_text_color);

                ((TextView) dialog.findViewById(R.id.show_warning_message_dlg_msg))
                        .setText(mContext.getString(R.string.msgs_main_app_specific_dir_used_msg) +
                                "\n" + sti.getSyncTaskName());

                final Button btnClose = (Button) dialog.findViewById(R.id.show_warning_message_dlg_close);
                final CheckedTextView ctvSuppr = (CheckedTextView) dialog.findViewById(R.id.show_warning_message_dlg_ctv_suppress);
                CommonUtilities.setCheckedTextView(ctvSuppr);

                CommonDialog.setDlgBoxSizeCompact(dialog);
                ctvSuppr.setChecked(false);
                // Closeボタンの指定
                btnClose.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (ctvSuppr.isChecked()) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                            prefs.edit().putBoolean(mContext.getString(R.string.settings_suppress_warning_app_specific_dir), true).commit();
                        }
                        if (p_ntfy != null) p_ntfy.notifyToListener(true, null);
                    }
                });
                // Cancelリスナーの指定
                dialog.setOnCancelListener(new Dialog.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        btnClose.performClick();
                    }
                });
                dialog.show();
            } else {
                if (p_ntfy != null) p_ntfy.notifyToListener(true, null);
            }
        }
    }

//    static public void checkLocationServiceWarning(Activity activity, GlobalParameters gp, CommonUtilities cu) {
//        if (Build.VERSION.SDK_INT<=26) return;
//        if (Build.VERSION.SDK_INT>=29) {
//            boolean coarse_granted=(activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED);
//            boolean background_granted=background_granted=(activity.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)==PackageManager.PERMISSION_GRANTED);
//            if (gp.settingSupressLocationServiceWarning ||
//                    (CommonUtilities.isLocationServiceEnabled(activity, gp) && coarse_granted && background_granted)) return;
//            boolean waring_required=false;
//            String used_st="", sep="-";
//            for(SyncTaskItem st_item:gp.syncTaskList) {
//                if (st_item.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_AP)) {
//                    waring_required=true;
//                    used_st+=sep+st_item.getSyncTaskName();
//                    sep="\n-";
//                }
//            }
//
//            if (!waring_required) return;
//
//            showLocationServiceWarning(activity, gp, cu, used_st);
//        } else {
//            boolean coarse_granted=(activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED);
//            if (gp.settingSupressLocationServiceWarning ||
//                    (CommonUtilities.isLocationServiceEnabled(activity, gp) && coarse_granted)) return;
//            boolean waring_required=false;
//            String used_st="", sep="-";
//            for(SyncTaskItem st_item:gp.syncTaskList) {
//                if (st_item.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_AP)) {
//                    waring_required=true;
//                    used_st+=sep+st_item.getSyncTaskName();
//                    sep="\n-";
//                }
//            }
//
//            if (!waring_required) return;
//
//            showLocationServiceWarning(activity, gp, cu, used_st);
//        }
//    }

//    static public void showLocationServiceWarning(final Activity activity, final GlobalParameters gp, final CommonUtilities cu, final String used_st) {
//
//        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
//
//        final Dialog dialog = new Dialog(activity, gp.applicationTheme);//, android.R.style.Theme_Black);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.show_warning_message_dlg);
//
//        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.show_warning_message_dlg_title_view);
//        final TextView title = (TextView) dialog.findViewById(R.id.show_warning_message_dlg_title);
//        title_view.setBackgroundColor(gp.themeColorList.title_background_color);
//        title.setText(activity.getString(R.string.msgs_main_app_specific_dir_used_title));
//        title.setTextColor(gp.themeColorList.title_text_color);
//        title.setText(activity.getString(R.string.msgs_main_location_service_warning_title));
//
//        String msg_text="";
//        boolean coarse_granted=(activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED);
//        if (Build.VERSION.SDK_INT>=29) {
//            boolean background_granted=(activity.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)==PackageManager.PERMISSION_GRANTED);
//            boolean loc=CommonUtilities.isLocationServiceEnabled(activity, gp);
//            if (!CommonUtilities.isLocationServiceEnabled(activity, gp) && (!coarse_granted||!background_granted)) {
//                msg_text = activity.getString(R.string.msgs_main_location_service_warning_msg_both);
//            } else if (!CommonUtilities.isLocationServiceEnabled(activity, gp)) {
//                msg_text=activity.getString(R.string.msgs_main_location_service_warning_msg_location);
//            } else if (!coarse_granted || !background_granted) {
//                msg_text=activity.getString(R.string.msgs_main_location_service_warning_msg_coarse);
//            }
//        } else {
//            if (!CommonUtilities.isLocationServiceEnabled(activity, gp) && !coarse_granted) {
//                msg_text=activity.getString(R.string.msgs_main_location_service_warning_msg_both);
//            } else if (!CommonUtilities.isLocationServiceEnabled(activity, gp)) {
//                msg_text=activity.getString(R.string.msgs_main_location_service_warning_msg_location);
//            } else if (!coarse_granted) {
//                msg_text=activity.getString(R.string.msgs_main_location_service_warning_msg_coarse);
//            }
//        }
//
//        if (!used_st.equals("")) msg_text+="\n\n"+used_st;
//        ((TextView) dialog.findViewById(R.id.show_warning_message_dlg_msg)).setText(msg_text);
//
//        final Button btnClose = (Button) dialog.findViewById(R.id.show_warning_message_dlg_close);
//        final CheckedTextView ctvSuppr = (CheckedTextView) dialog.findViewById(R.id.show_warning_message_dlg_ctv_suppress);
//        CommonUtilities.setCheckedTextView(ctvSuppr);
//        ctvSuppr.setText(R.string.msgs_main_location_service_warning_suppress);
//
//        CommonDialog.setDlgBoxSizeCompact(dialog);
//        ctvSuppr.setChecked(false);
//        // Closeボタンの指定
//        btnClose.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                dialog.dismiss();
//                if (ctvSuppr.isChecked()) {
//                    prefs.edit().putBoolean(activity.getString(R.string.settings_suppress_warning_location_service_disabled), true).commit();
//                    gp.settingSupressLocationServiceWarning =true;
//                }
//            }
//        });
//        // Cancelリスナーの指定
//        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface arg0) {
//                btnClose.performClick();
//            }
//        });
//        dialog.show();
//    }

    private void invokeEditDirFilterDlg(final Dialog dialog, final SyncTaskItem n_sti, final String type, final TextView dlg_msg) {
        final CheckedTextView ctvEnsureTargetExactMirror = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_ensure_target_is_exact_mirror);
        final CheckedTextView ctUseDirectoryFilterV2 = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_directory_filter_v2);
        final TextView dlg_dir_filter = (TextView) dialog.findViewById(R.id.sync_filter_edit_dir_filter_btn);
        NotifyEvent ntfy = new NotifyEvent(mContext);
        //Listen setRemoteShare response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                dlg_dir_filter.setText(buildDirectoryFilterInfo(n_sti.getDirFilter()));
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }

            @Override
            public void negativeResponse(Context arg0, Object[] arg1) {
            }

        });
        mTaskUtil.editDirFilterDlg(n_sti, ntfy, ctUseDirectoryFilterV2.isChecked(), ctvEnsureTargetExactMirror.isChecked());

    }

    private void invokeEditFileFilterDlg(Dialog dialog, final SyncTaskItem n_sti, final String type, final TextView dlg_msg) {
        final TextView dlg_file_filter = (TextView) dialog.findViewById(R.id.sync_filter_edit_file_filter_btn);
        final CheckedTextView ctvEnsureTargetExactMirror = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_ensure_target_is_exact_mirror);
        final CheckedTextView ctUseDirectoryFilterV2 = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_directory_filter_v2);
        NotifyEvent ntfy = new NotifyEvent(mContext);
        //Listen setRemoteShare response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context arg0, Object[] arg1) {
                dlg_file_filter.setText(buildFileFilterInfo(n_sti.getFileFilter()));
//				Log.v("","siz="+n_sti.getFileFilter().size());
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }

            @Override
            public void negativeResponse(Context arg0, Object[] arg1) {
            }

        });
        mTaskUtil.editFileFilterDlg(n_sti, ntfy, ctUseDirectoryFilterV2.isChecked(), ctvEnsureTargetExactMirror.isChecked());

    }

    private String buildMasterSyncFolderInfo(SyncTaskItem sti, Button info_btn, ImageView info_icon) {
        String info = "";
        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            String dir = sti.getMasterDirectoryName();
//        	Log.v("","md="+dir);
            if (dir.equals("")) info = sti.getMasterLocalMountPoint();
            else {
                if (dir.startsWith("/")) info = sti.getMasterLocalMountPoint() + dir;
                else info = sti.getMasterLocalMountPoint() + "/" + dir;
            }
            info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_mobile, null));
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            String dir = sti.getMasterDirectoryName();
            if (dir.equals("")) info = mGp.safMgr.getSdcardRootPath();
            else {
                if (dir.startsWith("/")) info = mGp.safMgr.getSdcardRootPath() + dir;
                else info = mGp.safMgr.getSdcardRootPath() + "/" + dir;
            }
            if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_sdcard_bad, null));
            } else {
                info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_sdcard, null));
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
            String dir = sti.getMasterDirectoryName();
            if (dir.equals("")) info = mGp.safMgr.getUsbRootPath();
            else {
                if (dir.startsWith("/")) info = mGp.safMgr.getUsbRootPath() + dir;
                else info = mGp.safMgr.getUsbRootPath() + "/" + dir;
            }
            if (mGp.safMgr.getUsbRootPath().equals(SafManager.UNKNOWN_USB_DIRECTORY)) {
                info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_usb_bad, null));
            } else {
                info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_usb, null));
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            String host = sti.getMasterSmbAddr();
            if (sti.getMasterSmbAddr().equals("")) host = sti.getMasterSmbHostName();
            String share = sti.getMasterSmbShareName();
            String dir = sti.getMasterDirectoryName();
            if (dir.equals("")) info = "smb://" + host + "/" + share;
            else {
                if (dir.startsWith("/")) info = "smb://" + host + "/" + share + dir;
                else info = "smb://" + host + "/" + share + "/" + dir;
            }
            info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_server, null));
        }
        return info;
    }

    private String buildTargetSyncFolderInfo(SyncTaskItem sti, Button info_btn, ImageView info_icon) {
        String info = "";
        if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            String dir = sti.getTargetDirectoryName();
            if (dir.equals("")) info = sti.getTargetLocalMountPoint();
            else {
                if (dir.startsWith("/")) info = sti.getTargetLocalMountPoint() + dir;
                else info = sti.getTargetLocalMountPoint() + "/" + dir;
            }
            info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_mobile, null));
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            String dir = sti.getTargetDirectoryName();
            if (dir.equals("")) info = mGp.safMgr.getSdcardRootPath();
            else {
                if (dir.startsWith("/")) info = mGp.safMgr.getSdcardRootPath() + dir;
                else info = mGp.safMgr.getSdcardRootPath() + "/" + dir;
            }
            if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_sdcard_bad, null));
            } else {
                info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_sdcard, null));
            }
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
            String dir = sti.getTargetDirectoryName();
            if (dir.equals("")) info = mGp.safMgr.getUsbRootPath();
            else {
                if (dir.startsWith("/")) info = mGp.safMgr.getUsbRootPath() + dir;
                else info = mGp.safMgr.getUsbRootPath() + "/" + dir;
            }
            if (mGp.safMgr.getUsbRootPath().equals(SafManager.UNKNOWN_USB_DIRECTORY)) {
                info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_usb_bad, null));
            } else {
                info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_usb, null));
            }
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
            if (!sti.isTargetZipUseExternalSdcard())
                info = mGp.internalRootDirectory + sti.getTargetZipOutputFileName();
            else {
                info = mGp.safMgr.getSdcardRootPath() + sti.getTargetZipOutputFileName();
            }
            if (sti.isTargetZipUseExternalSdcard() &&
                    mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_sdcard_bad, null));
            } else {
                info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_archive, null));
            }
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            String host = sti.getTargetSmbAddr();
            if (sti.getTargetSmbAddr().equals("")) host = sti.getTargetSmbHostName();
            String share = sti.getTargetSmbShareName();
            String dir = sti.getTargetDirectoryName();
            if (dir.equals("")) info = "smb://" + host + "/" + share;
            else {
                if (dir.startsWith("/")) info = "smb://" + host + "/" + share + dir;
                else info = "smb://" + host + "/" + share + "/" + dir;
            }
            info_icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_32_server, null));
        }
        return info;
    }

    private void setSpinnerSyncFolderZipCompressionLevel(Spinner spinner, String cv) {
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_title));
        spinner.setAdapter(adapter);
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_fastest));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_fast));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_normal));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_maximum));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_ultra));
//		adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_saf_usb));

        int sel = SyncTaskItem.ZIP_OPTION_COMP_LEVEL_DEFAULT_ITEM_INDEX;
        if (cv.equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_FASTEST)) sel = 0;
        else if (cv.equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_FAST)) sel = 1;
        else if (cv.equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_NORMAL)) sel = 2;
        else if (cv.equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_MAXIMUM)) sel = 3;
        else if (cv.equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_ULTRA)) sel = 4;

        spinner.setSelection(sel);
        adapter.notifyDataSetChanged();
    }

    private void setSpinnerSyncFolderZipEncryptMethod(Spinner spinner, String cv) {
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_encrypt_select_msg));
        spinner.setAdapter(adapter);
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_encrypt_none));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_encrypt_zip_crypto));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_encrypt_aes128));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_encrypt_aes256));

        int sel = SyncTaskItem.ZIP_OPTION_ENCRYPT_DEFAULT_ITEM_INDEX;
        if (cv.equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_NONE)) sel = 0;
        else if (cv.equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_STANDARD)) sel = 1;
        else if (cv.equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_AES128)) sel = 2;
        else if (cv.equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_AES256)) sel = 3;

        spinner.setSelection(sel);
        adapter.notifyDataSetChanged();
    }

    private void setSpinnerSyncFolderMountPoint(SyncTaskItem sti, Spinner spinner, String cv, boolean write_only) {
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        mGp.safMgr.loadSafFile();
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_local_mount_point ));
        spinner.setAdapter(adapter);

        int sel_no = 0;
//        ArrayList<String>mpl=LocalMountPoint.getLocalMountpointList2(mContext);
        ArrayList<String> mpl = LocalMountPoint.getLocalMountpointList2(mContext);

        if (Build.VERSION.SDK_INT>=26) {
            File[] fl=mContext.getExternalFilesDirs(null);
            if (fl!=null && fl.length>1) {
                for(File fi:fl) {
                    if (fi!=null && fi.getPath()!=null) {
                        String mp_name=fi.getPath().substring(0, fi.getPath().indexOf("/Android/data"));
                        boolean found=false;
                        for(String mi:mpl) {
                            if (mi.equals(mp_name)) {
                                found=true;
                                break;
                            }
                        }
                        if (!found) mpl.add(mp_name);
                    }
                }
                for(String mi:mpl) mUtil.addDebugMsg(1,"I","setSpinnerSyncFolderMountPoint MP entry="+mi);
            }
        }

        if (mpl == null || mpl.size() == 0) {
            adapter.add(mGp.internalRootDirectory);
            mUtil.addDebugMsg(1,"I","setSpinnerSyncFolderMountPoint add MP by InternalStorage only.");
        } else {
            for (String item : mpl) {
                mUtil.addDebugMsg(1,"I","setSpinnerSyncFolderMountPoint MP list="+item+", write="+ LocalMountPoint.isMountPointCanWrite(item));
                if ((write_only && LocalMountPoint.isMountPointCanWrite(item)) ||
                        !write_only) {
                    if (item.equals(cv)) {
                        sel_no = adapter.getCount();
                    }
                    adapter.add(item);
                    mUtil.addDebugMsg(1,"I","setSpinnerSyncFolderMountPoint MP added="+item);
                }
            }
        }
        spinner.setSelection(sel_no);
        adapter.notifyDataSetChanged();
    }

    private String getSmbSelectedProtocol(Spinner spinner) {
        if (spinner.getSelectedItem()==null) {
            return SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1;
        }
        String sel=spinner.getSelectedItem().toString();
        if (spinner.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb1))) {
            return SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1;
        } else if (spinner.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb201))) {
            return SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB201;
        } else if (spinner.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb211))) {
            return SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB211;
        } else if (spinner.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb212))) {
            return SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB212;
        } else if (spinner.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb214))) {
            return SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB214;
        }
        return SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1;
    }

    private void setSpinnerSyncFolderSmbProto(SyncTaskItem sti, Spinner spinner, String cv) {
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        mGp.safMgr.loadSafFile();
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_prompt_title));
        spinner.setAdapter(adapter);

//        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_system));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb1));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb201));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb211));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb212));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb214));

        if (cv.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1)) spinner.setSelection(0);
        else if (cv.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB201)) spinner.setSelection(1);
        else if (cv.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB211)) spinner.setSelection(2);
        else if (cv.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB212)) spinner.setSelection(3);
        else if (cv.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB214)) spinner.setSelection(4);
        else spinner.setSelection(0);
    }

    private void setSpinnerSyncFolderType(SyncTaskItem sti, Spinner spinner, String cv, boolean master) {
        final Spinner spinnerSyncType = (Spinner) mDialog.findViewById(R.id.edit_sync_task_sync_type);
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        mGp.safMgr.loadSafFile();
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_prompt));
        spinner.setAdapter(adapter);
        int sel = 0;
        if (master) {
            if (!sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
                    if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
                } else {
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));

                    if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
                }
            } else {
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
//                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
//                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));

                if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
//                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
//                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
            }
        } else {
            if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
                if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
            } else {
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
                if (!sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE) && sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL))
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_zip));
                if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                    if (!sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE) &&
                            sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 3;
                }
            }
        }
        spinner.setSelection(sel);
        adapter.notifyDataSetChanged();
    }

    private void setSpinnerSyncFolderTypeWithUsb(SyncTaskItem sti, Spinner spinner, String cv, boolean master) {
        final Spinner spinnerSyncType = (Spinner) mDialog.findViewById(R.id.edit_sync_task_sync_type);
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        mGp.safMgr.loadSafFile();
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_prompt));
        spinner.setAdapter(adapter);
        int sel = 0;
        if (master) {
            if (!sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb));
                    if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) sel = 3;
                } else {
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb));

                    if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) sel = 3;
                }
            } else {// Target is ZIP
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
//                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
//                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));

                if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
//                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
//                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
            }
        } else {
            if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb));
                if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) sel = 3;
            } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb));
                if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) sel=3;
            } else {
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb));
                if (!sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE) && sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL))
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_zip));
                if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) sel=3;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                    if (!sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE) &&
                            sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 4;
                }
            }
        }
        spinner.setSelection(sel);
        adapter.notifyDataSetChanged();
    }

    private void setSpinnerSyncTaskType(Spinner spinnerSyncOption, SyncTaskItem n_sti) {
        CommonUtilities.setSpinnerBackground(mContext, spinnerSyncOption, mGp.isScreenThemeIsLight());
        String prof_syncopt = n_sti.getSyncTaskType();
        final CustomSpinnerAdapter adapterSyncOption =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        adapterSyncOption.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinnerSyncOption.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_syncopt_prompt));
        spinnerSyncOption.setAdapter(adapterSyncOption);

        final CheckedTextView ctvEnsureTargetExactMirror = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_ensure_target_is_exact_mirror);
//        if (ctvEnsureTargetExactMirror.isChecked()) adapterSyncOption.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_mirror_image));
//        else adapterSyncOption.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_mirror));
        adapterSyncOption.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_mirror));

        adapterSyncOption.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_copy));
        adapterSyncOption.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_move));
        adapterSyncOption.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_archive));
        if (mGp.debuggable) adapterSyncOption.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync));

        int sel=0;
        if (prof_syncopt.equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) sel=0;
        else if (prof_syncopt.equals(SyncTaskItem.SYNC_TASK_TYPE_COPY)) sel=1;
        else if (prof_syncopt.equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) sel=2;
        else if (prof_syncopt.equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) sel=3;
		else if (mGp.debuggable && prof_syncopt.equals(SyncTaskItem.SYNC_TASK_TYPE_SYNC)) sel=4;

        spinnerSyncOption.setSelection(sel);
        adapterSyncOption.notifyDataSetChanged();
    }

    private void setSpinnerTwoWaySyncConflictRule(Spinner spinner, String cv) {
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_prompt));
        spinner.setAdapter(adapter);
        adapter.add(mContext.getString(R.string.msgs_profile_twoway_sync_conflict_copy_rurle_ask_user));
        adapter.add(mContext.getString(R.string.msgs_profile_twoway_sync_conflict_copy_rurle_copy_newer));
        adapter.add(mContext.getString(R.string.msgs_profile_twoway_sync_conflict_copy_rurle_copy_older));
        adapter.add(mContext.getString(R.string.msgs_profile_twoway_sync_conflict_copy_rurle_skip_sync_file));

        int sel = 0;
        if (cv.equals(SyncTaskItem.SYNC_TASK_TWO_WAY_OPTION_ASK_USER)) sel = 0;
        else if (cv.equals(SyncTaskItem.SYNC_TASK_TWO_WAY_OPTION_COPY_NEWER)) sel = 1;
        else if (cv.equals(SyncTaskItem.SYNC_TASK_TWO_WAY_OPTION_COPY_OLDER)) sel = 2;
        else if (cv.equals(SyncTaskItem.SYNC_TASK_TWO_WAY_OPTION_SKIP_SYNC_FILE)) sel = 3;

        spinner.setSelection(sel);

        adapter.notifyDataSetChanged();
    }

    private void setSpinnerSyncTaskWifiOption(Spinner spinner, String cv) {
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter =
                new AdapterWifiOption(getActivity(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_prompt));
        spinner.setAdapter(adapter);
        adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_off));
        adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_any_ap));
//        adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_specific_ap));
        adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_private_address));
        adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_specific_address));

        int sel = 0;
        if (cv.equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF)) sel = 0;
        else if (cv.equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP)) sel = 1;
        else if (cv.equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_AP)) sel = 2;
        else if (cv.equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_PRIVATE_ADDR)) sel = 2;
        else if (cv.equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_ADDR)) sel = 3;

        spinner.setSelection(sel);

        adapter.notifyDataSetChanged();
    }

    private void setSpinnerSyncTaskDiffTimeValue(Spinner spinner, int cv) {
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_diff_time_value_option_prompt));
        spinner.setAdapter(adapter);
        int sel=SyncTaskItem.SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_LIST_DEFAULT_ITEM_INDEX;
        for(int i=0;i<SyncTaskItem.SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_LIST.length;i++) {
            int item=SyncTaskItem.SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_LIST[i];
            adapter.add(String.valueOf(item));
            if (item==cv) sel=i;
        }
        spinner.setSelection(sel);

        adapter.notifyDataSetChanged();
    }

    private void setSpinnerSyncTaskDstOffsetValue(Spinner spinner, int cv) {
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_profile_sync_task_sync_option_offset_of_dst_time_value_title));
        spinner.setAdapter(adapter);
        int sel=SyncTaskItem.SYNC_OPTION_OFFSET_OF_DST_LIST_DEFAULT_ITEM_INDEX;
        for(int i=0;i<SyncTaskItem.SYNC_OPTION_OFFSET_OF_DST_LIST.length;i++) {
            int item=SyncTaskItem.SYNC_OPTION_OFFSET_OF_DST_LIST[i];
            adapter.add(String.valueOf(item));
            if (item==cv) sel=i;
        }
        spinner.setSelection(sel);

        adapter.notifyDataSetChanged();
    }

    private String buildDirectoryFilterInfo(ArrayList<String> filter_list) {
        String info=buildFilterInfo(filter_list);
        if (info.equals("")) {
            return mContext.getString(R.string.msgs_profile_sync_task_dlg_dir_filter_not_specified);
        }
        return info;
    }

    private String buildFileFilterInfo(ArrayList<String> filter_list) {
        String info=buildFilterInfo(filter_list);
        if (info.equals("")) {
            return mContext.getString(R.string.msgs_profile_sync_task_dlg_file_filter_not_specified);
        }
        return info;
    }

    private String buildFilterInfo(ArrayList<String> filter_list) {
        String info = "";
        if (filter_list != null && filter_list.size() > 0) {
            String t_info = "", cn = "", sep="";
            for (int i = 0; i < filter_list.size(); i++) {
                String inc = filter_list.get(i).substring(0, 1);
                String filter = filter_list.get(i).substring(1, filter_list.get(i).length());
                if (inc.equals(SMBSYNC2_PROF_FILTER_INCLUDE)) cn = "\u2295"; //(+) ASCII
                else cn = "\u2296"; //(-) ASCII
                t_info += sep+cn + filter_list.get(i).substring(1, filter_list.get(i).length()) + ";";
                sep=" ";
            }
            if (!t_info.equals("")) info = t_info;
        } else {
            info = "";
        }
        return info;
    }

    public void editSyncTask(final String type, final SyncTaskItem pfli) {
        final SyncTaskItem n_sti = pfli.clone();
        mUtil.addDebugMsg(1,"I","editSyncTask entered, type="+type+", task="+pfli.getSyncTaskName());

        mGp.safMgr.loadSafFile();

        // カスタムダイアログの生成
        mDialog.setContentView(R.layout.edit_sync_task_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.title_background_color);

        final LinearLayout title_view = (LinearLayout) mDialog.findViewById(R.id.edit_profile_sync_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        final TextView dlg_title = (TextView) mDialog.findViewById(R.id.edit_profile_sync_title);
        dlg_title.setTextColor(mGp.themeColorList.title_text_color);
//        dlg_title.setBackgroundColor(mGp.themeColorList.title_background_color);

        final TextView dlg_title_sub = (TextView) mDialog.findViewById(R.id.edit_profile_sync_title_sub);
        dlg_title_sub.setTextColor(mGp.themeColorList.title_text_color);
//        dlg_title_sub.setBackgroundColor(mGp.themeColorList.title_background_color);

        final TextView dlg_msg = (TextView) mDialog.findViewById(R.id.edit_sync_task_msg);
        dlg_msg.setTextColor(mGp.themeColorList.text_color_error);
        dlg_msg.setVisibility(TextView.GONE);

        final Button btn_ok = (Button) mDialog.findViewById(R.id.edit_profile_sync_dlg_btn_ok);
        CommonDialog.setViewEnabled(getActivity(), btn_ok, false);

        final Button swap_master_target = (Button) mDialog.findViewById(R.id.edit_sync_task_change_master_and_target_btn);
        final Button master_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_master_folder_info_btn);
        final ImageView master_folder_icon = (ImageView) mDialog.findViewById(R.id.edit_sync_task_master_folder_info_icon);
//        master_folder_icon.bringToFront();
        final Button target_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_target_folder_info_btn);
        final ImageView target_folder_icon = (ImageView) mDialog.findViewById(R.id.edit_sync_task_target_folder_info_icon);
//        target_folder_icon.bringToFront();

        final EditText et_sync_main_task_name = (EditText) mDialog.findViewById(R.id.edit_sync_task_task_name);
        if (type.equals("EDIT")) {
            et_sync_main_task_name.setTextColor(Color.LTGRAY);
            et_sync_main_task_name.setText(n_sti.getSyncTaskName());
            et_sync_main_task_name.setEnabled(false);
            et_sync_main_task_name.setVisibility(EditText.GONE);
            dlg_title.setText(mContext.getString(R.string.msgs_edit_sync_profile));
            dlg_title_sub.setText(" (" + n_sti.getSyncTaskName() + ")");
        } else if (type.equals("COPY")) {
            et_sync_main_task_name.setText(n_sti.getSyncTaskName());
            dlg_title.setText(mContext.getString(R.string.msgs_copy_sync_profile));
            dlg_title_sub.setText(" (" + n_sti.getSyncTaskName() + ")");
            n_sti.setSyncTaskNameError(false);//allow copy of existing sync task that have an invalid name if renamed during copy
        } else if (type.equals("ADD")) {
            dlg_title.setText(mContext.getString(R.string.msgs_add_sync_profile));
            dlg_title_sub.setVisibility(TextView.GONE);
            n_sti.setSyncOptionWifiStatusOption(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF);
        }
        final CheckedTextView ctv_auto = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_ctv_auto);
        CommonUtilities.setCheckedTextView(ctv_auto);
        ctv_auto.setChecked(n_sti.isSyncTaskAuto());
        setCtvListenerForEditSyncTask(ctv_auto, type, n_sti, dlg_msg);

        final Spinner spinnerSyncType = (Spinner) mDialog.findViewById(R.id.edit_sync_task_sync_type);
        setSpinnerSyncTaskType(spinnerSyncType, n_sti);
        spinnerSyncType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSyncTaskTypeFromSpinnere(spinnerSyncType, n_sti);
                if (spinnerSyncType.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_archive))) {
                    if (n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                        n_sti.setTargetFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
                        mUtil.showCommonDialog(false, "W",
                                mContext.getString(R.string.msgs_sync_folder_archive_zip_folder_not_supported), "", null);
                        target_folder_info.setText(buildTargetSyncFolderInfo(n_sti, target_folder_info, target_folder_icon));
                        target_folder_info.requestLayout();
                    }

                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        final LinearLayout ll_wifi_condition_view = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_wifi_condition_view);
        final LinearLayout ll_spinner_wifi_status = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_ll_spinner_wifi_status);
        final LinearLayout ll_wifi_wl_view = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_wl_view);
//        final LinearLayout ll_wifi_wl_ap_view = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_ap_list_view);
        final LinearLayout ll_wifi_wl_address_view = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_address_list_view);
//        final Button edit_wifi_ap_list = (Button) mDialog.findViewById(R.id.edit_sync_task_option_btn_edit_ap_white_list);
        final Button edit_wifi_addr_list = (Button) mDialog.findViewById(R.id.edit_sync_task_option_btn_edit_address_white_list);
//        setWifiApWhileListInfo(n_sti.getSyncOptionWifiConnectedAccessPointWhiteList(), edit_wifi_ap_list);
        setWifiApWhileListInfo(n_sti.getSyncOptionWifiConnectedAddressWhiteList(), edit_wifi_addr_list);
        final CheckedTextView ctv_sync_allow_global_ip_addr = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_sync_allow_global_ip_address);
        ctv_sync_allow_global_ip_addr.setChecked(n_sti.isSyncOptionSyncAllowGlobalIpAddress());
        setCtvListenerForEditSyncTask(ctv_sync_allow_global_ip_addr, type, n_sti, dlg_msg);
        final CheckedTextView ctv_task_skip_if_ssid_invalid = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ap_list_task_skip_if_ssid_invalid);
        ctv_task_skip_if_ssid_invalid.setChecked(n_sti.isSyncOptionTaskSkipIfConnectAnotherWifiSsid());
        setCtvListenerForEditSyncTask(ctv_task_skip_if_ssid_invalid, type, n_sti, dlg_msg);

        final CheckedTextView ctv_task_sync_when_cahrging = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_start_when_charging);
//        CommonUtilities.setCheckedTextView(ctv_task_sync_when_cahrging);
        ctv_task_sync_when_cahrging.setChecked(n_sti.isSyncOptionSyncWhenCharging());
        setCtvListenerForEditSyncTask(ctv_task_sync_when_cahrging, type, n_sti, dlg_msg);

        final CheckedTextView ctv_never_overwrite_target_file_newer_than_the_master_file = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_never_overwrite_target_file_if_it_is_newer_than_the_master_file);
        ctv_never_overwrite_target_file_newer_than_the_master_file.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctv_never_overwrite_target_file_newer_than_the_master_file.isChecked();
                ctv_never_overwrite_target_file_newer_than_the_master_file.setChecked(isChecked);
                if (isChecked) {
                    mUtil.showCommonDialog(false, "D",
                            mContext.getString(R.string.msgs_profile_sync_task_sync_option_never_overwrite_target_file_if_it_is_newer_than_the_master_file),
                            mContext.getString(R.string.msgs_profile_sync_task_sync_option_never_overwrite_target_file_if_it_is_newer_than_the_master_file_warning),
                            mContext.getString(R.string.msgs_common_dialog_confirm),
                            mContext.getString(R.string.msgs_common_dialog_cancel),
                            null );
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });
        ctv_never_overwrite_target_file_newer_than_the_master_file.setChecked(n_sti.isSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile());
        //setCtvListenerForEditSyncTask(ctv_never_overwrite_target_file_newer_than_the_master_file, type, n_sti, dlg_msg);

        final CheckedTextView ctv_ignore_dst_difference = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_ignore_dst_difference);
        ctv_ignore_dst_difference.setChecked(n_sti.isSyncOptionIgnoreDstDifference());

        final CheckedTextView ctv_edit_sync_task_option_ignore_unusable_character_used_directory_file_name = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ignore_unusable_character_used_directory_file_name);
        ctv_edit_sync_task_option_ignore_unusable_character_used_directory_file_name.setChecked(n_sti.isSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters());
        setCtvListenerForEditSyncTask(ctv_edit_sync_task_option_ignore_unusable_character_used_directory_file_name, type, n_sti, dlg_msg);

        final CheckedTextView ctv_sync_remove_master_if_empty = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_remove_directory_if_empty_when_move);
        ctv_sync_remove_master_if_empty.setChecked(n_sti.isSyncOptionMoveOnlyRemoveMasterDirectoryIfEmpty());
        setCtvListenerForEditSyncTask(ctv_sync_remove_master_if_empty, type, n_sti, dlg_msg);

        final LinearLayout ll_advanced_network_option_view = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_ll_advanced_network_option_view);
        final CheckedTextView ctvRetry = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_retry_if_error_occured);
        CommonUtilities.setCheckedTextView(ctvRetry);
        if (n_sti.getSyncOptionRetryCount().equals("0")) ctvRetry.setChecked(false);
        else ctvRetry.setChecked(true);
        setCtvListenerForEditSyncTask(ctvRetry, type, n_sti, dlg_msg);

        final CheckedTextView ctvSyncUseRemoteSmallIoArea = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_remote_small_io_area);
        CommonUtilities.setCheckedTextView(ctvSyncUseRemoteSmallIoArea);
        ctvSyncUseRemoteSmallIoArea.setChecked(n_sti.isSyncOptionUseSmallIoBuffer());
        setCtvListenerForEditSyncTask(ctvSyncUseRemoteSmallIoArea, type, n_sti, dlg_msg);

        final CheckedTextView ctv_edit_sync_tak_option_do_not_use_rename_when_smb_file_write = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_do_mot_use_rename_when_smb_file_write);
        ctv_edit_sync_tak_option_do_not_use_rename_when_smb_file_write.setChecked(n_sti.isSyncOptionDoNotUseRenameWhenSmbFileWrite());
        setCtvListenerForEditSyncTask(ctv_edit_sync_tak_option_do_not_use_rename_when_smb_file_write, type, n_sti, dlg_msg);

        final Spinner spinnerSyncWifiStatus = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_wifi_status);
        setSpinnerSyncTaskWifiOption(spinnerSyncWifiStatus, n_sti.getSyncOptionWifiStatusOption());

        if ((n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) || n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) &&
                n_sti.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_AP)) {
            mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_main_permission_ap_list_no_longer_available_dialog_title),
                    mContext.getString(R.string.msgs_main_permission_ap_list_no_longer_available_dialog_msg), null);
        }

        if (n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) || n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            ll_wifi_condition_view.setVisibility(LinearLayout.VISIBLE);
            ll_advanced_network_option_view.setVisibility(LinearLayout.VISIBLE);
            ll_wifi_wl_view.setVisibility(Button.GONE);
//            if (n_sti.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_AP)) {
//                ll_wifi_wl_view.setVisibility(Button.VISIBLE);
//                ll_wifi_wl_ap_view.setVisibility(Button.VISIBLE);
//            } else {
//                ll_wifi_wl_view.setVisibility(Button.GONE);
//                ll_wifi_wl_ap_view.setVisibility(Button.GONE);
//            }
        } else {
            ll_wifi_condition_view.setVisibility(LinearLayout.GONE);
            ll_advanced_network_option_view.setVisibility(LinearLayout.GONE);
        }

        spinnerSyncWifiStatus.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ll_wifi_wl_view.setVisibility(Button.GONE);
//                ll_wifi_wl_ap_view.setVisibility(Button.GONE);
                ll_wifi_wl_address_view.setVisibility(Button.GONE);
                ctv_sync_allow_global_ip_addr.setVisibility(CheckedTextView.VISIBLE);
//                if (spinnerSyncWifiStatus.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_specific_ap))) {
//                    ll_wifi_wl_view.setVisibility(Button.VISIBLE);
//                    ll_wifi_wl_ap_view.setVisibility(Button.VISIBLE);
//                    ll_wifi_wl_address_view.setVisibility(Button.GONE);
//                } else
                if (spinnerSyncWifiStatus.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_private_address))) {
                    ctv_sync_allow_global_ip_addr.setVisibility(CheckedTextView.GONE);
                } else if (spinnerSyncWifiStatus.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_specific_address))) {
                    ll_wifi_wl_view.setVisibility(Button.VISIBLE);
//                    ll_wifi_wl_ap_view.setVisibility(Button.GONE);
                    ll_wifi_wl_address_view.setVisibility(Button.VISIBLE);
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setSyncTaskFieldHelpListener(mDialog, n_sti);

        master_folder_info.setText(buildMasterSyncFolderInfo(n_sti, master_folder_info, master_folder_icon));
        master_folder_info.requestLayout();
        target_folder_info.setText(buildTargetSyncFolderInfo(n_sti, target_folder_info, target_folder_icon));
        target_folder_info.requestLayout();

        boolean is_all_file_type = false;
        if (!n_sti.isSyncFileTypeAudio() && !n_sti.isSyncFileTypeImage() && !n_sti.isSyncFileTypeVideo() &&
                n_sti.getFileFilter().size() == 0) is_all_file_type = true;

        boolean is_all_sub_dir = false;
        if (n_sti.getDirFilter().size() == 0) is_all_sub_dir = true;

        final Button dir_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_dir_filter_btn);
        final Button file_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_file_filter_btn);
//		final TextView dlg_file_filter=(TextView) mDialog.findViewById(R.id.sync_filter_summary_file_filter);
        file_filter_btn.setText(buildFileFilterInfo(n_sti.getFileFilter()));
//		final TextView dlg_dir_filter=(TextView) mDialog.findViewById(R.id.sync_filter_summary_dir_filter);
        dir_filter_btn.setText(buildDirectoryFilterInfo(n_sti.getDirFilter()));

        final LinearLayout ll_file_filter_detail = (LinearLayout) mDialog.findViewById(R.id.sync_filter_file_type_detail_view);
        final LinearLayout ll_dir_filter_detail = (LinearLayout) mDialog.findViewById(R.id.sync_filter_sub_directory_detail_view);

        final CheckedTextView ctvSyncFileTypeSpecific = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_specific);
        ctvSyncFileTypeSpecific.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctvSyncFileTypeSpecific.isChecked();
                ctvSyncFileTypeSpecific.setChecked(isChecked);
                if (isChecked) ll_file_filter_detail.setVisibility(Button.VISIBLE);
                else {
                    ll_file_filter_detail.setVisibility(Button.GONE);
                    n_sti.getFileFilter().clear();
                    file_filter_btn.setText(buildFileFilterInfo(n_sti.getFileFilter()));
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });
        ctvSyncFileTypeSpecific.setChecked(!is_all_file_type);
        if (!is_all_file_type) ll_file_filter_detail.setVisibility(Button.VISIBLE);
        else ll_file_filter_detail.setVisibility(Button.GONE);

        final CheckedTextView ctvSyncFileTypeAudio = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_audio);
        ctvSyncFileTypeAudio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctvSyncFileTypeAudio.isChecked();
                ctvSyncFileTypeAudio.setChecked(isChecked);
                if (isChecked) {
                    String f_ext="", sep="";
                    for(String item:SYNC_FILE_TYPE_AUDIO) {
                        f_ext+=sep+item;
                        sep=", ";
                    }
                    mUtil.showCommonDialog(false, "I",
                            mContext.getString(R.string.msgs_profile_sync_task_sync_file_type_add_filter_title),f_ext,null );
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });
        ctvSyncFileTypeAudio.setChecked(n_sti.isSyncFileTypeAudio());

        final CheckedTextView ctvSyncFileTypeImage = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_image);
        ctvSyncFileTypeImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctvSyncFileTypeImage.isChecked();
                ctvSyncFileTypeImage.setChecked(isChecked);
                if (isChecked) {
                    String f_ext="", sep="";
                    for(String item:SYNC_FILE_TYPE_IMAGE) {
                        f_ext+=sep+item;
                        sep=", ";
                    }
                    mUtil.showCommonDialog(false, "I",
                            mContext.getString(R.string.msgs_profile_sync_task_sync_file_type_add_filter_title),f_ext,null );
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });
        ctvSyncFileTypeImage.setChecked(n_sti.isSyncFileTypeImage());

        final CheckedTextView ctvSyncFileTypeVideo = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_video);
        ctvSyncFileTypeVideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctvSyncFileTypeVideo.isChecked();
                ctvSyncFileTypeVideo.setChecked(isChecked);
                if (isChecked) {
                    String f_ext="", sep="";
                    for(String item:SYNC_FILE_TYPE_VIDEO) {
                        f_ext+=sep+item;
                        sep=", ";
                    }
                    mUtil.showCommonDialog(false, "I",
                            mContext.getString(R.string.msgs_profile_sync_task_sync_file_type_add_filter_title),f_ext,null );
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });
        ctvSyncFileTypeVideo.setChecked(n_sti.isSyncFileTypeVideo());

        final CheckedTextView ctvSyncSpecificSubDir = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_sub_directory_specific);
        ctvSyncSpecificSubDir.setChecked(!is_all_sub_dir);
        ctvSyncSpecificSubDir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctvSyncSpecificSubDir.isChecked();
                ctvSyncSpecificSubDir.setChecked(isChecked);
                if (isChecked) ll_dir_filter_detail.setVisibility(Button.VISIBLE);
                else {
                    ll_dir_filter_detail.setVisibility(Button.GONE);
                    n_sti.getDirFilter().clear();
                    dir_filter_btn.setText(buildDirectoryFilterInfo(n_sti.getDirFilter()));
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });
        if (!is_all_sub_dir) ll_dir_filter_detail.setVisibility(Button.VISIBLE);
        else ll_dir_filter_detail.setVisibility(Button.GONE);

        final CheckedTextView ctvProcessRootDirFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_master_root_dir_file);
        CommonUtilities.setCheckedTextView(ctvProcessRootDirFile);
        ctvProcessRootDirFile.setChecked(n_sti.isSyncProcessRootDirFile());

        final CheckedTextView ctvSyncSubDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_sub_dir);
        CommonUtilities.setCheckedTextView(ctvSyncSubDir);
        ctvSyncSubDir.setChecked(n_sti.isSyncOptionSyncSubDirectory());

        if (n_sti.isSyncProcessRootDirFile()) {
            ctvProcessRootDirFile.setChecked(true);
            ctvSyncSubDir.setChecked(n_sti.isSyncOptionSyncSubDirectory());
        } else {
            ctvProcessRootDirFile.setChecked(false);
            ctvSyncSubDir.setChecked(true);
            ctvSyncSubDir.setEnabled(false);
        }

        ctvProcessRootDirFile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctvProcessRootDirFile.toggle();
                boolean isChecked = ctvProcessRootDirFile.isChecked();
                if (!isChecked) {
                    ctvSyncSubDir.setEnabled(false);
                    ctvSyncSubDir.setChecked(true);
                } else {
                    ctvSyncSubDir.setEnabled(true);
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        setCtvListenerForEditSyncTask(ctvSyncSubDir, type, n_sti, dlg_msg);

        final CheckedTextView ctvSyncEmptyDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_empty_directory);
        CommonUtilities.setCheckedTextView(ctvSyncEmptyDir);
        ctvSyncEmptyDir.setChecked(n_sti.isSyncOptionSyncEmptyDirectory());
        setCtvListenerForEditSyncTask(ctvSyncEmptyDir, type, n_sti, dlg_msg);

        final CheckedTextView ctvSyncHiddenDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_directory);
        CommonUtilities.setCheckedTextView(ctvSyncHiddenDir);
        ctvSyncHiddenDir.setChecked(n_sti.isSyncOptionSyncHiddenDirectory());
        setCtvListenerForEditSyncTask(ctvSyncHiddenDir, type, n_sti, dlg_msg);

        final CheckedTextView ctvSyncHiddenFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_file);
        CommonUtilities.setCheckedTextView(ctvSyncHiddenFile);
        ctvSyncHiddenFile.setChecked(n_sti.isSyncOptionSyncHiddenFile());
        setCtvListenerForEditSyncTask(ctvSyncHiddenFile, type, n_sti, dlg_msg);

        final CheckedTextView ctvProcessOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_process_override_delete_file);
        ctvProcessOverride.setChecked(n_sti.isSyncOverrideCopyMoveFile());
        setCtvListenerForEditSyncTask(ctvProcessOverride, type, n_sti, dlg_msg);

        final CheckedTextView ctvConfirmOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_confirm_override_delete_file);
        CommonUtilities.setCheckedTextView(ctvConfirmOverride);
        ctvConfirmOverride.setChecked(n_sti.isSyncConfirmOverrideOrDelete());
        setCtvListenerForEditSyncTask(ctvConfirmOverride, type, n_sti, dlg_msg);

        final CheckedTextView ctvDeleteFirst = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_delete_first_when_mirror);
        CommonUtilities.setCheckedTextView(ctvDeleteFirst);
        ctvDeleteFirst.setChecked(n_sti.isSyncOptionDeleteFirstWhenMirror());
        setCtvListenerForEditSyncTask(ctvDeleteFirst, type, n_sti, dlg_msg);

        final CheckedTextView ctvEnsureTargetExactMirror = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_ensure_target_is_exact_mirror);
        CommonUtilities.setCheckedTextView(ctvEnsureTargetExactMirror);
        ctvEnsureTargetExactMirror.setChecked(n_sti.isSyncOptionEnsureTargetIsExactMirror());
//        setSpinnerSyncTaskType(spinnerSyncType, n_sti);
        ctvEnsureTargetExactMirror.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctvEnsureTargetExactMirror.isChecked();
                NotifyEvent ntfy=new NotifyEvent(mContext);
                if (isChecked) {//if checked, display warning that also files not matching the filters will be removed from the target
                    mUtil.showCommonDialog(true, "D",
                            mContext.getString(R.string.msgs_profile_sync_task_sync_option_ensure_target_is_exact_mirror_warn_dialog_title),
                            mContext.getString(R.string.msgs_profile_sync_task_sync_option_ensure_target_is_exact_mirror_warn_dialog),
                            mContext.getString(R.string.msgs_common_dialog_confirm),
                            mContext.getString(R.string.msgs_common_dialog_cancel),
                            ntfy);
                } else {//if unchecked, no unexpected deletes will ever be done on the target (delete only files/dir included by filters AND removed from master), do not display warning
                    ctvEnsureTargetExactMirror.setChecked(isChecked);
//                    setSpinnerSyncTaskType(spinnerSyncType, n_sti);
                    checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                }

                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        ctvEnsureTargetExactMirror.setChecked(isChecked);//true
//                        setSpinnerSyncTaskType(spinnerSyncType, n_sti);
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                    }
                });
            }
        });

        final CheckedTextView ctUseExtendedDirectoryFilter1 = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_extended_filter1);
        final LinearLayout ll_ctUseExtendedDirectoryFilter1 = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_ll_sync_use_extended_filter1);
        CommonUtilities.setCheckedTextView(ctUseExtendedDirectoryFilter1);
        ctUseExtendedDirectoryFilter1.setChecked(n_sti.isSyncOptionUseExtendedDirectoryFilter1());
        if (n_sti.isSyncOptionUseDirectoryFilterV2()) ll_ctUseExtendedDirectoryFilter1.setVisibility(LinearLayout.GONE);
        else ll_ctUseExtendedDirectoryFilter1.setVisibility(LinearLayout.VISIBLE);
        setCtvListenerForEditSyncTask(ctUseExtendedDirectoryFilter1, type, n_sti, dlg_msg);

        final CheckedTextView ctUseDirectoryFilterV2 = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_directory_filter_v2);
        CommonUtilities.setCheckedTextView(ctUseDirectoryFilterV2);
        ctUseDirectoryFilterV2.setChecked(n_sti.isSyncOptionUseDirectoryFilterV2());
        ctUseDirectoryFilterV2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctUseDirectoryFilterV2.isChecked();
                NotifyEvent ntfy=new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        ctUseDirectoryFilterV2.setChecked(isChecked);
                        if (isChecked) ll_ctUseExtendedDirectoryFilter1.setVisibility(LinearLayout.GONE);
                        else ll_ctUseExtendedDirectoryFilter1.setVisibility(LinearLayout.VISIBLE);

                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                    }
                });
                mUtil.showCommonDialog(true, "D",
                        mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_v2_title),
                        mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_v2_warning),
                        mContext.getString(R.string.msgs_common_dialog_confirm),
                        mContext.getString(R.string.msgs_common_dialog_cancel),
                        ntfy);
            }
        });
        //Disable option for release APK
//        if (!mGp.debuggable) {
//            CommonDialog.setViewEnabled(getActivity(), ctUseDirectoryFilterV2, false);
//            ctUseDirectoryFilterV2.setChecked(false);
//            CommonDialog.setViewEnabled(getActivity(), ctvEnsureTargetExactMirror, false);
//            ctvEnsureTargetExactMirror.setChecked(false);
//        }

        final LinearLayout ll_special_option_view = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_special_option_view);
        final CheckedTextView ctvShowSpecialOption = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_show_special_option);
        ll_special_option_view.setVisibility(LinearLayout.GONE);
        ctvShowSpecialOption.setChecked(false);
        ctvShowSpecialOption.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctvShowSpecialOption.isChecked();
                ctvShowSpecialOption.setChecked(isChecked);
                if (isChecked) ll_special_option_view.setVisibility(LinearLayout.VISIBLE);
                else ll_special_option_view.setVisibility(LinearLayout.GONE);
            }
        });

        final CheckedTextView ctvDoNotResetFileLastMod = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_do_mot_reset_file_last_mod_time);
        CommonUtilities.setCheckedTextView(ctvDoNotResetFileLastMod);
        ctvDoNotResetFileLastMod.setChecked(n_sti.isSyncDoNotResetFileLastModified());
//        setCtvListenerForEditSyncTask(ctvDoNotResetFileLastMod, type, n_sti, dlg_msg);
        ctvDoNotResetFileLastMod.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctvDoNotResetFileLastMod.isChecked();
                NotifyEvent ntfy=new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        ctvDoNotResetFileLastMod.setChecked(true);
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                    }
                });
                if (isChecked) {
                    mUtil.showCommonDialog(true, "D",
                            mContext.getString(R.string.msgs_profile_sync_task_sync_option_not_set_file_last_modified),
                            mContext.getString(R.string.msgs_profile_sync_task_sync_option_not_set_file_last_modified_warning),
                            mContext.getString(R.string.msgs_common_dialog_confirm),
                            mContext.getString(R.string.msgs_common_dialog_cancel),
                            ntfy);
                } else {
                    ctvDoNotResetFileLastMod.setChecked(false);
                    checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                }
            }
        });


        final CheckedTextView ctvUseSmbsyncLastMod = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_use_smbsync_last_mod_time);
        CommonUtilities.setCheckedTextView(ctvUseSmbsyncLastMod);
        ctvUseSmbsyncLastMod.setChecked(n_sti.isSyncDetectLastModifiedBySmbsync());
        setCtvListenerForEditSyncTask(ctvUseSmbsyncLastMod, type, n_sti, dlg_msg);

        final CheckedTextView ctvTestMode = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_test_mode);
        ctvTestMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctvTestMode.isChecked();
                ctvTestMode.setChecked(isChecked);
                if (isChecked) {
                    ctv_auto.setEnabled(false);
                } else {
                    ctv_auto.setEnabled(true);
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });
        ctvTestMode.setChecked(n_sti.isSyncTestMode());
        if (ctvTestMode.isChecked()) {
            ctv_auto.setEnabled(false);
        } else {
            ctv_auto.setEnabled(true);
        }

        final CheckedTextView ctDeterminChangedFileByTime = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_last_mod_time);
        ctDeterminChangedFileByTime.setChecked(n_sti.isSyncOptionDifferentFileByTime());
        final CheckedTextView ctvDeterminChangedFileSizeGtTarget = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_file_size_greater_than_target);
        ctvDeterminChangedFileSizeGtTarget.setChecked(n_sti.isSyncDifferentFileSizeGreaterThanTagetFile());
        final CheckedTextView ctvDiffUseFileSize = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_file_size);
        CommonUtilities.setCheckedTextView(ctvDiffUseFileSize);
        ctvDiffUseFileSize.setChecked(n_sti.isSyncOptionDifferentFileBySize());
        ctvDiffUseFileSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                if (isChecked) {
                    ctvDeterminChangedFileSizeGtTarget.setEnabled(true);
                } else {
                    ctvDeterminChangedFileSizeGtTarget.setEnabled(false);
                    if (!ctDeterminChangedFileByTime.isChecked()) {
                        mUtil.showCommonDialog(false, "D",
                                mContext.getString(R.string.msgs_profile_sync_task_sync_option_copy_no_compare_title),
                                mContext.getString(R.string.msgs_profile_sync_task_sync_option_copy_no_compare_warning),
                                mContext.getString(R.string.msgs_common_dialog_confirm),
                                mContext.getString(R.string.msgs_common_dialog_cancel),
                                null );
                    }
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        setCtvListenerForEditSyncTask(ctvDeterminChangedFileSizeGtTarget, type, n_sti, dlg_msg);

        if (ctvDiffUseFileSize.isChecked()) ctvDeterminChangedFileSizeGtTarget.setEnabled(true);
        else ctvDeterminChangedFileSizeGtTarget.setEnabled(false);

        final Spinner spinnerSyncDiffTimeValue = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_diff_file_determin_time_value);
        setSpinnerSyncTaskDiffTimeValue(spinnerSyncDiffTimeValue, n_sti.getSyncOptionDifferentFileAllowableTime());

        final LinearLayout ll_DeterminChangedFileByTime_dependant_view=(LinearLayout)mDialog.findViewById(R.id.edit_sync_task_option_sync_diff_use_last_mod_time_dependant_view);

        final Spinner spinnerSyncDstOffsetValue = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_offset_daylight_saving_time_value);
        setSpinnerSyncTaskDstOffsetValue(spinnerSyncDstOffsetValue, n_sti.getSyncOptionOffsetOfDst());
        final LinearLayout ll_offset_dst_view=(LinearLayout)mDialog.findViewById(R.id.edit_sync_task_option_spinner_offset_daylight_saving_time_value_view);
        if (n_sti.isSyncOptionIgnoreDstDifference()) {
            ll_offset_dst_view.setVisibility(LinearLayout.VISIBLE);
        } else {
            ll_offset_dst_view.setVisibility(LinearLayout.GONE);
        }

        if (n_sti.isSyncOptionDifferentFileByTime()) {
            ll_DeterminChangedFileByTime_dependant_view.setVisibility(LinearLayout.VISIBLE);
            if (n_sti.isSyncOptionIgnoreDstDifference()) {
                ll_offset_dst_view.setVisibility(LinearLayout.VISIBLE);
            } else {
                ll_offset_dst_view.setVisibility(LinearLayout.GONE);
            }
        } else {
            ll_DeterminChangedFileByTime_dependant_view.setVisibility(LinearLayout.GONE);
            ll_offset_dst_view.setVisibility(LinearLayout.GONE);
        }

        ctDeterminChangedFileByTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                if (isChecked) {
                    ll_DeterminChangedFileByTime_dependant_view.setVisibility(LinearLayout.VISIBLE);
                    if (ctv_ignore_dst_difference.isChecked()) {
                        ll_offset_dst_view.setVisibility(LinearLayout.VISIBLE);
                    } else {
                        ll_offset_dst_view.setVisibility(LinearLayout.GONE);
                    }
                } else {
                    ll_DeterminChangedFileByTime_dependant_view.setVisibility(LinearLayout.GONE);
                    if (!ctvDiffUseFileSize.isChecked()) {
                        mUtil.showCommonDialog(false, "D",
                                mContext.getString(R.string.msgs_profile_sync_task_sync_option_copy_no_compare_title),
                                mContext.getString(R.string.msgs_profile_sync_task_sync_option_copy_no_compare_warning),
                                mContext.getString(R.string.msgs_common_dialog_confirm),
                                mContext.getString(R.string.msgs_common_dialog_cancel),
                                null );
                    }
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        ctv_ignore_dst_difference.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                if (isChecked) {
                    ll_offset_dst_view.setVisibility(LinearLayout.VISIBLE);
                } else {
                    ll_offset_dst_view.setVisibility(LinearLayout.GONE);
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        spinnerSyncDstOffsetValue.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerSyncDiffTimeValue.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        CommonDialog.setDlgBoxSizeLimit(mDialog, true);

        et_sync_main_task_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                n_sti.setSyncTaskName(s.toString());
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });
        if (n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) CommonDialog.setViewEnabled(getActivity(), swap_master_target,false);
        else CommonDialog.setViewEnabled(getActivity(), swap_master_target, true);

        master_folder_info.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        final TextView dlg_title = (TextView) mDialog.findViewById(R.id.edit_profile_sync_title);
                        final TextView dlg_title_sub = (TextView) mDialog.findViewById(R.id.edit_profile_sync_title_sub);
                        dlg_title.requestLayout();
                        dlg_title_sub.requestLayout();
                        SyncFolderEditValue nsfev = (SyncFolderEditValue) o[0];
                        nsfev.folder_master = true;
                        String prev_master_folder_type = n_sti.getMasterFolderType();
                        n_sti.setMasterDirectoryName(nsfev.folder_directory);
                        n_sti.setMasterLocalMountPoint(nsfev.folder_mountpoint);
                        n_sti.setMasterSmbAddr(nsfev.folder_remote_addr);
                        n_sti.setMasterSmbDomain(nsfev.folder_remote_domain);
                        n_sti.setMasterSmbHostName(nsfev.folder_remote_host);
                        n_sti.setMasterSmbPort(nsfev.folder_remote_port);
                        n_sti.setMasterSmbPassword(nsfev.folder_remote_pswd);
                        n_sti.setMasterSmbShareName(nsfev.folder_remote_share);
                        n_sti.setMasterSmbUserName(nsfev.folder_remote_user);
                        n_sti.setMasterFolderType(nsfev.folder_type);
                        n_sti.setMasterSmbProtocol(nsfev.folder_smb_protocol);
                        n_sti.setMasterSmbIpcSigningEnforced(nsfev.folder_smb_ipc_enforced);
                        n_sti.setMasterSmbUseSmb2Negotiation(nsfev.folder_smb_use_smb2_negotiation);
                        n_sti.setMasterRemovableStorageID(nsfev.folder_removable_uuid);
//						n_sti.setMasterFolderUseInternalUsbFolder(nsfev.folder_use_usb_folder);
//						Log.v("","mdi="+n_sti.getMasterDirectoryName());
                        n_sti.setMasterFolderError(nsfev.folder_error_code);
                        master_folder_info.setText(buildMasterSyncFolderInfo(n_sti, master_folder_info, master_folder_icon));
                        master_folder_info.requestLayout();
                        target_folder_info.setText(buildTargetSyncFolderInfo(n_sti, target_folder_info, target_folder_icon));
                        target_folder_info.requestLayout();

                        setSpinnerSyncTaskType(spinnerSyncType, n_sti);
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                        if (!prev_master_folder_type.equals(n_sti.getMasterFolderType())) {
                            ll_wifi_condition_view.setVisibility(LinearLayout.VISIBLE);
                            ll_advanced_network_option_view.setVisibility(LinearLayout.VISIBLE);
                            if ((!n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) &&
                                    !n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB))) {
                                ll_wifi_condition_view.setVisibility(LinearLayout.GONE);
                                ll_advanced_network_option_view.setVisibility(LinearLayout.GONE);
                            } else if (n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) ||
                                    n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                                if (n_sti.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF)) {
                                    String msg="", opt_temp="";
                                    opt_temp=SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP;
                                    msg=mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_change_wifi_confition_to_any);
//                                    if ((Build.VERSION.SDK_INT>=27 && CommonUtilities.isLocationServiceEnabled(mContext, mGp)) || Build.VERSION.SDK_INT<=26) {
//                                        opt_temp=SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP;
//                                        msg=mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_change_wifi_confition_to_any);
//                                    } else {
//                                        opt_temp=SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_PRIVATE_ADDR;
//                                        msg=mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_change_wifi_confition_has_private);
//                                    }
                                    final String option=opt_temp;
                                    NotifyEvent ntfy = new NotifyEvent(mContext);
                                    ntfy.setListener(new NotifyEventListener() {
                                        @Override
                                        public void positiveResponse(Context context, Object[] objects) {
                                            n_sti.setSyncOptionWifiStatusOption(option);
                                            spinnerSyncWifiStatus.setSelection(Integer.valueOf(option));
                                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                                        }

                                        @Override
                                        public void negativeResponse(Context context, Object[] objects) {
                                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                                        }
                                    });
                                    mUtil.showCommonDialog(true, "W", msg, "", ntfy);
                                }
                            }
                        } else {
                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        mGp.safMgr.loadSafFile();
                        master_folder_info.setText(buildMasterSyncFolderInfo(n_sti, master_folder_info, master_folder_icon));
                        master_folder_info.requestLayout();
                    }

                });
                SyncFolderEditValue sfev = new SyncFolderEditValue();
                sfev.folder_master = true;
                sfev.folder_title = mContext.getString(R.string.msgs_main_sync_profile_dlg_title_master);
                sfev.folder_directory = n_sti.getMasterDirectoryName();
                sfev.folder_mountpoint = n_sti.getMasterLocalMountPoint();
                sfev.folder_remote_addr = n_sti.getMasterSmbAddr();
                sfev.folder_remote_domain = n_sti.getMasterSmbDomain();
                sfev.folder_remote_host = n_sti.getMasterSmbHostName();
                sfev.folder_remote_port = n_sti.getMasterSmbPort();
                sfev.folder_remote_pswd = n_sti.getMasterSmbPassword();
                sfev.folder_remote_share = n_sti.getMasterSmbShareName();
                sfev.folder_remote_user = n_sti.getMasterSmbUserName();
                sfev.folder_smb_protocol=n_sti.getMasterSmbProtocol();
                sfev.folder_smb_ipc_enforced=n_sti.isMasterSmbIpcSigningEnforced();
                sfev.folder_smb_use_smb2_negotiation=n_sti.isMasterSmbUseSmb2Negotiation();
                sfev.folder_removable_uuid = n_sti.getMasterRemovableStorageID();
                sfev.folder_type = n_sti.getMasterFolderType();
                if (!sfev.folder_remote_user.equals("") || !sfev.folder_remote_pswd.equals("")) {
                    sfev.folder_remote_use_pswd=true;
                } else {
                    sfev.folder_remote_use_pswd=false;
                }
                sfev.folder_error_code=n_sti.getMasterFolderError();
                editSyncFolder(false, n_sti, sfev, ntfy);
            }
        });

        swap_master_target.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                NotifyEvent ntfy_swap=new NotifyEvent(mContext);
                ntfy_swap.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        SyncTaskItem t_sti = n_sti.clone();
                        n_sti.setTargetUseTakenDateTimeToDirectoryNameKeyword(t_sti.isTargetUseTakenDateTimeToDirectoryNameKeyword());
                        n_sti.setMasterDirectoryName(t_sti.getTargetDirectoryName());
                        n_sti.setMasterLocalMountPoint(t_sti.getTargetLocalMountPoint());
                        n_sti.setMasterFolderType(t_sti.getTargetFolderType());
                        n_sti.setMasterSmbAddr(t_sti.getTargetSmbAddr());
                        n_sti.setMasterSmbDomain(t_sti.getTargetSmbDomain());
                        n_sti.setMasterSmbHostName(t_sti.getTargetSmbHostName());
                        n_sti.setMasterSmbPassword(t_sti.getTargetSmbPassword());
                        n_sti.setMasterSmbPort(t_sti.getTargetSmbPort());
                        n_sti.setMasterSmbShareName(t_sti.getTargetSmbShareName());
                        n_sti.setMasterSmbUserName(t_sti.getTargetSmbUserName());
                        n_sti.setMasterSmbProtocol(t_sti.getTargetSmbProtocol());
                        n_sti.setMasterSmbIpcSigningEnforced(t_sti.isTargetSmbIpcSigningEnforced());
                        n_sti.setMasterRemovableStorageID(t_sti.getTargetRemovableStorageID());
                        n_sti.setMasterFolderError(t_sti.getTargetFolderError());

                        n_sti.setTargetDirectoryName(t_sti.getMasterDirectoryName());
                        n_sti.setTargetLocalMountPoint(t_sti.getMasterLocalMountPoint());
                        n_sti.setTargetFolderType(t_sti.getMasterFolderType());
                        n_sti.setTargetSmbAddr(t_sti.getMasterSmbAddr());
                        n_sti.setTargetSmbDomain(t_sti.getMasterSmbDomain());
                        n_sti.setTargetSmbHostname(t_sti.getMasterSmbHostName());
                        n_sti.setTargetSmbPassword(t_sti.getMasterSmbPassword());
                        n_sti.setTargetSmbPort(t_sti.getMasterSmbPort());
                        n_sti.setTargetSmbShareName(t_sti.getMasterSmbShareName());
                        n_sti.setTargetSmbUserName(t_sti.getMasterSmbUserName());
                        n_sti.setTargetSmbProtocol(t_sti.getMasterSmbProtocol());
                        n_sti.setTargetSmbIpcSigningEnforced(t_sti.isMasterSmbIpcSigningEnforced());
                        n_sti.setTargetSmbUseSmb2Negotiation(t_sti.isMasterSmbUseSmb2Negotiation());
                        n_sti.setTargetRemovableStorageID(t_sti.getMasterRemovableStorageID());
                        n_sti.setTargetFolderError(t_sti.getMasterFolderError());

                        master_folder_info.setText(buildMasterSyncFolderInfo(n_sti, master_folder_info, master_folder_icon));
                        master_folder_info.requestLayout();
                        target_folder_info.setText(buildTargetSyncFolderInfo(n_sti, target_folder_info, target_folder_icon));
                        target_folder_info.requestLayout();

                        setSpinnerSyncTaskType(spinnerSyncType, n_sti);
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {}
                });
                mUtil.showCommonDialog(true, "D",
                        mContext.getString(R.string.msgs_profile_sync_task_dlg_swap_warning_title),
                        mContext.getString(R.string.msgs_profile_sync_task_dlg_swap_warning_message),
                        mContext.getString(R.string.msgs_common_dialog_confirm),
                        mContext.getString(R.string.msgs_common_dialog_cancel),
                        ntfy_swap);
            }
        });

        target_folder_info.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        final TextView dlg_title = (TextView) mDialog.findViewById(R.id.edit_profile_sync_title);
                        final TextView dlg_title_sub = (TextView) mDialog.findViewById(R.id.edit_profile_sync_title_sub);
                        dlg_title.requestLayout();
                        dlg_title_sub.requestLayout();
                        String prev_target_folder_type = n_sti.getTargetFolderType();
                        SyncFolderEditValue nsfev = (SyncFolderEditValue) o[0];
                        nsfev.folder_master = false;
                        n_sti.setTargetUseTakenDateTimeToDirectoryNameKeyword(nsfev.folder_use_taken_date_time_for_directory_keyword);
                        n_sti.setTargetDirectoryName(nsfev.folder_directory);
                        n_sti.setTargetLocalMountPoint(nsfev.folder_mountpoint);
                        n_sti.setTargetSmbAddr(nsfev.folder_remote_addr);
                        n_sti.setTargetSmbDomain(nsfev.folder_remote_domain);
                        n_sti.setTargetSmbHostname(nsfev.folder_remote_host);
                        n_sti.setTargetSmbPort(nsfev.folder_remote_port);
                        n_sti.setTargetSmbPassword(nsfev.folder_remote_pswd);
                        n_sti.setTargetSmbShareName(nsfev.folder_remote_share);
                        n_sti.setTargetSmbUserName(nsfev.folder_remote_user);
                        n_sti.setTargetFolderType(nsfev.folder_type);
                        n_sti.setTargetSmbProtocol(nsfev.folder_smb_protocol);
                        n_sti.setTargetSmbIpcSigningEnforced(nsfev.folder_smb_ipc_enforced);
                        n_sti.setTargetSmbUseSmb2Negotiation(nsfev.folder_smb_use_smb2_negotiation);
                        n_sti.setTargetRemovableStorageID(nsfev.folder_removable_uuid);

                        n_sti.setArchiveCreateDirectory(nsfev.archive_create_directory);
                        n_sti.setArchiveUseRename(nsfev.archive_rename_when_archive);
                        n_sti.setSyncOptionConfirmNotExistsExifDate(nsfev.archive_confirm_exif_date);
                        n_sti.setArchiveCreateDirectoryTemplate(nsfev.archive_directory_name_template);
                        n_sti.setArchiveSuffixOption(nsfev.archive_append_seq_number);
                        n_sti.setArchiveRenameFileTemplate(nsfev.archive_file_name_template);
                        n_sti.setArchiveRetentionPeriod(nsfev.archive_retention_period);

                        n_sti.setTargetZipUseExternalSdcard(nsfev.zip_file_use_sdcard);
                        n_sti.setTargetZipCompressionLevel(nsfev.zip_comp_level);
                        n_sti.setTargetZipEncryptMethod(nsfev.zip_enc_method);
                        n_sti.setTargetZipOutputFileName(nsfev.zip_file_name);
                        n_sti.setTargetZipPassword(nsfev.zip_file_password);
                        n_sti.setTargetFolderError(nsfev.folder_error_code);

                        master_folder_info.setText(buildMasterSyncFolderInfo(n_sti, master_folder_info, master_folder_icon));
                        master_folder_info.requestLayout();
                        target_folder_info.setText(buildTargetSyncFolderInfo(n_sti, target_folder_info, target_folder_icon));
                        target_folder_info.requestLayout();

                        if (n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) CommonDialog.setViewEnabled(getActivity(), swap_master_target, false);
                        else CommonDialog.setViewEnabled(getActivity(), swap_master_target, true);

                        setSpinnerSyncTaskType(spinnerSyncType, n_sti);
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);

                        if (!prev_target_folder_type.equals(n_sti.getTargetFolderType())) {
                            ll_wifi_condition_view.setVisibility(LinearLayout.VISIBLE);
                            ll_advanced_network_option_view.setVisibility(LinearLayout.VISIBLE);
                            if (!n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) &&
                                    !n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                                ll_wifi_condition_view.setVisibility(LinearLayout.GONE);
                                ll_advanced_network_option_view.setVisibility(LinearLayout.GONE);
                            } else if (n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) ||
                                    n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                                if (n_sti.getSyncOptionWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF)) {
                                    String msg="", opt_temp="";
                                    opt_temp=SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP;
                                    msg=mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_change_wifi_confition_to_any);
//                                    if ((Build.VERSION.SDK_INT>=27 && CommonUtilities.isLocationServiceEnabled(mContext, mGp)) || Build.VERSION.SDK_INT<=26) {
//                                        opt_temp=SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP;
//                                        msg=mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_change_wifi_confition_to_any);
//                                    } else {
//                                        opt_temp=SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_PRIVATE_ADDR;
//                                        msg=mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_change_wifi_confition_has_private);
//                                    }
                                    final String option=opt_temp;
                                    NotifyEvent ntfy = new NotifyEvent(mContext);
                                    ntfy.setListener(new NotifyEventListener() {
                                        @Override
                                        public void positiveResponse(Context context, Object[] objects) {
                                            n_sti.setSyncOptionWifiStatusOption(option);
                                            spinnerSyncWifiStatus.setSelection(Integer.valueOf(option));
                                            confirmUseAppSpecificDir(n_sti, n_sti.getTargetDirectoryName(), null);
                                        }

                                        @Override
                                        public void negativeResponse(Context context, Object[] objects) {
                                            confirmUseAppSpecificDir(n_sti, n_sti.getTargetDirectoryName(), null);
                                        }
                                    });
                                    mUtil.showCommonDialog(true, "W", msg, "", ntfy);
                                }
                            }
                        } else {
                            confirmUseAppSpecificDir(n_sti, n_sti.getTargetDirectoryName(), null);
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        mGp.safMgr.loadSafFile();
                        target_folder_info.setText(buildTargetSyncFolderInfo(n_sti, target_folder_info, target_folder_icon));
                        target_folder_info.requestLayout();
                        confirmUseAppSpecificDir(n_sti, n_sti.getTargetDirectoryName(), null);
                    }

                });
                SyncFolderEditValue sfev = new SyncFolderEditValue();
                sfev.folder_master = false;
                sfev.folder_title = mContext.getString(R.string.msgs_main_sync_profile_dlg_title_target);
                sfev.folder_use_taken_date_time_for_directory_keyword=n_sti.isTargetUseTakenDateTimeToDirectoryNameKeyword();
                sfev.folder_directory = n_sti.getTargetDirectoryName();
                sfev.folder_mountpoint = n_sti.getTargetLocalMountPoint();
                sfev.folder_remote_addr = n_sti.getTargetSmbAddr();
                sfev.folder_remote_domain = n_sti.getTargetSmbDomain();
                sfev.folder_remote_host = n_sti.getTargetSmbHostName();
                sfev.folder_remote_port = n_sti.getTargetSmbPort();
                sfev.folder_remote_pswd = n_sti.getTargetSmbPassword();
                sfev.folder_remote_share = n_sti.getTargetSmbShareName();
                sfev.folder_remote_user = n_sti.getTargetSmbUserName();
                sfev.folder_type = n_sti.getTargetFolderType();
                sfev.folder_smb_protocol = n_sti.getTargetSmbProtocol();
                sfev.folder_smb_ipc_enforced=n_sti.isTargetSmbIpcSigningEnforced();
                sfev.folder_smb_use_smb2_negotiation=n_sti.isTargetSmbUseSmb2Negotiation();
                sfev.folder_removable_uuid = n_sti.getTargetRemovableStorageID();

                if (!sfev.folder_remote_user.equals("") || !sfev.folder_remote_pswd.equals("")) {
                    sfev.folder_remote_use_pswd=true;
                } else {
                    sfev.folder_remote_use_pswd=false;
                }

                sfev.zip_file_use_sdcard = n_sti.isTargetZipUseExternalSdcard();
                sfev.zip_comp_level = n_sti.getTargetZipCompressionLevel();
                sfev.zip_enc_method = n_sti.getTargetZipEncryptMethod();
                sfev.zip_file_name = n_sti.getTargetZipOutputFileName();
                sfev.zip_file_password = n_sti.getTargetZipPassword();
                sfev.zip_file_confirm_password = n_sti.getTargetZipPassword();

                sfev.archive_create_directory=n_sti.isArchiveCreateDirectory();
                sfev.archive_rename_when_archive=n_sti.isArchiveUseRename();
                sfev.archive_confirm_exif_date=n_sti.isSyncOptionConfirmNotExistsExifDate();
                sfev.archive_directory_name_template=n_sti.getArchiveCreateDirectoryTemplate();
                sfev.archive_append_seq_number=n_sti.getArchiveSuffixOption();
                sfev.archive_file_name_template=n_sti.getArchiveRenameFileTemplate();
                sfev.archive_retention_period = n_sti.getArchiveRetentionPeriod();

                sfev.folder_error_code=n_sti.getTargetFolderError();

                editSyncFolder(false, n_sti, sfev, ntfy);
            }
        });
        // wifi ap listボタンの指定
//        edit_wifi_ap_list.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                NotifyEvent ntfy = new NotifyEvent(mContext);
//                //Listen setRemoteShare response
//                ntfy.setListener(new NotifyEventListener() {
//                    @Override
//                    public void positiveResponse(Context arg0, Object[] arg1) {
//                        setWifiApWhileListInfo(n_sti.getSyncOptionWifiConnectedAccessPointWhiteList(), edit_wifi_ap_list);
//                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
//                    }
//
//                    @Override
//                    public void negativeResponse(Context arg0, Object[] arg1) {
//                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
//                    }
//
//                });
//                mTaskUtil.editWifiAccessPointListDlg(n_sti.getSyncOptionWifiConnectedAccessPointWhiteList(), ntfy);
//            }
//        });

        // wifi address listボタンの指定
        edit_wifi_addr_list.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                //Listen setRemoteShare response
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context arg0, Object[] arg1) {
                        setWifiApWhileListInfo(n_sti.getSyncOptionWifiConnectedAddressWhiteList(), edit_wifi_addr_list);
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                    }

                    @Override
                    public void negativeResponse(Context arg0, Object[] arg1) {
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                    }

                });
                mTaskUtil.editWifiIPAddressListDlg(n_sti.getSyncOptionWifiConnectedAddressWhiteList(), ntfy);
            }
        });

        // file filterボタンの指定
        file_filter_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                invokeEditFileFilterDlg(mDialog, n_sti, type, dlg_msg);
            }
        });
        // directory filterボタンの指定
        dir_filter_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                invokeEditDirFilterDlg(mDialog, n_sti, type, dlg_msg);
            }
        });

        final Button btn_cancel = (Button) mDialog.findViewById(R.id.edit_profile_sync_dlg_btn_cancel);
        btn_cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mGp.syncTaskAdapter.notifyDataSetChanged();
                if (btn_ok.isEnabled()) {
                    NotifyEvent ntfy = new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            mUtil.addDebugMsg(1,"I","editSyncTask edit cancelled, type="+type+", task="+pfli.getSyncTaskName());
                            mFragment.dismiss();
                            if (mNotifyComplete != null)
                                mNotifyComplete.notifyToListener(false, null);
                        }

                        @Override
                        public void negativeResponse(Context context, Object[] objects) {
                        }
                    });
                    mUtil.showCommonDialog(true, "W",
                            mContext.getString(R.string.msgs_schedule_confirm_title_nosave),
                            mContext.getString(R.string.msgs_profile_sync_folder_dlg_confirm_msg_nosave), ntfy);
                } else {
                    mUtil.addDebugMsg(1,"I","editSyncTask edit cancelled");
                    mFragment.dismiss();
                    if (mNotifyComplete != null) mNotifyComplete.notifyToListener(false, null);
                }
            }
        });
        // Cancelリスナーの指定
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int kc, KeyEvent keyEvent) {
                switch (kc) {
                    case KEYCODE_BACK:
                        if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                            btn_cancel.performClick();
                        }
                        return true;
                    // break;
                    default:
                        // break;
                }

                return false;
            }
        });
        // OKボタンの指定
        btn_ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final SyncTaskItem new_stli = buildSyncTaskListItem(mDialog, n_sti);
                if (new_stli.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR))
                    new_stli.setTargetUseTakenDateTimeToDirectoryNameKeyword(false);
                NotifyEvent ntfy_target_dir_not_specified = new NotifyEvent(mContext);
                ntfy_target_dir_not_specified.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        if (type.equals("EDIT")) {
                            mGp.syncTaskAdapter.remove(pfli);
                            mGp.syncTaskAdapter.add(new_stli);
                            mGp.syncTaskAdapter.sort();
                            mGp.syncTaskAdapter.notifyDataSetChanged();
                        } else if (type.equals("COPY")) {
                            mGp.syncTaskAdapter.setAllItemChecked(false);
                            new_stli.setChecked(true);
                            new_stli.setSyncTaskPosition(mGp.syncTaskAdapter.getCount());
                            mGp.syncTaskAdapter.add(new_stli);
                            mGp.syncTaskAdapter.sort();
                            mGp.syncTaskAdapter.notifyDataSetChanged();
                            mGp.syncTaskListView.setSelection(mGp.syncTaskAdapter.getCount() - 1);
                        } else if (type.equals("ADD")) {
                            new_stli.setSyncTaskPosition(mGp.syncTaskAdapter.getCount());
                            mGp.syncTaskAdapter.add(new_stli);
                            mGp.syncTaskAdapter.sort();
                            mGp.syncTaskAdapter.notifyDataSetChanged();
                            mGp.syncTaskListView.setSelection(mGp.syncTaskAdapter.getCount() - 1);
                        }
                        if (mNotifyComplete != null) mNotifyComplete.notifyToListener(true, null);
                        SyncTaskUtil.saveSyncTaskListToFile(mGp, mContext, mUtil, false, "", "", mGp.syncTaskList, false);
                        SyncTaskUtil.autosaveSyncTaskList(mGp, getActivity(), mUtil, mCommonDlg, mGp.syncTaskList);
                        mFragment.dismissAllowingStateLoss();
                        mUtil.addDebugMsg(1,"I","editSyncTask edit saved, type="+type+", task="+new_stli.getSyncTaskName());
                        ((ActivityMain)getActivity()).refreshOptionMenu();

//                        checkLocationServiceWarning(getActivity(), mGp, mUtil);

                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {}
                });
                if (!new_stli.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP) && new_stli.getTargetDirectoryName().equals("")) {
                    mUtil.showCommonDialog(true, "W",
                            mContext.getString(R.string.msgs_main_sync_profile_dlg_target_directory_not_specified), "",
                            ntfy_target_dir_not_specified);
                } else {
                    ntfy_target_dir_not_specified.notifyToListener(true, null);
                }
            }
        });
    }

    private void setCtvListenerForEditSyncTask(CheckedTextView ctv, String type, SyncTaskItem n_sti, TextView dlg_msg) {
        ctv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });
    }

    private void checkArchiveOkButtonEnabled(SyncTaskItem n_sti, Dialog dialog) {
        final Button btn_sync_folder_ok = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_ok);
        final Spinner sp_sync_retain_period = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_retention_period);
        final Spinner sp_sync_suffix_option = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_suffix_option);

        final CheckedTextView ctvConfirmExifDate = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_confirm_exif_date);
        final CheckedTextView ctvRenameWhenArchive = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_rename_when_archive);

        final EditText et_file_template = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_file_name_template);

        final CheckedTextView ctvCreateDircetory = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_use_archive_directory);
        final EditText et_dir_template = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_archive_directory_name_template);

        boolean changed=false;

        if (sp_sync_retain_period.getSelectedItemPosition()!=n_sti.getArchiveRetentionPeriod()) changed=true;
        else {
            String seqopt="0";
            if (sp_sync_suffix_option.getSelectedItemPosition()==0) seqopt="0";
            else if (sp_sync_suffix_option.getSelectedItemPosition()==1) seqopt="2";
            else if (sp_sync_suffix_option.getSelectedItemPosition()==2) seqopt="3";
            else if (sp_sync_suffix_option.getSelectedItemPosition()==3) seqopt="4";
            else if (sp_sync_suffix_option.getSelectedItemPosition()==4) seqopt="5";
            else if (sp_sync_suffix_option.getSelectedItemPosition()==5) seqopt="6";
            if (!seqopt.equals(n_sti.getArchiveSuffixOption())) changed=true;
            else {
                if (ctvRenameWhenArchive.isChecked()!=n_sti.isArchiveUseRename()) changed=true;
                else if (ctvConfirmExifDate.isChecked()!=n_sti.isSyncOptionConfirmNotExistsExifDate()) changed=true;
                else if (ctvCreateDircetory.isChecked()!=n_sti.isArchiveCreateDirectory()) changed=true;
                else if (!et_file_template.getText().toString().equals(n_sti.getArchiveRenameFileTemplate())) changed=true;
                else if (!et_dir_template.getText().toString().equals(n_sti.getArchiveCreateDirectoryTemplate())) changed=true;
            }
        }
        if (changed) CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_ok, true);
        else CommonDialog.setViewEnabled(getActivity(), btn_sync_folder_ok, false);

    }

    private String getArchiveSuffixOptionFromSpinner(Spinner spinner) {
        String result="4";
        String sel=spinner.getSelectedItem().toString();

        if (sel.equals(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_0))) result="0";
        else if (sel.equals(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_2))) result="2";
        else if (sel.equals(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_3))) result="3";
        else if (sel.equals(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_4))) result="4";
        else if (sel.equals(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_5))) result="5";
        else if (sel.equals(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_6))) result="6";

        return result;
    }

    private String getSyncTaskArchiveTemplateNewName(boolean create_directory_option, int suffix_option, String file_template,
                                                     String dir_template, String target_dir, SyncTaskItem n_sti) {

        String result="";

        String year="", month="", day="", hours="", minutes="", seconds="";
        String date_time= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(System.currentTimeMillis());
        year=date_time.substring(0,4);
        month=date_time.substring(5,7);
        day=date_time.substring(8,10);
        hours=date_time.substring(11,13);
        minutes=date_time.substring(14,16);
        seconds=date_time.substring(17,19);

        String suffix="";
        if (suffix_option==1) suffix="_01";
        else if (suffix_option==2) suffix="_001";
        else if (suffix_option==3) suffix="_0001";
        else if (suffix_option==4) suffix="_00001";
        else if (suffix_option==5) suffix="_000001";

        String new_name=file_template.replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_DATE, year+"-"+month+"-"+day).
                replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_TIME, hours+"-"+minutes+"-"+seconds).
                replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_YYYYMMDD, year+month+day).
                replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_HHMMSS, hours+minutes+seconds)
                .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_ORIGINAL_NAME, "DSC_0001")+suffix+".jpg";
        String temp_dir="";
        if (create_directory_option) {
            temp_dir=dir_template.replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_YEAR, year)
                    .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_MONTH, month)
                    .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_DAY, day)+"/";
        }
        if (target_dir.equals("")) result="/"+temp_dir+new_name;
        else {
            String tgt_dir_temp=SyncThread.replaceKeywordValue(target_dir, System.currentTimeMillis());
            result="/"+tgt_dir_temp+"/"+temp_dir+new_name;
        }

        return result;

    }

    private void setSpinnerSyncTaskArchiveSuffixSeq(Spinner spinner, String cv) {
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_prompt_title));
        spinner.setAdapter(adapter);
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_0));
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_2));
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_3));
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_4));
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_5));
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_suffix_seq_digit_6));

        int sel=SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_DEFAULT;
        if (cv.equals(String.valueOf(SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_NOT_USED))) sel=0;
        else if (cv.equals(String.valueOf(SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_2_DIGIT))) sel=1;
        else if (cv.equals(String.valueOf(SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_3_DIGIT))) sel=2;
        else if (cv.equals(String.valueOf(SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_4_DIGIT))) sel=3;
        else if (cv.equals(String.valueOf(SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_5_DIGIT))) sel=4;
        else if (cv.equals(String.valueOf(SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_6_DIGIT))) sel=5;
        spinner.setSelection(sel);

    }

    private void setSpinnerSyncTaskPictureRetainPeriod(Spinner spinner, int cv) {
        CommonUtilities.setSpinnerBackground(mContext, spinner, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setPrompt(mContext.getString(R.string.msgs_sync_folder_archive_period_prompt_title));
        spinner.setAdapter(adapter);
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_period_0_days));
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_period_7_days));
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_period_30_days));
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_period_60_days));
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_period_90_days));
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_period_180_days));
        adapter.add(mContext.getString(R.string.msgs_sync_folder_archive_period_1_years));
        spinner.setSelection(cv);
    }

    private SyncTaskItem buildSyncTaskListItem(Dialog dialog, SyncTaskItem base_stli) {
        final EditText et_sync_main_task_name = (EditText) dialog.findViewById(R.id.edit_sync_task_task_name);
        final CheckedTextView ctv_auto = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_ctv_auto);

        final Spinner spinnerSyncType = (Spinner) dialog.findViewById(R.id.edit_sync_task_sync_type);
        final Spinner spinnerSyncWifiStatus = (Spinner) dialog.findViewById(R.id.edit_sync_task_option_spinner_wifi_status);
//        final Button edit_wifi_ap_list = (Button) dialog.findViewById(R.id.edit_sync_task_option_btn_edit_ap_white_list);
        final CheckedTextView ctv_task_skip_if_ssid_invalid = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ap_list_task_skip_if_ssid_invalid);

        final CheckedTextView ctv_task_sync_when_cahrging = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_start_when_charging);
        final CheckedTextView ctv_never_overwrite_target_file_newer_than_the_master_file = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_never_overwrite_target_file_if_it_is_newer_than_the_master_file);
        final CheckedTextView ctv_ignore_dst_difference = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_ignore_dst_difference);
        final Spinner spinnerSyncDstOffsetValue = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_offset_daylight_saving_time_value);

        final CheckedTextView ctv_ignore_unusable_character_used_directory_file_name = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ignore_unusable_character_used_directory_file_name);
        final CheckedTextView ctv_do_not_use_rename_when_smb_file_write = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_do_mot_use_rename_when_smb_file_write);

        final Button swap_master_target = (Button) dialog.findViewById(R.id.edit_sync_task_change_master_and_target_btn);
        final Button master_folder_info = (Button) dialog.findViewById(R.id.edit_sync_task_master_folder_info_btn);
        final Button target_folder_info = (Button) dialog.findViewById(R.id.edit_sync_task_target_folder_info_btn);

        final Button dir_filter_btn = (Button) dialog.findViewById(R.id.sync_filter_edit_dir_filter_btn);
        final Button file_filter_btn = (Button) dialog.findViewById(R.id.sync_filter_edit_file_filter_btn);

        final CheckedTextView ctvSyncFileTypeSpecific = (CheckedTextView) dialog.findViewById(R.id.sync_filter_file_type_specific);

        final CheckedTextView ctvSyncFileTypeAudio = (CheckedTextView) dialog.findViewById(R.id.sync_filter_file_type_audio);
        final CheckedTextView ctvSyncFileTypeImage = (CheckedTextView) dialog.findViewById(R.id.sync_filter_file_type_image);
        final CheckedTextView ctvSyncFileTypeVideo = (CheckedTextView) dialog.findViewById(R.id.sync_filter_file_type_video);
        final CheckedTextView ctvSyncSpecificSubDir = (CheckedTextView) dialog.findViewById(R.id.sync_filter_sub_directory_specific);
        final CheckedTextView ctvProcessRootDirFile = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_master_root_dir_file);
        final CheckedTextView ctvSyncSubDir = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_sub_dir);
        final CheckedTextView ctvSyncEmptyDir = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_empty_directory);
        final CheckedTextView ctvSyncHiddenDir = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_directory);
        final CheckedTextView ctvSyncHiddenFile = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_file);
        final CheckedTextView ctvProcessOverride = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_process_override_delete_file);
        final CheckedTextView ctvConfirmOverride = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_confirm_override_delete_file);
        final CheckedTextView ctvDeleteFirst = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_delete_first_when_mirror);
        final CheckedTextView ctvEnsureTargetExactMirror = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_ensure_target_is_exact_mirror);
        final CheckedTextView ctUseExtendedDirectoryFilter1 = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_extended_filter1);
        final CheckedTextView ctUseDirectoryFilterV2 = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_directory_filter_v2);
        final CheckedTextView ctvShowSpecialOption = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_show_special_option);
        final CheckedTextView ctvDoNotResetFileLasyMod = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_do_mot_reset_file_last_mod_time);
        final CheckedTextView ctvUseSmbsyncLastMod = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_use_smbsync_last_mod_time);
        final CheckedTextView ctvDeterminChangedFileSizeGtTarget = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_file_size_greater_than_target);
        final CheckedTextView ctvRetry = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_retry_if_error_occured);
        final CheckedTextView ctvSyncUseRemoteSmallIoArea = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_remote_small_io_area);
        final CheckedTextView ctvTestMode = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_test_mode);
        final CheckedTextView ctvDiffUseFileSize = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_file_size);
        final CheckedTextView ctDeterminChangedFileByTime = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_last_mod_time);

        final CheckedTextView ctv_sync_allow_global_ip_addr = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_sync_allow_global_ip_address);

        final CheckedTextView ctv_sync_remove_master_if_empty = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_remove_directory_if_empty_when_move);

        final Spinner spinnerSyncDiffTimeValue = (Spinner) dialog.findViewById(R.id.edit_sync_task_option_spinner_diff_file_determin_time_value);
        final Button btn_ok = (Button) dialog.findViewById(R.id.edit_profile_sync_dlg_btn_ok);

        SyncTaskItem nstli = base_stli.clone();

        nstli.setSyncTaskAuto(ctv_auto.isChecked());
        nstli.setSyncTaskName(et_sync_main_task_name.getText().toString());

        setSyncTaskTypeFromSpinnere(spinnerSyncType, nstli);

        nstli.setSyncProcessRootDirFile(ctvProcessRootDirFile.isChecked());
        nstli.setSyncOptionSyncSubDirectory(ctvSyncSubDir.isChecked());
        nstli.setSyncOptionSyncEmptyDirectory(ctvSyncEmptyDir.isChecked());
        nstli.setSyncOptionSyncHiddenDirectory(ctvSyncHiddenDir.isChecked());
        nstli.setSyncOptionSyncHiddenFile(ctvSyncHiddenFile.isChecked());

        nstli.setSyncOverrideCopyMoveFile(ctvProcessOverride.isChecked());

        nstli.setSyncConfirmOverrideOrDelete(ctvConfirmOverride.isChecked());

        nstli.setSyncOptionDeleteFirstWhenMirror(ctvDeleteFirst.isChecked());
        nstli.setSyncOptionEnsureTargetIsExactMirror(ctvEnsureTargetExactMirror.isChecked());

        nstli.setSyncOptionUseExtendedDirectoryFilter1(ctUseExtendedDirectoryFilter1.isChecked());
        nstli.setsyncOptionUseDirectoryFilterV2(ctUseDirectoryFilterV2.isChecked());

        String wifi_sel = Integer.toString(spinnerSyncWifiStatus.getSelectedItemPosition());

        // If "Specific AP" is set from previous versions, set to new default end remove status error
        if (wifi_sel.equals("2")) wifi_sel=SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_PRIVATE_ADDR;
        else if (wifi_sel.equals("3")) wifi_sel=SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_ADDR;

        nstli.setSyncOptionWifiStatusOption(wifi_sel);
        nstli.setSyncOptionTaskSkipIfConnectAnotherWifiSsid(ctv_task_skip_if_ssid_invalid.isChecked());
        nstli.setSyncOptionSyncAllowGlobalIpAddress(ctv_sync_allow_global_ip_addr.isChecked());

        nstli.setSyncOptionSyncWhenCharging(ctv_task_sync_when_cahrging.isChecked());

        nstli.setSyncDoNotResetFileLastModified(ctvDoNotResetFileLasyMod.isChecked());
        nstli.setSyncDetectLastModidiedBySmbsync(ctvUseSmbsyncLastMod.isChecked());
        if (ctvRetry.isChecked()) nstli.setSyncOptionRetryCount("3");
        else nstli.setSyncOptionRetryCount("0");
        nstli.setSyncOptionUseSmallIoBuffer(ctvSyncUseRemoteSmallIoArea.isChecked());
        nstli.setSyncTestMode(ctvTestMode.isChecked());
        nstli.setSyncOptionDifferentFileBySize(ctvDiffUseFileSize.isChecked());
        nstli.setSyncDifferentFileSizeGreaterThanTagetFile(ctvDeterminChangedFileSizeGtTarget.isChecked());
        nstli.setSyncOptionDifferentFileByTime(ctDeterminChangedFileByTime.isChecked());

        nstli.setSyncOptionIgnoreDstDifference(ctv_ignore_dst_difference.isChecked());
        try {
            String dst_offset=(String)spinnerSyncDstOffsetValue.getSelectedItem();
            nstli.setSyncOptionOffsetOfDst(Integer.valueOf(dst_offset));
        } catch(Exception e) {}

        nstli.setSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile(ctv_never_overwrite_target_file_newer_than_the_master_file.isChecked());

        nstli.setSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters(ctv_ignore_unusable_character_used_directory_file_name.isChecked());

        nstli.setSyncOptionDoNotUseRenameWhenSmbFileWrite(ctv_do_not_use_rename_when_smb_file_write.isChecked());

        String diff_val = spinnerSyncDiffTimeValue.getSelectedItem().toString();
        nstli.setSyncOptionDifferentFileAllowableTime(Integer.valueOf(diff_val));
        if (!ctvSyncFileTypeSpecific.isChecked()) {
            nstli.getFileFilter().clear();
            nstli.setSyncFileTypeAudio(false);
            nstli.setSyncFileTypeImage(false);
            nstli.setSyncFileTypeVideo(false);
        } else {
            nstli.setSyncFileTypeAudio(ctvSyncFileTypeAudio.isChecked());
            nstli.setSyncFileTypeImage(ctvSyncFileTypeImage.isChecked());
            nstli.setSyncFileTypeVideo(ctvSyncFileTypeVideo.isChecked());
        }

        if (!ctvSyncSpecificSubDir.isChecked()) {
            nstli.getDirFilter().clear();
        }

        nstli.setSyncOptionMoveOnlyRemoveMasterDirectoryIfEmpty(ctv_sync_remove_master_if_empty.isChecked());

        return nstli;
    }

    static public void showFieldHelp(Activity a, GlobalParameters gp, String title, String help_msg) {
        Dialog dialog = new Dialog(a, gp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.help_view);
        LinearLayout ll_view = (LinearLayout) dialog.findViewById(R.id.help_view_title_view);
        ll_view.setBackgroundColor(gp.themeColorList.title_background_color);

        TextView dlg_tv = (TextView) dialog.findViewById(R.id.help_view_title_text);
//        dlg_tv.setBackgroundColor(mGp.themeColorList.title_background_color);
        dlg_tv.setTextColor(gp.themeColorList.title_text_color);
//        dlg_tv.setTextSize(32);

        WebView dlg_wb = (WebView) dialog.findViewById(R.id.help_view_help);

        dlg_wb.loadUrl("file:///android_asset/" + help_msg);
        dlg_wb.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        dlg_wb.getSettings().setBuiltInZoomControls(true);
        dlg_wb.setInitialScale(0);

        dlg_tv.setText(title);

//        CommonDialog.setDlgBoxSizeLimit(dialog, false);

        dialog.show();
    }

    private void setTwoWaySyncConflictRuleFromSpinnere(Spinner spinner, SyncTaskItem n_stli) {
        String so = mContext.getString(R.string.msgs_profile_twoway_sync_conflict_copy_rurle_copy_newer);
        if (spinner.getSelectedItemPosition()<spinner.getAdapter().getCount()) so=spinner.getSelectedItem().toString();
        if (so.equals(mContext.getString(R.string.msgs_profile_twoway_sync_conflict_copy_rurle_ask_user)))
            n_stli.setSyncTwoWayConflictFileRule(SyncTaskItem.SYNC_TASK_TWO_WAY_OPTION_ASK_USER);
        else if (so.equals(mContext.getString(R.string.msgs_profile_twoway_sync_conflict_copy_rurle_copy_newer)))
            n_stli.setSyncTwoWayConflictFileRule(SyncTaskItem.SYNC_TASK_TWO_WAY_OPTION_COPY_NEWER);
        else if (so.equals(mContext.getString(R.string.msgs_profile_twoway_sync_conflict_copy_rurle_copy_older)))
            n_stli.setSyncTwoWayConflictFileRule(SyncTaskItem.SYNC_TASK_TWO_WAY_OPTION_COPY_OLDER);
        else if (so.equals(mContext.getString(R.string.msgs_profile_twoway_sync_conflict_copy_rurle_skip_sync_file)))
            n_stli.setSyncTwoWayConflictFileRule(SyncTaskItem.SYNC_TASK_TWO_WAY_OPTION_SKIP_SYNC_FILE);
    }

    private void setSyncTaskTypeFromSpinnere(Spinner spinner, SyncTaskItem n_stli) {
        String so = null;

        final CheckedTextView ctvEnsureTargetExactMirror = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_ensure_target_is_exact_mirror);
//        if (ctvEnsureTargetExactMirror.isChecked()) so = mContext.getString(R.string.msgs_main_sync_profile_dlg_mirror_image);
//        else so = mContext.getString(R.string.msgs_main_sync_profile_dlg_mirror);
        so = mContext.getString(R.string.msgs_main_sync_profile_dlg_mirror);

        if (spinner.getSelectedItemPosition()<spinner.getAdapter().getCount()) so=spinner.getSelectedItem().toString();//Mirror adapter.getCount() = 0 (Spinner position 0)
        if (so.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_mirror)))// || so.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_mirror_image)))
            n_stli.setSyncTaskType(SyncTaskItem.SYNC_TASK_TYPE_MIRROR);
        else if (so.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_copy)))
            n_stli.setSyncTaskType(SyncTaskItem.SYNC_TASK_TYPE_COPY);
        else if (so.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_move)))
            n_stli.setSyncTaskType(SyncTaskItem.SYNC_TASK_TYPE_MOVE);
        else if (so.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync)))
            n_stli.setSyncTaskType(SyncTaskItem.SYNC_TASK_TYPE_SYNC);
        else if (so.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_archive)))
            n_stli.setSyncTaskType(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE);
    }

    private void setSyncTaskFieldHelpListener(Dialog dialog, SyncTaskItem sti) {
        final ImageButton help_sync_option = (ImageButton) dialog.findViewById(R.id.edit_profile_sync_help);
//        help_sync_option.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
//		final ImageButton help_folder_dir = (ImageButton)dialog.findViewById(R.id.edit_sync_folder_dlg_directory_help);
//		final ImageButton help_folder_share = (ImageButton)dialog.findViewById(R.id.edit_sync_folder_dlg_share_help);
//		final ImageButton help_folder_logon = (ImageButton)dialog.findViewById(R.id.edit_sync_folder_dlg_logon_help);
        help_sync_option.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFieldHelp(getActivity(), mGp, mContext.getString(R.string.msgs_help_sync_task_title),
                        mContext.getString(R.string.msgs_help_sync_task_file));
            }
        });
    }

    private void setSyncFolderFieldHelpListener(Dialog dialog, final String f_type) {
        final ImageButton help_sync_folder = (ImageButton) dialog.findViewById(R.id.edit_sync_folder_dlg_help);
//        help_sync_folder.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
//		Log.v("","f_type="+f_type);
        help_sync_folder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (f_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
                    showFieldHelp(getActivity(), mGp, mContext.getString(R.string.msgs_help_sync_folder_internal_title),
                            mContext.getString(R.string.msgs_help_sync_folder_internal_file));
                } else if (f_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                    showFieldHelp(getActivity(), mGp, mContext.getString(R.string.msgs_help_sync_folder_sdcard_title),
                            mContext.getString(R.string.msgs_help_sync_folder_sdcard_file));
                } else if (f_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
                    showFieldHelp(getActivity(), mGp, mContext.getString(R.string.msgs_help_sync_folder_usb_title),
                            mContext.getString(R.string.msgs_help_sync_folder_usb_file));
                } else if (f_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                    showFieldHelp(getActivity(), mGp, mContext.getString(R.string.msgs_help_sync_folder_smb_title),
                            mContext.getString(R.string.msgs_help_sync_folder_smb_file));
                } else if (f_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                    showFieldHelp(getActivity(), mGp, mContext.getString(R.string.msgs_help_sync_folder_zip_title),
                            mContext.getString(R.string.msgs_help_sync_folder_zip_file));
                }
            }
        });
    }

    private void checkSyncTaskOkButtonEnabled(Dialog dialog, String type, SyncTaskItem n_sti, TextView dlg_msg) {
//        Thread.dumpStack();
        final LinearLayout ll_file_filter = (LinearLayout) mDialog.findViewById(R.id.sync_filter_file_detail_view);

//        final CheckedTextView ctvSyncEmptyDir = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_empty_directory);
//        final CheckedTextView ctvSyncHiddenDir = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_directory);
//        final CheckedTextView ctvSyncHiddenFile = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_file);

        final LinearLayout ll_ctvProcessOverride = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_ll_process_override_delete_file);
//        final LinearLayout ll_ctvConfirmOverride = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_ll_confirm_override_delete_file);

//        final CheckedTextView ctUseExtendedDirectoryFilter1 = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_extended_filter1);
//        final CheckedTextView ctvShowSpecialOption = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_show_special_option);
        final LinearLayout ll_ctvDoNotResetFileLastMod = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_ll_do_mot_reset_file_last_mod_time);
//        final CheckedTextView ctvDoNotResetRemoteFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_do_mot_reset_file_last_mod_time);

        final LinearLayout ll_ctvUseSmbsyncLastMod = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_use_smbsync_last_mod_time_view);
        final CheckedTextView ctvUseSmbsyncLastMod = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_use_smbsync_last_mod_time);
//        final CheckedTextView ctv_ignore_dst_difference = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_ignore_dst_difference);
//        final CheckedTextView ctvRetry = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_retry_if_error_occured);
//        final CheckedTextView ctvSyncUseRemoteSmallIoArea = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_remote_small_io_area);
//        final CheckedTextView ctvTestMode = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_test_mode);

        final LinearLayout ll_ctvDiffUseFileSize = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_ll_sync_diff_use_file_size);
        final LinearLayout ll_ctDeterminChangedFileByTime = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_sync_diff_use_last_mod_time_view);
        final CheckedTextView ctDeterminChangedFileByTime = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_last_mod_time);
        final LinearLayout ll_DeterminChangedFileByTime_dependant_view=(LinearLayout)mDialog.findViewById(R.id.edit_sync_task_option_sync_diff_use_last_mod_time_dependant_view);

        final LinearLayout ll_sync_remove_master_if_empty = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_ll_remove_directory_if_empty_when_move_view);

        final CheckedTextView ctUseDirectoryFilterV2 = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_directory_filter_v2);

        final CheckedTextView ctvDeterminChangedFileSizeGtTarget = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_file_size_greater_than_target);
        final CheckedTextView ctvDiffUseFileSize = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_file_size);

        final CheckedTextView ctv_sync_remove_master_if_empty = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_remove_directory_if_empty_when_move);
        final CheckedTextView ctvDeleteFirst = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_delete_first_when_mirror);
        final LinearLayout ll_sync_delete_first_when_mirror=(LinearLayout)mDialog.findViewById(R.id.edit_sync_task_option_ll_sync_delete_first_when_mirror);

        final CheckedTextView ctvEnsureTargetExactMirror = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_ensure_target_is_exact_mirror);
        final LinearLayout ll_sync_ensure_target_is_exact_mirror=(LinearLayout)mDialog.findViewById(R.id.edit_sync_task_option_ll_sync_ensure_target_is_exact_mirror);

        final EditText et_sync_main_task_name = (EditText) mDialog.findViewById(R.id.edit_sync_task_task_name);

        final Button swap_master_target = (Button) mDialog.findViewById(R.id.edit_sync_task_change_master_and_target_btn);

        final Button btn_ok = (Button) dialog.findViewById(R.id.edit_profile_sync_dlg_btn_ok);

        boolean error_detected = false;
        swap_master_target.setVisibility(Button.VISIBLE);

        if (n_sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE)) {
            ll_sync_remove_master_if_empty.setVisibility(CheckedTextView.VISIBLE);
        } else {
            ll_sync_remove_master_if_empty.setVisibility(CheckedTextView.GONE);
            ctv_sync_remove_master_if_empty.setChecked(false);
        }

        if (n_sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) ll_sync_delete_first_when_mirror.setVisibility(CheckedTextView.VISIBLE);
        else ll_sync_delete_first_when_mirror.setVisibility(CheckedTextView.GONE);

        if (n_sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR)) ll_sync_ensure_target_is_exact_mirror.setVisibility(CheckedTextView.VISIBLE);
        else ll_sync_ensure_target_is_exact_mirror.setVisibility(CheckedTextView.GONE);

        if (!n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            ll_ctvUseSmbsyncLastMod.setVisibility(LinearLayout.GONE);
            ctvUseSmbsyncLastMod.setChecked(false);
        } else ll_ctvUseSmbsyncLastMod.setVisibility(LinearLayout.VISIBLE);

        if (n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) ll_ctvDoNotResetFileLastMod.setVisibility(CheckedTextView.GONE);

        if (n_sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE)) {
            ll_file_filter.setVisibility(LinearLayout.GONE);

            ll_ctvProcessOverride.setVisibility(CheckedTextView.GONE);
//            ll_ctvConfirmOverride.setVisibility(CheckedTextView.GONE);

            ll_ctvDoNotResetFileLastMod.setVisibility(CheckedTextView.GONE);
            ll_ctvDiffUseFileSize.setVisibility(CheckedTextView.GONE);

            ll_ctvUseSmbsyncLastMod.setVisibility(CheckedTextView.GONE);

            ll_ctDeterminChangedFileByTime.setVisibility(CheckedTextView.GONE);
            ll_DeterminChangedFileByTime_dependant_view.setVisibility(CheckedTextView.GONE);
        } else {
            ll_file_filter.setVisibility(LinearLayout.VISIBLE);

            ll_ctvProcessOverride.setVisibility(CheckedTextView.VISIBLE);
//            ll_ctvConfirmOverride.setVisibility(CheckedTextView.VISIBLE);

            if (!n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) ll_ctvDoNotResetFileLastMod.setVisibility(CheckedTextView.VISIBLE);

            ll_ctvDiffUseFileSize.setVisibility(CheckedTextView.VISIBLE);

            if (ctvDiffUseFileSize.isChecked() && ctvDeterminChangedFileSizeGtTarget.isChecked()) {
                ll_ctvUseSmbsyncLastMod.setVisibility(LinearLayout.GONE);
                ll_ctDeterminChangedFileByTime.setVisibility(LinearLayout.GONE);
                ctDeterminChangedFileByTime.setChecked(false); // mandatory if we want that this option forces a compare by size only mode, no checks inside code to bypass time diff if sizes are the same
                ll_DeterminChangedFileByTime_dependant_view.setVisibility(LinearLayout.GONE);
            } else {
                ll_ctDeterminChangedFileByTime.setVisibility(LinearLayout.VISIBLE);
                if (ctDeterminChangedFileByTime.isChecked()) ll_DeterminChangedFileByTime_dependant_view.setVisibility(LinearLayout.VISIBLE);
                else ll_DeterminChangedFileByTime_dependant_view.setVisibility(LinearLayout.GONE);
            }

            if (n_sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_SYNC)) {
                swap_master_target.setVisibility(Button.GONE);
            }
        }

        //type=="EDIT": we cannot modify the task name -> check duplicates in the adapter (error if at least 2 entries with same name are present)
        //type=="COPY" or "ADD": we can edit the task name, check duplicate in the adapter (error if the entered text "et_sync_main_task_name" has at least one occurence)
        String t_name_msg = "";
        if (type.equals("EDIT")) t_name_msg= mTaskUtil.isValidSyncTaskName(mContext, mGp.syncTaskList, n_sti.getSyncTaskName(), true, false);
        else t_name_msg= mTaskUtil.isValidSyncTaskName(mContext, mGp.syncTaskList, n_sti.getSyncTaskName(), false, false);
        if (t_name_msg.equals("")) {
            String e_msg = checkMasterTargetCombination(dialog, n_sti);
            if (!e_msg.equals("")) {
                CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
                setDialogMsg(dlg_msg, e_msg);
            } else {
                final Spinner spinnerSyncWifiStatus = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_wifi_status);
//                if (spinnerSyncWifiStatus.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_specific_ap))) {
//                    if (n_sti.getSyncOptionWifiConnectedAccessPointWhiteList().size() == 0) {
//                        CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
//                        setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_ap_not_specified));
//                        error_detected = true;
//                    }
//                } else
                if (spinnerSyncWifiStatus.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_specific_address))) {
                    if (n_sti.getSyncOptionWifiConnectedAddressWhiteList().size() == 0) {
                        CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
                        setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_address_not_specified));
                        error_detected = true;
                    }
                } else {
                    n_sti.setSyncTaskWifiOptionError(false);//remove wifi option status error
                }

                if (!error_detected) {
                    String filter_msg = "";
                    filter_msg = checkFilter(dialog, type, n_sti);
                    if (filter_msg.equals("")) {
                        String s_msg = checkStorageStatus(dialog, type, n_sti);
                        if (s_msg.equals("")) {
                            if (n_sti.isSyncTaskError()) {
                                if (n_sti.getMasterFolderError()!=SyncTaskItem.SYNC_FOLDER_ERROR_NO_ERROR) {
                                    setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_import_failed_master_folder));
                                } else if (n_sti.getTargetFolderError()!=SyncTaskItem.SYNC_FOLDER_ERROR_NO_ERROR) {
                                    setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_import_failed_target_folder));
                                }
                            } else {
                                setDialogMsg(dlg_msg, s_msg);
                                if (isSyncTaskChanged(n_sti, mCurrentSyncTaskItem)) CommonDialog.setViewEnabled(getActivity(), btn_ok, true);
                                else CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
                            }
                        } else {
                            setDialogMsg(dlg_msg, s_msg);
                            if (isSyncTaskChanged(n_sti, mCurrentSyncTaskItem)) CommonDialog.setViewEnabled(getActivity(), btn_ok, true);
                            else CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
                        }
                    } else {
                        setDialogMsg(dlg_msg, filter_msg);
                        CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
                    }
                }
//				Log.v("","fm="+filter_msg);
            }
        } else {
            setDialogMsg(dlg_msg, t_name_msg);
            CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
        }
    }

    private boolean isSyncTaskChanged(SyncTaskItem curr_stli, SyncTaskItem org_stli) {
        SyncTaskItem new_stli = buildSyncTaskListItem(mDialog, curr_stli);
        String n_type = new_stli.getSyncTaskType();
        String c_type = mCurrentSyncTaskItem.getSyncTaskType();

        boolean result = !new_stli.isSame(org_stli);
        return result;
    }

    //EditSyncTask: display erronous file and folder filters
    //filter_entry: filter line entered by user, can be multiple filter items separated by ";": filter_item1;filter_item2;filter_item3...
    //error_filter_item: the filter_item with error
    private String checkFilter(Dialog dialog, String type, SyncTaskItem n_sti) {
        String result = "";
        final CheckedTextView ctvSyncFileTypeSpecific = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_specific);
        final CheckedTextView ctvSyncFileTypeAudio = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_audio);
        final CheckedTextView ctvSyncFileTypeImage = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_image);
        final CheckedTextView ctvSyncFileTypeVideo = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_video);
        final CheckedTextView ctvSyncSpecificSubDir = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_sub_directory_specific);

        final CheckedTextView ctUseDirectoryFilterV2 = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_directory_filter_v2);

        boolean error_detected = false;

        if (ctvSyncFileTypeSpecific.isChecked()) {
            if (ctvSyncFileTypeAudio.isChecked() || ctvSyncFileTypeImage.isChecked() || ctvSyncFileTypeVideo.isChecked()) {

            } else {
                if (n_sti.getFileFilter().size() == 0) {
                    result = mContext.getString(R.string.msgs_profile_sync_task_sync_file_type_detail_not_specified);
                    error_detected = true;
                }
            }
        }

        if (!error_detected) {
            if (ctvSyncSpecificSubDir.isChecked()) {
                if (n_sti.getDirFilter().size() == 0) {
                    result = mContext.getString(R.string.msgs_profile_sync_task_sync_sub_directory_dir_filter_not_specified);
                    error_detected = true;
                }
            }
        }

        //check dir and file filters for invalid chars
        if (!error_detected) {
            if (ctUseDirectoryFilterV2.isChecked() && n_sti.getDirFilter().size() > 0) {
                String error_filter="";
                String invalid_chars="";
                String sep="";
                for(String item:n_sti.getDirFilter()) {
                    //String filter_inc_exc=item.substring(0,1);
                    String filter_entry=item.substring(1);
                    String is_invalid_char= mTaskUtil.checkFilterInvalidCharacter(filter_entry, SMBSYNC2_PROF_FILTER_DIR);
                    if (!is_invalid_char.equals("")) {
                        error_filter+=sep + "[" + filter_entry + "]";
                        invalid_chars+=is_invalid_char + ", ";
                        sep="; ";
                        error_detected = true;
                    }
                }
                if (error_detected) result = mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_invalid_characters_error, invalid_chars, error_filter);
            }
        }
        if (!error_detected) {
            if (ctUseDirectoryFilterV2.isChecked() && n_sti.getFileFilter().size() > 0) {
                String error_filter="";
                String invalid_chars="";
                String sep="";
                for(String item:n_sti.getFileFilter()) {
                    //String filter_inc_exc=item.substring(0,1);
                    String filter_entry=item.substring(1);
                    String is_invalid_char= mTaskUtil.checkFilterInvalidCharacter(filter_entry, SMBSYNC2_PROF_FILTER_FILE);
                    if (!is_invalid_char.equals("")) {
                        error_filter+= sep+ "["+ filter_entry+ "]";
                        invalid_chars+= is_invalid_char+ ", ";
                        sep="; ";
                        error_detected = true;
                    }
                }
                if (error_detected) result = mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_invalid_characters_error, invalid_chars, error_filter);
            }
        }

        //check dir and file filters for generic wild card only path parts (* and *.* only paths)
        if (!error_detected) {
            if (ctUseDirectoryFilterV2.isChecked() && n_sti.getDirFilter().size() > 0) {
                String error_filter="";
                String invalid_chars="";
                String sep="";
                for(String item:n_sti.getDirFilter()) {
                    //String filter_inc_exc=item.substring(0,1);
                    String filter_entry=item.substring(1);
                    String is_invalid_char= mTaskUtil.checkFilterInvalidAsteriskOnlyPath(filter_entry);
                    if (!is_invalid_char.equals("")) {
                        error_filter+= sep + "[" + filter_entry + "]";
                        invalid_chars+= is_invalid_char+ ", ";
                        sep="; ";
                        error_detected = true;
                    }
                }
                if (error_detected) result = mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_invalid_asterisk_characters_error, invalid_chars, error_filter);
            }
        }
        if (!error_detected) {
            if (ctUseDirectoryFilterV2.isChecked() && n_sti.getFileFilter().size() > 0) {
                String error_filter="";
                String invalid_chars="";
                String sep="";
                for(String item:n_sti.getFileFilter()) {
                    //String filter_inc_exc=item.substring(0,1);
                    String filter_entry=item.substring(1);
                    String is_invalid_char= mTaskUtil.checkFilterInvalidAsteriskOnlyPath(filter_entry);
                    if (!is_invalid_char.equals("")) {
                        error_filter+= sep+ "[" +filter_entry+ "]";
                        invalid_chars+= is_invalid_char+ ", ";
                        sep="; ";
                        error_detected = true;
                    }
                }
                if (error_detected) result = mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_invalid_asterisk_characters_error, invalid_chars, error_filter);
            }
        }
        //file filter only: check for invalid asterisk in the path to file (* char allowed only in file name)
        if (!error_detected) {
            if (ctUseDirectoryFilterV2.isChecked() && n_sti.getFileFilter().size() > 0) {
                String error_filter="";
                String sep="";
                for(String item:n_sti.getFileFilter()) {
                    //String filter_inc_exc=item.substring(0,1);
                    String filter_entry=item.substring(1);
                    String error_filter_item= mTaskUtil.checkFileFilterHasAsteriskInPathToFile(filter_entry);
                    if (!error_filter_item.equals("")) {
                        error_filter+= sep + error_filter_item;
                        sep="; ";
                        error_detected = true;
                    }
                }
                if (error_detected) result = mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_file_filter_path_has_invalid_asterisk_error, error_filter);
            }
        }

        //check dir and file filters for duplicate entries
        if (!error_detected) {
            if (ctUseDirectoryFilterV2.isChecked() && n_sti.getDirFilter().size() > 0) {
                String error_filter="";
                String sep="";
                String[] dup_filters= mTaskUtil.getDuplicateFilterList(n_sti.getDirFilter());
                for (String filter : dup_filters) {
                    if (!filter.equals("")) {
                        error_filter+= sep + filter;
                        sep= "; ";
                        error_detected = true;
                    }
                }
                if (error_detected) result = mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_duplicate_filters_error, error_filter);
            }
        }
        if (!error_detected) {
            if (ctUseDirectoryFilterV2.isChecked() && n_sti.getFileFilter().size() > 0) {
                String error_filter="";
                String sep="";
                String[] dup_filters= mTaskUtil.getDuplicateFilterList(n_sti.getFileFilter());
                for (String filter : dup_filters) {
                    if (!filter.equals("")) {
                        error_filter+= sep + filter;
                        sep= "; ";
                        error_detected = true;
                    }
                }
                if (error_detected) result = mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_duplicate_filters_error, error_filter);
            }
        }

        //check dir filter for old whole dir prefix v1
        if (!error_detected) {
            if (ctUseDirectoryFilterV2.isChecked() && n_sti.getDirFilter().size() > 0) {
                String error_filter="";
                String error_filter_item="";
                String sep="";
                for(String item:n_sti.getDirFilter()) {
                    //String filter_inc_exc=item.substring(0,1);
                    String filter_entry=item.substring(1);
                    error_filter_item=mTaskUtil.hasWholeDirectoryFilterItemV1(filter_entry);
                    if (!error_filter_item.equals("")) {
                        error_filter+= sep + error_filter_item;
                        sep="; ";
                        error_detected = true;
                    }
                }
                if (error_detected) result = mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_old_whole_dir_prefix_error, WHOLE_DIRECTORY_FILTER_PREFIX_V1, WHOLE_DIRECTORY_FILTER_PREFIX_V2, error_filter);
            }
        }

        //check dir filters for valid leading whole dir prefix v2: allowed only if filter is exclude
        if (!error_detected) {
            if (ctUseDirectoryFilterV2.isChecked() && n_sti.getDirFilter().size() > 0) {
                String error_filter="";
                String error_filter_item="";
                String sep="";
                for(String item:n_sti.getDirFilter()) {
                    String filter_inc_exc=item.substring(0,1);
                    String filter_entry=item.substring(1);
                    if (filter_inc_exc.equals("I")) {
                        error_filter_item=mTaskUtil.hasWholeDirectoryFilterItemV2(filter_entry);
                        if (!error_filter_item.equals("")) {
                            error_filter+= sep+ error_filter_item;
                            sep="; ";
                            error_detected = true;
                        }
                    }
                }
                if (error_detected) result = mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_whole_dir_prefix_error, WHOLE_DIRECTORY_FILTER_PREFIX_V2, error_filter);
            }
        }

        //check file filters for whole dir prefix v1 and v2: not needed because whole dir prefix is always an invalid char, not allowed in file filters

        return result;
    }

    private String checkStorageStatus(Dialog dialog, String type, SyncTaskItem n_sti) {
        String emsg = "";
        if (n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                if (Build.VERSION.SDK_INT>=30) {
                    emsg = mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted);
                } else {
                    emsg = mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_please_edit_master);
                }
            }
        }
        if (emsg.equals("")) {
            if (n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                    if (Build.VERSION.SDK_INT>=30) {
                        emsg = mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted);
                    } else {
                        emsg = mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_please_edit_target);
                    }
                }
            }
        }
        return emsg;
    }

    private void setWifiApWhileListInfo(ArrayList<String> wpal, Button edit_wifi_ap_list) {
        if (wpal.size() > 0) {
            String ap_list = "", sep = "";
            for (String wapl : wpal) {
                ap_list += sep + wapl.substring(1);
                sep = ",";
            }
            edit_wifi_ap_list.setText(ap_list);
        } else {
            edit_wifi_ap_list.setText(mContext.getString(R.string.msgs_filter_list_dlg_not_specified));
        }
    }

    static public String checkDirectoryFilterForSameDirectoryAccess(Context c, SyncTaskItem sti) {
        String msg="";
        String master_dir=sti.getMasterDirectoryName().equals("")?"":sti.getMasterDirectoryName().toLowerCase();
        boolean select_found=false;
        boolean select_specified=false;
        boolean exclude_found=false;
        boolean exclude_specified=false;
        for(String item:sti.getDirFilter()) {
            String inc_exc=item.substring(0,1);
            String filter=master_dir.equals("")?item.substring(1):master_dir+"/"+item.substring(1);
            Pattern pattern=Pattern.compile("^"+ MiscUtil.convertRegExp(filter.toLowerCase())+"$");
            if (inc_exc.equals("I")) {
                select_specified=true;
                Matcher mt=pattern.matcher(sti.getTargetDirectoryName().toLowerCase());
                if (mt.find()) {
                    select_found=true;
                    break;
                }
            }
        }
        for(String item:sti.getDirFilter()) {
            String inc_exc=item.substring(0,1);
            String filter=master_dir.equals("")?item.substring(1):master_dir+"/"+item.substring(1);
            Pattern pattern=Pattern.compile("^"+ MiscUtil.convertRegExp(filter.toLowerCase())+"$");
            if (inc_exc.equals("E")) {
                exclude_specified=true;
                Matcher mt=pattern.matcher(sti.getTargetDirectoryName().toLowerCase());
                if (mt.find()) {
                    exclude_found=true;
                    break;
                }
            }
        }
        if (!select_specified && !exclude_specified) {
            msg = c.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_combination_same_dir);
        } else if (select_specified && !exclude_specified) {
            if (select_found) msg = c.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_combination_same_dir);
        } else if (!select_specified && exclude_specified) {
            if (!exclude_found) msg = c.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_combination_same_dir);
        } else if (select_specified && exclude_specified) {
            if (select_found && !exclude_found) msg = c.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_combination_same_dir);
        }
        return msg;
    }

    static public String isSameDirectoryAccess(Context c, SyncTaskItem sti, boolean directory_filter_specified) {
        String msg="";
        if (sti.getMasterDirectoryName().equals("")) {
            if (sti.getTargetDirectoryName().equals("")) {
                msg=c.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_combination_internal);
            } else {
                //Filter check required
                msg= checkDirectoryFilterForSameDirectoryAccess(c, sti);
            }
        } else {
            if (sti.getTargetDirectoryName().equals("")) {
                //Valid combination
            } else {
                if (sti.getMasterDirectoryName().toLowerCase().equals(sti.getTargetDirectoryName().toLowerCase())) {
                    msg=c.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_combination_internal);
                } else {
                    if (!sti.getTargetDirectoryName().toLowerCase().equals(sti.getMasterDirectoryName().toLowerCase())) {
                        //Valid combination
                    } else if (sti.getTargetDirectoryName().toLowerCase().startsWith(sti.getMasterDirectoryName().toLowerCase())) {
                        //Masterが上位
                        msg= checkDirectoryFilterForSameDirectoryAccess(c, sti);
                    }
                }
            }
        }
        return msg;
    }

    private String checkMasterTargetCombination(Dialog dialog, SyncTaskItem sti) {
        String result = "";
        final CheckedTextView ctvSyncSpecificSubDir = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_sub_directory_specific);
        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
                result= isSameDirectoryAccess(mContext, sti, ctvSyncSpecificSubDir.isChecked());
            } if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                if (!sti.isTargetZipUseExternalSdcard()) result= isSameDirectoryAccess(mContext, sti, ctvSyncSpecificSubDir.isChecked());
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                result= isSameDirectoryAccess(mContext, sti, ctvSyncSpecificSubDir.isChecked());
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
            if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
                result= isSameDirectoryAccess(mContext, sti, ctvSyncSpecificSubDir.isChecked());
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                if (sti.getMasterSmbAddr().equalsIgnoreCase(sti.getTargetSmbAddr()) &&
                        sti.getMasterSmbDomain().equalsIgnoreCase(sti.getTargetSmbDomain()) &&
                        sti.getMasterSmbHostName().equalsIgnoreCase(sti.getTargetSmbHostName()) &&
                        sti.getMasterSmbShareName().equalsIgnoreCase(sti.getTargetSmbShareName())) {
                    result= isSameDirectoryAccess(mContext, sti, ctvSyncSpecificSubDir.isChecked());
                }
            }
        }
        return result;
    }

    static class SyncFolderEditValue implements Externalizable, Serializable, Cloneable {
        public String folder_title = "";
        public boolean folder_master = false;
        public boolean folder_remote_use_pswd =false;
        public String folder_type = "";
        public String folder_directory = "";
        public String folder_mountpoint = "";
        public String folder_remote_user = "";
        public String folder_remote_pswd = "";
        public String folder_remote_domain = "";
        public String folder_remote_addr = "";
        public String folder_remote_host = "";
        public String folder_remote_share = "";
        public String folder_remote_port = "";
        public String folder_smb_protocol = "1";
        public boolean folder_smb_ipc_enforced=true;
        public boolean folder_smb_use_smb2_negotiation=false;
        public boolean folder_remote_use_port_number=false;
        public boolean folder_remote_use_account_name_password=false;
        public String folder_removable_uuid = "";

        public boolean folder_use_taken_date_time_for_directory_keyword=false;

        public boolean zip_file_use_sdcard = false;
        public String zip_comp_level = "";
        public String zip_enc_method = "";
        public String zip_file_name = "";
        public String zip_file_password = "";
        public String zip_file_confirm_password = "";

        public int archive_retention_period = 0;
        public String archive_append_seq_number = "";
        public String archive_file_name_template = "";
        public String archive_directory_name_template = "";
        public boolean archive_create_directory = false;
        public boolean archive_confirm_exif_date = false;
        public boolean archive_rename_when_archive = false;

        public int folder_error_code=SyncTaskItem.SYNC_FOLDER_ERROR_NO_ERROR;

        public SyncFolderEditValue(){};

        @Override
        public SyncFolderEditValue clone() {
            SyncFolderEditValue npfli = null;
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

                npfli = (SyncFolderEditValue) ois.readObject();
                ois.close();
                bais.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return npfli;
        }

        public boolean isSame(SyncFolderEditValue comp) {
            boolean result = false;
            if (folder_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                if (folder_type.equals(comp.folder_type)
                    && folder_directory.equals(comp.folder_directory)
                    && folder_use_taken_date_time_for_directory_keyword==comp.folder_use_taken_date_time_for_directory_keyword
                        ) result = true;
            } else if (folder_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
                if (folder_type.equals(comp.folder_type)
                        && folder_directory.equals(comp.folder_directory)
                        && folder_use_taken_date_time_for_directory_keyword==comp.folder_use_taken_date_time_for_directory_keyword
                        ) result = true;
            } else if (folder_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
                if (folder_type.equals(comp.folder_type) &&
                    folder_directory.equals(comp.folder_directory) &&
                    folder_mountpoint.equals(comp.folder_mountpoint)
                    && folder_use_taken_date_time_for_directory_keyword==comp.folder_use_taken_date_time_for_directory_keyword
                    ) result = true;
            } else if (folder_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                if (folder_type.equals(comp.folder_type) &&
                        folder_directory.equals(comp.folder_directory) &&
                        folder_remote_user.equals(comp.folder_remote_user) &&
                        folder_remote_pswd.equals(comp.folder_remote_pswd) &&
                        folder_remote_domain.equals(comp.folder_remote_domain) &&
                        folder_remote_addr.equals(comp.folder_remote_addr) &&
                        folder_remote_host.equals(comp.folder_remote_host) &&
                        folder_remote_share.equals(comp.folder_remote_share) &&
                        folder_remote_port.equals(comp.folder_remote_port) &&
                        folder_smb_protocol.equals(comp.folder_smb_protocol) &&
                        (folder_remote_use_pswd == comp.folder_remote_use_pswd)  &&
                        (folder_smb_ipc_enforced==comp.folder_smb_ipc_enforced) &&
                        (folder_smb_use_smb2_negotiation==comp.folder_smb_use_smb2_negotiation)
                        && folder_use_taken_date_time_for_directory_keyword==comp.folder_use_taken_date_time_for_directory_keyword
                    ) result = true;
            } else if (folder_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                if (folder_type.equals(comp.folder_type) &&
                        folder_directory.equals(comp.folder_directory) &&
                        folder_mountpoint.equals(comp.folder_mountpoint) &&
                        folder_removable_uuid.equals(comp.folder_removable_uuid) &&
                        (zip_file_use_sdcard == comp.zip_file_use_sdcard)  &&
                        zip_comp_level.equals(comp.zip_comp_level) &&
                        zip_enc_method.equals(comp.zip_enc_method) &&
                        zip_file_name.equals(comp.zip_file_name) &&
                        zip_file_password.equals(comp.zip_file_password)) result = true;
            }
            return result;
        }


        @Override
        public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
            folder_title=objectInput.readUTF();
            folder_master=objectInput.readBoolean();
            folder_type=objectInput.readUTF();
            folder_directory=objectInput.readUTF();
            folder_mountpoint=objectInput.readUTF();
            folder_remote_user=objectInput.readUTF();
            folder_remote_pswd=objectInput.readUTF();
            folder_remote_domain=objectInput.readUTF();
            folder_remote_addr=objectInput.readUTF();
            folder_remote_host=objectInput.readUTF();
            folder_remote_share=objectInput.readUTF();
            folder_remote_port=objectInput.readUTF();
            folder_smb_protocol=objectInput.readUTF();
            folder_smb_ipc_enforced=objectInput.readBoolean();
            folder_smb_use_smb2_negotiation=objectInput.readBoolean();
            folder_removable_uuid=objectInput.readUTF();

            zip_file_use_sdcard=objectInput.readBoolean();
            zip_comp_level=objectInput.readUTF();
            zip_enc_method=objectInput.readUTF();
            zip_file_name=objectInput.readUTF();
            zip_file_password=objectInput.readUTF();
        }

        @Override
        public void writeExternal(ObjectOutput objectOutput) throws IOException {
            objectOutput.writeUTF(folder_title);
            objectOutput.writeBoolean(folder_master);
            objectOutput.writeUTF(folder_type);
            objectOutput.writeUTF(folder_directory);
            objectOutput.writeUTF(folder_mountpoint);
            objectOutput.writeUTF(folder_remote_user);
            objectOutput.writeUTF(folder_remote_pswd);
            objectOutput.writeUTF(folder_remote_domain);
            objectOutput.writeUTF(folder_remote_addr);
            objectOutput.writeUTF(folder_remote_host);
            objectOutput.writeUTF(folder_remote_share);
            objectOutput.writeUTF(folder_remote_port);
            objectOutput.writeUTF(folder_smb_protocol);
            objectOutput.writeBoolean(folder_smb_ipc_enforced);
            objectOutput.writeBoolean(folder_smb_use_smb2_negotiation);
            objectOutput.writeUTF(folder_removable_uuid);

            objectOutput.writeBoolean(zip_file_use_sdcard);
            objectOutput.writeUTF(zip_comp_level);
            objectOutput.writeUTF(zip_enc_method);
            objectOutput.writeUTF(zip_file_name);
            objectOutput.writeUTF(zip_file_password);
        }
    }

    private class AdapterWifiOption extends CustomSpinnerAdapter {
        private Activity a;
        public AdapterWifiOption(Activity c, int textViewResourceId) {
            super(c, textViewResourceId);
            a=c;
        }

//        @Override
//        public boolean isEnabled(int position) {
//            return position!=2;
//        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            final TextView text=(TextView)super.getDropDownView(position, convertView, parent);
//            if (position==2) {
//                CommonDialog.setViewEnabled(a, text, false);
//            } else {
//                CommonDialog.setViewEnabled(a, text, true);
//            }
            return text;
        }
    }
}
