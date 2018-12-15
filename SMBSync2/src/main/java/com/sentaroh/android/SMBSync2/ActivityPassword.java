package com.sentaroh.android.SMBSync2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sentaroh.android.Utilities.Base64Compat;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.EncryptUtil;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.ThemeUtil;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_KEY_STORE_ALIAS;

public class ActivityPassword extends AppCompatActivity {
    private Context mContext=null;
    private GlobalParameters mGp=null;
    private Activity mActivity=null;
    private CommonUtilities mUtil = null;
    private CommonDialog commonDlg = null;

    private LinearLayout mPreferenceView=null;

    private TextView mAppPswdMsg=null;
    private Button mButtonCreate=null;
    private Button mButtonChange=null;
    private Button mButtonRemove=null;
    private Button mButtonClose=null;

    private String mCreatedPassword="";

    private CheckedTextView mCtvSettingTimeOut=null;
    private CheckedTextView mCtvSettingAppStartup=null;
    private CheckedTextView mCtvSettingUseEditTask=null;
    private CheckedTextView mCtvSettingUseExportTask=null;
    private CheckedTextView mCtvSettingInitSmbAccount=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = getApplicationContext();
        mGp= GlobalWorkArea.getGlobalParameters(mContext);
        mActivity = this;
        if (mGp.themeColorList == null) {
            mGp.themeColorList = ThemeUtil.getThemeColorList(this);
        }
        setTheme(mGp.applicationTheme);

        //Remove notification bar
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.preference_application_password_dlg);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mUtil = new CommonUtilities(this.getApplicationContext(), "AppPswd", mGp);

        commonDlg = new CommonDialog(this, getSupportFragmentManager());
        setResult(RESULT_OK);

        mPreferenceView=(LinearLayout)findViewById(R.id.preference_application_password_dlg_view);

        mAppPswdMsg=(TextView)findViewById(R.id.preference_application_password_password_status);

        mButtonCreate=(Button)findViewById(R.id.preference_application_password_create_button);
        mButtonChange=(Button)findViewById(R.id.preference_application_password_change_button);
        mButtonRemove=(Button)findViewById(R.id.preference_application_password_remove_button);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        mCtvSettingTimeOut=(CheckedTextView)findViewById(R.id.preference_application_password_setting_time_out);
        mCtvSettingAppStartup=(CheckedTextView)findViewById(R.id.preference_application_password_setting_use_app_startup);
        mCtvSettingUseEditTask=(CheckedTextView)findViewById(R.id.preference_application_password_setting_use_edit_task);
        mCtvSettingUseExportTask=(CheckedTextView)findViewById(R.id.preference_application_password_setting_use_export_task);
        mCtvSettingInitSmbAccount=(CheckedTextView)findViewById(R.id.preference_application_password_setting_init_smb_account_password);

        mPreferenceView.setVisibility(LinearLayout.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NotifyEvent ntfy_auth=new NotifyEvent(mContext);
        ntfy_auth.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                mPreferenceView.setVisibility(LinearLayout.VISIBLE);
                setAppPswdStatus();
                setPasswordButtonListener();
                setProtectItemButtonListener();
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        applicationPasswordAuthentication(mGp, mActivity, getSupportFragmentManager(), mUtil, true, ntfy_auth);

    }

    private void setProtectItemButtonListener() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mCtvSettingTimeOut.setChecked(prefs.getBoolean(mContext.getString(R.string.settings_security_use_auth_timeout), true));
        mCtvSettingTimeOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                prefs.edit().putBoolean(mContext.getString(R.string.settings_security_use_auth_timeout), isChecked).commit();
            }
        });
        mCtvSettingAppStartup.setChecked(prefs.getBoolean(mContext.getString(R.string.settings_security_application_password_use_app_startup), false));
        mCtvSettingAppStartup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                prefs.edit().putBoolean(mContext.getString(R.string.settings_security_application_password_use_app_startup), isChecked).commit();
            }
        });
        mCtvSettingUseEditTask.setChecked(prefs.getBoolean(mContext.getString(R.string.settings_security_application_password_use_edit_task), false));
        mCtvSettingUseEditTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                prefs.edit().putBoolean(mContext.getString(R.string.settings_security_application_password_use_edit_task), isChecked).commit();
            }
        });
        mCtvSettingUseExportTask.setChecked(prefs.getBoolean(mContext.getString(R.string.settings_security_application_password_use_export_task), false));
        mCtvSettingUseExportTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                prefs.edit().putBoolean(mContext.getString(R.string.settings_security_application_password_use_export_task), isChecked).commit();
            }
        });
        mCtvSettingInitSmbAccount.setChecked(prefs.getBoolean(mContext.getString(R.string.settings_security_init_smb_account_password), false));
        mCtvSettingInitSmbAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !((CheckedTextView) v).isChecked();
                ((CheckedTextView) v).setChecked(isChecked);
                prefs.edit().putBoolean(mContext.getString(R.string.settings_security_init_smb_account_password), isChecked).commit();
            }
        });
    }

    private void setPasswordButtonListener() {
        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ntfy_create=new NotifyEvent(mContext);
                ntfy_create.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        String new_hv=(String)objects[0];
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                        saveApplicationPasswordHashValue(prefs, new_hv);
                        setAppPswdStatus();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                        setAppPswdStatus();
                    }
                });
                createApplicationPassword(mGp, mActivity, getSupportFragmentManager(), mUtil,
                        mContext.getString(R.string.settings_security_application_password_desc_create), ntfy_create);
            }
        });

        mButtonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ntfy_verify=new NotifyEvent(mContext);
                ntfy_verify.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        NotifyEvent ntfy_create=new NotifyEvent(mContext);
                        ntfy_create.setListener(new NotifyEvent.NotifyEventListener() {
                            @Override
                            public void positiveResponse(Context context, Object[] objects) {
                                String new_hv=(String)objects[0];
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                                saveApplicationPasswordHashValue(prefs, new_hv);
                            }

                            @Override
                            public void negativeResponse(Context context, Object[] objects) {
                                setAppPswdStatus();
                            }
                        });
                        createApplicationPassword(mGp, mActivity, getSupportFragmentManager(), mUtil,
                                mContext.getString(R.string.settings_security_application_password_desc_change), ntfy_create);
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {}
                });
                ntfy_verify.notifyToListener(true, null);
