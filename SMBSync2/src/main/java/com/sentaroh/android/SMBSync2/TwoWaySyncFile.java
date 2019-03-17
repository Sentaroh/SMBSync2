package com.sentaroh.android.SMBSync2;

import android.os.RemoteException;
import android.view.View;
import android.widget.LinearLayout;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sentaroh.android.SMBSync2.SyncThread.SyncThreadWorkArea;
import com.sentaroh.android.Utilities.StringUtil;

import static com.sentaroh.android.SMBSync2.Constants.*;

public class TwoWaySyncFile {

    static public int syncTwowayInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, String to_path) {
        loadSyncFileInfoList(stwa, sti);

        File mf=new File(from_path);
        File tf=new File(to_path);
        int sync_result= syncPairInternalToInternal(stwa, sti, from_path, from_path, mf, to_path, to_path, tf);

        saveSyncFileInfoList(stwa, sti);

        return sync_result;
    }

    static private ArrayList<File> getFileList(SyncThreadWorkArea stwa, File file) {
        File[] fl=file.listFiles();
        ArrayList<File> s_fl=new ArrayList<File>();
        if (fl!=null) {
            for(File item:fl) {
                if (!item.getName().endsWith(SyncTaskItem.SYNC_TASK_TWO_WAY_CONFLICT_FILE_SUFFIX)) s_fl.add(item);
                else {
                    stwa.util.addDebugMsg(2,"I",CommonUtilities.getExecutedMethodName()," A file was ignored because file was previously conflict file. Path=",item.getPath());
                }
            }
            Collections.sort(s_fl, new Comparator<File>(){
                @Override
                public int compare(File o1, File o2) {
                    return o1.getPath().compareToIgnoreCase(o2.getPath());
                }
            });

        }
        return s_fl;
    }


    static private int syncPairInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                  String pair_a_base, String pair_a_path, File pair_a_file,
                                                  String pair_b_base, String pair_b_path, File pair_b_file) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName(), " entered, from=", pair_a_path, ", to=", pair_b_path);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        try {
            if (pair_a_file.isDirectory() && pair_b_file.isDirectory()) {
                ArrayList<File> pair_a_file_list= getFileList(stwa, pair_a_file);
                ArrayList<File> pair_b_file_list= getFileList(stwa, pair_b_file);
                boolean exit=false;
                int pair_a_cnt=0, pair_b_cnt=0;
                while(!exit && sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                    if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                        sync_result=SyncTaskItem.SYNC_STATUS_CANCEL;
                        return sync_result;
                    }
                    if (pair_a_cnt<pair_a_file_list.size() && pair_b_cnt<pair_b_file_list.size()) {
                        File pair_a_child_file=pair_a_file_list.get(pair_a_cnt);
                        String pair_a_child_file_path=pair_a_child_file.getPath().replace(pair_a_base, "");
                        File pair_b_child_file=pair_b_file_list.get(pair_b_cnt);
                        String pair_b_child_file_path=pair_b_child_file.getPath().replace(pair_b_base, "");

                        if (pair_a_child_file_path.compareToIgnoreCase(pair_b_child_file_path)==0) {
                            //Same name
                            if (pair_a_child_file.isDirectory() && pair_b_child_file.isDirectory()) {
                                //下位のディレクトリーを処理
                                sync_result=syncPairInternalToInternal(stwa, sti,
                                        pair_a_base, pair_a_child_file.getPath(), pair_a_child_file, pair_b_base, pair_b_child_file.getPath(), pair_b_child_file);
                                pair_a_cnt++;
                                pair_b_cnt++;
                            } else if (pair_a_child_file.isDirectory() && pair_b_child_file.isFile()) {
                                //ディレクトリーとファイルの名前が同じため同期不可
                                stwa.util.addDebugMsg(1,"E","ディレクトリーとファイルの名前が同じため同期不可");
                                stwa.util.addDebugMsg(1,"E","from directory="+pair_a_path+", to file="+pair_b_path);
                                break;
                            } else if (pair_a_child_file.isFile() && pair_b_child_file.isDirectory()) {
                                //ディレクトリーとファイルの名前が同じため同期不可
                                stwa.util.addDebugMsg(1,"E","ディレクトリーとファイルの名前が同じため同期不可");
                                stwa.util.addDebugMsg(1,"E","from file="+pair_a_path+", to directory="+pair_b_path);
                                break;
                            } else if (pair_a_child_file.isFile() == pair_b_child_file.isFile()) {
                                //Fileの同期
                                sync_result= syncFileInternalToInternal(stwa, sti,
                                        pair_a_child_file.getPath(), pair_a_child_file.length(), pair_a_child_file.lastModified(),
                                        pair_b_child_file.getPath(), pair_b_child_file.length(), pair_b_child_file.lastModified());
                                pair_a_cnt++;
                                pair_b_cnt++;
                            }
                        } else if (pair_a_child_file_path.compareToIgnoreCase(pair_b_child_file_path)>0) {
                            //ターゲットディレクトリーをマスターにコピー
                            sync_result= copyDirectoryInternalToInternal(stwa, sti, pair_b_child_file, new File(pair_a_base+"/"+pair_b_child_file_path));
                            pair_b_cnt++;
                        } else if (pair_a_child_file_path.compareToIgnoreCase(pair_b_child_file_path)<0) {
                            //マスターディレクトリーをターゲットにコピー
                            sync_result= copyDirectoryInternalToInternal(stwa, sti, pair_a_child_file, new File(pair_a_base+"/"+pair_a_child_file_path));
                            pair_a_cnt++;
                        }
                    } else {
                        if (pair_a_cnt<pair_a_file_list.size()) {
                            File pair_a_child_file=pair_a_file_list.get(pair_a_cnt);
                            String pair_a_child_file_path=pair_a_child_file.getPath().replace(pair_a_base, "");
                            File out_dir=new File(pair_b_file.getPath()+"/"+pair_a_child_file.getName());
                            //マスターをターゲットにコピー
                            if (pair_a_child_file.isDirectory()) {
                                sync_result= copyDirectoryInternalToInternal(stwa, sti, pair_a_child_file, out_dir);
                            } else {
                                //Fileの同期
                                sync_result=copyFileInternalToInternal(stwa, sti, pair_a_child_file.getPath(), out_dir.getPath());
                                updateSyncFileInfo(stwa, sti, pair_a_child_file.getPath());
                            }
                            pair_a_cnt++;
                        } else if (pair_b_cnt<pair_b_file_list.size()) {
                            File pair_b_child_file=pair_b_file_list.get(pair_b_cnt);
                            String pair_b_child_file_path=pair_b_child_file.getPath().replace(pair_b_base, "");
                            File out_dir=new File(pair_a_file.getPath()+"/"+pair_b_child_file.getName());
                            //ターゲットをマスターにコピー
                            if (pair_b_child_file.isDirectory()) {
                                sync_result= copyDirectoryInternalToInternal(stwa, sti, pair_b_child_file, out_dir);
                            } else {
                                //Fileの同期
                                sync_result=copyFileInternalToInternal(stwa, sti, pair_b_child_file.getPath(), out_dir+"/"+pair_b_child_file.getName());
                                updateSyncFileInfo(stwa, sti, out_dir+"/"+pair_b_child_file.getName());
                            }
                            pair_b_cnt++;
                        } else {
                            break;
                        }
                    }
                }
            } else {
                //ディレクトリーとファイルの名前が同じため同期不可
                stwa.util.addDebugMsg(1,"E","PairAとPairBがディレクトリーとファイルが混在しているため同期不可");
                stwa.util.addDebugMsg(1,"E","PairA="+pair_a_path+", PairB="+pair_b_path);
            }
        } catch (Exception e) {
            putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    };

    static private int copyDirectoryInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, File in_dir, File out_dir) {
        stwa.util.addDebugMsg(2,"I",CommonUtilities.getExecutedMethodName(), " entered, in=", in_dir.getPath(), ", out=", out_dir.getPath());
        if (in_dir.exists()) {
            if (in_dir.isDirectory()) {
                if (!sti.isSyncOptionSyncSubDirectory()) {
                    stwa.util.addDebugMsg(2,"I",CommonUtilities.getExecutedMethodName(), " sync aborted. Sync sub directory option is disabled");
                    return 0;
                }
                File[] file_list=in_dir.listFiles();
                if (sti.isSyncOptionSyncEmptyDirectory()) {
                    if (!sti.isSyncTestMode() && !out_dir.exists()) out_dir.mkdirs();
                } else {
                    stwa.util.addDebugMsg(2,"I",CommonUtilities.getExecutedMethodName(), " sync aborted. Sync empty directory option is disabled");
                    return 0;
                }
                for(File child_file:file_list) {
                    if (child_file.isDirectory()) {
                        copyDirectoryInternalToInternal(stwa, sti, child_file, new File(out_dir.getPath()+"/"+child_file.getName()));
                    } else {
                        copyFileInternalToInternal(stwa, sti, child_file.getPath(), out_dir.getPath()+"/"+child_file.getName());
                        updateSyncFileInfo(stwa, sti, child_file.getPath());
                    }
                }
            } else {
                copyFileInternalToInternal(stwa, sti, in_dir.getPath(), out_dir.getPath()+"/"+in_dir.getName());
                updateSyncFileInfo(stwa, sti, in_dir.getPath());
            }
        }
        return 0;
    }

    static private int syncFileInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                  String pair_a_path, long pair_a_length, long pair_a_last_mod,
                                                  String pair_b_path, long pair_b_length, long pair_b_last_mod) {
        int sync_result=0;
        int fc=isFileChanged(stwa, sti, pair_a_path, pair_a_length, pair_a_last_mod, pair_b_path, pair_b_length, pair_b_last_mod);
        if (fc!=FILE_WAS_NOT_CHANGED) {
            //Difference file detected

            if (fc==FILE_WAS_CONFLICT_BY_TIME) {
                //競合ファイルが検出された
                stwa.util.addDebugMsg(2, "I", "A conflict file has been detected.(Time)");
                if (sti.getSyncTwoWayConflictFileRule().equals(SyncTaskItem.SYNC_TASK_TWO_WAY_OPTION_ASK_USER)) {
                    int c_result = sendTwoWaySyncConfirmRequest(stwa, sti,
                            "双方のファイルで更新時刻に違いがあり同期元のファイルを判定できません、同期元とするファイルを選択してください。",
                            pair_a_path, pair_a_length, pair_a_last_mod, pair_b_path, pair_b_length, pair_b_last_mod);
                    if (c_result==TWOWAY_SYNC_CONFIRM_RESULT_SELECT_A) {//Slect pair A
                        renameConflictFile(stwa, sti, pair_b_path);
                        sync_result=copyFileInternalToInternal(stwa, sti, pair_a_path, pair_b_path);
                        updateSyncFileInfo(stwa, sti, pair_a_path);
                    } else if (c_result==TWOWAY_SYNC_CONFIRM_RESULT_SELECT_B) {//Slect pair B
                        renameConflictFile(stwa, sti, pair_a_path);
                        sync_result=copyFileInternalToInternal(stwa, sti, pair_b_path, pair_a_path);
                        updateSyncFileInfo(stwa, sti, pair_b_path);
                    } else if(c_result==TWOWAY_SYNC_CONFIRM_RESULT_SYNC_ABORT) {
                        sync_result=SyncTaskItem.SYNC_STATUS_CANCEL;
                    }
                } else if (sti.getSyncTwoWayConflictFileRule().equals(SyncTaskItem.SYNC_TASK_TWO_WAY_OPTION_COPY_NEWER)) {
                    if (pair_a_last_mod > pair_b_last_mod) {
                        renameConflictFile(stwa, sti, pair_b_path);
                        sync_result = copyFileInternalToInternal(stwa, sti, pair_a_path, pair_b_path);
                        updateSyncFileInfo(stwa, sti, pair_a_path);
                    } else {
                        renameConflictFile(stwa, sti, pair_a_path);
                        sync_result = copyFileInternalToInternal(stwa, sti, pair_b_path, pair_a_path);
                        updateSyncFileInfo(stwa, sti, pair_a_path);
                    }
                } else if (sti.getSyncTwoWayConflictFileRule().equals(SyncTaskItem.SYNC_TASK_TWO_WAY_OPTION_COPY_OLDER)) {
                    if (pair_a_last_mod < pair_b_last_mod) {
                        renameConflictFile(stwa, sti, pair_b_path);
                        sync_result = copyFileInternalToInternal(stwa, sti, pair_a_path, pair_b_path);
                        updateSyncFileInfo(stwa, sti, pair_a_path);
                    } else {
                        renameConflictFile(stwa, sti, pair_a_path);
                        sync_result = copyFileInternalToInternal(stwa, sti, pair_b_path, pair_a_path);
                        updateSyncFileInfo(stwa, sti, pair_a_path);
                    }
                }
            } else if (fc==FILE_WAS_CONFLICT_BY_LENGTH) {
                stwa.util.addDebugMsg(2, "I", "A conflict file has been detected.(Length)");
                int c_result = sendTwoWaySyncConfirmRequest(stwa, sti,
                        "最終更新時刻は同じですがファイルサイズに違いがあります、同期元とするファイルを選択してください。",
                        pair_a_path, pair_a_length, pair_a_last_mod, pair_b_path, pair_b_length, pair_b_last_mod);
                if (c_result==TWOWAY_SYNC_CONFIRM_RESULT_SELECT_A) {//Slect pair A
                    renameConflictFile(stwa, sti, pair_b_path);
                    sync_result=copyFileInternalToInternal(stwa, sti, pair_a_path, pair_b_path);
                    updateSyncFileInfo(stwa, sti, pair_a_path);
                } else if (c_result==TWOWAY_SYNC_CONFIRM_RESULT_SELECT_B) {//Slect pair B
                    renameConflictFile(stwa, sti, pair_a_path);
                    sync_result=copyFileInternalToInternal(stwa, sti, pair_b_path, pair_a_path);
                    updateSyncFileInfo(stwa, sti, pair_a_path);
                } else if(c_result==TWOWAY_SYNC_CONFIRM_RESULT_SYNC_ABORT) {
                    sync_result=SyncTaskItem.SYNC_STATUS_CANCEL;
                }
            } else {
                stwa.util.addDebugMsg(2,"I","The file has been detected to have a difference.");
                //Fileをコピー
                if (pair_a_last_mod>pair_b_last_mod) {
                    //Pair A to Pair B
                    if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, pair_b_path)) {
                        sync_result=copyFileInternalToInternal(stwa, sti, pair_a_path, pair_b_path);
                        updateSyncFileInfo(stwa, sti, pair_a_path);
                    } else {
                        stwa.util.addLogMsg("W", pair_b_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                    }
                } else if (pair_a_last_mod<pair_b_last_mod) {
                    //Pair B to Pair A
                    if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, pair_a_path)) {
                        sync_result=copyFileInternalToInternal(stwa, sti, pair_b_path, pair_a_path);
                        updateSyncFileInfo(stwa, sti, pair_a_path);
                    } else {
                        stwa.util.addLogMsg("W", pair_a_path, " "+stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                    }
                }
            }
        } else {
        }
        return sync_result;
    }

    static private void renameConflictFile(SyncThreadWorkArea stwa, SyncTaskItem sti, String fp) {
        if (sti.isSyncTwoWayKeepConflictFile() && !sti.isSyncTestMode()) {
            File tmp = new File(fp);
            tmp.renameTo(new File(tmp.getPath() + "." + Long.valueOf(System.currentTimeMillis()) + SyncTaskItem.SYNC_TASK_TWO_WAY_CONFLICT_FILE_SUFFIX));
        }
    }

    static private int copyFileInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, String to_path) {
        stwa.util.addDebugMsg(2,"I",CommonUtilities.getExecutedMethodName() + " entered, in="+from_path+", out="+to_path);
        int sync_result=SyncTaskItem.SYNC_STATUS_SUCCESS;
        if (from_path.endsWith(SyncTaskItem.SYNC_TASK_TWO_WAY_CONFLICT_FILE_SUFFIX)) {
            stwa.util.addDebugMsg(2,"I",CommonUtilities.getExecutedMethodName() + " copy ignored because file is conflict keep file.");
            return sync_result;
        }
        if (!sti.isSyncTestMode()) {
            //Write mode
            String dest_path=to_path;
            String temp_path = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";
            File out_file=new File(temp_path);
            File in_file=new File(from_path);
            InputStream fis=null;
            OutputStream fos=null;
            try {
                fis=new FileInputStream(in_file);
                fos=new FileOutputStream(out_file);
                sync_result=copyFile(stwa, sti, dest_path, in_file.getName(), in_file.length(), fos, fis);
                if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                    out_file.setLastModified(in_file.lastModified());
                    File df=new File(dest_path);
                    boolean dest_file_exists=df.exists();
                    if (dest_file_exists) df.delete();
                    out_file.renameTo(df);

                    String tmsg = dest_file_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", dest_path, in_file.getName(), tmsg);
                } else {
                    if (out_file.exists()) out_file.delete();
                }
            } catch(IOException e) {
                if (fis!=null) try {fis.close();} catch(IOException e1) {}
                if (fos!=null) try {fos.close();} catch(IOException e1) {}
                if (out_file.exists()) out_file.delete();
                putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
            }
        } else {
            //Test mode
            File df=new File(to_path);
            boolean dest_file_exists=df.exists();
            String tmsg = dest_file_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", to_path, df.getName(), tmsg);
        }
        return sync_result;
    }

    private final static int SHOW_PROGRESS_THRESHOLD_VALUE = 1024 * 1024 * 4;
    private final static int IO_AREA_SIZE = 1024 * 1024;
    public final static int LARGE_BUFFERED_STREAM_BUFFER_SIZE = 1024 * 1024 * 4;

    static private int copyFile(SyncThreadWorkArea stwa, SyncTaskItem sti, String to_path, String to_name,
                                long file_size, OutputStream fos, InputStream fis) {
        long read_begin_time = System.currentTimeMillis();

        int buffer_size=LARGE_BUFFERED_STREAM_BUFFER_SIZE;//, io_area_size=IO_AREA_SIZE;
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        if (sti.isSyncOptionUseSmallIoBuffer() && sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            buffer_size=1024*16-1;
//            io_area_size=1024*16-1;
            show_prog=(file_size > 1024*64);
        }

        try {
            byte[] buff=new byte[buffer_size];
            int rc=0;
            long file_read_bytes = 0;
            while((rc=fis.read(buff))>0) {
                if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                    fis.close();
                    fos.flush();
                    fos.close();
                    return SyncTaskItem.SYNC_STATUS_CANCEL;
                };
                fos.write(buff, 0, rc);
                file_read_bytes += rc;
                if (show_prog && file_size > file_read_bytes) {
                    SyncThread.showProgressMsg(stwa, sti.getSyncTaskName(), to_name + " " +
                            String.format(stwa.msgs_mirror_task_file_copying, (file_read_bytes * 100) / file_size));
                }

            }
            fis.close();
            fos.flush();
            fos.close();

            long file_read_time = System.currentTimeMillis() - read_begin_time;

            if (stwa.gp.settingDebugLevel >= 1)
                stwa.util.addDebugMsg(1, "I", to_path + " " + file_read_bytes + " bytes transfered in ",file_read_time + " mili seconds at " +
                        SyncThread.calTransferRate(file_read_bytes, file_read_time));
            stwa.totalTransferByte += file_read_bytes;
            stwa.totalTransferTime += file_read_time;
        } catch(IOException e) {
            try {fis.close();} catch(IOException e1) {}
            try {fos.close();} catch(IOException e1) {}
            putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
        }

        return 0;
    }

    private static final int FILE_WAS_NOT_CHANGED=0;
    private static final int FILE_WAS_CONFLICT_BY_TIME=1;
    private static final int FILE_WAS_CONFLICT_BY_LENGTH=2;
    private static final int FILE_WAS_CHANGED=3;
    static private int isFileChanged(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                     String pair_a_path,  long pair_a_length, long pair_a_last_mod,
                                     String pair_b_path,  long pair_b_length, long pair_b_last_mod) {
        int result=0;

        TwoWaySyncFileInfoItem sfi=getSyncFileInfo(stwa, sti, pair_a_path);
        boolean file_found=false;
        if (sfi==null) {
            updateSyncFileInfo(stwa, sti, pair_a_path, System.currentTimeMillis(), pair_a_length, pair_a_last_mod);
            sfi=new TwoWaySyncFileInfoItem();
            sfi.fileSize=pair_a_length;
            sfi.fileLastModified=pair_a_last_mod;
            sfi.filePath=pair_a_path;
        } else {
            file_found=true;
            sfi.referenced=true;
        }

//        long diff_length_a_b=Math.abs(a_length-b_length);
        long diff_last_mod_a_b=Math.abs(pair_a_last_mod-pair_b_last_mod);

//        long diff_length_s_b=Math.abs(sfi.fileSize-b_length);
        long diff_last_mod_s_b=Math.abs(sfi.fileLastModified-pair_b_last_mod);

        long allowed_time=0;//(long)sti.getSyncOptionDifferentFileAllowableTime();

//        boolean is_length_diff_a_b=diff_length_a_b!=0;
        boolean is_last_mod_diff_a_b=diff_last_mod_a_b>allowed_time;

//        boolean is_length_diff_s_b=diff_length_s_b!=0;
        boolean is_last_mod_diff_s_b=diff_last_mod_s_b>allowed_time;

//        boolean is_pair_a_saved_changed_length =a_length==sfi.fileSize;
        boolean is_pair_a_saved_changed_last_mod =pair_a_last_mod!=sfi.fileLastModified;

//        boolean is_pair_b_saved_changed_length =b_length==sfi.fileSize;
        boolean is_pair_b_saved_changed_last_mod =Math.abs(pair_b_last_mod-sfi.fileLastModified)>allowed_time;

        if (stwa.gp.settingDebugLevel>=3) {
            stwa.util.addDebugMsg(3,"I","isFileChanged File attrinute.");
            stwa.util.addDebugMsg(3,"I","   Pair_A="+pair_a_path+", Length="+pair_a_length+", LastModified="+pair_a_last_mod);
            stwa.util.addDebugMsg(3,"I","   Pair_B="+pair_b_path+", Length="+pair_b_length+", LastModified="+pair_b_last_mod);
            stwa.util.addDebugMsg(3,"I","   Last sync Pair_A="+sfi.filePath+", Length="+sfi.fileSize+", LastModified="+sfi.fileLastModified+", found="+file_found);
        }
        if (is_last_mod_diff_a_b) {
            //Difference file detected
            if (is_pair_a_saved_changed_last_mod && is_pair_b_saved_changed_last_mod) {
                //競合ファイルが検出された
                result=FILE_WAS_CONFLICT_BY_TIME;
                stwa.util.addDebugMsg(2,"I","A conflict file has been detected.");
            } else {
                result=FILE_WAS_CHANGED;
                stwa.util.addDebugMsg(2,"I","The file has been detected to have a difference.");
                //Fileをコピー
            }
        } else {
            if (pair_a_length!=pair_b_length) {
                result=FILE_WAS_CONFLICT_BY_LENGTH;
//                stwa.util.addLogMsg("W","The last modified time is the same, but the file size is different and cannot be synchronized. Copy the file manually or delete the files you don't need.");
                stwa.util.addLogMsg("W","最終更新時刻は同じですがファイルサイズに違いがあります");
                stwa.util.addLogMsg("I","   Pair_A="+pair_a_path+", Length="+pair_a_length+", " +
                        "LastModified="+ StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(pair_a_last_mod));
                stwa.util.addLogMsg("I","   Pair_B="+pair_b_path+", Length="+pair_b_length+", " +
                        "LastModified="+ StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(pair_b_last_mod));
            }
        }
        stwa.util.addDebugMsg(2,"I","isFileChanged result="+result+", Path=",pair_a_path);
        return result;
    }

    static private void putExceptionMessage(SyncThreadWorkArea stwa, StackTraceElement[] st, String e_msg) {
        String st_msg=formatStackTrace(st);
        stwa.util.addDebugMsg(1,"E",stwa.currentSTI.getSyncTaskName()," Error="+e_msg+st_msg);
    }

    static private String formatStackTrace(StackTraceElement[] st) {
        String st_msg = "";
        for (int i = 0; i < st.length; i++) {
            st_msg += "\n at " + st[i].getClassName() + "." +
                    st[i].getMethodName() + "(" + st[i].getFileName() +
                    ":" + st[i].getLineNumber() + ")";
        }
        return st_msg;
    }

    public static void showConfirmDialogConflict(GlobalParameters gp, CommonUtilities cu, ISvcClient sc, final String method, String msg,
                                                 final String pair_a_path, final long pair_a_length, final long pair_a_last_mod,
                                                 final String pair_b_path, final long pair_b_length, final long pair_b_last_mod) {
        cu.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered");
        gp.confirmOverrideView.setVisibility(LinearLayout.GONE);
        gp.confirmConflictView.setVisibility(LinearLayout.VISIBLE);
        gp.confirmDialogShowed = true;
        gp.confirmDialogFilePathPairA = pair_a_path;
        gp.confirmDialogFileLengthPairA = pair_a_length;
        gp.confirmDialogFileLastModPairA = pair_a_last_mod;
        gp.confirmDialogFilePathPairB = pair_b_path;
        gp.confirmDialogFileLengthPairB = pair_b_length;
        gp.confirmDialogFileLastModPairB = pair_b_last_mod;
        gp.confirmDialogMethod = method;
        gp.confirmDialogMessage = msg;
        gp.confirmDialogConflictFilePathA.setText(gp.confirmDialogFilePathPairA);
        gp.confirmDialogConflictFileLengthA.setText(String.format("%,d",gp.confirmDialogFileLengthPairA)+" bytes");
        gp.confirmDialogConflictFileLastModA.setText(StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(gp.confirmDialogFileLastModPairA));
        gp.confirmDialogConflictFilePathB.setText(gp.confirmDialogFilePathPairB);
        gp.confirmDialogConflictFileLengthB.setText(String.format("%,d",gp.confirmDialogFileLengthPairB)+" bytes");
        gp.confirmDialogConflictFileLastModB.setText(StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(gp.confirmDialogFileLastModPairB));

        gp.confirmView.setVisibility(LinearLayout.VISIBLE);
        gp.confirmView.setBackgroundColor(gp.themeColorList.dialog_msg_background_color);
        gp.confirmView.bringToFront();
        gp.confirmMsg.setText(msg);

        // Select Aボタンの指定
        gp.confirmDialogConflictButtonSelectAListener = new View.OnClickListener() {
            public void onClick(View v) {
                gp.confirmView.setVisibility(LinearLayout.GONE);
                ActivityMain.sendConfirmResponse(gp, sc, SMBSYNC2_CONFIRM_CONFLICT_RESP_SELECT_A);
            }
        };
        gp.confirmDialogConflictButtonSelectA.setOnClickListener(gp.confirmDialogConflictButtonSelectAListener);

        // Select Bボタンの指定
        gp.confirmDialogConflictButtonSelectBListener = new View.OnClickListener() {
            public void onClick(View v) {
                gp.confirmView.setVisibility(LinearLayout.GONE);
                ActivityMain.sendConfirmResponse(gp, sc, SMBSYNC2_CONFIRM_CONFLICT_RESP_SELECT_B);
            }
        };
        gp.confirmDialogConflictButtonSelectB.setOnClickListener(gp.confirmDialogConflictButtonSelectBListener);

        // Ignoreボタンの指定
        gp.confirmDialogConflictButtonSyncIgnoreFileListener = new View.OnClickListener() {
            public void onClick(View v) {
                gp.confirmView.setVisibility(LinearLayout.GONE);
                ActivityMain.sendConfirmResponse(gp, sc, SMBSYNC2_CONFIRM_CONFLICT_RESP_NO);
            }
        };
        gp.confirmDialogConflictButtonSyncIgnoreFile.setOnClickListener(gp.confirmDialogConflictButtonSyncIgnoreFileListener);

        // Ignoreボタンの指定
        gp.confirmDialogConflictButtonCancelSyncTaskListener = new View.OnClickListener() {
            public void onClick(View v) {
                gp.confirmView.setVisibility(LinearLayout.GONE);
                ActivityMain.sendConfirmResponse(gp, sc, SMBSYNC2_CONFIRM_CONFLICT_RESP_CANCEL);
            }
        };
        gp.confirmDialogConflictButtonCancelSyncTask.setOnClickListener(gp.confirmDialogConflictButtonCancelSyncTaskListener);

    }

    public final static int TWOWAY_SYNC_CONFIRM_RESULT_IGNORE=0;
    public final static int TWOWAY_SYNC_CONFIRM_RESULT_SELECT_A=1;
    public final static int TWOWAY_SYNC_CONFIRM_RESULT_SELECT_B=2;
    public final static int TWOWAY_SYNC_CONFIRM_RESULT_SYNC_ABORT=3;
    static final public int sendTwoWaySyncConfirmRequest(SyncThreadWorkArea stwa, SyncTaskItem sti, String msg,
                                                             String pair_a_path, long pair_a_length, long pair_a_last_mod,
                                                             String pair_b_path, long pair_b_length, long pair_b_last_mod) {
        int result = TWOWAY_SYNC_CONFIRM_RESULT_IGNORE;
        stwa.util.addDebugMsg(2, "I", "sendTwoWaySyncConfirmRequest entered PairA=", pair_a_path+", PairB="+pair_b_path);
        String type=SMBSYNC2_CONFIRM_REQUEST_CONFLICT_FILE;
        try {
            NotificationUtil.showOngoingMsg(stwa.gp, stwa.util, 0, msg);
            stwa.gp.confirmDialogShowed = true;
            stwa.gp.confirmDialogMethod = type;
            stwa.gp.syncThreadConfirm.initThreadCtrl();
            stwa.gp.releaseWakeLock(stwa.util);
            if (stwa.gp.callbackStub != null) {
                stwa.gp.callbackStub.cbShowConfirmDialog(type, msg, pair_a_path, pair_a_length, pair_a_last_mod,
                        pair_b_path, pair_b_length, pair_b_last_mod);
            }
            synchronized (stwa.gp.syncThreadConfirm) {
                stwa.gp.syncThreadConfirmWait = true;
                stwa.gp.syncThreadConfirm.wait();//Posted by SMBSyncService#aidlConfirmResponse()
                stwa.gp.syncThreadConfirmWait = false;
            }
            stwa.gp.acquireWakeLock(stwa.util);
            if (stwa.gp.syncThreadConfirm.getExtraDataInt()==SMBSYNC2_CONFIRM_CONFLICT_RESP_CANCEL) result=TWOWAY_SYNC_CONFIRM_RESULT_SYNC_ABORT;
            else if (stwa.gp.syncThreadConfirm.getExtraDataInt()==SMBSYNC2_CONFIRM_CONFLICT_RESP_SELECT_A) result=TWOWAY_SYNC_CONFIRM_RESULT_SELECT_A;
            else if (stwa.gp.syncThreadConfirm.getExtraDataInt()==SMBSYNC2_CONFIRM_CONFLICT_RESP_SELECT_B) result=TWOWAY_SYNC_CONFIRM_RESULT_SELECT_B;
        } catch (RemoteException e) {
            stwa.util.addLogMsg("E", "", "RemoteException occured");
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
        } catch (InterruptedException e) {
            stwa.util.addLogMsg("E", "", "InterruptedException occured");
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
        }

        if (result==TWOWAY_SYNC_CONFIRM_RESULT_SYNC_ABORT) stwa.gp.syncThreadCtrl.setDisabled();
        stwa.util.addDebugMsg(2, "I", "sendConfirmRequest result=" + result);

        return result;
    }

    static private TwoWaySyncFileInfoItem getSyncFileInfo(SyncThreadWorkArea stwa, SyncTaskItem sti, String fpath) {
        TwoWaySyncFileInfoItem srch=new TwoWaySyncFileInfoItem();
        srch.filePath =fpath;
        int idx=Collections.binarySearch(stwa.currSyncFileInfoList, srch, new Comparator<TwoWaySyncFileInfoItem>() {
            @Override
            public int compare(TwoWaySyncFileInfoItem o1, TwoWaySyncFileInfoItem o2) {
                return o1.filePath.compareToIgnoreCase(o2.filePath);
            }
        });
        TwoWaySyncFileInfoItem result=null;
        if (idx<0) {
            for(TwoWaySyncFileInfoItem item:stwa.newSyncFileInfoList) {
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

    static private void updateSyncFileInfo(SyncThreadWorkArea stwa, SyncTaskItem sti, String fpath) {
        File lf=new File(fpath);
        if (lf.exists()) updateSyncFileInfo(stwa, sti, fpath, System.currentTimeMillis(), lf.length(), lf.lastModified());
        else {
            stwa.util.addDebugMsg(1,"I",CommonUtilities.getExecutedMethodName() + " update failed because file does not exists. FP="+fpath);
        }
    }

    static private void updateSyncFileInfo(SyncThreadWorkArea stwa, SyncTaskItem sti, String fpath,
                                           long sync_time, long file_size, long file_last_modified) {
        TwoWaySyncFileInfoItem srch=new TwoWaySyncFileInfoItem();
        srch.filePath =fpath;
        int idx=Collections.binarySearch(stwa.currSyncFileInfoList, srch, new Comparator<TwoWaySyncFileInfoItem>() {
            @Override
            public int compare(TwoWaySyncFileInfoItem o1, TwoWaySyncFileInfoItem o2) {
                return o1.filePath.compareToIgnoreCase(o2.filePath);
            }
        });
        TwoWaySyncFileInfoItem result=null;
        if (idx<0) {
            for(TwoWaySyncFileInfoItem item:stwa.newSyncFileInfoList) {
                if (item.filePath.equals(srch.filePath)) {
                    result=item;
                    break;
                }
            }
        } else {
            result=stwa.currSyncFileInfoList.get(idx);
        }
        if (result!=null) {
            result.fileLastModified =file_last_modified;
            result.fileSize =file_size;
            result.syncTime=sync_time;
        } else {
            TwoWaySyncFileInfoItem new_item=new TwoWaySyncFileInfoItem();
            new_item.filePath=fpath;
            new_item.fileLastModified =file_last_modified;
            new_item.fileSize =file_size;
            new_item.syncTime=sync_time;
            stwa.newSyncFileInfoList.add(new_item);
        }
    }

    static public final String TWOWAY_SYNC_FILE_MANAGEMENT_FILE_NAME=".twoway_sync_file_list";

    static public void saveSyncFileInfoList(SyncThreadWorkArea stwa, SyncTaskItem sti) {
        if (stwa.newSyncFileInfoList.size()>0) {//merge current list
            stwa.currSyncFileInfoList.addAll(stwa.newSyncFileInfoList);
            Collections.sort(stwa.currSyncFileInfoList, new Comparator<TwoWaySyncFileInfoItem>() {
                @Override
                public int compare(TwoWaySyncFileInfoItem o1, TwoWaySyncFileInfoItem o2) {
                    return o1.filePath.compareToIgnoreCase(o2.filePath);
                }
            });
            stwa.newSyncFileInfoList.clear();
        }

        try {
            FileOutputStream fos = new FileOutputStream(new File(stwa.gp.settingMgtFileDir+"/"+TWOWAY_SYNC_FILE_MANAGEMENT_FILE_NAME), false);
            BufferedOutputStream bos = new BufferedOutputStream(fos, 1024 * 1024);
            ZipOutputStream zos = new ZipOutputStream(bos);
            ZipEntry ze = new ZipEntry("sync_list.txt");
            zos.putNextEntry(ze);
            OutputStreamWriter osw = new OutputStreamWriter(zos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw, 1024 * 1024 * 4);

            StringBuffer pl = new StringBuffer(512);
            String last_fp = "";
            String new_fp = "";
            for(TwoWaySyncFileInfoItem item:stwa.currSyncFileInfoList) {
                new_fp = item.filePath;
                if (!last_fp.equals(new_fp)) {
                    pl.append(new_fp)
                            .append("\t")
                            .append(String.valueOf(item.syncTime))
                            .append("\t")
                            .append(String.valueOf(item.fileSize))
                            .append("\t")
                            .append(String.valueOf(item.fileLastModified))
                            .append("\n");
                    bw.append(pl);
                    pl.setLength(0);
                } else {
                }
            }

            bw.flush();
            bw.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void loadSyncFileInfoList(SyncThreadWorkArea stwa, SyncTaskItem sti) {
        try {
            FileInputStream fis = new FileInputStream(new File(stwa.gp.settingMgtFileDir+"/"+TWOWAY_SYNC_FILE_MANAGEMENT_FILE_NAME));
            BufferedInputStream bis = new BufferedInputStream(fis, 1024 * 1024);
            ZipInputStream zis = new ZipInputStream(bis);
            zis.getNextEntry();
            InputStreamReader isr = new InputStreamReader(zis, "UTF-8");
            BufferedReader br = new BufferedReader(isr, 1024 * 1024 * 4);

            String line = null;

            ArrayList<TwoWaySyncFileInfoItem> fl=new ArrayList<TwoWaySyncFileInfoItem>();

            String[] l_array = null;
            String last_fp = "";
            while ((line = br.readLine()) != null) {
                l_array = line.split("\t");
                if (l_array != null && l_array.length == 4) {
                    if (!last_fp.equals(l_array[0])) {
                        fl.add(new TwoWaySyncFileInfoItem(false, false,
                                Long.valueOf(l_array[1]), l_array[0], Long.valueOf(l_array[2]), Long.valueOf(l_array[3])));
                        last_fp = l_array[0];
                    } else {
                        stwa.util.addDebugMsg(1,"W",CommonUtilities.getExecutedMethodName() + " duplicate entry detected, fp="+l_array[0]);
                    }
                }
            }
            fis.close();

            stwa.currSyncFileInfoList.clear();
            stwa.currSyncFileInfoList.addAll(fl);
            stwa.newSyncFileInfoList.clear();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

