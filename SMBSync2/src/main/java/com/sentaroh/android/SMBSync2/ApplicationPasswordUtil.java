package com.sentaroh.android.SMBSync2;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.sentaroh.android.Utilities.Base64Compat;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.EncryptUtil;
import com.sentaroh.android.Utilities.NotifyEvent;

import java.io.PrintWriter;
import java.io.StringWriter;

import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_KEY_STORE_ALIAS;

public class ApplicationPasswordUtil {

    final static private long SMBSYNC2_APPLICATION_PASSWORD_VALIDITY_PERIOD =30*60*1000;

    private static final String SMBSYNC2_APPLICATION_PASSWORD_HASH_VALUE= "settings_application_password_hash_value";
    static public String getApplicationPasswordHashValue(SharedPreferences prefs) {
        return prefs.getString(SMBSYNC2_APPLICATION_PASSWORD_HASH_VALUE, "");
    }

    static public void saveApplicationPasswordHashValue(GlobalParameters gp, SharedPreferences prefs, String hv) {
        prefs.edit().putString(SMBSYNC2_APPLICATION_PASSWORD_HASH_VALUE, hv).commit();
        gp.settingSecurityApplicationPasswordHashValue=hv;
    }

    public final static int APPLICATION_PASSWORD_RESOURCE_EXPORT_TASK_LIST=1;
    public final static int APPLICATION_PASSWORD_RESOURCE_START_APPLICATION=2;
    public final static int APPLICATION_PASSWORD_RESOURCE_EDIT_SYNC_TASK=3;
    public final static int APPLICATION_PASSWORD_RESOURCE_INVOKE_SECURITY_SETTINGS =4;

    static private boolean isAuthRequired(final GlobalParameters gp, int resource_id) {
        boolean result=false;
        if (!gp.settingSecurityApplicationPasswordHashValue.equals("")) {
            switch (resource_id) {
                case APPLICATION_PASSWORD_RESOURCE_EXPORT_TASK_LIST:
                    if (gp.settingSecurityApplicationPasswordUseExport) result=true;
                    break;
                case APPLICATION_PASSWORD_RESOURCE_START_APPLICATION:
                    if (gp.settingSecurityApplicationPasswordUseAppStartup) result=true;
                    break;
                case APPLICATION_PASSWORD_RESOURCE_EDIT_SYNC_TASK:
                    if (gp.settingSecurityApplicationPasswordUseEditTask) result=true;
                    break;
                case APPLICATION_PASSWORD_RESOURCE_INVOKE_SECURITY_SETTINGS:
                    result=true;
                    break;
                default :
                    break;
            }
        }
        return result;
    }

    static public void applicationPasswordAuthentication(final GlobalParameters gp, final Activity mActivity, final FragmentManager fm,
                                                         final CommonUtilities mUtil, boolean force_auth, final NotifyEvent notify_check, int resource_id) {
        if (!isAuthRequired(gp, resource_id)) {
            notify_check.notifyToListener(true,null);
            return;
        }

//        boolean auth_ok=false;
//        if (gp.settingSecurityApplicationPasswordHashValue.equals("")) {
//            if (notify_check!=null) notify_check.notifyToListener(true,null);
//            return;
//        }
        if (!force_auth) {
            if (gp.appPasswordAuthValidated) {
                if ((gp.appPasswordAuthLastTime + SMBSYNC2_APPLICATION_PASSWORD_VALIDITY_PERIOD)>System.currentTimeMillis()) {
                    if (notify_check!=null) notify_check.notifyToListener(true,null);
                    return;
                }
                gp.appPasswordAuthValidated=false;
            }
        }

        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_input_dlg);

        final TextView tv_title=(TextView)dialog.findViewById(R.id.password_input_title);
        tv_title.setText(gp.appContext.getString(R.string.msgs_security_application_password_auth_title));
        final TextView tv_msg=(TextView)dialog.findViewById(R.id.password_input_msg);
        tv_msg.setText(gp.appContext.getString(R.string.msgs_security_application_password_auth_specify_password));
        final CheckedTextView ctv_prot=(CheckedTextView)dialog.findViewById(R.id.password_input_ctv_protect);
        ctv_prot.setVisibility(CheckedTextView.GONE);
        final EditText et_pswd1=(EditText)dialog.findViewById(R.id.password_input_password);
        final EditText et_pswd2=(EditText)dialog.findViewById(R.id.password_input_password_confirm);
        et_pswd2.setVisibility(EditText.GONE);

