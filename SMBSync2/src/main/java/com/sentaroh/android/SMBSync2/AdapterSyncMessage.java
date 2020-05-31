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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.ThemeUtil;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("ALL")
public class AdapterSyncMessage extends ArrayAdapter<SyncMessageItem> {

    private Context c;
    private int id;
    private ArrayList<SyncMessageItem> items;
    private boolean msgDataChanged = false;

    private GlobalParameters mGp=null;
//	@SuppressWarnings("unused")
//	private boolean themeIsLight=false;
//	@SuppressWarnings("unused")
//	private Typeface msgTypeFace=null;

    private ThemeColorList mThemeColorList;

    public AdapterSyncMessage(Context context, int textViewResourceId,
                              ArrayList<SyncMessageItem> objects, GlobalParameters gp) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
        mGp=gp;
//		msgTypeFace=Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
        vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mThemeColorList = CommonUtilities.getThemeColorList(c);
    }

    final public void remove(int i) {
        items.remove(i);
        msgDataChanged = true;
    }

    @Override
    final public void add(SyncMessageItem mli) {
        items.add(mli);
        msgDataChanged = true;
        notifyDataSetChanged();
    }

    final public boolean resetDataChanged() {
        boolean tmp = msgDataChanged;
        msgDataChanged = false;
        return tmp;
    }

    ;

    @Override
    final public SyncMessageItem getItem(int i) {
        return items.get(i);
    }

    final public ArrayList<SyncMessageItem> getMessageList() {
        return items;
    }

    final public void setMessageList(List<SyncMessageItem> p) {
        items.clear();
        if (p != null) items.addAll(p);
        notifyDataSetChanged();
    }

//	@Override
//	public boolean isEnabled(int idx) {
//		 return getItem(idx).getActive().equals("A");
//	}

    private LayoutInflater vi;

    private ColorStateList mTextColor=null;

    @Override
    final public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        View v = convertView;
        if (v == null) {
            v = vi.inflate(id, null);
            holder = new ViewHolder();
//            holder.tv_row_cat= (TextView) v.findViewById(R.id.msg_list_view_item_cat);
            holder.tv_row_msg = (NonWordwrapTextView) v.findViewById(R.id.msg_list_view_item_msg);
            holder.tv_row_msg.setWordWrapEnabled(mGp.settingSyncMessageUseStandardTextView);
//            holder.tv_row_msg.setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE);
            holder.tv_row_time = (TextView) v.findViewById(R.id.msg_list_view_item_time);
            holder.tv_row_date = (TextView) v.findViewById(R.id.msg_list_view_item_date);
//        	holder.tv_row_msg.setTypeface(msgTypeFace);
//        	holder.tv_row_msg.setLineBreak(CustomTextView.LINE_BREAK_NO_WORD_WRAP);
//            holder.config=v.getResources().getConfiguration();
            v.setTag(holder);
            if (mTextColor==null) mTextColor=holder.tv_row_msg.getTextColors();
        } else {
            holder = (ViewHolder) v.getTag();
        }
        SyncMessageItem o = getItem(position);
        if (o != null) {
//       		wsz_w=activity.getWindow()
//    					.getWindowManager().getDefaultDisplay().getWidth();
//   			wsz_h=activity.getWindow()
//    					.getWindowManager().getDefaultDisplay().getHeight();
//    		
//    		if (wsz_w>=700) 
//       		holder.tv_row_time.setVisibility(TextView.VISIBLE);
//        	else holder.tv_row_time.setVisibility(TextView.GONE);
            String cat = o.getCategory();
            if (cat.equals("W")) {
                holder.tv_row_time.setTextColor(mThemeColorList.text_color_warning);
                holder.tv_row_date.setTextColor(mThemeColorList.text_color_warning);
                holder.tv_row_msg.setTextColor(mThemeColorList.text_color_warning);
                holder.tv_row_time.setText(o.getTime());
                holder.tv_row_date.setText(o.getDate());
                holder.tv_row_msg.setText(o.getMessage());
            } else if (cat.equals("E")) {
                holder.tv_row_time.setTextColor(mThemeColorList.text_color_error);
                holder.tv_row_date.setTextColor(mThemeColorList.text_color_error);
                holder.tv_row_msg.setTextColor(mThemeColorList.text_color_error);
                holder.tv_row_time.setText(o.getTime());
                holder.tv_row_date.setText(o.getDate());
                holder.tv_row_msg.setText(o.getMessage());
            } else if (o.getMessage().endsWith(c.getString(R.string.msgs_mirror_task_result_ok))) {
                if (ThemeUtil.isLightThemeUsed(c)) {
                    int color=0xff0088ff;
                    holder.tv_row_time.setTextColor(color);
                    holder.tv_row_date.setTextColor(color);
                    holder.tv_row_msg.setTextColor(color);
                } else {
                    holder.tv_row_time.setTextColor(Color.GREEN);
                    holder.tv_row_date.setTextColor(Color.GREEN);
                    holder.tv_row_msg.setTextColor(Color.GREEN);
                }
                holder.tv_row_time.setText(o.getTime());
                holder.tv_row_date.setText(o.getDate());
                holder.tv_row_msg.setText(o.getMessage());
            } else {
                holder.tv_row_time.setTextColor(mTextColor);
                holder.tv_row_date.setTextColor(mTextColor);
                holder.tv_row_msg.setTextColor(mTextColor);
                holder.tv_row_time.setText(o.getTime());
                holder.tv_row_date.setText(o.getDate());
                holder.tv_row_msg.setText(o.getMessage());
            }
        }
        return v;
    }

    ;

    private class ViewHolder {
        //		TextView tv_row_cat;
        TextView tv_row_time, tv_row_date;
        NonWordwrapTextView tv_row_msg;
//		Configuration config;
    }
}

