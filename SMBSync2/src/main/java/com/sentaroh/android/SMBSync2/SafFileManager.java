package com.sentaroh.android.SMBSync2;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.sentaroh.android.Utilities.LocalMountPoint;
import com.sentaroh.android.Utilities.SafFile;


@SuppressLint("SdCardPath")
public class SafFileManager {
	public static final String REMOVABLE_UUID_KEY="removable_tree_uuid_key";
	
	private static final String APPLICATION_TAG="SafFileManager";

	public class SafFileItem {
		public String storageUuid="";
		public boolean storageTypeSdcard=false;
		public boolean storageIsMounted=false;
		public SafFile storageRootFile=null;
		public String storageRootDirectory="";
	}
	
	private boolean mDebugEnabled=false;
	private Context mContext=null;
	private ArrayList<SafFileItem> removableStorageList=new ArrayList<SafFileItem>();
	
	public final static String UNKNOWN_USB_FS_DIRECTORY="/usb_unknown";
	private String usbFsRootDirectory=UNKNOWN_USB_FS_DIRECTORY;
	public final static String UNKNOWN_SDCARD_DIRECTORY="/sdcard_unknown";
	private String sdcardRootDirectory=UNKNOWN_SDCARD_DIRECTORY;

	private String msg_area="";
	
	public String getSafDebugMsg() {return msg_area;}
	
	public SafFileManager(Context c, boolean debug) {
		mContext=c;
		setDebugEnabled(debug);
		getRemovableStoragePaths(mContext, mDebugEnabled);
		loadSafFileList();
	};

	public void setDebugEnabled(boolean enabled) {
		mDebugEnabled=enabled;
	}
	
	public static boolean isSdcardMountPointExisted(Context context, boolean debug) {
		boolean result=false;
		if (Build.VERSION.SDK_INT>=23) {
			String[] rsvp=getRemovableStoragePaths(context, debug);
			for(String vp:rsvp) {
				File lf=new File(vp);
				if (lf.exists() && lf.canRead()) result=true;
			}
		} else {
			if (isFilePathExists("/storage/MicroSD")) result=true;
			else if (isFilePathExists("/Removable/MicroSD")) result=true;
			else if (isFilePathExists("/Removable/SD")) result=true;
			else if (isFilePathExists("/storage/sdcard1")) result=true;
			else if (isFilePathExists("/sdcard1")) result=true;
			else if (isFilePathExists("/mnt/extSdCard")) result=true;
			else if (isFilePathExists("/storage/extSdCard")) result=true;
		}
		return result;
	}
	
	@SuppressLint("InlinedApi")
	private static String[] getRemovableStoragePaths(Context context, boolean debug) {
		ArrayList<String> paths=new ArrayList<String>();
	    try {
	        StorageManager sm = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
	        Method getVolumeList = sm.getClass().getDeclaredMethod("getVolumeList");
	        Object[] volumeList = (Object[])getVolumeList.invoke(sm);
	        for (Object volume : volumeList) {
	            Method getPath = volume.getClass().getDeclaredMethod("getPath");
//	            Method isRemovable = volume.getClass().getDeclaredMethod("isRemovable");
	            Method getUuid = volume.getClass().getDeclaredMethod("getUuid");
//	            Method toString = volume.getClass().getDeclaredMethod("toString");
	            String path = (String)getPath.invoke(volume);
//	            boolean removable = (Boolean)isRemovable.invoke(volume);
	            if ((String)getUuid.invoke(volume)!=null) {
	            	paths.add(path);
					if (debug) {
//						Log.v(APPLICATION_TAG, "RemovableStorages Uuid="+(String)getUuid.invoke(volume)+", removable="+removable+", path="+path);
//						util.addLogMsg("I", (String)toString.invoke(volume));
					}
	            }
	        }
	    } catch (ClassCastException e) {
	        e.printStackTrace();
	    } catch (NoSuchMethodException e) {
	        e.printStackTrace();
	    } catch (InvocationTargetException e) {
	        e.printStackTrace();
	    } catch (IllegalAccessException e) {
	        e.printStackTrace();
	    }
	    return paths.toArray(new String[paths.size()]);
	};
	
