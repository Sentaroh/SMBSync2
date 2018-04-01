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

import static com.sentaroh.android.SMBSync2.Constants.*;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sentaroh.android.Utilities.Base64Compat;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.EncryptUtil;
import com.sentaroh.android.Utilities.EncryptUtil.CipherParms;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities.ThreadCtrl;
import com.sentaroh.android.Utilities.ContextMenu.CustomContextMenu;
import com.sentaroh.android.Utilities.Dialog.DialogBackKeyListener;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistAdapter;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistItem;
import com.sentaroh.android.Utilities.Widget.CustomTextView;

public class SyncTaskUtil {

    //	private CustomContextMenu ccMenu=null;
    private String smbUser, smbPass;

    private Context mContext;

    private SyncUtil util;

    private ArrayList<PreferenceParmListIItem>
            importedSettingParmList = new ArrayList<PreferenceParmListIItem>();

    private CommonDialog commonDlg = null;
    private GlobalParameters mGp = null;
    private FragmentManager mFragMgr = null;

    SyncTaskUtil(SyncUtil mu, Context c,
                 CommonDialog cd, CustomContextMenu ccm, GlobalParameters gp, FragmentManager fm) {
        mContext = c;
        mGp = gp;
        util = mu;
        commonDlg = cd;
//		ccMenu=ccm;
        mFragMgr = fm;
    }

    public void importSyncTaskListDlg(final NotifyEvent p_ntfy) {

        importedSettingParmList.clear();

        NotifyEvent ntfy = new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                final String fpath = (String) o[0];

                NotifyEvent ntfy_pswd = new NotifyEvent(mContext);
                ntfy_pswd.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        mGp.profilePassword = (String) o[0];
                        AdapterSyncTask tfl = null;
                        if (isSyncTaskListFileOldFormat(fpath)) {
                            tfl = new AdapterSyncTask(mContext, R.layout.sync_task_item_view,
                                    ImportOldProfileList.importOldProfileList(mGp, fpath), mGp);
                        } else {
                            tfl = new AdapterSyncTask(mContext, R.layout.sync_task_item_view,
                                    createSyncTaskListFromFile(mContext, mGp, util, true, fpath, importedSettingParmList), mGp);
                        }
                        if (tfl.getCount() > 0) {
                            selectImportProfileItem(tfl, p_ntfy);
                        } else {
                            commonDlg.showCommonDialog(false, "W",
                                    mContext.getString(R.string.msgs_export_import_profile_no_import_items), "", null);
                            p_ntfy.notifyToListener(false, null);
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                if (isSyncTaskListFileEncrypted(fpath)) {
                    promptPasswordForImport(fpath, ntfy_pswd);
                } else {
                    ntfy_pswd.notifyToListener(true, new Object[]{""});
                }
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        commonDlg.fileSelectorFileOnlySelectWithCreate(true,
                mGp.internalRootDirectory, "/" + APPLICATION_TAG, "profile.txt", mContext.getString(R.string.msgs_select_import_file), ntfy);
    }

    private boolean isSyncTaskListFileOldFormat(String fpath) {
        boolean result = false;

        File lf = new File(fpath);
        if (lf.exists() && lf.canRead()) {
            try {
                BufferedReader br;
                br = new BufferedReader(new FileReader(fpath), 8192);
                if (br!=null) {
                    String dec_str = "";
                    String pl = br.readLine();
                    String enc_str = pl.substring(6);
                    if (enc_str.startsWith("ENC")) {
                        pl = br.readLine();
                        enc_str = pl.substring(6);
                        byte[] enc_array = Base64Compat.decode(enc_str, Base64Compat.NO_WRAP);
                        CipherParms cp = EncryptUtil.initDecryptEnv(mGp.profileKeyPrefix + mGp.profilePassword);
                        dec_str = EncryptUtil.decrypt(enc_array, cp);
                        if (dec_str == null) {
                            CipherParms cp_old = EncryptUtil.initDecryptEnv(mGp.profileKeyPrefixOld + mGp.profilePassword);
                            dec_str = EncryptUtil.decrypt(enc_array, cp_old);
                        }
                    } else {
                        pl = br.readLine();
                        if (pl.length()>15) dec_str = pl.substring(6);
                        else dec_str="";
                    }
                    if (!dec_str.equals("")) {
                        String[] parm = dec_str.split("\t");
                        result = (parm[0].equals("Default") && parm[1].equals("S"));
                    }
                    br.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//		Log.v("","result="+result);
        return result;
    }

    static private boolean isSyncTaskListFileEncrypted(String fpath) {
        boolean result = false;
        File lf = new File(fpath);
        if (lf.exists() && lf.canRead()) {
            try {
                BufferedReader br;
                br = new BufferedReader(new FileReader(fpath), 8192);
                String pl = br.readLine();
                if (pl != null) {
                    if (pl.substring(6).startsWith(SMBSYNC2_PROF_ENC)) result = true;
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void promptPasswordForImport(final String fpath,
                                        final NotifyEvent ntfy_pswd) {

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.password_input_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.password_input_dlg_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.password_input_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.password_input_title);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);

        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.password_input_msg);
        final CheckedTextView ctv_protect = (CheckedTextView) dialog.findViewById(R.id.password_input_ctv_protect);
        final Button btn_ok = (Button) dialog.findViewById(R.id.password_input_ok_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.password_input_cancel_btn);
        final EditText et_password = (EditText) dialog.findViewById(R.id.password_input_password);
        final EditText et_confirm = (EditText) dialog.findViewById(R.id.password_input_password_confirm);
        et_confirm.setVisibility(EditText.GONE);
        btn_ok.setText(mContext.getString(R.string.msgs_export_import_pswd_btn_ok));
        ctv_protect.setVisibility(CheckedTextView.GONE);

        dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_password_required));

        CommonDialog.setDlgBoxSizeCompact(dialog);

        btn_ok.setEnabled(false);
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                if (arg0.length() > 0) btn_ok.setEnabled(true);
                else btn_ok.setEnabled(false);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });

        //OK button
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String passwd = et_password.getText().toString();
                BufferedReader br;
                String pl;
                boolean pswd_invalid = true;
                try {
                    br = new BufferedReader(new FileReader(fpath), 8192);
                    pl = br.readLine();
                    if (pl != null) {
                        String enc_str = "";
                        if (pl.substring(6).startsWith(SMBSYNC2_PROF_ENC)) {
                            enc_str = pl.substring(6).replace(SMBSYNC2_PROF_ENC, "");
                        }
//						Log.v("","pl="+pl.substring(6));
//						Log.v("","enc="+enc_str);
                        if (!enc_str.equals("")) {
                            byte[] enc_array = Base64Compat.decode(enc_str, Base64Compat.NO_WRAP);
                            String dec_str = "";
                            CipherParms cp = EncryptUtil.initDecryptEnv(mGp.profileKeyPrefix + passwd);
                            dec_str = EncryptUtil.decrypt(enc_array, cp);
//							Log.v("","dec1="+dec_str);
                            if (!SMBSYNC2_PROF_ENC.equals(dec_str)) {
                                CipherParms cp_old = EncryptUtil.initDecryptEnv(mGp.profileKeyPrefixOld + passwd);
                                dec_str = EncryptUtil.decrypt(enc_array, cp_old);
                            }
//							Log.v("","dec2="+dec_str);
                            if (!SMBSYNC2_PROF_ENC.equals(dec_str)) {
                                dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_invalid_password));
                            } else {
                                pswd_invalid = false;
                            }
                        }
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!pswd_invalid) {
                    dialog.dismiss();
                    ntfy_pswd.notifyToListener(true, new Object[]{passwd});
                }
            }
        });
        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                ntfy_pswd.notifyToListener(false, null);
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btn_cancel.performClick();
            }
        });
        dialog.show();

    }

    public void promptPasswordForExport(final String fpath, final NotifyEvent ntfy_pswd) {

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.password_input_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.password_input_dlg_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.password_input_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.password_input_title);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);

        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.password_input_msg);
        final CheckedTextView ctv_protect = (CheckedTextView) dialog.findViewById(R.id.password_input_ctv_protect);
        final Button btn_ok = (Button) dialog.findViewById(R.id.password_input_ok_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.password_input_cancel_btn);
        final EditText et_password = (EditText) dialog.findViewById(R.id.password_input_password);
        final EditText et_confirm = (EditText) dialog.findViewById(R.id.password_input_password_confirm);

        dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_specify_password));

        CommonDialog.setDlgBoxSizeCompact(dialog);

        ctv_protect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctv_protect.toggle();
                boolean isChecked = ctv_protect.isChecked();
                setPasswordFieldVisibility(isChecked, et_password, et_confirm, btn_ok, dlg_msg);
            }
        });

        ctv_protect.setChecked(mGp.settingExportedProfileEncryptRequired);
        setPasswordFieldVisibility(mGp.settingExportedProfileEncryptRequired, et_password, et_confirm, btn_ok, dlg_msg);

        et_password.setEnabled(true);
        et_confirm.setEnabled(false);
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                btn_ok.setEnabled(false);
                setPasswordPromptOkButton(et_password, et_confirm, btn_ok, dlg_msg);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });

        et_confirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                btn_ok.setEnabled(false);
                setPasswordPromptOkButton(et_password, et_confirm, btn_ok, dlg_msg);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });

        //OK button
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String passwd = et_password.getText().toString();
                if ((ctv_protect.isChecked() && !mGp.settingExportedProfileEncryptRequired) ||
                        (!ctv_protect.isChecked() && mGp.settingExportedProfileEncryptRequired)) {
                    mGp.settingExportedProfileEncryptRequired = ctv_protect.isChecked();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                    prefs.edit().putBoolean(mContext.getString(R.string.settings_exported_profile_encryption),
                            ctv_protect.isChecked()).commit();
                }
                if (!ctv_protect.isChecked()) {
                    dialog.dismiss();
                    ntfy_pswd.notifyToListener(true, new Object[]{""});
                } else {
                    if (!passwd.equals(et_confirm.getText().toString())) {
                        //Unmatch
                        dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_unmatched_confirm_pswd));
                    } else {
                        dialog.dismiss();
                        ntfy_pswd.notifyToListener(true, new Object[]{passwd});
                    }
                }
            }
        });

        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                ntfy_pswd.notifyToListener(false, null);
            }
        });

        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btn_cancel.performClick();
            }
        });
        dialog.show();

    }

    private void setPasswordFieldVisibility(boolean isChecked, EditText et_password,
                                            EditText et_confirm, Button btn_ok, TextView dlg_msg) {
        if (isChecked) {
            et_password.setVisibility(EditText.VISIBLE);
            et_confirm.setVisibility(EditText.VISIBLE);
            setPasswordPromptOkButton(et_password, et_confirm, btn_ok, dlg_msg);
        } else {
            dlg_msg.setText("");
            et_password.setVisibility(EditText.GONE);
            et_confirm.setVisibility(EditText.GONE);
            btn_ok.setEnabled(true);
        }
    }

    private void setPasswordPromptOkButton(EditText et_passwd, EditText et_confirm,
                                           Button btn_ok, TextView dlg_msg) {
        String password = et_passwd.getText().toString();
        String confirm = et_confirm.getText().toString();
        if (password.length() > 0 && et_confirm.getText().length() == 0) {
            dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_unmatched_confirm_pswd));
            et_confirm.setEnabled(true);
        } else if (password.length() > 0 && et_confirm.getText().length() > 0) {
            et_confirm.setEnabled(true);
            if (!password.equals(confirm)) {
                btn_ok.setEnabled(false);
                dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_unmatched_confirm_pswd));
            } else {
                btn_ok.setEnabled(true);
                dlg_msg.setText("");
            }
        } else if (password.length() == 0 && confirm.length() == 0) {
            btn_ok.setEnabled(false);
            dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_specify_password));
            et_passwd.setEnabled(true);
            et_confirm.setEnabled(false);
        } else if (password.length() == 0 && confirm.length() > 0) {
            dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_unmatched_confirm_pswd));
        }

    }

    private void selectImportProfileItem(final AdapterSyncTask tfl, final NotifyEvent p_ntfy) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.export_import_profile_dlg);
        dialog.setCanceledOnTouchOutside(false);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.export_import_profile_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        ArrayList<AdapterExportImportTask.ExportImportListItem> eipl = new ArrayList<AdapterExportImportTask.ExportImportListItem>();

        for (int i = 0; i < tfl.getCount(); i++) {
            SyncTaskItem pl = tfl.getItem(i);
            AdapterExportImportTask.ExportImportListItem eipli = new AdapterExportImportTask.ExportImportListItem();
            eipli.isChecked = true;
            eipli.item_name = pl.getSyncTaskName();
            eipl.add(eipli);
        }
        final AdapterExportImportTask imp_list_adapt =
                new AdapterExportImportTask(mContext, R.layout.export_import_profile_list_item, eipl);

        ListView lv = (ListView) dialog.findViewById(R.id.export_import_profile_listview);
        lv.setAdapter(imp_list_adapt);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.export_import_profile_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.export_import_profile_title);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);
        title.setText(mContext.getString(R.string.msgs_export_import_profile_title));
        LinearLayout ll_filelist = (LinearLayout) dialog.findViewById(R.id.export_import_profile_file_list);
        ll_filelist.setVisibility(LinearLayout.GONE);
        final Button ok_btn = (Button) dialog.findViewById(R.id.export_import_profile_dlg_btn_ok);
        Button cancel_btn = (Button) dialog.findViewById(R.id.export_import_profile_dlg_btn_cancel);

        final Button rb_select_all = (Button) dialog.findViewById(R.id.export_import_profile_list_select_all);
        final Button rb_unselect_all = (Button) dialog.findViewById(R.id.export_import_profile_list_unselect_all);
        final CheckedTextView ctv_reset_profile = (CheckedTextView) dialog.findViewById(R.id.export_import_profile_list_ctv_reset_profile);
        final CheckedTextView ctv_import_settings = (CheckedTextView) dialog.findViewById(R.id.export_import_profile_list_ctv_import_settings);
        final CheckedTextView ctv_import_schedule = (CheckedTextView) dialog.findViewById(R.id.export_import_profile_list_ctv_import_schedule);

        if (importedSettingParmList.size() == 0) {
            ctv_import_settings.setVisibility(CheckedTextView.GONE);
            ctv_import_schedule.setVisibility(CheckedTextView.GONE);
        }

        ctv_reset_profile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
                setImportOkBtnEnabled(ctv_reset_profile, ctv_import_settings, ctv_import_schedule, imp_list_adapt, ok_btn);
            }
        });

        ctv_import_settings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
                setImportOkBtnEnabled(ctv_reset_profile, ctv_import_settings, ctv_import_schedule, imp_list_adapt, ok_btn);
            }
        });

        ctv_import_schedule.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
                setImportOkBtnEnabled(ctv_reset_profile, ctv_import_settings, ctv_import_schedule, imp_list_adapt, ok_btn);
            }
        });

        ctv_import_settings.setChecked(true);
        ctv_import_schedule.setChecked(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3) {
                setImportOkBtnEnabled(ctv_reset_profile, ctv_import_settings, ctv_import_schedule, imp_list_adapt, ok_btn);
            }
        });

        rb_select_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < imp_list_adapt.getCount(); i++)
                    imp_list_adapt.getItem(i).isChecked = true;
                ctv_import_settings.setChecked(true);
                ctv_import_schedule.setChecked(true);
                imp_list_adapt.notifyDataSetChanged();
                ok_btn.setEnabled(true);
            }
        });
        rb_unselect_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < imp_list_adapt.getCount(); i++)
                    imp_list_adapt.getItem(i).isChecked = false;
                ctv_import_settings.setChecked(false);
                ctv_import_schedule.setChecked(false);
                imp_list_adapt.notifyDataSetChanged();
                ok_btn.setEnabled(false);
            }
        });

        NotifyEvent ntfy_ctv_listener = new NotifyEvent(mContext);
        ntfy_ctv_listener.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                setImportOkBtnEnabled(ctv_reset_profile, ctv_import_settings, ctv_import_schedule, imp_list_adapt, ok_btn);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        imp_list_adapt.setCheckButtonListener(ntfy_ctv_listener);


        ok_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (ctv_reset_profile.isChecked()) mGp.syncTaskAdapter.clear();
                importSelectedSyncTaskItem(imp_list_adapt, tfl,
                        ctv_import_settings.isChecked(),
                        ctv_import_schedule.isChecked(),
                        p_ntfy);
                dialog.dismiss();
            }
        });
        cancel_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void setImportOkBtnEnabled(
            final CheckedTextView ctv_reset_profile,
            final CheckedTextView ctv_import_settings,
            final CheckedTextView ctv_import_schedule,
            final AdapterExportImportTask imp_list_adapt,
            final Button ok_btn) {
        if (ctv_import_settings.isChecked() || ctv_import_schedule.isChecked() || imp_list_adapt.isItemSelected())
            ok_btn.setEnabled(true);
        else ok_btn.setEnabled(false);
    }

    private void importSelectedSyncTaskItem(
            final AdapterExportImportTask imp_list_adapt,
            final AdapterSyncTask tfl,
            final boolean import_settings,
            final boolean import_schedule,
            final NotifyEvent p_ntfy) {
        String repl_list = "";
        for (int i = 0; i < imp_list_adapt.getCount(); i++) {
            AdapterExportImportTask.ExportImportListItem eipli = imp_list_adapt.getItem(i);
            if (eipli.isChecked &&
                    getSyncTaskByName(mGp.syncTaskAdapter, eipli.item_name) != null) {
                repl_list += eipli.item_name + "\n";
            }
        }

        NotifyEvent ntfy = new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                String imp_list = "";
                for (int i = 0; i < tfl.getCount(); i++) {
                    SyncTaskItem ipfli = tfl.getItem(i);
                    AdapterExportImportTask.ExportImportListItem eipli = imp_list_adapt.getItem(i);
                    if (eipli.isChecked) {
                        imp_list += ipfli.getSyncTaskName() + "\n";
//						Log.v("","name1="+ipfli.getName()+
//								", result="+getProfile(ipfli.getName(), mGp.syncTaskListAdapter));
                        SyncTaskItem mpfli = getSyncTaskByName(mGp.syncTaskAdapter, ipfli.getSyncTaskName());
                        if (mpfli != null) {
                            mGp.syncTaskAdapter.remove(mpfli);
                            ipfli.setSyncTaskPosition(mpfli.getSyncTaskPosition());
                            mGp.syncTaskAdapter.add(ipfli);
                        } else {
                            ipfli.setSyncTaskPosition(mGp.syncTaskAdapter.getCount());
                            mGp.syncTaskAdapter.add(ipfli);
                        }
                    }
                }
                restoreImportedSystemOption();
                if (import_settings) {
                    restoreImportedSettingParms();
                    imp_list += mContext.getString(R.string.msgs_export_import_profile_setting_parms) + "\n";
                }
                if (import_schedule) {
                    restoreImportedScheduleParms();
                    imp_list += mContext.getString(R.string.msgs_export_import_profile_schedule_parms) + "\n";
                }
                if (imp_list.length() > 0) imp_list += " ";
                mGp.syncTaskAdapter.sort();
                mGp.syncTaskListView.setSelection(0);
                saveSyncTaskList(mGp, mContext, util, mGp.syncTaskAdapter.getArrayList());
                commonDlg.showCommonDialog(false, "I",
                        mContext.getString(R.string.msgs_export_import_profile_import_success),
                        imp_list, null);
                if (import_settings || import_schedule) {
                    boolean[] parm = new boolean[]{import_settings, import_schedule};
                    p_ntfy.notifyToListener(true, new Object[]{parm});
                }
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        if (!repl_list.equals("")) {
            //Confirm
            commonDlg.showCommonDialog(true, "W",
                    mContext.getString(R.string.msgs_export_import_profile_confirm_override),
                    repl_list, ntfy);
        } else {
            ntfy.notifyToListener(true, null);
        }

    }

    private void restoreImportedSystemOption() {
        final ArrayList<PreferenceParmListIItem> spl = importedSettingParmList;

        if (spl.size() == 0) {
            util.addDebugMsg(2, "I", "Import setting parms can not be not found.");
            return;
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Editor pe = prefs.edit();

        if (spl.size() >= 0) {
            for (int i = 0; i < spl.size(); i++) {
                if (spl.get(i).parms_key.startsWith("system_rest")) {
                    restorePreferenceParms(pe, spl.get(i));
                }
            }
            pe.commit();
//			applySettingParms();
        }
    }

    private void restorePreferenceParms(Editor pe, PreferenceParmListIItem pa) {
        if (pa.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_STRING)) {
            pe.putString(pa.parms_key, pa.parms_value);
            util.addDebugMsg(2, "I", "Restored parms=" + pa.parms_key + "=" + pa.parms_value);
        } else if (pa.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_BOOLEAN)) {
            boolean b_val = false;
            if (pa.parms_value.equals("false")) b_val = false;
            else b_val = true;
            pe.putBoolean(pa.parms_key, b_val);
            util.addDebugMsg(2, "I", "Restored parms=" + pa.parms_key + "=" + pa.parms_value);
        } else if (pa.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_INT)) {
            int i_val = 0;
            i_val = Integer.parseInt(pa.parms_value);
            ;
            pe.putInt(pa.parms_key, i_val);
            util.addDebugMsg(2, "I", "Restored parms=" + pa.parms_key + "=" + pa.parms_value);
        } else if (pa.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_LONG)) {
            long i_val = 0;
            i_val = Long.parseLong(pa.parms_value);
            ;
            pe.putLong(pa.parms_key, i_val);
            util.addDebugMsg(2, "I", "Restored parms=" + pa.parms_key + "=" + pa.parms_value);
        }
    }

    private void restoreImportedScheduleParms() {
        final ArrayList<PreferenceParmListIItem> spl = importedSettingParmList;

        if (spl.size() == 0) {
            util.addDebugMsg(2, "I", "Import setting parms can not be not found.");
            return;
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Editor pe = prefs.edit();

        if (spl.size() >= 0) {
            for (int i = 0; i < spl.size(); i++) {
                if (spl.get(i).parms_key.startsWith("schedule")) {
                    restorePreferenceParms(pe, spl.get(i));
                }
            }
            pe.commit();
//			applySettingParms();
        }
    }

    private void restoreImportedSettingParms() {
        final ArrayList<PreferenceParmListIItem> spl = importedSettingParmList;

        if (spl.size() == 0) {
            util.addDebugMsg(2, "I", "Import setting parms can not be not found.");
            return;
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Editor pe = prefs.edit();

        if (spl.size() >= 0) {
            for (int i = 0; i < spl.size(); i++) {
                if (spl.get(i).parms_key.startsWith("settings")) {
                    restorePreferenceParms(pe, spl.get(i));
                }
            }
            pe.commit();
//			applySettingParms();
        }
    }

    public void exportSyncTaskListDlg() {

        NotifyEvent ntfy = new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                final String fpath = (String) o[0];
                NotifyEvent ntfy_pswd = new NotifyEvent(mContext);
                ntfy_pswd.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        mGp.profilePassword = (String) o[0];
                        boolean encrypt_required = false;
                        if (!mGp.profilePassword.equals("")) encrypt_required = true;
                        String fd = fpath.substring(0, fpath.lastIndexOf("/"));
                        String fn = fpath.replace(fd + "/", "");
//		    			Log.v("","fp="+fpath+", fd="+fd+", fn="+fn);
                        exportSyncTaskListToFile(fd, fn, encrypt_required);
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                promptPasswordForExport(fpath, ntfy_pswd);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        commonDlg.fileSelectorFileOnlySelectWithCreate(true,
                mGp.internalRootDirectory, "/" + APPLICATION_TAG, "profile.txt", mContext.getString(R.string.msgs_select_export_file), ntfy);
    }

    public void exportSyncTaskListToFile(final String profile_dir,
                                         final String profile_filename, final boolean encrypt_required) {

        File lf = new File(profile_dir + "/" + profile_filename);
        if (lf.exists()) {
            NotifyEvent ntfy = new NotifyEvent(mContext);
            ntfy.setListener(new NotifyEventListener() {
                @Override
                public void positiveResponse(Context c, Object[] o) {
                    String fp = profile_dir + "/" + profile_filename;
                    String fd = profile_dir;

                    if (saveSyncTaskListToFile(mGp, mContext, util, true, fd, fp,
                            mGp.syncTaskAdapter.getArrayList(), encrypt_required)) {
                        commonDlg.showCommonDialog(false, "I",
                                mContext.getString(R.string.msgs_export_prof_success), "File=" + fp, null);
                        util.addDebugMsg(1, "I", "Profile was exported. fn=" + fp);
                    } else {
                        commonDlg.showCommonDialog(false, "E",
                                mContext.getString(R.string.msgs_export_prof_fail), "File=" + fp, null);
                    }
                }

                @Override
                public void negativeResponse(Context c, Object[] o) {
                }
            });
            commonDlg.showCommonDialog(true, "W",
                    mContext.getString(R.string.msgs_export_prof_title),
                    profile_dir + "/" + profile_filename + " " + mContext.getString(R.string.msgs_override), ntfy);
        } else {
            String fp = profile_dir + "/" + profile_filename;
            String fd = profile_dir;
            if (saveSyncTaskListToFile(mGp, mContext, util, true, fd, fp,
                    mGp.syncTaskAdapter.getArrayList(), encrypt_required)) {
                commonDlg.showCommonDialog(false, "I",
                        mContext.getString(R.string.msgs_export_prof_success),
                        "File=" + fp, null);
                util.addDebugMsg(1, "I", "Profile was exported. fn=" + fp);
            } else {
                commonDlg.showCommonDialog(false, "E",
                        mContext.getString(R.string.msgs_export_prof_fail),
                        "File=" + fp, null);
            }
        }
    }

    static public void setAllSyncTaskToUnchecked(boolean hideCheckBox, AdapterSyncTask pa) {
        pa.setAllItemChecked(false);
        if (hideCheckBox) pa.setShowCheckBox(false);
        pa.notifyDataSetChanged();
    }

    public void setSyncTaskToAuto(GlobalParameters gp) {
        SyncTaskItem item;

//		int pos=gp.profileListView.getFirstVisiblePosition();
//		int posTop=gp.profileListView.getChildAt(0).getTop();
        for (int i = 0; i < gp.syncTaskAdapter.getCount(); i++) {
            item = gp.syncTaskAdapter.getItem(i);
            if (item.isChecked()) {
                item.setSyncTaskAuto(true);
            }
        }

        saveSyncTaskList(mGp, mContext, util, gp.syncTaskAdapter.getArrayList());
        mGp.syncTaskAdapter.notifyDataSetChanged();
        gp.syncTaskAdapter.setNotifyOnChange(true);
//		gp.profileListView.setSelectionFromTop(pos,posTop);
    }

    public void setSyncTaskToManual() {
        SyncTaskItem item;

        int pos = mGp.syncTaskListView.getFirstVisiblePosition();
        int posTop = mGp.syncTaskListView.getChildAt(0).getTop();
        for (int i = 0; i < mGp.syncTaskAdapter.getCount(); i++) {
            item = mGp.syncTaskAdapter.getItem(i);
            if (item.isChecked()) {
                item.setSyncTaskAuto(false);
//				item.setChecked(false);
            }
        }

        saveSyncTaskListToFile(mGp, mContext, util, false, "", "", mGp.syncTaskAdapter.getArrayList(), false);
        mGp.syncTaskAdapter.notifyDataSetChanged();
        mGp.syncTaskAdapter.setNotifyOnChange(true);
        mGp.syncTaskListView.setSelectionFromTop(pos, posTop);
    }

    public void deleteSyncTask(final NotifyEvent p_ntfy) {
        final int[] dpnum = new int[mGp.syncTaskAdapter.getCount()];
        String dpmsg = "";

        for (int i = 0; i < mGp.syncTaskAdapter.getCount(); i++) {
            if (mGp.syncTaskAdapter.getItem(i).isChecked()) {
                dpmsg = dpmsg + mGp.syncTaskAdapter.getItem(i).getSyncTaskName() + "\n";
                dpnum[i] = i;
            } else dpnum[i] = -1;
        }

        NotifyEvent ntfy = new NotifyEvent(mContext);
        // set commonDlg.showCommonDialog response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                ArrayList<SyncTaskItem> dpItemList = new ArrayList<SyncTaskItem>();
                int pos = mGp.syncTaskListView.getFirstVisiblePosition();
                for (int i = 0; i < dpnum.length; i++) {
                    if (dpnum[i] != -1)
                        dpItemList.add(mGp.syncTaskAdapter.getItem(dpnum[i]));
                }
                for (int i = 0; i < dpItemList.size(); i++)
                    mGp.syncTaskAdapter.remove(dpItemList.get(i));

                saveSyncTaskList(mGp, mContext, util, mGp.syncTaskAdapter.getArrayList());

                mGp.syncTaskAdapter.setNotifyOnChange(true);
                mGp.syncTaskListView.setSelection(pos);

                if (mGp.syncTaskAdapter.isEmptyAdapter()) {
                    mGp.syncTaskAdapter.setShowCheckBox(false);
                }

                SyncTaskUtil.setAllSyncTaskToUnchecked(true, mGp.syncTaskAdapter);

                p_ntfy.notifyToListener(true, null);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
                p_ntfy.notifyToListener(false, null);
            }
        });
        commonDlg.showCommonDialog(true, "W",
                mContext.getString(R.string.msgs_delete_following_profile), dpmsg, ntfy);
    }

    public void showSelectSdcardMsg(final NotifyEvent ntfy) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.show_select_sdcard_dlg);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.show_select_sdcard_dlg_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.show_select_sdcard_dlg_title);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);

        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.show_select_sdcard_dlg_msg);
        String msg = "";
        if (Build.VERSION.SDK_INT >= 23)
            msg = mContext.getString(R.string.msgs_main_external_sdcard_select_required_select_msg_api23);
        else if (Build.VERSION.SDK_INT >= 21)
            msg = mContext.getString(R.string.msgs_main_external_sdcard_select_required_select_msg_api21);
        else
            msg = mContext.getString(R.string.msgs_main_external_sdcard_select_required_select_msg_api21);
        dlg_msg.setText(msg);

        final ImageView func_view = (ImageView) dialog.findViewById(R.id.show_select_sdcard_dlg_image);


        try {
            String fn = "";
            if (Build.VERSION.SDK_INT >= 23)
                fn = mContext.getString(R.string.msgs_main_external_sdcard_select_required_select_msg_file_api23);
            else if (Build.VERSION.SDK_INT >= 21)
                fn = mContext.getString(R.string.msgs_main_external_sdcard_select_required_select_msg_file_api21);
            else
                fn = mContext.getString(R.string.msgs_main_external_sdcard_select_required_select_msg_file_api21);
            InputStream is = mContext.getResources().getAssets().open(fn);
            Bitmap bm = BitmapFactory.decodeStream(is);
            func_view.setImageBitmap(bm);
        } catch (IOException e) {
            /* 例外処理 */
        }

        final Button btnOk = (Button) dialog.findViewById(R.id.show_select_sdcard_dlg_btn_ok);
        final Button btnCancel = (Button) dialog.findViewById(R.id.show_select_sdcard_dlg_btn_cancel);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        // OKボタンの指定
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                ntfy.notifyToListener(true, null);
            }
        });
        // Cancelボタンの指定
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                ntfy.notifyToListener(false, null);
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btnCancel.performClick();
            }
        });

        dialog.show();

    }

    public boolean isExternalSdcardUsedByOutput() {
        boolean result = false;
        for (SyncTaskItem pli : mGp.syncTaskAdapter.getArrayList()) {
//			Log.v("","name="+pli.getProfileName()+", type="+pli.getProfileType()+", act="+pli.isProfileActive());
            if (pli.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                result = true;
                break;
            }
        }
        return result;
    }

