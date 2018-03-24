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

import android.os.SystemClock;
import android.util.Log;

import static com.sentaroh.android.SMBSync2.Constants.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

import com.sentaroh.android.SMBSync2.SyncThread.SyncThreadWorkArea;
import com.sentaroh.android.Utilities.SafFile;

import jcifs.smb.SmbFile;

public class SyncThreadSyncFile {

//	static final private int syncDeleteSmbToExternal(SyncThreadCommonArea stwa,   
//			SyncTaskItem sti, String master_dir, String target_dir) {
//		File tf=new File(target_dir);
//		Collections.sort(stwa.smbFileList);
//		int sr=syncDeleteSmbToExternal(stwa,   
//				sti, master_dir, master_dir, target_dir, target_dir, tf, stwa.smbFileList);
//		stwa.smbFileList=new ArrayList<String>();
//		return sr;
//	};

    static final private int syncDeleteSmbToExternal(SyncThreadWorkArea stwa,
                                                     SyncTaskItem sti, String from_base, String master_dir, String to_base, String target_dir,
                                                     File tf, ArrayList<String> smb_fl) {
        int sync_result = 0;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
        try {
            String tmp_target_dir = target_dir.substring(to_base.length());
            if (tmp_target_dir.startsWith("/")) tmp_target_dir = tmp_target_dir.substring(1);
            if (tf.isDirectory()) { // Directory Delete
                if (//!SyncThread.isDirectoryExcluded(stwa, tmp_target_dir) &&
                        !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                                SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                    if (isSmbFileExitst(stwa, smb_fl, master_dir)) {
                        File[] children = tf.listFiles();
                        if (children != null) {
                            for (File element : children) {
                                String tmp = element.getName();
                                if (element.isFile() || (element.isDirectory() && sti.isSyncSubDirectory())) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isFile()) {
                                            sync_result = syncDeleteSmbToExternal(stwa, sti, from_base, master_dir + tmp,
                                                    to_base, target_dir + "/" + tmp, element, smb_fl);
                                        } else {
                                            sync_result = syncDeleteSmbToExternal(stwa, sti, from_base, master_dir + tmp ,
                                                    to_base, target_dir + "/" + tmp, element, smb_fl);
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                            stwa.syncTaskRetryCount--;
                                            if (stwa.syncTaskRetryCount > 0)
                                                sync_result = waitRetryInterval(stwa);
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                break;
                                        } else {
                                            stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                            break;
                                        }
                                    }
                                    if (sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) break;
                                } else {
                                    stwa.util.addDebugMsg(2, "I", "sub directory ignored by option, dir=" + master_dir + "/" + tmp);
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!stwa.gp.syncThreadControl.isEnabled())
                                        sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            stwa.util.addLogMsg("W", "File/Directory was not found, fp=" + tf.getPath());
//							sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        }
                    } else {
                        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR, target_dir)) {
                            SyncThread.deleteExternalStorageItem(stwa, true, sti, target_dir);
                        } else {
                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                    stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                        }
                    }
                }
            } else { // file Delete
//				String parent_dir="", t_dir=tmp_target_dir.length()>0?tmp_target_dir.substring(1):"";
//				if (t_dir.lastIndexOf("/")>=0) parent_dir="/"+t_dir.substring(0, t_dir.lastIndexOf("/"));
//				Log.v("","parent="+parent_dir+", t="+t_dir+", tmp="+tmp_target_dir);
                if (//!SyncThread.isDirectoryExcluded(stwa, parent_dir) &&
                        !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                                SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                    if (!SyncThread.isHiddenFile(stwa, sti, tf)) {
                        if (!isSmbFileExitst(stwa, smb_fl, master_dir)) {
                            if (!(tmp_target_dir.equals("") && !sti.isSyncProcessRootDirFile())) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE, target_dir)) {
                                    SyncThread.deleteExternalStorageItem(stwa, true, sti, target_dir);
                                } else {
                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                            stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " master=" + master_dir + ", target=" + target_dir);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    ;

//	static final private int syncDeleteSmbToInternal(SyncThreadCommonArea stwa, SyncTaskItem sti,
//			String master_dir, String target_dir) {
//		Collections.sort(stwa.smbFileList);
//		File tf=new File(target_dir);
//		int sr=syncDeleteSmbToInternal(stwa, sti, master_dir,
//				master_dir, target_dir, target_dir, tf, stwa.smbFileList);
//		stwa.smbFileList=new ArrayList<String>();
//		return sr;
//	};

    static final private int syncDeleteSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_base,
                                                     String master_dir, String to_base, String target_dir, File tf, ArrayList<String> smb_fl) {
        int sync_result = 0;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
        try {
            String tmp_target_dir = target_dir.substring(to_base.length());
            if (tmp_target_dir.startsWith("/")) tmp_target_dir = tmp_target_dir.substring(1);
            if (tf.isDirectory()) { // Directory Delete
                if (//!SyncThread.isDirectoryExcluded(stwa, tmp_target_dir) &&
                        !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                                SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                    if (isSmbFileExitst(stwa, smb_fl, master_dir)) {
                        File[] children = tf.listFiles();
                        if (children != null) {
                            for (File element : children) {
                                String tmp_fname = element.getName();
                                if (element.isFile() || (element.isDirectory() && sti.isSyncSubDirectory())) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isFile()) {
                                            sync_result = syncDeleteSmbToInternal(stwa, sti, from_base, master_dir + tmp_fname,
                                                    to_base, target_dir + "/" + tmp_fname, element, smb_fl);
                                        } else {
                                            sync_result = syncDeleteSmbToInternal(stwa, sti, from_base, master_dir + tmp_fname + "/",
                                                    to_base, target_dir + "/" + tmp_fname, element, smb_fl);
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                            stwa.syncTaskRetryCount--;
                                            if (stwa.syncTaskRetryCount > 0)
                                                sync_result = waitRetryInterval(stwa);
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                break;
                                        } else {
                                            stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                            break;
                                        }
                                    }
                                    if (sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) break;
                                } else {
                                    if (stwa.gp.settingDebugLevel >= 2)
                                        stwa.util.addDebugMsg(2, "I", "sub directory ignored by option, dir=" + master_dir + "/" + tmp_fname);
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!stwa.gp.syncThreadControl.isEnabled())
                                        sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            stwa.util.addLogMsg("W", "File/Directory was not found, fp=" + tf.getPath());
//								sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        }
                    } else {
                        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR, target_dir)) {
                            SyncThread.deleteInternalStorageItem(stwa, true, sti, target_dir);
                        } else {
                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                    stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                        }
                    }
                }
            } else { // file Delete
//				String parent_dir="", t_dir=tmp_target_dir.length()>0?tmp_target_dir.substring(1):"";
//				if (t_dir.lastIndexOf("/")>=0) parent_dir="/"+t_dir.substring(0, t_dir.lastIndexOf("/"));
//				Log.v("","parent="+parent_dir+", t="+t_dir+", tmp="+tmp_target_dir);
                if (//!SyncThread.isDirectoryExcluded(stwa, parent_dir) &&
                        !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                                SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                    if (!SyncThread.isHiddenFile(stwa, sti, tf)) {
                        if (!isSmbFileExitst(stwa, smb_fl, master_dir)) {
                            if (!(tmp_target_dir.equals("") && !sti.isSyncProcessRootDirFile())) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE, target_dir)) {
                                    SyncThread.deleteInternalStorageItem(stwa, true, sti, target_dir);
                                } else {
                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                            stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " master=" + master_dir + ", target=" + target_dir);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    ;

    private static boolean isSmbFileExitst(SyncThreadWorkArea stwa, ArrayList<String> smb_fl, String fp) throws IOException {
        boolean mf_exists = (Collections.binarySearch(stwa.smbFileList, fp) >= 0);
        if (!mf_exists) {
            stwa.util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " file list not found, fp=" + fp);
            SmbFile mf = new SmbFile(fp, stwa.masterCifsContext);
            mf_exists = mf.exists();
        }
        return mf_exists;
    }

    ;

//	static final private int syncDeleteInternalToInternal(SyncThreadCommonArea stwa, SyncTaskItem sti,
//			String master_dir, String target_dir) {
//		File tf=new File(target_dir);
//		return syncDeleteInternalToInternal(stwa, sti, master_dir,
//				master_dir, target_dir, target_dir, tf);
//	};

    static final private int syncDeleteInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_base,
                                                          String master_dir, String to_base, String target_dir, File tf) {
        int sync_result = 0;
        File mf;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
        String tmp_target_dir = target_dir.substring(to_base.length());
        if (tmp_target_dir.startsWith("/")) tmp_target_dir = tmp_target_dir.substring(1);
        if (tf.isDirectory()) { // Directory Delete
            if (//!SyncThread.isDirectoryExcluded(stwa, tmp_target_dir) &&
                    !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)
                    ) {
                mf = new File(master_dir);
                if (mf.exists()) {
                    File[] children = tf.listFiles();
                    if (children != null) {
                        for (File element : children) {
                            String tmp = element.getName();
                            if (element.isFile() || (element.isDirectory() && sti.isSyncSubDirectory())) {
                                sync_result = syncDeleteInternalToInternal(stwa, sti, from_base, master_dir + "/" + tmp,
                                        to_base, target_dir + "/" + tmp, element);
                            } else {
                                stwa.util.addDebugMsg(2, "I", "sub directory ignored by option, dir=" + master_dir + "/" + tmp);
                            }
                            if (!stwa.gp.syncThreadControl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                if (!stwa.gp.syncThreadControl.isEnabled())
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                break;
                            }
                        }
                    } else {
                        stwa.util.addLogMsg("W", "File/Directory was not found, fp=" + tf.getPath());
//						sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                    }
                } else {
                    if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR, target_dir)) {
                        SyncThread.deleteInternalStorageItem(stwa, true, sti, target_dir);
                    } else {
                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                    }
                }
            }
        } else { // file Delete
//			String parent_dir="", t_dir=tmp_target_dir.length()>0?tmp_target_dir.substring(1):"";
//			if (t_dir.lastIndexOf("/")>=0) parent_dir="/"+t_dir.substring(0, t_dir.lastIndexOf("/"));
//			Log.v("","parent="+parent_dir+", t="+t_dir+", tmp="+tmp_target_dir);
            if (//!SyncThread.isDirectoryExcluded(stwa, parent_dir) &&
                    !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)
                    ) {
                if (!SyncThread.isHiddenFile(stwa, sti, tf)) {
                    mf = new File(master_dir);
                    if (!mf.exists()) {
                        if (!(tmp_target_dir.equals("") && !sti.isSyncProcessRootDirFile())) {
                            if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE, target_dir)) {
                                SyncThread.deleteInternalStorageItem(stwa, true, sti, target_dir);
                            } else {
                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                        stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                            }
                        }
                    }
                }
            }
        }
        return sync_result;
    }

    ;

