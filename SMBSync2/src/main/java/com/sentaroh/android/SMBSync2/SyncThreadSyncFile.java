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

import android.os.Build;
import android.os.SystemClock;

import com.sentaroh.android.SMBSync2.SyncThread.SyncThreadWorkArea;
import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.jcifs.JcifsException;
import com.sentaroh.jcifs.JcifsFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import static com.sentaroh.android.SMBSync2.Constants.ARCHIVE_FILE_TYPE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_COPY;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_DELETE_DIR;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_MOVE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_YEAR;

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
        stwa.jcifsNtStatusCode=0;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
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
                                if (element.isFile() || (element.isDirectory() && sti.isSyncOptionSyncSubDirectory())) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isFile()) {
                                            sync_result = syncDeleteSmbToExternal(stwa, sti, from_base, master_dir + tmp,
                                                    to_base, target_dir + "/" + tmp, element, smb_fl);
                                        } else {
                                            sync_result = syncDeleteSmbToExternal(stwa, sti, from_base, master_dir + tmp+"/" ,
                                                    to_base, target_dir + "/" + tmp, element, smb_fl);
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!stwa.gp.syncThreadCtrl.isEnabled())
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
                                    " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
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
                                            " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti, e, master_dir, target_dir);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, master_dir, target_dir);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

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
        stwa.jcifsNtStatusCode=0;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
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
                                if (element.isFile() || (element.isDirectory() && sti.isSyncOptionSyncSubDirectory())) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isFile()) {
                                            sync_result = syncDeleteSmbToInternal(stwa, sti, from_base, master_dir + tmp_fname,
                                                    to_base, target_dir + "/" + tmp_fname, element, smb_fl);
                                        } else {
                                            sync_result = syncDeleteSmbToInternal(stwa, sti, from_base, master_dir + tmp_fname + "/",
                                                    to_base, target_dir + "/" + tmp_fname, element, smb_fl);
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!stwa.gp.syncThreadCtrl.isEnabled())
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
                                    " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
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
                                            " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti, e, master_dir, target_dir);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, master_dir, target_dir);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    private static boolean isSmbFileExitst(SyncThreadWorkArea stwa, ArrayList<String> smb_fl, String fp) throws IOException, JcifsException {
        boolean mf_exists = (Collections.binarySearch(stwa.smbFileList, fp) >= 0);
        if (!mf_exists) {
            stwa.util.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " file list not found, fp=" + fp);
            JcifsFile mf = new JcifsFile(fp, stwa.masterAuth);
            mf_exists = mf.exists();
        }
        return mf_exists;
    }

//	static final private int syncDeleteInternalToInternal(SyncThreadCommonArea stwa, SyncTaskItem sti,
//			String master_dir, String target_dir) {
//		File tf=new File(target_dir);
//		return syncDeleteInternalToInternal(stwa, sti, master_dir,
//				master_dir, target_dir, target_dir, tf);
//	};

    static final private int syncDeleteInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_base,
                                                          String master_dir, String to_base, String target_dir, File tf) {
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File mf;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
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
                            if (element.isFile() || (element.isDirectory() && sti.isSyncOptionSyncSubDirectory())) {
                                sync_result = syncDeleteInternalToInternal(stwa, sti, from_base, master_dir + "/" + tmp,
                                        to_base, target_dir + "/" + tmp, element);
                            } else {
                                stwa.util.addDebugMsg(2, "I", "sub directory ignored by option, dir=" + master_dir + "/" + tmp);
                            }
                            if (!stwa.gp.syncThreadCtrl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                if (!stwa.gp.syncThreadCtrl.isEnabled())
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
                                " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
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
                                        " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                            }
                        }
                    }
                }
            }
        }
        return sync_result;
    }

