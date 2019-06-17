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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sentaroh.android.Utilities.ContextButton.ContextButtonUtil;
import com.sentaroh.android.Utilities.ContextMenu.CustomContextMenu;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.NotifyEvent;

import java.util.ArrayList;
import java.util.Comparator;

import static android.view.KeyEvent.KEYCODE_BACK;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_INTENT_SET_TIMER;

/**
 * Created by sentaroh on 2018/03/07.
 */

public class ScheduleListEditor {
    private CommonDialog mCommonDlg = null;

    private GlobalParameters mGp = null;
    private Context mContext = null;
    private ActivityMain mActivity = null;
    private CommonUtilities mUtil = null;
    private CustomContextMenu ccMenu = null;
    private ArrayList<ScheduleItem> mScheduleList = null;
    private ScheduleListAdapter mScheduleAdapter = null;

    private boolean mAdapterChanged = false;

    private Dialog mDialog = null;

    private ImageButton mContextButtonAdd = null;
    private ImageButton mContextButtonActivate = null;
    private ImageButton mContextButtonInactivate = null;
    private ImageButton mContextButtonCopy = null;
    private ImageButton mContextButtonRename = null;
    private ImageButton mContextButtonDelete = null;
    private ImageButton mContextButtonSelectAll = null;
    private ImageButton mContextButtonUnselectAll = null;

    private LinearLayout mContextButtonAddView = null;
    private LinearLayout mContextButtonActivateView = null;
    private LinearLayout mContextButtonInactivateView = null;
    private LinearLayout mContextButtonCopyView = null;
    private LinearLayout mContextButtonRenameView = null;
    private LinearLayout mContextButtonDeleteView = null;
    private LinearLayout mContextButtonSelectAllView = null;
    private LinearLayout mContextButtonUnselectAllView = null;


    public ScheduleListEditor(CommonUtilities mu, ActivityMain a, Context c,
                       CommonDialog cd, CustomContextMenu ccm, GlobalParameters gp) {
        mContext = c;
        mActivity = a;
        mGp = gp;
        mUtil = mu;
        mCommonDlg = cd;
        ccMenu = ccm;
        initDialog();
    }

    private ScheduleItem getScheduleListItem(String name, ArrayList<ScheduleItem>sl) {
        ScheduleItem result=null;
        for(ScheduleItem item:sl) {
            if (item.scheduleName.equals(name)) {
                result=item;
                break;
            }
        }
        return result;
    }

    private void initDialog() {
        // カスタムダイアログの生成
        mDialog = new Dialog(mActivity, mGp.applicationTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setContentView(R.layout.schedule_list_edit_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) mDialog.findViewById(R.id.schedule_list_edit_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        LinearLayout title_view = (LinearLayout) mDialog.findViewById(R.id.schedule_list_edit_dlg_title_view);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        TextView dlg_title = (TextView) mDialog.findViewById(R.id.schedule_list_edit_dlg_title);
        dlg_title.setTextColor(mGp.themeColorList.title_text_color);

        final ImageButton btn_toggle = (ImageButton) mDialog.findViewById(R.id.schedule_list_edit_dlg_toggle_schedule);
        btn_toggle.setBackgroundColor(Color.TRANSPARENT);//.DKGRAY);
        final TextView tv_msg = (TextView) mDialog.findViewById(R.id.schedule_list_edit_dlg_msg);
        tv_msg.setTextColor(mGp.themeColorList.text_color_warning);

        final ListView lv = (ListView) mDialog.findViewById(R.id.schedule_list_edit_dlg_schedule_list_view);

        mScheduleList = ScheduleUtil.loadScheduleData(mGp);

        mScheduleAdapter = new ScheduleListAdapter(mActivity, R.layout.schedule_list_edit_list_item, mScheduleList);
        lv.setAdapter(mScheduleAdapter);

        createContextView(mDialog);
        setContextButtonListener(mDialog);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.v("","before="+adapter.getItem(i).scheduleName);
                if (mScheduleAdapter.isSelectMode()) {
                    if (mScheduleAdapter.getItem(i).isChecked) {
                        mScheduleAdapter.getItem(i).isChecked = false;
                    } else {
                        mScheduleAdapter.getItem(i).isChecked = true;
                    }
                    mScheduleAdapter.notifyDataSetChanged();
                    setContextButtonMode(mDialog, mScheduleAdapter);
                } else {
                    NotifyEvent ntfy = new NotifyEvent(mContext);
                    ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                        @Override
                        public void positiveResponse(Context context, Object[] objects) {
                            saveScheduleList();
                            mScheduleAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void negativeResponse(Context context, Object[] objects) {
                        }
                    });
                    ScheduleItemEditor sm = new ScheduleItemEditor(mUtil, mActivity, mContext, mCommonDlg, ccMenu, mGp,
                            true, mScheduleList, mScheduleList.get(i), ntfy);
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                mScheduleAdapter.setSelectMode(true);
                mScheduleAdapter.getItem(i).isChecked = !mScheduleAdapter.getItem(i).isChecked;
                mScheduleAdapter.notifyDataSetChanged();
                setContextButtonMode(mDialog, mScheduleAdapter);
                return true;
            }
        });

