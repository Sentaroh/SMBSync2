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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sentaroh.android.Utilities.Base64Compat;
import com.sentaroh.android.Utilities.ContextMenu.CustomContextMenu;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.Dialog.DialogBackKeyListener;
import com.sentaroh.android.Utilities.EncryptUtil;
import com.sentaroh.android.Utilities.EncryptUtil.CipherParms;
import com.sentaroh.android.Utilities.MiscUtil;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.android.Utilities.StringUtil;
import com.sentaroh.android.Utilities.ThemeUtil;
import com.sentaroh.android.Utilities.ThreadCtrl;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistAdapter;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistItem;
import com.sentaroh.android.Utilities.Widget.CustomTextView;
import com.sentaroh.jcifs.JcifsAuth;
import com.sentaroh.jcifs.JcifsException;
import com.sentaroh.jcifs.JcifsFile;
import com.sentaroh.jcifs.JcifsUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.sentaroh.android.SMBSync2.AdapterNetworkScanResult.NetworkScanListItem.SMB_STATUS_ACCESS_DENIED;
import static com.sentaroh.android.SMBSync2.AdapterNetworkScanResult.NetworkScanListItem.SMB_STATUS_INVALID_LOGON_TYPE;
import static com.sentaroh.android.SMBSync2.AdapterNetworkScanResult.NetworkScanListItem.SMB_STATUS_UNKNOWN_ACCOUNT;
import static com.sentaroh.android.SMBSync2.AdapterNetworkScanResult.NetworkScanListItem.SMB_STATUS_UNSUCCESSFULL;
import static com.sentaroh.android.SMBSync2.Constants.APPLICATION_TAG;
import static com.sentaroh.android.SMBSync2.Constants.CURRENT_SMBSYNC2_PROFILE_FILE_NAME;
import static com.sentaroh.android.SMBSync2.Constants.CURRENT_SMBSYNC2_PROFILE_VERSION;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_KEY_STORE_ALIAS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROFILE_FILE_NAME_V1;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROFILE_FILE_NAME_V2;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROFILE_FILE_NAME_V3;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROFILE_FILE_NAME_V4;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROFILE_FILE_NAME_V5;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROFILE_FILE_NAME_V6;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROFILE_FILE_NAME_V7;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROFILE_FILE_NAME_V8;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_DEC;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_ENC;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_DIR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_DIR_INVALID_CHARS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_EXCLUDE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_FILE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_FILE_INVALID_CHARS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_INCLUDE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_TYPE_SETTINGS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_TYPE_SYNC;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_VER1;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_VER2;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_VER3;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_VER4;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_VER5;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_VER6;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_VER7;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_VER8;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_UNLOAD_SETTINGS_TYPE_BOOLEAN;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_UNLOAD_SETTINGS_TYPE_INT;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_UNLOAD_SETTINGS_TYPE_LONG;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_UNLOAD_SETTINGS_TYPE_STRING;
import static com.sentaroh.android.SMBSync2.Constants.SYNC_TASK_NAME_UNUSABLE_CHARACTER;
import static com.sentaroh.android.SMBSync2.Constants.WHOLE_DIRECTORY_FILTER_PREFIX_V1;
import static com.sentaroh.android.SMBSync2.Constants.WHOLE_DIRECTORY_FILTER_PREFIX_V2;
import static com.sentaroh.android.SMBSync2.GlobalParameters.DEFAULT_NOCOMPRESS_FILE_TYPE;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_SAVED_DATA_V5;

public class SyncTaskUtil {

    //	private CustomContextMenu ccMenu=null;
    private String smbUser, smbPass;

    private Context mContext;
    private Activity mActivity;

    private CommonUtilities mUtil;

    private ArrayList<PreferenceParmListIItem>
            importedSettingParmList = new ArrayList<PreferenceParmListIItem>();

    private CommonDialog mCommonDlg = null;
    private GlobalParameters mGp = null;
    private FragmentManager mFragMgr = null;

    SyncTaskUtil(CommonUtilities mu, Activity a,
                 CommonDialog cd, CustomContextMenu ccm, GlobalParameters gp, FragmentManager fm) {
        mContext = a;
        mGp = gp;
        mUtil = mu;
        mCommonDlg = cd;
		mActivity=a;
        mFragMgr = fm;
    }

//    public void fileSelectorFileOnlySelectWithCreate(Boolean inc_mp, String mount_point, String dir_name, String file_name, String title, NotifyEvent ntfy) {
//        boolean include_root=false;
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, true, true, CommonFileSelector.DIALOG_SELECT_CATEGORY_FILE,
//                        true, inc_mp, mount_point, dir_name, file_name, title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };
//
//    public void fileSelectorDirOnlySelectWithCreate(Boolean inc_mp, String mount_point, String dir_name, String title, NotifyEvent ntfy) {
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, true, false, CommonFileSelector.DIALOG_SELECT_CATEGORY_DIRECTORY,
//                        true, inc_mp, mount_point, dir_name, "", title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };
//
//    public void fileSelectorFileOnlySelectWithCreateHideMP(Boolean inc_mp, String mount_point, String dir_name, String file_name, String title, NotifyEvent ntfy) {
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, true, true, CommonFileSelector.DIALOG_SELECT_CATEGORY_FILE,
//                        true, inc_mp, mount_point, dir_name, file_name, title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };
//
//    public void fileSelectorDirOnlySelectWithCreateHideMP(Boolean inc_mp, String mount_point, String dir_name, String title, NotifyEvent ntfy) {
//        CommonFileSelector fsdf=
//                CommonFileSelector.newInstance(false, true, false, CommonFileSelector.DIALOG_SELECT_CATEGORY_DIRECTORY,
//                        true, inc_mp, mount_point, dir_name, "", title);
//        fsdf.showDialog(mFragMgr, fsdf, ntfy);
//    };
//

    public void importSyncTaskListDlg(final NotifyEvent p_ntfy) {
        final ArrayList<String>auto_saved_selector_list=new ArrayList<String>();
        final ArrayList<File>auto_saved_file_list=createAutoSaveFileList(mGp, mUtil);
        Collections.sort(auto_saved_file_list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
//                return (int)(o2.lastModified()-o1.lastModified());
                return o2.getName().compareToIgnoreCase(o1.getName());
            }
        });
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        boolean latest_used=false;
        for(File item:auto_saved_file_list) {
            String entry="";
            if (!latest_used) {
                latest_used=true;
                entry=String.format(mContext.getString(R.string.msgs_import_autosave_dlg_autosave_enty_item_latest), sdf.format(item.lastModified()));
            } else {
                entry=String.format(mContext.getString(R.string.msgs_import_autosave_dlg_autosave_enty_item), sdf.format(item.lastModified()));
            }
            auto_saved_selector_list.add(entry);
        }

        final ArrayList<String>manual_save_file_list=getExportedFileList();
        final ArrayList<String>manual_save_selector_list=new ArrayList<String>();
        for(int i=0;i<manual_save_file_list.size();i++) {
            String fp_item=manual_save_file_list.get(i);
            File lf=new File(fp_item);
            String dt=sdf.format(lf.lastModified());
            String entry="";
            if (i==0) entry=String.format(mContext.getString(R.string.msgs_import_autosave_dlg_autosave_enty_item_latest), dt);
            else entry=String.format(mContext.getString(R.string.msgs_import_autosave_dlg_autosave_enty_item), dt);

            manual_save_selector_list.add(fp_item+"\n"+entry);
        }

        if (auto_saved_selector_list.size()==0 && manual_save_selector_list.size()==0) {
            importSyncTaskListDlgWithFileSelection(p_ntfy);
            return;
        }

        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.import_autosave_dlg);

        final LinearLayout ll_title=(LinearLayout) dialog.findViewById(R.id.import_autosave_dlg_title_view);
        ll_title.setBackgroundColor(mGp.themeColorList.title_background_color);
        final TextView tv_title=(TextView)dialog.findViewById(R.id.import_autosave_dlg_title);
        tv_title.setTextColor(mGp.themeColorList.title_text_color);
//        if (Build.VERSION.SDK_INT>=23) tv_msg.setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY);

        final Button btn_ok=(Button)dialog.findViewById(R.id.import_autosave_dlg_btn_ok);
        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
        final Button btn_cancel=(Button)dialog.findViewById(R.id.import_autosave_dlg_btn_cancel);
        final Button btn_select=(Button)dialog.findViewById(R.id.import_autosave_dlg_select_exported_file);

        CommonDialog.setDlgBoxSizeLimit(dialog,true);


        final ListView auto_save_list_view = (ListView) dialog.findViewById(R.id.import_autosave_dlg_autosave_listview);
        final ListView manual_save_list_view = (ListView) dialog.findViewById(R.id.import_autosave_dlg_manual_save_listview);

        SyncTaskListFileSelectorAdapter manual_save_adapter=new SyncTaskListFileSelectorAdapter(mActivity, R.layout.sync_task_list_save_file_selector_item,
                manual_save_selector_list);
        manual_save_list_view.setAdapter(manual_save_adapter);
        manual_save_list_view.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        manual_save_list_view.setScrollingCacheEnabled(false);
        manual_save_list_view.setScrollbarFadingEnabled(false);
        manual_save_list_view.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                auto_save_list_view.setItemChecked(-1, true);
                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
            }
        });

        SyncTaskListFileSelectorAdapter auto_save_adapter=new SyncTaskListFileSelectorAdapter(mActivity, R.layout.sync_task_list_save_file_selector_item,
                auto_saved_selector_list);
        auto_save_list_view.setAdapter(auto_save_adapter);
        auto_save_list_view.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        auto_save_list_view.setScrollingCacheEnabled(false);
        auto_save_list_view.setScrollbarFadingEnabled(false);
        auto_save_list_view.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                manual_save_list_view.setItemChecked(-1, true);
                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
            }
        });

        btn_select.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                importSyncTaskListDlgWithFileSelection(p_ntfy);
                Handler hndl=new Handler();
                hndl.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                },500);
            }
        });

        btn_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SparseBooleanArray checked_auto_save = auto_save_list_view.getCheckedItemPositions();
                SparseBooleanArray checked_manual_save = manual_save_list_view.getCheckedItemPositions();
                boolean auto_selected=false;
                for (int i = 0; i <= auto_saved_selector_list.size(); i++) {
                    if (checked_auto_save.get(i) == true) {
                        importSyncTaskList(p_ntfy, auto_saved_file_list.get(i).getPath(), true);
                        auto_selected=true;
                        break;
                    }
                }
                if (!auto_selected) {
                    for (int i = 0; i <= manual_save_file_list.size(); i++) {
                        if (checked_manual_save.get(i) == true) {
                            importSyncTaskList(p_ntfy, manual_save_file_list.get(i), false);
                            break;
                        }
                    }
                }
                Handler hndl=new Handler();
                hndl.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                },500);
            }
        });

        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                btn_cancel.performClick();
            }
        });

        dialog.show();

    }

    private class SyncTaskListFileSelectorAdapter extends ArrayAdapter<String> {
        private Context c;
        private int id;
        private ArrayList<String> items;

        public SyncTaskListFileSelectorAdapter(Context context, int resource, ArrayList<String> objects) {
            super(context, resource, objects);
            c = context;
            id = resource;
            items = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(id, null);
                holder = new ViewHolder();
                holder.tv_text1 =(TextView) v.findViewById(android.R.id.text1);

                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            final String o = getItem(position);

            if (o != null) {
                holder.tv_text1.setText(o);
            }

            return v;
        }

        private class ViewHolder {
            TextView tv_text1;
            TextView tv_text2;
        }
    }

    public void importSyncTaskListDlgWithFileSelection(final NotifyEvent p_ntfy) {
        mUtil.addDebugMsg(1,"I","importSyncTaskListDlg entered");
        importedSettingParmList.clear();

        NotifyEvent ntfy = new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                final String fpath = (String) o[0]+o[1]+"/"+(String)o[2];
                importSyncTaskList(p_ntfy, fpath, false);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        mCommonDlg.fileSelectorFileOnly(true,
                mGp.internalRootDirectory, "", "", mContext.getString(R.string.msgs_select_import_file), ntfy);
    }

    private void importSyncTaskList(final NotifyEvent p_ntfy, final String fpath, final boolean from_auto_save) {
        NotifyEvent ntfy_pswd = new NotifyEvent(mContext);
        ntfy_pswd.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                mGp.profilePassword = (String) o[0];
                final Handler hndl=new Handler();
                Thread th=new Thread() {
                    @Override
                    public void run() {
                        AdapterSyncTask tfl_temp = null;
                        if (isSyncTaskListFileOldFormat(fpath)) {
                            tfl_temp = new AdapterSyncTask(mActivity, R.layout.sync_task_item_view, ImportOldProfileList.importOldProfileList(mGp, fpath), mGp);
                        } else {
                            tfl_temp = new AdapterSyncTask(mActivity, R.layout.sync_task_item_view,
                                    createSyncTaskListFromFile(mContext, mGp, mUtil, true, fpath, importedSettingParmList, from_auto_save), mGp);
                        }
                        final AdapterSyncTask tfl = tfl_temp;
                        hndl.post(new Runnable(){//
                            @Override
                            public void run() {
                                if (tfl.getCount() > 0) {
                                    importSyncTaskItemSelector(tfl, fpath, p_ntfy, from_auto_save);
                                } else {
                                    mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_export_import_profile_no_import_items), "", null);
                                    p_ntfy.notifyToListener(false, null);
                                }
                            }
                        });
                    }
                };
                th.start();
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
                    if (pl!=null && pl.length()==9) {
                        String enc_str = pl.substring(6);
                        if (enc_str!=null) {
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
                                if (pl!=null && pl.length()>15) dec_str = pl.substring(6);
                                else dec_str="";
                            }
                            if (!dec_str.equals("")) {
                                String[] parm = dec_str.split("\t");
                                result = (parm[0].equals("Default") && parm[1].equals("S"));
                            }
                        }
                    }
                    br.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                if (pl != null && pl.length()>=9) {
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
    public void promptPasswordForImport(final String fpath, final NotifyEvent ntfy_pswd) {
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setContentView(R.layout.password_input_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.password_input_dlg_view);
        CommonUtilities.setDialogBoxOutline(mContext, ll_dlg_view);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.password_input_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.password_input_title);

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

        CommonDialog.setDlgBoxSizeCompactWithInput(dialog);

        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                if (arg0.length() > 0) CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                else CommonDialog.setViewEnabled(mActivity, btn_ok, false);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });

        //OK button
        final Handler hndl=new Handler();
        btn_ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String passwd = et_password.getText().toString();
                BufferedReader br;
                String pl;
                boolean pswd_invalid = true;
                CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                try {
                    br = new BufferedReader(new FileReader(fpath), 8192);
                    pl = br.readLine();
                    if (pl != null) {
                        String enc_str = "";
                        if (pl.substring(6).startsWith(SMBSYNC2_PROF_ENC)) {
                            enc_str = pl.substring(6).replace(SMBSYNC2_PROF_ENC, "");
                        }
                        if (!enc_str.equals("")) {
                            byte[] enc_array = Base64Compat.decode(enc_str, Base64Compat.NO_WRAP);
                            String dec_str = "";
                            CipherParms cp = EncryptUtil.initDecryptEnv(mGp.profileKeyPrefix + passwd);
                            dec_str = EncryptUtil.decrypt(enc_array, cp);
                            if (!SMBSYNC2_PROF_ENC.equals(dec_str)) {
                                CipherParms cp_old = EncryptUtil.initDecryptEnv(mGp.profileKeyPrefixOld + passwd);
                                dec_str = EncryptUtil.decrypt(enc_array, cp_old);
                            }
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
                } else {
                    hndl.postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                        }
                    },1000);
                }
            }
        });
        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new OnClickListener() {
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
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        dialog.setContentView(R.layout.password_input_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.password_input_dlg_view);
        CommonUtilities.setDialogBoxOutline(mContext, ll_dlg_view);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.password_input_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.password_input_title);

        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.password_input_msg);
        final CheckedTextView ctv_protect = (CheckedTextView) dialog.findViewById(R.id.password_input_ctv_protect);
        final Button btn_ok = (Button) dialog.findViewById(R.id.password_input_ok_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.password_input_cancel_btn);
        final EditText et_password = (EditText) dialog.findViewById(R.id.password_input_password);
        final EditText et_confirm = (EditText) dialog.findViewById(R.id.password_input_password_confirm);

        dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_specify_password));

        CommonDialog.setDlgBoxSizeCompactWithInput(dialog);

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

        CommonDialog.setViewEnabled(mActivity, et_password, true);
        CommonDialog.setViewEnabled(mActivity, et_confirm, false);
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                CommonDialog.setViewEnabled(mActivity, btn_ok, false);
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
                CommonDialog.setViewEnabled(mActivity, btn_ok, false);
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
        btn_ok.setOnClickListener(new OnClickListener() {
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
        btn_cancel.setOnClickListener(new OnClickListener() {
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
            CommonDialog.setViewEnabled(mActivity, btn_ok, true);
        }
    }

    private void setPasswordPromptOkButton(EditText et_passwd, EditText et_confirm,
                                           Button btn_ok, TextView dlg_msg) {
        String password = et_passwd.getText().toString();
        String confirm = et_confirm.getText().toString();
        if (password.length() > 0 && et_confirm.getText().length() == 0) {
            dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_unmatched_confirm_pswd));
            CommonDialog.setViewEnabled(mActivity, et_confirm, true);
        } else if (password.length() > 0 && et_confirm.getText().length() > 0) {
            CommonDialog.setViewEnabled(mActivity, et_confirm, true);
            if (!password.equals(confirm)) {
                CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_unmatched_confirm_pswd));
            } else {
                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                dlg_msg.setText("");
            }
        } else if (password.length() == 0 && confirm.length() == 0) {
            CommonDialog.setViewEnabled(mActivity, btn_ok, false);
            dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_specify_password));
            CommonDialog.setViewEnabled(mActivity, et_passwd, true);
            CommonDialog.setViewEnabled(mActivity, et_confirm, false);
        } else if (password.length() == 0 && confirm.length() > 0) {
            dlg_msg.setText(mContext.getString(R.string.msgs_export_import_pswd_unmatched_confirm_pswd));
        }

    }

    private void importSyncTaskItemSelector(final AdapterSyncTask tfl, final String fp, final NotifyEvent p_ntfy, final boolean from_auto_save) {
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.export_import_profile_dlg);
        dialog.setCanceledOnTouchOutside(false);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.export_import_profile_view);

        ArrayList<AdapterExportImportTask.ExportImportListItem> eipl = new ArrayList<AdapterExportImportTask.ExportImportListItem>();

        for (int i = 0; i < tfl.getCount(); i++) {
            SyncTaskItem pl = tfl.getItem(i);
            AdapterExportImportTask.ExportImportListItem eipli = new AdapterExportImportTask.ExportImportListItem();
            eipli.isChecked = true;
            eipli.item_name = pl.getSyncTaskName();
            eipl.add(eipli);
        }
        final AdapterExportImportTask imp_list_adapt =
                new AdapterExportImportTask(mActivity, R.layout.export_import_profile_list_item, eipl);

        ListView lv = (ListView) dialog.findViewById(R.id.export_import_profile_listview);
        lv.setAdapter(imp_list_adapt);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.export_import_profile_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.export_import_profile_title);
        title.setText(mContext.getString(R.string.msgs_export_import_profile_title));
        final TextView from_path = (TextView) dialog.findViewById(R.id.export_import_profile_from_file_path);
        from_path.setVisibility(TextView.VISIBLE);
        from_path.setText(fp);
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
                CommonDialog.setViewEnabled(mActivity, ok_btn, true);
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
                CommonDialog.setViewEnabled(mActivity, ok_btn, false);
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
                        p_ntfy, from_auto_save);
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
            CommonDialog.setViewEnabled(mActivity, ok_btn, true);
        else CommonDialog.setViewEnabled(mActivity, ok_btn, false);
    }

    private void importSelectedSyncTaskItem(
            final AdapterExportImportTask imp_list_adapt,
            final AdapterSyncTask tfl,
            final boolean import_settings_required,
            final boolean import_schedule_required,
            final NotifyEvent p_ntfy, final boolean from_auto_save) {
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
                String imp_list_temp = "";
                String imp_error_temp = "";
                for (int i = 0; i < tfl.getCount(); i++) {
                    SyncTaskItem ipfli = tfl.getItem(i);
                    AdapterExportImportTask.ExportImportListItem eipli = imp_list_adapt.getItem(i);
                    if (eipli.isChecked) {
                        imp_list_temp += ipfli.getSyncTaskName() + "\n";
                        SyncTaskItem mpfli = getSyncTaskByName(mGp.syncTaskAdapter, ipfli.getSyncTaskName());
                        if (mpfli != null) {
                            mGp.syncTaskAdapter.remove(mpfli);
                            ipfli.setSyncTaskPosition(mpfli.getSyncTaskPosition());
                            mGp.syncTaskAdapter.add(ipfli);
                        } else {
                            ipfli.setSyncTaskPosition(mGp.syncTaskAdapter.getCount());
                            mGp.syncTaskAdapter.add(ipfli);
                        }
                        if (ipfli.isSyncTaskError()) {
                            imp_error_temp+=ipfli.getSyncTaskName()+"\n";
                        }
                    }
                }
                restoreImportedSystemOption();
                boolean import_setting_restored_temp=false;
                boolean import_schedule_restored_temp=false;
                if (import_settings_required) {
                    import_setting_restored_temp=restoreImportedSettingParms();
                    if (import_setting_restored_temp) imp_list_temp += mContext.getString(R.string.msgs_export_import_profile_setting_parms) + "\n";
                }
                if (import_schedule_required) {
                    import_schedule_restored_temp=restoreImportedScheduleParms();
                    if (import_schedule_restored_temp) imp_list_temp += mContext.getString(R.string.msgs_export_import_profile_schedule_parms) + "\n";
                }
                if (imp_list_temp.length() > 0) imp_list_temp += " ";
                final String imp_list=imp_list_temp;
                final String imp_smb=imp_error_temp;
                mGp.syncTaskAdapter.sort();
                mGp.syncTaskListView.setSelection(0);
                final Dialog pd=CommonDialog.showProgressSpinIndicator(mActivity);
                pd.show();
                final Handler hndl=new Handler();
                Thread th=new Thread(){
                    @Override
                    public void run(){
                        final boolean save_success=saveSyncTaskList(mGp, mContext, mUtil, mGp.syncTaskAdapter.getArrayList());
                        hndl.post(new Runnable(){
                            @Override
                            public void run() {
                                if (save_success) {
                                    NotifyEvent ntfy_success=new NotifyEvent(mContext);
                                    ntfy_success.setListener(new NotifyEventListener() {
                                        @Override
                                        public void positiveResponse(Context context, Object[] objects) {
                                            if (from_auto_save && !imp_smb.equals("")) {
                                                NotifyEvent ntfy_decrypt_warning=new NotifyEvent(mContext);
                                                ntfy_decrypt_warning.setListener(new NotifyEventListener() {
                                                    @Override
                                                    public void positiveResponse(Context context, Object[] objects) {
                                                        p_ntfy.notifyToListener(true, null);
                                                        pd.dismiss();
                                                    }
                                                    @Override
                                                    public void negativeResponse(Context context, Object[] objects) {}
                                                });
                                                mUtil.showCommonDialog(false, "W",mContext.getString(R.string.msgs_import_autosave_dlg_title),
                                                        mContext.getString(R.string.msgs_export_import_profile_smb_account_name_password_not_restored)+"\n\n"+imp_smb, ntfy_decrypt_warning);
                                                mUtil.addLogMsg("W",
                                                        mContext.getString(R.string.msgs_export_import_profile_smb_account_name_password_not_restored)+"\n"+imp_smb);
                                            } else {
                                                p_ntfy.notifyToListener(true, null);
                                                pd.dismiss();
                                            }
                                        }
                                        @Override
                                        public void negativeResponse(Context context, Object[] objects) {}
                                    });
                                    mUtil.showCommonDialog(false, "I",mContext.getString(R.string.msgs_import_autosave_dlg_title),
                                            mContext.getString(R.string.msgs_export_import_profile_import_success)+"\n\n"+imp_list, ntfy_success);
                                    mUtil.addLogMsg("I", mContext.getString(R.string.msgs_export_import_profile_import_success)+"\n"+imp_list);
                                    SyncTaskUtil.autosaveSyncTaskList(mGp, mActivity, mUtil, mCommonDlg, mGp.syncTaskList);
                                } else {
                                    mUtil.showCommonDialog(false, "E",mContext.getString(R.string.msgs_import_autosave_dlg_title),
                                            mContext.getString(R.string.msgs_export_import_profile_import_failed), null);
                                    mUtil.addLogMsg("E",mContext.getString(R.string.msgs_export_import_profile_import_failed));
                                    p_ntfy.notifyToListener(true, null);
                                    pd.dismiss();
                                }
                            }
                        });
                    }
                };
                th.start();
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        if (!repl_list.equals("")) {
            //Confirm
            mUtil.showCommonDialog(true, "W",
                    mContext.getString(R.string.msgs_export_import_profile_confirm_override),
                    repl_list, ntfy);
        } else {
            ntfy.notifyToListener(true, null);
        }

    }

    private boolean restoreImportedSystemOption() {
        final ArrayList<PreferenceParmListIItem> spl = importedSettingParmList;
        boolean result=false;
        if (spl.size() == 0) {
            mUtil.addDebugMsg(1, "I", "Import setting parms can not be not found.");
            return result;
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Editor pe = prefs.edit();

        if (spl.size() >= 0) {
            for (int i = 0; i < spl.size(); i++) {
                if (spl.get(i).parms_key.startsWith("system_rest")) {
                    result=restorePreferenceParms(pe, spl.get(i));
                }
            }
            pe.commit();
        }
        return result;
    }

    private boolean restorePreferenceParms(Editor pe, PreferenceParmListIItem pa) {
        boolean result=false;
        if (pa.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_STRING)) {
            pe.putString(pa.parms_key, pa.parms_value);
            result=true;
            mUtil.addDebugMsg(2, "I", "Restored parms=" + pa.parms_key + "=" + pa.parms_value);
        } else if (pa.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_BOOLEAN)) {
            boolean b_val = false;
            if (pa.parms_value.equals("false")) b_val = false;
            else b_val = true;
            pe.putBoolean(pa.parms_key, b_val);
            result=true;
            mUtil.addDebugMsg(2, "I", "Restored parms=" + pa.parms_key + "=" + pa.parms_value);
        } else if (pa.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_INT)) {
            int i_val = 0;
            i_val = Integer.parseInt(pa.parms_value);
            pe.putInt(pa.parms_key, i_val);
            result=true;
            mUtil.addDebugMsg(2, "I", "Restored parms=" + pa.parms_key + "=" + pa.parms_value);
        } else if (pa.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_LONG)) {
            long i_val = 0;
            i_val = Long.parseLong(pa.parms_value);
            pe.putLong(pa.parms_key, i_val);
            result=true;
            mUtil.addDebugMsg(2, "I", "Restored parms=" + pa.parms_key + "=" + pa.parms_value);
        }
        return result;
    }

    private boolean restoreImportedScheduleParms() {
        final ArrayList<PreferenceParmListIItem> spl = importedSettingParmList;
        boolean result=false;
        if (spl.size() == 0) {
            mUtil.addDebugMsg(1, "I", "Import setting parms can not be not found.");
            return result;
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Editor pe = prefs.edit();

        if (spl.size() >= 0) {
            for (int i = 0; i < spl.size(); i++) {
                if (spl.get(i).parms_key.equals(SCHEDULER_SCHEDULE_SAVED_DATA_V5)) {
                    result=restorePreferenceParms(pe, spl.get(i));
                }
            }
            pe.commit();
//			applySettingParms();
        }
        return result;
    }

    private boolean restoreImportedSettingParms() {
        final ArrayList<PreferenceParmListIItem> spl = importedSettingParmList;
        boolean result=false;
        if (spl.size() == 0) {
            mUtil.addDebugMsg(2, "I", "Import setting parms can not be not found.");
            return result;
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final Editor pe = prefs.edit();

        if (spl.size() >= 0) {
            for (int i = 0; i < spl.size(); i++) {
                if (spl.get(i).parms_key.startsWith("settings")) {
                    result=restorePreferenceParms(pe, spl.get(i));
                }
            }
            pe.commit();
//			applySettingParms();
        }
        return result;
    }

    public void exportSyncTaskListDlg() {
        mUtil.addDebugMsg(1,"I","exportSyncTaskListDlg entered");
        NotifyEvent ntfy_file_select = new NotifyEvent(mContext);
        ntfy_file_select.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                final String fpath = (String) o[0]+o[1]+"/"+(String)o[2];
                NotifyEvent ntfy_pswd = new NotifyEvent(mContext);
                ntfy_pswd.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        mGp.profilePassword = (String) o[0];
                        boolean encrypt_required = false;
                        if (!mGp.profilePassword.equals("")) encrypt_required = true;
                        String fd = fpath.substring(0, fpath.lastIndexOf("/"));
                        String fn = fpath.replace(fd + "/", "");
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
        String dt= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(System.currentTimeMillis());
        String fn=APPLICATION_TAG+"_profile_"+dt.substring(0,10).replaceAll("/","-")+"_"+dt.substring(11).replaceAll(":","-")+".txt";
        mCommonDlg.fileSelectorFileOnlyWithCreate(true,
                mGp.internalRootDirectory, "", fn, mContext.getString(R.string.msgs_select_export_file), ntfy_file_select);
    }

    public void exportSyncTaskListToFile(final String profile_dir, final String profile_filename, final boolean encrypt_required) {

        File lf = new File(profile_dir + "/" + profile_filename);
        if (lf.exists()) {
            NotifyEvent ntfy = new NotifyEvent(mContext);
            ntfy.setListener(new NotifyEventListener() {
                @Override
                public void positiveResponse(Context c, Object[] o) {
                    String fp = profile_dir + "/" + profile_filename;
                    String fd = profile_dir;

                    if (saveSyncTaskListToFile(mGp, mContext, mUtil, true, fd, fp,
                            mGp.syncTaskAdapter.getArrayList(), encrypt_required)) {
                        mUtil.showCommonDialog(false, "I",
                                mContext.getString(R.string.msgs_export_prof_success), "File=" + fp, null);
                        mUtil.addDebugMsg(1, "I", "Profile was exported. fn=" + fp);
                        putExportedFileList(fp);
                    } else {
                        mUtil.showCommonDialog(false, "E",
                                mContext.getString(R.string.msgs_export_prof_fail), "File=" + fp, null);
                    }
                }

                @Override
                public void negativeResponse(Context c, Object[] o) {
                }
            });
            mUtil.showCommonDialog(true, "W",
                    mContext.getString(R.string.msgs_export_prof_title),
                    profile_dir + "/" + profile_filename + " " + mContext.getString(R.string.msgs_override), ntfy);
        } else {
            String fp = profile_dir + "/" + profile_filename;
            String fd = profile_dir;
            if (saveSyncTaskListToFile(mGp, mContext, mUtil, true, fd, fp,
                    mGp.syncTaskAdapter.getArrayList(), encrypt_required)) {
                mUtil.showCommonDialog(false, "I",
                        mContext.getString(R.string.msgs_export_prof_success), "File=" + fp, null);
                mUtil.addDebugMsg(1, "I", "Profile was exported. fn=" + fp);
                putExportedFileList(fp);
            } else {
                mUtil.showCommonDialog(false, "E",
                        mContext.getString(R.string.msgs_export_prof_fail), "File=" + fp, null);
            }
        }
    }

    final static String EXPORTED_FILE_LIST_KEY="exported_file_list_key";
    private void putExportedFileList(String fp) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        ArrayList<String> saved_file_list=getExportedFileList();
        boolean save_required=false;
        if (!saved_file_list.contains(fp)) {
            saved_file_list.add(fp);
            Collections.sort(saved_file_list, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    File o1_file=new File(o1);
                    File o2_file=new File(o2);
                    return (int)(o2_file.lastModified()-o1_file.lastModified());
                }
            });
            if (saved_file_list.size()>9) {
                for(int i=9;i<saved_file_list.size();i++) saved_file_list.remove(i);
            }
            save_required=true;
        }
        if (prefs.contains(EXPORTED_FILE_LIST_KEY)) {
            prefs.edit().remove(EXPORTED_FILE_LIST_KEY).commit();
            save_required=true;
        }
        if (save_required) {
            String saved_list="";
            for(String fp_item:saved_file_list) {
                saved_list+=fp_item+"\n";
            }

            try {
                File sfd=new File(mGp.settingMgtFileDir);
                sfd.mkdirs();
                File sfl=new File(mGp.settingMgtFileDir+"/.saved_sync_task_file_list");
                FileOutputStream fos=new FileOutputStream(sfl);
                fos.write(saved_list.getBytes());
                fos.flush();
                fos.close();
            } catch(Exception e) {}
        }

    }

    private ArrayList<String> getExportedFileList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String saved_list_string=null;
        try {
            File sfd=new File(mGp.settingMgtFileDir);
            sfd.mkdirs();
            File sfl=new File(mGp.settingMgtFileDir+"/.saved_sync_task_file_list");
            if (sfl.exists()) {
                FileInputStream fis=new FileInputStream(sfl);
                byte[] buff=new byte[1024*1024];
                int rc=fis.read(buff);
                fis.close();
                saved_list_string=new String(buff,0,rc);
            } else {
                saved_list_string=prefs.getString(EXPORTED_FILE_LIST_KEY, null);
            }
        } catch(Exception e) {}
        ArrayList<String> file_list=new ArrayList<String>();
        if (saved_list_string!=null) {
            String[] saved_array=saved_list_string.split("\n");
            for (String saved_fp:saved_array) {
                if (saved_fp!=null && !saved_fp.equals("")) {
                    File lf=new File(saved_fp);
                    if (lf.exists()) file_list.add(saved_fp);
                }
            }
            Collections.sort(file_list, new Comparator<String>(){
                @Override
                public int compare(String o1, String o2) {
                    File o1_file=new File(o1);
                    File o2_file=new File(o2);
                    return (int)(o2_file.lastModified()-o1_file.lastModified());
                }
            });
        }
        return file_list;
    }

    static public void setAllSyncTaskToUnchecked(boolean hideCheckBox, AdapterSyncTask pa) {
        pa.setAllItemChecked(false);
        if (hideCheckBox) pa.setShowCheckBox(false);
        pa.notifyDataSetChanged();
    }

    public void setSyncTaskToAuto(GlobalParameters gp) {
        SyncTaskItem item;

        for (int i = 0; i < gp.syncTaskAdapter.getCount(); i++) {
            item = gp.syncTaskAdapter.getItem(i);
            if (item.isChecked()) {
                item.setSyncTaskAuto(true);
            }
        }

        saveSyncTaskList(mGp, mContext, mUtil, gp.syncTaskAdapter.getArrayList());
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
            }
        }

        saveSyncTaskListToFile(mGp, mContext, mUtil, false, "", "", mGp.syncTaskAdapter.getArrayList(), false);
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
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                ArrayList<SyncTaskItem> dpItemList = new ArrayList<SyncTaskItem>();
//                ArrayList<ScheduleItem>sl=ScheduleUtil.loadScheduleData(c, mGp);
                int pos = mGp.syncTaskListView.getFirstVisiblePosition();
                for (int i = 0; i < dpnum.length; i++) {
                    if (dpnum[i] != -1)
                        dpItemList.add(mGp.syncTaskAdapter.getItem(dpnum[i]));
                }
                for (int i = 0; i < dpItemList.size(); i++) {
                    mGp.syncTaskAdapter.remove(dpItemList.get(i));
                    mUtil.addDebugMsg(1,"I","Sync task deleted, name="+dpItemList.get(i).getSyncTaskName());
                    ScheduleUtil.removeSyncTaskFromSchedule(mGp, mUtil, mGp.syncTabScheduleList, dpItemList.get(i).getSyncTaskName());
                }
                mGp.syncTabScheduleAdapter.notifyDataSetChanged();
                ScheduleUtil.saveScheduleData(c, mGp, mGp.syncTabScheduleList);

                mGp.syncTaskAdapter.sort();
                mGp.syncTaskAdapter.notifyDataSetChanged();

                saveSyncTaskList(mGp, mContext, mUtil, mGp.syncTaskAdapter.getArrayList());

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
        mUtil.showCommonDialog(true, "W",
                mContext.getString(R.string.msgs_delete_following_profile),
                mContext.getString(R.string.msgs_task_name_remove_with_schedule_group_task_list)+"\n\n"+dpmsg+"\n", ntfy);
    }

    public void showSelectSdcardMsg(final NotifyEvent ntfy) {
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.show_select_sdcard_dlg);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.show_select_sdcard_dlg_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.show_select_sdcard_dlg_title);

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
        btnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                ntfy.notifyToListener(true, null);
            }
        });
        // Cancelボタンの指定
        btnCancel.setOnClickListener(new OnClickListener() {
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

    public void showSelectUsbMsg(final NotifyEvent ntfy) {
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.show_select_sdcard_dlg);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.show_select_sdcard_dlg_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.show_select_sdcard_dlg_title);
        title.setText(R.string.msgs_main_external_usb_select_required_title);

        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.show_select_sdcard_dlg_msg);
        String msg = "";
        msg = mContext.getString(R.string.msgs_main_external_usb_select_required_select_msg_api23);
        dlg_msg.setText(msg);

        final ImageView func_view = (ImageView) dialog.findViewById(R.id.show_select_sdcard_dlg_image);


        try {
            String fn = "";
            fn = mContext.getString(R.string.msgs_main_external_usb_select_required_select_msg_file_api23);
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
        btnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                ntfy.notifyToListener(true, null);
            }
        });
        // Cancelボタンの指定
        btnCancel.setOnClickListener(new OnClickListener() {
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
            if (pli.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void testSmbLogonDlg(final String host, final String addr, final String port, final String share,
                                RemoteAuthInfo ra, final NotifyEvent p_ntfy) {
        final ThreadCtrl tc = new ThreadCtrl();
        tc.setEnabled();
        tc.setThreadResultSuccess();

        final Dialog dialog=showProgressSpinIndicator(mActivity);

        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                tc.setDisabled();//disableAsyncTask();
                mUtil.addDebugMsg(1, "W", "Logon is cancelled.");
            }
        });
        dialog.show();

        Thread th = new Thread() {
            @Override
            public void run() {
                String un="";
                if (mGp.settingSecurityReinitSmbAccountPasswordValue && !mGp.settingSecurityApplicationPasswordHashValue.equals("")) {
                    if (ra.smb_user_name!=null) un=(ra.smb_user_name.equals(""))?"":"????????";
                    else un=null;
                } else {
                    un=ra.smb_user_name;
                }
                mUtil.addDebugMsg(1, "I", "Test logon started, host=" + host + ", addr=" + addr + ", port=" + port + ", user=" + un);
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        dialog.dismiss();
                        String err_msg = (String) o[0];
                        if (tc.isEnabled()) {
                            if (err_msg != null) {
                                mUtil.showCommonDialog(false, "E", mContext.getString(R.string.msgs_remote_profile_dlg_logon_error)
                                        , err_msg, null);
                                if (p_ntfy != null) p_ntfy.notifyToListener(false, null);
                            } else {
                                mUtil.showCommonDialog(false, "I", mContext.getString(R.string.msgs_remote_profile_dlg_logon_success), "", null);
                                if (p_ntfy != null) p_ntfy.notifyToListener(true, null);
                            }
                        } else {
                            //NOP
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });

                if (host.equals("")) {
                    boolean reachable = false;
                    int port_num=0;
                    if (!port.equals("")) {
                        try {
                            port_num=Integer.parseInt(port);
                        } catch(Exception e) {
                            mUtil.addDebugMsg(1, "I", "Test logon failed, invalid port number specified");
                            String unreachble_msg = "";
                            unreachble_msg = String.format(mContext.getString(R.string.msgs_mirror_smb_invalid_port_specified), port);
                            ntfy.notifyToListener(true, new Object[]{unreachble_msg});
                        }
                    }
                    reachable = true;
                    if (reachable) {
                        testSmbAuth(addr, port, share, ra, ntfy);
                    } else {
                        mUtil.addDebugMsg(1, "I", "Test logon failed, remote server not connected");
                        String unreachble_msg = "";
                        if (port.equals("")) {
                            unreachble_msg = String.format(mContext.getString(R.string.msgs_mirror_smb_addr_not_connected), addr);
                        } else {
                            unreachble_msg = String.format(mContext.getString(R.string.msgs_mirror_smb_addr_not_connected_with_port), addr, port);
                        }
                        ntfy.notifyToListener(true, new Object[]{unreachble_msg});
                    }
                } else {
                    int smb_level=Integer.parseInt(ra.smb_smb_protocol);
                    String ipAddress = CommonUtilities.resolveHostName(mGp, mUtil, smb_level, host);
                    if (ipAddress != null) testSmbAuth(CommonUtilities.addScopeidToIpv6Address(ipAddress), port, share, ra, ntfy);
                    else {
                        mUtil.addDebugMsg(1, "I", "Test logon failed, remote server not connected");
                        String unreachble_msg = "";
                        unreachble_msg = mContext.getString(R.string.msgs_mirror_smb_name_not_found) + host;
                        ntfy.notifyToListener(true, new Object[]{unreachble_msg});
                    }
                }
            }
        };
        th.start();
    }

    private void testSmbAuth(final String host, String port, String share, RemoteAuthInfo ra, final NotifyEvent ntfy) {
        final UncaughtExceptionHandler defaultUEH = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
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

        InetAddress ia=CommonUtilities.getInetAddress(host);
        if ((ia instanceof Inet6Address)) {//IPV6
            String scopeid="";
            if (ia.isLinkLocalAddress()) {
                if (!host.contains("%")) {
                    scopeid="%wlan0";
                }
            }
            if (port.equals("")) url = "smb://" + "["+host+scopeid+"]" ;
            else url = "smb://" + "["+host+scopeid+"]" + ":" + port;
        } else {
            if (port.equals("")) url = "smb://" + host ;//+ "/"+share+"/";
            else url = "smb://" + host + ":" + port;// + "/"+share+"/";
        }

        String un="";
        if (mGp.settingSecurityReinitSmbAccountPasswordValue  && !mGp.settingSecurityApplicationPasswordHashValue.equals("")) {
            if (ra.smb_user_name!=null) un=(ra.smb_user_name.equals(""))?"":"????????";
            else un=null;
        } else {
            un=ra.smb_user_name;
        }


        JcifsAuth auth=null;
        int smb_level=Integer.parseInt(ra.smb_smb_protocol);
        if (ra.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1)) {
            auth=new JcifsAuth(JcifsAuth.JCIFS_FILE_SMB1, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password);
        } else {
            auth=new JcifsAuth(smb_level, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password, ra.smb_ipc_signing_enforced, ra.smb_use_smb2_negotiation);
        }
        try {
            JcifsFile sf = new JcifsFile(url, auth);
//            String[] fl=sf.list();
            sf.connect();
            mUtil.addDebugMsg(1, "I", "Test logon completed, host="+host+", port="+port+", Smb Proto="+ra.smb_smb_protocol+", user="+un);
        } catch (JcifsException e) {
            String cm="";
            try {
                cm=e.getCause().getCause().getMessage();
            } catch(Exception ex) {
            }
            String[] e_msg = JcifsUtil.analyzeNtStatusCode(e, url, un);
            err_msg = cm+"\n"+e_msg[0];
            mUtil.addDebugMsg(1, "I", "Test logon failed." + "\n" + err_msg);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            err_msg = e.getMessage();
            mUtil.addDebugMsg(1, "I", "Test logon failed." + "\n" + err_msg);
        }
        Thread.currentThread().setUncaughtExceptionHandler(defaultUEH);
        ntfy.notifyToListener(true, new Object[]{err_msg});
    }

    public void copySyncTask(SyncTaskItem pli, NotifyEvent p_ntfy) {
        final SyncTaskUtil stu=this;
        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                SyncTaskItem npfli = pli.clone();
                npfli.setLastSyncResult(0);
                npfli.setLastSyncTime("");
                SyncTaskEditor pmsp = SyncTaskEditor.newInstance();
                pmsp.showDialog(mFragMgr, pmsp, "COPY", npfli, stu, mUtil, mCommonDlg, mGp, p_ntfy);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        ApplicationPasswordUtil.applicationPasswordAuthentication(mGp, mActivity, mFragMgr, mUtil, false, ntfy, ApplicationPasswordUtil.APPLICATION_PASSWORD_RESOURCE_EDIT_SYNC_TASK);
    }

    public void renameSyncTask(final SyncTaskItem pli, final NotifyEvent p_ntfy) {

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setContentView(R.layout.single_item_input_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_dlg_view);
        CommonUtilities.setDialogBoxOutline(mContext, ll_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.single_item_input_title);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        title.setTextColor(mGp.themeColorList.title_text_color);

		final TextView dlg_msg = (TextView) dialog.findViewById(R.id.single_item_input_msg);
        final TextView dlg_cmp = (TextView) dialog.findViewById(R.id.single_item_input_name);
        final Button btn_ok = (Button) dialog.findViewById(R.id.single_item_input_ok_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.single_item_input_cancel_btn);
        final EditText etInput = (EditText) dialog.findViewById(R.id.single_item_input_dir);

        title.setText(mContext.getString(R.string.msgs_rename_profile));

        dlg_cmp.setVisibility(TextView.VISIBLE);
        dlg_cmp.setText(mContext.getString(R.string.msgs_task_name_rename_with_schedule_group_task_list));

        dlg_msg.setVisibility(TextView.VISIBLE);
        dlg_msg.setTextColor(mGp.themeColorList.text_color_error);
//        String e_msg=isValidSyncTaskName(mContext, mGp.syncTaskList, pli.getSyncTaskName());
//        if (!e_msg.equals("")) {
//            dlg_msg.setText(e_msg);
//            dlg_msg.setVisibility(TextView.VISIBLE);
//        }

        CommonDialog.setDlgBoxSizeCompactWithInput(dialog);
        etInput.setText(pli.getSyncTaskName());
        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                String edit_text=arg0.toString();
                String error_msg = isValidSyncTaskName(mContext, mGp.syncTaskList, edit_text);
                if (!error_msg.equals("")) {
                    dlg_msg.setText(error_msg);
                    dlg_msg.setVisibility(TextView.VISIBLE);
                    CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                } else {
                    dlg_msg.setText("");
                    dlg_msg.setVisibility(TextView.GONE);
                    CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });

        //OK button
        btn_ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
