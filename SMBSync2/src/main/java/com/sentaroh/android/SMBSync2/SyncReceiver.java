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

import static com.sentaroh.android.SMBSync2.ScheduleConstants.*;
import static com.sentaroh.android.SMBSync2.Constants.*;

import com.sentaroh.android.SMBSync2.Log.LogUtil;
import com.sentaroh.android.Utilities.StringUtil;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public class SyncReceiver extends BroadcastReceiver {

    private static Context mContext = null;

    private static GlobalParameters mGp = null;

    private static LogUtil mLog = null;

    private static ArrayList<ScheduleItem> mSchedList = null;

    @SuppressLint("Wakelock")
    @Override
    final public void onReceive(Context c, Intent received_intent) {
        WakeLock wl =
                ((PowerManager) c.getSystemService(Context.POWER_SERVICE))
                        .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                                | PowerManager.ON_AFTER_RELEASE, "Receiver");
        wl.acquire(1000);
        mContext = c;
        if (mGp == null) {
            mGp = new GlobalParameters();
            mGp.appContext = c;
        }
        mGp.loadSettingsParms();
        mGp.setLogParms(mGp);

        if (mLog == null) mLog = new LogUtil(c, "Receiver", mGp);

        loadScheduleData();

        String action = received_intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                    action.equals(Intent.ACTION_DATE_CHANGED) ||
                    action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                    action.equals(Intent.ACTION_TIME_CHANGED) ||
                    action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
                if (mGp.settingDebugLevel > 0)
                    mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                for (ScheduleItem si : mSchedList)
                    si.scheduleLastExecTime = System.currentTimeMillis();
                ScheduleUtil.saveScheduleData(mGp, mSchedList);
                setTimer();
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED) ||
                    action.equals(Intent.ACTION_MEDIA_EJECT) ||
                    action.equals(Intent.ACTION_MEDIA_REMOVED) ||
                    action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                if (mGp.settingDebugLevel > 0)
                    mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                Intent in = new Intent(mContext, SyncService.class);
                in.setAction(action);
                if (received_intent.getExtras() != null) in.putExtras(received_intent.getExtras());
                mContext.startService(in);
            } else if (action.equals(SCHEDULER_INTENT_SET_TIMER)) {
                if (mGp.settingDebugLevel > 0)
                    mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                setTimer();
            } else if (action.equals(SCHEDULER_INTENT_SET_TIMER_IF_NOT_SET)) {
                if (mGp.settingDebugLevel > 0)
                    mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                if (!isTimerScheduled()) setTimer();
            } else if (action.equals(SCHEDULER_INTENT_TIMER_EXPIRED)) {
                if (mGp.settingDebugLevel > 0)
                    mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                if (received_intent.getExtras().containsKey(SCHEDULER_SCHEDULE_NAME_KEY)) {
                    Intent send_intent = new Intent(mContext, SyncService.class);
                    send_intent.setAction(SCHEDULER_INTENT_TIMER_EXPIRED);
                    send_intent.putExtra(SCHEDULER_SCHEDULE_NAME_KEY, received_intent.getStringExtra(SCHEDULER_SCHEDULE_NAME_KEY));
                    mContext.startService(send_intent);
                    String[] schedule_list=received_intent.getStringExtra(SCHEDULER_SCHEDULE_NAME_KEY).split(",");
                    for (String sched_name:schedule_list) {
                        if (ScheduleUtil.getScheduleInformation(mSchedList, sched_name) != null) {
                            ScheduleUtil.getScheduleInformation(mSchedList, sched_name).scheduleLastExecTime = System.currentTimeMillis();
                        }
                    }
                    ScheduleUtil.saveScheduleData(mGp, mSchedList);
                    setTimer();
                }
            } else if (action.equals(SMBSYNC2_START_SYNC_INTENT)) {
                if (mGp.settingDebugLevel > 0)
                    mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                Intent in = new Intent(mContext, SyncService.class);
                in.setAction(SMBSYNC2_START_SYNC_INTENT);
                if (received_intent.getExtras() != null) in.putExtras(received_intent.getExtras());
                mContext.startService(in);
            } else if (action.equals(SMBSYNC2_AUTO_SYNC_INTENT)) {
                if (mGp.settingDebugLevel > 0)
                    mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                Intent in = new Intent(mContext, SyncService.class);
                in.setAction(SMBSYNC2_AUTO_SYNC_INTENT);
                if (received_intent.getExtras() != null) in.putExtras(received_intent.getExtras());
                mContext.startService(in);
            } else if (action.equals(SMBSYNC2_SERVICE_HEART_BEAT)) {
                if (mGp.settingDebugLevel >= 2)
                    mLog.addDebugMsg(3, "I", "Receiver action=" + action);
                Intent in = new Intent(mContext, SyncService.class);
                in.setAction(SMBSYNC2_SERVICE_HEART_BEAT);
                if (received_intent.getExtras() != null) in.putExtras(received_intent.getExtras());
                mContext.startService(in);
//            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED) ||
//                    action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
//                if (mGp.settingDebugLevel >= 1)
//                    mLog.addDebugMsg(1, "I", "Receiver action=" + action);
//                Intent in = new Intent(mContext, SyncService.class);
//                in.setAction(action);
//                if (received_intent.getExtras() != null) in.putExtras(received_intent.getExtras());
//                mContext.startService(in);
            } else {
                if (mGp.settingDebugLevel >= 1)
                    mLog.addDebugMsg(1, "I", "Receiver action=" + action);
            }
        }
    }

    static private void loadScheduleData() {
        mSchedList = ScheduleUtil.loadScheduleData(mGp);
        if (mGp.settingDebugLevel >= 2)
            for (ScheduleItem si : mSchedList) {
                mLog.addDebugMsg(2, "I", "loadScheduleData " +
                        "enabled=" + si.scheduleEnabled +
                        ", name=" + si.scheduleName +
                        ", type=" + si.scheduleType +
                        ", sync=" + si.syncTaskList +
                        ", group=" + si.syncGroupList +
                        ", hours=" + si.scheduleHours +
                        ", minutes=" + si.scheduleMinutes +
                        ", dw=" + si.scheduleDayOfTheWeek +
                        ", Wifi On=" + si.syncWifiOnBeforeStart +
                        ", Wifi Off=" + si.syncWifiOffAfterEnd +
                        ", Wifi On dlayed=" + si.syncDelayAfterWifiOn
                );
            }
    }

    @SuppressLint("NewApi")
    static private void setTimer() {
        if (mGp.settingDebugLevel > 0) mLog.addDebugMsg(1, "I", "setTimer entered");
        cancelTimer();
        boolean scheduleEnabled = false;
        for (ScheduleItem si : mSchedList) if (si.scheduleEnabled) scheduleEnabled = true;
        if (scheduleEnabled) {
            ArrayList<ScheduleItem> begin_sched_list = new ArrayList<ScheduleItem>();
            ArrayList<String> sched_list=new ArrayList<String>();
            for (ScheduleItem si : mSchedList) {
                if (si.scheduleEnabled) {
                    long time = ScheduleUtil.getNextSchedule(si);
                    String item=StringUtil.convDateTimeTo_YearMonthDayHourMin(time)+","+si.scheduleName;
                    sched_list.add(item);
                    mLog.addDebugMsg(1,"I", "setTimer Schedule item added. item="+item);
//                    if (sched_time == -1) {
//                        sched_time = time;
//                        begin_sched_list.add(si);
//                    } else if (time < sched_time) {
//                        sched_time = time;
//                        begin_sched_list.add(si);
//                    } else if (time == sched_time) {
//                        begin_sched_list.add(si);
//                    }
                }
            }
            if (sched_list.size()>0) {
                Collections.sort(sched_list);
                String sched_time="";

                for(String item:sched_list) {
                    String[]sa=item.split(",");
                    if (sched_time.equals("")) {
                        sched_time=sa[0];
                        ScheduleItem si=ScheduleUtil.getScheduleInformation(mSchedList, sa[1]);
                        if (si!=null) {
                            begin_sched_list.add(si);
                            mLog.addDebugMsg(1,"I", "setTimer NextSchedule added. Name="+si.scheduleName+", "+sa[0]);
                        } else {
                            mLog.addLogMsg("E", "setTimer Schedule can not be found. Name="+sa[1]);
                        }
                    } else if (sched_time.equals(sa[0])) {
                        ScheduleItem si=ScheduleUtil.getScheduleInformation(mSchedList, sa[1]);
                        if (si!=null) {
                            begin_sched_list.add(si);
                            mLog.addDebugMsg(1,"I", "setTimer NextSchedule added. Name="+si.scheduleName+", "+sa[0]);
                        } else {
                            mLog.addLogMsg("E", "setTimer Schedule can not be found. Name="+sa[1]);
                        }
                    }
                }
            }
            if (begin_sched_list.size() > 0) {
                String sched_names = "", sep="";
                for (ScheduleItem si : begin_sched_list) {
                    sched_names += sep + si.scheduleName;
                    sep=",";
                }

                long time = ScheduleUtil.getNextSchedule(begin_sched_list.get(0));
                mLog.addDebugMsg(1, "I", "setTimer result=" + StringUtil.convDateTimeTo_YearMonthDayHourMinSec(time) + ", name=(" + sched_names+")");
                Intent in = new Intent();
                in.setAction(SCHEDULER_INTENT_TIMER_EXPIRED);
                in.putExtra(SCHEDULER_SCHEDULE_NAME_KEY, sched_names);
                in.setClass(mContext, SyncReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= 23)
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
                else am.set(AlarmManager.RTC_WAKEUP, time, pi);
            }
        }
    }

    static private SyncTaskItem getSyncTask(String job_name) {
        for (SyncTaskItem sji : mGp.syncTaskList) {
            if (sji.getSyncTaskName().equals(job_name)) {
                return sji;
            }
        }
        return null;
    }

    private static boolean isTimerScheduled() {
        Intent iw = new Intent();
        iw.setAction(SCHEDULER_INTENT_TIMER_EXPIRED);
        iw.setClass(mContext, SyncReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, iw, PendingIntent.FLAG_NO_CREATE);
        if (pi == null) {
            return false;
        } else {
            return true;
        }
    }

    static private void cancelTimer() {
        if (mGp.settingDebugLevel > 0) mLog.addDebugMsg(1, "I", "cancelTimer entered");
        Intent in = new Intent();
        in.setClass(mContext, SyncReceiver.class);
        in.setAction(SCHEDULER_INTENT_TIMER_EXPIRED);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, in, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

//	private static PrintWriter log_writer=null;

//	final static public void putLogMsg(String cat, String log_msg) {
//		
//		String dt=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(System.currentTimeMillis());
//		String log_msg_dt="D "+cat+" "+dt.substring(0,10)+" "+dt.substring(11)+" "+log_id+log_msg.toString();
//
//		Intent in=new Intent(mContext,SMBSyncService.class);
//		in.setAction(SMBSYNC2_LOG_WRITE);
//		in.putExtra("LOG", log_msg_dt);
//		mContext.startService(in);
//	};
}
