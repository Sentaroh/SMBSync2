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
package com.sentaroh.android.SMBSync2;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Window;

import com.sentaroh.android.Utilities.Dialog.MessageDialogAppFragment;
import com.sentaroh.android.Utilities.Dialog.MessageDialogFragment;
import com.sentaroh.android.Utilities.NotifyEvent;

public class ActivityIntentHandler extends Activity {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new GlobalParameters().setNewLocale(base, false));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transrucent);
        final Context c=ActivityIntentHandler.this;
        Intent received_intent=getIntent();
        if (received_intent.getAction()!=null && !received_intent.getAction().equals("")) {
            Intent in=new Intent(received_intent.getAction());
            in.setClass(ActivityIntentHandler.this, SyncService.class);
            if (received_intent.getExtras() != null) in.putExtras(received_intent.getExtras());
            final FragmentManager fm=getFragmentManager();
            try {
                if (Build.VERSION.SDK_INT>=26) {
//                    startForegroundService(in);
                    startService(in);
                } else {
                    startService(in);
                }
                finish();
            }catch(Exception e){
                e.printStackTrace();
                NotifyEvent ntfy=new NotifyEvent(c);
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context context, Object[] objects) {
                        finish();
                    }

                    @Override
                    public void negativeResponse(Context context, Object[] objects) {

                    }
                });
                MessageDialogAppFragment mdf=MessageDialogAppFragment.newInstance(false, "E",
                        "SMBSync2", "ActivityIntentHandler start service error\n"+e.getMessage());
                mdf.showDialog(fm, mdf, ntfy);
            }

        }
    }

}
