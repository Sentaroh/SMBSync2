package com.sentaroh.android.SMBSync2;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import java.util.List;

public class ActivityIntentHandler extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transrucent);

        Intent received_intent=getIntent();
        if (received_intent.getAction()!=null && !received_intent.getAction().equals("")) {
            Intent in=new Intent(received_intent.getAction());
            in.setClass(this, SyncReceiver.class);
            if (received_intent.getExtras() != null) in.putExtras(received_intent.getExtras());
            sendBroadcast(in,null);
        }

        finish();
    }

}
