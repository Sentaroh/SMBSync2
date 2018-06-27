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

import com.sentaroh.android.SMBSync2.SyncThread.SyncThreadWorkArea;
import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.jcifs.JcifsException;
import com.sentaroh.jcifs.JcifsFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.sentaroh.android.SMBSync2.Constants.APP_SPECIFIC_DIRECTORY;

public class SyncThreadCopyFile {


    static public int copyFileExternalToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                 File mf, String to_dir, String file_name) throws IOException {
        int sync_result=0;
        if (Build.VERSION.SDK_INT>=24 && stwa.lastModifiedIsFunctional) sync_result= copyFileExternalToExternalSetLastMod(stwa, sti, from_dir, mf, to_dir, file_name);
            else sync_result= copyFileExternalToExternalUnsetLastMod(stwa, sti, from_dir, mf, to_dir, file_name);
            return sync_result;
    }

    static private int copyFileExternalToExternalUnsetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                              File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I",SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";

        File out_file = new File(to_file_temp);
        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        InputStream is =null;
        long m_saf_length=-1;
        SafFile m_saf = getSafFile(stwa, sti, mf.getPath());//stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
        if (m_saf == null) {
            is=new FileInputStream(mf);
        } else {
            m_saf_length=m_saf.length();
            if (m_saf.length()==mf.length()) is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
            else {
                is=new FileInputStream(mf);
                stwa.util.addLogMsg("W",SyncUtil.getExecutedMethodName()+" " +stwa.gp.appContext.getString(R.string.msgs_mirror_file_sdcard_info_not_reflect_media_store));
            }
        }

        OutputStream os =null;
        File t_file=new File(to_file_temp);
        SafFile t_df=null;
        t_df = getSafFile(stwa, sti, to_file_temp);
        if (t_df == null) return SyncTaskItem.SYNC_STATUS_ERROR;
        os = stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri());

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            t_df.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+t_df.lastModified()+", master="+mf.lastModified()+", target_size="+t_df.length()+", master_size="+mf.length()+
                ", m_saf_size="+m_saf.length());

        File out_dest = new File(to_file_dest);
        SafFile o_df = stwa.gp.safMgr.createSdcardItem(to_file_dest, false);
        if (out_dest.exists()) o_df.delete();
        t_df.renameTo(file_name);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static private int copyFileExternalToExternalSetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                            File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I",SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