//                ArrayList<ScheduleItem>sl=ScheduleUtil.loadScheduleData(mActivity, mGp);

                String new_name = etInput.getText().toString();
                String prev_name=pli.getSyncTaskName();
                pli.setSyncTaskName(new_name);
                mUtil.addDebugMsg(1,"I","Sync task renamed, from="+prev_name+", new="+new_name);
                ScheduleUtil.renameSyncTaskFromSchedule(mGp, mUtil, mGp.syncTabScheduleList, prev_name, new_name);
                mGp.syncTabScheduleAdapter.notifyDataSetChanged();
                ScheduleUtil.saveScheduleData(mActivity, mGp, mGp.syncTabScheduleList);

                mGp.syncTaskAdapter.sort();
                mGp.syncTaskAdapter.notifyDataSetChanged();

                saveSyncTaskList(mGp, mContext, mUtil, mGp.syncTaskAdapter.getArrayList());

                SyncTaskUtil.setAllSyncTaskToUnchecked(true, mGp.syncTaskAdapter);

                p_ntfy.notifyToListener(true, null);
            }
        });
        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new OnClickListener() {
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

    public void invokeScanSmbServerDlg(Dialog dialog) {
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
        final Spinner sp_sync_folder_smb_proto = (Spinner) dialog.findViewById(R.id.edit_sync_folder_dlg_smb_protocol);
//        scanSmbServerDlg(ntfy, port_num, false, sp_sync_folder_smb_proto.getSelectedItemPosition()+1);
        scanSmbServerDlg(ntfy, port_num, true, sp_sync_folder_smb_proto.getSelectedItemPosition()+1);
    }

    public void invokeSelectSmbShareDlg(Dialog dialog) {
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
        final CheckedTextView ctv_sync_folder_smb_use_smb2_negotiation = (CheckedTextView) dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_smb_use_smb2_negotiation);
        String remote_addr="", remote_user = "", remote_pass = "", remote_host="";

        if (ctv_use_userpass.isChecked()) {
            remote_user = edituser.getText().toString().trim();
            remote_pass = editpass.getText().toString();
        }

        final String smb_proto=""+(sp_sync_folder_smb_proto.getSelectedItemPosition()+1);
        final boolean ipc_enforced=ctv_sync_folder_smb_ipc_enforced.isChecked();
        final boolean smb2_negotiation=ctv_sync_folder_smb_use_smb2_negotiation.isChecked();
        setSmbUserPass(remote_user, remote_pass);
        String host=edithost.getText().toString().trim();
        if (JcifsUtil.isValidIpAddress(host)) {
            remote_addr = host;
        } else {
            if (host.contains(":")) remote_addr=host;
            else remote_host = host;
        }
        String remote_port = "";
        if (ctv_use_port_number.isChecked() && editport.getText().length() > 0)
            remote_port = editport.getText().toString();
        NotifyEvent ntfy = new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context arg0, Object[] arg1) {
                editshare.setText((String) arg1[0]);
            }

            @Override
            public void negativeResponse(Context arg0, Object[] arg1) {
                if (arg1 != null) {
                    String msg_text = (String) arg1[0];
                    mUtil.showCommonDialog(false, "E", "SMB Error", msg_text, null);
                }
            }

        });
        selectRemoteShareDlg(remote_host, remote_addr, remote_port, ipc_enforced, smb2_negotiation, smb_proto, ntfy);
    }

    public void setSmbUserPass(String user, String pass) {
        smbUser = user;
        smbPass = pass;
    }

    public void selectRemoteDirectoryDlg(Dialog p_dialog, final boolean show_create) {
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
        final CheckedTextView ctv_sync_folder_smb_use_smb2_negotiation = (CheckedTextView) p_dialog.findViewById(R.id.edit_sync_folder_dlg_ctv_smb_use_smb2_negotiation);
        String t_remote_addr="", remote_user = "", remote_pass = "", t_remote_host="";
        if (ctv_use_userpass.isChecked()) {
            remote_user = edituser.getText().toString();
            remote_pass = editpass.getText().toString();
        }

        final String smb_proto=""+(sp_sync_folder_smb_proto.getSelectedItemPosition()+1);
        final boolean ipc_enforced=ctv_sync_folder_smb_ipc_enforced.isChecked();
        final boolean smb2_negotiation=ctv_sync_folder_smb_use_smb2_negotiation.isChecked();

        final String p_dir = editdir.getText().toString();

        setSmbUserPass(remote_user, remote_pass);
        String host = edithost.getText().toString();
        if (JcifsUtil.isValidIpAddress(host)) {
            t_remote_addr = host;
        } else {
            if (host.contains(":")) t_remote_addr = host;
            else t_remote_host = host;
        }
        String t_remote_port = "";
        if (ctv_use_port_number.isChecked() && editport.getText().length() > 0)
            t_remote_port = editport.getText().toString();
        final String remote_addr=t_remote_addr;
        final String remote_host=t_remote_host;
        final String remote_share = editshare.getText().toString();
        final String remote_port=t_remote_port;
//        mSmbBaseUrl="smb://" + t_url + h_port + "/" + remote_share + "/";

        final ArrayList<TreeFilelistItem> rows = new ArrayList<TreeFilelistItem>();
        NotifyEvent ntfy = new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                @SuppressWarnings("unchecked")
                ArrayList<TreeFilelistItem> rfl = (ArrayList<TreeFilelistItem>) o[0];

                for (int i = 0; i < rfl.size(); i++) {
                    if (rfl.get(i).isDir() && rfl.get(i).canRead()) rows.add(rfl.get(i));
                }
                Collections.sort(rows);
                NotifyEvent ntfy_sel=new NotifyEvent(mContext);
                ntfy_sel.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] o) {
                        String sel = (String)o[0];
                        editdir.setText(sel.startsWith("/")?sel.substring(1):sel);
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                remoteDirectorySelector(rows, remote_host, remote_addr, remote_share, remote_port, p_dir, ipc_enforced, smb2_negotiation, smb_proto, show_create, ntfy_sel);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
                String msg_text = (String) o[0];
                mUtil.showCommonDialog(false, "E", "SMB Error", msg_text, null);
            }
        });
//        createRemoteFileList(remurl, p_dir, ipc_enforced, smb_proto, ntfy, true);
        createRemoteFileList(remote_host, remote_addr, remote_share, remote_port, "", ipc_enforced, smb2_negotiation, smb_proto, ntfy, true);
    }

    private void remoteDirectorySelector(ArrayList<TreeFilelistItem> rows, String host_name, String host_addr, String host_share, String host_port, String p_dir,
                                         boolean ipc_enforced, boolean smb2_negotiation, String smb_proto, final boolean show_create, final NotifyEvent p_ntfy) {
        //カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.common_file_selector_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.common_file_selector_dlg_view);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.common_file_selector_dlg_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.common_file_selector_dlg_title);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        title.setTextColor(mGp.themeColorList.title_text_color);

        final TextView tv_empty = (TextView) dialog.findViewById(R.id.common_file_selector_empty);
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.common_file_selector_dlg_msg);
        dlg_msg.setVisibility(TextView.GONE);

        final Spinner mp = (Spinner) dialog.findViewById(R.id.common_file_selector_storage_spinner);
        mp.setVisibility(LinearLayout.GONE);
        final LinearLayout dir_name_view = (LinearLayout) dialog.findViewById(R.id.common_file_selector_dir_name_view);
        dir_name_view.setVisibility(LinearLayout.GONE);
        final EditText et_dir_name = (EditText) dialog.findViewById(R.id.common_file_selector_dir_name);

        final String directory_pre="smb://"+host_name+host_addr+"/"+host_share;

        final CustomTextView tv_home = (CustomTextView) dialog.findViewById(R.id.common_file_selector_filepath);
//        tv_home.setTextColor(mGp.themeColorList.text_color_primary);
        tv_home.setText(directory_pre);

        final Button btn_create = (Button) dialog.findViewById(R.id.common_file_selector_create_btn);
        if (show_create) btn_create.setVisibility(Button.VISIBLE);
        else btn_create.setVisibility(Button.GONE);
        title.setText(mContext.getString(R.string.msgs_select_remote_dir));
        final Button btn_ok = (Button) dialog.findViewById(R.id.common_file_selector_btn_ok);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.common_file_selector_btn_cancel);
        final Button btn_up = (Button) dialog.findViewById(R.id.common_file_selector_up_btn);
        final Button btn_top = (Button) dialog.findViewById(R.id.common_file_selector_top_btn);
        final Button btn_refresh = (Button) dialog.findViewById(R.id.common_file_selector_refresh_btn);

        if (mGp.isScreenThemeIsLight()) {
            btn_up.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_dark, 0, 0, 0);
            btn_top.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_dark, 0, 0, 0);
        } else {
            btn_up.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_light, 0, 0, 0);
            btn_top.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_light, 0, 0, 0);
        }

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        RemoteAuthInfo ra=new RemoteAuthInfo();
        ra.smb_smb_protocol=smb_proto;
        ra.smb_ipc_signing_enforced=ipc_enforced;
        ra.smb_use_smb2_negotiation=smb2_negotiation;
        ra.smb_domain_name=null;
        ra.smb_user_name=smbUser.length()==0?null:smbUser;
        ra.smb_user_password=smbPass.length()==0?null:smbPass;

        final ListView lv = (ListView) dialog.findViewById(R.id.common_file_selector_list);
        final TreeFilelistAdapter tfa = new TreeFilelistAdapter(mActivity, true, false);
        if (rows.size()==0) {
            tv_empty.setVisibility(TextView.VISIBLE);
            lv.setVisibility(ListView.GONE);
        } else {
            tv_empty.setVisibility(TextView.GONE);
            lv.setVisibility(ListView.VISIBLE);
        }
        tfa.setDataList(rows);
        tfa.setSelectable(false);
        lv.setAdapter(tfa);
        lv.setScrollingCacheEnabled(false);
        lv.setScrollbarFadingEnabled(false);

        setTopUpButtonEnabled(dialog, false);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                final int pos = tfa.getItem(idx);
                final TreeFilelistItem tfi = tfa.getDataItem(pos);
                if (tfi.isDir()) {
                    final String n_dir=tfi.getPath()+tfi.getName()+"/";
                    if (tfi.getSubDirItemCount()>=0) {
                        NotifyEvent ntfy = new NotifyEvent(mContext);
                        ntfy.setListener(new NotifyEventListener() {
                            @Override
                            public void positiveResponse(Context c, Object[] o) {
                                setTopUpButtonEnabled(dialog, true);
                                tv_home.setText(directory_pre+n_dir);
                                ArrayList<TreeFilelistItem> rfl = (ArrayList<TreeFilelistItem>) o[0];
                                ArrayList<TreeFilelistItem> new_tfl = new ArrayList<TreeFilelistItem>();
                                for (int i = 0; i < rfl.size(); i++) {
                                    if (rfl.get(i).isDir() && rfl.get(i).canRead()) new_tfl.add(rfl.get(i));
                                }
                                Collections.sort(new_tfl);
                                if (new_tfl.size()==0) {
                                    tv_empty.setVisibility(TextView.VISIBLE);
                                    lv.setVisibility(ListView.GONE);
                                } else {
                                    tv_empty.setVisibility(TextView.GONE);
                                    lv.setVisibility(ListView.VISIBLE);
                                }
                                tfa.setDataList(new_tfl);
                            }

                            @Override
                            public void negativeResponse(Context c, Object[] o) {
                                String msg_text = (String) o[0];
                                mUtil.showCommonDialog(false, "E", "SMB Error", msg_text, null);
                            }
                        });
                        createRemoteFileList(host_name, host_addr, host_share, host_port, n_dir, ipc_enforced, smb2_negotiation, smb_proto, ntfy, true);
                    }
                } else {

                }
            }
        });

        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> items, View view, int idx, long id) {
                final int pos = tfa.getItem(idx);
                final TreeFilelistItem tfi = tfa.getDataItem(pos);
                tfi.setChecked(true);
                tfa.notifyDataSetChanged();
                return true;
            }
        });

        NotifyEvent ctv_ntfy = new NotifyEvent(mContext);
        ctv_ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                if (o != null) {
                    int pos = (Integer) o[0];
                    final TreeFilelistItem tfi = tfa.getDataItem(pos);
                    et_dir_name.setText((tfi.getPath()+tfi.getName()).substring(1));
                }
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
            }
        });
        tfa.setCbCheckListener(ctv_ntfy);

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
                createRemoteDirectoryDlg(tv_home.getText().toString(), ra, ne);
            }
        });

        btn_refresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ntfy_refresh=new NotifyEvent(mContext);
                ntfy_refresh.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] o) {
                        tv_home.setText(directory_pre+tv_home.getText().toString().replace(directory_pre,""));
                        ArrayList<TreeFilelistItem> new_rfl = (ArrayList<TreeFilelistItem>) o[0];

                        if (new_rfl.size()==0) {
                            tv_empty.setVisibility(TextView.VISIBLE);
                            lv.setVisibility(ListView.GONE);
                        } else {
                            tv_empty.setVisibility(TextView.GONE);
                            lv.setVisibility(ListView.VISIBLE);
                        }

                        tfa.setDataList(new_rfl);
                        tfa.notifyDataSetChanged();
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                createRemoteFileList(host_name, host_addr, host_share, host_port, "/"+tv_home.getText().toString().replace(directory_pre,""),
                        ipc_enforced, smb2_negotiation, smb_proto, ntfy_refresh, true);
            }
        });

        btn_up.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final String c_dir=tv_home.getText().toString().replace(directory_pre,"");
                String t_dir=c_dir.substring(0,c_dir.lastIndexOf("/"));
                final String n_dir=t_dir.lastIndexOf("/")>0?t_dir.substring(0,t_dir.lastIndexOf("/"))+"/":"";

                NotifyEvent ntfy_refresh=new NotifyEvent(mContext);
                ntfy_refresh.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] o) {
                        tv_home.setText(directory_pre+n_dir);
                        if (n_dir.equals("")) setTopUpButtonEnabled(dialog, false);
                        else setTopUpButtonEnabled(dialog, true);
                        ArrayList<TreeFilelistItem> new_rfl = (ArrayList<TreeFilelistItem>) o[0];
                        if (new_rfl.size()==0) {
                            tv_empty.setVisibility(TextView.VISIBLE);
                            lv.setVisibility(ListView.GONE);
                        } else {
                            tv_empty.setVisibility(TextView.GONE);
                            lv.setVisibility(ListView.VISIBLE);
                        }
                        tfa.setDataList(new_rfl);
                        tfa.notifyDataSetChanged();
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                createRemoteFileList(host_name, host_addr, host_share, host_port, "/"+n_dir, ipc_enforced, smb2_negotiation, smb_proto, ntfy_refresh, true);
            }
        });

        btn_top.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ntfy_refresh=new NotifyEvent(mContext);
                ntfy_refresh.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] o) {
                        setTopUpButtonEnabled(dialog, false);
                        tv_home.setText(directory_pre);
                        ArrayList<TreeFilelistItem> new_rfl = (ArrayList<TreeFilelistItem>) o[0];
                        if (new_rfl.size()==0) {
                            tv_empty.setVisibility(TextView.VISIBLE);
                            lv.setVisibility(ListView.GONE);
                        } else {
                            tv_empty.setVisibility(TextView.GONE);
                            lv.setVisibility(ListView.VISIBLE);
                        }
                        tfa.setDataList(new_rfl);
                        tfa.notifyDataSetChanged();
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                createRemoteFileList(host_name, host_addr, host_share, host_port, "", ipc_enforced, smb2_negotiation, smb_proto, ntfy_refresh, true);
            }
        });

        //OKボタンの指定
