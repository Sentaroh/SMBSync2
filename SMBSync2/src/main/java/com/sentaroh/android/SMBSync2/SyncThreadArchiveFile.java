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
import android.os.Build;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.mp4.Mp4Directory;
import com.sentaroh.android.SMBSync2.SyncThread.SyncThreadWorkArea;
import com.sentaroh.android.Utilities.MiscUtil;
import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.android.Utilities.StringUtil;
import com.sentaroh.jcifs.JcifsException;
import com.sentaroh.jcifs.JcifsFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import static com.sentaroh.android.SMBSync2.Constants.APP_SPECIFIC_DIRECTORY;
import static com.sentaroh.android.SMBSync2.Constants.ARCHIVE_FILE_TYPE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_MOVE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY_LONG;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_NUMBER;

public class SyncThreadArchiveFile {
    private static final Logger log= LoggerFactory.getLogger(SyncThreadArchiveFile.class);

    static public int syncArchiveInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                   String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = buildArchiveListInternalToInternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private String getTempFilePath(SyncThreadWorkArea stwa, SyncTaskItem sti, String to_path) {
        String tmp_path="";
        if (to_path.startsWith("/storage/emulated/0")) tmp_path=stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";
        else {
            String[] dir_parts=to_path.split("/");
            if (dir_parts.length>=3) {
                tmp_path="/"+dir_parts[1]+"/"+dir_parts[2]+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";
            }
        }
        return tmp_path;
    }

