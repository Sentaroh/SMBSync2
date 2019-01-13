package com.sentaroh.android.SMBSync2;

import android.content.Context;
import android.os.Environment;

import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.android.Utilities.SafManager;
import com.sentaroh.jcifs.JcifsFile;

import java.io.File;

public class SyncFile {
    private String mFilePath="";
    private File mLocalFile=null;
    private JcifsFile mJcifsFile=null;
    private SafFile mSafFile=null;
    private String mLocalPath="";
    private SafManager mSafMgr=null;
    private static final String SDCARD_PATH_PREFIX="sdcard://";
    private static final String USB_PATH_PREFIX="usb://";
    public SyncFile(Context c, String path) {
        mLocalPath=Environment.getExternalStorageDirectory().toString();
        mFilePath=path;

        mSafMgr=new SafManager(c,false);
        if (mFilePath.startsWith("smb://")) {

        } if (mFilePath.startsWith(mLocalPath)) {

        } if (mFilePath.startsWith(SDCARD_PATH_PREFIX)) {

        } if (mFilePath.startsWith(USB_PATH_PREFIX)) {

        }
    }
}