//                applicationPasswordAuthentication(mGp, mActivity, getSupportFragmentManager(), mUtil, true, ntfy_verify);
            }
        });

        mButtonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotifyEvent ntfy_verify=new NotifyEvent(mContext);
                ntfy_verify.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {

                        NotifyEvent ntfy_confirm=new NotifyEvent(mContext);
                        ntfy_confirm.setListener(new NotifyEvent.NotifyEventListener() {
                            @Override
                            public void positiveResponse(Context context, Object[] objects) {
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                                saveApplicationPasswordHashValue(prefs, "");
                                setAppPswdStatus();
                            }

                            @Override
                            public void negativeResponse(Context context, Object[] objects) {}
                        });
                        commonDlg.showCommonDialog(true, "W",
                                mContext.getString(R.string.settings_security_application_password_confirm_remove), "", ntfy_confirm);

                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {}
                });
                ntfy_verify.notifyToListener(true, null);
//                applicationPasswordAuthentication(mGp, mActivity, getSupportFragmentManager(), mUtil, true, ntfy_verify);
            }
        });

    }

    private static final String SMBSYNC2_APPLICATION_PASSWORD_HASH_VALUE= "settings_application_password_hash_value";
    static public String getApplicationPasswordHashValue(SharedPreferences prefs) {
        return prefs.getString(SMBSYNC2_APPLICATION_PASSWORD_HASH_VALUE, "");
    }

    public void saveApplicationPasswordHashValue(SharedPreferences prefs, String hv) {
        saveApplicationPasswordHashValue(mGp, prefs,  hv) ;
    }

    static public void saveApplicationPasswordHashValue(GlobalParameters mGp, SharedPreferences prefs, String hv) {
        prefs.edit().putString(SMBSYNC2_APPLICATION_PASSWORD_HASH_VALUE, hv).commit();
        mGp.settingSecurityApplicationPasswordHashValue=hv;
    }

    private void setAppPswdStatus() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String hv= getApplicationPasswordHashValue(prefs);

        if (hv.equals("")) {
            mAppPswdMsg.setText(mContext.getString(R.string.settings_security_application_password_not_created));
            mButtonCreate.setEnabled(true);
            mButtonChange.setEnabled(false);
            mButtonRemove.setEnabled(false);

            mCtvSettingTimeOut.setEnabled(false);
            mCtvSettingAppStartup.setEnabled(false);
            mCtvSettingUseEditTask.setEnabled(false);
            mCtvSettingUseExportTask.setEnabled(false);
            mCtvSettingInitSmbAccount.setEnabled(false);
        } else {
            mAppPswdMsg.setText(mContext.getString(R.string.settings_security_application_password_created));
            mButtonCreate.setEnabled(false);
            mButtonChange.setEnabled(true);
            mButtonRemove.setEnabled(true);

            mCtvSettingTimeOut.setEnabled(true);
            mCtvSettingAppStartup.setEnabled(true);
            mCtvSettingUseEditTask.setEnabled(true);
            mCtvSettingUseExportTask.setEnabled(true);
            mCtvSettingInitSmbAccount.setEnabled(true);
        }
    }

    static public void applicationPasswordAuthentication(final GlobalParameters mGp, final Activity mActivity, final FragmentManager fm,
                                                         final CommonUtilities mUtil, boolean force_auth, final NotifyEvent notify_check) {
        boolean auth_ok=false;
        if (mGp.settingSecurityApplicationPasswordHashValue.equals("")) {
            if (notify_check!=null) notify_check.notifyToListener(true,null);
            return;
        }
        if (!force_auth) {
            if (mGp.appPasswordAuthValidated) {
                if ((mGp.appPasswordAuthLastTime+30*60*1000)>System.currentTimeMillis()) {
                    if (notify_check!=null) notify_check.notifyToListener(true,null);
                    return;
                }
                mGp.appPasswordAuthValidated=false;
            }
        }

        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_input_dlg);

        final TextView tv_title=(TextView)dialog.findViewById(R.id.password_input_title);
        tv_title.setText(mGp.appContext.getString(R.string.msgs_security_application_password_auth_title));
        final TextView tv_msg=(TextView)dialog.findViewById(R.id.password_input_msg);
        tv_msg.setText(mGp.appContext.getString(R.string.msgs_security_application_password_auth_specify_password));
        final CheckedTextView ctv_prot=(CheckedTextView)dialog.findViewById(R.id.password_input_ctv_protect);
        ctv_prot.setVisibility(CheckedTextView.GONE);
        final EditText et_pswd1=(EditText)dialog.findViewById(R.id.password_input_password);
        final EditText et_pswd2=(EditText)dialog.findViewById(R.id.password_input_password_confirm);
        et_pswd2.setVisibility(EditText.GONE);

        final TextView tv_warn=(TextView)dialog.findViewById(R.id.password_input_warning_msg);
        tv_warn.setVisibility(TextView.VISIBLE);
        tv_warn.setText(mGp.appContext.getString(R.string.msgs_security_application_password_create_forget_recovery));

        final Button btn_ok=(Button)dialog.findViewById(R.id.password_input_ok_btn);
        final Button btn_cancel=(Button)dialog.findViewById(R.id.password_input_cancel_btn);

        CommonDialog.setDlgBoxSizeCompact(dialog);

        btn_ok.setEnabled(false);
        et_pswd1.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (et_pswd1.length()>0) btn_ok.setEnabled(true);
                else btn_ok.setEnabled(false);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notify_check!=null) notify_check.notifyToListener(false,null);
                dialog.dismiss();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String decrypted_hv="";
                String input_hv="";
                try {
                    input_hv=EncryptUtil.makeSHA1Hash(et_pswd1.getText().toString());
                    String enc_password=KeyStoreUtil.getGeneratedPassword(mGp.appContext, SMBSYNC2_KEY_STORE_ALIAS);
                    EncryptUtil.CipherParms cp_int = EncryptUtil.initDecryptEnv(enc_password);
                    byte[] encrypted_hv=Base64Compat.decode(mGp.settingSecurityApplicationPasswordHashValue, Base64Compat.NO_WRAP);
                    decrypted_hv =EncryptUtil.decrypt(encrypted_hv, cp_int);
                } catch (Exception e) {
                    e.printStackTrace();
                    final StringWriter sw = new StringWriter();
                    final PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    pw.flush();
                    pw.close();

                    CommonDialog cd=new CommonDialog(mGp.appContext, fm);
                    cd.showCommonDialog(false, "E","Application password authentication error",sw.toString(), null);
                    mUtil.addLogMsg("E","Application password authentication error");
                    mUtil.addLogMsg("E",sw.toString());
                }
                if (decrypted_hv.equals(input_hv)) {
                    mGp.appPasswordAuthValidated=true;
                    mGp.appPasswordAuthLastTime=System.currentTimeMillis();
                    dialog.dismiss();
                    if (notify_check!=null) notify_check.notifyToListener(true,null);
                } else {
                    tv_msg.setText(mGp.appContext.getString(R.string.msgs_security_application_password_auth_wrong_password_specified));
                }
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

    static private void createApplicationPassword(final GlobalParameters mGp, final Activity mActivity, final FragmentManager fm,
                                           final CommonUtilities mUtil, final String title, final NotifyEvent ntfy_create) {
        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_input_dlg);

        final TextView tv_title=(TextView)dialog.findViewById(R.id.password_input_title);
        tv_title.setText(title);
        final TextView tv_msg=(TextView)dialog.findViewById(R.id.password_input_msg);
        tv_msg.setText(mGp.appContext.getString(R.string.msgs_security_application_password_auth_specify_password));
        final CheckedTextView ctv_prot=(CheckedTextView)dialog.findViewById(R.id.password_input_ctv_protect);
        ctv_prot.setVisibility(CheckedTextView.GONE);
        final EditText et_pswd1=(EditText)dialog.findViewById(R.id.password_input_password);
        final EditText et_pswd2=(EditText)dialog.findViewById(R.id.password_input_password_confirm);

        final TextView tv_warn=(TextView)dialog.findViewById(R.id.password_input_warning_msg);
        tv_warn.setVisibility(TextView.VISIBLE);
        tv_warn.setText(mGp.appContext.getString(R.string.msgs_security_application_password_create_forget_warning));

        final Button btn_ok=(Button)dialog.findViewById(R.id.password_input_ok_btn);
        final Button btn_cancel=(Button)dialog.findViewById(R.id.password_input_cancel_btn);

        CommonDialog.setDlgBoxSizeLimit(dialog, true);

