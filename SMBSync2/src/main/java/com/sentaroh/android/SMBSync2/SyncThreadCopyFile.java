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

import android.content.ContentProviderClient;
import android.os.Build;
import android.os.Bundle;

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
import static com.sentaroh.android.SMBSync2.Constants.SYNC_IO_AREA_SIZE;

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
        if (stwa.gp.settingDebugLevel >= 2) stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/"+System.currentTimeMillis();

        File out_file = new File(to_file_temp);
        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        InputStream is =null;
        long m_saf_length=-1;
        if (mf.getPath().startsWith(stwa.gp.safMgr.getSdcardRootPath()+"/"+"Android/data/")) {
            is=new FileInputStream(mf);
        } else {
            SafFile m_saf = SyncThread.createSafFile(stwa, sti, mf.getPath());//stwa.gp.safMgr.SyncThread.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
            if (m_saf == null) {
                is=new FileInputStream(mf);
            } else {
                m_saf_length=m_saf.length();
                if (m_saf.length()==mf.length()) is = stwa.context.getContentResolver().openInputStream(m_saf.getUri());
                else {
                    is=new FileInputStream(mf);
                    stwa.util.addLogMsg("W", CommonUtilities.getExecutedMethodName()+" " +stwa.context.getString(R.string.msgs_mirror_file_sdcard_info_not_reflect_media_store));
                }
            }
        }

        OutputStream os =null;
        File t_file=new File(to_file_temp);
        SafFile t_df=null;
        t_df = SyncThread.createSafFile(stwa, sti, to_file_temp);
        if (t_df == null) return SyncTaskItem.SYNC_STATUS_ERROR;
        os = stwa.context.getContentResolver().openOutputStream(t_df.getUri());

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            t_df.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+t_df.lastModified()+", master="+mf.lastModified()+", target_size="+t_df.length()+", master_size="+mf.length()+", m_saf_size="+m_saf_length);

        File out_dest = new File(to_file_dest);

        SafFile o_df =null;
        if (to_file_dest.startsWith(stwa.gp.safMgr.getSdcardRootPath())) o_df=stwa.gp.safMgr.createSdcardItem(to_file_dest, false);
        else o_df=stwa.gp.safMgr.createUsbItem(to_file_dest, false);

        if (out_dest.exists()) o_df.delete();
        t_df.renameTo(file_name);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static private int copyFileExternalToExternalSetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                            File mf, String to_dir, String file_name) throws IOException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_temp = null, to_file_dir_tmp="";

        if (to_dir.startsWith(stwa.gp.safMgr.getSdcardRootPath())) to_file_dir_tmp = stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/";//+file_name;
        else to_file_dir_tmp = stwa.gp.safMgr.getUsbRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/";//+file_name;
        to_file_temp=to_file_dir_tmp+System.currentTimeMillis();//file_name;

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        InputStream is =null;
        long m_saf_length=-1;
        if (mf.getPath().startsWith(stwa.gp.safMgr.getSdcardRootPath()+"/"+"Android/data/")) {
            is=new FileInputStream(mf);
        } else {
            SafFile m_saf = SyncThread.createSafFile(stwa, sti, mf.getPath());
            if (m_saf == null) {
                is=new FileInputStream(mf);
//            return SyncTaskItem.SYNC_STATUS_ERROR;
            } else {
                m_saf_length=m_saf.length();
                if (m_saf.length()==mf.length()) is = stwa.context.getContentResolver().openInputStream(m_saf.getUri());
                else {
                    is=new FileInputStream(mf);
                    stwa.util.addLogMsg("W", CommonUtilities.getExecutedMethodName()+" " +stwa.context.getString(R.string.msgs_mirror_file_sdcard_info_not_reflect_media_store));
                }
            }
        }

        OutputStream os =null;
        File temp_file=new File(to_file_temp);
        SafFile temp_sf=SyncThread.createSafFile(stwa, sti, temp_file.getPath());
        os=new FileOutputStream(temp_file);//stwa.context.getContentResolver().openOutputStream(temp_sf.getUri());

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        try {
            if (!sti.isSyncDoNotResetFileLastModified()) temp_file.setLastModified(mf.lastModified());
        } catch(Exception e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_temp+
                ", target="+temp_sf.lastModified()+", master="+mf.lastModified()+", target_size="+temp_sf.length()+", master_size="+mf.length()+", m_saf_size="+m_saf_length);

