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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.sentaroh.android.Utilities.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import static com.sentaroh.android.SMBSync2.Constants.SYNC_TASK_NAME_MAX_LENGTH;
import static com.sentaroh.android.SMBSync2.Constants.SYNC_TASK_LIST_SEPARATOR;
import static com.sentaroh.android.SMBSync2.Constants.SYNC_TASK_NAME_UNUSABLE_CHARACTER;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_LAST_SCHEDULED_UTC_TIME_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_DAY_OF_THE_WEEK_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_ENABLED_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_HOURS_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_INTERVAL_FIRST_RUN_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_LAST_EXEC_TIME_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_MINUTES_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_SAVED_DATA_V2;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_SAVED_DATA_V3;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_SAVED_DATA_V4;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_SAVED_DATA_V5;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SCHEDULE_TYPE_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SEPARATOR_DUMMY_DATA;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SEPARATOR_ENTRY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SEPARATOR_ITEM;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SYNC_DELAYED_TIME_FOR_WIFI_ON_DEFAULT_VALUE;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SYNC_DELAYED_TIME_FOR_WIFI_ON_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SYNC_PROFILE_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SYNC_WIFI_OFF_AFTER_SYNC_END_KEY;
import static com.sentaroh.android.SMBSync2.ScheduleConstants.SCHEDULER_SYNC_WIFI_ON_BEFORE_SYNC_START_KEY;

public class ScheduleUtil {
    private static Logger slf4jLog = LoggerFactory.getLogger(ScheduleUtil.class);

    final static public ArrayList<ScheduleItem> loadScheduleData(Context c, GlobalParameters gp) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        ArrayList<ScheduleItem> sl = new ArrayList<ScheduleItem>();
        ScheduleItem sp = new ScheduleItem();
        String v2_data = prefs.getString(SCHEDULER_SCHEDULE_SAVED_DATA_V2, "-1");
        String v3_data = prefs.getString(SCHEDULER_SCHEDULE_SAVED_DATA_V3, "-1");
        String v4_data = prefs.getString(SCHEDULER_SCHEDULE_SAVED_DATA_V4, "-1");
        String v5_data = prefs.getString(SCHEDULER_SCHEDULE_SAVED_DATA_V5, "-1");
        if (!v5_data.equals("-1")) {
            sl = buildScheduleListV5(gp, v5_data);
        } else if (!v4_data.equals("-1")) {
            sl = buildScheduleListV4(gp, v4_data);
        } else if (!v3_data.equals("-1")) {
            sl = buildScheduleListV3(gp, v3_data);
        } else if (!v2_data.equals("-1")) {
            sl = buildScheduleListV2(gp, v2_data);
            saveScheduleData(c, gp, sl);
        } else {
            if (!prefs.getString(SCHEDULER_SCHEDULE_HOURS_KEY, "-1").equals("-1")) {
                sp.scheduleName = "NO NAME";
                sp.scheduleEnabled = prefs.getBoolean(SCHEDULER_SCHEDULE_ENABLED_KEY, false);
//                sp.scheduleIntervalFirstRunImmed = prefs.getBoolean(SCHEDULER_SCHEDULE_INTERVAL_FIRST_RUN_KEY, false);
                sp.scheduleType = prefs.getString(SCHEDULER_SCHEDULE_TYPE_KEY, ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_DAY);
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
                saveScheduleData(c, gp, sl);
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
            if (sd_sub.equals("end")) break;
            String[] sub_array = sd_sub.split("\t");
            if (sub_array.length >= 14) {
                for (String item : sub_array) item = item.replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                ScheduleItem si = new ScheduleItem();
                si.scheduleEnabled = sub_array[0].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                si.scheduleName = sub_array[1].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                nc++;
                if (si.scheduleName==null || si.scheduleName.equals("")) si.scheduleName="NO NAME"+nc;
                if (sub_array[2].length() > 0)
                    si.schedulePosition = Integer.valueOf(sub_array[2].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));
                si.scheduleType = sub_array[3].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleHours = sub_array[4].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleMinutes = sub_array[5].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleDayOfTheWeek = sub_array[6].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
//                si.scheduleIntervalFirstRunImmed = sub_array[7].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                if (sub_array[8].length() > 0)
                    si.scheduleLastExecTime = Long.valueOf(sub_array[8].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));
                si.syncTaskList = sub_array[9].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.syncGroupList = sub_array[10].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.syncWifiOnBeforeStart = sub_array[11].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                si.syncWifiOffAfterEnd = sub_array[12].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                if (sub_array[13].length() > 0)
                    si.syncDelayAfterWifiOn = Integer.valueOf(sub_array[13].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));

                if (!si.syncTaskList.equals("")) si.syncAutoSyncTask=false;

                if (si.scheduleLastExecTime == 0)
                    si.scheduleLastExecTime = System.currentTimeMillis();