//        tv_title.setText("Application startup password");

        et_pswd1.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateNewPasswordSpec(mGp, btn_ok, et_pswd1, et_pswd2, tv_msg);
            }
        });
        et_pswd2.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateNewPasswordSpec(mGp, btn_ok, et_pswd1, et_pswd2, tv_msg);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ntfy_create.notifyToListener(false, null);
                dialog.dismiss();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String encrypted_hv="";
                try {
                    String user_pw_hv=EncryptUtil.makeSHA1Hash(et_pswd1.getText().toString());
                    String enc_password=KeyStoreUtil.getGeneratedPassword(mGp.appContext, SMBSYNC2_KEY_STORE_ALIAS);
                    EncryptUtil.CipherParms cp_int = EncryptUtil.initDecryptEnv(enc_password);
                    encrypted_hv=Base64Compat.encodeToString(EncryptUtil.encrypt(user_pw_hv, cp_int), Base64Compat.NO_WRAP);
                    ntfy_create.notifyToListener(true, new Object[]{encrypted_hv});
                } catch (Exception e) {
                    e.printStackTrace();
                    final StringWriter sw = new StringWriter();
                    final PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    pw.flush();
                    pw.close();

                    CommonDialog cd=new CommonDialog(mGp.appContext, fm);
                    cd.showCommonDialog(false,"E","Application password creation error",sw.toString(), null);

                    mUtil.addLogMsg("E","Application password creation error");
                    mUtil.addLogMsg("E",sw.toString());

                    ntfy_create.notifyToListener(false, null);
                }
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

    static private void validateNewPasswordSpec(GlobalParameters mGp, Button btn_ok, EditText et_pswd1, EditText et_pswd2, TextView msg) {
        btn_ok.setEnabled(false);
        if (et_pswd1.getText().length()>=4) {
            if (et_pswd1.getText().length()>0 && et_pswd2.getText().length()>0) {
                if (et_pswd1.getText().toString().equals(et_pswd2.getText().toString())) {
                    btn_ok.setEnabled(true);
                    msg.setText(mGp.appContext.getString(com.sentaroh.android.Utilities.R.string.msgs_password_input_preference_match));
                } else {
                    msg.setText(mGp.appContext.getString(com.sentaroh.android.Utilities.R.string.msgs_password_input_preference_unmatch));
                }
            } else {
                if (et_pswd1.getText().length()==0) {
                    msg.setText(mGp.appContext.getString(com.sentaroh.android.Utilities.R.string.msgs_password_input_preference_new_not_specified));
                } else {
                    msg.setText(mGp.appContext.getString(com.sentaroh.android.Utilities.R.string.msgs_password_input_preference_conf_not_specified));
                }
            }
        } else {
            msg.setText(mGp.appContext.getString(R.string.msgs_security_application_password_create_min_length_is_4_digit));
        }
    }


}