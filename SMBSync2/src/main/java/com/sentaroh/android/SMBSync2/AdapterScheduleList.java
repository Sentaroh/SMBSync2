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
package com.sentaroh.android.SMBSync2;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.NotifyEvent;

import java.util.ArrayList;
import java.util.Comparator;

import static com.sentaroh.android.SMBSync2.Constants.SYNC_TASK_LIST_SEPARATOR;

class AdapterScheduleList extends ArrayAdapter<ScheduleItem> {
    private int layout_id = 0;
    private Context mContext = null;
    private int text_color = 0;
    private NotifyEvent mCbNotify = null;
    private NotifyEvent mSwNotify = null;
    private NotifyEvent mSyncButtonNotify = null;
    private ArrayList<ScheduleItem> mScheduleList = null;
    private GlobalParameters mGp=null;

    public AdapterScheduleList(Context c, int textViewResourceId, ArrayList<ScheduleItem> sl) {
        super(c, textViewResourceId, sl);
        layout_id = textViewResourceId;
        mContext = c;
        mScheduleList = sl;
        mGp=GlobalWorkArea.getGlobalParameters(c);
    }

    public void setCbNotify(NotifyEvent ntfy) {
        mCbNotify = ntfy;
    }

    public void setSwNotify(NotifyEvent ntfy) {
        mSwNotify = ntfy;
    }

    public void setSyncButtonNotify(NotifyEvent ntfy) {
        mSyncButtonNotify = ntfy;
    }

    public void sort() {
        sort(new Comparator<ScheduleItem>() {
            @Override
            public int compare(ScheduleItem lhs, ScheduleItem rhs) {
                return lhs.scheduleName.compareToIgnoreCase(rhs.scheduleName);
            }
        });
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (ScheduleItem si : mScheduleList) si.isChecked = true;
        notifyDataSetChanged();
    }

    public void unselectAll() {
        for (ScheduleItem si : mScheduleList) {
            si.isChecked = false;
        }
        notifyDataSetChanged();
    }

    private boolean mSelectMode = false;

    public void setSelectMode(boolean select_mode) {
        mSelectMode = select_mode;
        if (!mSelectMode) unselectAll();
        else notifyDataSetChanged();
    }

    public boolean isSelectMode() {
        return mSelectMode;
    }

    public int getSelectedItemCount() {
        int result=0;
        for(ScheduleItem si:mScheduleList) if (si.isChecked) result++;
        return result;
    }
//        @Override
//        public void add(ScheduleItem si) {
//            mScheduleList.add(si);
//        }
//
//        @Override
//        public ScheduleItem getItem(int pos) {
//            return mScheduleList.get(pos);
//        }
//        @Override
//        public int getCount() {
//            return mScheduleList.size();
//        }