//        btn_ok.setEnabled(false);
        btn_ok.setVisibility(Button.VISIBLE);
        btn_ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                String sel=tv_home.getText().toString().replace(directory_pre,"");
                if (sel.endsWith("/")) p_ntfy.notifyToListener(true, new Object[]{sel.substring(0,sel.length()-1)});
                else p_ntfy.notifyToListener(true, new Object[]{sel});
            }
        });
        //CANCELボタンの指定
        btn_cancel.setOnClickListener(new OnClickListener() {
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

    private void setTopUpButtonEnabled(Dialog dialog, boolean p) {
        final Button btnTop = (Button)dialog.findViewById(R.id.common_file_selector_top_btn);
        final Button btnUp = (Button)dialog.findViewById(R.id.common_file_selector_up_btn);

        CommonDialog.setViewEnabled(mActivity, btnUp, p);
        CommonDialog.setViewEnabled(mActivity, btnTop, p);
    };


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

    static public SyncTaskItem getUsbMediaUsedSyncTask(GlobalParameters gp) {
        SyncTaskItem pli = null;
        for (int i = 0; i < gp.syncTaskAdapter.getCount(); i++) {
            if (gp.syncTaskAdapter.getItem(i).getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
                pli = gp.syncTaskAdapter.getItem(i);
                break;
            }
        }
        return pli;
    }

    private void createRemoteDirectoryDlg(final String c_dir, final RemoteAuthInfo ra, final NotifyEvent p_ntfy) {
        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.single_item_input_dlg);

        final LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_dlg_view);
        CommonUtilities.setDialogBoxOutline(mContext, ll_dlg_view);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);

        final TextView dlg_title = (TextView) dialog.findViewById(R.id.single_item_input_title);
        dlg_title.setTextColor(mGp.themeColorList.title_text_color);
        dlg_title.setText(mContext.getString(R.string.msgs_file_select_edit_dlg_create));
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.single_item_input_msg);
        final TextView dlg_cmp = (TextView) dialog.findViewById(R.id.single_item_input_name);
//        dlg_cmp.setTextColor(mGp.themeColorList.text_color_primary);
        final Button btnOk = (Button) dialog.findViewById(R.id.single_item_input_ok_btn);
        final Button btnCancel = (Button) dialog.findViewById(R.id.single_item_input_cancel_btn);
        final EditText etDir=(EditText) dialog.findViewById(R.id.single_item_input_dir);

        dlg_cmp.setText(mContext.getString(R.string.msgs_file_select_edit_parent_directory)+":"+c_dir);

        CommonDialog.setDlgBoxSizeCompact(dialog);

        CommonDialog.setViewEnabled(mActivity, btnOk, false);
        final Handler hndl=new Handler();
        etDir.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0) {
                    CommonDialog.setViewEnabled(mActivity, btnOk, true);
                } else {
                    CommonDialog.setViewEnabled(mActivity, btnOk, false);
                    dlg_msg.setText("");
                }
            }
        });

        //OK button
        btnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final String creat_dir=etDir.getText().toString();
                final String n_path=c_dir+"/"+creat_dir+"/";
                NotifyEvent ne_exists=new NotifyEvent(mContext);
                ne_exists.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        boolean n_exists=(boolean)objects[0];
                        if (n_exists) {
                            hndl.post(new Runnable() {
                                  @Override
                                  public void run() {
                                      dlg_msg.setText(mContext.getString(R.string.msgs_single_item_input_dlg_duplicate_dir));
                                  }
                            });
                            return;
                        }
                        NotifyEvent ntfy_confirm=new NotifyEvent(mContext);
                        ntfy_confirm.setListener(new NotifyEventListener(){
                            @Override
                            public void positiveResponse(Context c, Object[] o) {
                                NotifyEvent notify_create=new NotifyEvent(mContext);
                                notify_create.setListener(new NotifyEventListener() {
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
                                                final String e_msg=(String)objects[0];
                                                hndl.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mUtil.showCommonDialog(false,"E","SMB Error",e_msg,null);
                                                        dialog.dismiss();
                                                        p_ntfy.notifyToListener(false, null);
                                                    }
                                                });

                                            }
                                        });
                                    }
                                });
                                createRemoteDirectory(c_dir+"/"+etDir.getText().toString(), ra, notify_create);
                            }
                            @Override
                            public void negativeResponse(Context c, Object[] o) {
                            }
                        });
                        CommonDialog cd=new CommonDialog(mContext, mFragMgr);
                        cd.showCommonDialog(true, "W", mContext.getString(R.string.msgs_file_select_edit_confirm_create_directory), n_path, ntfy_confirm);
                    }
                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                isRemoteItemExists(n_path, ra, ne_exists);
            }
        });
        // CANCELボタンの指定
        btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                p_ntfy.notifyToListener(false, null);
            }
        });
        dialog.show();
    };

    private void isRemoteItemExists(final String new_dir, final RemoteAuthInfo ra, final NotifyEvent p_ntfy) {
        final Dialog dialog=showProgressSpinIndicator(mActivity);
        dialog.show();
        Thread th=new Thread(){
          @Override
          public void run() {
              JcifsAuth auth=null;
              int smb_level=Integer.parseInt(ra.smb_smb_protocol);
              if (ra.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1)) {
                  auth=new JcifsAuth(JcifsAuth.JCIFS_FILE_SMB1, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password);
              } else {
                  auth=new JcifsAuth(smb_level, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password);
              }
              try {
                  JcifsFile jf=new JcifsFile(new_dir, auth);
                  if (jf.exists()) p_ntfy.notifyToListener(true, new Object[] {true});
                  else p_ntfy.notifyToListener(true, new Object[] {false});
              } catch (MalformedURLException e) {
                  e.printStackTrace();
                  mUtil.addDebugMsg(1, "E", e.toString());
                  p_ntfy.notifyToListener(false, new Object[]{e.toString()});
              } catch (JcifsException e) {
                  e.printStackTrace();
                  String cause="";
                  String un="";
                  if (mGp.settingSecurityReinitSmbAccountPasswordValue  && !mGp.settingSecurityApplicationPasswordHashValue.equals("")) {
                      if (ra.smb_user_name!=null) un=(ra.smb_user_name.equals(""))?"":"????????";
                      else un=null;
                  } else {
                      un=ra.smb_user_name;
                  }
                  String[] e_msg= JcifsUtil.analyzeNtStatusCode(e, new_dir, un);
                  if (e.getCause()!=null) {
                      String tc=e.getCause().toString();
                      cause=tc.substring(tc.indexOf(":")+1);
                      e_msg[0]=cause+"\n"+e_msg[0];
                  }
                  mUtil.addDebugMsg(1, "E", e_msg[0]);
                  p_ntfy.notifyToListener(false, new Object[]{e_msg[0]});
              }
              dialog.dismiss();
          }
        };
        th.start();
    }

    private void createRemoteDirectory(final String new_dir, final RemoteAuthInfo ra, final NotifyEvent p_ntfy) {
        final Dialog dialog=showProgressSpinIndicator(mActivity);
        dialog.show();
        Thread th=new Thread(){
            @Override
            public void run() {
                JcifsAuth auth=null;
                int smb_level=Integer.parseInt(ra.smb_smb_protocol);
                if (ra.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1)) {
                    auth=new JcifsAuth(JcifsAuth.JCIFS_FILE_SMB1, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password);
                } else {
                    auth=new JcifsAuth(smb_level, ra.smb_domain_name, ra.smb_user_name, ra.smb_user_password);
                }
                try {
                    JcifsFile jf=new JcifsFile(new_dir, auth);
                    jf.mkdirs();
                    if (jf.exists()) p_ntfy.notifyToListener(true, null);
                    else p_ntfy.notifyToListener(false, null);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    mUtil.addDebugMsg(1, "E", e.toString());
                    p_ntfy.notifyToListener(false, new Object[]{e.toString()});
                } catch (JcifsException e) {
                    e.printStackTrace();
                    String cause="";
                    String un="";
                    if (mGp.settingSecurityReinitSmbAccountPasswordValue) {
                        un=(ra.smb_user_name.equals(""))?"":"????????";
                    } else {
                        un=ra.smb_user_name;
                    }
                    String[] e_msg=JcifsUtil.analyzeNtStatusCode(e, new_dir, un);
                    if (e.getCause()!=null) {
                        String tc=e.getCause().toString();
                        cause=tc.substring(tc.indexOf(":")+1);
                        e_msg[0]=cause+"\n"+e_msg[0];
                    }
                    mUtil.addDebugMsg(1, "E", e_msg[0]);
                    p_ntfy.notifyToListener(false, new Object[]{e_msg[0]});
                }
                dialog.dismiss();
            }
        };
        th.start();
    }

    public void editWifiAccessPointListDlg(final ArrayList<String> ap_list, final NotifyEvent p_ntfy) {
        ArrayList<AdapterFilterList.FilterListItem> filterList = new ArrayList<AdapterFilterList.FilterListItem>();
        final AdapterFilterList filterAdapter;

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.filter_list_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.filter_select_edit_title);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        title.setTextColor(mGp.themeColorList.title_text_color);
        title.setText(mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_ap_title));

        Button add_current_ssid = (Button) dialog.findViewById(R.id.filter_select_edit_list_dir_btn);
        add_current_ssid.setText(mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_ap_add_current_ap));

        filterAdapter = new AdapterFilterList(mActivity,
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
        CommonDialog.setViewEnabled(mActivity, btn_ok, false);

        NotifyEvent ntfy_inc_exc = new NotifyEvent(mContext);
        ntfy_inc_exc.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
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
                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
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
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }

                });
                editFilter(idx, filterAdapter, fli, fli.getFilter(),
                        mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_ap_edit_title), ntfy, null, null);
            }
        });

        // Addボタンの指定
        CommonDialog.setViewEnabled(mActivity, addBtn, false);
        et_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    String dup_filter= getDuplicateFilter(s.toString().trim(), filterAdapter);
                    if (!dup_filter.equals("")) {
                        String mtxt = mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_duplicate_ap_specified);
                        dlg_msg.setText(String.format(mtxt, s.toString().trim()));
                        CommonDialog.setViewEnabled(mActivity, addBtn, false);
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    } else {
                        dlg_msg.setText("");
                        CommonDialog.setViewEnabled(mActivity, addBtn, true);
                        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    }
                } else {
                    CommonDialog.setViewEnabled(mActivity, addBtn, false);
                    CommonDialog.setViewEnabled(mActivity, btn_ok, true);
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
        add_current_ssid.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                String ssid = CommonUtilities.getWifiSsidName(wm);
                if (!ssid.equals("")) {
                    String dup_filter= getDuplicateFilter(ssid, filterAdapter);
                    if (!dup_filter.equals("")) {
                        String mtxt = mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_duplicate_ap_specified);
                        dlg_msg.setText(String.format(mtxt, ssid));
                        CommonDialog.setViewEnabled(mActivity, addBtn, false);
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    } else {
                        dlg_msg.setText("");
                        filterAdapter.add(new AdapterFilterList.FilterListItem(ssid, true));
                        filterAdapter.setNotifyOnChange(true);
                        filterAdapter.sort();
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    }
                } else {
                    String mtxt = mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_ap_not_connected);
                    dlg_msg.setText(mtxt);
                }
            }
        });

        addBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dlg_msg.setText("");
                String newfilter = et_filter.getText().toString().trim();
                et_filter.setText("");
                filterAdapter.add(new AdapterFilterList.FilterListItem(newfilter, true));
                filterAdapter.setNotifyOnChange(true);
                filterAdapter.sort();
                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
            }
        });

        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new OnClickListener() {
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
        btn_ok.setOnClickListener(new OnClickListener() {
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

    public void editWifiIPAddressListDlg(final ArrayList<String> addr_list, final NotifyEvent p_ntfy) {
        ArrayList<AdapterFilterList.FilterListItem> filterList = new ArrayList<AdapterFilterList.FilterListItem>();
        final AdapterFilterList filterAdapter;

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.filter_list_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_view);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.filter_select_edit_title);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        title.setTextColor(mGp.themeColorList.title_text_color);
        title.setText(mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_addr_title));

        Button add_current_addr = (Button) dialog.findViewById(R.id.filter_select_edit_list_dir_btn);
        add_current_addr.setText(mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_addr_add_current_addr));

        filterAdapter = new AdapterFilterList(mActivity, R.layout.filter_list_item_view, filterList, false);
        ListView lv = (ListView) dialog.findViewById(R.id.filter_select_edit_listview);

        for (int i = 0; i < addr_list.size(); i++) {
            String inc = addr_list.get(i).substring(0, 1);
            String filter = addr_list.get(i).substring(1, addr_list.get(i).length());
            boolean b_inc = false;
            if (inc.equals(SMBSYNC2_PROF_FILTER_INCLUDE)) b_inc = true;
            filterAdapter.add(new AdapterFilterList.FilterListItem(filter, b_inc));
        }
        lv.setAdapter(filterAdapter);
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.filter_select_edit_msg);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        final EditText et_filter = (EditText) dialog.findViewById(R.id.filter_select_edit_new_filter);
        et_filter.setHint(mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_addr_hint));

        final Button addBtn = (Button) dialog.findViewById(R.id.filter_select_edit_add_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.filter_select_edit_cancel_btn);
        final Button btn_ok = (Button) dialog.findViewById(R.id.filter_select_edit_ok_btn);
        CommonDialog.setViewEnabled(mActivity, btn_ok, false);

        NotifyEvent ntfy_inc_exc = new NotifyEvent(mContext);
        ntfy_inc_exc.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
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
                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
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
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }

                });
                editFilter(idx, filterAdapter, fli, fli.getFilter(),
                        mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_addr_edit_title), ntfy, null, null);
            }
        });

        // Addボタンの指定
        CommonDialog.setViewEnabled(mActivity, addBtn, false);
        et_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    String dup_filter= getDuplicateFilter(s.toString().trim(), filterAdapter);
                    if (!dup_filter.equals("")) {
                        String mtxt = mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_duplicate_addr_specified);
                        dlg_msg.setText(String.format(mtxt, s.toString().trim()));
                        CommonDialog.setViewEnabled(mActivity, addBtn, false);
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    } else {
                        dlg_msg.setText("");
                        CommonDialog.setViewEnabled(mActivity, addBtn, true);
                        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    }
                } else {
                    CommonDialog.setViewEnabled(mActivity, addBtn, false);
                    CommonDialog.setViewEnabled(mActivity, btn_ok, true);
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
        add_current_addr.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                String ip_addr = CommonUtilities.getIfIpAddress(mUtil);
                if (!ip_addr.equals("")) {
                    String dup_filter= getDuplicateFilter(ip_addr, filterAdapter);
                    if (!dup_filter.equals("")) {
                        String mtxt = mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_duplicate_addr_specified);
                        dlg_msg.setText(String.format(mtxt, ip_addr));
                        CommonDialog.setViewEnabled(mActivity, addBtn, false);
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    } else {
                        dlg_msg.setText("");
                        filterAdapter.add(new AdapterFilterList.FilterListItem(ip_addr, true));
                        filterAdapter.setNotifyOnChange(true);
                        filterAdapter.sort();
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    }
                } else {
                    String mtxt = mContext.getString(R.string.msgs_profile_sync_task_dlg_wifi_ap_not_connected);
                    dlg_msg.setText(mtxt);
                }
            }
        });

        addBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dlg_msg.setText("");
                String newfilter = et_filter.getText().toString().trim();
                et_filter.setText("");
                filterAdapter.add(new AdapterFilterList.FilterListItem(newfilter, true));
                filterAdapter.setNotifyOnChange(true);
                filterAdapter.sort();
                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
            }
        });

        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new OnClickListener() {
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
        btn_ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                addr_list.clear();
                if (filterAdapter.getCount() > 0) {
                    for (int i = 0; i < filterAdapter.getCount(); i++) {
                        String inc = SMBSYNC2_PROF_FILTER_EXCLUDE;
                        if (filterAdapter.getItem(i).isInclude())
                            inc = SMBSYNC2_PROF_FILTER_INCLUDE;
                        addr_list.add(inc + filterAdapter.getItem(i).getFilter());
                    }
                }
                p_ntfy.notifyToListener(true, null);
            }
        });
//		dialog.setOnKeyListener(new DialogOnKeyListener(context));
//		dialog.setCancelable(false);
        dialog.show();

    }

    public void editFileFilterDlg(final SyncTaskItem sti, final NotifyEvent p_ntfy, boolean use_dir_filter_v2, boolean use_ensure_target_exact_mirror) {
        ArrayList<AdapterFilterList.FilterListItem> filterList = new ArrayList<AdapterFilterList.FilterListItem>();
        final AdapterFilterList filterAdapter;

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.filter_list_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_title_view);
        final LinearLayout ll_file_filter_v2_guide = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_file_v2_guide_ll);
        final TextView title = (TextView) dialog.findViewById(R.id.filter_select_edit_title);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        title.setTextColor(mGp.themeColorList.title_text_color);

        final LinearLayout ll_dir_filter_mirror_warning = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_mirror_method_warning_view);
        final TextView tv_dir_filter_mirror_warning = (TextView) dialog.findViewById(R.id. filter_select_edit_mirror_method_warning_message);
        tv_dir_filter_mirror_warning.setTextColor(mGp.themeColorList.text_color_warning);

        Button dirbtn = (Button) dialog.findViewById(R.id.filter_select_edit_list_dir_btn);
        dirbtn.setVisibility(Button.GONE);

        filterAdapter = new AdapterFilterList(mActivity, R.layout.filter_list_item_view, filterList, SMBSYNC2_PROF_FILTER_FILE);
        ListView lv = (ListView) dialog.findViewById(R.id.filter_select_edit_listview);

        if (use_dir_filter_v2) ll_file_filter_v2_guide.setVisibility(LinearLayout.VISIBLE);
        else ll_file_filter_v2_guide.setVisibility(LinearLayout.GONE);

        for (int i = 0; i < sti.getFileFilter().size(); i++) {
            String inc = sti.getFileFilter().get(i).substring(0, 1);
            String filter = sti.getFileFilter().get(i).substring(1, sti.getFileFilter().get(i).length());
            AdapterFilterList.FilterListItem fli=new AdapterFilterList.FilterListItem(filter, inc.equals(SMBSYNC2_PROF_FILTER_INCLUDE));
            fli.setUseFilterV2(use_dir_filter_v2);
            filterAdapter.add(fli);
        }
        lv.setAdapter(filterAdapter);

        if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR) && use_ensure_target_exact_mirror && filterAdapter.getCount()>0) ll_dir_filter_mirror_warning.setVisibility(LinearLayout.VISIBLE);
        else ll_dir_filter_mirror_warning.setVisibility(LinearLayout.GONE);

        title.setText(mContext.getString(R.string.msgs_filter_list_dlg_file_filter));
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.filter_select_edit_msg);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        final EditText et_filter = (EditText) dialog.findViewById(R.id.filter_select_edit_new_filter);
        final Button addBtn = (Button) dialog.findViewById(R.id.filter_select_edit_add_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.filter_select_edit_cancel_btn);
        final Button btn_ok = (Button) dialog.findViewById(R.id.filter_select_edit_ok_btn);
        CommonDialog.setViewEnabled(mActivity, btn_ok, false);

        //on main filters dialog, show warning if invalid filters exist + disable ok button
        //no check for whole dir prefix in file filters: they are always invalid chars not allowed in file filter
        if (!hasInvalidCharsAndWildcardsFilterList(filterAdapter, btn_ok, dlg_msg, SMBSYNC2_PROF_FILTER_FILE) &&
                isValidWildcardsFileFilterWithPath(filterAdapter, btn_ok, dlg_msg) &&
                isNoDuplicateFilters(filterAdapter, btn_ok, dlg_msg)) {
            //ok
        }

        NotifyEvent ntfy_inc_exc = new NotifyEvent(mContext);
        ntfy_inc_exc.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                if (!hasInvalidCharsAndWildcardsFilterList(filterAdapter, btn_ok, dlg_msg, SMBSYNC2_PROF_FILTER_FILE) &&
                        isValidWildcardsFileFilterWithPath(filterAdapter, btn_ok, dlg_msg) && isNoDuplicateFilters(filterAdapter, btn_ok, dlg_msg)) {
                    CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    dlg_msg.setText("");
                }
            }
            @Override
            public void negativeResponse(Context c, Object[] o) {}
        });
        filterAdapter.setNotifyIncExcListener(ntfy_inc_exc);

        NotifyEvent ntfy_delete = new NotifyEvent(mContext);
        ntfy_delete.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                if (!hasInvalidCharsAndWildcardsFilterList(filterAdapter, btn_ok, dlg_msg, SMBSYNC2_PROF_FILTER_FILE) &&
                        isValidWildcardsFileFilterWithPath(filterAdapter, btn_ok, dlg_msg) && isNoDuplicateFilters(filterAdapter, btn_ok, dlg_msg)) {
                    CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    dlg_msg.setText("");
                }
                if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR) && use_ensure_target_exact_mirror && filterAdapter.getCount()>0) ll_dir_filter_mirror_warning.setVisibility(LinearLayout.VISIBLE);
                else ll_dir_filter_mirror_warning.setVisibility(LinearLayout.GONE);
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
                        if (!hasInvalidCharsAndWildcardsFilterList(filterAdapter, btn_ok, dlg_msg, SMBSYNC2_PROF_FILTER_FILE) &&
                                isValidWildcardsFileFilterWithPath(filterAdapter, btn_ok, dlg_msg) && isNoDuplicateFilters(filterAdapter, btn_ok, dlg_msg)) {
                            CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                            dlg_msg.setText("");
                        }
                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {}

                });
                editFilter(idx, filterAdapter, fli, fli.getFilter(), "", ntfy, sti, SMBSYNC2_PROF_FILTER_FILE);
            }
        });

        //main file filters dialog: On typing filter text enable/disable Add and bottom include/exclude buttons
        CommonDialog.setViewEnabled(mActivity, addBtn, false);
        et_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR) && use_ensure_target_exact_mirror && filterAdapter.getCount()>0) ll_dir_filter_mirror_warning.setVisibility(LinearLayout.VISIBLE);
                else ll_dir_filter_mirror_warning.setVisibility(LinearLayout.GONE);

                if (s.length() != 0) {
                    String entered_filter= s.toString();
                    String error_filter= "";

                    //check filter for duplicates
                    error_filter= getDuplicateFilter(entered_filter.trim(), filterAdapter);
                    if (!error_filter.equals("")) {
                        String mtxt = mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified);
                        dlg_msg.setText(String.format(mtxt, entered_filter.trim()));
                        CommonDialog.setViewEnabled(mActivity, addBtn, false);
                        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                        return;
                    }

                    dlg_msg.setText("");
                    CommonDialog.setViewEnabled(mActivity, addBtn, true);
                    CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                } else {
                    CommonDialog.setViewEnabled(mActivity, addBtn, false);

                    //recheck existing filters before enabling Ok button and clearing warning dialog msg
                    if (!hasInvalidCharsAndWildcardsFilterList(filterAdapter, btn_ok, dlg_msg, SMBSYNC2_PROF_FILTER_FILE) &&
                            isValidWildcardsFileFilterWithPath(filterAdapter, btn_ok, dlg_msg) && isNoDuplicateFilters(filterAdapter, btn_ok, dlg_msg)) {
                        dlg_msg.setText("");
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    } else {
                        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    }
                }
