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

import android.Manifest;
import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.storage.StorageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.sentaroh.android.SMBSync2.Log.LogUtil;
import com.sentaroh.android.Utilities.Dialog.MessageDialogFragment;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.StringUtil;
import com.sentaroh.android.Utilities.SystemInfo;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.ThemeUtil;
import com.sentaroh.jcifs.JcifsUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import it.sephiroth.android.library.easing.Linear;

import static android.content.Context.USAGE_STATS_SERVICE;
import static com.sentaroh.android.SMBSync2.Constants.APPLICATION_TAG;
import static com.sentaroh.android.SMBSync2.Constants.DEFAULT_PREFS_FILENAME;

public final class CommonUtilities {
    private Context mContext = null;
    private LogUtil mLog = null;
    private GlobalParameters mGp = null;
    private String mLogIdent = "";

    private FragmentManager mFragMgr=null;

    public CommonUtilities(Context c, String li, GlobalParameters gp, FragmentManager fm) {
        mContext = c;// ContextはApplicationContext
        mLog = new LogUtil(c, li, gp);
        mLogIdent = li;
        mGp = gp;
        mFragMgr=fm;
    }

    final public SharedPreferences getPrefMgr() {
        return getPrefMgr(mContext);
    }

    public static void setSpinnerBackground(Context c, Spinner spinner, boolean theme_is_light) {
        if (theme_is_light)
            spinner.setBackground(c.getDrawable(R.drawable.spinner_color_background_light));
        else spinner.setBackground(c.getDrawable(R.drawable.spinner_color_background));
    }