//        SyncThread.deleteTempMediaStoreItem(stwa,temp_file);

        SafFile to_sf=SyncThread.createSafFile(stwa, sti, to_file_dest);
//        if (to_sf.exists()) to_sf.delete();
        deleteSafIfExists(stwa, sti, from_dir, to_dir, to_file_dest, file_name, to_sf);
        if (!temp_sf.moveTo(to_sf)) {
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "SafFile moveTo Error="+temp_sf.getLastErrorMessage());
            if (temp_file.exists()) temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileExternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                 File mf, String to_dir, String file_name) throws IOException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        String to_dir_tmp = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/cache";
        File tmp_dir=new File(to_dir_tmp);
        if (!tmp_dir.exists()) tmp_dir.mkdirs();
        String to_file_path = to_dir_tmp+"/"+"temp_file.tmp";

        File out_file = new File(to_file_path);
        File t_dir = new File(to_dir);
        if (!t_dir.exists()) t_dir.mkdirs();

        InputStream is =null;
        long m_saf_length=-1;
        if (mf.getPath().startsWith(stwa.gp.safMgr.getSdcardRootPath()+"/"+"Android/data/") ||
                mf.getPath().startsWith(stwa.gp.safMgr.getUsbRootPath()+"/"+"Android/data/")) {
            is=new FileInputStream(mf);
        } else {
            SafFile m_saf = SyncThread.createSafFile(stwa, sti, mf.getPath());//stwa.gp.safMgr.getSafFileBySdcardPath(stwa.gp.safMgr.getSdcardSafFile(), mf.getPath(), false);
            if (m_saf == null) {
                is=new FileInputStream(mf);
            } else {
                m_saf_length=-m_saf.length();
                if (m_saf.length()==mf.length()) is = stwa.context.getContentResolver().openInputStream(m_saf.getUri());
                else {
                    is=new FileInputStream(mf);
                    stwa.util.addLogMsg("W", CommonUtilities.getExecutedMethodName()+" " +stwa.context.getString(R.string.msgs_mirror_file_sdcard_info_not_reflect_media_store));
                }
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
                        stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
                stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
            }
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+out_file.lastModified()+", master="+mf.lastModified()+", target_size="+out_file.length()+", master_size="+mf.length()+", m_saf_size="+m_saf_length);

        File out_dest = new File(to_file_dest);
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileExternalToSmb(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        int sync_result=0;
        if (sti.isSyncOptionDoNotUseRenameWhenSmbFileWrite()) {
            sync_result=copyFileExternalToSmbDirectWrite(stwa, sti, from_dir, mf, to_dir, file_name);
        } else {
            sync_result=copyFileExternalToSmbUseTempName(stwa, sti, from_dir, mf, to_dir, file_name);
        }

        return sync_result;
    }

    static public int copyFileExternalToSmbUseTempName(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                            String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/"+file_name+"."+System.currentTimeMillis();//"/temp.tmp";
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);

        JcifsFile out_file = new JcifsFile(to_file_temp, stwa.targetAuth);
        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        InputStream is =null;
        long m_saf_length=-1;
        if (mf.getPath().startsWith(stwa.gp.safMgr.getSdcardRootPath()+"/"+"Android/data/") ||
                mf.getPath().startsWith(stwa.gp.safMgr.getUsbRootPath()+"/"+"Android/data/")) {
            is=new FileInputStream(mf);
        } else {
            SafFile m_saf = SyncThread.createSafFile(stwa, sti, mf.getPath());
            if (m_saf == null) {
                is=new FileInputStream(mf);
            } else {
                m_saf_length=m_saf.length();
                if (m_saf.length()==mf.length()) is = stwa.context.getContentResolver().openInputStream(m_saf.getUri());
                else {
                    is=new FileInputStream(mf);
                    stwa.util.addLogMsg("W", CommonUtilities.getExecutedMethodName()+" " +stwa.context.getString(R.string.msgs_mirror_file_sdcard_info_not_reflect_media_store));
                }
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
                    stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp=",to_file_dest,
                ", target="+out_file.getLastModified(), ", master="+mf.lastModified(), ", target_size="+out_file.length(), ", master_size="+mf.length(), ", m_saf_size="+m_saf_length);
//        stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " temp="+out_file.getPath());
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileExternalToSmbDirectWrite(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                       String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);

        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        InputStream is =null;
        long m_saf_length=-1;
        if (mf.getPath().startsWith(stwa.gp.safMgr.getSdcardRootPath()+"/"+"Android/data/") ||
                mf.getPath().startsWith(stwa.gp.safMgr.getUsbRootPath()+"/"+"Android/data/")) {
            is=new FileInputStream(mf);
        } else {
            SafFile m_saf = SyncThread.createSafFile(stwa, sti, mf.getPath());
            if (m_saf == null) {
                is=new FileInputStream(mf);
            } else {
                m_saf_length=m_saf.length();
                if (m_saf.length()==mf.length()) is = stwa.context.getContentResolver().openInputStream(m_saf.getUri());
                else {
                    is=new FileInputStream(mf);
                    stwa.util.addLogMsg("W", CommonUtilities.getExecutedMethodName()+" " +stwa.context.getString(R.string.msgs_mirror_file_sdcard_info_not_reflect_media_store));
                }
            }
        }

        OutputStream os = out_dest.getOutputStream();

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            out_dest.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        try {
            if (!sti.isSyncDoNotResetFileLastModified()) out_dest.setLastModified(mf.lastModified());
        } catch(JcifsException e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp=", to_file_dest,
                ", target="+out_dest.getLastModified(), ", master="+mf.lastModified(), ", target_size="+out_dest.length(), ", master_size="+mf.length(), ", m_saf_size="+m_saf_length);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileInternalToInternal(SyncThreadWorkArea stwa,
                                                 SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;

        String to_dir_tmp = "";
        if (Build.VERSION.SDK_INT>=30) {
            to_dir_tmp=getTempFileDirectory(to_dir);
        } else {
            to_dir_tmp = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/cache";
        }

        File tmp_dir=new File(to_dir_tmp);
        if (!tmp_dir.exists()) tmp_dir.mkdirs();
        String temp_path = to_dir_tmp+"/"+"temp_file.tmp";

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
                        stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
                stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
            }
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_dest+
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
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        File tlf = new File(to_dir + "/" + file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/"+System.currentTimeMillis();

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        FileInputStream is = new FileInputStream(mf);
        OutputStream os =null;
//        File t_file=new File(to_file_path);
        SafFile t_df=null;
        t_df = SyncThread.createSafFile(stwa, sti, to_file_temp);
        if (t_df == null) return SyncTaskItem.SYNC_STATUS_ERROR;
        os = stwa.context.getContentResolver().openOutputStream(t_df.getUri());

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            t_df.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+t_df.lastModified()+", master="+mf.lastModified()+", target_size="+t_df.length()+", master_size="+mf.length());

        SafFile o_df =null;
        if (to_file_dest.startsWith(stwa.gp.safMgr.getSdcardRootPath())) o_df=stwa.gp.safMgr.createSdcardItem(to_file_dest, false);
        else o_df=stwa.gp.safMgr.createUsbItem(to_file_dest, false);

        boolean rc=false;
        if (o_df.exists()) rc=o_df.delete();
        t_df.renameTo(file_name);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public String getTempFileDirectory(String to_dir) {
        String result="";
        if (to_dir.startsWith("/storage/emulated/0")) {
            result="/storage/emulated/0/"+APP_SPECIFIC_DIRECTORY+"/cache";
        } else {
            String[] dir_parts=to_dir.split("/");
            result="/"+dir_parts[1]+"/"+dir_parts[2]+"/"+APP_SPECIFIC_DIRECTORY+"/cache";
        }
        return result;
    }

    static private int copyFileInternalToExternalSetLastMod(SyncThreadWorkArea stwa,
                                                            SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException {
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);


        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_temp = null, to_file_dir_tmp="";

        if (to_dir.startsWith(stwa.gp.safMgr.getSdcardRootPath())) to_file_dir_tmp = stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/";//+file_name;
        else to_file_dir_tmp = stwa.gp.safMgr.getUsbRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/";//+file_name;
        to_file_temp=to_file_dir_tmp+System.currentTimeMillis();//file_name;

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        FileInputStream is = new FileInputStream(mf);
        OutputStream os =null;

        File temp_file=new File(to_file_temp);
        SafFile from_sf=SyncThread.createSafFile(stwa, sti, to_file_temp);
        os=new FileOutputStream(temp_file);//stwa.context.getContentResolver().openOutputStream(from_sf.getUri());
        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        try {
            if (!sti.isSyncDoNotResetFileLastModified()) temp_file.setLastModified(mf.lastModified());
        } catch(Exception e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_temp+
                ", target="+from_sf.lastModified()+", master="+mf.lastModified()+", target_size="+from_sf.length()+", master_size="+mf.length());

//        SyncThread.deleteTempMediaStoreItem(stwa,temp_file);

        SafFile to_sf=SyncThread.createSafFile(stwa, sti, to_file_dest);
//        if (to_sf.exists()) to_sf.delete();
        deleteSafIfExists(stwa, sti, from_dir, to_dir, to_file_dest, file_name, to_sf);
        if (!from_sf.moveTo(to_sf)) {
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "SafFile moveTo Error="+from_sf.getLastErrorMessage());
            if (temp_file.exists()) temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileInternalToSmb(SyncThreadWorkArea stwa,
                             SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        int sync_result=0;
        if (sti.isSyncOptionDoNotUseRenameWhenSmbFileWrite()) {
            sync_result=copyFileInternalToSmbDirectWrite(stwa, sti, from_dir, mf, to_dir, file_name);
        } else {
            sync_result=copyFileInternalToSmbUseTempName(stwa, sti, from_dir, mf, to_dir, file_name);
        }

        return sync_result;
    }

    static public int copyFileInternalToSmbUseTempName(SyncThreadWorkArea stwa,
                                            SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/"+file_name+"."+System.currentTimeMillis();//"/temp.tmp";

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
                    stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+out_file.getLastModified()+", master="+mf.lastModified()+", target_size="+out_file.length()+", master_size="+mf.length());
//        stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " temp="+out_file.getPath());

        if (out_dest.exists()) out_dest.delete();
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " Rename issued. From="+out_file.getPath()+", To="+out_dest.getPath());

        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileInternalToSmbDirectWrite(SyncThreadWorkArea stwa,
                                                       SyncTaskItem sti, String from_dir, File mf, String to_dir, String file_name) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;

        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);

        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        FileInputStream is = new FileInputStream(mf);
        OutputStream os = out_dest.getOutputStream();

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            out_dest.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        try {
            if (!sti.isSyncDoNotResetFileLastModified()) out_dest.setLastModified(mf.lastModified());
        } catch(JcifsException e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+out_dest.getLastModified()+", master="+mf.lastModified()+", target_size="+out_dest.length()+", master_size="+mf.length());

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileSmbToSmb(SyncThreadWorkArea stwa,
                                       SyncTaskItem sti, String from_dir, String to_dir, String file_name) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        int sync_result=0;
        if (sti.isSyncOptionDoNotUseRenameWhenSmbFileWrite()) {
            sync_result=copyFileSmbToSmbDirectWrite(stwa, sti, from_dir, to_dir, file_name);
        } else {
            sync_result=copyFileSmbToSmbUseTempName(stwa, sti, from_dir, to_dir, file_name);
        }

        return sync_result;
    }

    static public int copyFileSmbToSmbUseTempName(SyncThreadWorkArea stwa,
                                                  SyncTaskItem sti, String from_dir, String to_dir, String file_name) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/"+file_name+"."+System.currentTimeMillis();//"/temp.tmp";
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
                    stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+out_file.getLastModified()+", master="+mf.getLastModified()+", target_size="+out_file.length()+", master_size="+mf.length());
