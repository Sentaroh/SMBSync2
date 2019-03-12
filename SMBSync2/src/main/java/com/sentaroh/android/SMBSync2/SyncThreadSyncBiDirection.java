package com.sentaroh.android.SMBSync2;

import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.sentaroh.android.SMBSync2.SyncThread.SyncThreadWorkArea;

import static com.sentaroh.android.SMBSync2.Constants.APP_SPECIFIC_DIRECTORY;

public class SyncThreadSyncBiDirection {

    static public int syncTwowayInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, String to_path) {
        File mf=new File(from_path);
        File tf=new File(to_path);
        int sync_result=syncFileTwowayInternalToInternal(stwa, sti, from_path, from_path, mf, to_path, to_path, tf);
        return sync_result;
    }

    static private ArrayList<File> getFileList(File file) {
        File[] fl=file.listFiles();
        ArrayList<File> s_fl=new ArrayList<File>();
        if (fl!=null) {
            for(File item:fl) s_fl.add(item);
            Collections.sort(s_fl, new Comparator<File>(){
                @Override
                public int compare(File o1, File o2) {
                    return o1.getPath().compareToIgnoreCase(o2.getPath());
                }
            });

        }
        return s_fl;
    }


    static private int syncFileTwowayInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                                        String pair_a_base, String pair_a_path, File pair_a_file, String pair_b_base, String pair_b_path, File pair_b_file) {
        if (stwa.gp.settingDebugLevel >= 2)
            stwa.util.addDebugMsg(2, "I", CommonUtilities.getExecutedMethodName() + " entered, from=" + pair_a_path + ", to=" + pair_b_path);
        int sync_result = 0;
        stwa.jcifsNtStatusCode=0;
        try {
            if (pair_a_file.isDirectory() && pair_b_file.isDirectory()) {
                ArrayList<File> pair_a_file_list= getFileList(pair_a_file);
                ArrayList<File> pair_b_file_list= getFileList(pair_b_file);
                boolean exit=false;
                int pair_a_cnt=0, pair_b_cnt=0;
                while(!exit) {
                    if (pair_a_cnt<pair_a_file_list.size() && pair_b_cnt<pair_b_file_list.size()) {
                        File pair_a_child_file=pair_a_file_list.get(pair_a_cnt);
                        String pair_a_child_file_path=pair_a_child_file.getPath().replace(pair_a_base, "");
                        File pair_b_child_file=pair_b_file_list.get(pair_b_cnt);
                        String pair_b_child_file_path=pair_b_child_file.getPath().replace(pair_b_base, "");

                        if (pair_a_child_file_path.compareToIgnoreCase(pair_b_child_file_path)==0) {
                            //Same name
                            if (pair_a_child_file.isDirectory() && pair_b_child_file.isDirectory()) {
                                //下位のディレクトリーを処理
                                syncFileTwowayInternalToInternal(stwa, sti,
                                        pair_a_base, pair_a_child_file.getPath(), pair_a_child_file, pair_b_base, pair_b_child_file.getPath(), pair_b_child_file);
                                pair_a_cnt++;
                                pair_b_cnt++;
                            } else if (pair_a_child_file.isDirectory() && pair_b_child_file.isFile()) {
                                //ディレクトリーとファイルの名前が同じため同期不可
                                stwa.util.addDebugMsg(1,"E","ディレクトリーとファイルの名前が同じため同期不可");
                                stwa.util.addDebugMsg(1,"E","from path="+pair_a_path+", to path="+pair_b_path);
                                break;
                            } else if (pair_a_child_file.isFile() && pair_b_child_file.isDirectory()) {
                                //ディレクトリーとファイルの名前が同じため同期不可
                                stwa.util.addDebugMsg(1,"E","ディレクトリーとファイルの名前が同じため同期不可");
                                stwa.util.addDebugMsg(1,"E","from path="+pair_a_path+", to path="+pair_b_path);
                                break;
                            } else if (pair_a_child_file.isFile() == pair_b_child_file.isFile()) {
                                //Fileの同期
                                stwa.util.addDebugMsg(1,"I","Perform sync file");
                                copyFileInternalToInternal(stwa, sti, pair_a_child_file.getPath(), pair_a_child_file, pair_b_child_file.getPath());
                                pair_a_cnt++;
                                pair_b_cnt++;
                            }
                        } else if (pair_a_child_file_path.compareToIgnoreCase(pair_b_child_file_path)>0) {
                            //ターゲットディレクトリーをマスターにコピー
                            sync_result= copyDirectoryInternalToInternal(stwa, sti, pair_b_child_file, new File(pair_a_base+"/"+pair_b_child_file_path));
                            stwa.util.addDebugMsg(1,"I","Perform sync target directory 1");
                            pair_b_cnt++;
                        } else if (pair_a_child_file_path.compareToIgnoreCase(pair_b_child_file_path)<0) {
                            //マスターディレクトリーをターゲットにコピー
                            sync_result= copyDirectoryInternalToInternal(stwa, sti, pair_a_child_file, new File(pair_a_base+"/"+pair_a_child_file_path));
                            stwa.util.addDebugMsg(1,"I","Perform sync master directory 1");
                            pair_a_cnt++;
                        }
                    } else {
                        if (pair_a_cnt<pair_a_file_list.size()) {
                            //マスターをターゲットにコピー
                            if (pair_a_file.isDirectory()) {
                                sync_result= copyDirectoryInternalToInternal(stwa, sti, pair_a_file_list.get(pair_a_cnt), pair_b_file);
                                stwa.util.addDebugMsg(1,"I","Perform sync master directory 2");
                            } else {
                                //Fileの同期
                                stwa.util.addDebugMsg(1,"I","Perform sync master file");
                            }
                            pair_a_cnt++;
                        } else if (pair_b_cnt<pair_b_file_list.size()) {
                            //ターゲットをマスターにコピー
                            if (pair_b_file.isDirectory()) {
                                sync_result= copyDirectoryInternalToInternal(stwa, sti, pair_b_file_list.get(pair_b_cnt), pair_a_file);
                                stwa.util.addDebugMsg(1,"I","Perform sync target directory 2");
                            } else {
                                //Fileの同期
                                stwa.util.addDebugMsg(1,"I","Perform sync target file");
                            }
                            pair_b_cnt++;
                        } else {
                            break;
                        }
                    }
                }
            } else {
                //ディレクトリーとファイルの名前が同じため同期不可
            }
        } catch (Exception e) {
            putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
            return SyncTaskItem.SYNC_STATUS_ERROR;
        }

        return sync_result;
    };

    static private int copyDirectoryInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, File in_dir, File out_dir) {
        stwa.util.addDebugMsg(1,"I","copyDirectoryInternalToInternal entered, in="+in_dir.getPath()+", out="+out_dir.getPath());
        if (in_dir.exists()) {
            if (in_dir.isDirectory()) {
                if (!sti.isSyncOptionSyncSubDirectory()) {
                    stwa.util.addDebugMsg(1,"I","copyDirectoryInternalToInternal sync aborted, because sync sub directory disabled");
                    return 0;
                }
                File[] file_list=in_dir.listFiles();
                if (sti.isSyncOptionSyncEmptyDirectory()) if (!out_dir.exists()) out_dir.mkdirs();
                else {
                    stwa.util.addDebugMsg(1,"I","copyDirectoryInternalToInternal sync aborted, because sync empty directory disabled");
                    return 0;
                }
                for(File child_file:file_list) {
                    if (child_file.isDirectory()) {
                        copyDirectoryInternalToInternal(stwa, sti, child_file, new File(out_dir.getPath()+"/"+child_file.getName()));
                    } else {
                        copyFileInternalToInternal(stwa, sti,child_file.getPath(), child_file, out_dir.getPath()+"/"+child_file.getName());
                    }
                }
            } else {
                copyFileInternalToInternal(stwa, sti, in_dir.getPath(), in_dir, out_dir.getPath()+"/"+in_dir.getName());
            }
        }
        return 0;
    }


    final static int FILE_CHANGED_REASON_NO_DIFFERENCE=0;
    final static int FILE_CHANGED_REASON_DIFFERENT_MASTER_NEWER=1;
    final static int FILE_CHANGED_REASON_DIFFERENT_MASTER_OLDER=2;
    static private int isFileChenged(SyncThreadWorkArea stwa, SyncTaskItem sti,
                                     String mf_path, long mf_file_size, long mf_file_last_modified,
                                     String tf_path, long tf_file_size, long tf_file_last_modified) {
        int sync_required=0;
        int allowable_time=sti.getSyncOptionDifferentFileAllowableTime();
        long last_modified_diff=mf_file_last_modified-tf_file_last_modified;
        if (Math.abs(last_modified_diff)>allowable_time) {
            if (allowable_time<0) sync_required=FILE_CHANGED_REASON_DIFFERENT_MASTER_OLDER;
            else sync_required=FILE_CHANGED_REASON_DIFFERENT_MASTER_NEWER;
        } else {
            sync_required=FILE_CHANGED_REASON_NO_DIFFERENCE;
        }

        return sync_required;
    }


    private final static int TWOWAY_SYNC_RULE_COPY_ASK_USER=0;
    private final static int TWOWAY_SYNC_RULE_COPY_NEWER=1;
    private final static int TWOWAY_SYNC_RULE_COPY_OLDER=2;
    private final static int TWOWAY_SYNC_RULE_KEEP_REPLACE_FILE=3;

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

    static private int copyFileInternalToInternal(SyncThreadWorkArea stwa, SyncTaskItem sti, String from_path, File mf, String to_path) {
        stwa.util.addDebugMsg(1,"I","copyFileInternalToInternal entered, in="+from_path+", out="+to_path);
        int sync_result=SyncTaskItem.SYNC_STATUS_SUCCESS;
        String dest_path=to_path;
        String temp_path = stwa.gp.internalRootDirectory+"/"+APP_SPECIFIC_DIRECTORY+"/files/temp_file.tmp";
        File of=new File(temp_path);
        InputStream fis=null;
        OutputStream fos=null;
        try {
            fis=new FileInputStream(mf);
            fos=new FileOutputStream(of);
            sync_result=copyFile(stwa, sti, dest_path, mf.getName(), mf.length(), fos, fis);
            if (sync_result==SyncTaskItem.SYNC_STATUS_SUCCESS) {
                of.setLastModified(mf.lastModified());
                File df=new File(dest_path);
                boolean df_exists=df.exists();
                if (df_exists) df.delete();
                of.renameTo(df);

                String tmsg = df_exists ? stwa.msgs_mirror_task_file_replaced : stwa.msgs_mirror_task_file_copied;
                SyncThread.showMsg(stwa, false, sti.getSyncTaskName(), "I", dest_path, mf.getName(), tmsg);
            } else {
                if (of.exists()) of.delete();
            }
        } catch(IOException e) {
            if (fis!=null) try {fis.close();} catch(IOException e1) {}
            if (fos!=null) try {fos.close();} catch(IOException e1) {}
            if (of.exists()) of.delete();
            putExceptionMessage(stwa, e.getStackTrace(), e.getMessage());
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

    static private SyncFileInfoItem getSyncFileInfo(SyncThreadWorkArea stwa, SyncTaskItem sti, String fpath) {
        SyncFileInfoItem srch=new SyncFileInfoItem();
        srch.filePath =fpath;
        int idx=Collections.binarySearch(stwa.currSyncFileInfoList, srch, new Comparator<SyncFileInfoItem>() {
            @Override
            public int compare(SyncFileInfoItem o1, SyncFileInfoItem o2) {
                return o1.filePath.compareToIgnoreCase(o2.filePath);
            }
        });
        SyncFileInfoItem result=null;
        if (idx<0) {
            for(SyncFileInfoItem item:stwa.newSyncFileInfoList) {
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

    static private void updateSyncFileInfo(SyncThreadWorkArea stwa, SyncTaskItem sti, File mf, File tf) {
        updateSyncFileInfo(stwa, sti, mf.getPath(), System.currentTimeMillis(), mf.length(), mf.lastModified());
        updateSyncFileInfo(stwa, sti, tf.getPath(), System.currentTimeMillis(), tf.length(), tf.lastModified());
    }

    static private void updateSyncFileInfo(SyncThreadWorkArea stwa, SyncTaskItem sti, String fpath,
                                           long sync_time, long file_size, long file_last_modified) {
        SyncFileInfoItem srch=new SyncFileInfoItem();
        srch.filePath =fpath;
        int idx=Collections.binarySearch(stwa.currSyncFileInfoList, srch, new Comparator<SyncFileInfoItem>() {
            @Override
            public int compare(SyncFileInfoItem o1, SyncFileInfoItem o2) {
                return o1.filePath.compareToIgnoreCase(o2.filePath);
            }
        });
        SyncFileInfoItem result=null;
        if (idx<0) {
            for(SyncFileInfoItem item:stwa.newSyncFileInfoList) {
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
            SyncFileInfoItem new_item=new SyncFileInfoItem();
            new_item.fileLastModified =file_last_modified;
            new_item.fileSize =file_size;
            new_item.syncTime=sync_time;
            stwa.newSyncFileInfoList.add(new_item);
        }
    }

    static public final String TWOWAY_SYNC_FILE_MANAGEMENT_FILE_NAME=".twoway_sync_file_list";

    private static final String SYNC_FILE_LIST_TAG_DOCUMENT ="sync_file_list";
    private static final String SYNC_FILE_LIST_TAG_DOCUMENT_VERSION="version";
    private static final String SYNC_FILE_LIST_VALUE_DOCUMENT_VERSION="1.0.1";
    private static final String SYNC_FILE_LIST_TAG_FILE="file";
    private static final String SYNC_FILE_LIST_TAG_PAIR_A_FILE_PATH="path";
    private static final String SYNC_FILE_LIST_TAG_FILE_REFERENCED ="referenced";
    private static final String SYNC_FILE_LIST_TAG_FILE_UPDATED="updated";
    private static final String SYNC_FILE_LIST_TAG_PAIR_A_FILE_LENGTH="length";
    private static final String SYNC_FILE_LIST_TAG_PAIR_A_FILE_LAST_MODIFIED="last_modified";

    static public void saveSyncFileInfoList(SyncThreadWorkArea stwa, SyncTaskItem sti) {
        if (stwa.newSyncFileInfoList.size()>0) {//merge current list
            stwa.currSyncFileInfoList.addAll(stwa.newSyncFileInfoList);
            Collections.sort(stwa.currSyncFileInfoList, new Comparator<SyncFileInfoItem>() {
                @Override
                public int compare(SyncFileInfoItem o1, SyncFileInfoItem o2) {
                    return o1.filePath.compareToIgnoreCase(o2.filePath);
                }
            });
            stwa.newSyncFileInfoList.clear();
        }

        try {
            FileOutputStream fos = new FileOutputStream(new File(stwa.gp.settingMgtFileDir+"/"+TWOWAY_SYNC_FILE_MANAGEMENT_FILE_NAME), false);
            BufferedOutputStream bos = new BufferedOutputStream(fos, 1024 * 1024);
            ZipOutputStream zos = new ZipOutputStream(bos);
            ZipEntry ze = new ZipEntry("list.txt");
            zos.putNextEntry(ze);
            OutputStreamWriter osw = new OutputStreamWriter(zos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw, 1024 * 1024 * 4);

            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbuilder = dbfactory.newDocumentBuilder();

            Document main_document = dbuilder.newDocument();
            Element config_tag = main_document.createElement(SYNC_FILE_LIST_TAG_DOCUMENT);
            config_tag.setAttribute(SYNC_FILE_LIST_TAG_DOCUMENT_VERSION, SYNC_FILE_LIST_VALUE_DOCUMENT_VERSION);
//                    root.appendChild(document.createTextNode("1.0.0"));

            for(SyncFileInfoItem item:stwa.currSyncFileInfoList) {
                Element server_tag = main_document.createElement(SYNC_FILE_LIST_TAG_FILE);
                server_tag.setAttribute(SYNC_FILE_LIST_TAG_PAIR_A_FILE_PATH, item.filePath);
                config_tag.appendChild(server_tag);

                server_tag.setAttribute(SYNC_FILE_LIST_TAG_FILE_REFERENCED, item.referenced?"true":"false");
                server_tag.setAttribute(SYNC_FILE_LIST_TAG_FILE_UPDATED, item.updated?"true":"false");
                server_tag.setAttribute(SYNC_FILE_LIST_TAG_PAIR_A_FILE_LENGTH, String.valueOf(item.fileSize));
                server_tag.setAttribute(SYNC_FILE_LIST_TAG_PAIR_A_FILE_LAST_MODIFIED, String.valueOf(item.fileSize));
            }

            main_document.appendChild(config_tag);

            bw.flush();
            bw.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
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

            ArrayList<SyncFileInfoItem> fl=new ArrayList<SyncFileInfoItem>();

            XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(new BufferedReader(isr));
            int eventType = xpp.getEventType();
            String config_ver="";
            while(eventType != XmlPullParser.END_DOCUMENT){
                switch(eventType){
                    case XmlPullParser.START_DOCUMENT:
                        stwa.util.addDebugMsg(2,"I","loadSyncFileInfoList Start Document");
                        break;
                    case XmlPullParser.START_TAG:
                        stwa.util.addDebugMsg(2,"I","loadSyncFileInfoList Start Tag="+xpp.getName());
                        if (xpp.getName().equals(SYNC_FILE_LIST_TAG_DOCUMENT)) {
                            if (xpp.getAttributeCount()==1) {
                                config_ver=xpp.getAttributeValue(0);
                                stwa.util.addDebugMsg(2,"I","loadSyncFileInfoList Version="+xpp.getAttributeValue(0));
                            }
                        } else if (xpp.getName().equals(SYNC_FILE_LIST_TAG_FILE)) {
                            SyncFileInfoItem item= createSyncFileInfoItemFromXmlTag(stwa, xpp);
                            item.version=config_ver;
                            fl.add(item);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        stwa.util.addDebugMsg(2,"I","loadSyncFileInfoList Text=" + xpp.getText()+", name="+xpp.getName());
                        break;
                    case XmlPullParser.END_TAG:
                        stwa.util.addDebugMsg(2,"I", "loadSyncFileInfoList End Tag="+xpp.getName());
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        stwa.util.addDebugMsg(2,"I", "loadSyncFileInfoList End Document="+xpp.getName());
                        break;
                }
                eventType = xpp.next();
            }
            stwa.util.addDebugMsg(2,"I","loadSyncFileInfoList End of document");

            fis.close();

            stwa.currSyncFileInfoList=fl;
            stwa.newSyncFileInfoList.clear();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

    }

    static private SyncFileInfoItem createSyncFileInfoItemFromXmlTag(SyncThreadWorkArea stwa, XmlPullParser xpp) {
        SyncFileInfoItem item=new SyncFileInfoItem();
        int ac=xpp.getAttributeCount();
        for(int i=0;i<ac;i++) {
            stwa.util.addDebugMsg(2,"I","createSyncFileInfoItemFromXmlTag Attribute="+xpp.getAttributeName(i)+", Value="+xpp.getAttributeValue(i));
            if (xpp.getAttributeName(i).equals(SYNC_FILE_LIST_TAG_PAIR_A_FILE_PATH)) {item.filePath =xpp.getAttributeValue(i);}
            else if (xpp.getAttributeName(i).equals(SYNC_FILE_LIST_TAG_PAIR_A_FILE_LENGTH)) {item.fileSize =Long.parseLong(xpp.getAttributeValue(i));}
            else if (xpp.getAttributeName(i).equals(SYNC_FILE_LIST_TAG_PAIR_A_FILE_LAST_MODIFIED)) {item.fileLastModified =Long.parseLong(xpp.getAttributeValue(i));}
            else if (xpp.getAttributeName(i).equals(SYNC_FILE_LIST_TAG_FILE_REFERENCED)) {item.referenced=xpp.getAttributeValue(i).equals("true");}
            else if (xpp.getAttributeName(i).equals(SYNC_FILE_LIST_TAG_FILE_UPDATED)) {item.updated=xpp.getAttributeValue(i).equals("true");}

        }
        return item;
    }


}

class SyncFileInfoItem {
    public boolean referenced=false;
    public boolean updated=false;
    public long syncTime=0L;

    public String version ="1.0.0";
    
    public String filePath ="";
    public long fileSize =0L;
    public long fileLastModified =0L;
    
    public SyncFileInfoItem() {}

    public SyncFileInfoItem(boolean ref, boolean upd, long sync_time,
                            String fp, long file_size, long last_modified) {
        syncTime=sync_time;
        referenced=ref;
        updated=upd;
        
        filePath =fp;
        fileSize =file_size;
        fileLastModified =last_modified;
    }

}