//	static final private int syncDeleteInternalToSmb(SyncThreadCommonArea stwa, SyncTaskItem sti,
//			String master_dir, String target_dir) {
//		SmbFile tf = null;
//		try {
//			tf = new SmbFile(target_dir + "/", stwa.ntlmPasswordAuth);
//		} catch (MalformedURLException e) {
//			stwa.util.addLogMsg("E","","syncDeleteInternalToSmb master="+master_dir+", target="+target_dir);
//			stwa.util.addLogMsg("E","",e.getMessage());//e.toString());
//			SyncThread.printStackTraceElement(stwa, e.getStackTrace());
//			stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
//			return SyncTaskItem.SYNC_STATUS_ERROR;
//		}
//		return syncDeleteInternalToSmb(stwa, sti, master_dir,
//				master_dir, target_dir, target_dir, tf);
//	};

    static final private int syncDeleteInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_base,
                                                     String master_dir, String to_base, String target_dir, JcifsFile tf) {
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File mf;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
        String tmp_target_dir = target_dir.substring(to_base.length());
        if (tmp_target_dir.startsWith("/")) tmp_target_dir = tmp_target_dir.substring(1);
        try {
            if (tf.isDirectory()) { // Directory Delete
                if (//!SyncThread.isDirectoryExcluded(stwa, tmp_target_dir) &&
                        !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                                SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                    mf = new File(master_dir);
                    if (mf.exists()) {
                        JcifsFile[] children = tf.listFiles();
                        if (children != null) {
                            for (JcifsFile element : children) {
                                String tmp = element.getName();
                                if (tmp.lastIndexOf("/") > 0)
                                    tmp = tmp.substring(0, tmp.lastIndexOf("/"));
                                if (element.isFile() || (element.isDirectory() && sti.isSyncOptionSyncSubDirectory())) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        sync_result = syncDeleteInternalToSmb(stwa, sti, from_base, master_dir + "/" + tmp,
                                                to_base, target_dir + "/" + tmp, element);
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!stwa.gp.syncThreadCtrl.isEnabled())
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
                            SyncThread.deleteSmbItem(stwa, true, sti, to_base, target_dir + "/", stwa.targetAuth);
                        } else {
                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                    " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
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
                                    SyncThread.deleteSmbItem(stwa, true, sti, to_base, target_dir, stwa.targetAuth);
                                } else {
                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                            " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti, e, master_dir, target_dir);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, master_dir, target_dir);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

//	static final private int syncDeleteInternalToExternal(SyncThreadCommonArea stwa, SyncTaskItem sti,
//			String master_dir, String target_dir) {
//		File tf = new File(target_dir);
//		return syncDeleteInternalToExternal(stwa, sti, master_dir,
//				master_dir, target_dir, target_dir, tf);
//	};

    static final private int syncDeleteInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_base,
                                                          String master_dir, String to_base, String target_dir, File tf) {
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File mf;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
        String tmp_target_dir = target_dir.replace(to_base, "");
        if (tmp_target_dir.startsWith("/")) tmp_target_dir = tmp_target_dir.substring(1);
        if (tf.isDirectory()) { // Directory Delete
            mf = new File(master_dir);
            if (mf.exists()) {
                if (!SyncThread.isHiddenDirectory(stwa, sti, tf) && SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                    File[] children = tf.listFiles();
                    if (children != null) {
                        for (File element : children) {
                            String tmp = element.getName();
                            if (tmp.lastIndexOf("/") > 0)
                                tmp = tmp.substring(0, tmp.lastIndexOf("/"));
                            if (element.isFile() || (element.isDirectory() && sti.isSyncOptionSyncSubDirectory())) {
                                sync_result = syncDeleteInternalToExternal(stwa, sti, from_base, master_dir + "/" + tmp,
                                        to_base, target_dir + "/" + tmp, element);
                            } else {
                                stwa.util.addDebugMsg(2, "I", "sub directory ignored by option, dir=" + master_dir + "/" + tmp);
                            }
                            if (!stwa.gp.syncThreadCtrl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                if (!stwa.gp.syncThreadCtrl.isEnabled())
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
                            " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                }
            }
        } else { // file Delete
            if (!SyncThread.isHiddenDirectory(stwa, sti, tf) && SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                if (!SyncThread.isHiddenFile(stwa, sti, tf)) {
                    mf = new File(master_dir);
                    if (!mf.exists()) {
                        if (!(tmp_target_dir.equals("") && !sti.isSyncProcessRootDirFile())) {
                            if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE, target_dir)) {
                                SyncThread.deleteExternalStorageItem(stwa, true, sti, target_dir);
                            } else {
                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                        " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                            }
                        }
                    }
                }
            }
        }

        return sync_result;
    }

    static public int syncMirrorInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result =0;
        File tf = new File(to_path);

        if (sti.isSyncOptionDeleteFirstWhenMirror()) {
            sync_result = syncDeleteInternalToInternal(stwa, sti, from_path, from_path, to_path, to_path, tf);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result =moveCopyInternalToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
            }
        } else {
            sync_result = moveCopyInternalToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result = syncDeleteInternalToInternal(stwa, sti, from_path, from_path, to_path, to_path, tf);
            }
        }

        return sync_result;
    }

    static public int syncCopyInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyInternalToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
    }

    static public int syncMoveInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyInternalToInternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
    }

    static private int moveCopyInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                                  String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File tf;
        try {
            String t_from_path = from_path.substring(from_base.length());
            if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
            if (mf.exists()) {
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncOptionSyncEmptyDirectory()) {
                            if (!sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) {
                                SyncThread.createDirectoryToInternalStorage(stwa, sti, to_path);
                            }
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
                                                if (sti.isSyncOptionSyncSubDirectory()) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled()) {
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
                        String parsed_to_path=to_path;
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword() && stwa.replaceKeywordRequiredAtWhileSync)
                            parsed_to_path=convertToExifDateTime(stwa, sti, new FileInputStream(mf), mf.lastModified(), from_path, to_path);
                        if (from_path.equals(parsed_to_path)) {
                            stwa.util.addLogMsg("W","Ignoring sync because master and target are the same file. File="+from_path);
                        } else {
                            tf = new File(parsed_to_path);
                            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword())
                                SyncThread.createDirectoryToInternalStorage(stwa, sti, tf.getParent());
                            boolean tf_exists = tf.exists();
                            if (!tf_exists || tf.isFile()) {
                                if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                                    //Ignore override the file
                                    if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY)) {
                                        if (move_file) stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                                        else stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                                    }
                                } else {
                                    if (move_file) {
                                        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, parsed_to_path)) {
                                            if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                                    SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.lastModified())) {
                                                sync_result = SyncThreadCopyFile.copyFileInternalToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                                    if (stwa.lastModifiedIsFunctional) {
                                                        SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                    } else {
                                                        SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                                parsed_to_path, tf.lastModified(), mf.lastModified());
                                                    }
//                                            tf.setLastModified(mf.lastModified());
                                                    SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                    stwa.totalCopyCount++;
                                                    SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                            stwa.msgs_mirror_task_file_moved);
                                                }
                                            } else {
                                                SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                        stwa.msgs_mirror_task_file_moved);
                                            }
                                        } else {
                                            stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                        }
                                    } else {
                                        if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                                SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.lastModified())) {
                                            if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, parsed_to_path)) {
                                                sync_result = SyncThreadCopyFile.copyFileInternalToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                                    String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(), tmsg);
                                                    if (stwa.lastModifiedIsFunctional) {
                                                        SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                    } else {
                                                        SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                                parsed_to_path, tf.lastModified(), mf.lastModified());
                                                    }
