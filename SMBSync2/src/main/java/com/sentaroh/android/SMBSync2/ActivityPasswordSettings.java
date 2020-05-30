package com.sentaroh.android.SMBSync2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.ThemeUtil;

public class ActivityPasswordSettings extends AppCompatActivity {
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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new GlobalParameters().setNewLocale(base, false));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = this;
        mGp= GlobalWorkArea.getGlobalParameters(mContext);
        mActivity = ActivityPasswordSettings.this;
        setTheme(mGp.applicationTheme);
        if (mGp.themeColorList == null) {
            mGp.themeColorList = CommonUtilities.getThemeColorList(mActivity);
        }

        //Remove notification bar
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.preference_application_password_dlg);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mUtil = new CommonUtilities(mContext, "AppPswd", mGp, getSupportFragmentManager());//use current activity context for short living stuff and UI

        commonDlg = new CommonDialog(mActivity, getSupportFragmentManager());
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
        ApplicationPasswordUtil.applicationPasswordAuthentication(mGp, mActivity,
                getSupportFragmentManager(), mUtil, true, ntfy_auth, ApplicationPasswordUtil.APPLICATION_PASSWORD_RESOURCE_INVOKE_SECURITY_SETTINGS);

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
//                if (isChecked) {
//                    mCtvSettingUseEditTask.setEnabled(false);
//                    mCtvSettingUseExportTask.setEnabled(false);
//                } else {
//                    mCtvSettingUseEditTask.setEnabled(true);
//                    mCtvSettingUseExportTask.setEnabled(true);
//                }
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
                ApplicationPasswordUtil.createApplicationPassword(mGp, mActivity, getSupportFragmentManager(), mUtil,
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
                        ApplicationPasswordUtil.createApplicationPassword(mGp, mActivity, getSupportFragmentManager(), mUtil,
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
                        mUtil.showCommonDialog(true, "W",
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

    public void saveApplicationPasswordHashValue(SharedPreferences prefs, String hv) {
        ApplicationPasswordUtil.saveApplicationPasswordHashValue(mGp, prefs,  hv) ;
    }

    private void setAppPswdStatus() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String hv= ApplicationPasswordUtil.getApplicationPasswordHashValue(prefs);

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

}