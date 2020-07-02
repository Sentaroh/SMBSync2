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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sentaroh.android.Utilities.NotifyEvent;

import java.util.ArrayList;
import java.util.Comparator;

import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_DIR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_DIR_INVALID_CHARS;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_FILE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_PROF_FILTER_FILE_INVALID_CHARS;

@SuppressWarnings("ALL")
public class AdapterFilterList extends ArrayAdapter<AdapterFilterList.FilterListItem> {
    private Context c;
    private int id;
    private ArrayList<FilterListItem> items;

    private boolean mShowIncludeExclude = true;

    private String mFileFolderFilter = "";

    public NotifyEvent mNotifyIncExcListener = null;

    public void setNotifyIncExcListener(NotifyEvent p) {
        mNotifyIncExcListener = p;
    }

    public void unsetNotifyIncExcListener() {
        mNotifyIncExcListener = null;
    }

    public NotifyEvent mNotifyDeleteListener = null;

    public void setNotifyDeleteListener(NotifyEvent p) {
        mNotifyDeleteListener = p;
    }

    public void unsetNotifyDeleteListener() {
        mNotifyDeleteListener = null;
    }

    public AdapterFilterList(Context context, int textViewResourceId,
                             ArrayList<FilterListItem> objects, String filter_type) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
        mShowIncludeExclude = true;
        mFileFolderFilter = filter_type;
    }

    public AdapterFilterList(Context context, int textViewResourceId,
                             ArrayList<FilterListItem> objects, boolean show_inc_exc) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
        mShowIncludeExclude = show_inc_exc;
        mFileFolderFilter = "";
    }

    public FilterListItem getItem(int i) {
        return items.get(i);
    }

    public void remove(int i) {
        items.remove(i);
        notifyDataSetChanged();
    }

    public void replace(FilterListItem fli, int i) {
        items.set(i, fli);
        notifyDataSetChanged();
    }

    public void sort() {
        this.sort(new Comparator<FilterListItem>() {
            @Override
            public int compare(FilterListItem lhs,
                               FilterListItem rhs) {
                return lhs.getFilter().compareToIgnoreCase(rhs.getFilter());
            }
        });
    }

    //in main filters dialog: check if exclude/include buttons and layout parts are enabled/disabled
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
            holder = new ViewHolder();
