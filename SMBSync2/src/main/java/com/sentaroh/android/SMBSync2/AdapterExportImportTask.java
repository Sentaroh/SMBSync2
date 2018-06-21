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
import android.widget.CheckedTextView;

import com.sentaroh.android.Utilities.NotifyEvent;

import java.util.ArrayList;

public class AdapterExportImportTask extends ArrayAdapter<AdapterExportImportTask.ExportImportListItem> {
    private Context c;
    private int id;
    private ArrayList<ExportImportListItem> items;

    public AdapterExportImportTask(Context context, int textViewResourceId, ArrayList<ExportImportListItem> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    @Override
    final public int getCount() {
        return items.size();
    }

    public boolean isItemSelected() {
        boolean result = false;

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isChecked) {
                result = true;
                break;
            }
        }

        return result;
    }

    private NotifyEvent cb_ntfy = null;

    final public void setCheckButtonListener(NotifyEvent ntfy) {
        cb_ntfy = ntfy;
    }

    @Override
    final public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
            holder = new ViewHolder();
            holder.ctv_item = (CheckedTextView) v.findViewById(R.id.export_import_profile_list_item_item);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        final ExportImportListItem o = items.get(position);
        if (o != null) {
            holder.ctv_item.setText(o.item_name);
            // 必ずsetChecked前にリスナを登録
            holder.ctv_item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.ctv_item.toggle();
                    boolean isChecked = holder.ctv_item.isChecked();
                    o.isChecked = isChecked;
                    if (cb_ntfy != null) cb_ntfy.notifyToListener(true, null);
                }
            });
            holder.ctv_item.setChecked(o.isChecked);
        }
        return v;
    }

    class ViewHolder {
        CheckedTextView ctv_item;
    }

    static class ExportImportListItem {
        public boolean isChecked = false;
        public String item_name = "";
    }

}