//	public boolean isUsbSafUsedByOutput() {
//		boolean result=false;
//		for(SyncTaskItem pli:mGp.syncTaskAdapter.getArrayList()) {
////			Log.v("","name="+pli.getProfileName()+", type="+pli.getProfileType()+", act="+pli.isProfileActive());
//			if (pli.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
//				result=true;
//				break;
//			}
//		}
//		return result;
//	};

    public void testSmbLogonDlg(final String host, final String addr, final String port, final String share,
                                RemoteAuthInfo ra, final NotifyEvent p_ntfy) {
        final ThreadCtrl tc = new ThreadCtrl();
        tc.setEnabled();
        tc.setThreadResultSuccess();

        final Dialog dialog=showProgressSpinIndicator(mContext);

        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                tc.setDisabled();//disableAsyncTask();
                util.addDebugMsg(1, "W", "Logon is cancelled.");
            }
        });
        dialog.show();

        Thread th = new Thread() {
            @Override
            public void run() {
                util.addDebugMsg(1, "I", "Test logon started, host=" + host + ", addr=" + addr + ", port=" + port + ", user=" + ra.smb_user_name);
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        dialog.dismiss();
                        String err_msg = (String) o[0];
                        if (tc.isEnabled()) {
                            if (err_msg != null) {
                                commonDlg.showCommonDialog(false, "E", mContext.getString(R.string.msgs_remote_profile_dlg_logon_error)
                                        , err_msg, null);
                                if (p_ntfy != null) p_ntfy.notifyToListener(false, null);
                            } else {
                                commonDlg.showCommonDialog(false, "I", mContext.getString(R.string.msgs_remote_profile_dlg_logon_success), "", null);
                                if (p_ntfy != null) p_ntfy.notifyToListener(true, null);
                            }
                        } else {
//                            commonDlg.showCommonDialog(false, "I", mContext.getString(R.string.msgs_remote_profile_dlg_logon_cancel), "", null);
//                            if (p_ntfy != null) p_ntfy.notifyToListener(true, null);
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });

                if (host.equals("")) {
                    boolean reachable = false;
                    if (port.equals("")) {
                        if (JcifsUtil.isIpAddressAndPortConnected(addr, 139, 3500) || JcifsUtil.isIpAddressAndPortConnected(addr, 445, 3500)) {
                            reachable = true;
                        }
                    } else {
                        reachable = JcifsUtil.isIpAddressAndPortConnected(addr, Integer.parseInt(port), 3500);
                    }
                    if (reachable) {
                        testSmbAuth(addr, port, share, ra, ntfy);
                    } else {
                        util.addDebugMsg(1, "I", "Test logon failed, remote server not connected");
                        String unreachble_msg = "";
                        if (port.equals("")) {
                            unreachble_msg = String.format(mContext.getString(R.string.msgs_mirror_smb_addr_not_connected), addr);
                        } else {
                            unreachble_msg = String.format(mContext.getString(R.string.msgs_mirror_smb_addr_not_connected_with_port), addr, port);
                        }
                        ntfy.notifyToListener(true, new Object[]{unreachble_msg});
                    }
                } else {
                    String lbl=ra.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY)? JcifsFile.JCIFS_LEVEL_JCIFS1:JcifsFile.JCIFS_LEVEL_JCIFS2;
                    String ipAddress = JcifsUtil.getSmbHostIpAddressFromName(lbl, host);
                    if (ipAddress == null) {
                        try {
                            InetAddress[] addr_list = Inet4Address.getAllByName(host);
                            for (InetAddress item : addr_list) {
//								Log.v("","addr="+item.getHostAddress()+", l="+item.getAddress().length);
                                if (item.getAddress().length == 4) {
                                    ipAddress = item.getHostAddress();
                                }
                            }
                        } catch (UnknownHostException e) {
//							e.printStackTrace();
                        }
                    }
                    if (ipAddress != null) testSmbAuth(ipAddress, port, share, ra, ntfy);
                    else {
                        util.addDebugMsg(1, "I", "Test logon failed, remote server not connected");
                        String unreachble_msg = "";
                        unreachble_msg = mContext.getString(R.string.msgs_mirror_smb_name_not_found) + host;
                        ntfy.notifyToListener(true, new Object[]{unreachble_msg});
                    }
                }
            }
        };
        th.start();
    }

    private void testSmbAuth(final String host, String port, String share,
                             RemoteAuthInfo ra, final NotifyEvent ntfy) {
        final UncaughtExceptionHandler defaultUEH = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Thread.currentThread().setUncaughtExceptionHandler(defaultUEH);
                ex.printStackTrace();
                StackTraceElement[] st = ex.getStackTrace();
                String st_msg = "";
                for (int i = 0; i < st.length; i++) {
                    st_msg += "\n at " + st[i].getClassName() + "." +
                            st[i].getMethodName() + "(" + st[i].getFileName() +
                            ":" + st[i].getLineNumber() + ")";
                }
                String end_msg = ex.toString() + st_msg;
                ntfy.notifyToListener(true, new Object[]{end_msg});
            }
        });

        String err_msg = null, url = "";

        if (port.equals("")) url = "smb://" + host + "/IPC$/";//+share+"/";
        else url = "smb://" + host + ":" + port + "/IPC%/";//+share+"/";

        JcifsAuth auth=null;
        if (ra.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY)) {
            auth=new JcifsAuth(JcifsFile.JCIFS_LEVEL_JCIFS1, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password);
        } else {
            auth=new JcifsAuth(JcifsFile.JCIFS_LEVEL_JCIFS2, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password, ra.smb_ipc_signing_enforced);
        }
        try {
            JcifsFile sf = new JcifsFile(url, auth);
            sf.connect();
            util.addDebugMsg(1, "I", "Test logon completed, host=" + host + ", port=" + port+", user="+ra.smb_user_name);
        } catch (JcifsException e) {
            String[] e_msg = JcifsUtil.analyzeNtStatusCode(e, url, ra.smb_user_name);
            err_msg = e_msg[0];
            util.addDebugMsg(1, "I", "Test logon failed." + "\n" + e_msg[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            err_msg = e.getMessage();
            util.addDebugMsg(1, "I", "Test logon failed." + "\n" + err_msg);
        }
        Thread.currentThread().setUncaughtExceptionHandler(defaultUEH);
        ntfy.notifyToListener(true, new Object[]{err_msg});
    }

    public void copySyncTask(SyncTaskItem pli, NotifyEvent p_ntfy) {
        SyncTaskItem npfli = pli.clone();
        npfli.setLastSyncResult(0);
        npfli.setLastSyncTime("");
        SyncTaskEditor pmsp = SyncTaskEditor.newInstance();
        pmsp.showDialog(mFragMgr, pmsp, "COPY", npfli, this, util, commonDlg, mGp, p_ntfy);
    }

    public void renameSyncTask(final SyncTaskItem pli, final NotifyEvent p_ntfy) {

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.single_item_input_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_dlg_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.single_item_input_title);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);

//		final TextView dlg_msg = (TextView) dialog.findViewById(R.id.single_item_input_msg);
        final TextView dlg_cmp = (TextView) dialog.findViewById(R.id.single_item_input_name);
        final Button btn_ok = (Button) dialog.findViewById(R.id.single_item_input_ok_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.single_item_input_cancel_btn);
        final EditText etInput = (EditText) dialog.findViewById(R.id.single_item_input_dir);

        title.setText(mContext.getString(R.string.msgs_rename_profile));

        dlg_cmp.setVisibility(TextView.GONE);
        CommonDialog.setDlgBoxSizeCompact(dialog);
        etInput.setText(pli.getSyncTaskName());
        btn_ok.setEnabled(false);
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                if (!arg0.toString().equalsIgnoreCase(pli.getSyncTaskName()))
                    btn_ok.setEnabled(true);
                else btn_ok.setEnabled(false);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });

        //OK button
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                String new_name = etInput.getText().toString();

                pli.setSyncTaskName(new_name);

                mGp.syncTaskAdapter.sort();
                mGp.syncTaskAdapter.notifyDataSetChanged();

                saveSyncTaskList(mGp, mContext, util, mGp.syncTaskAdapter.getArrayList());

                SyncTaskUtil.setAllSyncTaskToUnchecked(true, mGp.syncTaskAdapter);

                p_ntfy.notifyToListener(true, null);
            }
        });
        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btn_cancel.performClick();
            }
        });
        dialog.show();

    }

    public void ipAddressScanButtonDlg(Dialog dialog) {
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);
        final EditText edithost = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_server);
        final CheckedTextView ctv_use_port_number = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_remote_port_number);
        final EditText editport = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_port);
        NotifyEvent ntfy = new NotifyEvent(mContext);
        //Listen setRemoteShare response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context arg0, Object[] arg1) {
                edithost.setText((String) arg1[1]);
            }

            @Override
            public void negativeResponse(Context arg0, Object[] arg1) {
                dlg_msg.setText("");
            }

        });
        String port_num = "";
        if (ctv_use_port_number.isChecked()) port_num = editport.getText().toString();
        scanRemoteNetworkDlg(ntfy, port_num, false);
    }

    ;

    public void invokeSelectRemoteShareDlg(Dialog dialog) {
//		final TextView dlg_msg=(TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);

        final Spinner sp_sync_folder_smb_proto = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_protocol);
        final EditText edituser = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_user);
        final EditText editpass = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_pass);
        final EditText editshare = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_share_name);
        final EditText edithost = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_server);
        final CheckedTextView ctv_use_userpass = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_user_pass);
        final EditText editport = (EditText) dialog.findViewById(R.id.edit_sync_folder_dlg_remote_port);
        final CheckedTextView ctv_use_port_number = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_remote_port_number);
        final CheckedTextView ctv_sync_folder_smb_ipc_enforced = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_smb_ipc_signing_enforced);
        String remote_addr, remote_user = "", remote_pass = "", remote_host;

        if (ctv_use_userpass.isChecked()) {
            remote_user = edituser.getText().toString();
            remote_pass = editpass.getText().toString();
        }

        final String smb_proto=sp_sync_folder_smb_proto.getSelectedItemPosition()==0 ? SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY:SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB2_ONLY;
        final boolean ipc_enforced=ctv_sync_folder_smb_ipc_enforced.isChecked();
        setSmbUserPass(remote_user, remote_pass);
//		Log.v("","u="+remote_user+", pass="+remote_pass);
        String t_url = "";
        if (JcifsUtil.isValidIpAddress(edithost.getText().toString())) {
            remote_addr = edithost.getText().toString();
            t_url = remote_addr;
        } else {
            remote_host = edithost.getText().toString();
            t_url = remote_host;
        }
        String h_port = "";
        if (ctv_use_port_number.isChecked() && editport.getText().length() > 0)
            h_port = ":" + editport.getText().toString();
        String remurl = "smb://" + t_url + h_port + "/";
        NotifyEvent ntfy = new NotifyEvent(mContext);
        //Listen setRemoteShare response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context arg0, Object[] arg1) {
                editshare.setText((String) arg1[0]);
            }

            @Override
            public void negativeResponse(Context arg0, Object[] arg1) {
//				if (arg1!=null) dlg_msg.setText((String)arg1[0]);
//				else dlg_msg.setText("");
                if (arg1 != null) {
                    String msg_text = (String) arg1[0];
                    commonDlg.showCommonDialog(false, "E", "SMB Error", msg_text, null);
                }
            }

        });
        selectRemoteShareDlg(remurl, ipc_enforced, smb_proto, ntfy);
    }

    public void setSmbUserPass(String user, String pass) {
        smbUser = user;
        smbPass = pass;
    }

    public void selectRemoteDirectoryDlg(Dialog p_dialog, final boolean show_create) {
//		final TextView dlg_msg=(TextView) dialog.findViewById(R.id.edit_sync_folder_dlg_msg);

        final Spinner sp_sync_folder_smb_proto = (Spinner) p_dialog.findViewById(R.id.edit_sync_folder_dlg_smb_protocol);
        final EditText edithost = (EditText) p_dialog.findViewById(R.id.edit_sync_folder_dlg_remote_server);
        final EditText edituser = (EditText) p_dialog.findViewById(R.id.edit_sync_folder_dlg_remote_user);
        final EditText editpass = (EditText) p_dialog.findViewById(R.id.edit_sync_folder_dlg_remote_pass);
        final EditText editshare = (EditText) p_dialog.findViewById(R.id.edit_sync_folder_dlg_share_name);
        final EditText editdir = (EditText) p_dialog.findViewById(R.id.edit_sync_folder_dlg_smb_directory_name);
        final CheckedTextView ctv_use_userpass = (CheckedTextView) p_dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_user_pass);
        final EditText editport = (EditText) p_dialog.findViewById(R.id.edit_sync_folder_dlg_remote_port);
        final CheckedTextView ctv_use_port_number = (CheckedTextView) p_dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_use_remote_port_number);
        final CheckedTextView ctv_sync_folder_smb_ipc_enforced = (CheckedTextView) p_dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_smb_ipc_signing_enforced);
        String remote_addr, remote_user = "", remote_pass = "", remote_share, remote_host;
        if (ctv_use_userpass.isChecked()) {
            remote_user = edituser.getText().toString();
            remote_pass = editpass.getText().toString();
        }

        final String smb_proto=sp_sync_folder_smb_proto.getSelectedItemPosition()==0 ?
                SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY:SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB2_ONLY;
        final boolean ipc_enforced=ctv_sync_folder_smb_ipc_enforced.isChecked();
        remote_share = editshare.getText().toString();

        final String p_dir = editdir.getText().toString();

        setSmbUserPass(remote_user, remote_pass);
        String t_url = "";
        if (JcifsUtil.isValidIpAddress(edithost.getText().toString())) {
            remote_addr = edithost.getText().toString();
            t_url = remote_addr;
        } else {
            remote_host = edithost.getText().toString();
            t_url = remote_host;
        }
        String h_port = "";
        if (ctv_use_port_number.isChecked() && editport.getText().length() > 0)
            h_port = ":" + editport.getText().toString();
        final String remurl = "smb://" + t_url + h_port + "/" + remote_share + "/";

        final ArrayList<TreeFilelistItem> rows = new ArrayList<TreeFilelistItem>();
        NotifyEvent ntfy = new NotifyEvent(mContext);
        // set thread response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                @SuppressWarnings("unchecked")
                ArrayList<TreeFilelistItem> rfl = (ArrayList<TreeFilelistItem>) o[0];

