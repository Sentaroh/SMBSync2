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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.storage.StorageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckedTextView;
import android.widget.Spinner;

import com.sentaroh.android.SMBSync2.Log.LogUtil;
import com.sentaroh.android.Utilities.StringUtil;
import com.sentaroh.android.Utilities.SystemInfo;
import com.sentaroh.jcifs.JcifsUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;

import static com.sentaroh.android.SMBSync2.Constants.APPLICATION_TAG;
import static com.sentaroh.android.SMBSync2.Constants.DEFAULT_PREFS_FILENAME;

public final class CommonUtilities {
    private Context mContext = null;
    private LogUtil mLog = null;
    private GlobalParameters mGp = null;
    private String mLogIdent = "";

    public CommonUtilities(Context c, String li, GlobalParameters gp) {
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

    final static public SharedPreferences getPrefMgr(Context c) {
        return c.getSharedPreferences(DEFAULT_PREFS_FILENAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
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

    static public ArrayList<String> listSystemInfo(GlobalParameters mGp) {
        ArrayList<String> out=SystemInfo.listSystemInfo(mGp.appContext, mGp.safMgr);
        if (Build.VERSION.SDK_INT>=27) {
            out.add("setSettingGrantCoarseLocationRequired="+mGp.settingGrantCoarseLocationRequired);
            out.add("ACCESS_COARSE_LOCATION Permission="+mGp.appContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
        }
//        ConnectivityManager cm =(ConnectivityManager)mGp.appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        String ssid=activeNetwork.getExtraInfo();
//        out.add("SSID="+ssid);
//        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
//        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if (Build.VERSION.SDK_INT>=28) {
//            if (mGp.appContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
//            }
            out.add("LocationService enabled="+isLocationServiceEnabled(mGp));
        }
        return out;
    }

    final public static boolean isLocationServiceEnabled(GlobalParameters mGp) {
        if (Build.VERSION.SDK_INT>=28) {
            LocationManager lm= (LocationManager)mGp.appContext.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
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
        addDebugMsg(2, "I", "getConnectedWifiSsid WifiEnabled=" + mWifi.isWifiEnabled() +
                ", SSID=" + ssid + ", result=" + ret);
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
                    if (inetAddress.isSiteLocalAddress() && (inetAddress instanceof Inet4Address)) {
                        result = inetAddress.getHostAddress();
                        if (intf.getName().equals(if_name)) {
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

