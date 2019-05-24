package com.sentaroh.android.SMBSync2;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static android.content.Context.NSD_SERVICE;

public class NsdSmbNameResolution {
    private static Logger log = LoggerFactory.getLogger(NsdSmbNameResolution.class);
    static final String SMB_SERVICE_TYPE = "_smb._tcp.";

    private NsdManager mNsdManager =null;
    private String mQueryName ="";
    private String mQueryResult ="";
    private ArrayList<String>mNsdServiceList=new ArrayList<String>();

    public NsdSmbNameResolution(Context c) {
        mNsdManager = (NsdManager) c.getSystemService(NSD_SERVICE);
    }

    public String query(String query_name, final int time_out) {
        mQueryName =query_name;
        log.debug("Query Start for Name="+query_name);
        startDiscovery();
        synchronized (mNsdManager) {
            try {
                mNsdManager.wait(time_out);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopDiscovery();
        }
        log.debug("Query result="+ mQueryResult +", Name="+query_name);
        return mQueryResult;
    }

    public ArrayList<String> list(final int time_out) {
        mQueryName ="";
        log.debug("List Start");
        startDiscovery();
        synchronized (mNsdManager) {
            try {
                mNsdManager.wait(time_out);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopDiscovery();
        }
        log.debug("List ended, count="+mNsdServiceList.size());
        return mNsdServiceList;
    }

    private void startDiscovery() {
        mNsdManager.discoverServices(SMB_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    private void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(discoveryListener);
    }

    private NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            log.trace(String.format("Failed to stop discovery serviceType=%s, errorCode=%d", serviceType, errorCode));
        }
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            log.trace(String.format("Failed to start discovery serviceType=%s, errorCode=%d", serviceType, errorCode));
        }
        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            log.trace(String.format("Service lost serviceInfo=%s", serviceInfo));
            synchronized (mNsdManager) {
                mNsdManager.notify();
            }
        }
        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            mNsdServiceList.add(serviceInfo.getServiceName());
            log.trace(String.format("Service found serviceInfo=%s", serviceInfo));
            if (serviceInfo.getServiceName().equals(mQueryName)) {
                mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        log.trace(String.format("Service resolved serviceInfo=%s", serviceInfo));
                        mQueryResult =serviceInfo.getHost().getHostAddress();
                        synchronized (mNsdManager) {
                            mNsdManager.notify();
                        }
                    }
                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        log.trace(String.format("Failed to resolve serviceInfo=%s, errorCode=%d", serviceInfo, errorCode));
                        synchronized (mNsdManager) {
                            mNsdManager.notify();
                        }
                    }
                });
            }
        }
        @Override
        public void onDiscoveryStopped(String serviceType) {
            log.trace(String.format("Discovery stopped serviceType=%s", serviceType));
            synchronized (mNsdManager) {
                mNsdManager.notify();
            }
        }
        @Override
        public void onDiscoveryStarted(String serviceType) {
            log.trace(String.format("Discovery started serviceType=%s", serviceType));
        }
    };

}
