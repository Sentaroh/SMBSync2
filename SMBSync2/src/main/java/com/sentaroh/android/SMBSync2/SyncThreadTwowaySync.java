package com.sentaroh.android.SMBSync2;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

public class SyncThreadTwowaySync {

    static public int syncTwowayInternalToInternal(SyncThread.SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                    String from_base, String from_path, File mf, String to_base, String to_path) {
        int sync_result=syncFileTwowayInternalToInternal(stwa, sti, from_base, from_path, mf, to_base, to_path);
        if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
            File tf=new File(to_path);
            sync_result=syncFileTwowayInternalToInternal(stwa, sti, to_base, to_path, tf, from_base, from_path);
        }
        return sync_result;
    }

    static private int syncFileTwowayInternalToInternal(SyncThread.SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                  String from_base, String from_path, File mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path);
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
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToInternalStorage(stwa, sti, to_path);
                        }
                        File[] children = mf.listFiles();
                        if (children != null) {
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
                                        sync_result=syncFileTwowayInternalToInternal(stwa, sti, from_base, from_path, element, to_base, to_path);
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
                        tf = new File(to_path);

                        int master_changed=isFileChengedSincePreviousSync(stwa,sti,mf.getPath(),mf.length(), mf.lastModified());
                        int target_changed=isFileChengedSincePreviousSync(stwa,sti,tf.getPath(),tf.length(), tf.lastModified());

                        if (master_changed!=0 && target_changed!=0) {//競合ファイル

                        } else if (master_changed!=0 && target_changed==0) {//Copy master to target
                            copyFileInternalToInternal(stwa, sti, from_path, mf, to_path);
                        } else if (master_changed==0 && target_changed!=0) {//Copy target to master
                            copyFileInternalToInternal(stwa, sti, to_path, tf, from_path);
                        } else if (master_changed==0 && target_changed==0) {
                            //変更なし
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
//            putErrorMessageIOE(stwa, sti, e, from_path, to_path);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static private int copyFileInternalToInternal(SyncThread.SyncThreadWorkArea stwa,
                                 SyncTaskItem sti, String from_path, File mf, String to_path) throws IOException{
        return 0;
    }

    static private SyncFileInfoItem getSyncFileInfo(SyncThread.SyncThreadWorkArea stwa, SyncTaskItem sti, String fpath) {
        SyncFileInfoItem srch=new SyncFileInfoItem();
        srch.filePath=fpath;
        int idx=Collections.binarySearch(stwa.currSyncFileInfoList, srch, new Comparator<SyncFileInfoItem>() {
            @Override
            public int compare(SyncFileInfoItem o1, SyncFileInfoItem o2) {
                return o1.filePath.compareToIgnoreCase(o2.filePath);
            }
        });
        SyncFileInfoItem result=null;
        if (idx<0) {
            for(SyncFileInfoItem item:stwa.newSyncFileInfoList) {
                if (item.filePath.equals(srch.filePath)) {
                    result=item;
                    break;
                }
            }
        } else {
            result=stwa.currSyncFileInfoList.get(idx);
        }
        return result;
    }

    static private void updateSyncFileInfo(SyncThread.SyncThreadWorkArea stwa, SyncTaskItem sti, File mf, File tf) {
        updateSyncFileInfo(stwa, sti, mf.getPath(), System.currentTimeMillis(), mf.length(), mf.lastModified());
        updateSyncFileInfo(stwa, sti, tf.getPath(), System.currentTimeMillis(), tf.length(), tf.lastModified());
    }

    static private void updateSyncFileInfo(SyncThread.SyncThreadWorkArea stwa, SyncTaskItem sti, String fpath,
                                           long sync_time, long file_size, long file_last_modified) {
        SyncFileInfoItem srch=new SyncFileInfoItem();
        srch.filePath=fpath;
        int idx=Collections.binarySearch(stwa.currSyncFileInfoList, srch, new Comparator<SyncFileInfoItem>() {
            @Override
            public int compare(SyncFileInfoItem o1, SyncFileInfoItem o2) {
                return o1.filePath.compareToIgnoreCase(o2.filePath);
            }
        });
        SyncFileInfoItem result=null;
        if (idx<0) {
            for(SyncFileInfoItem item:stwa.newSyncFileInfoList) {
                if (item.filePath.equals(srch.filePath)) {
                    result=item;
                    break;
                }
            }
        } else {
            result=stwa.currSyncFileInfoList.get(idx);
        }
        if (result!=null) {
            result.fileLastModified=file_last_modified;
            result.fileSize=file_size;
            result.syncTime=sync_time;
        } else {
            SyncFileInfoItem new_item=new SyncFileInfoItem();
            new_item.fileLastModified=file_last_modified;
            new_item.fileSize=file_size;
            new_item.syncTime=sync_time;
            stwa.newSyncFileInfoList.add(new_item);
        }
    }

    final static int FILE_CHANGED_REASON_NO_DIFFERENCE=0;
    final static int FILE_CHANGED_REASON_EXISTS=1;
    final static int FILE_CHANGED_REASON_SIZE_OR_LAST_MODIFIED=2;
    static private int isFileChengedSincePreviousSync(SyncThread.SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                      String fpath, long file_size, long file_last_modified) {
        int sync_required=0;
        SyncFileInfoItem prev_info=getSyncFileInfo(stwa,sti,fpath);
        if (prev_info==null) {//前回から追加されたファイル
            sync_required=FILE_CHANGED_REASON_EXISTS;
        } else {
            if (prev_info.fileSize!=file_size || prev_info.fileLastModified!=file_last_modified) {//変更されたファイル
                sync_required=FILE_CHANGED_REASON_SIZE_OR_LAST_MODIFIED;
            }
        }
        return sync_required;
    }

}

class SyncFileInfoItem {
    public long syncTime=0L;
    public String filePath="";
    public long fileSize=0L;
    public long fileLastModified=0L;
}
