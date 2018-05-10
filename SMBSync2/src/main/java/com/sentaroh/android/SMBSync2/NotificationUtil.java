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

import java.io.File;

import com.sentaroh.android.SMBSync2.Log.LogUtil;
import com.sentaroh.android.SMBSync2.R;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NotificationUtil {

    static final public void setNotificationEnabled(GlobalParameters gwa, boolean p) {
        gwa.notificationEnabled = p;
    }

    static final public boolean isNotificationEnabled(GlobalParameters gwa) {
        return gwa.notificationEnabled;
    }

    static final public void initNotification(GlobalParameters gwa) {
        gwa.notificationManager = (NotificationManager) gwa.appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        gwa.notification = new Notification(R.drawable.ic_48_smbsync_wait,
                gwa.appContext.getString(R.string.app_name), 0);

        gwa.notificationAppName = gwa.appContext.getString(R.string.app_name);

        gwa.notificationIntent = new Intent(gwa.appContext, ActivityMain.class);
        gwa.notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        gwa.notificationIntent.setAction(Intent.ACTION_MAIN);
        gwa.notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        gwa.notificationPendingIntent = PendingIntent.getActivity(gwa.appContext, 0, gwa.notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        gwa.notificationLargeIcon = BitmapFactory.decodeResource(gwa.appContext.getResources(), gwa.notificationSmallIcon);
        gwa.notificationBuilder = new NotificationCompat.Builder(gwa.appContext);
        gwa.notificationBuilder.setContentIntent(gwa.notificationPendingIntent)
//		   	.setTicker(gwa.notificationAppName)
                .setOngoing(true)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_48_smbsync_wait)//smbsync_animation)
                .setLargeIcon(gwa.notificationLargeIcon)
                .setContentTitle(gwa.notificationAppName)
                .setContentText("")
//		    .setSubText("subtext")
//		    .setLargeIcon(largeIcon)
                .setWhen(0)
//			.addAction(action_icon, action_title, action_pi)
        ;
        if (Build.VERSION.SDK_INT>=26) {
            gwa.notificationBuilder.setChannelId("SMBSync2");
        }
        gwa.notification = gwa.notificationBuilder.build();
        gwa.notificationBigTextStyle =
                new NotificationCompat.BigTextStyle(gwa.notificationBuilder);
        gwa.notificationBigTextStyle
                .setBigContentTitle(gwa.notificationLastShowedTitle)
                .bigText(gwa.notificationLastShowedMessage);

        if (Build.VERSION.SDK_INT>=26) {
            NotificationChannel channel = new NotificationChannel(
                    "SMBSync2",
                    "SMBSync2",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.enableLights(false);
            channel.setSound(null,null);
//            channel.setLightColor(Color.GREEN);
            channel.enableVibration(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            gwa.notificationManager.deleteNotificationChannel("SMBSync2");
            gwa.notificationManager.createNotificationChannel(channel);

        }

    }

    static final public void setNotificationIcon(GlobalParameters gwa,
                                                 int small_icon, int large_icon) {
        gwa.notificationSmallIcon = small_icon;
        if (gwa.notificationLargeIcon != null) gwa.notificationLargeIcon.recycle();
        gwa.notificationLargeIcon = BitmapFactory.decodeResource(gwa.appContext.getResources(), large_icon);
        gwa.notificationBuilder.setContentIntent(gwa.notificationPendingIntent)
                .setSmallIcon(gwa.notificationSmallIcon)//smbsync_animation)
                .setLargeIcon(gwa.notificationLargeIcon);
        ;
        if (Build.VERSION.SDK_INT>=26) {
            gwa.notificationBuilder.setChannelId("SMBSync2");
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

    final static public void setNotificationMessage(GlobalParameters gwa, long when,
                                                    String prof, String fp, String msg) {
        if (prof.equals("")) gwa.notificationLastShowedTitle = gwa.notificationAppName;
        else gwa.notificationLastShowedTitle = prof;//gwa.notificationAppName+"       "+prof;

        if (fp.equals("")) gwa.notificationLastShowedMessage = msg;
        else gwa.notificationLastShowedMessage = fp.concat("\n").concat(msg);

        gwa.notificationLastShowedWhen = when;
    }

    final static public Notification showOngoingMsg(GlobalParameters gwa, long when,
                                                    String msg) {
        return showOngoingMsg(gwa, when, "", "", msg);
    }

    final static public Notification showOngoingMsg(GlobalParameters gwa, long when,
                                                    String prof, String msg) {
        return showOngoingMsg(gwa, when, prof, "", msg);
    }

    final static public Notification showOngoingMsg(GlobalParameters gwa, long when,
                                                    String prof, String fp, String msg) {
        setNotificationMessage(gwa, when, prof, fp, msg);
        gwa.notificationBuilder
                .setContentTitle(gwa.notificationLastShowedTitle)
                .setContentText(gwa.notificationLastShowedMessage)
                .setSmallIcon(gwa.notificationSmallIcon)//smbsync_animation)
                .setLargeIcon(gwa.notificationLargeIcon)
        ;
        if (when != 0) gwa.notificationBuilder.setWhen(when);
        if (Build.VERSION.SDK_INT>=26) {
            gwa.notificationBuilder.setChannelId("SMBSync2");
        }
        gwa.notificationBigTextStyle
                .setBigContentTitle(gwa.notificationLastShowedTitle)
                .bigText(gwa.notificationLastShowedMessage);
        gwa.notification = gwa.notificationBigTextStyle.build();
        if (isNotificationEnabled(gwa))
            gwa.notificationManager.notify(R.string.app_name, gwa.notification);

        return gwa.notification;
    }

    final static public Notification reShowOngoingMsg(GlobalParameters gwa) {
        gwa.notificationBuilder
                .setContentTitle(gwa.notificationLastShowedTitle)
                .setContentText(gwa.notificationLastShowedMessage)
                .setSmallIcon(gwa.notificationSmallIcon)//smbsync_animation)
                .setLargeIcon(gwa.notificationLargeIcon)
        ;
        if (Build.VERSION.SDK_INT>=26) {
            gwa.notificationBuilder.setChannelId("SMBSync2");
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

    final static public void showNoticeMsg(Context context, GlobalParameters gwa, String msg) {
        clearNotification(gwa);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder
//			.setTicker(gwa.notificationAppName)
                .setOngoing(false)
                .setAutoCancel(true)
                .setSmallIcon(gwa.notificationSmallIcon)//smbsync_animation)
                .setLargeIcon(gwa.notificationLargeIcon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(msg)
                .setWhen(System.currentTimeMillis())
//			.addAction(action_icon, action_title, action_pi)
        ;
        if (Build.VERSION.SDK_INT>=26) {
            gwa.notificationBuilder.setChannelId("SMBSync2");
        }
        if (gwa.callbackStub != null || (gwa.msgList != null && gwa.msgList.size() > 0)) {
            Intent activity_intent = new Intent(gwa.appContext, ActivityMain.class);
            PendingIntent activity_pi = PendingIntent.getActivity(context, 0, activity_intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(activity_pi);
        } else {
            if (!LogUtil.getLogFilePath(gwa).equals("") && gwa.settingLogOption) {
                File lf = new File(LogUtil.getLogFilePath(gwa));
                if (lf.exists()) {
                    Intent br_log_intent = new Intent(android.content.Intent.ACTION_VIEW);
                    br_log_intent.setDataAndType(Uri.parse("file://" + LogUtil.getLogFilePath(gwa)), "text/plain");
                    PendingIntent br_log_pi = PendingIntent.getActivity(context, 0, br_log_intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(br_log_pi);
                } else {
                    Intent dummy_intent = new Intent(context, ActivityMain.class);
                    PendingIntent dummy_pi = PendingIntent.getActivity(context, 0, dummy_intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    dummy_pi.cancel();
                    builder.setContentIntent(dummy_pi);
                }
            }
        }
        if (isNotificationEnabled(gwa))
            gwa.notificationManager.notify(R.string.app_name, builder.build());
    }

    final static public void clearNotification(GlobalParameters gwa) {
        gwa.notificationManager.cancelAll();
    }

}
