package com.sentaroh.android.SMBSync2;

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

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.sentaroh.android.SMBSync2.Log.LogUtil;
import com.sentaroh.android.Utilities.MiscUtil;
import com.sentaroh.android.Utilities.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;

import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_INTENT_SET_TIMER;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_INTENT_SET_TIMER_IF_NOT_SET;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_INTENT_TIMER_EXPIRED;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_LAST_SCHEDULED_UTC_TIME_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_NAME_KEY;

public class SyncReceiver extends BroadcastReceiver {
    private static Logger slf4jLog = LoggerFactory.getLogger(SyncReceiver.class);

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
        try {wl.acquire(1000); } catch(Exception e) {};
        mContext = c;
        if (mGp == null) {
            mGp =new GlobalParameters();
            mGp.initGlobalParamter(c);
        }
        mGp.loadSettingsParms(c);
        mGp.setLogParms(c, mGp);

        if (mLog == null) mLog = new LogUtil(c, "Receiver", mGp);

        loadScheduleData(c);

        String action = received_intent.getAction();
        if (action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                    action.equals(Intent.ACTION_DATE_CHANGED) ||
                    action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                    action.equals(Intent.ACTION_TIME_CHANGED) ||
                    action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
                mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                for (ScheduleItem si : mSchedList) si.scheduleLastExecTime = System.currentTimeMillis();
                ScheduleUtil.saveScheduleData(c, mGp, mSchedList, true);//Use apply by shared preference edit
                setTimer();
            } else if (action.equals(SCHEDULER_INTENT_SET_TIMER)) {
                mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                setTimer();
            } else if (action.equals(SCHEDULER_INTENT_SET_TIMER_IF_NOT_SET)) {
                mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                if (!isTimerScheduled()) setTimer();
            } else if (action.equals(SCHEDULER_INTENT_TIMER_EXPIRED)) {
                mLog.addDebugMsg(1, "I", "Receiver action=" + action);
                if (received_intent.getExtras().containsKey(SCHEDULER_SCHEDULE_NAME_KEY)) {
                    Intent send_intent = new Intent(mContext, SyncService.class);
                    send_intent.setAction(SCHEDULER_INTENT_TIMER_EXPIRED);
                    send_intent.putExtra(SCHEDULER_SCHEDULE_NAME_KEY, received_intent.getStringExtra(SCHEDULER_SCHEDULE_NAME_KEY));
                    try {
                        if (Build.VERSION.SDK_INT>=26) {
                            mContext.startForegroundService(send_intent);
                        } else {
                            mContext.startService(send_intent);
                        }
                    } catch(Exception e) {
//                        e.printStackTrace();
                        mLog.addDebugMsg(1,"E", "startService filed, action="+action+", error=" + e.getMessage());
                        mLog.addDebugMsg(1,"E", MiscUtil.getStackTraceString(e));
                    }
                    String[] schedule_list=received_intent.getStringExtra(SCHEDULER_SCHEDULE_NAME_KEY).split(",");
                    for (String sched_name:schedule_list) {
                        if (ScheduleUtil.getScheduleInformation(mSchedList, sched_name) != null) {
                            ScheduleUtil.getScheduleInformation(mSchedList, sched_name).scheduleLastExecTime = System.currentTimeMillis();
                        }
                    }
                    ScheduleUtil.saveScheduleData(c, mGp, mSchedList);
                    setTimer();
                }
            } else {
                mLog.addDebugMsg(1, "I", "Receiver action=" + action);
            }
        }
    }

    static private void loadScheduleData(Context c) {
        mSchedList = ScheduleUtil.loadScheduleData(c, mGp);
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
        mLog.addDebugMsg(1, "I", "setTimer entered, settingScheduleSyncEnabled="+mGp.settingScheduleSyncEnabled);
        cancelTimer();
        boolean scheduleEnabled = false;
        for (ScheduleItem si : mSchedList) if (si.scheduleEnabled) scheduleEnabled = true;
        if (scheduleEnabled && mGp.settingScheduleSyncEnabled) {
            ArrayList<ScheduleItem> begin_sched_list = new ArrayList<ScheduleItem>();
            ArrayList<String> sched_list=new ArrayList<String>();
            for (ScheduleItem si : mSchedList) {
                if (si.scheduleEnabled) {
                    long time = ScheduleUtil.getNextSchedule(si);
                    String item=StringUtil.convDateTimeTo_YearMonthDayHourMin(time)+","+si.scheduleName;
                    sched_list.add(item);
                    mLog.addDebugMsg(1,"I", "setTimer Schedule item added. item="+item);
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
                            mLog.addDebugMsg(1,"E", "setTimer Schedule can not be found. Name="+sa[1]);
                        }
                    } else if (sched_time.equals(sa[0])) {
                        ScheduleItem si=ScheduleUtil.getScheduleInformation(mSchedList, sa[1]);
                        if (si!=null) {
                            begin_sched_list.add(si);
                            mLog.addDebugMsg(1,"I", "setTimer NextSchedule added. Name="+si.scheduleName+", "+sa[0]);
                        } else {
                            mLog.addDebugMsg(1,"E", "setTimer Schedule can not be found. Name="+sa[1]);
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
                long prev_set_time=ScheduleUtil.getSchedulerLastScheduleTime(mContext);
                mLog.addDebugMsg(1, "I", "setTimer result=" + StringUtil.convDateTimeTo_YearMonthDayHourMinSec(time) + ", name=(" + sched_names+")"+
                        ", Next schedule UTC=("+CommonUtilities.convertDateTimeWithTimzone("UTC", time)+"), TimeValue="+time+
                        ", Prev schedule UTC=("+CommonUtilities.convertDateTimeWithTimzone("UTC", prev_set_time)+"), TimeValue="+prev_set_time);
                ScheduleUtil.setSchedulerLastScheduleTime(mContext, time);
                Intent in = new Intent();
                in.setAction(SCHEDULER_INTENT_TIMER_EXPIRED);
                in.putExtra(SCHEDULER_SCHEDULE_NAME_KEY, sched_names);
                in.setClass(mContext, SyncReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                try {
                    if (Build.VERSION.SDK_INT >= 23) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi);
                    else am.set(AlarmManager.RTC_WAKEUP, time, pi);
                } catch(Exception e) {
                    String stm= MiscUtil.getStackTraceString(e);
                    mLog.addDebugMsg(1, "I", "setTimer failed. error="+e.getMessage()+"\n"+stm);
                }
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

}
