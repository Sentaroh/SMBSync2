package com.sentaroh.android.SMBSync2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;


public class JcifsFile {

    static final public String JCIFS_LEVEL_JCIFS1="CIFS1";
    static final public String JCIFS_LEVEL_JCIFS2="CIFS2";
    private String mLevel=JCIFS_LEVEL_JCIFS1;

    private String mUrl="";

    private JcifsAuth mAuth=null;

    private jcifsng.smb.SmbFile mNgSmbFile=null;
    private jcifs.smb.SmbFile mOldSmbFile=null;

    public JcifsFile(String url, JcifsAuth auth) throws MalformedURLException {
        mLevel=auth.getCifsLevel();
        mUrl=url;
        mAuth=auth;
        
    	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
    		mOldSmbFile=new jcifs.smb.SmbFile(url, auth.getOldAuth());
    	} else {
    		mNgSmbFile=new jcifsng.smb.SmbFile(url,auth.getNgAuth());
    	}
        
    }

    public boolean exists()   throws JcifsException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		return mOldSmbFile.exists();
        	} else {
        		return mNgSmbFile.exists();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    	
    }

    public void delete()   throws JcifsException{
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		mOldSmbFile.delete();
        	} else {
        		mNgSmbFile.delete();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    	
    }

    public void mkdir()   throws JcifsException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		mOldSmbFile.mkdir();
        	} else {
        		mNgSmbFile.mkdir();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    	
    }

    public void mkdirs()   throws JcifsException{
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		mOldSmbFile.mkdirs();
        	} else {
        		mNgSmbFile.mkdirs();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    	
    }

    public InputStream getInputStream()   throws JcifsException, IOException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		return mOldSmbFile.getInputStream();
        	} else {
        		return mNgSmbFile.getInputStream();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    	
    }


    public OutputStream getOutputStream()  throws JcifsException, IOException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		return mOldSmbFile.getOutputStream();
        	} else {
        		return mNgSmbFile.getOutputStream();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    	
    }
    
    public void connect() throws JcifsException, IOException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		mOldSmbFile.connect();
        	} else {
        		mNgSmbFile.connect();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}

    }

    public void createNew() throws JcifsException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		mOldSmbFile.createNewFile();
        	} else {
        		mNgSmbFile.createNewFile();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    	
    }
    
    public String getName() {
        if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
			return mOldSmbFile.getName();
		} else {
			return mNgSmbFile.getName();
		}
    	
    }

    public String getPath() {
        if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
			return mOldSmbFile.getPath();
		} else {
			return mNgSmbFile.getPath();
		}
    }

    public String getParent() {
        if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
			return mOldSmbFile.getParent();
		} else {
			return mNgSmbFile.getParent();
		}
    	
    }

    public boolean canRead()   throws JcifsException {
        try {
            if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
                return mOldSmbFile.canRead();
            } else {
                return mNgSmbFile.canRead();
            }
        } catch (jcifsng.smb.SmbException e) {
            e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
        } catch (jcifs.smb.SmbException e) {
            e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
        }
    }

    public boolean canWrite()   throws JcifsException {
        try {
            if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
                return mOldSmbFile.canWrite();
            } else {
                return mNgSmbFile.canWrite();
            }
        } catch (jcifsng.smb.SmbException e) {
            e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
        } catch (jcifs.smb.SmbException e) {
            e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
        }
    }

    public boolean isDirectory() throws JcifsException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		return mOldSmbFile.isDirectory();
        	} else {
        		return mNgSmbFile.isDirectory();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    	
    }

    public boolean isFile() throws JcifsException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		return mOldSmbFile.isFile();
        	} else {
        		return mNgSmbFile.isFile();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    	
    }

    public boolean isHidden() throws JcifsException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		return mOldSmbFile.isHidden();
        	} else {
        		return mNgSmbFile.isHidden();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    }
    
    public long length() throws JcifsException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		return mOldSmbFile.length();
        	} else {
        		return mNgSmbFile.length();
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    	
    }
    
    public String[] list() throws JcifsException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) return mOldSmbFile.list();
        	else return mNgSmbFile.list();
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    	
    }

    public JcifsFile[] listFiles() throws JcifsException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		jcifs.smb.SmbFile[] files=mOldSmbFile.listFiles();
        		if (files==null) return null;
        		JcifsFile[] result=new JcifsFile[files.length];
        		for(int i=0;i<files.length;i++) result[i]=new JcifsFile(files[i].getPath(),mAuth);
        		return result;
        	} else {
                jcifsng.smb.SmbFile[] files=mNgSmbFile.listFiles();
        		if (files==null) return null;
        		JcifsFile[] result=new JcifsFile[files.length];
        		for(int i=0;i<files.length;i++) result[i]=new JcifsFile(files[i].getPath(),mAuth);
        		return result;
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
            throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (MalformedURLException e) {
            return null;
        }
    }
    
    public void renameTo ( JcifsFile d ) throws JcifsException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        		jcifs.smb.SmbFile to=new jcifs.smb.SmbFile(d.getPath(), d.getAuth().getOldAuth());
        		mOldSmbFile.renameTo(to);
        	} else {
                jcifsng.smb.SmbFile to=new jcifsng.smb.SmbFile(d.getPath(), d.getAuth().getNgAuth());
        		mNgSmbFile.renameTo(to);
        	}
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public JcifsAuth getAuth() {
    	return mAuth;
    }



    public void setLastModified(long lm) throws JcifsException {
        try {
        	if (mLevel.equals(JCIFS_LEVEL_JCIFS1)) {
        	    mOldSmbFile.setLastModified(lm);
            } else {
        	    mNgSmbFile.setLastModified(lm);
            }
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    }

    public long getLastModified() throws JcifsException {
        try {
			return mLevel.equals(JCIFS_LEVEL_JCIFS1)?mOldSmbFile.lastModified():mNgSmbFile.lastModified();
		} catch (jcifsng.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		} catch (jcifs.smb.SmbException e) {
			e.printStackTrace();
			throw(new JcifsException(e, e.getNtStatus(), e.getCause()));
		}
    }


}