                sl.add(si);
            }
        }
        return sl;
    }


    final static public ArrayList<ScheduleItem> buildScheduleListV3(GlobalParameters gp, String v3_data) {
        ArrayList<ScheduleItem> sl = new ArrayList<ScheduleItem>();
        String[] sd_array = v3_data.split(SCHEDULER_SEPARATOR_ENTRY);
        int nc=0;
        for (String sd_sub : sd_array) {
            if (sd_sub.equals("end")) break;
            String[] sub_array = sd_sub.split(SCHEDULER_SEPARATOR_ITEM);
            if (sub_array.length >= 14) {
                for (String item : sub_array) item = item.replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                ScheduleItem si = new ScheduleItem();
                si.scheduleEnabled = sub_array[0].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                si.scheduleName = sub_array[1].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                nc++;
                if (si.scheduleName==null || si.scheduleName.equals("")) si.scheduleName="NO NAME"+nc;

                if (sub_array[2].length() > 0)
                    si.schedulePosition = Integer.valueOf(sub_array[2].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));
                si.scheduleType = sub_array[3].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleHours = sub_array[4].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleMinutes = sub_array[5].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleDayOfTheWeek = sub_array[6].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
//                si.scheduleIntervalFirstRunImmed = sub_array[7].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                if (sub_array[8].length() > 0)
                    si.scheduleLastExecTime = Long.valueOf(sub_array[8].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));
                si.syncTaskList = sub_array[9].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.syncGroupList = sub_array[10].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.syncWifiOnBeforeStart = sub_array[11].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                si.syncWifiOffAfterEnd = sub_array[12].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                if (sub_array[13].length() > 0)
                    si.syncDelayAfterWifiOn = Integer.valueOf(sub_array[13].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));

                if (sub_array.length >= 15 && sub_array[14]!=null && sub_array[14].length() > 0)
                    si.scheduleDay = sub_array[14].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");

                if (!si.syncTaskList.equals("")) si.syncAutoSyncTask=false;

                if (si.scheduleLastExecTime == 0)
                    si.scheduleLastExecTime = System.currentTimeMillis();

                sl.add(si);
            }
        }
        return sl;
    }

    final static public ArrayList<ScheduleItem> buildScheduleListV4(GlobalParameters gp, String v4_data) {
        ArrayList<ScheduleItem> sl = new ArrayList<ScheduleItem>();
        String[] sd_array = v4_data.split(SCHEDULER_SEPARATOR_ENTRY);
        int nc=0;
        for (String sd_sub : sd_array) {
            if (sd_sub.equals("end")) break;
            String[] sub_array = sd_sub.split(SCHEDULER_SEPARATOR_ITEM);
            if (sub_array.length >= 14) {
                for (String item : sub_array) item = item.replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                ScheduleItem si = new ScheduleItem();
                si.scheduleEnabled = sub_array[0].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                si.scheduleName = sub_array[1].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                nc++;
                if (si.scheduleName==null || si.scheduleName.equals("")) si.scheduleName="NO NAME"+nc;

                if (sub_array[2].length() > 0)
                    si.schedulePosition = Integer.valueOf(sub_array[2].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));
                si.scheduleType = sub_array[3].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleHours = sub_array[4].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleMinutes = sub_array[5].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleDayOfTheWeek = sub_array[6].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
//                si.scheduleIntervalFirstRunImmed = sub_array[7].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                if (sub_array[8].length() > 0)
                    si.scheduleLastExecTime = Long.valueOf(sub_array[8].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));
                si.syncTaskList = sub_array[9].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.syncGroupList = sub_array[10].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.syncWifiOnBeforeStart = sub_array[11].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                si.syncWifiOffAfterEnd = sub_array[12].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                if (sub_array[13].length() > 0)
                    si.syncDelayAfterWifiOn = Integer.valueOf(sub_array[13].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));

                if (sub_array.length >= 15 && sub_array[14]!=null && sub_array[14].length() > 0)
                    si.scheduleDay = sub_array[14].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");

                if (sub_array.length >= 16 && sub_array[15]!=null && sub_array[15].length() > 0)
                    si.syncAutoSyncTask = sub_array[15].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                if (!si.syncTaskList.equals("")) si.syncAutoSyncTask=false;

                if (si.scheduleLastExecTime == 0)
                    si.scheduleLastExecTime = System.currentTimeMillis();

                sl.add(si);
            }
        }
        return sl;
    }

    final static public ArrayList<ScheduleItem> buildScheduleListV5(GlobalParameters gp, String v5_data) {
        ArrayList<ScheduleItem> sl = new ArrayList<ScheduleItem>();
        String[] sd_array = v5_data.split(SCHEDULER_SEPARATOR_ENTRY);
        int nc=0;
        for (String sd_sub : sd_array) {
            if (sd_sub.equals("end")) break;
            String[] sub_array = sd_sub.split(SCHEDULER_SEPARATOR_ITEM);
            if (sub_array.length >= 14) {
                for (String item : sub_array) item = item.replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                ScheduleItem si = new ScheduleItem();
                si.scheduleEnabled = sub_array[0].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                si.scheduleName = sub_array[1].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                nc++;
                if (si.scheduleName==null || si.scheduleName.equals("")) si.scheduleName="NO NAME"+nc;

                if (sub_array[2].length() > 0)
                    si.schedulePosition = Integer.valueOf(sub_array[2].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));
                si.scheduleType = sub_array[3].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleHours = sub_array[4].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleMinutes = sub_array[5].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.scheduleDayOfTheWeek = sub_array[6].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
//                si.scheduleIntervalFirstRunImmed = sub_array[7].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                if (sub_array[8].length() > 0)
                    si.scheduleLastExecTime = Long.valueOf(sub_array[8].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));
                si.syncTaskList = sub_array[9].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.syncGroupList = sub_array[10].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                si.syncWifiOnBeforeStart = sub_array[11].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                si.syncWifiOffAfterEnd = sub_array[12].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                if (sub_array[13].length() > 0)
                    si.syncDelayAfterWifiOn = Integer.valueOf(sub_array[13].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, ""));

                if (sub_array.length >= 15 && sub_array[14]!=null && sub_array[14].length() > 0)
                    si.scheduleDay = sub_array[14].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");

                if (sub_array.length >= 16 && sub_array[15]!=null && sub_array[15].length() > 0)
                    si.syncAutoSyncTask = sub_array[15].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "").equals("1") ? true : false;
                if (!si.syncTaskList.equals("")) si.syncAutoSyncTask=false;

                if (sub_array.length >= 17 && sub_array[16]!=null && sub_array[16].length() > 0) {
                    try {
                        si.syncOverrideOptionCharge = sub_array[16].replace(SCHEDULER_SEPARATOR_DUMMY_DATA, "");
                    } catch(Exception e) {}
                }

                if (si.scheduleLastExecTime == 0)
                    si.scheduleLastExecTime = System.currentTimeMillis();

                sl.add(si);
            }
        }
        return sl;
    }

    final static public ScheduleItem copyScheduleData(GlobalParameters gp, ScheduleItem sp) {
        ScheduleItem n_sp = new ScheduleItem();
        n_sp.scheduleDayOfTheWeek = sp.scheduleDayOfTheWeek;
        n_sp.scheduleDay = sp.scheduleDay;
        n_sp.scheduleHours = sp.scheduleHours;
//        n_sp.scheduleIntervalFirstRunImmed = sp.scheduleIntervalFirstRunImmed;
        n_sp.scheduleLastExecTime = sp.scheduleLastExecTime;
        n_sp.scheduleMinutes = sp.scheduleMinutes;
        n_sp.scheduleType = sp.scheduleType;
        n_sp.syncDelayAfterWifiOn = sp.syncDelayAfterWifiOn;
        n_sp.syncTaskList = sp.syncTaskList;
        n_sp.syncWifiOffAfterEnd = sp.syncWifiOffAfterEnd;
        n_sp.syncWifiOnBeforeStart = sp.syncWifiOnBeforeStart;
        n_sp.syncOverrideOptionCharge=sp.syncOverrideOptionCharge;
        return n_sp;
    }

    final static public void removeSyncTaskFromSchedule(GlobalParameters gp, CommonUtilities cu, ArrayList<ScheduleItem> sl, String delete_task_name) {
        for (ScheduleItem si : sl) {
            if (!si.syncTaskList.equals("")&& si.syncTaskList.contains(delete_task_name)) {
                if (si.syncTaskList.indexOf(SYNC_TASK_LIST_SEPARATOR)>0) {//Multiple entry
                    String[] task_list=si.syncTaskList.split(SYNC_TASK_LIST_SEPARATOR);
                    ArrayList<String>n_task_list=new ArrayList<String>();
                    if (task_list!=null) {
                        for(String stn:task_list) {
                            if (!stn.equals(delete_task_name)) n_task_list.add(stn);
                            else {
                                cu.addDebugMsg(1,"I","removeSyncTaskFromSchedule delete sync task from scheule. Schdule="+si.scheduleName+", Task="+stn);
                            }
                        }
                    }
                    if (n_task_list.size()>0) {
                        if (n_task_list.size()==1) si.syncTaskList=n_task_list.get(0);
                        else {
                            String sep="";
                            si.syncTaskList="";
                            for(String item:n_task_list) {
                                si.syncTaskList+=sep+item;
                                sep=SYNC_TASK_LIST_SEPARATOR;
                            }
                        }
                    } else {
                        cu.addDebugMsg(1,"I","removeSyncTaskFromSchedule all sync task list was deleted. Schdule="+si.scheduleName);
                        si.syncTaskList="";
                    }
                } else {
                    if (si.syncTaskList.equals(delete_task_name)) {
                        cu.addDebugMsg(1,"I","removeSyncTaskFromSchedule delete sync task from scheule. Schdule="+si.scheduleName+", Task="+si.syncTaskList);
                        cu.addDebugMsg(1,"I","removeSyncTaskFromSchedule all sync task list was deleted. Schdule="+si.scheduleName);
                        si.syncTaskList="";
                    }
                }
            }
        }
    }

    final static public void renameSyncTaskFromSchedule(GlobalParameters gp, CommonUtilities cu, ArrayList<ScheduleItem> schedule_list, String rename_task_name, String new_name) {
        for (ScheduleItem si : schedule_list) {
            if (!si.syncTaskList.equals("") && si.syncTaskList.contains(rename_task_name)) {
                if (si.syncTaskList.indexOf(SYNC_TASK_LIST_SEPARATOR)>0) {//Multiple entry
                    String[] task_list=si.syncTaskList.split(SYNC_TASK_LIST_SEPARATOR);
                    ArrayList<String>n_task_list=new ArrayList<String>();
                    if (task_list!=null) {
                        for(String stn:task_list) {
                            if (stn.equals(rename_task_name)) {
                                cu.addDebugMsg(1,"I","renameSyncTaskFromSchedule rename sync task from scheule. Schdule="+si.scheduleName+", Task="+stn+", New="+new_name);
                                n_task_list.add(new_name);
                            } else {
                                n_task_list.add(stn);
                            }
                        }
                    }
                    String sep="";
                    si.syncTaskList="";
                    for(String item:n_task_list) {
                        si.syncTaskList+=sep+item;
                        sep=SYNC_TASK_LIST_SEPARATOR;
                    }
                } else {
                    if (si.syncTaskList.equals(rename_task_name)) {
                        cu.addDebugMsg(1,"I","renameSyncTaskFromSchedule rename sync task from scheule. Schdule="+si.scheduleName+", Task="+si.syncTaskList+", New="+new_name);
                        si.syncTaskList=new_name;
                    }
                }
            }
        }
    }


    final static public void saveScheduleData(Context c, GlobalParameters gp, ArrayList<ScheduleItem> sl) {
        saveScheduleData(c, gp,sl,false);
    }

    final static public void saveScheduleData(Context c, GlobalParameters gp, ArrayList<ScheduleItem> sl, boolean use_apply) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String data = "";
        for (ScheduleItem si : sl) {
            data += (si.scheduleEnabled ? "1" : "0") + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;     //0
            data += si.scheduleName + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;                      //1
            data += String.valueOf(si.schedulePosition) + SCHEDULER_SEPARATOR_ITEM;             //2
            data += si.scheduleType + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;                      //3
            data += si.scheduleHours + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;                     //4
            data += si.scheduleMinutes + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;                   //5
            data += si.scheduleDayOfTheWeek + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;              //6
            data += "0" + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;//7
            data += String.valueOf(si.scheduleLastExecTime) + SCHEDULER_SEPARATOR_ITEM;         //8
            data += si.syncTaskList + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;                      //9
            data += si.syncGroupList + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;                     //10
            data += (si.syncWifiOnBeforeStart ? "1" : "0") + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;//11
            data += (si.syncWifiOffAfterEnd ? "1" : "0") + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM; //12
            data += String.valueOf(si.syncDelayAfterWifiOn) + SCHEDULER_SEPARATOR_ITEM;         //13
            data += si.scheduleDay + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;                       //14
            data += (si.syncAutoSyncTask ? "1" : "0") + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;    //15
            data += (si.syncOverrideOptionCharge) + SCHEDULER_SEPARATOR_DUMMY_DATA + SCHEDULER_SEPARATOR_ITEM;        //16
            data += SCHEDULER_SEPARATOR_ENTRY;

        }
        data += "end";
        if (use_apply) prefs.edit().putString(SCHEDULER_SCHEDULE_SAVED_DATA_V5, data).apply();
        else prefs.edit().putString(SCHEDULER_SCHEDULE_SAVED_DATA_V5, data).commit();
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
        if (sp.scheduleType.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_HOURS)) {
            if (c_mm >= s_min) {
                cal.set(c_year, c_month, c_day, c_hr, 0, 0);
                result = cal.getTimeInMillis() + (60 * 1000 * 60) + (60 * 1000 * s_min);
            } else {
                cal.set(c_year, c_month, c_day, c_hr, 0, 0);
                result = cal.getTimeInMillis() + (60 * 1000 * s_min);
            }
//    		cal.set(c_year, c_month, c_day, c_hr, c_mm, 0);
//    		result=cal.getTimeInMillis()+(60*1000);
        } else if (sp.scheduleType.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_DAY)) {
            cal.clear();
            cal.set(c_year, c_month, c_day, s_hrs, 0, 0);
            if ((c_hr * 100 + c_mm) >= (s_hrs * 100 + s_min)) {
                result = cal.getTimeInMillis() + (60 * 1000 * 60 * 24) + (60 * 1000 * s_min);
            } else {
                result = cal.getTimeInMillis() + (60 * 1000 * s_min);
            }
        } else if (sp.scheduleType.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_EVERY_MONTH)) {
            int s_day_last_day=0, s_day_temp=0;
            cal.set(Calendar.YEAR, c_year);
            cal.set(Calendar.MONTH, c_month);
            s_day_last_day=cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (s_day==99) {
                s_day_temp=s_day_last_day;
            } else {
                if (s_day>s_day_last_day) {
                    return 0;
                } else {
                    s_day_temp=s_day;
                }
            }
            cal.clear();
            cal.set(c_year, c_month, s_day_temp, s_hrs, s_min, 0);
            String curr=StringUtil.convDateTimeTo_YearMonthDayHourMinSec((System.currentTimeMillis()+59999));
            String cald=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(cal.getTimeInMillis());
            if ((System.currentTimeMillis()+59999)>=cal.getTimeInMillis()) {
                cal.add(Calendar.MONTH, 1);
            }
            result = cal.getTimeInMillis();
            slf4jLog.info("name="+sp.scheduleName+", c_year="+c_year+", c_month="+c_month+
                    ", s_day="+s_day_temp+", s_hrs="+s_hrs+", s_min="+s_min+", result="+StringUtil.convDateTimeTo_YearMonthDayHourMinSec(result));
        } else if (sp.scheduleType.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_INTERVAL)) {
            if (sp.scheduleLastExecTime == 0) {
                sp.scheduleLastExecTime = System.currentTimeMillis();
                long nt = sp.scheduleLastExecTime;
                if ((sp.scheduleLastExecTime % (60 * 1000)) > 0)
                    nt = (sp.scheduleLastExecTime / (60 * 1000)) * (60 * 1000);
                result = nt + s_min * (60 * 1000);
//                if (!sp.scheduleIntervalFirstRunImmed) {
//                    sp.scheduleLastExecTime = System.currentTimeMillis();
//                    long nt = sp.scheduleLastExecTime;
//                    if ((sp.scheduleLastExecTime % (60 * 1000)) > 0)
//                        nt = (sp.scheduleLastExecTime / (60 * 1000)) * (60 * 1000);
//                    result = nt + s_min * (60 * 1000);
//                } else {
//                    sp.scheduleLastExecTime = System.currentTimeMillis();
//                    long nt = sp.scheduleLastExecTime;
//                    if ((sp.scheduleLastExecTime % (60 * 1000)) > 0)
//                        nt = (sp.scheduleLastExecTime / (60 * 1000)) * (60 * 1000) + (60 * 1000);
//                    else nt += 60 * 1000;
//                    result = nt;
//                }
            } else {
                long nt = sp.scheduleLastExecTime;
                long m_nt=0l;
                if ((sp.scheduleLastExecTime % (60 * 1000)) > 0){
                    m_nt = (sp.scheduleLastExecTime / (60 * 1000)) * (60 * 1000);
                    result = m_nt + s_min * (60 * 1000);
                } else {
                    result = nt + s_min * (60 * 1000);
                }

                slf4jLog.info("name="+sp.scheduleName+", m_nt="+m_nt+", nt="+nt+", s_min="+s_min+", result="+StringUtil.convDateTimeTo_YearMonthDayHourMinSec(result));
            }
        } else if (sp.scheduleType.equals(ScheduleItem.SCHEDULER_SCHEDULE_TYPE_DAY_OF_THE_WEEK)) {
            boolean[] dwa = new boolean[]{false, false, false, false, false, false, false};
            for (int i = 0; i < sp.scheduleDayOfTheWeek.length(); i++) {
                String dw_s = sp.scheduleDayOfTheWeek.substring(i, i + 1);
                if (dw_s.equals("1")) dwa[i] = true;
            }
            int s_hhmm = Integer.parseInt(sp.scheduleHours) * 100 + s_min;
            int c_hhmm = c_hr * 100 + c_mm;
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
                } else {
                    c_dw++;
                    s_dw = 1;
                    boolean found = false;
                    for (int i = c_dw; i < 7; i++) {
                        if (dwa[i]) {
                            found = true;
                            break;
                        }
                        s_dw++;
                    }
                    if (!found) {
                        for (int i = 0; i < c_dw; i++) {
                            if (dwa[i]) {
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
                for (int i = c_dw; i < 7; i++) {
                    if (dwa[i]) {
                        found = true;
                        break;
                    }
                    s_dw++;
                }
                if (!found) {
                    for (int i = 0; i < c_dw; i++) {
                        if (dwa[i]) {
                            found = true;
                            break;
                        }
                        s_dw++;
                    }
                }
            }
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

    //check if schedule name is duplicate in Schedule List
    private static boolean isScheduleDuplicate(ArrayList<ScheduleItem> sl, String name) {
        int count = 0;
        for (ScheduleItem si : sl) {
            if (si.scheduleName.equalsIgnoreCase(name)) count++;
        }
        return count > 1;
    }

    //check if schedule name already exists in the ScheduleList
    //  - called when renaming or copying an existing schedule
    //  - on create a new schedule: called directly to set a unique default new name
    public static boolean isScheduleExists(ArrayList<ScheduleItem> sl, String name) {
        boolean result = false;
        for (ScheduleItem si : sl) {
            if (si.scheduleName.equalsIgnoreCase(name)) result = true;
        }
        return result;
    }

    private static boolean hasScheduleNameInvalidLength(String name) {
        return name.length() > SYNC_TASK_NAME_MAX_LENGTH;
    }

    private static String hasScheduleNameUnusableCharacter(Context c, String name) {
        for(String item:SYNC_TASK_NAME_UNUSABLE_CHARACTER) {
            if (name.contains(item)) return c.getString(R.string.msgs_schedule_list_edit_dlg_error_schedule_name_contains_unusabel_character,item);
        }
        return "";
    }

    //showAllErrors [true]: show all errors with "\n" separator
    //  - called by ScheduleAdapter for display of all name errors in Schedule TAB
    //  - else return error message of first error found in schedule
    //checkDup [false]: Check a NEW schedule name for validity against existing Schedules
    //  - called when rename/dupliacte/create a new schedule
    //checkDup [true]: Check if schedule name is already a duplicate in the ScheduleAdapter (previous versions bug, settings file modification)
    //  - called when editing an existing schedule
    //  - called in the Schedule Adapter to show duplicate entries in Schedule TAB
    //  - called to display/hide the start schedule button
    public static String isValidScheduleName(Context c, GlobalParameters gp, ArrayList<ScheduleItem> sl, String shedule_name, boolean checkDup, boolean showAllErrors) {
        String error_msg="";
        String sep_msg="";

        if (shedule_name.equals("")) {
            error_msg = c.getString(R.string.msgs_schedule_list_edit_dlg_error_sync_list_name_does_not_specified);
            sep_msg = "\n";
            if (!showAllErrors && !error_msg.equals("")) return error_msg;
        } else {
            if (hasScheduleNameInvalidLength(shedule_name)) {
                error_msg = c.getString(R.string.msgs_schedule_list_edit_dlg_error_schedule_name_too_long, SYNC_TASK_NAME_MAX_LENGTH, shedule_name.length());
                sep_msg = "\n";
                if (!showAllErrors && !error_msg.equals("")) return error_msg;
            }

            if (checkDup && isScheduleDuplicate(sl, shedule_name)) {
                error_msg += sep_msg + c.getString(R.string.msgs_schedule_confirm_msg_rename_duplicate_name);
                sep_msg = "\n";
                if (!showAllErrors && !error_msg.equals("")) return error_msg;
            } else if (!checkDup && isScheduleExists(sl, shedule_name)) {
                error_msg += sep_msg + c.getString(R.string.msgs_schedule_confirm_msg_rename_duplicate_name);
                sep_msg = "\n";
                if (!showAllErrors && !error_msg.equals("")) return error_msg;
            }

            String invalid_chars_msg = hasScheduleNameUnusableCharacter(c, shedule_name);
            if (!invalid_chars_msg.equals("")) {
                error_msg += sep_msg + invalid_chars_msg;
                sep_msg = "\n";
                if (!showAllErrors && !error_msg.equals("")) return error_msg;
            }
        }

        return error_msg;
    }

    public static String isValidScheduleItem(Context c, GlobalParameters gp, ArrayList<ScheduleItem> sl, ScheduleItem si, boolean checkDup, boolean showAllErrors) {
        String error_msg="";
        String sep_msg="";

        //check for schedule name errors
        error_msg = isValidScheduleName(c, gp, sl, si.scheduleName, checkDup, showAllErrors);
        if (!error_msg.equals("")) {
            sep_msg = "\n";
            if (!showAllErrors && !error_msg.equals("")) return error_msg;
        }

        //check for schedule sync tasks validity
        boolean schedule_error=false;
        String error_not_found_sync_task_name="";
        String error_sync_task_name="";
        if (si.syncAutoSyncTask) {
            //check if there is at least one autosync task
            if (gp.syncTaskList == null || gp.syncTaskList.size()==0) {
                schedule_error = true;
            } else {
                for(SyncTaskItem sti:gp.syncTaskList) {
                    if (sti.isSyncTaskAuto() && !sti.isSyncTestMode() && !sti.isSyncTaskError()) {
                        schedule_error = false;
                        break;
                    } else {
                        schedule_error = true;
                    }
                }
            }
        } else if (si.syncTaskList.equals("")) {
                //Schedule sync task list is empty
                schedule_error=true;
        } else if (si.syncTaskList.indexOf(SYNC_TASK_LIST_SEPARATOR)>0) {
            //Schedule has multiple sync task names
            String[] stl=si.syncTaskList.split(SYNC_TASK_LIST_SEPARATOR);
            String sep_not_found="", sep_error="";
            for(String stn:stl) {
                SyncTaskItem sti = getSyncTask(gp, stn);
                if (sti==null) {
                    schedule_error=true;
                    error_not_found_sync_task_name+=sep_not_found+stn;//display all not found sync tasks in Schedule Tab entries
                    sep_not_found=",";
                } else if (sti.isSyncTaskError()) {//display sync tasks with errors (invalid name, duplicate, error in master/target folder...)
                    schedule_error=true;
                    error_sync_task_name+=sep_error+stn;
                    sep_error=",";
                }
            }
        } else {
            //schedule has only one sync task name
            SyncTaskItem sti = ScheduleUtil.getSyncTask(gp, si.syncTaskList);
            if (sti==null) {
                schedule_error=true;
                error_not_found_sync_task_name=si.syncTaskList;
            } else if (sti.isSyncTaskError()) {
                schedule_error=true;
                error_sync_task_name=si.syncTaskList;
            }
        }

        if (schedule_error) {
            if (si.syncAutoSyncTask) {
                error_msg+= sep_msg + String.format(c.getString(R.string.msgs_active_sync_prof_not_found));
                if (!showAllErrors && !error_msg.equals("")) return error_msg;
            } else if (si.syncTaskList.equals("")) {
                error_msg+= sep_msg + c.getString(R.string.msgs_scheduler_info_sync_task_list_was_empty);
                if (!showAllErrors && !error_msg.equals("")) return error_msg;
            } else {
                if (!error_not_found_sync_task_name.equals("")) {
                    error_msg+= sep_msg + String.format(c.getString(R.string.msgs_scheduler_info_sync_task_was_not_found), error_not_found_sync_task_name);
                    sep_msg = "\n";
                    if (!showAllErrors && !error_msg.equals("")) return error_msg;
                }
                if (!error_sync_task_name.equals("")) {
                    error_msg+= sep_msg + String.format(c.getString(R.string.msgs_scheduler_info_sync_task_in_error), error_sync_task_name);
                    sep_msg = "\n";
                    if (!showAllErrors && !error_msg.equals("")) return error_msg;
                }
            }
        }

        return error_msg;
    }

    static public SyncTaskItem getSyncTask(GlobalParameters gp, String job_name) {
        for (SyncTaskItem sji : gp.syncTaskList) {
            if (sji.getSyncTaskName().equals(job_name)) {
                return sji;
            }
        }
        return null;
    }

    public static void setSchedulerLastScheduleTime(Context c, long utc) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putLong(SCHEDULER_LAST_SCHEDULED_UTC_TIME_KEY, utc).commit();
    }

    public static long getSchedulerLastScheduleTime(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getLong(SCHEDULER_LAST_SCHEDULED_UTC_TIME_KEY, 0L);
    }

    //set the next schedule info or schedule error message in bottom of main tabs
    public static void setSchedulerInfo(Context c, GlobalParameters gp, CommonUtilities cu) {
//        gp.scheduleInfoList =loadScheduleData(gp);
        ArrayList<ScheduleItem> sl = loadScheduleData(c, gp);
        String sched_list="";
        long latest_sched_time = -1;
        ArrayList<String>sched_array=new ArrayList<String>();
        boolean schedule_error=false;

///*debug*/for (SyncTaskItem sji : gp.syncTaskList) cu.addDebugMsg(1, "I", "setSchedulerInfo TaskName=\""+sji.getSyncTaskName()+"\"");
        if (gp.settingScheduleSyncEnabled) {
            for (ScheduleItem si : sl) {
///*debug*/   cu.addDebugMsg(1,"I", "setSchedulerInfo Schedule name="+si.scheduleName+", Enabled="+si.scheduleEnabled+", Type="+si.scheduleType+
//                    ", DayOfTheWeek="+si.scheduleDayOfTheWeek+", Day="+si.scheduleDay+", Hours="+si.scheduleHours+", Minutes="+si.scheduleMinutes+
//                    ", Tasklist="+si.syncTaskList+", Chnaged="+si.isChanged+", IntervalFirstRunImmed="+si.scheduleIntervalFirstRunImmed+
//                    ", WifiOnBeforeStart="+si.syncWifiOnBeforeStart+", DelayAfterWifiOn="+si.syncDelayAfterWifiOn+", WifiOffAfterEnd="+si.syncWifiOffAfterEnd);
                if (si.scheduleEnabled) {
                    long time = ScheduleUtil.getNextSchedule(si);
                    String dt=StringUtil.convDateTimeTo_YearMonthDayHourMin(time);
                    String item=dt+SYNC_TASK_LIST_SEPARATOR+si.scheduleName;

                    //check for schedule name errors
                    if (si.scheduleName.equals("")) {
                        schedule_error=true;
                        cu.addDebugMsg(1, "I", "setSchedulerInfo Error: schedule name is empty, schedule name=" + "\"" + "\"");
                    } else {
                        if (isScheduleDuplicate(sl, si.scheduleName)) {
                            schedule_error=true;
                            cu.addDebugMsg(1, "I", "setSchedulerInfo Error: schedule name is duplicate, schedule name=" + "\"" + si.scheduleName + "\"");
                        }
                        if (!hasScheduleNameUnusableCharacter(c, si.scheduleName).equals("")) {
                            schedule_error=true;
                            cu.addDebugMsg(1, "I", "setSchedulerInfo Error: schedule name has non valid chars, schedule name=" + "\"" + si.scheduleName + "\"");
                        }
                    }

                    //check for errors in schedule sync task list
                    if (si.syncAutoSyncTask) {
                        //NOP
                    } else {
                        String error_sched_name="", error_task_name="";
                        if (!si.syncTaskList.equals("")) {
                            if (si.syncTaskList.indexOf(SYNC_TASK_LIST_SEPARATOR)>0) {
                                String[] stl=si.syncTaskList.split(SYNC_TASK_LIST_SEPARATOR);
                                String sep="";
                                for(String stn:stl) {
                                    SyncTaskItem sti = getSyncTask(gp, stn);
                                    if (sti==null || sti.isSyncTaskError()) {
                                        schedule_error=true;
                                        error_task_name+= sep + "\"" + stn + "\"";//display all not found task names in the debug message
                                        error_sched_name="\""+si.scheduleName+"\"";
                                        sep = ", ";
                                    }
                                }
                            } else {
                                SyncTaskItem sti = getSyncTask(gp, si.syncTaskList);
                                if (sti==null || sti.isSyncTaskError()) {
                                    schedule_error=true;
                                    error_task_name="\""+si.syncTaskList+"\"";
                                    error_sched_name="\""+si.scheduleName+"\"";
                                }
                            }
                        } else {
                            schedule_error=true;
                            error_task_name= "\""+ si.syncTaskList+ "\"";
                            error_sched_name="\""+ si.scheduleName+ "\"";
                        }

                        if (!error_sched_name.equals("")) cu.addDebugMsg(1,"I", "setSchedulerInfo Error: invalid tasks in schedule, schedule name=" + error_sched_name + "; task name=" + error_task_name);
                    }
                    sched_array.add(item);
                }
            }
        }

        Collections.sort(sched_array);

        if (sched_array.size()>0) {
            String[] key=sched_array.get(0).split(SYNC_TASK_LIST_SEPARATOR);
            String sep="";
            for(String item:sched_array) {
                String[] s_key=item.split(SYNC_TASK_LIST_SEPARATOR);
                if (key[0].equals(s_key[0])) {
                    sched_list+=sep+s_key[1];
                    sep=SYNC_TASK_LIST_SEPARATOR;
                }
            }
            String sched_info ="";
            if (schedule_error) {
                gp.scheduleErrorText = String.format(c.getString(R.string.msgs_scheduler_info_next_schedule_main_error));
                gp.scheduleErrorView.setText(gp.scheduleErrorText);
                gp.scheduleErrorView.setTextColor(gp.themeColorList.text_color_warning);
                gp.scheduleErrorView.setVisibility(TextView.VISIBLE);
            } else {
                gp.scheduleErrorText="";
                gp.scheduleErrorView.setVisibility(TextView.GONE);
            }
            sched_info = c.getString(R.string.msgs_scheduler_info_next_schedule_main_info, key[0], sched_list);
            gp.scheduleInfoText = sched_info;
            gp.scheduleInfoView.setText(gp.scheduleInfoText);
        } else {
            gp.scheduleInfoText = c.getString(R.string.msgs_scheduler_info_schedule_disabled);
            gp.scheduleInfoView.setText(gp.scheduleInfoText);
            gp.scheduleErrorText="";
            gp.scheduleErrorView.setVisibility(TextView.GONE);
        }
    }

    public static String buildSchedulerNextInfo(Context c, ScheduleItem sp) {
        long nst = -1;
        nst = getNextSchedule(sp);
        String sched_time = "", result = "";
        if (nst != -1) {
            sched_time = StringUtil.convDateTimeTo_YearMonthDayHourMin(nst);
            if (sp.scheduleEnabled) {
                result = c.getString(R.string.msgs_scheduler_info_schedule_enabled) + ", " + String.format(c.getString(R.string.msgs_scheduler_info_next_schedule_time), sched_time);
            } else {
                result = c.getString(R.string.msgs_scheduler_info_schedule_disabled);
            }
        } else {
            result = c.getString(R.string.msgs_scheduler_info_schedule_disabled);
        }
        return result;
    }

}

