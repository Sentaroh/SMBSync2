package com.sentaroh.jcifs;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import jcifs.smb.SmbFile;

public class JcifsFile {
    public static final int JCIFS_FILE_SMB1 = 1;
    public static final int JCIFS_FILE_SMB201 = 2;
    public static final int JCIFS_FILE_SMB211 = 3;
    public static final int JCIFS_FILE_SMB212 = 4;
    public static final int JCIFS_FILE_SMB214 = 5;

    private SmbFile delegate;
    private JcifsAuth auth;
    private String url;

    public JcifsFile(String url, JcifsAuth auth) throws MalformedURLException, JcifsException {
        this.auth = auth;
        this.url = url;
        try {
            this.delegate = new SmbFile(url, auth.getContext());
        } catch (MalformedURLException e) {
            throw e;
        } catch (Exception e) {
            throw new JcifsException(e.getMessage());
        }
    }

    private JcifsFile(SmbFile delegate, JcifsAuth auth) {
        this.delegate = delegate;
        this.auth = auth;
        this.url = delegate.getURL().toString();
    }

    public boolean isSmb1File() { return false; }
    public boolean isSmb201File() { return auth != null && auth.isSmb201(); }
    public boolean isSmb211File() { return auth != null && auth.isSmb211(); }
    public boolean isSmb212File() { return auth != null && auth.isSmb212(); }
    public boolean isSmb214File() { return auth != null && auth.isSmb214(); }

    public jcifs.smb.SmbFile getSmb1File() { return null; }
    public SmbFile getSmb201File() { return delegate; }
    public SmbFile getSmb211File() { return delegate; }
    public SmbFile getSmb212File() { return delegate; }
    public SmbFile getSmb214File() { return delegate; }

    public boolean exists() throws JcifsException {
        try { return delegate.exists(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public void delete() throws JcifsException {
        try { delegate.delete(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public void mkdir() throws JcifsException {
        try { delegate.mkdir(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public void mkdirs() throws JcifsException {
        try { delegate.mkdirs(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public int getAttributes() throws JcifsException {
        try { return delegate.getAttributes(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public InputStream getInputStream() throws JcifsException {
        try { return delegate.getInputStream(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public OutputStream getOutputStream() throws JcifsException {
        try { return delegate.getOutputStream(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public void close() throws JcifsException {
        try { delegate.close(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public void connect() throws JcifsException {
        try { delegate.connect(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public void createNew() throws JcifsException {
        try { delegate.createNewFile(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public String getName() { return delegate.getName(); }
    public String getPath() { return delegate.getPath(); }
    public String getCanonicalPath() { return delegate.getCanonicalPath(); }
    public String getShare() { return delegate.getShare(); }
    
    public int getType() throws JcifsException {
        try { return delegate.getType(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public String getUncPath() { return delegate.getUncPath(); }
    public String getParent() { return delegate.getParent(); }

    public boolean canRead() throws JcifsException {
        try { return delegate.canRead(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public boolean canWrite() throws JcifsException {
        try { return delegate.canWrite(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public boolean isDirectory() throws JcifsException {
        try { return delegate.isDirectory(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public boolean isFile() throws JcifsException {
        try { return delegate.isFile(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public boolean isHidden() throws JcifsException {
        try { return delegate.isHidden(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public long length() throws JcifsException {
        try { return delegate.length(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public String[] list() throws JcifsException {
        try { return delegate.list(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public JcifsFile[] listFiles() throws JcifsException {
        try {
            SmbFile[] original = delegate.listFiles();
            if (original == null) return null;
            JcifsFile[] result = new JcifsFile[original.length];
            for (int i = 0; i < original.length; i++) {
                result[i] = new JcifsFile(original[i], auth);
            }
            return result;
        } catch (Exception e) {
            throw new JcifsException(e.getMessage());
        }
    }

    public void renameTo(JcifsFile dest) throws JcifsException {
        try { delegate.renameTo(dest.delegate); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public JcifsAuth getAuth() { return auth; }

    public void setLastModified(long time) throws JcifsException {
        try { delegate.setLastModified(time); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }

    public long getLastModified() throws JcifsException {
        try { return delegate.getLastModified(); } catch (Exception e) { throw new JcifsException(e.getMessage()); }
    }
}
