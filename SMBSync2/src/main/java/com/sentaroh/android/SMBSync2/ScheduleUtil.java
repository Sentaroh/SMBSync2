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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sentaroh.android.Utilities.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_DAY_OF_THE_WEEK_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_ENABLED_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_HOURS_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_INTERVAL_FIRST_RUN_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_LAST_EXEC_TIME_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_MINUTES_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_SAVED_DATA_V2;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_SAVED_DATA_V3;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_TYPE_DAY_OF_THE_WEEK;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_TYPE_EVERY_DAY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_TYPE_EVERY_HOURS;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_TYPE_EVERY_MONTH;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_TYPE_INTERVAL;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_TYPE_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SYNC_DELAYED_TIME_FOR_WIFI_ON_DEFAULT_VALUE;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SYNC_DELAYED_TIME_FOR_WIFI_ON_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SYNC_PROFILE_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SYNC_WIFI_OFF_AFTER_SYNC_END_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SYNC_WIFI_ON_BEFORE_SYNC_START_KEY;

public class ScheduleUtil {

    final static public ArrayList<ScheduleItem> loadScheduleData(GlobalParameters gp) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(gp.appContext);
        ArrayList<ScheduleItem> sl = new ArrayList<ScheduleItem>();
        ScheduleItem sp = new ScheduleItem();
        String v2_data = prefs.getString(SCHEDULER_SCHEDULE_SAVED_DATA_V2, "-1");
        String v3_data = prefs.getString(SCHEDULER_SCHEDULE_SAVED_DATA_V3, "-1");
//    	Log.v("","data="+v2_data);
        if (!v3_data.equals("-1")) {
            sl = buildScheduleListV3(gp, v3_data);
        } else if (!v2_data.equals("-1")) {
            sl = buildScheduleListV2(gp, v2_data);
            saveScheduleData(gp, sl);
        } else {
            if (!prefs.getString(SCHEDULER_SCHEDULE_HOURS_KEY, "-1").equals("-1")) {
                sp.scheduleName = "NO NAME";
                sp.scheduleEnabled = prefs.getBoolean(SCHEDULER_SCHEDULE_ENABLED_KEY, false);
                sp.scheduleIntervalFirstRunImmed = prefs.getBoolean(SCHEDULER_SCHEDULE_INTERVAL_FIRST_RUN_KEY, false);
                sp.scheduleType = prefs.getString(SCHEDULER_SCHEDULE_TYPE_KEY, SCHEDULER_SCHEDULE_TYPE_EVERY_DAY);
                sp.scheduleHours = prefs.getString(SCHEDULER_SCHEDULE_HOURS_KEY, "00");
                sp.scheduleMinutes = prefs.getString(SCHEDULER_SCHEDULE_MINUTES_KEY, "00");
                sp.scheduleDayOfTheWeek = prefs.getString(SCHEDULER_SCHEDULE_DAY_OF_THE_WEEK_KEY, "0000000");

                sp.scheduleLastExecTime = prefs.getLong(SCHEDULER_SCHEDULE_LAST_EXEC_TIME_KEY, -1);
                if (sp.scheduleLastExecTime == 0)
                    sp.scheduleLastExecTime = System.currentTimeMillis();

                sp.syncTaskList = prefs.getString(SCHEDULER_SYNC_PROFILE_KEY, "");

                sp.syncWifiOnBeforeStart = prefs.getBoolean(SCHEDULER_SYNC_WIFI_ON_BEFORE_SYNC_START_KEY, false);
                sp.syncWifiOffAfterEnd = prefs.getBoolean(SCHEDULER_SYNC_WIFI_OFF_AFTER_SYNC_END_KEY, false);

                sp.syncDelayAfterWifiOn = Integer.parseInt(
                        prefs.getString(SCHEDULER_SYNC_DELAYED_TIME_FOR_WIFI_ON_KEY,
                                SCHEDULER_SYNC_DELAYED_TIME_FOR_WIFI_ON_DEFAULT_VALUE));
                sl.add(sp);
                saveScheduleData(gp, sl);
            } else {
                //empty
            }
        }
        return sl;
    }

    final static public ArrayList<ScheduleItem> buildScheduleListV2(GlobalParameters gp, String v2_data) {
        ArrayList<ScheduleItem> sl = new ArrayList<ScheduleItem>();
        String[] sd_array = v2_data.split("\n");
        int nc=0;
        for (String sd_sub : sd_array) {
//            Log.v("","sub="+sd_sub);
            if (sd_sub.equals("end")) break;
            String[] sub_array = sd_sub.split("\t");
//            Log.v("","array="+sub_array.length);
            if (sub_array.length >= 14) {
                for (String item : sub_array) item = item.replace("\u0000", "");
                ScheduleItem si = new ScheduleItem();
                si.scheduleEnabled = sub_array[0].replace("\u0000", "").equals("1") ? true : false;
                si.scheduleName = sub_array[1].replace("\u0000", "");
                nc++;
                if (si.scheduleName==null || si.scheduleName.equals("")) si.scheduleName="NO NAME"+nc;
                if (sub_array[2].length() > 0)
                    si.schedulePosition = Integer.valueOf(sub_array[2].replace("\u0000", ""));
                si.scheduleType = sub_array[3].replace("\u0000", "");
                si.scheduleHours = sub_array[4].replace("\u0000", "");
                si.scheduleMinutes = sub_array[5].replace("\u0000", "");
                si.scheduleDayOfTheWeek = sub_array[6].replace("\u0000", "");
                si.scheduleIntervalFirstRunImmed = sub_array[7].replace("\u0000", "").equals("1") ? true : false;
                if (sub_array[8].length() > 0)
                    si.scheduleLastExecTime = Long.valueOf(sub_array[8].replace("\u0000", ""));
                si.syncTaskList = sub_array[9].replace("\u0000", "");
                si.syncGroupList = sub_array[10].replace("\u0000", "");
                si.syncWifiOnBeforeStart = sub_array[11].replace("\u0000", "").equals("1") ? true : false;
                si.syncWifiOffAfterEnd = sub_array[12].replace("\u0000", "").equals("1") ? true : false;
                if (sub_array[13].length() > 0)
                    si.syncDelayAfterWifiOn = Integer.valueOf(sub_array[13].replace("\u0000", ""));

                if (si.scheduleLastExecTime == 0)
                    si.scheduleLastExecTime = System.currentTimeMillis();

                sl.add(si);
//                Log.v("","load="+si.scheduleName);
            }
        }
//        if (sl.size()==0) {
//            ScheduleItem si=new ScheduleItem();
//            sl.add(si);
//        }
        return sl;
    }


    final static public ArrayList<ScheduleItem> buildScheduleListV3(GlobalParameters gp, String v3_data) {
        ArrayList<ScheduleItem> sl = new ArrayList<ScheduleItem>();
        String[] sd_array = v3_data.split("\u0001");
        int nc=0;
        for (String sd_sub : sd_array) {
//            Log.v("","sub="+sd_sub);
            if (sd_sub.equals("end")) break;
            String[] sub_array = sd_sub.split("\u0002");
//            Log.v("","array="+sub_array.length);
            if (sub_array.length >= 14) {
                for (String item : sub_array) item = item.replace("\u0000", "");
                ScheduleItem si = new ScheduleItem();
                si.scheduleEnabled = sub_array[0].replace("\u0000", "").equals("1") ? true : false;
                si.scheduleName = sub_array[1].replace("\u0000", "");
                nc++;
                if (si.scheduleName==null || si.scheduleName.equals("")) si.scheduleName="NO NAME"+nc;

                if (sub_array[2].length() > 0)
                    si.schedulePosition = Integer.valueOf(sub_array[2].replace("\u0000", ""));
                si.scheduleType = sub_array[3].replace("\u0000", "");
                si.scheduleHours = sub_array[4].replace("\u0000", "");
                si.scheduleMinutes = sub_array[5].replace("\u0000", "");
                si.scheduleDayOfTheWeek = sub_array[6].replace("\u0000", "");
                si.scheduleIntervalFirstRunImmed = sub_array[7].replace("\u0000", "").equals("1") ? true : false;
                if (sub_array[8].length() > 0)
                    si.scheduleLastExecTime = Long.valueOf(sub_array[8].replace("\u0000", ""));
                si.syncTaskList = sub_array[9].replace("\u0000", "");
                si.syncGroupList = sub_array[10].replace("\u0000", "");
                si.syncWifiOnBeforeStart = sub_array[11].replace("\u0000", "").equals("1") ? true : false;
                si.syncWifiOffAfterEnd = sub_array[12].replace("\u0000", "").equals("1") ? true : false;
                if (sub_array[13].length() > 0)
                    si.syncDelayAfterWifiOn = Integer.valueOf(sub_array[13].replace("\u0000", ""));

                if (sub_array.length >= 15 && sub_array[14]!=null && sub_array[14].length() > 0)
                    si.scheduleDay = sub_array[14].replace("\u0000", "");

                if (si.scheduleLastExecTime == 0)
                    si.scheduleLastExecTime = System.currentTimeMillis();

                sl.add(si);
//                Log.v("","load="+si.scheduleName);
            }
        }
//        if (sl.size()==0) {
//            ScheduleItem si=new ScheduleItem();
//            sl.add(si);
//        }
        return sl;
    }

    final static public ScheduleItem copyScheduleData(GlobalParameters gp, ScheduleItem sp) {
        ScheduleItem n_sp = new ScheduleItem();
        n_sp.scheduleDayOfTheWeek = sp.scheduleDayOfTheWeek;
        n_sp.scheduleDay = sp.scheduleDay;
        n_sp.scheduleHours = sp.scheduleHours;
        n_sp.scheduleIntervalFirstRunImmed = sp.scheduleIntervalFirstRunImmed;
        n_sp.scheduleLastExecTime = sp.scheduleLastExecTime;
        n_sp.scheduleMinutes = sp.scheduleMinutes;
        n_sp.scheduleType = sp.scheduleType;
        n_sp.syncDelayAfterWifiOn = sp.syncDelayAfterWifiOn;
        n_sp.syncTaskList = sp.syncTaskList;
        n_sp.syncWifiOffAfterEnd = sp.syncWifiOffAfterEnd;
        n_sp.syncWifiOnBeforeStart = sp.syncWifiOnBeforeStart;
        return n_sp;
    }

    final static public void saveScheduleData(GlobalParameters gp, ArrayList<ScheduleItem> sl) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(gp.appContext);
        String data = "";
        for (ScheduleItem si : sl) {
//                Log.v("","name="+si.scheduleName);
            data += (si.scheduleEnabled ? "1" : "0") + "\u0000" + "\u0002";
            data += si.scheduleName + "\u0000" + "\u0002";
            data += String.valueOf(si.schedulePosition) + "\u0002";
            data += si.scheduleType + "\u0000" + "\u0002";
            data += si.scheduleHours + "\u0000" + "\u0002";
            data += si.scheduleMinutes + "\u0000" + "\u0002";
            data += si.scheduleDayOfTheWeek + "\u0000" + "\u0002";
            data += (si.scheduleIntervalFirstRunImmed ? "1" : "0") + "\u0000" + "\u0002";
            data += String.valueOf(si.scheduleLastExecTime) + "\u0002";
            data += si.syncTaskList + "\u0000" + "\u0002";
            data += si.syncGroupList + "\u0000" + "\u0002";
            data += (si.syncWifiOnBeforeStart ? "1" : "0") + "\u0000" + "\u0002";
            data += (si.syncWifiOffAfterEnd ? "1" : "0") + "\u0000" + "\u0002";
            data += String.valueOf(si.syncDelayAfterWifiOn) + "\u0002";
            data += si.scheduleDay + "\u0000" + "\u0002";
            data += "\u0001";

        }
        data += "end";
        prefs.edit().putString(SCHEDULER_SCHEDULE_SAVED_DATA_V3, data).commit();

    }

    final static public long getNextSchedule(ScheduleItem sp) {
        return getNextSchedule(sp, 0L);
    }

    final static private long getNextSchedule(ScheduleItem sp, long offset) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis()+offset);
        long result = 0;
        int s_day = Integer.parseInt(sp.scheduleDay);
        int s_hrs = Integer.parseInt(sp.scheduleHours);
        int s_min = Integer.parseInt(sp.scheduleMinutes);
        int c_year = cal.get(Calendar.YEAR);
        int c_month = cal.get(Calendar.MONTH);
        int c_day = cal.get(Calendar.DAY_OF_MONTH);
        int c_dw = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int c_hr = cal.get(Calendar.HOUR_OF_DAY);
        int c_mm = cal.get(Calendar.MINUTE);
        if (sp.scheduleType.equals(SCHEDULER_SCHEDULE_TYPE_EVERY_HOURS)) {
            if (c_mm >= s_min) {
                cal.set(c_year, c_month, c_day, c_hr, 0, 0);
                result = cal.getTimeInMillis() + (60 * 1000 * 60) + (60 * 1000 * s_min);
            } else {
                cal.set(c_year, c_month, c_day, c_hr, 0, 0);
                result = cal.getTimeInMillis() + (60 * 1000 * s_min);
            }
//    		cal.set(c_year, c_month, c_day, c_hr, c_mm, 0);
//    		result=cal.getTimeInMillis()+(60*1000);
        } else if (sp.scheduleType.equals(SCHEDULER_SCHEDULE_TYPE_EVERY_DAY)) {
            cal.clear();
            cal.set(c_year, c_month, c_day, s_hrs, 0, 0);
            if ((c_hr * 100 + c_mm) >= (s_hrs * 100 + s_min))
                result = cal.getTimeInMillis() + (60 * 1000 * 60 * 24) + (60 * 1000 * s_min);
            else result = cal.getTimeInMillis() + (60 * 1000 * s_min);
        } else if (sp.scheduleType.equals(SCHEDULER_SCHEDULE_TYPE_EVERY_MONTH)) {
            cal.clear();
            cal.set(c_year, c_month, s_day, s_hrs, s_min, 0);
            String curr=StringUtil.convDateTimeTo_YearMonthDayHourMinSec((System.currentTimeMillis()+59999));
            String cald=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(cal.getTimeInMillis());
            if ((System.currentTimeMillis()+59999)>=cal.getTimeInMillis()) {
                cal.add(Calendar.MONTH, 1);
            }
            result = cal.getTimeInMillis();
            Log.v("ScheduleNextTime","name="+sp.scheduleName+", c_year="+c_year+", c_month="+c_month+
                    ", s_day="+s_day+", s_hrs="+s_hrs+", s_min="+s_min+", result="+StringUtil.convDateTimeTo_YearMonthDayHourMinSec(result));
        } else if (sp.scheduleType.equals(SCHEDULER_SCHEDULE_TYPE_INTERVAL)) {
//    		cal.clear();
//    		cal.setTimeInMillis(sp.scheduleLastExecTime+s_min*(60*1000));
//    		c_year=cal.get(Calendar.YEAR);
//    		c_month=cal.get(Calendar.MONTH);
//    		c_day=cal.get(Calendar.DAY_OF_MONTH);
//    		c_hr=cal.get(Calendar.HOUR_OF_DAY);
//    		c_mm=cal.get(Calendar.MINUTE);
//    		int c_ss=cal.get(Calendar.SECOND);
//    		cal.set(c_year, c_month, c_day, c_hr, c_mm, 0);
//    		if (c_ss==0) result=cal.getTimeInMillis();
//    		else result=cal.getTimeInMillis()+60*1000;
            if (sp.scheduleLastExecTime == 0) {
                if (!sp.scheduleIntervalFirstRunImmed) {
                    sp.scheduleLastExecTime = System.currentTimeMillis();
                    long nt = sp.scheduleLastExecTime;
                    if ((sp.scheduleLastExecTime % (60 * 1000)) > 0)
                        nt = (sp.scheduleLastExecTime / (60 * 1000)) * (60 * 1000);
                    result = nt + s_min * (60 * 1000);
                } else {
                    sp.scheduleLastExecTime = System.currentTimeMillis();
                    long nt = sp.scheduleLastExecTime;
                    if ((sp.scheduleLastExecTime % (60 * 1000)) > 0)
                        nt = (sp.scheduleLastExecTime / (60 * 1000)) * (60 * 1000) + (60 * 1000);
                    else nt += 60 * 1000;
                    result = nt;
                }
            } else {
                long nt = sp.scheduleLastExecTime;
                long m_nt=0l;
                if ((sp.scheduleLastExecTime % (60 * 1000)) > 0){
                    m_nt = (sp.scheduleLastExecTime / (60 * 1000)) * (60 * 1000);
                    result = m_nt + s_min * (60 * 1000);
                } else {
                    result = nt + s_min * (60 * 1000);
                }

                Log.v("ScheduleNextTime","name="+sp.scheduleName+", m_nt="+m_nt+", nt="+nt+", s_min="+s_min+", result="+StringUtil.convDateTimeTo_YearMonthDayHourMinSec(result));
            }
//    		Log.v("","last="+StringUtil.convDateTimeTo_YearMonthDayHourMinSec(sp.scheduleLastExecTime));
//    		Log.v("","result="+StringUtil.convDateTimeTo_YearMonthDayHourMinSec(result));
//    		Log.v("","last="+StringUtil.convDateTimeTo_YearMonthDayHourMinSec(sp.scheduleLastExecTime));
//    		Log.v("","c_year="+c_year+", c_month="+c_month+", c_day="+c_day+", c_hr="+c_hr+", c_mm="+c_mm+", c_ss="+c_ss);
//    		Log.v("","new="+StringUtil.convDateTimeTo_YearMonthDayHourMinSec(result));
        } else if (sp.scheduleType.equals(SCHEDULER_SCHEDULE_TYPE_DAY_OF_THE_WEEK)) {
            boolean[] dwa = new boolean[]{false, false, false, false, false, false, false};
            for (int i = 0; i < sp.scheduleDayOfTheWeek.length(); i++) {
                String dw_s = sp.scheduleDayOfTheWeek.substring(i, i + 1);
                if (dw_s.equals("1")) dwa[i] = true;
//    			Log.v("","i="+i+", de_s="+dw_s+", dwa="+dwa[i]);
            }
            int s_hhmm = Integer.parseInt(sp.scheduleHours) * 100 + s_min;
            int c_hhmm = c_hr * 100 + c_mm;
//        	Log.v("","c_hhmm="+c_hhmm+", s_hhmm="+s_hhmm+", c_dw="+c_dw);
            int s_dw = 0;
            if (c_hhmm >= s_hhmm) {
                if (c_dw == 6) {
                    c_dw = 0;
                    s_dw = 1;
                    for (int i = c_dw; i < 7; i++) {
                        if (dwa[i]) {
                            break;
                        }
                        s_dw++;
                    }
//        			Log.v("","c1 s_dw="+s_dw);
                } else {
                    c_dw++;
                    s_dw = 1;
                    boolean found = false;
                    for (int i = c_dw; i < 7; i++) {
                        if (dwa[i]) {
//        					Log.v("","c2 s_dw="+s_dw+", i="+i);
                            found = true;
                            break;
                        }
                        s_dw++;
                    }
                    if (!found) {
                        for (int i = 0; i < c_dw; i++) {
                            if (dwa[i]) {
//            					Log.v("","c3 s_dw="+s_dw+", i="+i);
                                found = true;
                                break;
                            }
                            s_dw++;
                        }
                    }
                }
            } else {
                s_dw = 0;
                boolean found = false;
//				Log.v("","c_dw="+c_dw);
                for (int i = c_dw; i < 7; i++) {
                    if (dwa[i]) {
//    					Log.v("","c4 s_dw="+s_dw);
                        found = true;
                        break;
                    }
                    s_dw++;
                }
                if (!found) {
                    for (int i = 0; i < c_dw; i++) {
                        if (dwa[i]) {
//        					Log.v("","c5 s_dw="+s_dw);
                            found = true;
                            break;
                        }
                        s_dw++;
                    }
                }
            }
//    		Log.v("","s_dw="+s_dw);
            cal.clear();
            cal.set(c_year, c_month, c_day, s_hrs, 0, 0);
            result = cal.getTimeInMillis() + s_dw * (60 * 1000 * 60 * 24) + (60 * 1000 * s_min);
        }