	public boolean isSdcardMounted(){
		boolean result=false;
        synchronized(removableStorageList) {
            for(SafFileItem rsi:removableStorageList) {
                if (rsi.storageTypeSdcard && rsi.storageRootFile!=null) {
                    result=true;
                    break;
                }
            }
        }
		return result;
	};
	
	public ArrayList<SafFileItem> getSafList() {
		return removableStorageList;
	}
	
	public boolean isRootTreeUri(Uri uri) {
		boolean result=false;
		String uuid=getUuidFromUri(uri.toString());
		if (!uuid.startsWith("primary")) {
			if (uri.toString().endsWith("%3A") || uri.toString().endsWith(":")) result=true;
		}
//		Log.v("","uuid="+uuid+", uri="+uri.toString()+", result="+result);
		return result;
	};

	public String getSdcardDirectory() {
		return sdcardRootDirectory;
	};
	
	public String getUsbFileSystemDirectory() {
		return usbFsRootDirectory;
	};
	
	public String getExternalSdcardPath() {
		File[] fl=ContextCompat.getExternalFilesDirs(mContext, null);
		String ld= LocalMountPoint.getExternalStorageDir();
		String esd=UNKNOWN_SDCARD_DIRECTORY;
		if (fl!=null) {
			for(File f:fl) {
//				Log.v("","f="+f.getPath());
				if (f!=null && f.getPath()!=null && !f.getPath().startsWith(ld)) {
					esd=f.getPath().substring(0, f.getPath().indexOf("/Android/data"));
//					Log.v("","esd="+esd);
					break;
				}
			}
		}
//		Log.v("","result esd="+esd);
		if (esd.equals(UNKNOWN_SDCARD_DIRECTORY)) {
			if (isFilePathExists("/storage/MicroSD")) esd="/storage/MicroSD";
			else if (isFilePathExists("/storage/sdcard1")) esd="/storage/sdcard1";
			else if (isFilePathExists("/sdcard1")) esd="/sdcard1";
			else if (isFilePathExists("/mnt/extSdCard")) esd="/mnt/extSdCard";
			else if (isFilePathExists("/storage/extSdCard")) esd="/storage/extSdCard";
		}
		return esd;
	};
	
	private static boolean isFilePathExists(String fp) {
		boolean result=false;
		File lf=new File(fp);
		if (lf.exists() && lf.canRead()) result=true;
		return result;
	};
	
