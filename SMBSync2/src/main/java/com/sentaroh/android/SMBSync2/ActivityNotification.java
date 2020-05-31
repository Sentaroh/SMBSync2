package com.sentaroh.android.SMBSync2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.Window;

public class ActivityNotification extends Activity {
    private ActivityNotification mActivity=null;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(new GlobalParameters().setNewLocale(base, false));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transrucent);
        mActivity=ActivityNotification.this;
        Intent in=getIntent();
        if (in!=null) {
            if (in.getBooleanExtra("SOUND",false)) playBackDefaultNotification(in.getIntExtra("SOUND_VOLUME",100));
            if (in.getBooleanExtra("VIBRATE",false)) vibrateDefaultPattern();
        }

        Handler hndl = new Handler();
        hndl.postDelayed(new Runnable() {
            @Override
            public void run() {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }, 1000);


        finish();
    }

    private void playBackDefaultNotification(int volume) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (uri != null) {
            final MediaPlayer player = MediaPlayer.create(mActivity, uri);
            if (player != null) {
                float vol = (float) volume / 100.0f;
                player.setVolume(vol, vol);
                if (player != null) {
                    final Thread th = new Thread() {
                        @Override
                        public void run() {
                            int dur = player.getDuration();
                            player.start();
                            SystemClock.sleep(dur + 10);
                            player.stop();
                            player.reset();
                            player.release();
                        }
                    };
                    th.setPriority(Thread.MAX_PRIORITY);
                    th.start();
                }
            } else {
//                mUtil.addLogMsg("I", "Default notification can not playback, because default playback is not initialized.");
            }
        }
    }

    private void vibrateDefaultPattern() {
        Thread th = new Thread() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(new long[]{0, 300, 200, 300}, -1);
            }
        };
        th.setPriority(Thread.MAX_PRIORITY);
        th.start();
    }


}
