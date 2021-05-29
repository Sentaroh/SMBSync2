package com.sentaroh.android.SMBSync2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.sentaroh.android.Utilities.CallBackListener;
import com.sentaroh.android.Utilities.ContextButton.ContextButtonUtil;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.NotifyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.sentaroh.android.SMBSync2.Constants.SYNC_TASK_LIST_SEPARATOR;


public class EditSyncTaskList {
    
    private Activity mActivity=null;
    private GlobalParameters mGp=null;
    private CommonUtilities mUtil=null;
    private ArrayList<DataListItem> mEditDataList =null;
    
    public EditSyncTaskList(Activity a, GlobalParameters gp, CommonUtilities cu) {
        mActivity=a;
        mGp=gp;
        mUtil=cu;
    }
    
    public void editSyncTaskList(final String prof_list, final NotifyEvent p_ntfy) {
        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.edit_data_list_dlg);

        final LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.edit_data_list_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.edit_data_list_dlg_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        final TextView dlg_title = (TextView) dialog.findViewById(R.id.edit_data_list_dlg_title);
        dlg_title.setTextColor(mGp.themeColorList.title_text_color);

        final ImageButton ib_help=(ImageButton)dialog.findViewById(R.id.edit_data_list_dlg_help);

        ib_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SyncTaskEditor.showFieldHelp(mActivity, mGp, mActivity.getString(R.string.msgs_edit_sync_task_list_help_title),
                        mActivity.getString(R.string.msgs_edit_sync_task_list_help_file));
            }
        });