	public void loadSafFileList() {
		
//		if (mDebugEnabled) getRemovableStoragePaths(mContext, mDebugEnabled);
		
		usbFsRootDirectory=LocalMountPoint.getUsbStorageDir();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		String uuid_list=prefs.getString(REMOVABLE_UUID_KEY, "");
//		Log.v("","uuid_list="+uuid_list);

        synchronized(removableStorageList) {
            removableStorageList.clear();

            if (!uuid_list.equals("")) {
                File slf=new File("/storage");
                File[] stor_list=slf.listFiles();
                String[] uuid_array=uuid_list.split("\t");
//			sdcardRootDirectory=(getExternalSdcardPath().equals(""))?UNKNOWN_SDCARD_DIRECTORY:getExternalSdcardPath();
//			for (String uuid:uuid_array) {
//				SafFileItem rsi=new SafFileItem();
//				rsi.storageUuid=uuid;
//				if (uuid!=null && !uuid.equals("")) {
//					if (Build.VERSION.SDK_INT>=23) {
//						for(File fl_item:stor_list) {
//							if (fl_item.canRead()) {
//								if (fl_item.getName().equals(uuid)) {
//									rsi.storageRootDirectory=sdcardRootDirectory;
//									rsi.storageTypeSdcard=true;
//									rsi.storageIsMounted=true;
//									break;
//								}
//							}
//						}
//						if (!rsi.storageTypeSdcard) {
//							SafFile sf=SafFile.fromTreeUri(mContext,
//									Uri.parse("content://com.android.externalstorage.documents/tree/"+rsi.storageUuid+"%3A"));
//							if (sf!=null && sf.getName()!=null) rsi.storageIsMounted=true;
//						}
//					} else {
//						SafFile sf=SafFile.fromTreeUri(mContext,
//								Uri.parse("content://com.android.externalstorage.documents/tree/"+rsi.storageUuid+"%3A"));
//						if (sf!=null && sf.getName()!=null) {
//							rsi.storageRootFile=sf;
//							rsi.storageRootDirectory=sdcardRootDirectory;
//							rsi.storageTypeSdcard=true;
//							rsi.storageIsMounted=true;
//						}
//
//					}
//					rsi.storageRootFile=SafFile.fromTreeUri(mContext,
//							Uri.parse("content://com.android.externalstsorage.documents/tree/"+rsi.storageUuid+"%3A"));
//					if (mDebugEnabled) Log.v(APPLICATION_TAG,"loadRemovableUuid Uuid="+uuid+
//							", mounted="+rsi.storageIsMounted+
//							", sdcard="+rsi.storageTypeSdcard+
//							", SafFile name="+rsi.storageRootFile.getName()+
//							", path="+rsi.storageRootDirectory);
//					removableStorageList.add(rsi);
//				}
//			}
                String sdcard_dir=getExternalSdcardPath();
                if (Build.VERSION.SDK_INT>=23) {
                    for (String uuid:uuid_array) {
                        SafFileItem rsi=new SafFileItem();
                        rsi.storageUuid=uuid;
                        rsi.storageRootDirectory=UNKNOWN_SDCARD_DIRECTORY;
                        if (uuid!=null && !uuid.equals("")) {
                            for(File fl_item:stor_list) {
                                if (fl_item.canRead()) {
                                    if (fl_item.getName().equals(uuid)) {
                                        sdcardRootDirectory=getExternalSdcardPath();
                                        rsi.storageRootDirectory=sdcard_dir;
                                        rsi.storageTypeSdcard=true;
                                        rsi.storageIsMounted=true;
                                        break;
                                    }
                                }
                            }
                            SafFile sf=SafFile.fromTreeUri(mContext,
                                    Uri.parse("content://com.android.externalstorage.documents/tree/"+rsi.storageUuid+"%3A"));
                            if (sf!=null && sf.getName()!=null) rsi.storageIsMounted=true;
                            rsi.storageRootFile=sf;
                            if (mDebugEnabled) Log.v(APPLICATION_TAG,"loadRemovableUuid Uuid="+uuid+
                                    ", mounted="+rsi.storageIsMounted+
                                    ", sdcard="+rsi.storageTypeSdcard+
                                    ", SafFile name="+rsi.storageRootFile.getName()+
                                    ", path="+rsi.storageRootDirectory);
                            removableStorageList.add(rsi);
                        }
                    }
                } else {
                    for (String uuid:uuid_array) {
                        SafFileItem rsi=new SafFileItem();
                        rsi.storageUuid=uuid;
                        rsi.storageRootDirectory=UNKNOWN_SDCARD_DIRECTORY;
                        if (uuid!=null && !uuid.equals("")) {
                            SafFile sf=SafFile.fromTreeUri(mContext,
                                    Uri.parse("content://com.android.externalstorage.documents/tree/"+rsi.storageUuid+"%3A"));
                            if (sf!=null && sf.getName()!=null) {
                                rsi.storageRootFile=sf;
                                rsi.storageRootDirectory=sdcard_dir;
                                rsi.storageTypeSdcard=true;
                                rsi.storageIsMounted=true;
                                sdcardRootDirectory=getExternalSdcardPath();
                            }
                            rsi.storageRootFile=sf;
                            if (mDebugEnabled) Log.v(APPLICATION_TAG,"loadRemovableUuid Uuid="+uuid+
                                    ", mounted="+rsi.storageIsMounted+
                                    ", sdcard="+rsi.storageTypeSdcard+
                                    ", SafFile name="+rsi.storageRootFile.getName()+
                                    ", path="+rsi.storageRootDirectory);
                            removableStorageList.add(rsi);
                        }
                    }
                }
                Collections.sort(removableStorageList, new Comparator<SafFileItem>(){
                    @Override
                    public int compare(SafFileItem l_item, SafFileItem r_item) {
                        if (l_item!=null && l_item.storageUuid!=null && r_item!=null && r_item.storageUuid!=null) return l_item.storageUuid.compareToIgnoreCase(r_item.storageUuid);
                        else return 0;
                    }
                });
            } else {

            }
        }
	};

