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


    static public int copyFileExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                 File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I", "copyFileExternalToExternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        File out_file = new File(to_file_path);
        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        InputStream is =null;
        if (mf.getPath().startsWith(stwa.gp.safMgr.getExternalSdcardPath()+"/"+"Android/data/")) {
            is=new FileInputStream(mf);
        } else {
            SafFile m_saf = getSafFile(stwa, sti, mf.getPath());//stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
            if (m_saf == null) return SyncTaskItem.SYNC_STATUS_ERROR;
            is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
        }

        OutputStream os =null;
        boolean output_use_file=false;
        File t_file=new File(to_file_path);
        SafFile t_df=null;
        if (to_file_path.startsWith(stwa.gp.safMgr.getExternalSdcardPath()+"/"+"Android/data/")) {
            os=new FileOutputStream(t_file);
            output_use_file=true;
        } else {
            t_df = getSafFile(stwa, sti, to_file_path);
            if (t_df == null) return SyncTaskItem.SYNC_STATUS_ERROR;
            os = stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri());
        }

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            if (sti.isSyncUseFileCopyByTempName()) {
                if (output_use_file) t_file.delete();
                else t_df.delete();
            }
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        if (sti.isSyncUseFileCopyByTempName()) {
            File out_dest = new File(to_file_dest);
            if (output_use_file) {
                t_file.setLastModified(mf.lastModified());
                if (out_dest.exists()) out_dest.delete();
                t_file.renameTo(out_dest);
            } else {
                SafFile o_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_file_dest, false);
                if (out_dest.exists()) {
                    o_df.delete();
                }
                t_df.renameTo(file_name);
            }
        }

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                 File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I", "copyFileExternalToInternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_path = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";

        File out_file = new File(to_file_path);
        File t_dir = new File(to_dir);
        if (!t_dir.exists()) t_dir.mkdirs();

        InputStream is =null;
        if (mf.getPath().startsWith(stwa.gp.safMgr.getExternalSdcardPath()+"/"+"Android/data/")) {
            is=new FileInputStream(mf);
        } else {
            SafFile m_saf = getSafFile(stwa, sti, mf.getPath());//stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
            if (m_saf == null) return SyncTaskItem.SYNC_STATUS_ERROR;
            is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
        }
        FileOutputStream os = new FileOutputStream(out_file);

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

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
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            out_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        File out_dest = new File(to_file_dest);
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I", "copyFileExternalToSmb from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        JcifsFile out_file = new JcifsFile(to_file_path, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        InputStream is =null;
        if (mf.getPath().startsWith(stwa.gp.safMgr.getExternalSdcardPath()+"/"+"Android/data/")) {
            is=new FileInputStream(mf);
        } else {
            SafFile m_saf = getSafFile(stwa, sti, mf.getPath());//stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
            if (m_saf == null) return SyncTaskItem.SYNC_STATUS_ERROR;
            is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
        }
        OutputStream os = out_file.getOutputStream();

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

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

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileInternalToInternal(SyncThreadWorkArea stwa,
                                                 SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I", "copyFileInternalToInternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_path = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";

        File out_file = new File(to_file_path);
        File t_dir = new File(to_dir);
        if (!t_dir.exists()) t_dir.mkdirs();

        FileInputStream is = new FileInputStream(mf);
        FileOutputStream os = new FileOutputStream(out_file);

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

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
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            out_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        File out_dest = new File(to_file_dest);
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileInternalToExternal(SyncThreadWorkArea stwa,
                                                 SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I", "copyFileInternalToExternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        File tlf = new File(to_dir + "/" + file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        FileInputStream is = new FileInputStream(mf);
        OutputStream os =null;
        boolean output_use_file=false;
        File t_file=new File(to_file_path);
        SafFile t_df=null;
        if (to_file_path.startsWith(stwa.gp.safMgr.getExternalSdcardPath()+"/"+"Android/data/")) {
            os=new FileOutputStream(t_file);
            output_use_file=true;
        } else {
            t_df = getSafFile(stwa, sti, to_file_path);
            if (t_df == null) return SyncTaskItem.SYNC_STATUS_ERROR;
            os = stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri());
        }

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            if (sti.isSyncUseFileCopyByTempName()) {
                if (output_use_file) t_file.delete();
                else t_df.delete();
            }
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        if (sti.isSyncUseFileCopyByTempName()) {
            File out_dest = new File(to_file_dest);
            if (output_use_file) {
                t_file.setLastModified(mf.lastModified());
                if (out_dest.exists()) out_dest.delete();
                t_file.renameTo(out_dest);
            } else {
                SafFile o_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_file_dest, false);
                if (out_dest.exists()) {
                    o_df.delete();
                }
                t_df.renameTo(file_name);
            }
        }

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static private SafFile getSafFile(SyncThreadWorkArea stwa, SyncTaskItem sti,String fp) {
        SafFile t_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), fp, false);
        if (t_df == null) {
            String saf_name = "";
            SafFile sf = stwa.gp.safMgr.getSdcardSafFile();
            if (sf != null) saf_name = sf.getName();
            stwa.util.addLogMsg("E", "SAF file not found error. path=" + fp + ", SafFile=" + saf_name +
                    ", sdcard=" + stwa.gp.safMgr.getSdcardDirectory());
            ArrayList<SafFileManager.SafFileItem> sl = stwa.gp.safMgr.getSafList();
            for (SafFileManager.SafFileItem sfi : sl) {
                stwa.util.addLogMsg("E", "SafFileItem UUID=" + sfi.storageUuid + ", path=" + sfi.storageRootDirectory +
                        ", mount=" + sfi.storageIsMounted + ", sdcard=" + sfi.storageTypeSdcard);
            }
            return null;
        }
        return t_df;
    }

    static public int copyFileInternalToSmb(SyncThreadWorkArea stwa,
                             SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I", "copyFileInternalToSmb from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        JcifsFile out_file = new JcifsFile(to_file_path, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        FileInputStream is = new FileInputStream(mf);
        OutputStream os = out_file.getOutputStream();

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

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

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileSmbToSmb(SyncThreadWorkArea stwa,
                                   SyncTaskItem sti, String from_dir, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I", "copyFileSmbToSmb from_dir=", from_dir, ", to_dir=", to_dir,", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        JcifsFile out_file = new JcifsFile(to_file_path, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        String in_file_path = from_dir + "/" + file_name;
        JcifsFile mf = new JcifsFile(in_file_path, stwa.masterAuth);

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), mf.getInputStream(), out_file.getOutputStream());
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        try {
            if (!sti.isSyncDoNotResetLastModifiedSmbFile())
                out_file.setLastModified(mf.getLastModified());
        } catch(JcifsException e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.gp.appContext.getString(R.string.msgs_mirror_smb_folder_file_set_last_modified_failed));
        }
        if (sti.isSyncUseFileCopyByTempName()) {
            if (out_dest.exists()) out_dest.delete();
            out_file.renameTo(out_dest);
        }
//        if (!sti.isSyncDoNotResetLastModifiedSmbFile()) out_dest.setLastModified(mf.getLastModified());


        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                            JcifsFile mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I", "copyFileSmbToInternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_path = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";

        File out_file = new File(to_file_path);
        File t_dir = new File(to_dir);
        if (!t_dir.exists()) t_dir.mkdirs();

        InputStream is = mf.getInputStream();
        OutputStream os = new FileOutputStream(out_file);

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            if (sti.isSyncUseFileCopyByTempName()) out_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

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
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            out_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        File out_dest = new File(to_file_dest);
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                            JcifsFile mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I", "copyFileSmbToExternal from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        String to_file_path = (sti.isSyncUseFileCopyByTempName()) ? to_file_temp : to_file_dest;

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        InputStream is = mf.getInputStream();
        OutputStream os =null;
        boolean output_use_file=false;
        File t_file=new File(to_file_path);
        SafFile t_df=null;
        if (to_file_path.startsWith(stwa.gp.safMgr.getExternalSdcardPath()+"/"+"Android/data/")) {
            os=new FileOutputStream(t_file);
            output_use_file=true;
        } else {
            t_df = getSafFile(stwa, sti, to_file_path);
            if (t_df == null) return SyncTaskItem.SYNC_STATUS_ERROR;
            os = stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri());
        }

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            if (sti.isSyncUseFileCopyByTempName()) {
                if (output_use_file) t_file.delete();
                else t_df.delete();
            }
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        if (sti.isSyncUseFileCopyByTempName()) {
            File out_dest = new File(to_file_dest);
            if (output_use_file) {
                t_file.setLastModified(mf.getLastModified());
                if (out_dest.exists()) out_dest.delete();
                t_file.renameTo(out_dest);
            } else {
                SafFile o_df = stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), to_file_dest, false);
                if (out_dest.exists()) {
                    o_df.delete();
                }
                t_df.renameTo(file_name);
            }
        }

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    private final static int SHOW_PROGRESS_THRESHOLD_VALUE = 1024 * 1024 * 4;
    private final static int IO_AREA_SIZE = 1024 * 1024;
    public final static int LARGE_BUFFERED_STREAM_BUFFER_SIZE = 1024 * 1024 * 4;

    static private int copyFile(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir, String to_dir,
                                String file_name, long file_size, InputStream is, OutputStream os) throws IOException {

        long read_begin_time = System.currentTimeMillis();

        int buffer_size=LARGE_BUFFERED_STREAM_BUFFER_SIZE, io_area_size=IO_AREA_SIZE;
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        if (sti.isSyncUseSmallIoBuffer() && sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            buffer_size=1024*16-1;
            io_area_size=1024*16-1;
            show_prog=(file_size > 1024*64);
        }

        BufferedInputStream ifs = new BufferedInputStream(is, buffer_size);
        BufferedOutputStream ofs = new BufferedOutputStream(os, buffer_size);

        int buffer_read_bytes = 0;
        long file_read_bytes = 0;
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
            stwa.util.addDebugMsg(1, "I", to_dir+"/"+file_name + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

}
