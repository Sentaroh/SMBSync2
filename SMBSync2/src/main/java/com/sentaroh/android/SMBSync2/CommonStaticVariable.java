package com.sentaroh.android.SMBSync2;

import android.content.Context;

public class CommonStaticVariable {
    static public GlobalParameters globalParameters=null;
    static public GlobalParameters getGlobalParameters(Context c) {
        if (globalParameters==null) {
            globalParameters=new GlobalParameters();
            globalParameters.initGlobalParamter(c);
        }
        return globalParameters;
    }
}
