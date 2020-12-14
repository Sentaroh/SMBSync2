package com.sentaroh.android.SMBSync2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.NotifyEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditUsbUuidList{
    private Context mContext=null;
    private GlobalParameters mGp=null;
    private Activity mActivity=null;
    private CommonUtilities mUtil = null;

    private ListView mMainListView=null;
    private Button mOkButton=null;
    private Button mCancelButton=null;
    private Button mAddSpecifiedUuidButton =null;
    private Button mAddAvailableUuidButton =null;
    private EditText mInputUuid=null;
    private TextView mMainMessage=null;
    private Dialog mDialog=null;

    public EditUsbUuidList(Activity a, CommonUtilities cu) {
        mActivity = a;
        mContext = mActivity;
        mGp= GlobalWorkArea.getGlobalParameters(mContext);
        mUtil = cu;

        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                initView();
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        mUtil.showCommonDialog(true, "W",
                mContext.getString(R.string.msgs_edit_usb_uuid_menu_tittle),
                mContext.getString(R.string.msgs_edit_usb_uuid_dialog_warning_message),
                ntfy);
    }

    private ArrayList<String> mNewUuidList=new ArrayList<String>();
    private AdapterRegeisteredUsbUuidList mMainListAdapter =null;
    private void initView() {
        mDialog=new Dialog(mActivity, mGp.applicationTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.edit_usb_uuid_list_dlg);

        mMainListView=(ListView)mDialog.findViewById(R.id.edit_usb_uuid_list_dlg_listview);
        mOkButton=(Button)mDialog.findViewById(R.id.edit_usb_uuid_list_dlg_ok_btn);
        mCancelButton=(Button)mDialog.findViewById(R.id.edit_usb_uuid_list_dlg_cancel_btn);
        mAddSpecifiedUuidButton =(Button)mDialog.findViewById(R.id.edit_usb_uuid_list_dlg_add_specified_uuid_btn);
        mInputUuid=(EditText) mDialog.findViewById(R.id.edit_usb_uuid_list_dlg_new_uuid);
        mMainMessage=(TextView)mDialog.findViewById(R.id.edit_usb_uuid_list_dlg_msg);
        mMainMessage.setText("");
        mAddAvailableUuidButton =(Button)mDialog.findViewById(R.id.edit_usb_uuid_list_dlg_list_available_uuid_btn);

        mNewUuidList.clear();
        for(String item:mGp.forceUsbUuidList) mNewUuidList.add(item);

        mMainListAdapter =new AdapterRegeisteredUsbUuidList(mActivity, R.layout.edit_usb_uuid_list_dlg_list_view_item, mNewUuidList);
        mMainListView.setAdapter(mMainListAdapter);
        mMainListAdapter.notifyDataSetChanged();

        NotifyEvent ntfy_delete=new NotifyEvent(mContext);
        ntfy_delete.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                String item=(String)objects[0];
                NotifyEvent ntfy_delete=new NotifyEvent(mContext);
                ntfy_delete.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        mMainListAdapter.remove(item);
                        mMainListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {}
                });
                mUtil.showCommonDialog(true, "D", mContext.getString(R.string.msgs_edit_usb_uuid_dialog_confirm_delete_title),
                        mContext.getString(R.string.msgs_edit_usb_uuid_dialog_confirm_delete_message, item),
                        ntfy_delete);

                CommonDialog.setViewEnabled(mActivity, mOkButton, true);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        mMainListAdapter.setNotifyDeleteListener(ntfy_delete);

