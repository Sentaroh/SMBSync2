package com.sentaroh.jcifs;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class JcifsUtil {

    public static boolean isValidIpAddress(String in) {
        if (in == null || in.isEmpty()) return false;
        try {
            return InetAddress.getByName(in).getHostAddress().equals(in);
        } catch (Exception e) {
            return false;
        }
    }

    public static String getSmbHostIpAddressByHostName(int smbLevel, String hostname) {
        try {
            return InetAddress.getByName(hostname).getHostAddress();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isIpAddressAndPortConnected(String addr, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(addr, port), timeout);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getSmbHostNameByAddress(int smbLevel, String address) {
        try {
            return InetAddress.getByName(address).getHostName();
        } catch (Exception e) {
            return address;
        }
    }

    public static boolean isNetbiosAddress(int smbLevel, String address) {
        // NetBIOS is unsupported for SMB2/3 purely
        return false;
    }

    public static String[] analyzeNtStatusCode(JcifsException e, String url, String defaultMsg) {
        // Return basic error strings
        return new String[] { "Error", e.getMessage() != null ? e.getMessage() : defaultMsg };
    }
}
