package com.sentaroh.android.SMBSync2;

interface ISvcCallback{ 
	void cbThreadStarted();
    void cbThreadEnded();
    
    void cbShowConfirmDialog(String fp, String method);
    void cbHideConfirmDialog();
    
    void cbWifiStatusChanged(String status, String ssid);
    
    void cbMediaStatusChanged();
}