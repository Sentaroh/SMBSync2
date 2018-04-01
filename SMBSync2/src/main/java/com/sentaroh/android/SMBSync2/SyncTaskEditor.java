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

import static android.view.KeyEvent.KEYCODE_BACK;
import static com.sentaroh.android.SMBSync2.Constants.*;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_YEAR;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.LocalMountPoint;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.SafFileManager;
import com.sentaroh.android.Utilities.Widget.CustomSpinnerAdapter;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class SyncTaskEditor extends DialogFragment {
    private final static String SUB_APPLICATION_TAG = "SyncTask ";

    private Dialog mDialog = null;
    private boolean mTerminateRequired = true;
    private Context mContext = null;
    private SyncTaskEditor mFragment = null;
    private GlobalParameters mGp = null;
    private SyncTaskUtil mTaskUtil = null;
    private SyncUtil mUtil = null;
    private CommonDialog mCommonDlg = null;

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
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
//		if(outState.isEmpty()){
//	        outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
//	    }
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        // Ignore orientation change to keep activity from restarting
        super.onConfigurationChanged(newConfig);
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");

        reInitViewWidget();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
        View view = super.onCreateView(inflater, container, savedInstanceState);
        CommonDialog.setDlgBoxSizeLimit(mDialog, true);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (mContext == null) mContext = this.getActivity();
        mFragment = this;
        mGp = (GlobalParameters) getActivity().getApplication();
        if (mUtil == null) mUtil = new SyncUtil(mContext, "SyncTaskEditor", mGp);
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
        mCommonDlg = new CommonDialog(mContext, getActivity().getSupportFragmentManager());
        if (mTerminateRequired) {
            this.dismiss();
        }
    }

    @Override
    final public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
    }

    @Override
    final public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) mContext = this.getActivity();
        mGp = (GlobalParameters) getActivity().getApplication();
        if (mUtil == null) mUtil = new SyncUtil(mContext, "SyncTaskEditor", mGp);
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
    }

    @Override
    final public void onDetach() {
        super.onDetach();
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
    }

    @Override
    final public void onStart() {
        CommonDialog.setDlgBoxSizeLimit(mDialog, true);
        super.onStart();
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
        if (mTerminateRequired) mDialog.cancel();
    }

    @Override
    final public void onStop() {
        super.onStop();
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
    }

    @Override
    public void onDestroyView() {
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onCancel(DialogInterface di) {
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
        if (!mTerminateRequired) {
            final Button btnCancel = (Button) mDialog.findViewById(R.id.edit_profile_sync_dlg_btn_cancel);
            btnCancel.performClick();
        }
        mFragment.dismiss();
        super.onCancel(di);
    }

    @Override
    public void onDismiss(DialogInterface di) {
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
        super.onDismiss(di);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");

//    	mContext=getActivity().getApplicationContext();
        mDialog = new Dialog(getActivity(), mGp.applicationTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!mTerminateRequired) {
            initViewWidget();
        }
        return mDialog;
    }

    class SavedViewContents {
        CharSequence prof_name_et;
        int prof_name_et_spos;
        int prof_name_et_epos;
        boolean cb_active;

        public int sync_opt = -1;
        public boolean sync_process_root_dir_file, sync_conf_required, sync_use_smbsync_last_mod, sync_do_not_reset_remote_file,
                sync_retry, sync_empty_dir, sync_hidden_dir, sync_hidden_file, sync_sub_dir, sync_use_ext_dir_fileter;
        public boolean sync_UseRemoteSmallIoArea;
        public int sync_master_pos = -1, sync_target_pos = -1;

        public String sync_master_foder_info = "";
        public String sync_target_foder_info = "";

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

    }

    private SavedViewContents saveViewContents() {
        SavedViewContents sv = new SavedViewContents();

        final EditText et_sync_main_task_name = (EditText) mDialog.findViewById(R.id.edit_sync_task_task_name);
        final CheckedTextView ctv_auto = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_ctv_auto);
        final Spinner spinnerSyncOption = (Spinner) mDialog.findViewById(R.id.edit_sync_task_sync_option);

//		final Button swap_master_target = (Button)mDialog.findViewById(R.id.edit_sync_task_change_master_and_target_btn);
//		final Button master_folder_edit_btn=(Button) mDialog.findViewById(R.id.edit_sync_task_edit_master_btn);
        final Button master_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_master_folder_info_btn);
//		final Button target_folder_edit_btn=(Button) mDialog.findViewById(R.id.edit_sync_task_edit_target_btn);
        final Button target_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_target_folder_info_btn);

        final Button dir_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_dir_filter_btn);
        final Button file_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_file_filter_btn);
//		final TextView dlg_file_filter=(TextView) mDialog.findViewById(R.id.sync_filter_summary_file_filter);
//		final TextView dlg_dir_filter=(TextView) mDialog.findViewById(R.id.sync_filter_summary_dir_filter);


        final CheckedTextView ctvProcessRootDirFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_master_root_dir_file);
        final CheckedTextView ctvSyncSubDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_sub_dir);
        final CheckedTextView ctvSyncEmptyDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_empty_directory);
        final CheckedTextView ctvSyncHiddenDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_directory);
        final CheckedTextView ctvSyncHiddenFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_file);
//        final CheckedTextView ctvProcessOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_process_override_delete_file);
        final CheckedTextView ctvConfirmOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_confirm_override_delete_file);
        final CheckedTextView ctvCopyByRename = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_copy_rename);
        final Spinner spinnerSyncWifiStatus = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_wifi_status);
        final CheckedTextView ctvDoNotResetRemoteFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_do_mot_reset_remote_file_last_mod_time);
        final CheckedTextView ctvUseSmbsyncLastMod = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_use_smbsync_last_mod_time);
        final CheckedTextView ctvRetry = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_retry_if_error_occured);
        final CheckedTextView ctvSyncUseRemoteSmallIoArea = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_remote_small_io_area);
        final CheckedTextView ctvTestMode = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_test_mode);
        final CheckedTextView ctvDiffUseFileSize = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_file_size);
        final CheckedTextView ctDeterminChangedFileByTime = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_last_mod_time);

        final CheckedTextView ctUseExtendedDirectoryFilter1 = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_extended_filter1);

        final Spinner spinnerSyncDiffTimeValue = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_diff_file_determin_time_value);

        final CheckedTextView ctvShowSpecialOption = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_show_special_option);


//		final ScrollView svx=(ScrollView)mDialog.findViewById(R.id.sync_profile_dlg_scroll_view);
//		Log.v("","x="+svx.getScrollX()+", y="+svx.getScrollY());

        sv.prof_name_et = et_sync_main_task_name.getText();
        sv.prof_name_et_spos = et_sync_main_task_name.getSelectionStart();
        sv.prof_name_et_epos = et_sync_main_task_name.getSelectionEnd();
        sv.cb_active = ctv_auto.isChecked();
        sv.sync_opt = spinnerSyncOption.getSelectedItemPosition();

        sv.sync_master_foder_info = master_folder_info.getText().toString();
        sv.sync_target_foder_info = target_folder_info.getText().toString();

        sv.sync_file_filter_info = file_filter_btn.getText().toString();
        sv.sync_dir_filter_info = dir_filter_btn.getText().toString();

        sv.sync_process_root_dir_file = ctvProcessRootDirFile.isChecked();
        sv.sync_sub_dir = ctvSyncSubDir.isChecked();
        sv.sync_empty_dir = ctvSyncEmptyDir.isChecked();
        sv.sync_hidden_dir = ctvSyncHiddenDir.isChecked();
        sv.sync_hidden_file = ctvSyncHiddenFile.isChecked();
//        sv.sync_process_override=ctvProcessOverride.isChecked();
        sv.sync_conf_required = ctvConfirmOverride.isChecked();
        sv.sync_copy_by_rename = ctvCopyByRename.isChecked();

        sv.sync_wifi_option = spinnerSyncWifiStatus.getSelectedItemPosition();

        sv.sync_show_special_option = ctvShowSpecialOption.isChecked();

        sv.sync_do_not_reset_remote_file = ctvDoNotResetRemoteFile.isChecked();
        sv.sync_use_smbsync_last_mod = ctvUseSmbsyncLastMod.isChecked();
        sv.sync_retry = ctvRetry.isChecked();
        sv.sync_UseRemoteSmallIoArea = ctvSyncUseRemoteSmallIoArea.isChecked();

        sv.sync_test_mode = ctvTestMode.isChecked();
        sv.sync_diff_use_file_size = ctvDiffUseFileSize.isChecked();
        sv.sync_diff_use_last_mod = ctDeterminChangedFileByTime.isChecked();

        sv.sync_diff_last_mod_value = spinnerSyncDiffTimeValue.getSelectedItemPosition();

        sv.sync_use_ext_dir_fileter = ctUseExtendedDirectoryFilter1.isChecked();
        return sv;
    }

    private void restoreViewContents(final SavedViewContents sv) {
        final EditText et_sync_main_task_name = (EditText) mDialog.findViewById(R.id.edit_sync_task_task_name);
        final CheckedTextView ctv_auto = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_ctv_auto);
        final Spinner spinnerSyncOption = (Spinner) mDialog.findViewById(R.id.edit_sync_task_sync_option);

//		final Button swap_master_target = (Button)mDialog.findViewById(R.id.edit_sync_task_change_master_and_target_btn);
//		final Button master_folder_edit_btn=(Button) mDialog.findViewById(R.id.edit_sync_task_edit_master_btn);
        final Button master_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_master_folder_info_btn);
//		final Button target_folder_edit_btn=(Button) mDialog.findViewById(R.id.edit_sync_task_edit_target_btn);
        final Button target_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_target_folder_info_btn);

        final Button dir_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_dir_filter_btn);
        final Button file_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_file_filter_btn);
