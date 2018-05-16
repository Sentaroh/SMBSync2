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

import com.sentaroh.android.Utilities.NotifyEvent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.sentaroh.android.SMBSync2.Constants.*;

public class FileLastModifiedTime {

//	FileLastModifiedTime(GlobalParameters gp, Context c, AdapterSyncTask pa, SyncUtil ut, CommonDialog cd) {
//	};


    final public static boolean isLastModifiedWasUsed(GlobalParameters gp, AdapterSyncTask profile_adapter) {
        boolean usable = false;
        for (int i = 0; i < profile_adapter.getCount(); i++) {
            SyncTaskItem syncprof_item = profile_adapter.getItem(i);
            if (syncprof_item.getTargetFolderType().equals(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL)) {
                String lmp = gp.internalRootDirectory;
                if (!syncprof_item.getTargetDirectoryName().equals(""))
                    lmp += "/" + syncprof_item.getTargetDirectoryName();
//				Log.v("","lmp="+lmp);
                if (!lmp.equals("/") && !lmp.equals("")) {
                    if (!isSetLastModifiedFunctional(lmp) ||
                            syncprof_item.isSyncDetectLastModifiedBySmbsync()) {
                        usable = true;
                        break;
                    }
                }
            }
        }
        return usable;
    }

    final public static FileLastModifiedTimeEntry getLastModifiedLisItemByFilePath(
            ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
            ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
            String fp) {
        FileLastModifiedTimeEntry lmli = null;
        int idx = Collections.binarySearch(curr_last_modified_list,
                new FileLastModifiedTimeEntry(fp, 0, 0, false),
                new Comparator<FileLastModifiedTimeEntry>() {
                    @Override
                    public int compare(FileLastModifiedTimeEntry ci,
                                       FileLastModifiedTimeEntry ni) {
                        return ci.getFilePath().compareToIgnoreCase(ni.getFilePath());
                    }
                });
        if (idx >= 0) {
            lmli = curr_last_modified_list.get(idx);
            return lmli;
        }

        for (FileLastModifiedTimeEntry item : new_last_modified_list) {
            if (item.getFilePath().equals(fp)) {
                return item;
            }
        }
        return null;
    }

    final public static boolean isCurrentListWasDifferent(
            ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
            ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
            String fp, long l_lm, long r_lm, int timeDifferenceLimit) {
        boolean result = false;
//		Log.v("","size c="+curr_last_modified_list.size()+", a="+new_last_modified_list.size());
        if (curr_last_modified_list.size() != 0) {
            int idx = Collections.binarySearch(curr_last_modified_list,
                    new FileLastModifiedTimeEntry(fp, 0, 0, false),
                    new Comparator<FileLastModifiedTimeEntry>() {
                        @Override
                        public int compare(FileLastModifiedTimeEntry ci,
                                           FileLastModifiedTimeEntry ni) {
                            return ci.getFilePath().compareToIgnoreCase(ni.getFilePath());
                        }
                    });
//			Log.v("","idx="+idx+", fp="+fp);
            if (idx >= 0) {
                long diff_lcl = Math.abs(curr_last_modified_list.get(idx).getLocalFileLastModified() - l_lm);
                long diff_rmt = Math.abs(curr_last_modified_list.get(idx).getRemoteFileLastModified() - r_lm);
//				Log.v("","diff_lcl="+diff_lcl+", diff_rmt="+diff_rmt);
                if (diff_lcl > timeDifferenceLimit || diff_rmt > timeDifferenceLimit) {
//					Log.v("","list l_lm="+curr_last_modified_list.get(idx).getLocalFileLastModified()+", r_lm="+curr_last_modified_list.get(idx).getRemoteFileLastModified());
//					Log.v("","file l_lm="+l_lm+", r_lm="+r_lm);
                    result = true;
                }
                curr_last_modified_list.get(idx).setReferenced(true);
            } else {
//				Log.v("","added file l_lm="+l_lm+", r_lm="+r_lm);
                result = isAddedListWasDifferent(
                        curr_last_modified_list, new_last_modified_list,
                        fp, l_lm, r_lm, timeDifferenceLimit);
            }
        } else {
//			Log.v("","added fp="+fp);
            result = isAddedListWasDifferent(
                    curr_last_modified_list, new_last_modified_list,
                    fp, l_lm, r_lm, timeDifferenceLimit);
        }
        return result;
    }

