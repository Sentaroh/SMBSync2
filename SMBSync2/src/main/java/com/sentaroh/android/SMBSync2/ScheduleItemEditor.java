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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.sentaroh.android.Utilities.ContextMenu.CustomContextMenu;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities.StringUtil;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.Widget.CustomSpinnerAdapter;

import java.util.ArrayList;

public class ScheduleItemEditor {
//    private CommonDialog commonDlg = null;

    private GlobalParameters mGp = null;

    private Context mContext = null;
    private AppCompatActivity mActivity = null;

    private CommonUtilities mUtil = null;

    private ScheduleItem mSched = null;

    private ArrayList<ScheduleItem> mScheduleList = null;
    private NotifyEvent mNotify = null;

    private boolean mEditMode = true;

    private boolean mInitialTime = true;

    private ThemeColorList mThemeColorList = null;

    ScheduleItemEditor(CommonUtilities mu, AppCompatActivity a, Context c,
                       CommonDialog cd, CustomContextMenu ccm, GlobalParameters gp,
                       boolean edit_mode, ArrayList<ScheduleItem> sl,
                       ScheduleItem si, NotifyEvent ntfy) {
        mContext = c;
        mActivity = a;
        mGp = gp;
        mUtil = mu;
//        commonDlg = cd;
        mSched = si;

//        Log.v("", "name=" + si.scheduleName);s

        mEditMode = edit_mode;

        mNotify = ntfy;
        mScheduleList = sl;

        mThemeColorList = CommonUtilities.getThemeColorList(a);

        initDialog();

        Handler hndl=new Handler();
        hndl.postDelayed(new Runnable() {
            @Override
            public void run() {
                mInitialTime=false;
            }
        },500);
    }

    private boolean mScheduleChanged = false;

    private void setScheduleWasChanged(Dialog dialog, ScheduleItem curr_si) {
        final Button btn_ok = (Button) dialog.findViewById(R.id.scheduler_main_dlg_ok);
        if (mInitialTime) {
            mScheduleChanged=!mEditMode;
        } else {
//            Thread.dumpStack();
            ScheduleItem new_si=curr_si.clone();
            buildSchedParms(dialog, new_si);
            if (mEditMode) mScheduleChanged = !curr_si.isSame(new_si);
            else mScheduleChanged=true;
            CommonDialog.setButtonEnabled(mActivity, btn_ok, mScheduleChanged);
        }
    }

    private boolean isScheduleWasChanged() {
        return mScheduleChanged;
    }

    private void initDialog() {
        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.schedule_sync_edit_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.schedule_edit_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.schedule_edit_dlg_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        TextView dlg_title = (TextView) dialog.findViewById(R.id.schedule_edit_dlg_title);
        dlg_title.setTextColor(mGp.themeColorList.title_text_color);

        final Button btn_ok = (Button) dialog.findViewById(R.id.scheduler_main_dlg_ok);

        final Button btn_cancel = (Button) dialog.findViewById(R.id.scheduler_main_dlg_cancel);

        final Button btn_edit = (Button) dialog.findViewById(R.id.scheduler_main_dlg_edit_sync_prof);
        final TextView tv_msg = (TextView) dialog.findViewById(R.id.scheduler_main_dlg_msg);

        final EditText et_name = (EditText) dialog.findViewById(R.id.schedule_main_dlg_sched_name);

        final CheckedTextView ctv_sched_enabled = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_enabled);
//        ctv_sched_enabled.setTextColor(mGp.themeColorList.text_color_primary);
        CommonUtilities.setCheckedTextView(ctv_sched_enabled);
        final Spinner sp_sched_type = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_date_time_type);
        final Spinner sp_sched_day = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_exec_day);
        final Spinner sp_sched_hours = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_exec_hours);
        final Spinner sp_sched_minutes = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_exec_minutes);
        final CheckBox cb_sched_sun = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_sunday);
        final CheckBox cb_sched_mon = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_monday);
        final CheckBox cb_sched_tue = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_tuesday);
        final CheckBox cb_sched_wed = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_wedsday);
        final CheckBox cb_sched_thu = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_thursday);
        final CheckBox cb_sched_fri = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_friday);
        final CheckBox cb_sched_sat = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_satday);
        final TextView tv_sync_prof = (TextView) dialog.findViewById(R.id.scheduler_main_dlg_sync_task_list);
//		final LinearLayout ll_sched_dw=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week);
//		final LinearLayout ll_sched_hm=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_hm);
//		final LinearLayout ll_sched_hours=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_hour);
//		final LinearLayout ll_sched_minutes=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_minute);
        final CheckedTextView ctv_last_day = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_last_day_of_the_month);
//        ctv_last_day.setTextColor(mGp.themeColorList.text_color_primary);

        final CheckedTextView ctv_first_time = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_interval_schedule_first_run);
//        ctv_first_time.setTextColor(mGp.themeColorList.text_color_primary);

        final CheckedTextView ctv_reset_interval = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_interval_schedule_reset);
//        ctv_reset_interval.setTextColor(mGp.themeColorList.text_color_primary);

        final CheckedTextView ctv_sync_all_prof = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_sync_all_sync_task);
//        ctv_sync_all_prof.setTextColor(mGp.themeColorList.text_color_primary);
        CommonUtilities.setCheckedTextView(ctv_sync_all_prof);

        final CheckedTextView ctv_wifi_on = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_wifi_on);
//        ctv_wifi_on.setTextColor(mGp.themeColorList.text_color_primary);
        CommonUtilities.setCheckedTextView(ctv_wifi_on);
		final LinearLayout ll_wifi_on_delay_time_view=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_view);
        final TextView tv_wifi_on_delay_time = (TextView) dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_text);
        final RadioGroup rg_wifi_on_delay_time = (RadioGroup) dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg);
        final RadioButton rb_wifi_on_delay_1 = (RadioButton) dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_1);
        final RadioButton rb_wifi_on_delay_2 = (RadioButton) dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_2);
        final RadioButton rb_wifi_on_delay_3 = (RadioButton) dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_3);
        final CheckedTextView ctv_wifi_off = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_wifi_off);
//        ctv_wifi_off.setTextColor(mGp.themeColorList.text_color_primary);
        CommonUtilities.setCheckedTextView(ctv_wifi_off);

        final TextView tv_schedule_time = (TextView) dialog.findViewById(R.id.scheduler_main_dlg_schedule_time);

        final RadioGroup rg_override_sync_option_charge=(RadioGroup)dialog.findViewById(R.id.scheduler_main_dlg_override_sync_option_charge_rg);
        final RadioButton rb_override_sync_option_charge_0=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_override_sync_option_charge_rg_0);
        final RadioButton rb_override_sync_option_charge_1=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_override_sync_option_charge_rg_1);
        final RadioButton rb_override_sync_option_charge_2=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_override_sync_option_charge_rg_2);

        if (mSched.syncOverrideOptionCharge==ScheduleItem.OVERRIDE_SYNC_OPTION_DO_NOT_CHANGE) rb_override_sync_option_charge_0.setChecked(true);
        else if (mSched.syncOverrideOptionCharge==ScheduleItem.OVERRIDE_SYNC_OPTION_ENABLED) rb_override_sync_option_charge_1.setChecked(true);
        else if (mSched.syncOverrideOptionCharge==ScheduleItem.OVERRIDE_SYNC_OPTION_DISABLED) rb_override_sync_option_charge_2.setChecked(true);
        rg_override_sync_option_charge.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                setScheduleWasChanged(dialog, mSched);
            }
        });
