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
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;

import static com.sentaroh.android.SMBSync2.Constants.PACKAGE_NAME;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_AUTO_SYNC_INTENT;

public class ShortcutAutoSync extends FragmentActivity {

    private Context mContext;

    private CommonUtilities mUtil = null;
    private GlobalParameters mGp = null;
    private ShortcutAutoSync mActivity=null;

    private int restartStatus = 0;
    private boolean displayDialogRequired = false;

    @Override
    final protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("displayDialogRequired", displayDialogRequired);
    }

    @Override
    final protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        displayDialogRequired = savedState.getBoolean("displayDialogRequired", true);
        restartStatus = 2;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new GlobalParameters().setNewLocale(base, true));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transrucent);
        mActivity=ShortcutAutoSync.this;
//        envParms.loadSettingParms(context);
//        mUtil=new CommonUtilities(context, "ShortCutSleep", envParms);
//        mGp = (GlobalParameters) getApplication();
//        mGp = (GlobalParameters) getApplicationContext();
        mContext = mActivity;
        mGp= GlobalWorkArea.getGlobalParameters(mContext);
        if (mGp.themeColorList == null) {
            mGp.themeColorList = CommonUtilities.getThemeColorList(mActivity);
        }

        mUtil = new CommonUtilities(mActivity.getApplicationContext(), "Shortcuut", mGp, getSupportFragmentManager());

        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
        // Application process is follow

    }

    @Override
    public void onStart() {
        super.onStart();
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
    }

    final public void onResume() {
        super.onResume();
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered restartStaus=" + restartStatus);

        if (restartStatus == 0) {
            NotifyEvent ntfy = new NotifyEvent(mContext);
            ntfy.setListener(new NotifyEventListener() {
                @Override
                public void positiveResponse(Context c, Object[] o) {
//                    if (SyncService.isDuplicateRequest(mGp, SMBSYNC2_SYNC_REQUEST_SHORTCUT)) {
//                        mUtil.addLogMsg("W",
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
                    mUtil.addDebugMsg(1,"I","startService issued");
                    Intent in = new Intent(mContext, SyncService.class);
                    in.setAction(SMBSYNC2_AUTO_SYNC_INTENT);
                    mContext.startService(in);
//                    if (Build.VERSION.SDK_INT<26) {//Android 5/6/7
//                        mUtil.addDebugMsg(1,"I","startService issued");
//                        Intent in = new Intent(mContext, SyncService.class);
//                        in.setAction(SMBSYNC2_AUTO_SYNC_INTENT);
//                        mContext.startService(in);
//                    } else {//Android 8/9/10
//                        mUtil.addDebugMsg(1,"I","startActivity issued");
//                        Intent in = new Intent(SMBSYNC2_AUTO_SYNC_INTENT);
//                        in.setClassName(PACKAGE_NAME, PACKAGE_NAME+".ActivityIntentHandler");
//                        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(in);
//                    }
                    terminateShortcut();
                }

                @Override
                public void negativeResponse(Context c, Object[] o) {
                    terminateShortcut();
                }
            });
            if (!mGp.settingSuppressShortcutWarning) {
                mUtil.showCommonDialog(true, "W", "auto syncを実行しますか？", "", ntfy);
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

    @Override
    public void onPause() {
        super.onPause();
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
        // Application process is follow


    }

    @Override
    public void onStop() {
        super.onStop();
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
        // Application process is follow


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUtil.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered restartStaus=" + restartStatus);
        // Application process is follow
        System.gc();
//		android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        // Ignore orientation change to keep activity from restarting
        super.onConfigurationChanged(newConfig);
    }

}