//                                            tf.setLastModified(mf.lastModified());
                                                    SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                    stwa.totalCopyCount++;
                                                }
                                            } else {
                                                stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                            }
                                        }
                                    }
                                    if (!stwa.gp.syncThreadCtrl.isEnabled())
                                        sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                }
                            } else {
                                stwa.util.addLogMsg("E", stwa.gp.appContext.getString(R.string.msgs_mirror_directory_with_same_name_as_the_file_found)+parsed_to_path);
                                sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                            }

                        }
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti, e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static private String convertToExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, InputStream is,
                                          long file_last_modified, String from_path, String to_path) {
        String parsed_to_path=to_path;
        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword() && stwa.replaceKeywordRequiredAtWhileSync) {
            String[] taken_date=null;
            long taken_millis=0L;
            if (isMovieFile(from_path) || isPictureFile(from_path)) {
                if (isPictureFile(from_path)) taken_date=SyncThreadArchiveFile.getExifDateTime(stwa, is);
                else taken_date=SyncThreadArchiveFile.getMp4ExifDateTime(stwa, is);
                if (taken_date!=null && taken_date.length==2 && taken_date[0]!=null && taken_date[1]!=null) {
                    SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                    Date date = null;
                    try {
                        date = sdFormat.parse(taken_date[0]+" "+taken_date[1]);
                        taken_millis=date.getTime();
                    } catch (ParseException e) {
                        taken_millis=file_last_modified;
                        stwa.util.addLogMsg("W","convertToExifDateTime date convert error occured, file last modified time used. File="+from_path);
                        stwa.util.addLogMsg("W","convertToExifDateTime error="+e.getMessage());
                    }
                } else {
//                    stwa.util.addDebugMsg(1,"W","convertToExifDateTime EXIF date not available.");
                    taken_millis=file_last_modified;
                }
            } else {
                taken_millis=file_last_modified;
            }
            parsed_to_path=SyncThread.replaceKeywordValue(to_path, taken_millis);
        }
        return parsed_to_path;
    }

    static private boolean isMovieFile(String fp) {
        boolean result=false;
        if (fp.toLowerCase().endsWith(".mp4") ||
                fp.toLowerCase().endsWith(".mov")) result=true;
        return result;
    }
    static private boolean isPictureFile(String fp) {
        boolean result=false;
        if (fp.toLowerCase().endsWith(".gif") ||
                fp.toLowerCase().endsWith(".jpg") ||
                fp.toLowerCase().endsWith(".jpeg") ||
                fp.toLowerCase().endsWith(".jpe") ||
                fp.toLowerCase().endsWith(".png")) result=true;
        return result;
    }

    static public int syncMirrorInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result =0;
        stwa.lastWriteSafFile=null;
        stwa.lastWriteFile=null;

        File tf = new File(to_path);
        if (sti.isSyncOptionDeleteFirstWhenMirror()) {
            sync_result =  syncDeleteInternalToExternal(stwa, sti, from_path, from_path, to_path, to_path, tf);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result =moveCopyInternalToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
                waitExternalMediaFileFlushed(stwa, sti);
            }
        } else {
            sync_result = moveCopyInternalToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result = syncDeleteInternalToExternal(stwa, sti, from_path, from_path, to_path, to_path, tf);
                waitExternalMediaFileFlushed(stwa, sti);
            }
        }
        return sync_result;
    }

    static public int syncCopyInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        stwa.lastWriteSafFile=null;
        stwa.lastWriteFile=null;

        File mf = new File(from_path);
        int sync_result=moveCopyInternalToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) waitExternalMediaFileFlushed(stwa, sti);
        return sync_result;
    }

    static public int syncMoveInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        stwa.lastWriteSafFile=null;
        stwa.lastWriteFile=null;

        File mf = new File(from_path);
        int sync_result=moveCopyInternalToExternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) waitExternalMediaFileFlushed(stwa, sti);
        return sync_result;
    }

    static private int moveCopyInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                                  String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        if (!SyncThread.isValidFileDirectoryName(stwa, sti, from_path)) {
            if (sti.isSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters()) return sync_result;
            else return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        stwa.jcifsNtStatusCode=0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncOptionSyncEmptyDirectory()) {
                            if (!sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) {
                                SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
                            }
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
                                            if (sti.isSyncOptionSyncSubDirectory()) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled()) {
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
                        String parsed_to_path=to_path;
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword() && stwa.replaceKeywordRequiredAtWhileSync)
                            parsed_to_path=convertToExifDateTime(stwa, sti, new FileInputStream(mf), mf.lastModified(), from_path, to_path);
                        tf = new File(parsed_to_path);
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()){
                            SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());
                        }
                        boolean tf_exists = tf.exists();
                        if (!tf_exists || tf.isFile()) {
                            if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                                //Ignore override the file
                                if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY)) {
                                    if (move_file) stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                                    else stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                                }
                            } else {
                                if (move_file) {
                                    if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, parsed_to_path) ) {
                                        if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                                SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.lastModified())) {
                                            sync_result = SyncThreadCopyFile.copyFileInternalToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                                if (!sti.isSyncTestMode()) {
                                                    SafFile sf = null;
//                                                sf = stwa.gp.safMgr.createSdcardItem(parsed_to_path, false);
                                                    sf=SyncThread.createSafFile(stwa, sti, parsed_to_path, false);
                                                    if (Build.VERSION.SDK_INT>=24 && stwa.lastModifiedIsFunctional) {
                                                        SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                    } else {
                                                        SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                                parsed_to_path, sf.lastModified(), mf.lastModified());
                                                    }
                                                    SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                    stwa.lastWriteSafFile=sf;
                                                    stwa.lastWriteFile=tf;
                                                }
                                                stwa.totalCopyCount++;
                                                SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                        stwa.msgs_mirror_task_file_moved);
                                            }
                                        } else {
                                            SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                    }
                                } else {
                                    if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                            SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.lastModified())) {
                                        if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, parsed_to_path)) {
                                            sync_result = SyncThreadCopyFile.copyFileInternalToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                                String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(), tmsg);
                                                if (!sti.isSyncTestMode()) {
                                                    SafFile sf = null;
//                                                sf = stwa.gp.safMgr.createSdcardItem(parsed_to_path, false);
                                                    sf=SyncThread.createSafFile(stwa, sti, parsed_to_path, false);
                                                    if (Build.VERSION.SDK_INT>=24 && stwa.lastModifiedIsFunctional) {
                                                        SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                    } else {
                                                        SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                                parsed_to_path, sf.lastModified(), mf.lastModified());
                                                    }
                                                    stwa.lastWriteSafFile=sf;
                                                    stwa.lastWriteFile=tf;
                                                    SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                }
                                                stwa.totalCopyCount++;
                                            }
                                        } else {
                                            stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                        }
                                    }
                                }
                                if (!stwa.gp.syncThreadCtrl.isEnabled())
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                            }
                        } else {
                            stwa.util.addLogMsg("E", stwa.gp.appContext.getString(R.string.msgs_mirror_directory_with_same_name_as_the_file_found)+parsed_to_path);
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti, e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncMirrorInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result =0;
        JcifsFile tf = null;
        try {
            tf = new JcifsFile(to_path + "/", stwa.targetAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " master=" + from_path + ", target=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        if (sti.isSyncOptionDeleteFirstWhenMirror()) {
            sync_result =syncDeleteInternalToSmb(stwa, sti, from_path, from_path, to_path, to_path, tf);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result = moveCopyInternalToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
            }
        } else {
            sync_result = moveCopyInternalToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result = syncDeleteInternalToSmb(stwa, sti, from_path, from_path, to_path, to_path, tf);
            }
        }
        return sync_result;
    }

    static public int syncCopyInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyInternalToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
    }

    static public int syncMoveInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyInternalToSmb(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
    }

    static private int moveCopyInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                             String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        stwa.jcifsNtStatusCode=0;
        int sync_result = 0;
        if (!SyncThread.isValidFileDirectoryName(stwa, sti, from_path)) {
            if (sti.isSyncOptionIgnoreDirectoriesOrFilesThatContainUnusableCharacters()) return sync_result;
            else return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        JcifsFile tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncOptionSyncEmptyDirectory()) {
                            if (!sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) {
                                SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.targetAuth);
                            }
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
                                                if (sti.isSyncOptionSyncSubDirectory()) {
                                                    sync_result = moveCopyInternalToSmb(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName());
                                                } else {
                                                    if (stwa.gp.settingDebugLevel >= 1)
                                                        stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                                }
                                            }
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled()) {
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
                        String parsed_to_path=to_path;
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword() && stwa.replaceKeywordRequiredAtWhileSync)
                            parsed_to_path=convertToExifDateTime(stwa, sti, new FileInputStream(mf), mf.lastModified(), from_path, to_path);
                        tf = new JcifsFile(parsed_to_path, stwa.targetAuth);
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword())
                            SyncThread.createDirectoryToSmb(stwa, sti, tf.getParent(), stwa.targetAuth);

                        boolean tf_exists = tf.exists();
                        if (!tf_exists || tf.isFile()) {
                            if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                                //Ignore override the file
                                if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, mf, tf, stwa.ALL_COPY)) {
                                    if (move_file) stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                                    else stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                                }
                            } else {
                                if (move_file) {
                                    if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, parsed_to_path)) {
                                        if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, mf, tf, stwa.ALL_COPY) &&
                                                SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.getLastModified())) {
                                            while (stwa.syncTaskRetryCount > 0) {
                                                sync_result = SyncThreadCopyFile.copyFileInternalToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                                        parsed_to_path, tf.getLastModified(), mf.lastModified());
//											SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                stwa.totalCopyCount++;
                                                SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                        stwa.msgs_mirror_task_file_moved);
                                            }
                                        } else {
                                            SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                    }
                                } else {
                                    if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, mf, tf, stwa.ALL_COPY) &&
                                            SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.getLastModified())) {
                                        if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, parsed_to_path)) {
                                            while (stwa.syncTaskRetryCount > 0) {
                                                sync_result = SyncThreadCopyFile.copyFileInternalToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(), tmsg);
                                                SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                        parsed_to_path, tf.getLastModified(), mf.lastModified());
