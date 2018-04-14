package com.sentaroh.android.SMBSync2.Log;
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
import static com.sentaroh.android.SMBSync2.Log.LogConstants.*;
import android.content.Context;
import com.sentaroh.android.SMBSync2.GlobalParameters;
import com.sentaroh.android.Utilities.CommonGlobalParms;
import com.sentaroh.android.Utilities.LogUtil.CommonLogReceiver;

public class LogReceiver extends CommonLogReceiver{
	@Override
	public void setLogParms(Context c, CommonGlobalParms cgp) {
		
		GlobalParameters mgp=new GlobalParameters();
		mgp.appContext=c;
		mgp.loadSettingsParms();
		
		cgp.setDebugLevel(mgp.settingDebugLevel);
		cgp.setLogLimitSize(mgp.settingLogFileMaxSize);
		cgp.setLogMaxFileCount(mgp.settingLogMaxFileCount);
		cgp.setLogEnabled(mgp.settingLogOption);
		cgp.setLogDirName(mgp.settingMgtFileDir);
		cgp.setLogFileName(mgp.settingLogMsgFilename);
		cgp.setApplicationTag(APPLICATION_TAG);
		cgp.setLogIntent(BROADCAST_LOG_RESET,
				BROADCAST_LOG_DELETE,
				BROADCAST_LOG_FLUSH,
				BROADCAST_LOG_ROTATE,
				BROADCAST_LOG_SEND,
				BROADCAST_LOG_CLOSE);

	};
}
