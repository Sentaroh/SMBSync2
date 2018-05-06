package com.sentaroh.android.SMBSync2;

import android.content.Context;

public class CommonStaticPointer {
    static public GlobalParameters globalParameters=null;
    static public GlobalParameters initGlobalParameters(Context c) {
        if (globalParameters==null) {
            globalParameters=new GlobalParameters();
            globalParameters.initGlobalParamter(c);
        }
        return globalParameters;
    }
}