//											SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                stwa.totalCopyCount++;
                                            }
                                        } else {
                                            stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                        }
                                    }
                                }
                                if (!stwa.gp.syncThreadCtrl.isEnabled())
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                            }
                        } else {
                            stwa.util.addLogMsg("E", stwa.gp.appContext.getString(R.string.msgs_mirror_directory_with_same_name_as_the_file_found)+parsed_to_path);
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti, e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncMirrorExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        File tf = new File(to_path);
        int sync_result=0;
        if (sti.isSyncOptionDeleteFirstWhenMirror()) {
            sync_result = syncDeleteInternalToInternal(stwa, sti, from_path,
                    from_path, to_path, to_path, tf);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result =moveCopyExternalToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);

            }
        } else {
            sync_result = moveCopyExternalToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result = syncDeleteInternalToInternal(stwa, sti, from_path,
                        from_path, to_path, to_path, tf);

            }
        }

        return sync_result;
    }

    static public int syncCopyExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyExternalToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
    }

    static public int syncMoveExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyExternalToInternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
    }

    static private int moveCopyExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                                  String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncOptionSyncEmptyDirectory()) {
                            if (!sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) {
                                SyncThread.createDirectoryToInternalStorage(stwa, sti, to_path);
                            }
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
                                            if (sti.isSyncOptionSyncSubDirectory()) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled()) {
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
                        String parsed_to_path=to_path;
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword() && stwa.replaceKeywordRequiredAtWhileSync)
                            parsed_to_path=convertToExifDateTime(stwa, sti, new FileInputStream(mf), mf.lastModified(), from_path, to_path);
                        tf = new File(parsed_to_path);
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword())
                            SyncThread.createDirectoryToInternalStorage(stwa, sti, tf.getParent());
                        boolean tf_exists = tf.exists();
                        if (!tf_exists || tf.isFile()) {
                            if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                                //Ignore override the file
                                if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY)) {
                                    if (move_file) stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                                    else stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                                }
                            } else {
                                if (move_file) {
                                    if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, parsed_to_path)) {
                                        if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                                SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.lastModified())) {
                                            sync_result = SyncThreadCopyFile.copyFileExternalToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                                if (stwa.lastModifiedIsFunctional) {
                                                    SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                } else {
                                                    SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                            parsed_to_path, tf.lastModified(), mf.lastModified());
                                                }
//                                            tf.setLastModified(mf.lastModified());
                                                SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                stwa.totalCopyCount++;
                                                SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                        stwa.msgs_mirror_task_file_moved);
                                            }
                                        } else {
                                            SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                    }
                                } else {
                                    if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                            SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.lastModified())) {
                                        if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, parsed_to_path)) {
                                            sync_result = SyncThreadCopyFile.copyFileExternalToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                    mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                                String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(), tmsg);
                                                if (stwa.lastModifiedIsFunctional) {
                                                    SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                } else {
                                                    SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                            parsed_to_path, tf.lastModified(), mf.lastModified());
                                                }