//				et_filter.setText(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        //On Add button click, check entered filters validity before adding them
        //only perform checks thar are not performed in addTextChangedListener()
        addBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String entered_filter=et_filter.getText().toString().trim();

                //check filter for invalid chars before add
                String invalid_char= checkFilterInvalidCharacter(entered_filter, SMBSYNC2_PROF_FILTER_FILE);
                if (!invalid_char.equals("")) {
                    String mtxt=mContext.getString(R.string.msgs_profile_sync_task_filter_list_dlg_file_name_contains_invalid_character);
                    dlg_msg.setText(String.format(mtxt, invalid_char));
                    CommonDialog.setViewEnabled(mActivity, addBtn, false);
                    return;
                }

                //check filter for wildcard only paths (* and *.* only path)
                String wild_card_only_path_parts=checkFilterInvalidAsteriskOnlyPath(entered_filter);
                if (!wild_card_only_path_parts.equals("")) {
                    String mtxt=mContext.getString(R.string.msgs_profile_sync_task_filter_list_dlg_file_name_contains_invalid_asterisk_only_parts);
                    dlg_msg.setText(String.format(mtxt, wild_card_only_path_parts));
                    CommonDialog.setViewEnabled(mActivity, addBtn, false);
                    return;
                }

                //file filters support use of * char only in the file name, but not in the path to file
                String error_filter=checkFileFilterHasAsteriskInPathToFile(entered_filter);
                if (!error_filter.equals("")) {
                    String mtxt=mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_file_filter_path_has_invalid_asterisk_edit_dlg_error);
                    dlg_msg.setText(String.format(mtxt, error_filter));
                    CommonDialog.setViewEnabled(mActivity, addBtn, false);
                    return;
                }

                //display a warning if file filter has hidden files while the sync hidden files option is not enabled
                checkFilterHiddenOptionWarning(sti, SMBSYNC2_PROF_FILTER_FILE, entered_filter);

                //add the new valid filter
                String new_filter = sortFilterSplitedItem(entered_filter);
                AdapterFilterList.FilterListItem fli=new AdapterFilterList.FilterListItem(new_filter, true);
                if (use_dir_filter_v2) fli.setUseFilterV2(true);
                filterAdapter.add(fli);

                et_filter.setText("");
                filterAdapter.setNotifyOnChange(true);
                filterAdapter.sort();
            }
        });

        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new OnClickListener() {
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
        btn_ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                sti.getFileFilter().clear();
                if (filterAdapter.getCount() > 0) {
                    for (int i = 0; i < filterAdapter.getCount(); i++) {
                        String inc = SMBSYNC2_PROF_FILTER_EXCLUDE;
                        if (filterAdapter.getItem(i).isInclude())
                            inc = SMBSYNC2_PROF_FILTER_INCLUDE;
                        sti.getFileFilter().add(inc + filterAdapter.getItem(i).getFilter());
                    }
                }
                p_ntfy.notifyToListener(true, new Object[]{sti.getFileFilter()});
            }
        });
        dialog.show();
    }

    public void editDirFilterDlg(final SyncTaskItem sti, final NotifyEvent p_ntfy, boolean use_dir_filter_v2, boolean use_ensure_target_exact_mirror) {
        ArrayList<AdapterFilterList.FilterListItem> filterList = new ArrayList<AdapterFilterList.FilterListItem>();
        final AdapterFilterList filterAdapter;

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.filter_list_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_title_view);
        final LinearLayout ll_dir_filter_v2_guide = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_dir_v2_guide_ll);

        final TextView title = (TextView) dialog.findViewById(R.id.filter_select_edit_title);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        title.setTextColor(mGp.themeColorList.title_text_color);

        final LinearLayout ll_dir_filter_mirror_warning = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_mirror_method_warning_view);
        final TextView tv_dir_filter_mirror_warning = (TextView) dialog.findViewById(R.id. filter_select_edit_mirror_method_warning_message);
        tv_dir_filter_mirror_warning.setTextColor(mGp.themeColorList.text_color_warning);

        filterAdapter = new AdapterFilterList(mActivity, R.layout.filter_list_item_view, filterList, SMBSYNC2_PROF_FILTER_DIR);
        final ListView lv = (ListView) dialog.findViewById(R.id.filter_select_edit_listview);
        final EditText et_filter = (EditText) dialog.findViewById(R.id.filter_select_edit_new_filter);
        final Button addbtn = (Button) dialog.findViewById(R.id.filter_select_edit_add_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.filter_select_edit_cancel_btn);
        final Button btn_ok = (Button) dialog.findViewById(R.id.filter_select_edit_ok_btn);
        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
        title.setText(mContext.getString(R.string.msgs_filter_list_dlg_dir_filter));
        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.filter_select_edit_msg);
        final Button dirbtn = (Button) dialog.findViewById(R.id.filter_select_edit_list_dir_btn);

        if (use_dir_filter_v2) ll_dir_filter_v2_guide.setVisibility(LinearLayout.VISIBLE);
        else ll_dir_filter_v2_guide.setVisibility(LinearLayout.GONE);

        final LinearLayout add_include_select_view = (LinearLayout) dialog.findViewById(R.id.filter_select_edit_add_include_exclude_view);
        add_include_select_view.setVisibility(LinearLayout.VISIBLE);
        final RadioButton add_include_btn = (RadioButton) dialog.findViewById(R.id.filter_select_edit_add_include_exclude_radio_button_include);
        final RadioButton add_exclude_btn = (RadioButton) dialog.findViewById(R.id.filter_select_edit_add_include_exclude_radio_button_exclude);
        add_include_btn.setChecked(true);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

        for (int i = 0; i < sti.getDirFilter().size(); i++) {
            String inc = sti.getDirFilter().get(i).substring(0, 1);
            String filter = sti.getDirFilter().get(i).substring(1, sti.getDirFilter().get(i).length());
            AdapterFilterList.FilterListItem fli=new AdapterFilterList.FilterListItem(filter, inc.equals(SMBSYNC2_PROF_FILTER_INCLUDE));
            fli.setUseFilterV2(use_dir_filter_v2);
            filterAdapter.add(fli);
        }
        lv.setAdapter(filterAdapter);
        lv.setScrollingCacheEnabled(false);
        lv.setScrollbarFadingEnabled(false);

        if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR) && use_ensure_target_exact_mirror && filterAdapter.getCount()>0) ll_dir_filter_mirror_warning.setVisibility(LinearLayout.VISIBLE);
        else ll_dir_filter_mirror_warning.setVisibility(LinearLayout.GONE);

        //when entering main dir filters dialog, check existing filters for errors and display warning dialog and enable/disable ok buton in main filter list view
        if (!hasInvalidCharsAndWildcardsFilterList(filterAdapter, btn_ok, dlg_msg, SMBSYNC2_PROF_FILTER_DIR) &&
                isNoDuplicateFilters(filterAdapter, btn_ok, dlg_msg) &&
                isValidWholeDirectoryFilterV1(filterAdapter, btn_ok, dlg_msg) &&
                isValidWholeDirectoryFilterV2(filterAdapter, btn_ok, dlg_msg)) {
            //ok
        }

        NotifyEvent ntfy_inc_exc = new NotifyEvent(mContext);
        ntfy_inc_exc.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                if (!hasInvalidCharsAndWildcardsFilterList(filterAdapter, btn_ok, dlg_msg, SMBSYNC2_PROF_FILTER_DIR) && isNoDuplicateFilters(filterAdapter, btn_ok, dlg_msg) &&
                        isValidWholeDirectoryFilterV1(filterAdapter, btn_ok, dlg_msg) && isValidWholeDirectoryFilterV2(filterAdapter, btn_ok, dlg_msg)) {
                    CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    dlg_msg.setText("");
                }
            }
            @Override
            public void negativeResponse(Context c, Object[] o) { }
        });
        filterAdapter.setNotifyIncExcListener(ntfy_inc_exc);

        NotifyEvent ntfy_delete = new NotifyEvent(mContext);
        ntfy_delete.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                if (!hasInvalidCharsAndWildcardsFilterList(filterAdapter, btn_ok, dlg_msg, SMBSYNC2_PROF_FILTER_DIR) && isNoDuplicateFilters(filterAdapter, btn_ok, dlg_msg) &&
                        isValidWholeDirectoryFilterV1(filterAdapter, btn_ok, dlg_msg) && isValidWholeDirectoryFilterV2(filterAdapter, btn_ok, dlg_msg)) {
                    CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    dlg_msg.setText("");
                }
                if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR) && use_ensure_target_exact_mirror && filterAdapter.getCount()>0) ll_dir_filter_mirror_warning.setVisibility(LinearLayout.VISIBLE);
                else ll_dir_filter_mirror_warning.setVisibility(LinearLayout.GONE);
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
                        if (!hasInvalidCharsAndWildcardsFilterList(filterAdapter, btn_ok, dlg_msg, SMBSYNC2_PROF_FILTER_DIR) && isNoDuplicateFilters(filterAdapter, btn_ok, dlg_msg) &&
                                isValidWholeDirectoryFilterV1(filterAdapter, btn_ok, dlg_msg) && isValidWholeDirectoryFilterV2(filterAdapter, btn_ok, dlg_msg)) {
                            CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                            dlg_msg.setText("");
                        }
                    }
                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                editFilter(idx, filterAdapter, fli, fli.getFilter(), "", ntfy, sti, SMBSYNC2_PROF_FILTER_DIR);
            }
        });

        //main dir filters dialog: add dir filters button, enable/disable bottom include/exclude buttons
        et_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR) && use_ensure_target_exact_mirror && filterAdapter.getCount()>0) ll_dir_filter_mirror_warning.setVisibility(LinearLayout.VISIBLE);
                else ll_dir_filter_mirror_warning.setVisibility(LinearLayout.GONE);

                if (s.length() != 0) {
                    CommonDialog.setViewEnabled(mActivity, addbtn, false);
                    CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    CommonDialog.setViewEnabled(mActivity, dirbtn, false);
                    CommonDialog.setViewEnabled(mActivity, add_include_btn, false);
                    CommonDialog.setViewEnabled(mActivity, add_exclude_btn, false);
                    
                    String entered_filter=s.toString();
                    String error_filter="";

                    //check for filter duplicates
                    error_filter= getDuplicateFilter(entered_filter.trim(), filterAdapter);
                    if (!error_filter.equals("")) {
                        String mtxt = mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified);
                        dlg_msg.setText(String.format(mtxt, error_filter));
                        return;
                    }

                    //if filter starts with whole dir prefix v1, do not allow for filters v2
                    if (use_dir_filter_v2) {
                        error_filter=hasWholeDirectoryFilterItemV1(entered_filter);
                        if (!error_filter.equals("")) {
                            String suggest_filter = error_filter.replace(WHOLE_DIRECTORY_FILTER_PREFIX_V1, WHOLE_DIRECTORY_FILTER_PREFIX_V2);
                            String mtxt = mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_old_whole_dir_prefix_edit_dlg_error);
                            dlg_msg.setText(String.format(mtxt, error_filter, WHOLE_DIRECTORY_FILTER_PREFIX_V1, WHOLE_DIRECTORY_FILTER_PREFIX_V2, suggest_filter));
                            return;
                        }
                    }

                    //if filter has whole dir prefix v2, add it as exclude filter, include is not allowed
                    if (use_dir_filter_v2) {
                        String has_whole_dir_item_v2=hasWholeDirectoryFilterItemV2(entered_filter);
                        if (!has_whole_dir_item_v2.equals("")) {
                            CommonDialog.setViewEnabled(mActivity, add_exclude_btn, true);
                            add_exclude_btn.setChecked(true);
                            CommonDialog.setViewEnabled(mActivity, add_include_btn, false);
                        } else {
                            CommonDialog.setViewEnabled(mActivity, add_include_btn, true);
                            CommonDialog.setViewEnabled(mActivity, add_exclude_btn, true);
                        }
                    } else {
                        CommonDialog.setViewEnabled(mActivity, add_include_btn, true);
                        CommonDialog.setViewEnabled(mActivity, add_exclude_btn, true);
                    }

                    dlg_msg.setText("");
                    CommonDialog.setViewEnabled(mActivity, addbtn, true);
                    CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    CommonDialog.setViewEnabled(mActivity, dirbtn, false);
                } else {
                    CommonDialog.setViewEnabled(mActivity, addbtn, false);
                    CommonDialog.setViewEnabled(mActivity, dirbtn, true);
                    CommonDialog.setViewEnabled(mActivity, add_include_btn, true);
                    CommonDialog.setViewEnabled(mActivity, add_exclude_btn, true);

                    //recheck existing filters before enabling Ok Button and clearing warning dialog msg
                    if (!hasInvalidCharsAndWildcardsFilterList(filterAdapter, btn_ok, dlg_msg, SMBSYNC2_PROF_FILTER_DIR) && isNoDuplicateFilters(filterAdapter, btn_ok, dlg_msg) &&
                            isValidWholeDirectoryFilterV1(filterAdapter, btn_ok, dlg_msg) && isValidWholeDirectoryFilterV2(filterAdapter, btn_ok, dlg_msg)) {
                        dlg_msg.setText("");
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    } else {
                        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    }
                }
//				et_filter.setText(s);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        //On Add button click, check entered filters validity before adding them
        //only perform checks thar are not performed in addTextChangedListener()
        CommonDialog.setViewEnabled(mActivity, addbtn, false);
        addbtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String entered_filter=et_filter.getText().toString().trim();

                //check filter for invalid chars before add
                String invalid_char= checkFilterInvalidCharacter(entered_filter, SMBSYNC2_PROF_FILTER_DIR);
                if (!invalid_char.equals("")) {
                    String mtxt=mContext.getString(R.string.msgs_profile_sync_task_filter_list_dlg_file_name_contains_invalid_character);
                    dlg_msg.setText(String.format(mtxt, invalid_char));
                    CommonDialog.setViewEnabled(mActivity, addbtn, false);
                    CommonDialog.setViewEnabled(mActivity, add_include_btn, false);
                    CommonDialog.setViewEnabled(mActivity, add_exclude_btn, false);
                    return;
                }

                //check filter for wildcard only paths (* and *.* only path)
                String wild_card_only_path_parts=checkFilterInvalidAsteriskOnlyPath(entered_filter);
                if (!wild_card_only_path_parts.equals("")) {
                    String mtxt=mContext.getString(R.string.msgs_profile_sync_task_filter_list_dlg_file_name_contains_invalid_asterisk_only_parts);
                    dlg_msg.setText(String.format(mtxt, wild_card_only_path_parts));
                    CommonDialog.setViewEnabled(mActivity, addbtn, false);
                    CommonDialog.setViewEnabled(mActivity, add_include_btn, false);
                    CommonDialog.setViewEnabled(mActivity, add_exclude_btn, false);
                    return;
                }

                //display a warning if file filter has hidden files while the sync hidden files option is not enabled
                checkFilterHiddenOptionWarning(sti, SMBSYNC2_PROF_FILTER_DIR, entered_filter);

                //add the new valid filter
                String new_filter = sortFilterSplitedItem(entered_filter);
                AdapterFilterList.FilterListItem fli=new AdapterFilterList.FilterListItem(new_filter, add_include_btn.isChecked());
                if (use_dir_filter_v2) fli.setUseFilterV2(true);
                filterAdapter.add(fli);

                et_filter.setText("");
                filterAdapter.setNotifyOnChange(true);
                filterAdapter.sort();
            }
        });

        // Directoryボタンの指定
        dirbtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context arg0, Object[] arg1) {
                        //recheck existing filters before enabling Ok Button and clearing warning dialog msg
                        if (!hasInvalidCharsAndWildcardsFilterList(filterAdapter, btn_ok, dlg_msg, SMBSYNC2_PROF_FILTER_DIR) && isNoDuplicateFilters(filterAdapter, btn_ok, dlg_msg) &&
                                isValidWholeDirectoryFilterV1(filterAdapter, btn_ok, dlg_msg) && isValidWholeDirectoryFilterV2(filterAdapter, btn_ok, dlg_msg)) {
                            dlg_msg.setText("");
                            CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                        } else {
                            CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                        }
                        if (sti.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR) && use_ensure_target_exact_mirror && filterAdapter.getCount()>0) ll_dir_filter_mirror_warning.setVisibility(LinearLayout.VISIBLE);
                        else ll_dir_filter_mirror_warning.setVisibility(LinearLayout.GONE);
                    }
                    @Override
                    public void negativeResponse(Context arg0, Object[] arg1) {
                        if (arg1 != null) {
                            String msg_text = (String) arg1[0];
                            mUtil.showCommonDialog(false, "E", mContext.getString(R.string.msgs_remote_profile_dlg_logon_error), msg_text, null);
                        }
                    }
                });
                listDirectoryFilter(sti, filterAdapter, ntfy);
            }
        });

        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new OnClickListener() {
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
        btn_ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
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
                p_ntfy.notifyToListener(true, new Object[]{sti.getDirFilter()});
            }
        });
        dialog.show();
    }

    //edit existing filters dialog
    private void editFilter(final int edit_idx, final AdapterFilterList fa,
                            final AdapterFilterList.FilterListItem fli, final String filter, String title_text, final NotifyEvent p_ntfy,
                            final SyncTaskItem sti, final String filter_type) {

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.filter_edit_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.filter_edit_dlg_view);
        CommonUtilities.setDialogBoxOutline(mContext, ll_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.filter_edit_dlg_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.filter_edit_dlg_title);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        title.setTextColor(mGp.themeColorList.title_text_color);
        if (!title_text.equals("")) title.setText(title_text);
        TextView dlg_msg = (TextView) dialog.findViewById(R.id.filter_edit_dlg_msg);

        final Button btn_cancel = (Button) dialog.findViewById(R.id.filter_edit_dlg_cancel_btn);
        final Button btn_ok = (Button) dialog.findViewById(R.id.filter_edit_dlg_ok_btn);

        CommonDialog.setDlgBoxSizeCompactWithInput(dialog);
        final EditText et_filter = (EditText) dialog.findViewById(R.id.filter_edit_dlg_filter);
        et_filter.setText(filter);

        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
        dlg_msg.setText(mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified, filter));//text is same as existing on entering edit dialog
        et_filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            //Check edited filter validity, display dlg warning and enable/disable ok button 
            @Override
            public void afterTextChanged(Editable s) {
                dlg_msg.setText("");
                if (s.length() == 0) {
                    dlg_msg.setText(mContext.getString(R.string.msgs_filter_list_dlg_not_specified));
                    CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    return;
                } else {
                    //check if edited filter has a duplicate in its self and in existing filters
                    if (!filter.equalsIgnoreCase(s.toString())) {

                        //check if edited filter has duplicates inside its self
                        String[] new_filter_array=s.toString().split(";");
                        for(int i= 0; i < new_filter_array.length; i++) {
                            for(int j= i+1; j < new_filter_array.length; j++) {
                                if (!new_filter_array[i].equals("") && new_filter_array[i].equalsIgnoreCase(new_filter_array[j])) {
                                    dlg_msg.setText(mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified, new_filter_array[i]));
                                    CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                                    return;
                                }
                            }
                        }

                        //check if edited filter has duplicates in existing filters
                        String[]changed_filter_array=getChangedFilter(filter, s.toString()).split(";");
                        for(String changed_item:changed_filter_array) {
                            String dup_filter= getDuplicateFilter(changed_item, fa);
                            if (!dup_filter.equals("")) {
                                dlg_msg.setText(mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified, dup_filter));
                                CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                                return;
                            } else {
                                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                            }
                        }
                    } else {//filter is same as the one being edited
                        dlg_msg.setText(mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified, filter));
                        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    }
                }
            }
        });

        // CANCELボタンの指定
        btn_cancel.setOnClickListener(new OnClickListener() {
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
        //On OK press: check edited filter for errors that are not checked by afterTextChanged()
        btn_ok.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String new_filter=et_filter.getText().toString().trim();

                if (filter_type != null && (filter_type.equals(SMBSYNC2_PROF_FILTER_DIR) || filter_type.equals(SMBSYNC2_PROF_FILTER_FILE))) {
                    //check filter for invalid chars before add
                    String has_invalid_char= checkFilterInvalidCharacter(new_filter, filter_type);
                    if (!has_invalid_char.equals("")) {
                        String mtxt=mContext.getString(R.string.msgs_profile_sync_task_filter_list_dlg_file_name_contains_invalid_character);
                        dlg_msg.setText(String.format(mtxt, has_invalid_char));
                        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                        return;
                    }

                    //check filter for wildcard only paths (* and *.* only path)
                    String wild_card_only_path_parts=checkFilterInvalidAsteriskOnlyPath(new_filter);
                    if (!wild_card_only_path_parts.equals("")) {
                        String mtxt=mContext.getString(R.string.msgs_profile_sync_task_filter_list_dlg_file_name_contains_invalid_asterisk_only_parts);
                        dlg_msg.setText(String.format(mtxt, wild_card_only_path_parts));
                        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                        return;
                    }

                    //file filters support use of * char only in the file name, but not in the path to file:
                    if (filter_type.equals(SMBSYNC2_PROF_FILTER_FILE)) {
                        String error_filter=checkFileFilterHasAsteriskInPathToFile(new_filter);
                        if (!error_filter.equals("")) {
                            String mtxt=mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_file_filter_path_has_invalid_asterisk_edit_dlg_error);
                            dlg_msg.setText(String.format(mtxt, error_filter));
                            CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                            return;
                        }
                    }

                    //check if added directory filter is v2 but has invalid whole dir prefix v1
                    //if filter is file, whole dir prefix v1 and v2 are invalid chars detected above
                    if (filter_type.equals(SMBSYNC2_PROF_FILTER_DIR) && fli.isUseFilterV2()) {
                        String error_filter=hasWholeDirectoryFilterItemV1(new_filter);
                        if (!error_filter.equals("")) {
                            String suggest_filter = error_filter.replace(WHOLE_DIRECTORY_FILTER_PREFIX_V1, WHOLE_DIRECTORY_FILTER_PREFIX_V2);
                            String mtxt = mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_old_whole_dir_prefix_edit_dlg_error);
                            dlg_msg.setText(String.format(mtxt, error_filter, WHOLE_DIRECTORY_FILTER_PREFIX_V1, WHOLE_DIRECTORY_FILTER_PREFIX_V2, suggest_filter));
                            CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                            return;
                        }
                    }

                    //display a warning if filter has hidden files/dirs while the sync hidden files/dirs option is not enabled
                    checkFilterHiddenOptionWarning(sti, filter_type, new_filter);
                }

                //set the new edited filter value
                dialog.dismiss();
                String new_filter_value=sortFilterSplitedItem(new_filter);
                fli.setFilter(new_filter_value);
                fa.sort();
                fa.setNotifyOnChange(true);
                if (p_ntfy != null) p_ntfy.notifyToListener(true, null);
            }
        });
        dialog.show();
    }

    //on add filter, edit it: sort + remove leading and trail ;
    private String sortFilterSplitedItem(String filter) {
        String[]new_filter_array=filter.split(";");
        String new_filter_value="";
        String separator="";
        ArrayList<String>new_filter_list=new ArrayList<String>();
        for(String item:new_filter_array) {
            if (!item.equals("")) new_filter_list.add(item);
        }

        Collections.sort(new_filter_list);
        for(String item:new_filter_list) {
            new_filter_value+=separator+item;
            separator=";";
        }

        return new_filter_value;
    }

    public static String checkFilterInvalidCharacter(String filter, String filter_type) {
        String invalid_char_seq="";
        if (filter==null || filter_type==null) return invalid_char_seq;

        String[] invalid_char_list;
        if (filter_type.equals(SMBSYNC2_PROF_FILTER_FILE)) invalid_char_list=SMBSYNC2_PROF_FILTER_FILE_INVALID_CHARS;
        else if (filter_type.equals(SMBSYNC2_PROF_FILTER_DIR)) invalid_char_list=SMBSYNC2_PROF_FILTER_DIR_INVALID_CHARS;
        else return invalid_char_seq;

        //do not allow filters with only ";", "." or "?" chars
        String tmp_filter=filter.replaceAll(Pattern.quote("/"), "");
        if (tmp_filter.equals("")) return "/";
        tmp_filter=tmp_filter.replaceAll(Pattern.quote(";"), "");
        if (tmp_filter.equals("")) return ";";
        tmp_filter=tmp_filter.replaceAll(Pattern.quote("."), "");
        if (tmp_filter.equals("")) return ".";
        tmp_filter=tmp_filter.replaceAll(Pattern.quote("?"), "");
        if (tmp_filter.equals("")) return "?";

        //do not allow redundant ";;" chars
        if (filter.contains(";;")) return ";;";

        String[] filter_item_array = filter.split(";");
        for(String filter_item:filter_item_array) {
            for(String item:invalid_char_list) {
                if (filter_item.contains(item)) {
                    invalid_char_seq = item;
                    break;
                }
            }

            //whole directory prefix is always an invalid filename char. If it is present elsewhere than in the beginning, consider it an invalid filter char
            //if it is at the beginning, further checks to see if it is a valid whole dir prefix are needed by caller
            //once v1 is dropped, if whole dir prefix is a double invalid_char sequence: this implementation is not valid because the single invalid char occurence cannot be asserted and further checks are needed
            if (invalid_char_seq.equals("")) {
                if (filter_item.lastIndexOf(WHOLE_DIRECTORY_FILTER_PREFIX_V1) > 0) invalid_char_seq = WHOLE_DIRECTORY_FILTER_PREFIX_V1;
                else if (filter_item.startsWith(WHOLE_DIRECTORY_FILTER_PREFIX_V1)) invalid_char_seq = "";//this line is a temp compatibility workaround: remove once v1 filter is dropped
                else if (filter_item.lastIndexOf(WHOLE_DIRECTORY_FILTER_PREFIX_V2) > 0) invalid_char_seq = WHOLE_DIRECTORY_FILTER_PREFIX_V2;
            }

            if (!invalid_char_seq.equals("")) break;
        }

        return invalid_char_seq;
    }

    //check ";" separated filter items for * and *.* only paths
    public static String checkFilterInvalidAsteriskOnlyPath(String filter) {
        String invalid_char_seq="";
        String[] filter_item_array=filter.split(";");
        for(String filter_item:filter_item_array) {
            invalid_char_seq=CommonUtilities.hasAsteriskOnlyPath(filter_item);
            if (!invalid_char_seq.equals("")) break;
        }

        return invalid_char_seq;
    }

    //file filter path cannot have asterisk outside filename (relative full path only)
    public static String checkFileFilterHasAsteriskInPathToFile(String filter) {
        String error_filter = "";
        String[] filter_item_array=filter.split(";");
        for(String filter_item:filter_item_array) {
            if (!filter_item.contains("/")) continue;//filename only filter

            //file filter item is a path to file
            if (CommonUtilities.basedirOf(filter_item).contains("*")) {
                error_filter=filter_item;
                break;
            }
        }

        return error_filter;
    }

    //file filter path cannot have asterisk outside filename
    private boolean isValidWildcardsFileFilterWithPath(AdapterFilterList filter_adapter, Button ok_btn, TextView dlg_msg) {
        boolean result=true;
        String error_filters="";
        for(int i=0; i<filter_adapter.getCount(); i++) {
            AdapterFilterList.FilterListItem fli=filter_adapter.getItem(i);
            if (!fli.isDeleted() && fli.isUseFilterV2()) {
                error_filters=checkFileFilterHasAsteriskInPathToFile(fli.getFilter());
                if (!error_filters.equals("")) break;
            }
        }
        if (!error_filters.equals("")) {
            dlg_msg.setText(mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_file_filter_path_has_invalid_asterisk_edit_dlg_error, error_filters));
            CommonDialog.setViewEnabled(mActivity, ok_btn, false);
            result=false;
        }
        return result;
    }

    //check if adapter filter list has invalid chars or generic asterisk only path (* and *.* only filter)
    private boolean hasInvalidCharsAndWildcardsFilterList(AdapterFilterList filter_adapter, Button ok_btn, TextView dlg_msg, String filter_type) {
        boolean has_invalid_chars=false;
        if (filter_type==null) return has_invalid_chars;

        String error_msg="";
        String error_filter="";
        String invalid_char_seq="";
        for(int i=0; i<filter_adapter.getCount(); i++) {
            AdapterFilterList.FilterListItem fli=filter_adapter.getItem(i);
            if (!fli.isDeleted() && fli.isUseFilterV2()) {
                error_filter=fli.getFilter();
                invalid_char_seq=checkFilterInvalidCharacter(error_filter, filter_type);
                if (!invalid_char_seq.equals("")) {
                    has_invalid_chars=true;
                    error_msg=mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_invalid_characters_edit_dlg_error, error_filter, invalid_char_seq);
                    break;
                }

                //check for invalid asterisk only path in filter (not allowed for all file/dir filters)
                invalid_char_seq=checkFilterInvalidAsteriskOnlyPath(error_filter);
                if (!invalid_char_seq.equals("")) {
                    has_invalid_chars=true;
                    error_msg=mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_invalid_asterisk_characters_edit_dlg_error, error_filter, invalid_char_seq);
                    break;
                }
            }
        }

        if (has_invalid_chars == true) {
            dlg_msg.setText(error_msg);
            CommonDialog.setViewEnabled(mActivity, ok_btn, false);
        }

        return has_invalid_chars;
    }

    //check filter string for leading `\\` old whole dir prefix
    public static String hasWholeDirectoryFilterItemV1(String filter) {
        String whole_dir_filter="";
        String[] filter_item_array=filter.split(";");
        for(String filter_item:filter_item_array) {
            if (filter_item.startsWith(WHOLE_DIRECTORY_FILTER_PREFIX_V1)) {
                whole_dir_filter=filter_item;
                break;
            }
        }
        return whole_dir_filter;
    }

    //check if there are filters with whole dir prefix v1 that are invalid (v2 filters with the old whole dir prefix)
    private boolean isValidWholeDirectoryFilterV1(AdapterFilterList filter_adapter, Button ok_btn, TextView dlg_msg) {
        boolean result=true;
        String error_filter="";
        for(int i=0; i<filter_adapter.getCount(); i++) {
            AdapterFilterList.FilterListItem fli=filter_adapter.getItem(i);
            if (!fli.isDeleted() && fli.isUseFilterV2()) {
                error_filter=hasWholeDirectoryFilterItemV1(fli.getFilter());
                if (!error_filter.equals("")) break;
            }
        }
        if (!error_filter.equals("")) {
            String suggest_filter = error_filter.replace(WHOLE_DIRECTORY_FILTER_PREFIX_V1, WHOLE_DIRECTORY_FILTER_PREFIX_V2);
            dlg_msg.setText(mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_old_whole_dir_prefix_edit_dlg_error,
                    error_filter, WHOLE_DIRECTORY_FILTER_PREFIX_V1, WHOLE_DIRECTORY_FILTER_PREFIX_V2, suggest_filter));
            CommonDialog.setViewEnabled(mActivity, ok_btn, false);
            result=false;
        }
        return result;
    }

    //check if filter string has inc or exc filter items staring with whole dir prefix v2 `\`
    public static String hasWholeDirectoryFilterItemV2(String filter) {
        String anywhere_dir_filter="";
        String[] filter_item_array=filter.split(";");
        for(String filter_item:filter_item_array) {
            if (filter_item.startsWith(WHOLE_DIRECTORY_FILTER_PREFIX_V2)) {
                anywhere_dir_filter=filter_item;
                break;
            }
        }
        return anywhere_dir_filter;
    }

    //Check if there are Include directory filters starting with whole dir prefix v2 `\` (invalid)
    private boolean isValidWholeDirectoryFilterV2(AdapterFilterList filter_adapter, Button ok_btn, TextView dlg_msg) {
        boolean result=true;
        String error_filters="";
        for(int i=0; i<filter_adapter.getCount(); i++) {
            AdapterFilterList.FilterListItem fli=filter_adapter.getItem(i);
            if (!fli.isDeleted() && fli.isUseFilterV2() && fli.isInclude()) {
                error_filters=hasWholeDirectoryFilterItemV2(fli.getFilter());
                if (!error_filters.equals("")) break;
            }
        }
        if (!error_filters.equals("")) {
            dlg_msg.setText(mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_whole_dir_prefix_edit_dlg_error, error_filters, WHOLE_DIRECTORY_FILTER_PREFIX_V2));
            CommonDialog.setViewEnabled(mActivity, ok_btn, false);
            result=false;
        }
        return result;
    }