//                if (rfl.size()==0) {
//                    String msg=mContext.getString(R.string.msgs_dir_empty);
//                    commonDlg.showCommonDialog(false,"W",msg,"",null);
//                    return;
//                }

                for (int i = 0; i < rfl.size(); i++) {
                    if (rfl.get(i).isDir() && rfl.get(i).canRead()) rows.add(rfl.get(i));
                }
                Collections.sort(rows);
                NotifyEvent ntfy_sel=new NotifyEvent(mContext);
                ntfy_sel.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] o) {
                        ArrayList<String> sel = (ArrayList<String>)o[0];
                        editdir.setText(sel.get(0));
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                remoteDirectorySelector(rows, remurl, p_dir, ipc_enforced, smb_proto, show_create, ntfy_sel);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
                String msg_text = (String) o[0];
                commonDlg.showCommonDialog(false, "E", "SMB Error", msg_text, null);
            }
        });
        createRemoteFileList(remurl, "", ipc_enforced, smb_proto, ntfy, true);

    }

    private void remoteDirectorySelector(ArrayList<TreeFilelistItem> rows, String remurl, String p_dir,
                                         boolean ipc_enforced, String smb_proto, final boolean show_create, final NotifyEvent p_ntfy) {
        //カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.file_select_edit_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.file_select_edit_dlg_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.file_select_edit_dlg_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.file_select_edit_dlg_title);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);
//				subtitle.setTextColor(mGp.themeColorList.text_color_dialog_title);

        final LinearLayout ll_mp = (LinearLayout) dialog.findViewById(R.id.file_select_edit_dlg_mp_view);
        ll_mp.setVisibility(LinearLayout.GONE);
        final EditText et_name = (EditText) dialog.findViewById(R.id.file_select_edit_dlg_file_name);
        et_name.setVisibility(EditText.GONE);

        final CustomTextView tv_home = (CustomTextView) dialog.findViewById(R.id.file_select_edit_dlg_dir_name);
        tv_home.setText(remurl);

        final Button btn_create = (Button) dialog.findViewById(R.id.file_select_edit_dlg_create_btn);

        title.setText(mContext.getString(R.string.msgs_select_remote_dir));
        final Button btn_ok = (Button) dialog.findViewById(R.id.file_select_edit_dlg_ok_btn);

        final Button btn_home = (Button) dialog.findViewById(R.id.file_select_edit_dlg_home_dir_btn);
        final Button btn_refresh = (Button) dialog.findViewById(R.id.file_select_edit_dlg_refresh_btn);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        RemoteAuthInfo ra=new RemoteAuthInfo();
        ra.smb_smb_protocol=smb_proto;
        ra.smb_ipc_signing_enforced=ipc_enforced;
        ra.smb_user_name=smbUser;
        ra.smb_user_password=smbPass;

        final ListView lv = (ListView) dialog.findViewById(android.R.id.list);
        final TreeFilelistAdapter tfa = new TreeFilelistAdapter(mContext, true, false);
        tfa.setDataList(rows);
        lv.setAdapter(tfa);
        lv.setScrollingCacheEnabled(false);
        lv.setScrollbarFadingEnabled(false);

        if (p_dir.length() != 0)
            for (int i = 0; i < tfa.getDataItemCount(); i++) {
                if (tfa.getDataItem(i).getName().equals(p_dir)) {
                    lv.setSelection(i);
                    tfa.getDataItem(i).setChecked(true);
                    tfa.notifyDataSetChanged();
                    break;
                }
            }
        NotifyEvent ntfy_expand_close = new NotifyEvent(mContext);
        ntfy_expand_close.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                int idx = (Integer) o[0];
                final int pos = tfa.getItem(idx);
                final TreeFilelistItem tfi = tfa.getDataItem(pos);
                expandHideRemoteDirTree(remurl, ipc_enforced, smb_proto, pos, tfi, tfa);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        tfa.setExpandCloseListener(ntfy_expand_close);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                final int pos = tfa.getItem(idx);
                final TreeFilelistItem tfi = tfa.getDataItem(pos);
//						tfa.setDataItemIsSelected(pos);
                expandHideRemoteDirTree(remurl, ipc_enforced, smb_proto, pos, tfi, tfa);
            }
        });
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> items, View view, int idx, long id) {
                return true;
            }
        });

        NotifyEvent ctv_ntfy = new NotifyEvent(mContext);
        // set file list thread response listener
        ctv_ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                if (o != null) {
                    int pos = (Integer) o[0];
                    if (tfa.getDataItem(pos).isChecked()) btn_ok.setEnabled(true);
                }
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
                btn_ok.setEnabled(false);
                for (int i = 0; i < tfa.getDataItemCount(); i++) {
                    if (tfa.getDataItem(i).isChecked()) {
                        btn_ok.setEnabled(true);
                        break;
                    }
                }
            }
        });
        tfa.setCbCheckListener(ctv_ntfy);

//        if (show_create) btn_create.setVisibility(Button.VISIBLE);
//        else btn_create.setVisibility(Button.GONE);
        btn_create.setVisibility(Button.VISIBLE);
        btn_create.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ne=new NotifyEvent(mContext);
                ne.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        btn_refresh.performClick();
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                createRemoteDirectoryDlg(tv_home.getText(), ra, ne);
            }
        });

        btn_refresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ntfy_refresh=new NotifyEvent(mContext);
                ntfy_refresh.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] o) {
                        ArrayList<TreeFilelistItem> new_rfl = (ArrayList<TreeFilelistItem>) o[0];
                        tfa.setDataList(new_rfl);
                        tfa.notifyDataSetChanged();
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                createRemoteFileList(tv_home.getText().toString(), "", ipc_enforced, smb_proto, ntfy_refresh, true);
            }
        });

        btn_home.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        //OKボタンの指定
        btn_ok.setEnabled(false);
        btn_ok.setVisibility(Button.VISIBLE);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                ArrayList<String> sel=new ArrayList<String>();
                for (int i = 0; i < tfa.getCount(); i++) {
                    if (tfa.getDataItem(i).isChecked()) {
                        if (tfa.getDataItem(i).getPath().length() == 1)
                            sel.add(tfa.getDataItem(i).getName());
                        else sel.add(tfa.getDataItem(i).getPath()
                                .substring(1, tfa.getDataItem(i).getPath().length()) +tfa.getDataItem(i).getName());
                        break;
                    }
                }
                p_ntfy.notifyToListener(true, new Object[]{sel});
            }
        });
        //CANCELボタンの指定
        final Button btn_cancel = (Button) dialog.findViewById(R.id.file_select_edit_dlg_cancel_btn);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btn_cancel.performClick();
            }
        });

//			    dialog.setOnKeyListener(new DialogOnKeyListener(context));
//			    dialog.setCancelable(false);
        dialog.show();

    }

    static public SyncTaskItem getExternalSdcardUsedSyncTask(GlobalParameters gp) {
        SyncTaskItem pli = null;
        for (int i = 0; i < gp.syncTaskAdapter.getCount(); i++) {
            if (gp.syncTaskAdapter.getItem(i).getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                pli = gp.syncTaskAdapter.getItem(i);
                break;
            }
        }
        return pli;
    }

    private void createRemoteDirectoryDlg(final String c_dir, final RemoteAuthInfo ra, final NotifyEvent p_ntfy) {
        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(com.sentaroh.android.Utilities.R.layout.single_item_input_dlg);
        final TextView dlg_title = (TextView) dialog.findViewById(com.sentaroh.android.Utilities.R.id.single_item_input_title);
        dlg_title.setText(mContext.getString(com.sentaroh.android.Utilities.R.string.msgs_file_select_edit_dlg_create));
        final TextView dlg_msg = (TextView) dialog.findViewById(com.sentaroh.android.Utilities.R.id.single_item_input_msg);
        final TextView dlg_cmp = (TextView) dialog.findViewById(com.sentaroh.android.Utilities.R.id.single_item_input_name);
        final Button btnOk = (Button) dialog.findViewById(com.sentaroh.android.Utilities.R.id.single_item_input_ok_btn);
        final Button btnCancel = (Button) dialog.findViewById(com.sentaroh.android.Utilities.R.id.single_item_input_cancel_btn);
        final EditText etDir=(EditText) dialog.findViewById(com.sentaroh.android.Utilities.R.id.single_item_input_dir);

        dlg_cmp.setText(mContext.getString(com.sentaroh.android.Utilities.R.string.msgs_file_select_edit_parent_directory)+":"+c_dir);
        CommonDialog.setDlgBoxSizeCompact(dialog);
        btnOk.setEnabled(false);
        final Handler hndl=new Handler();
        etDir.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0) {
                    btnOk.setEnabled(false);
                    String n_dir=c_dir+s.toString();
                    NotifyEvent ne=new NotifyEvent(mContext);
                    ne.setListener(new NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            hndl.post(new Runnable() {
                                  @Override
                                  public void run() {
                                      btnOk.setEnabled(false);
                                      dlg_msg.setText(mContext.getString(
                                              com.sentaroh.android.Utilities.R.string.msgs_single_item_input_dlg_duplicate_dir));
                                  }
                              });
                        }

                        @Override
                        public void negativeResponse(Context context, Object[] objects) {
                            hndl.post(new Runnable() {
                                @Override
                                public void run() {
                                    btnOk.setEnabled(true);
                                    dlg_msg.setText("");
                                }
                            });
                        }
                    });
                    isRemoteDirectoryExists(n_dir, ra, ne);
                } else {
                    btnOk.setEnabled(false);
                    dlg_msg.setText("");
                }
            }
        });

        //OK button
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//				NotifyEvent
                final String creat_dir=etDir.getText().toString();
                final String n_path=c_dir+creat_dir+"/";
                NotifyEvent ntfy=new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEvent.NotifyEventListener(){
                    @Override
                    public void positiveResponse(Context c, Object[] o) {

                        NotifyEvent ne=new NotifyEvent(mContext);
                        ne.setListener(new NotifyEventListener() {
                            @Override
                            public void positiveResponse(Context context, Object[] objects) {
                                hndl.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        p_ntfy.notifyToListener(true, null);
                                        dialog.dismiss();
                                    }
                                });
                            }
                            @Override
                            public void negativeResponse(Context context, Object[] objects) {
                                hndl.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        p_ntfy.notifyToListener(false, null);
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });
                        createRemoteDirectory(c_dir+etDir.getText().toString(), ra, ne);
                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                CommonDialog cd=new CommonDialog(mContext, mFragMgr);
                cd.showCommonDialog(true, "W", mContext.getString(com.sentaroh.android.Utilities.R.string.msgs_file_select_edit_confirm_create_directory), n_path, ntfy);
            }
        });
        // CANCELボタンの指定
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                p_ntfy.notifyToListener(false, null);
            }
        });
        dialog.show();
    };

    private void isRemoteDirectoryExists(final String new_dir, final RemoteAuthInfo ra, final NotifyEvent p_ntfy) {
        final Dialog dialog=showProgressSpinIndicator(mContext);
        dialog.show();
        Thread th=new Thread(){
          @Override
          public void run() {
              JcifsAuth auth=null;
              if (ra.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY)) auth=new JcifsAuth(JcifsFile.JCIFS_LEVEL_JCIFS1, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password);
              else auth=new JcifsAuth(JcifsFile.JCIFS_LEVEL_JCIFS2, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password);
              try {
                  JcifsFile jf=new JcifsFile(new_dir, auth);
                  if (jf.exists()) p_ntfy.notifyToListener(true, null);
                  else p_ntfy.notifyToListener(false, null);
              } catch (MalformedURLException e) {
                  e.printStackTrace();
              } catch (JcifsException e) {
                  e.printStackTrace();
              }
              dialog.dismiss();
          }
        };
        th.start();
    }

    private void createRemoteDirectory(final String new_dir, final RemoteAuthInfo ra, final NotifyEvent p_ntfy) {
        final Dialog dialog=showProgressSpinIndicator(mContext);
        dialog.show();
        Thread th=new Thread(){
            @Override
            public void run() {
                JcifsAuth auth=null;
                if (ra.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY)) auth=new JcifsAuth(JcifsFile.JCIFS_LEVEL_JCIFS1, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password);
                else auth=new JcifsAuth(JcifsFile.JCIFS_LEVEL_JCIFS2, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password);
                try {
                    JcifsFile jf=new JcifsFile(new_dir, auth);
                    jf.mkdirs();
                    if (jf.exists()) p_ntfy.notifyToListener(true, null);
                    else p_ntfy.notifyToListener(false, null);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (JcifsException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        };
        th.start();
    }

//	static public SyncTaskItem getUsbSafUsedSyncTask(GlobalParameters gp) {
//		SyncTaskItem pli=null;
//		for (int i=0;i<gp.syncTaskAdapter.getCount();i++) {
//			if (gp.syncTaskAdapter.getItem(i).getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
//				pli=gp.syncTaskAdapter.getItem(i);
//				break;
//			}
//		}
//		return pli;
//	};

    public void editWifiApListDlg(final ArrayList<String> ap_list, final NotifyEvent p_ntfy) {
        ArrayList<AdapterFilterList.FilterListItem> filterList = new ArrayList<AdapterFilterList.FilterListItem>();
        final AdapterFilterList filterAdapter;

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.filter_list_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.filter_select_edit_title);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);
        title.setText(mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_ap_title));

        Button add_current_ssid = (Button) dialog.findViewById(R.id.filter_select_edit_list_dir_btn);
        add_current_ssid.setText(mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_ap_add_current_ap));

        filterAdapter = new AdapterFilterList(mContext,
                R.layout.filter_list_item_view, filterList, false);
        ListView lv = (ListView) dialog.findViewById(R.id.filter_select_edit_listview);

        for (int i = 0; i < ap_list.size(); i++) {
            String inc = ap_list.get(i).substring(0, 1);
            String filter = ap_list.get(i).substring(1, ap_list.get(i).length());
            boolean b_inc = false;
            if (inc.equals(SMBSYNC2_PROF_FILTER_INCLUDE)) b_inc = true;
            filterAdapter.add(new AdapterFilterList.FilterListItem(filter, b_inc));
        }
        lv.setAdapter(filterAdapter);
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.filter_select_edit_msg);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        final EditText et_filter = (EditText) dialog.findViewById(R.id.filter_select_edit_new_filter);
        et_filter.setHint(mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_ap_hint));

        final Button addBtn = (Button) dialog.findViewById(R.id.filter_select_edit_add_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.filter_select_edit_cancel_btn);
        final Button btn_ok = (Button) dialog.findViewById(R.id.filter_select_edit_ok_btn);
        btn_ok.setEnabled(false);

        NotifyEvent ntfy_inc_exc = new NotifyEvent(mContext);
        ntfy_inc_exc.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                btn_ok.setEnabled(true);
                dlg_msg.setText("");
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        filterAdapter.setNotifyIncExcListener(ntfy_inc_exc);

        NotifyEvent ntfy_delete = new NotifyEvent(mContext);
        ntfy_delete.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                btn_ok.setEnabled(true);
                dlg_msg.setText("");
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        filterAdapter.setNotifyDeleteListener(ntfy_delete);

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                AdapterFilterList.FilterListItem fli = filterAdapter.getItem(idx);
                // リストアイテムを選択したときの処理
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        btn_ok.setEnabled(true);
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }

                });
                editFilter(idx, filterAdapter, fli, fli.getFilter(),
                        mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_ap_edit_title), ntfy);
            }
        });

        // Addボタンの指定
        addBtn.setEnabled(false);
        et_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    if (isFilterExists(s.toString().trim(), filterAdapter)) {
                        String mtxt = mContext.getString(R.string.msgs_profile_sync_task_dlg_duplicate_ap_specified);
                        dlg_msg.setText(String.format(mtxt, s.toString().trim()));
                        addBtn.setEnabled(false);
                        btn_ok.setEnabled(true);
                    } else {
                        dlg_msg.setText("");
                        addBtn.setEnabled(true);
                        btn_ok.setEnabled(false);
                    }
                } else {
                    addBtn.setEnabled(false);
                    btn_ok.setEnabled(true);
                }
//				et_filter.setText(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        add_current_ssid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wm = (WifiManager) mGp.appContext.getSystemService(Context.WIFI_SERVICE);
                String ssid = SyncUtil.getWifiSsidName(wm);
                if (!ssid.equals("")) {
                    if (isFilterExists(ssid, filterAdapter)) {
                        String mtxt = mContext.getString(R.string.msgs_profile_sync_task_dlg_duplicate_ap_specified);
                        dlg_msg.setText(String.format(mtxt, ssid));
                        addBtn.setEnabled(false);
                        btn_ok.setEnabled(true);
                    } else {
                        dlg_msg.setText("");
                        filterAdapter.add(new AdapterFilterList.FilterListItem(ssid, true));
                        filterAdapter.setNotifyOnChange(true);
                        filterAdapter.sort(new Comparator<AdapterFilterList.FilterListItem>() {
                            @Override
                            public int compare(AdapterFilterList.FilterListItem lhs,
                                               AdapterFilterList.FilterListItem rhs) {
                                return lhs.getFilter().compareToIgnoreCase(rhs.getFilter());
                            }

                            ;
                        });
                        btn_ok.setEnabled(true);
                    }
                } else {
                    String mtxt = mContext.getString(R.string.msgs_profile_sync_task_dlg_ap_not_connected);
                    dlg_msg.setText(mtxt);
                }
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg_msg.setText("");
                String newfilter = et_filter.getText().toString().trim();
                et_filter.setText("");
                filterAdapter.add(new AdapterFilterList.FilterListItem(newfilter, true));
                filterAdapter.setNotifyOnChange(true);
                filterAdapter.sort(new Comparator<AdapterFilterList.FilterListItem>() {
                    @Override
                    public int compare(AdapterFilterList.FilterListItem lhs,
                                       AdapterFilterList.FilterListItem rhs) {
                        return lhs.getFilter().compareToIgnoreCase(rhs.getFilter());
                    }

                    ;
                });
                btn_ok.setEnabled(true);
            }
        });

        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
//				glblParms.profileListView.setSelectionFromTop(currentViewPosX,currentViewPosY);
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btn_cancel.performClick();
            }
        });
        // OKボタンの指定
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                ap_list.clear();
                if (filterAdapter.getCount() > 0) {
                    for (int i = 0; i < filterAdapter.getCount(); i++) {
                        String inc = SMBSYNC2_PROF_FILTER_EXCLUDE;
                        if (filterAdapter.getItem(i).isInclude())
                            inc = SMBSYNC2_PROF_FILTER_INCLUDE;
                        ap_list.add(inc + filterAdapter.getItem(i).getFilter());
                    }
                }
                p_ntfy.notifyToListener(true, null);
            }
        });
//		dialog.setOnKeyListener(new DialogOnKeyListener(context));
//		dialog.setCancelable(false);
        dialog.show();

    }

    ;

    public void editFileFilterDlg(final ArrayList<String> file_filter, final NotifyEvent p_ntfy) {
        ArrayList<AdapterFilterList.FilterListItem> filterList = new ArrayList<AdapterFilterList.FilterListItem>();
        final AdapterFilterList filterAdapter;

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.filter_list_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.filter_select_edit_title);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);

        Button dirbtn = (Button) dialog.findViewById(R.id.filter_select_edit_list_dir_btn);
        dirbtn.setVisibility(Button.GONE);

        filterAdapter = new AdapterFilterList(mContext, R.layout.filter_list_item_view, filterList);
        ListView lv = (ListView) dialog.findViewById(R.id.filter_select_edit_listview);

        for (int i = 0; i < file_filter.size(); i++) {
            String inc = file_filter.get(i).substring(0, 1);
            String filter = file_filter.get(i).substring(1, file_filter.get(i).length());
            boolean b_inc = false;
            if (inc.equals(SMBSYNC2_PROF_FILTER_INCLUDE)) b_inc = true;
            filterAdapter.add(new AdapterFilterList.FilterListItem(filter, b_inc));
        }
        lv.setAdapter(filterAdapter);
        title.setText(mContext.getString(R.string.msgs_filter_list_dlg_file_filter));
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.filter_select_edit_msg);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        final EditText et_filter = (EditText) dialog.findViewById(R.id.filter_select_edit_new_filter);
        final Button addBtn = (Button) dialog.findViewById(R.id.filter_select_edit_add_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.filter_select_edit_cancel_btn);
        final Button btn_ok = (Button) dialog.findViewById(R.id.filter_select_edit_ok_btn);
        btn_ok.setEnabled(false);

        NotifyEvent ntfy_inc_exc = new NotifyEvent(mContext);
        ntfy_inc_exc.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                btn_ok.setEnabled(true);
                dlg_msg.setText("");
            }
            @Override
            public void negativeResponse(Context c, Object[] o) {}
        });
        filterAdapter.setNotifyIncExcListener(ntfy_inc_exc);

        NotifyEvent ntfy_delete = new NotifyEvent(mContext);
        ntfy_delete.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                btn_ok.setEnabled(true);
                dlg_msg.setText("");
            }
            @Override
            public void negativeResponse(Context c, Object[] o) {}
        });
        filterAdapter.setNotifyDeleteListener(ntfy_delete);

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                AdapterFilterList.FilterListItem fli = filterAdapter.getItem(idx);
                // リストアイテムを選択したときの処理
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        btn_ok.setEnabled(true);
                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {}

                });
                editFilter(idx, filterAdapter, fli, fli.getFilter(), "", ntfy);
            }
        });

        // Addボタンの指定
        addBtn.setEnabled(false);
        et_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    if (isFilterExists(s.toString().trim(), filterAdapter)) {
                        String mtxt = mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified);
                        dlg_msg.setText(String.format(mtxt, s.toString().trim()));
                        addBtn.setEnabled(false);
                        btn_ok.setEnabled(false);
                    } else {
                        dlg_msg.setText("");
                        addBtn.setEnabled(true);
                        btn_ok.setEnabled(false);
                    }
                } else {
                    addBtn.setEnabled(false);
                    btn_ok.setEnabled(true);
                }