//        stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " temp="+out_file.getPath());
        if (out_dest.exists()) out_dest.delete();
        out_file.renameTo(out_dest);

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileSmbToSmbDirectWrite(SyncThreadWorkArea stwa,
                                                  SyncTaskItem sti, String from_dir, String to_dir, String file_name) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name;
        JcifsFile out_dest = new JcifsFile(to_file_dest, stwa.targetAuth);

        SyncThread.createDirectoryToSmb(stwa, sti, to_dir, stwa.targetAuth);

        String in_file_path = from_dir + "/" + file_name;
        JcifsFile mf = new JcifsFile(in_file_path, stwa.masterAuth);

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), mf.getInputStream(), out_dest.getOutputStream());
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            out_dest.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }

        try {
            if (!sti.isSyncDoNotResetFileLastModified()) out_dest.setLastModified(mf.getLastModified());
        } catch(JcifsException e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+out_dest.getLastModified()+", master="+mf.getLastModified()+", target_size="+out_dest.length()+", master_size="+mf.length());

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static public int copyFileSmbToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                            JcifsFile mf, String to_dir, String file_name) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        long read_begin_time = System.currentTimeMillis();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;

        String to_dir_tmp = "";
        if (Build.VERSION.SDK_INT>=30) {
            to_dir_tmp=getTempFileDirectory(to_dir);
        } else {
            to_dir_tmp = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/cache";
        }

        File tmp_dir=new File(to_dir_tmp);
        if (!tmp_dir.exists()) tmp_dir.mkdirs();
        String to_file_temp = to_dir_tmp+"/"+"temp_file.tmp";

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
                        stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
                stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
            }
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+out_file.getPath()+
                ", target="+out_file.lastModified()+", master="+mf.getLastModified()+", target_size="+out_file.length()+", master_size="+mf.length());

        File out_dest = new File(to_file_dest);
        if (out_dest.exists()) out_dest.delete();
        boolean rc=out_file.renameTo(out_dest);