//		loadScheduleData();

        CommonDialog.setDlgBoxSizeLimit(dialog, true);
//		CommonDialog.setDlgBoxSizeHeightMax(dialog);

        setScheduleTypeSpinner(dialog, mSched.scheduleType);
        setScheduleDaySpinner(dialog, mSched.scheduleDay);
        setScheduleHoursSpinner(dialog, mSched.scheduleHours);
        setScheduleMinutesSpinner(dialog, mSched.scheduleType, mSched.scheduleMinutes);
        setDayOfTheWeekCb(dialog, mSched.scheduleDayOfTheWeek);

        setViewVisibility(dialog);

        if (mSched.scheduleName.equals("")) {
            String new_name="";
            for(int i=1;i<1000;i++) {
                new_name="NONAME"+i;
                if (!ScheduleUtil.isScheduleExists(mScheduleList,new_name)) {
                    et_name.setText(new_name);
                    break;
                }
            }
        } else {
            et_name.setText(mSched.scheduleName);
        }
        if (mEditMode) {
            et_name.setVisibility(EditText.GONE);
            String title=dlg_title.getText().toString()+"("+mSched.scheduleName+")";
            dlg_title.setText(title);
        }

        ctv_sched_enabled.setChecked(mSched.scheduleEnabled);

        ctv_last_day.setChecked(mSched.scheduleDay.equals("99"));
        if(ctv_last_day.isChecked()) {
            CommonDialog.setViewEnabled(mActivity, sp_sched_day, false);
        } else {
            CommonDialog.setViewEnabled(mActivity, sp_sched_day, true);
        }
        setWarningMessageLastDay(dialog);
        ctv_last_day.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
                boolean isChecked = ((CheckedTextView) v).isChecked();
                if(isChecked) {
                    CommonDialog.setViewEnabled(mActivity, sp_sched_day, false);
                } else {
                    CommonDialog.setViewEnabled(mActivity, sp_sched_day, true);
                }
                setWarningMessageLastDay(dialog);
                setScheduleWasChanged(dialog, mSched);
            }
        });

        ctv_first_time.setChecked(mSched.scheduleIntervalFirstRunImmed);
        ctv_reset_interval.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
                boolean isChecked = ((CheckedTextView) v).isChecked();
                setScheduleWasChanged(dialog, mSched);
            }
        });

        ctv_wifi_off.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckedTextView) v).toggle();
                boolean isChecked = ((CheckedTextView) v).isChecked();
                setScheduleWasChanged(dialog, mSched);
            }
        });

        rg_wifi_on_delay_time.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                setScheduleWasChanged(dialog, mSched);
            }
        });

        final boolean prev_enabled = mSched.scheduleEnabled;
        setScheduleInfo(dialog, mSched);

        ctv_sched_enabled.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctv_sched_enabled.toggle();
                boolean isChecked = ctv_sched_enabled.isChecked();
                ScheduleItem n_sli = mSched.clone();
                if (isChecked) {
                    if (!mSched.scheduleEnabled) {
                        n_sli.scheduleEnabled = true;
                        n_sli.scheduleLastExecTime = System.currentTimeMillis();
                        setScheduleInfo(dialog, n_sli);
                    }
                } else {
                    n_sli.scheduleEnabled = false;
                    setScheduleInfo(dialog, n_sli);
                }
                setScheduleWasChanged(dialog, mSched);
            }
        });

        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setScheduleWasChanged(dialog, mSched);
                setOkButtonEnabledDisabled(dialog);
            }
        });
        ctv_first_time.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = !ctv_first_time.isChecked();
                ctv_first_time.setChecked(isChecked);
                if ((mSched.scheduleIntervalFirstRunImmed && !isChecked) ||
                        (!mSched.scheduleIntervalFirstRunImmed && isChecked)) {
                    ctv_reset_interval.setChecked(true);
                } else {
                    if (mSched.scheduleMinutes.equals(sp_sched_minutes.getSelectedItem().toString())) {
                        ctv_reset_interval.setChecked(false);
                    }
                }
                setScheduleInfo(dialog, mSched);
                setScheduleWasChanged(dialog, mSched);
            }
        });

        sp_sched_minutes.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (getScheduleTypeFromSpinner(sp_sched_type).equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_INTERVAL)) {//Interval
                    if (mSched.scheduleMinutes.equals(sp_sched_minutes.getSelectedItem().toString())) {
                        ctv_reset_interval.setChecked(false);
                    } else {
                        ctv_reset_interval.setChecked(true);
                    }
                    setScheduleInfo(dialog, mSched);
                }
                setScheduleWasChanged(dialog, mSched);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        setWarningMessageLastDay(dialog);
        sp_sched_day.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setScheduleWasChanged(dialog, mSched);
                setWarningMessageLastDay(dialog);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        sp_sched_hours.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setScheduleWasChanged(dialog, mSched);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (mSched.syncAutoSyncTask) {
            ctv_sync_all_prof.setChecked(true);
            btn_edit.setVisibility(Button.GONE);//.setEnabled(false);
            tv_sync_prof.setVisibility(TextView.GONE);//.setEnabled(false);
        } else {
            ctv_sync_all_prof.setChecked(false);
            btn_edit.setVisibility(Button.VISIBLE);//.setEnabled(true);
            tv_sync_prof.setVisibility(TextView.VISIBLE);//.setEnabled(true);
        }
        tv_sync_prof.setText(mSched.syncTaskList);

        sp_sched_type.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setWarningMessageLastDay(dialog);
                String sched_type = getScheduleTypeFromPosition(position);
                setScheduleMinutesSpinner(dialog, sched_type, mSched.scheduleMinutes);
                setViewVisibility(dialog);