//				et_filter.setText(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg_msg.setText("");
                String newfilter = et_filter.getText().toString().trim();
                et_filter.setText("");
                filterAdapter.add(new AdapterFilterList.FilterListItem(newfilter, true));
                filterAdapter.setNotifyOnChange(true);
                filterAdapter.sort(new Comparator<AdapterFilterList.FilterListItem>() {
                    @Override
                    public int compare(AdapterFilterList.FilterListItem lhs,
                                       AdapterFilterList.FilterListItem rhs) {
                        return lhs.getFilter().compareToIgnoreCase(rhs.getFilter());
                    }
                });
                btn_ok.setEnabled(true);
            }
        });

        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btn_cancel.performClick();
            }
        });
        // OKボタンの指定
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                file_filter.clear();
                if (filterAdapter.getCount() > 0) {
                    for (int i = 0; i < filterAdapter.getCount(); i++) {
                        String inc = SMBSYNC2_PROF_FILTER_EXCLUDE;
                        if (filterAdapter.getItem(i).isInclude())
                            inc = SMBSYNC2_PROF_FILTER_INCLUDE;
                        file_filter.add(inc + filterAdapter.getItem(i).getFilter());
                    }
                }
                p_ntfy.notifyToListener(true, null);
            }
        });
        dialog.show();
    }

    public void editDirFilterDlg(final SyncTaskItem sti, final NotifyEvent p_ntfy) {
        ArrayList<AdapterFilterList.FilterListItem> filterList = new ArrayList<AdapterFilterList.FilterListItem>();
        final AdapterFilterList filterAdapter;

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.filter_list_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.filter_select_edit_title);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);

        filterAdapter = new AdapterFilterList(mContext, R.layout.filter_list_item_view, filterList);
        final ListView lv = (ListView) dialog.findViewById(R.id.filter_select_edit_listview);

        for (int i = 0; i < sti.getDirFilter().size(); i++) {
            String inc = sti.getDirFilter().get(i).substring(0, 1);
            String filter = sti.getDirFilter().get(i).substring(1, sti.getDirFilter().get(i).length());
            boolean b_inc = false;
            if (inc.equals(SMBSYNC2_PROF_FILTER_INCLUDE)) b_inc = true;
            filterAdapter.add(new AdapterFilterList.FilterListItem(filter, b_inc));
        }
        lv.setAdapter(filterAdapter);
        lv.setScrollingCacheEnabled(false);
        lv.setScrollbarFadingEnabled(false);

        title.setText(mContext.getString(R.string.msgs_filter_list_dlg_dir_filter));
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.filter_select_edit_msg);
        final Button dirbtn = (Button) dialog.findViewById(R.id.filter_select_edit_list_dir_btn);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        final EditText et_filter = (EditText) dialog.findViewById(R.id.filter_select_edit_new_filter);
        final Button addbtn = (Button) dialog.findViewById(R.id.filter_select_edit_add_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.filter_select_edit_cancel_btn);
        final Button btn_ok = (Button) dialog.findViewById(R.id.filter_select_edit_ok_btn);
        btn_ok.setEnabled(false);

        NotifyEvent ntfy_inc_exc = new NotifyEvent(mContext);
        ntfy_inc_exc.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                String e_msg = isFilterSameDirectoryAccess(sti, filterAdapter);
                if (!e_msg.equals("")) {
                    dlg_msg.setText(e_msg);
                    return;
                }
                btn_ok.setEnabled(true);
                dlg_msg.setText("");
            }
            @Override
            public void negativeResponse(Context c, Object[] o) { }
        });
        filterAdapter.setNotifyIncExcListener(ntfy_inc_exc);

        NotifyEvent ntfy_delete = new NotifyEvent(mContext);
        ntfy_delete.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                String e_msg = isFilterSameDirectoryAccess(sti, filterAdapter);
                if (!e_msg.equals("")) {
                    dlg_msg.setText(e_msg);
                    return;
                }
                btn_ok.setEnabled(true);
                dlg_msg.setText("");
            }
            @Override
            public void negativeResponse(Context c, Object[] o) { }
        });
        filterAdapter.setNotifyDeleteListener(ntfy_delete);

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                AdapterFilterList.FilterListItem fli = filterAdapter.getItem(idx);
                // リストアイテムを選択したときの処理
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        btn_ok.setEnabled(true);
                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                editFilter(idx, filterAdapter, fli, fli.getFilter(), "", ntfy);
            }
        });

        // Addボタンの指定
        et_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    if (isFilterExists(s.toString().trim(), filterAdapter)) {
                        String mtxt = mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified);
                        dlg_msg.setText(String.format(mtxt, s.toString().trim()));
                        addbtn.setEnabled(false);
                        dirbtn.setEnabled(true);
                        btn_ok.setEnabled(false);
                    } else {
                        dlg_msg.setText("");
                        addbtn.setEnabled(true);
                        dirbtn.setEnabled(false);
                        btn_ok.setEnabled(false);
                    }
                } else {
                    addbtn.setEnabled(false);
                    dirbtn.setEnabled(true);
                    btn_ok.setEnabled(true);
                }
//				et_filter.setText(s);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });
        addbtn.setEnabled(false);
        addbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg_msg.setText("");
                String newfilter = et_filter.getText().toString();
                if (isFilterExists(newfilter, filterAdapter)) {
                    String mtxt = mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified);
                    dlg_msg.setText(String.format(mtxt, newfilter));
                    return;
                }
                dlg_msg.setText("");
                et_filter.setText("");
                filterAdapter.add(new AdapterFilterList.FilterListItem(newfilter, true));
                filterAdapter.setNotifyOnChange(true);
                filterAdapter.sort(new Comparator<AdapterFilterList.FilterListItem>() {
                    @Override
                    public int compare(AdapterFilterList.FilterListItem lhs,
                                       AdapterFilterList.FilterListItem rhs) {
                        return lhs.getFilter().compareToIgnoreCase(rhs.getFilter());
                    }
                });
                String e_msg = isFilterSameDirectoryAccess(sti, filterAdapter);
                if (!e_msg.equals("")) {
                    dlg_msg.setText(e_msg);
                    return;
                }
                dirbtn.setEnabled(true);
                btn_ok.setEnabled(true);
            }
        });

        // Directoryボタンの指定
        dirbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context arg0, Object[] arg1) {
                        String e_msg = isFilterSameDirectoryAccess(sti, filterAdapter);
                        if (!e_msg.equals("")) {
                            dlg_msg.setText(e_msg);
                            return;
                        }
                        btn_ok.setEnabled(true);
                        dlg_msg.setText("");
                    }
                    @Override
                    public void negativeResponse(Context arg0, Object[] arg1) {
                        if (arg1 != null) {
                            String msg_text = (String) arg1[0];
                            commonDlg.showCommonDialog(false, "E", "SMB Error", msg_text, null);
                        }
                    }
                });
                listDirectoryFilter(sti, filterAdapter, ntfy);
            }
        });

        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                p_ntfy.notifyToListener(false, null);
            }
        });

        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btn_cancel.performClick();
            }
        });

        // OKボタンの指定
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String e_msg = isFilterSameDirectoryAccess(sti, filterAdapter);
                if (!e_msg.equals("")) {
                    dlg_msg.setText(e_msg);
                    return;
                }

                dialog.dismiss();
                sti.getDirFilter().clear();
                if (filterAdapter.getCount() > 0) {
                    for (int i = 0; i < filterAdapter.getCount(); i++) {
                        String inc = SMBSYNC2_PROF_FILTER_EXCLUDE;
                        if (filterAdapter.getItem(i).isInclude())
                            inc = SMBSYNC2_PROF_FILTER_INCLUDE;
                        sti.getDirFilter().add(inc + filterAdapter.getItem(i).getFilter());
                    }
                }
                p_ntfy.notifyToListener(true, null);
            }
        });
        dialog.show();

    }

    private void editFilter(final int edit_idx, final AdapterFilterList fa,
                            final AdapterFilterList.FilterListItem fli, final String filter, String title_text, final NotifyEvent p_ntfy) {

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.filter_edit_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.filter_edit_dlg_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.filter_edit_dlg_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.filter_edit_dlg_title);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);
        if (!title_text.equals("")) title.setText(title_text);

        CommonDialog.setDlgBoxSizeCompact(dialog);
        final EditText et_filter = (EditText) dialog.findViewById(R.id.filter_edit_dlg_filter);
        et_filter.setText(filter);
        // CANCELボタンの指定
        final Button btn_cancel = (Button) dialog.findViewById(R.id.filter_edit_dlg_cancel_btn);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btn_cancel.performClick();
            }
        });
        // OKボタンの指定
        Button btn_ok = (Button) dialog.findViewById(R.id.filter_edit_dlg_ok_btn);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView dlg_msg = (TextView) dialog.findViewById(R.id.filter_edit_dlg_msg);

                String newfilter = et_filter.getText().toString();
                if (!filter.equalsIgnoreCase(newfilter)) {
                    if (isFilterExists(newfilter, fa)) {
                        String mtxt = mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified);
                        dlg_msg.setText(String.format(mtxt, newfilter));
                        return;
                    }
                }
                dialog.dismiss();
                fli.setFilter(newfilter);

                fa.setNotifyOnChange(true);
                if (p_ntfy != null) p_ntfy.notifyToListener(true, null);
            }
        });
        dialog.show();
    }

    private boolean isFilterExists(String nf, AdapterFilterList fa) {
        if (fa.getCount() == 0) return false;
        for (int i = 0; i < fa.getCount(); i++) {
            if (!fa.getItem(i).isDeleted())
                if (fa.getItem(i).getFilter().equalsIgnoreCase(nf)) return true;
        }
        return false;
    }

    static public SyncTaskItem getSyncTaskByName(ArrayList<SyncTaskItem> t_prof, String task_name) {
        SyncTaskItem stli = null;

        for (SyncTaskItem li : t_prof) {
            if (li.getSyncTaskName().equalsIgnoreCase(task_name)) {
                stli = li;
                break;
            }
        }
        return stli;
    }

    static public SyncTaskItem getSyncTaskByName(AdapterSyncTask t_prof, String task_name) {
        return getSyncTaskByName(t_prof.getArrayList(), task_name);
    }

    private void listDirectoryFilter(SyncTaskItem sti,
                                     AdapterFilterList fla, final NotifyEvent p_ntfy) {
        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            listLocalDirectoryFilter(sti, fla, p_ntfy);
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            listLocalDirectoryFilter(sti, fla, p_ntfy);
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            listSmbDirectoryFilter(sti, fla, p_ntfy);
        }
    }

    private void listLocalDirectoryFilter(final SyncTaskItem sti,
                                          final AdapterFilterList fla, final NotifyEvent p_ntfy) {
        final String m_dir = sti.getMasterDirectoryName();
        String localBaseDir_t = sti.getMasterLocalMountPoint();//mGp.internalRootDirectory;
        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD))
            localBaseDir_t = mGp.safMgr.getSdcardDirectory();
        final String localBaseDir = localBaseDir_t;

        ArrayList<TreeFilelistItem> tfl = createLocalFilelist(true, localBaseDir, "/" + m_dir);

        if (tfl.size()==0) {
            String msg=mContext.getString(R.string.msgs_dir_empty);
            commonDlg.showCommonDialog(false,"W",msg,"",null);
            return;
        }

        //カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.item_select_list_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.item_select_list_dlg_title);
        final TextView subtitle = (TextView) dialog.findViewById(R.id.item_select_list_dlg_subtitle);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);
        subtitle.setTextColor(mGp.themeColorList.text_color_dialog_title);

        title.setText(mContext.getString(R.string.msgs_filter_list_dlg_add_dir_filter));

        String c_dir = (m_dir.equals("")) ? localBaseDir + "/" : localBaseDir + "/" + m_dir + "/";
        subtitle.setText(mContext.getString(R.string.msgs_current_dir) + " " + c_dir);
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.item_select_list_dlg_msg);
        final Button btn_ok = (Button) dialog.findViewById(R.id.item_select_list_dlg_ok_btn);

        final LinearLayout ll_context = (LinearLayout) dialog.findViewById(R.id.context_view_file_select);
        ll_context.setVisibility(LinearLayout.VISIBLE);
        final ImageButton ib_select_all = (ImageButton) ll_context.findViewById(R.id.context_button_select_all);
        final ImageButton ib_unselect_all = (ImageButton) ll_context.findViewById(R.id.context_button_unselect_all);

        dlg_msg.setVisibility(TextView.VISIBLE);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        final ListView lv = (ListView) dialog.findViewById(android.R.id.list);
        final TreeFilelistAdapter tfa = new TreeFilelistAdapter(mContext, false, false);
        lv.setAdapter(tfa);
        tfa.setDataList(tfl);
        lv.setScrollingCacheEnabled(false);
        lv.setScrollbarFadingEnabled(false);

        NotifyEvent ntfy_expand_close = new NotifyEvent(mContext);
        ntfy_expand_close.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                int idx = (Integer) o[0];
                final int pos = tfa.getItem(idx);
                final TreeFilelistItem tfi = tfa.getDataItem(pos);
                expandHideLocalDirTree(true, localBaseDir, pos, tfi, tfa);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        tfa.setExpandCloseListener(ntfy_expand_close);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                final int pos = tfa.getItem(idx);
                final TreeFilelistItem tfi = tfa.getDataItem(pos);
//				tfa.setDataItemIsSelected(pos);
                expandHideLocalDirTree(true, localBaseDir, pos, tfi, tfa);
            }
        });
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> items, View view, int idx, long id) {
                return true;
            }
        });

        ib_select_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < tfa.getDataItemCount(); i++) {
                    TreeFilelistItem tfli = tfa.getDataItem(i);
                    if (!tfli.isHideListItem()) tfa.setDataItemIsSelected(i);
                }
                tfa.notifyDataSetChanged();
                btn_ok.setEnabled(true);
            }
        });

        ib_unselect_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < tfa.getDataItemCount(); i++) {
                    tfa.setDataItemIsUnselected(i);
                }
                tfa.notifyDataSetChanged();
                btn_ok.setEnabled(false);
            }
        });

        //OKボタンの指定
        btn_ok.setEnabled(false);
        NotifyEvent ntfy = new NotifyEvent(mContext);
        //Listen setRemoteShare response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context arg0, Object[] arg1) {
                btn_ok.setEnabled(true);
            }

            @Override
            public void negativeResponse(Context arg0, Object[] arg1) {
                boolean checked = false;
                for (int i = 0; i < tfa.getDataItemCount(); i++) {
                    if (tfa.getDataItem(i).isChecked()) {
                        checked = true;
                        break;
                    }
                }
                if (checked) btn_ok.setEnabled(true);
                else btn_ok.setEnabled(false);
            }
        });
        tfa.setCbCheckListener(ntfy);

        btn_ok.setText(mContext.getString(R.string.msgs_filter_list_dlg_add));
        btn_ok.setVisibility(Button.VISIBLE);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!addDirFilter(true, tfa, fla, "/" + m_dir + "/", dlg_msg, sti, false)) return;
                addDirFilter(false, tfa, fla, "/" + m_dir + "/", dlg_msg, sti, false);
                dialog.dismiss();
                p_ntfy.notifyToListener(true, null);
            }
        });

        //CANCELボタンの指定
        final Button btn_cancel = (Button) dialog.findViewById(R.id.item_select_list_dlg_cancel_btn);
        btn_cancel.setText(mContext.getString(R.string.msgs_filter_list_dlg_close));
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                p_ntfy.notifyToListener(true, null);
            }
        });

        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btn_cancel.performClick();
            }
        });

        dialog.show();

    }

    private void listSmbDirectoryFilter(final SyncTaskItem sti,
                                        final AdapterFilterList fla, final NotifyEvent p_ntfy) {
        setSmbUserPass(sti.getMasterSmbUserName(), sti.getMasterSmbPassword());
        String t_remurl = "";
        if (sti.getMasterSmbHostName().equals("")) t_remurl = sti.getMasterSmbAddr();
        else t_remurl = sti.getMasterSmbHostName();
        String h_port = "";
        if (!sti.getMasterSmbPort().equals("")) h_port = ":" + sti.getMasterSmbPort();
        final String remurl = "smb://" + t_remurl + h_port + "/" + sti.getMasterRemoteSmbShareName();
        final String remdir = "/" + sti.getMasterDirectoryName() + "/";
        final String smb_proto=sti.getMasterSmbProtocol();
        final boolean ipc_enforced=sti.isMasterSmbIpcSigningEnforced();

        NotifyEvent ntfy = new NotifyEvent(mContext);
        // set thread response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                @SuppressWarnings("unchecked")
                ArrayList<TreeFilelistItem> rfl = (ArrayList<TreeFilelistItem>) o[0];

                if (rfl.size()==0) {
                    String msg=mContext.getString(R.string.msgs_dir_empty);
                    commonDlg.showCommonDialog(false,"W",msg,"",null);
                    return;
                }

                //カスタムダイアログの生成
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setContentView(R.layout.item_select_list_dlg);

                LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_view);
                ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

                final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_title_view);
                final TextView title = (TextView) dialog.findViewById(R.id.item_select_list_dlg_title);
                final TextView subtitle = (TextView) dialog.findViewById(R.id.item_select_list_dlg_subtitle);
                title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
                title.setTextColor(mGp.themeColorList.text_color_dialog_title);
                subtitle.setTextColor(mGp.themeColorList.text_color_dialog_title);

                title.setText(mContext.getString(R.string.msgs_filter_list_dlg_add_dir_filter));
                subtitle.setText((remdir.equals("//")) ? remurl + "/" : remurl + remdir);
                final TextView dlg_msg = (TextView) dialog.findViewById(R.id.item_select_list_dlg_msg);
//                final LinearLayout ll_context = (LinearLayout) dialog.findViewById(R.id.context_view_file_select);
//                ll_context.setVisibility(LinearLayout.VISIBLE);
//                final ImageButton ib_select_all = (ImageButton) ll_context.findViewById(R.id.context_button_select_all);
//                final ImageButton ib_unselect_all = (ImageButton) ll_context.findViewById(R.id.context_button_unselect_all);
                final Button btn_ok = (Button) dialog.findViewById(R.id.item_select_list_dlg_ok_btn);
                dlg_msg.setVisibility(TextView.VISIBLE);

                CommonDialog.setDlgBoxSizeLimit(dialog, true);

                final ListView lv = (ListView) dialog.findViewById(android.R.id.list);
                final TreeFilelistAdapter tfa = new TreeFilelistAdapter(mContext, false, false);
                final ArrayList<TreeFilelistItem> rows = new ArrayList<TreeFilelistItem>();
                for (int i = 0; i < rfl.size(); i++) {
                    if (rfl.get(i).isDir() && rfl.get(i).canRead()) rows.add(rfl.get(i));
                }
                Collections.sort(rows);

                tfa.setDataList(rows);
                lv.setAdapter(tfa);
                lv.setScrollingCacheEnabled(false);
                lv.setScrollbarFadingEnabled(false);
//			    lv.setFastScrollEnabled(true);

                NotifyEvent ntfy_expand_close = new NotifyEvent(mContext);
                ntfy_expand_close.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        int idx = (Integer) o[0];
                        final int pos = tfa.getItem(idx);
                        final TreeFilelistItem tfi = tfa.getDataItem(pos);
                        expandHideRemoteDirTree(remurl, ipc_enforced, smb_proto, pos, tfi, tfa);
                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {}
                });
                tfa.setExpandCloseListener(ntfy_expand_close);
                lv.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                        final int pos = tfa.getItem(idx);
                        final TreeFilelistItem tfi = tfa.getDataItem(pos);
//						tfa.setDataItemIsSelected(pos);
                        expandHideRemoteDirTree(remurl, ipc_enforced, smb_proto, pos, tfi, tfa);
                    }
                });
                lv.setOnItemLongClickListener(new OnItemLongClickListener() {
                    public boolean onItemLongClick(AdapterView<?> items, View view, int idx, long id) {
                        return true;
                    }
                });

//                ib_select_all.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        for (int i = 0; i < tfa.getDataItemCount(); i++) {
//                            TreeFilelistItem tfli = tfa.getDataItem(i);
//                            if (!tfli.isHideListItem()) tfa.setDataItemIsSelected(i);
//                        }
//                        tfa.notifyDataSetChanged();
//                        btn_ok.setEnabled(true);
//                    }
//                });
//
//                ib_unselect_all.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        for (int i = 0; i < tfa.getDataItemCount(); i++) {
//                            tfa.setDataItemIsUnselected(i);
//                        }
//                        tfa.notifyDataSetChanged();
//                        btn_ok.setEnabled(false);
//                    }
//                });

                //OKボタンの指定
                btn_ok.setEnabled(false);
                NotifyEvent ntfy = new NotifyEvent(mContext);
                //Listen setRemoteShare response
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context arg0, Object[] o) {
                        Integer pos=(Integer)o[0];
                        boolean isChecked=(boolean)o[1];
                        TreeFilelistItem tfi=tfa.getDataItem(pos);
                        String sel="";
                        if (tfi.getPath().length() == 1) sel = tfi.getName();
                        else sel = tfi.getPath() + tfi.getName();
                        sel = sel.substring(remdir.length());
                        if (isFilterExists(sel, fla)) {
                            btn_ok.setEnabled(false);
                            tfi.setChecked(false);
                            tfa.notifyDataSetChanged();
                            String mtxt = mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified);
                            String dup_msg=String.format(mtxt, sel);

                            Toast.makeText(mContext, dup_msg, Toast.LENGTH_LONG).show();
                        } else {
                            btn_ok.setEnabled(true);
                        }
                    }

                    @Override
                    public void negativeResponse(Context arg0, Object[] arg1) {
                        if (tfa.isDataItemIsSelected()) btn_ok.setEnabled(true);
                        else btn_ok.setEnabled(false);
                    }
                });
                tfa.setCbCheckListener(ntfy);

                btn_ok.setText(mContext.getString(R.string.msgs_filter_list_dlg_add));
                btn_ok.setVisibility(Button.VISIBLE);
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (!addDirFilter(true, tfa, fla, remdir, dlg_msg, sti, true)) return;
                        addDirFilter(false, tfa, fla, remdir, dlg_msg, sti, true);
                        dialog.dismiss();
                        p_ntfy.notifyToListener(true, null);
                    }
                });
                //CANCELボタンの指定
                final Button btn_cancel = (Button) dialog.findViewById(R.id.item_select_list_dlg_cancel_btn);
                btn_cancel.setText(mContext.getString(R.string.msgs_filter_list_dlg_close));
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        p_ntfy.notifyToListener(true, null);
                    }
                });
                // Cancelリスナーの指定
                dialog.setOnCancelListener(new Dialog.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        btn_cancel.performClick();
                    }
                });
