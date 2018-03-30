package com.sentaroh.android.SMBSync2;


public class JcifsException extends Exception {
	private Throwable mException=null;
    private Throwable mCause=null;
	private int mNtStatus=0;
	private String mMessage=null;
	public JcifsException(Throwable e, int nt_status, Throwable cause ) {
		mException=e;
		mCause=cause;
		mNtStatus=nt_status;
        mMessage=e.getMessage();
	}

	public int getNtStatus() {
	    return mNtStatus;
    }
    public Throwable getCause() {
        return mCause;
    }
    public String getMessage() {
        return mMessage;
    }
}