    final static public SharedPreferences getPrefMgr(Context c) {
        return c.getSharedPreferences(DEFAULT_PREFS_FILENAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    public static ThemeColorList getThemeColorList(Context c) {
        return ThemeUtil.getThemeColorList(c);
    }

    public void showCommonDialog(final boolean negative, String type, String title, String msgtext, NotifyEvent ntfy) {
        MessageDialogFragment cdf =MessageDialogFragment.newInstance(negative, type, title, msgtext);
        cdf.showDialog(mFragMgr,cdf,ntfy);
    };

    public void showCommonDialog(final boolean negative, String type, String title, String msgtext,
                                 String button_text_ok, String button_text_cancel, NotifyEvent ntfy) {
        MessageDialogFragment cdf =MessageDialogFragment.newInstance(negative, type, title, msgtext, button_text_ok, button_text_cancel);
        cdf.showDialog(mFragMgr,cdf,ntfy);
    };

    public String getStringWithLocale(Activity c, String lang_code, int res_id) {

        Configuration config = new Configuration(c.getResources().getConfiguration());
        config.setLocale(new Locale(lang_code));
        String result = c.createConfigurationContext(config).getText(res_id).toString();
        return result;
    }

    final public void setLogId(String li) {
        mLog.setLogId(li);
    }

    final static public String getExecutedMethodName() {
        String name = Thread.currentThread().getStackTrace()[3].getMethodName();
        return name;
    }

    final public void resetLogReceiver() {
        mLog.resetLogReceiver();
    }

    final public void flushLog() {
        mLog.flushLog();
    }

    final public void rotateLogFile() {
        mLog.rotateLogFile();
    }

    static public String convertDateTimeWithTimzone(String time_zon_name, long time_value) {
        final Calendar gcal = new GregorianCalendar(TimeZone.getTimeZone(time_zon_name));
        gcal.setTimeInMillis(time_value);
        final int yyyy=gcal.get(Calendar.YEAR);
        final int month=gcal.get(Calendar.MONTH)+1;
        final int day=gcal.get(Calendar.DATE);
        final int hours=gcal.get(Calendar.HOUR_OF_DAY);
        final int minutes=gcal.get(Calendar.MINUTE);
        final int second=gcal.get(Calendar.SECOND);

        String date=String.format("%4d/%02d/%02d %02d:%02d:%02d", yyyy, month, day, hours, minutes, second);
        return date;
    }

    final public static String getWifiSsidName(WifiManager wm) {
        String wssid = "";
        if (wm.isWifiEnabled()) {
            String tssid = wm.getConnectionInfo().getSSID();
            if (tssid == null || tssid.equals("<unknown ssid>")) wssid = "";
            else wssid = tssid.replaceAll("\"", "");
            if (wssid.equals("0x")) wssid = "";
        }
        return wssid;
    }

    static public ArrayList<String> listSystemInfo(Context c, GlobalParameters gp) {

        ArrayList<String> out=SystemInfo.listSystemInfo(c, gp.safMgr);

        if (Build.VERSION.SDK_INT>=27) {
            out.add("setSettingGrantCoarseLocationRequired="+gp.settingGrantCoarseLocationRequired);
            out.add("ACCESS_COARSE_LOCATION Permission="+c.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
        }

        if (Build.VERSION.SDK_INT>=29) {
            boolean backgroundLocationPermissionApproved =
                    c.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)== PackageManager.PERMISSION_GRANTED;
            out.add("ACCESS_BACKGROUND_LOCATION="+backgroundLocationPermissionApproved);
        }

        if (Build.VERSION.SDK_INT>=27) {
            out.add("LocationService enabled="+isLocationServiceEnabled(c, gp)+", warning="+gp.settingSupressLocationServiceWarning);
        }

        if (Build.VERSION.SDK_INT >= 28) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) c.getSystemService(USAGE_STATS_SERVICE);
            if (usageStatsManager != null) {
                out.add("AppStnadbyBuket="+usageStatsManager.getAppStandbyBucket());
            }
        }
        out.add("");
        try {
            out.add("Network information:");

            WifiManager wm=(WifiManager)c.getSystemService(Context.WIFI_SERVICE);
            try {
                String ssid="";
                if (wm.isWifiEnabled() && wm.getConnectionInfo()!=null) ssid=wm.getConnectionInfo().getSSID();
                out.add("   WiFi="+wm.isWifiEnabled()+", SSID="+ssid);
            } catch(Exception e) {
                out.add("   WiFi status obtain error, error="+e.getMessage());
            }

            ConnectivityManager cm =(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork!=null) {
                String network=activeNetwork.getExtraInfo();

                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

                out.add("   Network="+network+", isConnected="+isConnected+", isWiFi="+isWiFi);
            } else {
                out.add("   No active network");
            }

            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    out.add("   Interface="+intf.getName()+", Address="+inetAddress.getHostAddress()+
                            ", isSiteLocalAddress="+inetAddress.isSiteLocalAddress());
                }
            }
        } catch (SocketException ex) {
            out.add("Network address error. error="+ex.getMessage());
        }

        out.add("Settings options:");
        out.add("  Error option="+gp.settingErrorOption);
        out.add("  WiFi lock option="+gp.settingWifiLockRequired);
        out.add("  Write sync result log="+gp.settingWriteSyncResultLog);
        out.add("  No compress file type="+gp.settingNoCompressFileType);
        out.add("  Prevent sync start delay="+gp.settingPreventSyncStartDelay);
        out.add("  Suppress Location service warning="+gp.settingSupressLocationServiceWarning);
        out.add("  Management file directory="+gp.settingMgtFileDir);

        out.add("");
        out.add("  Debug level="+gp.settingDebugLevel);
        out.add("  Log option="+gp.settingLogOption);
        out.add("  Logcat option="+gp.settingPutLogcatOption);
        out.add("  Log max file count="+gp.settingLogMaxFileCount);

        out.add("");
        out.add("  Suppress AppSpecific directory warning="+gp.settingSupressAppSpecifiDirWarning);
        out.add("  Notification message when sync ended="+gp.settingNotificationMessageWhenSyncEnded);
        out.add("  Ringtone when sync ended="+gp.settingRingtoneWhenSyncEnded);
        out.add("  Notification sound volume="+gp.settingNotificationVolume);
        out.add("  Vibrate when sync ended="+gp.settingVibrateWhenSyncEnded);
        out.add("  Fix device oprientation portrait="+gp.settingFixDeviceOrientationToPortrait);
        out.add("  Force tablet view in landscape="+gp.settingForceDeviceTabletViewInLandscape);
        out.add("  Screen theme language="+gp.settingScreenThemeLanguage);
        out.add("  Screen theme language value="+gp.settingScreenThemeLanguageValue);
        out.add("  Screen theme="+gp.settingScreenTheme);
        out.add("  Screen on if screen on at start of the sync="+gp.settingScreenOnIfScreenOnAtStartOfSync);

        out.add("");
        out.add("  Security use app startup="+gp.settingSecurityApplicationPasswordUseAppStartup);
        out.add("  Security use edit task="+gp.settingSecurityApplicationPasswordUseEditTask);
        out.add("  Security use export="+gp.settingSecurityApplicationPasswordUseExport);
        out.add("  Security re-init account and password="+gp.settingSecurityReinitSmbAccountPasswordValue);

        out.add("");
        out.add("  Sync message use standard text view="+gp.settingSyncMessageUseStandardTextView);
        out.add("  Exit clean="+gp.settingExitClean);

        return out;
    }

    final public static boolean isLocationServiceEnabled(Context c, GlobalParameters mGp) {
        if (Build.VERSION.SDK_INT>=27) {
            LocationManager lm= (LocationManager)c.getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT==27) {
                boolean gps=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean nw=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                return gps|nw;
            } else {
                return lm.isLocationEnabled();
            }
        }
        return false;
    }

    final public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
            return cm.getActiveNetworkInfo().isConnected();
        }
        return false;
    }

    final public void deleteLogFile() {
        mLog.deleteLogFile();
    }

    public String buildPrintMsg(String cat, String... msg) {
        return mLog.buildPrintLogMsg(cat, msg);
    }


    synchronized static public void saveMsgList(GlobalParameters gp) {
        long b_time=System.currentTimeMillis();
        try {
            OutputStream fos=null;
            String dir = gp.settingMgtFileDir;
            File mfd=new File(dir);
            if (!mfd.exists()) mfd.mkdirs();
            File mf = new File(dir + "/.messages");
            fos=new FileOutputStream(mf);
            PrintWriter bos=new PrintWriter(new BufferedOutputStream(fos,1024*1024*4));
            StringBuffer sb=new StringBuffer(1024);
            synchronized (gp.msgList) {
                for (SyncMessageItem smi:gp.msgList) {
                    sb.setLength(0);
                    sb.append("\u0001").append(smi.getCategory()).append("\u0000");
                    sb.append("\u0001").append(smi.getDate()).append("\u0000");
                    sb.append("\u0001").append(smi.getTime()).append("\u0000");
                    sb.append("\u0001").append(smi.getMessage()).append("\u0000");
//                String nl=sb.toString();
                    bos.println(sb.toString());
                }
            }
            bos.flush();
            bos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
//        Log.v(APPLICATION_TAG," saveMsgList elapsed time="+(System.currentTimeMillis()-b_time));
    }

    synchronized static public ArrayList<SyncMessageItem> loadMsgList(GlobalParameters gp) {
        long b_time=System.currentTimeMillis();
        ArrayList<SyncMessageItem> result=new ArrayList<SyncMessageItem>();
        try {
            String dir = gp.settingMgtFileDir;
            File mf = new File(dir + "/.messages");
//            File mf=new File(c.getFilesDir().getPath()+"/"+"message_list.txt");
            if (mf.exists()) {
                FileReader fr=new FileReader(mf);
                BufferedReader bis=new BufferedReader(fr, 1024*1024*2);
                String line=null;
                while((line=bis.readLine())!=null) {
                    String[] msg_array=line.split("\u0000");
                    if (msg_array.length>=4) {
                        SyncMessageItem smi=new SyncMessageItem(msg_array[0].replace("\u0001",""),
                                msg_array[1].replace("\u0001",""),
                                msg_array[2].replace("\u0001",""),
                                msg_array[3].replace("\u0001",""));
                        result.add(smi);
                    }
                }
                bis.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
//        Log.v(APPLICATION_TAG," loadMsgList elapsed time="+(System.currentTimeMillis()-b_time));
        return result;
    }

    private final int MAX_MSG_COUNT = 5000;

    final public void addLogMsg(String cat, String... msg) {
        addLogMsg(false, cat, msg);
    }

    final public void addLogMsgFromUI(String cat, String... msg) {
        addLogMsg(true, cat, msg);
    }

    final public void addLogMsg(boolean ui_thread, String cat, String... msg) {
        mLog.addLogMsg(cat, msg);
//		final SyncMessageItem mli=new SyncMessageItem(cat, "","",mLog.buildLogCatMsg("", cat, msg));
        StringBuilder log_msg = new StringBuilder(512);
        for (int i = 0; i < msg.length; i++) log_msg.append(msg[i]);
        String[] dt = StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis()).split(" ");

        final SyncMessageItem mli = new SyncMessageItem(cat, dt[0], dt[1], log_msg.toString());
        if (ui_thread) {
            putMsgListArray(mli);
        } else {
            mGp.uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    putMsgListArray(mli);
                }
            });
        }
    }

    private void putMsgListArray(SyncMessageItem mli) {
        synchronized (mGp.msgList) {
            if (mGp.msgList.size() > (MAX_MSG_COUNT + 200)) {
                for (int i = 0; i < 200; i++) mGp.msgList.remove(0);
            }
            mGp.msgList.add(mli);
            if (mGp.msgListAdapter != null) {
                mGp.msgListAdapter.notifyDataSetChanged();
                if (!mGp.freezeMessageViewScroll) {
//                    mGp.msgListView.setItemChecked(mGp.msgList.size() - 1, true);
//                    mGp.msgListView.setSelection(mGp.msgList.size() - 1);
                    mGp.msgListView.setSelection(mGp.msgList.size());
                }
            }
        }
    }

    final public void addDebugMsg(int lvl, String cat, String... msg) {
        mLog.addDebugMsg(lvl, cat, msg);
    }

    final public boolean isLogFileExists() {
        boolean result = false;
        result = mLog.isLogFileExists();
        if (mGp.settingDebugLevel >= 3) addDebugMsg(3, "I", "Log file exists=" + result);
        return result;
    }

    final public boolean getSettingsLogOption() {
        boolean result = false;
        result = getPrefMgr().getBoolean(mContext.getString(R.string.settings_log_option), false);
        if (mGp.settingDebugLevel >= 2) addDebugMsg(2, "I", "LogOption=" + result);
        return result;
    }

    final public boolean setSettingsLogOption(boolean enabled) {
        boolean result = false;
        getPrefMgr().edit().putBoolean(mContext.getString(R.string.settings_log_option), enabled).commit();
        if (mGp.settingDebugLevel >= 2) addDebugMsg(2, "I", "setLogOption=" + result);
        return result;
    }

    final public String getLogFilePath() {
        return mLog.getLogFilePath();
    }

    static public long getSettingsParmSaveDate(Context c, String dir, String fn) {
        File lf = new File(dir + "/" + fn);
        long result = 0;
        if (lf.exists()) {
            result = lf.lastModified();
        } else {
            result = -1;
        }
        return result;
    }

    public boolean isDebuggable() {
        PackageManager manager = mContext.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = manager.getApplicationInfo(mContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE)
            return true;
        return false;
    }

    public void initAppSpecificExternalDirectory(Context c) {
//		if (Build.VERSION.SDK_INT>=19) {
//			c.getExternalFilesDirs(null);
//		} else {
//		}
//        ContextCompat.getExternalFilesDirs(c, null);
        c.getExternalFilesDirs(null);
    }

    public static ArrayList<String> getUsbUuidListFromStorageManager(Context context) {
        ArrayList<String> uuids = new ArrayList<String>();
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList = sm.getClass().getDeclaredMethod("getVolumeList");
            Object[] volumeList = (Object[]) getVolumeList.invoke(sm);
            for (Object volume : volumeList) {
//                Method getPath = volume.getClass().getDeclaredMethod("getPath");
//	            Method isRemovable = volume.getClass().getDeclaredMethod("isRemovable");
                Method isPrimary = volume.getClass().getDeclaredMethod("isPrimary");
                Method getUuid = volume.getClass().getDeclaredMethod("getUuid");
                Method toString = volume.getClass().getDeclaredMethod("toString");
                String desc=(String)toString.invoke(volume);
                Method getLabel = volume.getClass().getDeclaredMethod("getUserLabel");
                boolean primary=(boolean)isPrimary.invoke(volume);
                String uuid=(String) getUuid.invoke(volume);
                String label=(String) getLabel.invoke(volume);
//                String path = (String) getPath.invoke(volume);
                if (uuid!=null && (!primary && label.toLowerCase().contains("usb"))) {
                    uuids.add(uuid);
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return uuids;
    }

    public boolean isWifiActive() {
        boolean ret = false;
        WifiManager mWifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (mWifi.isWifiEnabled()) ret = true;
        addDebugMsg(2, "I", "isWifiActive WifiEnabled=" + ret);
        return ret;
    }

    public String getConnectedWifiSsid() {
        String ret = "";
        WifiManager mWifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        String ssid = "";
        if (mWifi.isWifiEnabled()) {
            ssid = mWifi.getConnectionInfo().getSSID();
            if (ssid != null &&
                    !ssid.equals("0x") &&
                    !ssid.equals("<unknown ssid>") &&
                    !ssid.equals("")) ret = ssid;
//			Log.v("","ssid="+ssid);
        }
        addDebugMsg(2, "I", "getConnectedWifiSsid WifiEnabled=" + mWifi.isWifiEnabled() +", SSID=" + ssid + ", result=" + ret);
        return ret;
    }

    public static boolean isSmbHostAddressConnected(String addr) {
        boolean result = false;
        if (JcifsUtil.isIpAddressAndPortConnected(addr, 139, 3500) ||
                JcifsUtil.isIpAddressAndPortConnected(addr, 445, 3500)) result = true;
        return result;
    }

    public static boolean isSmbHostAddressConnected(String addr, int port) {
        boolean result = false;
        result = JcifsUtil.isIpAddressAndPortConnected(addr, port, 3500);
//		Log.v("","addr="+addr+", port="+port+", result="+result);
        return result;
    }

    public static String getLocalIpAddress() {
        String result = "";
        result=getIfIpAddress("wlan0");
        if (result.equals("")) result = "192.168.0.1";
        return result;
    }

    public static String getIfIpAddress(String if_name) {
        String result = "";
        boolean exit = false;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
//	            	Log.v("SMBSync2","ip="+inetAddress.getHostAddress()+", name="+intf.getName());
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress() && (inetAddress instanceof Inet4Address)) {
                        result = inetAddress.getHostAddress();
                        if (intf.getName().toLowerCase().equals(if_name)) {
                            exit = true;
                            break;
                        }
                    }
                }
                if (exit) break;
            }
        } catch (SocketException ex) {
            Log.e(APPLICATION_TAG, ex.toString());
//            result = "192.168.0.1";
        }
        return result;
    }

    public static boolean isIpAddressV6(String addr) {
        boolean result=false;
        InetAddress ia=getInetAddress(addr);
        if (ia!=null) result=ia instanceof Inet6Address;
        return result;
    }

    public static InetAddress getInetAddress(String addr) {
        InetAddress result=null;
        try {
            result=InetAddress.getByName(addr);
        } catch (Exception e) {
        }
        return result;
    }

    public static boolean isIpAddressV4(String addr) {
        boolean result=false;
        InetAddress ia=getInetAddress(addr);
        if (ia!=null) result=ia instanceof Inet4Address;
        return result;
    }

    public static String getIfIpAddress(CommonUtilities cu) {
        String result = "";
        boolean exit = false;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    cu.addDebugMsg(1,"I","getIfIpAddress() check1 intf="+intf.getName()+", isLoopbackAddress="+inetAddress.isLoopbackAddress()+
//                            ", isSiteLocalAddress="+inetAddress.isSiteLocalAddress()+", address="+inetAddress.getHostAddress()+", IPV4="+(inetAddress instanceof Inet4Address));
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress() && (inetAddress instanceof Inet4Address)) {
                        result = inetAddress.getHostAddress();
                        exit = true;
                        break;
                    }
                }
                if (exit) break;
            }
            if (!exit) {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
//                        cu.addDebugMsg(1,"I","getIfIpAddress() check2 intf="+intf.getName()+", isLoopbackAddress="+inetAddress.isLoopbackAddress()+
//                                ", isSiteLocalAddress="+inetAddress.isSiteLocalAddress()+", address="+inetAddress.getHostAddress()+", IPV4="+(inetAddress instanceof Inet4Address));
                        if (!inetAddress.isLoopbackAddress() &&(inetAddress instanceof Inet4Address)) {
                            result = inetAddress.getHostAddress();
                            exit = true;
                            break;
                        }
                    }
                    if (exit) break;
                }
            }
        } catch (SocketException ex) {
            cu.addDebugMsg(1,"I","getIfIpAddress() error="+ ex.toString());
        }
        return result;
    }

    public static String addScopeidToIpv6Address(String addr) {
        if (addr==null) return null;
        InetAddress ia=getInetAddress(addr);
        if (ia==null) return null;
        if ((ia instanceof  Inet6Address)) {
            if (ia.isLinkLocalAddress()) {
//                int si=((Inet6Address)ia).getScopeId();
//                NetworkInterface ni=((Inet6Address)ia).getScopedInterface();
                if (addr.contains("%")) return addr;
                else return addr+"%wlan0";
            }
        }
        return addr;
    }

    public static String resolveHostName(GlobalParameters gp, CommonUtilities cu, int smb_level, String hn) {
        String resolve_addr = JcifsUtil.getSmbHostIpAddressByHostName(smb_level, hn);
        if (resolve_addr != null) {//list dns name resolve
            try {
                InetAddress[] addr_list = InetAddress.getAllByName(hn);
                for (InetAddress item : addr_list) {
                    cu.addDebugMsg(1, "I", "resolveHostName DNS Query Name=" + hn + ", IP addr=" + item.getHostAddress());
                }
            } catch (Exception e) {
                cu.addDebugMsg(1, "I", "resolveHostName DNS Query failed. error="+e.getMessage());
            }
        }
//        resolve_addr="fe80::abd:43ff:fef6:482a";//for IPV6 Test
        cu.addDebugMsg(1, "I", "resolveHostName Name=" + hn + ", IP addr=" + resolve_addr+", smb="+smb_level);
        return resolve_addr;
    }


    public static String getIfHwAddress(String if_name) {
        String result = "";
        boolean exit = false;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
//	            	Log.v("SMBSync2","ip="+inetAddress.getHostAddress()+", name="+intf.getName());
                    if (inetAddress.isSiteLocalAddress() && (inetAddress instanceof Inet4Address)) {
                        if (intf.getName().equals(if_name)) {
                            for(int i=0;i<intf.getHardwareAddress().length;i++) result += String.format("%2h",intf.getHardwareAddress()[i]).replaceAll(" ","0");
                            exit = true;
                            break;
                        }
                    }
                }
                if (exit) break;
            }
        } catch (SocketException ex) {
            Log.e(APPLICATION_TAG, ex.toString());
//            result = "192.168.0.1";
        }
        return result;
    }

    private void sendMagicPacket(final String target_mac, final String if_network) {
//                sendMagicPacket("08:bd:43:f6:48:2a", if_ip);
        Thread th=new Thread(){
            @Override
            public void run() {
                byte[] broadcastMacAddress=new byte[6];
                for(int i=0;i<6;i++) broadcastMacAddress[i]=(byte)0xff;
                InetAddress broadcastIpAddress = null;
                try {
                    int j=if_network.lastIndexOf(".");
                    String if_ba=if_network.substring(0,if_network.lastIndexOf("."))+".255";
                    broadcastIpAddress = InetAddress.getByName(if_ba);//.getByAddress(new byte[]{-1,-1,-1,-1});

                    byte[] targetMacAddress=new byte[6];
                    String[] m_array=target_mac.split(":");
                    for(int i=0;i<6;i++) {
                        targetMacAddress[i]=Integer.decode("0x"+m_array[i]).byteValue();
                    }

                    byte[] magicPacket=new byte[102];
                    System.arraycopy(broadcastMacAddress,0, magicPacket,0,6);
                    for (int i=0;i<16;i++) {
                        System.arraycopy(targetMacAddress,0, magicPacket,(i*6)+6, 6);
                    }

// マジックパケットを任意のポートにブロードキャストするための UDPデータグラムパケット
                    DatagramPacket packet = new DatagramPacket(magicPacket, magicPacket.length, broadcastIpAddress, 9);

// マジックパケット 送信
                    DatagramSocket socket = new DatagramSocket();
                    socket.send(packet);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        th.start();
    }

    static public void setCheckedTextView(final CheckedTextView ctv) {
        ctv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctv.toggle();
            }
        });
    }

    public ArrayList<SyncHistoryItem> loadHistoryList() {
//		Log.v("","load hist started");
        ArrayList<SyncHistoryItem> hl = new ArrayList<SyncHistoryItem>();
        try {
            String dir = mGp.settingMgtFileDir;
            File lf = new File(dir + "/history.txt");
            if (lf.exists()) {
                FileReader fw = new FileReader(lf);
                BufferedReader br = new BufferedReader(fw, 4096 * 16);
                String line = "";
                String[] l_array = null;
                while ((line = br.readLine()) != null) {
                    l_array = line.split("\u0001");
//			    	Log.v("","line="+line);
//			    	Log.v("","em="+l_array[7]);
//			    	Log.v("","l_array_length="+l_array.length+", 2="+l_array[3]);
                    if (l_array != null && l_array.length >= 11 && !l_array[3].equals("")) {
                        SyncHistoryItem hli = new SyncHistoryItem();
                        try {
                            hli.sync_date = l_array[0];
                            hli.sync_time = l_array[1];
                            hli.sync_elapsed_time = l_array[2];
                            hli.sync_prof = l_array[3];
                            hli.sync_status = Integer.valueOf(l_array[4]);
                            hli.sync_test_mode = l_array[5].equals("1") ? true : false;
                            hli.sync_result_no_of_copied = Integer.valueOf(l_array[6]);
                            hli.sync_result_no_of_deleted = Integer.valueOf(l_array[7]);
                            hli.sync_result_no_of_ignored = Integer.valueOf(l_array[8]);
                            hli.sync_req = l_array[9];
                            hli.sync_error_text = l_array[10].replaceAll("\u0002", "\n");
                            if (!l_array[11].equals(" "))
                                hli.sync_result_no_of_retry = Integer.valueOf(l_array[11]);
                            hli.sync_transfer_speed=l_array[12];
//				    		hli.sync_ignored_file=string2Array(l_array[11]);
                            if (l_array.length >= 15) {
                                hli.sync_log_file_path = l_array[14];
                                if (!hli.sync_log_file_path.equals("")) {
                                    File tf = new File(hli.sync_log_file_path);
                                    if (tf.exists()) hli.isLogFileAvailable = true;
                                }
                                if (l_array.length >= 16) {
                                    hli.sync_result_file_path = l_array[15];
                                }
                            }
                            hl.add(hli);
                        } catch (Exception e) {
                            addLogMsg("W", "History list can not loaded");
                            e.printStackTrace();
                        }
                    }
                }
                br.close();
                if (hl.size() > 1) {
                    Collections.sort(hl, new Comparator<SyncHistoryItem>() {
                        @Override
                        public int compare(SyncHistoryItem lhs, SyncHistoryItem rhs) {
                            if (rhs.sync_date.equals(lhs.sync_date)) {
                                if (rhs.sync_time.equals(lhs.sync_time)) {
                                    return lhs.sync_prof.compareToIgnoreCase(rhs.sync_prof);
                                } else return rhs.sync_time.compareTo(lhs.sync_time);
                            } else return rhs.sync_date.compareTo(lhs.sync_date);
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hl;
    }

    final public String createSyncResultFilePath(String syncProfName) {
        String dir = mGp.settingMgtFileDir + "/result_log";
        File tlf = new File(dir);
        if (!tlf.exists()) {
            boolean create = tlf.mkdirs();
//            Log.v("","create="+create);
        }
//		Log.v("","fp="+dir+", exists="+tlf.exists());
        String dt = StringUtil.convDateTimeTo_YearMonthDayHourMinSec(System.currentTimeMillis());
        String fn = "result_" + syncProfName + "_" + dt + ".txt";
        String fp = dir + "/" + fn.replaceAll("/", "-").replaceAll(":", "").replaceAll(" ", "_");
        return fp;
    }

    final public void saveHistoryList(final ArrayList<SyncHistoryItem> hl) {
        if (hl == null) return;
        try {
            String dir = mGp.settingMgtFileDir;
            File lf = new File(dir);
            lf.mkdirs();
            lf = new File(dir + "/history.txt");
            FileWriter fw = new FileWriter(lf);
            BufferedWriter bw = new BufferedWriter(fw, 4096 * 16);
            int max = 500;
            StringBuilder sb_buf = new StringBuilder(1024 * 2);
            SyncHistoryItem shli = null;
            final ArrayList<SyncHistoryItem> del_list = new ArrayList<SyncHistoryItem>();
            for (int i = 0; i < hl.size(); i++) {
                if (!hl.get(i).sync_prof.equals("")) {
                    shli = hl.get(i);
                    if (i < max) {
                        String lfp = "";
                        if (shli.isLogFileAvailable) lfp = shli.sync_log_file_path;
                        sb_buf.setLength(0);
                        sb_buf.append(shli.sync_date).append("\u0001")                                      //0
                                .append(shli.sync_time).append("\u0001")                                    //1
                                .append(shli.sync_elapsed_time).append("\u0001")                            //2
                                .append(shli.sync_prof).append("\u0001")                                    //3
                                .append(shli.sync_status).append("\u0001")                                  //4
                                .append(shli.sync_test_mode ? "1" : "0").append("\u0001")                   //5
                                .append(shli.sync_result_no_of_copied).append("\u0001")                     //6
                                .append(shli.sync_result_no_of_deleted).append("\u0001")                    //7
                                .append(shli.sync_result_no_of_ignored).append("\u0001")                    //8
                                .append(shli.sync_req).append("\u0001")                                     //9
                                .append(shli.sync_error_text.replaceAll("\n", "\u0002")).append("\u0001")   //10
                                .append(shli.sync_result_no_of_retry).append("\u0001")                      //11 retry count
                                .append(shli.sync_transfer_speed).append("\u0001")                          //12
                                .append(" ").append("\u0001")                                               //13 Dummy
                                .append(lfp).append("\u0001")                                               //14
                                .append(shli.sync_result_file_path)                                         //15
                                .append("\n");

                        bw.append(sb_buf.toString());
                    } else {
                        del_list.add(shli);
                        if (!shli.sync_result_file_path.equals("")) {
                            File tlf = new File(shli.sync_result_file_path);
                            if (tlf.exists()) tlf.delete();
                        }
                    }
                }
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void setDialogBoxOutline(Context c, LinearLayout ll) {
        setDialogBoxOutline(c, ll, 3, 5);
    }

    static public void setDialogBoxOutline(Context c, LinearLayout ll, int padding_dp, int margin_dp) {
        ll.setBackgroundResource(R.drawable.dialog_box_outline);
        int padding=(int)toPixel(c.getResources(),padding_dp);
        ll.setPadding(padding, padding, padding, padding);

        ViewGroup.LayoutParams lp = ll.getLayoutParams();
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)lp;
        int margin=(int)toPixel(c.getResources(), margin_dp);
        mlp.setMargins(margin, mlp.topMargin, margin, mlp.bottomMargin);
        ll.setLayoutParams(mlp);
    }

    final static public float toPixel(Resources res, int dip) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
        return px;
    }

    public static boolean isCharging(Context c) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = c.registerReceiver(null, ifilter);
        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
//        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
//        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        return isCharging;
    }

    public static String trimTrailingBlank(String s) {
        if (s == null) return null;

        char[] val = s.toCharArray();
        int len = val.length;
        int st = 0;

        while ((st < len) && (val[len - 1] <= ' ' || val[len - 1] == '　')) {
            len--;
        }

        return ((st>0) || (len<val.length)) ? s.substring(st,len):s;
    }
//	final static private String array2String(StringBuilder sb_buf,String[] sa) {
//		sb_buf.setLength(0);
//		if (sa!=null) {
//			sb_buf
//				.append(Integer.toString(sa.length))
//				.append("\u0002");
//			for (int i=0;i<sa.length;i++) {
//				sb_buf.append("\u0003")
//					.append(sa[i])
//					.append("\u0002");
//			}
//		} else {
//			sb_buf.append(Integer.toString(0));
//		}
//		return sb_buf.toString();
//	};
//
//	final static private String[] string2Array(String str) {
//		String[]t_array=str.split("\u0002");
//		String[] result=null;
//		if (!t_array[0].equals("0")) {
//			result=new String[Integer.parseInt(t_array[0])];
//			for (int i=0;i<result.length;i++) {
//				result[i]=t_array[i+1].replace("\u0003", "");
//			}
//		} 
//		return result;
//	};

//	public void addHistoryList(ArrayList<SyncHistoryItem> hl, SyncHistoryItem item) {
//		synchronized(hl) {
//			if (hl.size()==1) {
//				if (hl.get(0).sync_prof.equals("")) hl.remove(0);
//			}
////			Log.v("","add");
//			hl.add(0,item);
////			Log.v("","Notify");
//		}
//	};
//	public void removeHistoryList(ArrayList<SyncHistoryItem> hl, int pos) {
//		String result_fp=hl.get(pos).sync_result_file_path;
//		if (!result_fp.equals("")) {
//			File lf=new File(result_fp);
//			lf.delete();
//		}
//		hl.remove(pos);
//	};
//	public void removeHistoryList(ArrayList<SyncHistoryItem> hl, SyncHistoryItem item) {
//		String result_fp=item.sync_result_file_path;
//		if (!result_fp.equals("")) {
//			File lf=new File(result_fp);
//			lf.delete();
//		}
//		hl.remove(item);
//	};
}