//		final TextView dlg_file_filter=(TextView) mDialog.findViewById(R.id.sync_filter_summary_file_filter);
//		final TextView dlg_dir_filter=(TextView) mDialog.findViewById(R.id.sync_filter_summary_dir_filter);


        final CheckedTextView ctvProcessRootDirFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_master_root_dir_file);
        final CheckedTextView ctvSyncSubDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_sub_dir);
        final CheckedTextView ctvSyncEmptyDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_empty_directory);
        final CheckedTextView ctvSyncHiddenDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_directory);
        final CheckedTextView ctvSyncHiddenFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_file);
        final CheckedTextView ctvProcessOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_process_override_delete_file);
        final CheckedTextView ctvConfirmOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_confirm_override_delete_file);
        final CheckedTextView ctvCopyByRename = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_copy_rename);
        final Spinner spinnerSyncWifiStatus = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_wifi_status);
        final CheckedTextView ctvDoNotResetRemoteFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_do_mot_reset_remote_file_last_mod_time);
        final CheckedTextView ctvUseSmbsyncLastMod = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_use_smbsync_last_mod_time);
        final CheckedTextView ctvRetry = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_retry_if_error_occured);
        final CheckedTextView ctvSyncUseRemoteSmallIoArea = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_remote_small_io_area);
        final CheckedTextView ctvTestMode = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_test_mode);
        final CheckedTextView ctvDiffUseFileSize = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_file_size);
        final CheckedTextView ctDeterminChangedFileByTime = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_last_mod_time);

        final CheckedTextView ctUseExtendedDirectoryFilter1 = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_extended_filter1);

        final Spinner spinnerSyncDiffTimeValue = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_diff_file_determin_time_value);

        final LinearLayout ll_special_option_view = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_special_option_view);
        final CheckedTextView ctvShowSpecialOption = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_show_special_option);

        Handler hndl1 = new Handler();
        hndl1.postDelayed(new Runnable() {
            @Override
            public void run() {
                et_sync_main_task_name.setText(sv.prof_name_et);
                ctv_auto.setChecked(sv.cb_active);
                spinnerSyncOption.setEnabled(false);
                spinnerSyncOption.setSelection(sv.sync_opt);

                master_folder_info.setText(sv.sync_master_foder_info);
                target_folder_info.setText(sv.sync_target_foder_info);

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
                ctvCopyByRename.setChecked(sv.sync_copy_by_rename);

                spinnerSyncWifiStatus.setEnabled(false);
                spinnerSyncWifiStatus.setSelection(sv.sync_wifi_option);

                ctvShowSpecialOption.setChecked(sv.sync_show_special_option);
                if (ctvShowSpecialOption.isChecked())
                    ll_special_option_view.setVisibility(LinearLayout.VISIBLE);
                else ll_special_option_view.setVisibility(LinearLayout.GONE);

                ctvDoNotResetRemoteFile.setChecked(sv.sync_do_not_reset_remote_file);
                ctvUseSmbsyncLastMod.setChecked(sv.sync_use_smbsync_last_mod);
                ctvRetry.setChecked(sv.sync_retry);
                ctvSyncUseRemoteSmallIoArea.setChecked(sv.sync_UseRemoteSmallIoArea);

                ctvTestMode.setChecked(sv.sync_test_mode);
                ctvDiffUseFileSize.setChecked(sv.sync_diff_use_file_size);
                ctDeterminChangedFileByTime.setChecked(sv.sync_diff_use_last_mod);

                spinnerSyncDiffTimeValue.setEnabled(false);
                spinnerSyncDiffTimeValue.setSelection(sv.sync_diff_last_mod_value);

                ctUseExtendedDirectoryFilter1.setChecked(sv.sync_use_ext_dir_fileter);

                Handler hndl2 = new Handler();
                hndl2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        spinnerSyncOption.setEnabled(true);
                        spinnerSyncWifiStatus.setEnabled(true);
                        spinnerSyncDiffTimeValue.setEnabled(true);
                    }
                }, 50);
            }
        }, 50);
    }

    public void reInitViewWidget() {
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");
        if (!mTerminateRequired) {
            SavedViewContents sv = null;
            sv = saveViewContents();
            initViewWidget();
            restoreViewContents(sv);
            CommonDialog.setDlgBoxSizeLimit(mDialog, true);
        }
    }

    public void initViewWidget() {
        mUtil.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered");

        editProfile(mOpType, mCurrentSyncTaskItem);
    }

    private String mOpType = "";
    private SyncTaskItem mCurrentSyncTaskItem;
    private NotifyEvent mNotifyComplete = null;

    public void showDialog(FragmentManager fm, Fragment frag,
                           final String op_type,
                           final SyncTaskItem pli,
                           SyncTaskUtil pm,
                           SyncUtil ut,
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
        String out = in_str.replaceAll(":", "").replaceAll("\\\\", "").replaceAll("\\*", "").replaceAll("\\?", "").replaceAll("\"", "").replaceAll("<", "")
                .replaceAll(">", "").replaceAll("\\|", "");
        return out;
    }

    private void setSyncFolderSmbListener(final Dialog dialog, final SyncTaskItem sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final Spinner sp_sync_folder_smb_proto = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_protocol);
        setSyncFolderSmbProtoSpinner(sti, sp_sync_folder_smb_proto, sfev.folder_smb_protocol);

        final Button btn_search_host = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_search_remote_host);
        final Button btn_sync_folder_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_smb_directory_btn);

        final EditText et_sync_folder_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_directory_name);
        et_sync_folder_dir_name.setText(sfev.folder_directory);

        final EditText et_remote_host = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_server);
        if (!sfev.folder_remote_addr.equals("")) et_remote_host.setText(sfev.folder_remote_addr);
        else et_remote_host.setText(sfev.folder_remote_host);

        //		final LinearLayout ll_sync_folder_port = (LinearLayout)dialog.findViewById(R.id.edit_sync_folder_dlg_port_option_view);
        final CheckedTextView ctv_sync_folder_use_port = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_remote_port_number);
        SyncUtil.setCheckedTextView(ctv_sync_folder_use_port);
        final EditText et_sync_folder_port = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_port);
        if (!sfev.folder_remote_port.equals("")) {
            ctv_sync_folder_use_port.setChecked(true);
            et_sync_folder_port.setEnabled(true);
            et_sync_folder_port.setText(sfev.folder_remote_port);
        } else {
            ctv_sync_folder_use_port.setChecked(false);
            et_sync_folder_port.setEnabled(false);
        }
        ctv_sync_folder_use_port.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctv_sync_folder_use_port.isChecked();
                ctv_sync_folder_use_port.setChecked(isChecked);
                et_sync_folder_port.setEnabled(isChecked);
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        final LinearLayout ll_dir_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_directory_view);

        final Button btn_dir_view_keyword_insert_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_year);
        final Button btn_dir_view_keyword_insert_month = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_month);
        final Button btn_dir_view_keyword_insert_day = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day);
        final Button btn_dir_view_keyword_insert_day_of_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day_of_year);

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

        final CheckedTextView ctv_sync_folder_use_pswd = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_user_pass);

        final EditText et_sync_folder_domain = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_domain);
        et_sync_folder_domain.setVisibility(EditText.GONE);
        final EditText et_sync_folder_user = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_user);
        final EditText et_sync_folder_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_pass);
        if (!sfev.folder_remote_user.equals("") || !sfev.folder_remote_pswd.equals("")) {
            ctv_sync_folder_use_pswd.setChecked(true);
            et_sync_folder_user.setEnabled(true);
            et_sync_folder_user.setText(sfev.folder_remote_user);
            et_sync_folder_pswd.setEnabled(true);
            et_sync_folder_pswd.setText(sfev.folder_remote_pswd);
        } else {
            ctv_sync_folder_use_pswd.setChecked(false);
            et_sync_folder_user.setEnabled(false);
            et_sync_folder_pswd.setEnabled(false);
        }
        sfev.folder_remote_use_pswd=ctv_sync_folder_use_pswd.isChecked();
        ctv_sync_folder_use_pswd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctv_sync_folder_use_pswd.isChecked();
                ctv_sync_folder_use_pswd.setChecked(isChecked);
                et_sync_folder_user.setEnabled(isChecked);
                et_sync_folder_pswd.setEnabled(isChecked);
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        final CheckedTextView ctv_show_password = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_show_password);
        ctv_show_password.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckedTextView ctv=(CheckedTextView)view;
                ctv.setChecked(!ctv.isChecked());
                if (!ctv.isChecked()) et_sync_folder_pswd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                else et_sync_folder_pswd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });

        final Button btn_sync_folder_logon = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_logon_btn);
