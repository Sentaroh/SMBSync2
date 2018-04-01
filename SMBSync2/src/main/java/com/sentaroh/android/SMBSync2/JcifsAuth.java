package com.sentaroh.android.SMBSync2;

import java.util.Properties;

public class JcifsAuth {
	
	private jcifs.smb.NtlmPasswordAuthentication mSmb1Auth=null;
	private jcifsng.CIFSContext mNgAuth=null;
	private String mLevel = JcifsFile.JCIFS_LEVEL_JCIFS1;

	private String mDomain=null, mUserName=null, mUserPass=null;
	public JcifsAuth(String level, String domain, String user, String pass) {
        mLevel=level;
        mDomain=domain;
        mUserName=user;
        mUserPass=pass;
		if (level.equals(JcifsFile.JCIFS_LEVEL_JCIFS1)) {
			mSmb1Auth=new jcifs.smb.NtlmPasswordAuthentication(domain, user, pass);
		} else {
	        jcifsng.context.BaseContext bc;
			try {
//				System.getProperties().setProperty("jcifs.smb.client.ipcSigningEnforced", "false");

                Properties prop=new Properties();
				bc = new jcifsng.context.BaseContext(new jcifsng.config.PropertyConfiguration(prop));
		        jcifsng.smb.NtlmPasswordAuthentication creds = new jcifsng.smb.NtlmPasswordAuthentication(bc, domain,user,pass);
		        mNgAuth = bc.withCredentials(creds);
			} catch (jcifsng.CIFSException e) {
				e.printStackTrace();
			}
		}
	}

    public JcifsAuth(String level, String domain, String user, String pass, boolean ipc_signing_enforced) {
        mLevel=level;
        mDomain=domain;
        mUserName=user;
        mUserPass=pass;
        if (level.equals(JcifsFile.JCIFS_LEVEL_JCIFS1)) {
            mSmb1Auth=new jcifs.smb.NtlmPasswordAuthentication(domain, user, pass);
        } else {
            jcifsng.context.BaseContext bc;
            try {
                Properties prop=new Properties();
                if (ipc_signing_enforced) prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "true");
                else prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "false");

                bc = new jcifsng.context.BaseContext(new jcifsng.config.PropertyConfiguration(prop));
                jcifsng.smb.NtlmPasswordAuthentication creds = new jcifsng.smb.NtlmPasswordAuthentication(bc, domain,user,pass);
                mNgAuth = bc.withCredentials(creds);
            } catch (jcifsng.CIFSException e) {
                e.printStackTrace();
            }
        }
    }

    public JcifsAuth(String level, String domain, String user, String pass, boolean ipc_signing_enforced, String min_version, String max_version) {
        mLevel=level;
        mDomain=domain;
        mUserName=user;
        mUserPass=pass;
        if (level.equals(JcifsFile.JCIFS_LEVEL_JCIFS1)) {
            mSmb1Auth=new jcifs.smb.NtlmPasswordAuthentication(domain, user, pass);
        } else {
            jcifsng.context.BaseContext bc;
            try {
                Properties prop=new Properties();
                if (ipc_signing_enforced) prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "true");
                else prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "false");
                prop.setProperty("jcifs.smb.client.minVersion",min_version);
                prop.setProperty("jcifs.smb.client.maxVersion",max_version);

                bc = new jcifsng.context.BaseContext(new jcifsng.config.PropertyConfiguration(prop));
                jcifsng.smb.NtlmPasswordAuthentication creds = new jcifsng.smb.NtlmPasswordAuthentication(bc, domain,user,pass);
                mNgAuth = bc.withCredentials(creds);
            } catch (jcifsng.CIFSException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSmb1Auth() {
    	return mLevel.equals(JcifsFile.JCIFS_LEVEL_JCIFS1)?true:false;
    }
    
    public jcifs.smb.NtlmPasswordAuthentication getSmb1Auth() {
		return mSmb1Auth;
	}
	
	public jcifsng.CIFSContext getSmb2Auth() {
		return mNgAuth;
	}

	public String getCifsLevel() {
	    return mLevel;
    }

    public String getDomain() {return mDomain;}
    public String getUserName() {return mUserName;}
    public String getUserPass() {return mUserPass;}
}
