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

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.sentaroh.android.SMBSync2.SyncThread.SyncThreadWorkArea;
import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.android.Utilities.SafFileManager.SafFileItem;

public class SyncThreadCopyFile {

    public final static int SHOW_PROGRESS_THRESHOLD_VALUE = 1024 * 1024 * 4;

    public final static int IO_AREA_SIZE = 1024 * 4 * 256;

    public final static int LARGE_BUFFERED_STREAM_BUFFER_SIZE = 1024 * 1024 * 4;

    static public int copyFileInternalToInternal(SyncThreadWorkArea stwa,
                                                 SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name)
            throws IOException {
        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        File out_file = new File(to_file_path);
        File t_dir = new File(to_dir);
        if (!t_dir.exists()) t_dir.mkdirs();

        FileInputStream is = new FileInputStream(mf);
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);
        FileOutputStream os = new FileOutputStream(out_file);
        BufferedOutputStream ofs = new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[IO_AREA_SIZE];
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (sti.isSyncUseFileCopyByTempName()) {
            File out_dest = new File(to_file_dest);
            if (out_dest.exists()) out_dest.delete();
            out_file.renameTo(out_dest);
        }

        long file_read_time = System.currentTimeMillis() - read_begin_time;
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileInternalToExternal(SyncThreadWorkArea stwa,
                                                 SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name)
            throws IOException {
        long read_begin_time = System.currentTimeMillis();
        File tlf = new File(to_dir + "/" + file_name);
//		Log.v("","name="+tlf.getPath()+"exists="+tlf.exists()+", length="+tlf.length());
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        SafFile t_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_file_path, false);
        if (t_df == null) {
            String saf_name = "";
            SafFile sf = stwa.gp.safMgr.getSdcardSafFile();
            if (sf != null) saf_name = sf.getName();
            stwa.util.addLogMsg("E", "SAF file not found error. path=" + to_file_path + ", SafFile=" + saf_name +
                    ", sdcard=" + stwa.gp.safMgr.getSdcardDirectory());
            ArrayList<SafFileItem> sl = stwa.gp.safMgr.getSafList();
            for (SafFileItem sfi : sl) {
                stwa.util.addLogMsg("E", "SafFileItem UUID=" + sfi.storageUuid + ", path=" + sfi.storageRootDirectory +
                        ", mount=" + sfi.storageIsMounted + ", sdcard=" + sfi.storageTypeSdcard);
            }
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        FileInputStream is = new FileInputStream(mf);
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);
        OutputStream os = stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri());
        BufferedOutputStream ofs = new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[IO_AREA_SIZE];
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
                if (sti.isSyncUseFileCopyByTempName()) t_df.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (sti.isSyncUseFileCopyByTempName()) {
            File out_dest = new File(to_file_dest);
            SafFile o_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_file_dest, false);
            if (out_dest.exists()) {
                o_df.delete();
            }
            t_df.renameTo(file_name);
        }

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    ;

    static public int copyFileInternalToSmb(SyncThreadWorkArea stwa,
                                            SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name)
            throws IOException, JcifsException {
        if (sti.isSyncUseSmallIoBuffer()) {
            return copyFileInternalToSmbSmallBuffer(stwa, sti, from_dir, mf, to_dir, file_name);
        } else {
            return copyFileInternalToSmbLargeBuffer(stwa, sti, from_dir, mf, to_dir, file_name);
        }
    }

    ;

    static private int copyFileInternalToSmbSmallBuffer(SyncThreadWorkArea stwa,
                                                        SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name)
            throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "copyFileInternalToSmbSmallBuffer from_dir=" + from_dir + ", to_dir=" + to_dir + ", name=" + file_name);
        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        JcifsFile out_file = new JcifsFile(to_file_path, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        FileInputStream is = new FileInputStream(mf);
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);
        OutputStream ofs = out_file.getOutputStream();
//		BufferedOutputStream ofs=new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[1024 * 16];
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_file.setLastModified(mf.lastModified());
        if (sti.isSyncUseFileCopyByTempName()) {
            if (out_dest.exists()) out_dest.delete();
            out_file.renameTo(out_dest);
        }