//        btn_sync_folder_logon.setVisibility(Button.GONE);
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
                mTaskUtil.ipAddressScanButtonDlg(dialog);
            }
        });

        btn_sync_folder_logon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String user=et_sync_folder_user.getText().toString().trim().length()>0?et_sync_folder_user.getText().toString().trim():null;
                String pass=et_sync_folder_pswd.getText().toString().trim().length()>0?et_sync_folder_pswd.getText().toString().trim():null;
                if (!ctv_sync_folder_use_pswd.isChecked()) {
                    user=pass=null;
                } else {
                    if (et_sync_folder_user.getText().length()==0) user=null;
                    if (et_sync_folder_pswd.getText().length()==0) pass=null;
                }
                RemoteAuthInfo ra=new RemoteAuthInfo();
                ra.smb_domain_name=null;
                ra.smb_user_name=user;
                ra.smb_user_password=pass;
                ra.smb_smb_protocol=getSmbSelectedProtocol(sp_sync_folder_smb_proto);
                ra.smb_ipc_signing_enforced=ctv_sync_folder_smb_ipc_enforced.isChecked();
                if (JcifsUtil.isValidIpAddress(et_remote_host.getText().toString())) {
                    mTaskUtil.testSmbLogonDlg("", et_remote_host.getText().toString().trim(),
                            et_sync_folder_port.getText().toString().trim(),
                            et_sync_folder_share_name.getText().toString().trim(), ra, null);
                } else {
                    mTaskUtil.testSmbLogonDlg(et_remote_host.getText().toString().trim(), "",
                            et_sync_folder_port.getText().toString().trim(),
                            et_sync_folder_share_name.getText().toString().trim(), ra, null);
                }
            }
        });

        btn_sync_folder_list_share.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTaskUtil.invokeSelectRemoteShareDlg(dialog);
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
                    String new_name = removeInvalidCharForFileDirName(editable.toString());
                    if (editable.length() != new_name.length()) {
                        //remove invalid char
                        et_sync_folder_dir_name.setText(new_name);
                        if (new_name.length() > 0)
                            et_sync_folder_dir_name.setSelection(new_name.length());
                        mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_dir_name_has_invalid_char)
                                , "", null);

                    }
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
    }

    private void setSyncFolderInternalListener(final Dialog dialog, final SyncTaskItem sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final Button btn_sync_folder_ok = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_ok);

        final LinearLayout ll_sync_folder_mp = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_local_mount_point_selector_view);
        final Spinner sp_sync_folder_mp = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_local_mount_point_selector);
        setSyncFolderMpSpinner(sti, sp_sync_folder_mp, sfev.folder_mountpoint, !sfev.folder_master);

        final Button btn_sync_folder_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_internal_directory_btn);

        final EditText et_sync_folder_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_directory_name);
        et_sync_folder_dir_name.setText(sfev.folder_directory);

        final Button btn_sdcard_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_select_document_tree);

        final LinearLayout ll_dir_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_directory_view);

        final Button btn_dir_view_keyword_insert_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_year);
        final Button btn_dir_view_keyword_insert_month = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_month);
        final Button btn_dir_view_keyword_insert_day = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day);
        final Button btn_dir_view_keyword_insert_day_of_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day_of_year);

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
                    String new_name = removeInvalidCharForFileDirName(editable.toString());
                    if (editable.length() != new_name.length()) {
                        //remove invalid char
                        et_sync_folder_dir_name.setText(new_name);
                        if (new_name.length() > 0)
                            et_sync_folder_dir_name.setSelection(new_name.length());
                        mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_dir_name_has_invalid_char)
                                , "", null);

                    }
                }
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });
        btn_sync_folder_list_dir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sel = sp_sync_folder_type.getSelectedItem().toString();
                String url = sp_sync_folder_mp.getSelectedItem().toString();
                String p_dir = et_sync_folder_dir_name.getText().toString();

                NotifyEvent ntfy = new NotifyEvent(mContext);
                //Listen setRemoteShare response
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context arg0, Object[] arg1) {
                        String dir = (String) arg1[0];
                        if (dir.endsWith("/"))
                            et_sync_folder_dir_name.setText(dir.substring(0, dir.length() - 1));
                        else et_sync_folder_dir_name.setText(dir);
                    }

                    @Override
                    public void negativeResponse(Context arg0, Object[] arg1) {
                        setDialogMsg(dlg_msg, "");
                    }
                });
                mCommonDlg.fileSelectorDirOnlySelectWithCreateHideMP(false, url, "",
                        mContext.getString(R.string.msgs_select_local_dir), ntfy);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_YEAR);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_month, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_MONTH);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day_of_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR);

    }

    private void setSyncFolderSdcardListener(final Dialog dialog, final SyncTaskItem sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final Button btn_sync_folder_ok = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_ok);

        final Button btn_sync_folder_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_sdcard_directory_btn);

        final EditText et_sync_folder_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_directory_name);
        et_sync_folder_dir_name.setText(sfev.folder_directory);
        final Button btn_sdcard_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_select_document_tree);

        final LinearLayout ll_dir_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_directory_view);

        final Button btn_dir_view_keyword_insert_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_year);
        final Button btn_dir_view_keyword_insert_month = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_month);
        final Button btn_dir_view_keyword_insert_day = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day);
        final Button btn_dir_view_keyword_insert_day_of_year = (Button) ll_dir_view.findViewById(R.id.edit_sync_folder_keyword_insert_day_of_year);

        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_YEAR);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_month, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_MONTH);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY);
        setSyncFolderkeywordButtonListener(dialog, sfev, btn_dir_view_keyword_insert_day_of_year, et_sync_folder_dir_name, SMBSYNC2_REPLACEABLE_KEYWORD_DAY_OF_YEAR);

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
                    String new_name = removeInvalidCharForFileDirName(editable.toString());
                    if (editable.length() != new_name.length()) {
                        //remove invalid char
                        et_sync_folder_dir_name.setText(new_name);
                        if (new_name.length() > 0)
                            et_sync_folder_dir_name.setSelection(new_name.length());
                        mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_dir_name_has_invalid_char)
                                , "", null);

                    }
                }
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });
        btn_sync_folder_list_dir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String sel = sp_sync_folder_type.getSelectedItem().toString();
                String url = mGp.safMgr.getSdcardDirectory();
                String p_dir = et_sync_folder_dir_name.getText().toString();

                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context arg0, Object[] arg1) {
                        String dir = (String) arg1[0];
                        if (dir.endsWith("/"))
                            et_sync_folder_dir_name.setText(dir.substring(0, dir.length() - 1));
                        else et_sync_folder_dir_name.setText(dir);
                    }

                    @Override
                    public void negativeResponse(Context arg0, Object[] arg1) {
                        setDialogMsg(dlg_msg, "");
                    }
                });
                mCommonDlg.fileSelectorDirOnlySelectWithCreateHideMP(false, url, "",
                        mContext.getString(R.string.msgs_select_local_dir), ntfy);
                setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
            }
        });

        btn_sdcard_select_sdcard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        if (mGp.safMgr.getSdcardDirectory().equals(SafFileManager.UNKNOWN_SDCARD_DIRECTORY)) {
                            if (SafFileManager.isSdcardMountPointExisted(mContext, mGp.settingDebugLevel > 0)) {
                                dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_press_select_btn));
                                dlg_msg.setVisibility(TextView.VISIBLE);
                                btn_sdcard_select_sdcard.setEnabled(true);
                            } else {
                                dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted));
                                dlg_msg.setVisibility(TextView.VISIBLE);
                                btn_sdcard_select_sdcard.setEnabled(false);
                            }
                            btn_sync_folder_list_dir.setEnabled(false);
                        } else {
                            btn_sync_folder_list_dir.setEnabled(true);
                            dlg_msg.setVisibility(TextView.GONE);
                            dlg_msg.setText("");
                            btn_sdcard_select_sdcard.setEnabled(true);
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

    private void setSyncFolderZipListener(final Dialog dialog, final SyncTaskItem sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final Button btn_sync_folder_ok = (Button) dialog.findViewById(R.id.edit_profile_remote_btn_ok);

        final Button btn_sdcard_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_select_document_tree);

        final LinearLayout ll_zip_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_view);

        final Button btn_zip_view_keyword_insert_year = (Button) ll_zip_view.findViewById(R.id.edit_sync_folder_keyword_insert_year);
        final Button btn_zip_view_keyword_insert_month = (Button) ll_zip_view.findViewById(R.id.edit_sync_folder_keyword_insert_month);
        final Button btn_zip_view_keyword_insert_day = (Button) ll_zip_view.findViewById(R.id.edit_sync_folder_keyword_insert_day);
        final Button btn_zip_view_keyword_insert_day_of_year = (Button) ll_zip_view.findViewById(R.id.edit_sync_folder_keyword_insert_day_of_year);

        final CheckedTextView ctv_zip_file_save_sdcard = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_use_sdcard);
        SyncUtil.setCheckedTextView(ctv_zip_file_save_sdcard);
        ctv_zip_file_save_sdcard.setChecked(sfev.zip_file_use_sdcard);

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
            zip_dir=sfev.zip_file_name.substring(0,sfev.zip_file_name.lastIndexOf("/")+1);
            zip_file=sfev.zip_file_name.substring(sfev.zip_file_name.lastIndexOf("/")+1);
        } else {
            zip_dir="/";
            if (sfev.zip_file_name.length()!=0) zip_file=sfev.zip_file_name.substring(1);
        }
        if (!ctv_zip_file_save_sdcard.isChecked()) {
            tv_zip_dir.setText(zip_dir.replace(mGp.internalRootDirectory, ""));
            et_zip_file.setText(zip_file);
        } else {
            tv_zip_dir.setText(zip_dir.replace(mGp.safMgr.getExternalSdcardPath(), ""));
            et_zip_file.setText(zip_file);
        }

        final Spinner sp_comp_level = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_comp_level);
        setSyncFolderZipCompressionLevelSpinner(sp_comp_level, sfev.zip_comp_level);
        final RadioGroup rg_zip_enc_type = (RadioGroup) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rg);
        final RadioButton rb_zip_enc_none = (RadioButton) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_none);
        final RadioButton rb_zip_enc_standard = (RadioButton) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_standard);
        final RadioButton rb_zip_enc_aes128 = (RadioButton) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_aes128);
        final RadioButton rb_zip_enc_aes256 = (RadioButton) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_aes256);
        final EditText et_zip_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_password);
        final EditText et_zip_conf_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_confirm);
        final Button btn_zip_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_select_document_tree);


        if (sfev.zip_enc_method.equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_NONE)) {
            rb_zip_enc_none.setChecked(true);
            et_zip_pswd.setVisibility(EditText.GONE);
            et_zip_conf_pswd.setVisibility(EditText.GONE);
        } else {
            if (sfev.zip_enc_method.equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_STANDARD))
                rb_zip_enc_standard.setChecked(true);
            else if (sfev.zip_enc_method.equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_AES128))
                rb_zip_enc_aes128.setChecked(true);
            else if (sfev.zip_enc_method.equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_AES256))
                rb_zip_enc_aes256.setChecked(true);
            et_zip_pswd.setVisibility(EditText.VISIBLE);
            et_zip_conf_pswd.setVisibility(EditText.VISIBLE);
        }
        et_zip_pswd.setText(sfev.zip_file_password);
        et_zip_conf_pswd.setText(sfev.zip_file_password);
        rg_zip_enc_type.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rb_zip_enc_none.getId()) {
                    et_zip_pswd.setVisibility(EditText.GONE);
                    et_zip_conf_pswd.setVisibility(EditText.GONE);
                } else {
                    et_zip_pswd.setVisibility(EditText.VISIBLE);
                    et_zip_conf_pswd.setVisibility(EditText.VISIBLE);
                }
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        btn_sdcard_select_sdcard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        if (mGp.safMgr.getSdcardDirectory().equals(SafFileManager.UNKNOWN_SDCARD_DIRECTORY)) {
                            if (SafFileManager.isSdcardMountPointExisted(mContext, mGp.settingDebugLevel > 0)) {
                                dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_press_select_btn));
                                dlg_msg.setVisibility(TextView.VISIBLE);
                                btn_sdcard_select_sdcard.setEnabled(true);
                            } else {
                                dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted));
                                dlg_msg.setVisibility(TextView.VISIBLE);
                                btn_sdcard_select_sdcard.setEnabled(false);
                            }
                            btn_zip_filelist.setEnabled(false);
                        } else {
                            btn_zip_filelist.setEnabled(true);
                            dlg_msg.setVisibility(TextView.GONE);
                            dlg_msg.setText("");
                            btn_sdcard_select_sdcard.setEnabled(true);
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
        btn_zip_select_sdcard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_sdcard_select_sdcard.performClick();
            }
        });
        btn_zip_filelist.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        String zip_path = (String) o[0];
                        String zip_dir="", zip_file="";
                        if (zip_path.lastIndexOf("/")>1) {
                            //Directory付
                            zip_dir=zip_path.substring(0,zip_path.lastIndexOf("/")+1);
                            zip_file=zip_path.substring(zip_path.lastIndexOf("/")+1);
                        } else {
                            zip_dir="/";
                            if (zip_path.length()!=0) zip_file=zip_path.substring(1);
                        }
                        if (!ctv_zip_file_save_sdcard.isChecked()) {
                            tv_zip_dir.setText(zip_dir.replace(mGp.internalRootDirectory, ""));
                            et_zip_file.setText(zip_file);
                        } else {
                            tv_zip_dir.setText(zip_dir.replace(mGp.safMgr.getExternalSdcardPath(), ""));
                            et_zip_file.setText(zip_file);
                        }
                        setSyncFolderOkButtonEnabledIfFolderChanged(dialog, sfev);
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                String title = mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_select_file_title);
                if (!ctv_zip_file_save_sdcard.isChecked())
                    mCommonDlg.fileSelectorFileOnlySelectWithCreateHideMP(true, mGp.internalRootDirectory, "", "", title, ntfy);
                else
                    mCommonDlg.fileSelectorFileOnlySelectWithCreateHideMP(true, mGp.safMgr.getExternalSdcardPath(), "", "", title, ntfy);
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
                    String new_name = removeInvalidCharForFileDirName(s.toString());
                    if (s.length() != new_name.length()) {
                        //remove invalid char
                        et_zip_file.setText(new_name);
                        if (new_name.length() > 0) et_zip_file.setSelection(new_name.length());
                        mCommonDlg.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_dlg_file_name_has_invalid_char)
                                , "", null);
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
                if (s.length() > 0) et_zip_conf_pswd.setEnabled(true);
                else et_zip_conf_pswd.setEnabled(false);
                checkSyncFolderValidation(dialog, sfev);
            }
        });

        if (et_zip_pswd.getText().length() > 0) et_zip_conf_pswd.setEnabled(true);
        else et_zip_conf_pswd.setEnabled(false);

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


    private void editSyncFolder(SyncTaskItem sti, final SyncFolderEditValue sfev, final NotifyEvent ntfy) {
        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(this.getActivity(), mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.edit_sync_folder_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        TextView dlg_title = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_title);
        dlg_title.setTextColor(mGp.themeColorList.text_color_dialog_title);
        dlg_title.setText(sfev.folder_title);

        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);

        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        setSyncFolderTypeSpinner(sti, sp_sync_folder_type, sfev.folder_type, sfev.folder_master);

        setSyncFolderSmbListener(dialog, sti, sfev, ntfy);
        setSyncFolderInternalListener(dialog, sti, sfev, ntfy);
        setSyncFolderSdcardListener(dialog, sti, sfev, ntfy);
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

        btn_sync_folder_ok.setEnabled(false);
        setSyncFolderOkButtonEnabled(btn_sync_folder_ok, false);
        btn_sync_folder_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SyncFolderEditValue nsfev = buildSyncFolderEditValue(dialog, sfev);
                ntfy.notifyToListener(true, new Object[]{nsfev});
                dialog.dismiss();
            }
        });

        btn_sync_folder_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_sync_folder_ok.isEnabled()) {
                    NotifyEvent ntfy = new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            dialog.dismiss();
                        }
                        @Override
                        public void negativeResponse(Context context, Object[] objects) {}
                    });
                    mCommonDlg.showCommonDialog(true, "W",
                            mContext.getString(R.string.msgs_schedule_confirm_title_nosave),
                            mContext.getString(R.string.msgs_profile_sync_folder_dlg_confirm_msg_nosave), ntfy);
                } else {
                    ntfy.notifyToListener(false, null);
                    dialog.dismiss();
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
        final CheckedTextView ctv_sync_folder_use_port = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_remote_port_number);
        final EditText et_sync_folder_port = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_port);
        final CheckedTextView ctv_sync_folder_use_pswd = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_user_pass);
        final EditText et_sync_folder_domain = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_domain);
        final EditText et_sync_folder_user = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_user);
        final EditText et_sync_folder_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_pass);

        final Button btn_sync_folder_logon = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_logon_btn);
        final Button btn_sync_folder_list_share = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_share_btn);
        final EditText et_sync_folder_share_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_share_name);
        final Button btn_sync_folder_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_internal_directory_btn);

        final EditText et_sync_folder_internal_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_directory_name);
        final EditText et_sync_folder_smb_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_directory_name);
        final EditText et_sync_folder_sdcard_dir_name = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_directory_name);


        final LinearLayout ll_dir_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_directory_view);
        final LinearLayout ll_zip_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_view);

        final CheckedTextView ctv_zip_file_save_sdcard = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_use_sdcard);

        final TextView tv_zip_dir = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_dir_name);
        final EditText et_zip_file = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_name);
        final Spinner sp_comp_level = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_comp_level);
        final RadioGroup rg_zip_enc_type = (RadioGroup) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rg);
        final RadioButton rb_zip_enc_none = (RadioButton) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_none);
        final RadioButton rb_zip_enc_standard = (RadioButton) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_standard);
        final RadioButton rb_zip_enc_aes128 = (RadioButton) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_aes128);
        final RadioButton rb_zip_enc_aes256 = (RadioButton) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_aes256);
        final EditText et_zip_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_password);
        final EditText et_zip_conf_pswd = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_confirm);

        SyncFolderEditValue nsfev = org_sfev.clone();
        String sel = sp_sync_folder_type.getSelectedItem().toString();
        if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal))) {//Internal
            nsfev.folder_directory = et_sync_folder_internal_dir_name.getText().toString().trim();
            nsfev.folder_mountpoint = sp_sync_folder_mp.getSelectedItem().toString().trim();
            nsfev.folder_type = SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL;
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard))) {//External sdcard
            nsfev.folder_directory = et_sync_folder_sdcard_dir_name.getText().toString().trim();
            nsfev.folder_mountpoint = sp_sync_folder_mp.getSelectedItem().toString().trim();
            nsfev.folder_type = SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD;
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_zip))) {//ZIP
            nsfev.folder_mountpoint = sp_sync_folder_mp.getSelectedItem().toString().trim();
            nsfev.zip_file_use_sdcard = ctv_zip_file_save_sdcard.isChecked();
            String cl = sp_comp_level.getSelectedItem().toString();
            if (cl.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_fastest)))
                nsfev.zip_comp_level = SyncTaskItem.ZIP_OPTION_COMP_LEVEL_FASTEST;
            else if (cl.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_fast)))
                nsfev.zip_comp_level = SyncTaskItem.ZIP_OPTION_COMP_LEVEL_FAST;
            else if (cl.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_normal)))
                nsfev.zip_comp_level = SyncTaskItem.ZIP_OPTION_COMP_LEVEL_NORMAL;
            else if (cl.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_maximum)))
                nsfev.zip_comp_level = SyncTaskItem.ZIP_OPTION_COMP_LEVEL_MAXIMUM;
            else if (cl.equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_ultra)))
                nsfev.zip_comp_level = SyncTaskItem.ZIP_OPTION_COMP_LEVEL_ULTRA;

            if (rb_zip_enc_none.isChecked())
                nsfev.zip_enc_method = SyncTaskItem.ZIP_OPTION_ENCRYPT_NONE;
            else if (rb_zip_enc_standard.isChecked())
                nsfev.zip_enc_method = SyncTaskItem.ZIP_OPTION_ENCRYPT_STANDARD;
            else if (rb_zip_enc_aes128.isChecked())
                nsfev.zip_enc_method = SyncTaskItem.ZIP_OPTION_ENCRYPT_AES128;
            else if (rb_zip_enc_aes256.isChecked())
                nsfev.zip_enc_method = SyncTaskItem.ZIP_OPTION_ENCRYPT_AES256;

            nsfev.zip_file_name=tv_zip_dir.getText().toString().trim()+et_zip_file.getText().toString().trim();
            if (!rb_zip_enc_none.isChecked()) {
                nsfev.zip_file_password = et_zip_pswd.getText().toString();
            } else {
                nsfev.zip_file_password = "";
            }
            nsfev.folder_type = SyncTaskItem.SYNC_FOLDER_TYPE_ZIP;
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb))) {//smb
            nsfev.folder_directory = et_sync_folder_smb_dir_name.getText().toString().trim();
            nsfev.folder_type = SyncTaskItem.SYNC_FOLDER_TYPE_SMB;
            if (JcifsUtil.isValidIpAddress(et_remote_host.getText().toString())) {
                nsfev.folder_remote_addr = et_remote_host.getText().toString().trim();
            } else {
                nsfev.folder_remote_host = et_remote_host.getText().toString().trim();
            }
            nsfev.folder_remote_domain = et_sync_folder_domain.getText().toString().trim();
            if (ctv_sync_folder_use_port.isChecked())
                nsfev.folder_remote_port = et_sync_folder_port.getText().toString();
            else nsfev.folder_remote_port = "";
            if (ctv_sync_folder_use_pswd.isChecked()) {
                nsfev.folder_remote_user = et_sync_folder_user.getText().toString();
                nsfev.folder_remote_pswd = et_sync_folder_pswd.getText().toString();
            } else {
                nsfev.folder_remote_user = "";
                nsfev.folder_remote_pswd = "";
            }
            nsfev.folder_remote_use_pswd =ctv_sync_folder_use_pswd.isChecked();
            nsfev.folder_remote_share = et_sync_folder_share_name.getText().toString().trim();
            nsfev.folder_smb_protocol=getSmbSelectedProtocol(sp_sync_folder_smb_proto);;
            nsfev.folder_smb_ipc_enforced=ctv_sync_folder_smb_ipc_enforced.isChecked();
        }
        return nsfev;
    }

    private void setSyncFolderOkButtonEnabled(Button ok_btn, boolean enabled) {
        ok_btn.setEnabled(enabled);
    }

    private void setSyncFolderViewVisibility(final Dialog dialog, final boolean master, SyncFolderEditValue org_sfev) {
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);
        final Spinner sp_sync_folder_type = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_folder_type);
        final LinearLayout ll_sync_folder_mp = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_local_mount_point_selector_view);
        final Spinner sp_sync_folder_mp = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_local_mount_point_selector);
        final Spinner sp_sync_folder_smb_proto = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_protocol);
        final CheckedTextView ctv_sync_folder_smb_ipc_enforced = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_smb_ipc_signing_enforced);
        final LinearLayout ll_sync_folder_smb_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_view);
        final LinearLayout ll_sync_folder_internal_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_internal_view);
        final LinearLayout ll_sync_folder_sdcard_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_view);
        final LinearLayout ll_sync_folder_zip_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_view);

        final Button btn_sync_folder_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_internal_directory_btn);
        final Button btn_sdcard_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_sdcard_select_document_tree);

        final LinearLayout ll_internal_keyword_view = (LinearLayout) ll_sync_folder_internal_view.findViewById(R.id.edit_sync_folder_dlg_internal_dir_keyword_view);
        final LinearLayout ll_smb_keyword_view = (LinearLayout) ll_sync_folder_smb_view.findViewById(R.id.edit_sync_folder_dlg_smb_dir_keyword_view);
        final LinearLayout ll_sdcard_keyword_view = (LinearLayout) ll_sync_folder_sdcard_view.findViewById(R.id.edit_sync_folder_dlg_sdcard_dir_keyword_view);
        final LinearLayout ll_zip_keyword_view = (LinearLayout) ll_sync_folder_zip_view.findViewById(R.id.edit_sync_folder_keyword_view);

        final CheckedTextView ctv_zip_file_save_sdcard = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_file_use_sdcard);
        final Button btn_zip_select_sdcard = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_select_document_tree);
