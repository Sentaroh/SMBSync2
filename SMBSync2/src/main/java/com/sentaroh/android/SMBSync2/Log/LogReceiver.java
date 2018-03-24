package com.sentaroh.android.SMBSync2.Log;

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
