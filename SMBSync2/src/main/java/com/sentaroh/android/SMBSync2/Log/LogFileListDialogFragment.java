package com.sentaroh.android.SMBSync2.Log;

import com.sentaroh.android.Utilities.LogUtil.CommonLogFileListDialogFragment;

import android.os.Bundle;

public class LogFileListDialogFragment extends CommonLogFileListDialogFragment{
	public static LogFileListDialogFragment newInstance(boolean retainInstance, String title) {
		LogFileListDialogFragment frag = new LogFileListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("retainInstance", retainInstance);
        bundle.putString("title", title);
//        bundle.putString("msgtext", msgtext);
        frag.setArguments(bundle);
        return frag;
    }

}