//		ctv_sync_folder_use_usb_folder.setVisibility(CheckedTextView.GONE);
        btn_sync_folder_list_dir.setEnabled(true);
        String sel = sp_sync_folder_type.getSelectedItem().toString();
        btn_sdcard_select_sdcard.setVisibility(Button.GONE);

        if (master) {
            ll_internal_keyword_view.setVisibility(LinearLayout.GONE);
            ll_sdcard_keyword_view.setVisibility(LinearLayout.GONE);
            ll_smb_keyword_view.setVisibility(LinearLayout.GONE);
            ll_zip_keyword_view.setVisibility(LinearLayout.GONE);
        } else {
            ll_internal_keyword_view.setVisibility(LinearLayout.VISIBLE);
            ll_sdcard_keyword_view.setVisibility(LinearLayout.VISIBLE);
            ll_smb_keyword_view.setVisibility(LinearLayout.VISIBLE);
            ll_zip_keyword_view.setVisibility(LinearLayout.VISIBLE);
        }
        if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb))) {
            ll_sync_folder_internal_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_smb_view.setVisibility(LinearLayout.VISIBLE);
            ll_sync_folder_sdcard_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_zip_view.setVisibility(LinearLayout.GONE);
            if (getSmbSelectedProtocol(sp_sync_folder_smb_proto).equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY)) {
                ctv_sync_folder_smb_ipc_enforced.setEnabled(false);
            } else {
                ctv_sync_folder_smb_ipc_enforced.setEnabled(true);
            }
            checkSyncFolderValidation(dialog, org_sfev);
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_SMB);
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal))) {
            ll_sync_folder_internal_view.setVisibility(LinearLayout.VISIBLE);
            ll_sync_folder_smb_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_sdcard_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_zip_view.setVisibility(LinearLayout.GONE);
            checkSyncFolderValidation(dialog, org_sfev);
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard))) {
            ll_sync_folder_internal_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_smb_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_sdcard_view.setVisibility(LinearLayout.VISIBLE);
            ll_sync_folder_zip_view.setVisibility(LinearLayout.GONE);
            checkSyncFolderValidation(dialog, org_sfev);
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD);

            ll_sync_folder_mp.setVisibility(LinearLayout.GONE);
            if (mGp.safMgr.getSdcardDirectory().equals(SafFileManager.UNKNOWN_SDCARD_DIRECTORY)) {
                if (SafFileManager.isSdcardMountPointExisted(mContext, mGp.settingDebugLevel > 0)) {
                    dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_press_select_btn));
                    dlg_msg.setVisibility(TextView.VISIBLE);
                    btn_sdcard_select_sdcard.setEnabled(true);
                } else {
                    dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted));
                    dlg_msg.setVisibility(TextView.VISIBLE);
                    btn_sdcard_select_sdcard.setEnabled(false);
                }
                btn_sync_folder_list_dir.setEnabled(false);
            } else {
                btn_sync_folder_list_dir.setEnabled(true);
                dlg_msg.setVisibility(TextView.GONE);
                dlg_msg.setText("");
                btn_sdcard_select_sdcard.setEnabled(true);
                checkSyncFolderValidation(dialog, org_sfev);
            }
            btn_sdcard_select_sdcard.setVisibility(Button.VISIBLE);
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD);

        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_zip))) {
            ll_sync_folder_internal_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_smb_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_sdcard_view.setVisibility(LinearLayout.GONE);
            ll_sync_folder_zip_view.setVisibility(LinearLayout.VISIBLE);
            checkSyncFolderValidation(dialog, org_sfev);
            setSyncFolderFieldHelpListener(dialog, SyncTaskItem.SYNC_FOLDER_TYPE_ZIP);

            if (ctv_zip_file_save_sdcard.isChecked()) {
                ll_sync_folder_mp.setVisibility(Spinner.GONE);
                btn_zip_select_sdcard.setVisibility(Button.VISIBLE);
                if (mGp.safMgr.getSdcardDirectory().equals(SafFileManager.UNKNOWN_SDCARD_DIRECTORY)) {
                    if (SafFileManager.isSdcardMountPointExisted(mContext, mGp.settingDebugLevel > 0)) {
                        dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_press_select_btn));
                        dlg_msg.setVisibility(TextView.VISIBLE);
                        btn_zip_select_sdcard.setEnabled(true);
                    } else {
                        dlg_msg.setText(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted));
                        dlg_msg.setVisibility(TextView.VISIBLE);
                        btn_zip_select_sdcard.setEnabled(false);
                    }
                    btn_sync_folder_list_dir.setEnabled(false);
                } else {
                    btn_sync_folder_list_dir.setEnabled(true);
                    dlg_msg.setVisibility(TextView.GONE);
                    dlg_msg.setText("");
                    btn_zip_select_sdcard.setEnabled(true);
                    checkSyncFolderValidation(dialog, org_sfev);
                }
            } else {
                btn_zip_select_sdcard.setVisibility(Button.GONE);
                checkSyncFolderValidation(dialog, org_sfev);
            }
        }
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
        final Button btn_sync_folder_logon = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_logon_btn);
        final Button btn_sync_folder_list_share = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_share_btn);
        final Button btn_sync_folder_list_dir = (Button) dialog.findViewById(R.id.edit_sync_folder_dlg_list_internal_directory_btn);
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
        final RadioButton rb_zip_enc_none = (RadioButton) dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_none);