//				btn_ok.setEnabled(true);
                setOkButtonEnabledDisabled(dialog);

                if (getScheduleTypeFromSpinner(sp_sched_type).equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_DAY_OF_THE_WEEK) &&
                        buildDayOfWeekString(dialog).equals("0000000")) {
                    cb_sched_sun.setChecked(true);
                }
                setScheduleInfo(dialog, mSched);
                setScheduleWasChanged(dialog, mSched);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        cb_sched_sun.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDayOfTheWeekSetting(dialog, mSched);
                setScheduleWasChanged(dialog, mSched);
            }
        });

        cb_sched_mon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDayOfTheWeekSetting(dialog, mSched);
                setScheduleWasChanged(dialog, mSched);
            }
        });

        cb_sched_tue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDayOfTheWeekSetting(dialog, mSched);
                setScheduleWasChanged(dialog, mSched);
            }
        });

        cb_sched_wed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDayOfTheWeekSetting(dialog, mSched);
                setScheduleWasChanged(dialog, mSched);
            }
        });

        cb_sched_thu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDayOfTheWeekSetting(dialog, mSched);
                setScheduleWasChanged(dialog, mSched);
            }
        });

        cb_sched_fri.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDayOfTheWeekSetting(dialog, mSched);
                setScheduleWasChanged(dialog, mSched);
            }
        });

        cb_sched_sat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDayOfTheWeekSetting(dialog, mSched);
                setScheduleWasChanged(dialog, mSched);
            }
        });

        ctv_wifi_on.setChecked(mSched.syncWifiOnBeforeStart);

        if (mSched.syncWifiOnBeforeStart) {
            ll_wifi_on_delay_time_view.setVisibility(LinearLayout.VISIBLE);
        } else {
            ll_wifi_on_delay_time_view.setVisibility(LinearLayout.GONE);
            ctv_wifi_off.setChecked(false);
        }

        if (mSched.syncDelayAfterWifiOn == 5) {
            rg_wifi_on_delay_time.check(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_1);
        } else if (mSched.syncDelayAfterWifiOn == 10) {
            rg_wifi_on_delay_time.check(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_2);
        } else if (mSched.syncDelayAfterWifiOn == 30) {
            rg_wifi_on_delay_time.check(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_3);
        }

        ctv_wifi_off.setChecked(mSched.syncWifiOffAfterEnd);

        ctv_wifi_on.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctv_wifi_on.toggle();
                boolean isChecked = ctv_wifi_on.isChecked();
                if (isChecked) {
                    ll_wifi_on_delay_time_view.setVisibility(LinearLayout.VISIBLE);
                } else {
                    ll_wifi_on_delay_time_view.setVisibility(LinearLayout.GONE);
                    ctv_wifi_off.setChecked(false);
                }
                setScheduleWasChanged(dialog, mSched);
            }
        });

        ctv_sync_all_prof.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctv_sync_all_prof.toggle();
                boolean isChecked = ctv_sync_all_prof.isChecked();

                if (getScheduleTypeFromSpinner(sp_sched_type).equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_DAY_OF_THE_WEEK)) {
                    if (cb_sched_sun.isChecked() || cb_sched_mon.isChecked() || cb_sched_tue.isChecked() ||
                            cb_sched_wed.isChecked() || cb_sched_thu.isChecked() || cb_sched_fri.isChecked() ||
                            cb_sched_sat.isChecked()) {
                        if (isChecked) {
                            btn_edit.setVisibility(Button.GONE);//.setEnabled(false);
                            tv_sync_prof.setVisibility(TextView.GONE);//.setEnabled(false);
                            if (isScheduleWasChanged()) CommonDialog.setButtonEnabled(mActivity, btn_ok, true);
                            else CommonDialog.setButtonEnabled(mActivity, btn_ok, false);
                            tv_msg.setText("");
                            setOkButtonEnabledDisabled(dialog);

                        } else {
                            btn_edit.setVisibility(Button.VISIBLE);//.setEnabled(true);
                            tv_sync_prof.setVisibility(TextView.VISIBLE);//.setEnabled(true);
                            if (tv_sync_prof.getText().equals("")) {
                                CommonDialog.setButtonEnabled(mActivity, btn_ok, false);
                                tv_msg.setText(mContext.getString(R.string.msgs_scheduler_edit_sync_prof_list_not_specified));
                            } else {
                                if (isScheduleWasChanged()) CommonDialog.setButtonEnabled(mActivity, btn_ok, true);
                                else CommonDialog.setButtonEnabled(mActivity, btn_ok, false);
                                tv_msg.setText("");
                                setOkButtonEnabledDisabled(dialog);

                            }
                        }
                        setScheduleWasChanged(dialog, mSched);
                    } else {
                        //NOP
                    }
                } else {
                    if (isChecked) {
                        btn_edit.setVisibility(Button.GONE);//.setEnabled(false);
                        tv_sync_prof.setVisibility(TextView.GONE);//.setEnabled(false);
                        if (isScheduleWasChanged()) CommonDialog.setButtonEnabled(mActivity, btn_ok, true);
                        else CommonDialog.setButtonEnabled(mActivity, btn_ok, false);
                        tv_msg.setText("");
                        setOkButtonEnabledDisabled(dialog);
                    } else {
                        btn_edit.setVisibility(Button.VISIBLE);//.setEnabled(true);
                        tv_sync_prof.setVisibility(TextView.VISIBLE);//.setEnabled(true);
                        if (tv_sync_prof.getText().equals("")) {
                            tv_msg.setText(mContext.getString(R.string.msgs_scheduler_edit_sync_prof_list_not_specified));
                            CommonDialog.setButtonEnabled(mActivity, btn_ok, false);
                        } else {
                            if (isScheduleWasChanged()) CommonDialog.setButtonEnabled(mActivity, btn_ok, true);
                            else CommonDialog.setButtonEnabled(mActivity, btn_ok, false);
                            tv_msg.setText("");
                            setOkButtonEnabledDisabled(dialog);
                        }
                    }
                    setScheduleWasChanged(dialog, mSched);
                }
            }
        });

        btn_edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        String prof_list = (String) o[0];
                        tv_sync_prof.setText(prof_list);
                        if (prof_list.equals("")) {
                            tv_msg.setText(mContext.getString(R.string.msgs_scheduler_edit_sync_prof_list_not_specified));
                            CommonDialog.setButtonEnabled(mActivity, btn_ok, false);
                        } else {
                            if (isScheduleWasChanged()) CommonDialog.setButtonEnabled(mActivity, btn_ok, true);
                            else CommonDialog.setButtonEnabled(mActivity, btn_ok, false);
                            tv_msg.setText("");
                            setOkButtonEnabledDisabled(dialog);
                        }
                        setScheduleWasChanged(dialog, mSched);
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                    }
                });
                editSyncTaskList(tv_sync_prof.getText().toString(), ntfy);
            }
        });

        btn_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                buildSchedParms(dialog, mSched);
                ScheduleUtil.getNextSchedule(mSched);
                if (!prev_enabled && mSched.scheduleEnabled)
                    mSched.scheduleLastExecTime = System.currentTimeMillis();
                if (mNotify != null) mNotify.notifyToListener(true, new Object[]{mSched});
            }
        });

        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScheduleWasChanged()) {
                    NotifyEvent ntfy = new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            dialog.dismiss();
                        }

                        @Override
                        public void negativeResponse(Context context, Object[] objects) {
                        }
                    });
                    mUtil.showCommonDialog(true, "W",
                            mContext.getString(R.string.msgs_schedule_confirm_title_nosave),
                            mContext.getString(R.string.msgs_schedule_confirm_msg_nosave), ntfy);
                } else {
                    dialog.dismiss();
                }


            }
        });

        dialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                btn_cancel.performClick();
            }
        });

        Handler hndl = new Handler();
        hndl.postDelayed(new Runnable() {
            @Override
            public void run() {
                mInitialTime = false;
                CommonDialog.setButtonEnabled(mActivity, btn_ok, !mEditMode);
            }
        }, 500);

        dialog.show();
    }

    private String getNotExistsSyncTaskName(String task_list) {
        String error_item_name="";
        if (task_list.indexOf(",")>0) {
            String[] stl=task_list.split(",");
            String sep="";
            for(String stn:stl) {
                if (ScheduleUtil.getSyncTask(mGp,stn)==null) {
                    error_item_name=sep+stn;
                    sep=",";
                }
            }
        } else {
            if (ScheduleUtil.getSyncTask(mGp, task_list)==null) {
                error_item_name=task_list;
            }
        }
        return  error_item_name;
    }

    private void setScheduleInfo(Dialog dialog, ScheduleItem sli) {
        final TextView tv_schedule_time = (TextView) dialog.findViewById(R.id.scheduler_main_dlg_schedule_time);
        if (sli.scheduleEnabled) {
            ScheduleItem n_sp = ScheduleUtil.copyScheduleData(mGp, sli);
            buildSchedParms(dialog, n_sp);
            tv_schedule_time.setText(
                    String.format(mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_schedule_time),
                            StringUtil.convDateTimeTo_YearMonthDayHourMinSec(ScheduleUtil.getNextSchedule(n_sp))));
        } else {
            tv_schedule_time.setText(mContext.getString(R.string.msgs_scheduler_info_schedule_disabled));
        }
    }

    private void setOkButtonEnabledDisabled(Dialog dialog) {
        final Button btn_ok = (Button) dialog.findViewById(R.id.scheduler_main_dlg_ok);
        final EditText et_name = (EditText) dialog.findViewById(R.id.schedule_main_dlg_sched_name);
        final TextView tv_msg = (TextView) dialog.findViewById(R.id.scheduler_main_dlg_msg);
        CommonDialog.setButtonEnabled(mActivity, btn_ok, !mEditMode);
        if (et_name.getText().length() == 0) {
            tv_msg.setText(mContext.getString(R.string.msgs_schedule_list_edit_dlg_error_sync_list_name_does_not_specified));
            CommonDialog.setButtonEnabled(mActivity, btn_ok, false);
        } else {
            if (!mEditMode && ScheduleUtil.isScheduleExists(mScheduleList, et_name.getText().toString())) {
                //Name alread exists
                tv_msg.setText(mContext.getString(R.string.msgs_schedule_list_edit_dlg_error_sync_list_name_already_exists));
                CommonDialog.setButtonEnabled(mActivity, btn_ok, false);
            } else {
                if (!mSched.syncAutoSyncTask) {
                    String error_task_name=getNotExistsSyncTaskName(mSched.syncTaskList);
                    if (!error_task_name.equals("")) {
                        tv_msg.setText(String.format(mContext.getString(R.string.msgs_scheduler_info_sync_task_was_not_found), error_task_name));
                    } else {
                        tv_msg.setText("");
                        CommonDialog.setButtonEnabled(mActivity, btn_ok, true);
                    }
                } else {
                    tv_msg.setText("");
                    CommonDialog.setButtonEnabled(mActivity, btn_ok, true);
                }
                if (isScheduleWasChanged()) CommonDialog.setButtonEnabled(mActivity, btn_ok, true);
                else CommonDialog.setButtonEnabled(mActivity, btn_ok, false);
            }
        }

    }

    private void checkDayOfTheWeekSetting(Dialog dialog, ScheduleItem sp) {
        final Button btn_ok = (Button) dialog.findViewById(R.id.scheduler_main_dlg_ok);
//		final Button btn_cancel = (Button) dialog.findViewById(R.id.scheduler_main_dlg_cancel);
        final Button btn_edit = (Button) dialog.findViewById(R.id.scheduler_main_dlg_edit_sync_prof);
        final TextView tv_msg = (TextView) dialog.findViewById(R.id.scheduler_main_dlg_msg);

//		final CheckedTextView ctv_sched_enabled=(CheckedTextView)dialog.findViewById(R.id.scheduler_main_dlg_ctv_enabled);
//		final Spinner sp_sched_type=(Spinner)dialog.findViewById(R.id.scheduler_main_dlg_date_time_type);
//		final Spinner sp_sched_hours=(Spinner)dialog.findViewById(R.id.scheduler_main_dlg_exec_hours);
//		final Spinner sp_sched_minutes=(Spinner)dialog.findViewById(R.id.scheduler_main_dlg_exec_minutes);
        final CheckBox cb_sched_sun = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_sunday);
        final CheckBox cb_sched_mon = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_monday);
        final CheckBox cb_sched_tue = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_tuesday);
        final CheckBox cb_sched_wed = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_wedsday);
        final CheckBox cb_sched_thu = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_thursday);
        final CheckBox cb_sched_fri = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_friday);
        final CheckBox cb_sched_sat = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_satday);
        final TextView tv_sync_prof = (TextView) dialog.findViewById(R.id.scheduler_main_dlg_sync_task_list);