//		result=System.currentTimeMillis()+(1000*60*5);//SchedulerReceiverも修正（SCHEDULER_INTENT_SET_TIMER_IF_NOT_SET)
        return result;
    }

    public static ScheduleItem getScheduleInformation(ArrayList<ScheduleItem> sl, String name) {
        for (ScheduleItem si : sl) {
            if (si.scheduleName.equals(name)) {
                return si;
            }
        }
        return null;
    }

    public static void sendTimerRequest(Context c, String act) {
        Intent intent = new Intent(act);
        intent.setClass(c, SyncReceiver.class);
        c.sendBroadcast(intent);
    }

    ;

    public static boolean isScheduleExists(ArrayList<ScheduleItem> sl, String name) {
        boolean result = false;
        for (ScheduleItem si : sl) {
            if (si.scheduleName.equals(name)) result = true;
        }
        return result;
    }

    public static void setSchedulerInfo(GlobalParameters gp) {
//        gp.scheduleInfoList =loadScheduleData(gp);
        ArrayList<ScheduleItem> sl = loadScheduleData(gp);
        String sched_list="", sep="", first="";
        long latest_sched_time = -1;
        ArrayList<String>sched_array=new ArrayList<String>();
        for (ScheduleItem si : sl) {
            if (si.scheduleEnabled) {
                long time = ScheduleUtil.getNextSchedule(si);
                String dt=StringUtil.convDateTimeTo_YearMonthDayHourMin(time);
                String item=dt+","+si.scheduleName;
                sched_array.add(item);
            }
        }
        Collections.sort(sched_array);

        if (sched_array.size()>0) {
            String[] key=sched_array.get(0).split(",");
            for(String item:sched_array) {
                String[] s_key=item.split(",");
                if (key[0].equals(s_key[0])) {
                    sched_list+=sep+s_key[1];
                    sep=",";
                }
            }
            String sched_info = String.format(gp.appContext.getString(R.string.msgs_scheduler_info_next_schedule_main_info),
                    key[0], sched_list);
            gp.scheduleInfoText = sched_info;
            gp.scheduleInfoView.setText(gp.scheduleInfoText);
        } else {
            gp.scheduleInfoText = gp.appContext.getString(R.string.msgs_scheduler_info_schedule_disabled);
            gp.scheduleInfoView.setText(gp.scheduleInfoText);
        }
    }

    public static String buildSchedulerNextInfo(Context c, ScheduleItem sp) {
        long nst = -1;
        nst = getNextSchedule(sp);
        String sched_time = "", result = "";
        if (nst != -1) {
            sched_time = StringUtil.convDateTimeTo_YearMonthDayHourMin(nst);
            if (sp.scheduleEnabled)
                result = c.getString(R.string.msgs_scheduler_info_schedule_enabled) + ", " + String.format(c.getString(R.string.msgs_scheduler_info_next_schedule_time), sched_time);
            else result = c.getString(R.string.msgs_scheduler_info_schedule_disabled);
        } else {
            result = c.getString(R.string.msgs_scheduler_info_schedule_disabled);
        }
        return result;
    }

    ;

}