//		final RadioButton rb_zip_enc_standard = (RadioButton)dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_standard);
//		final RadioButton rb_zip_enc_aes128 = (RadioButton)dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_aes128);
//		final RadioButton rb_zip_enc_aes256 = (RadioButton)dialog.findViewById(R.id.edit_sync_folder_dlg_zip_enc_type_rb_aes256);
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
            } else {
                if (ctv_sync_folder_use_port.isChecked() && sync_folder_port.equals("")) {
                    result = false;
                    setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_main_sync_profile_dlg_specify_host_port_number));
                } else {
                    if (ctv_sync_folder_use_pswd.isChecked()) {
                        if (sync_folder_user.equals("") && sync_folder_pswd.equals("")) {
                            result = false;
                            setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_main_sync_profile_dlg_specify_host_userid_pswd));
                        }
                    }
                    if (result && folder_share_name.equals("")) {
                        setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_main_sync_profile_dlg_specify_host_share_name));
                        result = false;
                    }
                }
            }
            if (folder_share_name.equals("")) btn_sync_folder_logon.setEnabled(false);
            else btn_sync_folder_logon.setEnabled(true);

            boolean enabled = true;
            if (ctv_sync_folder_use_pswd.isChecked()) {
                if (et_sync_folder_user.getText().toString().equals("") && et_sync_folder_pswd.getText().toString().equals(""))
                    enabled = false;
            }
            if (ctv_sync_folder_use_port.isChecked()) {
                if (et_sync_folder_port.getText().toString().equals("")) enabled = false;
            }
            if (et_remote_host.getText().toString().equals("")) enabled = false;

            btn_sync_folder_list_share.setEnabled(enabled);
            btn_sync_folder_list_dir.setEnabled(enabled);
        } else if (sel.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_zip))) {
            result = false;
            if (ctv_zip_file_save_sdcard.isChecked()) {
                if (mGp.safMgr.getSdcardSafFile() == null) {
                    btn_zip_filelist.setEnabled(false);
                } else {
                    btn_zip_filelist.setEnabled(true);
                }
            } else {
                btn_zip_filelist.setEnabled(true);
            }

            if (et_sync_folder_zip_file_name.getText().length() > 0) {
                if (rb_zip_enc_none.isChecked()) {
                    result = true;
                } else {
                    if (et_zip_pswd.getText().length() > 0) {
                        if (et_zip_pswd.getText().toString().equals(et_zip_conf_pswd.getText().toString())) {
                            result = true;
                        } else {
                            setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_diff_zip_password));
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
        if (!prefs.getBoolean(getString(R.string.settings_suppress_warning_app_specific_dir), false)) {
            if (dir.startsWith(APP_SPECIFIC_DIRECTORY)) {
                final Dialog dialog = new Dialog(getActivity());//, android.R.style.Theme_Black);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.confirm_app_specific_dir_dlg);

                final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.confirm_app_specific_dlg_title_view);
                final TextView title = (TextView) dialog.findViewById(R.id.confirm_app_specific_dlg_title);
                title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
                title.setText(mContext.getString(R.string.msgs_main_app_specific_dir_used_title));
                title.setTextColor(mGp.themeColorList.text_color_warning);

                ((TextView) dialog.findViewById(R.id.confirm_app_specific_dlg_msg))
                        .setText(mContext.getString(R.string.msgs_main_app_specific_dir_used_msg) +
                                "\n" + sti.getSyncTaskName());

                final Button btnClose = (Button) dialog.findViewById(R.id.confirm_app_specific_dlg_close);
                final CheckedTextView ctvSuppr = (CheckedTextView) dialog.findViewById(R.id.confirm_app_specific_dlg_ctv_suppress);
                SyncUtil.setCheckedTextView(ctvSuppr);

                CommonDialog.setDlgBoxSizeCompact(dialog);
                ctvSuppr.setChecked(false);
                // Closeボタンの指定
                btnClose.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (ctvSuppr.isChecked()) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                            prefs.edit().putBoolean(getString(R.string.settings_suppress_warning_app_specific_dir), true).commit();
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
//				dialog.setOnKeyListener(new DialogOnKeyListener(mContext));
//				dialog.setCancelable(false);
                dialog.show();
            } else {
                if (p_ntfy != null) p_ntfy.notifyToListener(true, null);
            }
        }
    }

    private void invokeEditDirFilterDlg(final Dialog dialog, final SyncTaskItem n_sti, final String type, final TextView dlg_msg) {
        final TextView dlg_dir_filter = (TextView) dialog.findViewById(R.id.sync_filter_edit_dir_filter_btn);
        NotifyEvent ntfy = new NotifyEvent(mContext);
        //Listen setRemoteShare response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context arg0, Object[] arg1) {
                dlg_dir_filter.setText(buildFilterInfo(n_sti.getDirFilter()));
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }

            @Override
            public void negativeResponse(Context arg0, Object[] arg1) {
            }

        });
        mTaskUtil.editDirFilterDlg(n_sti, ntfy);

    }

    private void invokeEditFileFilterDlg(Dialog dialog, final SyncTaskItem n_sti, final String type, final TextView dlg_msg) {
        final TextView dlg_file_filter = (TextView) dialog.findViewById(R.id.sync_filter_edit_file_filter_btn);
        NotifyEvent ntfy = new NotifyEvent(mContext);
        //Listen setRemoteShare response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context arg0, Object[] arg1) {
                dlg_file_filter.setText(buildFilterInfo(n_sti.getFileFilter()));
//				Log.v("","siz="+n_sti.getFileFilter().size());
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }

            @Override
            public void negativeResponse(Context arg0, Object[] arg1) {
            }

        });
        mTaskUtil.editFileFilterDlg(n_sti.getFileFilter(), ntfy);

    }

    private String buildMasterSyncFolderInfo(SyncTaskItem sti, Button ib) {
        String info = "";
        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            String dir = sti.getMasterDirectoryName();
//        	Log.v("","md="+dir);
            if (dir.equals("")) info = sti.getMasterLocalMountPoint();
            else info = sti.getMasterLocalMountPoint() + "/" + dir;
            ib.setCompoundDrawablePadding(32);
            ib.setCompoundDrawablesWithIntrinsicBounds(
                    mContext.getResources().getDrawable(R.drawable.ic_32_mobile, null), null, null, null);
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            String dir = sti.getMasterDirectoryName();
            if (dir.equals("")) info = mGp.safMgr.getSdcardDirectory();
            else info = mGp.safMgr.getSdcardDirectory() + "/" + dir;
            if (mGp.safMgr.getSdcardDirectory().equals(SafFileManager.UNKNOWN_SDCARD_DIRECTORY)) {
                ib.setCompoundDrawablePadding(32);
                ib.setCompoundDrawablesWithIntrinsicBounds(
                        mContext.getResources().getDrawable(R.drawable.ic_32_bad_media, null), null, null, null);
            } else {
                ib.setCompoundDrawablePadding(32);
                ib.setCompoundDrawablesWithIntrinsicBounds(
                        mContext.getResources().getDrawable(R.drawable.ic_32_sdcard, null), null, null, null);
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            String host = sti.getMasterSmbAddr();
            if (sti.getMasterSmbAddr().equals("")) host = sti.getMasterSmbHostName();
            String share = sti.getMasterRemoteSmbShareName();
            String dir = sti.getMasterDirectoryName();
            if (dir.equals("")) info = "smb://" + host + "/" + share;
            else info = "smb://" + host + "/" + share + "/" + dir;
            ib.setCompoundDrawablePadding(32);
            ib.setCompoundDrawablesWithIntrinsicBounds(
                    mContext.getResources().getDrawable(R.drawable.ic_32_server, null), null, null, null);
        }
        return info;
    }

    private String buildTargetSyncFolderInfo(SyncTaskItem sti, Button ib) {
        String info = "";
        if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            String dir = sti.getTargetDirectoryName();
            if (dir.equals("")) info = sti.getTargetLocalMountPoint();
            else info = sti.getTargetLocalMountPoint() + "/" + dir;
            ib.setCompoundDrawablePadding(32);
            ib.setCompoundDrawablesWithIntrinsicBounds(
                    mContext.getResources().getDrawable(R.drawable.ic_32_mobile, null), null, null, null);
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            String dir = sti.getTargetDirectoryName();
            if (dir.equals("")) info = mGp.safMgr.getSdcardDirectory();
            else info = mGp.safMgr.getSdcardDirectory() + "/" + dir;
            if (mGp.safMgr.getSdcardDirectory().equals(SafFileManager.UNKNOWN_SDCARD_DIRECTORY)) {
                ib.setCompoundDrawablePadding(32);
                ib.setCompoundDrawablesWithIntrinsicBounds(
                        mContext.getResources().getDrawable(R.drawable.ic_32_bad_media, null), null, null, null);
            } else {
                ib.setCompoundDrawablePadding(32);
                ib.setCompoundDrawablesWithIntrinsicBounds(
                        mContext.getResources().getDrawable(R.drawable.ic_32_sdcard, null), null, null, null);
            }
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
            if (!sti.isTargetZipUseExternalSdcard())
                info = mGp.internalRootDirectory + sti.getTargetZipOutputFileName();
            else {
                info = mGp.safMgr.getSdcardDirectory() + sti.getTargetZipOutputFileName();
            }
            if (sti.isTargetZipUseExternalSdcard() &&
                    mGp.safMgr.getSdcardDirectory().equals(SafFileManager.UNKNOWN_SDCARD_DIRECTORY)) {
                ib.setCompoundDrawablePadding(32);
                ib.setCompoundDrawablesWithIntrinsicBounds(
                        mContext.getResources().getDrawable(R.drawable.ic_32_bad_media, null), null, null, null);
            } else {
                ib.setCompoundDrawablePadding(32);
                ib.setCompoundDrawablesWithIntrinsicBounds(
                        mContext.getResources().getDrawable(R.drawable.ic_32_archive, null), null, null, null);
            }
        } else if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            String host = sti.getTargetSmbAddr();
            if (sti.getTargetSmbAddr().equals("")) host = sti.getTargetSmbHostName();
            String share = sti.getTargetSmbShareName();
            String dir = sti.getTargetDirectoryName();
            if (dir.equals("")) info = "smb://" + host + "/" + share;
            else info = "smb://" + host + "/" + share + "/" + dir;
            ib.setCompoundDrawablePadding(32);
            ib.setCompoundDrawablesWithIntrinsicBounds(
                    mContext.getResources().getDrawable(R.drawable.ic_32_server, null), null, null, null);
        }
        return info;
    }

    private void setSyncFolderZipCompressionLevelSpinner(Spinner spinner, String cv) {
        SyncUtil.setSpinnerBackground(mContext, spinner, mGp.themeIsLight);
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, R.layout.custom_simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_prompt));
        spinner.setAdapter(adapter);
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_fastest));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_fast));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_normal));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_maximum));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_zip_comp_level_ultra));
//		adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_saf_usb));

        int sel = 2;
        if (cv.equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_FASTEST)) sel = 0;
        else if (cv.equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_FAST)) sel = 1;
        else if (cv.equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_NORMAL)) sel = 2;
        else if (cv.equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_MAXIMUM)) sel = 3;
        else if (cv.equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_ULTRA)) sel = 4;

        spinner.setSelection(sel);
        adapter.notifyDataSetChanged();
    }

    private void setSyncFolderMpSpinner(SyncTaskItem sti, Spinner spinner, String cv, boolean write_only) {
        SyncUtil.setSpinnerBackground(mContext, spinner, mGp.themeIsLight);
        final CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(mContext, R.layout.custom_simple_spinner_item);
        mGp.safMgr.loadSafFileList();
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_prompt));
        spinner.setAdapter(adapter);

        int sel_no = 0;
//        ArrayList<String>mpl=LocalMountPoint.getLocalMountpointList2(mContext);
        ArrayList<String> mpl = LocalMountPoint.getLocalMountpointList2(mContext);
        if (mpl == null || mpl.size() == 0) {
            adapter.add(mGp.internalRootDirectory);
        } else {
            for (String item : mpl) {
                if ((write_only && LocalMountPoint.isMountPointCanWrite(item)) ||
                        !write_only) {
                    if (item.equals(cv)) {
                        sel_no = adapter.getCount();
                    }
                    adapter.add(item);
                }
            }
        }
        spinner.setSelection(sel_no);
        adapter.notifyDataSetChanged();
    }

    private String getSmbSelectedProtocol(Spinner spinner) {
        if (spinner.getSelectedItem()==null) {
            return SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY;
        }
        String sel=spinner.getSelectedItem().toString();
        if (spinner.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb1_only))) {
            return SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY;
        } else if (spinner.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb2_only))) {
            return SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB2_ONLY;
        }
        return SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY;
    }

    private void setSyncFolderSmbProtoSpinner(SyncTaskItem sti, Spinner spinner, String cv) {
        SyncUtil.setSpinnerBackground(mContext, spinner, mGp.themeIsLight);
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, R.layout.custom_simple_spinner_item);
        mGp.safMgr.loadSafFileList();
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_prompt));
        spinner.setAdapter(adapter);

//        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_system));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb1_only));
        adapter.add(mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_smb2_only));

        if (cv.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY)) spinner.setSelection(0);
        else if (cv.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB2_ONLY)) spinner.setSelection(1);
        else spinner.setSelection(0);
    }

    private void setSyncFolderTypeSpinner(SyncTaskItem sti, Spinner spinner, String cv, boolean master) {
        SyncUtil.setSpinnerBackground(mContext, spinner, mGp.themeIsLight);
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, R.layout.custom_simple_spinner_item);
        mGp.safMgr.loadSafFileList();
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_prompt));
        spinner.setAdapter(adapter);
        int sel = 0;
        if (master) {
            if (!sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
//					adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb));
                    if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
//					else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) sel=2;
                } else {
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                    adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
//					adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb));

                    if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                    else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
//					else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) sel=3;
                }
            } else {
//				adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
//					adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb));

                if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
//					else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) sel=3;
            }
        } else {
            if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
//                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_zip));
//				adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb));
                if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
//                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) sel=3;
//				else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) sel=2;
            } else {
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_internal));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_smb));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_sdcard));
                adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_zip));
//				adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_type_usb));
                if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) sel = 0;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) sel = 1;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) sel = 2;
                else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) sel = 3;
//				else if (cv.equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) sel=4;
            }
        }
        spinner.setSelection(sel);
        adapter.notifyDataSetChanged();
    }

    private void setSyncOptionSpinner(Spinner spinnerSyncOption, String prof_syncopt) {
        SyncUtil.setSpinnerBackground(mContext, spinnerSyncOption, mGp.themeIsLight);
        final CustomSpinnerAdapter adapterSyncOption =
                new CustomSpinnerAdapter(mContext, R.layout.custom_simple_spinner_item);
        adapterSyncOption.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSyncOption.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_syncopt_prompt));
        spinnerSyncOption.setAdapter(adapterSyncOption);
        adapterSyncOption.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_mirror));
        adapterSyncOption.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_copy));
        adapterSyncOption.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_move));
//		adapterSyncOption.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync));

        if (prof_syncopt.equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR))
            spinnerSyncOption.setSelection(0);
        else if (prof_syncopt.equals(SyncTaskItem.SYNC_TASK_TYPE_COPY))
            spinnerSyncOption.setSelection(1);
        else if (prof_syncopt.equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE))
            spinnerSyncOption.setSelection(2);
