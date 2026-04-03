package com.sentaroh.jcifs;

import java.util.Properties;
import jcifs.CIFSContext;
import jcifs.context.BaseContext;
import jcifs.config.BaseConfiguration;
import jcifs.config.PropertyConfiguration;
import jcifs.smb.NtlmPasswordAuthenticator;

public class JcifsAuth {
    public static final int JCIFS_FILE_SMB1 = 1;
    public static final int JCIFS_FILE_SMB201 = 2;
    public static final int JCIFS_FILE_SMB211 = 3;
    public static final int JCIFS_FILE_SMB212 = 4;
    public static final int JCIFS_FILE_SMB214 = 5;

    private int smbLevel;
    private String domain;
    private String userName;
    private String userPass;
    private CIFSContext context;

    public JcifsAuth(int smbLevel, String domain, String userName, String userPass) {
        this.smbLevel = smbLevel;
        this.domain = domain;
        this.userName = userName;
        this.userPass = userPass;
        initContext();
    }

    public JcifsAuth(int smbLevel, String domain, String userName, String userPass, boolean ipcSigningEnforced) {
        this(smbLevel, domain, userName, userPass);
    }

    public JcifsAuth(int smbLevel, String domain, String userName, String userPass, boolean ipcSigningEnforced, boolean useSmb2Negotiation) {
        this(smbLevel, domain, userName, userPass);
    }

    public JcifsAuth(int smbLevel, String domain, String userName, String userPass, boolean ipcSigningEnforced, boolean useSmb2Negotiation, Properties customProps) {
        this(smbLevel, domain, userName, userPass);
    }

    public JcifsAuth(int smbLevel, String domain, String userName, String userPass, boolean ipcSigningEnforced, String hostName, String ipAddress) {
        this(smbLevel, domain, userName, userPass);
    }

    private void initContext() {
        try {
            Properties props = new Properties();
            props.setProperty("jcifs.smb.client.enableSMB2", "true");
            props.setProperty("jcifs.smb.client.useSMB2Negotiation", "true");
            // Force SMB2/3 since SMB1 is dropped
            props.setProperty("jcifs.smb.client.disableSMB1", "true");
            
            PropertyConfiguration config = new PropertyConfiguration(props);
            BaseContext baseCtx = new BaseContext(config);
            NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(domain, userName, userPass);
            this.context = baseCtx.withCredentials(auth);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getSmbLevel() { return smbLevel; }
    public boolean isSmb1() { return false; } // SMB1 is dropped
    public boolean isSmb201() { return smbLevel == JCIFS_FILE_SMB201; }
    public boolean isSmb211() { return smbLevel == JCIFS_FILE_SMB211; }
    public boolean isSmb212() { return smbLevel == JCIFS_FILE_SMB212; }
    public boolean isSmb214() { return smbLevel == JCIFS_FILE_SMB214; }

    public jcifs.smb.NtlmPasswordAuthentication getSmb1Auth() { return null; }
    public CIFSContext getSmb201Auth() { return context; }
    public CIFSContext getSmb211Auth() { return context; }
    public CIFSContext getSmb212Auth() { return context; }
    public CIFSContext getSmb214Auth() { return context; }

    public String getDomain() { return domain; }
    public String getUserName() { return userName; }
    public String getUserPass() { return userPass; }
    
    public CIFSContext getContext() { return context; }
}
