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

import static com.sentaroh.android.SMBSync2.Constants.*;

import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities.ThemeUtil;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.Toast;

public class ShortcutAutoSync extends FragmentActivity {

    private Context mContext;

    private SyncUtil util = null;
    private GlobalParameters mGp = null;
    private CommonDialog commonDlg = null;

    private int restartStatus = 0;
    private boolean displayDialogRequired = false;

    @Override
    final protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("displayDialogRequired", displayDialogRequired);
    }

    ;

    @Override
    final protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        displayDialogRequired = savedState.getBoolean("displayDialogRequired", false);
        restartStatus = 2;
    }

    ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transrucent);

//        envParms.loadSettingParms(context);
//        util=new SyncUtil(context, "ShortCutSleep", envParms);
//        mGp = (GlobalParameters) getApplication();
//        mGp = (GlobalParameters) getApplicationContext();
        mContext = getApplicationContext();
        mGp=CommonStaticPointer.initGlobalParameters(mContext);
        if (mGp.themeColorList == null) {
            mGp.themeColorList = ThemeUtil.getThemeColorList(this);
        }

        util = new SyncUtil(this.getApplicationContext(), "Shortcuut", mGp);

        commonDlg = new CommonDialog(this, getSupportFragmentManager());

        util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
        // Application process is follow

    }

    @Override
    public void onStart() {
        super.onStart();
        util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
    }

    final public void onResume() {
        super.onResume();
        util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered restartStaus=" + restartStatus);

        if (restartStatus == 0) {
            NotifyEvent ntfy = new NotifyEvent(mContext);
            ntfy.setListener(new NotifyEventListener() {
                @Override
                public void positiveResponse(Context c, Object[] o) {
//                    if (SyncService.isDuplicateRequest(mGp, SMBSYNC2_SYNC_REQUEST_SHORTCUT)) {
//                        util.addLogMsg("W",
//                                String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_duplicate_request),
//                                        SMBSYNC2_SYNC_REQUEST_SHORTCUT));
//                        Toast.makeText(mContext,
//                                String.format(mContext.getString(R.string.msgs_svc_received_start_request_ignored_duplicate_request),
//                                        SMBSYNC2_SYNC_REQUEST_SHORTCUT), Toast.LENGTH_LONG)
//                                .show();
//                    } else {
////						Toast.makeText(mContext,
////								mContext.getString(R.string.msgs_svc_received_start_request_from_shortcut),
////							Toast.LENGTH_LONG)
////							.show();
//                        Intent in = new Intent(mContext, SyncService.class);
//                        in.setAction(SMBSYNC2_AUTO_SYNC_INTENT);
//                        mContext.startService(in);
//                    }
                    Intent in = new Intent(mContext, SyncService.class);
                    in.setAction(SMBSYNC2_AUTO_SYNC_INTENT);
                    mContext.startService(in);
                    terminateShortcut();
                }

                @Override
                public void negativeResponse(Context c, Object[] o) {
                    terminateShortcut();
                }
            });
            if (!mGp.settingSuppressShortcutWarning) {
                commonDlg.showCommonDialog(true, "W", "auto syncを実行しますか？", "", ntfy);
            } else {
                ntfy.notifyToListener(true, null);
            }
        }
    }

    ;

    private void terminateShortcut() {
        Handler hndl = new Handler();
        hndl.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 100);
    }

    ;

    @Override
    public void onPause() {
        super.onPause();
        util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
        // Application process is follow


    }

    ;

    @Override
    public void onStop() {
        super.onStop();
        util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
        // Application process is follow


    }

    ;

    @Override
    public void onDestroy() {
        super.onDestroy();
        util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
        // Application process is follow
        System.gc();
//		android.os.Process.killProcess(android.os.Process.myPid());
    }

    ;

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        // Ignore orientation change to keep activity from restarting
        super.onConfigurationChanged(newConfig);
    }

    ;

}