//				dialog.setOnKeyListener(new DialogOnKeyListener(context));
//				dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
                p_ntfy.notifyToListener(false, o);
            }
        });
        createRemoteFileList(remurl, remdir, ipc_enforced, smb_proto, ntfy, true);
    }

    @SuppressLint("DefaultLocale")
    private String isFilterSameDirectoryAccess(SyncTaskItem sti, AdapterFilterList filterAdapter) {
        if ((sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL) &&
                sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) ||
                (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD) &&
                        sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD))) {
            if (sti.getTargetDirectoryName().toLowerCase().startsWith(sti.getMasterDirectoryName().toLowerCase())) {
                for (int i = 0; i < filterAdapter.getCount(); i++) {
                    AdapterFilterList.FilterListItem fli = filterAdapter.getItem(i);
                    if (fli.isInclude() && !fli.isDeleted()) {
                        String filter = fli.getFilter();
                        if (fli.getFilter().indexOf("/") > 0)
                            filter = fli.getFilter().substring(0, fli.getFilter().indexOf("/"));
                        String dir = sti.getTargetDirectoryName();
                        if (sti.getTargetDirectoryName().indexOf("/") > 0)
                            dir = sti.getTargetDirectoryName().substring(0, sti.getTargetDirectoryName().indexOf("/"));
                        if (dir.toLowerCase().equals(filter.toLowerCase())) {
                            String mtxt = String.format(mContext.getString(R.string.msgs_filter_list_same_directory_access), fli.getFilter());
                            return mtxt;
                        }
                    }
                }
                boolean excluded = true;
                for (int i = 0; i < filterAdapter.getCount(); i++) {
                    AdapterFilterList.FilterListItem fli = filterAdapter.getItem(i);
                    if (!fli.isInclude() && !fli.isDeleted()) {
                        String dir = sti.getTargetDirectoryName();
                        if (sti.getTargetDirectoryName().indexOf("/") > 0)
                            dir = sti.getTargetDirectoryName().substring(0, sti.getTargetDirectoryName().indexOf("/"));
                        String filter = fli.getFilter();
                        if (dir.toLowerCase().equals(filter.toLowerCase())) {
                            excluded = true;
                            break;
                        } else excluded = false;
                    }
                }
                if (!excluded) {
                    String mtxt = String.format(mContext.getString(R.string.msgs_filter_list_same_directory_access_not_excluded), sti.getTargetDirectoryName());
                    return mtxt;
                }
            }
        }
        return "";
    }

    private boolean addDirFilter(boolean check_only, TreeFilelistAdapter tfa,
                                 AdapterFilterList fla, String cdir, TextView dlg_msg, SyncTaskItem sti, boolean smb_filter) {
        String sel = "", add_msg = "";
        //check duplicate entry
        for (int i = 0; i < tfa.getCount(); i++) {
            if (tfa.getDataItem(i).isChecked()) {
                if (tfa.getDataItem(i).getPath().length() == 1) sel = tfa.getDataItem(i).getName();
                else sel = tfa.getDataItem(i).getPath() + tfa.getDataItem(i).getName();
                sel = sel.substring(cdir.length());
                if (isFilterExists(sel, fla)) {
                    String mtxt = mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified);
                    dlg_msg.setText(String.format(mtxt, sel));
                    return false;
                } else {
                    dlg_msg.setText("");
                }
                if (!smb_filter) {
                }
                if (!check_only) {
                    fla.add(new AdapterFilterList.FilterListItem(sel, true));
                    if (add_msg.length() == 0) add_msg = sel;
                    else add_msg = add_msg + "," + sel;
                }
            }
        }
        if (!check_only) {
            fla.setNotifyOnChange(true);
            fla.sort(new Comparator<AdapterFilterList.FilterListItem>() {
                @Override
                public int compare(AdapterFilterList.FilterListItem lhs,
                                   AdapterFilterList.FilterListItem rhs) {
                    return lhs.getFilter().compareToIgnoreCase(rhs.getFilter());
                }

                ;
            });
            dlg_msg.setText(String.format(mContext.getString(R.string.msgs_filter_list_dlg_filter_added),
                    add_msg));
        }
        return true;
    }

    private String detectedInvalidChar = "", detectedInvalidCharMsg = "";

    public boolean hasInvalidChar(String in_text, String[] invchar) {
        for (int i = 0; i < invchar.length; i++) {
            if (in_text.indexOf(invchar[i]) >= 0) {
                if (invchar[i].equals("\t")) {
                    detectedInvalidCharMsg = "TAB";
                    detectedInvalidChar = "\t";
                } else {
                    detectedInvalidCharMsg = detectedInvalidChar = invchar[i];
                }
                return true;
            }

        }
        return false;
    }

    public String getInvalidCharMsg() {
        return detectedInvalidCharMsg;
    }

    public String removeInvalidChar(String in) {
        if (detectedInvalidChar == null || detectedInvalidChar.length() == 0) return in;
        String out = "";
        for (int i = 0; i < in.length(); i++) {
            if (in.substring(i, i + 1).equals(detectedInvalidChar)) {
                //ignore
            } else {
                out = out + in.substring(i, i + 1);
            }
        }
        return out;
    }

    public boolean isSyncTaskExists(String prof_name) {
        return isSyncTaskExists(prof_name, mGp.syncTaskAdapter.getArrayList());
    }

    static public boolean isSyncTaskExists(String prof_name, ArrayList<SyncTaskItem> pfl) {
        boolean dup = false;

        for (int i = 0; i <= pfl.size() - 1; i++) {
            SyncTaskItem item = pfl.get(i);
            String prof_chk = item.getSyncTaskName();
            if (prof_chk.equalsIgnoreCase(prof_name)) {
                dup = true;
                break;
            }
        }
        return dup;
    }

    static public boolean isSyncTaskAuto(GlobalParameters gp,
                                         String prof_name) {
        boolean active = false;

        for (int i = 0; i <= gp.syncTaskAdapter.getCount() - 1; i++) {
            String item_key = gp.syncTaskAdapter.getItem(i).getSyncTaskName();
            if (item_key.equalsIgnoreCase(prof_name)) {
                active = gp.syncTaskAdapter.getItem(i).isSyncTaskAuto();
            }
        }
        return active;
    }

    static public boolean isSyncTaskSelected(AdapterSyncTask pa) {
        boolean result = false;

        for (int i = 0; i < pa.getCount(); i++) {
            if (pa.getItem(i).isChecked()) {
                result = true;
                break;
            }
        }
        return result;
    }

    static public int getSyncTaskSelectedItemCount(AdapterSyncTask pa) {
        int result = 0;

        for (int i = 0; i < pa.getCount(); i++) {
            if (pa.getItem(i).isChecked()) {
                result++;
            }
        }
        return result;
    }

    public void scanRemoteNetworkDlg(final NotifyEvent p_ntfy,
                                     String port_number, boolean scan_start) {
        //カスタムダイアログの生成
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.scan_remote_ntwk_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.scan_remote_ntwk_dlg_view);
        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.scan_remote_ntwk_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.scan_remote_ntwk_title);
        title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
        title.setTextColor(mGp.themeColorList.text_color_dialog_title);

        final Button btn_scan = (Button) dialog.findViewById(R.id.scan_remote_ntwk_btn_ok);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.scan_remote_ntwk_btn_cancel);
        final TextView tvmsg = (TextView) dialog.findViewById(R.id.scan_remote_ntwk_msg);
        final TextView tv_result = (TextView) dialog.findViewById(R.id.scan_remote_ntwk_scan_result_title);
        tvmsg.setText(mContext.getString(R.string.msgs_scan_ip_address_press_scan_btn));
        tv_result.setVisibility(TextView.GONE);

        final String from = SyncUtil.getLocalIpAddress();
        String subnet = from.substring(0, from.lastIndexOf("."));
        String subnet_o1, subnet_o2, subnet_o3;
        subnet_o1 = subnet.substring(0, subnet.indexOf("."));
        subnet_o2 = subnet.substring(subnet.indexOf(".") + 1, subnet.lastIndexOf("."));
        subnet_o3 = subnet.substring(subnet.lastIndexOf(".") + 1, subnet.length());
        final EditText baEt1 = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_begin_address_o1);
        final EditText baEt2 = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_begin_address_o2);
        final EditText baEt3 = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_begin_address_o3);
        final EditText baEt4 = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_begin_address_o4);
        final EditText eaEt4 = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_end_address_o4);
        baEt1.setText(subnet_o1);
        baEt2.setText(subnet_o2);
        baEt3.setText(subnet_o3);
        baEt4.setText("1");
        baEt4.setSelection(1);
        eaEt4.setText("254");
        baEt4.requestFocus();

        final CheckedTextView ctv_use_port_number = (CheckedTextView) dialog.findViewById(R.id.scan_remote_ntwk_ctv_use_port);
        final EditText et_port_number = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_port_number);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        if (port_number.equals("")) {
            et_port_number.setEnabled(false);
            ctv_use_port_number.setChecked(false);
        } else {
            et_port_number.setEnabled(true);
            et_port_number.setText(port_number);
            ctv_use_port_number.setChecked(true);
        }
        ctv_use_port_number.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctv_use_port_number.toggle();
                boolean isChecked = ctv_use_port_number.isChecked();
                et_port_number.setEnabled(isChecked);
            }
        });

        final NotifyEvent ntfy_lv_click = new NotifyEvent(mContext);
        ntfy_lv_click.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                dialog.dismiss();
                p_ntfy.notifyToListener(true, o);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });

        final ArrayList<AdapterNetworkScanResult.NetworkScanListItem> ipAddressList = new ArrayList<AdapterNetworkScanResult.NetworkScanListItem>();
        final ListView lv = (ListView) dialog.findViewById(R.id.scan_remote_ntwk_scan_result_list);
        final AdapterNetworkScanResult adap = new AdapterNetworkScanResult
                (mContext, R.layout.scan_address_result_list_item, ipAddressList, ntfy_lv_click);
        lv.setAdapter(adap);
        lv.setScrollingCacheEnabled(false);
        lv.setScrollbarFadingEnabled(false);

        //SCANボタンの指定
        btn_scan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ipAddressList.clear();
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        if (ipAddressList.size() < 1) {
                            tvmsg.setText(mContext.getString(R.string.msgs_scan_ip_address_not_detected));
                            tv_result.setVisibility(TextView.GONE);
                        } else {
                            tvmsg.setText(mContext.getString(R.string.msgs_scan_ip_address_select_detected_host));
                            tv_result.setVisibility(TextView.VISIBLE);
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }

                });
                if (auditScanAddressRangeValue(dialog)) {
                    tv_result.setVisibility(TextView.GONE);
                    String ba1 = baEt1.getText().toString();
                    String ba2 = baEt2.getText().toString();
                    String ba3 = baEt3.getText().toString();
                    String ba4 = baEt4.getText().toString();
                    String ea4 = eaEt4.getText().toString();
                    String subnet = ba1 + "." + ba2 + "." + ba3;
                    int begin_addr = Integer.parseInt(ba4);
                    int end_addr = Integer.parseInt(ea4);
                    scanRemoteNetwork(dialog, lv, adap, ipAddressList,
                            subnet, begin_addr, end_addr, ntfy);
                } else {
                    //error
                }
            }
        });

        //CANCELボタンの指定
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                p_ntfy.notifyToListener(false, null);
            }
        });
        // Cancelリスナーの指定
        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                btn_cancel.performClick();
            }
        });
        dialog.show();

        if (scan_start) btn_scan.performClick();
    }

    private int mScanCompleteCount = 0, mScanAddrCount = 0;
    private ArrayList<String> mScanRequestedAddrList = new ArrayList<String>();
    private String mLockScanCompleteCount = "";

    private void scanRemoteNetwork(
            final Dialog dialog,
            final ListView lv_ipaddr,
            final AdapterNetworkScanResult adap,
            final ArrayList<AdapterNetworkScanResult.NetworkScanListItem> ipAddressList,
            final String subnet, final int begin_addr, final int end_addr,
            final NotifyEvent p_ntfy) {
        final Handler handler = new Handler();
        final ThreadCtrl tc = new ThreadCtrl();
        final LinearLayout ll_addr = (LinearLayout) dialog.findViewById(R.id.scan_remote_ntwk_scan_address);
        final LinearLayout ll_prog = (LinearLayout) dialog.findViewById(R.id.scan_remote_ntwk_progress);
        final TextView tvmsg = (TextView) dialog.findViewById(R.id.scan_remote_ntwk_progress_msg);
        final Button btn_scan = (Button) dialog.findViewById(R.id.scan_remote_ntwk_btn_ok);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.scan_remote_ntwk_btn_cancel);
        final Button scan_cancel = (Button) dialog.findViewById(R.id.scan_remote_ntwk_progress_cancel);

        final CheckedTextView ctv_use_port_number = (CheckedTextView) dialog.findViewById(R.id.scan_remote_ntwk_ctv_use_port);
        final EditText et_port_number = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_port_number);

        tvmsg.setText("");
        scan_cancel.setText(R.string.msgs_scan_progress_spin_dlg_addr_cancel);
        ll_addr.setVisibility(LinearLayout.GONE);
        ll_prog.setVisibility(LinearLayout.VISIBLE);
        btn_scan.setVisibility(Button.GONE);
        btn_cancel.setVisibility(Button.GONE);
        adap.setButtonEnabled(false);
        scan_cancel.setEnabled(true);
        dialog.setOnKeyListener(new DialogBackKeyListener(mContext));
        dialog.setCancelable(false);
        // CANCELボタンの指定
        scan_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scan_cancel.setText(mContext.getString(R.string.msgs_progress_dlg_canceling));
                scan_cancel.setEnabled(false);
                util.addDebugMsg(1, "W", "IP Address list creation was cancelled");
                tc.setDisabled();
            }
        });
        dialog.show();

        util.addDebugMsg(1, "I", "Scan IP address ransge is " + subnet + "." + begin_addr + " - " + end_addr);

        mScanRequestedAddrList.clear();

        final String scan_prog = mContext.getString(R.string.msgs_ip_address_scan_progress);
        String p_txt = String.format(scan_prog, 0);
        tvmsg.setText(p_txt);

        Thread th=new Thread(new Runnable() {
            @Override
            public void run() {//non UI thread
                mScanCompleteCount = 0;
                mScanAddrCount = end_addr - begin_addr + 1;
                int scan_thread = 60;
                String scan_port = "";
                if (ctv_use_port_number.isChecked())
                    scan_port = et_port_number.getText().toString();
                for (int i = begin_addr; i <= end_addr; i += scan_thread) {
                    if (!tc.isEnabled()) break;
                    boolean scan_end = false;
                    for (int j = i; j < (i + scan_thread); j++) {
                        if (j <= end_addr) {
                            startRemoteNetworkScanThread(handler, tc, dialog, p_ntfy,
                                    lv_ipaddr, adap, tvmsg, subnet + "." + j, ipAddressList, scan_port, JcifsFile.JCIFS_LEVEL_JCIFS1);
                        } else {
                            scan_end = true;
                        }
                    }
                    if (!scan_end) {
                        for (int wc = 0; wc < 210; wc++) {
                            if (!tc.isEnabled()) break;
                            SystemClock.sleep(30);
                        }
                    }
                }
                if (!tc.isEnabled()) {
                    for (int i = 0; i < 1000; i++) {
                        SystemClock.sleep(100);
                        synchronized (mScanRequestedAddrList) {
                            if (mScanRequestedAddrList.size() == 0) break;
                        }
                    }
                    handler.post(new Runnable() {// UI thread
                        @Override
                        public void run() {
                            closeScanRemoteNetworkProgressDlg(dialog, p_ntfy, lv_ipaddr, adap, tvmsg);
                        }
                    });
                } else {
                    for (int i = 0; i < 1000; i++) {
                        SystemClock.sleep(100);
                        synchronized (mScanRequestedAddrList) {
                            if (mScanRequestedAddrList.size() == 0) break;
                        }
                    }
                    handler.post(new Runnable() {// UI thread
                        @Override
                        public void run() {
                            synchronized (mLockScanCompleteCount) {
                                lv_ipaddr.setSelection(lv_ipaddr.getCount());
                                adap.notifyDataSetChanged();
                                closeScanRemoteNetworkProgressDlg(dialog, p_ntfy, lv_ipaddr, adap, tvmsg);
                            }
                        }
                    });
                }
            }
        });
        th.start();
    }

    private void closeScanRemoteNetworkProgressDlg(
            final Dialog dialog,
            final NotifyEvent p_ntfy,
            final ListView lv_ipaddr,
            final AdapterNetworkScanResult adap,
            final TextView tvmsg) {
        final LinearLayout ll_addr = (LinearLayout) dialog.findViewById(R.id.scan_remote_ntwk_scan_address);
        final LinearLayout ll_prog = (LinearLayout) dialog.findViewById(R.id.scan_remote_ntwk_progress);
        final Button btn_scan = (Button) dialog.findViewById(R.id.scan_remote_ntwk_btn_ok);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.scan_remote_ntwk_btn_cancel);
        ll_addr.setVisibility(LinearLayout.VISIBLE);
        ll_prog.setVisibility(LinearLayout.GONE);
        btn_scan.setVisibility(Button.VISIBLE);
        btn_cancel.setVisibility(Button.VISIBLE);
        adap.setButtonEnabled(true);
        dialog.setOnKeyListener(null);
        dialog.setCancelable(true);
        if (p_ntfy != null) p_ntfy.notifyToListener(true, null);

    }

    private void startRemoteNetworkScanThread(final Handler handler,
                                              final ThreadCtrl tc,
                                              final Dialog dialog,
                                              final NotifyEvent p_ntfy,
                                              final ListView lv_ipaddr,
                                              final AdapterNetworkScanResult adap,
                                              final TextView tvmsg,
                                              final String addr,
                                              final ArrayList<AdapterNetworkScanResult.NetworkScanListItem> ipAddressList,
                                              final String scan_port, final String cifs_level) {
        final String scan_prog = mContext.getString(R.string.msgs_ip_address_scan_progress);
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mScanRequestedAddrList) {
                    mScanRequestedAddrList.add(addr);
                }
                if (isIpAddrSmbHost(addr, scan_port)) {
                    final String srv_name = getSmbHostName(cifs_level, addr);
                    handler.post(new Runnable() {// UI thread
                        @Override
                        public void run() {
                            synchronized (mScanRequestedAddrList) {
                                mScanRequestedAddrList.remove(addr);
                                AdapterNetworkScanResult.NetworkScanListItem li = new AdapterNetworkScanResult.NetworkScanListItem();
                                li.server_address = addr;
                                li.server_name = srv_name;
                                ipAddressList.add(li);
                                Collections.sort(ipAddressList, new Comparator<AdapterNetworkScanResult.NetworkScanListItem>() {
                                    @Override
                                    public int compare(AdapterNetworkScanResult.NetworkScanListItem lhs,
                                                       AdapterNetworkScanResult.NetworkScanListItem rhs) {
                                        String r_o4 = rhs.server_address.substring(rhs.server_address.lastIndexOf(".") + 1);
                                        String r_key = String.format("%3s", Integer.parseInt(r_o4)).replace(" ", "0");
                                        String l_o4 = lhs.server_address.substring(lhs.server_address.lastIndexOf(".") + 1);
                                        String l_key = String.format("%3s", Integer.parseInt(l_o4)).replace(" ", "0");
//										Log.v("","r="+r_key+", l="+l_key);
                                        return l_key.compareTo(r_key);
                                    }
                                });
                                adap.notifyDataSetChanged();
                            }
                            synchronized (mLockScanCompleteCount) {
                                mScanCompleteCount++;
                            }
                        }
                    });
                } else {
                    synchronized (mScanRequestedAddrList) {
//						Log.v("","addr="+addr+", contained="+mScanRequestedAddrList.contains(addr));
                        mScanRequestedAddrList.remove(addr);
                    }
                    synchronized (mLockScanCompleteCount) {
                        mScanCompleteCount++;
                    }
                }
                handler.post(new Runnable() {// UI thread
                    @Override
                    public void run() {
                        synchronized (mLockScanCompleteCount) {
                            lv_ipaddr.setSelection(lv_ipaddr.getCount());
                            adap.notifyDataSetChanged();
                            String p_txt = String.format(scan_prog,
                                    (mScanCompleteCount * 100) / mScanAddrCount);
                            tvmsg.setText(p_txt);
                        }
                    }
                });
            }
        });
        th.start();
    }

    private boolean isIpAddrSmbHost(String address, String scan_port) {
        boolean smbhost = false;
        if (scan_port.equals("")) {
            if (!JcifsUtil.isIpAddressAndPortConnected(address, 139, 3000)) {
                smbhost = JcifsUtil.isIpAddressAndPortConnected(address, 445, 3000);
            } else smbhost = true;
        } else {
            smbhost = JcifsUtil.isIpAddressAndPortConnected(address,
                    Integer.parseInt(scan_port), 3000);
        }
        util.addDebugMsg(2, "I", "isIpAddrSmbHost Address=" + address +
                ", port=" + scan_port + ", smbhost=" + smbhost);
        return smbhost;
    }

    private String getSmbHostName(String cifs_level, String address) {
        String srv_name = JcifsUtil.getSmbHostNameFromAddress(cifs_level, address);
        util.addDebugMsg(1, "I", "getSmbHostName Address=" + address + ", name=" + srv_name);
        return srv_name;
    }

    private boolean auditScanAddressRangeValue(Dialog dialog) {
        boolean result = false;
        final EditText baEt1 = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_begin_address_o1);
        final EditText baEt2 = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_begin_address_o2);
        final EditText baEt3 = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_begin_address_o3);
        final EditText baEt4 = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_begin_address_o4);
        final EditText eaEt4 = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_end_address_o4);
        final TextView tvmsg = (TextView) dialog.findViewById(R.id.scan_remote_ntwk_msg);

        String ba1 = baEt1.getText().toString();
        String ba2 = baEt2.getText().toString();
        String ba3 = baEt3.getText().toString();
        String ba4 = baEt4.getText().toString();
        String ea4 = eaEt4.getText().toString();

        tvmsg.setText("");
        if (ba1.equals("")) {
            tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_begin_notspecified));
            baEt1.requestFocus();
            return false;
        } else if (ba2.equals("")) {
            tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_begin_notspecified));
            baEt2.requestFocus();
            return false;
        } else if (ba3.equals("")) {
            tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_begin_notspecified));
            baEt3.requestFocus();
            return false;
        } else if (ba4.equals("")) {
            tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_begin_notspecified));
            baEt4.requestFocus();
            return false;
        } else if (ea4.equals("")) {
            tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_end_notspecified));
            eaEt4.requestFocus();
            return false;
        }
        int iba1 = Integer.parseInt(ba1);
        if (iba1 > 255) {
            tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_addr_range_error));
            baEt1.requestFocus();
            return false;
        }
        int iba2 = Integer.parseInt(ba2);
        if (iba2 > 255) {
            tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_addr_range_error));
            baEt2.requestFocus();
            return false;
        }
        int iba3 = Integer.parseInt(ba3);
        if (iba3 > 255) {
            tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_addr_range_error));
            baEt3.requestFocus();
            return false;
        }
        int iba4 = Integer.parseInt(ba4);
        int iea4 = Integer.parseInt(ea4);
        if (iba4 > 0 && iba4 < 255) {
            if (iea4 > 0 && iea4 < 255) {
                if (iba4 <= iea4) {
                    result = true;
                } else {
                    baEt4.requestFocus();
                    tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_begin_addr_gt_end_addr));
                }
            } else {
                eaEt4.requestFocus();
                tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_end_range_error));
            }
        } else {
            baEt4.requestFocus();
            tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_begin_range_error));
        }

        if (iba1 == 192 && iba2 == 168) {
            //class c private
        } else {
            if (iba1 == 10) {
                //class a private
            } else {
                if (iba1 == 172 && (iba2 >= 16 && iba2 <= 31)) {
                    //class b private
                } else {
                    //not private
                    result = false;
                    tvmsg.setText(mContext.getString(R.string.msgs_ip_address_range_dlg_not_private));
                }
            }
        }

        return result;
    }

    private ArrayList<TreeFilelistItem> createLocalFilelist(boolean dironly,
                                                            String url, String dir) {

        ArrayList<TreeFilelistItem> tfl = new ArrayList<TreeFilelistItem>();
        ;
        String tdir, fp;

        if (dir.equals("")) fp = tdir = "/";
        else {
            tdir = dir;
            fp = dir + "/";
        }
        File lf = new File(url + tdir);
        final File[] ff = lf.listFiles();
        TreeFilelistItem tfi = null;
        if (ff != null) {
            for (int i = 0; i < ff.length; i++) {
//				Log.v("","name="+ff[i].getName()+", d="+ff[i].isDirectory()+", r="+ff[i].canRead());
                if (ff[i].canRead()) {
                    int dirct = 0;
                    if (ff[i].isDirectory()) {
                        File tlf = new File(url + tdir + "/" + ff[i].getName());
                        File[] lfl = tlf.listFiles();
                        if (lfl != null) {
                            for (int j = 0; j < lfl.length; j++) {
                                if (dironly) {
                                    if (lfl[j].isDirectory()) dirct++;
                                } else dirct++;
                            }
                        }
                    }
                    tfi = new TreeFilelistItem(ff[i].getName(),
                            "" + ", ", ff[i].isDirectory(), 0, 0, false,
                            ff[i].canRead(), ff[i].canWrite(),
                            ff[i].isHidden(), fp, 0);
                    tfi.setSubDirItemCount(dirct);

                    if (dironly) {
                        if (ff[i].isDirectory()) tfl.add(tfi);
                    } else tfl.add(tfi);
                }
            }
            Collections.sort(tfl);
        }
        return tfl;
    }

    private void createRemoteFileList(String remurl, String remdir, boolean ipc_enforced, String smb_proto,
                                      final NotifyEvent p_event, boolean readSubDirCnt) {
        final ArrayList<TreeFilelistItem> remoteFileList = new ArrayList<TreeFilelistItem>();
        final ThreadCtrl tc = new ThreadCtrl();
        tc.setEnabled();
        tc.setThreadResultSuccess();

        final Dialog dialog=showProgressSpinIndicator(mContext);

        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                tc.setDisabled();//disableAsyncTask();
//                btn_cancel.setText(mContext.getString(R.string.msgs_progress_dlg_canceling));
//                btn_cancel.setEnabled(false);
                util.addDebugMsg(1, "W", "Sharelist is cancelled.");
            }
        });

        final Handler hndl = new Handler();
        NotifyEvent ntfy = new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                hndl.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        String err;
                        util.addDebugMsg(1, "I", "FileListThread result=" + tc.getThreadResult() + "," +
                                "msg=" + tc.getThreadMessage() + ", enable=" +
                                tc.isEnabled());
                        if (tc.isThreadResultSuccess()) {
                            p_event.notifyToListener(true, new Object[]{remoteFileList});
                        } else {
                            if (tc.isThreadResultError()) {
                                err = mContext.getString(R.string.msgs_filelist_error) + "\n" + tc.getThreadMessage();
                                p_event.notifyToListener(false, new Object[]{err});
                            }
                        }
                    }
                });
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });

        RemoteAuthInfo ra=new RemoteAuthInfo();
        if (smbUser!=null && !smbUser.equals("")) ra.smb_user_name=smbUser;
        if (smbPass!=null && !smbPass.equals("")) ra.smb_user_password=smbPass;
        ra.smb_ipc_signing_enforced=ipc_enforced;
        ra.smb_smb_protocol=smb_proto;
        Thread tf = new Thread(new ReadSmbFilelist(mContext, tc, remurl, remdir, remoteFileList, ra, ntfy, true, readSubDirCnt, mGp));
        tf.start();

        dialog.show();
    }

    public Dialog showProgressSpinIndicator(Context c) {
        final Dialog dialog=new Dialog(c, android.R.style.Theme_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_spin_indicator_dlg);
//		RelativeLayout rl_view=(RelativeLayout)dialog.findViewById(R.id.progress_spin_indicator_dlg_view);
//		rl_view.setBackgroundColor(Color.TRANSPARENT);
//		ProgressBar pb_view=(ProgressBar)dialog.findViewById(R.id.progress_spin_indicator_dlg_progress_bar);
//		pb_view.setBackgroundColor(Color.TRANSPARENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public void selectRemoteShareDlg(final String remurl, boolean ipc_enforced, String smb_proto, final NotifyEvent p_ntfy) {

        NotifyEvent ntfy = new NotifyEvent(mContext);
        // set thread response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                final ArrayList<String> rows = new ArrayList<String>();
                @SuppressWarnings("unchecked")
                ArrayList<TreeFilelistItem> rfl = (ArrayList<TreeFilelistItem>) o[0];
                for (TreeFilelistItem item:rfl) rows.add(item.getName());
                if (rows.size() < 1) {
                    commonDlg.showCommonDialog(false, "W",
                            mContext.getString(R.string.msgs_share_list_not_obtained), "", null);
                    return;
                }
                Collections.sort(rows, String.CASE_INSENSITIVE_ORDER);
                //カスタムダイアログの生成
                final Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setContentView(R.layout.item_select_list_dlg);

                LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_view);
                ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

                final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_title_view);
                final TextView title = (TextView) dialog.findViewById(R.id.item_select_list_dlg_title);
                final TextView subtitle = (TextView) dialog.findViewById(R.id.item_select_list_dlg_subtitle);
                title_view.setBackgroundColor(mGp.themeColorList.dialog_title_background_color);
                title.setTextColor(mGp.themeColorList.text_color_dialog_title);
                subtitle.setTextColor(mGp.themeColorList.text_color_dialog_title);

                title.setText(mContext.getString(R.string.msgs_select_remote_share));
                subtitle.setVisibility(TextView.GONE);

                final Button btn_cancel = (Button) dialog.findViewById(R.id.item_select_list_dlg_cancel_btn);
                final Button btn_ok = (Button) dialog.findViewById(R.id.item_select_list_dlg_ok_btn);
                btn_ok.setEnabled(false);

                CommonDialog.setDlgBoxSizeLimit(dialog, false);

                final ListView lv = (ListView) dialog.findViewById(android.R.id.list);
                lv.setAdapter(new ArrayAdapter<String>(mContext,
//						R.layout.custom_simple_list_item_checked,rows));
                        android.R.layout.simple_list_item_single_choice, rows));
                lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                lv.setScrollingCacheEnabled(false);
                lv.setScrollbarFadingEnabled(false);

                lv.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                        btn_ok.setEnabled(true);
                    }
                });
                //CANCELボタンの指定
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        p_ntfy.notifyToListener(false, null);
                    }
                });
                //OKボタンの指定
                btn_ok.setVisibility(Button.VISIBLE);
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        SparseBooleanArray checked = lv.getCheckedItemPositions();
                        for (int i = 0; i <= rows.size(); i++) {
                            if (checked.get(i) == true) {
                                p_ntfy.notifyToListener(true, new Object[]{rows.get(i)});
                                break;
                            }
                        }
                    }
                });
                // Cancelリスナーの指定
                dialog.setOnCancelListener(new Dialog.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        btn_cancel.performClick();
                    }
                });