    static private int moveFileInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                             File mf, File tf, String to_path, String file_name) throws IOException {
        int sync_result=0;
        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            if (!sti.isSyncTestMode()) {
                String dir=tf.getParent();
                File lf_dir=new File(dir);
                if (!lf_dir.exists()) lf_dir.mkdirs();

                String tmp_path="";
                if (Build.VERSION.SDK_INT<=29) {
                    tmp_path=stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";
                } else {
                    tmp_path=getTempFilePath(stwa, sti, to_path);
                }
                File temp_file=new File(tmp_path);
                sync_result= copyFile(stwa, sti, new FileInputStream(mf),
                        new FileOutputStream(temp_file), from_path, to_path, file_name, sti.isSyncOptionUseSmallIoBuffer());
                if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                    temp_file.setLastModified(mf.lastModified());
                    temp_file.renameTo(tf);
                }
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    tf.setLastModified(mf.lastModified());
                    mf.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, mf.getPath());
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }

        return sync_result;
    }

    static private int archiveFileInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                     String from_path, String to_path) throws IOException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildLocalFileList(stwa, sti, children);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            if (!item.date_from_exif && sti.isSyncOptionConfirmNotExistsExifDate()) {
                if (!SyncThread.sendArchiveConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE, item.full_path)) {
                    stwa.totalIgnoreCount++;
                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", item.full_path, item.file_name,
                            "", stwa.context.getString(R.string.msgs_mirror_confirm_archive_date_time_from_file_cancel));
                    continue;
                }
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
            String converted_to_path=buildArchiveTargetDirectoryName(stwa, sti, to_path, item);

            if (!sti.isArchiveUseRename()) {
                File tf=new File(converted_to_path+"/"+item.file_name);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, converted_to_path, converted_to_path+"/"+to_file_name, to_file_ext) ;
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
                String temp_dir= buildArchiveSubDirectoryName(stwa, sti, item);

                File tf=new File(converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, converted_to_path, converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
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
                            converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int buildArchiveListInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                          String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File tf;
        try {
            String t_from_path = from_path.substring(from_base.length());
            if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
            if (mf.exists()) {
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead()) {
                        if (!SyncThread.isHiddenDirectory(stwa, sti, mf) && SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
//                          if (sti.isSyncOptionSyncEmptyDirectory()) {
//                              SyncThread.createDirectoryToInternalStorage(stwa, sti, to_path);
//                          }
                            File[] children = mf.listFiles();
                            if (children != null) {
                                archiveFileInternalToInternal(stwa, sti, children, from_path, to_path);
                                for (File element : children) {
                                    if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                        if (!element.getName().equals(".android_secure")) {
                                            if (!from_path.equals(to_path)) {
                                                if (element.isDirectory()) {
                                                    if (sti.isSyncOptionSyncSubDirectory()) {
                                                        sync_result = buildArchiveListInternalToInternal(stwa, sti, from_base, from_path + "/" + element.getName(),
                                                                element, to_base, to_path + "/" + element.getName());
                                                    } else {
                                                        stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=", from_path);
                                                    }
                                                }
                                            } else {
                                                stwa.util.addDebugMsg(1, "W",
                                                        String.format(stwa.context.getString(R.string.msgs_mirror_same_directory_ignored),
                                                                from_path, "/", element.getName()));
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
                        }
                    } else {
                        stwa.totalIgnoreCount++;
                        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "W", "", "",
                                stwa.context.getString(R.string.msgs_mirror_task_directory_ignored_because_can_not_read, from_path + "/" + mf.getName()));
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.context.getString(R.string.msgs_mirror_task_master_not_found) + " " + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
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
        int sync_result = buildArchiveListInternalToExternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                  File mf, File tf, String to_path, String file_name) throws IOException {
        int result=0;
        if (Build.VERSION.SDK_INT>=24) result= moveFileInternalToExternalSetLastMod(stwa, sti, from_path, mf, tf, to_path, file_name);
        else result= moveFileInternalToExternalUnsetLastMod(stwa, sti, from_path, mf, tf, to_path, file_name);
        return result;
    }

    static private int moveFileInternalToExternalUnsetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                              File mf, File tf, String to_path, String file_name) throws IOException {
        int sync_result=0;
        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            if (!sti.isSyncTestMode()) {
                SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());
                SafFile t_df = SyncThread.createSafFile(stwa, sti, to_path, false);
                if (t_df == null) {
                    return SyncTaskItem.SYNC_STATUS_ERROR;
                }
                sync_result= copyFile(stwa, sti, new FileInputStream(mf),
                        stwa.context.getContentResolver().openOutputStream(t_df.getUri()), from_path, to_path, file_name, sti.isSyncOptionUseSmallIoBuffer());
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    mf.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, mf.getPath());
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }
        return sync_result;
    }

    static private boolean isSdcardPath(SyncThreadWorkArea stwa,String fp) {
        if (fp.startsWith(stwa.gp.safMgr.getSdcardRootPath())) return true;
        else return false;
    }

    static private int moveFileInternalToExternalSetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                            File mf, File tf, String to_path, String file_name) throws IOException {
        int sync_result=0;
        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            if (!sti.isSyncTestMode()) {
                SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());

                String temp_path=isSdcardPath(stwa,to_path)?
                        stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/archive_temp.tmp":
                        stwa.gp.safMgr.getUsbRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/archive_temp.tmp";
                File temp_file=new File(temp_path);
                OutputStream os=null;
                try {
                    os=new FileOutputStream(temp_file);
                } catch (Exception e) {
                    SafFile sf=SyncThread.createSafFile(stwa, sti, temp_file.getPath(), false);
                    os=stwa.context.getContentResolver().openOutputStream(sf.getUri());
                }

                SafFile to_saf=SyncThread.createSafFile(stwa, sti, tf.getPath());
                if (to_saf == null) return SyncTaskItem.SYNC_STATUS_ERROR;
                to_saf.deleteIfExists();

                sync_result= copyFile(stwa, sti, new FileInputStream(mf), os, from_path, to_path, file_name, sti.isSyncOptionUseSmallIoBuffer());

                temp_file.setLastModified(mf.lastModified());

//                SyncThread.deleteTempMediaStoreItem(stwa, temp_file);

                SafFile from_saf = SyncThread.createSafFile(stwa, sti, temp_file.getPath());
                if (from_saf == null) return SyncTaskItem.SYNC_STATUS_ERROR;

                from_saf.moveTo(to_saf);
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    mf.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, mf.getPath());
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }
        return sync_result;
    }

    static private int archiveFileInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                     String from_path, String to_path) throws IOException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildLocalFileList(stwa, sti, children);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            if (!item.date_from_exif && sti.isSyncOptionConfirmNotExistsExifDate()) {
                if (!SyncThread.sendArchiveConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE, item.full_path)) {
                    stwa.totalIgnoreCount++;
                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", item.full_path, item.file_name,
                            "", stwa.context.getString(R.string.msgs_mirror_confirm_archive_date_time_from_file_cancel));
                    continue;
                }
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

            String converted_to_path=buildArchiveTargetDirectoryName(stwa, sti, to_path, item);

            if (!sti.isArchiveUseRename()) {
                File tf=new File(converted_to_path+"/"+item.file_name);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, converted_to_path, converted_to_path+"/"+to_file_name, to_file_ext) ;
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
                String temp_dir= buildArchiveSubDirectoryName(stwa, sti, item);

                File tf=new File(converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, converted_to_path, converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
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
                            converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }


    static private int buildArchiveListInternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                          String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
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
                    if (mf.canRead()) {
                        if (!SyncThread.isHiddenDirectory(stwa, sti, mf) && SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
//                          if (sti.isSyncOptionSyncEmptyDirectory()) {
//                              SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
//                          }
                            File[] children = mf.listFiles();
                            archiveFileInternalToExternal(stwa, sti, children, from_path, to_path);
                            if (children != null) {
                                for (File element : children) {
                                    if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                        if (!element.getName().equals(".android_secure")) {
                                            if (element.isDirectory()) {
                                                if (sti.isSyncOptionSyncSubDirectory()) {
                                                    sync_result = buildArchiveListInternalToExternal(stwa, sti, from_base, from_path + "/" + element.getName(),
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
                        }
                    } else {
                        stwa.totalIgnoreCount++;
                        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "W", "", "",
                                stwa.context.getString(R.string.msgs_mirror_task_directory_ignored_because_can_not_read, from_path + "/" + mf.getName()));
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.context.getString(R.string.msgs_mirror_task_master_not_found) + " " + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
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
        int sync_result = buildArchiveListInternalToSmb(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int archiveFileInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                String from_path, String to_path) throws IOException, JcifsException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildLocalFileList(stwa, sti, children);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            if (!item.date_from_exif && sti.isSyncOptionConfirmNotExistsExifDate()) {
                if (!SyncThread.sendArchiveConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE, item.full_path)) {
                    stwa.totalIgnoreCount++;
                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", item.full_path, item.file_name,
                            "", stwa.context.getString(R.string.msgs_mirror_confirm_archive_date_time_from_file_cancel));
                    continue;//Archive cancelled
                }
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
            String converted_to_path=buildArchiveTargetDirectoryName(stwa, sti, to_path, item);            

            if (!sti.isArchiveUseRename()) {//Renameしない
                JcifsFile jf=new JcifsFile(converted_to_path+"/"+item.file_name, stwa.targetAuth);
                if (jf.exists()) {
                    if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, (File)item.file, jf, stwa.ALL_COPY) &&
                            SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, ((File)item.file).getAbsolutePath(), ((File)item.file).lastModified(), jf.getLastModified())) {
                        String new_name = createArchiveSmbNewFilePath(stwa, sti, converted_to_path, converted_to_path + "/" + to_file_name, to_file_ext);
                        if (new_name.equals("")) {
                            stwa.util.addLogMsg("E", "Archive sequence number overflow error.");
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                            break;
                        } else {
                            jf = new JcifsFile(new_name, stwa.targetAuth);
                            sync_result = moveFileInternalToSmb(stwa, sti, item.full_path, (File) item.file, jf, jf.getPath());
                        }
                    }
                } else {
                    sync_result= moveFileInternalToSmb(stwa, sti, item.full_path, (File)item.file, jf, jf.getPath());
                }
            } else {//Renameする
                to_file_name=buildArchiveFileName(stwa, sti, item, to_file_name);
                String temp_dir= buildArchiveSubDirectoryName(stwa, sti, item);

                JcifsFile jf=new JcifsFile(converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext, stwa.targetAuth);
                if (jf.exists()) {
                    if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, (File)item.file, jf, stwa.ALL_COPY) &&
                            SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, ((File)item.file).getAbsolutePath(), ((File)item.file).lastModified(), jf.getLastModified())) {
                        String new_name = createArchiveSmbNewFilePath(stwa, sti, converted_to_path, converted_to_path + "/" + temp_dir + to_file_name + to_file_seqno, to_file_ext);
                        if (new_name.equals("")) {
                            stwa.util.addLogMsg("E", "Archive sequence number overflow error.");
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                            break;
                        } else {
                            jf = new JcifsFile(new_name, stwa.targetAuth);
                            sync_result = moveFileInternalToSmb(stwa, sti, item.full_path, (File) item.file, jf, jf.getPath());
                        }
                    }
                } else {
                    sync_result= moveFileInternalToSmb(stwa, sti, item.full_path, (File)item.file, jf, jf.getPath());
                }
            }
        }
        return sync_result;
    }

    static private int buildArchiveListInternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                     String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
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
                    if (mf.canRead()) {
                        if (!SyncThread.isHiddenDirectory(stwa, sti, mf) && SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
//                          if (sti.isSyncOptionSyncEmptyDirectory()) {
//                              SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.targetAuth);
//                          }
                            File[] children = mf.listFiles();
                            if (children != null) {
                                archiveFileInternalToSmb(stwa, sti, children, from_path, to_path);
                                for (File element : children) {
                                    if (element.isDirectory()) {
                                        if (!element.getName().equals(".android_secure")) {
                                            while (stwa.syncTaskRetryCount > 0) {
                                                if (sti.isSyncOptionSyncSubDirectory()) {
                                                    sync_result = buildArchiveListInternalToSmb(stwa, sti, from_base, from_path + "/" + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName());
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
                                        if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                                            sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                                            break;
                                        }
                                    }
                                }
                            } else {
                                stwa.util.addDebugMsg(1, "I", "Directory was null, dir=" + mf.getPath());
                            }
                        }
                    } else {
                        stwa.totalIgnoreCount++;
                        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "W", "", "",
                                stwa.context.getString(R.string.msgs_mirror_task_directory_ignored_because_can_not_read, from_path + "/" + mf.getName()));
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.context.getString(R.string.msgs_mirror_task_master_not_found) + " " + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
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
        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "",
                CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", To=" + to_path);
        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", e.getMessage());
        String sugget_msg=SyncTaskUtil.getJcifsErrorSugestionMessage(stwa.context, MiscUtil.getStackTraceString(e));
        if (!sugget_msg.equals("")) SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", sugget_msg);
        if (e.getCause()!=null) SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getCause().toString());
        if (e instanceof JcifsException) stwa.jcifsNtStatusCode=((JcifsException)e).getNtStatus();
        SyncThread.printStackTraceElement(stwa, e.getStackTrace());
        stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
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
                if (!jf_dir.exists()) jf_dir.mkdirs();