//	static final private int syncDeleteInternalToSmb(SyncThreadCommonArea stwa, SyncTaskItem sti,
//			String master_dir, String target_dir) {
//		SmbFile tf = null;
//		try {
//			tf = new SmbFile(target_dir + "/", stwa.ntlmPasswordAuth);
//		} catch (MalformedURLException e) {
//			stwa.util.addLogMsg("E","","syncDeleteInternalToSmb master="+master_dir+", target="+target_dir);
//			stwa.util.addLogMsg("E","",e.getMessage());//e.toString());
//			SyncThread.printStackTraceElement(stwa, e.getStackTrace());
//			stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
//			return SyncTaskItem.SYNC_STATUS_ERROR;
//		}
//		return syncDeleteInternalToSmb(stwa, sti, master_dir,
//				master_dir, target_dir, target_dir, tf);
//	};

    static final private int syncDeleteInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_base,
                                                     String master_dir, String to_base, String target_dir, SmbFile tf) {
        int sync_result = 0;
        File mf;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
        String tmp_target_dir = target_dir.substring(to_base.length());
        if (tmp_target_dir.startsWith("/")) tmp_target_dir = tmp_target_dir.substring(1);
        try {
            if (tf.isDirectory()) { // Directory Delete
                if (//!SyncThread.isDirectoryExcluded(stwa, tmp_target_dir) &&
                        !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                                SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                    mf = new File(master_dir);
                    if (mf.exists()) {
                        SmbFile[] children = tf.listFiles();
                        if (children != null) {
                            for (SmbFile element : children) {
                                String tmp = element.getName();
                                if (tmp.lastIndexOf("/") > 0)
                                    tmp = tmp.substring(0, tmp.lastIndexOf("/"));
                                if (element.isFile() || (element.isDirectory() && sti.isSyncSubDirectory())) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        sync_result = syncDeleteInternalToSmb(stwa, sti, from_base, master_dir + "/" + tmp,
                                                to_base, target_dir + "/" + tmp, element);
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                            stwa.syncTaskRetryCount--;
                                            if (stwa.syncTaskRetryCount > 0)
                                                sync_result = waitRetryInterval(stwa);
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                break;
                                        } else {
                                            stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                            break;
                                        }
                                    }
                                    if (sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) break;
                                } else {
                                    stwa.util.addDebugMsg(2, "I", "sub directory ignored by option, dir=" + master_dir + "/" + tmp);
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!stwa.gp.syncThreadControl.isEnabled())
                                        sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            stwa.util.addLogMsg("W", "File/Directory was not found, fp=" + tf.getPath());
//							sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        }
                    } else {
                        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR, target_dir)) {
                            SyncThread.deleteSmbItem(stwa, true, sti, to_base, target_dir + "/", stwa.masterCifsContext);
                        } else {
                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                    stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                        }
                    }
                }
            } else { // file Delete
//				String parent_dir="", t_dir=tmp_target_dir.length()>0?tmp_target_dir.substring(1):"";
//				if (t_dir.lastIndexOf("/")>=0) parent_dir="/"+t_dir.substring(0, t_dir.lastIndexOf("/"));
//				Log.v("","parent="+parent_dir+", t="+t_dir+", tmp="+tmp_target_dir);
                if (//!SyncThread.isDirectoryExcluded(stwa, parent_dir) &&
                        !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                                SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)
                        ) {
                    if (!SyncThread.isHiddenFile(stwa, sti, tf)) {
                        mf = new File(master_dir);
                        if (!mf.exists()) {
                            if (!(tmp_target_dir.equals("") && !sti.isSyncProcessRootDirFile())) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE, target_dir)) {
                                    SyncThread.deleteSmbItem(stwa, true, sti, to_base, target_dir, stwa.masterCifsContext);
                                } else {
                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                            stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " master=" + master_dir + ", target=" + target_dir);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    ;

//	static final private int syncDeleteInternalToExternal(SyncThreadCommonArea stwa, SyncTaskItem sti,
//			String master_dir, String target_dir) {
//		File tf = new File(target_dir);
//		return syncDeleteInternalToExternal(stwa, sti, master_dir,
//				master_dir, target_dir, target_dir, tf);
//	};

    static final private int syncDeleteInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_base,
                                                          String master_dir, String to_base, String target_dir, File tf) {
        int sync_result = 0;
        File mf;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
        String tmp_target_dir = target_dir.replace(to_base, "");
        if (tmp_target_dir.startsWith("/")) tmp_target_dir = tmp_target_dir.substring(1);
        if (tf.isDirectory()) { // Directory Delete
            mf = new File(master_dir);
            if (mf.exists()) {
                if (//!SyncThread.isDirectoryExcluded(stwa, tmp_target_dir) &&
                        !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                                SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)
                        ) {
                    File[] children = tf.listFiles();
                    if (children != null) {
                        for (File element : children) {
                            String tmp = element.getName();
                            if (tmp.lastIndexOf("/") > 0)
                                tmp = tmp.substring(0, tmp.lastIndexOf("/"));
                            if (element.isFile() || (element.isDirectory() && sti.isSyncSubDirectory())) {
                                sync_result = syncDeleteInternalToExternal(stwa, sti, from_base, master_dir + "/" + tmp,
                                        to_base, target_dir + "/" + tmp, element);
                            } else {
                                stwa.util.addDebugMsg(2, "I", "sub directory ignored by option, dir=" + master_dir + "/" + tmp);
                            }
                            if (!stwa.gp.syncThreadControl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                if (!stwa.gp.syncThreadControl.isEnabled())
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                break;
                            }
                        }
                    } else {
                        stwa.util.addLogMsg("W", "File/Directory was not found, fp=" + tf.getPath());
//						sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                    }
                }
            } else {
                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR, target_dir)) {
                    SyncThread.deleteExternalStorageItem(stwa, true, sti, target_dir);
                } else {
                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                            stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                }
            }
        } else { // file Delete
//			String parent_dir="", t_dir=tmp_target_dir.length()>0?tmp_target_dir.substring(1):"";
//			if (t_dir.lastIndexOf("/")>=0) parent_dir="/"+t_dir.substring(0, t_dir.lastIndexOf("/"));
//			Log.v("","parent="+parent_dir+", t="+t_dir+", tmp="+tmp_target_dir);
            if (//!SyncThread.isDirectoryExcluded(stwa, parent_dir) &&
                    !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                if (!SyncThread.isHiddenFile(stwa, sti, tf)) {
                    mf = new File(master_dir);
                    if (!mf.exists()) {
                        if (!(tmp_target_dir.equals("") && !sti.isSyncProcessRootDirFile())) {
                            if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE, target_dir)) {
                                SyncThread.deleteExternalStorageItem(stwa, true, sti, target_dir);
                            } else {
                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                        stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                            }
                        }
                    }
                }
            }
        }

        return sync_result;
    }

    ;

    static public int syncMirrorInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = moveCopyInternalToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
            File tf = new File(to_path);
            sync_result = syncDeleteInternalToInternal(stwa, sti, from_path,
                    from_path, to_path, to_path, tf);

        }
        return sync_result;
    }

    ;

    static public int syncCopyInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyInternalToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static public int syncMoveInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyInternalToInternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static private int moveCopyInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                                  String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        File tf;
        try {
            String t_from_path = from_path.substring(from_base.length());
            if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
            if (mf.exists()) {
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToInternalStorage(stwa, sti, to_path);
                        }
                        File[] children = mf.listFiles();
                        if (children != null) {
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
//										Log.v("","from="+from_path);
//										Log.v("","to  ="+to_path);
                                        if (!from_path.equals(to_path)) {
                                            if (element.isFile()) {
                                                sync_result = moveCopyInternalToInternal(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName());
                                            } else {
                                                if (sti.isSyncSubDirectory()) {
                                                    sync_result = moveCopyInternalToInternal(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName());
                                                } else {
                                                    if (stwa.gp.settingDebugLevel >= 1)
                                                        stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                                }
                                            }
                                        } else {
                                            stwa.util.addDebugMsg(1, "W",
                                                    String.format(stwa.gp.appContext.getString(R.string.msgs_mirror_same_directory_ignored),
                                                            from_path + "/" + element.getName()));
                                        }
                                    }
                                } else {
                                    return sync_result;
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled()) {
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            stwa.util.addDebugMsg(1, "I", "Directory was null, dir=" + mf.getPath());
                        }
                    } else {
                        if (!mf.canRead())
                            stwa.util.addDebugMsg(1, "I", "Directory ignored because can not read, fp=" + from_path + "/" + mf.getName());
                    }
                } else { // file copy
                    if (SyncThread.isDirectorySelectedByFileName(stwa, t_from_path) &&
                            !SyncThread.isHiddenFile(stwa, sti, mf) &&
                            SyncThread.isFileSelected(stwa, sti, t_from_path)) {
//						mf = new File(from_path);
                        tf = new File(to_path);
                        boolean tf_exists = tf.exists();
                        if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                            //Ignore override the file
                            if (move_file)
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                            else
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                        } else {
                            if (move_file) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, to_path)) {
                                    if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                        sync_result = SyncThreadCopyFile.copyFileInternalToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            tf.setLastModified(mf.lastModified());
                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                    to_path, tf.lastModified(), mf.lastModified());
                                            SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                            SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                stwa.msgs_mirror_task_file_moved);
                                    }
                                } else {
                                    stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                }
                            } else {
                                if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                    if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, to_path)) {
                                        sync_result = SyncThreadCopyFile.copyFileInternalToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(), tmsg);
                                            tf.setLastModified(mf.lastModified());
                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                    to_path, tf.lastModified(), mf.lastModified());
                                            SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                    }
                                }
                            }
                            if (!stwa.gp.syncThreadControl.isEnabled())
                                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;

                        }
                    }
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    ;

    static public int syncMirrorInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = moveCopyInternalToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
            File tf = new File(to_path);
            sync_result = syncDeleteInternalToExternal(stwa, sti, from_path, from_path, to_path, to_path, tf);
        }
        return sync_result;
    }

    ;

    static public int syncCopyInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyInternalToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static public int syncMoveInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyInternalToExternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static private int moveCopyInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                                  String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
                        }
                        File[] children = mf.listFiles();
                        if (children != null) {
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
                                        if (element.isFile()) {
                                            sync_result = moveCopyInternalToExternal(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                    element, to_base, to_path + "/" + element.getName());
                                        } else {
                                            if (sti.isSyncSubDirectory()) {
                                                sync_result = moveCopyInternalToExternal(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName());
                                            } else {
                                                if (stwa.gp.settingDebugLevel >= 1)
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                            }
                                        }
                                    }
                                } else {
                                    return sync_result;
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled()) {
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            stwa.util.addDebugMsg(1, "I", "Directory was null, dir=" + mf.getPath());
                        }
                    } else {
                        if (!mf.canRead())
                            stwa.util.addDebugMsg(1, "I", "Directory ignored because can not read, fp=" + from_path + "/" + mf.getName());
                    }
                } else { // file copy
                    if (SyncThread.isDirectorySelectedByFileName(stwa, t_from_path) &&
                            !SyncThread.isHiddenFile(stwa, sti, mf) &&
                            SyncThread.isFileSelected(stwa, sti, t_from_path)) {
//						mf = new File(from_path);
                        tf = new File(to_path);
                        boolean tf_exists = tf.exists();
                        if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                            //Ignore override the file
                            if (move_file)
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                            else
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                        } else {
                            if (move_file) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, to_path)) {
                                    if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                        sync_result = SyncThreadCopyFile.copyFileInternalToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            if (!sti.isSyncTestMode()) {
                                                SafFile sf = null;
                                                for (int i = 0; i <= 50; i++) {
                                                    sf = stwa.gp.safMgr.getSafFileBySdcardPath(
                                                            stwa.gp.safMgr.getSdcardSafFile(), to_path, false);
                                                    if (sf == null) {
                                                        SystemClock.sleep(20);
                                                    } else {
                                                        if (i > 0)
                                                            stwa.util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " wait count=" + i);
                                                        break;
                                                    }
                                                }
                                                SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                        to_path, sf.lastModified(), mf.lastModified());
                                                SyncThread.scanMediaFile(stwa, to_path);
                                            }
                                            stwa.totalCopyCount++;
                                            SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                stwa.msgs_mirror_task_file_moved);
                                    }
                                } else {
                                    stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                }
                            } else {
                                if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                    if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, to_path)) {
                                        sync_result = SyncThreadCopyFile.copyFileInternalToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(), tmsg);
                                            if (!sti.isSyncTestMode()) {
                                                SafFile sf = null;
                                                for (int i = 0; i <= 50; i++) {
//												sf= SafUtil.getSafDocumentFileByPath(mSyncThreadCA.safCA, to_path, false);
                                                    sf = stwa.gp.safMgr.getSafFileBySdcardPath(
                                                            stwa.gp.safMgr.getSdcardSafFile(), to_path, false);
                                                    if (sf == null) {
                                                        SystemClock.sleep(20);
                                                    } else {
                                                        if (i > 0)
                                                            stwa.util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " wait count=" + i);
                                                        break;
                                                    }
                                                }
                                                SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                        to_path, sf.lastModified(), mf.lastModified());
                                                SyncThread.scanMediaFile(stwa, to_path);
                                            }
                                            stwa.totalCopyCount++;
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                    }
                                }
                            }
                            if (!stwa.gp.syncThreadControl.isEnabled())
                                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    ;

    static public int syncMirrorInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = moveCopyInternalToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
            SmbFile tf = null;
            try {
                tf = new SmbFile(to_path + "/", stwa.masterCifsContext);
                sync_result = syncDeleteInternalToSmb(stwa, sti, from_path, from_path, to_path, to_path, tf);
            } catch (MalformedURLException e) {
                stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " master=" + from_path + ", target=" + to_path);
                stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
                SyncThread.printStackTraceElement(stwa, e.getStackTrace());
                stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }

        }
        return sync_result;
    }

    ;

    static public int syncCopyInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyInternalToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static public int syncMoveInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyInternalToSmb(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static private int moveCopyInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                             String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        SmbFile tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.masterCifsContext);
                        }
                        File[] children = mf.listFiles();
                        if (children != null) {
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            if (element.isFile()) {
                                                sync_result = moveCopyInternalToSmb(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName());
                                            } else {
                                                if (sti.isSyncSubDirectory()) {
                                                    sync_result = moveCopyInternalToSmb(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName());
                                                } else {
                                                    if (stwa.gp.settingDebugLevel >= 1)
                                                        stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                                }
                                            }
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS)
                                            break;
                                    }
                                } else {
                                    return sync_result;
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled()) {
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            stwa.util.addDebugMsg(1, "I", "Directory was null, dir=" + mf.getPath());
                        }
                    } else {
                        if (!mf.canRead())
                            stwa.util.addDebugMsg(1, "I", "Directory ignored because can not read, fp=" + from_path + "/" + mf.getName());
                    }
                } else { // file copy
                    if (SyncThread.isDirectorySelectedByFileName(stwa, t_from_path) &&
                            !SyncThread.isHiddenFile(stwa, sti, mf) &&
                            SyncThread.isFileSelected(stwa, sti, t_from_path)) {
//						mf = new File(from_path);
                        tf = new SmbFile(to_path, stwa.masterCifsContext);
                        boolean tf_exists = tf.exists();
                        if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                            //Ignore override the file
                            if (move_file)
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                            else
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                        } else {
                            if (move_file) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, to_path)) {
                                    if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, mf, tf, stwa.ALL_COPY)) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            sync_result = SyncThreadCopyFile.copyFileInternalToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                    to_path, tf.lastModified(), mf.lastModified());
