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

//import static com.sentaroh.android.SMBSync2.Constants.*;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.SafManager;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.ThemeUtil;

import java.util.ArrayList;

public class AdapterSyncTask extends ArrayAdapter<SyncTaskItem> {

    private Context mContext;
    private int id;
    private ArrayList<SyncTaskItem> items;
    @SuppressWarnings("unused")
    private String tv_active_active, tv_active_inact, tv_no_sync, tv_status_running, tv_active_test;
    private String tv_status_success, tv_status_error, tv_status_cancel, tv_status_warning;

    private ThemeColorList mThemeColorList;

    private GlobalParameters mGp = null;
    private CommonUtilities mUtil = null;

    private ColorStateList mTextColor=null;

    public AdapterSyncTask(Context c, int textViewResourceId,
                           ArrayList<SyncTaskItem> objects, GlobalParameters gp) {
        super(c, textViewResourceId, objects);
        mContext = c;
        id = textViewResourceId;
        items = objects;

        tv_active_active = mContext.getString(R.string.msgs_main_sync_list_array_activ_activ);
        tv_active_test = mContext.getString(R.string.msgs_main_sync_list_array_activ_test);
        tv_active_inact = mContext.getString(R.string.msgs_main_sync_list_array_activ_inact);
        tv_no_sync = mContext.getString(R.string.msgs_main_sync_list_array_no_last_sync_time);
        tv_status_running = mContext.getString(R.string.msgs_main_sync_history_status_running);
        tv_status_success = mContext.getString(R.string.msgs_main_sync_history_status_success);
        tv_status_error = mContext.getString(R.string.msgs_main_sync_history_status_error);
        tv_status_cancel = mContext.getString(R.string.msgs_main_sync_history_status_cancel);
        tv_status_warning = mContext.getString(R.string.msgs_main_sync_history_status_warning);

        mThemeColorList = CommonUtilities.getThemeColorList(c);

        mGp = gp;
        mUtil = new CommonUtilities(mContext, "AdapterSynctask", mGp, null);

    }

    public SyncTaskItem getItem(int i) {
        return items.get(i);
    }

    public void remove(int i) {
        items.remove(i);
        notifyDataSetChanged();
    }

    public void replace(SyncTaskItem pli, int i) {
        items.set(i, pli);
        notifyDataSetChanged();
    }

    private NotifyEvent mNotifySyncButtonEvent = null;

    public void setNotifySyncButtonEventHandler(NotifyEvent ntfy) {
        mNotifySyncButtonEvent = ntfy;
    }

    private NotifyEvent mNotifyCheckBoxEvent = null;

    public void setNotifyCheckBoxEventHandler(NotifyEvent ntfy) {
        mNotifyCheckBoxEvent = ntfy;
    }

    private boolean isShowCheckBox = false;

    public void setShowCheckBox(boolean p) {
        isShowCheckBox = p;
    }

    public boolean isShowCheckBox() {
        return isShowCheckBox;
    }

    public void setAllItemChecked(boolean p) {
        if (items != null) {
            for (int i = 0; i < items.size(); i++) items.get(i).setChecked(p);
        }
    }

    public boolean isEmptyAdapter() {
        boolean result = false;
        if (items != null) {
            if (items.size() == 0 || items.get(0).getSyncTaskType().equals("")) result = true;
        } else {
            result = true;
        }
        return result;
    }

    public ArrayList<SyncTaskItem> getArrayList() {
        return items;
    }

    public void setArrayList(ArrayList<SyncTaskItem> p) {
        items.clear();
        if (p != null) {
            for (int i = 0; i < p.size(); i++) items.add(p.get(i));
        }
        notifyDataSetChanged();
    }

    public void setArrayList(ArrayList<SyncTaskItem> p, boolean notify_data_set_changed) {
        items.clear();
        if (p != null) {
            for (int i = 0; i < p.size(); i++) items.add(p.get(i));
        }
        if (notify_data_set_changed) notifyDataSetChanged();
    }

    public void sort() {
        SyncTaskUtil.sortSyncTaskList(items);
    }


//		@Override
//		public boolean isEnabled(int idx) {
//			 return getItem(idx).getActive().equals("A");
//		}

    private Drawable ll_default = null;

    @SuppressWarnings("deprecation")
    @Override
    final public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final SyncTaskItem o = getItem(position);

        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
            holder = new ViewHolder();
            holder.tv_row_name = (TextView) v.findViewById(R.id.sync_task_name);
            holder.tv_row_active = (TextView) v.findViewById(R.id.sync_task_enabled);
            holder.cbv_row_cb1 = (CheckBox) v.findViewById(R.id.sync_task_selected);
            holder.ib_row_sync=(ImageButton)v.findViewById(R.id.sync_task_perform_sync);

