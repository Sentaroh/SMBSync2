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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.sentaroh.android.SMBSync2.Log.LogUtil;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class NotificationUtil {

    final public static String NOTIFICATION_CHANNEL_DEFAULT="SMBSync2";
    final public static String NOTIFICATION_CHANNEL_SOUND="Sound";
    final public static String NOTIFICATION_CHANNEL_VIBRATE="Vibrate";
    final public static String NOTIFICATION_CHANNEL_BOTH="Vibrate_Sound";
    static final public void setNotificationEnabled(GlobalParameters gwa, boolean p) {
        gwa.notificationEnabled = p;
    }

    static final public boolean isNotificationEnabled(GlobalParameters gwa) {
        return gwa.notificationEnabled;
    }

    static final public void initNotification(GlobalParameters gwa, CommonUtilities util, Context c) {
        gwa.notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        gwa.notification = new Notification(R.drawable.ic_48_smbsync_wait,
                c.getString(R.string.app_name), 0);

        gwa.notificationAppName = c.getString(R.string.app_name);

        gwa.notificationIntent = new Intent(c, ActivityMain.class);
        gwa.notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        gwa.notificationIntent.setAction(Intent.ACTION_MAIN);
        gwa.notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        gwa.notificationPendingIntent = PendingIntent.getActivity(c, 0, gwa.notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
//        gwa.notificationLargeIcon = BitmapFactory.decodeResource(c.getResources(), gwa.notificationSmallIcon);
        gwa.notificationBuilder = new NotificationCompat.Builder(c);
        gwa.notificationBuilder.setContentIntent(gwa.notificationPendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_48_smbsync_wait)//smbsync_animation)
                .setContentTitle(gwa.notificationAppName)
                .setContentText("")
                .setWhen(0)
        ;
        if (Build.VERSION.SDK_INT>=26) {
            gwa.notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_DEFAULT);
        }
        gwa.notification = gwa.notificationBuilder.build();
        gwa.notificationBigTextStyle =
                new NotificationCompat.BigTextStyle(gwa.notificationBuilder);
        gwa.notificationBigTextStyle
                .setBigContentTitle(gwa.notificationLastShowedTitle)
                .bigText(gwa.notificationLastShowedMessage);

        if (Build.VERSION.SDK_INT>=26) {
            NotificationChannel def_ch = new NotificationChannel(
                    "SMBSync2",
                    "SMBSync2",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            def_ch.enableLights(false);
            def_ch.setSound(null,null);
            def_ch.enableVibration(false);
            def_ch.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            gwa.notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_DEFAULT);
            gwa.notificationManager.createNotificationChannel(def_ch);

            NotificationChannel sound_ch = new NotificationChannel(
                    "Sound",
                    "Sound",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            sound_ch.enableLights(false);
            sound_ch.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
            sound_ch.enableVibration(false);
            sound_ch.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            gwa.notificationManager.deleteNotificationChannel("Sound");
            gwa.notificationManager.createNotificationChannel(sound_ch);

            NotificationChannel vibrate_ch = new NotificationChannel(
                    "Vibrate",
                    "Vibrate",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            vibrate_ch.enableLights(false);
            vibrate_ch.setSound(null, null);
            vibrate_ch.enableVibration(true);
            vibrate_ch.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            gwa.notificationManager.deleteNotificationChannel("Vibrate");
            gwa.notificationManager.createNotificationChannel(vibrate_ch);

            NotificationChannel vibrate_sound_ch = new NotificationChannel(
                    "Vibrate_Sound",
                    "Vibrate_Sound",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            vibrate_sound_ch.enableLights(false);
            vibrate_sound_ch.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
            vibrate_sound_ch.enableVibration(true);
            vibrate_sound_ch.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            gwa.notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_BOTH);
            gwa.notificationManager.createNotificationChannel(vibrate_sound_ch);

        }

    }

    static final public void setNotificationIcon(GlobalParameters gwa, CommonUtilities util,
                                                 int small_icon, int large_icon) {
        gwa.notificationSmallIcon = small_icon;
//        if (gwa.notificationLargeIcon != null) gwa.notificationLargeIcon.recycle();
//        gwa.notificationLargeIcon = BitmapFactory.decodeResource(c.getResources(), large_icon);
        gwa.notificationBuilder.setContentIntent(gwa.notificationPendingIntent)
                .setSmallIcon(gwa.notificationSmallIcon)//smbsync_animation)
                ;//.setLargeIcon(gwa.notificationLargeIcon);
        ;
        if (Build.VERSION.SDK_INT>=26) {
            gwa.notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_DEFAULT);
        }
        gwa.notification = gwa.notificationBuilder.build();
        gwa.notificationBigTextStyle =
                new NotificationCompat.BigTextStyle(gwa.notificationBuilder);
        gwa.notificationBigTextStyle
                .setBigContentTitle(gwa.notificationLastShowedTitle)
                .bigText(gwa.notificationLastShowedMessage);
    }

    final static public Notification getNotification(GlobalParameters gwa) {
        return gwa.notification;
    }

    //sets the text of the progress message in system notification area when app is in background
    final static public void setNotificationMessage(GlobalParameters gwa, CommonUtilities util, long when,
                                                    String prof, String fp, String msg, String type) {
        if (prof.equals("")) gwa.notificationLastShowedTitle = gwa.notificationAppName;
        else gwa.notificationLastShowedTitle = prof;//gwa.notificationAppName+"       "+prof;

        String[] msg_list = {fp, msg, type};
        String final_msg = "";
        for (String msg_part : msg_list) {//remove empty messages to avoid empty lines in the notification
            if (!msg_part.equals("")) {
                if (final_msg.equals("")) final_msg = msg_part;
                else final_msg = final_msg.concat("\n").concat(msg_part);
            }
        }

        gwa.notificationLastShowedMessage = final_msg;

        gwa.notificationLastShowedWhen = when;
    }

    final static public Notification showOngoingMsg(GlobalParameters gwa, CommonUtilities util, long when,
                                                    String msg) {
        return showOngoingMsg(gwa, util, when, "", "", msg, "");
    }

    final static public Notification showOngoingMsg(GlobalParameters gwa, CommonUtilities util, long when,
                                                    String prof, String msg) {
        return showOngoingMsg(gwa, util, when, prof, "", msg, "");
    }

    final static public Notification showOngoingMsg(GlobalParameters gwa, CommonUtilities util, long when,
                                                    String prof, String fp, String msg) {
        return showOngoingMsg(gwa, util, when, prof, fp, msg, "");
    }

    //draw the system notification with sync progress when app in background
    final static public Notification showOngoingMsg(GlobalParameters gwa, CommonUtilities util, long when,
                                                    String prof, String fp, String msg, String type) {
        setNotificationMessage(gwa, util, when, prof, fp, msg, type);
        gwa.notificationBuilder
                .setContentIntent(gwa.notificationPendingIntent)
                .setContentTitle(gwa.notificationLastShowedTitle)
                .setContentText(gwa.notificationLastShowedMessage)
                .setSmallIcon(gwa.notificationSmallIcon)//smbsync_animation)
//                .setLargeIcon(gwa.notificationLargeIcon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)//.PRIORITY_MAX)
        ;
        if (when != 0) gwa.notificationBuilder.setWhen(when);
        if (Build.VERSION.SDK_INT>=26) {
            gwa.notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_DEFAULT);
        }
        gwa.notificationBigTextStyle
                .setBigContentTitle(gwa.notificationLastShowedTitle)
                .bigText(gwa.notificationLastShowedMessage);
        gwa.notification = gwa.notificationBigTextStyle.build();
        if (isNotificationEnabled(gwa))
            gwa.notificationManager.notify(R.string.app_name, gwa.notification);

        return gwa.notification;
    }

    final static public Notification reShowOngoingMsg(GlobalParameters gwa, CommonUtilities util) {
        gwa.notificationBuilder
                .setContentIntent(gwa.notificationPendingIntent)
                .setContentTitle(gwa.notificationLastShowedTitle)
                .setContentText(gwa.notificationLastShowedMessage)
                .setSmallIcon(gwa.notificationSmallIcon)//smbsync_animation)
//                .setLargeIcon(gwa.notificationLargeIcon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)//.PRIORITY_MAX)
        ;
        if (Build.VERSION.SDK_INT>=26) {
            gwa.notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_DEFAULT);
        }
        gwa.notificationBuilder.setWhen(gwa.notificationLastShowedWhen);
        gwa.notificationBigTextStyle
                .setBigContentTitle(gwa.notificationLastShowedTitle)
                .bigText(gwa.notificationLastShowedMessage);
        gwa.notification = gwa.notificationBigTextStyle.build();
        if (isNotificationEnabled(gwa))
            gwa.notificationManager.notify(R.string.app_name, gwa.notification);
//		}

        return gwa.notification;
    }

    final static public void showNoticeMsg(Context c, GlobalParameters gwa, CommonUtilities util, String msg) {
        clearNotification(gwa, util);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
        builder.setOngoing(false)
                .setAutoCancel(true)
                .setSmallIcon(gwa.notificationSmallIcon)//smbsync_animation)
                .setContentTitle(c.getString(R.string.app_name))
                .setContentText(msg)
                .setWhen(System.currentTimeMillis())
        ;
        if (Build.VERSION.SDK_INT>=26) {
            builder.setChannelId("SMBSync2");//SMBSync2");
        } else {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        if (gwa.callbackStub != null || (gwa.syncMessageList != null && gwa.syncMessageList.size() > 0)) {
            Intent activity_intent = new Intent(c, ActivityMain.class);
            PendingIntent activity_pi = PendingIntent.getActivity(c, 0, activity_intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(activity_pi);
        } else {
            if (!LogUtil.getLogFilePath(gwa).equals("") && gwa.settingLogOption) {
                File lf = new File(LogUtil.getLogFilePath(gwa));
                if (lf.exists()) {
                    Intent br_log_intent = new Intent(Intent.ACTION_VIEW);
                    br_log_intent.setDataAndType(Uri.parse("file://" + LogUtil.getLogFilePath(gwa)), "text/plain");
                    PendingIntent br_log_pi = PendingIntent.getActivity(c, 0, br_log_intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(br_log_pi);
                } else {
                    Intent dummy_intent = new Intent(c, ActivityMain.class);
                    PendingIntent dummy_pi = PendingIntent.getActivity(c, 0, dummy_intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    dummy_pi.cancel();
                    builder.setContentIntent(dummy_pi);
                }
            }
        }
        if (isNotificationEnabled(gwa))
            gwa.notificationManager.notify(R.string.app_name, builder.build());
    }

    final static public void showNoticeMsg(Context c, GlobalParameters gwa, CommonUtilities util, String msg, boolean playback_sound, boolean vibration) {
        clearNotification(gwa, util);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
//        Bitmap bm=BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_48_smbsync_err);
        builder.setOngoing(false)
                .setAutoCancel(true)
                .setSmallIcon(gwa.notificationSmallIcon)//smbsync_animation)
//                .setLargeIcon(bm)//smbsync_animation)
                .setContentTitle(c.getString(R.string.app_name))
                .setContentText(msg)
                .setWhen(System.currentTimeMillis())
        ;

        if (playback_sound) builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        if (vibration) builder.setVibrate(new long[]{0, 300, 200, 300});
        if (Build.VERSION.SDK_INT>=26) {
            if (playback_sound && vibration) builder.setChannelId(NOTIFICATION_CHANNEL_BOTH);
            else if (playback_sound && !vibration) builder.setChannelId(NOTIFICATION_CHANNEL_SOUND);
            else if (!playback_sound && vibration) builder.setChannelId(NOTIFICATION_CHANNEL_VIBRATE);
            else builder.setChannelId(NOTIFICATION_CHANNEL_DEFAULT);
        }
        if (gwa.callbackStub != null || (gwa.syncMessageList != null && gwa.syncMessageList.size() > 0)) {
            Intent activity_intent = new Intent(c, ActivityMain.class);
            PendingIntent activity_pi = PendingIntent.getActivity(c, 0, activity_intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(activity_pi);
        } else {
            if (!LogUtil.getLogFilePath(gwa).equals("") && gwa.settingLogOption) {
                File lf = new File(LogUtil.getLogFilePath(gwa));
                if (lf.exists()) {
                    Intent br_log_intent = new Intent(Intent.ACTION_VIEW);
                    br_log_intent.setDataAndType(Uri.parse("file://" + LogUtil.getLogFilePath(gwa)), "text/plain");
                    PendingIntent br_log_pi = PendingIntent.getActivity(c, 0, br_log_intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(br_log_pi);
                } else {
                    Intent dummy_intent = new Intent(c, ActivityMain.class);
                    PendingIntent dummy_pi = PendingIntent.getActivity(c, 0, dummy_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    dummy_pi.cancel();
                    builder.setContentIntent(dummy_pi);
                }
            }
        }
        if (isNotificationEnabled(gwa))
            gwa.notificationManager.notify(R.string.app_name, builder.build());
    }

    final static public void clearNotification(GlobalParameters gwa, CommonUtilities util) {
        try {
            gwa.notificationManager.cancelAll();
        } catch(SecurityException e) {
            if (util!=null) {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                pw.close();
                util.addLogMsg("E","Error occured at clearNotification()","\n",sw.toString());
            }
        }
    }

}