	public SafFile getSdcardSafFile() {
		SafFile result=null;
        synchronized(removableStorageList) {
            for(SafFileItem rsi:removableStorageList) {
//			if (rsi.storageTypeSdcard && rsi.storageRootFile!=null && rsi.storageRootFile.getName()!=null) {
                if (rsi.storageTypeSdcard && rsi.storageRootFile!=null) {
                    result=rsi.storageRootFile;
                    break;
                }
            }
            if (mDebugEnabled) Log.v(APPLICATION_TAG,"getSdcardSafFile result="+((result==null)?null:result.getName()));
        }
		return result;
	};
	
	public SafFile getUsbSafFile() {
		SafFile result=null;
        synchronized(removableStorageList) {
            for(SafFileItem rsi:removableStorageList) {
                if (!rsi.storageTypeSdcard && rsi.storageRootFile!=null && rsi.storageRootFile.getName()!=null) {
                    result=rsi.storageRootFile;
                    break;
                }
            }
            if (mDebugEnabled) Log.v(APPLICATION_TAG,"getUsbSafFile result="+((result==null)?null:result.getName()));
        }
		return result;
	};

	public SafFile getSafFileByUuid(String uuid) {
		SafFile result=null;
        synchronized(removableStorageList) {
            for(SafFileItem rsi:removableStorageList) {
                if (rsi.storageUuid.equals(uuid)) {
                    result=rsi.storageRootFile;
                    break;
                }
            }
            if (mDebugEnabled) Log.v(APPLICATION_TAG,"getSafFileByUuid result="+((result==null)?null:result.getName()));
        }
		return result;
	};
	
	public boolean isUuidRegistered(String uuid) {
		boolean result=false;
        synchronized(removableStorageList) {
            for(SafFileItem rsi:removableStorageList) {
//			Log.v("","is uuid="+rsi.storageUuid+", s="+uuid);
                if (rsi.storageUuid.equals(uuid)) {
                    result=true;
                    break;
                }
            }
        }
		return result;
	};