//		final LinearLayout ll_sched_dw=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week);
//		final LinearLayout ll_sched_hm=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_hm);
//		final LinearLayout ll_sched_hours=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_hour);
//		final LinearLayout ll_sched_minutes=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_minute);
//		final CheckedTextView ctv_first_time=(CheckedTextView)dialog.findViewById(R.id.scheduler_main_dlg_ctv_interval_schedule_first_run);

//		final CheckedTextView ctv_reset_interval=(CheckedTextView)dialog.findViewById(R.id.scheduler_main_dlg_ctv_interval_schedule_reset);

        final CheckedTextView ctv_sync_all_prof = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_sync_all_sync_task);

//		final CheckedTextView ctv_wifi_on=(CheckedTextView)dialog.findViewById(R.id.scheduler_main_dlg_ctv_wifi_on);
//		final LinearLayout ll_wifi_on_delay_time_viewx=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_view);
//		final TextView tv_wifi_on_delay_time=(TextView)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_text);
//		final RadioGroup rg_wifi_on_delay_time=(RadioGroup)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg);
//		final RadioButton rb_wifi_on_delay_1=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_1);
//		final RadioButton rb_wifi_on_delay_2=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_2);
//		final RadioButton rb_wifi_on_delay_3=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_3);
//		final CheckedTextView ctv_wifi_off=(CheckedTextView)dialog.findViewById(R.id.scheduler_main_dlg_ctv_wifi_off);

        final TextView tv_schedule_time = (TextView) dialog.findViewById(R.id.scheduler_main_dlg_schedule_time);

        if (cb_sched_sun.isChecked() || cb_sched_mon.isChecked() || cb_sched_tue.isChecked() ||
                cb_sched_wed.isChecked() || cb_sched_thu.isChecked() || cb_sched_fri.isChecked() ||
                cb_sched_sat.isChecked()) {
            CommonDialog.setViewEnabled(mActivity, ctv_sync_all_prof, true);
            CommonDialog.setViewEnabled(mActivity, btn_edit, true);

            tv_msg.setText("");
            CommonDialog.setButtonEnabled(mActivity, btn_ok, true);

            ScheduleItem n_sp = ScheduleUtil.copyScheduleData(mGp, mSched);
            buildSchedParms(dialog, n_sp);
            tv_schedule_time.setText(
                    String.format(mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_schedule_time),
                            StringUtil.convDateTimeTo_YearMonthDayHourMinSec(ScheduleUtil.getNextSchedule(n_sp))));
            setOkButtonEnabledDisabled(dialog);
        } else {
            CommonDialog.setViewEnabled(mActivity, ctv_sync_all_prof, false);
            CommonDialog.setViewEnabled(mActivity, btn_edit, false);
            mSched.isChanged=false;
            tv_msg.setText(mContext.getString(R.string.msgs_scheduler_main_dw_not_selected));
            tv_schedule_time.setText(
                    String.format(mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_schedule_time), ""));
        }
    }

    private void setWarningMessageLastDay(Dialog dialog) {
        final Spinner sp_sched_day = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_exec_day);
        final Spinner sp_sched_type = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_date_time_type);
        final CheckedTextView ctv_last_day = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_last_day_of_the_month);

        final TextView tv_sched_day_warning = (TextView) dialog.findViewById(R.id.scheduler_main_dlg_exec_day_waring);
        if (ctv_last_day.isChecked()) {
            tv_sched_day_warning.setVisibility(TextView.GONE);
        } else {
            if (getScheduleTypeFromSpinner(sp_sched_type).equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_MONTH)) {//Every month
                tv_sched_day_warning.setTextColor(Color.YELLOW);
                try {
                    int day=Integer.parseInt(sp_sched_day.getSelectedItem().toString());
                    if (day>28 && day<=31) {
                        tv_sched_day_warning.setVisibility(TextView.VISIBLE);
                    } else {
                        tv_sched_day_warning.setVisibility(TextView.GONE);
                    }
                } catch(Exception e) {
                    tv_sched_day_warning.setVisibility(TextView.GONE);
                }
            } else {
                tv_sched_day_warning.setVisibility(TextView.GONE);
            }
        }
    }

    private void buildSchedParms(Dialog dialog, ScheduleItem sp) {
        final EditText et_name = (EditText) dialog.findViewById(R.id.schedule_main_dlg_sched_name);
        final CheckedTextView ctv_sched_enabled = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_enabled);
        final Spinner sp_sched_type = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_date_time_type);
        final Spinner sp_sched_day = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_exec_day);
        final Spinner sp_sched_hours = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_exec_hours);
        final Spinner sp_sched_minutes = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_exec_minutes);
