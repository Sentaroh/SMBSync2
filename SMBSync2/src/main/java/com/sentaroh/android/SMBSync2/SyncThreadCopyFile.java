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
import com.sentaroh.android.Utilities.SafFileManager;

import static com.sentaroh.android.SMBSync2.Constants.*;

public class SyncThreadCopyFile {

    public final static int SHOW_PROGRESS_THRESHOLD_VALUE = 1024 * 1024 * 4;

    public final static int IO_AREA_SIZE = 1024 * 1024;

    public final static int LARGE_BUFFERED_STREAM_BUFFER_SIZE = 1024 * 1024 * 4;

    static public int copyFileInternalToInternal(SyncThreadWorkArea stwa,
                                                 SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I", "copyFileInternalToInternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
//        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
//        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;
//
//        File out_file = new File(to_file_path);
//        File t_dir = new File(to_dir);
//        if (!t_dir.exists()) t_dir.mkdirs();

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_path = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";

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
//        if (sti.isSyncUseFileCopyByTempName()) {
//            File out_dest = new File(to_file_dest);
//            if (out_dest.exists()) out_dest.delete();
//            out_file.renameTo(out_dest);
//        }
        try {
            out_file.setLastModified(mf.lastModified());
        } catch(Exception e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_dir + ", To=" + to_dir+", name="+file_name);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", "Length="+mf.length()+", LastModified="+mf.lastModified()+
                    ", File="+mf.isFile());
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            if (e.getCause()!=null) SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getCause().toString());

            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            out_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        File out_dest = new File(to_file_dest);
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);


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
                                                 SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I", "copyFileInternalToExternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        long read_begin_time = System.currentTimeMillis();
        File tlf = new File(to_dir + "/" + file_name);

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
            ArrayList<SafFileManager.SafFileItem> sl = stwa.gp.safMgr.getSafList();
            for (SafFileManager.SafFileItem sfi : sl) {
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

    static public int copyFileInternalToSmb(SyncThreadWorkArea stwa,
                             SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I", "copyFileInternalToSmb from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        JcifsFile out_file = new JcifsFile(to_file_path, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        int buffer_size=LARGE_BUFFERED_STREAM_BUFFER_SIZE, io_area_size=IO_AREA_SIZE;
        if (sti.isSyncUseSmallIoBuffer()) {
            buffer_size=1024*16;
            io_area_size=1024*16;
        }

        FileInputStream is = new FileInputStream(mf);
        BufferedInputStream ifs = new BufferedInputStream(is, buffer_size);
        OutputStream os = out_file.getOutputStream();
        BufferedOutputStream ofs = new BufferedOutputStream(os, buffer_size);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        try {
            if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_file.setLastModified(mf.lastModified());
        } catch(JcifsException e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.gp.appContext.getString(R.string.msgs_mirror_smb_folder_file_set_last_modified_failed));
        }
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
                                   SyncTaskItem sti, String from_dir, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I", "copyFileSmbToSmb from_dir=", from_dir, ", to_dir=", to_dir,", name=", file_name);

        int buffer_size=LARGE_BUFFERED_STREAM_BUFFER_SIZE, io_area_size=IO_AREA_SIZE;
        if (sti.isSyncUseSmallIoBuffer()) {
            buffer_size=1024*16-1;
            io_area_size=1024*16-1;
        }

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
        BufferedInputStream ifs = new BufferedInputStream(is, buffer_size);

        OutputStream os = out_file.getOutputStream();
        BufferedOutputStream ofs = new BufferedOutputStream(os, buffer_size);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        try {
            if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_file.setLastModified(mf.getLastModified());
        } catch(JcifsException e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.gp.appContext.getString(R.string.msgs_mirror_smb_folder_file_set_last_modified_failed));
        }
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

    static public int copyFileExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                 File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I", "copyFileExternalToInternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
//        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
//        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;
//
//        File out_file = new File(to_file_path);
//        File t_dir = new File(to_dir);
//        if (!t_dir.exists()) t_dir.mkdirs();

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_path = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";

        File out_file = new File(to_file_path);
        File t_dir = new File(to_dir);
        if (!t_dir.exists()) t_dir.mkdirs();

//        SafFile m_saf = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
//        InputStream is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
        InputStream is =null;
        if (mf.getPath().startsWith(stwa.gp.safMgr.getExternalSdcardPath()+"/"+"Android/data/")) {
            is=new FileInputStream(mf);
        } else {
            SafFile m_saf = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
            is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
        }
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
//        if (sti.isSyncUseFileCopyByTempName()) {
//            File out_dest = new File(to_file_dest);
//            if (out_dest.exists()) out_dest.delete();
//            out_file.renameTo(out_dest);
//        }
        try {
            out_file.setLastModified(mf.lastModified());
        } catch(Exception e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_dir + ", To=" + to_dir+", name="+file_name);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", "Length="+mf.length()+", LastModified="+mf.lastModified()+
                    ", File="+mf.isFile());
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            if (e.getCause()!=null) SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getCause().toString());

            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            out_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        File out_dest = new File(to_file_dest);
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                 File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I", "copyFileExternalToExternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

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
            ArrayList<SafFileManager.SafFileItem> sl = stwa.gp.safMgr.getSafList();
            for (SafFileManager.SafFileItem sfi : sl) {
                stwa.util.addLogMsg("E", "SafFileItem UUID=" + sfi.storageUuid + ", path=" + sfi.storageRootDirectory +
                        ", mount=" + sfi.storageIsMounted + ", sdcard=" + sfi.storageTypeSdcard);
            }
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
//        SafFile m_saf = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
//        InputStream is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
        InputStream is =null;
        if (mf.getPath().startsWith(stwa.gp.safMgr.getExternalSdcardPath()+"/"+"Android/data/")) {
            is=new FileInputStream(mf);
        } else {
            SafFile m_saf = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
            is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
        }

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

    static public int copyFileExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                        String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I", "copyFileExternalToSmb from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        int buffer_size=LARGE_BUFFERED_STREAM_BUFFER_SIZE, io_area_size=IO_AREA_SIZE;
        if (sti.isSyncUseSmallIoBuffer()) {
            buffer_size=1024*16-1;
            io_area_size=1024*16-1;
        }

        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        JcifsFile out_file = new JcifsFile(to_file_path, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        InputStream is =null;
        if (mf.getPath().startsWith(stwa.gp.safMgr.getExternalSdcardPath()+"/"+"Android/data/")) {
            is=new FileInputStream(mf);
        } else {
            SafFile m_saf = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
            is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
        }
        BufferedInputStream ifs = new BufferedInputStream(is, buffer_size);
        OutputStream os = out_file.getOutputStream();
        BufferedOutputStream ofs = new BufferedOutputStream(os, buffer_size);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
        long file_size = mf.length();
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
                if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
                return SyncTaskItem.SYNC_STATUS_CANCEL;
            }
        }
        ifs.close();
        ofs.flush();
        ofs.close();
        try {
            if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_file.setLastModified(mf.lastModified());
        } catch(JcifsException e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.gp.appContext.getString(R.string.msgs_mirror_smb_folder_file_set_last_modified_failed));
        }
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

    static public int copyFileSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                            JcifsFile mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I", "copyFileSmbToInternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