//				dialog.setOnKeyListener(new DialogOnKeyListener(context));
//				dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
                p_ntfy.notifyToListener(false, o);
            }
        });
        createRemoteFileList(remurl, null, ipc_enforced, smb_proto, ntfy, false);

    }

    private void expandHideRemoteDirTree(String remurl, boolean ipc_enforced, String smb_proto, final int pos,
                                         final TreeFilelistItem tfi, final TreeFilelistAdapter tfa) {
        if (tfi.getSubDirItemCount() == 0) return;
        if (tfi.isChildListExpanded()) {
            tfa.hideChildItem(tfi, pos);
        } else {
            if (tfi.isSubDirLoaded())
                tfa.reshowChildItem(tfi, pos);
            else {
                if (tfi.isSubDirLoaded())
                    tfa.reshowChildItem(tfi, pos);
                else {
                    NotifyEvent ne = new NotifyEvent(mContext);
                    ne.setListener(new NotifyEventListener() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void positiveResponse(Context c, Object[] o) {
                            tfa.addChildItem(tfi, (ArrayList<TreeFilelistItem>) o[0], pos);
                        }

                        @Override
                        public void negativeResponse(Context c, Object[] o) {
                        }
                    });
                    createRemoteFileList(remurl, tfi.getPath() + tfi.getName() + "/", ipc_enforced, smb_proto, ne, true);
                }
            }
        }
    }

    private void expandHideLocalDirTree(boolean dironly, String lclurl, final int pos,
                                        final TreeFilelistItem tfi, final TreeFilelistAdapter tfa) {
        if (tfi.getSubDirItemCount() == 0) return;
        if (tfi.isChildListExpanded()) {
            tfa.hideChildItem(tfi, pos);
        } else {
            if (tfi.isSubDirLoaded())
                tfa.reshowChildItem(tfi, pos);
            else {
                if (tfi.isSubDirLoaded()) tfa.reshowChildItem(tfi, pos);
                else {
                    ArrayList<TreeFilelistItem> ntfl =
                            createLocalFilelist(dironly, lclurl, tfi.getPath() + tfi.getName());
                    tfa.addChildItem(tfi, ntfl, pos);
                }
            }
        }
    }

    public static ArrayList<SyncTaskItem> createSyncTaskList(Context context, GlobalParameters gp, SyncUtil util) {
        return createSyncTaskListFromFile(context, gp, util, false, "", null);
    }

    public static ArrayList<SyncTaskItem> createSyncTaskListFromFile(Context context, GlobalParameters gp, SyncUtil util,
                                                                     boolean sdcard, String fp, ArrayList<PreferenceParmListIItem> ispl) {
        ArrayList<SyncTaskItem> sync = new ArrayList<SyncTaskItem>();
        if (ispl != null) ispl.clear();

        if (sdcard) {
            File sf = new File(fp);
            if (sf.exists()) {
                CipherParms cp = null;
                boolean prof_encrypted = isSyncTaskListFileEncrypted(fp);
                if (prof_encrypted) {
                    cp = EncryptUtil.initDecryptEnv(
                            gp.profileKeyPrefix + gp.profilePassword);
                }
                try {
                    BufferedReader br = new BufferedReader(new FileReader(fp), 8192);
                    String pl;
                    while ((pl = br.readLine()) != null) {
                        String prof_pre = "";
                        if (pl.startsWith(SMBSYNC2_PROF_VER1)) prof_pre = SMBSYNC2_PROF_VER1;
                        else if (pl.startsWith(SMBSYNC2_PROF_VER2)) prof_pre = SMBSYNC2_PROF_VER2;
                        else if (pl.startsWith(SMBSYNC2_PROF_VER3)) prof_pre = SMBSYNC2_PROF_VER3;
                        else if (pl.startsWith(SMBSYNC2_PROF_VER4)) prof_pre = SMBSYNC2_PROF_VER4;
                        else if (pl.startsWith(SMBSYNC2_PROF_VER5)) prof_pre = SMBSYNC2_PROF_VER5;
                        else if (pl.startsWith(SMBSYNC2_PROF_VER6)) prof_pre = SMBSYNC2_PROF_VER6;
                        if (!pl.startsWith(prof_pre + SMBSYNC2_PROF_ENC) &&
                                !pl.startsWith(prof_pre + SMBSYNC2_PROF_DEC)) {
                            if (prof_encrypted) {
                                String enc_str = pl.replace(prof_pre, "");
                                byte[] enc_array = Base64Compat.decode(enc_str, Base64Compat.NO_WRAP);
                                String dec_str = EncryptUtil.decrypt(enc_array, cp);
                                addSyncTaskList(prof_pre + dec_str, sync, ispl);
                            } else {
                                addSyncTaskList(pl, sync, ispl);
                            }
                        }
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                util.addLogMsg("E", String.format(context.getString(R.string.msgs_create_profile_not_found), fp));
            }
        } else {
            BufferedReader br;
            String pf = CURRENT_SMBSYNC2_PROFILE_FILE_NAME;
            try {

//				File lf1= new File(gp.applicationRootDirectory+"/"+ SMBSYNC2_PROFILE_FILE_NAME_V1);
                File lf2 = new File(gp.applicationRootDirectory + "/" + SMBSYNC2_PROFILE_FILE_NAME_V2);
                File lf3 = new File(gp.applicationRootDirectory + "/" + SMBSYNC2_PROFILE_FILE_NAME_V3);
                File lf4 = new File(gp.applicationRootDirectory + "/" + SMBSYNC2_PROFILE_FILE_NAME_V4);
                File lf5 = new File(gp.applicationRootDirectory + "/" + SMBSYNC2_PROFILE_FILE_NAME_V5);
                File lf6 = new File(gp.applicationRootDirectory + "/" + SMBSYNC2_PROFILE_FILE_NAME_V6);
                if (lf6.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V6;
                else if (lf5.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V5;
                else if (lf4.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V4;
                else if (lf3.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V3;
                else if (lf2.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V2;
                else pf = SMBSYNC2_PROFILE_FILE_NAME_V1;

                File lf = new File(gp.applicationRootDirectory + "/" + pf);

                if (lf.exists()) {
                    br = new BufferedReader(new FileReader(gp.applicationRootDirectory + "/" + pf), 8192);
                    String pl;
                    while ((pl = br.readLine()) != null) {
//						Log.v("","read pl="+pl);
                        addSyncTaskList(pl, sync, ispl);
                    }
                    br.close();
                } else {
                    util.addDebugMsg(1, "W", "profile not found, empty profile list created. fn=" +
                            gp.applicationRootDirectory + "/" + pf);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (sync.size() == 0) {
                if (BUILD_FOR_AMAZON) {
                    //アマゾン用はサンプルプロファイルを作成しない
                } else {
                    if (gp.sampleProfileCreateRequired) {
                        createSampleSyncTask(sync);
                        saveSyncTaskList(gp, context, util, sync);
                        gp.sampleProfileCreateRequired = false;
                    }
                }
            }

        }

        sortSyncTaskList(sync);

        for (SyncTaskItem sti : sync) {
            if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL) ||
                    sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                if (sti.getMasterLocalMountPoint().equals(""))
                    sti.setMasterLocalMountPoint(gp.internalRootDirectory);
            }
            if (sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL) ||
                    sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                if (sti.getTargetLocalMountPoint().equals(""))
                    sti.setTargetLocalMountPoint(gp.internalRootDirectory);
            }
        }

        return sync;
    }

    private static void createSampleSyncTask(ArrayList<SyncTaskItem> pfl) {
        SyncTaskItem stli = null;
        stli = new SyncTaskItem("DOWNLOAD-MY-PICTURE", true, false);
        stli.setMasterFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_SMB);
        stli.setMasterSmbAddr("192.168.0.2");
        stli.setMasterSmbUserName("TESTUSER");
        stli.setMasterSmbPassword("PSWD");
        stli.setMasterSmbShareName("SHARE");
        stli.setSyncTestMode(false);
        stli.setMasterDirectoryName("Android/Pictures");

        stli.setTargetFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
        stli.setTargetDirectoryName("Pictures");

        stli.setSyncUseExtendedDirectoryFilter1(true);
        stli.setSyncTaskPosition(0);
        pfl.add(stli);

        stli = new SyncTaskItem("BACKUP-MY-PICTURE", true, false);

        stli.setMasterFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
        stli.setMasterDirectoryName("DCIM");

        stli.setTargetFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_SMB);
        stli.setTargetRemoteAddr("192.168.0.2");
        stli.setTargetSmbUserName("TESTUSER");
        stli.setTargetSmbPassword("PSWD");
        stli.setTargetSmbShareName("SHARE");
        stli.setSyncTestMode(false);
        stli.setTargetDirectoryName("Android/DCIM");

        stli.setSyncUseExtendedDirectoryFilter1(true);
        stli.setSyncTaskPosition(1);
        pfl.add(stli);

        stli = new SyncTaskItem("BACKUP-TO-SDCARD", true, false);

        stli.setMasterFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
        stli.setMasterDirectoryName("Pictures");

        stli.setTargetFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD);
        stli.setTargetDirectoryName("Pictures");
        stli.setSyncTestMode(false);
        stli.setSyncWifiStatusOption("0");

        stli.setSyncUseExtendedDirectoryFilter1(true);
        stli.setSyncTaskPosition(2);
        pfl.add(stli);
    }

    static public void sortSyncTaskList(ArrayList<SyncTaskItem> items) {
        Collections.sort(items, new Comparator<SyncTaskItem>() {
            @Override
            public int compare(SyncTaskItem l_item, SyncTaskItem r_item) {

                if (l_item.getSyncTaskPosition() == r_item.getSyncTaskPosition())
                    return l_item.getSyncTaskName().compareToIgnoreCase(r_item.getSyncTaskName());
                else {
                    String l_key = String.format("%3d", l_item.getSyncTaskPosition()) + l_item.getSyncTaskName();
                    String r_key = String.format("%3d", r_item.getSyncTaskPosition()) + r_item.getSyncTaskName();
                    return l_key.compareToIgnoreCase(r_key);
                }
            }
        });
        for (int i = 0; i < items.size(); i++) items.get(i).setSyncTaskPosition(i);
    }

    ;

    private static void addSyncTaskList(String pl, ArrayList<SyncTaskItem> sync,
                                        ArrayList<PreferenceParmListIItem> ispl) {
//		Log.v("","l="+ispl);
        if (pl.startsWith(SMBSYNC2_PROF_VER1)) {
            if (pl.length() > 10) {
                addSyncTaskListVer1(pl.replace(SMBSYNC2_PROF_VER1, ""), sync);
                if (ispl != null) addImportSettingsParm(pl.replace(SMBSYNC2_PROF_VER1, ""), ispl);
            }
        } else if (pl.startsWith(SMBSYNC2_PROF_VER2)) {
            if (pl.length() > 10) {
                addSyncTaskListVer2(pl.replace(SMBSYNC2_PROF_VER2, ""), sync);
                if (ispl != null) addImportSettingsParm(pl.replace(SMBSYNC2_PROF_VER2, ""), ispl);
            }
        } else if (pl.startsWith(SMBSYNC2_PROF_VER3)) {
            if (pl.length() > 10) {
                addSyncTaskListVer3(pl.replace(SMBSYNC2_PROF_VER3, ""), sync);
                if (ispl != null) addImportSettingsParm(pl.replace(SMBSYNC2_PROF_VER3, ""), ispl);
            }
        } else if (pl.startsWith(SMBSYNC2_PROF_VER4)) {
            if (pl.length() > 10) {
                addSyncTaskListVer4(pl.replace(SMBSYNC2_PROF_VER4, ""), sync);
                if (ispl != null) addImportSettingsParm(pl.replace(SMBSYNC2_PROF_VER4, ""), ispl);
            }
        } else if (pl.startsWith(SMBSYNC2_PROF_VER5)) {
            if (pl.length() > 10) {
                addSyncTaskListVer5(pl.replace(SMBSYNC2_PROF_VER5, ""), sync);
                if (ispl != null) addImportSettingsParm(pl.replace(SMBSYNC2_PROF_VER5, ""), ispl);
            }
        } else if (pl.startsWith(SMBSYNC2_PROF_VER6)) {
            if (pl.length() > 10) {
                addSyncTaskListVer6(pl.replace(SMBSYNC2_PROF_VER6, ""), sync);
                if (ispl != null) addImportSettingsParm(pl.replace(SMBSYNC2_PROF_VER6, ""), ispl);
            }
        }

    }

    ;

    private static void addSyncTaskListVer1(String pl, ArrayList<SyncTaskItem> sync) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", npl = "";
        //Extract ArrayList<String> field
//		Log.v("","pl="+pl);
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");
//		Log.v("","npl="+npl);
//		Log.v("","list="+list+", length="+list.length());

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);
//		Log.v("","list1="+list1+", length="+list1.length());
//		Log.v("","list2="+list2+", length="+list2.length());
//		Log.v("","list3="+list3+", length="+list3.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
//			Log.v("","i="+i+", "+parm[i]);
        }

        if (parm[0].equals(SMBSYNC2_PROF_TYPE_SYNC)) {//Sync
            ArrayList<String> ff = new ArrayList<String>();
            ArrayList<String> df = new ArrayList<String>();
            ArrayList<String> wifi_wl = new ArrayList<String>();
            if (list1.length() != 0) {
                String[] fp = list1.split("\t");
                for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
            } else ff.clear();
            if (list2.length() != 0) {
                String[] dp = list2.split("\t");
                for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
            } else df.clear();
            if (list3.length() != 0) {
                String[] wl = list3.split("\t");
                for (int i = 0; i < wl.length; i++) wifi_wl.add(convertToSpecChar(wl[i]));
            } else wifi_wl.clear();

            SyncTaskItem stli = new SyncTaskItem(parm[1], parm[2].equals("0") ? false : true, false);
            stli.setSyncTaskType(parm[3]);

            stli.setMasterFolderType(parm[4]);
            stli.setMasterSmbUserName(parm[5]);
            stli.setMasterSmbPassword(parm[6]);
            stli.setMasterSmbShareName(parm[7]);
            stli.setMasterDirectoryName(parm[8]);
            stli.setMasterSmbAddr(parm[9]);
            stli.setMasterSmbPort(parm[10]);
            stli.setMasterSmbHostName(parm[11]);
            stli.setMasterSmbDomain(parm[12]);

            stli.setTargetFolderType(parm[13]);
            stli.setTargetSmbUserName(parm[14]);
            stli.setTargetSmbPassword(parm[15]);
            stli.setTargetSmbShareName(parm[16]);
            stli.setTargetDirectoryName(parm[17]);
            stli.setTargetRemoteAddr(parm[18]);
            stli.setTargetRemotePort(parm[19]);
            stli.setTargetRemoteHostname(parm[20]);
            stli.setTargetRemoteDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncWifiConnectionWhiteList(wifi_wl);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetLastModifiedSmbFile(parm[26].equals("1") ? true : false);

            stli.setSyncRetryCount(parm[27]);

            stli.setSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            stli.setSyncDifferentFileAllowableTime(Integer.parseInt(parm[34]));
            stli.setSyncDifferentFileByModTime(parm[35].equals("1") ? true : false);

            stli.setSyncUseFileCopyByTempName(parm[36].equals("1") ? true : false);
            stli.setSyncWifiStatusOption(parm[37]);

            stli.setLastSyncTime(parm[38]);
            stli.setLastSyncResult(Integer.parseInt(parm[39]));

            if (!parm[40].equals("") && !parm[40].equals("end"))
                stli.setSyncTaskPosition(Integer.parseInt(parm[40]));

//			if (!parm[41].equals("") && !parm[41].equals("end")) stli.setMasterFolderUseInternalUsbFolder(parm[41].equals("1")?true:false);
//			if (!parm[42].equals("") && !parm[42].equals("end")) stli.setTargetFolderUseInternalUsbFolder(parm[42].equals("1")?true:false);

            sync.add(stli);
        }
    }

    ;

    private static void addSyncTaskListVer2(String pl, ArrayList<SyncTaskItem> sync) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", npl = "";
        //Extract ArrayList<String> field