//		else if (prof_syncopt.equals(SyncTaskItem.SYNC_TASK_TYPE_SYNC)) spinnerSyncOption.setSelection(3);

        adapterSyncOption.notifyDataSetChanged();
    }

    private void setSyncWifiOptionSpinner(Spinner spinner, String cv) {
        SyncUtil.setSpinnerBackground(mContext, spinner, mGp.themeIsLight);
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, R.layout.custom_simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_prompt));
        spinner.setAdapter(adapter);
        adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_off));
        adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_any_ap));
        adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_specific_ap));

        if (cv.equals("0")) spinner.setSelection(0);
        else if (cv.equals("1")) spinner.setSelection(1);
        else if (cv.equals("2")) spinner.setSelection(2);

        adapter.notifyDataSetChanged();
    }

    private void setSyncDiffTimeValue(Spinner spinner, int cv) {
        SyncUtil.setSpinnerBackground(mContext, spinner, mGp.themeIsLight);
        final CustomSpinnerAdapter adapter =
                new CustomSpinnerAdapter(mContext, R.layout.custom_simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt(mContext.getString(R.string.msgs_main_sync_profile_dlg_diff_time_value_option_prompt));
        spinner.setAdapter(adapter);
        adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_diff_time_value_option_1));
        adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_diff_time_value_option_3));
        adapter.add(mContext.getString(R.string.msgs_main_sync_profile_dlg_diff_time_value_option_10));

        if (cv == 1) spinner.setSelection(0);
        else if (cv == 3) spinner.setSelection(1);
        else if (cv == 10) spinner.setSelection(2);

        adapter.notifyDataSetChanged();
    }

    private String buildFilterInfo(ArrayList<String> filter_list) {
        String info = "";
        if (filter_list != null && filter_list.size() > 0) {
            String t_info = "", cn = "";
            for (int i = 0; i < filter_list.size(); i++) {
                t_info += cn + filter_list.get(i).substring(1, filter_list.get(i).length());
                cn = ",";
            }
            if (!t_info.equals("")) info = t_info;
//				info=mContext.getString(R.string.msgs_filter_list_dlg_filter_hint)+" : "+t_info;
        } else {
//			info=mContext.getString(R.string.msgs_filter_list_dlg_not_specified);
            info = "";
        }
        return info;
    }

    private String checkTaskNameValidity(String type, String t_name, TextView tv, Button ok) {
        String result = "";
        if (type.equals("EDIT")) {
        } else {
            if (t_name.length() > 0) {
                if (SyncTaskUtil.getSyncTaskByName(mGp.syncTaskAdapter, t_name) == null) {
                    result = "";
                } else {
                    result = mContext.getString(R.string.msgs_duplicate_task_name);
                }
            } else {
                result = mContext.getString(R.string.msgs_specify_task_name);
            }
        }
//		Log.v("","result="+result+", name="+t_name+", type="+type);
        return result;
    }

    public void editProfile(final String type, final SyncTaskItem pfli) {
        final SyncTaskItem n_sti = pfli.clone();

        mGp.safMgr.loadSafFileList();

        // カスタムダイアログの生成
        mDialog.setContentView(R.layout.edit_sync_task_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_dlg_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) mDialog.findViewById(R.id.edit_profile_sync_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        final TextView dlg_title = (TextView) mDialog.findViewById(R.id.edit_profile_sync_title);
        dlg_title.setTextColor(mGp.themeColorList.text_color_dialog_title);

        final TextView dlg_title_sub = (TextView) mDialog.findViewById(R.id.edit_profile_sync_title_sub);
        dlg_title_sub.setTextColor(mGp.themeColorList.text_color_dialog_title);

        final TextView dlg_msg = (TextView) mDialog.findViewById(R.id.edit_sync_task_msg);
        dlg_msg.setTextColor(mGp.themeColorList.text_color_error);
        dlg_msg.setVisibility(TextView.GONE);

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
        } else if (type.equals("ADD")) {
            dlg_title.setText(mContext.getString(R.string.msgs_add_sync_profile));
            dlg_title_sub.setVisibility(TextView.GONE);
            n_sti.setSyncWifiStatusOption("0");
        }
        final CheckedTextView ctv_auto = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_ctv_auto);
        SyncUtil.setCheckedTextView(ctv_auto);
        ctv_auto.setChecked(n_sti.isSyncTaskAuto());
        ctv_auto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final Spinner spinnerSyncOption = (Spinner) mDialog.findViewById(R.id.edit_sync_task_sync_option);
        setSyncOptionSpinner(spinnerSyncOption, n_sti.getSyncTaskType());
        spinnerSyncOption.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Spinner spinnerSyncWifiStatus = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_wifi_status);
        final LinearLayout ll_wifi_ap_list = (LinearLayout) mDialog.findViewById(R.id.edit_sync_task_option_ap_list_view);
        final Button edit_wifi_ap_list = (Button) mDialog.findViewById(R.id.edit_sync_task_option_btn_edit_ap_white_list);
        setWifiApWhileListInfo(n_sti.getSyncWifiConnectionWhiteList(), edit_wifi_ap_list);
        final CheckedTextView ctv_task_skip_if_ssid_invalid = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ap_list_task_skip_if_ssid_invalid);
        SyncUtil.setCheckedTextView(ctv_task_skip_if_ssid_invalid);
        ctv_task_skip_if_ssid_invalid.setChecked(n_sti.isSyncTaskSkipIfConnectAnotherWifiSsid());

        final CheckedTextView ctv_task_sync_when_cahrging = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_start_when_charging);
        SyncUtil.setCheckedTextView(ctv_task_sync_when_cahrging);
        ctv_task_sync_when_cahrging.setChecked(n_sti.isSyncOptionSyncWhenCharging());
        ctv_task_sync_when_cahrging.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        setSyncWifiOptionSpinner(spinnerSyncWifiStatus, n_sti.getSyncWifiStatusOption());
        if (n_sti.getSyncWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_SPECIFIC_AP))
            ll_wifi_ap_list.setVisibility(Button.VISIBLE);
        else ll_wifi_ap_list.setVisibility(Button.GONE);

        spinnerSyncWifiStatus.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerSyncWifiStatus.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_specific_ap))) {
                    ll_wifi_ap_list.setVisibility(Button.VISIBLE);
                } else {
                    ll_wifi_ap_list.setVisibility(Button.GONE);
                }
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setSyncTaskFieldHelpListener(mDialog, n_sti);

        final Button swap_master_target = (Button) mDialog.findViewById(R.id.edit_sync_task_change_master_and_target_btn);
        final Button master_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_master_folder_info_btn);
        master_folder_info.setText(buildMasterSyncFolderInfo(n_sti, master_folder_info));
        final Button target_folder_info = (Button) mDialog.findViewById(R.id.edit_sync_task_target_folder_info_btn);
        target_folder_info.setText(buildTargetSyncFolderInfo(n_sti, target_folder_info));

        boolean is_all_file_type = false;
        if (!n_sti.isSyncFileTypeAudio() && !n_sti.isSyncFileTypeImage() && !n_sti.isSyncFileTypeVideo() &&
                n_sti.getFileFilter().size() == 0) is_all_file_type = true;

        boolean is_all_sub_dir = false;
        if (n_sti.getDirFilter().size() == 0) is_all_sub_dir = true;

        final Button dir_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_dir_filter_btn);
        final Button file_filter_btn = (Button) mDialog.findViewById(R.id.sync_filter_edit_file_filter_btn);
//		final TextView dlg_file_filter=(TextView) mDialog.findViewById(R.id.sync_filter_summary_file_filter);
        file_filter_btn.setText(buildFilterInfo(n_sti.getFileFilter()));
//		final TextView dlg_dir_filter=(TextView) mDialog.findViewById(R.id.sync_filter_summary_dir_filter);
        dir_filter_btn.setText(buildFilterInfo(n_sti.getDirFilter()));

        final LinearLayout ll_file_filter_detail = (LinearLayout) mDialog.findViewById(R.id.sync_filter_file_type_detail_view);
        final LinearLayout ll_dir_filter_detail = (LinearLayout) mDialog.findViewById(R.id.sync_filter_sub_directory_detail_view);

        final CheckedTextView ctvSyncFileTypeSpecific = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_specific);
        ctvSyncFileTypeSpecific.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctvSyncFileTypeSpecific.isChecked();
                ctvSyncFileTypeSpecific.setChecked(isChecked);
                if (isChecked) ll_file_filter_detail.setVisibility(Button.VISIBLE);
                else ll_file_filter_detail.setVisibility(Button.GONE);
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
                else ll_dir_filter_detail.setVisibility(Button.GONE);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });
        if (!is_all_sub_dir) ll_dir_filter_detail.setVisibility(Button.VISIBLE);
        else ll_dir_filter_detail.setVisibility(Button.GONE);

        final CheckedTextView ctvProcessRootDirFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_master_root_dir_file);
        SyncUtil.setCheckedTextView(ctvProcessRootDirFile);
        ctvProcessRootDirFile.setChecked(n_sti.isSyncProcessRootDirFile());

        final CheckedTextView ctvSyncSubDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_sub_dir);
        SyncUtil.setCheckedTextView(ctvSyncSubDir);
        ctvSyncSubDir.setChecked(n_sti.isSyncSubDirectory());

        if (n_sti.isSyncProcessRootDirFile()) {
            ctvProcessRootDirFile.setChecked(true);
            ctvSyncSubDir.setChecked(n_sti.isSyncSubDirectory());
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

        ctvSyncSubDir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final CheckedTextView ctvSyncEmptyDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_empty_directory);
        SyncUtil.setCheckedTextView(ctvSyncEmptyDir);
        ctvSyncEmptyDir.setChecked(n_sti.isSyncEmptyDirectory());
        ctvSyncEmptyDir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final CheckedTextView ctvSyncHiddenDir = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_directory);
        SyncUtil.setCheckedTextView(ctvSyncHiddenDir);
        ctvSyncHiddenDir.setChecked(n_sti.isSyncHiddenDirectory());
        ctvSyncHiddenDir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final CheckedTextView ctvSyncHiddenFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_hidden_file);
        SyncUtil.setCheckedTextView(ctvSyncHiddenFile);
        ctvSyncHiddenFile.setChecked(n_sti.isSyncHiddenFile());
        ctvSyncHiddenFile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final CheckedTextView ctvProcessOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_process_override_delete_file);
        ctvProcessOverride.setChecked(n_sti.isSyncOverrideCopyMoveFile());
        ctvProcessOverride.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final CheckedTextView ctvConfirmOverride = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_confirm_override_delete_file);
        SyncUtil.setCheckedTextView(ctvConfirmOverride);
        ctvConfirmOverride.setChecked(n_sti.isSyncConfirmOverrideOrDelete());
        ctvConfirmOverride.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final CheckedTextView ctvCopyByRename = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_copy_rename);
        SyncUtil.setCheckedTextView(ctvCopyByRename);
        ctvCopyByRename.setChecked(n_sti.isSyncUseFileCopyByTempName());
        ctvCopyByRename.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final CheckedTextView ctUseExtendedDirectoryFilter1 = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_extended_filter1);
        SyncUtil.setCheckedTextView(ctUseExtendedDirectoryFilter1);
        ctUseExtendedDirectoryFilter1.setChecked(n_sti.isSyncUseExtendedDirectoryFilter1());
        ctUseExtendedDirectoryFilter1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

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

        final CheckedTextView ctvDoNotResetRemoteFile = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_do_mot_reset_remote_file_last_mod_time);
        SyncUtil.setCheckedTextView(ctvDoNotResetRemoteFile);
        ctvDoNotResetRemoteFile.setChecked(n_sti.isSyncDoNotResetLastModifiedSmbFile());
        ctvDoNotResetRemoteFile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final CheckedTextView ctvUseSmbsyncLastMod = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_use_smbsync_last_mod_time);
        SyncUtil.setCheckedTextView(ctvUseSmbsyncLastMod);
        ctvUseSmbsyncLastMod.setChecked(n_sti.isSyncDetectLastModifiedBySmbsync());
        ctvUseSmbsyncLastMod.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final CheckedTextView ctvRetry = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_retry_if_error_occured);
        SyncUtil.setCheckedTextView(ctvRetry);
        if (n_sti.getSyncRetryCount().equals("0")) ctvRetry.setChecked(false);
        else ctvRetry.setChecked(true);
        ctvRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final CheckedTextView ctvSyncUseRemoteSmallIoArea = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_remote_small_io_area);
        SyncUtil.setCheckedTextView(ctvSyncUseRemoteSmallIoArea);
        ctvSyncUseRemoteSmallIoArea.setChecked(n_sti.isSyncUseSmallIoBuffer());
        ctvSyncUseRemoteSmallIoArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

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

        final CheckedTextView ctvDiffUseFileSize = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_file_size);
        SyncUtil.setCheckedTextView(ctvDiffUseFileSize);
        ctvDiffUseFileSize.setChecked(n_sti.isSyncDifferentFileBySize());
        ctvDiffUseFileSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final CheckedTextView ctDeterminChangedFileByTime = (CheckedTextView) mDialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_last_mod_time);
        SyncUtil.setCheckedTextView(ctDeterminChangedFileByTime);
        ctDeterminChangedFileByTime.setChecked(n_sti.isSyncDifferentFileByTime());
        ctDeterminChangedFileByTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        final Spinner spinnerSyncDiffTimeValue = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_diff_file_determin_time_value);
        setSyncDiffTimeValue(spinnerSyncDiffTimeValue, n_sti.getSyncDifferentFileAllowableTime());

        CommonDialog.setDlgBoxSizeLimit(mDialog, true);

        final Button btn_ok = (Button) mDialog.findViewById(R.id.edit_profile_sync_dlg_btn_ok);

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
        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);

//		Log.v("","ft="+n_sti.getTargetFolderType());
        if (n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP))
            swap_master_target.setEnabled(false);
        else swap_master_target.setEnabled(true);

        master_folder_info.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
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
                        n_sti.setMasterRemovableStorageID(nsfev.folder_removable_uuid);