//        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
//        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;
//
//        File out_file = new File(to_file_path);
//        File t_dir = new File(to_dir);
//        if (!t_dir.exists()) t_dir.mkdirs();

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_path = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";

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
//        if (sti.isSyncUseFileCopyByTempName()) {
//            File out_dest = new File(to_file_dest);
//            if (out_dest.exists()) out_dest.delete();
//            out_file.renameTo(out_dest);
//        }

        try {
            out_file.setLastModified(mf.getLastModified());
        } catch(Exception e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    SyncUtil.getExecutedMethodName() + " From=" + from_dir + ", To=" + to_dir+", name="+file_name);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", "Length="+mf.length()+", LastModified="+mf.getLastModified()+
                    ", File="+mf.isFile()+", Attribute="+mf.getAttributes());
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            if (e.getCause()!=null) SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getCause().toString());

            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadControl.setThreadMessage(e.getMessage());
            out_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        File out_dest = new File(to_file_dest);
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        long file_read_time = System.currentTimeMillis() - read_begin_time;

        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_file_dest + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                            JcifsFile mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I", "copyFileSmbToExternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

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
            ArrayList<SafFileManager.SafFileItem> sl = stwa.gp.safMgr.getSafList();
            for (SafFileManager.SafFileItem sfi : sl) {
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

}