            holder.tv_row_master = (TextView) v.findViewById(R.id.sync_task_master_info);
            holder.tv_row_target = (TextView) v.findViewById(R.id.sync_task_target_info);
            holder.tv_row_synctype = (TextView) v.findViewById(R.id.sync_task_sync_type);
            holder.iv_row_sync_dir_image = (ImageView) v.findViewById(R.id.sync_task_direction_image);
            holder.iv_row_image_master = (ImageView) v.findViewById(R.id.sync_task_master_icon);
            holder.iv_row_image_target = (ImageView) v.findViewById(R.id.sync_task_target_icon);
//            if (o != null && o.isSyncOptionEnsureTargetIsExactMirror()) holder.tv_mtype_mirror = mContext.getString(R.string.msgs_main_sync_list_array_mtype_mirr_img);
//            else holder.tv_mtype_mirror = mContext.getString(R.string.msgs_main_sync_list_array_mtype_mirr);
            holder.tv_mtype_mirror = mContext.getString(R.string.msgs_main_sync_list_array_mtype_mirr);
            //mUtil.addDebugMsg(2, "I", "o.isSyncOptionEnsureTargetIsExactMirror()=" + o.isSyncOptionEnsureTargetIsExactMirror());
            holder.tv_mtype_copy = mContext.getString(R.string.msgs_main_sync_list_array_mtype_copy);
            holder.tv_mtype_move = mContext.getString(R.string.msgs_main_sync_list_array_mtype_move);
            holder.tv_mtype_sync = mContext.getString(R.string.msgs_main_sync_list_array_mtype_sync);
            holder.tv_mtype_archive = mContext.getString(R.string.msgs_main_sync_list_array_mtype_archive);

            holder.ll_sync = (LinearLayout) v.findViewById(R.id.profile_list_sync_layout);
            holder.ll_entry = (LinearLayout) v.findViewById(R.id.profile_list_entry_layout);
            holder.ll_view = (LinearLayout) v.findViewById(R.id.profile_list_view);
            if (ll_default != null) ll_default = holder.ll_view.getBackground();

            holder.tv_last_sync_result = (TextView) v.findViewById(R.id.sync_task_sync_result_text);
            holder.ll_last_sync = (LinearLayout) v.findViewById(R.id.sync_task_sync_result_view);

            if (mTextColor==null) mTextColor=holder.tv_row_name.getTextColors();
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
//            if (o != null && o.isSyncOptionEnsureTargetIsExactMirror()) holder.tv_mtype_mirror = mContext.getString(R.string.msgs_main_sync_list_array_mtype_mirr_img);
//            else holder.tv_mtype_mirror = mContext.getString(R.string.msgs_main_sync_list_array_mtype_mirr);
            holder.tv_mtype_mirror = mContext.getString(R.string.msgs_main_sync_list_array_mtype_mirr);
        }

        if (o != null) {
            boolean sync_btn_disable=false;

            holder.ll_view.setBackgroundDrawable(ll_default);
//            holder.ib_row_sync.setBackgroundDrawable(ll_default);

            String act = "";
            if (o.isSyncTaskAuto()) {
                if (!o.isSyncTestMode()) act = tv_active_active;
                else act = tv_active_test;
            } else {
                if (!o.isSyncTestMode()) act = tv_active_inact;
                else act = tv_active_test;
            }
            holder.tv_row_active.setText(act);
            holder.tv_row_name.setText(o.getSyncTaskName());

            holder.ll_sync.setVisibility(LinearLayout.VISIBLE);
            holder.ll_last_sync.setVisibility(LinearLayout.VISIBLE);
            holder.tv_row_active.setVisibility(LinearLayout.VISIBLE);
            holder.cbv_row_cb1.setVisibility(LinearLayout.VISIBLE);

            String synctp = "";

            if (o.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MIRROR))
                synctp = holder.tv_mtype_mirror;
            else if (o.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_MOVE))
                synctp = holder.tv_mtype_move;
            else if (o.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_COPY))
                synctp = holder.tv_mtype_copy;
            else if (o.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_SYNC))
                synctp = holder.tv_mtype_sync;
            else if (o.getSyncTaskType().equals(SyncTaskItem.SYNC_TASK_TYPE_ARCHIVE))
                synctp = holder.tv_mtype_archive;
            else synctp = "ERR";