//		final CheckBox cb_sched_sun=(CheckBox)dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_sunday);
//		final CheckBox cb_sched_mon=(CheckBox)dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_monday);
//		final CheckBox cb_sched_tue=(CheckBox)dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_tuesday);
//		final CheckBox cb_sched_wed=(CheckBox)dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_wedsday);
//		final CheckBox cb_sched_thu=(CheckBox)dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_thursday);
//		final CheckBox cb_sched_fri=(CheckBox)dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_friday);
//		final CheckBox cb_sched_sat=(CheckBox)dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_satday);
        final TextView tv_sync_prof = (TextView) dialog.findViewById(R.id.scheduler_main_dlg_sync_task_list);
//		final LinearLayout ll_sched_dw=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week);
//		final LinearLayout ll_sched_hm=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_hm);
//		final LinearLayout ll_sched_hours=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_hour);
//		final LinearLayout ll_sched_minutes=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_minute);
        final CheckedTextView ctv_last_day = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_last_day_of_the_month);

        final CheckedTextView ctv_first_time = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_interval_schedule_first_run);

        final CheckedTextView ctv_reset_interval = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_interval_schedule_reset);

        final CheckedTextView ctv_sync_all_prof = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_sync_all_sync_task);

        final CheckedTextView ctv_wifi_on = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_wifi_on);
//		final LinearLayout ll_wifi_on_delay_time_viewx=(LinearLayout)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_view);
//		final TextView tv_wifi_on_delay_time=(TextView)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_text);
        final RadioGroup rg_wifi_on_delay_time = (RadioGroup) dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg);
