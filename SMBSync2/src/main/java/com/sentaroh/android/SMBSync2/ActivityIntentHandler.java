package com.sentaroh.android.SMBSync2;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.sentaroh.android.Utilities.Dialog.MessageDialogAppFragment;
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
            try {
                c.startService(in);
                finish();
            } catch(Exception e) {
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
                final FragmentManager fm=getFragmentManager();
                MessageDialogAppFragment mdf=MessageDialogAppFragment.newInstance(false, "E",
                        "SMBSync2", "ShortcutAutoSync start service error\n"+e.getMessage());
                mdf.showDialog(fm, mdf, ntfy);
            }

        }
    }

}
