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

import android.content.Context;

import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.ThreadCtrl;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistItem;
import com.sentaroh.jcifs.JcifsAuth;
import com.sentaroh.jcifs.JcifsException;
import com.sentaroh.jcifs.JcifsFile;
import com.sentaroh.jcifs.JcifsUtil;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.ArrayList;

public class ReadSmbFilelist implements Runnable {
    private ThreadCtrl getFLCtrl = null;
    private GlobalParameters mGp=null;

    private ArrayList<TreeFilelistItem> remoteFileList;
    private String remoteUrl, remoteDir, remoteHostName, remoteHostAddr, remoteHostshare, remoteHostPort;

    private NotifyEvent notifyEvent;

    private boolean readDirOnly = false;
    private boolean readSubDirCnt = true;

    private CommonUtilities mUtil = null;

//    private String mHostName = "", mHostAddr = "", mHostPort = "";

    private Context mContext = null;

    private RemoteAuthInfo mRemoteAuthInfo=null;
    private String mRemoteUserNameForLog="";

    private int mSmbLevel =JcifsAuth.JCIFS_FILE_SMB1;

    public ReadSmbFilelist(Context c, ThreadCtrl ac, String host_name, String host_addr, String share, String host_port,
                           String rd,
                           ArrayList<TreeFilelistItem> fl, RemoteAuthInfo rauth,
                           NotifyEvent ne, boolean dironly, boolean dc, GlobalParameters gp) {
        mContext = c;
        mUtil = new CommonUtilities(mContext, "FileList", gp, null);
        remoteFileList = fl;
        remoteHostAddr=host_addr;
        remoteHostName=host_name;
        remoteHostshare=share;
        remoteHostPort=host_port;
        remoteDir = rd;
        getFLCtrl = ac; //new ThreadCtrl();
        notifyEvent = ne;
        mGp=gp;

        mRemoteAuthInfo=rauth;

        readDirOnly = dironly;
        readSubDirCnt = dc;

        if (rauth.smb_user_name!=null) mRemoteUserNameForLog=(rauth.smb_user_name.equals(""))?"":"????????";
        else mRemoteUserNameForLog=null;
        mSmbLevel = Integer.parseInt(rauth.smb_smb_protocol);

        mUtil.addDebugMsg(1, "I", "ReadSmbFilelist Hostname=" + remoteHostName + ", Hostaddr="+remoteHostAddr+", HostShare="+remoteHostshare+", Port="+remoteHostPort,
                ", Dir=" +remoteDir+", user="+mRemoteUserNameForLog+", smb_proto="+mSmbLevel);

    }

    @Override
    public void run() {
        defaultUEH = Thread.currentThread().getUncaughtExceptionHandler();
        Thread.currentThread().setUncaughtExceptionHandler(unCaughtExceptionHandler);

        getFLCtrl.setThreadResultSuccess();
        getFLCtrl.setThreadMessage("");

        mUtil.addDebugMsg(1, "I", "ReadSmbFilelist started, readSubDirCnt=" + readSubDirCnt + ", readDirOnly=" + readDirOnly);

        boolean error_exit = false;
        String smb_addr=null;
        if (remoteHostName.equals("")) {
            if (!CommonUtilities.isSmbHostAddressConnected(remoteHostAddr)) {
                error_exit = true;
                if (getFLCtrl.isEnabled()) {
                    getFLCtrl.setThreadResultError();
                    getFLCtrl.setThreadMessage(String.format(mContext.getString(R.string.msgs_mirror_smb_addr_not_connected), remoteHostAddr));
                } else {
                    getFLCtrl.setThreadResultCancelled();
                }
            } else {
                smb_addr=remoteHostAddr;
            }
        } else {
            smb_addr=CommonUtilities.resolveHostName(mGp, mUtil, mSmbLevel, remoteHostName);
            if (smb_addr == null) {
                error_exit = true;
                if (getFLCtrl.isEnabled()) {
                    getFLCtrl.setThreadResultError();
                    getFLCtrl.setThreadMessage(
                            mContext.getString(R.string.msgs_mirror_smb_name_not_found) + remoteHostName);
                } else {
                    getFLCtrl.setThreadResultCancelled();
                }
            }
        }
        if (!error_exit) {
            buildRemoteUrl();
            if (remoteDir != null) readFileList();
            else readShareList();
        }

        mUtil.addDebugMsg(1, "I", "ReadSmbFilelist ended.");
//        getFLCtrl.setDisabled();
        notifyEvent.notifyToListener(true, null);
    }

    private void readFileList() {
        remoteFileList.clear();
        JcifsAuth auth=null;
        int smb_level=Integer.parseInt(mRemoteAuthInfo.smb_smb_protocol);
        if (mRemoteAuthInfo.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1)) {
            auth=new JcifsAuth(JcifsAuth.JCIFS_FILE_SMB1, mRemoteAuthInfo.smb_domain_name, mRemoteAuthInfo.smb_user_name, mRemoteAuthInfo.smb_user_password);
        } else {
            auth=new JcifsAuth(smb_level, mRemoteAuthInfo.smb_domain_name, mRemoteAuthInfo.smb_user_name, mRemoteAuthInfo.smb_user_password,
                    mRemoteAuthInfo.smb_ipc_signing_enforced, mRemoteAuthInfo.smb_use_smb2_negotiation);
        }