//		Log.v("","pl="+pl);
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");
//		Log.v("","npl="+npl);
//		Log.v("","list="+list+", length="+list.length());

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);
//		Log.v("","list1="+list1+", length="+list1.length());
//		Log.v("","list2="+list2+", length="+list2.length());
//		Log.v("","list3="+list3+", length="+list3.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
//			Log.v("","i="+i+", "+parm[i]);
        }

        if (parm[0].equals(SMBSYNC2_PROF_TYPE_SYNC)) {//Sync
            ArrayList<String> ff = new ArrayList<String>();
            ArrayList<String> df = new ArrayList<String>();
            ArrayList<String> wifi_wl = new ArrayList<String>();
            if (list1.length() != 0) {
                String[] fp = list1.split("\t");
                for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
            } else ff.clear();
            if (list2.length() != 0) {
                String[] dp = list2.split("\t");
                for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
            } else df.clear();
            if (list3.length() != 0) {
                String[] wl = list3.split("\t");
                for (int i = 0; i < wl.length; i++) wifi_wl.add(convertToSpecChar(wl[i]));
            } else wifi_wl.clear();

            SyncTaskItem stli = new SyncTaskItem(parm[1], parm[2].equals("0") ? false : true, false);
            stli.setSyncTaskType(parm[3]);

            stli.setMasterFolderType(parm[4]);
            stli.setMasterSmbUserName(parm[5]);
            stli.setMasterSmbPassword(parm[6]);
            stli.setMasterSmbShareName(parm[7]);
            stli.setMasterDirectoryName(parm[8]);
            stli.setMasterSmbAddr(parm[9]);
            stli.setMasterSmbPort(parm[10]);
            stli.setMasterSmbHostName(parm[11]);
            stli.setMasterSmbDomain(parm[12]);

            stli.setTargetFolderType(parm[13]);
            stli.setTargetSmbUserName(parm[14]);
            stli.setTargetSmbPassword(parm[15]);
            stli.setTargetSmbShareName(parm[16]);
            stli.setTargetDirectoryName(parm[17]);
            stli.setTargetRemoteAddr(parm[18]);
            stli.setTargetRemotePort(parm[19]);
            stli.setTargetRemoteHostname(parm[20]);
            stli.setTargetRemoteDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncWifiConnectionWhiteList(wifi_wl);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetLastModifiedSmbFile(parm[26].equals("1") ? true : false);

            stli.setSyncRetryCount(parm[27]);

            stli.setSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            stli.setSyncDifferentFileAllowableTime(Integer.parseInt(parm[34]));
            stli.setSyncDifferentFileByModTime(parm[35].equals("1") ? true : false);

            stli.setSyncUseFileCopyByTempName(parm[36].equals("1") ? true : false);
            stli.setSyncWifiStatusOption(parm[37]);

            stli.setLastSyncTime(parm[38]);
            stli.setLastSyncResult(Integer.parseInt(parm[39]));

            if (!parm[40].equals("") && !parm[40].equals("end"))
                stli.setSyncTaskPosition(Integer.parseInt(parm[40]));

//			if (!parm[41].equals("") && !parm[41].equals("end")) stli.setMasterFolderUseInternalUsbFolder(parm[41].equals("1")?true:false);
//			if (!parm[42].equals("") && !parm[42].equals("end")) stli.setTargetFolderUseInternalUsbFolder(parm[42].equals("1")?true:false);

            if (!parm[43].equals("") && !parm[43].equals("end"))
                stli.setMasterRemovableStorageID(parm[43]);
            if (!parm[44].equals("") && !parm[44].equals("end"))
                stli.setTargetRemovableStorageID(parm[44]);

            sync.add(stli);
        }
    }

    ;

    private static void addSyncTaskListVer3(String pl, ArrayList<SyncTaskItem> sync) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", npl = "";
        //Extract ArrayList<String> field
//		Log.v("","pl="+pl);
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");
//		Log.v("","npl="+npl);
//		Log.v("","list="+list+", length="+list.length());

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);
//		Log.v("","list1="+list1+", length="+list1.length());
//		Log.v("","list2="+list2+", length="+list2.length());
//		Log.v("","list3="+list3+", length="+list3.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
//			Log.v("","i="+i+", "+parm[i]);
        }

        if (parm[0].equals(SMBSYNC2_PROF_TYPE_SYNC)) {//Sync
            ArrayList<String> ff = new ArrayList<String>();
            ArrayList<String> df = new ArrayList<String>();
            ArrayList<String> wifi_wl = new ArrayList<String>();
            if (list1.length() != 0) {
                String[] fp = list1.split("\t");
                for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
            } else ff.clear();
            if (list2.length() != 0) {
                String[] dp = list2.split("\t");
                for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
            } else df.clear();
            if (list3.length() != 0) {
                String[] wl = list3.split("\t");
                for (int i = 0; i < wl.length; i++) wifi_wl.add(convertToSpecChar(wl[i]));
            } else wifi_wl.clear();

            SyncTaskItem stli = new SyncTaskItem(parm[1], parm[2].equals("0") ? false : true, false);
            stli.setSyncTaskType(parm[3]);

            stli.setMasterFolderType(parm[4]);
            stli.setMasterSmbUserName(parm[5]);
            stli.setMasterSmbPassword(parm[6]);
            stli.setMasterSmbShareName(parm[7]);
            stli.setMasterDirectoryName(parm[8]);
            stli.setMasterSmbAddr(parm[9]);
            stli.setMasterSmbPort(parm[10]);
            stli.setMasterSmbHostName(parm[11]);
            stli.setMasterSmbDomain(parm[12]);

            stli.setTargetFolderType(parm[13]);
            stli.setTargetSmbUserName(parm[14]);
            stli.setTargetSmbPassword(parm[15]);
            stli.setTargetSmbShareName(parm[16]);
            stli.setTargetDirectoryName(parm[17]);
            stli.setTargetRemoteAddr(parm[18]);
            stli.setTargetRemotePort(parm[19]);
            stli.setTargetRemoteHostname(parm[20]);
            stli.setTargetRemoteDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncWifiConnectionWhiteList(wifi_wl);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetLastModifiedSmbFile(parm[26].equals("1") ? true : false);

            stli.setSyncRetryCount(parm[27]);

            stli.setSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            stli.setSyncDifferentFileAllowableTime(Integer.parseInt(parm[34]));
            stli.setSyncDifferentFileByModTime(parm[35].equals("1") ? true : false);

            stli.setSyncUseFileCopyByTempName(parm[36].equals("1") ? true : false);
            stli.setSyncWifiStatusOption(parm[37]);

            stli.setLastSyncTime(parm[38]);
            stli.setLastSyncResult(Integer.parseInt(parm[39]));

            if (!parm[40].equals("") && !parm[40].equals("end"))
                stli.setSyncTaskPosition(Integer.parseInt(parm[40]));

//			if (!parm[41].equals("") && !parm[41].equals("end")) stli.setMasterFolderUseInternalUsbFolder(parm[41].equals("1")?true:false);
//			if (!parm[42].equals("") && !parm[42].equals("end")) stli.setTargetFolderUseInternalUsbFolder(parm[42].equals("1")?true:false);

            if (!parm[43].equals("") && !parm[43].equals("end"))
                stli.setMasterRemovableStorageID(parm[43]);
            if (!parm[44].equals("") && !parm[44].equals("end"))
                stli.setTargetRemovableStorageID(parm[44]);

            if (!parm[45].equals("") && !parm[45].equals("end"))
                stli.setSyncFileTypeAudio(parm[45].equals("1") ? true : false);
            if (!parm[46].equals("") && !parm[46].equals("end"))
                stli.setSyncFileTypeImage(parm[46].equals("1") ? true : false);
            if (!parm[47].equals("") && !parm[47].equals("end"))
                stli.setSyncFileTypeVideo(parm[47].equals("1") ? true : false);

            sync.add(stli);
        }
    }

    ;

    private static void addSyncTaskListVer4(String pl, ArrayList<SyncTaskItem> sync) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", npl = "";
        //Extract ArrayList<String> field
//		Log.v("","pl="+pl);
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");
//		Log.v("","npl="+npl);
//		Log.v("","list="+list+", length="+list.length());

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);
//		Log.v("","list1="+list1+", length="+list1.length());
//		Log.v("","list2="+list2+", length="+list2.length());
//		Log.v("","list3="+list3+", length="+list3.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
//			Log.v("","i="+i+", "+parm[i]);
        }

        if (parm[0].equals(SMBSYNC2_PROF_TYPE_SYNC)) {//Sync
            ArrayList<String> ff = new ArrayList<String>();
            ArrayList<String> df = new ArrayList<String>();
            ArrayList<String> wifi_wl = new ArrayList<String>();
            if (list1.length() != 0) {
                String[] fp = list1.split("\t");
                for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
            } else ff.clear();
            if (list2.length() != 0) {
                String[] dp = list2.split("\t");
                for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
            } else df.clear();
            if (list3.length() != 0) {
                String[] wl = list3.split("\t");
                for (int i = 0; i < wl.length; i++) wifi_wl.add(convertToSpecChar(wl[i]));
            } else wifi_wl.clear();

            SyncTaskItem stli = new SyncTaskItem(parm[1], parm[2].equals("0") ? false : true, false);
            stli.setSyncTaskType(parm[3]);

            stli.setMasterFolderType(parm[4]);
            stli.setMasterSmbUserName(parm[5]);
            stli.setMasterSmbPassword(parm[6]);
            stli.setMasterSmbShareName(parm[7]);
            stli.setMasterDirectoryName(parm[8]);
            stli.setMasterSmbAddr(parm[9]);
            stli.setMasterSmbPort(parm[10]);
            stli.setMasterSmbHostName(parm[11]);
            stli.setMasterSmbDomain(parm[12]);

            stli.setTargetFolderType(parm[13]);
            stli.setTargetSmbUserName(parm[14]);
            stli.setTargetSmbPassword(parm[15]);
            stli.setTargetSmbShareName(parm[16]);
            stli.setTargetDirectoryName(parm[17]);
            stli.setTargetRemoteAddr(parm[18]);
            stli.setTargetRemotePort(parm[19]);
            stli.setTargetRemoteHostname(parm[20]);
            stli.setTargetRemoteDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncWifiConnectionWhiteList(wifi_wl);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetLastModifiedSmbFile(parm[26].equals("1") ? true : false);

            stli.setSyncRetryCount(parm[27]);

            stli.setSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            stli.setSyncDifferentFileAllowableTime(Integer.parseInt(parm[34]));
            stli.setSyncDifferentFileByModTime(parm[35].equals("1") ? true : false);

            stli.setSyncUseFileCopyByTempName(parm[36].equals("1") ? true : false);
            stli.setSyncWifiStatusOption(parm[37]);

            stli.setLastSyncTime(parm[38]);
            stli.setLastSyncResult(Integer.parseInt(parm[39]));

            if (!parm[40].equals("") && !parm[40].equals("end"))
                stli.setSyncTaskPosition(Integer.parseInt(parm[40]));

//			if (!parm[41].equals("") && !parm[41].equals("end")) stli.setMasterFolderUseInternalUsbFolder(parm[41].equals("1")?true:false);
//			if (!parm[42].equals("") && !parm[42].equals("end")) stli.setTargetFolderUseInternalUsbFolder(parm[42].equals("1")?true:false);

            if (!parm[43].equals("") && !parm[43].equals("end"))
                stli.setMasterRemovableStorageID(parm[43]);
            if (!parm[44].equals("") && !parm[44].equals("end"))
                stli.setTargetRemovableStorageID(parm[44]);

            if (!parm[45].equals("") && !parm[45].equals("end"))
                stli.setSyncFileTypeAudio(parm[45].equals("1") ? true : false);
            if (!parm[46].equals("") && !parm[46].equals("end"))
                stli.setSyncFileTypeImage(parm[46].equals("1") ? true : false);
            if (!parm[47].equals("") && !parm[47].equals("end"))
                stli.setSyncFileTypeVideo(parm[47].equals("1") ? true : false);

            if (!parm[48].equals("") && !parm[48].equals("end"))
                stli.setTargetZipOutputFileName(parm[48]);
            if (!parm[49].equals("") && !parm[49].equals("end"))
                stli.setTargetZipCompressionLevel(parm[49]);
            if (!parm[50].equals("") && !parm[50].equals("end"))
                stli.setTargetZipCompressionMethod(parm[50]);
            if (!parm[51].equals("") && !parm[51].equals("end"))
                stli.setTargetZipEncryptMethod(parm[51]);
            if (!parm[52].equals("") && !parm[52].equals("end"))
                stli.setTargetZipPassword(parm[52]);
            if (!parm[53].equals("") && !parm[53].equals("end"))
                stli.setSyncTaskSkipIfConnectAnotherWifiSsid(parm[53].equals("1") ? true : false);

//			if (!parm[54].equals("") && !parm[54].equals("end")) stli.setTargetZipUseInternalUsbFolder(parm[54].equals("1")?true:false);
            if (!parm[55].equals("") && !parm[55].equals("end"))
                stli.setTargetZipUseExternalSdcard(parm[55].equals("1") ? true : false);

            if (!parm[56].equals("") && !parm[56].equals("end"))
                stli.setSyncTaskTwoWay(parm[56].equals("1") ? true : false);
            if (!parm[57].equals("") && !parm[57].equals("end"))
                stli.setSyncTwoWayConflictOption(parm[57]);

            if (!parm[58].equals("") && !parm[58].equals("end"))
                stli.setTargetZipFileNameEncoding(parm[58]);

            if (!parm[59].equals("") && !parm[59].equals("end"))
                stli.setSyncDifferentFileBySize((parm[59].equals("1") ? true : false));

            sync.add(stli);
        }
    }

    ;

    private static void addSyncTaskListVer5(String pl, ArrayList<SyncTaskItem> sync) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", npl = "";
        //Extract ArrayList<String> field
//		Log.v("","pl="+pl);
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");
//		Log.v("","npl="+npl);
//		Log.v("","list="+list+", length="+list.length());

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);
//		Log.v("","list1="+list1+", length="+list1.length());
//		Log.v("","list2="+list2+", length="+list2.length());
//		Log.v("","list3="+list3+", length="+list3.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
//			Log.v("","i="+i+", "+parm[i]);
        }

        if (parm[0].equals(SMBSYNC2_PROF_TYPE_SYNC)) {//Sync
            ArrayList<String> ff = new ArrayList<String>();
            ArrayList<String> df = new ArrayList<String>();
            ArrayList<String> wifi_wl = new ArrayList<String>();
            if (list1.length() != 0) {
                String[] fp = list1.split("\t");
                for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
            } else ff.clear();
            if (list2.length() != 0) {
                String[] dp = list2.split("\t");
                for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
            } else df.clear();
            if (list3.length() != 0) {
                String[] wl = list3.split("\t");
                for (int i = 0; i < wl.length; i++) wifi_wl.add(convertToSpecChar(wl[i]));
            } else wifi_wl.clear();

            SyncTaskItem stli = new SyncTaskItem(parm[1], parm[2].equals("0") ? false : true, false);
            stli.setSyncTaskType(parm[3]);

            stli.setMasterFolderType(parm[4]);
            stli.setMasterSmbUserName(parm[5]);
            stli.setMasterSmbPassword(parm[6]);
            stli.setMasterSmbShareName(parm[7]);
            stli.setMasterDirectoryName(parm[8]);
            stli.setMasterSmbAddr(parm[9]);
            stli.setMasterSmbPort(parm[10]);
            stli.setMasterSmbHostName(parm[11]);
            stli.setMasterSmbDomain(parm[12]);

            stli.setTargetFolderType(parm[13]);
            stli.setTargetSmbUserName(parm[14]);
            stli.setTargetSmbPassword(parm[15]);
            stli.setTargetSmbShareName(parm[16]);
            stli.setTargetDirectoryName(parm[17]);
            stli.setTargetRemoteAddr(parm[18]);
            stli.setTargetRemotePort(parm[19]);
            stli.setTargetRemoteHostname(parm[20]);
            stli.setTargetRemoteDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncWifiConnectionWhiteList(wifi_wl);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetLastModifiedSmbFile(parm[26].equals("1") ? true : false);

            stli.setSyncRetryCount(parm[27]);

            stli.setSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            stli.setSyncDifferentFileAllowableTime(Integer.parseInt(parm[34]));
            stli.setSyncDifferentFileByModTime(parm[35].equals("1") ? true : false);

            stli.setSyncUseFileCopyByTempName(parm[36].equals("1") ? true : false);
            stli.setSyncWifiStatusOption(parm[37]);

            stli.setLastSyncTime(parm[38]);
            stli.setLastSyncResult(Integer.parseInt(parm[39]));

            if (!parm[40].equals("") && !parm[40].equals("end"))
                stli.setSyncTaskPosition(Integer.parseInt(parm[40]));

//            if (!parm[41].equals("") && !parm[41].equals("end")) stli.setMasterFolderUseInternalUsbFolder(parm[41].equals("1")?true:false);
//            if (!parm[42].equals("") && !parm[42].equals("end")) stli.setTargetFolderUseInternalUsbFolder(parm[42].equals("1")?true:false);

            if (!parm[43].equals("") && !parm[43].equals("end"))
                stli.setMasterRemovableStorageID(parm[43]);
            if (!parm[44].equals("") && !parm[44].equals("end"))
                stli.setTargetRemovableStorageID(parm[44]);

            if (!parm[45].equals("") && !parm[45].equals("end"))
                stli.setSyncFileTypeAudio(parm[45].equals("1") ? true : false);
            if (!parm[46].equals("") && !parm[46].equals("end"))
                stli.setSyncFileTypeImage(parm[46].equals("1") ? true : false);
            if (!parm[47].equals("") && !parm[47].equals("end"))
                stli.setSyncFileTypeVideo(parm[47].equals("1") ? true : false);

            if (!parm[48].equals("") && !parm[48].equals("end"))
                stli.setTargetZipOutputFileName(parm[48]);
            if (!parm[49].equals("") && !parm[49].equals("end"))
                stli.setTargetZipCompressionLevel(parm[49]);
            if (!parm[50].equals("") && !parm[50].equals("end"))
                stli.setTargetZipCompressionMethod(parm[50]);
            if (!parm[51].equals("") && !parm[51].equals("end"))
                stli.setTargetZipEncryptMethod(parm[51]);
            if (!parm[52].equals("") && !parm[52].equals("end"))
                stli.setTargetZipPassword(parm[52]);
            if (!parm[53].equals("") && !parm[53].equals("end"))
                stli.setSyncTaskSkipIfConnectAnotherWifiSsid(parm[53].equals("1") ? true : false);

//            if (!parm[54].equals("") && !parm[54].equals("end")) stli.setTargetZipUseInternalUsbFolder(parm[54].equals("1")?true:false);
            if (!parm[55].equals("") && !parm[55].equals("end"))
                stli.setTargetZipUseExternalSdcard(parm[55].equals("1") ? true : false);

            if (!parm[56].equals("") && !parm[56].equals("end"))
                stli.setSyncTaskTwoWay(parm[56].equals("1") ? true : false);
            if (!parm[57].equals("") && !parm[57].equals("end"))
                stli.setSyncTwoWayConflictOption(parm[57]);

            if (!parm[58].equals("") && !parm[58].equals("end"))
                stli.setTargetZipFileNameEncoding(parm[58]);

            if (!parm[59].equals("") && !parm[59].equals("end"))
                stli.setSyncDifferentFileBySize((parm[59].equals("1") ? true : false));

            if (!parm[60].equals("") && !parm[60].equals("end"))
                stli.setSyncUseExtendedDirectoryFilter1((parm[60].equals("1") ? true : false));

            sync.add(stli);
        }
    }

    ;

    private static void addSyncTaskListVer6(String pl, ArrayList<SyncTaskItem> sync) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", npl = "";
        //Extract ArrayList<String> field
//		Log.v("","pl="+pl);
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");
//		Log.v("","npl="+npl);
//		Log.v("","list="+list+", length="+list.length());

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);
//		Log.v("","list1="+list1+", length="+list1.length());
//		Log.v("","list2="+list2+", length="+list2.length());
//		Log.v("","list3="+list3+", length="+list3.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
//			Log.v("","i="+i+", "+parm[i]);
        }

        if (parm[0].equals(SMBSYNC2_PROF_TYPE_SYNC)) {//Sync
            ArrayList<String> ff = new ArrayList<String>();
            ArrayList<String> df = new ArrayList<String>();
            ArrayList<String> wifi_wl = new ArrayList<String>();
            if (list1.length() != 0) {
                String[] fp = list1.split("\t");
                for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
            } else ff.clear();
            if (list2.length() != 0) {
                String[] dp = list2.split("\t");
                for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
            } else df.clear();
            if (list3.length() != 0) {
                String[] wl = list3.split("\t");
                for (int i = 0; i < wl.length; i++) wifi_wl.add(convertToSpecChar(wl[i]));
            } else wifi_wl.clear();

            SyncTaskItem stli = new SyncTaskItem(parm[1], parm[2].equals("0") ? false : true, false);
            stli.setSyncTaskType(parm[3]);

            stli.setMasterFolderType(parm[4]);
            stli.setMasterSmbUserName(parm[5]);
            stli.setMasterSmbPassword(parm[6]);
            stli.setMasterSmbShareName(parm[7]);
            stli.setMasterDirectoryName(parm[8]);
            stli.setMasterSmbAddr(parm[9]);
            stli.setMasterSmbPort(parm[10]);
            stli.setMasterSmbHostName(parm[11]);
            stli.setMasterSmbDomain(parm[12]);

            stli.setTargetFolderType(parm[13]);
            stli.setTargetSmbUserName(parm[14]);
            stli.setTargetSmbPassword(parm[15]);
            stli.setTargetSmbShareName(parm[16]);
            stli.setTargetDirectoryName(parm[17]);
            stli.setTargetRemoteAddr(parm[18]);
            stli.setTargetRemotePort(parm[19]);
            stli.setTargetRemoteHostname(parm[20]);
            stli.setTargetRemoteDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncWifiConnectionWhiteList(wifi_wl);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetLastModifiedSmbFile(parm[26].equals("1") ? true : false);

            stli.setSyncRetryCount(parm[27]);

            stli.setSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            stli.setSyncDifferentFileAllowableTime(Integer.parseInt(parm[34]));
            stli.setSyncDifferentFileByModTime(parm[35].equals("1") ? true : false);

            stli.setSyncUseFileCopyByTempName(parm[36].equals("1") ? true : false);
            stli.setSyncWifiStatusOption(parm[37]);

            stli.setLastSyncTime(parm[38]);
            stli.setLastSyncResult(Integer.parseInt(parm[39]));

            if (!parm[40].equals("") && !parm[40].equals("end"))
                stli.setSyncTaskPosition(Integer.parseInt(parm[40]));

//            if (!parm[41].equals("") && !parm[41].equals("end")) stli.setMasterFolderUseInternalUsbFolder(parm[41].equals("1")?true:false);
//            if (!parm[42].equals("") && !parm[42].equals("end")) stli.setTargetFolderUseInternalUsbFolder(parm[42].equals("1")?true:false);

            if (!parm[43].equals("") && !parm[43].equals("end"))
                stli.setMasterRemovableStorageID(parm[43]);
            if (!parm[44].equals("") && !parm[44].equals("end"))
                stli.setTargetRemovableStorageID(parm[44]);

            if (!parm[45].equals("") && !parm[45].equals("end"))
                stli.setSyncFileTypeAudio(parm[45].equals("1") ? true : false);
            if (!parm[46].equals("") && !parm[46].equals("end"))
                stli.setSyncFileTypeImage(parm[46].equals("1") ? true : false);
            if (!parm[47].equals("") && !parm[47].equals("end"))
                stli.setSyncFileTypeVideo(parm[47].equals("1") ? true : false);

            if (!parm[48].equals("") && !parm[48].equals("end"))
                stli.setTargetZipOutputFileName(parm[48]);
            if (!parm[49].equals("") && !parm[49].equals("end"))
                stli.setTargetZipCompressionLevel(parm[49]);
            if (!parm[50].equals("") && !parm[50].equals("end"))
                stli.setTargetZipCompressionMethod(parm[50]);
            if (!parm[51].equals("") && !parm[51].equals("end"))
                stli.setTargetZipEncryptMethod(parm[51]);
            if (!parm[52].equals("") && !parm[52].equals("end"))
                stli.setTargetZipPassword(parm[52]);
            if (!parm[53].equals("") && !parm[53].equals("end"))
                stli.setSyncTaskSkipIfConnectAnotherWifiSsid(parm[53].equals("1") ? true : false);

            if (!parm[54].equals("") && !parm[54].equals("end"))
                stli.setSyncOptionSyncWhenCharging(parm[54].equals("1") ? true : false);

            if (!parm[55].equals("") && !parm[55].equals("end"))
                stli.setTargetZipUseExternalSdcard(parm[55].equals("1") ? true : false);

            if (!parm[56].equals("") && !parm[56].equals("end"))
                stli.setSyncTaskTwoWay(parm[56].equals("1") ? true : false);
            if (!parm[57].equals("") && !parm[57].equals("end"))
                stli.setSyncTwoWayConflictOption(parm[57]);

            if (!parm[58].equals("") && !parm[58].equals("end"))
                stli.setTargetZipFileNameEncoding(parm[58]);

            if (!parm[59].equals("") && !parm[59].equals("end"))
                stli.setSyncDifferentFileBySize((parm[59].equals("1") ? true : false));

            if (!parm[60].equals("") && !parm[60].equals("end"))
                stli.setSyncUseExtendedDirectoryFilter1((parm[60].equals("1") ? true : false));

            if (!parm[61].equals("") && !parm[61].equals("end"))
                stli.setMasterLocalMountPoint(parm[61]);

            if (!parm[62].equals("") && !parm[62].equals("end"))
                stli.setMasterLocalMountPoint(parm[62]);

            if (!parm[63].equals("") && !parm[63].equals("end")) stli.setSyncTaskGroup(parm[63]);

            if (!parm[64].equals("") && !parm[64].equals("end")) stli.setMasterSmbProtocol(parm[64]);

            if (!parm[65].equals("") && !parm[65].equals("end")) stli.setTargetSmbProtocol(parm[65]);

            if (!parm[66].equals("") && !parm[66].equals("end")) stli.setMasterSmbIpcSigningEnforced((parm[66].equals("1") ? true : false));
            if (!parm[67].equals("") && !parm[67].equals("end")) stli.setTargetSmbIpcSigningEnforced((parm[67].equals("1") ? true : false));

            if (stli.getMasterSmbProtocol().equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SYSTEM))
                stli.setMasterSmbProtocol(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY);
            if (stli.getTargetSmbProtocol().equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SYSTEM))
                stli.setTargetSmbProtocol(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY);
            sync.add(stli);
        }
    }

    ;

    private static String convertToSpecChar(String in) {
        if (in == null || in.length() == 0) return "";
        boolean cont = true;
        String out = in;
        while (cont) {
            if (out.indexOf("\u0001") >= 0) out = out.replace("\u0001", "[");
            else cont = false;
        }

        cont = true;
        while (cont) {
            if (out.indexOf("\u0002") >= 0) out = out.replace("\u0002", "]");
            else cont = false;
        }

        return out;
    }

    ;

    private static String convertToCodeChar(String in) {
        if (in == null || in.length() == 0) return "";
        boolean cont = true;
        String out = in;
        while (cont) {
            if (out.indexOf("[") >= 0) out = out.replace("[", "\u0001");
            else cont = false;
        }

        cont = true;
        while (cont) {
            if (out.indexOf("]") >= 0) out = out.replace("]", "\u0002");
            else cont = false;
        }
        return out;
    }

    ;

    //	public static SyncTaskItem getProfile(String pfn, AdapterSyncTask pa) {