        final TextView tv_warn=(TextView)dialog.findViewById(R.id.password_input_warning_msg);
        tv_warn.setVisibility(TextView.VISIBLE);
        tv_warn.setText(gp.appContext.getString(R.string.msgs_security_application_password_create_forget_recovery));

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
                btn_ok.setEnabled(false);
                final Handler hndl=new Handler();
                String decrypted_hv="";
                String input_hv="";
                try {
                    input_hv=EncryptUtil.makeSHA1Hash(et_pswd1.getText().toString());
                    String enc_password=null;
                    if ( Build.VERSION.SDK_INT>=28) enc_password=KeyStoreUtil.getGeneratedPasswordNewVersion(gp.appContext, SMBSYNC2_KEY_STORE_ALIAS);
                    else enc_password=KeyStoreUtil.getGeneratedPasswordOldVersion(gp.appContext, SMBSYNC2_KEY_STORE_ALIAS);

                    EncryptUtil.CipherParms cp_int = EncryptUtil.initDecryptEnv(enc_password);
                    byte[] encrypted_hv=Base64Compat.decode(gp.settingSecurityApplicationPasswordHashValue, Base64Compat.NO_WRAP);
                    decrypted_hv =EncryptUtil.decrypt(encrypted_hv, cp_int);
                } catch (Exception e) {
                    e.printStackTrace();
                    final StringWriter sw = new StringWriter();
                    final PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    pw.flush();
                    pw.close();

                    CommonDialog cd=new CommonDialog(gp.appContext, fm);
                    cd.showCommonDialog(false, "E","Application password authentication error",sw.toString(), null);
                    mUtil.addLogMsg("E","Application password authentication error");
                    mUtil.addLogMsg("E",sw.toString());
                }
                if (decrypted_hv.equals(input_hv)) {
                    gp.appPasswordAuthValidated=true;
                    gp.appPasswordAuthLastTime=System.currentTimeMillis();
                    dialog.dismiss();
                    if (notify_check!=null) notify_check.notifyToListener(true,null);
                } else {
                    tv_msg.setText(gp.appContext.getString(R.string.msgs_security_application_password_auth_wrong_password_specified));
                    hndl.postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            btn_ok.setEnabled(true);
                        }
                    },1000);
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

    static public void createApplicationPassword(final GlobalParameters gp, final Activity mActivity, final FragmentManager fm,
                                                  final CommonUtilities mUtil, final String title, final NotifyEvent ntfy_create) {
        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.password_input_dlg);

        final TextView tv_title=(TextView)dialog.findViewById(R.id.password_input_title);
        tv_title.setText(title);
        final TextView tv_msg=(TextView)dialog.findViewById(R.id.password_input_msg);
        tv_msg.setText(gp.appContext.getString(R.string.msgs_security_application_password_auth_specify_password));
        final CheckedTextView ctv_prot=(CheckedTextView)dialog.findViewById(R.id.password_input_ctv_protect);
        ctv_prot.setVisibility(CheckedTextView.GONE);
        final EditText et_pswd1=(EditText)dialog.findViewById(R.id.password_input_password);
        final EditText et_pswd2=(EditText)dialog.findViewById(R.id.password_input_password_confirm);

        final TextView tv_warn=(TextView)dialog.findViewById(R.id.password_input_warning_msg);
        tv_warn.setVisibility(TextView.VISIBLE);
        tv_warn.setText(gp.appContext.getString(R.string.msgs_security_application_password_create_forget_warning));

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
                validateNewPasswordSpec(gp, btn_ok, et_pswd1, et_pswd2, tv_msg);
            }
        });
        et_pswd2.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateNewPasswordSpec(gp, btn_ok, et_pswd1, et_pswd2, tv_msg);
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
                    String enc_password=null;
                    if ( Build.VERSION.SDK_INT>=28) enc_password=KeyStoreUtil.getGeneratedPasswordNewVersion(gp.appContext, SMBSYNC2_KEY_STORE_ALIAS);
                    else enc_password=KeyStoreUtil.getGeneratedPasswordOldVersion(gp.appContext, SMBSYNC2_KEY_STORE_ALIAS);

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

                    CommonDialog cd=new CommonDialog(gp.appContext, fm);
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

    static private void validateNewPasswordSpec(GlobalParameters gp, Button btn_ok, EditText et_pswd1, EditText et_pswd2, TextView msg) {
        btn_ok.setEnabled(false);
        if (et_pswd1.getText().length()>=4) {
            if (et_pswd1.getText().length()>0 && et_pswd2.getText().length()>0) {
                if (et_pswd1.getText().toString().equals(et_pswd2.getText().toString())) {
                    btn_ok.setEnabled(true);
                    msg.setText(gp.appContext.getString(com.sentaroh.android.Utilities.R.string.msgs_password_input_preference_match));
                } else {
                    msg.setText(gp.appContext.getString(com.sentaroh.android.Utilities.R.string.msgs_password_input_preference_unmatch));
                }
            } else {
                if (et_pswd1.getText().length()==0) {
                    msg.setText(gp.appContext.getString(com.sentaroh.android.Utilities.R.string.msgs_password_input_preference_new_not_specified));
                } else {
                    msg.setText(gp.appContext.getString(com.sentaroh.android.Utilities.R.string.msgs_password_input_preference_conf_not_specified));
                }
            }
        } else {
            msg.setText(gp.appContext.getString(R.string.msgs_security_application_password_create_min_length_is_4_digit));
        }
    }

}
