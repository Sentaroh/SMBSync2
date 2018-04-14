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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

class FileLastModifiedTimeEntry implements Externalizable {
    private static final long serialVersionUID = 1L;
    private String file_path = "";
    private long local_last_modified_time = 0;
    private long remote_last_modified_time = 0;
    private boolean referenced = false;

    FileLastModifiedTimeEntry(String fp, long l_lm, long r_lm, boolean ref) {
        file_path = fp;
        local_last_modified_time = l_lm;
        remote_last_modified_time = r_lm;
        referenced = ref;
    }

    public boolean isReferenced() {
        return referenced;
    }

    public void setReferenced(boolean p) {
        referenced = p;
    }

    public String getFilePath() {
        return file_path;
    }

    public long getLocalFileLastModified() {
        return local_last_modified_time;
    }

    public long getRemoteFileLastModified() {
        return remote_last_modified_time;
    }

    public void setFilePath(String p) {
        file_path = p;
    }

    public void setLocalFileLastModified(long p) {
        local_last_modified_time = p;
    }

    public void setRemoteFileLastModified(long p) {
        remote_last_modified_time = p;
    }

    @Override
    public void readExternal(ObjectInput input) throws IOException,
            ClassNotFoundException {
        file_path = input.readUTF();
        local_last_modified_time = input.readLong();
        remote_last_modified_time = input.readLong();
    }

    ;

    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeUTF(file_path);
        output.writeLong(local_last_modified_time);
        output.writeLong(remote_last_modified_time);
    }

    ;
}