//		return getProfile(pfn, pa.getArrayList());
//	};
//
//	public static SyncTaskItem getProfile(String pfn, ArrayList<SyncTaskItem> pfl) {
//		for (int i=0;i<pfl.size();i++)
//			if (pfl.get(i).getSyncTaskName().equals(pfn))
//				return pfl.get(i);
//		return null;
//	};
//
    public static boolean saveSyncTaskList(GlobalParameters mGp, Context c, SyncUtil util,
                                           ArrayList<SyncTaskItem> pfl) {
        return saveSyncTaskListToFile(mGp, c, util, false, "", "", pfl, false);
    }

    ;

    public static boolean saveSyncTaskListToFile(GlobalParameters mGp, Context c, SyncUtil util,
                                                 boolean sdcard, String fd, String fp,
                                                 ArrayList<SyncTaskItem> pfl, boolean encrypt_required) {
        boolean result = true;
        String ofp = "";
        PrintWriter pw;
        BufferedWriter bw = null;
        try {
            CipherParms cp = null;
            if (sdcard) {
                if (encrypt_required) {
                    cp = EncryptUtil.initEncryptEnv(mGp.profileKeyPrefix + mGp.profilePassword);
                }
                File lf = new File(fd);
                if (!lf.exists()) lf.mkdir();
                bw = new BufferedWriter(new FileWriter(fp), 8192);
                pw = new PrintWriter(bw);
                ofp = fp;
                if (encrypt_required) {
                    byte[] enc_array = EncryptUtil.encrypt(SMBSYNC2_PROF_ENC, cp);
                    String enc_str =
                            Base64Compat.encodeToString(enc_array, Base64Compat.NO_WRAP);
//					MiscUtil.hexString("", enc_array, 0, enc_array.length);
                    pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + SMBSYNC2_PROF_ENC + enc_str);
                } else pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + SMBSYNC2_PROF_DEC);
            } else {
//				OutputStream out = context.openFileOutput(SMBSYNC2_PROFILE_FILE_NAME,
//						Context.MODE_PRIVATE);
//				pw = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
//				ofp=SMBSYNC2_PROFILE_FILE_NAME;
                ofp = mGp.applicationRootDirectory + "/" + CURRENT_SMBSYNC2_PROFILE_FILE_NAME;
                File lf = new File(mGp.applicationRootDirectory);
                if (!lf.exists()) lf.mkdir();
                bw = new BufferedWriter(new FileWriter(ofp), 8192);
                pw = new PrintWriter(bw);
            }

            if (pfl.size() > 0) {
                String pl;
                for (int i = 0; i < pfl.size(); i++) {
                    SyncTaskItem item = pfl.get(i);
                    String pl_name = convertToCodeChar(item.getSyncTaskName());
                    String pl_active = item.isSyncTaskAuto() ? "1" : "0";

                    String pl_master_folder_type = convertToCodeChar(item.getMasterFolderType());
                    String pl_master_remote_user_id = convertToCodeChar(item.getMasterSmbUserName());
                    String pl_master_remote_password = convertToCodeChar(item.getMasterSmbPassword());
                    String pl_master_remoteSmbShare = convertToCodeChar(item.getMasterRemoteSmbShareName());
                    String pl_master_directory_name = convertToCodeChar(item.getMasterDirectoryName());
                    String pl_master_remote_addr = item.getMasterSmbAddr();
                    String pl_master_remote_port = item.getMasterSmbPort();
                    String pl_master_remote_hostname = item.getMasterSmbHostName();
//					String pl_master_use_usb_folder=item.isMasterFolderUseInternalUsbFolder()?"1":"0";

                    String pl_target_folder_type = convertToCodeChar(item.getTargetFolderType());
                    String pl_target_remote_user_id = convertToCodeChar(item.getTargetSmbUserName());
                    String pl_target_remote_password = convertToCodeChar(item.getTargetSmbPassword());
                    String pl_target_remoteSmbShare = convertToCodeChar(item.getTargetSmbShareName());
                    String pl_target_directory_name = convertToCodeChar(item.getTargetDirectoryName());
                    String pl_target_remote_addr = item.getTargetSmbAddr();
                    String pl_target_remote_port = item.getTargetSmbPort();
                    String pl_target_remote_hostname = item.getTargetSmbHostName();
//					String pl_target_use_usb_folder=item.isTargetFolderUseInternalUsbFolder()?"1":"0";

                    String pl_synctype = item.getSyncTaskType();

                    pl = "";
                    String fl = "", dl = "", wifi_wl = "";
                    for (int j = 0; j < item.getFileFilter().size(); j++) {
                        if (fl.length() != 0) fl += "\t";
                        if (!item.getFileFilter().get(j).equals(""))
                            fl += item.getFileFilter().get(j);
                    }
                    fl = convertToCodeChar(fl);
                    fl = "[" + fl + "]";

                    for (int j = 0; j < item.getDirFilter().size(); j++) {
                        if (dl.length() != 0) dl += "\t";
                        if (!item.getDirFilter().get(j).equals(""))
                            dl += item.getDirFilter().get(j);
                    }
                    dl = convertToCodeChar(dl);
                    dl = "[" + dl + "]";

                    for (int j = 0; j < item.getSyncWifiConnectionWhiteList().size(); j++) {
                        if (wifi_wl.length() != 0) wifi_wl += "\t";
                        if (!item.getSyncWifiConnectionWhiteList().get(j).equals(""))
                            wifi_wl += item.getSyncWifiConnectionWhiteList().get(j);
                    }
                    wifi_wl = convertToCodeChar(wifi_wl);
                    wifi_wl = "[" + wifi_wl + "]";

                    String sync_root_dir_file_tobe_processed = item.isSyncProcessRootDirFile() ? "1" : "0";
                    String sync_process_override_delete = item.isSyncOverrideCopyMoveFile() ? "1" : "0";

                    String sync_confirm_override_delete = item.isSyncConfirmOverrideOrDelete() ? "1" : "0";
                    String sync_force_last_mod_use_smbsync = item.isSyncDetectLastModifiedBySmbsync() ? "1" : "0";
                    String sync_not_used_last_mod_for_remote = item.isSyncDoNotResetLastModifiedSmbFile() ? "1" : "0";
                    String sync_retry_count = item.getSyncRetryCount();
                    String sync_sync_empty_dir = item.isSyncEmptyDirectory() ? "1" : "0";
                    String sync_sync_hidden_file = item.isSyncHiddenFile() ? "1" : "0";
                    String sync_sync_hidden_dir = item.isSyncHiddenDirectory() ? "1" : "0";
                    String sync_sync_sub_dir = item.isSyncSubDirectory() ? "1" : "0";
                    String sync_use_small_io_buf = item.isSyncUseSmallIoBuffer() ? "1" : "0";

                    String sync_sync_test_mode = item.isSyncTestMode() ? "1" : "0";
                    String sync_file_copy_by_diff_file = String.valueOf(item.getSyncDifferentFileAllowableTime());
                    String sync_sync_diff_file_by_file_size = item.isSyncDifferentFileBySize() ? "1" : "0";
                    String sync_sync_diff_file_by_last_mod = item.isSyncDifferentFileByTime() ? "1" : "0";

                    String sync_sync_use_file_copy_by_temp_name = item.isSyncUseFileCopyByTempName() ? "1" : "0";
                    String sync_sync_wifi_status_option = item.getSyncWifiStatusOption();

                    String sync_result_last_time = item.getLastSyncTime();
                    String sync_result_last_status = String.valueOf(item.getLastSyncResult());

                    String sync_pos = String.valueOf(item.getSyncTaskPosition());

                    String sync_file_type_audio = item.isSyncFileTypeAudio() ? "1" : "0";
                    String sync_file_type_image = item.isSyncFileTypeImage() ? "1" : "0";
                    String sync_file_type_video = item.isSyncFileTypeVideo() ? "1" : "0";

                    String sync_use_ext_dir_filter1 = item.isSyncUseExtendedDirectoryFilter1() ? "1" : "0"; //60

                    pl = SMBSYNC2_PROF_TYPE_SYNC + "\t" +
                            pl_name + "\t" + //1
                            pl_active + "\t" +//2
                            pl_synctype + "\t" +//3

                            pl_master_folder_type + "\t" +//4
                            pl_master_remote_user_id + "\t" +//5
                            pl_master_remote_password + "\t" +//6
                            pl_master_remoteSmbShare + "\t" +//7
                            pl_master_directory_name + "\t" +//8
                            pl_master_remote_addr + "\t" +//9
                            pl_master_remote_port + "\t" +//10
                            pl_master_remote_hostname + "\t" +//11
                            item.getMasterSmbDomain() + "\t" +//12

                            pl_target_folder_type + "\t" +//13
                            pl_target_remote_user_id + "\t" +//14
                            pl_target_remote_password + "\t" +//15
                            pl_target_remoteSmbShare + "\t" +//16
                            pl_target_directory_name + "\t" +//17
                            pl_target_remote_addr + "\t" +//18
                            pl_target_remote_port + "\t" +//19
                            pl_target_remote_hostname + "\t" +//20
                            item.getTargetSmbDomain() + "\t" +//21

                            fl + "\t" +
                            dl + "\t" +
                            wifi_wl + "\t" +
                            sync_root_dir_file_tobe_processed + "\t" +//22
                            sync_process_override_delete + "\t" +//23

                            sync_confirm_override_delete + "\t" +//24
                            sync_force_last_mod_use_smbsync + "\t" +//25
                            sync_not_used_last_mod_for_remote + "\t" +//26
                            sync_retry_count + "\t" +//27
                            sync_sync_empty_dir + "\t" +//28
                            sync_sync_hidden_file + "\t" +//29
                            sync_sync_hidden_dir + "\t" +//30
                            sync_sync_sub_dir + "\t" +//31
                            sync_use_small_io_buf + "\t" +//32

                            sync_sync_test_mode + "\t" +//33
                            sync_file_copy_by_diff_file + "\t" +//34
                            sync_sync_diff_file_by_last_mod + "\t" +//35

                            sync_sync_use_file_copy_by_temp_name + "\t" +//36
                            sync_sync_wifi_status_option + "\t" +//37

                            sync_result_last_time + "\t" +//38
                            sync_result_last_status + "\t" +//39

                            sync_pos + "\t" +                                                  //40

                            "-" + "\t" +                                                       //41
                            "-" + "\t" +                                                       //42

                            item.getMasterRemovableStorageID() + "\t" +//43
                            item.getTargetRemovableStorageID() + "\t" +//44

                            sync_file_type_audio + "\t" +//45
                            sync_file_type_image + "\t" +//46
                            sync_file_type_video + "\t" +//47

                            item.getTargetZipOutputFileName() + "\t" +                            //48
                            item.getTargetZipCompressionLevel() + "\t" +                        //49
                            item.getTargetZipCompressionMethod() + "\t" +                        //50
                            item.getTargetZipEncryptMethod() + "\t" +                            //51
                            item.getTargetZipPassword() + "\t" +                                //52
                            (item.isSyncTaskSkipIfConnectAnotherWifiSsid() ? "1" : "0") + "\t" +    //53

                            (item.isSyncOptionSyncWhenCharging() ? "1" : "0") + "\t" +                //54

                            (item.isTargetZipUseExternalSdcard() ? "1" : "0") + "\t" +          //55

                            (item.isSyncTaskTwoWay() ? "1" : "0") + "\t" +                      //56
                            item.getSyncTwoWayConflictOption() + "\t" +                         //57

                            item.getTargetZipFileNameEncoding() + "\t" +                        //58

                            sync_sync_diff_file_by_file_size + "\t" +                           //59

                            sync_use_ext_dir_filter1 + "\t" +                                   //60

                            item.getMasterLocalMountPoint() + "\t" +                            //61

                            item.getTargetLocalMountPoint() + "\t" +                            //62

                            item.getSyncTaskGroup() + "\t" +                                    //63

                            item.getMasterSmbProtocol() + "\t" +                                //64

                            item.getTargetSmbProtocol() + "\t" +                                //65

                            (item.isMasterSmbIpcSigningEnforced() ? "1" : "0") + "\t" +         //66
                            (item.isTargetSmbIpcSigningEnforced() ? "1" : "0") + "\t" +         //67

                            "end"
                    ;

//					Log.v("","write pl="+pl);
                    if (sdcard) {
                        if (encrypt_required) {
                            String enc =
                                    Base64Compat.encodeToString(
                                            EncryptUtil.encrypt(pl, cp),
                                            Base64Compat.NO_WRAP);
                            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + enc);
                        } else {
                            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + pl);
                        }
                    } else {
                        pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + pl);
                    }

                }
            }
            saveSettingsParmsToFile(c, pw, encrypt_required, cp);
            pw.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            util.addLogMsg("E", String.format(mGp.appContext.getString(R.string.msgs_save_to_profile_error), ofp));
            util.addLogMsg("E", e.toString());
            result = false;
        }

        return result;
    }

    ;

    static private void addImportSettingsParm(String pl, ArrayList<PreferenceParmListIItem> ispl) {
        String tmp_ps = pl;//pl.substring(7,pl.length());
        String[] tmp_pl = tmp_ps.split("\t");// {"type","name","active",options...};
//		Log.v("","0="+tmp_pl[0]+", l="+tmp_pl.length+", 1="+tmp_pl[1]+", 2="+tmp_pl[2]);//+", 3="+tmp_pl[3]);
        if (tmp_pl[0] != null && tmp_pl.length >= 3 && tmp_pl[0].equals(SMBSYNC2_PROF_TYPE_SETTINGS)) {
//			String[] val = new String[]{parm[2],parm[3],parm[4]};
            PreferenceParmListIItem ppli = new PreferenceParmListIItem();
            if (tmp_pl[1] != null) ppli.parms_key = tmp_pl[1];
            if (tmp_pl[2] != null) ppli.parms_type = tmp_pl[2];
            if (tmp_pl.length >= 4 && tmp_pl[3] != null) ppli.parms_value = tmp_pl[3];
            if (!ppli.parms_key.equals("") && !ppli.parms_type.equals("")) {
                ispl.add(ppli);
            }
        }
    }

    ;

    public class FilterAdapterSort implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }

    public static void saveSettingsParmsToFile(Context c, PrintWriter pw, boolean encrypt_required, final CipherParms cp) {
        saveSettingsParmsToFileBoolean(c, pw, false, encrypt_required, cp, c.getString(R.string.settings_error_option));
        saveSettingsParmsToFileBoolean(c, pw, false, encrypt_required, cp, c.getString(R.string.settings_wifi_lock));

        saveSettingsParmsToFileBoolean(c, pw, false, encrypt_required, cp, c.getString(R.string.settings_log_option));
        saveSettingsParmsToFileBoolean(c, pw, false, encrypt_required, cp, c.getString(R.string.settings_put_logcat_option));
        saveSettingsParmsToFileString(c, pw, "0", encrypt_required, cp, c.getString(R.string.settings_log_level));
        saveSettingsParmsToFileString(c, pw, "", encrypt_required, cp, c.getString(R.string.settings_mgt_dir));
        saveSettingsParmsToFileString(c, pw, "10", encrypt_required, cp, c.getString(R.string.settings_log_file_max_count));
        saveSettingsParmsToFileString(c, pw, "0", encrypt_required, cp, c.getString(R.string.settings_playback_ringtone_when_sync_ended));
        saveSettingsParmsToFileString(c, pw, "0", encrypt_required, cp, c.getString(R.string.settings_vibrate_when_sync_ended));
        saveSettingsParmsToFileBoolean(c, pw, false, encrypt_required, cp, c.getString(R.string.settings_use_light_theme));
        saveSettingsParmsToFileBoolean(c, pw, false, encrypt_required, cp, c.getString(R.string.settings_device_orientation_portrait));

        saveSettingsParmsToFileBoolean(c, pw, false, encrypt_required, cp, c.getString(R.string.settings_exported_profile_encryption));

        saveSettingsParmsToFileString(c, pw, "0", encrypt_required, cp, c.getString(R.string.settings_smb_lm_compatibility));
        saveSettingsParmsToFileBoolean(c, pw, false, encrypt_required, cp, c.getString(R.string.settings_smb_use_extended_security));

        saveSettingsParmsToFileString(c, pw, "", encrypt_required, cp, c.getString(R.string.settings_smb_rcv_buf_size));
        saveSettingsParmsToFileString(c, pw, "", encrypt_required, cp, c.getString(R.string.settings_smb_snd_buf_size));
        saveSettingsParmsToFileString(c, pw, "", encrypt_required, cp, c.getString(R.string.settings_smb_listSize));
        saveSettingsParmsToFileString(c, pw, "", encrypt_required, cp, c.getString(R.string.settings_smb_maxBuffers));

        saveSettingsParmsToFileString(c, pw, "-1", encrypt_required, cp, SCHEDULER_SCHEDULE_SAVED_DATA_V2);
    }

    private static void saveSettingsParmsToFileString(Context c, PrintWriter pw, String dflt,
                                                      boolean encrypt_required, final CipherParms cp, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String k_type, k_val;

        k_val = prefs.getString(key, dflt);
        k_type = SMBSYNC2_UNLOAD_SETTINGS_TYPE_STRING;
        String k_str =
                SMBSYNC2_PROF_TYPE_SETTINGS + "\t" + key + "\t" + k_type + "\t" + k_val;
        if (encrypt_required) {
            byte[] out = EncryptUtil.encrypt(k_str, cp);
            String enc = Base64Compat.encodeToString(
                    out,
                    Base64Compat.NO_WRAP);
            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + enc);
        } else {
            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + k_str);
        }
    }

    ;

    @SuppressWarnings("unused")
    static private void saveSettingsParmsToFileInt(Context c, PrintWriter pw, int dflt,
                                                   boolean encrypt_required, final CipherParms cp, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String k_type;
        int k_val;

        k_val = prefs.getInt(key, dflt);
        k_type = SMBSYNC2_UNLOAD_SETTINGS_TYPE_INT;
        String k_str =
                SMBSYNC2_PROF_TYPE_SETTINGS + "\t" + key + "\t" + k_type + "\t" + k_val;
        if (encrypt_required) {
            String enc = Base64Compat.encodeToString(
                    EncryptUtil.encrypt(k_str, cp),
                    Base64Compat.NO_WRAP);
            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + enc);
        } else {
            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + k_str);
        }
    }

    ;

    static private void saveSettingsParmsToFileLong(Context c, PrintWriter pw, long dflt,
                                                    boolean encrypt_required, final CipherParms cp, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String k_type;
        long k_val;

        k_val = prefs.getLong(key, dflt);
        k_type = SMBSYNC2_UNLOAD_SETTINGS_TYPE_LONG;
        String k_str =
                SMBSYNC2_PROF_TYPE_SETTINGS + "\t" + key + "\t" + k_type + "\t" + k_val;
        if (encrypt_required) {
            String enc = Base64Compat.encodeToString(
                    EncryptUtil.encrypt(k_str, cp),
                    Base64Compat.NO_WRAP);
            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + enc);
        } else {
            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + k_str);
        }
    }

    ;

    static private void saveSettingsParmsToFileBoolean(Context c, PrintWriter pw, boolean dflt,
                                                       boolean encrypt_required, final CipherParms cp, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String k_type;
        boolean k_val;

        k_val = prefs.getBoolean(key, dflt);
        k_type = SMBSYNC2_UNLOAD_SETTINGS_TYPE_BOOLEAN;
        String k_str =
                SMBSYNC2_PROF_TYPE_SETTINGS + "\t" + key + "\t" + k_type + "\t" + k_val;
        if (encrypt_required) {
            String enc = Base64Compat.encodeToString(
                    EncryptUtil.encrypt(k_str, cp),
                    Base64Compat.NO_WRAP);
            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + enc);
        } else {
            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + k_str);
        }
    }

    ;

    static private class PreferenceParmListIItem {
        public String parms_key = "";
        public String parms_type = "";
        public String parms_value = "";
    }

}

