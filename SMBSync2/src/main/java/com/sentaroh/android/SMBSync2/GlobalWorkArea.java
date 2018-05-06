package com.sentaroh.android.SMBSync2;

import android.content.Context;

public class GlobalWorkArea {
    static public GlobalParameters gp=null;
    static public GlobalParameters getGlobalParameters(Context c) {
        if (gp ==null) {
            gp =new GlobalParameters();
            gp.initGlobalParamter(c);
        }
        return gp;
    }
}