//        final TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_sync_task_list_dlg_msg);

        final ImageButton ib_add=(ImageButton)dialog.findViewById(R.id.context_button_add);
        ContextButtonUtil.setButtonLabelListener(mActivity, ib_add,
                mActivity.getString(R.string.msgs_edit_sync_task_list_cont_label_add_sync_task));
        final ImageButton ib_delete = (ImageButton) dialog.findViewById(R.id.context_button_delete);
        ContextButtonUtil.setButtonLabelListener(mActivity, ib_delete,
                mActivity.getString(R.string.msgs_edit_sync_task_list_cont_label_remove_sync_task));
        final ImageButton ib_select_all = (ImageButton) dialog.findViewById(R.id.context_button_select_all);
        ContextButtonUtil.setButtonLabelListener(mActivity, ib_select_all,
                mActivity.getString(R.string.msgs_edit_sync_task_list_cont_label_select_all));
        final ImageButton ib_unselect_all = (ImageButton) dialog.findViewById(R.id.context_button_unselect_all);
        ContextButtonUtil.setButtonLabelListener(mActivity, ib_unselect_all,
                mActivity.getString(R.string.msgs_edit_sync_task_list_cont_label_unselect_all));


        final Button btn_ok = (Button) dialog.findViewById(R.id.edit_data_list_dlg_ok);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.edit_data_list_dlg_cancel);

        final RecyclerView rv_task_list = (RecyclerView) dialog.findViewById(R.id.edit_data_list_dlg_recycler_view);
        String[]task_array=prof_list.split(SYNC_TASK_LIST_SEPARATOR);
        mEditDataList=new ArrayList<DataListItem>();
        for(String item:task_array) {
            if (!item.equals("")) {
                DataListItem etli=new DataListItem();
                etli.item_name =item;
                mEditDataList.add(etli);
            }
        }
        final EditDataListAdapter data_list_adapter = new EditDataListAdapter(mEditDataList);

        rv_task_list.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        rv_task_list.setLayoutManager(layoutManager);
        rv_task_list.setAdapter(data_list_adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv_task_list.getContext(),
                new LinearLayoutManager(mActivity).getOrientation());
        rv_task_list.addItemDecoration(dividerItemDecoration);

        ItemTouchHelper.SimpleCallback scb=new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN , 0) {
            private Drawable defaultBackGroundColor=null;

            @Override
            public boolean isLongPressDragEnabled() {
                return data_list_adapter.isDragDropEnabled();
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int action_state) {
                mUtil.addDebugMsg(1, "I", "onSelectedChanged state="+action_state);
                if (viewHolder!=null && viewHolder.itemView!=null) {
                    viewHolder.itemView.setAlpha(0.5f);
//                    if (defaultBackGroundColor==null)defaultBackGroundColor=viewHolder.itemView.getBackground();
//                    if (ThemeUtil.isLightThemeUsed(mActivity)) viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
//                    else viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
                }
                if (action_state==ItemTouchHelper.ACTION_STATE_IDLE) {
                    for(int i=0;i<mEditDataList.size();i++) data_list_adapter.notifyItemChanged(i);
                    if (isSyncTaskListChanged(prof_list, data_list_adapter)) {
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                    } else {
                        CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                    }
                }
            }

            @Override
            public void clearView(RecyclerView recycle_view, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.itemView!=null) {
                    viewHolder.itemView.setAlpha(1.0f);
                }
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();
                DataListItem fromTask=mEditDataList.get(fromPos);
                data_list_adapter.notifyItemMoved(fromPos, toPos);
                mEditDataList.remove(fromPos);
                mEditDataList.add(toPos, fromTask);
                return true;// true if moved, false otherwise
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//                final int fromPos = viewHolder.getAdapterPosition();
//                dataset.remove(fromPos);
//                adapter.notifyItemRemoved(fromPos);
            }
        };
        ItemTouchHelper ith  = new ItemTouchHelper(scb);
        ith.attachToRecyclerView(rv_task_list);

        CommonDialog.setViewEnabled(mActivity, btn_ok, false);

        setContextViewVisibility(dialog, data_list_adapter);

        data_list_adapter.setNotifyTextViewClicked(new CallBackListener() {
            @Override
            public boolean onCallBack(Context context, Object o, Object[] objects) {
                setContextViewVisibility(dialog, data_list_adapter);
                return true;
            }
        });

        data_list_adapter.setNotifyCheckBox(new CallBackListener() {
            @Override
            public boolean onCallBack(Context context, Object o, Object[] objects) {
                setContextViewVisibility(dialog, data_list_adapter);
                return true;
            }
        });

        data_list_adapter.setNotifyHandleTouch(new CallBackListener() {
            @Override
            public boolean onCallBack(Context context, Object o, Object[] objects) {
                if (data_list_adapter.isDragDropEnabled()) {
                    EditDataListAdapter.ViewHolder vh=(EditDataListAdapter.ViewHolder)objects[0];
                    ith.startDrag(vh);
                }
                return true;
            }
        });

        ib_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String curr_task_list=buildSyncTaskList(data_list_adapter);
                NotifyEvent ntfy=new NotifyEvent(mActivity);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        String[] add_list_array=((String)objects[0]).split(SYNC_TASK_LIST_SEPARATOR);
                        for(String item:add_list_array) {
                            DataListItem etli=new DataListItem();
                            etli.item_name =item;
                            mEditDataList.add(etli);
                        }
                        data_list_adapter.unselectAllItem();
                        data_list_adapter.applyDataSetChanged();
                        setOkButtonEnabledEditSyncTaskList(dialog, curr_task_list, data_list_adapter);
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                        setContextViewVisibility(dialog, data_list_adapter);
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {

                    }
                });
                addTaskListDlg(curr_task_list, ntfy);
            }
        });

        ib_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSyncTaskFromTaskList(dialog, data_list_adapter, prof_list);
            }
        });

        ib_select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data_list_adapter.selectAllItem();
                setContextViewVisibility(dialog, data_list_adapter);
            }
        });

        ib_unselect_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data_list_adapter.unselectAllItem();
                setContextViewVisibility(dialog, data_list_adapter);
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String n_prof_list = buildSyncTaskList(data_list_adapter);
                p_ntfy.notifyToListener(true, new Object[]{n_prof_list});
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSyncTaskListChanged(prof_list, data_list_adapter)) {
                    NotifyEvent ntfy=new NotifyEvent(mActivity);
                    ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            dialog.dismiss();
                            p_ntfy.notifyToListener(false, null);
                        }

                        @Override
                        public void negativeResponse(Context context, Object[] objects) {
                        }
                    });
                    mUtil.showCommonDialog(true, "W",
                            mActivity.getString(R.string.msgs_edit_sync_task_list_confirm_msg_nosave), "", ntfy);
                    return;
                }
                dialog.dismiss();
                p_ntfy.notifyToListener(false, null);
            }
        });

        setOkButtonEnabledEditSyncTaskList(dialog, prof_list, data_list_adapter);

        dialog.show();
    }

    private void setContextViewVisibility(Dialog dialog, EditDataListAdapter adapter) {
        final ImageButton ib_add=(ImageButton)dialog.findViewById(R.id.context_button_add);
        final ImageButton ib_delete = (ImageButton) dialog.findViewById(R.id.context_button_delete);
        final ImageButton ib_select_all = (ImageButton) dialog.findViewById(R.id.context_button_select_all);
        final ImageButton ib_unselect_all = (ImageButton) dialog.findViewById(R.id.context_button_unselect_all);

        final String curr_task_list=buildSyncTaskList(adapter);
        ArrayList<DataListItem>add_task_list= getAddableTaskList(curr_task_list);
        if (add_task_list.size()==0) {
            ib_add.setVisibility(Button.INVISIBLE);
        } else {
            ib_add.setVisibility(Button.VISIBLE);
        }

        if (adapter.isAllItemChecked()) ib_select_all.setVisibility(ImageButton.INVISIBLE);
        else ib_select_all.setVisibility(ImageButton.VISIBLE);

        if (adapter.isAnyItemChecked()) ib_unselect_all.setVisibility(ImageButton.VISIBLE);
        else ib_unselect_all.setVisibility(ImageButton.INVISIBLE);

        if (adapter.isAnyItemChecked()) ib_delete.setVisibility(ImageButton.VISIBLE);
        else ib_delete.setVisibility(ImageButton.INVISIBLE);
    }

    private void removeSyncTaskFromTaskList(Dialog dialog, EditDataListAdapter data_list_adapter, String org_list) {
        final Button btn_ok = (Button) dialog.findViewById(R.id.edit_data_list_dlg_ok);
//        final LinearLayout dlg_select_view = (LinearLayout) dialog.findViewById(R.id.edit_sync_task_list_dlg_select_view);
        final ArrayList<DataListItem> del_task=new ArrayList<DataListItem>();
        NotifyEvent ntfy_conf=new NotifyEvent(mActivity);
        ntfy_conf.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                for(DataListItem etli:del_task) {
                    mEditDataList.remove(etli);
                }
                data_list_adapter.applyDataSetChanged();
                if (isSyncTaskListChanged(org_list, data_list_adapter)) CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                else CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                setContextViewVisibility(dialog, data_list_adapter);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        String del_list="", sep="";
        for(DataListItem etli:mEditDataList) {
            if (etli.item_checked) {
                del_list+=sep+etli.item_name;
                sep=", ";
                del_task.add(etli);
            }
        }
        mUtil.showCommonDialog(true, "W", mActivity.getString(R.string.msgs_edit_sync_task_list_delete_sync_task), del_list, ntfy_conf);

    }

    private String buildSyncTaskList(EditDataListAdapter data_list_adapter) {
        String n_prof_list = "", sep = "";
        for (int i = 0; i < data_list_adapter.getItemCount(); i++) {
            n_prof_list = n_prof_list + sep + mEditDataList.get(i).item_name;
            sep = SYNC_TASK_LIST_SEPARATOR;
        }
        return n_prof_list;
    }

    private boolean isSyncTaskListChanged(String org_list, EditDataListAdapter data_list_adapter) {
        String new_list=buildSyncTaskList(data_list_adapter);
        if (org_list.equals(new_list)) return false;
        return true;
    }

    private void setOkButtonEnabledEditSyncTaskList(Dialog dialog, String org_task_list,
                                                    EditDataListAdapter data_list_adapter) {
        final RecyclerView lv_sync_list = (RecyclerView) dialog.findViewById(R.id.edit_data_list_dlg_recycler_view);
        final Button btn_ok = (Button) dialog.findViewById(R.id.edit_data_list_dlg_ok);
        TextView dlg_msg = (TextView) dialog.findViewById(R.id.edit_data_list_dlg_msg);
        boolean selected=false;
        String task_list="", sep="";
        for (int i = 0; i < data_list_adapter.getItemCount(); i++) {
            task_list+=sep+mEditDataList.get(i).item_name;
            sep= SYNC_TASK_LIST_SEPARATOR;
        }
        if (task_list.equals("")) {
            dlg_msg.setVisibility(TextView.VISIBLE);
            dlg_msg.setText(mActivity.getString(R.string.msgs_edit_sync_task_list_info_sync_task_list_was_empty));
            CommonDialog.setViewEnabled(mActivity, btn_ok, false);
            return;
        } else {
            dlg_msg.setVisibility(TextView.GONE);
        }
        dlg_msg.setText("");
        if (!task_list.equals(org_task_list)) {
            CommonDialog.setViewEnabled(mActivity, btn_ok, true);
        } else {
            CommonDialog.setViewEnabled(mActivity, btn_ok, false);
        }
    }

    private ArrayList<DataListItem> getAddableTaskList(final String current_task_list) {
        ArrayList<DataListItem>add_task_list=new ArrayList<DataListItem>();
        String[] curr_task_list_array=current_task_list.split(SYNC_TASK_LIST_SEPARATOR);
        for(SyncTaskItem item:mGp.syncTaskList) {
            String e_msg= SyncTaskUtil.hasSyncTaskNameUnusableCharacter(mActivity, item.getSyncTaskName());
            if (e_msg.equals("")) {
                boolean found=false;
                for(String curr_item:curr_task_list_array) {
                    if (curr_item.equals(item.getSyncTaskName())) {
                        found=true;
                        break;
                    }
                }
                if (!found) {
                    DataListItem atli=new DataListItem();
                    atli.item_name =item.getSyncTaskName();
                    add_task_list.add(atli);
                }
            }
        }
        return add_task_list;
    }

    private class DataListItem {
        public String item_name ="";
        public boolean item_checked =false;
    }

    private class EditDataListAdapter extends RecyclerView.Adapter<EditDataListAdapter.ViewHolder> {
        private ArrayList<DataListItem> recyclerViewDataList = new ArrayList<>();

        private CallBackListener mNtfyDeleteButtonClick=null;
        public void setNotifyDeleteButton(CallBackListener ntfy) {mNtfyDeleteButtonClick=ntfy;}

        private CallBackListener mNtfyCheckBox =null;
        public void setNotifyCheckBox(CallBackListener ntfy) {mNtfyCheckBox =ntfy;}

        private CallBackListener mNtfyHandleTouch =null;
        public void setNotifyHandleTouch(CallBackListener ntfy) {mNtfyHandleTouch =ntfy;}

        private CallBackListener mNtfyTextViewClicked =null;
        public void setNotifyTextViewClicked(CallBackListener ntfy) {mNtfyTextViewClicked =ntfy;}

        private CallBackListener mNtfyTextViewLongClicked =null;
        public void setNotifyTextViewLongClicked(CallBackListener ntfy) {mNtfyTextViewLongClicked =ntfy;}

        private boolean mDropDropEnabled=true;
        public boolean isDragDropEnabled() {return mDropDropEnabled;}
        public void setDragDropEnabled(boolean enabled) {mDropDropEnabled=enabled;}

        private class ViewHolder extends RecyclerView.ViewHolder {
            TextView mItemName, mErrorMessage;
            ImageButton mHandle;
            CheckBox mCheckBox;
            LinearLayout mTextAreaView;
            ViewHolder(View v) {
                super(v);
                mCheckBox = (CheckBox) v.findViewById(R.id.edit_data_list_item_check_box);
                mItemName = (TextView)v.findViewById(R.id.edit_data_list_item_name);
                mErrorMessage = (TextView)v.findViewById(R.id.edit_data_list_item_error_message);
                mHandle = (ImageButton) v.findViewById(R.id.edit_data_list_item_handle);
                mTextAreaView = (LinearLayout) v.findViewById(R.id.edit_data_list_item_text_area_view);
            }
        }

        public EditDataListAdapter(ArrayList<DataListItem> dataset) {
            recyclerViewDataList = dataset;
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_data_list_item, parent, false);
            return new ViewHolder(view);
        }

        public boolean isAnyItemChecked() {
            for(DataListItem etli:recyclerViewDataList) {
                if (etli.item_checked) return true;
            }
            return false;
        }

        public boolean isAllItemChecked() {
            boolean result=true;
            for(DataListItem etli:recyclerViewDataList) {
                if (!etli.item_checked) result=false;
            }
            return result;
        }

        public void selectAllItem() {
            for(DataListItem item:recyclerViewDataList){
                item.item_checked=true;
            }
            applyDataSetChanged();
        }

        public void unselectAllItem() {
            for(DataListItem item:recyclerViewDataList){
                item.item_checked=false;
            }
            applyDataSetChanged();
        }

        public void applyDataSetChanged() {
            super.notifyDataSetChanged();
            if (recyclerViewDataList.size()>1) {
                if (isAnyItemChecked()) setDragDropEnabled(false);
                else setDragDropEnabled(true);
            } else {
                setDragDropEnabled(false);
            }
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final DataListItem o=recyclerViewDataList.get(position);
            holder.mItemName.setText(o.item_name);
            String[]task_array=o.item_name.split(SYNC_TASK_LIST_SEPARATOR);
            holder.mErrorMessage.setText("");
            for(String item:task_array) {
                SyncTaskItem sti= SyncTaskUtil.getSyncTaskByName(mGp.syncTaskList, item);
                if (sti==null) {
                    holder.mErrorMessage.setText(mActivity.getString(R.string.msgs_edit_sync_task_list_error_specified_task_does_not_exists));
                    break;
                }
            }
            if (holder.mErrorMessage.getText().length()==0) holder.mErrorMessage.setVisibility(TextView.GONE);
            else holder.mErrorMessage.setVisibility(TextView.VISIBLE);

            holder.mCheckBox.setChecked(o.item_checked);

            if (isDragDropEnabled()) holder.mHandle.setVisibility(ImageButton.VISIBLE);
            else holder.mHandle.setVisibility(ImageButton.INVISIBLE);

            holder.mTextAreaView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
//                    for(DataListItem item:recyclerViewDataList) item.item_checked=false;
//                    if (mNtfyTextViewLongClicked!=null) mNtfyTextViewLongClicked.notifyToListener(true, null);
                    return true;
                }
            });

            holder.mTextAreaView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    o.item_checked=!o.item_checked;
                    applyDataSetChanged();
                    if (mNtfyTextViewClicked!=null) mNtfyTextViewClicked.onCallBack(mActivity, true, null);
                }
            });

            holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    o.item_checked=((CheckBox)view).isChecked();
                    applyDataSetChanged();
                    if (mNtfyCheckBox!=null) mNtfyCheckBox.onCallBack(mActivity, true, new Object[]{o.item_checked});
                }
            });

            holder.mHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (isDragDropEnabled()) {
                        if (recyclerViewDataList.size()>1) {
                            if (event.getAction()==MotionEvent.ACTION_DOWN) {
                                if (mNtfyHandleTouch !=null) mNtfyHandleTouch.onCallBack(mActivity, true, new Object[]{holder});
                            }
                        }
                    }
                    return true;
                }
            });

        }

        @Override
        public int getItemCount() {
            return recyclerViewDataList.size();
        }
    }


    private void addTaskListDlg(final String current_task_list, final NotifyEvent p_ntfy) {

        ArrayList<DataListItem>add_task_list= getAddableTaskList(current_task_list);
        if (add_task_list.size()==0) {
            mUtil.showCommonDialog(false, "W", mActivity.getString(R.string.msgs_edit_sync_task_list_add_sync_task_no_task_exists_for_add), "", null);
            return;
        }
        Collections.sort(add_task_list,new Comparator<DataListItem>(){
            @Override
            public int compare(DataListItem o1, DataListItem o2) {
                return o1.item_name.compareToIgnoreCase(o2.item_name);
            }
        });

        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.data_list_add_data_item_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.data_list_add_data_item_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.data_list_add_data_item_dlg_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        TextView dlg_title = (TextView) dialog.findViewById(R.id.data_list_add_data_item_dlg_title);
        dlg_title.setTextColor(mGp.themeColorList.title_text_color);

        TextView dlg_msg = (TextView) dialog.findViewById(R.id.data_list_add_data_item_dlg_msg);

        final ImageButton ib_select_all = (ImageButton) dialog.findViewById(R.id.context_button_select_all);
        final ImageButton ib_unselect_all = (ImageButton) dialog.findViewById(R.id.context_button_unselect_all);
        final Button btn_ok = (Button) dialog.findViewById(R.id.data_list_add_data_item_dlg_ok);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.data_list_add_data_item_dlg_cancel);

        final ListView lv_sync_list = (ListView) dialog.findViewById(R.id.data_list_add_data_item_dlg_task_list);
        final AddDataListAdapter add_data_item_adapter = new AddDataListAdapter(mActivity, R.layout.data_list_add_data_item, add_task_list);
        lv_sync_list.setAdapter(add_data_item_adapter);

        CommonDialog.setViewEnabled(mActivity, btn_ok, false);

        NotifyEvent ntfy_check=new NotifyEvent(mActivity);
        ntfy_check.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                for(int i=0;i<add_data_item_adapter.getCount();i++) {
                    if (add_data_item_adapter.getItem(i).item_checked) {
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                        break;
                    }
                }
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        add_data_item_adapter.setNotifyCheckBox(ntfy_check);

        lv_sync_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataListItem atli=add_data_item_adapter.getItem(position);
                atli.item_checked =!atli.item_checked;
                add_data_item_adapter.notifyDataSetChanged();
                CommonDialog.setViewEnabled(mActivity, btn_ok, false);
                for(int i=0;i<add_data_item_adapter.getCount();i++) {
                    if (add_data_item_adapter.getItem(i).item_checked) {
                        CommonDialog.setViewEnabled(mActivity, btn_ok, true);
                        break;
                    }
                }
            }
        });

        ib_select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0;i<add_data_item_adapter.getCount();i++) {
                    add_data_item_adapter.getItem(i).item_checked =true;
                }
                add_data_item_adapter.notifyDataSetChanged();
                CommonDialog.setViewEnabled(mActivity, btn_ok, true);
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, ib_select_all, mActivity.getString(R.string.msgs_edit_sync_task_list_cont_label_select_all));

        ib_unselect_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0;i<add_data_item_adapter.getCount();i++) {
                    add_data_item_adapter.getItem(i).item_checked =false;
                }
                add_data_item_adapter.notifyDataSetChanged();
                CommonDialog.setViewEnabled(mActivity, btn_ok, false);
            }
        });
        ContextButtonUtil.setButtonLabelListener(mActivity, ib_unselect_all, mActivity.getString(R.string.msgs_edit_sync_task_list_cont_label_unselect_all));

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String n_prof_list = "", sep = "";
                for (int i = 0; i < add_data_item_adapter.getCount(); i++) {
                    if (add_data_item_adapter.getItem(i).item_checked) {
                        n_prof_list = n_prof_list + sep + add_data_item_adapter.getItem(i).item_name;
                        sep = SYNC_TASK_LIST_SEPARATOR;
                    }
                }
                p_ntfy.notifyToListener(true, new Object[]{n_prof_list});
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                p_ntfy.notifyToListener(false, null);
            }
        });

        dialog.show();

    }

    private class AddDataListAdapter extends ArrayAdapter<DataListItem> {
        private int layout_id = 0;
        private Context context = null;
        private NotifyEvent mNtfyCheckbox;
        private int text_color = 0;

        private ArrayList<DataListItem>mTaskList=null;

        public AddDataListAdapter(Context c, int textViewResourceId, ArrayList<DataListItem>tl) {
            super(c, textViewResourceId, tl);
            layout_id = textViewResourceId;
            context = c;
            mTaskList=tl;
        }

        public void setNotifyCheckBox(NotifyEvent ntfy) {mNtfyCheckbox=ntfy;}

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder holder;
            final DataListItem o = getItem(position);
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(layout_id, null);
                holder = new ViewHolder();
                holder.tv_name = (TextView) v.findViewById(R.id.data_list_add_data_item_name);
                holder.cb_selected = (CheckBox) v.findViewById(R.id.data_list_add_data_item_checked);
                text_color = holder.tv_name.getCurrentTextColor();
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            if (o != null) {
                holder.tv_name.setText(o.item_name);
                holder.tv_name.setTextColor(text_color);
            }

            holder.cb_selected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    o.item_checked =holder.cb_selected.isChecked();
                    if (mNtfyCheckbox!=null) mNtfyCheckbox.notifyToListener(true, new Object[]{o});
                }
            });
            holder.cb_selected.setChecked(o.item_checked);
            return v;

        }

        private class ViewHolder {
            TextView tv_name;
            CheckBox cb_selected;
        }
    }

}
