package com.sentaroh.android.SMBSync2;

/*
The MIT License (MIT)
Copyright (c) 2011-2013 Sentaroh

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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class JcifsUtil {

    final static public boolean isValidIpAddress(String in_addr) {
        boolean result = false;
        String strip_addr = in_addr;
        if (in_addr.indexOf(":") >= 0) strip_addr = in_addr.substring(0, in_addr.indexOf(":"));
        String[] addr = strip_addr.split("\\.");
        if (addr.length == 4) {
            boolean error = false;
            for (int i = 0; i < 4; i++) {
                try {
                    int num = Integer.parseInt(addr[i]);
                    if (num < 0 || num > 255) {
                        error = true;
                        break;
                    } else {
                        if (i > 2) {
                            if (addr[i].startsWith("0")) {
                                error = true;
                                break;
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    error = true;
                    break;
                }
            }
            if (!error) result = true;
        }
        return result;
    }

    final static public String getSmbHostIpAddressByHostName(boolean smb1, String hn) {
        if (smb1) {
            return getSmbHostIpAddressFromNameSmb1(hn);
        } else {
            return getSmbHostIpAddressFromNameSmb2(hn);
        }
    }

    final static private String getSmbHostIpAddressFromNameSmb1(String hn) {
        String ipAddress = null;
        try {
            jcifs.netbios.NbtAddress nbtAddress = jcifs.netbios.NbtAddress.getByName(hn);
            InetAddress address = nbtAddress.getInetAddress();
            ipAddress = address.getHostAddress();
        } catch (UnknownHostException e) {
//			e.printStackTrace();
        }
        return ipAddress;
    }

    final static private String getSmbHostIpAddressFromNameSmb2(String hn) {
        String ipAddress = null;
        try {
            jcifsng.context.BaseContext bc = new jcifsng.context.BaseContext(new jcifsng.config.PropertyConfiguration(System.getProperties()));
            ipAddress = bc.getNameServiceClient().getByName(hn).getHostAddress();
        } catch (UnknownHostException e) {
//			e.printStackTrace();
        } catch (jcifsng.CIFSException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

    final static public boolean isIpAddressAndPortConnected(String address, int port, int timeout) {
        boolean reachable = false;
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(address, port)), timeout);
            reachable = true;
            socket.close();
        } catch (IOException e) {
//        	e.printStackTrace();
        } catch (Exception e) {
//        	e.printStackTrace();
        }
        return reachable;
    }

    final static public String getSmbHostNameByAddress(boolean smb1, String address) {
        if (smb1) {
            return getSmbHostNameFromAddressSmb1(address);
        } else {
            return getSmbHostNameFromAddressSmb2(address);
        }
    }

    final static private String getSmbHostNameFromAddressSmb1(String address) {
        String srv_name = "";
        try {
            jcifs.netbios.NbtAddress[] uax = jcifs.netbios.NbtAddress.getAllByAddress(address);
            if (uax != null) {
                for (int i = 0; i < uax.length; i++) {
                    jcifs.netbios.NbtAddress ua = uax[i];
                    String hn;
                    hn = ua.firstCalledName();
                    if (ua.getNameType() == 32) {
                        srv_name = hn;
                        break;
                    }
                }
            }
        } catch (UnknownHostException e) {
        }
        return srv_name;
    }

    final static private String getSmbHostNameFromAddressSmb2(String address) {
        String srv_name = "";
        try {
            jcifsng.context.BaseContext bc = new jcifsng.context.BaseContext(new jcifsng.config.PropertyConfiguration(System.getProperties()));
            jcifsng.NetbiosAddress[] uax = bc.getNameServiceClient().getNbtAllByAddress(address);
            if (uax != null) {
                for (int i = 0; i < uax.length; i++) {
                    jcifsng.NetbiosAddress ua = uax[i];
                    String hn;
                    hn = ua.firstCalledName();
                    if (ua.getNameType() == 32) {
                        srv_name = hn;
                        break;
                    }
                }
            }
        } catch (UnknownHostException e) {
        } catch (jcifsng.CIFSException e) {
            e.printStackTrace();
        }
        return srv_name;
    }

    final static public boolean isNetbiosAddress(boolean smb1, String address) {
        if (smb1) {
            return isNbtAddressActiveSmb1(address);
        } else {
            return isNbtAddressActiveSmb2(address);
        }
    }

    final static private boolean isNbtAddressActiveSmb1(String address) {
        boolean result = false;
        try {
            jcifs.netbios.NbtAddress na = jcifs.netbios.NbtAddress.getByName(address);
            result = na.isActive();
        } catch (UnknownHostException e) {
        }
        return result;
    }

    final static private boolean isNbtAddressActiveSmb2(String address) {
        boolean result = false;
        try {
            jcifsng.context.BaseContext bc = new jcifsng.context.BaseContext(new jcifsng.config.PropertyConfiguration(System.getProperties()));
            jcifsng.NetbiosAddress na = bc.getNameServiceClient().getNbtByName(address);
            result = na.isActive(bc);
        } catch (UnknownHostException e) {
        } catch (jcifsng.CIFSException e) {
            e.printStackTrace();
        }
        return result;
    }

    final static public String[] analyzeNtStatusCode(JcifsException e, String url, String user) {
        String[] result = new String[4];

        String host_t1 = url.replace("smb://", "");
        String host_id = host_t1;
        String share_name = "";
        String file_path = "";
        if (host_t1.indexOf("/") >= 0) {
            host_id = host_t1.substring(0, host_t1.indexOf("/"));
            String share_t1 = host_t1.replace(host_id + "/", "");
            if (share_t1.indexOf("/") >= 0) {
                share_name = share_t1.substring(0, share_t1.indexOf("/"));
                String fpath_t1 = share_t1.replace(share_name + "/", "");
                file_path = fpath_t1;
            }
        }

        result[1] = host_id;
        result[2] = share_name;
        result[3] = file_path;

        String msg_text = e.getMessage();

        result[0] = msg_text + "\n" +
                "User=" + user + String.format(", NT Status 0x%h", e.getNtStatus()) + "\n" + "share=" + share_name + ", path=" + file_path;
        return result;
    }

}
