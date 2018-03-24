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

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.ThreadCtrl;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistItem;

public class ReadSmbFilelist implements Runnable {
    private ThreadCtrl getFLCtrl = null;

    private ArrayList<TreeFilelistItem> remoteFileList;
    private String remoteUrl, remoteDir;

    private NotifyEvent notifyEvent;

    private boolean readDirOnly = false;
    private boolean readSubDirCnt = true;

    private SyncUtil mUtil = null;

    private String mHostName = "", mHostAddr = "", mHostPort = "";

    private Context mContext = null;

    private String mUserName = null, mUserPassword = null;
    private String mSmbProtocol="0";

    public ReadSmbFilelist(Context c, ThreadCtrl ac, String ru, String rd,
                           ArrayList<TreeFilelistItem> fl, String user, String pass, String smb_proto,
                           NotifyEvent ne, boolean dironly, boolean dc, GlobalParameters gp) {
        mContext = c;
        mUtil = new SyncUtil(mContext, "FileList", gp);
        remoteFileList = fl;
        remoteUrl = ru;
        remoteDir = rd;
        getFLCtrl = ac; //new ThreadCtrl();
        notifyEvent = ne;

        mSmbProtocol=smb_proto;

        readDirOnly = dironly;
        readSubDirCnt = dc;

        String t_host1 = ru.replace("smb://", "");
        String t_host11 = t_host1;
        if (t_host1.indexOf("/") >= 0) t_host11 = t_host1.substring(0, t_host1.indexOf("/"));
        String t_host2 = t_host11;
        mHostPort = "";
        if (t_host11.indexOf(":") >= 0) {
            t_host2 = t_host11.substring(0, t_host11.indexOf(":"));
            mHostPort = t_host11.replace(t_host2 + ":", "");
        }
        if (SmbUtil.isValidIpAddress(t_host2)) {
            mHostAddr = t_host2;
        } else {
            mHostName = t_host2;
        }
        mUtil.addDebugMsg(1, "I", "ReadSmbFilelist init. name=" + mHostName +
                ", addr=" + mHostAddr + ", port=" + mHostPort + ", remoteUrl=" + remoteUrl + ", Dir=" + remoteDir+", user="+user+", smb_proto="+smb_proto);

        if (user.length() != 0) mUserName = user;
        if (pass.length() != 0) mUserPassword = pass;

    }

    @Override
    public void run() {
        defaultUEH = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(unCaughtExceptionHandler);

        getFLCtrl.setThreadResultSuccess();
        getFLCtrl.setThreadMessage("");

        mUtil.addDebugMsg(1, "I", "ReadSmbFilelist started, readSubDirCnt=" + readSubDirCnt + ", readDirOnly=" + readDirOnly);

        boolean error_exit = false;
        if (mHostName.equals("")) {
            if (mHostPort.equals("")) {
                if (!SyncUtil.isSmbHostAddressConnected(mHostAddr)) {
                    error_exit = true;
                    if (getFLCtrl.isEnabled()) {
                        getFLCtrl.setThreadResultError();
                        getFLCtrl.setThreadMessage(
                                String.format(mContext.getString(R.string.msgs_mirror_smb_addr_not_connected), mHostAddr));
                    } else {
                        getFLCtrl.setThreadResultCancelled();
                    }
                }
            } else {
                if (!SyncUtil.isSmbHostAddressConnected(mHostAddr,
                        Integer.parseInt(mHostPort))) {
                    error_exit = true;
                    if (getFLCtrl.isEnabled()) {
                        getFLCtrl.setThreadResultError();
                        getFLCtrl.setThreadMessage(
                                String.format(mContext.getString(R.string.msgs_mirror_smb_addr_not_connected_with_port),
                                        mHostAddr, mHostPort));
                    } else {
                        getFLCtrl.setThreadResultCancelled();
                    }
                }
            }
        } else {
            if (SmbUtil.getSmbHostIpAddressFromName(mHostName) == null) {
                error_exit = true;
                if (getFLCtrl.isEnabled()) {
                    getFLCtrl.setThreadResultError();
                    getFLCtrl.setThreadMessage(
                            mContext.getString(R.string.msgs_mirror_smb_name_not_found) + mHostName);
                } else {
                    getFLCtrl.setThreadResultCancelled();
                }
            }
        }
        if (!error_exit) {
            if (remoteDir != null) readFileList();
            else readShareList();
        }

        mUtil.addDebugMsg(1, "I", "ReadSmbFilelist ended.");
        getFLCtrl.setDisabled();
        notifyEvent.notifyToListener(true, null);
    }

