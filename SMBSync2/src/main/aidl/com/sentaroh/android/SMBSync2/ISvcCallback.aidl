package com.sentaroh.android.SMBSync2;

interface ISvcCallback{ 
	void cbThreadStarted();
    void cbThreadEnded();
    
    void cbShowConfirmDialog(String method, String msg, String pair_a_path, long pair_a_length, long pair_a_last_mod,
                String pair_b_path, long pair_b_length, long pair_b_last_mod);
    void cbHideConfirmDialog();
    
    void cbWifiStatusChanged(String status, String ssid);
    
    void cbMediaStatusChanged();
}