/*
    //check if FILE filter adapter list has inc or exc filter items starting with whole dir prefix v2 `\`: invalid File Filter
    //not used because whole dir prefix is always an invalid char, not allowed in file filters
    private boolean hasWholeDirectoryFilterV2(AdapterFilterList filter_adapter, Button ok_btn, TextView dlg_msg) {
        boolean result=false;
        String error_filters="";
        for(int i=0;i<filter_adapter.getCount();i++) {
            AdapterFilterList.FilterListItem fli=filter_adapter.getItem(i);
            if (!fli.isDeleted()) {
                error_filters=hasWholeDirectoryFilterItemV2(fli.getFilter());
                if (!error_filters.equals("")) break;
            }
        }
        if (!error_filters.equals("")) {
            dlg_msg.setText(mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_whole_dir_prefix_file_filter_edit_dlg_error, error_filters, WHOLE_DIRECTORY_FILTER_PREFIX_V2));
            CommonDialog.setViewEnabled(mActivity, ok_btn, false);
            result=true;
        }
        return result;
    }
*/

    //Check if filter adapter list has duplicates
    private boolean isNoDuplicateFilters(AdapterFilterList filter_adapter, Button ok_btn, TextView dlg_msg) {
        boolean no_duplicates=true;
        String error_filters="";
        Set<String> unique_filters = new HashSet<String>();

        //filter adapter line to compare with next filter lines
        for(int line = 0; line < filter_adapter.getCount(); line++) {
            AdapterFilterList.FilterListItem fli=filter_adapter.getItem(line);
            if (!fli.isDeleted()) {
                //get each ";" separated filter item from the current filter "line"
                String[] filter_items=fli.getFilter().split(";");
                for (String filter : filter_items) {
                    if (!filter.equals("") && !unique_filters.add(filter.toUpperCase())) error_filters+=filter + ";";
                }
            }
        }

        if (!error_filters.equals("")) {
            no_duplicates = false;
            String[] unique_duplicates = error_filters.split(";");
            if (Build.VERSION.SDK_INT >= 24) unique_duplicates = Arrays.stream(unique_duplicates).distinct().toArray(String[]::new);
            else unique_duplicates = new HashSet<String>(Arrays.asList(unique_duplicates)).toArray(new String[0]);

            if (Build.VERSION.SDK_INT >= 26) error_filters = String.join("; ", unique_duplicates);
            else error_filters = TextUtils.join(";", unique_duplicates);

            dlg_msg.setText(mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_has_duplicate_filters_edit_dlg_error, error_filters));
            CommonDialog.setViewEnabled(mActivity, ok_btn, false);
        }
        return no_duplicates;
    }

    //return duplicate filter entry if it exists in the filter array
    //each array entry can hold multiple ";" separated filter items
    //filter_list: item.substring(1) is the filter entry, item.substring(0,1) is "I" for include and any for exclude
    public static String[] getDuplicateFilterList(ArrayList<String> filter_list) {
        Set<String> unique_filters = new HashSet<String>();
        String duplicate_entries="";

        for(String filter_entry : filter_list) {
            String[] filter_items = filter_entry.substring(1).split(";");
            for (String filter : filter_items) {
                if (!filter.equals("") && !unique_filters.add(filter.toUpperCase())) duplicate_entries+=filter + ";";
            }
        }

        String[] unique_duplicates = duplicate_entries.split(";");
        if (Build.VERSION.SDK_INT >= 24) unique_duplicates = Arrays.stream(unique_duplicates).distinct().toArray(String[]::new);
        else unique_duplicates = new HashSet<String>(Arrays.asList(unique_duplicates)).toArray(new String[0]);

        return unique_duplicates;
    }


    //check if currently entered/edited filter has duplicates inside its self + inside the existing filter entries in the adapter
    private static String getDuplicateFilter(String filter, AdapterFilterList filter_adapter) {
        if (filter_adapter.getCount() == 0) return "";

        Set<String> unique_filters = new HashSet<String>();
        String duplicate_entries="";
        String sep="";

        //add existing filters to new unique_filters array
        for (int i = 0; i < filter_adapter.getCount(); i++) {
            if (!filter_adapter.getItem(i).isDeleted()) {
                String[] filter_entries=filter_adapter.getItem(i).getFilter().split(";");
                for(String filter_item : filter_entries) {
                    if (!filter_item.equals("")) unique_filters.add(filter_item.toUpperCase());
                }
            }
        }

        //check if input filter has duplicates inside its self or in existing filters
        String[] filter_array=filter.split(";");
        for (String filter_item : filter_array) {
            if (!filter_item.equals("") && !unique_filters.add(filter_item.toUpperCase())) {
                duplicate_entries+= sep + filter_item;
                sep= ", ";
            }
        }

        return duplicate_entries;
    }

    //do not compare filter being edited to its own filter adapter entry
    private String getChangedFilter(String current_filter, String new_filter) {
        if (current_filter==null || new_filter==null) return "";
        String[] current_filter_array=current_filter.split(";");
        String[] new_filter_array=new_filter.split(";");
        String changed_filter="";
        String separator="";
        for (String new_item:new_filter_array) {
            boolean found=false;
            for(String current_item:current_filter_array) {
                if (!new_item.equals("") && new_item.equalsIgnoreCase(current_item)) {
                    found=true;
                    break;
                }
            }
            if (!found) {
                changed_filter+=separator+new_item;
                separator=";";
            }
        }
        return changed_filter;
    }

    //when adding exclude/include filter with hidden file/folder, display a Warning dialog if the corresponding Sync Hidden Files/Dirs option is not enabled
    private void checkFilterHiddenOptionWarning(SyncTaskItem sti, String filter_type, String filter) {
        String hidden_filters="";
        String hf_sep="";
        String f_flt_with_hidden_path="";//file filter with hidden dir path like dir1/.dir2/file.txt
        String fflt_sep="";
        String[] filter_item_array=filter.split(";");
        for(String filter_item : filter_item_array) {
            if (filter_type.equals(SMBSYNC2_PROF_FILTER_FILE)) {
                if (CommonUtilities.filenameOf(filter_item).startsWith(".")) {
                    hidden_filters+=hf_sep+filter_item;
                    hf_sep="; ";
                }
                String[] path_array = CommonUtilities.basedirOf(filter_item).split("/");
                for(String path_part : path_array) {
                    if (path_part.startsWith(".")) {
                        f_flt_with_hidden_path+=fflt_sep+filter_item;
                        fflt_sep="; ";
                        break;
                    }
                }
            } else if (filter_type.equals(SMBSYNC2_PROF_FILTER_DIR)) {
                String[] path_array = filter_item.split("/");
                for(String path_part : path_array) {
                    if (path_part.startsWith(".")) {
                        hidden_filters+=hf_sep+filter_item;
                        hf_sep="; ";
                        break;
                    }
                }
            }
        }

        if (filter_type.equals(SMBSYNC2_PROF_FILTER_FILE)) {
            if (!hidden_filters.equals("") && !sti.isSyncOptionSyncHiddenFile())
                mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_show_hidden_file_option_warning, hidden_filters), "", null);
            if (!f_flt_with_hidden_path.equals("") && !sti.isSyncOptionSyncHiddenDirectory())//file filter contains hidden dir path like dir1/.dir2/file.txt
                mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_show_hidden_directory_option_warning, f_flt_with_hidden_path), "", null);
        } else if (filter_type.equals(SMBSYNC2_PROF_FILTER_DIR)) {
            if (!hidden_filters.equals("") && !sti.isSyncOptionSyncHiddenDirectory())
                mUtil.showCommonDialog(false, "W", mContext.getString(R.string.msgs_profile_sync_task_sync_option_use_directory_filter_show_hidden_directory_option_warning, hidden_filters), "", null);
        }
    }

    static public String hasSyncTaskNameUnusableCharacter(Context c, String task_name) {
        for(String item:SYNC_TASK_NAME_UNUSABLE_CHARACTER) {
            if (task_name.contains(item)) return c.getString(R.string.msgs_task_name_contains_invalid_character,item);
        }
        return "";
    }

    static public boolean hasSyncTaskNameUnusableCharacter(String task_name) {
        boolean result=false;
        for(String item:SYNC_TASK_NAME_UNUSABLE_CHARACTER) {
            if (task_name.contains(item)) result=true;
        }
        return result;
    }

    //check if an existing task name is already a duplicate in the syncTaskAdapter (could be caused by bug in previous versions or modification to the settings file)
    static public boolean isSyncTaskNameDuplicate (ArrayList<SyncTaskItem> stl, String t_name) {
        int count = 0;
        for (SyncTaskItem sti : stl) {
            if (t_name.equalsIgnoreCase(sti.getSyncTaskName())) count++;
        }
        return count > 1;
    }

    //check if provided sync task name already exists
    static public boolean isSyncTasknameExists(ArrayList<SyncTaskItem> stl, String t_name) {
        return getSyncTaskByName(stl, t_name) != null;
    }

    static public String isValidSyncTaskName(Context c, ArrayList<SyncTaskItem> stl, String t_name) {
        return isValidSyncTaskName(c, stl, t_name, false, false);
    }

    //showAllError: show all errors with "\n" separator (for display of all name errors in Sync Task List details)
    //checkDup==false: check a new sync task name for validity against existing sync tasks (called when rename/create a sync task)
    //checkDup==true: check if task name is already a duplicate in the syncTaskAdapter (could be caused by bug in previous versions or modification to the settings file)
    //                        called when editing sync task and in the Sync Task Adapter
    //WARNING: when first loading Sync Task settings from file, gp.syncTaskAdapter is not init (ActivityMain and SyncService), so check for duplicates will fail
    static public String isValidSyncTaskName(Context c, ArrayList<SyncTaskItem> stl, String t_name, boolean checkDup, boolean showAllError) {
        String result = "", invalid_chars_msg="", dup_msg="", sep="";
        if (t_name.length() > 0) {
            invalid_chars_msg= hasSyncTaskNameUnusableCharacter(c, t_name);
            if (!checkDup && isSyncTasknameExists(stl, t_name)) {
                dup_msg= c.getString(R.string.msgs_duplicate_task_name);
            } else if (checkDup && isSyncTaskNameDuplicate(stl, t_name)) {
                dup_msg= c.getString(R.string.msgs_duplicate_task_name);
            }
            sep= !invalid_chars_msg.equals("") && !dup_msg.equals("") ? "\n":"";
            if (showAllError) result= invalid_chars_msg + sep + dup_msg;
            else result= invalid_chars_msg.equals("") ? dup_msg:invalid_chars_msg;
        } else {
            result = c.getString(R.string.msgs_specify_task_name);
        }

        return result;
    }

    static public boolean isValidSyncTaskName(ArrayList<SyncTaskItem> stl, String t_name, boolean checkDup) {
        boolean result=true;
        if (t_name.length() > 0) {
            if (hasSyncTaskNameUnusableCharacter(t_name)) {
                result = false;
            } else if (!checkDup && isSyncTasknameExists(stl, t_name)) {
                result = false;
            } else if (checkDup && isSyncTaskNameDuplicate(stl, t_name)) {
                result = false;
            }
        } else {
            result = false;
        }

        return result;
    }

    static public SyncTaskItem getSyncTaskByName(ArrayList<SyncTaskItem> t_prof, String task_name) {
        SyncTaskItem stli = null;

        if (t_prof!=null) {
            for (SyncTaskItem li : t_prof) {
                if (li.getSyncTaskName().equalsIgnoreCase(task_name)) {
                    stli = li;
                    break;
                }
            }
        }
        return stli;
    }

    static public SyncTaskItem getSyncTaskByName(AdapterSyncTask t_prof, String task_name) {
        return getSyncTaskByName(t_prof.getArrayList(), task_name);
    }

    private void listDirectoryFilter(SyncTaskItem sti, AdapterFilterList fla, final NotifyEvent p_ntfy) {
        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
            listLocalDirectoryFilter(sti, fla, p_ntfy);
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
            listLocalDirectoryFilter(sti, fla, p_ntfy);
        } else if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            listSmbDirectoryFilter(sti, fla, p_ntfy);
        }
    }

    private void listLocalDirectoryFilter(final SyncTaskItem sti, final AdapterFilterList fla, final NotifyEvent p_ntfy) {
        final String m_dir = sti.getMasterDirectoryName();
        String localBaseDir_t = sti.getMasterLocalMountPoint();//mGp.internalRootDirectory;
        if (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD))
            localBaseDir_t = mGp.safMgr.getSdcardRootPath();
        final String localBaseDir = localBaseDir_t;

        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                ArrayList<TreeFilelistItem>tfl=(ArrayList<TreeFilelistItem>)objects[0];
                if (tfl.size()==0) {
                    String msg=mContext.getString(R.string.msgs_dir_empty);
                    mUtil.showCommonDialog(false,"W",msg,"",null);
                    return;
                }

                //カスタムダイアログの生成
                final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setContentView(R.layout.item_select_list_dlg);

                LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_view);
//                ll_dlg_view.setBackgroundColor(mGp.themeColorList.title_background_color);

                final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_title_view);
                final TextView title = (TextView) dialog.findViewById(R.id.item_select_list_dlg_title);
                final TextView subtitle = (TextView) dialog.findViewById(R.id.item_select_list_dlg_subtitle);
                title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
                title.setTextColor(mGp.themeColorList.title_text_color);
//                subtitle.setTextColor(mGp.themeColorList.title_text_color);

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
                final TreeFilelistAdapter tfa = new TreeFilelistAdapter(mActivity, false, false);
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
                        final int pos = tfa.getItem(idx);
                        final TreeFilelistItem tfi = tfa.getDataItem(pos);
                        tfi.setChecked(true);
                        tfa.notifyDataSetChanged();
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
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    }
                });

                ib_unselect_all.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < tfa.getDataItemCount(); i++) {
                            tfa.setDataItemIsUnselected(i);
                        }
                        tfa.notifyDataSetChanged();
                        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    }
                });

                //OKボタンの指定
                CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                NotifyEvent ntfy = new NotifyEvent(mContext);
                //Listen setRemoteShare response
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context arg0, Object[] arg1) {
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
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
                        if (checked) CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                        else CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    }
                });
                tfa.setCbCheckListener(ntfy);

                btn_ok.setText(mContext.getString(R.string.msgs_filter_list_dlg_add));
                btn_ok.setVisibility(Button.VISIBLE);
                btn_ok.setOnClickListener(new OnClickListener() {
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
                btn_cancel.setOnClickListener(new OnClickListener() {
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

            @Override
            public void negativeResponse(Context context, Object[] objects) {
                mUtil.showCommonDialog(false, "E", "Local file list creation aborted", "", null);
            }
        });

        createLocalFilelist(true, localBaseDir, "/" + m_dir, ntfy);


    }

    private void listSmbDirectoryFilter(final SyncTaskItem sti,
                                        final AdapterFilterList fla, final NotifyEvent p_ntfy) {
        setSmbUserPass(sti.getMasterSmbUserName(), sti.getMasterSmbPassword());
        String host_addr = sti.getMasterSmbAddr();
        String host_name = sti.getMasterSmbHostName();
        String host_share = sti.getMasterSmbShareName();
        String h_port = "";
        if (!sti.getMasterSmbPort().equals("")) h_port = sti.getMasterSmbPort();
        final String host_port=h_port;
        String remdir_tmp="";
        if (sti.getMasterDirectoryName().equals("/") || sti.getMasterDirectoryName().equals("")) {
            remdir_tmp = "/";
        } else {
            remdir_tmp = sti.getMasterDirectoryName().startsWith("/")?sti.getMasterDirectoryName()+"/":"/" + sti.getMasterDirectoryName() + "/";
        }
        final String remdir = remdir_tmp;
        final String smb_proto=sti.getMasterSmbProtocol();
        final boolean ipc_enforced=sti.isMasterSmbIpcSigningEnforced();
        final boolean smb2_negotiation=sti.isMasterSmbUseSmb2Negotiation();

        NotifyEvent ntfy = new NotifyEvent(mContext);
        // set thread response
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                @SuppressWarnings("unchecked")
                ArrayList<TreeFilelistItem> rfl = (ArrayList<TreeFilelistItem>) o[0];

                if (rfl.size()==0) {
                    String msg=mContext.getString(R.string.msgs_dir_empty);
                    mUtil.showCommonDialog(false,"W",msg,"",null);
                    return;
                }

                //カスタムダイアログの生成
                final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setContentView(R.layout.item_select_list_dlg);

                LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_view);
//                ll_dlg_view.setBackgroundColor(mGp.themeColorList.title_background_color);

                final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_title_view);
                final TextView title = (TextView) dialog.findViewById(R.id.item_select_list_dlg_title);
                final TextView subtitle = (TextView) dialog.findViewById(R.id.item_select_list_dlg_subtitle);
                title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
                title.setTextColor(mGp.themeColorList.title_text_color);
//                subtitle.setTextColor(mGp.themeColorList.title_text_color);

                title.setText(mContext.getString(R.string.msgs_filter_list_dlg_add_dir_filter));
                subtitle.setText((remdir.equals("//")) ? host_name+host_addr+"/"+host_share : host_name+host_addr+"/"+host_share+  remdir);
                final TextView dlg_msg = (TextView) dialog.findViewById(R.id.item_select_list_dlg_msg);