//                if (sti.isArchiveCreateDirectory()) {
//                    if (!jf_dir.exists()) jf_dir.mkdirs();
//                }
                while (stwa.syncTaskRetryCount > 0) {
                    sync_result= copyFile(stwa, sti, new FileInputStream(mf), tf.getOutputStream(), from_path, to_path,
                            tf.getName(), sti.isSyncOptionUseSmallIoBuffer());
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
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
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
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }
        return sync_result;
    }

    static public int syncArchiveExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = buildArchiveListExternalToInternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                  File mf, File tf, String to_path, String file_name) throws IOException {
        int sync_result=0;
        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            SafFile m_df =SyncThread.createSafFile(stwa, sti, from_path, false);
            if (!sti.isSyncTestMode()) {
                SyncThread.createDirectoryToInternalStorage(stwa, sti, tf.getParent());
                String temp_path=isSdcardPath(stwa,to_path)?
                        stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/temp_file.tmp":
                        stwa.gp.safMgr.getUsbRootPath()+   "/"+APP_SPECIFIC_DIRECTORY+"/cache/temp_file.tmp";
                File temp_file=new File(temp_path);
                sync_result= copyFile(stwa, sti,  stwa.context.getContentResolver().openInputStream(m_df.getUri()),
                        new FileOutputStream(temp_file), from_path, to_path, file_name, sti.isSyncOptionUseSmallIoBuffer());
                if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                    temp_file.setLastModified(mf.lastModified());
                    temp_file.renameTo(tf);
                }
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    m_df.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, from_path);
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }
        return sync_result;
    }

    static private int archiveFileExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                     String from_path, String to_path) throws IOException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl= buildSafFileList(stwa, sti, children);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            if (!item.date_from_exif && sti.isSyncOptionConfirmNotExistsExifDate()) {
                if (!SyncThread.sendArchiveConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE, item.full_path)) {
                    stwa.totalIgnoreCount++;
                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", item.full_path, item.file_name,
                            "", stwa.context.getString(R.string.msgs_mirror_confirm_archive_date_time_from_file_cancel));
                    continue;
                }
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
            String converted_to_path=buildArchiveTargetDirectoryName(stwa, sti, to_path, item);

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
                String temp_dir= buildArchiveSubDirectoryName(stwa, sti, item);

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

    static private int buildArchiveListExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                          String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead()) {
                        if (!SyncThread.isHiddenDirectory(stwa, sti, mf) && SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                            if (sti.isSyncOptionSyncEmptyDirectory()) {
                                SyncThread.createDirectoryToInternalStorage(stwa, sti, to_path);
                            }
                            File[] children = mf.listFiles();
                            if (children != null) {
                                archiveFileExternalToInternal(stwa, sti, children, from_path, to_path);
                                for (File element : children) {
                                    if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                        if (!element.getName().equals(".android_secure")) {
                                            if (element.isDirectory()) {
                                                if (sti.isSyncOptionSyncSubDirectory()) {
                                                    sync_result = buildArchiveListExternalToInternal(stwa, sti, from_base, from_path + "/" + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName());
                                                } else {
                                                    stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=", from_path);
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
                        }
                    } else {
                        stwa.totalIgnoreCount++;
                        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "W", "", "",
                                stwa.context.getString(R.string.msgs_mirror_task_directory_ignored_because_can_not_read, from_path + "/" + mf.getName()));
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.context.getString(R.string.msgs_mirror_task_master_not_found) + " " + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
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
        int sync_result = buildArchiveListExternalToExternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                  File mf, File tf, String to_path, String file_name) throws IOException {
        int result=0;
        if (Build.VERSION.SDK_INT>=24) result= moveFileExternalToExternalSetLastMod(stwa, sti, from_path, mf, tf, to_path, file_name);
        else result= moveFileExternalToExternalUnsetLastMod(stwa, sti, from_path, mf, tf, to_path, file_name);
        return result;
    }

    static private int moveFileExternalToExternalUnsetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                              File mf, File tf, String to_path, String file_name) throws IOException {
        int sync_result=0;
        if (SyncThread.sendArchiveConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            SafFile m_df =SyncThread.createSafFile(stwa, sti, from_path, false);
            if (!sti.isSyncTestMode()) {
                SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());
                SafFile t_df =SyncThread.createSafFile(stwa, sti, to_path, false);
                if (t_df == null) {
                    return SyncTaskItem.SYNC_STATUS_ERROR;
                }
                sync_result= copyFile(stwa, sti, stwa.context.getContentResolver().openInputStream(m_df.getUri()),
                        stwa.context.getContentResolver().openOutputStream(t_df.getUri()), from_path, to_path, file_name, sti.isSyncOptionUseSmallIoBuffer());
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    m_df.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, from_path);
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }
        return sync_result;
    }

    static private int moveFileExternalToExternalSetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                            File mf, File tf, String to_path, String file_name) throws IOException {
        int sync_result=0;
        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            SafFile m_df = SyncThread.createSafFile(stwa, sti, from_path);
            if (!sti.isSyncTestMode()) {
                SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());

                String temp_path=isSdcardPath(stwa,to_path)?
                        stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/archive_temp.tmp":
                        stwa.gp.safMgr.getUsbRootPath()+   "/"+APP_SPECIFIC_DIRECTORY+"/cache/archive_temp.tmp";
                File temp_file=new File(temp_path);
                OutputStream os=null;
                try {
                    os=new FileOutputStream(temp_file);
                } catch (Exception e) {
                    SafFile sf=SyncThread.createSafFile(stwa, sti, temp_file.getPath(), false);
                    os=stwa.context.getContentResolver().openOutputStream(sf.getUri());
                }

                sync_result= copyFile(stwa, sti, stwa.context.getContentResolver().openInputStream(m_df.getUri()),
                        os, from_path, to_path, file_name, sti.isSyncOptionUseSmallIoBuffer());

                temp_file.setLastModified(mf.lastModified());

//                SyncThread.deleteTempMediaStoreItem(stwa, temp_file);

                SafFile from_saf = SyncThread.createSafFile(stwa, sti, temp_file.getPath());
                if (from_saf == null) return SyncTaskItem.SYNC_STATUS_ERROR;
                SafFile to_saf = SyncThread.createSafFile(stwa, sti, tf.getPath());
                if (to_saf == null) return SyncTaskItem.SYNC_STATUS_ERROR;

                from_saf.moveTo(to_saf);

            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
                if (!sti.isSyncTestMode()) {
                    m_df.delete();
                    stwa.totalDeleteCount++;
                    SyncThread.scanMediaFile(stwa, from_path);
                    SyncThread.scanMediaFile(stwa, tf.getPath());
                }
            }
        } else {
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }
        return sync_result;
    }

    static private int archiveFileExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                     String from_path, String to_path) throws IOException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl= buildSafFileList(stwa, sti, children);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            if (!item.date_from_exif && sti.isSyncOptionConfirmNotExistsExifDate()) {
                if (!SyncThread.sendArchiveConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE, item.full_path)) {
                    stwa.totalIgnoreCount++;
                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", item.full_path, item.file_name,
                            "", stwa.context.getString(R.string.msgs_mirror_confirm_archive_date_time_from_file_cancel));
                    continue;
                }
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
            String converted_to_path=buildArchiveTargetDirectoryName(stwa, sti, to_path, item);

            if (!sti.isArchiveUseRename()) {
                File tf=new File(converted_to_path+"/"+item.file_name);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, converted_to_path, converted_to_path+"/"+to_file_name, to_file_ext) ;
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
                String temp_dir= buildArchiveSubDirectoryName(stwa, sti, item);

                File tf=new File(converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, converted_to_path, converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
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
                            converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int buildArchiveListExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                          String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName(), " entered, from=", from_path, ", to=" + to_path);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead()) {
                        if (!SyncThread.isHiddenDirectory(stwa, sti, mf) && SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
    //                      if (sti.isSyncOptionSyncEmptyDirectory()) {
    //                          SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
    //                      }
                            File[] children = mf.listFiles();
                            archiveFileExternalToExternal(stwa, sti, children, from_path, to_path);
                            if (children != null) {
                                for (File element : children) {
                                    if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                        if (!element.getName().equals(".android_secure")) {
                                            if (!from_path.equals(to_path)) {
                                                if (element.isDirectory()) {
                                                    if (sti.isSyncOptionSyncSubDirectory()) {
                                                        sync_result = buildArchiveListExternalToExternal(stwa, sti, from_base, from_path + "/" + element.getName(),
                                                                element, to_base, to_path + "/" + element.getName());
                                                    } else {
                                                        stwa.util.addDebugMsg(1, "I", "Sub directory was not sync, dir=" + from_path);
                                                    }
                                                }
                                            } else {
                                                stwa.util.addDebugMsg(1, "W",
                                                        String.format(stwa.context.getString(R.string.msgs_mirror_same_directory_ignored),
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
                        }
                    } else {
                        stwa.totalIgnoreCount++;
                        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "W", "", "",
                                stwa.context.getString(R.string.msgs_mirror_task_directory_ignored_because_can_not_read, from_path + "/" + mf.getName()));
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.context.getString(R.string.msgs_mirror_task_master_not_found) + " " + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            putExceptionMsg(stwa, sti, from_path, to_path, e);
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static public int syncArchiveExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, String to_path) {
        File mf = new File(from_path);
        int sync_result = buildArchiveListExternalToSmb(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                             File mf, JcifsFile tf, String to_path, String file_name) throws IOException, JcifsException {
        int sync_result=0;

        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            SafFile m_df =SyncThread.createSafFile(stwa, sti, from_path, false);
            if (!sti.isSyncTestMode()) {
                String dir=tf.getParent();
                JcifsFile jf_dir=new JcifsFile(dir,stwa.targetAuth);
//                if (sti.isArchiveCreateDirectory()) {
//                    if (!jf_dir.exists()) jf_dir.mkdirs();
//                }
                if (!jf_dir.exists()) jf_dir.mkdirs();
                while (stwa.syncTaskRetryCount > 0) {
                    sync_result= copyFile(stwa, sti, stwa.context.getContentResolver().openInputStream(m_df.getUri()),
                            tf.getOutputStream(), from_path, to_path,
                            tf.getName(), sti.isSyncOptionUseSmallIoBuffer());
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
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
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
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }

        return sync_result;
    }

    static private int archiveFileExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children,
                                                String from_path, String to_path) throws IOException, JcifsException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl= buildSafFileList(stwa, sti, children);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            if (!item.date_from_exif && sti.isSyncOptionConfirmNotExistsExifDate()) {
                if (!SyncThread.sendArchiveConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE, item.full_path)) {
                    stwa.totalIgnoreCount++;
                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", item.full_path, item.file_name,
                            "", stwa.context.getString(R.string.msgs_mirror_confirm_archive_date_time_from_file_cancel));
                    continue;
                }
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
            String converted_to_path=buildArchiveTargetDirectoryName(stwa, sti, to_path, item);            

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
                String temp_dir= buildArchiveSubDirectoryName(stwa, sti, item);

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

    static private int buildArchiveListExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                     String from_base, String from_path, File mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        JcifsFile tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead()) {
                        if (!SyncThread.isHiddenDirectory(stwa, sti, mf) && SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
    //                      if (sti.isSyncOptionSyncEmptyDirectory()) {
    //                          SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.targetAuth);
    //                      }
                            File[] children = mf.listFiles();
                            if (children != null) {
                                archiveFileExternalToSmb(stwa, sti, children, from_path, to_path);
                                for (File element : children) {
                                    if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                        if (!element.getName().equals(".android_secure")) {
                                            while (stwa.syncTaskRetryCount > 0) {
                                                if (element.isDirectory()) {
                                                    if (sti.isSyncOptionSyncSubDirectory()) {
                                                        sync_result = buildArchiveListExternalToSmb(stwa, sti, from_base, from_path + "/" + element.getName(),
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
                        }
                    } else {
                        stwa.totalIgnoreCount++;
                        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "W", "", "",
                                stwa.context.getString(R.string.msgs_mirror_task_directory_ignored_because_can_not_read, from_path + "/" + mf.getName()));
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.context.getString(R.string.msgs_mirror_task_master_not_found) + " " + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
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
        int sync_result = buildArchiveListSmbToInternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
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
                File temp_file=new File(stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/cache/temp_file.tmp");
                while (stwa.syncTaskRetryCount > 0) {
                    sync_result= copyFile(stwa, sti, mf.getInputStream(), new FileOutputStream(temp_file), from_path, to_path, file_name, sti.isSyncOptionUseSmallIoBuffer());
                    if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                        temp_file.setLastModified(mf.getLastModified());
                        temp_file.renameTo(tf);
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
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
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
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }

        return sync_result;
    }

    static private int archiveFileSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile[] children,
                                                String from_path, String to_path) throws IOException, JcifsException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildSmbFileList(stwa, sti, children);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            if (!item.date_from_exif && sti.isSyncOptionConfirmNotExistsExifDate()) {
                if (!SyncThread.sendArchiveConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE, item.full_path)) {
                    stwa.totalIgnoreCount++;
                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", item.full_path, item.file_name,
                            "", stwa.context.getString(R.string.msgs_mirror_confirm_archive_date_time_from_file_cancel));
                    continue;
                }
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
            String converted_to_path=buildArchiveTargetDirectoryName(stwa, sti, to_path, item);

            if (!sti.isArchiveUseRename()) {
                File tf=new File(converted_to_path+"/"+item.file_name);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, converted_to_path, converted_to_path+"/"+to_file_name, to_file_ext) ;
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
                String temp_dir= buildArchiveSubDirectoryName(stwa, sti, item);

                File tf=new File(converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, converted_to_path, converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
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
                            converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int buildArchiveListSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                     String from_base, String from_path, JcifsFile mf, String to_base, String to_path) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path + ", to=" + to_path);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead()) {
                        if (!SyncThread.isHiddenDirectory(stwa, sti, mf) && SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
    //                      if (sti.isSyncOptionSyncEmptyDirectory()) {
    //                          SyncThread.createDirectoryToInternalStorage(stwa, sti, to_path);
    //                      }
                            JcifsFile[] children = mf.listFiles();
                            if (children != null) {
                                archiveFileSmbToInternal(stwa, sti, children, from_path, to_path);
                                for (JcifsFile element : children) {
                                    if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            if (element.isDirectory()) {
                                                if (sti.isSyncOptionSyncSubDirectory()) {
                                                    sync_result = buildArchiveListSmbToInternal(stwa, sti, from_base, from_path + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName().replace("/", ""));
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
                        }
                    } else {
                        stwa.totalIgnoreCount++;
                        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "W", "", "",
                                stwa.context.getString(R.string.msgs_mirror_task_directory_ignored_because_can_not_read, from_path + "/" + mf.getName()));
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.context.getString(R.string.msgs_mirror_task_master_not_found) + " " + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
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

    static public int syncArchiveSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, String to_path) {
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
        int sync_result = buildArchiveListSmbToExternal(stwa, sti, from_path, from_path, mf, to_path, to_path);
        return sync_result;
    }

    static private int moveFileSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                             JcifsFile mf, File tf, String to_path, String file_name) throws IOException, JcifsException {
        int result=0;
        if (Build.VERSION.SDK_INT>=24) result= moveFileSmbToExternalSetLastMod(stwa, sti, from_path, mf, tf, to_path, file_name);
        else result= moveFileSmbToExternalUnsetLastMod(stwa, sti, from_path, mf, tf, to_path, file_name);
        return result;
    }

    static private int moveFileSmbToExternalUnsetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                         JcifsFile mf, File tf, String to_path, String file_name) throws IOException, JcifsException {
        int sync_result=0;

        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            if (!sti.isSyncTestMode()) {
                SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());
                SafFile t_df =SyncThread.createSafFile(stwa, sti, to_path, false);
                if (t_df == null) {
                    return SyncTaskItem.SYNC_STATUS_ERROR;
                }
                while (stwa.syncTaskRetryCount > 0) {
                    sync_result= copyFile(stwa, sti, mf.getInputStream(), stwa.context.getContentResolver().openOutputStream(t_df.getUri()),
                            from_path, to_path, file_name, sti.isSyncOptionUseSmallIoBuffer());
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
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
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
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }

        return sync_result;
    }

    static private int moveFileSmbToExternalSetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                       JcifsFile mf, File tf, String to_path, String file_name) throws IOException, JcifsException {
        int sync_result=0;

        if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, from_path)) {
            if (!sti.isSyncTestMode()) {
                SyncThread.createDirectoryToExternalStorage(stwa, sti, tf.getParent());

                OutputStream os=null;
                String temp_path=isSdcardPath(stwa,to_path)?
                        stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/archive_temp.tmp":
                        stwa.gp.safMgr.getUsbRootPath()+   "/"+APP_SPECIFIC_DIRECTORY+"/cache/archive_temp.tmp";
                File temp_file=new File(temp_path);
                try {
                    os=new FileOutputStream(temp_file);
                } catch (Exception e) {
                    SafFile sf=SyncThread.createSafFile(stwa, sti, temp_file.getPath(), false);
                    os=stwa.context.getContentResolver().openOutputStream(sf.getUri());
                }

                while (stwa.syncTaskRetryCount > 0) {
                    sync_result= copyFile(stwa, sti, mf.getInputStream(), os, from_path, to_path, file_name, sti.isSyncOptionUseSmallIoBuffer());
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
                if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                    temp_file.setLastModified(mf.getLastModified());

//                    SyncThread.deleteTempMediaStoreItem(stwa, temp_file);

                    SafFile from_saf = SyncThread.createSafFile(stwa, sti, temp_file.getPath());
                    if (from_saf == null) return SyncTaskItem.SYNC_STATUS_ERROR;
                    SafFile to_saf = SyncThread.createSafFile(stwa, sti, tf.getPath());
                    if (to_saf == null) return SyncTaskItem.SYNC_STATUS_ERROR;

                    from_saf.moveTo(to_saf);
                }
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
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
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }

        return sync_result;
    }

    static private int archiveFileSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile[] children,
                                                String from_path, String to_path) throws IOException, JcifsException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildSmbFileList(stwa, sti, children);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            if (!item.date_from_exif && sti.isSyncOptionConfirmNotExistsExifDate()) {
                if (!SyncThread.sendArchiveConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE, item.full_path)) {
                    stwa.totalIgnoreCount++;
                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", item.full_path, item.file_name,
                            "", stwa.context.getString(R.string.msgs_mirror_confirm_archive_date_time_from_file_cancel));
                    continue;
                }
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
            String converted_to_path=buildArchiveTargetDirectoryName(stwa, sti, to_path, item);

            if (!sti.isArchiveUseRename()) {
                File tf=new File(converted_to_path+"/"+item.file_name);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, converted_to_path, converted_to_path+"/"+to_file_name, to_file_ext) ;
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
                String temp_dir= buildArchiveSubDirectoryName(stwa, sti, item);

                File tf=new File(converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                if (tf.exists()) {
                    String new_name=createArchiveLocalNewFilePath(stwa, sti, converted_to_path, converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno,to_file_ext) ;
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
                            converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int buildArchiveListSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                     String from_base, String from_path, JcifsFile mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName(), " entered, from=", from_path + ", to=", to_path);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        File tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead()) {
                        if (!SyncThread.isHiddenDirectory(stwa, sti, mf) && SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
    //                      if (sti.isSyncOptionSyncEmptyDirectory()) {
    //                          SyncThread.createDirectoryToExternalStorage(stwa, sti, to_path);
    //                      }
                            JcifsFile[] children = mf.listFiles();
                            if (children != null) {
                                archiveFileSmbToExternal(stwa, sti, children, from_path, to_path);
                                for (JcifsFile element : children) {
                                    if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            if (element.isDirectory()) {
                                                if (sti.isSyncOptionSyncSubDirectory()) {
                                                    sync_result = buildArchiveListSmbToExternal(stwa, sti, from_base, from_path + element.getName(),
                                                            element, to_base, to_path + "/" + element.getName().replace("/", ""));
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
                        }
                    } else {
                        stwa.totalIgnoreCount++;
                        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "W", "", "",
                                stwa.context.getString(R.string.msgs_mirror_task_directory_ignored_because_can_not_read, from_path + "/" + mf.getName()));
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.context.getString(R.string.msgs_mirror_task_master_not_found) + " " + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
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
            stwa.util.addLogMsg("E", "", CommonUtilities.getExecutedMethodName() + " From=" + from_path + ", Master file error");
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
        int sync_result = buildArchiveListSmbToSmb(stwa, sti, from_path, from_path, mf, to_path, to_path);
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
                    sync_result= copyFile(stwa, sti, mf.getInputStream(), tf.getOutputStream(), from_path, to_path, file_name, sti.isSyncOptionUseSmallIoBuffer());
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
            }
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                stwa.totalCopyCount++;
                SyncThread.showArchiveMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, to_path, mf.getName(), tf.getName(),
                        "", stwa.msgs_mirror_task_file_archived);
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
            stwa.totalIgnoreCount++;
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", to_path, mf.getName(),
                    "", stwa.context.getString(R.string.msgs_mirror_confirm_move_cancel));
        }

        return sync_result;
    }

    static private int archiveFileSmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile[] children,
                                           String from_path, String to_path) throws IOException, JcifsException {
        int file_seq_no=0, sync_result=0;
        ArrayList<ArchiveFileListItem>fl=buildSmbFileList(stwa, sti, children);
        for(ArchiveFileListItem item:fl) {
            if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                break;
            }
            if (!item.date_from_exif && sti.isSyncOptionConfirmNotExistsExifDate()) {
                if (!SyncThread.sendArchiveConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_ARCHIVE_DATE_FROM_FILE, item.full_path)) {
                    stwa.totalIgnoreCount++;
                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", item.full_path, item.file_name,
                            "", stwa.context.getString(R.string.msgs_mirror_confirm_archive_date_time_from_file_cancel));
                    continue;
                }
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
            String converted_to_path=buildArchiveTargetDirectoryName(stwa, sti, to_path, item);

            if (!sti.isArchiveUseRename()) {
                JcifsFile tf=new JcifsFile(converted_to_path+"/"+item.file_name, stwa.targetAuth);
                if (tf.exists()) {
                    if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, (File)item.file, tf, stwa.ALL_COPY) &&
                            SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, ((File)item.file).getAbsolutePath(), ((File)item.file).lastModified(), tf.getLastModified())) {
                        String new_name = createArchiveSmbNewFilePath(stwa, sti, converted_to_path, converted_to_path + "/" + to_file_name, to_file_ext);
                        if (new_name.equals("")) {
                            stwa.util.addLogMsg("E", "Archive sequence number overflow error.");
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                            break;
                        } else {
                            tf = new JcifsFile(new_name, stwa.targetAuth);
                            sync_result = moveFileSmbToSmb(stwa, sti, item.full_path, (JcifsFile) item.file, tf, tf.getPath(), new_name);
                        }
                    }
                } else {
                    sync_result= moveFileSmbToSmb(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(), to_file_name);
                }
            } else {
                to_file_name=buildArchiveFileName(stwa, sti, item, to_file_name);
                String temp_dir= buildArchiveSubDirectoryName(stwa, sti, item);

                JcifsFile tf=new JcifsFile(converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext, stwa.targetAuth);
                if (tf.exists()) {
                    if (SyncThread.isFileChangedForLocalToRemote(stwa, sti, from_path, (File)item.file, tf, stwa.ALL_COPY) &&
                            SyncThread.checkMasterFileNewerThanTargetFile(stwa, sti, ((File)item.file).getAbsolutePath(), ((File)item.file).lastModified(), tf.getLastModified())) {
                        String new_name = createArchiveSmbNewFilePath(stwa, sti, converted_to_path, converted_to_path + "/" + temp_dir + to_file_name + to_file_seqno, to_file_ext);
                        if (new_name.equals("")) {
                            stwa.util.addLogMsg("E", "Archive sequence number overflow error.");
                            sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
                            break;
                        } else {
                            tf = new JcifsFile(new_name, stwa.targetAuth);
                            sync_result = moveFileSmbToSmb(stwa, sti, item.full_path, (JcifsFile) item.file, tf, tf.getPath(), new_name);
                        }
                    }
                } else {
                    sync_result= moveFileSmbToSmb(stwa, sti, item.full_path, (JcifsFile)item.file, tf, tf.getPath(),
                            converted_to_path+"/"+temp_dir+to_file_name+to_file_seqno+to_file_ext);
                }
            }
        }
        return sync_result;
    }

    static private int buildArchiveListSmbToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                String from_base, String from_path, JcifsFile mf, String to_base, String to_path) {
        stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName(), " entered, from=", from_path, ", to=", to_path);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        JcifsFile tf;
        try {
            if (mf.exists()) {
                String t_from_path = from_path.substring(from_base.length());
                if (t_from_path.startsWith("/")) t_from_path = t_from_path.substring(1);
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead()){
                        if (!SyncThread.isHiddenDirectory(stwa, sti, mf) && SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
    //                      if (sti.isSyncOptionSyncEmptyDirectory()) {
    //                          SyncThread.createDirectoryToSmb(stwa, sti, to_path, stwa.targetAuth);
    //                      }
                            JcifsFile[] children = mf.listFiles();
                            if (children != null) {
                                archiveFileSmbToSmb(stwa, sti, children, from_path, to_path);
                                for (JcifsFile element : children) {
                                    if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                        while (stwa.syncTaskRetryCount > 0) {
                                            if (element.isDirectory()) {
                                                if (sti.isSyncOptionSyncSubDirectory()) {
                                                    sync_result = buildArchiveListSmbToSmb(stwa, sti, from_base, from_path + element.getName(),
                                                            element, to_base, to_path + element.getName());
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
                        }
                    } else {
                        stwa.totalIgnoreCount++;
                        SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "W", "", "",
                                stwa.context.getString(R.string.msgs_mirror_task_directory_ignored_because_can_not_read, from_path + "/" + mf.getName()));
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.context.getString(R.string.msgs_mirror_task_master_not_found) + " " + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
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

    final static int SHOW_PROGRESS_THRESHOLD_VALUE=512;

    static private int copyFile(SyncThreadWorkArea stwa, SyncTaskItem sti, InputStream ifs, OutputStream ofs, String from_path,
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

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = ifs.available();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[io_area_size];
        while ((buffer_read_bytes = ifs.read(buffer)) > 0) {
            ofs.write(buffer, 0, buffer_read_bytes);
            file_read_bytes += buffer_read_bytes;
            if (show_prog && file_size > file_read_bytes) {
                SyncThread.showProgressMsg(stwa, sti.getSyncTaskName(), file_name + " " +
                        String.format(stwa.msgs_mirror_task_file_copying, (file_read_bytes * 100) / file_size));
            }
            if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                ifs.close();
                ofs.flush();
                ofs.close();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();

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
        String shoot_week_number="", shoot_week_day="", shoot_week_day_long="";
        String file_name="";
        String full_path="";
        boolean date_from_exif=true;
    }

    static private ArrayList<ArchiveFileListItem> buildLocalFileList(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children) {
        ArrayList<ArchiveFileListItem>fl=new ArrayList<ArchiveFileListItem>();
        for(File element:children) {
            if (element.isFile() && isFileTypeArchiveTarget(element.getName())) {
                String[] date_time=getFileExifDateTime(stwa, sti, element);
                ArchiveFileListItem afli=new ArchiveFileListItem();
                afli.file=element;
                afli.file_name=element.getName();
                afli.full_path=element.getPath();
                Date date=null;
                if (date_time==null || date_time[0]==null) {
                    String[] dt= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(element.lastModified()).split(" ");
                    afli.shoot_date=dt[0].replace("/","-");
                    afli.shoot_time=dt[1].replace(":","-");
                    date=new Date(element.lastModified());
                    afli.date_from_exif=false;
                } else {
//                    Log.v("SMBSync2","name="+afli.file_name+", 0="+date_time[0]+", 1="+date_time[1]);
                    date=new Date(date_time[0]+" "+date_time[1]);
                    String[] dt= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(date.getTime()).split(" ");
                    afli.shoot_date=date_time[0].replace("/","-");
                    afli.shoot_time=date_time[1].replace(":","-");
                }
                SimpleDateFormat sdf=new SimpleDateFormat(("w"));
                afli.shoot_week_number=sdf.format(date.getTime());
                afli.shoot_week_day=getWeekDay(date.getTime());
                afli.shoot_week_day_long=getWeekDay(date.getTime());

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

    static private ArrayList<ArchiveFileListItem> buildSafFileList(SyncThreadWorkArea stwa, SyncTaskItem sti, File[] children) {
        ArrayList<ArchiveFileListItem>fl=new ArrayList<ArchiveFileListItem>();
        for(File element:children) {
            if (element.isFile() && isFileTypeArchiveTarget(element.getName())) {
                SafFile m_df = SyncThread.createSafFile(stwa, sti, element.getPath(), false);
                String[] date_time=getFileExifDateTime(stwa, sti, m_df);
                ArchiveFileListItem afli=new ArchiveFileListItem();
                afli.file=element;
                afli.file_name=element.getName();
                afli.full_path=element.getPath();
                Date date=null;
                if (date_time==null || date_time[0]==null) {
                    String[] dt= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(element.lastModified()).split(" ");
                    date=new Date(dt[0]+" "+dt[1]);
                    afli.shoot_date=dt[0].replace("/","-");
                    afli.shoot_time=dt[1].replace(":","-");
                    afli.date_from_exif=false;
                } else {
                    date=new Date(date_time[0]+" "+date_time[1]);
                    afli.shoot_date=date_time[0].replace("/","-");
                    afli.shoot_time=date_time[1].replace(":","-");
                }
                SimpleDateFormat sdf=new SimpleDateFormat(("w"));
                afli.shoot_week_number=sdf.format(date.getTime());
                afli.shoot_week_day=getWeekDay(date.getTime());
                afli.shoot_week_day_long=getWeekDay(date.getTime());
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

    static private ArrayList<ArchiveFileListItem> buildSmbFileList(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile[] children) throws JcifsException {
        ArrayList<ArchiveFileListItem>fl=new ArrayList<ArchiveFileListItem>();
        for(JcifsFile element:children) {
            if (element.isFile() && isFileTypeArchiveTarget(element.getName())) {
                String[] date_time=getFileExifDateTime(stwa, sti, element);
                ArchiveFileListItem afli=new ArchiveFileListItem();
                afli.file=element;
                afli.file_name=element.getName();
                afli.full_path=element.getPath();
                Date date=null;
                if (date_time==null || date_time[0]==null) {
                    String[] dt= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(element.getLastModified()).split(" ");
                    date=new Date(dt[0]+" "+dt[1]);
                    afli.shoot_date=dt[0].replace("/","-");
                    afli.shoot_time=dt[1].replace(":","-");
                    afli.date_from_exif=false;
                } else {
                    date=new Date(date_time[0]+" "+date_time[1]);
                    afli.shoot_date=date_time[0].replace("/","-");
                    afli.shoot_time=date_time[1].replace(":","-");
                }
                SimpleDateFormat sdf=new SimpleDateFormat(("w"));
                afli.shoot_week_number=sdf.format(date.getTime());
                sdf=new SimpleDateFormat(("EEE"));
                afli.shoot_week_day=getWeekDay(date.getTime());
                afli.shoot_week_day_long=getWeekDay(date.getTime());
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

    static private String getWeekDay(long time) {
        SimpleDateFormat sdf=new SimpleDateFormat(("w"));
        sdf=new SimpleDateFormat(("EEE"));
        String tmp=sdf.format(time).toLowerCase();
        String result=tmp.endsWith(".")?tmp.substring(0, tmp.length()-1):tmp;
        return result;
    }

    static private String getWeekDayLong(long time) {
        SimpleDateFormat sdf=new SimpleDateFormat(("w"));
        sdf=new SimpleDateFormat(("EEEE"));
        String tmp=sdf.format(time).toLowerCase();
        String result=tmp.endsWith(".")?tmp.substring(0, tmp.length()-1):tmp;
        return result;
    }

    static final public boolean isFileArchiveRequired(SyncThreadWorkArea stwa, SyncTaskItem sti, ArchiveFileListItem afli) {
        Calendar cal=Calendar.getInstance() ;
        String[] dt=afli.shoot_date.split("-");
        String[] tm=afli.shoot_time.split("-");
        cal.set(Integer.parseInt(dt[0]),Integer.parseInt(dt[1])-1,Integer.parseInt(dt[2]),
                Integer.parseInt(tm[0]),Integer.parseInt(tm[1]),Integer.parseInt(tm[2]));
        String c_ft= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(cal.getTimeInMillis());
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
        String n_exp= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(cal.getTimeInMillis()+exp_time);
//        boolean result=(System.currentTimeMillis()>cal.getTimeInMillis());
        boolean result=(System.currentTimeMillis()>(cal.getTimeInMillis()+exp_time));
        stwa.util.addDebugMsg(1,"I","isFileArchiveRequired path=",afli.full_path,", shoot date=",afli.shoot_date,
                ", shoot time=", afli.shoot_time,", exif="+afli.date_from_exif,", archive required="+result, ", " +
                "retention period="+sti.getArchiveRetentionPeriod(), ", expiration date=", n_exp, ", expiration period="+exp_time);
        return result;
    }

    static private String buildArchiveSubDirectoryName(SyncThreadWorkArea stwa, SyncTaskItem sti, ArchiveFileListItem afli) {
        String temp_dir="";
        if (sti.isArchiveCreateDirectory()) {
            if (!sti.getArchiveCreateDirectoryTemplate().equals("")){
                String year=afli.shoot_date.substring(0,4);
                String month=afli.shoot_date.substring(5,7);
                String day=afli.shoot_date.substring(8,10);

                temp_dir=sti.getArchiveCreateDirectoryTemplate().replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_YEAR,year)
                        .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_MONTH,month)
                        .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_DAY,day)
                        .replaceAll(SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_NUMBER,afli.shoot_week_number)
                        .replaceAll(SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY,afli.shoot_week_day)
                        .replaceAll(SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY_LONG,afli.shoot_week_day_long)
                        +"/";
            }
        }

        return temp_dir;
    }

    static private String buildArchiveTargetDirectoryName(SyncThreadWorkArea stwa, SyncTaskItem sti, String to_path, ArchiveFileListItem afli) {
        String target_directory="";
        if (sti.isTargetUseTakenDateTimeToDirectoryNameKeyword()) {
            if (!to_path.equals("")){
                String year=afli.shoot_date.substring(0,4);
                String month=afli.shoot_date.substring(5,7);
                String day=afli.shoot_date.substring(8,10);

                target_directory=to_path.replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_YEAR,year)
                        .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_MONTH,month)
                        .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_DAY,day)
                        .replaceAll(SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_NUMBER,afli.shoot_week_number)
                        .replaceAll(SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY,afli.shoot_week_day)
                        .replaceAll(SMBSYNC2_REPLACEABLE_KEYWORD_WEEK_DAY_LONG,afli.shoot_week_day_long)
                ;
            }
        } else {
            target_directory=to_path;
        }

        return target_directory;
    }

    static private String buildArchiveFileName(SyncThreadWorkArea stwa, SyncTaskItem sti, ArchiveFileListItem afli, String original_name) {
        String to_file_name=original_name;
        if (!sti.getArchiveRenameFileTemplate().equals("")) {
            to_file_name=sti.getArchiveRenameFileTemplate()
                    .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_ORIGINAL_NAME, original_name)
                    .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_DATE, afli.shoot_date)
                    .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_TIME, afli.shoot_time)
                    .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_YYYYMMDD, afli.shoot_date.replaceAll("-",""))
                    .replaceAll(SyncTaskItem.PICTURE_ARCHIVE_RENAME_KEYWORD_HHMMSS, afli.shoot_time.replaceAll("-",""))
            ;
        }
        return to_file_name;
    }

    static private String getFileSeqNumber(SyncThreadWorkArea stwa, SyncTaskItem sti, int seq_no) {
        String seqno="";
        if (sti.getArchiveSuffixOption().equals("2")) seqno=String.format("_%02d", seq_no);
        else if (sti.getArchiveSuffixOption().equals("3")) seqno=String.format("_%03d", seq_no);
        else if (sti.getArchiveSuffixOption().equals("4")) seqno=String.format("_%04d", seq_no);
        else if (sti.getArchiveSuffixOption().equals("5")) seqno=String.format("_%05d", seq_no);
        else if (sti.getArchiveSuffixOption().equals("6")) seqno=String.format("_%06d", seq_no);
        return seqno;
    }

    static final public String[] getFileExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, File lf) {
        String[] date_time=null;
        try {
            FileInputStream fis=new FileInputStream(lf);
            FileInputStream fis_retry=new FileInputStream(lf);
            date_time=getFileExifDateTime(stwa, sti, fis, fis_retry, lf.lastModified(), lf.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return date_time;
    }

    static final public String[] getFileExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, SafFile sf) {
        String[] date_time=null;
        try {
            InputStream fis=stwa.context.getContentResolver().openInputStream(sf.getUri());
            InputStream fis_retry=stwa.context.getContentResolver().openInputStream(sf.getUri());
            date_time=getFileExifDateTime(stwa, sti, fis, fis_retry, sf.lastModified(), sf.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return date_time;
    }

    static final public String[] getFileExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile lf) throws JcifsException {
        String[] date_time=null;
        InputStream fis=lf.getInputStream();
        InputStream fis_retry=lf.getInputStream();
        date_time=getFileExifDateTime(stwa, sti, fis, fis_retry, lf.getLastModified(), lf.getName());
        return date_time;
    }

    static final public String[] getFileExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, InputStream fis,
                                                     InputStream fis_retry, long last_mod, String file_name) {
        String[] date_time=null;
        if (file_name.endsWith(".mp4") || file_name.endsWith(".mov") ) {
            date_time=getMp4ExifDateTime(stwa, fis);
        } else {
            try {
                date_time=getExifDateTime(stwa, fis);//, buff);
                fis.close();
                if (date_time==null || date_time[0]==null) {
                    stwa.util.addDebugMsg(1,"W","Read exif date and time failed, name="+file_name);
                    if (Build.VERSION.SDK_INT>=24) {
                        ExifInterface ei = new ExifInterface(fis_retry);
                        String dt=ei.getAttribute(ExifInterface.TAG_DATETIME);
                        if (dt!=null) {
                            date_time=new String[2];
                            if (dt.endsWith("Z")) {
                                String[] date=dt.split("T");
                                date_time[0]=date[0].replaceAll(":", "/");//Date
                                date_time[1]=date[1].substring(0,date[1].length()-1);//Time
                            } else {
                                String[] date=dt.split(" ");
                                date_time[0]=date[0].replaceAll(":", "/");//Date
                                date_time[1]=date[1];//Time
                            }
                        } else {
                            stwa.util.addDebugMsg(1,"I","Read exif date and time failed by ExifInterface, name="+file_name);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
            }
        }
//        if (date_time==null || date_time[0]==null) {
//            date_time= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(last_mod).split(" ");
//        }
        return date_time;
    }

    static final public String[] getMp4ExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, JcifsFile lf) throws JcifsException {
        String[] result=null;
        InputStream fis=lf.getInputStream();
        result=getMp4ExifDateTime(stwa, fis);
        return result;
    }

    static final public String[] getMp4ExifDateTime(SyncThreadWorkArea stwa, InputStream fis)  {
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
                    if (result!=null && result[0].startsWith("1904")) result=null;
                }
            }
        } catch (ImageProcessingException e) {
            e.printStackTrace();
            putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
        }
        try {
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
        }
        return result;
    }

    static private void putExceptionMessage(SyncThreadWorkArea stwa, StackTraceElement[] st, String e_msg) {
        String st_msg=formatStackTrace(st);
        stwa.util.addDebugMsg(1,"E",stwa.currentSTI.getSyncTaskName()," Error="+e_msg+st_msg);
    }

    static final private String[] parseDateValue(String date_val) {
        String[] result=null;
        if (date_val!=null) {
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
            result= StringUtil.convDateTimeTo_YearMonthDayHourMinSec(cal.getTimeInMillis()).split(" ");
        }
        return result;
    }

    static final public String[] getMp4ExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, File lf) {
        String[] result=null;
        try {
            InputStream fis=new FileInputStream(lf);
            result=getMp4ExifDateTime(stwa, fis);
        } catch (IOException e) {
            e.printStackTrace();
            putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
        }
        return result;
    }

    static final public String[] getMp4ExifDateTime(SyncThreadWorkArea stwa, SyncTaskItem sti, SafFile sf) {
        String[] result=null;
        InputStream fis=null;
        try {
            fis=stwa.context.getContentResolver().openInputStream(sf.getUri());
            result=getMp4ExifDateTime(stwa, fis);
        } catch (IOException e) {
            e.printStackTrace();
            putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
        }
        return result;
    }

    static private byte[] readExifData(BufferedInputStream bis, int read_size) throws IOException {
        byte[] buff=new byte[read_size];
        int rc=bis.read(buff,0,read_size);
        if (rc>0) return buff;
        else return null;
    }

    static public String[] getExifDateTime(SyncThreadWorkArea stwa, InputStream fis) {
        BufferedInputStream bis=new BufferedInputStream(fis, 1024*32);
        String[] result=null;
        try {
            byte[] buff=readExifData(bis, 2);
            if (buff!=null && buff[0]==(byte)0xff && buff[1]==(byte)0xd8) { //JPEG SOI
                while(buff!=null) {// find dde1 jpeg segemnt
                    buff=readExifData(bis, 4);
                    if (buff!=null) {
                        if (buff[0]==(byte)0xff && buff[1]==(byte)0xe1) { //APP1マーカ
                            int seg_size=getIntFrom2Byte(false, buff[2], buff[3]);
                            buff=readExifData(bis, 14);
                            if (buff!=null) {
                                boolean little_endian=false;
                                if (buff[6]==(byte)0x49 && buff[7]==(byte)0x49) little_endian=true;
                                int ifd_offset=getIntFrom4Byte(little_endian, buff[10], buff[11], buff[12], buff[13]);

                                byte[] ifd_buff=new byte[seg_size+ifd_offset];
                                System.arraycopy(buff,6,ifd_buff,0,8);
                                buff=readExifData(bis, seg_size);
                                if (buff!=null) {
                                    System.arraycopy(buff,0,ifd_buff,8,seg_size);
                                    result=process0thIfdTag(little_endian, ifd_buff, ifd_offset);
                                    break;
                                } else {
                                    stwa.util.addDebugMsg(1,"W","Read Exif date and time failed, because unpredical EOF reached.");
                                    return null;
                                }
                            } else {
                                stwa.util.addDebugMsg(1,"W","Read Exif date and time failed, because unpredical EOF reached.");
                                return null;
                            }
                        } else {
                            int offset=((int)buff[2]&0xff)*256+((int)buff[3]&0xff)-2;
                            if (offset<1) {
                                stwa.util.addDebugMsg(1,"W","Read Exif date and time failed, because invalid offset.");
                                return null;
                            }
                            buff=readExifData(bis, offset);
                        }
                    } else {
                        stwa.util.addDebugMsg(1,"W","Read Exif date and time failed, because unpredical EOF reached.");
                        return null;
                    }
                }

            } else {
                stwa.util.addDebugMsg(1,"W","Read exif date and time failed, because Exif header can not be found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
            return null;
        }
        return result;
    }

//    static private String[] getExifDateTime(SyncThreadWorkArea stwa, InputStream fis, byte[] buff) {
//        String[] result=null;
//        try {
//            if (buff[0]==(byte)0xff && buff[1]==(byte)0xd8) {//if jpeg header
//                int i=2;
//                while(i<buff.length-3) {// find dde1 jpeg segemnt
//                    if (buff[i]==(byte)0xff && buff[i+1]==(byte)0xe1) {
//                        int seg_size=getIntFrom2Byte(false, buff[i+2], buff[i+3]);
//                        boolean little_endian=false;
//                        if (buff[i+10]==(byte)0x49 && buff[i+11]==(byte)0x49) little_endian=true;
//                        int ifd_offset=getIntFrom4Byte(little_endian, buff[i+14], buff[i+15], buff[i+16], buff[i+17]);
//
//                        byte[] ifd_buff= Arrays.copyOfRange(buff, i+10, seg_size+ifd_offset);
//                        result=process0thIfdTag(little_endian, ifd_buff, ifd_offset);
//                        break;
//                    } else {
//                        int offset=((int)buff[i+2]&0xff)*256+((int)buff[i+3]&0xff);
//                        i=offset+i+2;
//                    }
//                }
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
//            return null;
//        }
//        return result;
//    }

    static private String formatStackTrace(StackTraceElement[] st) {
        String st_msg = "";
        for (int i = 0; i < st.length; i++) {
            st_msg += "\n at " + st[i].getClassName() + "." +
                    st[i].getMethodName() + "(" + st[i].getFileName() +
                    ":" + st[i].getLineNumber() + ")";
        }
        return st_msg;
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

            if (tag_number==(0x8769&0xffff)) {//Exif IFD
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
            if (tag_number==(0x9003&0xffff)) {//Date&Time TAG
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
