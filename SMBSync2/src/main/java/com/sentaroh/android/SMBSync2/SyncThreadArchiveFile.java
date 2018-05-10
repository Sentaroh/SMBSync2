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

import android.media.ExifInterface;
import android.util.Log;

import static com.sentaroh.android.SMBSync2.Constants.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import com.sentaroh.android.SMBSync2.SyncThread.SyncThreadWorkArea;
import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.android.Utilities.SafFileManager;
import com.sentaroh.android.Utilities.StringUtil;

public class SyncThreadArchiveFile {

    static public int syncArchiveInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = archiveDirectoryInternalToInternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                             File mf, File tf, String to_path, String file_name) throws IOException {
        int sync_result=0;
        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            if (!sti.isSyncTestMode()) {
                String dir=tf.getParent();
                File lf_dir=new File(dir);
                if (!lf_dir.exists()) lf_dir.mkdirs();

                File temp_file=new File(stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp");
                sync_result= copyFile(stwa, sti, new FileInputStream(mf),
                        new FileOutputStream(temp_file), from_path, to_path, file_name, sti.isSyncUseSmallIoBuffer());
                if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                    temp_file.setLastModified(mf.lastModified());
                    temp_file.renameTo(tf);
                }
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, tf.getName(),
                        stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    tf.setLastModified(mf.lastModified());
                    mf.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, mf.getPath());
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.util.addLogMsg("W", to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
        }

        return sync_result;
    }

    static private int archiveFileInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                     String from_path, String to_path) throws IOException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildLocalFileList(stwa, sti, children, to_path);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadControl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            file_seq_no++;
            String to_file_name="", to_file_ext="", to_file_seqno="";
            if (item.file_name.lastIndexOf(".")>=0) {
                to_file_ext=item.file_name.substring(item.file_name.lastIndexOf("."));
                to_file_name=item.file_name.substring(0,item.file_name.lastIndexOf("."));
            } else {
                to_file_name=item.file_name;
            }
            to_file_seqno=getFileSeqNumber(stwa, sti, file_seq_no);

            if (!sti.isArchiveUseRename()) {
                File tf=new File(to_path+"/"+item.file_name);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+to_file_name, to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileInternalToInternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileInternalToInternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), to_file_name);
                }
            } else {
                to_file_name=buildArchiveFileName(stwa, sti, item, to_file_name);
                String temp_dir=buildArchiveDirectoryName(stwa, sti, item);

                File tf=new File(to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileInternalToInternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileInternalToInternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(),
                            to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int archiveDirectoryInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                          String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
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
                            archiveFileInternalToInternal(stwa, sti, children, from_path, to_path);
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
                                        if (!from_path.equals(to_path)) {
                                            if (element.isDirectory()) {
                                                if (sti.isSyncSubDirectory()) {
                                                    sync_result = archiveDirectoryInternalToInternal(stwa, sti, from_base, from_path + "/" + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName());
                                                } else {
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=", from_path);
                                                }
                                            }
                                        } else {
                                            stwa.util.addDebugMsg(1, "W",
                                                    String.format(stwa.gp.appContext.getString(R.string.msgs_mirror_same_directory_ignored),
                                                            from_path, "/", element.getName()));
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
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncArchiveInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = archiveDirectoryInternalToExternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                  File mf, File tf, String to_path, String file_name) throws IOException {
        int sync_result=0;
        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            if (!sti.isSyncTestMode()) {
                SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());
                SafFile t_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_path, false);
                if (t_df == null) {
                    String saf_name = "";
                    SafFile sf = stwa.gp.safMgr.getSdcardSafFile();
                    if (sf != null) saf_name = sf.getName();
                    stwa.util.addLogMsg("E", "SAF file not found error. path=", to_path, ", SafFile=", saf_name, ", sdcard=", stwa.gp.safMgr.getSdcardDirectory());
                    ArrayList<SafFileManager.SafFileItem> sl = stwa.gp.safMgr.getSafList();
                    for (SafFileManager.SafFileItem sfi : sl) {
                        stwa.util.addLogMsg("E", "SafFileItem UUID=", sfi.storageUuid, ", path=", sfi.storageRootDirectory,
                                ", mount="+sfi.storageIsMounted, ", sdcard="+sfi.storageTypeSdcard);
                    }
                    return SyncTaskItem.SYNC_STATUS_ERROR;
                }
                sync_result= copyFile(stwa, sti, new FileInputStream(mf),
                        stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri()), from_path, to_path, file_name, sti.isSyncUseSmallIoBuffer());
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, tf.getName(),
                        stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    mf.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, mf.getPath());
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.util.addLogMsg("W", to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
        }
        return sync_result;
    }

    static private int archiveFileInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                     String from_path, String to_path) throws IOException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildLocalFileList(stwa, sti, children, to_path);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadControl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            file_seq_no++;
            String to_file_name="", to_file_ext="", to_file_seqno="";
            if (item.file_name.lastIndexOf(".")>=0) {
                to_file_ext=item.file_name.substring(item.file_name.lastIndexOf("."));
                to_file_name=item.file_name.substring(0,item.file_name.lastIndexOf("."));
            } else {
                to_file_name=item.file_name;
            }
            to_file_seqno=getFileSeqNumber(stwa, sti, file_seq_no);

            if (!sti.isArchiveUseRename()) {
                File tf=new File(to_path+"/"+item.file_name);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+to_file_name, to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileInternalToExternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileInternalToExternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), to_file_name);
                }
            } else {
                to_file_name=buildArchiveFileName(stwa, sti, item, to_file_name);
                String temp_dir=buildArchiveDirectoryName(stwa, sti, item);

                File tf=new File(to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileInternalToExternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileInternalToExternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(),
                            to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }


    static private int archiveDirectoryInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                          String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
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
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
                        }
                        File[] children = mf.listFiles();
                        archiveFileInternalToExternal(stwa, sti, children, from_path, to_path);
                        if (children != null) {
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
                                        if (element.isDirectory()) {
                                            if (sti.isSyncSubDirectory()) {
                                                sync_result = archiveDirectoryInternalToExternal(stwa, sti, from_base, from_path + "/" + element.getName(),
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
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncArchiveInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = archiveDirectoryInternalToSmb(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int archiveFileInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                String from_path, String to_path) throws IOException, JcifsException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildLocalFileList(stwa, sti, children, to_path);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadControl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            file_seq_no++;
            String to_file_name="", to_file_ext="", to_file_seqno="";
            if (item.file_name.lastIndexOf(".")>=0) {
                to_file_ext=item.file_name.substring(item.file_name.lastIndexOf("."));
                to_file_name=item.file_name.substring(0,item.file_name.lastIndexOf("."));
            } else {
                to_file_name=item.file_name;
            }
            to_file_seqno=getFileSeqNumber(stwa, sti, file_seq_no);

            if (!sti.isArchiveUseRename()) {//Renameしない
                JcifsFile jf=new JcifsFile(to_path+"/"+item.file_name, stwa.targetAuth);
                if (jf.exists()) {
                    String new_name=createArchiveSmbNewFilePath(stwa, sti, to_path, to_path+"/"+to_file_name, to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        jf=new JcifsFile(new_name, stwa.targetAuth);
                        sync_result= moveFileInternalToSmb(stwa, sti, item.full_path, (File)item.file, jf, jf.getPath());
                    }
                } else {
                    sync_result= moveFileInternalToSmb(stwa, sti, item.full_path, (File)item.file, jf, jf.getPath());
                }
            } else {//Renameする
                to_file_name=buildArchiveFileName(stwa, sti, item, to_file_name);
                String temp_dir=buildArchiveDirectoryName(stwa, sti, item);

                JcifsFile jf=new JcifsFile(to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext, stwa.targetAuth);
                if (jf.exists()) {
                    String new_name=createArchiveSmbNewFilePath(stwa, sti, to_path, to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        jf=new JcifsFile(new_name, stwa.targetAuth);
                        sync_result= moveFileInternalToSmb(stwa, sti, item.full_path, (File)item.file, jf, jf.getPath());
                    }
                } else {
                    sync_result= moveFileInternalToSmb(stwa, sti, item.full_path, (File)item.file, jf, jf.getPath());
                }
            }
        }
        return sync_result;
    }

    static private int archiveDirectoryInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                     String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
        stwa.jcifsNtStatusCode=0;
        int sync_result = 0;
        JcifsFile tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.targetAuth);
                        }
                        File[] children = mf.listFiles();
                        if (children != null) {
                            archiveFileInternalToSmb(stwa, sti, children, from_path, to_path);
                            for (File element : children) {
                                if (element.isDirectory()) {
                                    if (!element.getName().equals(".android_secure")) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            if (sti.isSyncSubDirectory()) {
                                                sync_result = archiveDirectoryInternalToSmb(stwa, sti, from_base, from_path + "/" + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName());
                                            }
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && stwa.jcifsNtStatusCode!=0xc000006d) {
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
                                    if (!stwa.gp.syncThreadControl.isEnabled()) {
                                        sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                        break;
                                    }
                                }
                            }
                        } else {
                            stwa.util.addDebugMsg(1, "I", "Directory was null, dir=" + mf.getPath());
                        }
                    } else {
                        if (!mf.canRead())
                            stwa.util.addDebugMsg(1, "I", "Directory ignored because can not read, fp=" + from_path + "/" + mf.getName());
                    }
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static private void putExceptionMsg(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, String to_path, Exception e) {
        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
        if (e.getCause()!=null) SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getCause().toString());
        if (e instanceof JcifsException) stwa.jcifsNtStatusCode=((JcifsException)e).getNtStatus();
        SyncThread.printStackTraceElement(stwa, e.getStackTrace());
        stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
    }

    static private String createArchiveSmbNewFilePath(SyncThreadWorkArea stwa, SyncTaskItem sti, String path, String fn, String fe)
            throws MalformedURLException, JcifsException {
        String result="";

        for (int i=1;i<100000;i++) {
            String suffix_base=String.format("_%d", i);
            JcifsFile jf=new JcifsFile(fn+suffix_base+fe, stwa.targetAuth);
            if (!jf.exists()) {
                result=fn+suffix_base+fe;
                break;
            }
        }

        return result;
    }

    static private String createArchiveLocalNewFilePath(SyncThreadWorkArea stwa, SyncTaskItem sti, String path, String fn, String fe) {
        String result="";

        for (int i=1;i<100000;i++) {
            String suffix_base=String.format("_%d", i);
            File jf=new File(fn+suffix_base+fe);
            if (!jf.exists()) {
                result=fn+suffix_base+fe;
                break;
            }
        }

        return result;
    }

    static private int moveFileInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                             File mf, JcifsFile tf, String to_path) throws IOException, JcifsException {
        int sync_result=0;
        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            if (!sti.isSyncTestMode()) {
                String dir=tf.getParent();
                JcifsFile jf_dir=new JcifsFile(dir,stwa.targetAuth);
                if (sti.isArchiveCreateDirectory()) {
                    if (!jf_dir.exists()) jf_dir.mkdirs();
                }
                while (stwa.syncTaskRetryCount > 0) {
                    sync_result= copyFile(stwa, sti, new FileInputStream(mf), tf.getOutputStream(), from_path, to_path,
                            tf.getName(), sti.isSyncUseSmallIoBuffer());
                    if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && stwa.jcifsNtStatusCode!=0xc000006d) {
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
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, tf.getName(),
                        stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    try {
                        tf.setLastModified(mf.lastModified());
                    } catch(JcifsException e) {
                        // nop
                    }
                    mf.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, mf.getPath());
                }
            }
        } else {
            stwa.util.addLogMsg("W", to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
        }
        return sync_result;
    }

    static public int syncArchiveExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = archiveDirectoryExternalToInternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                  File mf, File tf, String to_path, String file_name) throws IOException {
        int sync_result=0;
        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            SafFile m_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), from_path, false);
            if (!sti.isSyncTestMode()) {
                SyncThread.createDirectoryToInternalStorage(stwa, sti, tf.getParent());
                File temp_file=new File(stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp");
                sync_result= copyFile(stwa, sti,  stwa.gp.appContext.getContentResolver().openInputStream(m_df.getUri()),
                        new FileOutputStream(temp_file), from_path, to_path, file_name, sti.isSyncUseSmallIoBuffer());
                if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                    temp_file.setLastModified(mf.lastModified());
                    temp_file.renameTo(tf);
                }
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, tf.getName(),
                        stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    m_df.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, from_path);
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.util.addLogMsg("W", to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
        }
        return sync_result;
    }

    static private int archiveFileExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                     String from_path, String to_path) throws IOException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildSdcardFileList(stwa, sti, children, to_path);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadControl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            file_seq_no++;
            String to_file_name="", to_file_ext="", to_file_seqno="";
            if (item.file_name.lastIndexOf(".")>=0) {
                to_file_ext=item.file_name.substring(item.file_name.lastIndexOf("."));
                to_file_name=item.file_name.substring(0,item.file_name.lastIndexOf("."));
            } else {
                to_file_name=item.file_name;
            }
            to_file_seqno=getFileSeqNumber(stwa, sti, file_seq_no);

            if (!sti.isArchiveUseRename()) {
                File tf=new File(to_path+"/"+item.file_name);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+to_file_name, to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileExternalToInternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileExternalToInternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), to_file_name);
                }
            } else {
                to_file_name=buildArchiveFileName(stwa, sti, item, to_file_name);
                String temp_dir=buildArchiveDirectoryName(stwa, sti, item);

                File tf=new File(to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileExternalToInternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileExternalToInternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(),
                            to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int archiveDirectoryExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                          String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
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
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToInternalStorage(stwa, sti, to_path);
                        }
                        File[] children = mf.listFiles();
                        if (children != null) {
                            archiveFileExternalToInternal(stwa, sti, children, from_path, to_path);
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
                                        if (element.isDirectory()) {
                                            if (sti.isSyncSubDirectory()) {
                                                sync_result = archiveDirectoryExternalToInternal(stwa, sti, from_base, from_path + "/" + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName());
                                            } else {
                                                stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=", from_path);
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
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncArchiveExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = archiveDirectoryExternalToExternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                  File mf, File tf, String to_path, String file_name) throws IOException {
        int sync_result=0;
        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            SafFile m_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), from_path, false);
            if (!sti.isSyncTestMode()) {
                SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());
                SafFile t_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_path, false);
                if (t_df == null) {
                    String saf_name = "";
                    SafFile sf = stwa.gp.safMgr.getSdcardSafFile();
                    if (sf != null) saf_name = sf.getName();
                    stwa.util.addLogMsg("E", "SAF file not found error. path=", to_path, ", SafFile=", saf_name, ", sdcard=", stwa.gp.safMgr.getSdcardDirectory());
                    ArrayList<SafFileManager.SafFileItem> sl = stwa.gp.safMgr.getSafList();
                    for (SafFileManager.SafFileItem sfi : sl) {
                        stwa.util.addLogMsg("E", "SafFileItem UUID=", sfi.storageUuid, ", path=", sfi.storageRootDirectory,
                                ", mount="+sfi.storageIsMounted, ", sdcard="+sfi.storageTypeSdcard);
                    }
                    return SyncTaskItem.SYNC_STATUS_ERROR;
                }
                sync_result= copyFile(stwa, sti, stwa.gp.appContext.getContentResolver().openInputStream(m_df.getUri()),
                        stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri()), from_path, to_path, file_name, sti.isSyncUseSmallIoBuffer());
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, tf.getName(),
                        stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    m_df.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, from_path);
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.util.addLogMsg("W", to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
        }
        return sync_result;
    }

    static private int archiveFileExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                     String from_path, String to_path) throws IOException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildSdcardFileList(stwa, sti, children, to_path);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadControl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            file_seq_no++;
            String to_file_name="", to_file_ext="", to_file_seqno="";
            if (item.file_name.lastIndexOf(".")>=0) {
                to_file_ext=item.file_name.substring(item.file_name.lastIndexOf("."));
                to_file_name=item.file_name.substring(0,item.file_name.lastIndexOf("."));
            } else {
                to_file_name=item.file_name;
            }
            to_file_seqno=getFileSeqNumber(stwa, sti, file_seq_no);

            if (!sti.isArchiveUseRename()) {
                File tf=new File(to_path+"/"+item.file_name);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+to_file_name, to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileExternalToExternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileExternalToExternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), to_file_name);
                }
            } else {
                to_file_name=buildArchiveFileName(stwa, sti, item, to_file_name);
                String temp_dir=buildArchiveDirectoryName(stwa, sti, item);

                File tf=new File(to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileExternalToExternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileExternalToExternal(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(),
                            to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int archiveDirectoryExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                          String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName(), " entered, from=", from_path, ", to=" + to_path);
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
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
                        }
                        File[] children = mf.listFiles();
                        archiveFileExternalToExternal(stwa, sti, children, from_path, to_path);
                        if (children != null) {
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
                                        if (!from_path.equals(to_path)) {
                                            if (element.isDirectory()) {
                                                if (sti.isSyncSubDirectory()) {
                                                    sync_result = archiveDirectoryExternalToExternal(stwa, sti, from_base, from_path + "/" + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName());
                                                } else {
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
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncArchiveExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = archiveDirectoryExternalToSmb(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                  File mf, JcifsFile tf, String to_path, String file_name) throws IOException, JcifsException {
        int sync_result=0;

        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            SafFile m_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), from_path, false);
            if (!sti.isSyncTestMode()) {
                String dir=tf.getParent();
                JcifsFile jf_dir=new JcifsFile(dir,stwa.targetAuth);
                if (sti.isArchiveCreateDirectory()) {
                    if (!jf_dir.exists()) jf_dir.mkdirs();
                }
                while (stwa.syncTaskRetryCount > 0) {
                    sync_result= copyFile(stwa, sti, stwa.gp.appContext.getContentResolver().openInputStream(m_df.getUri()),
                            tf.getOutputStream(), from_path, to_path,
                            tf.getName(), sti.isSyncUseSmallIoBuffer());
                    if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && stwa.jcifsNtStatusCode!=0xc000006d) {
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
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, tf.getName(),
                        stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    try {
                        tf.setLastModified(mf.lastModified());
                    } catch(JcifsException e) {
                        // nop
                    }
                    if (!sti.isSyncTestMode()) {
                        stwa.totalDeleteCount++;
                        m_df.delete();
                        SyncThread.scanMediaFile(stwa, from_path);
                    }
                }
            }
        } else {
            stwa.util.addLogMsg("W", to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
        }

        return sync_result;
    }

    static private int archiveFileExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                String from_path, String to_path) throws IOException, JcifsException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildSdcardFileList(stwa, sti, children, to_path);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadControl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            file_seq_no++;
            String to_file_name="", to_file_ext="", to_file_seqno="";
            if (item.file_name.lastIndexOf(".")>=0) {
                to_file_ext=item.file_name.substring(item.file_name.lastIndexOf("."));
                to_file_name=item.file_name.substring(0,item.file_name.lastIndexOf("."));
            } else {
                to_file_name=item.file_name;
            }
            to_file_seqno=getFileSeqNumber(stwa, sti, file_seq_no);

            if (!sti.isArchiveUseRename()) {
                JcifsFile tf=new JcifsFile(to_path+"/"+item.file_name, stwa.targetAuth);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+to_file_name, to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new JcifsFile(new_name, stwa.targetAuth);
                        sync_result= moveFileExternalToSmb(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileExternalToSmb(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), to_file_name);
                }
            } else {
                to_file_name=buildArchiveFileName(stwa, sti, item, to_file_name);
                String temp_dir=buildArchiveDirectoryName(stwa, sti, item);

                JcifsFile tf=new JcifsFile(to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext, stwa.targetAuth);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new JcifsFile(new_name, stwa.targetAuth);
                        sync_result= moveFileExternalToSmb(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileExternalToSmb(stwa, sti, item.full_path, (File)item.file, tf, tf.getPath(),
                            to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int archiveDirectoryExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                     String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
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
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.targetAuth);
                        }
                        File[] children = mf.listFiles();
                        if (children != null) {
                            archiveFileExternalToSmb(stwa, sti, children, from_path, to_path);
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            if (element.isDirectory()) {
                                                if (sti.isSyncSubDirectory()) {
                                                    sync_result = archiveDirectoryExternalToSmb(stwa, sti, from_base, from_path + "/" + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName());
                                                } else {
                                                    if (stwa.gp.settingDebugLevel >= 1)
                                                        stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                                }
                                            }
                                            if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && stwa.jcifsNtStatusCode!=0xc000006d) {
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
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncArchiveSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, String to_path) {
        JcifsFile mf = null;
        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        int sync_result = archiveDirectorySmbToInternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                             JcifsFile mf, File tf, String to_path, String file_name) throws IOException, JcifsException {
        int sync_result=0;

        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            if (!sti.isSyncTestMode()) {
                String dir=tf.getParent();
                File lf_dir=new File(dir);
                if (!lf_dir.exists()) lf_dir.mkdirs();
                File temp_file=new File(stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp");
                while (stwa.syncTaskRetryCount > 0) {
                    sync_result= copyFile(stwa, sti, mf.getInputStream(), new FileOutputStream(temp_file), from_path, to_path, file_name, sti.isSyncUseSmallIoBuffer());
                    if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                        temp_file.setLastModified(mf.getLastModified());
                        temp_file.renameTo(tf);
                    }
                    if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && stwa.jcifsNtStatusCode!=0xc000006d) {
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
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, tf.getName(),
                        stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    try {
                        tf.setLastModified(mf.getLastModified());
                    } catch(JcifsException e) {
                        // nop
                    }
                    mf.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.util.addLogMsg("W", to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
        }

        return sync_result;
    }

    static private int archiveFileSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile[] children,
                                                String from_path, String to_path) throws IOException, JcifsException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildSmbFileList(stwa, sti, children, to_path);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadControl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            file_seq_no++;
            String to_file_name="", to_file_ext="", to_file_seqno="";
            if (item.file_name.lastIndexOf(".")>=0) {
                to_file_ext=item.file_name.substring(item.file_name.lastIndexOf("."));
                to_file_name=item.file_name.substring(0,item.file_name.lastIndexOf("."));
            } else {
                to_file_name=item.file_name;
            }
            to_file_seqno=getFileSeqNumber(stwa, sti, file_seq_no);

            if (!sti.isArchiveUseRename()) {
                File tf=new File(to_path+"/"+item.file_name);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+to_file_name, to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileSmbToInternal(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileSmbToInternal(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(), to_file_name);
                }
            } else {
                to_file_name=buildArchiveFileName(stwa, sti, item, to_file_name);
                String temp_dir=buildArchiveDirectoryName(stwa, sti, item);

                File tf=new File(to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileSmbToInternal(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileSmbToInternal(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(),
                            to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int archiveDirectorySmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                     String from_base, String from_path, JcifsFile mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
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
                        JcifsFile[] children = mf.listFiles();
                        if (children != null) {
                            archiveFileSmbToInternal(stwa, sti, children, from_path, to_path);
                            for (JcifsFile element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isDirectory()) {
                                            if (sti.isSyncSubDirectory()) {
                                                sync_result = archiveDirectorySmbToInternal(stwa, sti, from_base, from_path + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName().replace("/", ""));
                                            } else {
                                                if (stwa.gp.settingDebugLevel >= 1)
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && stwa.jcifsNtStatusCode!=0xc000006d) {
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
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    static public int syncArchiveSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                              String from_path, String to_path) {
        stwa.smbFileList = new ArrayList<String>();
        JcifsFile mf = null;
        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        int sync_result = archiveDirectorySmbToExternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                             JcifsFile mf, File tf, String to_path, String file_name) throws IOException, JcifsException {
        int sync_result=0;

        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            if (!sti.isSyncTestMode()) {
                SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());
                SafFile t_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_path, false);
                if (t_df == null) {
                    String saf_name = "";
                    SafFile sf = stwa.gp.safMgr.getSdcardSafFile();
                    if (sf != null) saf_name = sf.getName();
                    stwa.util.addLogMsg("E", "SAF file not found error. path=", to_path, ", SafFile=", saf_name, ", sdcard=", stwa.gp.safMgr.getSdcardDirectory());
                    ArrayList<SafFileManager.SafFileItem> sl = stwa.gp.safMgr.getSafList();
                    for (SafFileManager.SafFileItem sfi : sl) {
                        stwa.util.addLogMsg("E", "SafFileItem UUID=", sfi.storageUuid, ", path=", sfi.storageRootDirectory,
                                ", mount="+sfi.storageIsMounted, ", sdcard="+sfi.storageTypeSdcard);
                    }
                    return SyncTaskItem.SYNC_STATUS_ERROR;
                }
                while (stwa.syncTaskRetryCount > 0) {
                    sync_result= copyFile(stwa, sti, mf.getInputStream(), stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri()),
                            from_path, to_path, file_name, sti.isSyncUseSmallIoBuffer());
                    if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && stwa.jcifsNtStatusCode!=0xc000006d) {
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
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, tf.getName(),
                        stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    try {
                        tf.setLastModified(mf.getLastModified());
                    } catch(JcifsException e) {
                        // nop
                    }
                    mf.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.util.addLogMsg("W", to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
        }

        return sync_result;
    }

    static private int archiveFileSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile[] children,
                                                String from_path, String to_path) throws IOException, JcifsException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildSmbFileList(stwa, sti, children, to_path);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadControl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            file_seq_no++;
            String to_file_name="", to_file_ext="", to_file_seqno="";
            if (item.file_name.lastIndexOf(".")>=0) {
                to_file_ext=item.file_name.substring(item.file_name.lastIndexOf("."));
                to_file_name=item.file_name.substring(0,item.file_name.lastIndexOf("."));
            } else {
                to_file_name=item.file_name;
            }
            to_file_seqno=getFileSeqNumber(stwa, sti, file_seq_no);

            if (!sti.isArchiveUseRename()) {
                File tf=new File(to_path+"/"+item.file_name);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+to_file_name, to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileSmbToExternal(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileSmbToExternal(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(), to_file_name);
                }
            } else {
                to_file_name=buildArchiveFileName(stwa, sti, item, to_file_name);
                String temp_dir=buildArchiveDirectoryName(stwa, sti, item);

                File tf=new File(to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, to_path, to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new File(new_name);
                        sync_result= moveFileSmbToExternal(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileSmbToExternal(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(),
                            to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int archiveDirectorySmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                     String from_base, String from_path, JcifsFile mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName(), " entered, from=", from_path + ", to=", to_path);
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
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
                        }
                        JcifsFile[] children = mf.listFiles();
                        if (children != null) {
                            archiveFileSmbToExternal(stwa, sti, children, from_path, to_path);
                            for (JcifsFile element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isDirectory()) {
                                            if (sti.isSyncSubDirectory()) {
                                                sync_result = archiveDirectorySmbToExternal(stwa, sti, from_base, from_path + element.getName(),
                                                        element, to_base, to_path + "/" + element.getName().replace("/", ""));
                                            } else {
                                                if (stwa.gp.settingDebugLevel >= 1)
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && stwa.jcifsNtStatusCode!=0xc000006d) {
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
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncArchiveSmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                         String from_path, String to_path) {
        JcifsFile mf = null;
        try {
            mf = new JcifsFile(from_path, stwa.masterAuth);
        } catch (MalformedURLException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() +
                    " From=" + from_path + ", Master file error");
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            stwa.util.addLogMsg("E", "", SyncUtil.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
            stwa.util.addLogMsg("E", "", e.getMessage());//e.toString());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        int sync_result = archiveDirectorySmbToSmb(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileSmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                             JcifsFile mf, JcifsFile tf, String to_path, String file_name) throws IOException, JcifsException {
        int sync_result=0;

        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            if (!sti.isSyncTestMode()) {
                String dir=tf.getParent();
                JcifsFile jf_dir=new JcifsFile(dir,stwa.targetAuth);
                if (!jf_dir.exists()) jf_dir.mkdirs();
                while (stwa.syncTaskRetryCount > 0) {
                    sync_result= copyFile(stwa, sti, mf.getInputStream(), tf.getOutputStream(), from_path, to_path, file_name, sti.isSyncUseSmallIoBuffer());
                    if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && stwa.jcifsNtStatusCode!=0xc000006d) {
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
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, tf.getName(),
                        stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    try {
                        tf.setLastModified(mf.getLastModified());
                    } catch(JcifsException e) {
                        // nop
                    }
                    mf.delete();
                    stwa.totalDeleteCount++;
                }
            }
        } else {
            stwa.util.addLogMsg("W", to_path, " ", stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
        }

        return sync_result;
    }

    static private int archiveFileSmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile[] children,
                                           String from_path, String to_path) throws IOException, JcifsException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildSmbFileList(stwa, sti, children, to_path);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadControl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            file_seq_no++;
            String to_file_name="", to_file_ext="", to_file_seqno="";
            if (item.file_name.lastIndexOf(".")>=0) {
                to_file_ext=item.file_name.substring(item.file_name.lastIndexOf("."));
                to_file_name=item.file_name.substring(0,item.file_name.lastIndexOf("."));
            } else {
                to_file_name=item.file_name;
            }
            to_file_seqno=getFileSeqNumber(stwa, sti, file_seq_no);

            if (!sti.isArchiveUseRename()) {
                JcifsFile tf=new JcifsFile(to_path+"/"+item.file_name, stwa.targetAuth);
                if (tf.exists()) {
                    String new_name=createArchiveSmbNewFilePath(stwa, sti, to_path, to_path+"/"+to_file_name, to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new JcifsFile(new_name, stwa.targetAuth);
                        sync_result= moveFileSmbToSmb(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileSmbToSmb(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(), to_file_name);
                }
            } else {
                to_file_name=buildArchiveFileName(stwa, sti, item, to_file_name);
                String temp_dir=buildArchiveDirectoryName(stwa, sti, item);

                JcifsFile tf=new JcifsFile(to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext, stwa.targetAuth);
                if (tf.exists()) {
                    String new_name=createArchiveSmbNewFilePath(stwa, sti, to_path, to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
                    if (new_name.equals("")) {
                        stwa.util.addLogMsg("E","Archive sequence number overflow error.");
                        sync_result=SyncTaskItem.SYNC_STATUS_ERROR;
                        break;
                    } else {
                        tf=new JcifsFile(new_name, stwa.targetAuth);
                        sync_result= moveFileSmbToSmb(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(), new_name);
                    }
                } else {
                    sync_result= moveFileSmbToSmb(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(),
                            to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int archiveDirectorySmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                String from_base, String from_path, JcifsFile mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
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
                        if (sti.isSyncEmptyDirectory()) {
                            SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.targetAuth);
                        }
                        JcifsFile[] children = mf.listFiles();
                        if (children != null) {
                            archiveFileSmbToSmb(stwa, sti, children, from_path, to_path);
                            for (JcifsFile element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    while (stwa.syncTaskRetryCount > 0) {
                                        if (element.isDirectory()) {
                                            if (sti.isSyncSubDirectory()) {
                                                sync_result = archiveDirectorySmbToSmb(stwa, sti, from_base, from_path + element.getName(),
                                                        element, to_base, to_path + element.getName());
                                            } else {
                                                if (stwa.gp.settingDebugLevel >= 1)
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                            }
                                        }
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_ERROR && stwa.jcifsNtStatusCode!=0xc000006d) {
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
                }
            } else {
                stwa.gp.syncThreadControl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadControl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (JcifsException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }
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

    final static int SHOW_PROGRESS_THRESHOLD_VALUE=512;

    static private int copyFile(SyncThreadWorkArea stwa, SyncTaskItem sti, InputStream is, OutputStream os, String from_path,
                                String to_path, String file_name, boolean small_buffer) throws IOException {
        stwa.util.addDebugMsg(2, "I", "copyFile from=", from_path, ", to=", to_path);

        int buff_size=0, io_area_size=0;
        if (small_buffer) {
            buff_size=1024*16-1;
            io_area_size=buff_size;
        } else {
            buff_size=1024*1024*4;
            io_area_size=1024*1024*2;
        }

        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        BufferedInputStream ifs = new BufferedInputStream(is, buff_size);
        BufferedOutputStream ofs = new BufferedOutputStream(os, buff_size);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = is.available();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[io_area_size];
        while ((buffer_read_bytes = ifs.read(buffer)) > 0) {
            ofs.write(buffer, 0, buffer_read_bytes);
            file_read_bytes += buffer_read_bytes;
            if (show_prog && file_size > file_read_bytes) {
                SyncThread.showProgressMsg(stwa, sti.getSyncTaskName(), file_name + " " +
                        String.format(stwa.msgs_mirror_task_file_copying, (file_read_bytes * 100) / file_size));
            }
            if (!stwa.gp.syncThreadControl.isEnabled()) {
                ifs.close();
                ofs.flush();
                ofs.close();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        is.close();
        ofs.flush();
        ofs.close();
        os.close();
//        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_dest.setLastModified(mf.lastModified());

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_path + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static private class ArchiveFileListItem {
        Object file=null;
        String shoot_date="", shoot_time="";
        String file_name="";
        String full_path="";
        boolean date_from_exif=true;
    }

    static private ArrayList<ArchiveFileListItem> buildLocalFileList(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children, String to_path) {
        ArrayList<ArchiveFileListItem>fl=new ArrayList<ArchiveFileListItem>();
        for(File element:children) {
            if (element.isFile() && isFileTypeArchiveTarget(element.getName())) {
                String[] date_time=getFileExifDateTime(stwa, sti, element);
                ArchiveFileListItem afli=new ArchiveFileListItem();
                afli.file=element;
                afli.file_name=element.getName();
                afli.full_path=element.getPath();
                if (date_time[0]==null) {
                    String[] dt=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(element.lastModified()).split(" ");
                    afli.shoot_date=dt[0].replace("/","-");
                    afli.shoot_time=dt[1].replace(":","-");
                    afli.date_from_exif=false;
                } else {
                    Log.v("SMBSync2","name="+afli.file_name+", 0="+date_time[0]+", 1="+date_time[1]);
                    afli.shoot_date=date_time[0].replace("/","-");
                    afli.shoot_time=date_time[1].replace(":","-");
                }
                if (isFileArchiveRequired(stwa, sti, afli)) fl.add(afli);
            }
        }
        Collections.sort(fl, new Comparator<ArchiveFileListItem>(){
            @Override
            public int compare(ArchiveFileListItem ri, ArchiveFileListItem li) {
                return (ri.shoot_date+ri.shoot_time+ri.file_name).compareToIgnoreCase(li.shoot_date+li.shoot_time+li.file_name);
            }
        });
        return fl;
    }

    static private ArrayList<ArchiveFileListItem> buildSdcardFileList(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children, String to_path) {
        ArrayList<ArchiveFileListItem>fl=new ArrayList<ArchiveFileListItem>();
        for(File element:children) {
            if (element.isFile() && isFileTypeArchiveTarget(element.getName())) {
                SafFile m_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), element.getPath(), false);
                String[] date_time=getFileExifDateTime(stwa, sti, m_df);
                ArchiveFileListItem afli=new ArchiveFileListItem();
                afli.file=element;
                afli.file_name=element.getName();
                afli.full_path=element.getPath();
                if (date_time==null) {
                    String[] dt=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(element.lastModified()).split(" ");
                    afli.shoot_date=dt[0].replace("/","-");
                    afli.shoot_time=dt[1].replace(":","-");
                    afli.date_from_exif=false;
                } else {
                    afli.shoot_date=date_time[0].replace("/","-");
                    afli.shoot_time=date_time[1].replace(":","-");
                }
                if (isFileArchiveRequired(stwa, sti, afli)) fl.add(afli);
            }
        }
        Collections.sort(fl, new Comparator<ArchiveFileListItem>(){
            @Override
            public int compare(ArchiveFileListItem ri, ArchiveFileListItem li) {
                return (ri.shoot_date+ri.shoot_time+ri.file_name).compareToIgnoreCase(li.shoot_date+li.shoot_time+li.file_name);
            }
        });
        return fl;
    }

    static final private boolean isFileTypeArchiveTarget(String name) {
        boolean result=false;
        for(String item:ARCHIVE_FILE_TYPE) {
            if (name.toLowerCase().endsWith("."+item)) {
                result=true;
                break;
            }
        }
        return result;
    }

    static private ArrayList<ArchiveFileListItem> buildSmbFileList(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile[] children, String to_path) throws JcifsException {
        ArrayList<ArchiveFileListItem>fl=new ArrayList<ArchiveFileListItem>();
        for(JcifsFile element:children) {
            if (element.isFile() && isFileTypeArchiveTarget(element.getName())) {
                String[] date_time=getFileExifDateTime(stwa, sti, element);
                ArchiveFileListItem afli=new ArchiveFileListItem();
                afli.file=element;
                afli.file_name=element.getName();
                afli.full_path=element.getPath();
                if (date_time==null) {
                    String[] dt=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(element.getLastModified()).split(" ");
                    afli.shoot_date=dt[0].replace("/","-");
                    afli.shoot_time=dt[1].replace(":","-");
                    afli.date_from_exif=false;
                } else {
                    afli.shoot_date=date_time[0].replace("/","-");
                    afli.shoot_time=date_time[1].replace(":","-");
                }
                if (isFileArchiveRequired(stwa, sti, afli)) fl.add(afli);
            }
        }
        Collections.sort(fl, new Comparator<ArchiveFileListItem>(){
            @Override
            public int compare(ArchiveFileListItem ri, ArchiveFileListItem li) {
                return (ri.shoot_date+ri.shoot_time+ri.file_name).compareToIgnoreCase(li.shoot_date+li.shoot_time+li.file_name);
            }
        });
        return fl;
    }

    static final public boolean isFileArchiveRequired(SyncThreadWorkArea stwa, SyncTaskItem sti, ArchiveFileListItem afli) {
        Calendar cal=Calendar.getInstance() ;
        String[] dt=afli.shoot_date.split("-");
        String[] tm=afli.shoot_time.split("-");
        cal.set(Integer.parseInt(dt[0]),Integer.parseInt(dt[1])-1,Integer.parseInt(dt[2]),
                Integer.parseInt(tm[0]),Integer.parseInt(tm[1]),Integer.parseInt(tm[2]));
        String c_ft=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(cal.getTimeInMillis());
        long exp_time=0, day_mili=1000L*60L*60L*24L;
        if (sti.getArchiveRetentionPeriod()==SyncTaskItem.PICTURE_ARCHIVE_RETAIN_FOR_A_7_DAYS) exp_time=day_mili*7L;
        else if (sti.getArchiveRetentionPeriod()==SyncTaskItem.PICTURE_ARCHIVE_RETAIN_FOR_A_30_DAYS) exp_time=day_mili*30L;
        else if (sti.getArchiveRetentionPeriod()==SyncTaskItem.PICTURE_ARCHIVE_RETAIN_FOR_A_60_DAYS) exp_time=day_mili*60L;
        else if (sti.getArchiveRetentionPeriod()==SyncTaskItem.PICTURE_ARCHIVE_RETAIN_FOR_A_90_DAYS) exp_time=day_mili*90L;
        else if (sti.getArchiveRetentionPeriod()==SyncTaskItem.PICTURE_ARCHIVE_RETAIN_FOR_A_180_DAYS) exp_time=day_mili*180L;
        else if (sti.getArchiveRetentionPeriod()==SyncTaskItem.PICTURE_ARCHIVE_RETAIN_FOR_A_1_YEARS) {
            int n_year=cal.getTime().getYear();
            Calendar n_cal=Calendar.getInstance() ;
            n_cal.setTimeInMillis(cal.getTimeInMillis());
            n_cal.add(Calendar.YEAR, 1);
            exp_time=n_cal.getTimeInMillis()-cal.getTimeInMillis();
        }
        String n_exp=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(cal.getTimeInMillis()+exp_time);
        boolean result=(System.currentTimeMillis()>cal.getTimeInMillis());
        stwa.util.addDebugMsg(1,"I","isFileArchiveRequired path=",afli.full_path,", shoot date=",afli.shoot_date,
                ", shoot time=", afli.shoot_time,", exif="+afli.date_from_exif,", archive="+result, ", retention period="+sti.getArchiveRetentionPeriod());
        return result;
    }

    static private String buildArchiveDirectoryName(SyncThreadWorkArea stwa, SyncTaskItem sti, ArchiveFileListItem afli) {
        String temp_dir="";
        if (sti.isArchiveCreateDirectory()) {
            if (!sti.getArchiveCreateDirectoryTemplate().equals("")){
                String year=afli.shoot_date.substring(0,4);
                String month=afli.shoot_date.substring(5,7);
                String day=afli.shoot_date.substring(8,10);

                temp_dir=sti.getArchiveCreateDirectoryTemplate().replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_YEAR,year)
                        .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_MONTH,month)
                        .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_DAY,day)
                        +"/";
            }
        }

        return temp_dir;
    }

    static private String buildArchiveFileName(SyncThreadWorkArea stwa, SyncTaskItem sti, ArchiveFileListItem afli, String original_name) {
        String to_file_name=original_name;
        if (!sti.getArchiveRenameFileTemplate().equals("")) {
            to_file_name=sti.getArchiveRenameFileTemplate()
                    .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_ORIGINAL_NAME, original_name)
                    .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_DATE, afli.shoot_date)
                    .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_TIME, afli.shoot_time);
        }
        return to_file_name;
    }

    static private String getFileSeqNumber(SyncThreadWorkArea stwa, SyncTaskItem sti, int seq_no) {
        String seqno="";
        if (sti.getArchiveSuffixOption().equals("3")) seqno=String.format("_%03d", seq_no);
        else if (sti.getArchiveSuffixOption().equals("4")) seqno=String.format("_%04d", seq_no);
        else if (sti.getArchiveSuffixOption().equals("5")) seqno=String.format("_%05d", seq_no);
        else if (sti.getArchiveSuffixOption().equals("6")) seqno=String.format("_%06d", seq_no);
        return seqno;
    }


    static final public String[] getFileExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, File lf) {
        String[] date_time=null;
        try {
            FileInputStream fis=new FileInputStream(lf);
            date_time=getFileExifDateTime(stwa, sti, fis, lf.lastModified(), lf.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return date_time;
    }

    static final public String[] getFileExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, SafFile sf) {
        String[] date_time=null;
        try {
            InputStream fis=stwa.gp.appContext.getContentResolver().openInputStream(sf.getUri());
            date_time=getFileExifDateTime(stwa, sti, fis, sf.lastModified(), sf.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return date_time;
    }

    static final public String[] getFileExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile lf) throws JcifsException {
        String[] date_time=null;
        InputStream fis=lf.getInputStream();
        date_time=getFileExifDateTime(stwa, sti, fis, lf.getLastModified(), lf.getName());
        return date_time;
    }

    static final public String[] getFileExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, InputStream fis, long last_mod, String file_name) {
        String[] date_time=null;
        if (file_name.endsWith(".mp4") || file_name.endsWith(".mov") ) {
            date_time=getMp4ExifDateTime(stwa, sti, fis);
        } else {
            date_time=getExifDateTime(fis, 8);
            if (date_time==null) {
                date_time=getExifDateTime(fis, 64);
            }
        }
        if (date_time==null || date_time[0]==null) {
            date_time= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(last_mod).split(" ");
        }
        return date_time;
    }

    static final public String[] getMp4ExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile lf) throws JcifsException {
        String[] result=null;
        InputStream fis=lf.getInputStream();
        result=getMp4ExifDateTime(stwa, sti, fis);
        return result;
    }

    static final public String[] getMp4ExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, InputStream fis)  {
        String[] result=null;
        try {
            Metadata metaData;
            metaData = ImageMetadataReader.readMetadata(fis);
            Mp4Directory directory=null;
            if (metaData!=null) {
                directory=metaData.getFirstDirectoryOfType(Mp4Directory.class);
                if (directory!=null) {
                    String date = directory.getString(Mp4Directory.TAG_CREATION_TIME);
                    result=parseDateValue(date);
                }
            }
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static final private String[] parseDateValue(String date_val) {
        String[] result=null;
        String[] dt=date_val.split(" ");
        int year=Integer.parseInt(dt[5]);
        int month=0;
        int day=Integer.parseInt(dt[2]);
        if      (dt[1].equals("Jan")) month=0;
        else if (dt[1].equals("Feb")) month=1;
        else if (dt[1].equals("Mar")) month=2;
        else if (dt[1].equals("Apr")) month=3;
        else if (dt[1].equals("May")) month=4;
        else if (dt[1].equals("Jun")) month=5;
        else if (dt[1].equals("Jul")) month=6;
        else if (dt[1].equals("Aug")) month=7;
        else if (dt[1].equals("Sep")) month=8;
        else if (dt[1].equals("Oct")) month=9;
        else if (dt[1].equals("Nov")) month=10;
        else if (dt[1].equals("Dec")) month=11;

        String[] tm=dt[3].split(":");
        int hours=  Integer.parseInt(tm[0]);
        int minutes=Integer.parseInt(tm[1]);
        int seconds=Integer.parseInt(tm[2]);

        Calendar cal=Calendar.getInstance() ;
        TimeZone tz=TimeZone.getDefault();
        tz.setID(dt[3]);
        cal.setTimeZone(tz);
        cal.set(year, month, day, hours, minutes, seconds);
        result=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(cal.getTimeInMillis()).split(" ");
        return result;
    }

    static final public String[] getMp4ExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, File lf) {
        String[] result=null;
        try {
            InputStream fis=new FileInputStream(lf);
            result=getMp4ExifDateTime(stwa, sti, fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static final public String[] getMp4ExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, SafFile sf) {
        String[] result=null;
        try {
            InputStream fis=stwa.gp.appContext.getContentResolver().openInputStream(sf.getUri());
            result=getMp4ExifDateTime(stwa, sti, fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static private String[] getExifDateTime(InputStream fis, int bsz) {
        String[] result=null;
        try {
            int buf_sz=1024*bsz;
            byte[] buff=new byte[buf_sz];
            fis.read(buff);
            fis.close();
            if (buff[0]==(byte)0xff && buff[1]==(byte)0xd8) {//if jpeg header
                int i=2;
                while(i<buf_sz-3) {// find dde1 jpeg segemnt
                    if (buff[i]==(byte)0xff && buff[i+1]==(byte)0xe1) {
                        int seg_size=getIntFrom2Byte(false, buff[i+2], buff[i+3]);
                        boolean little_endian=false;
                        if (buff[i+10]==(byte)0x49 && buff[i+11]==(byte)0x49) little_endian=true;
                        int ifd_offset=getIntFrom4Byte(little_endian, buff[i+14], buff[i+15], buff[i+16], buff[i+17]);

                        byte[] ifd_buff= Arrays.copyOfRange(buff, i+10, seg_size+ifd_offset);
                        result=process0thIfdTag(little_endian, ifd_buff, ifd_offset);
                        break;
                    } else {
                        int offset=((int)buff[i+2]&0xff)*256+((int)buff[i+3]&0xff);
                        i=offset+i+2;
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static private int getIntFrom2Byte(boolean little_endian, byte b1, byte b2) {
        int result=0;
        if (little_endian) result=((int)b2&0xff)*256+((int)b1&0xff);
        else result=((int)b1&0xff)*256+((int)b2&0xff);
        return result;
    }

    static private int getIntFrom4Byte(boolean little_endian, byte b1, byte b2, byte b3, byte b4) {
        int result=0;
        if (little_endian) result=((int)b4&0xff)*65536+((int)b3&0xff)*4096+((int)b2&0xff)*256+((int)b1&0xff);
        else result=((int)b1&0xff)*65536+((int)b2&0xff)*4096+((int)b3&0xff)*256+((int)b4&0xff);
        return result;
    }

    static private String[] process0thIfdTag(boolean little_endian, byte[]ifd_buff, int ifd_offset) {
        int count=getIntFrom2Byte(little_endian, ifd_buff[ifd_offset+0], ifd_buff[ifd_offset+1]);
        int i=0;
        int ba=ifd_offset+2;
        String[] result=null;
        while(i<count) {
            int tag_number=getIntFrom2Byte(little_endian, ifd_buff[ba+0], ifd_buff[ba+1]);
            int tag_offset=getIntFrom4Byte(little_endian, ifd_buff[ba+8], ifd_buff[ba+9], ifd_buff[ba+10], ifd_buff[ba+11]);

            if (tag_number==(0x8769&0xffff)) {
                result=processExifIfdTag(little_endian, ifd_buff, tag_offset);
                break;
            }
            ba+=12;
            i++;
        }
        return result;
    }

    static private String[] processExifIfdTag(boolean little_endian, byte[]ifd_buff, int ifd_offset) {
        int count=getIntFrom2Byte(little_endian, ifd_buff[ifd_offset+0], ifd_buff[ifd_offset+1]);
        int i=0;
        int ba=ifd_offset+2;
        String[] date_time=new String[2];
        while(i<count) {
            int tag_number=getIntFrom2Byte(little_endian, ifd_buff[ba+0], ifd_buff[ba+1]);
            int tag_offset=getIntFrom4Byte(little_endian, ifd_buff[ba+8], ifd_buff[ba+9], ifd_buff[ba+10], ifd_buff[ba+11]);
            if (tag_number==(0x9003&0xffff)) {
                String[] date = new String(ifd_buff, tag_offset, 19).split(" ");
                if (date.length==2) {
                    date_time[0]=date[0].replaceAll(":", "/");//Date
                    date_time[1]=date[1];//Time
                    break;
                }
            }
            ba+=12;
            i++;
        }
        return date_time;
    }

}