    final public static boolean isAddedListWasDifferent(
            ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
            ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
            String fp, long l_lm, long r_lm, int timeDifferenceLimit) {
        boolean result = true, found = false;
        if (new_last_modified_list.size() == 0) result = true;
        else for (FileLastModifiedTimeEntry fli : new_last_modified_list) {
            if (fli.getFilePath().equals(fp)) {
                found = true;
                long diff_lcl = Math.abs(fli.getLocalFileLastModified() - l_lm);
                long diff_rmt = Math.abs(fli.getRemoteFileLastModified() - r_lm);
                if (diff_lcl <= 1 || diff_rmt <= 1) result = false;
                else result = true;
                break;
            }
        }
        if (!found) addLastModifiedItem(
                curr_last_modified_list, new_last_modified_list, fp, l_lm, r_lm);
//		Log.v("","isAddedListWasDifferent="+result+", added fp="+fp);
        return result;
    }

    final public static void addLastModifiedItem(
            ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
            ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
            String fp, long l_lm, long r_lm) {
//		Thread.dumpStack();
        if (new_last_modified_list.size() > 1000)
            mergeLastModifiedList(curr_last_modified_list, new_last_modified_list);
        FileLastModifiedTimeEntry fli = new FileLastModifiedTimeEntry
                (fp, l_lm, r_lm, true);
        new_last_modified_list.add(fli);
    }

    final public static boolean deleteLastModifiedItem(
            ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
            ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
            String fp) {
        boolean deleted=false;
        int idx = Collections.binarySearch(curr_last_modified_list,
                new FileLastModifiedTimeEntry(fp, 0, 0, false),
                new Comparator<FileLastModifiedTimeEntry>() {
                    @Override
                    public int compare(FileLastModifiedTimeEntry ci,
                                       FileLastModifiedTimeEntry ni) {
                        return ci.getFilePath().compareToIgnoreCase(ni.getFilePath());
                    }
                });
        if (idx >= 0) {
            curr_last_modified_list.remove(idx);
            deleted=true;
        }
        else for (FileLastModifiedTimeEntry fli : new_last_modified_list) {
            if (fli.getFilePath().equals(fp)) {
                new_last_modified_list.remove(fli);
                deleted=true;
                break;
            }
        }
        return deleted;
    }

    final public static boolean updateLastModifiedList(
            ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
            ArrayList<FileLastModifiedTimeEntry> new_last_modified_list,
            String target_path, long l_lm, long r_lm) {
//		long b_time=System.currentTimeMillis();
        boolean result = false;
        int idx = Collections.binarySearch(curr_last_modified_list,
                new FileLastModifiedTimeEntry(target_path, 0, 0, false),
                new Comparator<FileLastModifiedTimeEntry>() {
                    @Override
                    public int compare(FileLastModifiedTimeEntry ci,
                                       FileLastModifiedTimeEntry ni) {
                        return ci.getFilePath().compareToIgnoreCase(ni.getFilePath());
                    }
                });
//		long et1=System.currentTimeMillis()-b_time;
        if (idx >= 0) {
//			Log.v("","lf="+curr_last_modified_list.get(idx).getLocalFileLastModified()+", cur="+l_lm);
            curr_last_modified_list.get(idx).setLocalFileLastModified(l_lm);
            curr_last_modified_list.get(idx).setRemoteFileLastModified(r_lm);
            curr_last_modified_list.get(idx).setReferenced(true);
            result = true;
        } else for (FileLastModifiedTimeEntry fli : new_last_modified_list) {
//			Log.v("","tu2="+target_path);
            if (fli.getFilePath().equals(target_path)) {
//				Log.v("","tu2="+target_path);
                fli.setLocalFileLastModified(l_lm);
                fli.setRemoteFileLastModified(r_lm);
                result = true;
                break;
            }
        }
//		Log.v("","et1="+et1+", et2="+(System.currentTimeMillis()-b_time));
        return result;

    }

    final public static boolean isSetLastModifiedFunctional(String lmp) {
        boolean result = false;
//        File lf = new File(lmp + "/" + "SMBSyncLastModifiedTest.temp");
//        File dir = new File(lmp + "/");
        File lf = new File(lmp + "/Android/data/com.sentaroh.android.SMBSync2/files/SMBSyncLastModifiedTest.temp");
        File dir = new File(lmp + "/");
        try {
            if (dir.canWrite()) {
                if (lf.exists()) lf.delete();
                lf.createNewFile();
                result = lf.setLastModified(1000*1000);
                lf.delete();
            }
        } catch (IOException e) {
//			e.printStackTrace();
        }
        return result;
    }

    final static private void mergeLastModifiedList(
            ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
            ArrayList<FileLastModifiedTimeEntry> new_last_modified_list) {
        curr_last_modified_list.addAll(new_last_modified_list);
        new_last_modified_list.clear();
        Collections.sort(curr_last_modified_list,
            new Comparator<FileLastModifiedTimeEntry>() {
                @Override
                public int compare(FileLastModifiedTimeEntry ci, FileLastModifiedTimeEntry ni) {
                    return ci.getFilePath().compareToIgnoreCase(ni.getFilePath());
                }
            });
    }

