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

import java.io.Serializable;

/**
 * Created by sentaroh on 2018/03/21.
 */
class SyncMessageItem implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String msgCat, msgBody, msgDate, msgTime, msgTitle, msgPath, msgType;

    public SyncMessageItem(String cat, String mdate, String mtime, String mtitle, String msg, String mpath, String mtype) {
        msgCat = cat;
        msgBody = msg;
        msgDate = mdate;
        msgTime = mtime;
        msgTitle = mtitle;
        msgType = mtype;
        msgPath = mpath;
    }

    public String getCategory() {
        return msgCat;
    }

    public String getDate() {
        return msgDate;
    }

    public String getTime() {
        return msgTime;
    }

    public String getMessage() {
        return msgBody;
    }

    public String getTitle() {
        return msgTitle;
    }

    public String getPath() {
        return msgPath;
    }

    public String getType() {
        return msgType;
    }

    public String toString() {
        return msgCat + " " + msgDate + " " + msgTime + " " + msgTitle + " " + msgPath + " " + msgBody + " " + msgType;
    }

}