        setContextButtonMode(mDialog, mScheduleAdapter);

        NotifyEvent ntfy = new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context context, Object[] objects) {
                setContextButtonMode(mDialog, mScheduleAdapter);
            }

            @Override
            public void negativeResponse(Context context, Object[] objects) {
            }
        });
        mScheduleAdapter.setCbNotify(ntfy);

        setScheduleMsg();
        btn_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGp.setScheduleEnabled(!mGp.settingScheduleSyncEnabled);
                setScheduleMsg();
                ScheduleUtil.sendTimerRequest(mContext, SCHEDULER_INTENT_SET_TIMER);
                ScheduleUtil.setSchedulerInfo(mGp, mUtil);
            }
        });
        btn_toggle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast toast = Toast.makeText(mContext, mContext.getString(R.string.msgs_schedule_list_edit_scheduler_toggle_enable_disable), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 500, 150);
                toast.getView().setBackgroundColor(mGp.themeColorList.title_background_color);
                ((TextView)((LinearLayout)toast.getView()).getChildAt(0)).setTextColor(mGp.themeColorList.title_text_color);
                toast.show();
                return true;
            }
        });

        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int kc, KeyEvent keyEvent) {
                switch (kc) {
                    case KEYCODE_BACK:
                        if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                            if (mScheduleAdapter.isSelectMode()) {
                                mScheduleAdapter.setSelectMode(false);
                                setContextButtonMode(mDialog, mScheduleAdapter);
                            } else {
                                mDialog.dismiss();
                            }
                        }
                        return true;
                    // break;
                    default:
                        // break;
                }

                return false;
            }
        });
        mDialog.show();
    }

    private void setScheduleMsg() {
        final ImageButton btn_toggle = (ImageButton) mDialog.findViewById(R.id.schedule_list_edit_dlg_toggle_schedule);
        final TextView tv_msg = (TextView) mDialog.findViewById(R.id.schedule_list_edit_dlg_msg);
        if (mScheduleAdapter.getCount() == 0) {
            tv_msg.setVisibility(TextView.VISIBLE);
            tv_msg.setText(mContext.getString(R.string.msgs_schedule_list_edit_no_schedule));
        } else {
            if (mGp.settingScheduleSyncEnabled) {
                if (mScheduleAdapter.getCount()!=0) {
                    tv_msg.setVisibility(TextView.GONE);
                }
            } else {
                tv_msg.setVisibility(TextView.VISIBLE);
                tv_msg.setText(mContext.getString(R.string.msgs_schedule_list_edit_scheduler_disabled));
            }
        }
        if (mGp.settingScheduleSyncEnabled) {
            btn_toggle.setImageResource(R.drawable.ic_64_schedule);
        } else {
            btn_toggle.setImageResource(R.drawable.ic_64_schedule_disabled);
        }

    }

    private void setContextButtonMode(Dialog dialog, ScheduleListAdapter adapter) {
        boolean selected = false;
        int sel_cnt = 0;
        boolean enabled = false, disabled = false;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).isChecked) {
                selected = true;
                sel_cnt++;
                if (adapter.getItem(i).scheduleEnabled) enabled = true;
                else disabled = true;
            }
        }

        mContextButtonAddView.setVisibility(LinearLayout.VISIBLE);
        mContextButtonActivateView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonInactivateView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonCopyView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonRenameView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonDeleteView.setVisibility(LinearLayout.INVISIBLE);
        mContextButtonSelectAllView.setVisibility(LinearLayout.VISIBLE);
        mContextButtonUnselectAllView.setVisibility(LinearLayout.INVISIBLE);