//        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_dest.setLastModified(mf.lastModified());

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static private int copyFileInternalToSmbLargeBuffer(SyncThreadWorkArea stwa,
                                                        SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name)
            throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "copyFileInternalToSmbLargeBuffer from_dir=" + from_dir + ", to_dir=" + to_dir + ", name=" + file_name);

        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
//        JcifsFile jout_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
//        jcifs.smb.SmbFile out_dest=jout_dest.getSmb1File();
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        JcifsFile out_file = new JcifsFile(to_file_path, stwa.targetAuth);
//        JcifsFile jout_file = new JcifsFile(to_file_path, stwa.targetAuth);
//        jcifs.smb.SmbFile out_file=jout_file.getSmb1File();
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        FileInputStream is = new FileInputStream(mf);
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);
        OutputStream os = out_file.getOutputStream();
        BufferedOutputStream ofs = new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[IO_AREA_SIZE];
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_file.setLastModified(mf.lastModified());
        if (sti.isSyncUseFileCopyByTempName()) {
            if (out_dest.exists()) out_dest.delete();
            out_file.renameTo(out_dest);
        }
//        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_dest.setLastModified(mf.lastModified());

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileSmbToSmb(SyncThreadWorkArea stwa,
                                       SyncTaskItem sti, String from_dir, String to_dir, String file_name)
            throws IOException, JcifsException {
        if (sti.isSyncUseSmallIoBuffer()) {
            return copyFileSmbToSmbSmallBuffer(stwa, sti, from_dir, to_dir, file_name);
        } else {
            return copyFileSmbToSmbLargeBuffer(stwa, sti, from_dir, to_dir, file_name);
        }
    }

    static private int copyFileSmbToSmbSmallBuffer(SyncThreadWorkArea stwa,
                                                   SyncTaskItem sti, String from_dir, String to_dir, String file_name)
            throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "copyFileSmbToSmbSmallBuffer from_dir=" + from_dir + ", to_dir=" + to_dir + ", name=" + file_name);
        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        JcifsFile out_file = new JcifsFile(to_file_path, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        String in_file_path = from_dir + "/" + file_name;
        JcifsFile mf = new JcifsFile(in_file_path, stwa.masterAuth);

        InputStream is = mf.getInputStream();
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        OutputStream ofs = out_file.getOutputStream();
//		BufferedOutputStream ofs=new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[1024 * 16];
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_file.setLastModified(mf.getLastModified());
        if (sti.isSyncUseFileCopyByTempName()) {
            if (out_dest.exists()) out_dest.delete();
            out_file.renameTo(out_dest);
        }
//        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_dest.setLastModified(mf.getLastModified());

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    ;

    static private int copyFileSmbToSmbLargeBuffer(SyncThreadWorkArea stwa,
                                                   SyncTaskItem sti, String from_dir, String to_dir, String file_name)
            throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", "copyFileSmbToSmbLargeBuffer from_dir=" + from_dir + ", to_dir=" + to_dir + ", name=" + file_name);

        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        JcifsFile out_file = new JcifsFile(to_file_path, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        String in_file_path = from_dir + "/" + file_name;
        JcifsFile mf = new JcifsFile(in_file_path, stwa.masterAuth);

        InputStream is = mf.getInputStream();
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        OutputStream os = out_file.getOutputStream();
        BufferedOutputStream ofs = new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[IO_AREA_SIZE];
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_file.setLastModified(mf.getLastModified());
        if (sti.isSyncUseFileCopyByTempName()) {
            if (out_dest.exists()) out_dest.delete();
            out_file.renameTo(out_dest);
        }
//        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_dest.setLastModified(mf.getLastModified());

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    ;

    static public int copyFileExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                 File mf, String to_dir, String file_name)
            throws IOException {
        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        File out_file = new File(to_file_path);
        File t_dir = new File(to_dir);
        if (!t_dir.exists()) t_dir.mkdirs();

        FileInputStream is = new FileInputStream(mf);
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);
        FileOutputStream os = new FileOutputStream(out_file);
        BufferedOutputStream ofs = new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[IO_AREA_SIZE];
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (sti.isSyncUseFileCopyByTempName()) {
            File out_dest = new File(to_file_dest);
            if (out_dest.exists()) out_dest.delete();
            out_file.renameTo(out_dest);
        }

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    ;

    static public int copyFileExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                 File mf, String to_dir, String file_name)
            throws IOException {
        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        File out_file = new File(to_file_path);
        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        SafFile t_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_file_path, false);
        if (t_df == null) {
            String saf_name = "";
            SafFile sf = stwa.gp.safMgr.getSdcardSafFile();
            if (sf != null) saf_name = sf.getName();
            stwa.util.addLogMsg("E", "SAF file not found error. path=" + to_file_path + ", SafFile=" + saf_name +
                    ", sdcard=" + stwa.gp.safMgr.getSdcardDirectory());
            ArrayList<SafFileItem> sl = stwa.gp.safMgr.getSafList();
            for (SafFileItem sfi : sl) {
                stwa.util.addLogMsg("E", "SafFileItem UUID=" + sfi.storageUuid + ", path=" + sfi.storageRootDirectory +
                        ", mount=" + sfi.storageIsMounted + ", sdcard=" + sfi.storageTypeSdcard);
            }
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        FileInputStream is = new FileInputStream(mf);
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);
        OutputStream os = stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri());
        BufferedOutputStream ofs = new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[IO_AREA_SIZE];
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (sti.isSyncUseFileCopyByTempName()) {
            File out_dest = new File(to_file_dest);
            SafFile o_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_file_dest, false);
            if (out_dest.exists()) {
                o_df.delete();
            }
            t_df.renameTo(file_name);
        }

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    ;

    static public int copyFileExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_dir, File mf, String to_dir, String file_name)
            throws IOException, JcifsException {
        if (sti.isSyncUseSmallIoBuffer()) {
            return copyFileExternalToSmbSmallBuffer(stwa, sti, from_dir, mf, to_dir, file_name);
        } else {
            return copyFileExternalToSmbLargeBuffer(stwa, sti, from_dir, mf, to_dir, file_name);
        }
    }

    ;

    static private int copyFileExternalToSmbSmallBuffer(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                        String from_dir, File mf, String to_dir, String file_name)
            throws IOException, JcifsException {
        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        JcifsFile out_file = new JcifsFile(to_file_path, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        FileInputStream is = new FileInputStream(mf);
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);
        OutputStream ofs = out_file.getOutputStream();
//		BufferedOutputStream ofs=new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[1024 * 16];
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_file.setLastModified(mf.lastModified());
        if (sti.isSyncUseFileCopyByTempName()) {
            if (out_dest.exists()) out_dest.delete();
            out_file.renameTo(out_dest);
        }
//        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_dest.setLastModified(mf.lastModified());

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    ;

    static private int copyFileExternalToSmbLargeBuffer(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                        String from_dir, File mf, String to_dir, String file_name)
            throws IOException, JcifsException {
        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        JcifsFile out_file = new JcifsFile(to_file_path, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        FileInputStream is = new FileInputStream(mf);
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);
        OutputStream os = out_file.getOutputStream();
        BufferedOutputStream ofs = new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[IO_AREA_SIZE];
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_file.setLastModified(mf.lastModified());
        if (sti.isSyncUseFileCopyByTempName()) {
            if (out_dest.exists()) out_dest.delete();
            out_file.renameTo(out_dest);
        }
//        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_dest.setLastModified(mf.lastModified());

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    ;

//	private boolean copyFileLocalToLocalByChannel(File iLf, String fromUrl, String toUrl,
//			String title_header) 
//					throws IOException {
//
//		File oLf;
//		long t0 = System.currentTimeMillis();
//		FileInputStream fin = new FileInputStream( iLf );
//		FileChannel inCh = fin.getChannel();
//		FileOutputStream fout = new FileOutputStream(toUrl);
//		FileChannel outCh = fout.getChannel();
//		int n=0;
//		long tot = 0;
//		long fileBytes=iLf.length();
//		String fn=iLf.getName();
//		
//		sendMsgToProgDlg(String.format(title_header+" %s %s%% completed.",fn,0));
//		
//		ByteBuffer mFileChBuffer=ByteBuffer.allocate(1024*1024*1);
//		mFileChBuffer.
//		while (( n = inCh.read( mFileChBuffer )) > 0) {
//		    n=mFileChBuffer.position();
//		    mFileChBuffer.flip();
//		    outCh.write(mFileChBuffer);
//		    tot += n;
//		    if (n<fileBytes) 
//		    	sendMsgToProgDlg(String.format(title_header+" %s %s%% completed.",fn,
//		    					(tot*100)/fileBytes));
//		    mFileChBuffer.clear();
//		}
//		
//		inCh.close();
//		outCh.close();
//		
//		oLf = new File(toUrl);
//		boolean slm=false;
//		if (setLastModified) slm=oLf.setLastModified(iLf.lastModified());
//		long t = System.currentTimeMillis() - t0;
//		if (mGp.settingsMslScan) scanMediaStoreLibraryFile(toUrl);
//		sendLogMsg("I",fromUrl+" was copied to "+toUrl+", "+
//				tot + " bytes transfered in " + 
//				t  + " mili seconds at " + calTransferRate(tot,t));
//		return true;
//	};

    static public int copyFileSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                            JcifsFile mf, String to_dir, String file_name)
            throws IOException, JcifsException {
        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        File out_file = new File(to_file_path);
        File t_dir = new File(to_dir);
        if (!t_dir.exists()) t_dir.mkdirs();

        InputStream is = mf.getInputStream();
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        FileOutputStream os = new FileOutputStream(out_file);
        BufferedOutputStream ofs = new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[IO_AREA_SIZE];
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (sti.isSyncUseFileCopyByTempName()) {
            File out_dest = new File(to_file_dest);
            if (out_dest.exists()) out_dest.delete();
            out_file.renameTo(out_dest);
        }

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    ;

    static public int copyFileSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                            JcifsFile mf, String to_dir, String file_name)
            throws IOException, JcifsException {
        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        SafFile r_saf = stwa.gp.safMgr.getSdcardSafFile();
        SafFile t_df = stwa.gp.safMgr.getSafFileBySdcardPath(r_saf, to_file_path, false);
        stwa.util.addDebugMsg(2, "I", "SafDebugMsg:\n" + stwa.gp.safMgr.getSafDebugMsg());
        stwa.util.addDebugMsg(2, "I", "SafDebugMsg: ended");

        if (t_df == null) {
            String saf_name = "";
            SafFile sf = stwa.gp.safMgr.getSdcardSafFile();
            if (sf != null) saf_name = sf.getName();
            stwa.util.addLogMsg("E", "SAF file not found error. path=" + to_file_path + ", SafFile=" + saf_name +
                    ", sdcard=" + stwa.gp.safMgr.getSdcardDirectory());
            ArrayList<SafFileItem> sl = stwa.gp.safMgr.getSafList();
            for (SafFileItem sfi : sl) {
                stwa.util.addLogMsg("E", "SafFileItem UUID=" + sfi.storageUuid + ", path=" + sfi.storageRootDirectory +
                        ", mount=" + sfi.storageIsMounted + ", sdcard=" + sfi.storageTypeSdcard);
            }
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        InputStream is = mf.getInputStream();
        BufferedInputStream ifs = new BufferedInputStream(is, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        OutputStream os = stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri());
        BufferedOutputStream ofs = new BufferedOutputStream(os, LARGE_BUFFERED_STREAM_BUFFER_SIZE);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        byte[] buffer = new byte[IO_AREA_SIZE];
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
                if (sti.isSyncUseFileCopyByTempName()) t_df.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        if (sti.isSyncUseFileCopyByTempName()) {
            File out_dest = new File(to_file_dest);
            SafFile o_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_file_dest, false);
            if (out_dest.exists()) {
                o_df.delete();
            }
            t_df.renameTo(file_name);
        }

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                file_read_time + " mili seconds at " +
                        SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    ;

}