//						n_sti.setMasterFolderUseInternalUsbFolder(nsfev.folder_use_usb_folder);
//						Log.v("","mdi="+n_sti.getMasterDirectoryName());
                        master_folder_info.setText(buildMasterSyncFolderInfo(n_sti, master_folder_info));

                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);

                        if (!prev_master_folder_type.equals(n_sti.getMasterFolderType())) {
                            if ((!n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) &&
                                    !n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB))) {
                                if (!n_sti.getSyncWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF)) {
                                    NotifyEvent ntfy = new NotifyEvent(mContext);
                                    ntfy.setListener(new NotifyEventListener() {
                                        @Override
                                        public void positiveResponse(Context context, Object[] objects) {
                                            n_sti.setSyncWifiStatusOption(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF);
                                            spinnerSyncWifiStatus.setSelection(0);
                                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                                        }

                                        @Override
                                        public void negativeResponse(Context context, Object[] objects) {
                                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                                        }
                                    });
                                    mCommonDlg.showCommonDialog(true, "W",
                                            mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_change_wifi_confition_to_off), "", ntfy);
                                }
                            } else if (n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) ||
                                    n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                                if (n_sti.getSyncWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF)) {
                                    NotifyEvent ntfy = new NotifyEvent(mContext);
                                    ntfy.setListener(new NotifyEventListener() {
                                        @Override
                                        public void positiveResponse(Context context, Object[] objects) {
                                            n_sti.setSyncWifiStatusOption(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP);
                                            spinnerSyncWifiStatus.setSelection(1);
                                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                                        }

                                        @Override
                                        public void negativeResponse(Context context, Object[] objects) {
                                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                                        }
                                    });
                                    mCommonDlg.showCommonDialog(true, "W",
                                            mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_change_wifi_confition_to_any), "", ntfy);
                                }
                            }
                        } else {
                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        mGp.safMgr.loadSafFileList();
                        master_folder_info.setText(buildMasterSyncFolderInfo(n_sti, master_folder_info));
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
                sfev.folder_remote_share = n_sti.getMasterRemoteSmbShareName();
                sfev.folder_remote_user = n_sti.getMasterSmbUserName();
                sfev.folder_smb_protocol=n_sti.getMasterSmbProtocol();
                sfev.folder_smb_ipc_enforced=n_sti.isMasterSmbIpcSigningEnforced();
                sfev.folder_removable_uuid = n_sti.getMasterRemovableStorageID();
                sfev.folder_type = n_sti.getMasterFolderType();
                if (!sfev.folder_remote_user.equals("") || !sfev.folder_remote_pswd.equals("")) {
                    sfev.folder_remote_use_pswd=true;
                } else {
                    sfev.folder_remote_use_pswd=false;
                }
                editSyncFolder(n_sti, sfev, ntfy);
            }
        });

        swap_master_target.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                SyncTaskItem t_sti = n_sti.clone();
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

                n_sti.setTargetDirectoryName(t_sti.getMasterDirectoryName());
                n_sti.setTargetLocalMountPoint(t_sti.getMasterLocalMountPoint());
                n_sti.setTargetFolderType(t_sti.getMasterFolderType());
                n_sti.setTargetRemoteAddr(t_sti.getMasterSmbAddr());
                n_sti.setTargetRemoteDomain(t_sti.getMasterSmbDomain());
                n_sti.setTargetRemoteHostname(t_sti.getMasterSmbHostName());
                n_sti.setTargetSmbPassword(t_sti.getMasterSmbPassword());
                n_sti.setTargetRemotePort(t_sti.getMasterSmbPort());
                n_sti.setTargetSmbShareName(t_sti.getMasterRemoteSmbShareName());
                n_sti.setTargetSmbUserName(t_sti.getMasterSmbUserName());
                n_sti.setTargetSmbProtocol(t_sti.getMasterSmbProtocol());
                n_sti.setTargetSmbIpcSigningEnforced(t_sti.isMasterSmbIpcSigningEnforced());
                n_sti.setTargetRemovableStorageID(t_sti.getMasterRemovableStorageID());

                master_folder_info.setText(buildMasterSyncFolderInfo(n_sti, master_folder_info));
                target_folder_info.setText(buildTargetSyncFolderInfo(n_sti, target_folder_info));

                checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
            }
        });

        target_folder_info.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        String prev_target_folder_type = n_sti.getTargetFolderType();
                        SyncFolderEditValue nsfev = (SyncFolderEditValue) o[0];
                        nsfev.folder_master = false;
                        n_sti.setTargetDirectoryName(nsfev.folder_directory);
                        n_sti.setTargetLocalMountPoint(nsfev.folder_mountpoint);
                        n_sti.setTargetRemoteAddr(nsfev.folder_remote_addr);
                        n_sti.setTargetRemoteDomain(nsfev.folder_remote_domain);
                        n_sti.setTargetRemoteHostname(nsfev.folder_remote_host);
                        n_sti.setTargetRemotePort(nsfev.folder_remote_port);
                        n_sti.setTargetSmbPassword(nsfev.folder_remote_pswd);
                        n_sti.setTargetSmbShareName(nsfev.folder_remote_share);
                        n_sti.setTargetSmbUserName(nsfev.folder_remote_user);
                        n_sti.setTargetFolderType(nsfev.folder_type);
                        n_sti.setTargetSmbProtocol(nsfev.folder_smb_protocol);
                        n_sti.setTargetSmbIpcSigningEnforced(nsfev.folder_smb_ipc_enforced);
                        n_sti.setTargetRemovableStorageID(nsfev.folder_removable_uuid);

                        n_sti.setTargetZipUseExternalSdcard(nsfev.zip_file_use_sdcard);
                        n_sti.setTargetZipCompressionLevel(nsfev.zip_comp_level);
                        n_sti.setTargetZipEncryptMethod(nsfev.zip_enc_method);
                        n_sti.setTargetZipOutputFileName(nsfev.zip_file_name);
                        n_sti.setTargetZipPassword(nsfev.zip_file_password);

                        target_folder_info.setText(buildTargetSyncFolderInfo(n_sti, target_folder_info));

                        if (n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP))
                            swap_master_target.setEnabled(false);
                        else swap_master_target.setEnabled(true);

                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);

                        if (!prev_target_folder_type.equals(n_sti.getTargetFolderType())) {
                            if (!n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) &&
                                    !n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                                if (!n_sti.getSyncWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF)) {
                                    NotifyEvent ntfy = new NotifyEvent(mContext);
                                    ntfy.setListener(new NotifyEventListener() {
                                        @Override
                                        public void positiveResponse(Context context, Object[] objects) {
                                            n_sti.setSyncWifiStatusOption(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF);
                                            spinnerSyncWifiStatus.setSelection(0);
                                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                                        }

                                        @Override
                                        public void negativeResponse(Context context, Object[] objects) {
                                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                                        }
                                    });
                                    mCommonDlg.showCommonDialog(true, "W",
                                            mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_change_wifi_confition_to_off), "", ntfy);
                                }
                            } else if (n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB) ||
                                    n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                                if (n_sti.getSyncWifiStatusOption().equals(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF)) {
                                    NotifyEvent ntfy = new NotifyEvent(mContext);
                                    ntfy.setListener(new NotifyEventListener() {
                                        @Override
                                        public void positiveResponse(Context context, Object[] objects) {
                                            n_sti.setSyncWifiStatusOption(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP);
                                            spinnerSyncWifiStatus.setSelection(1);
                                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                                        }

                                        @Override
                                        public void negativeResponse(Context context, Object[] objects) {
                                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                                        }
                                    });
                                    mCommonDlg.showCommonDialog(true, "W",
                                            mContext.getString(R.string.msgs_profile_edit_sync_folder_dlg_change_wifi_confition_to_any), "", ntfy);
                                }
                            }
                        } else {
                            confirmUseAppSpecificDir(n_sti, n_sti.getMasterDirectoryName(), null);
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        mGp.safMgr.loadSafFileList();
                        target_folder_info.setText(buildTargetSyncFolderInfo(n_sti, target_folder_info));
                        confirmUseAppSpecificDir(n_sti, n_sti.getTargetDirectoryName(), null);
                    }

                });
                SyncFolderEditValue sfev = new SyncFolderEditValue();
                sfev.folder_master = false;
                sfev.folder_title = mContext.getString(R.string.msgs_main_sync_profile_dlg_title_target);
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

//				Log.v("","level="+sfev.zip_file_use_sdcard);

                editSyncFolder(n_sti, sfev, ntfy);
            }
        });
        // wifi ap listボタンの指定
        edit_wifi_ap_list.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                //Listen setRemoteShare response
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context arg0, Object[] arg1) {
                        setWifiApWhileListInfo(n_sti.getSyncWifiConnectionWhiteList(), edit_wifi_ap_list);
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                    }

                    @Override
                    public void negativeResponse(Context arg0, Object[] arg1) {
                        checkSyncTaskOkButtonEnabled(mDialog, type, n_sti, dlg_msg);
                    }

                });
                mTaskUtil.editWifiApListDlg(n_sti.getSyncWifiConnectionWhiteList(), ntfy);
            }
        });

        // file filterボタンの指定
        file_filter_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                invokeEditFileFilterDlg(mDialog, n_sti, type, dlg_msg);
            }
        });
        // directory filterボタンの指定
        dir_filter_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                invokeEditDirFilterDlg(mDialog, n_sti, type, dlg_msg);
            }
        });

        // CANCELボタンの指定
        final Button btn_cancel = (Button) mDialog.findViewById(R.id.edit_profile_sync_dlg_btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (btn_ok.isEnabled()) {
                    NotifyEvent ntfy = new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            mFragment.dismiss();
                            if (mNotifyComplete != null)
                                mNotifyComplete.notifyToListener(false, null);
                        }

                        @Override
                        public void negativeResponse(Context context, Object[] objects) {
                        }
                    });
                    mCommonDlg.showCommonDialog(true, "W",
                            mContext.getString(R.string.msgs_schedule_confirm_title_nosave),
                            mContext.getString(R.string.msgs_profile_sync_folder_dlg_confirm_msg_nosave), ntfy);
                } else {
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
        btn_ok.setEnabled(false);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final SyncTaskItem new_stli = buildSyncTaskListItem(mDialog, n_sti);
                NotifyEvent ntfy_target_dir_not_specified = new NotifyEvent(mContext);
                ntfy_target_dir_not_specified.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        mFragment.dismiss();
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
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                if (!new_stli.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP) && new_stli.getTargetDirectoryName().equals("")) {
                    mCommonDlg.showCommonDialog(true, "W", "",
                            mContext.getString(R.string.msgs_main_sync_profile_dlg_target_directory_not_specified),
                            ntfy_target_dir_not_specified);
                } else {
                    ntfy_target_dir_not_specified.notifyToListener(true, null);
                }
            }
        });
    }

    private SyncTaskItem buildSyncTaskListItem(Dialog dialog, SyncTaskItem base_stli) {
        final EditText et_sync_main_task_name = (EditText) dialog.findViewById(R.id.edit_sync_task_task_name);
        final CheckedTextView ctv_auto = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_ctv_auto);

        final Spinner spinnerSyncOption = (Spinner) dialog.findViewById(R.id.edit_sync_task_sync_option);
        final Spinner spinnerSyncWifiStatus = (Spinner) dialog.findViewById(R.id.edit_sync_task_option_spinner_wifi_status);
        final Button edit_wifi_ap_list = (Button) dialog.findViewById(R.id.edit_sync_task_option_btn_edit_ap_white_list);
        final CheckedTextView ctv_task_skip_if_ssid_invalid = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ap_list_task_skip_if_ssid_invalid);

        final CheckedTextView ctv_task_sync_when_cahrging = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_start_when_charging);

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
        final CheckedTextView ctvCopyByRename = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_copy_rename);
        final CheckedTextView ctUseExtendedDirectoryFilter1 = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_extended_filter1);
        final CheckedTextView ctvShowSpecialOption = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_show_special_option);
        final CheckedTextView ctvDoNotResetRemoteFile = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_do_mot_reset_remote_file_last_mod_time);
        final CheckedTextView ctvUseSmbsyncLastMod = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_use_smbsync_last_mod_time);
        final CheckedTextView ctvRetry = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_retry_if_error_occured);
        final CheckedTextView ctvSyncUseRemoteSmallIoArea = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_use_remote_small_io_area);
        final CheckedTextView ctvTestMode = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_test_mode);
        final CheckedTextView ctvDiffUseFileSize = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_file_size);
        final CheckedTextView ctDeterminChangedFileByTime = (CheckedTextView) dialog.findViewById(R.id.edit_sync_task_option_ctv_sync_diff_use_last_mod_time);
        final Spinner spinnerSyncDiffTimeValue = (Spinner) dialog.findViewById(R.id.edit_sync_task_option_spinner_diff_file_determin_time_value);
        final Button btn_ok = (Button) dialog.findViewById(R.id.edit_profile_sync_dlg_btn_ok);

        SyncTaskItem nstli = base_stli.clone();

        nstli.setSyncTaskAuto(ctv_auto.isChecked());
        nstli.setSyncTaskName(et_sync_main_task_name.getText().toString());

        String so = spinnerSyncOption.getSelectedItem().toString();
        if (so.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_mirror)))
            nstli.setSyncTaskType(SyncTaskItem.SYNC_TASK_TYPE_MIRROR);
        else if (so.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_copy)))
            nstli.setSyncTaskType(SyncTaskItem.SYNC_TASK_TYPE_COPY);
        else if (so.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_move)))
            nstli.setSyncTaskType(SyncTaskItem.SYNC_TASK_TYPE_MOVE);
        else if (so.equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_sync)))
            nstli.setSyncTaskType(SyncTaskItem.SYNC_TASK_TYPE_SYNC);

        nstli.setSyncProcessRootDirFile(ctvProcessRootDirFile.isChecked());
        nstli.setSyncSubDirectory(ctvSyncSubDir.isChecked());
        nstli.setSyncEmptyDirectory(ctvSyncEmptyDir.isChecked());
        nstli.setSyncHiddenDirectory(ctvSyncHiddenDir.isChecked());
        nstli.setSyncHiddenFile(ctvSyncHiddenFile.isChecked());

        nstli.setSyncOverrideCopyMoveFile(ctvProcessOverride.isChecked());

        nstli.setSyncConfirmOverrideOrDelete(ctvConfirmOverride.isChecked());

        nstli.setSyncUseFileCopyByTempName(ctvCopyByRename.isChecked());

        nstli.setSyncUseExtendedDirectoryFilter1(ctUseExtendedDirectoryFilter1.isChecked());

        String wifi_sel = Integer.toString(spinnerSyncWifiStatus.getSelectedItemPosition());
        nstli.setSyncWifiStatusOption(wifi_sel);
        nstli.setSyncTaskSkipIfConnectAnotherWifiSsid(ctv_task_skip_if_ssid_invalid.isChecked());

        nstli.setSyncOptionSyncWhenCharging(ctv_task_sync_when_cahrging.isChecked());

        nstli.setSyncDoNotResetLastModifiedSmbFile(ctvDoNotResetRemoteFile.isChecked());
        nstli.setSyncDetectLastModidiedBySmbsync(ctvUseSmbsyncLastMod.isChecked());
        if (ctvRetry.isChecked()) nstli.setSyncRetryCount("3");
        else nstli.setSyncRetryCount("0");
        nstli.setSyncUseSmallIoBuffer(ctvSyncUseRemoteSmallIoArea.isChecked());
        nstli.setSyncTestMode(ctvTestMode.isChecked());
        nstli.setSyncDifferentFileBySize(ctvDiffUseFileSize.isChecked());
        nstli.setSyncDifferentFileByModTime(ctDeterminChangedFileByTime.isChecked());

        String diff_val = spinnerSyncDiffTimeValue.getSelectedItem().toString();
        nstli.setSyncDifferentFileAllowableTime(Integer.valueOf(diff_val));
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

        return nstli;
    }

    private void showFieldHelp(String title, String help_msg) {
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.help_view);
        LinearLayout ll_view = (LinearLayout) dialog.findViewById(R.id.help_view_title_view);
        ll_view.setBackgroundColor(Color.WHITE);

        TextView dlg_tv = (TextView) dialog.findViewById(R.id.help_view_title);
        dlg_tv.setBackgroundColor(Color.WHITE);
        dlg_tv.setTextColor(Color.BLACK);