//        final ImageButton btn_ok = (ImageButton) mDialog.findViewById(R.id.schedule_list_edit_dlg_save);
//        final ImageButton btn_cancel = (ImageButton) mDialog.findViewById(R.id.schedule_list_edit_dlg_close);

        if (mScheduleAdapter.isSelectMode()) {
            if (sel_cnt == 0) {
                mContextButtonAddView.setVisibility(LinearLayout.INVISIBLE);
                mContextButtonActivateView.setVisibility(LinearLayout.INVISIBLE);
                mContextButtonInactivateView.setVisibility(LinearLayout.INVISIBLE);
                mContextButtonCopyView.setVisibility(LinearLayout.INVISIBLE);
                mContextButtonRenameView.setVisibility(LinearLayout.INVISIBLE);
                mContextButtonDeleteView.setVisibility(LinearLayout.INVISIBLE);
                mContextButtonSelectAllView.setVisibility(LinearLayout.VISIBLE);
                mContextButtonUnselectAllView.setVisibility(LinearLayout.INVISIBLE);
            } else if (sel_cnt == 1) {
                mContextButtonAddView.setVisibility(LinearLayout.INVISIBLE);
                if (disabled) mContextButtonActivateView.setVisibility(LinearLayout.VISIBLE);
                if (enabled) mContextButtonInactivateView.setVisibility(LinearLayout.VISIBLE);
                mContextButtonCopyView.setVisibility(LinearLayout.VISIBLE);
                mContextButtonRenameView.setVisibility(LinearLayout.VISIBLE);
                mContextButtonDeleteView.setVisibility(LinearLayout.VISIBLE);
                mContextButtonSelectAllView.setVisibility(LinearLayout.VISIBLE);
                mContextButtonUnselectAllView.setVisibility(LinearLayout.VISIBLE);
            } else if (sel_cnt >= 2) {
                mContextButtonAddView.setVisibility(LinearLayout.INVISIBLE);
                if (disabled) mContextButtonActivateView.setVisibility(LinearLayout.VISIBLE);
                if (enabled) mContextButtonInactivateView.setVisibility(LinearLayout.VISIBLE);
                mContextButtonCopyView.setVisibility(LinearLayout.INVISIBLE);
                mContextButtonRenameView.setVisibility(LinearLayout.INVISIBLE);
                mContextButtonDeleteView.setVisibility(LinearLayout.VISIBLE);
                mContextButtonSelectAllView.setVisibility(LinearLayout.VISIBLE);
                mContextButtonUnselectAllView.setVisibility(LinearLayout.VISIBLE);
            }
//            btn_ok.setVisibility(Button.INVISIBLE);
//            btn_cancel.setVisibility(Button.INVISIBLE);
//            btn_ok.setEnabled(false);
//            btn_cancel.setEnabled(false);
        } else {
            mContextButtonAddView.setVisibility(LinearLayout.VISIBLE);
            mContextButtonActivateView.setVisibility(LinearLayout.INVISIBLE);
            mContextButtonInactivateView.setVisibility(LinearLayout.INVISIBLE);
            mContextButtonCopyView.setVisibility(LinearLayout.INVISIBLE);
            mContextButtonRenameView.setVisibility(LinearLayout.INVISIBLE);
            mContextButtonDeleteView.setVisibility(LinearLayout.INVISIBLE);
            mContextButtonUnselectAllView.setVisibility(LinearLayout.INVISIBLE);
            if (adapter.getCount() == 0) {
                mContextButtonSelectAllView.setVisibility(LinearLayout.INVISIBLE);
            }
//            if (isScheduleListChanged()) btn_ok.setEnabled(true);
//            else btn_ok.setEnabled(false);
//            btn_cancel.setEnabled(true);
        }

    }

    private void setContextButtonEnabled(final ImageButton btn, boolean enabled) {
        if (enabled) {
            btn.postDelayed(new Runnable() {
                @Override
                public void run() {
                    btn.setEnabled(true);
                }
            }, 1000);
        } else {
            btn.setEnabled(false);
        }
    }

    private void saveScheduleList() {
        mScheduleAdapter.sort();
        ScheduleUtil.saveScheduleData(mGp, mScheduleList);
        ScheduleUtil.sendTimerRequest(mContext, SCHEDULER_INTENT_SET_TIMER);
        ScheduleUtil.setSchedulerInfo(mGp, mUtil);
    }

    private void setContextButtonListener(final Dialog dialog) {
        final TextView tv_msg = (TextView) dialog.findViewById(R.id.schedule_list_edit_dlg_msg);
        mContextButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        ScheduleItem si = (ScheduleItem) objects[0];
                        mScheduleAdapter.add(si);
                        mScheduleAdapter.sort();
                        mScheduleAdapter.notifyDataSetChanged();
                        setContextButtonMode(dialog, mScheduleAdapter);
                        saveScheduleList();
                        setScheduleMsg();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                ScheduleItemEditor sm = new ScheduleItemEditor(mUtil, mActivity, mContext, mCommonDlg, ccMenu, mGp,
                        false, mScheduleList, new ScheduleItem(), ntfy);
            }
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonAdd, mContext.getString(R.string.msgs_schedule_cont_label_add));

        mContextButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        for (int i = mScheduleAdapter.getCount() - 1; i >= 0; i--) {
                            if (mScheduleAdapter.getItem(i).isChecked) {
                                mScheduleAdapter.remove(mScheduleAdapter.getItem(i));
                            }
                        }
                        if (mScheduleAdapter.getCount() == 0) {
                            mScheduleAdapter.setSelectMode(false);
                            tv_msg.setVisibility(TextView.VISIBLE);
                        }
                        setContextButtonMode(dialog, mScheduleAdapter);
                        mScheduleAdapter.notifyDataSetChanged();
                        saveScheduleList();
                        setScheduleMsg();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                String del_list = "";
                for (int i = 0; i < mScheduleAdapter.getCount(); i++) {
                    if (mScheduleAdapter.getItem(i).isChecked) {
                        del_list += mScheduleAdapter.getItem(i).scheduleName + "\n";
                    }
                }
                mUtil.showCommonDialog(true, "W",
                        mContext.getString(R.string.msgs_schedule_confirm_title_delete),
                        mContext.getString(R.string.msgs_schedule_confirm_msg_delete) + "\n" + del_list, ntfy);
            }
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonDelete, mContext.getString(R.string.msgs_schedule_cont_label_delete));

        mContextButtonActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        for (int i = mScheduleAdapter.getCount() - 1; i >= 0; i--) {
                            if (mScheduleAdapter.getItem(i).isChecked && !mScheduleAdapter.getItem(i).scheduleEnabled) {
                                mScheduleAdapter.getItem(i).scheduleEnabled = true;
                                mScheduleAdapter.getItem(i).isChanged = true;
                                mScheduleAdapter.getItem(i).scheduleLastExecTime = System.currentTimeMillis();
                            }
                        }
