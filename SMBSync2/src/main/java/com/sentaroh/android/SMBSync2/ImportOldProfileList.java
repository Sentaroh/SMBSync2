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

import android.annotation.SuppressLint;

import com.sentaroh.android.Utilities.Base64Compat;
import com.sentaroh.android.Utilities.EncryptUtil;
import com.sentaroh.android.Utilities.EncryptUtil.CipherParms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class ImportOldProfileList {

    private final static String SMBSYNC_PROF_VER1 = "PROF 1";
    private final static String SMBSYNC_PROF_VER2 = "PROF 2";
    private final static String SMBSYNC_PROF_VER3 = "PROF 3";
    private final static String SMBSYNC_PROF_VER4 = "PROF 4";
    private final static String SMBSYNC_PROF_VER5 = "PROF 5";
    private final static String SMBSYNC_PROF_VER6 = "PROF 6";
    private final static String SMBSYNC_PROF_VER7 = "PROF 7";
    private final static String SMBSYNC_PROF_VER8 = "PROF 8";
    private final static String SMBSYNC_PROF_ENC = "ENC";
    private final static String SMBSYNC_PROF_DEC = "DEC";

    private final static String SMBSYNC_PROF_GROUP_DEFAULT = "Default";

    private final static String SMBSYNC_PROF_TYPE_SYNC = "S";
    private final static String SMBSYNC_PROF_TYPE_LOCAL = "L";
    private final static String SMBSYNC_PROF_TYPE_REMOTE = "R";
    private final static String SMBSYNC_PROF_ACTIVE = "A";

    static public ArrayList<SyncTaskItem> importOldProfileList(GlobalParameters gp, final String fpath) {
        final ArrayList<ProfileListItem> pfl = createProfileList(gp, true, fpath);
        return convertProfile(gp, pfl);
    }

    ;

    @SuppressLint("SdCardPath")
    static private ArrayList<SyncTaskItem> convertProfile(GlobalParameters gp, ArrayList<ProfileListItem> pfl) {
        ArrayList<SyncTaskItem> stl = new ArrayList<SyncTaskItem>();
        for (ProfileListItem pli : pfl) {
            if (pli.getProfileType().equals(SMBSYNC_PROF_TYPE_SYNC)) {
                SyncTaskItem sti = new SyncTaskItem();
                sti.setSyncTaskName(pli.getProfileName());
                sti.setSyncTaskAuto(false);
                sti.setSyncTaskType(pli.getSyncType());

                sti.setSyncConfirmOverrideOrDelete(pli.isConfirmRequired());
                sti.setSyncOptionSyncEmptyDirectory(pli.isSyncEmptyDirectory());
                sti.setSyncOptionSyncHiddenDirectory(pli.isSyncHiddenDirectory());
                sti.setSyncOptionSyncHiddenFile(pli.isSyncHiddenFile());
                sti.setSyncProcessRootDirFile(pli.isMasterDirFileProcess());
                sti.setSyncOptionSyncSubDirectory(pli.isSyncSubDirectory());
                sti.setDirFilter(pli.getDirFilter());
                sti.setFileFilter(pli.getFileFilter());

                sti.setSyncDetectLastModidiedBySmbsync(pli.isForceLastModifiedUseSmbsync());
                sti.setSyncDoNotResetFileLastModified(pli.isNotUseLastModifiedForRemote());
                sti.setSyncOptionUseSmallIoBuffer(pli.isSyncUseRemoteSmallIoArea());

                ProfileListItem m_pli = getProfile(pli.getMasterName(), pfl);
                ProfileListItem t_pli = getProfile(pli.getTargetName(), pfl);
                if (m_pli != null) {
                    if (pli.getMasterType().equals(SMBSYNC_PROF_TYPE_REMOTE)) {
                        sti.setMasterFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_SMB);
                        sti.setMasterSmbAddr(m_pli.getRemoteAddr());
                        sti.setMasterSmbHostName(m_pli.getRemoteHostname());
                        sti.setMasterSmbPort(m_pli.getRemotePort());
                        sti.setMasterSmbShareName(m_pli.getRemoteShareName());
                        sti.setMasterSmbUserName(m_pli.getRemoteUserID());
                        sti.setMasterSmbPassword(m_pli.getRemotePassword());
                        sti.setMasterDirectoryName(m_pli.getDirectoryName());
                    } else {
                        sti.setMasterDirectoryName(m_pli.getDirectoryName());
                        if (m_pli.getLocalMountPoint().equals("/sdcard1") ||
                                m_pli.getLocalMountPoint().equals("/storage/sdcard1")) {
                            sti.setMasterFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD);
                        } else if (m_pli.getLocalMountPoint().equals("/storage/usbdisk") ||
                                m_pli.getLocalMountPoint().equals("/Removable/USBdisk1/Drive1") ||
                                m_pli.getLocalMountPoint().equals("/Removable/USBdisk2/Drive1")) {
                            sti.setMasterFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
//							sti.setMasterFolderUseInternalUsbFolder(true);
                        } else {
                            sti.setMasterFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
                        }
                    }
                }
                if (t_pli != null) {
                    if (pli.getTargetType().equals(SMBSYNC_PROF_TYPE_REMOTE)) {
                        sti.setTargetFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_SMB);
                        sti.setTargetRemoteAddr(t_pli.getRemoteAddr());
                        sti.setTargetRemoteHostname(t_pli.getRemoteHostname());
                        sti.setTargetRemotePort(t_pli.getRemotePort());
                        sti.setTargetSmbShareName(t_pli.getRemoteShareName());
                        sti.setTargetSmbUserName(t_pli.getRemoteUserID());
                        sti.setTargetSmbPassword(t_pli.getRemotePassword());
                        sti.setTargetDirectoryName(t_pli.getDirectoryName());
                    } else {
                        sti.setTargetDirectoryName(t_pli.getDirectoryName());
                        if (t_pli.getLocalMountPoint().equals("/sdcard1") ||
                                t_pli.getLocalMountPoint().equals("/storage/sdcard1")) {
                            sti.setTargetFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_SDCARD);
                        } else if (t_pli.getLocalMountPoint().equals("/storage/usbdisk") ||
                                t_pli.getLocalMountPoint().equals("/Removable/USBdisk1/Drive1") ||
                                t_pli.getLocalMountPoint().equals("/Removable/USBdisk2/Drive1")) {
                            sti.setTargetFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
//							sti.setTargetFolderUseInternalUsbFolder(true);
                        } else {
                            sti.setTargetFolderType(SyncTaskItem.SYNC_FOLDER_TYPE_INTERNAL);
                        }
                    }
                }
                stl.add(sti);
            }
        }
        return stl;
    }

    ;

    static private boolean isProfileWasEncrypted(GlobalParameters gp, String fpath) {
        boolean result = false;
        File lf = new File(fpath);
        if (lf.exists() && lf.canRead()) {
            try {
                BufferedReader br;
                br = new BufferedReader(new FileReader(fpath), 8192);
                String pl = br.readLine();
                if (pl != null) {
                    if (pl.startsWith(SMBSYNC_PROF_VER1) || pl.startsWith(SMBSYNC_PROF_VER2)) {
                        //NOtencrypted
                    } else if (pl.startsWith(SMBSYNC_PROF_VER3)) {
                        if (pl.startsWith(SMBSYNC_PROF_VER3 + SMBSYNC_PROF_ENC)) result = true;
                    } else if (pl.startsWith(SMBSYNC_PROF_VER4)) {
                        if (pl.startsWith(SMBSYNC_PROF_VER4 + SMBSYNC_PROF_ENC)) result = true;
                    } else if (pl.startsWith(SMBSYNC_PROF_VER5)) {
                        if (pl.startsWith(SMBSYNC_PROF_VER5 + SMBSYNC_PROF_ENC)) result = true;
                    } else if (pl.startsWith(SMBSYNC_PROF_VER6)) {
                        if (pl.startsWith(SMBSYNC_PROF_VER6 + SMBSYNC_PROF_ENC)) result = true;
                    } else if (pl.startsWith(SMBSYNC_PROF_VER7)) {
                        if (pl.startsWith(SMBSYNC_PROF_VER7 + SMBSYNC_PROF_ENC)) result = true;
                    } else if (pl.startsWith(SMBSYNC_PROF_VER8)) {
                        if (pl.startsWith(SMBSYNC_PROF_VER8 + SMBSYNC_PROF_ENC)) result = true;
                    }
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    ;

    static private ArrayList<ProfileListItem> createProfileList(GlobalParameters gp, boolean sdcard, String fp) {

        ArrayList<ProfileListItem> sync = new ArrayList<ProfileListItem>();
        ArrayList<ProfileListItem> rem = new ArrayList<ProfileListItem>();
        ArrayList<ProfileListItem> lcl = new ArrayList<ProfileListItem>();

        if (sdcard) {
            File sf = new File(fp);
            if (sf.exists()) {
                CipherParms cp = null;
                boolean prof_encrypted = isProfileWasEncrypted(gp, fp);
                if (prof_encrypted) {
                    cp = EncryptUtil.initDecryptEnv(gp.profileKeyPrefixOld + gp.profilePassword);
                }
                try {
                    BufferedReader br;
                    br = new BufferedReader(new FileReader(fp), 8192);
                    String pl;
                    while ((pl = br.readLine()) != null) {
                        if (pl.startsWith(SMBSYNC_PROF_VER1) || pl.startsWith(SMBSYNC_PROF_VER2)) {
                            addProfileList(gp, pl, sync, rem, lcl);
                        } else if (pl.startsWith(SMBSYNC_PROF_VER3) ||
                                pl.startsWith(SMBSYNC_PROF_VER4) ||
                                pl.startsWith(SMBSYNC_PROF_VER5) ||
                                pl.startsWith(SMBSYNC_PROF_VER6) ||
                                pl.startsWith(SMBSYNC_PROF_VER7) ||
                                pl.startsWith(SMBSYNC_PROF_VER8)
                                ) {
                            String prof_pre = "";
                            if (pl.startsWith(SMBSYNC_PROF_VER3)) prof_pre = SMBSYNC_PROF_VER3;
                            else if (pl.startsWith(SMBSYNC_PROF_VER4)) prof_pre = SMBSYNC_PROF_VER4;
                            else if (pl.startsWith(SMBSYNC_PROF_VER5)) prof_pre = SMBSYNC_PROF_VER5;
                            else if (pl.startsWith(SMBSYNC_PROF_VER6)) prof_pre = SMBSYNC_PROF_VER6;
                            else if (pl.startsWith(SMBSYNC_PROF_VER7)) prof_pre = SMBSYNC_PROF_VER7;
                            else if (pl.startsWith(SMBSYNC_PROF_VER8)) prof_pre = SMBSYNC_PROF_VER8;
                            if (!pl.startsWith(prof_pre + SMBSYNC_PROF_ENC) &&
                                    !pl.startsWith(prof_pre + SMBSYNC_PROF_DEC)) {
                                if (prof_encrypted) {
                                    String enc_str = pl.replace(prof_pre, "");
//									Log.v("","enc load="+enc_str);
                                    byte[] enc_array = Base64Compat.decode(enc_str, Base64Compat.NO_WRAP);
                                    String dec_str = EncryptUtil.decrypt(enc_array, cp);
//									Log.v("","dec load="+dec_str);
                                    addProfileList(gp, prof_pre + dec_str, sync, rem, lcl);
                                } else {
                                    addProfileList(gp, pl, sync, rem, lcl);
                                }
                            }
                        }
                    }
                    br.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return sync;
                } catch (IOException e) {
                    e.printStackTrace();
                    return sync;
                }
            }
        }

        Collections.sort(sync);
        Collections.sort(rem);
        Collections.sort(lcl);
        sync.addAll(rem);
        sync.addAll(lcl);

        for (int i = 0; i < sync.size(); i++) {
            ProfileListItem item = sync.get(i);
            if (item.getMasterType().equals("")) {
                item.setMasterType(getProfileType(item.getMasterName(), sync));
                item.setTargetType(getProfileType(item.getTargetName(), sync));
//				pfl.replace(item, i);
            }
        }

        return sync;
    }

    ;

    static private ProfileListItem getProfile(String pfn, ArrayList<ProfileListItem> pfl) {
        for (int i = 0; i < pfl.size(); i++)
            if (pfl.get(i).getProfileName().equals(pfn))
                return pfl.get(i);
        return null;
    }

    ;

    static private String getProfileType(String pfn, ArrayList<ProfileListItem> pfl) {
        for (int i = 0; i < pfl.size(); i++)
            if (pfl.get(i).getProfileName().equals(pfn))
                return pfl.get(i).getProfileType();
        return "";
    }

    ;

    static private void addProfileList(GlobalParameters gp, String pl, ArrayList<ProfileListItem> sync,
                                       ArrayList<ProfileListItem> rem, ArrayList<ProfileListItem> lcl) {
        String profVer = "";
        if (pl.length() > 7) profVer = pl.substring(0, 6);
        if (profVer.equals(SMBSYNC_PROF_VER1)) {
            if (pl.length() > 10) {
                addProfileListVer1(gp, pl.substring(7, pl.length()), sync, rem, lcl);
            }
        } else if (profVer.equals(SMBSYNC_PROF_VER2)) {
            if (pl.length() > 10) {
                addProfileListVer2(gp, pl.substring(7, pl.length()), sync, rem, lcl);
            }
        } else if (profVer.equals(SMBSYNC_PROF_VER3)) {
            if (pl.length() > 10) {
                addProfileListVer3(gp, pl.substring(6, pl.length()), sync, rem, lcl);
            }
        } else if (profVer.equals(SMBSYNC_PROF_VER4)) {
            if (pl.length() > 10) {
                addProfileListVer4(gp, pl.substring(6, pl.length()), sync, rem, lcl);
            }
        } else if (profVer.equals(SMBSYNC_PROF_VER5)) {
            if (pl.length() > 10) {
                addProfileListVer5(gp, pl.substring(6, pl.length()), sync, rem, lcl);
            }
        } else if (profVer.equals(SMBSYNC_PROF_VER6)) {
            if (pl.length() > 10) {
                addProfileListVer6(gp, pl.substring(6, pl.length()), sync, rem, lcl);
            }
        } else if (profVer.equals(SMBSYNC_PROF_VER7)) {
            if (pl.length() > 10) {
                addProfileListVer7(gp, pl.substring(6, pl.length()), sync, rem, lcl);
            }
        } else if (profVer.equals(SMBSYNC_PROF_VER8)) {
            if (pl.length() > 10) {
                addProfileListVer8(gp, pl.substring(6, pl.length()), sync, rem, lcl);
            }
        } else addProfileListVer0(gp, pl, sync, rem, lcl);
    }

    ;

    static private void addProfileListVer0(GlobalParameters gp, String pl, ArrayList<ProfileListItem> sync,
                                           ArrayList<ProfileListItem> rem, ArrayList<ProfileListItem> lcl) {

        String prof_group = SMBSYNC_PROF_GROUP_DEFAULT;

        String[] tmp_pl = pl.split(",");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = tmp_pl[i];
            }
        }
        if (parm[0].equals(SMBSYNC_PROF_TYPE_REMOTE)) {//Remote
            rem.add(createRemoteProfilelistItem(gp,
                    prof_group,// group
                    parm[1],//Name
                    parm[2],//Active
                    parm[7],//directory
                    parm[5],//user
                    parm[6],//pass
                    parm[4],//share
                    parm[3],//address
                    "",        //hostname
                    "",//port
                    "", 0, 0,
                    false));
        } else {
            if (parm[0].equals(SMBSYNC_PROF_TYPE_LOCAL)) {//Local
                lcl.add(createLocalProfilelistItem(gp,
                        prof_group,// group
                        parm[1],//Name
                        parm[2],//Active
                        parm[3],//Directory
                        "",
                        "", 0, 0,
                        false));
            } else if (parm[0].equals(SMBSYNC_PROF_TYPE_SYNC)) {//Sync
                ArrayList<String> ff = new ArrayList<String>();
                ArrayList<String> df = new ArrayList<String>();
                if (parm[6].length() != 0) ff.add("IF" + parm[6]);
                if (parm[7].length() != 0) ff.add("IF" + parm[7]);
                if (parm[8].length() != 0) ff.add("IF" + parm[8]);
                sync.add(createSyncProfilelistItem(gp,
                        prof_group,// group
                        parm[1],//Name
                        parm[2],//Active
                        parm[5],//Sync type
                        "",//Master type
                        parm[3],//Master name
                        "",//Target type
                        parm[4],//Target name
                        ff,//File Filter
                        df,//Dir Filter
                        true,
                        false,
                        true,
                        false,
                        "0", false, true, true, true, false,
                        "", 0, 0,
                        "", 0,
                        false));
            }
        }
    }

    ;

    static private void addProfileListVer1(GlobalParameters gp, String pl, ArrayList<ProfileListItem> sync,
                                           ArrayList<ProfileListItem> rem, ArrayList<ProfileListItem> lcl) {
        if (pl.startsWith(SMBSYNC_PROF_VER1 + "," + "SETTINGS")) return; //ignore settings entry
        //Extract ArrayList<String> field
        String list1 = "", list2 = "", npl = "";
        if (pl.indexOf("[") >= 0) {
            // found first List
            list1 = pl.substring(pl.indexOf("[") + 1, pl.indexOf("]"));
            npl = pl.replace("[" + list1 + "]\t", "");
            if (npl.indexOf("[") >= 0) {
                // found second List
                list2 = npl.substring(npl.indexOf("[") + 1, npl.indexOf("]"));
                npl = npl.replace("[" + list2 + "]\t", "");
            }
        } else npl = pl;
//		String prof_group = npl.substring(0,11).trim();
//		String tmp_ps=npl.substring(12,npl.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i]);
            }
        }
        if (parm[1].equals(SMBSYNC_PROF_TYPE_REMOTE)) {//Remote
            rem.add(createRemoteProfilelistItem(gp,
                    parm[0],//group
                    parm[2],//Name
                    parm[3],//Active
                    parm[8],//directory
                    parm[4],//user
                    parm[5],//pass
                    parm[7],//share
                    parm[6],//address
                    "",        //hostname
                    "",//port
                    "", 0, 0,
                    false));

        } else {
            if (parm[1].equals(SMBSYNC_PROF_TYPE_LOCAL)) {//Local
                lcl.add(createLocalProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Directory
                        parm[5],//Local mount point
                        "", 0, 0,
                        false));
            } else if (parm[1].equals(SMBSYNC_PROF_TYPE_SYNC)) {//Sync
                ArrayList<String> ff = new ArrayList<String>();
                ArrayList<String> df = new ArrayList<String>();
                if (list1.length() != 0) {
                    String[] fp = list1.split("\t");
                    for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
                } else ff.clear();
                if (list2.length() != 0) {
                    String[] dp = list2.split("\t");
                    for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
                } else df.clear();
                boolean mpd = true, conf = false, ujlm = false;
                if (parm[9].equals("0")) mpd = false;
                if (parm[10].equals("1")) conf = true;
                if (parm[11].equals("1")) ujlm = true;
                sync.add(createSyncProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Sync type
                        parm[5],//Master type
                        parm[6],//Master name
                        parm[7],//Target type
                        parm[8],//Target name
                        ff,//File Filter
                        df,//Dir Filter
                        mpd,
                        conf,
                        ujlm,
                        false,
                        "0", false, true, true, true, false,
                        "", 0, 0,
                        "", 0,
                        false));
            }
        }
    }

    ;

    static private void addProfileListVer2(GlobalParameters gp, String pl, ArrayList<ProfileListItem> sync,
                                           ArrayList<ProfileListItem> rem, ArrayList<ProfileListItem> lcl) {
        if (pl.startsWith(SMBSYNC_PROF_VER2 + "," + "SETTINGS")) return; //ignore settings entry
        //Extract ArrayList<String> field
        String list1 = "", list2 = "", npl = "";
        if (pl.indexOf("[") >= 0) {
            // found first List
            list1 = pl.substring(pl.indexOf("[") + 1, pl.indexOf("]"));
            npl = pl.replace("[" + list1 + "]\t", "");
            if (npl.indexOf("[") >= 0) {
                // found second List
                list2 = npl.substring(npl.indexOf("[") + 1, npl.indexOf("]"));
                npl = npl.replace("[" + list2 + "]\t", "");
            }
        } else npl = pl;
//		String prof_group = npl.substring(0,11).trim();
//		String tmp_ps=npl.substring(12,npl.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i]);
            }
        }
        if (parm[1].equals(SMBSYNC_PROF_TYPE_REMOTE)) {//Remote
            rem.add(createRemoteProfilelistItem(gp,
                    parm[0],//group
                    parm[2],//Name
                    parm[3],//Active
                    parm[8],//directory
                    parm[4],//user
                    parm[5],//pass
                    parm[7],//share
                    parm[6],//address
                    parm[9],//hostname
                    "",//port
                    "", 0, 0,
                    false));

        } else {
            if (parm[1].equals(SMBSYNC_PROF_TYPE_LOCAL)) {//Local
                lcl.add(createLocalProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Directory
                        parm[5],//Local mount point
                        "", 0, 0,
                        false));
            } else if (parm[1].equals(SMBSYNC_PROF_TYPE_SYNC)) {//Sync
                ArrayList<String> ff = new ArrayList<String>();
                ArrayList<String> df = new ArrayList<String>();
                if (list1.length() != 0) {
                    String[] fp = list1.split("\t");
                    for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
                } else ff.clear();
                if (list2.length() != 0) {
                    String[] dp = list2.split("\t");
                    for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
                } else df.clear();
                boolean mpd = true, conf = false, ujlm = false;
                if (parm[9].equals("0")) mpd = false;
                if (parm[10].equals("1")) conf = true;
                if (parm[11].equals("1")) ujlm = true;
                sync.add(createSyncProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Sync type
                        parm[5],//Master type
                        parm[6],//Master name
                        parm[7],//Target type
                        parm[8],//Target name
                        ff,//File Filter
                        df,//Dir Filter
                        mpd,
                        conf,
                        ujlm,
                        false,
                        "0", false, true, true, true, false,
                        "", 0, 0,
                        "", 0,
                        false));
            }
        }
    }

    ;

    static private void addProfileListVer3(GlobalParameters gp, String pl, ArrayList<ProfileListItem> sync,
                                           ArrayList<ProfileListItem> rem, ArrayList<ProfileListItem> lcl) {
        //Extract ArrayList<String> field
        String list1 = "", list2 = "", npl = "";
        if (pl.indexOf("[") >= 0) {
            // found first List
            list1 = pl.substring(pl.indexOf("[") + 1, pl.indexOf("]"));
            npl = pl.replace("[" + list1 + "]\t", "");
            if (npl.indexOf("[") >= 0) {
                // found second List
                list2 = npl.substring(npl.indexOf("[") + 1, npl.indexOf("]"));
                npl = npl.replace("[" + list2 + "]\t", "");
            }
        } else npl = pl;
//		String prof_group = npl.substring(0,11).trim();
//		String tmp_ps=npl.substring(12,npl.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
        }
        if (parm[1].equals("SETTINGS")) return; //ignore settings entry

        if (parm[1].equals(SMBSYNC_PROF_TYPE_REMOTE)) {//Remote
            String h_addr = "", h_name = "";
            if (parm[6].length() > 0) {
                if (parm[6].substring(0, 1).compareTo("0") >= 0 && parm[6].substring(0, 1).compareTo("9") <= 0) {
                    h_addr = parm[6];
                } else {
                    h_name = parm[6];
                }
            } else {
                h_addr = "";
                h_name = parm[9];
            }
//			Log.v("","h_addr="+h_addr+", h_name="+h_name);
            rem.add(createRemoteProfilelistItem(gp,
                    parm[0],//group
                    parm[2],//Name
                    parm[3],//Active
                    parm[8],//directory
                    parm[4],//user
                    parm[5],//pass
                    parm[7],//share
                    h_addr,//address
                    h_name,//hostname
                    "",//port
                    "", 0, 0,
                    false));

        } else {
            if (parm[1].equals(SMBSYNC_PROF_TYPE_LOCAL)) {//Local
                lcl.add(createLocalProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Directory
                        parm[5],//Local mount point
                        "", 0, 0,
                        false));
            } else if (parm[1].equals(SMBSYNC_PROF_TYPE_SYNC)) {//Sync
                ArrayList<String> ff = new ArrayList<String>();
                ArrayList<String> df = new ArrayList<String>();
                if (list1.length() != 0) {
                    String[] fp = list1.split("\t");
                    for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
                } else ff.clear();
                if (list2.length() != 0) {
                    String[] dp = list2.split("\t");
                    for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
                } else df.clear();
                boolean mpd = true, conf = false, ujlm = false;
                if (parm[9].equals("0")) mpd = false;
                if (parm[10].equals("1")) conf = true;
                if (parm[11].equals("1")) ujlm = true;
                sync.add(createSyncProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Sync type
                        parm[5],//Master type
                        parm[6],//Master name
                        parm[7],//Target type
                        parm[8],//Target name
                        ff,//File Filter
                        df,//Dir Filter
                        mpd,
                        conf,
                        ujlm,
                        false,
                        "0", false, true, true, true, false,
                        "", 0, 0,
                        "", 0,
                        false));
            }
        }
    }

    ;

    static private void addProfileListVer4(GlobalParameters gp, String pl, ArrayList<ProfileListItem> sync,
                                           ArrayList<ProfileListItem> rem, ArrayList<ProfileListItem> lcl) {
        //Extract ArrayList<String> field
        String list1 = "", list2 = "", npl = "";
        if (pl.indexOf("[") >= 0) {
            // found first List
            list1 = pl.substring(pl.indexOf("[") + 1, pl.indexOf("]"));
            npl = pl.replace("[" + list1 + "]\t", "");
            if (npl.indexOf("[") >= 0) {
                // found second List
                list2 = npl.substring(npl.indexOf("[") + 1, npl.indexOf("]"));
                npl = npl.replace("[" + list2 + "]\t", "");
            }
        } else npl = pl;
//		String prof_group = npl.substring(0,11).trim();
//		String tmp_ps=npl.substring(12,npl.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
        }
        if (parm[1].equals("SETTINGS")) return; //ignore settings entry

        if (parm[1].equals(SMBSYNC_PROF_TYPE_REMOTE)) {//Remote
            String h_addr = "", h_name = "";
            if (parm[6].length() > 0) {
                if (parm[6].substring(0, 1).compareTo("0") >= 0 && parm[6].substring(0, 1).compareTo("9") <= 0) {
                    h_addr = parm[6];
                } else {
                    h_name = parm[6];
                }
            } else {
                h_addr = "";
                h_name = parm[9];
            }
//			Log.v("","h_addr="+h_addr+", h_name="+h_name);
            rem.add(createRemoteProfilelistItem(gp,
                    parm[0],//group
                    parm[2],//Name
                    parm[3],//Active
                    parm[8],//directory
                    parm[4],//user
                    parm[5],//pass
                    parm[7],//share
                    h_addr,//address
                    h_name,//hostname
                    "",//port
                    "", 0, 0,
                    false));

        } else {
            if (parm[1].equals(SMBSYNC_PROF_TYPE_LOCAL)) {//Local
                lcl.add(createLocalProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Directory
                        parm[5],//Local mount point
                        "", 0, 0,
                        false));
            } else if (parm[1].equals(SMBSYNC_PROF_TYPE_SYNC)) {//Sync
                ArrayList<String> ff = new ArrayList<String>();
                ArrayList<String> df = new ArrayList<String>();
                if (list1.length() != 0) {
                    String[] fp = list1.split("\t");
                    for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
                } else ff.clear();
                if (list2.length() != 0) {
                    String[] dp = list2.split("\t");
                    for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
                } else df.clear();
                boolean mpd = true, conf = false, ujlm = false, nulm_remote = false;
                if (parm[9].equals("0")) mpd = false;
                if (parm[10].equals("1")) conf = true;
                if (parm[11].equals("1")) ujlm = true;
                if (parm[12].equals("1")) nulm_remote = true;
                sync.add(createSyncProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Sync type
                        parm[5],//Master type
                        parm[6],//Master name
                        parm[7],//Target type
                        parm[8],//Target name
                        ff,//File Filter
                        df,//Dir Filter
                        mpd,
                        conf,
                        ujlm,
                        nulm_remote,
                        "0", false, true, true, true, false,
                        "", 0, 0,
                        "", 0,
                        false));
            }
        }
    }

    ;

    static private void addProfileListVer5(GlobalParameters gp, String pl, ArrayList<ProfileListItem> sync,
                                           ArrayList<ProfileListItem> rem, ArrayList<ProfileListItem> lcl) {
        //Extract ArrayList<String> field
        String list1 = "", list2 = "", npl = "";
        if (pl.indexOf("[") >= 0) {
            // found first List
            list1 = pl.substring(pl.indexOf("[") + 1, pl.indexOf("]"));
            npl = pl.replace("[" + list1 + "]\t", "");
            if (npl.indexOf("[") >= 0) {
                // found second List
                list2 = npl.substring(npl.indexOf("[") + 1, npl.indexOf("]"));
                npl = npl.replace("[" + list2 + "]\t", "");
            }
        } else npl = pl;
//		Log.v("","pl="+pl);
//		String prof_group = npl.substring(0,11).trim();
//		String tmp_ps=npl.substring(12,npl.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
//			Log.v("","i="+i+", "+parm[i]);
        }
        if (parm[1].equals("SETTINGS")) return; //ignore settings entry

        if (parm[1].equals(SMBSYNC_PROF_TYPE_REMOTE)) {//Remote
            String h_addr = "", h_name = "";
            if (parm[6].length() > 0) {
                if (parm[6].substring(0, 1).compareTo("0") >= 0 && parm[6].substring(0, 1).compareTo("9") <= 0) {
                    h_addr = parm[6];
                } else {
                    h_name = parm[6];
                }
            } else {
                h_addr = "";
                h_name = parm[9];
            }
//			Log.v("","h_addr="+h_addr+", h_name="+h_name);
            rem.add(createRemoteProfilelistItem(gp,
                    parm[0],//group
                    parm[2],//Name
                    parm[3],//Active
                    parm[8],//directory
                    parm[4],//user
                    parm[5],//pass
                    parm[7],//share
                    h_addr,//address
                    h_name,//hostname
                    parm[10],//port
                    "", 0, 0,
                    false));

        } else {
            if (parm[1].equals(SMBSYNC_PROF_TYPE_LOCAL)) {//Local
                lcl.add(createLocalProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Directory
                        parm[5],//Local mount point
                        "", 0, 0,
                        false));
            } else if (parm[1].equals(SMBSYNC_PROF_TYPE_SYNC)) {//Sync
                ArrayList<String> ff = new ArrayList<String>();
                ArrayList<String> df = new ArrayList<String>();
                if (list1.length() != 0) {
                    String[] fp = list1.split("\t");
                    for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
                } else ff.clear();
                if (list2.length() != 0) {
                    String[] dp = list2.split("\t");
                    for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
                } else df.clear();
                boolean mpd = true, conf = false, ujlm = false, nulm_remote = false;
                if (parm[9].equals("0")) mpd = false;
                if (parm[10].equals("1")) conf = true;
                if (parm[11].equals("1")) ujlm = true;
                if (parm[12].equals("1")) nulm_remote = true;
                sync.add(createSyncProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Sync type
                        parm[5],//Master type
                        parm[6],//Master name
                        parm[7],//Target type
                        parm[8],//Target name
                        ff,//File Filter
                        df,//Dir Filter
                        mpd,
                        conf,
                        ujlm,
                        nulm_remote,
                        "0", false, true, true, true, false,
                        "", 0, 0,
                        "", 0,
                        false));
            }
        }
    }

    ;

    static private void addProfileListVer6(GlobalParameters gp, String pl, ArrayList<ProfileListItem> sync,
                                           ArrayList<ProfileListItem> rem, ArrayList<ProfileListItem> lcl) {
        //Extract ArrayList<String> field
        String list1 = "", list2 = "", npl = "";
        if (pl.indexOf("[") >= 0) {
            // found first List
            list1 = pl.substring(pl.indexOf("[") + 1, pl.indexOf("]"));
            npl = pl.replace("[" + list1 + "]\t", "");
            if (npl.indexOf("[") >= 0) {
                // found second List
                list2 = npl.substring(npl.indexOf("[") + 1, npl.indexOf("]"));
                npl = npl.replace("[" + list2 + "]\t", "");
            }
        } else npl = pl;
//		Log.v("","pl="+pl);
//		String prof_group = npl.substring(0,11).trim();
//		String tmp_ps=npl.substring(12,npl.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
//			Log.v("","i="+i+", "+parm[i]);
        }
        if (parm[1].equals("SETTINGS")) return; //ignore settings entry

        if (parm[1].equals(SMBSYNC_PROF_TYPE_REMOTE)) {//Remote
            String h_addr = "", h_name = "";
            if (parm[6].length() > 0) {
                if (parm[6].substring(0, 1).compareTo("0") >= 0 && parm[6].substring(0, 1).compareTo("9") <= 0) {
                    h_addr = parm[6];
                } else {
                    h_name = parm[6];
                }
            } else {
                h_addr = "";
                h_name = parm[9];
            }
//			Log.v("","h_addr="+h_addr+", h_name="+h_name);
            rem.add(createRemoteProfilelistItem(gp,
                    parm[0],//group
                    parm[2],//Name
                    parm[3],//Active
                    parm[8],//directory
                    parm[4],//user
                    parm[5],//pass
                    parm[7],//share
                    h_addr,//address
                    h_name,//hostname
                    parm[10],//port
                    "", 0, 0,
                    false));

        } else {
            if (parm[1].equals(SMBSYNC_PROF_TYPE_LOCAL)) {//Local
                lcl.add(createLocalProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Directory
                        parm[5],//Local mount point
                        "", 0, 0,
                        false));
            } else if (parm[1].equals(SMBSYNC_PROF_TYPE_SYNC)) {//Sync
                ArrayList<String> ff = new ArrayList<String>();
                ArrayList<String> df = new ArrayList<String>();
                if (list1.length() != 0) {
                    String[] fp = list1.split("\t");
                    for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
                } else ff.clear();
                if (list2.length() != 0) {
                    String[] dp = list2.split("\t");
                    for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
                } else df.clear();
                boolean mpd = true, conf = false, ujlm = false, nulm_remote = false;
                if (parm[9].equals("0")) mpd = false;
                if (parm[10].equals("1")) conf = true;
                if (parm[11].equals("1")) ujlm = true;
                if (parm[12].equals("1")) nulm_remote = true;
                boolean sync_empty_dir = false;
                if (parm[14].equals("1")) sync_empty_dir = true;
                boolean sync_hidden_dir = false;
                if (parm[15].equals("1")) sync_hidden_dir = true;
                boolean sync_hidden_file = false;
                if (parm[16].equals("1")) sync_hidden_file = true;
                boolean sync_sub_dir = false;
                if (parm[17].equals("1")) sync_sub_dir = true;
//				Log.v("","17="+parm[17]);
                sync.add(createSyncProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Sync type
                        parm[5],//Master type
                        parm[6],//Master name
                        parm[7],//Target type
                        parm[8],//Target name
                        ff,//File Filter
                        df,//Dir Filter
                        mpd,
                        conf,
                        ujlm,
                        nulm_remote,
                        parm[13],//Retry count
                        sync_empty_dir,
                        sync_hidden_dir, sync_hidden_file, sync_sub_dir, false,
                        "", 0, 0,
                        "", 0,
                        false));
            }
        }
    }

    ;

    static private void addProfileListVer7(GlobalParameters gp, String pl, ArrayList<ProfileListItem> sync,
                                           ArrayList<ProfileListItem> rem, ArrayList<ProfileListItem> lcl) {
        //Extract ArrayList<String> field
        String list1 = "", list2 = "", npl = "";
        if (pl.indexOf("[") >= 0) {
            // found first List
            list1 = pl.substring(pl.indexOf("[") + 1, pl.indexOf("]"));
            npl = pl.replace("[" + list1 + "]\t", "");
            if (npl.indexOf("[") >= 0) {
                // found second List
                list2 = npl.substring(npl.indexOf("[") + 1, npl.indexOf("]"));
                npl = npl.replace("[" + list2 + "]\t", "");
            }
        } else npl = pl;
//		Log.v("","pl="+pl);
//		String prof_group = npl.substring(0,11).trim();
//		String tmp_ps=npl.substring(12,npl.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
//			Log.v("","i="+i+", "+parm[i]);
        }
        if (parm[1].equals("SETTINGS")) return; //ignore settings entry

        if (parm[1].equals(SMBSYNC_PROF_TYPE_REMOTE)) {//Remote
            String h_addr = "", h_name = "";
            if (parm[6].length() > 0) {
                if (parm[6].substring(0, 1).compareTo("0") >= 0 && parm[6].substring(0, 1).compareTo("9") <= 0) {
                    h_addr = parm[6];
                } else {
                    h_name = parm[6];
                }
            } else {
                h_addr = "";
                h_name = parm[9];
            }
//			Log.v("","h_addr="+h_addr+", h_name="+h_name);
            rem.add(createRemoteProfilelistItem(gp,
                    parm[0],//group
                    parm[2],//Name
                    parm[3],//Active
                    parm[8],//directory
                    parm[4],//user
                    parm[5],//pass
                    parm[7],//share
                    h_addr,//address
                    h_name,//hostname
                    parm[10],//port
                    parm[11],//Zip file name
                    Integer.parseInt(parm[12]),//Zip enc method
                    Integer.parseInt(parm[13]),//Zip enc key length
                    false));

        } else {
            if (parm[1].equals(SMBSYNC_PROF_TYPE_LOCAL)) {//Local
                lcl.add(createLocalProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Directory
                        parm[5],//Local mount point
                        parm[6],//Zip file name
                        Integer.parseInt(parm[7]),//Zip enc method
                        Integer.parseInt(parm[8]),//Zip enc key length
                        false));
            } else if (parm[1].equals(SMBSYNC_PROF_TYPE_SYNC)) {//Sync
                ArrayList<String> ff = new ArrayList<String>();
                ArrayList<String> df = new ArrayList<String>();
                if (list1.length() != 0) {
                    String[] fp = list1.split("\t");
                    for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
                } else ff.clear();
                if (list2.length() != 0) {
                    String[] dp = list2.split("\t");
                    for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
                } else df.clear();
                boolean mpd = true, conf = false, ujlm = false, nulm_remote = false;
                if (parm[9].equals("0")) mpd = false;
                if (parm[10].equals("1")) conf = true;
                if (parm[11].equals("1")) ujlm = true;
                if (parm[12].equals("1")) nulm_remote = true;
                boolean sync_empty_dir = false;
                if (parm[14].equals("1")) sync_empty_dir = true;
                boolean sync_hidden_dir = false;
                if (parm[15].equals("1")) sync_hidden_dir = true;
                boolean sync_hidden_file = false;
                if (parm[16].equals("1")) sync_hidden_file = true;
                boolean sync_sub_dir = false;
                if (parm[17].equals("1")) sync_sub_dir = true;
//				Log.v("","17="+parm[17]);
                sync.add(createSyncProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Sync type
                        parm[5],//Master type
                        parm[6],//Master name
                        parm[7],//Target type
                        parm[8],//Target name
                        ff,//File Filter
                        df,//Dir Filter
                        mpd,
                        conf,
                        ujlm,
                        nulm_remote,
                        parm[13],//Retry count
                        sync_empty_dir,
                        sync_hidden_dir, sync_hidden_file, sync_sub_dir, false,
                        parm[18],//Zip file name
                        Integer.parseInt(parm[19]),//Zip enc method
                        Integer.parseInt(parm[20]),//Zip enc key length
                        parm[21],//Last sync time
                        Integer.parseInt(parm[22]),//Last sync result
                        false));
            }
        }
    }

    ;

    static private void addProfileListVer8(GlobalParameters gp, String pl, ArrayList<ProfileListItem> sync,
                                           ArrayList<ProfileListItem> rem, ArrayList<ProfileListItem> lcl) {
        //Extract ArrayList<String> field
        String list1 = "", list2 = "", npl = "";
        if (pl.indexOf("[") >= 0) {
            // found first List
            list1 = pl.substring(pl.indexOf("[") + 1, pl.indexOf("]"));
            npl = pl.replace("[" + list1 + "]\t", "");
            if (npl.indexOf("[") >= 0) {
                // found second List
                list2 = npl.substring(npl.indexOf("[") + 1, npl.indexOf("]"));
                npl = npl.replace("[" + list2 + "]\t", "");
            }
        } else npl = pl;
//		Log.v("","pl="+pl);
//		String prof_group = npl.substring(0,11).trim();
//		String tmp_ps=npl.substring(12,npl.length());

        String[] tmp_pl = npl.split("\t");// {"type","name","active",options...};
        String[] parm = new String[100];
        for (int i = 0; i < 100; i++) parm[i] = "";
        for (int i = 0; i < tmp_pl.length; i++) {
            if (tmp_pl[i] == null) parm[i] = "";
            else {
                if (tmp_pl[i] == null) parm[i] = "";
                else parm[i] = convertToSpecChar(tmp_pl[i].trim());
            }
//			Log.v("","i="+i+", "+parm[i]);
        }
        if (parm[1].equals("SETTINGS")) return; //ignore settings entry

        if (parm[1].equals(SMBSYNC_PROF_TYPE_REMOTE)) {//Remote
            String h_addr = "", h_name = "";
            if (parm[6].length() > 0) {
                if (parm[6].substring(0, 1).compareTo("0") >= 0 && parm[6].substring(0, 1).compareTo("9") <= 0) {
                    h_addr = parm[6];
                } else {
                    h_name = parm[6];
                }
            } else {
                h_addr = "";
                h_name = parm[9];
            }
//			Log.v("","h_addr="+h_addr+", h_name="+h_name);
            rem.add(createRemoteProfilelistItem(gp,
                    parm[0],//group
                    parm[2],//Name
                    parm[3],//Active
                    parm[8],//directory
                    parm[4],//user
                    parm[5],//pass
                    parm[7],//share
                    h_addr,//address
                    h_name,//hostname
                    parm[10],//port
                    parm[11],//Zip file name
                    Integer.parseInt(parm[12]),//Zip enc method
                    Integer.parseInt(parm[13]),//Zip enc key length
                    false));

        } else {
            if (parm[1].equals(SMBSYNC_PROF_TYPE_LOCAL)) {//Local
                lcl.add(createLocalProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Directory
                        parm[5],//Local mount point
                        parm[6],//Zip file name
                        Integer.parseInt(parm[7]),//Zip enc method
                        Integer.parseInt(parm[8]),//Zip enc key length
                        false));
            } else if (parm[1].equals(SMBSYNC_PROF_TYPE_SYNC)) {//Sync
                ArrayList<String> ff = new ArrayList<String>();
                ArrayList<String> df = new ArrayList<String>();
                if (list1.length() != 0) {
                    String[] fp = list1.split("\t");
                    for (int i = 0; i < fp.length; i++) ff.add(convertToSpecChar(fp[i]));
                } else ff.clear();
                if (list2.length() != 0) {
                    String[] dp = list2.split("\t");
                    for (int i = 0; i < dp.length; i++) df.add(convertToSpecChar(dp[i]));
                } else df.clear();
                boolean mpd = true, conf = false, ujlm = false, nulm_remote = false;
                if (parm[9].equals("0")) mpd = false;
                if (parm[10].equals("1")) conf = true;
                if (parm[11].equals("1")) ujlm = true;
                if (parm[12].equals("1")) nulm_remote = true;
                boolean sync_empty_dir = false;
                if (parm[14].equals("1")) sync_empty_dir = true;
                boolean sync_hidden_dir = false;
                if (parm[15].equals("1")) sync_hidden_dir = true;
                boolean sync_hidden_file = false;
                if (parm[16].equals("1")) sync_hidden_file = true;
                boolean sync_sub_dir = false;
                if (parm[17].equals("1")) sync_sub_dir = true;

                boolean sync_use_remote_small_io_area = false;
                if (parm[18].equals("1")) sync_use_remote_small_io_area = true;

//				Log.v("","17="+parm[17]);
                sync.add(createSyncProfilelistItem(gp,
                        parm[0],//group
                        parm[2],//Name
                        parm[3],//Active
                        parm[4],//Sync type
                        parm[5],//Master type
                        parm[6],//Master name
                        parm[7],//Target type
                        parm[8],//Target name
                        ff,//File Filter
                        df,//Dir Filter
                        mpd,
                        conf,
                        ujlm,
                        nulm_remote,
                        parm[13],//Retry count
                        sync_empty_dir,
                        sync_hidden_dir, sync_hidden_file, sync_sub_dir, sync_use_remote_small_io_area,
                        parm[19],//Zip file name
                        Integer.parseInt(parm[20]),//Zip enc method
                        Integer.parseInt(parm[21]),//Zip enc key length
                        parm[22],//Last sync time
                        Integer.parseInt(parm[23]),//Last sync result
                        false));
            }
        }
    }

    ;

    static private String convertToSpecChar(String in) {
        if (in == null || in.length() == 0) return "";
        boolean cont = true;
        String out = in;
        while (cont) {
            if (out.indexOf("\u0001") >= 0) out = out.replace("\u0001", "[");
            else cont = false;
        }

        cont = true;
        while (cont) {
            if (out.indexOf("\u0002") >= 0) out = out.replace("\u0002", "]");
            else cont = false;
        }

        return out;
    }

    ;

    static private ProfileListItem createSyncProfilelistItem(GlobalParameters gp,
                                                             String prof_group, String prof_name,
                                                             String prof_act, String prof_syncopt, String prof_master_typ, String prof_master,
                                                             String prof_target_typ, String prof_target,
                                                             ArrayList<String> ff, ArrayList<String> df, boolean prof_mpd,
                                                             boolean prof_conf, boolean prof_ujlm, boolean nulm_remote,
                                                             String retry_count, boolean sync_empty_dir, boolean sync_hidden_file,
                                                             boolean sync_hidden_dir, boolean sync_sub_dir, boolean sync_use_remote_small_io_area,
                                                             String zip_file_name, int zip_enc_method, int zip_enc_key_length,
                                                             String last_sync_time, int last_sync_result,
                                                             boolean isChk) {
        return new ProfileListItem(prof_group, SMBSYNC_PROF_TYPE_SYNC, prof_name, prof_act,
                prof_syncopt,
                prof_master_typ,
                prof_master,
                prof_target_typ,
                prof_target,
                ff,
                df,
                prof_mpd,
                prof_conf,
                prof_ujlm,
                nulm_remote,
                retry_count,
                sync_empty_dir,
                sync_hidden_file,
                sync_hidden_dir,
                sync_sub_dir,
                sync_use_remote_small_io_area,
                zip_file_name, zip_enc_method, zip_enc_key_length,
                last_sync_time, last_sync_result,
                isChk);
    }

    ;

    static private ProfileListItem createRemoteProfilelistItem(GlobalParameters gp, String prof_group, String prof_name,
                                                               String prof_act, String prof_dir, String prof_user, String prof_pass,
                                                               String prof_share, String prof_addr, String prof_host, String prof_port,
                                                               String zip_file_name, int zip_enc_method, int zip_enc_key_length,
                                                               boolean isChk) {
        return new ProfileListItem(prof_group, SMBSYNC_PROF_TYPE_REMOTE, prof_name, prof_act,
                prof_user,
                prof_pass,
                prof_addr,
                prof_host,
                prof_port,
                prof_share,
                prof_dir,
                zip_file_name, zip_enc_method, zip_enc_key_length,
                isChk);
    }

    ;

    static private ProfileListItem createLocalProfilelistItem(GlobalParameters gp, String prof_group,
                                                              String prof_name, String prof_act, String prof_dir,
                                                              String prof_lmp,
                                                              String zip_file_name, int zip_enc_method, int zip_enc_key_length,
                                                              boolean isChk) {
        return new ProfileListItem(prof_group, SMBSYNC_PROF_TYPE_LOCAL,
                prof_name, prof_act, prof_lmp, prof_dir,
                zip_file_name, zip_enc_method, zip_enc_key_length,
                isChk);
    }

    ;

    @SuppressWarnings("unused")
    static private class ProfileListItem implements Serializable, Comparable<ProfileListItem> {
        private static final long serialVersionUID = 1L;
        private String profileGroup = "";
        private String profileType = "";
        private String profileName = "";
        private String profileActive = "";
        private boolean profileChk = false;
        private String profileDir = "";
        private String profileShare = "";
        private String profileAddr = "";
        private String profileHostname = "";
        private String profilePort = "";
        private String profileUser = "";
        private String profilePass = "";
        private String profileSyncType = "";
        private String profileMasterType = "";
        private String profileMasterName = "";
        private String profileTargetType = "";
        private String profileTargetName = "";
        private String profileLocalMountPoint = "";
        private boolean profileMasterDirFileProcess = true;
        private boolean profileConfirm = true;
        private boolean profileForceLastModifiedUseSmbsync = true;
        private boolean profileNotUsedLastModifiedForRemote = false;
        private ArrayList<String> profileFileFilter = new ArrayList<String>();
        private ArrayList<String> profileDirFilter = new ArrayList<String>();

        private String profileRetryCount = "0";
        private boolean profileSyncEmptyDir = false;
        private boolean profileSyncHiddenFile = true;
        private boolean profileSyncHiddenDir = true;
        private boolean profileSyncSubDir = true;
        private boolean profileSyncUseRemoteSmallIoArea = false;

        private String profileLastSyncTime = "";
        private int profileLastSyncResult = 0;

        private String profileLocalZipFileName = "";
        private int profileLocalZipEncMethod = 0, profileLocalZipAesStrength = 256;
        private String profileRemoteZipFileName = "";
        private int profileRemoteZipEncMethod = 0, profileRemoteZipAesStrength = 256;
        private String profileSyncZipFileName = "";
        private int profileSyncZipEncMethod = 0, profileSyncZipAesStrength = 256;

        //Not save variables
        private boolean profileSyncRunning = false;

        // constructor for local profile
        public ProfileListItem(String pfg, String pft, String pfn,
                               String pfa, String pf_mp, String pf_dir,
                               String zip_file_name, int zip_enc_method, int zip_enc_key_length,
                               boolean ic) {
            profileGroup = pfg;
            profileType = pft;
            profileName = pfn;
            profileActive = pfa;
            profileLocalMountPoint = pf_mp;
            profileDir = pf_dir;
            profileLocalZipFileName = zip_file_name;
            profileLocalZipEncMethod = zip_enc_method;
            profileLocalZipAesStrength = zip_enc_key_length;
            profileChk = ic;
        }

        ;

        // constructor for remote profile
        public ProfileListItem(String pfg, String pft, String pfn, String pfa,
                               String pf_user, String pf_pass, String pf_addr, String pf_hostname,
                               String pf_port, String pf_share, String pf_dir,
                               String zip_file_name, int zip_enc_method, int zip_enc_key_length,
                               boolean ic) {
            profileGroup = pfg;
            profileType = pft;
            profileName = pfn;
            profileActive = pfa;
            profileDir = pf_dir;
            profileUser = pf_user;
            profilePass = pf_pass;
            profileShare = pf_share;
            profileAddr = pf_addr;
            profilePort = pf_port;
            profileHostname = pf_hostname;
            profileRemoteZipFileName = zip_file_name;
            profileRemoteZipEncMethod = zip_enc_method;
            profileRemoteZipAesStrength = zip_enc_key_length;

            profileChk = ic;
        }

        ;

        // constructor for sync profile
        public ProfileListItem(String pfg, String pft, String pfn, String pfa,
                               String pf_synctype, String pf_master_type, String pf_master_name,
                               String pf_target_type, String pf_target_name,
                               ArrayList<String> ff, ArrayList<String> df, boolean master_dir_file_process, boolean confirm,
                               boolean jlm, boolean nulm_remote, String retry_count, boolean sync_empty_dir,
                               boolean sync_hidden_dir, boolean sync_hidden_file, boolean sync_sub_dir, boolean sync_remote_small_ioarea,
                               String zip_file_name, int zip_enc_method, int zip_enc_key_length,
                               String last_sync_time, int last_sync_result,
                               boolean ic) {
            profileGroup = pfg;
            profileType = pft;
            profileName = pfn;
            profileActive = pfa;
            profileSyncType = pf_synctype;
            profileMasterType = pf_master_type;
            profileMasterName = pf_master_name;
            profileTargetType = pf_target_type;
            profileTargetName = pf_target_name;
            profileFileFilter = ff;
            profileDirFilter = df;
            profileMasterDirFileProcess = master_dir_file_process;
            profileConfirm = confirm;
            profileForceLastModifiedUseSmbsync = jlm;
            profileChk = ic;
            profileNotUsedLastModifiedForRemote = nulm_remote;
            profileRetryCount = retry_count;
            profileSyncEmptyDir = sync_empty_dir;
            profileSyncHiddenFile = sync_hidden_file;
            profileSyncHiddenDir = sync_hidden_dir;
            profileSyncSubDir = sync_sub_dir;
            profileSyncUseRemoteSmallIoArea = sync_remote_small_ioarea;

            profileSyncZipFileName = zip_file_name;
            profileSyncZipEncMethod = zip_enc_method;
            profileSyncZipAesStrength = zip_enc_key_length;

            profileLastSyncTime = last_sync_time;
            profileLastSyncResult = last_sync_result;
        }

        ;

        public ProfileListItem() {
        }

        public String getProfileGroup() {
            return profileGroup;
        }

        public String getProfileName() {
            return profileName;
        }

        public String getProfileType() {
            return profileType;
        }

        public String getProfileActive() {
            return profileActive;
        }

        public boolean isProfileActive() {
            return profileActive.equals(SMBSYNC_PROF_ACTIVE) ? true : false;
        }

        public String getRemoteUserID() {
            return profileUser;
        }

        public String getRemotePassword() {
            return profilePass;
        }

        public String getRemoteShareName() {
            return profileShare;
        }

        public String getDirectoryName() {
            return profileDir;
        }

        public String getRemoteAddr() {
            return profileAddr;
        }

        public String getRemotePort() {
            return profilePort;
        }

        public String getRemoteHostname() {
            return profileHostname;
        }

        public String getSyncType() {
            return profileSyncType;
        }

        public String getMasterType() {
            return profileMasterType;
        }

        public String getMasterName() {
            return profileMasterName;
        }

        public String getTargetType() {
            return profileTargetType;
        }

        public String getTargetName() {
            return profileTargetName;
        }

        public ArrayList<String> getFileFilter() {
            return profileFileFilter;
        }

        public ArrayList<String> getDirFilter() {
            return profileDirFilter;
        }

        public boolean isMasterDirFileProcess() {
            return profileMasterDirFileProcess;
        }

        public boolean isConfirmRequired() {
            return profileConfirm;
        }

        public boolean isForceLastModifiedUseSmbsync() {
            return profileForceLastModifiedUseSmbsync;
        }

        public boolean isChecked() {
            return profileChk;
        }

        public boolean isNotUseLastModifiedForRemote() {
            return profileNotUsedLastModifiedForRemote;
        }

        public void setNotUseLastModifiedForRemote(boolean p) {
            profileNotUsedLastModifiedForRemote = p;
        }

        public void setProfileGroup(String p) {
            profileGroup = p;
        }

        public void setProfileName(String p) {
            profileName = p;
        }

        public void setProfileType(String p) {
            profileType = p;
        }

        public void setProfileActive(String p) {
            profileActive = p;
        }

        public void setRemoteUserID(String p) {
            profileUser = p;
        }

        public void setRemotePassword(String p) {
            profilePass = p;
        }

        public void setRemoteShareName(String p) {
            profileShare = p;
        }

        public void setDirectoryName(String p) {
            profileDir = p;
        }

        public void setRemoteAddr(String p) {
            profileAddr = p;
        }

        public void setRemotePort(String p) {
            profilePort = p;
        }

        public void setRemoteHostname(String p) {
            profileHostname = p;
        }

        public void setSyncType(String p) {
            profileSyncType = p;
        }

        public void setMasterType(String p) {
            profileMasterType = p;
        }

        public void setMasterName(String p) {
            profileMasterName = p;
        }

        public void setTargetType(String p) {
            profileTargetType = p;
        }

        public void setTargetName(String p) {
            profileTargetName = p;
        }

        public void setFileFilter(ArrayList<String> p) {
            profileFileFilter = p;
        }

        public void setDirFilter(ArrayList<String> p) {
            profileDirFilter = p;
        }

        public void setMasterDirFileProcess(boolean p) {
            profileMasterDirFileProcess = p;
        }

        public void setConfirmRequired(boolean p) {
            profileConfirm = p;
        }

        public void setForceLastModifiedUseSmbsync(boolean p) {
            profileForceLastModifiedUseSmbsync = p;
        }

        public void setChecked(boolean p) {
            profileChk = p;
        }

        public void setLocalMountPoint(String p) {
            profileLocalMountPoint = p;
        }

        public String getLocalMountPoint() {
            return profileLocalMountPoint;
        }

        public String getRetryCount() {
            return profileRetryCount;
        }

        public void setRetryCount(String p) {
            profileRetryCount = p;
        }

        public boolean isSyncEmptyDirectory() {
            return profileSyncEmptyDir;
        }

        public void setSyncEmptyDirectory(boolean p) {
            profileSyncEmptyDir = p;
        }

        public boolean isSyncHiddenFile() {
            return profileSyncHiddenFile;
        }

        public void setSyncHiddenFile(boolean p) {
            profileSyncHiddenFile = p;
        }

        public boolean isSyncHiddenDirectory() {
            return profileSyncHiddenDir;
        }

        public void setSyncHiddenDirectory(boolean p) {
            profileSyncHiddenDir = p;
        }

        public boolean isSyncSubDirectory() {
            return profileSyncSubDir;
        }

        public void setSyncSubDirectory(boolean p) {
            profileSyncSubDir = p;
        }

        public boolean isSyncUseRemoteSmallIoArea() {
            return profileSyncUseRemoteSmallIoArea;
        }

        public void setSyncRemoteSmallIoArea(boolean p) {
            profileSyncUseRemoteSmallIoArea = p;
        }

        public void setLastSyncTime(String p) {
            profileLastSyncTime = p;
        }

        public void setLastSyncResult(int p) {
            profileLastSyncResult = p;
        }

        public String getLastSyncTime() {
            return profileLastSyncTime;
        }

        public int getLastSyncResult() {
            return profileLastSyncResult;
        }

        public void setLocalZipFileName(String p) {
            profileLocalZipFileName = p;
        }

        public void setLocalZipEncMethod(int p) {
            profileLocalZipEncMethod = p;
        }

        public void setLocalZipAesKeyLength(int p) {
            profileLocalZipAesStrength = p;
        }

        public String getLocalZipFileName() {
            return profileLocalZipFileName;
        }

        public int getLocalZipEncMethod() {
            return profileLocalZipEncMethod;
        }

        public int getLocalZipAesKeyLength() {
            return profileLocalZipAesStrength;
        }

        public void setRemoteZipFileName(String p) {
            profileRemoteZipFileName = p;
        }

        public void setRemoteZipEncMethod(int p) {
            profileRemoteZipEncMethod = p;
        }

        public void setRemoteZipAesKeyLength(int p) {
            profileRemoteZipAesStrength = p;
        }

        public String getRemoteZipFileName() {
            return profileRemoteZipFileName;
        }

        public int getRemoteZipEncMethod() {
            return profileRemoteZipEncMethod;
        }

        public int getRemoteZipAesKeyLength() {
            return profileRemoteZipAesStrength;
        }

        public void setSyncZipFileName(String p) {
            profileSyncZipFileName = p;
        }

        public void setSyncZipEncMethod(int p) {
            profileSyncZipEncMethod = p;
        }

        public void setSyncZipAesKeyLength(int p) {
            profileSyncZipAesStrength = p;
        }

        public String getSyncZipFileName() {
            return profileSyncZipFileName;
        }

        public int getSyncZipEncMethod() {
            return profileSyncZipEncMethod;
        }

        public int getSyncZipAesKeyLength() {
            return profileSyncZipAesStrength;
        }

        public void setSyncRunning(boolean p) {
            profileSyncRunning = p;
        }

        public boolean isSyncRunning() {
            return profileSyncRunning;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public int compareTo(ProfileListItem o) {
            if (this.profileName != null)
                return this.profileName.toLowerCase(Locale.getDefault()).compareTo(o.getProfileName().toLowerCase());
//					return this.filename.toLowerCase().compareTo(o.getName().toLowerCase()) * (-1);
            else
                throw new IllegalArgumentException();
        }
    }

}