//                final LinearLayout ll_context = (LinearLayout) dialog.findViewById(R.id.context_view_file_select);
//                ll_context.setVisibility(LinearLayout.VISIBLE);
//                final ImageButton ib_select_all = (ImageButton) ll_context.findViewById(R.id.context_button_select_all);
//                final ImageButton ib_unselect_all = (ImageButton) ll_context.findViewById(R.id.context_button_unselect_all);
                final Button btn_ok = (Button) dialog.findViewById(R.id.item_select_list_dlg_ok_btn);
                dlg_msg.setVisibility(TextView.VISIBLE);

                CommonDialog.setDlgBoxSizeLimit(dialog, true);

                final ListView lv = (ListView) dialog.findViewById(android.R.id.list);
                final TreeFilelistAdapter tfa = new TreeFilelistAdapter(mActivity, false, false);
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
                        expandHideRemoteDirTree(host_name, host_addr, host_share, host_port, ipc_enforced, smb2_negotiation, smb_proto, pos, tfi, tfa);
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
                        expandHideRemoteDirTree(host_name, host_addr, host_share, host_port, ipc_enforced, smb2_negotiation, smb_proto, pos, tfi, tfa);
                    }
                });
                lv.setOnItemLongClickListener(new OnItemLongClickListener() {
                    public boolean onItemLongClick(AdapterView<?> items, View view, int idx, long id) {
                        final int pos = tfa.getItem(idx);
                        final TreeFilelistItem tfi = tfa.getDataItem(pos);
                        tfi.setChecked(true);
                        tfa.notifyDataSetChanged();
                        return true;
                    }
                });

                //OKボタンの指定
                CommonDialog.setViewEnabled(mActivity, btn_ok, false);
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
                        try {
                            sel = sel.substring(remdir.length());
                            String dup_filter= getDuplicateFilter(sel, fla);
                            if (!dup_filter.equals("")) {
                                CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                                tfi.setChecked(false);
                                tfa.notifyDataSetChanged();
                                String mtxt = mContext.getString(R.string.msgs_filter_list_duplicate_filter_specified);
                                String dup_msg=String.format(mtxt, sel);
                                CommonUtilities.showToastMessageShort(mActivity, dup_msg);
                            } else {
                                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                            }
                        } catch(Exception e) {
                            mUtil.showCommonDialog(false,"E","Error","sel="+sel+", remdir="+remdir+"\n"+
                                    e.getMessage()+"\n"+ MiscUtil.getStackTraceString(e),null);
                        }
                    }

                    @Override
                    public void negativeResponse(Context arg0, Object[] arg1) {
                        if (tfa.isDataItemIsSelected()) CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                        else CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    }
                });
                tfa.setCbCheckListener(ntfy);

                btn_ok.setText(mContext.getString(R.string.msgs_filter_list_dlg_add));
                btn_ok.setVisibility(Button.VISIBLE);
                btn_ok.setOnClickListener(new OnClickListener() {
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
                btn_cancel.setOnClickListener(new OnClickListener() {
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
        createRemoteFileList(host_name, host_addr, host_share, host_port, remdir, ipc_enforced, smb2_negotiation, smb_proto, ntfy, true);
    }

//    @SuppressLint("DefaultLocale")
//    private String isFilterSameDirectoryAccess(SyncTaskItem sti, AdapterFilterList filterAdapter) {
//        if ((sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL) && sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) ||
//                (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD) && sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) ||
//                (sti.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB) && sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB))) {
//            SyncTaskItem new_sti=sti.clone();
//            ArrayList<String>dir_filter=new ArrayList<String>();
//
//            for(int i=0;i<filterAdapter.getCount();i++) {
//                AdapterFilterList.FilterListItem fli=filterAdapter.getItem(i);
//                String item="";
//                if (!fli.isDeleted()) {
//                    if (fli.isInclude()) item="I"+fli.getFilter();
//                    else item="E"+fli.getFilter();
//                    dir_filter.add(item);
//                }
//            }
//            new_sti.setDirFilter(dir_filter);
//            String msg=SyncTaskEditor.checkDirectoryFilterForSameDirectoryAccess(mContext, new_sti);
//            return msg;
//        }
//        return "";
//    }

    private boolean addDirFilter(boolean check_only, TreeFilelistAdapter tfa,
                                 AdapterFilterList fla, String cdir, TextView dlg_msg, SyncTaskItem sti, boolean smb_filter) {
        String sel = "", add_msg = "";
        //check duplicate entry
        for (int i = 0; i < tfa.getCount(); i++) {
            if (tfa.getDataItem(i).isChecked()) {
                if (tfa.getDataItem(i).getPath().length() == 1) sel = tfa.getDataItem(i).getName();
                else sel = tfa.getDataItem(i).getPath() + tfa.getDataItem(i).getName();
                if (sel.startsWith("/")) sel = sel.substring(cdir.length());
                String dup_filter= getDuplicateFilter(sel, fla);
                if (!dup_filter.equals("")) {
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
            fla.sort();
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

    public void scanSmbServerDlg(final NotifyEvent p_ntfy,
                                 String port_number, boolean scan_start, final int smb_protocol) {
        //カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.scan_remote_ntwk_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.scan_remote_ntwk_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.scan_remote_ntwk_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.scan_remote_ntwk_title);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        title.setTextColor(mGp.themeColorList.title_text_color);

        final Button btn_scan = (Button) dialog.findViewById(R.id.scan_remote_ntwk_btn_ok);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.scan_remote_ntwk_btn_cancel);
        final TextView tvmsg = (TextView) dialog.findViewById(R.id.scan_remote_ntwk_msg);
        final TextView tv_result = (TextView) dialog.findViewById(R.id.scan_remote_ntwk_scan_result_title);
        tvmsg.setText(mContext.getString(R.string.msgs_scan_ip_address_press_scan_btn));
        tv_result.setVisibility(TextView.GONE);

        final String from = CommonUtilities.getIfIpAddress(mUtil).equals("")?"192.168.0.1":CommonUtilities.getIfIpAddress(mUtil);
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
            CommonDialog.setViewEnabled(mActivity, et_port_number, false);
            ctv_use_port_number.setChecked(false);
        } else {
            CommonDialog.setViewEnabled(mActivity, et_port_number, true);
            et_port_number.setText(port_number);
            ctv_use_port_number.setChecked(true);
        }
        ctv_use_port_number.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctv_use_port_number.toggle();
                boolean isChecked = ctv_use_port_number.isChecked();
                CommonDialog.setViewEnabled(mActivity, et_port_number, isChecked);
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
        btn_scan.setOnClickListener(new OnClickListener() {
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
                    scanRemoteNetwork(dialog, lv, adap, //ipAddressList,
                            subnet, begin_addr, end_addr, ntfy, smb_protocol);
                } else {
                    //error
                }
            }
        });

        //CANCELボタンの指定
        btn_cancel.setOnClickListener(new OnClickListener() {
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
//            final ArrayList<AdapterNetworkScanResult.NetworkScanListItem> ipAddressList,
            final String subnet, final int begin_addr, final int end_addr,
            final NotifyEvent p_ntfy, final int smb_protcol) {
        final Handler handler = new Handler();
        final ThreadCtrl tc = new ThreadCtrl();
        final LinearLayout ll_addr = (LinearLayout) dialog.findViewById(R.id.scan_remote_ntwk_scan_address);
        final LinearLayout ll_prog = (LinearLayout) dialog.findViewById(R.id.scan_remote_ntwk_progress);
        final TextView tvmsg = (TextView) dialog.findViewById(R.id.scan_remote_ntwk_progress_msg);
        final Button btn_scan = (Button) dialog.findViewById(R.id.scan_remote_ntwk_btn_ok);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.scan_remote_ntwk_btn_cancel);
        final Button scan_cancel = (Button) dialog.findViewById(R.id.scan_remote_ntwk_progress_cancel);

//        final CheckedTextView ctv_use_port_number = (CheckedTextView) dialog.findViewById(R.id.scan_remote_ntwk_ctv_use_port);
        final EditText et_port_number = (EditText) dialog.findViewById(R.id.scan_remote_ntwk_port_number);

        tvmsg.setText("");
        scan_cancel.setText(R.string.msgs_scan_progress_spin_dlg_addr_cancel);
        ll_addr.setVisibility(LinearLayout.GONE);
        ll_prog.setVisibility(LinearLayout.VISIBLE);
        btn_scan.setVisibility(Button.GONE);
        btn_cancel.setVisibility(Button.GONE);
        adap.setButtonEnabled(false);
        CommonDialog.setViewEnabled(mActivity, scan_cancel, true);
        dialog.setOnKeyListener(new DialogBackKeyListener(mContext));
        dialog.setCancelable(false);
        // CANCELボタンの指定
        scan_cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
//                scan_cancel.setText(mContext.getString(R.string.msgs_progress_dlg_canceling));
                CommonDialog.setViewEnabled(mActivity, scan_cancel, false);
                mUtil.addDebugMsg(1, "W", "IP Address list creation was cancelled");
                tc.setDisabled();
            }
        });
        dialog.show();

        mUtil.addDebugMsg(1, "I", "Scan IP address ransge is " + subnet + "." + begin_addr + " - " + end_addr);

        mScanRequestedAddrList.clear();

        final String scan_prog = mContext.getString(R.string.msgs_ip_address_scan_progress);
        String p_txt = String.format(scan_prog, 0);
        tvmsg.setText(p_txt);

        mScanSmbErrorMessage="";
        Thread th=new Thread(new Runnable() {
            @Override
            public void run() {//non UI thread
                mScanCompleteCount = 0;
                mScanAddrCount = end_addr - begin_addr + 1;
                int scan_thread = 100;
                String scan_port = "";
                if (et_port_number.getText().length()>0) scan_port = et_port_number.getText().toString();
                for (int i = begin_addr; i <= end_addr; i += scan_thread) {
                    if (!tc.isEnabled()) break;
                    boolean scan_end = false;
                    for (int j = i; j < (i + scan_thread); j++) {
                        if (!tc.isEnabled()) break;
                        if (j <= end_addr) {
                            startRemoteNetworkScanThread(handler, tc, dialog, p_ntfy,
                                    lv_ipaddr, adap, tvmsg, subnet + "." + j,
//                                    ipAddressList,
                                    scan_port, smb_protcol);
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
                            adap.sort();
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
                                adap.sort();
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

    private String mScanSmbErrorMessage="";
    private void startRemoteNetworkScanThread(final Handler handler,
                                              final ThreadCtrl tc,
                                              final Dialog dialog,
                                              final NotifyEvent p_ntfy,
                                              final ListView lv_ipaddr,
                                              final AdapterNetworkScanResult adap,
                                              final TextView tvmsg,
                                              final String addr,
//                                              final ArrayList<AdapterNetworkScanResult.NetworkScanListItem> ipAddressList,
                                              final String scan_port, final int smb_level) {
        final String scan_prog = mContext.getString(R.string.msgs_ip_address_scan_progress);
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {//non UI thread
                if (tc.isEnabled()) {
                    synchronized (mScanRequestedAddrList) {
                        mScanRequestedAddrList.add(addr);
                    }
                    if (isIpAddrSmbHost(addr, scan_port)) {
                        final String srv_name = getSmbHostName(smb_level, addr);
                        final AdapterNetworkScanResult.NetworkScanListItem smb_server_item = new AdapterNetworkScanResult.NetworkScanListItem();
                        smb_server_item.server_address = addr;
                        String r_addr=null;
                        if (srv_name!=null) {
                            smb_server_item.server_name = srv_name;
                        }
                        buildSmbServerList(smb_server_item, "", "", "", addr);
                        handler.post(new Runnable() {// UI thread
                            @Override
                            public void run() {
                                synchronized (mScanRequestedAddrList) {
                                    mScanRequestedAddrList.remove(addr);
                                    synchronized (adap) {
                                        adap.add(smb_server_item);
                                        adap.sort();
                                    }
                                }
                                synchronized (mLockScanCompleteCount) {
                                    mScanCompleteCount++;
                                }
                            }
                        });
                    } else {
                        synchronized (mScanRequestedAddrList) {
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
//                            adap.notifyDataSetChanged();
                                String p_txt = String.format(scan_prog, (mScanCompleteCount * 100) / mScanAddrCount);
                                tvmsg.setText(p_txt);
                            }
                        }
                    });
                }
            }
        });
        th.start();
    }


    final private void buildSmbServerList(AdapterNetworkScanResult.NetworkScanListItem li, String domain, String user, String pass, String address) {
        SmbServerStatusResult s_result1=createSmbServerVersionList(1, domain, user, pass, address, "SMB1", "SMB1");
        li.server_smb_smb1_status=s_result1.server_status;
        li.server_smb_smb1_share_list=s_result1.share_lists;
        if (li.server_smb_smb1_status.equals("")) li.server_smb_supported="SMB1 ";
        else if (!li.server_smb_smb1_status.equals(SMB_STATUS_UNSUCCESSFULL)) li.server_smb_supported="SMB1 ";

        SmbServerStatusResult s_result2=createSmbServerVersionList(4, domain, user, pass, address, "SMB202", "SMB210");
        li.server_smb_smb2_status=s_result2.server_status;
        li.server_smb_smb2_share_list=s_result2.share_lists;
        if (li.server_smb_smb2_status.equals("")) li.server_smb_supported+="SMB2 ";
        else if (!li.server_smb_smb2_status.equals(SMB_STATUS_UNSUCCESSFULL)) li.server_smb_supported+="SMB2 ";

        SmbServerStatusResult s_result3=createSmbServerVersionList(5, domain, user, pass, address, "SMB300", "SMB311");
        li.server_smb_smb3_status=s_result3.server_status;
        li.server_smb_smb3_share_list=s_result3.share_lists;
        if (li.server_smb_smb3_status.equals("")) li.server_smb_supported+="SMB3";
        else if (!li.server_smb_smb3_status.equals(SMB_STATUS_UNSUCCESSFULL)) li.server_smb_supported+="SMB3";

    }

    private class SmbServerStatusResult {
        public String server_status="";
        public String share_lists="";
    }

    final private SmbServerStatusResult createSmbServerVersionList(int smb_level, String domain, String user, String pass, String address,
                                                                   String min_ver, String max_ver) {
        JcifsAuth auth=null;
        if (smb_level== JcifsAuth.JCIFS_FILE_SMB1) auth=new JcifsAuth(JcifsAuth.JCIFS_FILE_SMB1, domain, user, pass);
        else auth=new JcifsAuth(smb_level, domain, user, pass, true, min_ver, max_ver);
        String[] share_list=null;
        String server_status="";
        try {
            JcifsFile sf = new JcifsFile("smb://"+address, auth);
            share_list=sf.list();
            server_status="";
            mUtil.addDebugMsg(1,"I","createSmbServerVersionList level="+smb_level+", address="+address+", min="+min_ver+", max="+max_ver+", result="+server_status);
            for(String item:share_list) mUtil.addDebugMsg(1,"I","   Share="+item);
            try {
                sf.close();
            } catch(Exception e) {
                mUtil.addDebugMsg(1,"I","close() failed. Error=",e.getMessage());
            }
        } catch (JcifsException e) {
            if (e.getNtStatus()==0xc0000001) server_status=SMB_STATUS_UNSUCCESSFULL;                 //
            else if (e.getNtStatus()==0xc0000022) server_status=SMB_STATUS_ACCESS_DENIED;  //
            else if (e.getNtStatus()==0xc000015b) server_status=SMB_STATUS_INVALID_LOGON_TYPE;  //
            else if (e.getNtStatus()==0xc000006d) server_status=SMB_STATUS_UNKNOWN_ACCOUNT;  //
            String cause=e.getCause()==null?"":e.getCause().getMessage();
            mUtil.addDebugMsg(1,"I","createSmbServerVersionList level="+smb_level+", address="+address+", min="+min_ver+", max="+max_ver+
                    ", result="+server_status+String.format(", status=0x%8h",e.getNtStatus())+", error="+e.getMessage()+", cause="+cause);
        } catch (MalformedURLException e) {
//            log.info("Test logon failed." , e);
        }
        SmbServerStatusResult result=new SmbServerStatusResult();
        result.server_status=server_status;
        if (share_list!=null) {
            String sep="";
            for(String sli:share_list) {
                if (!sli.endsWith("$") && !sli.endsWith("$/")) {
                    if (sli.endsWith("/")) result.share_lists+=sep+sli.substring(0,sli.length()-1);
                    else result.share_lists+=sep+sli;
                    sep=",";
                }
            }
        }
        return result;
    }


    private boolean isIpAddrSmbHost(String address, String scan_port) {
        boolean smbhost = false;
        if (scan_port.equals("")) {
            if (!JcifsUtil.isIpAddressAndPortConnected(address, 445, 3500)) {
                smbhost = JcifsUtil.isIpAddressAndPortConnected(address, 139, 3500);
            } else smbhost = true;
        } else {
            smbhost = JcifsUtil.isIpAddressAndPortConnected(address, Integer.parseInt(scan_port), 3500);
        }
        mUtil.addDebugMsg(2, "I", "isIpAddrSmbHost Address=" + address + ", port=" + scan_port + ", smbhost=" + smbhost);
        return smbhost;
    }

    private String getSmbHostName(int smb_level, String address) {
        String srv_name = JcifsUtil.getSmbHostNameByAddress(smb_level, address);
        mUtil.addDebugMsg(1, "I", "getSmbHostName Address=" + address + ", name=" + srv_name);
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

    private void createLocalFilelist(boolean dironly, final String url, final String dir, final NotifyEvent p_ntfy) {
        mUtil.addDebugMsg(1, "I", "createLocalFilelist entered.");
        final long b_time=System.currentTimeMillis();
        final Dialog pd=showProgressSpinIndicator(mActivity);
        final ThreadCtrl tc = new ThreadCtrl();
        tc.setEnabled();
        tc.setThreadResultSuccess();
        pd.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                tc.setDisabled();//disableAsyncTask();
                mUtil.addDebugMsg(1, "W", "localFileList creation was cancelled.");
            }
        });
        pd.show();

        final Handler hndl = new Handler();
        Thread th=new Thread(){
          @Override
          public void run() {
              ArrayList<TreeFilelistItem> tfl = new ArrayList<TreeFilelistItem>();
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
                      if (!tc.isEnabled()) break;
                      if (ff[i].canRead()) {
                          int dirct = 0;
                          if (ff[i].isDirectory()) {
                              File tlf = new File(url + tdir + "/" + ff[i].getName());
                              File[] lfl = tlf.listFiles();
                              if (lfl != null) {
                                  for (int j = 0; j < lfl.length; j++) {
                                      if (!tc.isEnabled()) break;
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
                  if (tc.isEnabled()) Collections.sort(tfl);
              }
              hndl.post(new Runnable(){
                  @Override
                  public void run() {
                      mUtil.addDebugMsg(1, "I", "createLocalFilelist ended, tc="+tc.isEnabled()+", elapsed time="+(System.currentTimeMillis()-b_time));
                      p_ntfy.notifyToListener(tc.isEnabled(), new Object[]{tfl});
                      pd.dismiss();
                  }
              });
          }
        };
        th.start();
    }

    private void createRemoteFileList(String host_name, String host_addr, String host_share, String host_port,
                                      String remdir, boolean ipc_enforced, boolean smb2_negotiation,
                                      String smb_proto, final NotifyEvent p_event, boolean readSubDirCnt) {
        mUtil.addDebugMsg(1, "I", "createRemoteFilelist entered.");
        final long b_time=System.currentTimeMillis();

        final ArrayList<TreeFilelistItem> remoteFileList = new ArrayList<TreeFilelistItem>();
        final ThreadCtrl tc = new ThreadCtrl();
        tc.setEnabled();
        tc.setThreadResultSuccess();

        final Dialog dialog=showProgressSpinIndicator(mActivity);

        dialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                tc.setDisabled();//disableAsyncTask();
//                btn_cancel.setText(mContext.getString(R.string.msgs_progress_dlg_canceling));
//                btn_cancel.setEnabled(false);
                mUtil.addDebugMsg(1, "W", "createRemoteFileList cancelled.");
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
                        Collections.sort(remoteFileList);
                        mUtil.addDebugMsg(1, "I", "createRemoteFileList result=" + tc.getThreadResult() +
                                ", msg=" + tc.getThreadMessage() + ", tc=" + tc.isEnabled()+", elapsed time="+(System.currentTimeMillis()-b_time));
                        if (tc.isThreadResultSuccess()) {
                            p_event.notifyToListener(true, new Object[]{remoteFileList});
                        } else {
                            if (tc.isThreadResultError()) {
                                String sugget_msg=getJcifsErrorSugestionMessage(mContext, tc.getThreadMessage());
                                if (!sugget_msg.equals("")) {
                                    err=mContext.getString(R.string.msgs_filelist_error) + "\n"+sugget_msg+"\n"+tc.getThreadMessage();
                                } else {
                                    err=mContext.getString(R.string.msgs_filelist_error) + "\n"+tc.getThreadMessage();
                                }
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
        ra.smb_use_smb2_negotiation=smb2_negotiation;
        ra.smb_smb_protocol=smb_proto;
        Thread tf = new Thread(new ReadSmbFilelist(mContext, tc, host_name, host_addr, host_share, host_port,
                remdir, remoteFileList, ra, ntfy, true, readSubDirCnt, mGp));
        tf.start();

        dialog.show();
    }

    public static String getJcifsErrorSugestionMessage(Context c, String error_msg) {
        String sugget_msg="";
        if (isJcifsErrorChangeProtocolRequired(error_msg)) {
            sugget_msg=c.getString(R.string.msgs_profile_edit_sync_folder_dlg_smb_protocol_suggestion_message);
        }
        return sugget_msg;
    }

    public static boolean isJcifsErrorChangeProtocolRequired(String msg_text) {
        boolean result=false;
        String[] change_required_msg=new String[]{"This client is not compatible with the server"};
        for(String item:change_required_msg) {
            if (msg_text.contains(item)) {
                result=true;
                break;
            }
        }
        return result;
    }

    public Dialog showProgressSpinIndicator(Activity a) {
        final Dialog dialog=new Dialog(a, android.R.style.Theme_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_spin_indicator_dlg);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public void selectRemoteShareDlg(final String host_name, final String host_addr, String host_port,
                                     boolean ipc_enforced, boolean smb2_negotiation, String smb_proto, final NotifyEvent p_ntfy) {

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
                    mUtil.showCommonDialog(false, "W",
                            mContext.getString(R.string.msgs_share_list_not_obtained), "", null);
                    return;
                }
                Collections.sort(rows, String.CASE_INSENSITIVE_ORDER);
                //カスタムダイアログの生成
                final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setContentView(R.layout.item_select_list_dlg);

                LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_view);
                CommonUtilities.setDialogBoxOutline(mContext, ll_dlg_view);
//                ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

                final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.item_select_list_dlg_title_view);
                final TextView title = (TextView) dialog.findViewById(R.id.item_select_list_dlg_title);
                final TextView subtitle = (TextView) dialog.findViewById(R.id.item_select_list_dlg_subtitle);
                title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
                title.setTextColor(mGp.themeColorList.title_text_color);
//                subtitle.setTextColor(mGp.themeColorList.text_color_dialog_title);

                title.setText(mContext.getString(R.string.msgs_select_remote_share));
                subtitle.setVisibility(TextView.GONE);

                final Button btn_cancel = (Button) dialog.findViewById(R.id.item_select_list_dlg_cancel_btn);
                final Button btn_ok = (Button) dialog.findViewById(R.id.item_select_list_dlg_ok_btn);
                CommonDialog.setViewEnabled(mActivity, btn_ok, false);

                CommonDialog.setDlgBoxSizeLimit(dialog, false);

                final ListView lv = (ListView) dialog.findViewById(android.R.id.list);
                lv.setAdapter(new ArrayAdapter<String>(mActivity,
//						R.layout.custom_simple_list_item_checked,rows));
                        android.R.layout.simple_list_item_single_choice, rows));
                lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                lv.setScrollingCacheEnabled(false);
                lv.setScrollbarFadingEnabled(false);

                lv.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    }
                });
                //CANCELボタンの指定
                btn_cancel.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        p_ntfy.notifyToListener(false, null);
                    }
                });
                //OKボタンの指定
                btn_ok.setVisibility(Button.VISIBLE);
                btn_ok.setOnClickListener(new OnClickListener() {
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
        createRemoteFileList(host_name, host_addr, "", host_port, null, ipc_enforced, smb2_negotiation, smb_proto, ntfy, false);

    }

    private void expandHideRemoteDirTree(final String host_name, final String host_addr, final String host_share, final String host_port,
                                         boolean ipc_enforced, boolean smb2_negotiation, String smb_proto, final int pos,
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
                    createRemoteFileList(host_name, host_addr, host_share, host_port, tfi.getPath() + tfi.getName() + "/", ipc_enforced, smb2_negotiation, smb_proto, ne, true);
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
                    NotifyEvent ne = new NotifyEvent(mContext);
                    ne.setListener(new NotifyEventListener() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void positiveResponse(Context c, Object[] o) {
                            tfa.addChildItem(tfi, (ArrayList<TreeFilelistItem>) o[0], pos);
                        }

                        @Override
                        public void negativeResponse(Context c, Object[] o) {
                            mUtil.showCommonDialog(false, "E", "Local file list creation aborted", "", null);
                        }
                    });
                    createLocalFilelist(dironly, lclurl, tfi.getPath() + tfi.getName(), ne);
                }
            }
        }
    }

    synchronized public static ArrayList<SyncTaskItem> createSyncTaskList(Context context, GlobalParameters gp, CommonUtilities util, boolean auto_save) {
        return createSyncTaskListFromFile(context, gp, util, false, "", null, auto_save);
    }

    synchronized public static ArrayList<SyncTaskItem> createSyncTaskListFromFile(Context context, GlobalParameters gp, CommonUtilities util,
                                                                     boolean sdcard, String fp, ArrayList<PreferenceParmListIItem> ispl, boolean auto_save) {
        ArrayList<SyncTaskItem> sync = new ArrayList<SyncTaskItem>();
        if (ispl != null) ispl.clear();

        if (sdcard) {
            File sf = new File(fp);
            if (sf.exists()) {
                CipherParms cp = null;
                CipherParms cp_autosave=null;
                boolean prof_encrypted = isSyncTaskListFileEncrypted(fp);
                if (prof_encrypted) {
                    cp = EncryptUtil.initDecryptEnv(gp.profileKeyPrefix + gp.profilePassword);
                }
                if (auto_save) {
                    String priv_key= null;
                    try {
                        priv_key = KeyStoreUtil.getGeneratedPasswordNewVersion(context, SMBSYNC2_KEY_STORE_ALIAS);
                    } catch (Exception e) {
                        String stm= MiscUtil.getStackTraceString(e);
                        util.addDebugMsg(1,"W","createSyncTaskListFromFile decrypt password obtain error="+e.getMessage()+"\n"+stm);
                        e.printStackTrace();
                    }
                    if (priv_key!=null) cp_autosave= EncryptUtil.initDecryptEnv(priv_key);
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
                        else if (pl.startsWith(SMBSYNC2_PROF_VER7)) prof_pre = SMBSYNC2_PROF_VER7;
                        else if (pl.startsWith(SMBSYNC2_PROF_VER8)) prof_pre = SMBSYNC2_PROF_VER8;
                        if (!pl.startsWith(prof_pre + SMBSYNC2_PROF_ENC) &&
                                !pl.startsWith(prof_pre + SMBSYNC2_PROF_DEC)) {
                            if (prof_encrypted) {
                                String enc_str = pl.replace(prof_pre, "");
                                byte[] enc_array = Base64Compat.decode(enc_str, Base64Compat.NO_WRAP);
                                String dec_str = EncryptUtil.decrypt(enc_array, cp);
                                addSyncTaskList(context, gp, sdcard, prof_pre + dec_str, sync, ispl, util, cp_autosave, auto_save);
                            } else {
                                addSyncTaskList(context, gp, sdcard, pl, sync, ispl, util, cp_autosave, auto_save);
                            }
                        }
                    }
                    br.close();
                } catch (IOException e) {
                    String stm= MiscUtil.getStackTraceString(e);
                    util.addDebugMsg(1,"I","createSyncTaskListFromFile error="+e.getMessage()+"\n"+stm);
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
                File lf7 = new File(gp.applicationRootDirectory + "/" + SMBSYNC2_PROFILE_FILE_NAME_V7);
                File lf8 = new File(gp.applicationRootDirectory + "/" + SMBSYNC2_PROFILE_FILE_NAME_V8);
                if (lf8.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V8;
                else if (lf7.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V7;
                else if (lf6.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V6;
                else if (lf5.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V5;
                else if (lf4.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V4;
                else if (lf3.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V3;
                else if (lf2.exists()) pf = SMBSYNC2_PROFILE_FILE_NAME_V2;
                else pf = SMBSYNC2_PROFILE_FILE_NAME_V1;

                File lf=new File(gp.applicationRootDirectory + "/" + pf);

                File pd=new File(gp.applicationRootDirectory);
                File[] pd_list=pd.listFiles();
                if (pd_list!=null && pd_list.length>0) {
                    for(File item:pd_list) util.addDebugMsg(1,"I","createSyncTaskListFromFile list files="+item.getName());
                }

                if (lf.exists()) {
                    String priv_key=null;
                    CipherParms cp_int=null;
                    if (pf.equals(SMBSYNC2_PROFILE_FILE_NAME_V7)) {
                        priv_key=KeyStoreUtil.getGeneratedPasswordOldVersion(context, SMBSYNC2_KEY_STORE_ALIAS);
                    } else if (pf.equals(SMBSYNC2_PROFILE_FILE_NAME_V8)) {
                        priv_key=KeyStoreUtil.getGeneratedPasswordNewVersion(context, SMBSYNC2_KEY_STORE_ALIAS);
                    } else {
                        priv_key=KeyStoreUtil.getGeneratedPasswordNewVersion(context, SMBSYNC2_KEY_STORE_ALIAS);
                    }
                    cp_int= EncryptUtil.initDecryptEnv(priv_key);
                    br = new BufferedReader(new FileReader(gp.applicationRootDirectory + "/" + pf), 8192);
                    util.addDebugMsg(1,"I","createSyncTaskListFromFile profile="+lf.getPath());
                    String pl;
                    while ((pl = br.readLine()) != null) {
                        if (pl.startsWith(SMBSYNC2_PROF_VER7)) {
                            String prof_pre = "";
                            if (pl.startsWith(SMBSYNC2_PROF_VER7)) prof_pre = SMBSYNC2_PROF_VER7;
                            String enc_str = pl.substring(6);
                            byte[] dec_array = Base64Compat.decode(enc_str, Base64Compat.NO_WRAP);
                            String dec_str = EncryptUtil.decrypt(dec_array, cp_int);
                            addSyncTaskList(context, gp, sdcard, prof_pre + dec_str, sync, ispl, util, null, auto_save);
                        } else if (pl.startsWith(SMBSYNC2_PROF_VER8)) {
                            String prof_pre="";
                            if (pl.startsWith(SMBSYNC2_PROF_VER8)) prof_pre=SMBSYNC2_PROF_VER8;
                            String enc_str=pl.substring(6);
                            byte[] dec_array = Base64Compat.decode(enc_str, Base64Compat.NO_WRAP);
                            String dec_str = EncryptUtil.decrypt(dec_array, cp_int);
                            addSyncTaskList(context, gp, sdcard,prof_pre+dec_str , sync, ispl, util, null, auto_save);
                        } else {
                            addSyncTaskList(context, gp, sdcard, pl, sync, ispl, util, null, auto_save);
                        }
                    }
                    br.close();
                    if (!lf7.exists()) saveSyncTaskList(gp, context, util, sync);
                } else {
                    util.addDebugMsg(1, "W", "profile not found, empty profile list created. fn=" +
                            gp.applicationRootDirectory + "/" + pf);
                }
            } catch (IOException e) {
                String stm= MiscUtil.getStackTraceString(e);
                util.addDebugMsg(1,"I","createSyncTaskListFromFile error="+e.getMessage()+"\n"+stm);
                e.printStackTrace();
            } catch (Exception e) {
                String stm= MiscUtil.getStackTraceString(e);
                util.addDebugMsg(1,"I","createSyncTaskListFromFile error="+e.getMessage()+"\n"+stm);
                e.printStackTrace();
            }
            if (sync.size() == 0) {
//                if (BUILD_FOR_AMAZON) {
//                    //アマゾン用はサンプルプロファイルを作成しない
//                } else {
//                    if (gp.sampleProfileCreateRequired) {
//                        createSampleSyncTask(sync);
//                        saveSyncTaskList(gp, context, util, sync);
//                        gp.sampleProfileCreateRequired = false;
//                    }
//                }
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

        stli.setSyncOptionWifiStatusOption(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP);
        stli.setSyncOptionUseExtendedDirectoryFilter1(true);
        stli.setSyncTaskPosition(0);
        pfl.add(stli);

        stli = new SyncTaskItem("BACKUP-MY-PICTURE", true, false);

        stli.setMasterFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
        stli.setMasterDirectoryName("DCIM");

        stli.setTargetFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_SMB);
        stli.setTargetSmbAddr("192.168.0.2");
        stli.setTargetSmbUserName("TESTUSER");
        stli.setTargetSmbPassword("PSWD");
        stli.setTargetSmbShareName("SHARE");
        stli.setSyncTestMode(false);
        stli.setTargetDirectoryName("Android/DCIM");

        stli.setSyncOptionWifiStatusOption(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_CONNECT_ANY_AP);
        stli.setSyncOptionUseExtendedDirectoryFilter1(true);
        stli.setSyncTaskPosition(1);
        pfl.add(stli);

        stli = new SyncTaskItem("BACKUP-TO-SDCARD", true, false);

        stli.setMasterFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
        stli.setMasterDirectoryName("Pictures");

        stli.setTargetFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD);
        stli.setTargetDirectoryName("Pictures");
        stli.setSyncTestMode(false);
        stli.setSyncOptionWifiStatusOption(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_OFF);

        stli.setSyncOptionUseExtendedDirectoryFilter1(true);
        stli.setSyncTaskPosition(2);
        pfl.add(stli);
    }

    static public void sortSyncTaskList(ArrayList<SyncTaskItem> stl) {
        Collections.sort(stl, new Comparator<SyncTaskItem>() {
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

        for (int i = 0; i < stl.size(); i++) {
            SyncTaskItem sti = stl.get(i);
            sti.setSyncTaskPosition(i);
            if (!isValidSyncTaskName(stl, sti.getSyncTaskName(), true)) {
                sti.setSyncTaskNameError(true);
            } else {
                sti.setSyncTaskNameError(false);
            }
            //stl.set(i, sti);
        }
    }

    private static void addSyncTaskList(Context c, GlobalParameters gp, boolean sdcard, String pl, ArrayList<SyncTaskItem> sync,
                                        ArrayList<PreferenceParmListIItem> ispl, CommonUtilities util, CipherParms cp_autosave, boolean auto_save) {
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
        } else if (pl.startsWith(SMBSYNC2_PROF_VER7)) {
            if (pl.length() > 10) {
                addSyncTaskListVer7(sdcard, pl.replace(SMBSYNC2_PROF_VER7, ""), sync, util);
                if (ispl != null) addImportSettingsParm(pl.replace(SMBSYNC2_PROF_VER7, ""), ispl);
            }
        } else if (pl.startsWith(SMBSYNC2_PROF_VER8)) {
            if (pl.length() > 10) {
                addSyncTaskListVer8(c, gp, sdcard, pl.replace(SMBSYNC2_PROF_VER8, ""), sync, util, cp_autosave, auto_save);
                if (ispl != null) addImportSettingsParm(pl.replace(SMBSYNC2_PROF_VER8, ""), ispl);
            }
        }
    }

    private static void addSyncTaskListVer1(String pl, ArrayList<SyncTaskItem> sync) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", npl = "";
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i]);//.trim());
            }
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
            stli.setTargetSmbAddr(parm[18]);
            stli.setTargetSmbPort(parm[19]);
            stli.setTargetSmbHostname(parm[20]);
            stli.setTargetSmbDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncOptionWifiConnectedAccessPointWhiteList(wifi_wl);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetFileLastModified(parm[26].equals("1") ? true : false);

            stli.setSyncOptionRetryCount(parm[27]);

            stli.setSyncOptionSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncOptionSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncOptionUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            stli.setSyncOptionDifferentFileAllowableTime(Integer.parseInt(parm[34]));
            stli.setSyncOptionDifferentFileByTime(parm[35].equals("1") ? true : false);

//            stli.setSyncUseFileCopyByTempNamex(parm[36].equals("1") ? true : false);
            stli.setSyncOptionWifiStatusOption(parm[37]);

            stli.setLastSyncTime(parm[38]);
            stli.setLastSyncResult(Integer.parseInt(parm[39]));

            if (!parm[40].equals("") && !parm[40].equals("end"))
                stli.setSyncTaskPosition(Integer.parseInt(parm[40]));

//			if (!parm[41].equals("") && !parm[41].equals("end")) stli.setMasterFolderUseInternalUsbFolder(parm[41].equals("1")?true:false);
//			if (!parm[42].equals("") && !parm[42].equals("end")) stli.setTargetFolderUseInternalUsbFolder(parm[42].equals("1")?true:false);

            sync.add(stli);
        }
    }

    private static void addSyncTaskListVer2(String pl, ArrayList<SyncTaskItem> sync) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", npl = "";
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i]);//.trim());
            }
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
            stli.setTargetSmbAddr(parm[18]);
            stli.setTargetSmbPort(parm[19]);
            stli.setTargetSmbHostname(parm[20]);
            stli.setTargetSmbDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncOptionWifiConnectedAccessPointWhiteList(wifi_wl);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetFileLastModified(parm[26].equals("1") ? true : false);

            stli.setSyncOptionRetryCount(parm[27]);

            stli.setSyncOptionSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncOptionSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncOptionUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            stli.setSyncOptionDifferentFileAllowableTime(Integer.parseInt(parm[34]));
            stli.setSyncOptionDifferentFileByTime(parm[35].equals("1") ? true : false);

//            stli.setSyncUseFileCopyByTempNamex(parm[36].equals("1") ? true : false);
            stli.setSyncOptionWifiStatusOption(parm[37]);

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
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i]);//.trim());
            }
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
            stli.setTargetSmbAddr(parm[18]);
            stli.setTargetSmbPort(parm[19]);
            stli.setTargetSmbHostname(parm[20]);
            stli.setTargetSmbDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncOptionWifiConnectedAccessPointWhiteList(wifi_wl);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetFileLastModified(parm[26].equals("1") ? true : false);

            stli.setSyncOptionRetryCount(parm[27]);

            stli.setSyncOptionSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncOptionSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncOptionUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            stli.setSyncOptionDifferentFileAllowableTime(Integer.parseInt(parm[34]));
            stli.setSyncOptionDifferentFileByTime(parm[35].equals("1") ? true : false);

//            stli.setSyncUseFileCopyByTempNamex(parm[36].equals("1") ? true : false);
            stli.setSyncOptionWifiStatusOption(parm[37]);

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
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i]);//.trim());
            }
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
            stli.setTargetSmbAddr(parm[18]);
            stli.setTargetSmbPort(parm[19]);
            stli.setTargetSmbHostname(parm[20]);
            stli.setTargetSmbDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncOptionWifiConnectedAccessPointWhiteList(wifi_wl);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetFileLastModified(parm[26].equals("1") ? true : false);

            stli.setSyncOptionRetryCount(parm[27]);

            stli.setSyncOptionSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncOptionSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncOptionUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            stli.setSyncOptionDifferentFileAllowableTime(Integer.parseInt(parm[34]));
            stli.setSyncOptionDifferentFileByTime(parm[35].equals("1") ? true : false);

//            stli.setSyncUseFileCopyByTempNamex(parm[36].equals("1") ? true : false);
            stli.setSyncOptionWifiStatusOption(parm[37]);

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
                stli.setSyncOptionTaskSkipIfConnectAnotherWifiSsid(parm[53].equals("1") ? true : false);

//			if (!parm[54].equals("") && !parm[54].equals("end")) stli.setTargetZipUseInternalUsbFolder(parm[54].equals("1")?true:false);
            if (!parm[55].equals("") && !parm[55].equals("end"))
                stli.setTargetZipUseExternalSdcard(parm[55].equals("1") ? true : false);

//            if (!parm[56].equals("") && !parm[56].equals("end"))
//                stli.setSyncTaskTwoWay(parm[56].equals("1") ? true : false);
//            if (!parm[57].equals("") && !parm[57].equals("end"))
//                stli.setSyncTwoWayConflictFileRule(parm[57]);

            if (!parm[58].equals("") && !parm[58].equals("end"))
                stli.setTargetZipFileNameEncoding(parm[58]);

            if (!parm[59].equals("") && !parm[59].equals("end"))
                stli.setSyncOptionDifferentFileBySize((parm[59].equals("1") ? true : false));

            sync.add(stli);
        }
    }

    ;

    private static void addSyncTaskListVer5(String pl, ArrayList<SyncTaskItem> sync) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", npl = "";
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i]);//.trim());
            }
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
            stli.setTargetSmbAddr(parm[18]);
            stli.setTargetSmbPort(parm[19]);
            stli.setTargetSmbHostname(parm[20]);
            stli.setTargetSmbDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncOptionWifiConnectedAccessPointWhiteList(wifi_wl);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetFileLastModified(parm[26].equals("1") ? true : false);

            stli.setSyncOptionRetryCount(parm[27]);

            stli.setSyncOptionSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncOptionSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncOptionUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            stli.setSyncOptionDifferentFileAllowableTime(Integer.parseInt(parm[34]));
            stli.setSyncOptionDifferentFileByTime(parm[35].equals("1") ? true : false);

//            stli.setSyncUseFileCopyByTempNamex(parm[36].equals("1") ? true : false);
            stli.setSyncOptionWifiStatusOption(parm[37]);

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
                stli.setSyncOptionTaskSkipIfConnectAnotherWifiSsid(parm[53].equals("1") ? true : false);

//            if (!parm[54].equals("") && !parm[54].equals("end")) stli.setTargetZipUseInternalUsbFolder(parm[54].equals("1")?true:false);
            if (!parm[55].equals("") && !parm[55].equals("end"))
                stli.setTargetZipUseExternalSdcard(parm[55].equals("1") ? true : false);

//            if (!parm[56].equals("") && !parm[56].equals("end"))
//                stli.setSyncTaskTwoWay(parm[56].equals("1") ? true : false);
//            if (!parm[57].equals("") && !parm[57].equals("end"))
//                stli.setSyncTwoWayConflictFileRule(parm[57]);

            if (!parm[58].equals("") && !parm[58].equals("end"))
                stli.setTargetZipFileNameEncoding(parm[58]);

            if (!parm[59].equals("") && !parm[59].equals("end"))
                stli.setSyncOptionDifferentFileBySize((parm[59].equals("1") ? true : false));

            if (!parm[60].equals("") && !parm[60].equals("end"))
                stli.setSyncOptionUseExtendedDirectoryFilter1((parm[60].equals("1") ? true : false));

            sync.add(stli);
        }
    }

    private static void addSyncTaskListVer6(String pl, ArrayList<SyncTaskItem> sync) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", list4="", npl = "";
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);
        if (list_array.length>=4) list4 = list_array[3].substring(1);

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i]);//.trim());
            }
        }

        if (parm[0].equals(SMBSYNC2_PROF_TYPE_SYNC)) {//Sync
            ArrayList<String> ff = new ArrayList<String>();
            ArrayList<String> df = new ArrayList<String>();
            ArrayList<String> wifi_ap_list = new ArrayList<String>();
            ArrayList<String> wifi_addr_list = new ArrayList<String>();
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
                for (int i = 0; i < wl.length; i++) wifi_ap_list.add(convertToSpecChar(wl[i]));
            } else wifi_ap_list.clear();

            if (list4.length() != 0) {
                String[] al = list4.split("\t");
                for (int i = 0; i < al.length; i++) wifi_addr_list.add(convertToSpecChar(al[i]));
            } else wifi_addr_list.clear();

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
            stli.setTargetSmbAddr(parm[18]);
            stli.setTargetSmbPort(parm[19]);
            stli.setTargetSmbHostname(parm[20]);
            stli.setTargetSmbDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncOptionWifiConnectedAccessPointWhiteList(wifi_ap_list);
            stli.setSyncOptionWifiConnectedAddressWhiteList(wifi_addr_list);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetFileLastModified(parm[26].equals("1") ? true : false);

            stli.setSyncOptionRetryCount(parm[27]);

            stli.setSyncOptionSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncOptionSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncOptionUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            try {stli.setSyncOptionDifferentFileAllowableTime(Integer.parseInt(parm[34]));} catch(Exception e) {}
            stli.setSyncOptionDifferentFileByTime(parm[35].equals("1") ? true : false);

//            stli.setSyncUseFileCopyByTempNamex(parm[36].equals("1") ? true : false);
            stli.setSyncOptionWifiStatusOption(parm[37]);

            stli.setLastSyncTime(parm[38]);
            try {stli.setLastSyncResult(Integer.parseInt(parm[39]));} catch(Exception e) {}

            try {if (!parm[40].equals("") && !parm[40].equals("end"))stli.setSyncTaskPosition(Integer.parseInt(parm[40]));} catch(Exception e) {}

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
                stli.setSyncOptionTaskSkipIfConnectAnotherWifiSsid(parm[53].equals("1") ? true : false);

            if (!parm[54].equals("") && !parm[54].equals("end"))
                stli.setSyncOptionSyncWhenCharging(parm[54].equals("1") ? true : false);

            if (!parm[55].equals("") && !parm[55].equals("end"))
                stli.setTargetZipUseExternalSdcard(parm[55].equals("1") ? true : false);

