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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import com.sentaroh.android.SMBSync2.SyncThread.SyncThreadWorkArea;

public class SyncThreadSyncTwoway {

    @SuppressWarnings("unused")
    static private int syncTwoWayMasterInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                          String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path);
        int sync_result = 0;
        File tf;
        try {
            String t_from_path = from_path.substring(from_base.length());
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
                                                sync_result = syncTwoWayMasterInternalToInternal(stwa, sti, from_base, from_path + "/" + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName());
                                            } else {
                                                if (sti.isSyncSubDirectory()) {
                                                    sync_result = syncTwoWayMasterInternalToInternal(stwa, sti, from_base, from_path + "/" + element.getName(),
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
                        if (SyncThread.isFileChanged(stwa, sti, to_path, tf, mf, stwa.ALL_COPY)) {
                            boolean tf_exists = tf.exists();
                            FileLastModifiedTimeEntry lmli =
                                    FileLastModifiedTime.getLastModifiedLisItemByFilePath(stwa.currLastModifiedList, stwa.newLastModifiedList, to_path);

                            if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, to_path)) {
                                sync_result = SyncThreadCopyFile.copyFileInternalToInternal(stwa, sti, from_path.replace("/" + mf.getName(), ""),
                                        mf, to_path.replace("/" + mf.getName(), ""), mf.getName());
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
                        if (!stwa.gp.syncThreadControl.isEnabled())
                            sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
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

    @SuppressWarnings("unused")
    private long converDateTimeStringToLong(String date_time) {

        String[] date_array = date_time.substring(0, date_time.lastIndexOf(" ")).split("/");
        String[] time_array = date_time.substring(date_time.lastIndexOf(" ") + 1).split(":");

        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(date_array[0]), Integer.parseInt(date_array[1]), Integer.parseInt(date_array[2]),
                Integer.parseInt(time_array[0]), Integer.parseInt(time_array[1]), Integer.parseInt(time_array[2]));
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().getTime();
    }

    ;

//	private boolean isConflicted(SyncTaskItem sti,
//			boolean mf_exists, int mf_length, long mf_lastMod, boolean tf_exists, int tf_length, long tf_lastMod) {
//		if (mf_exists && tf_exists) {
//			if (mf_length==tf_length) {
//				if (mf_lastMod==tf_lastMod) {
//					//no conflict
//				} else {
//					//
//					long last_sync_time=converDateTimeStringToLong(sti.getLastSyncTime());
//					if (mf_lastMod>last_sync_time && tf_lastMod>last_sync_time) {
//						//同期時間より、どちらも新しい						
//						if (mf_lastMod>tf_lastMod) {
//							//
//						} else {
//							
//						}
//					} else if (mf_lastMod>last_sync_time && tf_lastMod<=last_sync_time) {
//						//同期時間より、マスターが新しい
//					} else if (mf_lastMod<=last_sync_time && tf_lastMod<=last_sync_time) {	
//						//同期時間より、どちらも古い
//					} else if (mf_lastMod<=last_sync_time && tf_lastMod>last_sync_time) {
//						//同期時間よりもターゲットが新しい
//					}
//							
//				}
//			}
//		}
//		
//		
//		
//	};
}