//        dlg_tv.setTextSize(32);

        WebView dlg_wb = (WebView) dialog.findViewById(R.id.help_view_help);

        dlg_wb.loadUrl("file:///android_asset/" + help_msg);
        dlg_wb.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        dlg_wb.getSettings().setBuiltInZoomControls(true);
        dlg_wb.setInitialScale(0);

        dlg_tv.setText(title);

        CommonDialog.setDlgBoxSizeLimit(dialog, false);

        dialog.show();
    }

    ;

    private void setSyncTaskFieldHelpListener(Dialog dialog, SyncTaskItem sti) {
        final ImageButton help_sync_option = (ImageButton) dialog.findViewById(R.id.edit_profile_sync_help);
        help_sync_option.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
//		final ImageButton help_folder_dir = (ImageButton)dialog.findViewById(R.id.edit_sync_folder_dlg_directory_help);
//		final ImageButton help_folder_share = (ImageButton)dialog.findViewById(R.id.edit_sync_folder_dlg_share_help);
//		final ImageButton help_folder_logon = (ImageButton)dialog.findViewById(R.id.edit_sync_folder_dlg_logon_help);
        help_sync_option.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFieldHelp(mContext.getString(R.string.msgs_help_sync_task_title),
                        mContext.getString(R.string.msgs_help_sync_task_file));
            }
        });
    }

    ;

    private void setSyncFolderFieldHelpListener(Dialog dialog, final String f_type) {
        final ImageButton help_sync_folder = (ImageButton) dialog.findViewById(R.id.edit_sync_folder_dlg_help);
        help_sync_folder.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
//		Log.v("","f_type="+f_type);
        help_sync_folder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (f_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
                    showFieldHelp(mContext.getString(R.string.msgs_help_sync_folder_internal_title),
                            mContext.getString(R.string.msgs_help_sync_folder_internal_file));
                } else if (f_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                    showFieldHelp(mContext.getString(R.string.msgs_help_sync_folder_sdcard_title),
                            mContext.getString(R.string.msgs_help_sync_folder_sdcard_file));
                } else if (f_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                    showFieldHelp(mContext.getString(R.string.msgs_help_sync_folder_smb_title),
                            mContext.getString(R.string.msgs_help_sync_folder_smb_file));
                } else if (f_type.equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                    showFieldHelp(mContext.getString(R.string.msgs_help_sync_folder_zip_title),
                            mContext.getString(R.string.msgs_help_sync_folder_zip_file));
                }
            }
        });
    }

    private void checkSyncTaskOkButtonEnabled(Dialog dialog, String type, SyncTaskItem n_sti, TextView dlg_msg) {
        final Button btn_ok = (Button) dialog.findViewById(R.id.edit_profile_sync_dlg_btn_ok);
        String t_name_msg = checkTaskNameValidity(type, n_sti.getSyncTaskName(), dlg_msg, btn_ok);
        boolean error_detected = false;
        if (t_name_msg.equals("")) {
            String e_msg = checkMasterTargetCombination(dialog, n_sti);
            if (!e_msg.equals("")) {
                btn_ok.setEnabled(false);
                setDialogMsg(dlg_msg, e_msg);
            } else {
                final Spinner spinnerSyncWifiStatus = (Spinner) mDialog.findViewById(R.id.edit_sync_task_option_spinner_wifi_status);
                if (spinnerSyncWifiStatus.getSelectedItem().toString().equals(mContext.getString(R.string.msgs_main_sync_profile_dlg_wifi_option_wifi_connect_specific_ap))) {
                    if (n_sti.getSyncWifiConnectionWhiteList().size() == 0) {
                        btn_ok.setEnabled(false);
                        setDialogMsg(dlg_msg, mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_ap_not_specified));
                        error_detected = true;
                    }
                }
                if (!error_detected) {
                    String filter_msg = "";
                    filter_msg = checkFilter(dialog, type, n_sti);
                    if (filter_msg.equals("")) {
                        String s_msg = checkStorageStatus(dialog, type, n_sti);
                        if (s_msg.equals("")) {
                            setDialogMsg(dlg_msg, s_msg);
                            if (!isSyncTaskChanged(n_sti, mCurrentSyncTaskItem))
                                btn_ok.setEnabled(true);
                            else btn_ok.setEnabled(false);
                        } else {
                            setDialogMsg(dlg_msg, s_msg);
                            if (!isSyncTaskChanged(n_sti, mCurrentSyncTaskItem))
                                btn_ok.setEnabled(true);
                            else btn_ok.setEnabled(false);
                        }
                    } else {
                        setDialogMsg(dlg_msg, filter_msg);
                        btn_ok.setEnabled(false);
                    }
                }
//				Log.v("","fm="+filter_msg);
            }
        } else {
            setDialogMsg(dlg_msg, t_name_msg);
            btn_ok.setEnabled(false);
        }
    }

    private boolean isSyncTaskChanged(SyncTaskItem curr_stli, SyncTaskItem org_stli) {
        SyncTaskItem new_stli = buildSyncTaskListItem(mDialog, curr_stli);
        String n_type = new_stli.getSyncTaskType();
        String c_type = mCurrentSyncTaskItem.getSyncTaskType();

        boolean result = new_stli.isSame(org_stli);
        return result;
    }

    private String checkFilter(Dialog dialog, String type, SyncTaskItem n_sti) {
        String result = "";
        final CheckedTextView ctvSyncFileTypeSpecific = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_specific);
        final CheckedTextView ctvSyncFileTypeAudio = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_audio);
        final CheckedTextView ctvSyncFileTypeImage = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_image);
        final CheckedTextView ctvSyncFileTypeVideo = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_file_type_video);
        final CheckedTextView ctvSyncSpecificSubDir = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_sub_directory_specific);

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
                }
            }
        }
        return result;
    }

    private String checkStorageStatus(Dialog dialog, String type, SyncTaskItem n_sti) {
        String emsg = "";
        if (n_sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            if (mGp.safMgr.getSdcardDirectory().equals(SafFileManager.UNKNOWN_SDCARD_DIRECTORY)) {
                if (SafFileManager.isSdcardMountPointExisted(mContext, mGp.settingDebugLevel > 0)) {
                    emsg = mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_please_edit_master);
                } else {
                    emsg = mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted);
                }
            }
        }
        if (emsg.equals("")) {
            if (n_sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                if (mGp.safMgr.getSdcardDirectory().equals(SafFileManager.UNKNOWN_SDCARD_DIRECTORY)) {
                    if (SafFileManager.isSdcardMountPointExisted(mContext, mGp.settingDebugLevel > 0)) {
                        emsg = mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_auth_please_edit_target);
                    } else {
                        emsg = mContext.getString(R.string.msgs_main_sync_profile_dlg_sync_folder_sdcard_not_mounted);
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

    private String checkMasterTargetCombination(Dialog dialog, SyncTaskItem sti) {
        String result = "";
        final CheckedTextView ctvSyncSpecificSubDir = (CheckedTextView) mDialog.findViewById(R.id.sync_filter_sub_directory_specific);
        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
                if (sti.getMasterDirectoryName().toLowerCase().equals(sti.getTargetDirectoryName().toLowerCase()) &&
                        sti.getMasterLocalMountPoint().toLowerCase().equals(sti.getTargetLocalMountPoint().toLowerCase())) {
//					if ((sti.isMasterFolderUseInternalUsbFolder() && sti.isTargetFolderUseInternalUsbFolder()) ||
//							(!sti.isMasterFolderUseInternalUsbFolder() && !sti.isTargetFolderUseInternalUsbFolder()))
                    result = mContext.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_cobination_internal);
                } else {
                    if (sti.getMasterDirectoryName().equals("") || sti.getTargetDirectoryName().equals("")) {
                        if (sti.getDirFilter().size() == 0 || !ctvSyncSpecificSubDir.isChecked()) {
                            if (sti.getTargetDirectoryName().toLowerCase().startsWith(sti.getMasterDirectoryName().toLowerCase()) &&
                                    sti.getMasterLocalMountPoint().toLowerCase().equals(sti.getTargetLocalMountPoint().toLowerCase())) {

//								if ((sti.isMasterFolderUseInternalUsbFolder() && sti.isTargetFolderUseInternalUsbFolder()) ||
//										(!sti.isMasterFolderUseInternalUsbFolder() && !sti.isTargetFolderUseInternalUsbFolder())) {
                                result = mContext.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_cobination_same_dir);
//								}
                            }
                        }
                    }
                }
            }
//		} else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
//			if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
//				if (sti.getMasterDirectoryName().toLowerCase().equals(sti.getTargetDirectoryName().toLowerCase()))
//					result=mContext.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_cobination_usb);
//			}
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                if (sti.getMasterDirectoryName().toLowerCase().equals(sti.getTargetDirectoryName().toLowerCase())) {
                    result = mContext.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_cobination_internal);
                } else {
                    if (sti.getMasterDirectoryName().equals("") || sti.getTargetDirectoryName().equals("")) {
                        if (sti.getDirFilter().size() == 0 || !ctvSyncSpecificSubDir.isChecked()) {
                            if (sti.getTargetDirectoryName().toLowerCase().startsWith(sti.getMasterDirectoryName().toLowerCase())) {
                                result = mContext.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_cobination_same_dir);
                            }
                        }
                    }
                }
            }
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                if (sti.getMasterSmbAddr().equalsIgnoreCase(sti.getTargetSmbAddr()) &&
                        sti.getMasterSmbDomain().equalsIgnoreCase(sti.getTargetSmbDomain()) &&
                        sti.getMasterSmbHostName().equalsIgnoreCase(sti.getTargetSmbHostName()) &&
                        sti.getMasterRemoteSmbShareName().equalsIgnoreCase(sti.getTargetSmbShareName()) &&
                        sti.getMasterDirectoryName().equalsIgnoreCase(sti.getTargetDirectoryName())) {
                    result = mContext.getString(R.string.msgs_main_sync_profile_dlg_invalid_master_target_cobination_same_dir);
                }
            }
        }
//		Log.v("","mt="+sti.getMasterFolderType()+", md="+sti.getMasterDirectoryName()+", tt="+sti.getTargetFolderType()+", td="+sti.getTargetDirectoryName());
//		Log.v("","result="+result);
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
        public String folder_removable_uuid = "";

        public boolean zip_file_use_sdcard = false;
        public String zip_comp_level = "";
        public String zip_enc_method = "";
        public String zip_file_name = "";
        public String zip_file_password = "";

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
                    folder_removable_uuid.equals(comp.folder_removable_uuid) &&
                    (folder_remote_use_pswd == comp.folder_remote_use_pswd)  &&
                    (zip_file_use_sdcard == comp.zip_file_use_sdcard)  &&
                    zip_comp_level.equals(comp.zip_comp_level) &&
                    zip_enc_method.equals(comp.zip_enc_method) &&
                    zip_file_name.equals(comp.zip_file_name) &&
                    zip_file_password.equals(comp.zip_file_password)) result = true;
            } else {
                if (folder_type.equals(comp.folder_type) &&
                    folder_directory.equals(comp.folder_directory) &&
                    folder_mountpoint.equals(comp.folder_mountpoint) &&
                    folder_remote_user.equals(comp.folder_remote_user) &&
                    folder_remote_pswd.equals(comp.folder_remote_pswd) &&
                    folder_remote_domain.equals(comp.folder_remote_domain) &&
                    folder_remote_addr.equals(comp.folder_remote_addr) &&
                    folder_remote_host.equals(comp.folder_remote_host) &&
                    folder_remote_share.equals(comp.folder_remote_share) &&
                    folder_remote_port.equals(comp.folder_remote_port) &&
                    folder_smb_protocol.equals(comp.folder_smb_protocol) &&
                    folder_removable_uuid.equals(comp.folder_removable_uuid) &&
                    (folder_remote_use_pswd == comp.folder_remote_use_pswd)  &&
                    (zip_file_use_sdcard == comp.zip_file_use_sdcard)  &&
                    (folder_smb_ipc_enforced==comp.folder_smb_ipc_enforced) &&
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
            objectOutput.writeUTF(folder_removable_uuid);

            objectOutput.writeBoolean(zip_file_use_sdcard);
            objectOutput.writeUTF(zip_comp_level);
            objectOutput.writeUTF(zip_enc_method);
            objectOutput.writeUTF(zip_file_name);
            objectOutput.writeUTF(zip_file_password);
        }
    }
}
