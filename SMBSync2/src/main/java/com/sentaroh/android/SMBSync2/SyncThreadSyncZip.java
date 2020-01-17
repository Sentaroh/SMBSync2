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
import android.os.SystemClock;

import com.sentaroh.android.SMBSync2.SyncThread.SyncThreadWorkArea;
import com.sentaroh.android.Utilities.BufferedZipFile;
import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.android.Utilities.StringUtil;
import com.sentaroh.android.Utilities.ZipFileListItem;
import com.sentaroh.android.Utilities.ZipUtil;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.sentaroh.android.SMBSync2.Constants.APP_SPECIFIC_DIRECTORY;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_COPY;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE;
import static com.sentaroh.android.SMBSync2.Constants.SMBSYNC2_CONFIRM_REQUEST_MOVE;

public class SyncThreadSyncZip {

    static private ZipFile setZipEnvironment(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                             String from_path, String dest_path, ZipParameters zp) {
        File lf = new File(dest_path);
        stwa.zipFileNameEncoding = ZipUtil.DEFAULT_ZIP_FILENAME_ENCODING;
        if (lf.exists()) {
//			stwa.zipFileNameEncoding=ZipUtil.getFileNameEncoding(dest_path);
            stwa.zipFileList = ZipUtil.buildZipFileList(dest_path, ZipUtil.DEFAULT_ZIP_FILENAME_ENCODING);
            if (stwa.zipFileList.size() == 0) {
                lf.delete();
//				Log.v("","delete");
            }
        } else {
            stwa.zipFileList = new ArrayList<ZipFileListItem>();
        }

        ZipFile zf = null;
        try {
            zf = new ZipFile(dest_path);
            zf.setFileNameCharset(ZipUtil.DEFAULT_ZIP_FILENAME_ENCODING);
//			stwa.zipFileNameEncoding=ZipUtil.getFileNameEncoding(dest_path);
            zp.setDefaultFolderPath(stwa.gp.internalRootDirectory);
            zp.setRootFolderInZip("");

            if (sti.getTargetZipCompressionLevel().equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_FASTEST))
                zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FASTEST);
            else if (sti.getTargetZipCompressionLevel().equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_FAST))
                zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FAST);
            else if (sti.getTargetZipCompressionLevel().equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_NORMAL))
                zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            else if (sti.getTargetZipCompressionLevel().equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_MAXIMUM))
                zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
            else if (sti.getTargetZipCompressionLevel().equals(SyncTaskItem.ZIP_OPTION_COMP_LEVEL_ULTRA))
                zp.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);

            zp.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

            if (sti.getTargetZipEncryptMethod().equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_STANDARD)) {
                zp.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
                zp.setEncryptFiles(true);
                zp.setPassword(sti.getTargetZipPassword());
            } else if (sti.getTargetZipEncryptMethod().equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_AES128)) {
                zp.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                zp.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_128);
                zp.setEncryptFiles(true);
                zp.setPassword(sti.getTargetZipPassword());
            } else if (sti.getTargetZipEncryptMethod().equals(SyncTaskItem.ZIP_OPTION_ENCRYPT_AES256)) {
                zp.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
                zp.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
                zp.setEncryptFiles(true);
                zp.setPassword(sti.getTargetZipPassword());
            }

        } catch (ZipException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    CommonUtilities.getExecutedMethodName() + " master=" + from_path + ", target=" + dest_path);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
        }
//		Log.v("","zf path="+zf.getFile().getPath());
        return zf;
    }

    private static boolean copyZipFileToWorkDirectory(SyncThreadWorkArea stwa, String from_dir, String file_path) {
        stwa.util.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_dir + file_path);
        File in = new File(from_dir + file_path);
        if (!in.exists()) return true;//No iput file

        File out = new File(stwa.zipWorkFileName);
