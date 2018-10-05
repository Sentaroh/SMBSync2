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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * Created by sentaroh on 2018/03/21.
 */
class ScheduleItem implements Serializable, Cloneable {
    public static final String SCHEDULER_SCHEDULE_TYPE_EVERY_HOURS = "H";
    public static final String SCHEDULER_SCHEDULE_TYPE_EVERY_DAY = "D";
    public static final String SCHEDULER_SCHEDULE_TYPE_DAY_OF_THE_WEEK = "W";
    public static final String SCHEDULER_SCHEDULE_TYPE_EVERY_MONTH = "M";
    public static final String SCHEDULER_SCHEDULE_TYPE_INTERVAL = "I";

    public boolean scheduleEnabled = false;

    public String scheduleName = "";

    public int schedulePosition = 0;

    public String scheduleType = SCHEDULER_SCHEDULE_TYPE_EVERY_DAY;
    public String scheduleDay = "1";
    public String scheduleHours = "00";
    public String scheduleMinutes = "00";
    public String scheduleDayOfTheWeek = "0000000";

    public boolean scheduleIntervalFirstRunImmed = true;

    public long scheduleLastExecTime = 0;

    public String syncTaskList = "";

    public String syncGroupList = "";

    public boolean syncWifiOnBeforeStart = false;
    public boolean syncWifiOffAfterEnd = false;
    public int syncDelayAfterWifiOn = 5;

    public transient boolean isChecked = false;
    public transient boolean isChanged = false;

    @Override
    public ScheduleItem clone() {
        ScheduleItem new_si = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            oos.flush();
            oos.close();

            baos.flush();
            byte[] ba_buff = baos.toByteArray();
            baos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(ba_buff);
            ObjectInputStream ois = new ObjectInputStream(bais);

            new_si = (ScheduleItem) ois.readObject();
            ois.close();
            bais.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return new_si;
    }

}
