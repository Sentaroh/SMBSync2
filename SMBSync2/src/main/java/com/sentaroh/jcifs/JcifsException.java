package com.sentaroh.jcifs;

public class JcifsException extends Exception {
    private int ntStatus;

    public JcifsException(String message) {
        super(message);
    }

    public JcifsException(Throwable cause, int ntStatus, Throwable originalCause) {
        super(cause);
        this.ntStatus = ntStatus;
    }

    public int getNtStatus() {
        return ntStatus;
    }

    @Override
    public Throwable getCause() {
        return super.getCause();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