//		Log.v("","copy to work from="+in.getPath()+", out="+out.getPath());
//        if (!in.exists()) {
//            out.delete();
//            return false;
//        }
        stwa.zipFileCopyBackRequired = false;
        try {
            FileInputStream fis = new FileInputStream(in);
            FileOutputStream fos = new FileOutputStream(out);
            byte[] buff = new byte[1024 * 1024 * 8];
            int rc = fis.read(buff);
            long read_byte=0, tot_byte=in.length();
            while (rc > 0) {
                if (!stwa.gp.syncThreadCtrl.isEnabled()) break;
                fos.write(buff, 0, rc);
                read_byte+=rc;
                SyncThread.showProgressMsg(stwa, stwa.currentSTI.getSyncTaskName(),
                        String.format(stwa.gp.appContext.getString(R.string.msgs_mirror_file_zip_copy_to_work),(read_byte*100/tot_byte)));
                rc = fis.read(buff);
            }
            fis.close();
            fos.flush();
            fos.close();
            if (!stwa.gp.syncThreadCtrl.isEnabled()) out.delete();
            stwa.zipFileCopyBackRequired = false;
            return true;
        } catch (FileNotFoundException e) {
            SyncThread.showMsg(stwa, true, stwa.currentSTI.getSyncTaskName(), "I", "", "",
                    CommonUtilities.getExecutedMethodName() + " from=" + from_dir + file_path);
            SyncThread.showMsg(stwa, true, stwa.currentSTI.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return false;
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, stwa.currentSTI.getSyncTaskName(), "I", "", "",
                    CommonUtilities.getExecutedMethodName() + " from=" + from_dir + file_path);
            SyncThread.showMsg(stwa, true, stwa.currentSTI.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return false;
        }
    }

    private static boolean copyZipFileToDestination(SyncThreadWorkArea stwa, String to_dir, String file_path) {
        boolean result=false;
        if (Build.VERSION.SDK_INT>=24) result=copyZipFileToDestinationMoveMode(stwa, to_dir, file_path);
        else result=copyZipFileToDestinationCopyMode(stwa, to_dir, file_path);
        return result;
    }

    private static boolean copyZipFileToDestinationCopyMode(SyncThreadWorkArea stwa, String to_dir, String file_path) {
        stwa.util.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered, to=" + to_dir + file_path);
        boolean result=false;
        File in = new File(stwa.zipWorkFileName);
        if ((stwa.totalCopyCount > 0 || stwa.totalDeleteCount > 0) || stwa.zipFileCopyBackRequired) {
            SafFile out = stwa.gp.safMgr.createSdcardItem(to_dir + file_path + ".tmp", false);
            SafFile dest = stwa.gp.safMgr.createSdcardItem(to_dir + file_path, false);
            String dest_file_name = file_path.lastIndexOf("/") >= 0 ? file_path.substring(file_path.lastIndexOf("/") + 1) : file_path;
            try {
                long tot_byte=in.length();
                FileInputStream fis = new FileInputStream(in);
                OutputStream fos = stwa.gp.appContext.getContentResolver().openOutputStream(out.getUri());
                byte[] buff = new byte[1024 * 1024 * 8];
                int rc = fis.read(buff);
                long read_byte=0;
                while (rc > 0) {
                    if (!stwa.gp.syncThreadCtrl.isEnabled()) break;
                    read_byte+=rc;
                    fos.write(buff, 0, rc);
                    rc = fis.read(buff);
                    SyncThread.showProgressMsg(stwa, stwa.currentSTI.getSyncTaskName(),
                            String.format(stwa.gp.appContext.getString(R.string.msgs_mirror_file_zip_copy_back_from_work),(read_byte*100/tot_byte)));
                }
                fis.close();
                fos.flush();
                fos.close();
                if (stwa.gp.syncThreadCtrl.isEnabled()) {
                    dest.delete();
                    out.renameTo(dest_file_name);
                } else {
                    out.delete();
                }
                stwa.util.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " copy was completed");
                result=true;
            } catch (FileNotFoundException e) {
                SyncThread.showMsg(stwa, true, stwa.currentSTI.getSyncTaskName(), "I", "", "",
                        CommonUtilities.getExecutedMethodName() + " to=" + to_dir + file_path);
                SyncThread.showMsg(stwa, true, stwa.currentSTI.getSyncTaskName(), "I", "", "", e.getMessage());
                SyncThread.printStackTraceElement(stwa, e.getStackTrace());
                stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            } catch (IOException e) {
                SyncThread.showMsg(stwa, true, stwa.currentSTI.getSyncTaskName(), "I", "", "",
                        CommonUtilities.getExecutedMethodName() + " to=" + to_dir + file_path);
                SyncThread.showMsg(stwa, true, stwa.currentSTI.getSyncTaskName(), "I", "", "", e.getMessage());
                SyncThread.printStackTraceElement(stwa, e.getStackTrace());
                stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            }
        } else {
            stwa.util.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " copy was ignored, because file was not modififed.");
            result=true;
        }
        in.delete();
        return result;
    }

    private static boolean copyZipFileToDestinationMoveMode(SyncThreadWorkArea stwa, String to_dir, String file_path) {
        stwa.util.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " entered, to=" + to_dir + file_path);
        boolean result=false;
        File in = new File(stwa.zipWorkFileName);
        if ((stwa.totalCopyCount > 0 || stwa.totalDeleteCount > 0) || stwa.zipFileCopyBackRequired) {
            SafFile out  = stwa.gp.safMgr.createSdcardItem(stwa.zipWorkFileName, false);
            SafFile dest = stwa.gp.safMgr.createSdcardItem(to_dir + file_path, false);
            if (dest.exists()) dest.delete();
            boolean mv=out.moveTo(dest);
            if (!mv) {
                String emsg="SafFile moveTo() error\n"+out.getLastErrorMessage();
                SyncThread.showMsg(stwa, true, stwa.currentSTI.getSyncTaskName(), "I", "", "",
                        CommonUtilities.getExecutedMethodName() + " to=" + to_dir + file_path);
                SyncThread.showMsg(stwa, true, stwa.currentSTI.getSyncTaskName(), "I", "", "", emsg);
                stwa.gp.syncThreadCtrl.setThreadMessage(emsg);
            } else {
                result=true;
            }
        } else {
            stwa.util.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " copy was ignored, because file was not modififed.");
            result=true;
        }
        in.delete();
        return result;
    }

    static public int syncMirrorInternalToInternalZip(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, String dest_file) {
        int sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
        ZipParameters zp = new ZipParameters();
        ZipFile zf = null;
        if (sti.isTargetZipUseExternalSdcard()) {
            File lf=new File(stwa.gp.safMgr.getSdcardRootPath()+"/"+dest_file);
            File df=new File(lf.getParent());
            if (!df.exists()) {
                SafFile sf=stwa.gp.safMgr.createSdcardDirectory(df.getPath());
            }
        } else {
            File lf=new File(stwa.gp.internalRootDirectory+"/"+dest_file);
            File df=new File(lf.getParent());
            if (!df.exists()) {
                df.mkdirs();
            }
        }
        if (sti.isTargetZipUseExternalSdcard()) {
            File lf=new File(stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache");
            lf.mkdirs();
            stwa.zipWorkFileName=stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/zip_work_file.zip";
            if (!copyZipFileToWorkDirectory(stwa, stwa.gp.safMgr.getSdcardRootPath(), dest_file)) return SyncTaskItem.SYNC_STATUS_ERROR;
            zf = setZipEnvironment(stwa, sti, from_path, stwa.zipWorkFileName, zp);
        } else {
            zf = setZipEnvironment(stwa, sti, from_path, stwa.gp.internalRootDirectory + dest_file, zp);
        }
        if (stwa.gp.syncThreadCtrl.isEnabled() && zf != null) {
            File mf = new File(from_path);
            sync_result = moveCopyInternalToInternalZip(stwa, sti, false, from_path, from_path, mf, zf, zp);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                sync_result = syncDeleteInternalToInternalZip(stwa, sti, from_path, zf, zp);
                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                    if (sti.isTargetZipUseExternalSdcard()) {
                        if (!copyZipFileToDestination(stwa, stwa.gp.safMgr.getSdcardRootPath(), dest_file)) return SyncTaskItem.SYNC_STATUS_ERROR;
                    }
                }
            }

//            if (sti.isSyncOptionDeleteFirstWhenMirror()) {
//                sync_result =syncDeleteInternalToInternalZip(stwa, sti, from_path, zf, zp);
//                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
//                    sync_result =moveCopyInternalToInternalZip(stwa, sti, false, from_path, from_path, mf, zf, zp);
//                    if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
//                        if (sti.isTargetZipUseExternalSdcard()) {
//                            copyZipFileToDestination(stwa, stwa.gp.safMgr.getSdcardRootPath(), dest_file);
//                        }
//                    }
//                }
//            } else {
//                sync_result = moveCopyInternalToInternalZip(stwa, sti, false, from_path, from_path, mf, zf, zp);
//                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
//                    sync_result = syncDeleteInternalToInternalZip(stwa, sti, from_path, zf, zp);
//                    if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
//                        if (sti.isTargetZipUseExternalSdcard()) {
//                            copyZipFileToDestination(stwa, stwa.gp.safMgr.getSdcardRootPath(), dest_file);
//                        }
//                    }
//                }
//            }
        }
        return sync_result;
    }

    static public int syncCopyInternalToInternalZip(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                    String from_path, String dest_file) {
        int sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
        ZipParameters zp = new ZipParameters();
        ZipFile zf = null;
        if (sti.isTargetZipUseExternalSdcard()) {
            File lf=new File(stwa.gp.safMgr.getSdcardRootPath()+"/"+dest_file);
            File df=new File(lf.getParent());
            if (!df.exists()) {
                SafFile sf=stwa.gp.safMgr.createSdcardDirectory(df.getPath());
            }
        } else {
            File lf=new File(stwa.gp.internalRootDirectory+"/"+dest_file);
            File df=new File(lf.getParent());
            if (!df.exists()) {
                df.mkdirs();
            }
        }

        if (sti.isTargetZipUseExternalSdcard()) {
            File lf=new File(stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache");
            lf.mkdirs();
            stwa.zipWorkFileName=stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/zip_work_file.zip";
            if (!copyZipFileToWorkDirectory(stwa, stwa.gp.safMgr.getSdcardRootPath(), dest_file)) return SyncTaskItem.SYNC_STATUS_ERROR;
            zf = setZipEnvironment(stwa, sti, from_path, stwa.zipWorkFileName, zp);
        } else {
            zf = setZipEnvironment(stwa, sti, from_path, stwa.gp.internalRootDirectory + dest_file, zp);
        }
        if (stwa.gp.syncThreadCtrl.isEnabled() && zf != null) {
            File mf = new File(from_path);
            sync_result = moveCopyInternalToInternalZip(stwa, sti, false, from_path, from_path, mf, zf, zp);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                if (sti.isTargetZipUseExternalSdcard()) {
                    if (!copyZipFileToDestination(stwa, stwa.gp.safMgr.getSdcardRootPath(), dest_file)) return SyncTaskItem.SYNC_STATUS_ERROR;
                }
            }
        }
        return sync_result;
    }

    static public int syncMoveInternalToInternalZip(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                    String from_path, String dest_file) {
        int sync_result = SyncTaskItem.SYNC_STATUS_ERROR;
        ZipParameters zp = new ZipParameters();
        ZipFile zf = null;
        if (sti.isTargetZipUseExternalSdcard()) {
            File lf=new File(stwa.gp.safMgr.getSdcardRootPath()+"/"+dest_file);
            File df=new File(lf.getParent());
            if (!df.exists()) {
                SafFile sf=stwa.gp.safMgr.createSdcardDirectory(df.getPath());
            }
        } else {
            File lf=new File(stwa.gp.internalRootDirectory+"/"+dest_file);
            File df=new File(lf.getParent());
            if (!df.exists()) {
                df.mkdirs();
            }
        }

        if (sti.isTargetZipUseExternalSdcard()) {
            File lf=new File(stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache");
            lf.mkdirs();
            stwa.zipWorkFileName=stwa.gp.safMgr.getSdcardRootPath()+"/"+APP_SPECIFIC_DIRECTORY+"/cache/zip_work_file.zip";
            if (!copyZipFileToWorkDirectory(stwa, stwa.gp.safMgr.getSdcardRootPath(), dest_file)) return SyncTaskItem.SYNC_STATUS_ERROR;
            zf = setZipEnvironment(stwa, sti, from_path, stwa.zipWorkFileName, zp);
        } else {
            zf = setZipEnvironment(stwa, sti, from_path, stwa.gp.internalRootDirectory + dest_file, zp);
        }
        if (stwa.gp.syncThreadCtrl.isEnabled() && zf != null) {
            File mf = new File(from_path);
            sync_result = moveCopyInternalToInternalZip(stwa, sti, true, from_path, from_path, mf, zf, zp);
            if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                if (sti.isTargetZipUseExternalSdcard()) {
                    if (!copyZipFileToDestination(stwa, stwa.gp.safMgr.getSdcardRootPath(), dest_file)) return SyncTaskItem.SYNC_STATUS_ERROR;
                }
            }
        }
        return sync_result;
    }

    static private ZipFileListItem getZipFileListItem(SyncThreadWorkArea stwa, String fp) {
        ZipFileListItem zfli = null;
        for (ZipFileListItem item : stwa.zipFileList) {
//			Log.v("","item="+item.getPath()+", fp="+fp);
            if (item.getPath().equals(fp)) {
                zfli = item;
                break;
            }
        }
//		Log.v("","fp="+fp+", result="+zfli);
        return zfli;
    }

    static private boolean isFileChanged(SyncThreadWorkArea stwa, SyncTaskItem sti, String to_path,
                                         File mf, boolean ac) {
        boolean result = false;
        ZipFileListItem zfli = getZipFileListItem(stwa, to_path);
        if (zfli != null) {
            result = isFileChangedDetailCompare(stwa, sti, to_path,
                    true, zfli.getLastModifiedTime(), zfli.getFileLength(),
                    mf.exists(), mf.lastModified(), mf.length(),
                    ac);
        } else {
            result = true;
        }
        return result;
    }

    static final private boolean isFileChangedDetailCompare(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                            String lf_path, boolean lf_exists, long lf_time, long lf_length,
                                                            boolean mf_exists, long mf_time, long mf_length,
                                                            boolean ac) {
        boolean diff = false;
        boolean exists_diff = false;

        long time_diff = Math.abs((mf_time - lf_time));
        long length_diff = Math.abs((mf_length - lf_length));

        if (mf_exists != lf_exists) exists_diff = true;
        if (exists_diff || (sti.isSyncOptionDifferentFileBySize() && length_diff > 0) || ac) {
            diff = true;
        } else {//Check lastModified()
            if (sti.isSyncOptionDifferentFileByTime()) {
                if (time_diff > stwa.syncDifferentFileAllowableTime) { //LastModified was changed
                    diff = true;
                } else diff = false;
            }
        }
        if (stwa.gp.settingDebugLevel >= 3) {
            stwa.util.addDebugMsg(3, "I", "isFileChangedDetailCompare");
            if (mf_exists) stwa.util.addDebugMsg(3, "I", "Master file length=" + mf_length +
                    ", last modified(ms)=" + mf_time +
                    ", date=" + StringUtil.convDateTimeTo_YearMonthDayHourMinSec((mf_time / 1000) * 1000));
            else stwa.util.addDebugMsg(3, "I", "Master file was not exists");
            if (lf_exists) stwa.util.addDebugMsg(3, "I", "Target file length=" + lf_length +
                    ", last modified(ms)=" + lf_time +
                    ", date=" + StringUtil.convDateTimeTo_YearMonthDayHourMinSec((lf_time / 1000) * 1000));
            else stwa.util.addDebugMsg(3, "I", "Target file was not exists");
            stwa.util.addDebugMsg(3, "I", "allcopy=" + ac + ",exists_diff=" + exists_diff +
                    ",time_diff=" + time_diff + ",length_diff=" + length_diff + ", diff=" + diff);
        } else {
            stwa.util.addDebugMsg(1, "I", "isFileChangedDetailCompare(ZIP) fp="+lf_path+ ", exists_diff=" + exists_diff +
                    ", time_diff=" + time_diff + ", length_diff=" + length_diff + ", diff=" + diff+", target_time="+lf_time+", master_time="+mf_time);
        }
        return diff;
    }

    static private boolean createDirectoryToZip(SyncThreadWorkArea stwa, SyncTaskItem sti, String to_dir, ZipFile zf, ZipParameters zp) {
        boolean result = false;
        if (!sti.isSyncTestMode()) {
            File mf = new File(to_dir);
//			Log.v("","from="+to_dir+", base="+zp.getDefaultFolderPath());
            if (zp.getDefaultFolderPath().equals(to_dir)) {
                stwa.util.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " directory not created, Directory=" + to_dir +
                        ", Base=" + zp.getDefaultFolderPath());
            } else {
                try {
                    FileHeader fh = null;
                    boolean error = false;
                    String zip_dir_name = to_dir.replace(zp.getDefaultFolderPath() + "/", "");
                    try {
                        fh = zf.getFileHeader(zip_dir_name + "/");
                    } catch (ZipException e) {
//							e.printStackTrace();
                        error = true;
                    }

                    if (!error) {
                        if (fh == null) {
                            zf.setFileNameCharset(ZipUtil.DEFAULT_ZIP_FILENAME_ENCODING);
                            zf.addFile(mf, zp);
                            stwa.zipFileCopyBackRequired = true;
                            stwa.util.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " directory created, dir=" + to_dir);
                        } else {
//							Log.v("","name="+fh.getFileName());
                            stwa.util.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " directory was already exist, dir=" + to_dir);
                        }
                    } else {
                        zf.getFile().delete();
                        zf.setFileNameCharset(ZipUtil.DEFAULT_ZIP_FILENAME_ENCODING);
                        zf.createZipFile(mf, zp);
                        stwa.util.addDebugMsg(1, "I", CommonUtilities.getExecutedMethodName() + " directory created, dir=" + to_dir);
                    }
                    result=true;
                } catch (ZipException e) {
                    SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                            CommonUtilities.getExecutedMethodName() + " directory=" + to_dir + ", Zip=" + zf.getFile().getPath());
                    SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
                    SyncThread.printStackTraceElement(stwa, e.getStackTrace());
                    stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
                    result=false;
                }
            }
        } else {
            result = true;
        }
        return result;
    }

    static private int moveCopyInternalToInternalZip(SyncThreadWorkArea stwa, SyncTaskItem sti, boolean move_file,
                                                     String from_base, String from_path, File mf, ZipFile zf, ZipParameters zp) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + from_path +
                    ", dest=" + zf.getFile().getPath() + ", move=" + move_file);
        int sync_result = 0;
        try {
            String t_from_path = from_path.substring(from_base.length());
            if (mf.exists()) {
                if (mf.isDirectory()) { // Directory copy
                    if (mf.canRead() && !SyncThread.isHiddenDirectory(stwa, sti, mf) &&
                            SyncThread.isDirectoryToBeProcessed(stwa, t_from_path)) {
                        if (sti.isSyncOptionSyncEmptyDirectory()) {
                            createDirectoryToZip(stwa, sti, from_path, zf, zp);
                        }
                        File[] children = mf.listFiles();
                        if (children != null) {
                            for (File element : children) {
                                if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                    if (!element.getName().equals(".android_secure")) {
//										Log.v("","from="+from_path);
//										Log.v("","to  ="+to_path);
                                        if (element.isFile()) {
                                            sync_result = moveCopyInternalToInternalZip(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                    element, zf, zp);
                                        } else {
                                            if (sti.isSyncOptionSyncSubDirectory()) {
                                                sync_result = moveCopyInternalToInternalZip(stwa, sti, move_file, from_base, from_path + "/" + element.getName(),
                                                        element, zf, zp);
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
                    } else {
                        if (!mf.canRead())
                            stwa.util.addDebugMsg(1, "I", "Directory ignored because can not read, fp=" + from_path + "/" + mf.getName());
                    }
                } else { // file copy
                    if (SyncThread.isDirectorySelectedByFileName(stwa, t_from_path) &&
                            !SyncThread.isHiddenFile(stwa, sti, mf) &&
                            SyncThread.isFileSelected(stwa, sti, t_from_path)) {
                        boolean tf_exists = false;
                        try {
//							Log.v("","t="+t_from_path);
                            FileHeader fh = zf.getFileHeader(t_from_path.substring(1));
                            tf_exists = fh == null ? false : true;
//							Log.v("","fh="+fh);
                        } catch (ZipException e) {
//							e.printStackTrace();
                        }
                        if (tf_exists && !sti.isSyncOverrideCopyMoveFile()) {
                            //Ignore override the file
                            if (move_file)
                                stwa.util.addLogMsg("W", from_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_move_file));
                            else
                                stwa.util.addLogMsg("W", from_path, stwa.gp.appContext.getString(R.string.msgs_mirror_ignore_override_copy_file));
                        } else {
                            if (move_file) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_MOVE, t_from_path)) {
                                    if (isFileChanged(stwa, sti, from_path.replace(stwa.gp.internalRootDirectory + "/", ""), mf, stwa.ALL_COPY)) {
                                        sync_result = copyFileInternalToInternalZip(stwa, sti, from_path.replace("/" + mf.getName(), ""),
                                                mf, t_from_path, mf.getName(), zf, zp);
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            stwa.totalCopyCount++;
                                            SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, mf.getName(),
                                                    stwa.msgs_mirror_task_file_moved);
                                        }
                                    } else {
                                        SyncThread.deleteInternalStorageItem(stwa, false, sti, from_path);
                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, mf.getName(),
                                                stwa.msgs_mirror_task_file_moved);
                                    }
                                } else {
                                    stwa.util.addLogMsg("W", t_from_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_move_cancel));
                                }
                            } else {
                                if (isFileChanged(stwa, sti, from_path.replace(stwa.gp.internalRootDirectory + "/", ""), mf, stwa.ALL_COPY)) {
                                    if (!tf_exists || SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_COPY, t_from_path)) {
                                        sync_result = copyFileInternalToInternalZip(stwa, sti, from_path.replace("/" + mf.getName(), ""),
                                                mf, t_from_path, mf.getName(), zf, zp);
                                        if (sync_result == SyncTaskItem.SYNC_STATUS_SUCCESS) {
                                            String tmsg = tf_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                                            SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", from_path, mf.getName(), tmsg);
                                            stwa.totalCopyCount++;
                                        }
                                    } else {
                                        stwa.util.addLogMsg("W", t_from_path, stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_copy_cancel));
                                    }
                                }
                            }
                            if (!stwa.gp.syncThreadCtrl.isEnabled())
                                sync_result = SyncTaskItem.SYNC_STATUS_CANCEL;
                        }
                    }
                }
            } else {
                stwa.gp.syncThreadCtrl.setThreadMessage(stwa.gp.appContext.getString(R.string.msgs_mirror_task_master_not_found) + "," + from_path);
                SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "E", "", "", stwa.gp.syncThreadCtrl.getThreadMessage());
                return SyncTaskItem.SYNC_STATUS_ERROR;
            }
        } catch (IOException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    CommonUtilities.getExecutedMethodName() + " From=" + from_path);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    }

    static final private int syncDeleteInternalToInternalZip(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path,
                                                             ZipFile zf, ZipParameters zp) {
        int sync_result = 0;
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " master=", from_path, ", target=", zf.getFile().getPath());
        try {
            String root_dir = from_path.replace(stwa.gp.internalRootDirectory + "/", "");
//			Log.v("","root="+root_dir);
            ArrayList<FileHeader> remove_list = new ArrayList<FileHeader>();
            for (ZipFileListItem zfli : stwa.zipFileList) {
                String master_path = "", zf_name = zfli.getPath().replace(root_dir + "/", "");
                if (root_dir.equals("")) master_path = from_path + "/" + zfli.getPath();
                else master_path = from_path + "/" + zfli.getPath().replace(root_dir + "/", "");
//				Log.v("","master="+master_path+", zfname="+zf_name);
                if (!zf_name.equals(root_dir) && !zf_name.equals(root_dir + "/")) {
                    File lf = new File(master_path);
                    if (!lf.exists()) {
                        ArrayList<FileHeader> fhl = (ArrayList<FileHeader>) zf.getFileHeaders();
                        String del = "";
                        if (zfli.isDirectory()) del = "/";
                        for (FileHeader dfh : fhl) {
                            if (dfh.getFileName().equals(zfli.getPath() + del)) {
                                if (SyncThread.sendConfirmRequest(stwa, sti, SMBSYNC2_CONFIRM_REQUEST_DELETE_FILE, dfh.getFileName())) {
                                    remove_list.add(dfh);
//									if (!sti.isSyncTestMode()) zf.removeFile(dfh);
                                    if (zfli.isDirectory())
                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", zfli.getPath(), zfli.getFileName(),
                                                stwa.gp.appContext.getString(R.string.msgs_mirror_task_dir_deleted));
                                    else
                                        SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", zfli.getPath(), zfli.getFileName(),
                                                stwa.gp.appContext.getString(R.string.msgs_mirror_task_file_deleted));
                                    stwa.totalDeleteCount++;
                                    break;
                                } else {
                                    SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", zfli.getPath(), zfli.getFileName(),
                                            stwa.gp.appContext.getString(R.string.msgs_mirror_confirm_delete_cancel));
                                }
                            }
                        }
                    }
                }
            }
            if (remove_list.size() > 0 && !sti.isSyncTestMode()) {
                BufferedZipFile bzf = new BufferedZipFile(zf.getFile().getPath(), stwa.zipFileNameEncoding,
                        stwa.gp.settingDebugLevel > 1);
                bzf.removeItem(remove_list);
                bzf.close();
            }
        } catch (ZipException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    CommonUtilities.getExecutedMethodName() + " master=" + from_path + ", target=" + zf.getFile().getPath());
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (Exception e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    CommonUtilities.getExecutedMethodName() + " master=" + from_path + ", target=" + zf.getFile().getPath());
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }
        return sync_result;
    }

    static private int copyFileInternalToInternalZip(SyncThreadWorkArea stwa,
                       SyncTaskItem sti, String from_dir, File mf, String to_dirx, String dest_path, ZipFile zf, ZipParameters zp) throws IOException {
        int sync_result=0;
        String to_dir = from_dir.replace(stwa.gp.internalRootDirectory + "/", "");
//		Log.v("","copy from="+from_dir+", to="+to_dir);
        long read_begin_time = System.currentTimeMillis();
        long file_read_bytes = mf.length();
        if (sti.isSyncTestMode()) return SyncTaskItem.SYNC_STATUS_SUCCESS;
//		ZipModel zipModel=null;
        FileInputStream is = new FileInputStream(mf);
        BufferedInputStream ifs = new BufferedInputStream(is, SyncThreadCopyFile.LARGE_BUFFERED_STREAM_BUFFER_SIZE);
        try {
            String to_name = to_dir + "/" + dest_path;

            ZipParameters n_zp = (ZipParameters) zp.clone();
            n_zp.setFileNameInZip(to_name);
            if (stwa.gp.settingNoCompressFileType.length() > 0) {
                String[] no_comp_array = stwa.gp.settingNoCompressFileType.split(";");
                if (no_comp_array != null && no_comp_array.length > 0) {
                    for (String no_comp_type : no_comp_array) {
//						Log.v("","item="+item+", path="+add_item.getName().toLowerCase());
                        if (mf.getName().toLowerCase().endsWith("." + no_comp_type)) {
                            n_zp.setCompressionMethod(Zip4jConstants.COMP_STORE);
                            break;
                        }
                    }
                }
            }

            zf.setRunInThread(true);
            zf.addFile(mf, n_zp);

            while (zf.getProgressMonitor().getState() == ProgressMonitor.STATE_BUSY) {
                if (!stwa.gp.syncThreadCtrl.isEnabled()) {
                    zf.getProgressMonitor().cancelAllTasks();
                    while (zf.getProgressMonitor().getState() == ProgressMonitor.STATE_BUSY) {
                        SystemClock.sleep(100);
                    }
                    break;
                } else {
                    SyncThread.showProgressMsg(stwa, sti.getSyncTaskName(), dest_path + " " +
                            String.format(stwa.msgs_mirror_task_file_copying, zf.getProgressMonitor().getPercentDone()));
                    SystemClock.sleep(100);
                }
            }
            zf.setRunInThread(false);
//			fh=zf.getFileHeader(to_name);
//			fh.setLastModFileTime((int)ZipUtil.javaToDosTime(mf.lastModified()));

//			Log.v("","name="+fh.getFileName()+", fh_lastmod="+StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(ZipUtil.dosToJavaTme(fh.getLastModFileTime()))+
//					", file_lastMod="+StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(mf.lastModified()));
            ifs.close();
        } catch (ZipException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    CommonUtilities.getExecutedMethodName() + " master=" + from_dir + ", target=" + to_dir);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        } catch (CloneNotSupportedException e) {
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "",
                    CommonUtilities.getExecutedMethodName() + " master=" + from_dir + ", target=" + to_dir);
            SyncThread.showMsg(stwa, true, sti.getSyncTaskName(), "I", "", "", e.getMessage());
            SyncThread.printStackTraceElement(stwa, e.getStackTrace());
            stwa.gp.syncThreadCtrl.setThreadMessage(e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        long file_read_time = System.currentTimeMillis() - read_begin_time;
        if (stwa.gp.settingDebugLevel >= 1)
            stwa.util.addDebugMsg(1, "I", to_dir + "/" + dest_path + " " + file_read_bytes + " bytes transfered in ",
                    file_read_time + " mili seconds at " +
                            SyncThread.calTransferRate(file_read_bytes, file_read_time));
        stwa.totalTransferByte += file_read_bytes;
        stwa.totalTransferTime += file_read_time;

        return sync_result;
    }

}