//            if (!parm[56].equals("") && !parm[56].equals("end"))
//                stli.setSyncTaskTwoWay(parm[56].equals("1") ? true : false);
//            if (!parm[57].equals("") && !parm[57].equals("end"))
//                stli.setSyncTwoWayConflictFileRule(parm[57]);

            if (!parm[58].equals("") && !parm[58].equals("end"))
                stli.setTargetZipFileNameEncoding(parm[58]);

            if (!parm[59].equals("") && !parm[59].equals("end"))
                stli.setSyncOptionDifferentFileBySize((parm[59].equals("1") ? true : false));

            if (!parm[60].equals("") && !parm[60].equals("end"))
                stli.setSyncOptionUseExtendedDirectoryFilter1((parm[60].equals("1") ? true : false));

            if (!parm[61].equals("") && !parm[61].equals("end"))
                stli.setMasterLocalMountPoint(parm[61]);

            if (!parm[62].equals("") && !parm[62].equals("end"))
                stli.setMasterLocalMountPoint(parm[62]);

            if (!parm[63].equals("") && !parm[63].equals("end")) stli.setSyncTaskGroup(parm[63]);

            if (!parm[64].equals("") && !parm[64].equals("end")) stli.setMasterSmbProtocol(parm[64]);

            if (!parm[65].equals("") && !parm[65].equals("end")) stli.setTargetSmbProtocol(parm[65]);

            if (!parm[66].equals("") && !parm[66].equals("end")) stli.setMasterSmbIpcSigningEnforced((parm[66].equals("1") ? true : false));
            if (!parm[67].equals("") && !parm[67].equals("end")) stli.setTargetSmbIpcSigningEnforced((parm[67].equals("1") ? true : false));

            if (!parm[68].equals("") && !parm[68].equals("end")) stli.setArchiveRenameFileTemplate(parm[68]);
            if (!parm[69].equals("") && !parm[69].equals("end")) stli.setArchiveUseRename((parm[69].equals("1") ? true : false));
            try {if (!parm[70].equals("") && !parm[70].equals("end")) stli.setArchiveRetentionPeriod(Integer.parseInt(parm[70]));} catch(Exception e) {}

            if (!parm[71].equals("") && !parm[71].equals("end")) stli.setArchiveCreateDirectory((parm[71].equals("1") ? true : false));
            if (!parm[72].equals("") && !parm[72].equals("end")) {
                if (parm[72].equals("1")) stli.setArchiveSuffixOption("5");
                else if (parm[72].equals("1")) stli.setArchiveSuffixOption("6");
                else stli.setArchiveSuffixOption(parm[72]);
            }

            if (!parm[73].equals("") && !parm[73].equals("end")) stli.setArchiveCreateDirectoryTemplate(parm[73]);
            if (!parm[74].equals("") && !parm[74].equals("end")) stli.setArchiveEnabled((parm[74].equals("1") ? true : false));

            if (!parm[75].equals("") && !parm[75].equals("end")) stli.setSyncDifferentFileSizeGreaterThanTagetFile((parm[75].equals("1") ? true : false));

            if (!parm[76].equals("") && !parm[76].equals("end")) stli.setSyncOptionDeleteFirstWhenMirror((parm[76].equals("1") ? true : false));

            if (!parm[77].equals("") && !parm[77].equals("end")) stli.setSyncOptionConfirmNotExistsExifDate((parm[77].equals("1") ? true : false));

            if (!parm[78].equals("") && !parm[78].equals("end")) stli.setTargetZipUseUsb((parm[78].equals("1") ? true : false));

            if (!parm[79].equals("") && !parm[79].equals("end")) stli.setSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile((parm[79].equals("1") ? true : false));

            if (!parm[80].equals("") && !parm[80].equals("end")) stli.setSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters((parm[80].equals("1") ? true : false));

            if (stli.getMasterSmbProtocol().equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SYSTEM))
                stli.setMasterSmbProtocol(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1);
            if (stli.getTargetSmbProtocol().equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SYSTEM))
                stli.setTargetSmbProtocol(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1);
            sync.add(stli);
        }
    }

    private static void addSyncTaskListVer7(boolean sdcard, String pl, ArrayList<SyncTaskItem> sync, CommonUtilities util) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", list4="", npl = "";
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);
        if (list_array.length>=4) list4 = list_array[3].substring(1);

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i]);//.trim());
            }
        }

        if (parm[0].equals(SMBSYNC2_PROF_TYPE_SYNC)) {//Sync
            ArrayList<String> ff = new ArrayList<String>();
            ArrayList<String> df = new ArrayList<String>();
            ArrayList<String> wifi_ap_list = new ArrayList<String>();
            ArrayList<String> wifi_addr_list = new ArrayList<String>();
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
                for (int i = 0; i < wl.length; i++) wifi_ap_list.add(convertToSpecChar(wl[i]));
            } else wifi_ap_list.clear();

            if (list4.length() != 0) {
                String[] al = list4.split("\t");
                for (int i = 0; i < al.length; i++) wifi_addr_list.add(convertToSpecChar(al[i]));
            } else wifi_addr_list.clear();

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
            stli.setTargetSmbAddr(parm[18]);
            stli.setTargetSmbPort(parm[19]);
            stli.setTargetSmbHostname(parm[20]);
            stli.setTargetSmbDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncOptionWifiConnectedAccessPointWhiteList(wifi_ap_list);
            stli.setSyncOptionWifiConnectedAddressWhiteList(wifi_addr_list);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetFileLastModified(parm[26].equals("1") ? true : false);

            stli.setSyncOptionRetryCount(parm[27]);

            stli.setSyncOptionSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncOptionSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncOptionUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            try {stli.setSyncOptionDifferentFileAllowableTime(Integer.parseInt(parm[34]));} catch(Exception e) {}
            stli.setSyncOptionDifferentFileByTime(parm[35].equals("1") ? true : false);

//            stli.setSyncUseFileCopyByTempNamex(parm[36].equals("1") ? true : false);
            stli.setSyncOptionWifiStatusOption(parm[37]);

            stli.setLastSyncTime(parm[38]);
            try {stli.setLastSyncResult(Integer.parseInt(parm[39]));} catch(Exception e) {}

            try {if (!parm[40].equals("") && !parm[40].equals("end"))stli.setSyncTaskPosition(Integer.parseInt(parm[40]));} catch(Exception e) {}

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
                stli.setSyncOptionTaskSkipIfConnectAnotherWifiSsid(parm[53].equals("1") ? true : false);

            if (!parm[54].equals("") && !parm[54].equals("end"))
                stli.setSyncOptionSyncWhenCharging(parm[54].equals("1") ? true : false);

            if (!parm[55].equals("") && !parm[55].equals("end"))
                stli.setTargetZipUseExternalSdcard(parm[55].equals("1") ? true : false);

//            if (!parm[56].equals("") && !parm[56].equals("end"))
//                stli.setSyncTaskTwoWay(parm[56].equals("1") ? true : false);
//            if (!parm[57].equals("") && !parm[57].equals("end"))
//                stli.setSyncTwoWayConflictFileRule(parm[57]);

            if (!parm[58].equals("") && !parm[58].equals("end"))
                stli.setTargetZipFileNameEncoding(parm[58]);

            if (!parm[59].equals("") && !parm[59].equals("end"))
                stli.setSyncOptionDifferentFileBySize((parm[59].equals("1") ? true : false));

            if (!parm[60].equals("") && !parm[60].equals("end"))
                stli.setSyncOptionUseExtendedDirectoryFilter1((parm[60].equals("1") ? true : false));

            if (!parm[61].equals("") && !parm[61].equals("end"))
                stli.setMasterLocalMountPoint(parm[61]);

            if (!parm[62].equals("") && !parm[62].equals("end"))
                stli.setMasterLocalMountPoint(parm[62]);

            if (!parm[63].equals("") && !parm[63].equals("end")) stli.setSyncTaskGroup(parm[63]);

            if (!parm[64].equals("") && !parm[64].equals("end")) stli.setMasterSmbProtocol(parm[64]);

            if (!parm[65].equals("") && !parm[65].equals("end")) stli.setTargetSmbProtocol(parm[65]);

            if (!parm[66].equals("") && !parm[66].equals("end")) stli.setMasterSmbIpcSigningEnforced((parm[66].equals("1") ? true : false));
            if (!parm[67].equals("") && !parm[67].equals("end")) stli.setTargetSmbIpcSigningEnforced((parm[67].equals("1") ? true : false));

            if (!parm[68].equals("") && !parm[68].equals("end")) stli.setArchiveRenameFileTemplate(parm[68]);
            if (!parm[69].equals("") && !parm[69].equals("end")) stli.setArchiveUseRename((parm[69].equals("1") ? true : false));
            try {if (!parm[70].equals("") && !parm[70].equals("end")) stli.setArchiveRetentionPeriod(Integer.parseInt(parm[70]));} catch(Exception e) {}

            if (!parm[71].equals("") && !parm[71].equals("end")) stli.setArchiveCreateDirectory((parm[71].equals("1") ? true : false));
            if (!parm[72].equals("") && !parm[72].equals("end")) {
                if (parm[72].equals("1")) stli.setArchiveSuffixOption("5");
                else if (parm[72].equals("1")) stli.setArchiveSuffixOption("6");
                else stli.setArchiveSuffixOption(parm[72]);
            }

            if (!parm[73].equals("") && !parm[73].equals("end")) stli.setArchiveCreateDirectoryTemplate(parm[73]);
            if (!parm[74].equals("") && !parm[74].equals("end")) stli.setArchiveEnabled((parm[74].equals("1") ? true : false));

            if (!parm[75].equals("") && !parm[75].equals("end")) stli.setSyncDifferentFileSizeGreaterThanTagetFile((parm[75].equals("1") ? true : false));

            if (!parm[76].equals("") && !parm[76].equals("end")) stli.setSyncOptionDeleteFirstWhenMirror((parm[76].equals("1") ? true : false));

            if (!parm[77].equals("") && !parm[77].equals("end")) stli.setSyncOptionConfirmNotExistsExifDate((parm[77].equals("1") ? true : false));

            if (!parm[78].equals("") && !parm[78].equals("end")) stli.setTargetZipUseUsb((parm[78].equals("1") ? true : false));

            if (!parm[79].equals("") && !parm[79].equals("end")) stli.setSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile((parm[79].equals("1") ? true : false));

            if (!parm[80].equals("") && !parm[80].equals("end")) stli.setSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters((parm[80].equals("1") ? true : false));

            if (stli.getMasterSmbProtocol().equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SYSTEM))
                stli.setMasterSmbProtocol(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1);
            if (stli.getTargetSmbProtocol().equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SYSTEM))
                stli.setTargetSmbProtocol(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1);
            sync.add(stli);
        }
    }

    private static String decryptByInternalPassword(CommonUtilities util, CipherParms cp_autosave, String enc_str) {
        String dec_str =null;
        try {
            byte[] dec_array = Base64Compat.decode(enc_str, Base64Compat.NO_WRAP);
            dec_str = EncryptUtil.decrypt(dec_array, cp_autosave);
        } catch(Exception e) {
            String stm= MiscUtil.getStackTraceString(e);
            util.addDebugMsg(1,"W","createSyncTaskListFromFile error="+e.getMessage()+"\n"+stm);
            e.printStackTrace();
        }
        return dec_str;
    }

    private static void putTaskListValueErrorMessage(CommonUtilities cu, String item_name, Object assumed_value) {
        String msg_template="Invalid \"%s\" was detected while reading the task list, so \"%s\" was set to \"%s\".";
        String val="";
        if (assumed_value instanceof String) val=(String)assumed_value;
        else if (assumed_value instanceof Integer) val=String.valueOf((Integer)assumed_value);
        else val="?????";
        cu.addLogMsg("W", String.format(msg_template, item_name, val, item_name));
    }

    private static void addSyncTaskListVer8(Context c, GlobalParameters gp, boolean sdcard, String pl, ArrayList<SyncTaskItem> sync, CommonUtilities util, CipherParms cp_autosave, boolean auto_save) {
        if (!pl.startsWith(SMBSYNC2_PROF_TYPE_SYNC)) return; //ignore settings entry
        String list1 = "", list2 = "", list3 = "", list4="", npl = "";
        int ls = pl.indexOf("[");
        int le = pl.lastIndexOf("]\t");
        String list = pl.substring(ls, le + 2);
        npl = pl.replace(list, "");

        String[] list_array = list.split("]\t");
        list1 = list_array[0].substring(1);
        list2 = list_array[1].substring(1);
        list3 = list_array[2].substring(1);
        if (list_array.length>=4) list4 = list_array[3].substring(1);

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[200];
        for (int i = 0; i < 200; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i]);//.trim());
            }
        }

        if (parm[0].equals(SMBSYNC2_PROF_TYPE_SYNC)) {//Sync
            ArrayList<String> ff = new ArrayList<String>();
            ArrayList<String> df = new ArrayList<String>();
            ArrayList<String> wifi_ap_list = new ArrayList<String>();
            ArrayList<String> wifi_addr_list = new ArrayList<String>();
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
                for (int i = 0; i < wl.length; i++) wifi_ap_list.add(convertToSpecChar(wl[i]));
            } else wifi_ap_list.clear();

            if (list4.length() != 0) {
                String[] al = list4.split("\t");
                for (int i = 0; i < al.length; i++) wifi_addr_list.add(convertToSpecChar(al[i]));
            } else wifi_addr_list.clear();

            SyncTaskItem stli = new SyncTaskItem(parm[1], parm[2].equals("0") ? false : true, false); //parm[1]: syncTasｋName, parm[2]: syncTaskEnabled, last SyncTaskItem param (..., false): isChecked
            if (isValidTaskItemValue(SyncTaskItem.SYNC_TASK_TYPE_LIST, parm[3])) {
                stli.setSyncTaskType(parm[3]);
            } else {
                stli.setSyncTaskType(SyncTaskItem.SYNC_TASK_TYPE_DEFAULT);
                putTaskListValueErrorMessage(util, "Sync task type", SyncTaskItem.SYNC_TASK_TYPE_DEFAULT_DESCRIPTION);
            }

            if (isValidTaskItemValue(SyncTaskItem.SYNC_FOLDER_TYPE_LIST, parm[4])) {
                stli.setMasterFolderType(parm[4]);
                if (auto_save && stli.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                    if (!parm[5].equals("") ) {
                        String dec_user=decryptByInternalPassword(util, cp_autosave, parm[5]);
                        if (dec_user!=null) stli.setMasterSmbUserName(dec_user);
                        else {
//                        stli.setMasterSmbUserName(SMBSYNC2_PROF_DECRYPT_FAILED);
                            stli.setMasterFolderError(stli.getMasterFolderError()|SyncTaskItem.SYNC_FOLDER_ERROR_ACCOUNT_NAME);
                        }
                    } else {
                        stli.setMasterSmbUserName(parm[5]);
                    }
                    if (!parm[6].equals("")) {
                        String dec_pswd = decryptByInternalPassword(util, cp_autosave, parm[6]);
                        if (dec_pswd != null) stli.setMasterSmbPassword(dec_pswd);
                        else {
//                        stli.setMasterSmbPassword(SMBSYNC2_PROF_DECRYPT_FAILED);
                            stli.setMasterFolderError(stli.getMasterFolderError()|SyncTaskItem.SYNC_FOLDER_ERROR_ACCOUNT_PASSWORD);
                        }
                    } else {
                        stli.setMasterSmbPassword(parm[6]);
                    }
                } else {
                    stli.setMasterSmbUserName(parm[5]);
                    stli.setMasterSmbPassword(parm[6]);
                }
            } else {
                stli.setMasterFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_DEFAULT);
                putTaskListValueErrorMessage(util, "Master folder type", SyncTaskItem.SYNC_FOLDER_TYPE_DEFAULT);
            }

            stli.setMasterSmbShareName(parm[7]);
            stli.setMasterDirectoryName(parm[8]);
            stli.setMasterSmbAddr(parm[9]);
            if (!parm[10].equals("")) {
                try {
                    int port_number=Integer.parseInt(parm[10]);
                    stli.setMasterSmbPort(parm[10]);
                } catch(Exception e) {
                    stli.setMasterSmbPort(SyncTaskItem.SYNC_FOLDER_SMB_PORT_DEFAULT);
                    String msg_template="Invalid \"%s\" was detected while reading the task list, so set to system determination.";
                    util.addLogMsg("W", String.format(msg_template, SyncTaskItem.SYNC_FOLDER_SMB_PORT_DEFAULT_DESCRIPTION));
                }
            }
            stli.setMasterSmbHostName(parm[11]);
            stli.setMasterSmbDomain(parm[12]);

            if (isValidTaskItemValue(SyncTaskItem.SYNC_FOLDER_TYPE_LIST, parm[13])) {
                stli.setTargetFolderType(parm[13]);
                if (auto_save && stli.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                    if (!parm[14].equals("")) {
                        String dec_user=decryptByInternalPassword(util, cp_autosave, parm[14]);
                        if (dec_user!=null) stli.setTargetSmbUserName(dec_user);
                        else {
//                        stli.setTargetSmbUserName(SMBSYNC2_PROF_DECRYPT_FAILED);
                            stli.setTargetFolderError(stli.getTargetFolderError()|SyncTaskItem.SYNC_FOLDER_ERROR_ACCOUNT_NAME);
                        }
                    } else {
                        stli.setTargetSmbUserName(parm[14]);
                    }
                    if (!parm[15].equals("")) {
                        String dec_pswd=decryptByInternalPassword(util, cp_autosave, parm[15]);
                        if (dec_pswd!=null) stli.setTargetSmbPassword(dec_pswd);
                        else {
//                        stli.setTargetSmbPassword(SMBSYNC2_PROF_DECRYPT_FAILED);
                            stli.setTargetFolderError(stli.getTargetFolderError()|SyncTaskItem.SYNC_FOLDER_ERROR_ACCOUNT_PASSWORD);
                        }
                    } else {
                        stli.setTargetSmbPassword(parm[15]);
                    }
                } else {
                    stli.setTargetSmbUserName(parm[14]);
                    stli.setTargetSmbPassword(parm[15]);
                }
            } else {
                stli.setMasterFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_DEFAULT);
                putTaskListValueErrorMessage(util, "Target folder type", SyncTaskItem.SYNC_FOLDER_TYPE_DEFAULT);
            }
            stli.setTargetSmbShareName(parm[16]);
            stli.setTargetDirectoryName(parm[17]);
            stli.setTargetSmbAddr(parm[18]);
            if (!parm[19].equals("")) {
                try {
                    int port_number=Integer.parseInt(parm[19]);
                    stli.setTargetSmbPort(parm[19]);
                } catch(Exception e) {
                    stli.setTargetSmbPort(SyncTaskItem.SYNC_FOLDER_SMB_PORT_DEFAULT);
                    String msg_template="Invalid \"%s\" was detected while reading the task list, so set to system determination.";
                    util.addLogMsg("W", String.format(msg_template, SyncTaskItem.SYNC_FOLDER_SMB_PORT_DEFAULT_DESCRIPTION));
                }
            }
            stli.setTargetSmbHostname(parm[20]);
            stli.setTargetSmbDomain(parm[21]);

            stli.setFileFilter(ff);
            stli.setDirFilter(df);
            stli.setSyncOptionWifiConnectedAccessPointWhiteList(wifi_ap_list);
            stli.setSyncOptionWifiConnectedAddressWhiteList(wifi_addr_list);

            stli.setSyncProcessRootDirFile(parm[22].equals("1") ? true : false);

            stli.setSyncOverrideCopyMoveFile(parm[23].equals("1") ? true : false);
            stli.setSyncConfirmOverrideOrDelete(parm[24].equals("1") ? true : false);

            stli.setSyncDetectLastModidiedBySmbsync(parm[25].equals("1") ? true : false);

            stli.setSyncDoNotResetFileLastModified(parm[26].equals("1") ? true : false);

