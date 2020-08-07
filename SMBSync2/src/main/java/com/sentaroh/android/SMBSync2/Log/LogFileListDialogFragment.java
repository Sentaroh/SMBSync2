package com.sentaroh.android.SMBSync2.Log;
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


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;

import com.sentaroh.android.SMBSync2.BuildConfig;
import com.sentaroh.android.SMBSync2.R;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.Dialog.MessageDialogFragment;
import com.sentaroh.android.Utilities.LogUtil.CommonLogFileListDialogFragment;

import java.io.File;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class LogFileListDialogFragment extends CommonLogFileListDialogFragment {
    public static LogFileListDialogFragment newInstance(String theme_id, boolean retainInstance, String title, String send_msg, String enable_msg, String send_subject) {
        LogFileListDialogFragment frag = new LogFileListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("theme_id", theme_id);
        bundle.putBoolean("retainInstance", retainInstance);
        bundle.putBoolean("showSaveButton", true);
        bundle.putString("title", title);
        bundle.putString("msgtext", send_msg);
        bundle.putString("enableMsg", enable_msg);
        bundle.putString("subject", send_subject);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void startLogfileViewerIntent(Context c, String fpath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri=createLogFileUri(c, fpath);
        intent.setDataAndType(uri, "text/plain");
        startActivity(intent);
    }

    @Override
    public Uri createLogFileUri(Context c, String fpath) {
        Uri uri=null;
        if (Build.VERSION.SDK_INT>=24) {
            uri= FileProvider.getUriForFile(c, BuildConfig.APPLICATION_ID + ".provider", new File(fpath));
        } else {
            uri=Uri.parse("file://"+fpath);
        }
        return uri;
    }

}