//		final RadioButton rb_wifi_on_delay_1=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_1);
//		final RadioButton rb_wifi_on_delay_2=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_2);
//		final RadioButton rb_wifi_on_delay_3=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_wifi_on_delay_time_rg_3);
        final CheckedTextView ctv_wifi_off = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_wifi_off);

        final RadioGroup rg_override_sync_option_charge=(RadioGroup)dialog.findViewById(R.id.scheduler_main_dlg_override_sync_option_charge_rg);
        final RadioButton rb_override_sync_option_charge_0=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_override_sync_option_charge_rg_0);
        final RadioButton rb_override_sync_option_charge_1=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_override_sync_option_charge_rg_1);
        final RadioButton rb_override_sync_option_charge_2=(RadioButton)dialog.findViewById(R.id.scheduler_main_dlg_override_sync_option_charge_rg_2);

        boolean p_intv_first_run = sp.scheduleIntervalFirstRunImmed;
        sp.scheduleDayOfTheWeek = buildDayOfWeekString(dialog);
        sp.scheduleName = et_name.getText().toString();
        sp.scheduleEnabled = ctv_sched_enabled.isChecked();
        sp.scheduleIntervalFirstRunImmed = ctv_first_time.isChecked();

        String p_sched_type = sp.scheduleType;
        String p_sched_mm = sp.scheduleMinutes;

        sp.scheduleType=getScheduleTypeFromSpinner(sp_sched_type);

        if (ctv_last_day.isChecked()) {
            sp.scheduleDay = "99";
        } else {
            sp.scheduleDay = sp_sched_day.getSelectedItem().toString();
        }

        sp.scheduleHours = sp_sched_hours.getSelectedItem().toString();

        sp.scheduleMinutes = sp_sched_minutes.getSelectedItem().toString();

        if (sp.scheduleType.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_INTERVAL)) {
            if (!p_sched_type.equals(sp.scheduleType) || !p_sched_mm.equals(sp.scheduleMinutes) ||
                    (!p_intv_first_run && sp.scheduleIntervalFirstRunImmed) ||
                    (p_intv_first_run && !sp.scheduleIntervalFirstRunImmed) ||
                    ctv_reset_interval.isChecked()) {
                sp.scheduleLastExecTime = 0;
            }
        }

        if (ctv_sync_all_prof.isChecked()) {
            sp.syncTaskList = "";
            sp.syncAutoSyncTask=true;
        } else {
            sp.syncTaskList = tv_sync_prof.getText().toString();
            sp.syncAutoSyncTask=false;
        }

        sp.syncWifiOnBeforeStart = ctv_wifi_on.isChecked();

        if (rg_wifi_on_delay_time.getCheckedRadioButtonId() == R.id.scheduler_main_dlg_wifi_on_delay_time_rg_1) {
            sp.syncDelayAfterWifiOn = 5;
        } else if (rg_wifi_on_delay_time.getCheckedRadioButtonId() == R.id.scheduler_main_dlg_wifi_on_delay_time_rg_2) {
            sp.syncDelayAfterWifiOn = 10;
        } else if (rg_wifi_on_delay_time.getCheckedRadioButtonId() == R.id.scheduler_main_dlg_wifi_on_delay_time_rg_3) {
            sp.syncDelayAfterWifiOn = 30;
        } else {
            sp.syncDelayAfterWifiOn = 5;
        }

        if (ctv_wifi_on.isChecked()) sp.syncWifiOffAfterEnd = ctv_wifi_off.isChecked();
        else sp.syncWifiOffAfterEnd = false;

        if (rb_override_sync_option_charge_0.isChecked()) sp.syncOverrideOptionCharge=ScheduleItem.OVERRIDE_SYNC_OPTION_DO_NOT_CHANGE;
        else if (rb_override_sync_option_charge_1.isChecked()) sp.syncOverrideOptionCharge=ScheduleItem.OVERRIDE_SYNC_OPTION_ENABLED;
        else if (rb_override_sync_option_charge_2.isChecked()) sp.syncOverrideOptionCharge=ScheduleItem.OVERRIDE_SYNC_OPTION_DISABLED;
    }

    private void editSyncTaskList(final String prof_list, final NotifyEvent p_ntfy) {
        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.schedule_sync_edit_synclist_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.scheduler_edit_synclist_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.scheduler_edit_synclist_dlg_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        TextView dlg_title = (TextView) dialog.findViewById(R.id.scheduler_edit_synclist_dlg_title);
        dlg_title.setTextColor(mGp.themeColorList.title_text_color);

        final Button btn_ok = (Button) dialog.findViewById(R.id.scheduler_edit_synclist_dlg_ok);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.scheduler_edit_synclist_dlg_cancel);

        final ListView lv_sync_list = (ListView) dialog.findViewById(R.id.scheduler_edit_synclist_dlg_sync_prof_list);

        final SchedulerAdapterSyncList adapter =
                new SchedulerAdapterSyncList(mActivity, android.R.layout.simple_list_item_checked);

        CommonDialog.setViewEnabled(mActivity, btn_ok, setSyncTaskListView(true, prof_list, lv_sync_list, adapter));
        setSyncTaskListView(true, prof_list, lv_sync_list, adapter);

        lv_sync_list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean sel = false;
                for (int i = 0; i < lv_sync_list.getCount(); i++) {
                    if (lv_sync_list.isItemChecked(i)) {
                        sel = true;
                        break;
                    }
                }
                if (sel) CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                else CommonDialog.setViewEnabled(mActivity, btn_ok, false);
            }
        });

        btn_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
//				SparseBooleanArray sba=lv_sync_list.getCheckedItemPositions();
                String n_prof_list = "", sep = "";
                for (int i = 0; i < lv_sync_list.getCount(); i++) {
                    if (lv_sync_list.isItemChecked(i)) {
                        n_prof_list = n_prof_list + sep + adapter.getItem(i).substring(1);
                        sep = ",";
                    }
                }
                p_ntfy.notifyToListener(true, new Object[]{n_prof_list});
            }
        });

        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                p_ntfy.notifyToListener(false, null);
            }
        });
        dialog.show();
    }

    private final static String SYNC_TASK_ENABLED="E";
    private final static String SYNC_TASK_DISABLED="D";

    private boolean setSyncTaskListView(boolean active,
                                        String prof_list, ListView lv, SchedulerAdapterSyncList adapter) {
        adapter.clear();

        for (int i = 0; i < mGp.syncTaskAdapter.getCount(); i++) {
            SyncTaskItem pfli = mGp.syncTaskAdapter.getItem(i);
            adapter.add(SYNC_TASK_ENABLED + pfli.getSyncTaskName());
        }

        String[] pfa = null;
        pfa = prof_list.split(",");
        if (!prof_list.equals("")) {
            for (int i = 0; i < pfa.length; i++) {
                setSelectedSyncList(pfa[i], lv, adapter);
            }
        }

        lv.setAdapter(adapter);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv.setSelected(true);

        if (!prof_list.equals("")) {
            for (int k = 0; k < pfa.length; k++) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    String prof_name = adapter.getItem(i).substring(1);
                    if (prof_name.equals(pfa[k])) {
                        if (adapter.getItem(i).startsWith(SYNC_TASK_ENABLED)) lv.setItemChecked(i, true);
                        break;
                    }
                }
            }
        }
        boolean selected = false;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (lv.isItemChecked(i)) {
                selected = true;
                break;
            }
        }
        adapter.notifyDataSetChanged();
        return selected;
    }

    private void setSelectedSyncList(String sel, ListView lv, SchedulerAdapterSyncList adapter) {
        boolean found = false;
        for (int i = 0; i < adapter.getCount(); i++) {
            String prof_name = adapter.getItem(i).substring(1);
            if (prof_name.equals(sel)) {
                found = true;
//				lv.setItemChecked(i, true);
                break;
            }
        }
        if (!found) {
            for (int i = 0; i < adapter.getCount(); i++) {
                String prof_name = adapter.getItem(i).substring(1);
                if (prof_name.compareToIgnoreCase(sel) > 0) {
                    adapter.insert(SYNC_TASK_DISABLED + sel, i + 1);
                    adapter.notifyDataSetChanged();
//					lv.setItemChecked(i+1, true);
                    break;
                }
            }
        }
    }

    private void setViewVisibility(Dialog dialog) {
        final Spinner sp_sched_type = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_date_time_type);