//            if (o.isSyncOptionEnsureTargetIsExactMirror()) holder.tv_row_synctype.setTextColor(mThemeColorList.text_color_warning);
//            else holder.tv_row_synctype.setTextColor(mTextColor);
            holder.tv_row_synctype.setTextColor(mTextColor);
            holder.tv_row_synctype.setText(synctp);

            String result = "";
            if (o.isSyncTaskRunning()) {
                result = tv_status_running;
                if (ThemeUtil.isLightThemeUsed(mContext))
                    holder.ll_view.setBackgroundColor(Color.argb(255, 0, 192, 192));
                else holder.ll_view.setBackgroundColor(Color.argb(255, 0, 128, 128));
            } else {
                if (o.getLastSyncResult() == SyncHistoryItem.SYNC_STATUS_SUCCESS) {
                    result = tv_status_success;
                } else if (o.getLastSyncResult() == SyncHistoryItem.SYNC_STATUS_CANCEL) {
                    result = tv_status_cancel;
                } else if (o.getLastSyncResult() == SyncHistoryItem.SYNC_STATUS_ERROR) {
                    result = tv_status_error;
                } else if (o.getLastSyncResult() == SyncHistoryItem.SYNC_STATUS_WARNING) {
                    result = tv_status_warning;
                }
            }
            if (!o.getLastSyncTime().equals("")) {
                holder.ll_last_sync.setVisibility(LinearLayout.VISIBLE);
                holder.tv_last_sync_result.setTextColor(mTextColor);
                holder.tv_last_sync_result.setText(o.getLastSyncTime()+" - "+result);
            } else {
                holder.ll_last_sync.setVisibility(LinearLayout.GONE);
            }
            if (!o.isSyncTaskError()) {
                if (o.isSyncTestMode()) {
                    if (ThemeUtil.isLightThemeUsed(mContext))
                        holder.ll_view.setBackgroundColor(Color.argb(64, 255, 32, 255));
                    else holder.ll_view.setBackgroundColor(Color.argb(64, 255, 0, 128));
                }
            } else {
                holder.ll_view.setBackgroundColor(Color.argb(64, 255, 0, 0));
            }
            String master_dir = o.getMasterDirectoryName().startsWith("/")?o.getMasterDirectoryName().substring(1):o.getMasterDirectoryName();
            if (o.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
                if (master_dir.equals("")) holder.tv_row_master.setText(o.getMasterLocalMountPoint());
                else holder.tv_row_master.setText(o.getMasterLocalMountPoint() + "/" + master_dir);
                holder.iv_row_image_master.setImageResource(R.drawable.ic_32_mobile);
            } else if (o.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                if (master_dir.equals("")) holder.tv_row_master.setText((mGp.safMgr.getSdcardRootPath()));
                else holder.tv_row_master.setText((mGp.safMgr.getSdcardRootPath() + "/" + master_dir));
                if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                    holder.iv_row_image_master.setImageResource(R.drawable.ic_32_sdcard_bad);
                    sync_btn_disable=true;
                } else {
                    holder.iv_row_image_master.setImageResource(R.drawable.ic_32_sdcard);
                }
            } else if (o.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
                if (master_dir.equals("")) holder.tv_row_master.setText((mGp.safMgr.getUsbRootPath()));
                else holder.tv_row_master.setText((mGp.safMgr.getUsbRootPath() + "/" + master_dir));
                if (mGp.safMgr.getUsbRootPath().equals(SafManager.UNKNOWN_USB_DIRECTORY)) {
                    holder.iv_row_image_master.setImageResource(R.drawable.ic_32_usb_bad);
                    sync_btn_disable=true;
                } else {
                    holder.iv_row_image_master.setImageResource(R.drawable.ic_32_usb);
                }
            } else if (o.getMasterFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                String host = o.getMasterSmbAddr();
                if (o.getMasterSmbAddr().equals("")) host = o.getMasterSmbHostName();
                String share = o.getMasterSmbShareName();
                if (master_dir.equals("")) holder.tv_row_master.setText("smb://" + host + "/" + share);
                else {
                    holder.tv_row_master.setText("smb://" + host + "/" + share + "/"+ master_dir);
                }
                holder.iv_row_image_master.setImageResource(R.drawable.ic_32_server);
            }
            holder.tv_row_master.requestLayout();
            String target_dir = o.getTargetDirectoryName().startsWith("/")?o.getTargetDirectoryName().substring(1):o.getTargetDirectoryName();
            if (o.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
                if (target_dir.equals("")) holder.tv_row_target.setText(o.getTargetLocalMountPoint());
                else holder.tv_row_target.setText(o.getTargetLocalMountPoint() + "/" + target_dir);
                holder.iv_row_image_target.setImageResource(R.drawable.ic_32_mobile);
            } else if (o.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD)) {
                if (target_dir.equals("")) holder.tv_row_target.setText((mGp.safMgr.getSdcardRootPath()));
                else holder.tv_row_target.setText((mGp.safMgr.getSdcardRootPath() + "/" + target_dir));
                if (mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                    holder.iv_row_image_target.setImageResource(R.drawable.ic_32_sdcard_bad);
                    sync_btn_disable=true;
                } else {
                    holder.iv_row_image_target.setImageResource(R.drawable.ic_32_sdcard);
                }
            } else if (o.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_USB)) {
                if (target_dir.equals("")) holder.tv_row_target.setText((mGp.safMgr.getUsbRootPath()));
                else holder.tv_row_target.setText((mGp.safMgr.getUsbRootPath() + "/" + target_dir));
                if (mGp.safMgr.getUsbRootPath().equals(SafManager.UNKNOWN_USB_DIRECTORY)) {
                    holder.iv_row_image_target.setImageResource(R.drawable.ic_32_usb_bad);
                    sync_btn_disable=true;
                } else {
                    holder.iv_row_image_target.setImageResource(R.drawable.ic_32_usb);
                }
            } else if (o.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_ZIP)) {
                holder.iv_row_image_target.setImageResource(R.drawable.ic_32_archive);
                if (!o.isTargetZipUseExternalSdcard())
                    holder.tv_row_target.setText((o.getTargetLocalMountPoint() + o.getTargetZipOutputFileName()));
                else
                    holder.tv_row_target.setText((o.getTargetLocalMountPoint() + o.getTargetZipOutputFileName()));
                if (o.isTargetZipUseExternalSdcard() &&
                        mGp.safMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                    holder.iv_row_image_target.setImageResource(R.drawable.ic_32_sdcard_bad);
                    sync_btn_disable=true;
                } else {
                    holder.iv_row_image_target.setImageResource(R.drawable.ic_32_archive);
                }
            } else if (o.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
                String host = o.getTargetSmbAddr();
                if (o.getTargetSmbAddr().equals("")) host = o.getTargetSmbHostName();
                String share = o.getTargetSmbShareName();
                if (target_dir.equals("")) holder.tv_row_target.setText("smb://" + host + "/" + share);
                else {
                    holder.tv_row_target.setText("smb://" + host + "/" + share + "/"+ target_dir);
                }
                holder.iv_row_image_target.setImageResource(R.drawable.ic_32_server);
            }
            holder.tv_row_target.requestLayout();

            if (isShowCheckBox) {
                holder.cbv_row_cb1.setVisibility(CheckBox.VISIBLE);
                holder.ib_row_sync.setVisibility(CheckBox.GONE);
            } else {
                holder.cbv_row_cb1.setVisibility(CheckBox.GONE);
                if (o.isSyncTaskError() || sync_btn_disable) {
                    holder.ib_row_sync.setVisibility(CheckBox.INVISIBLE);
                } else {
                    holder.ib_row_sync.setVisibility(CheckBox.VISIBLE);
                }
            }
            final int p = position;

            holder.ib_row_sync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!o.isSyncTaskError()) {
                        holder.ib_row_sync.setEnabled(false);
                        if (mNotifySyncButtonEvent!=null) mNotifySyncButtonEvent.notifyToListener(true,new Object[]{o});
                        holder.ib_row_sync.postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                holder.ib_row_sync.setEnabled(true);
                            }
                        },1000);
                    }
                }
            });

            holder.cbv_row_cb1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    o.setChecked(isChecked);
                    items.set(p, o);
                    if (mNotifyCheckBoxEvent != null && isShowCheckBox)
                        mNotifyCheckBoxEvent.notifyToListener(true, null);
                }
            });
            holder.cbv_row_cb1.setChecked(items.get(position).isChecked());

            notifyDataSetChanged();
        }
        return v;
    }

    static class ViewHolder {
        TextView tv_row_name, tv_row_active;
        CheckBox cbv_row_cb1;
        ImageButton ib_row_sync;

        TextView tv_row_synctype, tv_row_master, tv_row_target;
        ImageView iv_row_sync_dir_image;
        ImageView iv_row_image_master, iv_row_image_target;
        String tv_mtype_mirror, tv_mtype_move, tv_mtype_copy, tv_mtype_sync, tv_mtype_archive;

        TextView tv_dir_const;

        TextView tv_last_sync_result;
        LinearLayout ll_sync, ll_entry, ll_last_sync, ll_view;
    }
}