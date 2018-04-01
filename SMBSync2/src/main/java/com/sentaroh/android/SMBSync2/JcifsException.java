package com.sentaroh.android.SMBSync2;


public class JcifsException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5966004473207908418L;
	private Throwable mException=null;
//    private Throwable mCause=null;
	private int mNtStatus=0;
	private String mMessage=null;
//	private String mCause=null;
	public JcifsException(Throwable e, int nt_status, Throwable cause ) {
		mException=new Exception(e.getMessage(), cause);
		mException.setStackTrace(e.getStackTrace());
		
//		mCause=cause.getMessage();
		mNtStatus=nt_status;
        mMessage=e.getMessage();
	}

	public int getNtStatus() {
	    return mNtStatus;
    }
    public Throwable getCause() {
        return mException.getCause();
    }
    public String getMessage() {
        return mMessage;
    }
}