//											SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                            SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                stwa.msgs_mirror_task_file_moved);
                                    }
                                } else {
                                    stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                }
                            } else {
                                if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, mf, tf, stwa.ALL_COPY)) {
                                    if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, to_path)) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            sync_result = SyncThreadCopyFile.copyFileInternalToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(), tmsg);
                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                    to_path, tf.lastModified(), mf.lastModified());
//											SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                    }
                                }
                            }
                            if (!stwa.gp.syncThreadControl.isEnabled())
                                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    ;

    static public int syncMirrorExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = moveCopyExternalToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
            File tf = new File(to_path);
            sync_result = syncDeleteInternalToInternal(stwa, sti, from_path,
                    from_path, to_path, to_path, tf);

        }
        return sync_result;
    }

    ;

    static public int syncCopyExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyExternalToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static public int syncMoveExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyExternalToInternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static private int moveCopyExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                                  String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToInternalStorage(stwa, sti, to_path);
                        }
                        File[] children = mf.listFiles();
                        if (children != null) {
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
                                        if (element.isFile()) {
                                            sync_result = moveCopyExternalToInternal(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                    element, to_base, to_path + "/" + element.getName());
                                        } else {
                                            if (sti.isSyncSubDirectory()) {
                                                sync_result = moveCopyExternalToInternal(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName());
                                            } else {
                                                if (stwa.gp.settingDebugLevel >= 1)
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                            }
                                        }
                                    }
                                } else {
                                    return sync_result;
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled()) {
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            stwa.util.addDebugMsg(1, "I", "Directory was null, dir=" + mf.getPath());
                        }
                    } else {
                        if (!mf.canRead())
                            stwa.util.addDebugMsg(1, "I", "Directory ignored because can not read, fp=" + from_path + "/" + mf.getName());
                    }
                } else { // file copy
                    if (SyncThread.isDirectorySelectedByFileName(stwa, t_from_path) &&
                            !SyncThread.isHiddenFile(stwa, sti, mf) &&
                            SyncThread.isFileSelected(stwa, sti, t_from_path)) {
//						mf = new File(from_path);
                        tf = new File(to_path);
                        boolean tf_exists = tf.exists();
                        if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                            //Ignore override the file
                            if (move_file)
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                            else
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                        } else {
                            if (move_file) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, to_path)) {
                                    if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                        sync_result = SyncThreadCopyFile.copyFileExternalToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            tf.setLastModified(mf.lastModified());
                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                    to_path, tf.lastModified(), mf.lastModified());
                                            SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                            SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                stwa.msgs_mirror_task_file_moved);
                                    }
                                } else {
                                    stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                }
                            } else {
                                if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                    if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, to_path)) {
                                        sync_result = SyncThreadCopyFile.copyFileExternalToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(), tmsg);
                                            tf.setLastModified(mf.lastModified());
                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                    to_path, tf.lastModified(), mf.lastModified());
                                            SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                    }
                                }
                            }
                            if (!stwa.gp.syncThreadControl.isEnabled())
                                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    ;

    static public int syncMirrorExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = moveCopyExternalToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
            File tf = new File(to_path);
            sync_result = syncDeleteInternalToExternal(stwa, sti, from_path, from_path, to_path, to_path, tf);
        }
        return sync_result;
    }

    ;

    static public int syncCopyExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyExternalToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static public int syncMoveExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyExternalToExternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static private int moveCopyExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                                  String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
                        }
                        File[] children = mf.listFiles();
                        if (children != null) {
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
//									String tmp = element.getName();
                                    if (!element.getName().equals(".android_secure")) {
//										if (tmp.lastIndexOf("/")>0) tmp=tmp.substring(0,tmp.lastIndexOf("/"));
                                        if (!from_path.equals(to_path)) {
                                            if (element.isFile()) {
                                                sync_result = moveCopyExternalToExternal(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName());
                                            } else {
                                                if (sti.isSyncSubDirectory()) {
                                                    sync_result = moveCopyExternalToExternal(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName());
                                                } else {
                                                    if (stwa.gp.settingDebugLevel >= 1)
                                                        stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                                }
                                            }
                                        } else {
                                            stwa.util.addDebugMsg(1, "W",
                                                    String.format(stwa.gp.appContext.getString(R.string.msgs_mirror_same_directory_ignored),
                                                            from_path + "/" + element.getName()));
                                        }
                                    }
                                } else {
                                    return sync_result;
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled()) {
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            stwa.util.addDebugMsg(1, "I", "Directory was null, dir=" + mf.getPath());
                        }
                    } else {
                        if (!mf.canRead())
                            stwa.util.addDebugMsg(1, "I", "Directory ignored because can not read, fp=" + from_path + "/" + mf.getName());
                    }
                } else { // file copy
                    if (SyncThread.isDirectorySelectedByFileName(stwa, t_from_path) &&
                            !SyncThread.isHiddenFile(stwa, sti, mf) &&
                            SyncThread.isFileSelected(stwa, sti, t_from_path)) {
//						mf = new File(from_path);
                        tf = new File(to_path);
                        boolean tf_exists = tf.exists();
                        if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                            //Ignore override the file
                            if (move_file)
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                            else
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                        } else {
                            if (move_file) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, to_path)) {
                                    if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                        sync_result = SyncThreadCopyFile.copyFileExternalToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            if (!sti.isSyncTestMode()) {
                                                SafFile sf = null;
                                                for (int i = 0; i <= 50; i++) {
                                                    sf = stwa.gp.safMgr.getSafFileBySdcardPath(
                                                            stwa.gp.safMgr.getSdcardSafFile(), to_path, false);
                                                    if (sf == null) {
                                                        SystemClock.sleep(20);
                                                    } else {
                                                        if (i > 0)
                                                            stwa.util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " wait count=" + i);
                                                        break;
                                                    }
                                                }
                                                SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                        to_path, sf.lastModified(), mf.lastModified());
                                                SyncThread.scanMediaFile(stwa, to_path);
                                            }
                                            stwa.totalCopyCount++;
                                            SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                stwa.msgs_mirror_task_file_moved);
                                    }
                                } else {
                                    stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                }
                            } else {
                                if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                    if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, to_path)) {
                                        sync_result = SyncThreadCopyFile.copyFileExternalToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(), tmsg);
                                            if (!sti.isSyncTestMode()) {
                                                SafFile sf = null;
                                                for (int i = 0; i <= 50; i++) {
                                                    sf = stwa.gp.safMgr.getSafFileBySdcardPath(
                                                            stwa.gp.safMgr.getSdcardSafFile(), to_path, false);
                                                    if (sf == null) {
                                                        SystemClock.sleep(20);
                                                    } else {
                                                        if (i > 0)
                                                            stwa.util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " wait count=" + i);
                                                        break;
                                                    }
                                                }
                                                SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                        to_path, sf.lastModified(), mf.lastModified());
                                                SyncThread.scanMediaFile(stwa, to_path);
                                            }
                                            stwa.totalCopyCount++;
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                    }
                                }
                            }
                            if (!stwa.gp.syncThreadControl.isEnabled())
                                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    ;

    static public int syncMirrorExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = moveCopyInternalToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
            SmbFile tf = null;
            try {
                tf = new SmbFile(to_path + "/", stwa.masterCifsContext);
                sync_result = syncDeleteInternalToSmb(stwa, sti, from_path, from_path, to_path, to_path, tf);
            } catch (MalformedURLException e) {
                stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " master=" + from_path + ", target=" + to_path);
                stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
                SyncThread.printStackTraceElement(stwa, e.getStackTrace());
                stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }

        }
        return sync_result;
    }

    ;

    static public int syncCopyExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyExternalToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static public int syncMoveExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyExternalToSmb(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
    }

    ;

    static private int moveCopyExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                             String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        SmbFile tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.masterCifsContext);
                        }
                        File[] children = mf.listFiles();
                        if (children != null) {
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            if (element.isFile()) {
                                                sync_result = moveCopyExternalToSmb(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName());
                                            } else {
                                                if (sti.isSyncSubDirectory()) {
                                                    sync_result = moveCopyExternalToSmb(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName());
                                                } else {
                                                    if (stwa.gp.settingDebugLevel >= 1)
                                                        stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                                }
                                            }
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS)
                                            break;
                                    }
                                } else {
                                    return sync_result;
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled()) {
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            stwa.util.addDebugMsg(1, "I", "Directory was null, dir=" + mf.getPath());
                        }
                    } else {
                        if (!mf.canRead())
                            stwa.util.addDebugMsg(1, "I", "Directory ignored because can not read, fp=" + from_path + "/" + mf.getName());
                    }
                } else { // file copy
                    if (SyncThread.isDirectorySelectedByFileName(stwa, t_from_path) &&
                            !SyncThread.isHiddenFile(stwa, sti, mf) &&
                            SyncThread.isFileSelected(stwa, sti, t_from_path)) {
//						mf = new File(from_path);
                        tf = new SmbFile(to_path, stwa.masterCifsContext);
                        boolean tf_exists = tf.exists();
                        if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                            //Ignore override the file
                            if (move_file)
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                            else
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                        } else {
                            if (move_file) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, to_path)) {
                                    if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, mf, tf, stwa.ALL_COPY)) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            sync_result = SyncThreadCopyFile.copyFileExternalToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                    to_path, tf.lastModified(), mf.lastModified());