//		final Spinner sp_sched_hours=(Spinner)dialog.findViewById(R.id.scheduler_main_dlg_exec_hours);
//		final Spinner sp_sched_minutes=(Spinner)dialog.findViewById(R.id.scheduler_main_dlg_exec_minutes);
        final LinearLayout ll_sched_dw = (LinearLayout) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week);
        final LinearLayout ll_sched_hm = (LinearLayout) dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_hm);
        final LinearLayout ll_sched_day = (LinearLayout) dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_day);
        final LinearLayout ll_sched_hours = (LinearLayout) dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_hour);
        final LinearLayout ll_sched_minutes = (LinearLayout) dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_minute);
        final CheckedTextView ctv_last_day = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ll_exec_last_day_of_the_month);
        final CheckedTextView ctv_first_time = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_interval_schedule_first_run);
        ctv_first_time.setVisibility(CheckedTextView.GONE);
        final CheckedTextView ctv_reset_interval = (CheckedTextView) dialog.findViewById(R.id.scheduler_main_dlg_ctv_interval_schedule_reset);
        ctv_reset_interval.setVisibility(CheckedTextView.GONE);

        if (getScheduleTypeFromSpinner(sp_sched_type).equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_HOURS)) {//Every hours
            ll_sched_dw.setVisibility(LinearLayout.GONE);
            ll_sched_hm.setVisibility(LinearLayout.VISIBLE);
            ll_sched_day.setVisibility(LinearLayout.GONE);
            ll_sched_hours.setVisibility(LinearLayout.GONE);
            ll_sched_minutes.setVisibility(LinearLayout.VISIBLE);
            ctv_last_day.setVisibility(CheckedTextView.GONE);
        } else if (getScheduleTypeFromSpinner(sp_sched_type).equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_DAY)) {//Every day
            ll_sched_dw.setVisibility(LinearLayout.GONE);
            ll_sched_hm.setVisibility(LinearLayout.VISIBLE);
            ll_sched_day.setVisibility(LinearLayout.GONE);
            ll_sched_hours.setVisibility(LinearLayout.VISIBLE);
            ll_sched_minutes.setVisibility(LinearLayout.VISIBLE);
            ctv_last_day.setVisibility(CheckedTextView.GONE);
        } else if (getScheduleTypeFromSpinner(sp_sched_type).equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_MONTH)) {//Every month
            ll_sched_dw.setVisibility(LinearLayout.GONE);
            ll_sched_hm.setVisibility(LinearLayout.VISIBLE);
            ll_sched_day.setVisibility(LinearLayout.VISIBLE);
            ll_sched_hours.setVisibility(LinearLayout.VISIBLE);
            ll_sched_minutes.setVisibility(LinearLayout.VISIBLE);
            ctv_last_day.setVisibility(CheckedTextView.VISIBLE);
        } else if (getScheduleTypeFromSpinner(sp_sched_type).equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_DAY_OF_THE_WEEK)) {//Day off the week
            ll_sched_dw.setVisibility(LinearLayout.VISIBLE);
            ll_sched_hm.setVisibility(LinearLayout.VISIBLE);
            ll_sched_day.setVisibility(LinearLayout.GONE);
            ll_sched_hours.setVisibility(LinearLayout.VISIBLE);
            ll_sched_minutes.setVisibility(LinearLayout.VISIBLE);
            ctv_last_day.setVisibility(CheckedTextView.GONE);
        } else if (getScheduleTypeFromSpinner(sp_sched_type).equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_INTERVAL)) {//Interval
            ll_sched_dw.setVisibility(LinearLayout.GONE);
            ll_sched_hm.setVisibility(LinearLayout.VISIBLE);
            ll_sched_day.setVisibility(LinearLayout.GONE);
            ll_sched_hours.setVisibility(LinearLayout.GONE);
            ll_sched_minutes.setVisibility(LinearLayout.VISIBLE);
            ctv_last_day.setVisibility(CheckedTextView.GONE);
            ctv_first_time.setVisibility(CheckedTextView.VISIBLE);
            ctv_reset_interval.setVisibility(CheckedTextView.VISIBLE);
        }
    }

    private String buildDayOfWeekString(Dialog dialog) {
        String sun = "0", mon = "0", tue = "0", wed = "0", thu = "0", fri = "0", sat = "0";
        final CheckBox cb_sched_sun = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_sunday);
        final CheckBox cb_sched_mon = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_monday);
        final CheckBox cb_sched_tue = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_tuesday);
        final CheckBox cb_sched_wed = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_wedsday);
        final CheckBox cb_sched_thu = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_thursday);
        final CheckBox cb_sched_fri = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_friday);
        final CheckBox cb_sched_sat = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_satday);
        if (cb_sched_sun.isChecked()) sun = "1";
        if (cb_sched_mon.isChecked()) mon = "1";
        if (cb_sched_tue.isChecked()) tue = "1";
        if (cb_sched_wed.isChecked()) wed = "1";
        if (cb_sched_thu.isChecked()) thu = "1";
        if (cb_sched_fri.isChecked()) fri = "1";
        if (cb_sched_sat.isChecked()) sat = "1";
        return sun + mon + tue + wed + thu + fri + sat;
    }

    private void setDayOfTheWeekCb(Dialog dialog, String dw) {
        final CheckBox cb_sched_sun = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_sunday);
        final CheckBox cb_sched_mon = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_monday);
        final CheckBox cb_sched_tue = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_tuesday);
        final CheckBox cb_sched_wed = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_wedsday);
        final CheckBox cb_sched_thu = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_thursday);
        final CheckBox cb_sched_fri = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_friday);
        final CheckBox cb_sched_sat = (CheckBox) dialog.findViewById(R.id.scheduler_main_dlg_day_of_the_week_satday);
        if (dw.substring(0, 1).equals("1")) cb_sched_sun.setChecked(true);
        else cb_sched_sun.setChecked(false);
        if (dw.substring(1, 2).equals("1")) cb_sched_mon.setChecked(true);
        else cb_sched_mon.setChecked(false);
        if (dw.substring(2, 3).equals("1")) cb_sched_tue.setChecked(true);
        else cb_sched_tue.setChecked(false);
        if (dw.substring(3, 4).equals("1")) cb_sched_wed.setChecked(true);
        else cb_sched_wed.setChecked(false);
        if (dw.substring(4, 5).equals("1")) cb_sched_thu.setChecked(true);
        else cb_sched_thu.setChecked(false);
        if (dw.substring(5, 6).equals("1")) cb_sched_fri.setChecked(true);
        else cb_sched_fri.setChecked(false);
        if (dw.substring(6, 7).equals("1")) cb_sched_sat.setChecked(true);
        else cb_sched_sat.setChecked(false);
    }

    private void setScheduleTypeSpinner(Dialog dialog, String type) {
        final Spinner sp_sched_type = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_date_time_type);
        CommonUtilities.setSpinnerBackground(mContext, sp_sched_type, mGp.isScreenThemeIsLight());

        final CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(mActivity, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        sp_sched_type.setPrompt(mContext.getString(R.string.msgs_scheduler_main_spinner_sched_type_prompt));
        sp_sched_type.setAdapter(adapter);
        adapter.add(mContext.getString(R.string.msgs_scheduler_main_spinner_sched_type_every_hour));
        adapter.add(mContext.getString(R.string.msgs_scheduler_main_spinner_sched_type_every_day));
        adapter.add(mContext.getString(R.string.msgs_scheduler_main_spinner_sched_type_every_month));
        adapter.add(mContext.getString(R.string.msgs_scheduler_main_spinner_sched_type_day_of_week));
        adapter.add(mContext.getString(R.string.msgs_scheduler_main_spinner_sched_type_interval));

        if (!type.equals("")) {
            int sel = -1;
            if (type.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_HOURS)) sel = 0;
            else if (type.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_DAY)) sel = 1;
            else if (type.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_MONTH)) sel = 2;
            else if (type.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_DAY_OF_THE_WEEK)) sel = 3;
            else if (type.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_INTERVAL)) sel = 4;
            sp_sched_type.setSelection(sel);
        }
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    private String getScheduleTypeFromSpinner(Spinner spinner) {
        return getScheduleTypeFromPosition(spinner.getSelectedItemPosition());
    }


    private String getScheduleTypeFromPosition(int position) {
        String sched_type = "";
        if (position == 0) sched_type = ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_HOURS;
        else if (position == 1) sched_type = ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_DAY;
        else if (position == 2) sched_type = ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_MONTH;
        else if (position == 3) sched_type = ScheduleItem.SCHEDULER_SCHEDULE_TYPE_DAY_OF_THE_WEEK;
        else if (position == 4) sched_type = ScheduleItem.SCHEDULER_SCHEDULE_TYPE_INTERVAL;
        return sched_type;
    }

    private void setScheduleDaySpinner(Dialog dialog, String dd) {
        final Spinner sp_sched_hours = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_exec_day);
        CommonUtilities.setSpinnerBackground(mContext, sp_sched_hours, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(mActivity, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        sp_sched_hours.setPrompt(mContext.getString(R.string.msgs_scheduler_main_spinner_sched_day_prompt));
        sp_sched_hours.setAdapter(adapter);

        int sel = -1, s_hh = Integer.parseInt(dd);
        for (int i = 1; i < 32; i++) {
            if (i >= 10) adapter.add("" + i);
            else adapter.add("0" + i);
            if (s_hh == i) sel = i;
        }
        sp_sched_hours.setSelection(sel-1);
        adapter.notifyDataSetChanged();
    }

    private void setScheduleHoursSpinner(Dialog dialog, String hh) {
        final Spinner sp_sched_hours = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_exec_hours);
        CommonUtilities.setSpinnerBackground(mContext, sp_sched_hours, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(mActivity, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        sp_sched_hours.setPrompt(mContext.getString(R.string.msgs_scheduler_main_spinner_sched_hours_prompt));
        sp_sched_hours.setAdapter(adapter);

        int sel = -1, s_hh = Integer.parseInt(hh);
        for (int i = 0; i < 24; i++) {
            if (i >= 10) adapter.add("" + i);
            else adapter.add("0" + i);
            if (s_hh == i) sel = i;
        }
        sp_sched_hours.setSelection(sel);
        adapter.notifyDataSetChanged();
    }

    private void setScheduleMinutesSpinner(Dialog dialog, String sched_type, String mm) {
        final Spinner sp_sched_minutes = (Spinner) dialog.findViewById(R.id.scheduler_main_dlg_exec_minutes);
        CommonUtilities.setSpinnerBackground(mContext, sp_sched_minutes, mGp.isScreenThemeIsLight());
        final CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(mActivity, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        sp_sched_minutes.setPrompt(mContext.getString(R.string.msgs_scheduler_main_spinner_sched_hours_prompt));
        sp_sched_minutes.setAdapter(adapter);

        int sel = 0, s_mm = Integer.parseInt(mm);
        if (sched_type.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_INTERVAL)) {
            for (int i = 1; i < 5; i++) {
                adapter.add("0" + i);
                if (s_mm == i) sel = adapter.getCount() - 1;
            }
            for (int i = 5; i < 245; i += 5) {
                if (i >= 10) adapter.add("" + i);
                else adapter.add("0" + i);
                if (s_mm == i) sel = adapter.getCount() - 1;
            }
        } else {
            for (int i = 0; i < 60; i++) {
                if (i >= 10) adapter.add("" + i);
                else adapter.add("0" + i);
                if (s_mm == i) sel = i;
            }
        }
        sp_sched_minutes.setSelection(sel);
        adapter.notifyDataSetChanged();
    }

    ;

//    private void loadScheduleData() {
//    	mGp.scheduleInfoList = ScheduleUtil.loadScheduleData(mGp);
//    	mSched=mGp.scheduleInfoList.get(0);
//
//    	mUtil.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName()+" type="+mSched.scheduleType+
//    			", hours="+mSched.scheduleHours+
//    			", minutes="+mSched.scheduleMinutes+
//    			", dw="+mSched.scheduleDayOfTheWeek+
//    			", sync_task="+mSched.syncTaskList+
//				", Wifi On="+mSched.syncWifiOnBeforeStart+
//				", Wifi Off="+mSched.syncWifiOffAfterEnd+
//				", Wifi On dlayed="+mSched.syncDelayAfterWifiOn
//    			);
//    };

    private class SchedulerAdapterSyncList extends ArrayAdapter<String> {
        private int layout_id = 0;
        private Context context = null;
        @SuppressWarnings("unused")
        private int text_color = 0;

        public SchedulerAdapterSyncList(Context c, int textViewResourceId) {
            super(c, textViewResourceId);
            layout_id = textViewResourceId;
            context = c;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder holder;
            final String o = getItem(position);
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(layout_id, null);
                holder = new ViewHolder();
                holder.tv_name = (CheckedTextView) v.findViewById(android.R.id.text1);
                text_color = holder.tv_name.getCurrentTextColor();
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            if (o != null) {
                if (o.substring(0, 1).equals(SYNC_TASK_DISABLED)) {
                    holder.tv_name.setText(o.substring(1)+"\n"+mContext.getString(R.string.msgs_scheduler_info_sync_task_was_deleted_or_renamed));
                    holder.tv_name.setTextColor(mThemeColorList.text_color_warning);
                    holder.tv_name.setChecked(false);
                } else {
                    holder.tv_name.setText(o.substring(1));
                    holder.tv_name.setTextColor(text_color);
                }
//            	if (o.substring(0, 1).equals(SMBSYNC2_TASK_ENABLED)) {
//            		holder.tv_name.setTextColor(text_color);
//            	} else {
//            		holder.tv_name.setTextColor(Color.DKGRAY);
//            	}
            }
            return v;

        }

        class ViewHolder {
            CheckedTextView tv_name;
        }
    }

}
