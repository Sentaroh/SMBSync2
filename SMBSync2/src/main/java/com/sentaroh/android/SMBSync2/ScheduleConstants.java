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


import static com.sentaroh.android.SMBSync2.Constants.APPLICATION_TAG;

public class ScheduleConstants {
    public static final String SCHEDULER_SCHEDULE_ENABLED_KEY = "scheduler_schedule_enabled_key";

    public static final String SCHEDULER_SCHEDULE_SAVED_DATA_V2 = "scheduler_schedule_saved_data_v2_key";
    public static final String SCHEDULER_SCHEDULE_SAVED_DATA_V3 = "scheduler_schedule_saved_data_v3_key";
    public static final String SCHEDULER_SCHEDULE_SAVED_DATA_V4 = "scheduler_schedule_saved_data_v4_key";
    public static final String SCHEDULER_SCHEDULE_SAVED_DATA_V5 = "scheduler_schedule_saved_data_v5_key";

    public static final String SCHEDULER_SCHEDULE_INTERVAL_FIRST_RUN_KEY = "scheduler_schedule_interval_first_run_key";
    public static final String SCHEDULER_SCHEDULE_TYPE_KEY = "scheduler_schedule_type_key";

    public static final String SCHEDULER_SCHEDULE_LAST_EXEC_TIME_KEY = "scheduler_schedule_last_exec_time_key";

    public static final String SCHEDULER_SCHEDULE_HOURS_KEY = "scheduler_schedule_hours_key";
    public static final String SCHEDULER_SCHEDULE_MINUTES_KEY = "scheduler_schedule_minutes_key";
    public static final String SCHEDULER_SCHEDULE_DAY_OF_THE_WEEK_KEY = "scheduler_schedule_day_of_the_week_key";

    public static final String SCHEDULER_INTENT_TIMER_EXPIRED = "com.sentaroh.android." + APPLICATION_TAG + ".ACTION_TIMER_EXPIRED";
    public static final String SCHEDULER_INTENT_START_BY_USER = SCHEDULER_INTENT_TIMER_EXPIRED;
    public static final String SCHEDULER_INTENT_SET_TIMER = "com.sentaroh.android." + APPLICATION_TAG + ".ACTION_SET_TIMER";
    public static final String SCHEDULER_INTENT_SET_TIMER_IF_NOT_SET = "com.sentaroh.android." + APPLICATION_TAG + ".ACTION_SET_TIMER_IF_NOT_SET";

    public static final String SCHEDULER_SYNC_PROFILE_KEY = "scheduler_sync_profile_key";

    public static final String SCHEDULER_SYNC_WIFI_ON_BEFORE_SYNC_START_KEY = "scheduler_sync_wifi_on_before_sync_start_key";
    public static final String SCHEDULER_SYNC_WIFI_OFF_AFTER_SYNC_END_KEY = "scheduler_sync_wifi_off_after_sync_end_key";
    public static final String SCHEDULER_SYNC_DELAYED_TIME_FOR_WIFI_ON_KEY = "scheduler_sync_delayed_time_for_wifi_on_key";
    public static final String SCHEDULER_SYNC_DELAYED_TIME_FOR_WIFI_ON_DEFAULT_VALUE = "5";

    public static final String SCHEDULER_SCHEDULE_NAME_KEY = "scheduler_schedule_name_key";

    public static final String SCHEDULER_ENABLED_KEY = "scheduler_enabled_key";

    public static final String SCHEDULER_LAST_SCHEDULED_UTC_TIME_KEY = "scheduler_last_set_utc_time_key";

    public static final String SCHEDULER_SEPARATOR_DUMMY_DATA = "\u0000";
    public static final String SCHEDULER_SEPARATOR_ENTRY = "\u0001";
    public static final String SCHEDULER_SEPARATOR_ITEM = "\u0002";
}