        try {
//            JcifsFile remoteFile = null;
//            JcifsFile t_rf = new JcifsFile(remoteUrl + remoteDir+"/", auth);
//            if (t_rf.exists()) remoteFile=t_rf;
//            else remoteFile=new JcifsFile(remoteUrl, auth);
            JcifsFile remoteFile = new JcifsFile(remoteUrl + remoteDir, auth);
            JcifsFile[] fl = remoteFile.listFiles();

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
                    try {
                        if (fl[i].isDirectory() &&
                                !fn.equals("System Volume Information") &&
                                fl[i].canRead()) {
                            if (readSubDirCnt) {
                                JcifsFile tdf = new JcifsFile(fl[i].getPath(), auth);
                                JcifsFile[] tfl = null;
                                try {
                                    tfl = tdf.listFiles();
                                    if (readDirOnly) {
                                        for (int j = 0; j < tfl.length; j++) {
//                                            Log.v("","name="+tfl[j].getPath());
                                            if (tfl[j].isDirectory()) dirct++;
                                        }
                                    } else {
                                        dirct = tfl.length;
                                    }
                                    TreeFilelistItem fi = new TreeFilelistItem(
                                            fn,
                                            "",
                                            fl[i].isDirectory(),
                                            fl[i].length(),
                                            fl[i].getLastModified(),
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
                                } catch (JcifsException e) {
                                }
                            }
                        } else {
                            mUtil.addDebugMsg(2, "I", "filelist ignored :" + fn + ",isDir=" +
                                    fl[i].isDirectory() + ", canRead=" + fl[i].canRead() +
                                    ", canWrite=" + fl[i].canWrite() + ",fp=" + fp + ", dircnt=" + dirct);
                            mUtil.addDebugMsg(2, "I", "filelist ignored :" + fn);
                        }

                    } catch (JcifsException e) {
                        e.printStackTrace();
                    }
                } else {
                    getFLCtrl.setThreadResultCancelled();
                    mUtil.addDebugMsg(1, "W", "File list creation cancelled by main task.");
                    break;
                }
            }

        } catch (JcifsException e) {
            e.printStackTrace();
            String cause="";
            String[] e_msg=JcifsUtil.analyzeNtStatusCode(e, remoteUrl + remoteDir, mRemoteUserNameForLog);
//            e_msg[0] = e.getMessage()+"\n"+e_msg[0];
            if (e.getCause()!=null) {
                String tc=e.getCause().toString();
                cause=tc.substring(tc.indexOf(":")+1);
                mUtil.addDebugMsg(1, "E", cause.substring(cause.indexOf(":")+1));
                e_msg[0]=cause+"\n"+e_msg[0];
            }
            mUtil.addDebugMsg(1, "E", e.toString());
            getFLCtrl.setThreadMessage(e_msg[0]);
            if (getFLCtrl.isEnabled()) {
                getFLCtrl.setThreadResultError();
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

    private void buildRemoteUrl() {
        String t_addr="";
        if (remoteHostName.equals("")) {
            t_addr=remoteHostAddr;
        } else {
            t_addr=CommonUtilities.resolveHostName(mGp, mUtil, mSmbLevel, remoteHostName);
        }
        String resolved_addr=CommonUtilities.addScopeidToIpv6Address(t_addr);
        String t_share="";
        if (remoteDir!=null) {
            if (remoteDir.equals("")) t_share=remoteHostshare+"/";
            else t_share=remoteHostshare;
        }
        if (remoteHostPort.equals("")) {
            if (resolved_addr.contains(":")) remoteUrl="smb://"+"["+resolved_addr+"]"+"/"+t_share;
            else remoteUrl="smb://"+resolved_addr+"/"+t_share;
        } else {
            if (resolved_addr.contains(":")) remoteUrl="smb://"+"["+resolved_addr+"]"+":"+remoteHostPort+"/"+t_share;
            else remoteUrl="smb://"+resolved_addr+":"+remoteHostPort+"/"+t_share;
        }
        mUtil.addDebugMsg(1, "I", "buildRemoteUrl result="+remoteUrl);
    }

    private void readShareList() {
        remoteFileList.clear();
        JcifsAuth auth=null;
        int smb_level = Integer.parseInt(mRemoteAuthInfo.smb_smb_protocol);
        if (mRemoteAuthInfo.smb_smb_protocol.equals(SyncTaskItem.SYNC_FOLDER_SMB_PROTOCOL_SMB1)) {
            auth=new JcifsAuth(JcifsAuth.JCIFS_FILE_SMB1, mRemoteAuthInfo.smb_domain_name, mRemoteAuthInfo.smb_user_name, mRemoteAuthInfo.smb_user_password);
        } else {
            auth=new JcifsAuth(smb_level, mRemoteAuthInfo.smb_domain_name, mRemoteAuthInfo.smb_user_name, mRemoteAuthInfo.smb_user_password,
                    mRemoteAuthInfo.smb_ipc_signing_enforced, mRemoteAuthInfo.smb_use_smb2_negotiation);
        }
        JcifsFile[] fl=null;
        try {
            JcifsFile remoteFile = new JcifsFile(remoteUrl, auth);
            fl = remoteFile.listFiles();
        } catch (JcifsException e) {
            e.printStackTrace();
            String cause="";
            if (e.getCause()!=null) {
                cause=e.getCause().toString();
                mUtil.addDebugMsg(1, "E", cause.substring(cause.indexOf(":")+1));
            }
            mUtil.addDebugMsg(1, "E", e.toString());
            getFLCtrl.setThreadResultError();
            String[] e_msg = JcifsUtil.analyzeNtStatusCode(e, remoteUrl, mRemoteUserNameForLog);
            if (!cause.equals("")) getFLCtrl.setThreadMessage(cause.substring(cause.indexOf(":")+1)+"\n"+e_msg[0]);
            else getFLCtrl.setThreadMessage(e_msg[0]);

            getFLCtrl.setDisabled();
            return;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (fl!=null) {
            for (JcifsFile item:fl) {
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
        } else {
            mUtil.addDebugMsg(1, "W", "Share name can not be found.");
        }
    }

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