//                        mScheduleAdapter.setSelectMode(false);
                        mScheduleAdapter.unselectAll();
                        setContextButtonMode(dialog, mScheduleAdapter);
                        saveScheduleList();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                String del_list = "";
                for (int i=0;i<mScheduleAdapter.getCount();i++) {
                    if (mScheduleAdapter.getItem(i).isChecked && !mScheduleAdapter.getItem(i).scheduleEnabled) {
                        del_list += mScheduleAdapter.getItem(i).scheduleName + "\n";
                    }
                }
                mUtil.showCommonDialog(true, "W",
                        mContext.getString(R.string.msgs_schedule_confirm_title_enable),
                        mContext.getString(R.string.msgs_schedule_confirm_msg_enable) + "\n" + del_list, ntfy);
            }
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonActivate, mContext.getString(R.string.msgs_schedule_cont_label_activate));

        mContextButtonInactivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        for (int i = mScheduleAdapter.getCount() - 1; i >= 0; i--) {
                            if (mScheduleAdapter.getItem(i).isChecked && mScheduleAdapter.getItem(i).scheduleEnabled) {
                                mScheduleAdapter.getItem(i).scheduleEnabled = false;
                                mScheduleAdapter.getItem(i).isChanged = true;
                            }
                        }
//                        mScheduleAdapter.setSelectMode(false);
                        mScheduleAdapter.unselectAll();
                        setContextButtonMode(dialog, mScheduleAdapter);
                        saveScheduleList();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                String del_list = "";
                for (int i=0;i<mScheduleAdapter.getCount();i++) {
                    if (mScheduleAdapter.getItem(i).isChecked && mScheduleAdapter.getItem(i).scheduleEnabled) {
                        del_list += mScheduleAdapter.getItem(i).scheduleName + "\n";
                    }
                }
                mUtil.showCommonDialog(true, "W",
                        mContext.getString(R.string.msgs_schedule_confirm_title_disable),
                        mContext.getString(R.string.msgs_schedule_confirm_msg_disable) + "\n" + del_list, ntfy);
            }
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonInactivate, mContext.getString(R.string.msgs_schedule_cont_label_inactivate));

        mContextButtonRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