//                                            tf.setLastModified(mf.lastModified());
                                                SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                stwa.totalCopyCount++;
                                            }
                                        } else {
                                            stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                        }
                                    }
                                }
                                if (!stwa.gp.syncThreadCtrl.isEnabled())
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                            }
                        } else {
                            stwa.util.addLogMsg("E", stwa.gp.appContext.getString(R.string.msgs_mirror_directory_with_same_name_as_the_file_found)+parsed_to_path);
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti, e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncMirrorExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result =0;
        stwa.lastWriteSafFile=null;
        stwa.lastWriteFile=null;

        File tf = new File(to_path);
        if (sti.isSyncOptionDeleteFirstWhenMirror()) {
            sync_result = syncDeleteInternalToExternal(stwa, sti, from_path, from_path, to_path, to_path, tf);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result =moveCopyExternalToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
                waitExternalMediaFileFlushed(stwa, sti);
            }
        } else {
            sync_result = moveCopyExternalToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result = syncDeleteInternalToExternal(stwa, sti, from_path, from_path, to_path, to_path, tf);
                waitExternalMediaFileFlushed(stwa, sti);
            }
        }

        return sync_result;
    }

    static public int syncCopyExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        stwa.lastWriteSafFile=null;
        stwa.lastWriteFile=null;

        File mf = new File(from_path);
        int sync_result=moveCopyExternalToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) waitExternalMediaFileFlushed(stwa, sti);
        return sync_result;
    }

    static public int syncMoveExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                 String from_path, String to_path) {
        stwa.lastWriteSafFile=null;
        stwa.lastWriteFile=null;

        File mf = new File(from_path);
        int sync_result=moveCopyExternalToExternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) waitExternalMediaFileFlushed(stwa, sti);
        return sync_result;
    }

    static private int moveCopyExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                                  String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncOptionSyncEmptyDirectory()) {
                            if (!sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) {
                                SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
                            }
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
                                                if (sti.isSyncOptionSyncSubDirectory()) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled()) {
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
                        String parsed_to_path=to_path;
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword() && stwa.replaceKeywordRequiredAtWhileSync)
                            parsed_to_path=convertToExifDateTime(stwa, sti, new FileInputStream(mf), mf.lastModified(), from_path, to_path);
                        if (from_path.equals(parsed_to_path)) {
                            stwa.util.addLogMsg("W","Ignoring sync because master and target are the same file. File="+from_path);
                        } else {
                            tf = new File(parsed_to_path);
                            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()){
                                SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());
                            }
                            boolean tf_exists = tf.exists();
                            if (!tf_exists || tf.isFile()) {
                                if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                                    //Ignore override the file
                                    if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY)) {
                                        if (move_file) stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                                        else stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                                    }
                                } else {
                                    if (move_file) {
                                        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, parsed_to_path)) {
                                            if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY)  &&
                                                    SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.lastModified())) {
                                                sync_result = SyncThreadCopyFile.copyFileExternalToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                                    if (!sti.isSyncTestMode()) {
                                                        SafFile sf = null;
//                                                sf = stwa.gp.safMgr.createSdcardItem(parsed_to_path, false);
                                                        sf=SyncThread.createSafFile(stwa, sti, parsed_to_path, false);
                                                        if (Build.VERSION.SDK_INT>=24 && stwa.lastModifiedIsFunctional) {
                                                            SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                        } else {
                                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path, sf.lastModified(), mf.lastModified());
                                                        }
                                                        SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                        stwa.lastWriteSafFile=sf;
                                                        stwa.lastWriteFile=tf;
                                                    }
                                                    stwa.totalCopyCount++;
                                                    SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                            stwa.msgs_mirror_task_file_moved);
                                                }
                                            } else {
                                                SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                        stwa.msgs_mirror_task_file_moved);
                                            }
                                        } else {
                                            stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                        }
                                    } else {
                                        if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                                SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.lastModified())) {
                                            if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, parsed_to_path)) {
                                                sync_result = SyncThreadCopyFile.copyFileExternalToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                                    String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(), tmsg);
                                                    if (!sti.isSyncTestMode()) {
                                                        SafFile sf = null;
//                                                sf = stwa.gp.safMgr.createSdcardItem(parsed_to_path, false);
                                                        sf=SyncThread.createSafFile(stwa, sti, parsed_to_path, false);
                                                        if (Build.VERSION.SDK_INT>=24 && stwa.lastModifiedIsFunctional) {
                                                            SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                        } else {
                                                            SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path, sf.lastModified(), mf.lastModified());
                                                        }
                                                        SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                        stwa.lastWriteSafFile=sf;
                                                        stwa.lastWriteFile=tf;
                                                    }
                                                    stwa.totalCopyCount++;
                                                }
                                            } else {
                                                stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                            }
                                        }
                                    }
                                    if (!stwa.gp.syncThreadCtrl.isEnabled())
                                        sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                }
                            } else {
                                stwa.util.addLogMsg("E", stwa.gp.appContext.getString(R.string.msgs_mirror_directory_with_same_name_as_the_file_found)+parsed_to_path);
                                sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                            }
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti, e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncMirrorExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result =0;
        JcifsFile tf = null;
        try {
            tf = new JcifsFile(to_path + "/", stwa.targetAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " master=" + from_path + ", target=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        if (sti.isSyncOptionDeleteFirstWhenMirror()) {
            sync_result = syncDeleteInternalToSmb(stwa, sti, from_path, from_path, to_path, to_path, tf);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result =moveCopyExternalToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
            }
        } else {
            sync_result = moveCopyExternalToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result = syncDeleteInternalToSmb(stwa, sti, from_path, from_path, to_path, to_path, tf);
            }
        }

        return sync_result;
    }

    static public int syncCopyExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyExternalToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path);
    }

    static public int syncMoveExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        File mf = new File(from_path);
        return moveCopyExternalToSmb(stwa, sti, true, from_path, from_path, mf, to_path, to_path);
    }

    static private int moveCopyExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                             String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        JcifsFile tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncOptionSyncEmptyDirectory()) {
                            if (!sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) {
                                SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.targetAuth);
                            }
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
                                                if (sti.isSyncOptionSyncSubDirectory()) {
                                                    sync_result = moveCopyExternalToSmb(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName());
                                                } else {
                                                    if (stwa.gp.settingDebugLevel >= 1)
                                                        stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                                }
                                            }
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled()) {
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
                        String parsed_to_path=to_path;
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword() && stwa.replaceKeywordRequiredAtWhileSync)
                            parsed_to_path=convertToExifDateTime(stwa, sti, new FileInputStream(mf), mf.lastModified(), from_path, to_path);
                        tf = new JcifsFile(parsed_to_path, stwa.targetAuth);
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword())
                            SyncThread.createDirectoryToSmb(stwa, sti, tf.getParent(), stwa.targetAuth);
                        boolean tf_exists = tf.exists();
                        if (!tf_exists || tf.isFile()) {
                            if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                                //Ignore override the file
                                if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, mf, tf, stwa.ALL_COPY)) {
                                    if (move_file) stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                                    else stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                                }
                            } else {
                                if (move_file) {
                                    if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, parsed_to_path)) {
                                        if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, mf, tf, stwa.ALL_COPY) &&
                                                SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.getLastModified())) {
                                            while (stwa.syncTaskRetryCount > 0) {
                                                sync_result = SyncThreadCopyFile.copyFileExternalToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                                        parsed_to_path, tf.getLastModified(), mf.lastModified());
//											SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                stwa.totalCopyCount++;
                                                SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                        stwa.msgs_mirror_task_file_moved);
                                            }
                                        } else {
                                            SyncThread.deleteExternalStorageItem(stwa, false, sti, from_path);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                    }
                                } else {
                                    if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, mf, tf, stwa.ALL_COPY) &&
                                            SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.lastModified(), tf.getLastModified())) {
                                        if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, parsed_to_path)) {
                                            while (stwa.syncTaskRetryCount > 0) {
                                                sync_result = SyncThreadCopyFile.copyFileExternalToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR  && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(), tmsg);
                                                SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                        parsed_to_path, tf.getLastModified(), mf.lastModified());
