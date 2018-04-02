package com.sentaroh.android.SMBSync2;

import java.util.Properties;

public class JcifsAuth {
	
	private jcifs.smb.NtlmPasswordAuthentication mSmb1Auth=null;
	private jcifsng.CIFSContext mSmb2Auth =null;
	private boolean mSmb1=true;

	private String mDomain=null, mUserName=null, mUserPass=null;
	public JcifsAuth(boolean smb1, String domain, String user, String pass) {
        mSmb1=smb1;
        mDomain=domain;
        mUserName=user;
        mUserPass=pass;
        if (mSmb1) {
			mSmb1Auth=new jcifs.smb.NtlmPasswordAuthentication(domain, user, pass);
		} else {
	        jcifsng.context.BaseContext bc;
			try {
                Properties prop=new Properties();
                prop.setProperty("jcifs.smb.client.minVersion","SMB210");
                prop.setProperty("jcifs.smb.client.maxVersion","SMB210");
				bc = new jcifsng.context.BaseContext(new jcifsng.config.PropertyConfiguration(prop));
		        jcifsng.smb.NtlmPasswordAuthentication creds = new jcifsng.smb.NtlmPasswordAuthentication(bc, domain,user,pass);
		        mSmb2Auth = bc.withCredentials(creds);
			} catch (jcifsng.CIFSException e) {
				e.printStackTrace();
			}
		}
	}

    public JcifsAuth(boolean smb1, String domain, String user, String pass, boolean ipc_signing_enforced) {
        mSmb1=smb1;
        mDomain=domain;
        mUserName=user;
        mUserPass=pass;
        if (mSmb1) {
            mSmb1Auth=new jcifs.smb.NtlmPasswordAuthentication(domain, user, pass);
        } else {
            jcifsng.context.BaseContext bc;
            try {
                Properties prop=new Properties();
                if (ipc_signing_enforced) prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "true");
                else prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "false");
                prop.setProperty("jcifs.smb.client.minVersion","SMB210");
                prop.setProperty("jcifs.smb.client.maxVersion","SMB210");

                bc = new jcifsng.context.BaseContext(new jcifsng.config.PropertyConfiguration(prop));
                jcifsng.smb.NtlmPasswordAuthentication creds = new jcifsng.smb.NtlmPasswordAuthentication(bc, domain,user,pass);
                mSmb2Auth = bc.withCredentials(creds);
            } catch (jcifsng.CIFSException e) {
                e.printStackTrace();
            }
        }
    }

    public JcifsAuth(boolean smb1, String domain, String user, String pass, boolean ipc_signing_enforced, String min_version, String max_version) {
        mSmb1=smb1;
        mDomain=domain;
        mUserName=user;
        mUserPass=pass;
        if (mSmb1) {
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
                mSmb2Auth = bc.withCredentials(creds);
            } catch (jcifsng.CIFSException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSmb1Auth() {
    	return mSmb1;
    }
    
    public jcifs.smb.NtlmPasswordAuthentication getSmb1Auth() {
		return mSmb1Auth;
	}
	
	public jcifsng.CIFSContext getSmb2Auth() {
		return mSmb2Auth;
	}

    public String getDomain() {return mDomain;}
    public String getUserName() {return mUserName;}
    public String getUserPass() {return mUserPass;}
}