//                        mScheduleAdapter.setSelectMode(false);
                        mScheduleAdapter.sort();
                        mScheduleAdapter.unselectAll();
                        setContextButtonMode(dialog, mScheduleAdapter);
                        saveScheduleList();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                ScheduleItem si = null;
                for (int i = mScheduleAdapter.getCount() - 1; i >= 0; i--) {
                    if (mScheduleAdapter.getItem(i).isChecked) {
                        si = mScheduleAdapter.getItem(i);
                        break;
                    }
                }
                if (si==null) {
                    mUtil.addLogMsg("E","renameSchedule error, schedule item can not be found.");
                    mUtil.showCommonDialog(false, "E", "renameSchedule error, schedule item can not be found.", "", null);
                } else {
                    renameSchedule(si, ntfy);
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonRename, mContext.getString(R.string.msgs_schedule_cont_label_rename));

        mContextButtonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotifyEvent ntfy = new NotifyEvent(mContext);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        ScheduleItem si = (ScheduleItem) objects[0];
//                        mScheduleAdapter.setSelectMode(false);
                        mScheduleAdapter.add(si);
                        mScheduleAdapter.unselectAll();
                        mScheduleAdapter.sort();
                        saveScheduleList();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {
                    }
                });
                ScheduleItem si = null;
                for (int i = mScheduleAdapter.getCount() - 1; i >= 0; i--) {
                    if (mScheduleAdapter.getItem(i).isChecked) {
                        si = mScheduleAdapter.getItem(i);
                        ScheduleItem new_si = si.clone();
                        ScheduleItemEditor sm = new ScheduleItemEditor(mUtil, mActivity, mContext, mCommonDlg, ccMenu, mGp,
                                false, mScheduleList, new_si, ntfy);
                        break;
                    }
                }
            }
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonCopy, mContext.getString(R.string.msgs_schedule_cont_label_copy));

        mContextButtonSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScheduleAdapter.setSelectMode(true);
                mScheduleAdapter.selectAll();
                setContextButtonMode(dialog, mScheduleAdapter);
            }
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonSelectAll, mContext.getString(R.string.msgs_schedule_cont_label_select_all));

        mContextButtonUnselectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScheduleAdapter.setSelectMode(false);
                mScheduleAdapter.unselectAll();
                setContextButtonMode(dialog, mScheduleAdapter);
            }
        });
        ContextButtonUtil.setButtonLabelListener(mContext, mContextButtonUnselectAll, mContext.getString(R.string.msgs_schedule_cont_label_unselect_all));

    }

    private void renameSchedule(final ScheduleItem si, final NotifyEvent p_ntfy) {

        // カスタムダイアログの生成
        final Dialog dialog = new Dialog(mActivity, mGp.applicationTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.setContentView(R.layout.single_item_input_dlg);

        LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_dlg_view);
        CommonUtilities.setDialogBoxOutline(mContext, ll_dlg_view);

//        Drawable db = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.dialog_box_outline, null);
//        ll_dlg_view.setBackground(db);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.single_item_input_title_view);
        final TextView title = (TextView) dialog.findViewById(R.id.single_item_input_title);
        title_view.setBackgroundColor(mGp.themeColorList.title_background_color);
        title.setTextColor(mGp.themeColorList.title_text_color);

		final TextView dlg_msg = (TextView) dialog.findViewById(R.id.single_item_input_msg);
		dlg_msg.setVisibility(TextView.VISIBLE);
        final TextView dlg_cmp = (TextView) dialog.findViewById(R.id.single_item_input_name);
        final Button btn_ok = (Button) dialog.findViewById(R.id.single_item_input_ok_btn);
        final Button btn_cancel = (Button) dialog.findViewById(R.id.single_item_input_cancel_btn);
        final EditText etInput = (EditText) dialog.findViewById(R.id.single_item_input_dir);

        title.setText(mContext.getString(R.string.msgs_schedule_rename_schedule));

        dlg_cmp.setVisibility(TextView.GONE);
        CommonDialog.setDlgBoxSizeCompactWithInput(dialog);
        etInput.setText(si.scheduleName);
        dlg_msg.setText(mContext.getString(R.string.msgs_schedule_confirm_msg_rename_duplicate_name));
        btn_ok.setEnabled(false);
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                if (!arg0.toString().equalsIgnoreCase(si.scheduleName)) {
                    btn_ok.setEnabled(true);
                    dlg_msg.setText(mContext.getString(R.string.msgs_schedule_confirm_msg_rename_warning));
                } else {
                    btn_ok.setEnabled(false);
                    dlg_msg.setText(mContext.getString(R.string.msgs_schedule_confirm_msg_rename_duplicate_name));
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
        btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                String new_name = etInput.getText().toString();

                si.scheduleName = new_name;
                si.isChanged = true;

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

    private void createContextView(Dialog dialog) {
        mContextButtonAddView = (LinearLayout) dialog.findViewById(R.id.context_button_add_view);
        mContextButtonActivateView = (LinearLayout) dialog.findViewById(R.id.context_button_activate_view);
        mContextButtonInactivateView = (LinearLayout) dialog.findViewById(R.id.context_button_inactivate_view);
        mContextButtonCopyView = (LinearLayout) dialog.findViewById(R.id.context_button_copy_view);
        mContextButtonRenameView = (LinearLayout) dialog.findViewById(R.id.context_button_rename_view);
        mContextButtonDeleteView = (LinearLayout) dialog.findViewById(R.id.context_button_delete_view);
        mContextButtonSelectAllView = (LinearLayout) dialog.findViewById(R.id.context_button_select_all_view);
        mContextButtonUnselectAllView = (LinearLayout) dialog.findViewById(R.id.context_button_unselect_all_view);

        mContextButtonAdd = (ImageButton) dialog.findViewById(R.id.context_button_add);
        mContextButtonActivate = (ImageButton) dialog.findViewById(R.id.context_button_activate);
        mContextButtonInactivate = (ImageButton) dialog.findViewById(R.id.context_button_inactivate);
        mContextButtonCopy = (ImageButton) dialog.findViewById(R.id.context_button_copy);
        mContextButtonRename = (ImageButton) dialog.findViewById(R.id.context_button_rename);
        mContextButtonDelete = (ImageButton) dialog.findViewById(R.id.context_button_delete);
        mContextButtonSelectAll = (ImageButton) dialog.findViewById(R.id.context_button_select_all);
        mContextButtonUnselectAll = (ImageButton) dialog.findViewById(R.id.context_button_unselect_all);
    }

    ;

//    private void releaseImageResource(){
//        releaseImageBtnRes(mContextButtonAdd);
//        releaseImageBtnRes(mContextButtonActivate);
//        releaseImageBtnRes(mContextButtonInactivate);
//        releaseImageBtnRes(mContextButtonCopy);
//        releaseImageBtnRes(mContextButtonRename);
//        releaseImageBtnRes(mContextButtonDelete);
//        releaseImageBtnRes(mContextButtonSelectAll);
//        releaseImageBtnRes(mContextButtonUnselectAll);
//    };
//
//    private void releaseImageBtnRes(ImageButton ib) {
//        ib.setImageDrawable(null);
//        ib.setBackgroundDrawable(null);
//        ib.setImageBitmap(null);
//    };

    private class ScheduleListAdapter extends ArrayAdapter<ScheduleItem> {
        private int layout_id = 0;
        private Context context = null;
        private int text_color = 0;
        private NotifyEvent mCbNotify = null;
        private ArrayList<ScheduleItem> mScheduleList = null;

        public ScheduleListAdapter(Context c, int textViewResourceId, ArrayList<ScheduleItem> sl) {
            super(c, textViewResourceId, sl);
            layout_id = textViewResourceId;
            context = c;
            mScheduleList = sl;
        }

        public void setCbNotify(NotifyEvent ntfy) {
            mCbNotify = ntfy;
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
        }

        public boolean isSelectMode() {
            return mSelectMode;
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
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(layout_id, null);
                holder = new ViewHolder();
                holder.ll_view=(LinearLayout)v.findViewById(R.id.schedule_list_view);
                ll_default_background_color=holder.ll_view.getBackground();
                holder.tv_name = (TextView) v.findViewById(R.id.schedule_list_name);
                holder.tv_info = (TextView) v.findViewById(R.id.schedule_list_info);
                holder.tv_time_info = (TextView) v.findViewById(R.id.schedule_list_time_info);
                holder.tv_error_info = (TextView) v.findViewById(R.id.schedule_list_error_info);
                holder.tv_error_info.setTextColor(mGp.themeColorList.text_color_warning);
                holder.cbChecked = (CheckBox) v.findViewById(R.id.schedule_list_checked);
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
                } else {
                    holder.cbChecked.setVisibility(CheckBox.INVISIBLE);
                }

                holder.ll_view.setBackground(ll_default_background_color);
                if (!mGp.settingScheduleSyncEnabled) holder.ll_view.setBackgroundColor(mGp.themeColorList.text_color_disabled);
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
                String sync_prof = "";
                if (o.syncAutoSyncTask) {
                    sync_prof = mContext.getString(R.string.msgs_scheduler_info_sync_all_active_profile);
                    holder.tv_error_info.setVisibility(TextView.GONE);
                } else {
                    boolean schedule_error=false;
                    String error_item_name="";
                    if (o.syncTaskList.equals("")) {
                        schedule_error=true;
                    } else {
                        if (o.syncTaskList.indexOf(",")>0) {
                            String[] stl=o.syncTaskList.split(",");
                            String sep="";
                            for(String stn:stl) {
                                if (ScheduleUtil.getSyncTask(mGp,stn)==null) {
                                    schedule_error=true;
                                    error_item_name=sep+stn;
                                    sep=",";
                                }
                            }
                        } else {
                            if (ScheduleUtil.getSyncTask(mGp, o.syncTaskList)==null) {
                                schedule_error=true;
                                error_item_name=o.syncTaskList;
                            }
                        }
                    }
                    if (schedule_error) {
                        holder.tv_error_info.setVisibility(TextView.VISIBLE);
                        if (o.syncTaskList.equals("")) {
                            holder.tv_error_info.setText(mContext.getString(R.string.msgs_scheduler_info_sync_task_list_was_empty));
                        } else {
                            holder.tv_error_info.setText(String.format(mContext.getString(R.string.msgs_scheduler_info_sync_task_was_not_found),
                                    error_item_name));
                        }
                    } else {
                        holder.tv_error_info.setVisibility(TextView.GONE);
                    }
                    sync_prof = String.format(mContext.getString(R.string.msgs_scheduler_info_sync_selected_profile),
                            o.syncTaskList);
                }
                holder.tv_time_info.setText(time_info + " " + sync_prof);

                // 必ずsetChecked前にリスナを登録
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
                holder.cbChecked.setChecked(o.isChecked);
            }
            return v;

        }

        class ViewHolder {
            TextView tv_name, tv_info, tv_enabled, tv_time_info, tv_error_info;
            LinearLayout ll_view;
            CheckBox cbChecked;
        }

    }

}
