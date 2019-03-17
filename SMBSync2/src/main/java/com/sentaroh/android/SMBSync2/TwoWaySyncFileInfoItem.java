package com.sentaroh.android.SMBSync2;

class TwoWaySyncFileInfoItem {
    public boolean referenced=false;
    public boolean updated=false;
    public long syncTime=0L;

    public String filePath ="";
    public long fileSize =0L;
    public long fileLastModified =0L;

    public TwoWaySyncFileInfoItem() {}

    public TwoWaySyncFileInfoItem(boolean ref, boolean upd, long sync_time,
                                  String fp, long file_size, long last_modified) {
        syncTime=sync_time;
        referenced=ref;
        updated=upd;

        filePath =fp;
        fileSize =file_size;
        fileLastModified =last_modified;
    }

}