//            holder.ll_entry=(LinearLayout) v.findViewById(R.id.filter_list_item_entry);
            holder.btn_row_delbtn = (Button) v.findViewById(R.id.filter_list_item_del_btn);
            holder.tv_row_filter = (TextView) v.findViewById(R.id.filter_list_item_filter);

            holder.rb_grp = (RadioGroup) v.findViewById(R.id.filter_list_item_rbgrp);
            holder.rb_inc = (RadioButton) v.findViewById(R.id.filter_list_item_rb_inc);
            holder.rb_exc = (RadioButton) v.findViewById(R.id.filter_list_item_rb_exc);

            holder.del_msg = c.getString(R.string.msgs_filter_list_filter_deleted);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        final FilterListItem o = getItem(position);

        if (o != null) {
            holder.tv_row_filter.setText(o.getFilter());
            holder.tv_row_filter.setVisibility(View.VISIBLE);
            holder.btn_row_delbtn.setVisibility(View.VISIBLE);
            holder.rb_grp.setVisibility(View.VISIBLE);

            if (mShowIncludeExclude) {
                holder.rb_grp.setVisibility(RadioGroup.VISIBLE);
            } else {
                holder.rb_grp.setVisibility(RadioGroup.GONE);
            }

            if (o.isDeleted()) {
                holder.tv_row_filter.setEnabled(false);
                holder.btn_row_delbtn.setEnabled(false);
                holder.rb_inc.setEnabled(false);
                holder.rb_exc.setEnabled(false);
                holder.tv_row_filter.setText(
                        holder.del_msg + " : " + o.getFilter());
            } else {
                holder.tv_row_filter.setEnabled(true);
                holder.btn_row_delbtn.setEnabled(true);
                if (o.isUseFilterV2()) {
                    String whole_dir_filter_v1=SyncTaskUtil.hasWholeDirectoryFilterItemV1(o.getFilter());
                    String whole_dir_filter_v2=SyncTaskUtil.hasWholeDirectoryFilterItemV2(o.getFilter());
                    String wild_card_only_path_parts=SyncTaskUtil.checkFilterInvalidAsteriskOnlyPath(o.getFilter());
                    String invalid_chars="";
                    String file_filter_asterisk_path="";
                    if (mFileFolderFilter.equals(SMBSYNC2_PROF_FILTER_FILE)) {
                        invalid_chars=SyncTaskUtil.checkFilterInvalidCharacter(o.getFilter(), SMBSYNC2_PROF_FILTER_FILE_INVALID_CHARS);
                        file_filter_asterisk_path=SyncTaskUtil.checkFileFilterHasAsteriskInPathToFile(o.getFilter());
                    } else if (mFileFolderFilter.equals(SMBSYNC2_PROF_FILTER_DIR)) {
                        invalid_chars=SyncTaskUtil.checkFilterInvalidCharacter(o.getFilter(), SMBSYNC2_PROF_FILTER_DIR_INVALID_CHARS);
                        //file_filter_asterisk_path="";
                    }

                    if (!invalid_chars.equals("") || !wild_card_only_path_parts.equals("")){
                        holder.rb_inc.setEnabled(false);
                        holder.rb_exc.setEnabled(false);
                    } else if (!file_filter_asterisk_path.equals("")) {//file filters cannot have asterisk in path outside the file name
                        holder.rb_inc.setEnabled(false);
                        holder.rb_exc.setEnabled(false);
                    } else if (!whole_dir_filter_v1.equals("")) {//not allowed in new filter v2
                        holder.rb_inc.setEnabled(false);
                        holder.rb_exc.setEnabled(false);
                    } else if (!whole_dir_filter_v2.equals("")) {//only exclude dir filters support whole dir prefix
                        holder.rb_inc.setEnabled(false);
                        if (mFileFolderFilter.equals(SMBSYNC2_PROF_FILTER_DIR)) holder.rb_exc.setEnabled(true);
                        else if (mFileFolderFilter.equals(SMBSYNC2_PROF_FILTER_FILE)) holder.rb_exc.setEnabled(false);
                        else holder.rb_exc.setEnabled(true);//not used
                    } else {
                        holder.rb_inc.setEnabled(true);
                        holder.rb_exc.setEnabled(true);
                    }
                } else {
                    holder.rb_inc.setEnabled(true);
                    holder.rb_exc.setEnabled(true);
                }
            }

            final int p = position;
            // 必ずsetChecked前にリスナを登録(convertView != null の場合は既に別行用のリスナが登録されている！)
            holder.btn_row_delbtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    o.delete();
// 					items.set(p,o);

                    holder.tv_row_filter.setEnabled(false);
                    holder.btn_row_delbtn.setEnabled(false);
                    holder.rb_inc.setEnabled(false);
                    holder.rb_exc.setEnabled(false);
                    holder.tv_row_filter.setText(holder.del_msg + " : " + o.getFilter());

                    items.remove(p);
                    notifyDataSetChanged();

                    if (mNotifyDeleteListener != null)
                        mNotifyDeleteListener.notifyToListener(true, new Object[]{o});
                }

            });
            holder.rb_inc.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    o.setInclude(true);
                    if (mNotifyIncExcListener != null)
                        mNotifyIncExcListener.notifyToListener(true, new Object[]{o});
// 					items.set(p, o);
// 					Log.v("","cb i filter="+o.getFilter()+",incexc="+o.isInclude());
                }
            });
            holder.rb_exc.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    o.setInclude(false);
                    if (mNotifyIncExcListener != null)
                        mNotifyIncExcListener.notifyToListener(true, new Object[]{o});
// 					items.set(p, o);
// 					Log.v("","cb i filter="+o.getFilter()+",incexc="+o.isInclude());
                }
            });
            if (o.isInclude()) holder.rb_inc.setChecked(true);
            else holder.rb_exc.setChecked(true);
        }

        return v;
    }

    private static class ViewHolder {
        TextView tv_row_filter, tv_row_cat, tv_row_incExc;
        Button btn_row_delbtn;
        //		EditText et_filter;
        RadioButton rb_inc, rb_exc;
        RadioGroup rb_grp;
        //		LinearLayout ll_entry;
        String del_msg;

    }

    static class FilterListItem implements Comparable<FilterListItem> {

        private String filter="";
        private boolean includeFilter=true;// false:Exclude, true: Include
        private boolean regExp=false;
        private boolean deleted = false;

        private boolean use_filter_v2 = false;

        public FilterListItem(String filter, boolean include) {
            this.filter = filter;
            this.includeFilter = include;
            this.deleted = false;
        }

        public boolean isUseFilterV2() {
            return use_filter_v2;
        }
        public void setUseFilterV2(boolean enable) {
            use_filter_v2=enable;
        }

        public String getFilter() {
            return this.filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public boolean isInclude() {
            return this.includeFilter;
        }

        public void setInclude(boolean include) {
            this.includeFilter = include;
        }

        public boolean isRegExp() {
            return this.regExp;
        }

        public void setRegExp(boolean include) {
            this.regExp = include;
        }

        public boolean isDeleted() {
            return this.deleted;
        }

        public void delete() {
            this.deleted = true;
        }

        @Override
        public int compareTo(FilterListItem o) {
            if (this.filter != null)
                return this.filter.toLowerCase().compareTo(o.getFilter().toLowerCase());
//				return this.filename.toLowerCase().compareTo(o.getName().toLowerCase()) * (-1);
            else
                throw new IllegalArgumentException();
        }
    }

}