    private Drawable ll_default_background_color=null;

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        final ScheduleItem o = getItem(position);
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(layout_id, null);
            holder = new ViewHolder();
            holder.ll_view=(LinearLayout)v.findViewById(R.id.schedule_sync_list_view);
            ll_default_background_color=holder.ll_view.getBackground();
            holder.tv_name = (TextView) v.findViewById(R.id.schedule_sync_list_name);
            holder.tv_info = (TextView) v.findViewById(R.id.schedule_sync_list_info);
            holder.tv_time_info = (TextView) v.findViewById(R.id.schedule_sync_list_time_info);
            holder.tv_error_info = (TextView) v.findViewById(R.id.schedule_sync_list_error_info);
            holder.tv_error_info.setTextColor(mGp.themeColorList.text_color_warning);
            holder.swEnabled=(Switch)v.findViewById(R.id.schedule_sync_list_switch);
            holder.cbChecked = (CheckBox) v.findViewById(R.id.schedule_sync_list_checked);
            holder.ib_sync_button=(ImageButton) v.findViewById(R.id.schedule_sync_list_sync);
            text_color = holder.tv_name.getCurrentTextColor();
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        if (o != null) {
            holder.tv_name.setText(o.scheduleName);
            holder.tv_info.setText(ScheduleUtil.buildSchedulerNextInfo(mContext, o));

            if (mSelectMode) {
                holder.cbChecked.setVisibility(CheckBox.VISIBLE);
                holder.swEnabled.setVisibility(CheckBox.GONE);
            } else {
                holder.cbChecked.setVisibility(CheckBox.GONE);
                holder.swEnabled.setVisibility(CheckBox.VISIBLE);
            }

            holder.ll_view.setBackground(ll_default_background_color);
            if (!mGp.settingScheduleSyncEnabled) {
                holder.ll_view.setBackgroundColor(mGp.themeColorList.text_color_disabled);
                holder.swEnabled.setEnabled(false);
            } else {
                holder.swEnabled.setEnabled(true);
            }
            String time_info = "";
            if (o.scheduleType.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_HOURS)) {
                time_info = mContext.getString(R.string.msgs_scheduler_main_spinner_sched_type_every_hour) + " " + o.scheduleMinutes + " " +
                        mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_minute);
            } else if (o.scheduleType.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_DAY)) {
                time_info = mContext.getString(R.string.msgs_scheduler_main_spinner_sched_type_every_day) + " " + o.scheduleHours + ":" + o.scheduleMinutes;
            } else if (o.scheduleType.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_MONTH)) {
                String ld=o.scheduleDay.equals("99")?mContext.getString(R.string.msgs_scheduler_info_last_day_of_the_month):o.scheduleDay;
                time_info = mContext.getString(R.string.msgs_scheduler_main_spinner_sched_type_every_month) + " " + ld + " " + o.scheduleHours + ":" + o.scheduleMinutes;
            } else if (o.scheduleType.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_DAY_OF_THE_WEEK)) {
                String day_of_the_week = "";
                if (o.scheduleDayOfTheWeek.substring(0, 1).equals("1")) {
                    if (day_of_the_week.length() == 0)
                        day_of_the_week += mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_sun);
                    else
                        day_of_the_week += "," + mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_sun);
                }
                if (o.scheduleDayOfTheWeek.substring(1, 2).equals("1")) {
                    if (day_of_the_week.length() == 0)
                        day_of_the_week += mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_mon);
                    else
                        day_of_the_week += "," + mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_mon);
                }
                if (o.scheduleDayOfTheWeek.substring(2, 3).equals("1")) {
                    if (day_of_the_week.length() == 0)
                        day_of_the_week += mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_tue);
                    else
                        day_of_the_week += "," + mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_tue);
                }
                if (o.scheduleDayOfTheWeek.substring(3, 4).equals("1")) {
                    if (day_of_the_week.length() == 0)
                        day_of_the_week += mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_wed);
                    else
                        day_of_the_week += "," + mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_wed);
                }
                if (o.scheduleDayOfTheWeek.substring(4, 5).equals("1")) {
                    if (day_of_the_week.length() == 0)
                        day_of_the_week += mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_thu);
                    else
                        day_of_the_week += "," + mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_thu);
                }
                if (o.scheduleDayOfTheWeek.substring(5, 6).equals("1")) {
                    if (day_of_the_week.length() == 0)
                        day_of_the_week += mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_fri);
                    else
                        day_of_the_week += "," + mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_fri);
                }
                if (o.scheduleDayOfTheWeek.substring(6, 7).equals("1")) {
                    if (day_of_the_week.length() == 0)
                        day_of_the_week += mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_sat);
                    else
                        day_of_the_week += "," + mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_sat);
                }
                time_info = mContext.getString(R.string.msgs_scheduler_main_spinner_sched_type_day_of_week) +
                        " " + day_of_the_week + " " + o.scheduleHours + ":" + o.scheduleMinutes;
            } else if (o.scheduleType.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_INTERVAL)) {
                time_info = mContext.getString(R.string.msgs_scheduler_main_spinner_sched_type_interval) + " " + o.scheduleMinutes + " " +
                        mContext.getString(R.string.msgs_scheduler_main_dlg_hdr_minute);
            }

            if (isSelectMode()) holder.ib_sync_button.setVisibility(ImageButton.INVISIBLE);
            else holder.ib_sync_button.setVisibility(ImageButton.VISIBLE);

            String sync_prof = "", error_msg = "";
            holder.tv_error_info.setVisibility(TextView.GONE);
            error_msg=ScheduleUtil.isValidScheduleItem(mContext, mGp, mScheduleList, o, true, true);

            if (o.syncAutoSyncTask) {
                sync_prof = mContext.getString(R.string.msgs_scheduler_info_sync_all_active_profile);
            } else {
                sync_prof = String.format(mContext.getString(R.string.msgs_scheduler_info_sync_selected_profile), o.syncTaskList);
            }

            if (!error_msg.equals("")) {
                holder.ib_sync_button.setVisibility(ImageButton.INVISIBLE);
                holder.tv_error_info.setText(error_msg);
                holder.tv_error_info.setVisibility(TextView.VISIBLE);
            }

            holder.tv_time_info.setText(time_info + " " + sync_prof);

            holder.cbChecked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                        holder.cbChecked.toggle();
                    boolean isChecked = holder.cbChecked.isChecked();
                    o.isChecked = isChecked;
                    if (mCbNotify != null)
                        mCbNotify.notifyToListener(true, new Object[]{isChecked});
                }
            });

            holder.cbChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     o.isChecked = isChecked;
                     if (mCbNotify != null)
                         mCbNotify.notifyToListener(true, new Object[]{isChecked});
                 }
            });
            holder.cbChecked.setChecked(o.isChecked);

            holder.swEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (o.scheduleEnabled!=isChecked) {
                        o.scheduleEnabled = isChecked;
                        if (mSwNotify != null)
                            mSwNotify.notifyToListener(true, new Object[]{position});
                    }
                    notifyDataSetChanged();
                }
            });
            holder.swEnabled.setChecked(o.scheduleEnabled);

            holder.ib_sync_button.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     if (mSyncButtonNotify!=null) mSyncButtonNotify.notifyToListener(true, new Object[]{o});
                 }
            });

            holder.ib_sync_button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CommonDialog.showPopupMessageAsUpAnchorView((ActivityMain)(parent.getContext()), v,
                            mContext.getString(R.string.msgs_schedule_list_edit_execute_selected_schedule_with_schedule_name, o.scheduleName), 2);
                    return true;
                }
            });

        }
        return v;

    }

    class ViewHolder {
        TextView tv_name, tv_info, tv_enabled, tv_time_info, tv_error_info;
        LinearLayout ll_view;
        CheckBox cbChecked;
        Switch swEnabled;
        ImageButton ib_sync_button;
    }

}