    final public static void saveLastModifiedList(String dir,
                ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
                ArrayList<FileLastModifiedTimeEntry> new_last_modified_list) {
        if (new_last_modified_list.size() != 0) {
            mergeLastModifiedList(curr_last_modified_list, new_last_modified_list);
        }
        try {
//			long b_time=System.currentTimeMillis();
            File lf_tmp = new File(dir + "/lflm/");
            if (!lf_tmp.exists()) lf_tmp.mkdirs();
            String fn = SMBSYNC2_LOCAL_FILE_LAST_MODIFIED_NAME_V1;
            lf_tmp = new File(dir + "/lflm/" + fn + ".tmp");
            lf_tmp.delete();

            File lf_save = new File(dir + "/lflm/" + fn);

            FileOutputStream fos = new FileOutputStream(lf_tmp, false);
            BufferedOutputStream bos = new BufferedOutputStream(fos, 1024 * 1024);
            ZipOutputStream zos = new ZipOutputStream(bos);
            ZipEntry ze = new ZipEntry("list.txt");
            zos.putNextEntry(ze);
            OutputStreamWriter osw = new OutputStreamWriter(zos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw, 1024 * 1024 * 4);

            StringBuffer pl = new StringBuffer(512);
            String last_fp = "";
            String new_fp = "";
            for (FileLastModifiedTimeEntry lfme : curr_last_modified_list) {
                new_fp = lfme.getFilePath();
                if (!last_fp.equals(new_fp)) {
                    boolean f_exists = true;
                    if (!lfme.isReferenced()) {
                        last_fp = new_fp;
                        File slf = new File(last_fp);
                        f_exists = slf.exists();
                    }
                    if (f_exists || new_fp.equals(SMBSYNC2_LOCAL_FILE_LAST_MODIFIED_WAS_FORCE_LASTEST)) {
                        pl.append(new_fp)
                                .append("\t")
                                .append(String.valueOf(lfme.getLocalFileLastModified()))
                                .append("\t")
                                .append(String.valueOf(lfme.getRemoteFileLastModified()))
                                .append("\n");
                        bw.append(pl);
                        pl.setLength(0);
                    } else {
                    }
                } else {
                }
            }
		    bw.flush();
            bw.close();
            lf_save.delete();
            lf_tmp.renameTo(lf_save);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    final public static boolean isLastModifiedFileV1Exists(String dir) {
        boolean exists = false;

        File lf = new File(dir + "/lflm/" +
                SMBSYNC2_LOCAL_FILE_LAST_MODIFIED_NAME_V1);
        exists = lf.exists();

        return exists;
    }

    final public static boolean loadLastModifiedList(String dir,
                             ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
                             ArrayList<FileLastModifiedTimeEntry> new_last_modified_list, NotifyEvent p_ntfy) {
        boolean list_was_corrupted = false;
        curr_last_modified_list.clear();
        new_last_modified_list.clear();

        File lf1 = new File(dir + "/lflm/" +
                SMBSYNC2_LOCAL_FILE_LAST_MODIFIED_NAME_V1);
        if (lf1.exists()) loadLastModifiedListV1(dir,
                curr_last_modified_list, new_last_modified_list, p_ntfy);


        return list_was_corrupted;
    }

    final public static boolean loadLastModifiedListV1(String dir,
                               ArrayList<FileLastModifiedTimeEntry> curr_last_modified_list,
                               ArrayList<FileLastModifiedTimeEntry> new_last_modified_list, NotifyEvent p_ntfy) {
        curr_last_modified_list.clear();
        new_last_modified_list.clear();
        boolean list_was_corrupted = false;
        try {
//			long b_time=System.currentTimeMillis();
            File lf = new File(dir + "/lflm/" +
                    SMBSYNC2_LOCAL_FILE_LAST_MODIFIED_NAME_V1);
            FileInputStream fis = new FileInputStream(lf);
            BufferedInputStream bis = new BufferedInputStream(fis, 1024 * 1024 * 1);
            ZipInputStream zis = new ZipInputStream(bis);
            zis.getNextEntry();
            InputStreamReader isr = new InputStreamReader(zis, "UTF-8");
            BufferedReader br = new BufferedReader(isr, 1024 * 1024 * 4);
            String line = null;
            String[] l_array = null;
            String last_fp = "";
            while ((line = br.readLine()) != null) {
                l_array = line.split("\t");
                if (l_array != null && l_array.length == 3) {
                    if (!last_fp.equals(l_array[0])) {
                        curr_last_modified_list.add(new FileLastModifiedTimeEntry(
                                l_array[0], Long.valueOf(l_array[1]), Long.valueOf(l_array[2]), false));
                        last_fp = l_array[0];
                    } else {
                        if (p_ntfy != null) p_ntfy.notifyToListener(false, new Object[]{last_fp});
                        list_was_corrupted = true;
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list_was_corrupted;
    }

}