	@SuppressLint("NewApi")
	public String saveSafFileList() {
        String edit_string="", sep="";
        synchronized(removableStorageList) {
            for(SafFileItem rsi:removableStorageList) {
//        	Log.v("","uuid="+rsi.storageUuid);
                edit_string+=sep+rsi.storageUuid;
                sep="\t";
            }
        }
//        Log.v("","edit_string="+edit_string);
        if (!edit_string.equals("")) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    		prefs.edit().putString(REMOVABLE_UUID_KEY, edit_string).commit();
        }
		return edit_string;
	};
	
	public static String getUuidFromUri(String uri) {
		String result="";
		try {
            result=uri.substring(uri.lastIndexOf("/")+1,uri.length()-3);
        } catch(Exception e) {}
//		Log.v("","result="+result);
		return result;
	};
	
	public static String getFileNameFromPath(String fpath) {
		String result="";
		String[] st=fpath.split("/");
		if (st!=null) {
			if (st[st.length-1]!=null) result=st[st.length-1];
		}
		return result;
	};

	public void addSafFileFromUri(Uri uri) {
	    String uuid=getUuidFromUri(uri.toString());
	    if (uuid.length()>0) addSafFile(uuid);
	};
	
	@SuppressLint("NewApi")
	public void addSafFile(String uuid) {
		if (!isUuidRegistered(uuid)) {
//			Log.v("","size="+removableStorageList.size());
			SafFileItem rsi=new SafFileItem();
			rsi.storageUuid=uuid;

            synchronized(removableStorageList) {
                removableStorageList.clear();
                removableStorageList.add(rsi);
            }
			saveSafFileList();
            try {
                mContext.getContentResolver().takePersistableUriPermission(
                        Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                loadSafFileList();
            } catch(Exception e) {
                e.printStackTrace();
                if (mDebugEnabled) {
                    Log.v(APPLICATION_TAG,"addSafFile error, uuid="+uuid);
                }
                msg_area="addSafFile error, uuid="+uuid+"\n";
            }
		} else {
			if (mDebugEnabled) Log.v(APPLICATION_TAG,"addUuid already registerd, Uuid="+uuid);
		}
	};
	
	public SafFile getSafFileBySdcardPath(SafFile rf, String target_path, boolean isDirectory) {
		if (mDebugEnabled) {
			Log.v(APPLICATION_TAG,"target_path="+target_path+", sdcard_dir="+sdcardRootDirectory);
		}
		msg_area="target_path="+target_path+", sdcard_dir="+sdcardRootDirectory+"\n";
		long b_time=System.currentTimeMillis();
    	SafFile document=rf;
    	
    	String relativePath="";
    	
    	if (!target_path.equals(sdcardRootDirectory)) 
    		relativePath=target_path.replace(sdcardRootDirectory+"/", "");
    	
    	if (mDebugEnabled) {
    		Log.v(APPLICATION_TAG,"rootsaf="+rf+", relativePath="+relativePath);
    	}
		msg_area+="rootsaf="+rf+", relativePath="+relativePath+"\n";
    	
    	if (!relativePath.equals("")) {
            String[] parts = relativePath.split("\\/");
            for (int i = 0; i < parts.length; i++) {
            	if (mDebugEnabled) {
            		Log.v(APPLICATION_TAG,"parts="+parts[i]);
            	}
    			msg_area+="parts="+parts[i]+"\n";
            	if (!parts[i].equals("")) {
                    SafFile nextDocument = document.findFile(parts[i]);
                    if (mDebugEnabled) {
                    	Log.v(APPLICATION_TAG,"find name="+parts[i]+", result="+nextDocument);
                    }
        			msg_area+="find name="+parts[i]+", result="+nextDocument+"\n";
                    if (nextDocument == null) {
                        if ((i < parts.length - 1) || isDirectory) {
                        	String c_dir=parts[i];
                       		nextDocument = document.createDirectory(c_dir);
                        	if (mDebugEnabled) {
                        		if (nextDocument!=null) Log.v(APPLICATION_TAG,"dir created name="+nextDocument.getName());
                        		else Log.v(APPLICATION_TAG,"dir create failed, name="+c_dir);
                        	}
                			msg_area+="dir created name="+c_dir+", result="+nextDocument+"\n";
                			msg_area+=document.getMsgArea();
//                			Log.v("","saf="+document.getMsgArea());
                        } else {
                            nextDocument = document.createFile("", parts[i]);
                            if (mDebugEnabled) {
                            	if (nextDocument!=null) Log.v(APPLICATION_TAG,"file created name="+nextDocument.getName());
                            	else Log.v(APPLICATION_TAG,"file create failed, name="+parts[i]);
                            }
                			msg_area+="file created name="+parts[i]+", result="+nextDocument+"\n";
                        }
                    }
                    document = nextDocument;
            	}
            }
    	}
        if (mDebugEnabled) Log.v(APPLICATION_TAG,"getSafFileBySdcardPath elapsed="+(System.currentTimeMillis()-b_time));
        msg_area+="getSafFileBySdcardPath elapsed="+(System.currentTimeMillis()-b_time)+"\n";
//        Log.v("","msg="+msg_area);
        return document;
	};
	
	
}

