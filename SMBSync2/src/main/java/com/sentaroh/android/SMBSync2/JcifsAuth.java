package com.sentaroh.android.SMBSync2;

import java.util.Properties;

public class JcifsAuth {
	final static public boolean JCIFS_FILE_SMB1=true;
    final static public boolean JCIFS_FILE_SMB2=false;
	private jcifs.smb.NtlmPasswordAuthentication mSmb1Auth=null;
	private jcifsng.CIFSContext mSmb2Auth =null;
	private boolean mSmb1=true;

	private String mDomain=null, mUserName=null, mUserPass=null;

    /**
     * SMB1 or SMB2 Constructor
     *
     * @param smb1
     *            true is use jcifs-1.3.17, false is use jcifs-ng
     * @param domain
     *            A domain name
     * @param user
     *            A user name
     * @param pass
     *            A password for user
     * @throws JcifsException
     */
    public JcifsAuth(boolean smb_level, String domain, String user, String pass) {
        mSmb1=smb_level;
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

    /**
     * SMB2 Constructor
     *
     * @param domain
     *            A domain name
     * @param user
     *            A user name
     * @param pass
     *            A password for user
     * @param ipc_signing_enforced
     *            true is use IpcSigningEnforced
     * @throws JcifsException
     */
    public JcifsAuth(String domain, String user, String pass, boolean ipc_signing_enforced) {
        mSmb1=JCIFS_FILE_SMB2;
        mDomain=domain;
        mUserName=user;
        mUserPass=pass;
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

    /**
     * SMB2 Constructor
     *
     * @param domain
     *            A domain name
     * @param user
     *            A user name
     * @param pass
     *            A password for user
     * @param ipc_signing_enforced
     *            true is use IpcSigningEnforced
     * @param min_version
     *            min SMB version ("SMB1" or "SMB210")
     * @param max_version
     *            max SMB version ("SMB1" or "SMB210")
     * @throws JcifsException
     */
    public JcifsAuth(String domain, String user, String pass, boolean ipc_signing_enforced, String min_version, String max_version) {
        mSmb1=JCIFS_FILE_SMB2;
        mDomain=domain;
        mUserName=user;
        mUserPass=pass;
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
