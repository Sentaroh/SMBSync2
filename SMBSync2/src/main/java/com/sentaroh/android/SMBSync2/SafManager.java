package com.sentaroh.android.SMBSync2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.sentaroh.android.Utilities.LocalMountPoint;
import com.sentaroh.android.Utilities.SafFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SafManager {
    public static final String SDCARD_UUID_KEY ="removable_tree_uuid_key";

    public static final String USB_UUID_KEY ="usb_tree_uuid_key";

    private static final String APPLICATION_TAG="SafManager";

    private boolean mDebugEnabled=false;
    private Context mContext=null;

    public final static String UNKNOWN_USB_DIRECTORY="/unknown_usb";
    private String usbRootDirectory=UNKNOWN_USB_DIRECTORY;
    private SafFile usbRootSafFile=null;
    private String usbRootUuid=null;

    public final static String UNKNOWN_SDCARD_DIRECTORY="/sdcard_unknown";
    private String sdcardRootDirectory=UNKNOWN_SDCARD_DIRECTORY;
    private SafFile sdcardRootSafFile=null;
    private String sdcardRootUuid=null;

    private String msg_area="";

    public String getMessages() {
        String result=msg_area;
        msg_area="";
        return result;
    }

    public SafManager(Context c, boolean debug) {
        mContext=c;
        setDebugEnabled(debug);
        loadSafFile();
    }

    public void setDebugEnabled(boolean enabled) {
        mDebugEnabled=enabled;
    }

    public boolean isSdcardMounted(){
        boolean result=false;
        if (sdcardRootDirectory.equals(UNKNOWN_SDCARD_DIRECTORY)) result=false;
        else result=true;
        return result;
    }

    public boolean isRootTreeUri(Uri uri) {
        boolean result=false;
        String uuid=getUuidFromUri(uri.toString());
        if (!uuid.startsWith("primary")) {
            if (uri.toString().endsWith("%3A") || uri.toString().endsWith(":")) result=true;
        }
//		Log.v("","uuid="+uuid+", uri="+uri.toString()+", result="+result);
        return result;
    }

    private String getExternalSdcardPath() {
        File[] fl= ContextCompat.getExternalFilesDirs(mContext, null);
        String ld= LocalMountPoint.getExternalStorageDir();
        String esd=UNKNOWN_SDCARD_DIRECTORY;
        if (fl!=null) {
            for(File f:fl) {
                if (f!=null && f.getPath()!=null && !f.getPath().startsWith(ld)) {
                    esd=f.getPath().substring(0, f.getPath().indexOf("/Android/data"));
                    break;
                }
            }
        }
        if (esd.equals(UNKNOWN_SDCARD_DIRECTORY)) {
            if (isFilePathExists("/storage/MicroSD", true)) esd="/storage/MicroSD";
            else if (isFilePathExists("/storage/sdcard1", true)) esd="/storage/sdcard1";
            else if (isFilePathExists("/sdcard1", true)) esd="/sdcard1";
            else if (isFilePathExists("/mnt/extSdCard", true)) esd="/mnt/extSdCard";
            else if (isFilePathExists("/storage/extSdCard", true)) esd="/storage/extSdCard";
        }
        return esd;
    }

    public boolean hasExternalSdcardPath() {
        File[] fl= ContextCompat.getExternalFilesDirs(mContext, null);
        String ld= LocalMountPoint.getExternalStorageDir();
        String esd="";
        if (fl!=null) {
            for(File f:fl) {
                if (f!=null && f.getPath()!=null && !f.getPath().startsWith(ld)) {
                    String path=f.getPath().substring(0, f.getPath().indexOf("/Android/data"));
                    if (isFilePathExists(path, false)) esd=path;
                    break;
                }
            }
        }
        if (esd.equals("")) {
            if (isFilePathExists("/storage/MicroSD", false)) esd="/storage/MicroSD";
            else if (isFilePathExists("/storage/sdcard1", false)) esd="/storage/sdcard1";
            else if (isFilePathExists("/sdcard1", false)) esd="/sdcard1";
            else if (isFilePathExists("/mnt/extSdCard", false)) esd="/mnt/extSdCard";
            else if (isFilePathExists("/storage/extSdCard", false)) esd="/storage/extSdCard";
        }
        return esd.equals("")?false:true;
    }

    private static boolean isFilePathExists(String fp, boolean read) {
        boolean result=false;
        File lf=new File(fp);
        if (read && lf.exists() && lf.canRead()) result=true;
        if (!read && lf.exists() ) result=true;
        return result;
    }

    public void loadSafFile() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String uuid_list=prefs.getString(SDCARD_UUID_KEY, "");
        sdcardRootDirectory=UNKNOWN_SDCARD_DIRECTORY;
        sdcardRootSafFile=null;
        sdcardRootUuid=null;

        if (!uuid_list.equals("")) {
            String[] uuid_array=uuid_list.split(",");
            for(String uuid:uuid_array) {
                SafFile sf= SafFile.fromTreeUri(mContext, Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"));
                if (sf!=null && sf.getName()!=null) {
                    sdcardRootUuid=uuid;
                    String esd=getExternalSdcardPath();
                    if (esd!=null && !esd.equals("")) {
                        File mp=new File(esd);
                        if (mp.exists()) {
                            sdcardRootSafFile=sf;
                            sdcardRootDirectory=esd;
                            break;
                        }
                    }
                }
            }
        }
        uuid_list=prefs.getString(USB_UUID_KEY, "");
        usbRootDirectory=UNKNOWN_USB_DIRECTORY;
        usbRootSafFile=null;
        usbRootUuid=null;
        if (!uuid_list.equals("")) {
            String[] uuid_array=uuid_list.split(",");
            for(String uuid:uuid_array) {
                SafFile sf= SafFile.fromTreeUri(mContext, Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"));
                if (sf!=null && sf.getName()!=null) {
                    usbRootDirectory="/usb/"+uuid;
                    usbRootSafFile=sf;
                    usbRootUuid=uuid;
                    break;
                }
            }
        }
    }

    public SafFile getSdcardRootSafFile() {
        return sdcardRootSafFile;
    }

    public String getSdcardRootPath() {
        return sdcardRootDirectory;
    }

    public SafFile getUsbRootSafFile() {
        return usbRootSafFile;
    }

    public String getUsbRootPath() {
        return usbRootDirectory;
    }


    public void saveSdcardUuidList(String uuid) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String sdcard_uuid_data=prefs.getString(SDCARD_UUID_KEY, uuid);
        if (!sdcard_uuid_data.equals("")) {
            ArrayList<String>sdcard_uuid_list=new ArrayList<String>();
            String[] sdcard_uuid_array=sdcard_uuid_data.split(",");
            for(String item:sdcard_uuid_array) {
                sdcard_uuid_list.add(item);
            }
            if (!sdcard_uuid_list.contains(uuid)) sdcard_uuid_list.add(uuid);
            String sd="", sep="";
            for(String item:sdcard_uuid_list) {
                sd+=sep+item;
                sep=",";
            }
            prefs.edit().putString(SDCARD_UUID_KEY, sd).commit();
        } else {
            prefs.edit().putString(SDCARD_UUID_KEY, uuid).commit();
        }
    }

    public void saveUsbUuidList(String uuid) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String usb_uuid_data=prefs.getString(USB_UUID_KEY, "");
        if (!usb_uuid_data.equals("")) {
            ArrayList<String>usb_uuid_list=new ArrayList<String>();
            String[] usb_uuid_array=usb_uuid_data.split(",");
            for(String item:usb_uuid_array) {
                usb_uuid_list.add(item);
            }
            if (!usb_uuid_list.contains(uuid)) usb_uuid_list.add(uuid);
            String sd="", sep="";
            for(String item:usb_uuid_list) {
                sd+=sep+item;
                sep=",";
            }
            prefs.edit().putString(USB_UUID_KEY, sd).commit();
        } else {
            prefs.edit().putString(USB_UUID_KEY, uuid).commit();
        }
    }

    public static String getUuidFromUri(String uri) {
        String result="";
        try {
            int semicolon = uri.lastIndexOf("%3A");
            if (semicolon>0) result=uri.substring(uri.lastIndexOf("/")+1,semicolon);
            else result=uri.substring(uri.lastIndexOf("/")+1,uri.length()-3);
        } catch(Exception e) {}
//		Log.v("","result="+result);
        return result;
    }

    public static String getFileNameFromPath(String fpath) {
        String result="";
        String[] st=fpath.split("/");
        if (st!=null) {
            if (st[st.length-1]!=null) result=st[st.length-1];
        }
        return result;
    }

    public boolean addSdcardUuid(Uri uri) {
        boolean result=true;
        String uuid=getUuidFromUri(uri.toString());
        if (uuid.length()>0) result=addSdcardUuid(uuid);
        return result;
    }

    public boolean isUsbUuid(String uuid) {
        File usb=new File("/storage/"+uuid);
        boolean exists=usb.exists();
        boolean read=usb.canRead();
        if ((exists && !read) || (!exists)) return true;
        else return false;
    }

    public boolean addSdcardUuid(final String uuid) {
        boolean result=true;
        msg_area="addSdcardUuid uuif="+uuid+"\n";
        List<UriPermission> permissions = mContext.getContentResolver().getPersistedUriPermissions();
        for(UriPermission item:permissions) msg_area+=item.toString()+"\n";
        if (isUsbUuid(uuid)) return result;
        try {
            mContext.getContentResolver().takePersistableUriPermission(
                    Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            saveSdcardUuidList(uuid);
            loadSafFile();
        } catch(Exception e) {
            msg_area+="addSdcardUuid error, uuid="+uuid+", Error="+e.getMessage()+"\n";
            result=false;
        }
        return result;
    }

    public boolean isUsbMounted(){
        boolean result=false;
        if (getUsbRootSafFile()==null) result=false;
        else result=true;
        return result;
    }

    public void addUsbUuid(Uri uri) {
        String uuid=getUuidFromUri(uri.toString());
        if (uuid.length()>0) addUsbUuid(uuid);
    }

    public void addUsbUuid(final String uuid) {
        msg_area="addUsbUuid uuif="+uuid+"\n";
        List<UriPermission> permissions = mContext.getContentResolver().getPersistedUriPermissions();
        for(UriPermission item:permissions) msg_area+=item.toString()+"\n";
        try {
            mContext.getContentResolver().takePersistableUriPermission(
                    Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            saveUsbUuidList(uuid);
            loadSafFile();
        } catch(Exception e) {
            msg_area="addUsbUuid error, uuid="+uuid+", Error="+e.getMessage()+"\n";
        }
    }

    public SafFile createUsbItem(String target_path, boolean isDirectory) {
        return createItem(usbRootSafFile, target_path, isDirectory);
    }

    public SafFile createSdcardItem(String target_path, boolean isDirectory) {
        return createItem(sdcardRootSafFile, target_path, isDirectory);
    }
    private SafFile createItem(SafFile rf, String target_path, boolean isDirectory) {
        Uri parent=null;
        msg_area="createItem target_path="+target_path+", root name="+rf.getName()+", isDirectory="+isDirectory+"\n";
        List<UriPermission> permissions = mContext.getContentResolver().getPersistedUriPermissions();
        for(UriPermission item:permissions) msg_area+=item.toString()+"\n";

        long b_time=System.currentTimeMillis();
        SafFile document=rf;

        String relativePath="";
        if (target_path.startsWith(sdcardRootDirectory)) {
            if (!target_path.equals(sdcardRootDirectory))
                relativePath=target_path.replace(sdcardRootDirectory+"/", "");
        } else {
            if (!target_path.equals(usbRootDirectory))
                relativePath=target_path.replace(usbRootDirectory+"/", "");
        }

        msg_area+="rootUri="+rf.getUri()+", relativePath="+relativePath+"\n";

        if (!relativePath.equals("")) {
            String[] parts = relativePath.split("\\/");
            for (int i = 0; i < parts.length; i++) {
                msg_area+="parts="+parts[i]+"\n";
                if (!parts[i].equals("")) {
                    SafFile nextDocument = document.findFile(parts[i]);
                    msg_area+="findFile="+parts[i]+", result="+nextDocument+"\n";
                    if (nextDocument == null) {
                        if ((i < parts.length - 1) || isDirectory) {
                            String c_dir=parts[i];
                            nextDocument = document.createDirectory(c_dir);
                            msg_area+="Directory was created name="+c_dir+", result="+nextDocument+"\n";
                            msg_area+=document.getMessages();
//                			Log.v("","saf="+document.getMsgArea());
                        } else {
                            nextDocument = document.createFile("", parts[i]);
                            msg_area+="File was created name="+parts[i]+", result="+nextDocument+"\n";
                            msg_area+=document.getMessages();
                        }
                    }
                    parent=document.getUri();
                    document = nextDocument;
                    if (document!=null) document.setParent(parent);
                }
            }
        }
        msg_area+="createItem elapsed="+(System.currentTimeMillis()-b_time)+"\n";
        return document;
    }

    public SafFile findSdcardItem(String target_path) {
        return findItem(sdcardRootSafFile, target_path);
    }

    public SafFile findUsbItem(String target_path) {
        return findItem(usbRootSafFile, target_path);
    }

    private SafFile findItem(SafFile rf, String target_path) {
        Uri parent=null;
        msg_area="findItem target_path="+target_path+", root name="+rf.getName()+"\n";
        List<UriPermission> permissions = mContext.getContentResolver().getPersistedUriPermissions();
        for(UriPermission item:permissions) msg_area+=item.toString()+"\n";

        long b_time=System.currentTimeMillis();
        SafFile document=rf;

        String relativePath="";
        if (target_path.startsWith(sdcardRootDirectory)) {
            if (!target_path.equals(sdcardRootDirectory))
                relativePath=target_path.replace(sdcardRootDirectory+"/", "");
        } else {
            if (!target_path.equals(usbRootDirectory))
                relativePath=target_path.replace(usbRootDirectory+"/", "");
        }

        msg_area+="rootUri="+rf.getUri()+", relativePath="+relativePath+"\n";

        if (!relativePath.equals("")) {
            String[] parts = relativePath.split("\\/");
            for (int i = 0; i < parts.length; i++) {
                msg_area+="parts="+parts[i]+"\n";
                if (!parts[i].equals("")) {
                    SafFile nextDocument = document.findFile(parts[i]);
                    msg_area+="findFile="+parts[i]+", result="+nextDocument+"\n";
                    if (nextDocument != null) {
                        parent=document.getUri();
                        document = nextDocument;
                        if (document!=null) document.setParent(parent);
                    } else {
                        document = null;
                        break;
                    }
                }
            }
        }
        msg_area+="findItem elapsed="+(System.currentTimeMillis()-b_time)+"\n";
        return document;
    };

}