//        stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " rename result="+rc+", dest path="+file_name);
//        stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " rename result="+rc+", dest path lenth="+file_name.length()+", temp path length="+out_file.getPath().length());
//        stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " rename result="+rc+", dest path lenth="+file_name.getBytes().length+", temp path length="+out_file.getPath().getBytes().length);
        if (!rc) {
            stwa.util.addLogMsg("E", sti.getSyncTaskName(), " ", "rename error detected, from="+out_file.getPath()+", to="+out_dest.getPath());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

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
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
        String to_file_dest = to_dir + "/" + file_name, to_file_temp = to_dir + "/"+System.currentTimeMillis();

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        InputStream is = mf.getInputStream();
        OutputStream os =null;
        SafFile t_df = SyncThread.createSafFile(stwa, sti, to_file_temp);
        if (t_df == null) return SyncTaskItem.SYNC_STATUS_ERROR;
        os = stwa.context.getContentResolver().openOutputStream(t_df.getUri());

        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            t_df.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_dest+
                ", target="+t_df.lastModified()+", master="+mf.getLastModified()+", target_size="+t_df.length()+", master_size="+mf.length());

        SafFile o_df =null;
        if (to_file_dest.startsWith(stwa.gp.safMgr.getSdcardRootPath())) o_df=stwa.gp.safMgr.createSdcardItem(to_file_dest, false);
        else o_df=stwa.gp.safMgr.createUsbItem(to_file_dest, false);

        if (o_df.exists()) o_df.delete();
        boolean rc=t_df.renameTo(file_name);
        if (!rc) {
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "SafFile renameTo Error="+t_df.getLastErrorMessage());
            if (t_df.exists()) t_df.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static private int copyFileSmbToExternalSetLastMod(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir,
                                                       JcifsFile mf, String to_dir, String file_name) throws IOException, JcifsException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;

        String to_file_dest = to_dir + "/" + file_name;
        String to_file_temp = null, to_file_dir_tmp="";

        if (to_dir.startsWith(stwa.gp.safMgr.getSdcardRootPath())) to_file_dir_tmp = stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/";//+file_name;
        else to_file_dir_tmp = stwa.gp.safMgr.getUsbRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/";//+file_name;
        to_file_temp=to_file_dir_tmp+System.currentTimeMillis();//file_name;

        SyncThread.createDirectoryToExternalStorage(stwa, sti, to_dir);

        InputStream is = mf.getInputStream();
        OutputStream os =null;
        File temp_file=new File(to_file_temp);
        SafFile from_sf=SyncThread.createSafFile(stwa, sti, temp_file.getPath());
        os =new FileOutputStream(temp_file);//stwa.context.getContentResolver().openOutputStream(from_sf.getUri());
        int result=copyFile(stwa, sti, from_dir, to_dir, file_name, mf.length(), is, os);
        if (result==SyncTaskItem.SYNC_STATUS_CANCEL) {
            temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_CANCEL;
        }
        try {
            if (!sti.isSyncDoNotResetFileLastModified()) temp_file.setLastModified(mf.getLastModified());
        } catch(Exception e) {
            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "W", to_file_dest, mf.getName(),
                    stwa.context.getString(R.string.msgs_mirror_file_set_last_modified_failed));
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "Error="+e.getMessage());
        }
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1,"I", CommonUtilities.getExecutedMethodName(), " After copy fp="+to_file_temp+
                ", target="+from_sf.lastModified()+", master="+mf.getLastModified()+", target_size="+from_sf.length()+", master_size="+mf.length());