//											SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                            SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                stwa.msgs_mirror_task_file_moved);
                                    }
                                } else {
                                    stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                }
                            } else {
                                if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, mf, tf, stwa.ALL_COPY)) {
                                    if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, to_path)) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            sync_result = SyncThreadCopyFile.copyFileExternalToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(), tmsg);
                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                    to_path, tf.lastModified(), mf.lastModified());
//											SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                    }
                                }
                            }
                            if (!stwa.gp.syncThreadControl.isEnabled())
                                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    ;

    static public int syncMirrorSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String from_path, String to_path) {
        SmbFile mf = null;
        try {
            mf = new SmbFile(from_path, stwa.masterCifsContext);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        stwa.smbFileList = new ArrayList<String>();
        int sync_result = moveCopySmbToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path, stwa.smbFileList);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
            Collections.sort(stwa.smbFileList);
            File tf = new File(to_path);
            sync_result = syncDeleteSmbToInternal(stwa, sti,
                    from_path, from_path, to_path, to_path, tf, stwa.smbFileList);
        }
        stwa.smbFileList = null;
        return sync_result;
    }

    ;

    static public int syncCopySmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        SmbFile mf = null;
        try {
            mf = new SmbFile(from_path, stwa.masterCifsContext);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        int sr = moveCopySmbToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path, null);
        return sr;
    }

    ;

    static public int syncMoveSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        SmbFile mf = null;
        try {
            mf = new SmbFile(from_path, stwa.masterCifsContext);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return moveCopySmbToInternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path, null);
    }

    ;

    static private int moveCopySmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                             String from_base, String from_path, SmbFile mf, String to_base, String to_path, ArrayList<String> smb_fl) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToInternalStorage(stwa, sti, to_path);
                        }
                        SmbFile[] children = mf.listFiles();
                        if (children != null) {
                            for (SmbFile element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isFile()) {
                                            sync_result = moveCopySmbToInternal(stwa, sti, move_file, from_base, from_path + element.getName(),
                                                    element, to_base, to_path + "/" + element.getName(), smb_fl);
                                            if (smb_fl != null && sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS)
                                                stwa.smbFileList.add(element.getPath());
                                        } else {
                                            if (sti.isSyncSubDirectory()) {
                                                sync_result = moveCopySmbToInternal(stwa, sti, move_file, from_base, from_path + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName().replace("/", ""), smb_fl);
                                            } else {
                                                if (stwa.gp.settingDebugLevel >= 1)
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                            stwa.syncTaskRetryCount--;
                                            if (stwa.syncTaskRetryCount > 0)
                                                sync_result = waitRetryInterval(stwa);
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                break;
                                        } else {
                                            stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                            break;
                                        }
                                    }
                                    if (smb_fl != null && sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS)
                                        smb_fl.add(element.getPath());
                                    if (sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) break;
                                } else {
                                    return sync_result;
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled()) {
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            if (stwa.gp.settingDebugLevel >= 1)
                                stwa.util.addDebugMsg(1, "I", "Directory was null, dir=" + mf.getPath());
                        }
                    } else {
                        if (!mf.canRead()) {
                            if (stwa.gp.settingDebugLevel >= 1)
                                stwa.util.addDebugMsg(1, "I", "Directory ignored because can not read, fp=" + from_path + "/" + mf.getName());
                        }
                    }
                } else { // file copy
                    if (SyncThread.isDirectorySelectedByFileName(stwa, t_from_path) &&
                            !SyncThread.isHiddenFile(stwa, sti, mf) && SyncThread.isFileSelected(stwa, sti, t_from_path)) {
                        tf = new File(to_path);
                        boolean tf_exists = tf.exists();
                        if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                            //Ignore override the file
                            if (move_file)
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                            else
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                        } else {
                            if (move_file) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, to_path)) {
                                    if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            sync_result = SyncThreadCopyFile.copyFileSmbToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            tf.setLastModified(mf.lastModified());
                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                    to_path, tf.lastModified(), mf.lastModified());
                                            SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                            SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.masterCifsContext);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.masterCifsContext);

                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                stwa.msgs_mirror_task_file_moved);
                                    }
                                } else {
                                    stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                }
                            } else {
                                if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                    if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, to_path)) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            sync_result = SyncThreadCopyFile.copyFileSmbToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(), tmsg);
                                            tf.setLastModified(mf.lastModified());
                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                    to_path, tf.lastModified(), mf.lastModified());
                                            SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                    }
                                }
                            }
                            if (!stwa.gp.syncThreadControl.isEnabled())
                                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    ;

    static public int syncMirrorSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String from_path, String to_path) {
        stwa.smbFileList = new ArrayList<String>();
        SmbFile mf = null;
        try {
            mf = new SmbFile(from_path, stwa.masterCifsContext);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        int sync_result = moveCopySmbToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path, stwa.smbFileList);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
//			sync_result=SyncThreadSyncFile.syncDeleteSmbToExternal(stwa, sti, from_path, to_path);
            File tf = new File(to_path);
            Collections.sort(stwa.smbFileList);
            sync_result = syncDeleteSmbToExternal(stwa, sti, from_path, from_path, to_path, to_path, tf, stwa.smbFileList);
        }
        stwa.smbFileList = null;
        return sync_result;
    }

    ;

    static public int syncCopySmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        SmbFile mf = null;
        try {
            mf = new SmbFile(from_path, stwa.masterCifsContext);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return moveCopySmbToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path, null);
    }

    ;

    static public int syncMoveSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        SmbFile mf = null;
        try {
            mf = new SmbFile(from_path, stwa.masterCifsContext);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return moveCopySmbToExternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path, null);
    }

    ;

    static private int moveCopySmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                             String from_base, String from_path, SmbFile mf, String to_base, String to_path, ArrayList<String> smb_fl) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
                        }
                        SmbFile[] children = mf.listFiles();
                        if (children != null) {
                            for (SmbFile element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isFile()) {
                                            sync_result = moveCopySmbToExternal(stwa, sti, move_file, from_base,
                                                    from_path + element.getName(), element, to_base, to_path + "/" + element.getName(), smb_fl);
                                            if (smb_fl != null && sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS)
                                                smb_fl.add(element.getPath());
                                        } else {
                                            if (sti.isSyncSubDirectory()) {
                                                sync_result = moveCopySmbToExternal(stwa, sti, move_file, from_base, from_path + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName().replace("/", ""), smb_fl);
                                            } else {
                                                if (stwa.gp.settingDebugLevel >= 1)
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                            stwa.syncTaskRetryCount--;
                                            if (stwa.syncTaskRetryCount > 0)
                                                sync_result = waitRetryInterval(stwa);
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                break;
                                        } else {
                                            stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                            break;
                                        }
                                    }
                                    if (smb_fl != null && sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS)
                                        smb_fl.add(element.getPath());
                                    if (sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) break;
                                } else {
                                    return sync_result;
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled()) {
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            stwa.util.addDebugMsg(1, "I", "Directory was null, dir=" + mf.getPath());
                        }
                    } else {
                        if (!mf.canRead())
                            stwa.util.addDebugMsg(1, "I", "Directory ignored because can not read, fp=" + from_path + "/" + mf.getName());
                    }
                } else { // file copy
                    if (SyncThread.isDirectorySelectedByFileName(stwa, t_from_path) &&
                            !SyncThread.isHiddenFile(stwa, sti, mf) && SyncThread.isFileSelected(stwa, sti, t_from_path)) {
                        tf = new File(to_path);
                        boolean tf_exists = tf.exists();
                        if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                            //Ignore override the file
                            if (move_file)
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                            else
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                        } else {
                            if (move_file) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, to_path)) {
                                    if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            sync_result = SyncThreadCopyFile.copyFileSmbToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            if (!sti.isSyncTestMode()) {
                                                SafFile sf = null;
                                                for (int i = 0; i <= 50; i++) {
                                                    sf = stwa.gp.safMgr.getSafFileBySdcardPath(
                                                            stwa.gp.safMgr.getSdcardSafFile(), to_path, false);
                                                    if (sf == null) {
                                                        SystemClock.sleep(20);
                                                    } else {
                                                        if (i > 0)
                                                            stwa.util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " wait count=" + i);
                                                        break;
                                                    }
                                                }
                                                SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                        to_path, sf.lastModified(), mf.lastModified());
                                                SyncThread.scanMediaFile(stwa, to_path);
                                            }
                                            stwa.totalCopyCount++;
                                            SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.masterCifsContext);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.masterCifsContext);
                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                stwa.msgs_mirror_task_file_moved);
                                    }
                                } else {
                                    stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                }
                            } else {
                                if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                    if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, to_path)) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            sync_result = SyncThreadCopyFile.copyFileSmbToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(), tmsg);
                                            if (!sti.isSyncTestMode()) {
                                                SafFile sf = null;
                                                for (int i = 0; i <= 50; i++) {
                                                    sf = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_path, false);
                                                    if (sf == null) {
                                                        SystemClock.sleep(20);
                                                    } else {
                                                        if (i > 0)
                                                            stwa.util.addDebugMsg(1, "I", SyncUtil.getExecutedMethodName() + " wait count=" + i);
                                                        break;
                                                    }
                                                }
                                                SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                        to_path, sf.lastModified(), mf.lastModified());
                                                SyncThread.scanMediaFile(stwa, to_path);
                                            }
                                            stwa.totalCopyCount++;
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                    }
                                }
                            }
                            if (!stwa.gp.syncThreadControl.isEnabled())
                                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    ;

    static public int syncMirrorSmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                         String from_path, String to_path) {
        SmbFile mf = null;
        try {
            mf = new SmbFile(from_path, stwa.masterCifsContext);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() +
                    " From=" + from_path + ", Master file error");
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        stwa.smbFileList = new ArrayList<String>();
        int sync_result = moveCopySmbToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path, stwa.smbFileList);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
            Collections.sort(stwa.smbFileList);
            SmbFile tf = null;
            try {
                tf = new SmbFile(from_path, stwa.masterCifsContext);
            } catch (MalformedURLException e) {
                stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() +
                        " To=" + to_path + ", Target file instance creation error occurred during sync delete process");
                stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
                SyncThread.printStackTraceElement(stwa, e.getStackTrace());
                stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