    private void readFileList() {
        remoteFileList.clear();
        BaseContext bc=SyncUtil.buildBaseContextWithSmbProtocol(mSmbProtocol);
        NtlmPasswordAuthentication creds = new NtlmPasswordAuthentication(bc, "", mUserName, mUserPassword);
        CIFSContext ct= bc.withCredentials(creds);

        try {
            SmbFile remoteFile = new SmbFile(remoteUrl + remoteDir, ct);
            SmbFile[] fl = remoteFile.listFiles();

            for (int i = 0; i < fl.length; i++) {
                String fn = fl[i].getName();
                if (fn.endsWith("/")) fn = fn.substring(0, fn.length() - 1);
                if (getFLCtrl.isEnabled()) {
                    int dirct = 0;
                    String fp = fl[i].getPath();
                    if (fp.endsWith("/")) fp = fp.substring(0, fp.lastIndexOf("/"));
                    fp = fp.substring(remoteUrl.length() + 1, fp.length());
                    if (fp.lastIndexOf("/") > 0) {
                        fp = "/" + fp.substring(0, fp.lastIndexOf("/") + 1);
                    } else fp = "/";
//					Log.v("","name="+fl[i].getPath());
                    if (fl[i].isDirectory() &&
                            !fn.equals("System Volume Information") &&
                            fl[i].canRead()) {
                        if (readSubDirCnt) {
                            SmbFile tdf = new SmbFile(fl[i].getPath(), ct);
                            SmbFile[] tfl = null;
                            try {
                                tfl = tdf.listFiles();
                                if (readDirOnly) {
                                    for (int j = 0; j < tfl.length; j++)
                                        if (tfl[j].isDirectory()) dirct++;
                                } else {
                                    dirct = tfl.length;
                                }
                            } catch (SmbException e) {
                            }
                        }
                        TreeFilelistItem fi = new TreeFilelistItem(
                                fn,
                                "",
                                fl[i].isDirectory(),
                                fl[i].length(),
                                fl[i].lastModified(),
                                false,
                                fl[i].canRead(),
                                fl[i].canWrite(),
                                fl[i].isHidden(),
                                fp, 0);
                        fi.setSubDirItemCount(dirct);
                        if (readDirOnly) {
                            if (fi.isDir()) {
                                remoteFileList.add(fi);
                                mUtil.addDebugMsg(2, "I", "filelist added :" + fn + ",isDir=" +
                                        fl[i].isDirectory() + ", canRead=" + fl[i].canRead() +
                                        ", canWrite=" + fl[i].canWrite() + ",fp=" + fp + ", dircnt=" + dirct);
                            }
                        } else {
                            remoteFileList.add(fi);
                            mUtil.addDebugMsg(2, "I", "filelist added :" + fn + ",isDir=" +
                                    fl[i].isDirectory() + ", canRead=" + fl[i].canRead() +
                                    ", canWrite=" + fl[i].canWrite() + ",fp=" + fp + ", dircnt=" + dirct);
                        }
                    } else {
                        mUtil.addDebugMsg(2, "I", "filelist ignored :" + fn + ",isDir=" +
                                fl[i].isDirectory() + ", canRead=" + fl[i].canRead() +
                                ", canWrite=" + fl[i].canWrite() + ",fp=" + fp + ", dircnt=" + dirct);
                        mUtil.addDebugMsg(2, "I", "filelist ignored :" + fn);
                    }
                } else {
                    getFLCtrl.setThreadResultCancelled();
                    mUtil.addDebugMsg(1, "W", "File list creation cancelled by main task.");
                    break;
                }
            }

        } catch (SmbException e) {
            e.printStackTrace();
            mUtil.addDebugMsg(1, "E", e.toString());
            if (getFLCtrl.isEnabled()) {
                getFLCtrl.setThreadResultError();
                String[] e_msg = SmbUtil.analyzeNtStatusCode(e, mContext,
                        remoteUrl + remoteDir, mUserName);
                getFLCtrl.setThreadMessage(e_msg[0]);
                getFLCtrl.setDisabled();
            } else {
                getFLCtrl.setThreadResultCancelled();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            mUtil.addDebugMsg(1, "E", e.toString());
            if (getFLCtrl.isEnabled()) {
                getFLCtrl.setThreadResultError();
                getFLCtrl.setThreadMessage(e.getMessage());
                getFLCtrl.setDisabled();
            } else {
                getFLCtrl.setThreadResultCancelled();
            }
        }
    }

    ;

    private void readShareList() {
        remoteFileList.clear();
        BaseContext bc=SyncUtil.buildBaseContextWithSmbProtocol(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SYSTEM);
        NtlmPasswordAuthentication creds = new NtlmPasswordAuthentication(bc, "", mUserName, mUserPassword);
        CIFSContext ct = bc.withCredentials(creds);

        try {
            SmbFile[] fl=null;
            SmbException last_smb_exception=null;
            try {
                SmbFile remoteFile = new SmbFile(remoteUrl, ct);
                fl = remoteFile.listFiles();
                for(SmbFile item:fl) Log.v("","fn="+item.getName());
                mUtil.addDebugMsg(1, "W", "Read Share list with user provided auth info.");
            } catch (SmbException e) {
                last_smb_exception=e;
            }

            if (fl==null) {
                mUtil.addDebugMsg(1, "E", last_smb_exception.toString());
                getFLCtrl.setThreadResultError();
                String[] e_msg = SmbUtil.analyzeNtStatusCode(last_smb_exception, mContext,
                        remoteUrl + remoteDir, "");
                getFLCtrl.setThreadMessage(e_msg[0]);
                getFLCtrl.setDisabled();
                return;
            }
            for (SmbFile item:fl) {
                String fn = item.getName().substring(0,item.getName().length()-1);
                String fp = item.getPath().substring(0,item.getPath().length()-1);
                if (getFLCtrl.isEnabled()) {
                    if (!fn.endsWith("$")) {
                        TreeFilelistItem fi = new TreeFilelistItem(
                                fn,
                                "",
                                true,//fl[i].isDirectory(),
                                0,//fl[i].length(),
                                0,//fl[i].lastModified(),
                                false,
                                true,//fl[i].canRead(),
                                false,//fl[i].canWrite(),
                                false,//fl[i].isHidden(),
                                fp, 0);
                        remoteFileList.add(fi);
                        mUtil.addDebugMsg(2, "I", "filelist added :" + fn);
                    }

                } else {
                    getFLCtrl.setThreadResultCancelled();
                    mUtil.addDebugMsg(1, "W", "File list creation cancelled by main task.");
                    break;
                }
            }
        } catch (MalformedURLException e) {
            mUtil.addDebugMsg(1, "E", e.toString());
            if (getFLCtrl.isEnabled()) {
                getFLCtrl.setThreadResultError();
                getFLCtrl.setThreadMessage(e.getMessage());
                getFLCtrl.setDisabled();
            } else {
                getFLCtrl.setThreadResultCancelled();
            }
        }
    }

    ;

    // Default uncaught exception handler variable
    private UncaughtExceptionHandler defaultUEH;

    // handler listener
    private Thread.UncaughtExceptionHandler unCaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Thread.currentThread().setUncaughtExceptionHandler(defaultUEH);
                    ex.printStackTrace();
                    StackTraceElement[] st = ex.getStackTrace();
                    String st_msg = "";
                    for (int i = 0; i < st.length; i++) {
                        st_msg += "\n at " + st[i].getClassName() + "." +
                                st[i].getMethodName() + "(" + st[i].getFileName() +
                                ":" + st[i].getLineNumber() + ")";
                    }
                    getFLCtrl.setThreadResultError();
                    String end_msg = ex.toString() + st_msg;
                    getFLCtrl.setThreadMessage(end_msg);
                    getFLCtrl.setDisabled();
                    notifyEvent.notifyToListener(true, null);
                    // re-throw critical exception further to the os (important)
//                defaultUEH.uncaughtException(thread, ex);
                }
            };

}