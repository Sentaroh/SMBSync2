package com.sentaroh.android.SMBSync2;

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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.sentaroh.android.Utilities.NotifyEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@SuppressWarnings("ALL")
public class AdapterNetworkScanResult extends ArrayAdapter<AdapterNetworkScanResult.NetworkScanListItem> {

    private ArrayList<NetworkScanListItem> mResultList = null;
    private int mResourceId = 0;
    private Context mContext;
    private NotifyEvent mNtfyEvent = null;
    private boolean mButtonEnabled = true;

    public AdapterNetworkScanResult(Context context, int resource,
                                    ArrayList<NetworkScanListItem> objects, NotifyEvent ntfy) {
        super(context, resource, objects);
        mResultList = objects;
        mResourceId = resource;
        mContext = context;
        mNtfyEvent = ntfy;
    }

    public void setButtonEnabled(boolean p) {
        mButtonEnabled = p;
        notifyDataSetChanged();
    }

    @Override
    public void add(NetworkScanListItem item) {
        synchronized (mResultList) {
            mResultList.add(item);
            notifyDataSetChanged();
        }
    }

    public void sort() {
        synchronized (mResultList) {
            Collections.sort(mResultList, new Comparator<NetworkScanListItem>() {
                @Override
                public int compare(NetworkScanListItem lhs,
                                   NetworkScanListItem rhs) {
                    String r_o4 = rhs.server_address.substring(rhs.server_address.lastIndexOf(".") + 1);
                    String r_key = String.format("%3s", Integer.parseInt(r_o4)).replace(" ", "0");
                    String l_o4 = lhs.server_address.substring(lhs.server_address.lastIndexOf(".") + 1);
                    String l_key = String.format("%3s", Integer.parseInt(l_o4)).replace(" ", "0");
                    return l_key.compareTo(r_key);
                }
            });
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        final NetworkScanListItem o = getItem(position);
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(mResourceId, null);
            holder = new ViewHolder();
            holder.tv_name = (Button) v.findViewById(R.id.scan_result_list_item_server_name);
            holder.tv_addr = (Button) v.findViewById(R.id.scan_result_list_item_server_addr);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        if (o != null) {
            holder.tv_name.setText(o.server_name+"\n"+o.server_smb_supported);
            holder.tv_addr.setText(o.server_address);
//            if (o.server_name.startsWith("---")) {
//                holder.tv_addr.setVisibility(Button.GONE);
//            } else {
//                holder.tv_addr.setVisibility(Button.VISIBLE);
//            }
            if (o.server_name.equals("")) holder.tv_name.setEnabled(false);
            else holder.tv_name.setEnabled(true);
//            Log.v("SMBSync2","name="+o.server_name+", addr="+o.server_address);
            holder.tv_name.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (o.server_name.equals("") || !mButtonEnabled) return;
                    mNtfyEvent.notifyToListener(true, new String[]{"N", o.server_name});
                }
            });

            holder.tv_addr.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (!mButtonEnabled) return;
                    mNtfyEvent.notifyToListener(true, new String[]{"A", o.server_address});
                }
            });
        }
        return v;
    }

    class ViewHolder {
        Button tv_name, tv_addr;
    }

    static class NetworkScanListItem {
        public static final String SMB_STATUS_UNSUCCESSFULL="Unsuccessfull";
        public static final String SMB_STATUS_ACCESS_DENIED="Access denied";
        public static final String SMB_STATUS_INVALID_LOGON_TYPE="Invalid login type";
        public static final String SMB_STATUS_UNKNOWN_ACCOUNT="Unknown account or invalid password";

        public String server_name = "";
        public String server_address = "";

        public String server_smb_supported = "";
        public String server_smb_smb1_status = "";
        public String server_smb_smb2_status = "";
        public String server_smb_smb3_status = "";
        public String server_smb_smb1_share_list = "";
        public String server_smb_smb2_share_list = "";
        public String server_smb_smb3_share_list = "";

    }
}

