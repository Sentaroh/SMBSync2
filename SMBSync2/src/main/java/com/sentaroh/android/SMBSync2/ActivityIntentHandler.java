package com.sentaroh.android.SMBSync2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class ActivityIntentHandler extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transrucent);

        Intent received_intent=getIntent();
        if (received_intent.getAction()!=null && !received_intent.getAction().equals("")) {
            Intent in=new Intent(received_intent.getAction());
            in.setClass(ActivityIntentHandler.this, SyncService.class);
            if (received_intent.getExtras() != null) in.putExtras(received_intent.getExtras());
            startService(in);
        }

        finish();
    }

}