//        String to_file_temp = stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/files/SMBSync2_temp.tmp";
        String to_file_temp = stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/files/"+file_name;

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        SafFile m_saf = getSafFile(stwa, sti, mf.getPath());//stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
        long m_saf_length=-1;
        InputStream is =null;
        if (m_saf == null) {
            is=new FileInputStream(mf);
//            return SyncTaskItem.SYNC_STATUS_ERROR;
        } else {
            m_saf_length=m_saf.length();
            if (m_saf.length()==mf.length()) is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
            else {
                is=new FileInputStream(mf);
                stwa.util.addLogMsg("W",SyncUtil.getExecutedMethodName()+" " +stwa.gp.appContext.getString(R.string.msgs_mirror_file_sdcard_info_not_reflect_media_store));
            }
        }

        OutputStream os =null;
        File temp_file=new File(to_file_temp);
        SafFile temp_sf=getSafFile(stwa, sti, temp_file.getPath());
        os=stwa.gp.appContext.getContentResolver().openOutputStream(temp_sf.getUri());

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        try {
            if (!sti.isSyncDoNotResetFileLastModified()) temp_file.setLastModified(mf.lastModified());
        } catch(Exception e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.gp.appContext.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+to_file_temp+
                ", target="+temp_sf.lastModified()+", master="+mf.lastModified()+", target_size="+temp_sf.length()+", master_size="+mf.length()+
                ", m_saf_size="+m_saf.length());

        SafFile to_sf=getSafFile(stwa, sti, to_file_dest);
        if (to_sf.exists()) to_sf.delete();
        if (!temp_sf.moveTo(to_sf)) {
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "SafFile moveTo Error="+temp_sf.getMessages());
            if (temp_file.exists()) temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                 File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I",SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_path = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";

        File out_file = new File(to_file_path);
        File t_dir = new File(to_dir);
        if (!t_dir.exists()) t_dir.mkdirs();

        InputStream is =null;
        long m_saf_length=-1;
        SafFile m_saf = getSafFile(stwa, sti, mf.getPath());//stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
        if (m_saf == null) {
            is=new FileInputStream(mf);
        } else {
            m_saf_length=-m_saf.length();
            if (m_saf.length()==mf.length()) is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
            else {
                is=new FileInputStream(mf);
                stwa.util.addLogMsg("W",SyncUtil.getExecutedMethodName()+" " +stwa.gp.appContext.getString(R.string.msgs_mirror_file_sdcard_info_not_reflect_media_store));
            }
        }

        FileOutputStream os = new FileOutputStream(out_file);

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            out_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        if (stwa.lastModifiedIsFunctional) {
            try {
                if (!sti.isSyncDoNotResetFileLastModified()) out_file.setLastModified(mf.lastModified());
            } catch(Exception e) {
                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                        stwa.gp.appContext.getString(R.string.msgs_mirror_file_set_last_modified_failed));
                stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
            }
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+out_file.lastModified()+", master="+mf.lastModified()+", target_size="+out_file.length()+", master_size="+mf.length()+
                ", m_saf_size="+m_saf_length);

        File out_dest = new File(to_file_dest);
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I",SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);

        JcifsFile out_file = new JcifsFile(to_file_temp, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        InputStream is =null;
        long m_saf_length=-1;
        SafFile m_saf = getSafFile(stwa, sti, mf.getPath());//stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
        if (m_saf == null) {
            is=new FileInputStream(mf);
//            return SyncTaskItem.SYNC_STATUS_ERROR;
        } else {
            m_saf_length=m_saf.length();
            if (m_saf.length()==mf.length()) is = stwa.gp.appContext.getContentResolver().openInputStream(m_saf.getUri());
            else {
                is=new FileInputStream(mf);
                stwa.util.addLogMsg("W",SyncUtil.getExecutedMethodName()+" " +stwa.gp.appContext.getString(R.string.msgs_mirror_file_sdcard_info_not_reflect_media_store));
            }
        }

        OutputStream os = out_file.getOutputStream();

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            out_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        try {
            if (!sti.isSyncDoNotResetFileLastModified()) out_file.setLastModified(mf.lastModified());
        } catch(JcifsException e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.gp.appContext.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+out_file.getLastModified()+", master="+mf.lastModified()+", target_size="+out_file.length()+", master_size="+mf.length()+
                ", m_saf_size="+m_saf.length());
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileInternalToInternal(SyncThreadWorkArea stwa,
                                                 SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I",SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        String temp_path = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";

        File temp_file = new File(temp_path);
        File t_dir = new File(to_dir);
        if (!t_dir.exists()) t_dir.mkdirs();

        FileInputStream is = new FileInputStream(mf);
        FileOutputStream os = new FileOutputStream(temp_file);

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        if (stwa.lastModifiedIsFunctional) {
            try {
                if (!sti.isSyncDoNotResetFileLastModified()) temp_file.setLastModified(mf.lastModified());
            } catch(Exception e) {
                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                        stwa.gp.appContext.getString(R.string.msgs_mirror_file_set_last_modified_failed));
                stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
            }
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+temp_file.lastModified()+", master="+mf.lastModified()+", target_size="+temp_file.length()+", master_size="+mf.length());
        File out_dest = new File(to_file_dest);
        if (out_dest.exists()) out_dest.delete();
        temp_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileInternalToExternal(SyncThreadWorkArea stwa,
                                                       SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException {
        int result=0;
        if (Build.VERSION.SDK_INT>=24 && stwa.lastModifiedIsFunctional) result= copyFileInternalToExternalSetLastMod(stwa,sti,from_dir, mf, to_dir, file_name);
        else result= copyFileInternalToExternalUnsetLastMod(stwa,sti,from_dir, mf, to_dir, file_name);
        return result;
    }

    static private int copyFileInternalToExternalUnsetLastMod(SyncThreadWorkArea stwa,
                                                              SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        File tlf = new File(to_dir + "/" + file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        FileInputStream is = new FileInputStream(mf);
        OutputStream os =null;
//        File t_file=new File(to_file_path);
        SafFile t_df=null;
        t_df = getSafFile(stwa, sti, to_file_temp);
        if (t_df == null) return SyncTaskItem.SYNC_STATUS_ERROR;
        os = stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri());

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            t_df.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+t_df.lastModified()+", master="+mf.lastModified()+", target_size="+t_df.length()+", master_size="+mf.length());

        SafFile o_df = stwa.gp.safMgr.createSdcardItem(to_file_dest, false);
        boolean rc=false;
        if (o_df.exists()) rc=o_df.delete();
        t_df.renameTo(file_name);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static private int copyFileInternalToExternalSetLastMod(SyncThreadWorkArea stwa,
                                                            SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException {
        stwa.util.addDebugMsg(2, "I", SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);
        File tlf = new File(to_dir + "/" + file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name;
        String to_file_temp = stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/files/"+file_name;

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        FileInputStream is = new FileInputStream(mf);
        OutputStream os =null;

        File temp_file=new File(to_file_temp);
        SafFile from_sf=getSafFile(stwa, sti, to_file_temp);
        os=stwa.gp.appContext.getContentResolver().openOutputStream(from_sf.getUri());
        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }
        File out_dest = new File(to_file_dest);

        try {
            if (!sti.isSyncDoNotResetFileLastModified()) temp_file.setLastModified(mf.lastModified());
        } catch(Exception e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.gp.appContext.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+to_file_temp+
                ", target="+from_sf.lastModified()+", master="+mf.lastModified()+", target_size="+from_sf.length()+", master_size="+mf.length());
        SafFile to_sf=getSafFile(stwa, sti, to_file_dest);
        if (to_sf.exists()) to_sf.delete();
        if (!from_sf.moveTo(to_sf)) {
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "SafFile moveTo Error="+from_sf.getMessages());
            if (temp_file.exists()) temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static private SafFile getSafFile(SyncThreadWorkArea stwa, SyncTaskItem sti,String fp) {
        SafFile t_df = stwa.gp.safMgr.createSdcardItem(fp, false);
        if (t_df == null) {
            String saf_name = "";
            SafFile sf = stwa.gp.safMgr.getSdcardRootSafFile();
            if (sf != null) saf_name = sf.getName();
            stwa.util.addLogMsg("E", "SAF file not found error. path=" + fp + ", SafFile=" + saf_name +
                    ", sdcard=" + stwa.gp.safMgr.getSdcardRootPath());
            stwa.util.addLogMsg("E", "SafManager msg=="+stwa.gp.safMgr.getMessages() );
            return null;
        }
        return t_df;
    }

    static public int copyFileInternalToSmb(SyncThreadWorkArea stwa,
                             SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I",SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);

        JcifsFile out_file = new JcifsFile(to_file_temp, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        FileInputStream is = new FileInputStream(mf);
        OutputStream os = out_file.getOutputStream();

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            out_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        try {
            if (!sti.isSyncDoNotResetFileLastModified()) out_file.setLastModified(mf.lastModified());
        } catch(JcifsException e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.gp.appContext.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+out_file.getLastModified()+", master="+mf.lastModified()+", target_size="+out_file.length()+", master_size="+mf.length());
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileSmbToSmb(SyncThreadWorkArea stwa,
                                   SyncTaskItem sti, String from_dir, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I",SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);

        JcifsFile out_file = new JcifsFile(to_file_temp, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        String in_file_path = from_dir + "/" + file_name;
        JcifsFile mf = new JcifsFile(in_file_path, stwa.masterAuth);

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), mf.getInputStream(), out_file.getOutputStream());
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            out_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        try {
            if (!sti.isSyncDoNotResetFileLastModified()) out_file.setLastModified(mf.getLastModified());
        } catch(JcifsException e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.gp.appContext.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+out_file.getLastModified()+", master="+mf.getLastModified()+", target_size="+out_file.length()+", master_size="+mf.length());
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                            JcifsFile mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I",SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_temp = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";

        File out_file = new File(to_file_temp);
        File t_dir = new File(to_dir);
        if (!t_dir.exists()) t_dir.mkdirs();

        InputStream is = mf.getInputStream();
        OutputStream os = new FileOutputStream(out_file);

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            out_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        if (stwa.lastModifiedIsFunctional) {
            try {
                if (!sti.isSyncDoNotResetFileLastModified()) out_file.setLastModified(mf.getLastModified());
            } catch(Exception e) {
                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                        stwa.gp.appContext.getString(R.string.msgs_mirror_file_set_last_modified_failed));
                stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
            }
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+out_file.getPath()+
                ", target="+out_file.lastModified()+", master="+mf.getLastModified()+", target_size="+out_file.length()+", master_size="+mf.length());
        File out_dest = new File(to_file_dest);
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileSmbToExternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                            JcifsFile mf, String to_dir, String file_name) throws IOException, JcifsException {
        int sync_result=0;
        if (Build.VERSION.SDK_INT>=24 && stwa.lastModifiedIsFunctional) sync_result= copyFileSmbToExternalSetLastMod(stwa, sti, from_dir, mf, to_dir, file_name);
        else sync_result= copyFileSmbToExternalUnsetLastMod(stwa, sti, from_dir, mf, to_dir, file_name);
        return sync_result;
    }

    static private int copyFileSmbToExternalUnsetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                         JcifsFile mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I",SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/temp.tmp";

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        InputStream is = mf.getInputStream();
        OutputStream os =null;
        SafFile t_df=null;
        t_df = getSafFile(stwa, sti, to_file_temp);
        if (t_df == null) return SyncTaskItem.SYNC_STATUS_ERROR;
        os = stwa.gp.appContext.getContentResolver().openOutputStream(t_df.getUri());

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            t_df.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+t_df.lastModified()+", master="+mf.getLastModified()+", target_size="+t_df.length()+", master_size="+mf.length());
        SafFile o_df = stwa.gp.safMgr.createSdcardItem(to_file_dest, false);
        if (o_df.exists()) o_df.delete();
        t_df.renameTo(file_name);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static private int copyFileSmbToExternalSetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                       JcifsFile mf, String to_dir, String file_name) throws IOException, JcifsException {
        stwa.util.addDebugMsg(2, "I",SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_temp = stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/files/"+file_name;

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        InputStream is = mf.getInputStream();
        OutputStream os =null;
        File temp_file=new File(to_file_temp);
        SafFile from_sf=getSafFile(stwa, sti, temp_file.getPath());
        os = stwa.gp.appContext.getContentResolver().openOutputStream(from_sf.getUri());
        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }
        File out_dest = new File(to_file_dest);
        try {
            if (!sti.isSyncDoNotResetFileLastModified()) temp_file.setLastModified(mf.getLastModified());
        } catch(Exception e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.gp.appContext.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        stwa.util.addDebugMsg(1,"I", SyncUtil.getExecutedMethodName(), " After copy fp="+to_file_temp+
                ", target="+from_sf.lastModified()+", master="+mf.getLastModified()+", target_size="+from_sf.length()+", master_size="+mf.length());
        SafFile to_sf=getSafFile(stwa, sti, to_file_dest);
        if (to_sf.exists()) to_sf.delete();
        if (!from_sf.moveTo(to_sf)) {
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "SafFile moveTo Error="+from_sf.getMessages());
            if (temp_file.exists()) temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    private final static int SHOW_PROGRESS_THRESHOLD_VALUE = 1024 * 1024 * 4;
    private final static int IO_AREA_SIZE = 1024 * 1024;
    public final static int LARGE_BUFFERED_STREAM_BUFFER_SIZE = 1024 * 1024 * 4;

    static public int copyFile(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir, String to_dir,
                                String file_name, long file_size, InputStream ifs, OutputStream ofs) throws IOException {
        stwa.util.addDebugMsg(2, "I",SyncUtil.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        long read_begin_time = System.currentTimeMillis();

        int buffer_size=LARGE_BUFFERED_STREAM_BUFFER_SIZE, io_area_size=IO_AREA_SIZE;
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        if (sti.isSyncUseSmallIoBuffer() && sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
            buffer_size=1024*16-1;
            io_area_size=1024*16-1;
            show_prog=(file_size > 1024*64);
        }

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
            stwa.util.addDebugMsg(1, "I", to_dir+"/"+file_name + " " + file_read_bytes + " bytes transfered in ",file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

}