//											SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                stwa.totalCopyCount++;
                                            }
                                        } else {
                                            stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                        }
                                    }
                                }
                                if (!stwa.gp.syncThreadCtrl.isEnabled())
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                            }
                        } else {
                            stwa.util.addLogMsg("E", stwa.gp.appContext.getString(R.string.msgs_mirror_directory_with_same_name_as_the_file_found)+parsed_to_path);
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti, e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncMirrorSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String from_path, String to_path) {
        JcifsFile mf = null;
        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        stwa.smbFileList = new ArrayList<String>();
        int sync_result =0;

        File tf = new File(to_path);
        if (sti.isSyncOptionDeleteFirstWhenMirror()) {
            sync_result = syncDeleteSmbToInternal(stwa, sti,from_path, from_path, to_path, to_path, tf, stwa.smbFileList);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result =moveCopySmbToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path, stwa.smbFileList);
            }
        } else {
            sync_result = moveCopySmbToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path, stwa.smbFileList);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                Collections.sort(stwa.smbFileList);
                sync_result = syncDeleteSmbToInternal(stwa, sti,from_path, from_path, to_path, to_path, tf, stwa.smbFileList);
            }
        }

        stwa.smbFileList = null;

        return sync_result;
    }

    static public int syncCopySmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        JcifsFile mf = null;
        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        int sr = moveCopySmbToInternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path, null);
        return sr;
    }

    static public int syncMoveSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        JcifsFile mf = null;
        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return moveCopySmbToInternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path, null);
    }

    static private int moveCopySmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                             String from_base, String from_path, JcifsFile mf, String to_base, String to_path, ArrayList<String> smb_fl) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncOptionSyncEmptyDirectory()) {
                            if (!sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) {
                                SyncThread.createDirectoryToInternalStorage(stwa, sti, to_path);
                            }
                        }
                        JcifsFile[] children = mf.listFiles();
                        if (children != null) {
                            for (JcifsFile element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isFile()) {
                                            sync_result = moveCopySmbToInternal(stwa, sti, move_file, from_base, from_path + element.getName(),
                                                    element, to_base, to_path + "/" + element.getName(), smb_fl);
                                            if (smb_fl != null && sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS)
                                                stwa.smbFileList.add(element.getPath());
                                        } else {
                                            if (sti.isSyncOptionSyncSubDirectory()) {
                                                sync_result = moveCopySmbToInternal(stwa, sti, move_file, from_base, from_path + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName().replace("/", ""), smb_fl);
                                            } else {
                                                if (stwa.gp.settingDebugLevel >= 1)
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled()) {
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
                        String parsed_to_path=to_path;
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword() && stwa.replaceKeywordRequiredAtWhileSync)
                            parsed_to_path=convertToExifDateTime(stwa, sti, mf.getInputStream(), mf.getLastModified(), from_path, to_path);
                        tf = new File(parsed_to_path);
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword())
                            SyncThread.createDirectoryToInternalStorage(stwa, sti, tf.getParent());
                        boolean tf_exists = tf.exists();
                        if (!tf_exists || tf.isFile()) {
                            if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                                //Ignore override the file
                                if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY)) {
                                    if (move_file) stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                                    else stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                                }
                            } else {
                                if (move_file) {
                                    if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, parsed_to_path)) {
                                        if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                                SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.getLastModified(), tf.lastModified())) {
                                            while (stwa.syncTaskRetryCount > 0) {
                                                sync_result = SyncThreadCopyFile.copyFileSmbToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                                if (stwa.lastModifiedIsFunctional) {
                                                    SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                } else {
                                                    SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                            parsed_to_path, tf.lastModified(), mf.getLastModified());
                                                }
                                                SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                stwa.totalCopyCount++;
                                                SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.masterAuth);
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                        stwa.msgs_mirror_task_file_moved);
                                            }
                                        } else {
                                            SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.masterAuth);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                    }
                                } else {
                                    if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                            SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.getLastModified(), tf.lastModified())) {
                                        if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, parsed_to_path)) {
                                            while (stwa.syncTaskRetryCount > 0) {
                                                sync_result = SyncThreadCopyFile.copyFileSmbToInternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(), tmsg);
                                                if (stwa.lastModifiedIsFunctional) {
                                                    SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                } else {
                                                    SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                            parsed_to_path, tf.lastModified(), mf.getLastModified());
                                                }
                                                SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                stwa.totalCopyCount++;
                                            }
                                        } else {
                                            stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                        }
                                    }
                                }
                                if (!stwa.gp.syncThreadCtrl.isEnabled()) sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                            }
                        } else {
                            stwa.util.addLogMsg("E", stwa.gp.appContext.getString(R.string.msgs_mirror_directory_with_same_name_as_the_file_found)+parsed_to_path);
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    static public int syncMirrorSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String from_path, String to_path) {
        stwa.smbFileList = new ArrayList<String>();
        JcifsFile mf = null;
        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        int sync_result =0;
        stwa.lastWriteSafFile=null;
        stwa.lastWriteFile=null;

        File tf = new File(to_path);
        if (sti.isSyncOptionDeleteFirstWhenMirror()) {
            sync_result =syncDeleteSmbToExternal(stwa, sti, from_path, from_path, to_path, to_path, tf, stwa.smbFileList);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result = moveCopySmbToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path, stwa.smbFileList);
                waitExternalMediaFileFlushed(stwa, sti);
            }
        } else {
            sync_result = moveCopySmbToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path, stwa.smbFileList);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                Collections.sort(stwa.smbFileList);
                sync_result = syncDeleteSmbToExternal(stwa, sti, from_path, from_path, to_path, to_path, tf, stwa.smbFileList);
                waitExternalMediaFileFlushed(stwa, sti);
            }
        }

        stwa.smbFileList = null;
        return sync_result;
    }

    static private void waitExternalMediaFileFlushed(SyncThreadWorkArea stwa, SyncTaskItem sti) {
        if (stwa.lastWriteSafFile!=null) {
            long b_time=System.currentTimeMillis();
            int count=30;
            String fp=stwa.lastWriteFile.getPath();
            File lf=stwa.lastWriteFile;
            SafFile sf=null;
//            while(count>0 && (stwa.lastWriteSafFile.lastModified()!=stwa.lastWriteFile.lastModified() ||
//                              stwa.lastWriteSafFile.length()!=stwa.lastWriteFile.length() ) )  {
//                SystemClock.sleep(100);
//                count--;
//            }
            while(count>0)  {
//                if (sf==null) sf=stwa.gp.safMgr.findSdcardItem(fp);
                if (sf==null) {
                    if (fp.startsWith(stwa.gp.safMgr.getSdcardRootPath())) sf=stwa.gp.safMgr.findSdcardItem(fp);
                    else sf=stwa.gp.safMgr.findUsbItem(fp);
                }
                if (sf!=null) {
                    if (sf.lastModified()==lf.lastModified() && sf.length()==lf.length()) {
                        break;
                    }
                }
                SystemClock.sleep(500);
                count--;
            }
            if (count==0) stwa.util.addDebugMsg(1,"I","External media file flush wait time over occured");
            else stwa.util.addDebugMsg(1,"I","External media file flush wait ended, elapsed time="+(System.currentTimeMillis()-b_time));
        }
    }

    static public int syncCopySmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        JcifsFile mf = null;
        stwa.lastWriteSafFile=null;
        stwa.lastWriteFile=null;
        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        int sync_result=moveCopySmbToExternal(stwa, sti, false, from_path, from_path, mf, to_path, to_path, null);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) waitExternalMediaFileFlushed(stwa, sti);
        return sync_result;
    }

    static public int syncMoveSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_path, String to_path) {
        JcifsFile mf = null;
        stwa.lastWriteSafFile=null;
        stwa.lastWriteFile=null;

        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        int sync_result=moveCopySmbToExternal(stwa, sti, true, from_path, from_path, mf, to_path, to_path, null);
        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) waitExternalMediaFileFlushed(stwa, sti);
        return sync_result;
    }

    static private int moveCopySmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                             String from_base, String from_path, JcifsFile mf, String to_base, String to_path, ArrayList<String> smb_fl) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncOptionSyncEmptyDirectory()) {
                            if (!sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) {
                                SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
                            }
                        }
                        JcifsFile[] children = mf.listFiles();
                        if (children != null) {
                            for (JcifsFile element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isFile()) {
                                            sync_result = moveCopySmbToExternal(stwa, sti, move_file, from_base,
                                                    from_path + element.getName(), element, to_base, to_path + "/" + element.getName(), smb_fl);
                                            if (smb_fl != null && sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS)
                                                smb_fl.add(element.getPath());
                                        } else {
                                            if (sti.isSyncOptionSyncSubDirectory()) {
                                                sync_result = moveCopySmbToExternal(stwa, sti, move_file, from_base, from_path + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName().replace("/", ""), smb_fl);
                                            } else {
                                                if (stwa.gp.settingDebugLevel >= 1)
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled()) {
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
                        String parsed_to_path=to_path;
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword() && stwa.replaceKeywordRequiredAtWhileSync)
                            parsed_to_path=convertToExifDateTime(stwa, sti, mf.getInputStream(), mf.getLastModified(), from_path, to_path);
                        tf = new File(parsed_to_path);
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()){
                            SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());
                        }
                        boolean tf_exists = tf.exists();
                        if (!tf_exists || tf.isFile()) {
                            if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                                //Ignore override the file
                                if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY)) {
                                    if (move_file) stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                                    else stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                                }
                            } else {
                                if (move_file) {
                                    if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, parsed_to_path)) {
                                        if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                                SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.getLastModified(), tf.lastModified())) {
                                            while (stwa.syncTaskRetryCount > 0) {
                                                sync_result = SyncThreadCopyFile.copyFileSmbToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
//                                                sf = stwa.gp.safMgr.createSdcardItem(parsed_to_path, false);
                                                    sf=SyncThread.createSafFile(stwa, sti, parsed_to_path, false);
                                                    if (Build.VERSION.SDK_INT>=24 && stwa.lastModifiedIsFunctional) {
                                                        SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                    } else {
                                                        SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                                parsed_to_path, sf.lastModified(), mf.getLastModified());
                                                    }
                                                    SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                    stwa.lastWriteSafFile=sf;
                                                    stwa.lastWriteFile=tf;
                                                }
                                                stwa.totalCopyCount++;
                                                SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.masterAuth);
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                        stwa.msgs_mirror_task_file_moved);
                                            }
                                        } else {
                                            SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.masterAuth);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                    }
                                } else {
                                    if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                            SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.getLastModified(), tf.lastModified())) {
                                        if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, parsed_to_path)) {
                                            while (stwa.syncTaskRetryCount > 0) {
                                                sync_result = SyncThreadCopyFile.copyFileSmbToExternal(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                        mf, parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(), tmsg);
                                                if (!sti.isSyncTestMode()) {
                                                    SafFile sf = null;
//                                                sf = stwa.gp.safMgr.createSdcardItem(parsed_to_path, false);
                                                    sf=SyncThread.createSafFile(stwa, sti, parsed_to_path, false);
                                                    if (Build.VERSION.SDK_INT>=24 && stwa.lastModifiedIsFunctional) {
                                                        SyncThread.deleteLocalFileLastModifiedEntry(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList, parsed_to_path);
                                                    } else {
                                                        SyncThread.updateLocalFileLastModifiedList(stwa, stwa.currLastModifiedList, stwa.newLastModifiedList,
                                                                parsed_to_path, sf.lastModified(), mf.getLastModified());
                                                    }
                                                    SyncThread.scanMediaFile(stwa, parsed_to_path);
                                                    stwa.lastWriteSafFile=sf;
                                                    stwa.lastWriteFile=tf;
                                                }
                                                stwa.totalCopyCount++;
                                            }
                                        } else {
                                            stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                        }
                                    }
                                }
                                if (!stwa.gp.syncThreadCtrl.isEnabled())
                                    sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                            }
                        } else {
                            stwa.util.addLogMsg("E", stwa.gp.appContext.getString(R.string.msgs_mirror_directory_with_same_name_as_the_file_found)+parsed_to_path);
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncMirrorSmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                         String from_path, String to_path) {
        JcifsFile mf = null;
        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() +
                    " From=" + from_path + ", Master file error");
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        JcifsFile tf = null;
        try {
            tf = new JcifsFile(to_path, stwa.targetAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() +
                    " To=" + to_path + ", Target file instance creation error occurred during sync delete process");
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        stwa.smbFileList = new ArrayList<String>();
        int sync_result =0;

        if (sti.isSyncOptionDeleteFirstWhenMirror()) {
            sync_result = syncDeleteSmbToSmb(stwa, sti, from_path, from_path, to_path, to_path, tf, stwa.smbFileList);
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS)
                sync_result =moveCopySmbToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path, stwa.smbFileList);
        } else {
            sync_result =sync_result =moveCopySmbToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path, stwa.smbFileList);
            Collections.sort(stwa.smbFileList);
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS)
                syncDeleteSmbToSmb(stwa, sti, from_path, from_path, to_path, to_path, tf, stwa.smbFileList);
        }

        stwa.smbFileList = null;
        return sync_result;
    }

    static public int syncCopySmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                       String from_path, String to_path) {
        JcifsFile mf = null;
        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        int sr = moveCopySmbToSmb(stwa, sti, false, from_path, from_path, mf, to_path, to_path, null);
        return sr;
    }

    static public int syncMoveSmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                       String from_path, String to_path) {
        JcifsFile mf = null;
        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return moveCopySmbToSmb(stwa, sti, true, from_path, from_path, mf, to_path, to_path, null);
    }

    static private int moveCopySmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                        String from_base, String from_path, JcifsFile mf, String to_base, String to_path, ArrayList<String> smb_fl) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path + ", move=" + move_file);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        JcifsFile tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncOptionSyncEmptyDirectory()) {
                            if (!sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) {
                                SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.targetAuth);
                            }
                        }
                        JcifsFile[] children = mf.listFiles();
                        if (children != null) {
                            for (JcifsFile element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isFile()) {
                                            sync_result = moveCopySmbToSmb(stwa, sti, move_file, from_base, from_path + element.getName(),
                                                    element, to_base, to_path + element.getName(), smb_fl);
                                            if (smb_fl != null && sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS)
                                                stwa.smbFileList.add(element.getPath());
                                        } else {
                                            if (sti.isSyncOptionSyncSubDirectory()) {
                                                sync_result = moveCopySmbToSmb(stwa, sti, move_file, from_base, from_path + element.getName(),
                                                        element, to_base, to_path + element.getName(), smb_fl);
                                            } else {
                                                if (stwa.gp.settingDebugLevel >= 1)
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled()) {
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
                        String parsed_to_path=to_path;
                        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword() && stwa.replaceKeywordRequiredAtWhileSync)
                            parsed_to_path=convertToExifDateTime(stwa, sti, mf.getInputStream(), mf.getLastModified(), from_path, to_path);
                        if (from_path.equals(parsed_to_path)) {
                            stwa.util.addLogMsg("W","Ignoring sync because master and target are the same file. File="+from_path);
                        } else {
                            tf = new JcifsFile(parsed_to_path, stwa.targetAuth);
                            if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword())
                                SyncThread.createDirectoryToSmb(stwa, sti, tf.getParent(), stwa.targetAuth);
                            boolean tf_exists = tf.exists();
                            if (!tf_exists || tf.isFile()) {
                                if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                                    //Ignore override the file
                                    if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY)) {
                                        if (move_file) stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                                        else stwa.util.addLogMsg("W", parsed_to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                                    }
                                } else {
                                    if (move_file) {
                                        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, parsed_to_path)) {
                                            if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                                    SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.getLastModified(), tf.getLastModified())) {
                                                while (stwa.syncTaskRetryCount > 0) {
                                                    sync_result = SyncThreadCopyFile.copyFileSmbToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                            parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                    if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                                    stwa.totalCopyCount++;
                                                    SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.masterAuth);
                                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                            stwa.msgs_mirror_task_file_moved);
                                                }
                                            } else {
                                                SyncThread.deleteSmbItem(stwa, false, sti, to_base, from_path, stwa.masterAuth);
                                                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(),
                                                        stwa.msgs_mirror_task_file_moved);
                                            }
                                        } else {
                                            stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                        }
                                    } else {
                                        if (SyncThread.isFileChanged(stwa, sti, parsed_to_path, tf, mf, stwa.ALL_COPY) &&
                                                SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, parsed_to_path, mf.getLastModified(), tf.getLastModified())) {
                                            if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, parsed_to_path)) {
                                                while (stwa.syncTaskRetryCount > 0) {
                                                    sync_result = SyncThreadCopyFile.copyFileSmbToSmb(stwa, sti, from_path.substring(0, from_path.lastIndexOf("/")),
                                                            parsed_to_path.substring(0, parsed_to_path.lastIndexOf("/")), mf.getName());
                                                    if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", parsed_to_path, mf.getName(), tmsg);
                                                    stwa.totalCopyCount++;
                                                }
                                            } else {
                                                stwa.util.addLogMsg("W", parsed_to_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                            }
                                        }
                                    }
                                    if (!stwa.gp.syncThreadCtrl.isEnabled()) sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                }
                            } else {
                                stwa.util.addLogMsg("E", stwa.gp.appContext.getString(R.string.msgs_mirror_directory_with_same_name_as_the_file_found)+parsed_to_path);
                                sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                            }
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    static final private int syncDeleteSmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_base,
                                                String master_dir, String to_base, String target_dir, JcifsFile tf, ArrayList<String> smb_fl) {
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " master=", master_dir, ", target=", target_dir);
        try {
            String tmp_target_dir = target_dir.substring(to_base.length());
            if (tmp_target_dir.startsWith("/")) tmp_target_dir = tmp_target_dir.substring(1);
            if (tf.isDirectory()) { // Directory Delete
                if (//!SyncThread.isDirectoryExcluded(stwa, tmp_target_dir) &&
                        !SyncThread.isHiddenDirectory(stwa, sti, tf) &&
                                SyncThread.isDirectoryToBeProcessed(stwa, tmp_target_dir)) {
                    if (isSmbFileExitst(stwa, smb_fl, master_dir)) {
                        JcifsFile[] children = tf.listFiles();
                        if (children != null) {
                            for (JcifsFile element : children) {
                                String tmp_fname = element.getName();
                                if (element.isFile() || (element.isDirectory() && sti.isSyncOptionSyncSubDirectory())) {
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
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && SyncThread.isRetryRequiredError(stwa.jcifsNtStatusCode)) {
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
                                if (!stwa.gp.syncThreadCtrl.isEnabled() || sync_result != SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!stwa.gp.syncThreadCtrl.isEnabled())
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
                            SyncThread.deleteSmbItem(stwa, true, sti, target_dir, target_dir, stwa.targetAuth);
                        } else {
                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                    " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
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
                                    SyncThread.deleteSmbItem(stwa, true, sti, to_base, target_dir, stwa.targetAuth);
                                } else {
                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", target_dir, tf.getName(),
                                            " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            putErrorMessageIOE(stwa, sti, e, master_dir, target_dir);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putErrorMessageJcifs(stwa, sti,e, master_dir, target_dir);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    static private int waitRetryInterval(SyncThreadWorkArea stwa) {
        int result = 0;
        if (!stwa.gp.syncThreadCtrl.isEnabled()) {
            result = SyncTaskItem.SYNC_STATUS_CANCEL;
        } else {
            synchronized (stwa.gp.syncThreadCtrl) {
                try {
                    stwa.gp.syncThreadCtrl.wait(1000 * SyncThread.SYNC_RETRY_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!stwa.gp.syncThreadCtrl.isEnabled())
                result = SyncTaskItem.SYNC_STATUS_CANCEL;
        }
        return result;
    }

    static private void putErrorMessageIOE(SyncThreadWorkArea stwa, SyncTaskItem sti, IOException e, String from_path, String to_path) {
        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
        if (e.getCause()!=null) SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getCause().toString());

        SyncThread.printStackTraceElement(stwa, e.getStackTrace());
        stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
    }

    static private void putErrorMessageJcifs(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsException e, String from_path, String to_path) {
        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", "NT Status="+String.format("0x%8x",e.getNtStatus()));

        if (e.getCause()!=null) SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getCause().toString());
        stwa.jcifsNtStatusCode=e.getNtStatus();
        SyncThread.printStackTraceElement(stwa, e.getStackTrace());
        stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
    }

}