//            for(String item:stwa.smbFileList) Log.v("","fp="+item);
            sync_result = syncDeleteSmbToSmb(stwa, sti,
                    from_path, from_path, to_path, to_path, tf, stwa.smbFileList);
        }
        stwa.smbFileList = null;
        return sync_result;
    }

    ;

    static public int syncCopySmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                       String from_path, String to_path) {
        SmbFile mf = null;
        try {
            mf = new SmbFile(from_path, stwa.masterCifsContext);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        int sr = moveCopySmbToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path, null);
        return sr;
    }

    ;

    static public int syncMoveSmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                       String from_path, String to_path) {
        SmbFile mf = null;
        try {
            mf = new SmbFile(from_path, stwa.masterCifsContext);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return moveCopySmbToSmb(stwa, sti, true, from_path, from_path, mf, to_path, to_path, null);
    }

    ;

    static private int moveCopySmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                        String from_base, String from_path, SmbFile mf, String to_base, String to_path, ArrayList<String> smb_fl) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        SmbFile tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.targetCifsContext);
                        }
                        SmbFile[] children = mf.listFiles();
                        if (children != null) {
                            for (SmbFile element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isFile()) {
                                            sync_result = moveCopySmbToSmb(stwa, sti, move_file, from_base, from_path + element.getName(),
                                                    element, to_base, to_path + element.getName(), smb_fl);
                                            if (smb_fl != null && sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS)
                                                stwa.smbFileList.add(element.getPath());
                                        } else {
                                            if (sti.isSyncSubDirectory()) {
                                                sync_result = moveCopySmbToSmb(stwa, sti, move_file, from_base, from_path + element.getName(),
                                                        element, to_base, to_path + element.getName(), smb_fl);
                                            } else {
                                                if (stwa.gp.settingDebugLevel >= 1)
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                            stwa.syncTaskRetryCount--;
                                            if (stwa.syncTaskRetryCount > 0)
                                                sync_result = waitRetryInterval(stwa);
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                break;
                                        } else {
                                            stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                            break;
                                        }
                                    }
                                    if (smb_fl != null && sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS)
                                        smb_fl.add(element.getPath());
                                    if (sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) break;
                                } else {
                                    return sync_result;
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled()) {
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            if (stwa.gp.settingDebugLevel >= 1)
                                stwa.util.addDebugMsg(1, "I", "Directory was null, dir=" + mf.getPath());
                        }
                    } else {
                        if (!mf.canRead()) {
                            if (stwa.gp.settingDebugLevel >= 1)
                                stwa.util.addDebugMsg(1, "I", "Directory ignored because can not read, fp=" + from_path + "/" + mf.getName());
                        }
                    }
                } else { // file copy
                    if (SyncThread.isDirectorySelectedByFileName(stwa, t_from_path) &&
                            !SyncThread.isHiddenFile(stwa, sti, mf) && SyncThread.isFileSelected(stwa, sti, t_from_path)) {
//						tf = new File(to_path);
                        tf = new SmbFile(to_path, stwa.targetCifsContext);
                        boolean tf_exists = tf.exists();
                        if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                            //Ignore override the file
                            if (move_file)
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                            else
                                stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                        } else {
                            if (move_file) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, to_path)) {
                                    if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            sync_result = SyncThreadCopyFile.copyFileSmbToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
//											tf.setLastModified(mf.lastModified());
//											SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList,stwa.newLastModifiedList,
//													to_path, tf.lastModified(), mf.lastModified());
                                            SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                            SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.targetCifsContext);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.targetCifsContext);

                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                                                stwa.msgs_mirror_task_file_moved);
                                    }
                                } else {
                                    stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                }
                            } else {
                                if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                                    if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, to_path)) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            sync_result = SyncThreadCopyFile.copyFileSmbToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    to_path.substring(0, to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                                stwa.syncTaskRetryCount--;
                                                if (stwa.syncTaskRetryCount > 0)
                                                    sync_result = waitRetryInterval(stwa);
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                    break;
                                            } else {
                                                stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                                break;
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, mf.getName(), tmsg);
//											tf.setLastModified(mf.lastModified());
//											SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList,stwa.newLastModifiedList,
//													to_path, tf.lastModified(), mf.lastModified());
                                            SyncThread.scanMediaFile(stwa, to_path);
                                            stwa.totalCopyCount++;
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", to_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                    }
                                }
                            }
                            if (!stwa.gp.syncThreadControl.isEnabled())
                                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    ;

    static final private int syncDeleteSmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_base,
                                                String master_dir, String to_base, String target_dir, SmbFile tf, ArrayList<String> smb_fl) {
        int sync_result = 0;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
        try {
            String tmp_target_dir = target_dir.substring(to_base.length());
            if (tmp_target_dir.startsWith("/")) tmp_target_dir = tmp_target_dir.substring(1);
            if (tf.isDirectory()) { // Directory Delete
                if (//!SyncThread.isDirectoryExcluded(stwa, tmp_target_dir) &&
                        !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                                SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                    if (isSmbFileExitst(stwa, smb_fl, master_dir)) {
                        SmbFile[] children = tf.listFiles();
                        if (children != null) {
                            for (SmbFile element : children) {
                                String tmp_fname = element.getName();
                                if (element.isFile() || (element.isDirectory() && sti.isSyncSubDirectory())) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isFile()) {
                                            sync_result = syncDeleteSmbToSmb(stwa, sti, from_base, master_dir + tmp_fname,
                                                    to_base, target_dir + tmp_fname, element, smb_fl);
                                        } else {
//                                            sync_result = syncDeleteSmbToSmb(stwa, sti, from_base, master_dir + tmp_fname + "/",
//                                                    to_base, target_dir + "/" + tmp_fname, element, smb_fl);
                                            sync_result = syncDeleteSmbToSmb(stwa, sti, from_base, master_dir + tmp_fname,
                                                    to_base, target_dir + tmp_fname, element, smb_fl);
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR) {
                                            stwa.syncTaskRetryCount--;
                                            if (stwa.syncTaskRetryCount > 0)
                                                sync_result = waitRetryInterval(stwa);
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_CANCEL)
                                                break;
                                        } else {
                                            stwa.syncTaskRetryCount = stwa.syncTaskRetryCountOriginal;
                                            break;
                                        }
                                    }
                                    if (sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) break;
                                } else {
                                    if (stwa.gp.settingDebugLevel >= 2)
                                        stwa.util.addDebugMsg(2, "I", "sub directory ignored by option, dir=" + master_dir + "/" + tmp_fname);
                                }
                                if (!stwa.gp.syncThreadControl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!stwa.gp.syncThreadControl.isEnabled())
                                        sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                    break;
                                }
                            }
                        } else {
                            stwa.util.addLogMsg("W", "File/Directory was not found, fp=" + tf.getPath());
//								sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        }
                    } else {
                        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR, target_dir)) {
                            SyncThread.deleteSmbItem(stwa, true, sti, target_dir, target_dir, stwa.targetCifsContext);
                        } else {
                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                    stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                        }
                    }
                }
            } else { // file Delete
//				String parent_dir="", t_dir=tmp_target_dir.length()>0?tmp_target_dir.substring(1):"";
//				if (t_dir.lastIndexOf("/")>=0) parent_dir="/"+t_dir.substring(0, t_dir.lastIndexOf("/"));
//				Log.v("","parent="+parent_dir+", t="+t_dir+", tmp="+tmp_target_dir);
                if (//!SyncThread.isDirectoryExcluded(stwa, parent_dir) &&
                        !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                                SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                    if (!SyncThread.isHiddenFile(stwa, sti, tf)) {
                        if (!isSmbFileExitst(stwa, smb_fl, master_dir)) {
                            if (!(tmp_target_dir.equals("") && !sti.isSyncProcessRootDirFile())) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE, target_dir)) {
                                    SyncThread.deleteSmbItem(stwa, true, sti, target_dir, target_dir, stwa.targetCifsContext);
                                } else {
                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                            stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " master=" + master_dir + ", target=" + target_dir);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    ;

    static private int waitRetryInterval(SyncThreadWorkArea stwa) {
        int result = 0;
        if (!stwa.gp.syncThreadControl.isEnabled()) {
            result = SyncTaskItem.SYNC_STATUS_CANCEL;
        } else {
            synchronized (stwa.gp.syncThreadControl) {
                try {
                    stwa.gp.syncThreadControl.wait(1000 * SyncThread.SYNC_RETRY_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!stwa.gp.syncThreadControl.isEnabled())
                result = SyncTaskItem.SYNC_STATUS_CANCEL;
        }
        return result;
    }

    ;

}