//        SyncThread.deleteTempMediaStoreItem(stwa,temp_file);

        SafFile to_sf=SyncThread.createSafFile(stwa, sti, to_file_dest);
        deleteSafIfExists(stwa, sti, from_dir, to_dir, to_file_dest, file_name, to_sf);
        if (!from_sf.moveTo(to_sf)) {
            stwa.util.addLogMsg("W", sti.getSyncTaskName(), " ", "SafFile moveTo Error="+from_sf.getLastErrorMessage());
            if (temp_file.exists()) temp_file.delete();
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return SyncTaskItem.SYNC_STATUS_SUCCESS;
    }

    static private void deleteSafIfExists(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir, String to_dir, String to_file_dest, String file_name, SafFile to_sf) {
        if (to_sf.exists()) {
            try {
                final String EXTRA_URI = "uri";
                final String METHOD_DELETE_DOCUMENT = "android:deleteDocument";
                ContentProviderClient client=stwa.context.getContentResolver().acquireContentProviderClient(to_sf.getUri().getAuthority());
                final Bundle in = new Bundle();
                in.putParcelable(EXTRA_URI, to_sf.getUri());
                client.call(METHOD_DELETE_DOCUMENT, null, in);
            } catch (Exception e) {
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                        CommonUtilities.getExecutedMethodName() + " From=" + from_dir+"/"+file_name + ", To=" + to_file_dest);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
                if (e.getCause()!=null) SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getCause().toString());

                SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            }
        }
    }

    private final static int SHOW_PROGRESS_THRESHOLD_VALUE = 1024 * 1024 * 4;

    static public int copyFile(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_dir, String to_dir,
                                String file_name, long file_size, InputStream ifs, OutputStream ofs) throws IOException {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName()+" from_dir=", from_dir, ", to_dir=", to_dir, ", name=", file_name);

        long read_begin_time = System.currentTimeMillis();

        int io_area_size= SYNC_IO_AREA_SIZE;
        boolean show_prog = (file_size > SHOW_PROGRESS_THRESHOLD_VALUE);
        if (sti.isSyncOptionUseSmallIoBuffer() && sti.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_SMB)) {
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
