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

import android.util.Log;

import java.util.Properties;

public class JcifsAuth {
    final static public boolean JCIFS_FILE_SMB1 = true;
    final static public boolean JCIFS_FILE_SMB2 = false;
    private jcifs.smb.NtlmPasswordAuthentication mSmb1Auth = null;
    private jcifsng.CIFSContext mSmb2Auth = null;
    private boolean mSmb1 = true;

    private String mDomain = null, mUserName = null, mUserPass = null;

    /**
     * SMB1 or SMB2 Constructor
     *
     * @param smb1   true is use jcifs-1.3.17, false is use jcifs-ng
     * @param domain A domain name
     * @param user   A user name
     * @param pass   A password for user
     * @throws JcifsException
     */
    @SuppressWarnings("deprecation")
    public JcifsAuth(boolean smb_level, String domain, String user, String pass) {
        mSmb1 = smb_level;
        mDomain = domain;
        mUserName = user;
        mUserPass = pass;
        if (mSmb1) {
            mSmb1Auth = new jcifs.smb.NtlmPasswordAuthentication(domain, user, pass);
        } else {
            try {
                Properties prop = new Properties();
                prop.setProperty("jcifs.smb.client.minVersion", "SMB210");
                prop.setProperty("jcifs.smb.client.maxVersion", "SMB210");
                jcifsng.context.BaseContext bc = new jcifsng.context.BaseContext(new jcifsng.config.PropertyConfiguration(prop));
                jcifsng.smb.NtlmPasswordAuthentication creds = new jcifsng.smb.NtlmPasswordAuthentication(bc, domain, user, pass);
                mSmb2Auth = bc.withCredentials(creds);
            } catch (jcifsng.CIFSException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * SMB2 Constructor
     *
     * @param domain               A domain name
     * @param user                 A user name
     * @param pass                 A password for user
     * @param ipc_signing_enforced true is use IpcSigningEnforced
     * @throws JcifsException
     */
    @SuppressWarnings("deprecation")
    public JcifsAuth(String domain, String user, String pass, boolean ipc_signing_enforced) {
        mSmb1 = JCIFS_FILE_SMB2;
        mDomain = domain;
        mUserName = user;
        mUserPass = pass;
        try {
            Properties prop = new Properties();
            if (ipc_signing_enforced)
                prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "true");
            else prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "false");
            prop.setProperty("jcifs.smb.client.minVersion", "SMB210");
            prop.setProperty("jcifs.smb.client.maxVersion", "SMB210");

            jcifsng.context.BaseContext bc = new jcifsng.context.BaseContext(new jcifsng.config.PropertyConfiguration(prop));
            jcifsng.smb.NtlmPasswordAuthentication creds = new jcifsng.smb.NtlmPasswordAuthentication(bc, domain, user, pass);
            mSmb2Auth = bc.withCredentials(creds);
        } catch (jcifsng.CIFSException e) {
            e.printStackTrace();
        }
    }

    /**
     * SMB2 Constructor
     *
     * @param domain               A domain name
     * @param user                 A user name
     * @param pass                 A password for user
     * @param ipc_signing_enforced true is use IpcSigningEnforced
     * @param min_version          min SMB version ("SMB1" or "SMB210")
     * @param max_version          max SMB version ("SMB1" or "SMB210")
     * @throws JcifsException
     */
    @SuppressWarnings("deprecation")
    public JcifsAuth(String domain, String user, String pass, boolean ipc_signing_enforced, String min_version, String max_version) {
        mSmb1 = JCIFS_FILE_SMB2;
        mDomain = domain;
        mUserName = user;
        mUserPass = pass;
        try {
            Properties prop = new Properties();
            if (ipc_signing_enforced)
                prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "true");
            else prop.setProperty("jcifs.smb.client.ipcSigningEnforced", "false");
            prop.setProperty("jcifs.smb.client.minVersion", min_version);
            prop.setProperty("jcifs.smb.client.maxVersion", max_version);

            jcifsng.context.BaseContext bc = new jcifsng.context.BaseContext(new jcifsng.config.PropertyConfiguration(prop));
            jcifsng.smb.NtlmPasswordAuthentication creds = new jcifsng.smb.NtlmPasswordAuthentication(bc, domain, user, pass);
            mSmb2Auth = bc.withCredentials(creds);
        } catch (jcifsng.CIFSException e) {
            e.printStackTrace();
        }
    }

    public boolean isSmb1() {
        return mSmb1;
    }

    public jcifs.smb.NtlmPasswordAuthentication getSmb1Auth() {
        return mSmb1Auth;
    }

    public jcifsng.CIFSContext getSmb2Auth() {
        return mSmb2Auth;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getUserPass() {
        return mUserPass;
    }
}
