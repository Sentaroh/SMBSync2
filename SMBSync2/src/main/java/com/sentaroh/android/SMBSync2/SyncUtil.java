package com.sentaroh.android.SMBSync2;

/*
The MIT License (MIT)
Copyright (c) 2011-2013 Sentaroh

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

import static com.sentaroh.android.SMBSync2.Constants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckedTextView;
import android.widget.Spinner;

import com.sentaroh.android.SMBSync2.Log.LogUtil;
import com.sentaroh.android.Utilities.StringUtil;
import com.sentaroh.jcifs.JcifsUtil;

public final class SyncUtil {
    private Context mContext = null;

    private LogUtil mLog = null;

    private GlobalParameters mGp = null;

    @SuppressWarnings("unused")
    private String mLogIdent = "";

    public SyncUtil(Context c, String li, GlobalParameters gp) {
        mContext = c;// ContextはApplicationContext
        mLog = new LogUtil(c, li, gp);
        mLogIdent = li;
        mGp = gp;
    }

    final public SharedPreferences getPrefMgr() {
        return getPrefMgr(mContext);
    }

    public static void setSpinnerBackground(Context c, Spinner spinner, boolean theme_is_light) {
        if (theme_is_light)
            spinner.setBackground(c.getDrawable(R.drawable.spinner_color_background_light));
        else spinner.setBackground(c.getDrawable(R.drawable.spinner_color_background));
    }

    ;

    @SuppressWarnings("deprecation")
    @SuppressLint("InlinedApi")
    final static public SharedPreferences getPrefMgr(Context c) {
        return c.getSharedPreferences(DEFAULT_PREFS_FILENAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    final public void setLogId(String li) {
        mLog.setLogId(li);
    }

    ;

    final static public String getExecutedMethodName() {
        String name = Thread.currentThread().getStackTrace()[3].getMethodName();
        return name;
    }

    final public void resetLogReceiver() {
        mLog.resetLogReceiver();
    }

//    static public BaseContext buildBaseContextWithSmbProtocol(RemoteAuthInfo ra) {
//        BaseContext bc=null;
//        try {
//            Properties prop=new Properties();
////            prop_master.setProperty("jcifs.smb.lmCompatibility", "0");
////            prop_master.setProperty("jcifs.smb.client.useExtendedSecurity", "false");
////            prop_master.setProperty("jcifs.smb.client.ipcSigningEnforced","false");
////            prop_master.setProperty("jcifs.smb.client.signingPreferred", "false");
////            prop_master.setProperty("jcifs.smb.client.signingEnforced", "false");
////            prop_master.setProperty("jcifs.smb.client.encryptionEnabled", "false");
////            prop_master.setProperty("jcifs.smb.useRawNTLM", "true");
////
////            prop_master.setProperty("jcifs.smb.client.forceExtendedSecurity", "false");
////
////            prop_master.setProperty("jcifs.smb.client.useSMB2Negotiation", "false");
//
////            prop_master.setProperty("jcifs.smb.client.minVersion","SMB1");
////            prop_master.setProperty("jcifs.smb.client.maxVersion","SMB1");
//            if (ra.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SYSTEM)) {
//                if (ra.smb_ipc_signing_enforced) prop.setProperty("jcifs.smb.client.ipcSigningEnforced","true");
//                else prop.setProperty("jcifs.smb.client.ipcSigningEnforced","false");
//                prop.setProperty("jcifs.smb.client.minVersion","SMB1");
//                prop.setProperty("jcifs.smb.client.maxVersion","SMB210");
//            } else if (ra.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1_ONLY)) {
//                prop.setProperty("jcifs.smb.client.minVersion","SMB1");
//                prop.setProperty("jcifs.smb.client.maxVersion","SMB1");
//                prop.setProperty("jcifs.smb.client.ipcSigningEnforced","false");
//            } else if (ra.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB2_ONLY)) {
//                if (ra.smb_ipc_signing_enforced) prop.setProperty("jcifs.smb.client.ipcSigningEnforced","true");
//                else prop.setProperty("jcifs.smb.client.ipcSigningEnforced","false");
//                prop.setProperty("jcifs.smb.client.minVersion","SMB210");
//                prop.setProperty("jcifs.smb.client.maxVersion","SMB210");
//            }
//            bc = new BaseContext(new PropertyConfiguration(prop));
//        } catch (CIFSException e) {
//            e.printStackTrace();
//        }
//        return bc;
//    }

    final public void flushLog() {
        mLog.flushLog();
    }

    final public void rotateLogFile() {
        mLog.rotateLogFile();
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

    final public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
            return cm.getActiveNetworkInfo().isConnected();
        }
        return false;
    }

    ;

    final public void deleteLogFile() {
        mLog.deleteLogFile();
    }

    ;

    public String buildPrintMsg(String cat, String... msg) {
        return mLog.buildPrintLogMsg(cat, msg);
    }

    ;

    private final int MAX_MSG_COUNT = 5000;

    final public void addLogMsg(String cat, String... msg) {
        mLog.addLogMsg(cat, msg);
//		final SyncMessageItem mli=new SyncMessageItem(cat, "","",mLog.buildLogCatMsg("", cat, msg));
        StringBuilder log_msg = new StringBuilder(512);
        for (int i = 0; i < msg.length; i++) log_msg.append(msg[i]);
        String[] dt = StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis()).split(" ");

        final SyncMessageItem mli = new SyncMessageItem(cat, dt[0], dt[1], log_msg.toString());
        mGp.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mGp.msgList) {
                    if (mGp.msgList.size() > (MAX_MSG_COUNT + 100)) {
                        for (int i = 0; i < 100; i++) mGp.msgList.remove(0);
                    }
                    mGp.msgList.add(mli);
                    if (mGp.msgListAdapter != null) {
                        mGp.msgListAdapter.notifyDataSetChanged();
                        if (!mGp.freezeMessageViewScroll)
                            mGp.msgListView.setSelection(mGp.msgList.size());
                    }
                }
            }
        });
    }

    ;

    final public void addDebugMsg(int lvl, String cat, String... msg) {
        mLog.addDebugMsg(lvl, cat, msg);
    }

    ;

    final public boolean isLogFileExists() {
        boolean result = false;
        result = mLog.isLogFileExists();
        if (mGp.settingDebugLevel >= 3) addDebugMsg(3, "I", "Log file exists=" + result);
        return result;
    }

    ;

    final public boolean getSettingsLogOption() {
        boolean result = false;
        result = getPrefMgr().getBoolean(mContext.getString(R.string.settings_log_option), false);
        if (mGp.settingDebugLevel >= 2) addDebugMsg(2, "I", "LogOption=" + result);
        return result;
    }

    ;

    final public boolean setSettingsLogOption(boolean enabled) {
        boolean result = false;
        getPrefMgr().edit().putBoolean(mContext.getString(R.string.settings_log_option), enabled).commit();
        if (mGp.settingDebugLevel >= 2) addDebugMsg(2, "I", "setLLogOption=" + result);
        return result;
    }

    ;

    final public String getLogFilePath() {
        return mLog.getLogFilePath();
    }

    ;


//	static public ArrayList<PreferenceParmListIItem> loadSettingsParmFromFile(Context c, String dir, String fn) {
//		ArrayList<PreferenceParmListIItem>ispl=new ArrayList<PreferenceParmListIItem>();
//		File lf=new File(dir+"/"+fn);
//		if (lf.exists()) {
//			BufferedReader br;
//			try {
//				Editor prefs = getPrefMgr(c).edit();
//				br = new BufferedReader(new FileReader(lf),8192);
//				String pl;
//				while ((pl = br.readLine()) != null) {
//					String tmp_ps=pl.substring(7,pl.length());
//					String[] tmp_pl=tmp_ps.split("\t");// {"type","name","active",options...};
//					if (tmp_pl[1]!=null && tmp_pl.length>=5 && tmp_pl[1].equals(SMBSYNC2_PROF_TYPE_SETTINGS)) {
////						String[] val = new String[]{parm[2],parm[3],parm[4]};
//						PreferenceParmListIItem ppli=new PreferenceParmListIItem();
//						if (tmp_pl[2]!=null) ppli.parms_key=tmp_pl[2];
//						if (tmp_pl[3]!=null) ppli.parms_type=tmp_pl[3];
//						if (tmp_pl[4]!=null) {
//							if (ppli.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_STRING)) {
//								byte[] enc_array=Base64Compat.decode(tmp_pl[4], Base64Compat.NO_WRAP);
//								ppli.parms_value=new String(enc_array);
//							} else {
//								ppli.parms_value=tmp_pl[4];
//							}
//						}
//						if (!ppli.parms_key.equals("") && !ppli.parms_type.equals("")) {
////							Log.v("","key="+tmp_pl[2]+", value="+ppli.parms_value+", type="+tmp_pl[3]);
////							Log.v("","key="+ppli.parms_key+", value="+ppli.parms_value+", type="+ppli.parms_type);
//							ispl.add(ppli);
//							if (ppli.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_STRING)) {
//								prefs.putString(ppli.parms_key, ppli.parms_value).commit();
//							} else if (ppli.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_LONG)) {
//								prefs.putLong(ppli.parms_key, Long.parseLong(ppli.parms_value)).commit();
//							} else if (ppli.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_INT)) {
//								prefs.putInt(ppli.parms_key, Integer.parseInt(ppli.parms_value)).commit();
//							} else if (ppli.parms_type.equals(SMBSYNC2_UNLOAD_SETTINGS_TYPE_BOOLEAN)) {
//								if (ppli.parms_value.equals("true")) prefs.putBoolean(ppli.parms_key, true).commit();
//								else prefs.putBoolean(ppli.parms_key, false).commit();
//							}
//						}
//					}
//				}
//				br.close();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else {
//			ispl=null;
//		}
//		return ispl;
//	};

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

    ;


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

    ;

    public void initAppSpecificExternalDirectory(Context c) {
//		if (Build.VERSION.SDK_INT>=19) {
//			c.getExternalFilesDirs(null);
//		} else {
//		}
        ContextCompat.getExternalFilesDirs(c, null);
    }

    ;

    public boolean isWifiActive() {
        boolean ret = false;
        WifiManager mWifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (mWifi.isWifiEnabled()) ret = true;
        addDebugMsg(2, "I", "isWifiActive WifiEnabled=" + ret);
        return ret;
    }

    ;

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
        addDebugMsg(2, "I", "getConnectedWifiSsid WifiEnabled=" + mWifi.isWifiEnabled() +
                ", SSID=" + ssid + ", result=" + ret);
        return ret;
    }

    ;

    public static boolean isSmbHostAddressConnected(String addr) {
        boolean result = false;
        if (JcifsUtil.isIpAddressAndPortConnected(addr, 139, 3500) ||
                JcifsUtil.isIpAddressAndPortConnected(addr, 445, 3500)) result = true;
        return result;
    }

    ;

    public static boolean isSmbHostAddressConnected(String addr, int port) {
        boolean result = false;
        result = JcifsUtil.isIpAddressAndPortConnected(addr, port, 3500);
//		Log.v("","addr="+addr+", port="+port+", result="+result);
        return result;
    }

    ;

    public static String getLocalIpAddress() {
        String result = "";
        boolean exit = false;
        try {
            for (Enumeration<NetworkInterface> en =
                 NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr =
                     intf.getInetAddresses();
                     enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
//	                if (!inetAddress.isLoopbackAddress() && !(inetAddress.toString().indexOf(":")>=0)) {
//	                    return inetAddress.getHostAddress().toString();
//	                }
//	            	Log.v("","ip="+inetAddress.getHostAddress()+
//	            			", name="+intf.getName());
                    if (inetAddress.isSiteLocalAddress() && (inetAddress instanceof Inet4Address)) {
                        result = inetAddress.getHostAddress();
//	                    Log.v("","result="+result+", name="+intf.getName()+"-");
                        if (intf.getName().equals("wlan0")) {
                            exit = true;
                            break;
                        }
                    }
                }
                if (exit) break;
            }
        } catch (SocketException ex) {
            Log.e(APPLICATION_TAG, ex.toString());
            result = "192.168.0.1";
        }
//		Log.v("","getLocalIpAddress result="+result);
        if (result.equals("")) result = "192.168.0.1";
        return result;
    }

    ;

    public static String getIfIpAddress() {
        String result = "";
        try {
            for (Enumeration<NetworkInterface> en =
                 NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr =
                     intf.getInetAddresses();
                     enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
//	            	Log.v("","ip="+inetAddress.getHostAddress());
                    if (!inetAddress.isLoopbackAddress() &&
                            (inetAddress.getHostAddress().startsWith("0") ||
                                    inetAddress.getHostAddress().startsWith("1") ||
                                    inetAddress.getHostAddress().startsWith("2"))) {
                        result = inetAddress.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(APPLICATION_TAG, ex.toString());
            result = "192.168.0.1";
        }
//		Log.v("","getIfIpAddress result="+result);
        if (result.equals("")) result = "192.168.0.1";
        return result;
    }

    ;

    static public void setCheckedTextView(final CheckedTextView ctv) {
        ctv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ctv.toggle();
            }
        });
    }

    ;


    @SuppressLint("SdCardPath")
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
//				    		hli.sync_deleted_file=string2Array(l_array[10]);
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

    ;

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

    ;

    @SuppressLint("SdCardPath")
    final public void saveHistoryList(final ArrayList<SyncHistoryItem> hl) {
//		Log.v("","save hist started");
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
//			String cpy_str, del_str, ign_str;
            final ArrayList<SyncHistoryItem> del_list = new ArrayList<SyncHistoryItem>();
            for (int i = 0; i < hl.size(); i++) {
//				Log.v("","i="+i+", n="+hl.get(i).sync_prof);
                if (!hl.get(i).sync_prof.equals("")) {
                    shli = hl.get(i);
                    if (i < max) {
//						cpy_str=array2String(sb_buf,shli.sync_copied_file);
//						del_str=array2String(sb_buf,shli.sync_deleted_file);
//						ign_str=array2String(sb_buf,shli.sync_ignored_file);
                        String lfp = "";
                        if (shli.isLogFileAvailable) lfp = shli.sync_log_file_path;
                        sb_buf.setLength(0);
                        sb_buf.append(shli.sync_date).append("\u0001")
                                .append(shli.sync_time).append("\u0001")
                                .append(shli.sync_elapsed_time).append("\u0001")
                                .append(shli.sync_prof).append("\u0001")
                                .append(shli.sync_status).append("\u0001")
                                .append(shli.sync_test_mode ? "1" : "0").append("\u0001")
                                .append(shli.sync_result_no_of_copied).append("\u0001")
                                .append(shli.sync_result_no_of_deleted).append("\u0001")
                                .append(shli.sync_result_no_of_ignored).append("\u0001")
                                .append(shli.sync_req).append("\u0001")
                                .append(shli.sync_error_text.replaceAll("\n", "\u0002")).append("\u0001")
                                .append(shli.sync_result_no_of_retry).append("\u0001") //retry count
                                .append(" ").append("\u0001") //Dummy
                                .append(" ").append("\u0001") //Dummy
                                .append(lfp).append("\u0001")
                                .append(shli.sync_result_file_path)
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
//		Log.v("","save hist ended");
    }

    ;

    public static boolean isCharging(Context c) {
//        long b_time=System.currentTimeMillis();
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