//          Currently This option cannot be changed. 2020/04/10
//            stli.setSyncOptionRetryCount(parm[27]);

            stli.setSyncOptionSyncEmptyDirectory(parm[28].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenFile(parm[29].equals("1") ? true : false);
            stli.setSyncOptionSyncHiddenDirectory(parm[30].equals("1") ? true : false);

            stli.setSyncOptionSyncSubDirectory(parm[31].equals("1") ? true : false);
            stli.setSyncOptionUseSmallIoBuffer(parm[32].equals("1") ? true : false);
            stli.setSyncTestMode(parm[33].equals("1") ? true : false);
            try {
                if (isValidTaskItemValue(SyncTaskItem.SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_LIST, Integer.parseInt(parm[34]))) {
                    stli.setSyncOptionDifferentFileAllowableTime(Integer.parseInt(parm[34]));
                } else {
                    stli.setSyncOptionDifferentFileAllowableTime(SyncTaskItem.SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_DEFAULT);
                    putTaskListValueErrorMessage(util, "Min allowed time", SyncTaskItem.SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_DEFAULT);
                }
            } catch(Exception e) {
                stli.setSyncOptionDifferentFileAllowableTime(SyncTaskItem.SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_DEFAULT);
                putTaskListValueErrorMessage(util, "Min allowed time", SyncTaskItem.SYNC_FILE_DIFFERENCE_ALLOWABLE_TIME_DEFAULT);
            }
            stli.setSyncOptionDifferentFileByTime(parm[35].equals("1") ? true : false);

//            stli.setSyncUseFileCopyByTempNamex(parm[36].equals("1") ? true : false);
            try {
                if (isValidTaskItemValue(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_LIST, parm[37])) {
                    stli.setSyncOptionWifiStatusOption(parm[37]);
                } else {
                    stli.setSyncOptionWifiStatusOption(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_DEFAULT );
                    putTaskListValueErrorMessage(util, "WiFi status option", SyncTaskItem.SYNC_WIFI_STATUS_WIFI_DEFAULT_DESCRIPTION);
                }
            } catch(Exception e) {
                stli.setSyncOptionWifiStatusOption(SyncTaskItem.SYNC_WIFI_STATUS_WIFI_DEFAULT );
                putTaskListValueErrorMessage(util, "WiFi status option", SyncTaskItem.SYNC_WIFI_STATUS_WIFI_DEFAULT_DESCRIPTION);
            }

            stli.setLastSyncTime(parm[38]);
            try {
                if (isValidTaskItemValue(SyncTaskItem.SYNC_STATUS_LIST, Integer.parseInt(parm[39]))) {
                    stli.setLastSyncResult(Integer.parseInt(parm[39]));
                } else {
                    stli.setLastSyncResult(SyncTaskItem.SYNC_STATUS_DEFAULT);
                    putTaskListValueErrorMessage(util, "Last sync result", SyncTaskItem.SYNC_STATUS_DEFAULT_DESCRIPTION);
                }
            } catch(Exception e) {
                stli.setLastSyncResult(SyncTaskItem.SYNC_STATUS_DEFAULT);
                putTaskListValueErrorMessage(util, "Last sync result", SyncTaskItem.SYNC_STATUS_DEFAULT_DESCRIPTION);
            }

            try {
                if (!parm[40].equals("") && !parm[40].equals("end")) {
                    stli.setSyncTaskPosition(Integer.parseInt(parm[40]));
                }
            } catch(Exception e) {
                stli.setSyncTaskPosition(0);
                putTaskListValueErrorMessage(util, "Task list position", "0");
            }

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
            if (!parm[49].equals("") && !parm[49].equals("end")) {
                if (isValidTaskItemValue(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_LIST, parm[49])) {
                    stli.setTargetZipCompressionLevel(parm[49]);
                } else {
                    stli.setTargetZipCompressionLevel(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_DEFAULT);
                    putTaskListValueErrorMessage(util, "ZIP compression level", SyncTaskItem.ZIP_OPTION_COMP_LEVEL_DEFAULT);
                }
            }
            if (!parm[50].equals("") && !parm[50].equals("end")) {
                if (isValidTaskItemValue(SyncTaskItem.ZIP_OPTION_COMP_METGOD_LIST, parm[50])) {
                    stli.setTargetZipCompressionMethod(parm[50]);
                } else {
                    stli.setTargetZipCompressionMethod(SyncTaskItem.ZIP_OPTION_COMP_METHOD_DEFAULT);
                    putTaskListValueErrorMessage(util, "ZIP compression method", SyncTaskItem.ZIP_OPTION_COMP_METHOD_DEFAULT);
                }
            }
            if (!parm[51].equals("") && !parm[51].equals("end")) {
                if (isValidTaskItemValue(SyncTaskItem.ZIP_OPTION_ENCRYPT_LIST, parm[51])) {
                    stli.setTargetZipEncryptMethod(parm[51]);
                } else {
                    stli.setTargetZipEncryptMethod(SyncTaskItem.ZIP_OPTION_ENCRYPT_DEFAULT);
                    putTaskListValueErrorMessage(util, "ZIP encrypt method", SyncTaskItem.ZIP_OPTION_ENCRYPT_DEFAULT);
                }
            }
            if (!parm[52].equals("") && !parm[52].equals("end")) {
                if (!parm[52].equals("")) {
                    if (auto_save && stli.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                        String dec_pswd=decryptByInternalPassword(util, cp_autosave, parm[52]);
                        if (dec_pswd!=null) stli.setTargetZipPassword(dec_pswd);
                        else {
                            stli.setTargetFolderError(stli.getTargetFolderError()|SyncTaskItem.SYNC_FOLDER_ERROR_ZIP_PASSWORD);
                        }
                    } else {
                        stli.setTargetZipPassword(parm[52]);
                    }
                } else {
                    stli.setTargetZipPassword(parm[52]);
                }
            }
            if (!parm[53].equals("") && !parm[53].equals("end"))
                stli.setSyncOptionTaskSkipIfConnectAnotherWifiSsid(parm[53].equals("1") ? true : false);

            if (!parm[54].equals("") && !parm[54].equals("end"))
                stli.setSyncOptionSyncWhenCharging(parm[54].equals("1") ? true : false);

            if (!parm[55].equals("") && !parm[55].equals("end"))
                stli.setTargetZipUseExternalSdcard(parm[55].equals("1") ? true : false);

//            if (!parm[56].equals("") && !parm[56].equals("end"))
//                stli.setSyncTaskTwoWay(parm[56].equals("1") ? true : false);
//            if (!parm[57].equals("") && !parm[57].equals("end"))
//                stli.setSyncTwoWayConflictFileRule(parm[57]);

//          Currently This option cannot be changed. 2020/04/10
//            if (!parm[58].equals("") && !parm[58].equals("end"))
//                stli.setTargetZipFileNameEncoding(parm[58]);

            if (!parm[59].equals("") && !parm[59].equals("end"))
                stli.setSyncOptionDifferentFileBySize((parm[59].equals("1") ? true : false));

            if (!parm[60].equals("") && !parm[60].equals("end"))
                stli.setSyncOptionUseExtendedDirectoryFilter1((parm[60].equals("1") ? true : false));

            if (!parm[61].equals("") && !parm[61].equals("end"))
                stli.setMasterLocalMountPoint(parm[61]);

            if (!parm[62].equals("") && !parm[62].equals("end"))
                stli.setTargetLocalMountPoint(parm[62]);

            if (!parm[63].equals("") && !parm[63].equals("end")) stli.setSyncTaskGroup(parm[63]);

            if (!parm[64].equals("") && !parm[64].equals("end")) {
                if (isValidTaskItemValue(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_LIST, parm[64])) {
                    stli.setMasterSmbProtocol(parm[64]);
                } else {
                    stli.setMasterSmbProtocol(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_DEFAULT);
                    putTaskListValueErrorMessage(util, "Master SMB protocol", SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_DEFAULT_DESCRIPTION);
                }
            }

            if (!parm[65].equals("") && !parm[65].equals("end")) {
                if (isValidTaskItemValue(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_LIST, parm[65])) {
                    stli.setTargetSmbProtocol(parm[65]);
                } else {
                    stli.setTargetSmbProtocol(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_DEFAULT);
                    putTaskListValueErrorMessage(util, "Target SMB protocol", SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_DEFAULT_DESCRIPTION);
                }
            }

            if (!parm[66].equals("") && !parm[66].equals("end")) stli.setMasterSmbIpcSigningEnforced((parm[66].equals("1") ? true : false));
            if (!parm[67].equals("") && !parm[67].equals("end")) stli.setTargetSmbIpcSigningEnforced((parm[67].equals("1") ? true : false));

            if (!parm[68].equals("") && !parm[68].equals("end")) stli.setArchiveRenameFileTemplate(parm[68]);
            if (!parm[69].equals("") && !parm[69].equals("end")) stli.setArchiveUseRename((parm[69].equals("1") ? true : false));
            if (!parm[70].equals("") && !parm[70].equals("end")) {
                try {
                    if (isValidTaskItemValue(SyncTaskItem.PICTURE_ARCHIVE_RETAIN_FOR_A_LIST, Integer.parseInt(parm[70]))) {
                        stli.setArchiveRetentionPeriod(Integer.parseInt(parm[70]));
                    } else {
                        stli.setArchiveRetentionPeriod(SyncTaskItem.PICTURE_ARCHIVE_RETAIN_FOR_A_DEFAULT);
                        putTaskListValueErrorMessage(util, "Archive retention period", SyncTaskItem.PICTURE_ARCHIVE_RETAIN_FOR_A_DEFAULT_DESCRIPTION);
                    }
                } catch(Exception e) {
                    stli.setArchiveRetentionPeriod(SyncTaskItem.PICTURE_ARCHIVE_RETAIN_FOR_A_DEFAULT);
                    putTaskListValueErrorMessage(util, "Archive retention period", SyncTaskItem.PICTURE_ARCHIVE_RETAIN_FOR_A_DEFAULT_DESCRIPTION);
                }
            }

            if (!parm[71].equals("") && !parm[71].equals("end")) stli.setArchiveCreateDirectory((parm[71].equals("1") ? true : false));
            if (!parm[72].equals("") && !parm[72].equals("end")) {
                try {
                    if (isValidTaskItemValue(SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_LIST, Integer.parseInt(parm[72]))) {
                        stli.setArchiveSuffixOption(parm[72]);
                    } else {
                        stli.setArchiveSuffixOption(String.valueOf(SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_DEFAULT));
                        putTaskListValueErrorMessage(util, "Archive suffix digit", String.valueOf(SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_DEFAULT));
                    }
                } catch(Exception e) {
                    stli.setArchiveSuffixOption(String.valueOf(SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_DEFAULT));
                    putTaskListValueErrorMessage(util, "Archive suffix digit", String.valueOf(SyncTaskItem.PICTURE_ARCHIVE_SUFFIX_DIGIT_DEFAULT));
                }
            }

            if (!parm[73].equals("") && !parm[73].equals("end")) stli.setArchiveCreateDirectoryTemplate(parm[73]);
            if (!parm[74].equals("") && !parm[74].equals("end")) stli.setArchiveEnabled((parm[74].equals("1") ? true : false));

            if (!parm[75].equals("") && !parm[75].equals("end")) stli.setSyncDifferentFileSizeGreaterThanTagetFile((parm[75].equals("1") ? true : false));

            if (!parm[76].equals("") && !parm[76].equals("end")) stli.setSyncOptionDeleteFirstWhenMirror((parm[76].equals("1") ? true : false));

            if (!parm[77].equals("") && !parm[77].equals("end")) stli.setSyncOptionConfirmNotExistsExifDate((parm[77].equals("1") ? true : false));

            if (!parm[78].equals("") && !parm[78].equals("end")) stli.setTargetZipUseUsb((parm[78].equals("1") ? true : false));

            if (!parm[79].equals("") && !parm[79].equals("end")) stli.setSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile((parm[79].equals("1") ? true : false));

            if (!parm[80].equals("") && !parm[80].equals("end")) stli.setSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters((parm[80].equals("1") ? true : false));

            if (!parm[81].equals("") && !parm[81].equals("end")) stli.setSyncOptionDoNotUseRenameWhenSmbFileWrite((parm[81].equals("1") ? true : false));

            if (!parm[82].equals("") && !parm[82].equals("end")) stli.setTargetUseTakenDateTimeToDirectoryNameKeyword((parm[82].equals("1") ? true : false));

//          Currently This option cannot be changed. 2020/04/10
//            if (!parm[83].equals("") && !parm[83].equals("end")) stli.setSyncTwoWayConflictFileRule(parm[83]);
//            if (!parm[84].equals("") && !parm[84].equals("end")) stli.setSyncTwoWayKeepConflictFile((parm[84].equals("1") ? true : false));

            if (!parm[85].equals("") && !parm[85].equals("end")) stli.setMasterSmbUseSmb2Negotiation((parm[85].equals("1") ? true : false));
            if (!parm[86].equals("") && !parm[86].equals("end")) stli.setTargetSmbUseSmb2Negotiation((parm[86].equals("1") ? true : false));

            if (!parm[87].equals("") && !parm[87].equals("end")) stli.setSyncOptionSyncAllowGlobalIpAddress((parm[87].equals("1") ? true : false));

            if (!parm[88].equals("") && !parm[88].equals("end")) stli.setSyncOptionMoveOnlyRemoveMasterDirectoryIfEmpty((parm[88].equals("1") ? true : false));

            if (!parm[89].equals("") && !parm[89].equals("end"))
                try {
                    stli.setMasterFolderError(stli.getMasterFolderError()|Integer.parseInt(parm[89]));
                } catch(Exception e) {
                    stli.setMasterFolderError(stli.getMasterFolderError()|SyncTaskItem.SYNC_FOLDER_ERROR_ACCOUNT_NAME);
                    putTaskListValueErrorMessage(util, "Master folder error", "Account name");
                }
            if (!parm[90].equals("") && !parm[90].equals("end"))
                try {
                    stli.setTargetFolderError(stli.getTargetFolderError()|Integer.parseInt(parm[90]));
                } catch(Exception e) {
                    stli.setTargetFolderError(stli.getTargetFolderError()|SyncTaskItem.SYNC_FOLDER_ERROR_ACCOUNT_NAME);
                    putTaskListValueErrorMessage(util, "Target folder error", "Account name");
                }

            if (!parm[91].equals("") && !parm[91].equals("end")) stli.setSyncOptionIgnoreDstDifference((parm[91].equals("1") ? true : false));
            if (!parm[92].equals("") && !parm[92].equals("end")) {
                try {
                    if (isValidTaskItemValue(SyncTaskItem.SYNC_OPTION_OFFSET_OF_DST_LIST, Integer.valueOf(parm[92]))) {
                        stli.setSyncOptionOffsetOfDst(Integer.valueOf(parm[92]));
                    } else {
                        stli.setSyncOptionOffsetOfDst(SyncTaskItem.SYNC_OPTION_OFFSET_OF_DST_DEFAULT);
                        putTaskListValueErrorMessage(util, "Offset of DST", SyncTaskItem.SYNC_OPTION_OFFSET_OF_DST_DEFAULT);
                    }
                } catch (Exception e) {
                    stli.setSyncOptionOffsetOfDst(SyncTaskItem.SYNC_OPTION_OFFSET_OF_DST_DEFAULT);
                    putTaskListValueErrorMessage(util, "Offset of DST", SyncTaskItem.SYNC_OPTION_OFFSET_OF_DST_DEFAULT);
                }
            }

            if (!parm[93].equals("") && !parm[93].equals("end")) stli.setsyncOptionUseDirectoryFilterV2((parm[93].equals("1") ? true : false));

            if (!parm[94].equals("") && !parm[94].equals("end")) stli.setSyncOptionEnsureTargetIsExactMirror((parm[94].equals("1") ? true : false));

            if (stli.getMasterSmbProtocol().equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SYSTEM))
                stli.setMasterSmbProtocol(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1);
            if (stli.getTargetSmbProtocol().equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SYSTEM))
                stli.setTargetSmbProtocol(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1);
            sync.add(stli);
        }
    }

    private static boolean isValidTaskItemValue(String[] valid_value, String obtained_value) {
        boolean result=false;
        for(String item:valid_value) {
            if (item.equals(obtained_value)) {
                result=true;
                break;
            }
        }
        return result;
    }

    private static boolean isValidTaskItemValue(int[] valid_value, int obtained_value) {
        boolean result=false;
        for(int item:valid_value) {
            if (item==obtained_value) {
                result=true;
                break;
            }
        }
        return result;
    }

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
    public static boolean saveSyncTaskList(GlobalParameters mGp, Context c, CommonUtilities util,
                                           ArrayList<SyncTaskItem> pfl) {
        return saveSyncTaskListToFile(mGp, c, util, false, "", "", pfl, false);
    }

    public static boolean saveSyncTaskListToFile(GlobalParameters mGp, Context c, CommonUtilities util,
                                                 boolean sdcard, String fd, String fp,
                                                 ArrayList<SyncTaskItem> pfl, boolean encrypt_required) {
        return saveSyncTaskListToFileWithAutosave(mGp, c, util, sdcard, fd, fp, pfl, encrypt_required, false);
    }


    final static private int MAX_AUTOSAVE_FILE_COUNT=50;
    private static String getAutosaveDirectory(GlobalParameters mGp, CommonUtilities util) {
        return mGp.settingMgtFileDir+"/"+"autosave";
    }

    private static ArrayList<File> createAutoSaveFileList(GlobalParameters mGp, CommonUtilities util) {
        String fd=getAutosaveDirectory(mGp, util);
        File lf=new File(fd);

        File[] fl=lf.listFiles();
        ArrayList<File>as_fl=new ArrayList<File>();
        if (fl!=null) {
            for(File item:fl) {
                if (item.isFile() && item.getName().endsWith(".stf")) {
                    as_fl.add(item);
                }
            }
            Collections.sort(as_fl, new Comparator<File>(){
                @Override
                public int compare(File o1, File o2) {
                    return o1.getPath().compareToIgnoreCase(o2.getPath());
                }
            });
        }
        return as_fl;
    }
    public static boolean autosaveSyncTaskList(GlobalParameters mGp, Activity c, CommonUtilities util, CommonDialog cd,
                                               ArrayList<SyncTaskItem> pfl) {
        boolean result=false;
        if (pfl.size()==0) {
            util.addDebugMsg(1,"I","Sync task auto save was bypassed because empty sync task list");
//            result=true;
        } else {
            SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fd=getAutosaveDirectory(mGp, util);
            String fp="autosave_"+sdf.format(System.currentTimeMillis())+".stf";

            File lf=new File(fd);
            if (fd.startsWith(mGp.safMgr.getSdcardRootPath())) {
                SafFile odr=null;
                if (!lf.exists()) odr=mGp.safMgr.createSdcardDirectory(fd);
            } else if (fd.startsWith(mGp.safMgr.getUsbRootPath())) {
                SafFile odr=null;
                if (!lf.exists()) odr=mGp.safMgr.createUsbDirectory(fd);
            } else {
                if (!lf.exists()) {
                    File of=new File(fd);
                    of.mkdirs();
                }
            }

            ArrayList<File>as_fl=createAutoSaveFileList(mGp, util);

            //Delete auto save file if max count exceded
            if (as_fl.size()>MAX_AUTOSAVE_FILE_COUNT) {
                int dc=as_fl.size()-(MAX_AUTOSAVE_FILE_COUNT-1);
                for(int i=0;i<dc;i++) {
                    File ai=as_fl.get(0);
                    if (ai.getPath().startsWith(mGp.safMgr.getSdcardRootPath())) {
                        SafFile odr=mGp.safMgr.createSdcardFile(ai.getPath());
                    } else if (ai.getPath().startsWith(mGp.safMgr.getUsbRootPath())) {
                        SafFile odr=mGp.safMgr.createUsbFile(ai.getPath());
                    } else {
                        ai.delete();
                    }
                    as_fl.remove(0);
                    util.addDebugMsg(1,"I","Sync task auto save file was deleted, fp="+ai.getPath());
                }
            }

            result=saveSyncTaskListToFileWithAutosave(mGp, c, util, true, fd, fd+"/autosave_temp", pfl, false, true);
            if (result) {
                File tmp_file=new File(fd+"/autosave_temp");
                File out_file=new File(fd+"/"+fp);
                tmp_file.renameTo(out_file);
                String msg=c.getString(R.string.msgs_import_autosave_dlg_autosave_completed);
                String path_str=c.getString(R.string.msgs_import_autosave_dlg_autosave_completed_path);
                util.addLogMsg("I",msg+". "+path_str+"="+fd+"/"+fp);
                for(SyncTaskItem sti:pfl) util.addDebugMsg(1,"I","  Task="+sti.getSyncTaskName());
                CommonUtilities.showToastMessageShort(c, msg);
            } else {
                File tmp_file=new File(fd+"/autosave_temp");
                tmp_file.delete();
                util.addLogMsg("W",c.getString(R.string.msgs_import_autosave_dlg_autosave_failed));
                cd.showCommonDialog(false, "E",c.getString(R.string.msgs_import_autosave_dlg_autosave_failed),"",null);
            }
        }
        return result;
    }

    private static String encryptByInternalPassword(GlobalParameters mGp, CommonUtilities util, CipherParms cp_int, String from_str) {
        String result="";
        try {
            result= Base64Compat.encodeToString(EncryptUtil.encrypt(from_str, cp_int), Base64Compat.NO_WRAP);
        } catch(Exception e) {
            result=null;
            e.printStackTrace();
            util.addLogMsg("E", "encryptByInternalPassword failed");
            util.addLogMsg("E", e.toString());
            String stm= MiscUtil.getStackTraceString(e);
            util.addLogMsg("E", stm);
        }
        return result;
    }

    synchronized private static boolean saveSyncTaskListToFileWithAutosave(GlobalParameters mGp, Context c, CommonUtilities util,
                                                 boolean sdcard, String fd, String fp,
                                                 ArrayList<SyncTaskItem> pfl, boolean encrypt_required, boolean auto_save) {
        boolean result = true;
        String ofp = "";
        PrintWriter pw=null;
        BufferedWriter bw = null;
        try {
            CipherParms cp_sdcard = null;
            CipherParms cp_int = null;
            if (sdcard) {
                OutputStream pos=null;
                if (fd.startsWith(mGp.safMgr.getSdcardRootPath())) {
                    SafFile of=mGp.safMgr.createSdcardFile(fp);
                    pos=c.getContentResolver().openOutputStream(of.getUri());
                } else if (fd.startsWith(mGp.safMgr.getUsbRootPath())) {
                    SafFile of=mGp.safMgr.createUsbFile(fp);
                    pos=c.getContentResolver().openOutputStream(of.getUri());
                } else {
                    pos=new FileOutputStream(fp);
                }
                if (encrypt_required) {
                    cp_sdcard = EncryptUtil.initEncryptEnv(mGp.profileKeyPrefix + mGp.profilePassword);
                }
//                File lf = new File(fd);
//                if (!lf.exists()) lf.mkdir();
                bw = new BufferedWriter(new OutputStreamWriter(pos), 8192);
                pw = new PrintWriter(bw);
                ofp = fp;
                if (encrypt_required) {
                    byte[] enc_array = EncryptUtil.encrypt(SMBSYNC2_PROF_ENC, cp_sdcard);
                    String enc_str = Base64Compat.encodeToString(enc_array, Base64Compat.NO_WRAP);
//					MiscUtil.hexString("", enc_array, 0, enc_array.length);
                    pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + SMBSYNC2_PROF_ENC + enc_str);
                } else {
                    pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + SMBSYNC2_PROF_DEC);
                }
                if (auto_save) {
                    String priv_key=KeyStoreUtil.getGeneratedPasswordNewVersion(c, SMBSYNC2_KEY_STORE_ALIAS);
                    cp_int = EncryptUtil.initDecryptEnv(priv_key);
                }
            } else {
                String priv_key=KeyStoreUtil.getGeneratedPasswordNewVersion(c, SMBSYNC2_KEY_STORE_ALIAS);
                cp_int = EncryptUtil.initDecryptEnv(priv_key);

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

                    String pl_master_remote_user_id = "";
                    if (!auto_save) pl_master_remote_user_id = convertToCodeChar(item.getMasterSmbUserName());
                    else if (!item.getMasterSmbUserName().equals("")) {
                        String enc_str=encryptByInternalPassword(mGp, util, cp_int, item.getMasterSmbUserName());
                        if (enc_str!=null) pl_master_remote_user_id = enc_str;
                        else {
                            result=false;
                            break;
                        }
                    }

                    String pl_master_remote_password = "";
                    if (!auto_save) pl_master_remote_password = convertToCodeChar(item.getMasterSmbPassword());
                    else if (!item.getMasterSmbPassword().equals("")) {
                        String enc_str=encryptByInternalPassword(mGp, util, cp_int, item.getMasterSmbPassword());
                        if (enc_str!=null) pl_master_remote_password = enc_str;
                        else {
                            result=false;
                            break;
                        }
                    }

                    String pl_master_remoteSmbShare = convertToCodeChar(item.getMasterSmbShareName());
                    String pl_master_directory_name = convertToCodeChar(item.getMasterDirectoryName());
                    String pl_master_remote_addr = item.getMasterSmbAddr();
                    String pl_master_remote_port = item.getMasterSmbPort();
                    String pl_master_remote_hostname = item.getMasterSmbHostName();
//					String pl_master_use_usb_folder=item.isMasterFolderUseInternalUsbFolder()?"1":"0";

                    String pl_target_folder_type = convertToCodeChar(item.getTargetFolderType());
                    String pl_target_remote_user_id = "";
                    if (!auto_save) pl_target_remote_user_id = convertToCodeChar(item.getTargetSmbUserName());
                    else if (!item.getTargetSmbUserName().equals("")) {
                        String enc_str=encryptByInternalPassword(mGp, util, cp_int, item.getTargetSmbUserName());
                        if (enc_str!=null) pl_target_remote_user_id = enc_str;
                        else {
                            result=false;
                            break;
                        }
                    }

                    String pl_target_remote_password = "";
                    if (!auto_save) pl_target_remote_password = convertToCodeChar(item.getTargetSmbPassword());
                    else if (!item.getTargetSmbPassword().equals("")) {
                        String enc_str=encryptByInternalPassword(mGp, util, cp_int, item.getTargetSmbPassword());
                        if (enc_str!=null) pl_target_remote_password = enc_str;
                        else {
                            result=false;
                            break;
                        }
                    }

                    String pl_target_remoteSmbShare = convertToCodeChar(item.getTargetSmbShareName());
                    String pl_target_directory_name = convertToCodeChar(item.getTargetDirectoryName());
                    String pl_target_remote_addr = item.getTargetSmbAddr();
                    String pl_target_remote_port = item.getTargetSmbPort();
                    String pl_target_remote_hostname = item.getTargetSmbHostName();
//					String pl_target_use_usb_folder=item.isTargetFolderUseInternalUsbFolder()?"1":"0";

                    String pl_synctype = item.getSyncTaskType();

                    pl = "";
                    String fl = "", dl = "", wifi_wl = "", wifi_addr_list="";
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

                    for (int j = 0; j < item.getSyncOptionWifiConnectedAccessPointWhiteList().size(); j++) {
                        if (wifi_wl.length() != 0) wifi_wl += "\t";
                        if (!item.getSyncOptionWifiConnectedAccessPointWhiteList().get(j).equals(""))
                            wifi_wl += item.getSyncOptionWifiConnectedAccessPointWhiteList().get(j);
                    }
                    wifi_wl = convertToCodeChar(wifi_wl);
                    wifi_wl = "[" + wifi_wl + "]";

                    for (int j = 0; j < item.getSyncOptionWifiConnectedAddressWhiteList().size(); j++) {
                        if (wifi_addr_list.length() != 0) wifi_addr_list += "\t";
                        if (!item.getSyncOptionWifiConnectedAddressWhiteList().get(j).equals(""))
                            wifi_addr_list += item.getSyncOptionWifiConnectedAddressWhiteList().get(j);
                    }
                    wifi_addr_list = convertToCodeChar(wifi_addr_list);
                    wifi_addr_list = "[" + wifi_addr_list + "]";

                    String sync_root_dir_file_tobe_processed = item.isSyncProcessRootDirFile() ? "1" : "0";
                    String sync_process_override_delete = item.isSyncOverrideCopyMoveFile() ? "1" : "0";

                    String sync_confirm_override_delete = item.isSyncConfirmOverrideOrDelete() ? "1" : "0";
                    String sync_force_last_mod_use_smbsync = item.isSyncDetectLastModifiedBySmbsync() ? "1" : "0";
                    String sync_not_used_last_mod_for_remote = item.isSyncDoNotResetFileLastModified() ? "1" : "0";
                    String sync_retry_count = item.getSyncOptionRetryCount();
                    String sync_sync_empty_dir = item.isSyncOptionSyncEmptyDirectory() ? "1" : "0";
                    String sync_sync_hidden_file = item.isSyncOptionSyncHiddenFile() ? "1" : "0";
                    String sync_sync_hidden_dir = item.isSyncOptionSyncHiddenDirectory() ? "1" : "0";
                    String sync_sync_sub_dir = item.isSyncOptionSyncSubDirectory() ? "1" : "0";
                    String sync_use_small_io_buf = item.isSyncOptionUseSmallIoBuffer() ? "1" : "0";

                    String sync_sync_test_mode = item.isSyncTestMode() ? "1" : "0";
                    String sync_file_copy_by_diff_file = String.valueOf(item.getSyncOptionDifferentFileAllowableTime());
                    String sync_sync_diff_file_by_file_size = item.isSyncOptionDifferentFileBySize() ? "1" : "0";
                    String sync_sync_diff_file_by_last_mod = item.isSyncOptionDifferentFileByTime() ? "1" : "0";

//                    String sync_sync_use_file_copy_by_temp_name = "0";//item.isSyncUseFileCopyByTempNamex() ? "1" : "0";
                    String sync_sync_wifi_status_option = item.getSyncOptionWifiStatusOption();

                    String sync_result_last_time = item.getLastSyncTime();
                    String sync_result_last_status = String.valueOf(item.getLastSyncResult());

                    String sync_pos = String.valueOf(item.getSyncTaskPosition());

                    String sync_file_type_audio = item.isSyncFileTypeAudio() ? "1" : "0";
                    String sync_file_type_image = item.isSyncFileTypeImage() ? "1" : "0";
                    String sync_file_type_video = item.isSyncFileTypeVideo() ? "1" : "0";

                    String sync_use_ext_dir_filter1 = item.isSyncOptionUseExtendedDirectoryFilter1() ? "1" : "0"; //60

                    String zip_password=item.getTargetZipPassword();
                    if (!auto_save) zip_password=item.getTargetZipPassword();
                    else if (!item.getTargetZipPassword().equals("")) {
                        String enc_str=encryptByInternalPassword(mGp, util, cp_int, item.getTargetZipPassword());
                        if (enc_str!=null) zip_password = enc_str;
                        else {
                            result=false;
                            break;
                        }
                    }

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
                            wifi_addr_list + "\t" +

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
                            sync_file_copy_by_diff_file + "\t" +    //34
                            sync_sync_diff_file_by_last_mod + "\t" +//35

                            "-" + "\t" +                            //36
                            sync_sync_wifi_status_option + "\t" +   //37

                            sync_result_last_time + "\t" +                                      //38
                            sync_result_last_status + "\t" +                                    //39

                            sync_pos + "\t" +                                                   //40

                            "-" + "\t" +                                                        //41
                            "-" + "\t" +                                                        //42

                            item.getMasterRemovableStorageID() + "\t" +//43
                            item.getTargetRemovableStorageID() + "\t" +//44

                            sync_file_type_audio + "\t" +                                       //45
                            sync_file_type_image + "\t" +                                       //46
                            sync_file_type_video + "\t" +                                       //47

                            item.getTargetZipOutputFileName() + "\t" +                          //48
                            item.getTargetZipCompressionLevel() + "\t" +                        //49
                            item.getTargetZipCompressionMethod() + "\t" +                       //50
                            item.getTargetZipEncryptMethod() + "\t" +                           //51
                            zip_password + "\t" +                                               //52
                            (item.isSyncOptionTaskSkipIfConnectAnotherWifiSsid() ? "1" : "0") + "\t" +//53

                            (item.isSyncOptionSyncWhenCharging() ? "1" : "0") + "\t" +          //54

                            (item.isTargetZipUseExternalSdcard() ? "1" : "0") + "\t" +          //55

                            "-" + "\t" +                                                        //56 Dummy
                            "-" + "\t" +                                                         //57 Dummy

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

                            (item.getArchiveRenameFileTemplate()) + "\t" +                      //68
                            (item.isArchiveUseRename() ? "1" : "0") + "\t" +            //69
                            (item.getArchiveRetentionPeriod()) + "\t" +                         //70
                            (item.isArchiveCreateDirectory() ? "1" : "0") + "\t" +              //71
                            (item.getArchiveSuffixOption()) + "\t" +                            //72
                            (item.getArchiveCreateDirectoryTemplate()) + "\t" +                 //73
                            (item.isArchiveEnabled() ? "1" : "0") + "\t" +                      //74

                            (item.isSyncDifferentFileSizeGreaterThanTagetFile() ? "1" : "0") + "\t" + //75

                            (item.isSyncOptionDeleteFirstWhenMirror() ? "1" : "0") + "\t" +     //76

                            (item.isSyncOptionConfirmNotExistsExifDate() ? "1" : "0") + "\t" +      //77

                            (item.isTargetZipUseUsb() ? "1" : "0") + "\t" +                         //78

                            (item.isSyncOptionNeverOverwriteTargetFileIfItIsNewerThanTheMasterFile() ? "1" : "0") + "\t" +     //79

                            (item.isSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters() ? "1" : "0") + "\t" +     //80

                            (item.isSyncOptionDoNotUseRenameWhenSmbFileWrite() ? "1" : "0") + "\t" +     //81

                            (item.isTargetUseTakenDateTimeToDirectoryNameKeyword() ? "1" : "0") + "\t" +     //82

                            item.getSyncTwoWayConflictFileRule() + "\t" +                           //83
                            (item.isSyncTwoWayKeepConflictFile() ? "1" : "0") + "\t" +              //84

                            (item.isMasterSmbUseSmb2Negotiation() ? "1" : "0") + "\t" +             //85
                            (item.isTargetSmbUseSmb2Negotiation() ? "1" : "0") + "\t" +             //86

                            (item.isSyncOptionSyncAllowGlobalIpAddress() ? "1" : "0") + "\t" +      //87

                            (item.isSyncOptionMoveOnlyRemoveMasterDirectoryIfEmpty() ? "1" : "0") + "\t" +   //88

                            (item.getMasterFolderError()) + "\t" +                                      //89
                            (item.getTargetFolderError()) + "\t" +                                      //90

                            (item.isSyncOptionIgnoreDstDifference() ? "1" : "0") + "\t" +               //91
                            item.getSyncOptionOffsetOfDst()+ "\t" +                                     //92

                            (item.isSyncOptionUseDirectoryFilterV2() ? "1" : "0") + "\t" +             //93
                            
                            (item.isSyncOptionEnsureTargetIsExactMirror() ? "1" : "0") + "\t" +     //94

                            "end"
                    ;

                    if (sdcard) {
                        if (encrypt_required) {
                            String enc = Base64Compat.encodeToString(EncryptUtil.encrypt(pl, cp_sdcard), Base64Compat.NO_WRAP);
                            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + enc);
                        } else {
                            pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + pl);
                        }
                    } else {
                        String enc = Base64Compat.encodeToString(EncryptUtil.encrypt(pl, cp_int), Base64Compat.NO_WRAP);
                        pw.println(CURRENT_SMBSYNC2_PROFILE_VERSION + enc);
                    }

                }
                if (sdcard && result) {
                    saveSettingsParmsToFile(c, pw, encrypt_required, cp_sdcard);
                }
            }
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
            util.addLogMsg("E", String.format(c.getString(R.string.msgs_save_to_profile_error), ofp));
            util.addLogMsg("E", e.toString());
            String stm= MiscUtil.getStackTraceString(e);
            util.addLogMsg("E", stm);
            result = false;
            if (pw!=null) {
                pw.flush();
                pw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            util.addLogMsg("E", "Sync task list encryption failed");
            util.addLogMsg("E", e.toString());
            String stm= MiscUtil.getStackTraceString(e);
            util.addLogMsg("E", stm);
            result = false;
            if (pw!=null) {
                pw.flush();
                pw.close();
            }
        }

        return result;
    }

    static private void addImportSettingsParm(String pl, ArrayList<PreferenceParmListIItem> ispl) {
        String tmp_ps = pl;//pl.substring(7,pl.length());
        String[] tmp_pl = tmp_ps.split("\t");// {"type","name","active",options...};
        if (tmp_pl[0] != null && tmp_pl.length >= 3 && tmp_pl[0].equals(SMBSYNC2_PROF_TYPE_SETTINGS)) {
            PreferenceParmListIItem ppli = new PreferenceParmListIItem();
            if (tmp_pl[1] != null) ppli.parms_key = tmp_pl[1];
            if (tmp_pl[2] != null) ppli.parms_type = tmp_pl[2];
            if (tmp_pl.length >= 4 && tmp_pl[3] != null) ppli.parms_value = tmp_pl[3];
            if (!ppli.parms_key.equals("") && !ppli.parms_type.equals("")) {
                ispl.add(ppli);
            }
        }
    }

    public static void saveSettingsParmsToFile(Context c, PrintWriter pw, boolean encrypt_required, final CipherParms cp) {
        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_error_option));
        saveSettingsParmsToFileBoolean(c, pw, true,     encrypt_required, cp, c.getString(R.string.settings_wifi_lock));
        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_sync_history_log));
        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_force_screen_on_while_sync));
        saveSettingsParmsToFileString(c, pw, DEFAULT_NOCOMPRESS_FILE_TYPE,
                                                        encrypt_required, cp, c.getString(R.string.settings_no_compress_file_type));
        saveSettingsParmsToFileString(c, pw, "",        encrypt_required, cp, c.getString(R.string.settings_mgt_dir));

        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_suppress_warning_app_specific_dir));
        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_suppress_warning_location_service_disabled));

        saveSettingsParmsToFileString(c, pw, "1",       encrypt_required, cp, c.getString(R.string.settings_notification_message_when_sync_ended));
        saveSettingsParmsToFileString(c, pw, "0",       encrypt_required, cp, c.getString(R.string.settings_playback_ringtone_when_sync_ended));
        saveSettingsParmsToFileInt(c, pw, 100,          encrypt_required, cp, c.getString(R.string.settings_playback_ringtone_volume));
        saveSettingsParmsToFileString(c, pw, "0",       encrypt_required, cp, c.getString(R.string.settings_vibrate_when_sync_ended));
        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_device_orientation_portrait));
        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_device_orientation_landscape_tablet));
        saveSettingsParmsToFileString(c, pw, "0",       encrypt_required, cp, c.getString(R.string.settings_screen_theme_language));
        saveSettingsParmsToFileString(c, pw, "0",       encrypt_required, cp, c.getString(R.string.settings_screen_theme));
        saveSettingsParmsToFileBoolean(c, pw, true,     encrypt_required, cp, c.getString(R.string.settings_dim_screen_on_while_sync));

        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_log_option));
        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_put_logcat_option));
        saveSettingsParmsToFileString(c, pw, "0",       encrypt_required, cp, c.getString(R.string.settings_log_level));
        saveSettingsParmsToFileString(c, pw, "10",      encrypt_required, cp, c.getString(R.string.settings_log_file_max_count));

        saveSettingsParmsToFileString(c, pw, "0",       encrypt_required, cp, c.getString(R.string.settings_smb_lm_compatibility));
        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_smb_use_extended_security));
        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_smb_disable_plain_text_passwords));
        saveSettingsParmsToFileString(c, pw, "30000",   encrypt_required, cp, c.getString(R.string.settings_smb_client_response_timeout));

        saveSettingsParmsToFileString(c, pw, "-1",      encrypt_required, cp, SCHEDULER_SCHEDULE_SAVED_DATA_V5);

        saveSettingsParmsToFileBoolean(c, pw, false,    encrypt_required, cp, c.getString(R.string.settings_exported_profile_encryption));

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

    static private class PreferenceParmListIItem {
        public String parms_key = "";
        public String parms_type = "";
        public String parms_value = "";
    }

}