//        mMainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });

        CommonDialog.setViewEnabled(mActivity, mOkButton, false);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy=new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        mGp.forceUsbUuidList.clear();
                        for(String item:mNewUuidList) mGp.forceUsbUuidList.add(item);
                        mGp.saveForceUsbUuidList(mContext);
                        mGp.safMgr.setUsbUuidList(mGp.forceUsbUuidList);
                        mGp.safMgr.loadSafFile();
                        mGp.syncTaskAdapter.notifyDataSetChanged();
                        mDialog.dismiss();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {}
                });
                if (mNewUuidList.size()>0) {
                    String list="", sep="";
                    for(String list_item:mNewUuidList) {
                        list+=sep+"-"+list_item;
                        sep="\n";
                    }
                    mUtil.showCommonDialog(true, "D", mContext.getString(R.string.msgs_edit_usb_uuid_dialog_confirm_save_title),
                            mContext.getString(R.string.msgs_edit_usb_uuid_dialog_confirm_save_message, "\n"+list),
                            ntfy);
                } else {
                    mUtil.showCommonDialog(true, "D", mContext.getString(R.string.msgs_edit_usb_uuid_dialog_confirm_delete_title),
                            mContext.getString(R.string.msgs_edit_usb_uuid_dialog_confirm_delete_all_uuids_message),
                            ntfy);
                }

            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prev_uuid="", new_uuid="";
                for(String uuid_item:mGp.forceUsbUuidList) prev_uuid+=uuid_item;
                for(String uuid_item:mNewUuidList) new_uuid+=uuid_item;
                if (!prev_uuid.equals(new_uuid)) {

                    NotifyEvent ntfy=new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            mDialog.dismiss();
                        }

                        @Override
                        public void negativeResponse(Context context, Object[] objects) {}
                    });
                    mUtil.showCommonDialog(true, "W", mContext.getString(R.string.msgs_edit_usb_uuid_dialog_confirm_exit_title),
                            mContext.getString(R.string.msgs_edit_usb_uuid_dialog_confirm_exit_message),
                            ntfy);
                } else {
                    mDialog.dismiss();
                }
            }
        });

        mAddAvailableUuidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy=new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        ArrayList<String>added_list=(ArrayList<String>) objects[0];
                        if (added_list.size()>0) {
                            for(String add_item:added_list) mNewUuidList.add(add_item);
                            sortUuidList(mNewUuidList);
                            mMainListAdapter.notifyDataSetChanged();
                            CommonDialog.setViewEnabled(mActivity, mOkButton, true);
                        }
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {

                    }
                });
                addUuidList(ntfy);
            }
        });

        CommonDialog.setViewEnabled(mActivity, mAddSpecifiedUuidButton, false);
        mAddSpecifiedUuidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewUuidList.add(mInputUuid.getText().toString().toUpperCase());
                sortUuidList(mNewUuidList);
                mMainListAdapter.notifyDataSetChanged();
                mInputUuid.setText("");
                CommonDialog.setViewEnabled(mActivity, mAddSpecifiedUuidButton, false);
                CommonDialog.setViewEnabled(mActivity, mOkButton, true);
            }
        });

        mInputUuid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0) {
                    String uuid=s.toString();
                    if (uuid.length()==9) {
                        if (uuid.contains("-")) {
                            String[] uuid_array=uuid.split("-");
                            if (uuid_array.length==2) {
                                if (isUsbUuidAlreadyRegisterd(mNewUuidList, uuid)) {
                                    mMainMessage.setText(mContext.getString(R.string.msgs_edit_usb_uuid_dialog_audit_error_already_registered, uuid));
                                    CommonDialog.setViewEnabled(mActivity, mAddSpecifiedUuidButton, false);
                                } else {
                                    if ((uuid_array[0].toUpperCase().replaceAll("[0-9A-F]+", "").length()>0) ||
                                            (uuid_array[1].toUpperCase().replaceAll("[0-9A-F]+", "").length()>0)) {
                                        CommonDialog.setViewEnabled(mActivity, mAddSpecifiedUuidButton, false);
                                        mMainMessage.setText(mContext.getString(R.string.msgs_edit_usb_uuid_dialog_audit_error_character_combination));
                                    } else {
                                        CommonDialog.setViewEnabled(mActivity, mAddSpecifiedUuidButton, true);
                                        mMainMessage.setText("");
                                    }
                                }
                            } else {
                                CommonDialog.setViewEnabled(mActivity, mAddSpecifiedUuidButton, false);
                                mMainMessage.setText(mContext.getString(R.string.msgs_edit_usb_uuid_dialog_audit_error_format));
                            }
                        } else {
                            CommonDialog.setViewEnabled(mActivity, mAddSpecifiedUuidButton, false);
                            mMainMessage.setText(mContext.getString(R.string.msgs_edit_usb_uuid_dialog_audit_error_format));
                        }
                    } else {
                        CommonDialog.setViewEnabled(mActivity, mAddSpecifiedUuidButton, false);
                        mMainMessage.setText(mContext.getString(R.string.msgs_edit_usb_uuid_dialog_audit_error_length));
                    }
                } else {
                    mMainMessage.setText("");
                    CommonDialog.setViewEnabled(mActivity, mAddSpecifiedUuidButton, false);
                }
            }
        });

        mDialog.show();
    }

    private void sortUuidList(ArrayList<String>list) {
        Collections.sort(list);
    }

    private boolean isUsbUuidAlreadyRegisterd(ArrayList<String> uuid_list, String uuid) {
        boolean result=false;
        for(String item:uuid_list) {
            if (item.toLowerCase().equals(uuid.toLowerCase())) {
                result=true;
                break;
            }
        }
        return result;
    }

    private ArrayList<AvailableUuidListItem> createAvailableUuidList(Context c) {
        ArrayList<AvailableUuidListItem> available_uuid_list=new ArrayList<AvailableUuidListItem>();
        StorageManager sm = (StorageManager) c.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> vol_list=sm.getStorageVolumes();
        for(StorageVolume sv_item:vol_list) {
            if (sv_item.isRemovable() && sv_item.getUuid().length()==9) {
                if (!isUsbUuidAlreadyRegisterd(mNewUuidList, sv_item.getUuid())) {
                    File lf=new File("/storage/"+sv_item.getUuid());
                    if (lf.canRead()) {
                        AvailableUuidListItem uuid_item=new AvailableUuidListItem();
                        uuid_item.uuid=sv_item.getUuid();
                        uuid_item.description=sv_item.getDescription(c);
                        available_uuid_list.add(uuid_item);
                    }
                }
            }
        }
        return available_uuid_list;
    }

    private void addUuidList(final NotifyEvent p_ntfy) {
        Dialog dialog=new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_usb_uuid_select_dlg);

        final TextView tv_msg=(TextView)dialog.findViewById(R.id.edit_usb_uuid_select_dlg_msg);
        final Button ok_btn=(Button)dialog.findViewById(R.id.edit_usb_uuid_select_dlg_ok_btn);
        final Button cancel_btn=(Button)dialog.findViewById(R.id.edit_usb_uuid_select_dlg_cancel_btn);
        final ListView uuid_list_view=(ListView) dialog.findViewById(R.id.edit_usb_uuid_select_dlg_listview);
        uuid_list_view.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ArrayList<AvailableUuidListItem> select_uuid_list=createAvailableUuidList(mContext);
        final AdapterAvailableUuidList adapter=new AdapterAvailableUuidList(mActivity, R.layout.edit_usb_uuid_select_list_item, select_uuid_list);
        uuid_list_view.setAdapter(adapter);
        if (select_uuid_list.size()==0) {
            tv_msg.setText(mContext.getString(R.string.msgs_edit_usb_uuid_select_dialog_no_uuid));
        }

        NotifyEvent ntfy_click=new NotifyEvent(mContext);
        ntfy_click.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                boolean checked=false;
                for(AvailableUuidListItem uuid_item:select_uuid_list) {
                    if (uuid_item.checked) {
                        checked=true;
                        break;
                    }
                }
                CommonDialog.setViewEnabled(mActivity, ok_btn, checked);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {}
        });
        adapter.setNotifyUuidClickListener(ntfy_click);

        CommonDialog.setViewEnabled(mActivity, ok_btn, false);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String>added_list=new ArrayList<String>();
                for(AvailableUuidListItem uuid_item:select_uuid_list) {
                    if (uuid_item.checked) {
                        added_list.add(uuid_item.uuid);
                    }
                }
                p_ntfy.notifyToListener(true, new Object[]{added_list});
                dialog.dismiss();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private class AdapterRegeisteredUsbUuidList extends ArrayAdapter<String> {

        private ArrayList<String>mUuidList=null;
        private Context c;
        private int mListResource =0;

        public AdapterRegeisteredUsbUuidList(@NonNull Context context, int resource, ArrayList<String>objects) {
            super(context, resource, objects);
            mUuidList=objects;
            c=context;
            mListResource =resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(mListResource, null);
                holder = new ViewHolder();
                holder.ib_delete=(ImageButton)v.findViewById(R.id.edit_usb_uuid_list_dlg_list_delete);
                holder.tv_uuid=(TextView)v.findViewById(R.id.edit_usb_uuid_list_dlg_list_uuid);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            final String o = getItem(position);
            holder.tv_uuid.setText(o);
            if (o != null) {
                final int p = position;
                // 必ずsetChecked前にリスナを登録(convertView != null の場合は既に別行用のリスナが登録されている！)
                holder.ib_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mNotifyDeketeListener!=null) mNotifyDeketeListener.notifyToListener(true, new Object[]{o});
                    }
                });
            }

            return v;
        }

        private NotifyEvent mNotifyDeketeListener=null;
        public void setNotifyDeleteListener(NotifyEvent ntfy) {
            mNotifyDeketeListener=ntfy;
        }

        private class ViewHolder {
            TextView tv_uuid;
            ImageButton ib_delete;
        }
    }

    private class AvailableUuidListItem {
        public String uuid="";
        public String description="";
        public boolean checked=false;
    }

    private class AdapterAvailableUuidList extends ArrayAdapter<AvailableUuidListItem> {

        private ArrayList<AvailableUuidListItem>mUuidList=null;
        private Context c;
        private int mListResource =0;

        public AdapterAvailableUuidList(@NonNull Context context, int resource, ArrayList<AvailableUuidListItem>objects) {
            super(context, resource, objects);
            mUuidList=objects;
            c=context;
            mListResource =resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(mListResource, null);
                holder = new ViewHolder();
                holder.ll_view=(LinearLayout)v.findViewById(R.id.edit_usb_uuid_select_list_item_view);
                holder.tv_uuid=(CheckedTextView)v.findViewById(R.id.edit_usb_uuid_select_list_item_uuid);
                holder.tv_desc=(TextView)v.findViewById(R.id.edit_usb_uuid_select_list_item_description);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            final AvailableUuidListItem o = getItem(position);
            if (o != null) {
                holder.tv_uuid.setText(o.uuid);
                holder.tv_desc.setText(o.description);
                holder.ll_view.setSoundEffectsEnabled(false);
                holder.ll_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.tv_uuid.performClick();
                    }
                });

                holder.tv_uuid.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isChecked=!holder.tv_uuid.isChecked();
                        holder.tv_uuid.setChecked(isChecked);
                        o.checked=isChecked;
                        if (mNotifyUuidClick!=null) {
                            mNotifyUuidClick.notifyToListener(true, null);
                        }
                    }
                });
            }

            return v;
        }

        private NotifyEvent mNotifyUuidClick=null;
        private void setNotifyUuidClickListener(NotifyEvent ntfy) {
            mNotifyUuidClick=ntfy;
        }

        private class ViewHolder {
            LinearLayout ll_view;
            CheckedTextView tv_uuid;
            TextView tv_desc;
        }
    }

}
