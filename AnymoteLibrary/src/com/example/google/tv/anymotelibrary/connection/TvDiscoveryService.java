/*
 * Copyright (C) 2012 Google Inc.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.google.tv.anymotelibrary.connection;

import android.app.Activity;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.google.tv.anymotelibrary.R;
import com.example.google.tv.anymotelibrary.client.AnymoteClientService;
import com.example.google.tv.anymotelibrary.connection.BroadcastDiscoveryClient.BroadcastAdvertisement;
import com.example.google.tv.anymotelibrary.connection.BroadcastDiscoveryClient.DeviceDiscoveredListener;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Service which discovers Google TV devices on the local network.
 */
public class TvDiscoveryService extends Handler {

    /**
     * Tag for debug logging.
     */
    private static final String LOG_TAG = "TvDiscoveryService";

    /**
     * Anymote service name.
     */
    private static final String SERVICE_TCP = "_anymote._tcp";

    /**
     * The service that handles connection to the TV device and sends events to
     * it.
     */
    private final AnymoteClientService coreService;

    /**
     * The Broadcast client that listens for L3 broadcasts for Anymote service
     * on the network.
     */
    private BroadcastDiscoveryClient broadcastClient;

    /**
     * The thread that handles network communications.
     */
    private Thread broadcastThread;

    /**
     * The wifi connectivity manager.
     */
    WifiManager wifiManager;

    /**
     * All discovered TVs are stored in this list.
     */
    private List<TvDevice> devices;

    /**
     * Constructor
     * 
     * @param coreService The service that handles connectivity to the TV
     *            device.
     */
    public TvDiscoveryService(AnymoteClientService coreService) {
        this.coreService = coreService;
        devices = new ArrayList();
        wifiManager = (WifiManager) coreService.getSystemService(Activity.WIFI_SERVICE);
    }

    /**
     * Enum that declares internal messages.
     */
    private enum RequestType {

        BROADCAST_TIMEOUT,
    }

    /**
     * Sends message to the handler.
     * 
     * @param type
     */
    private void sendMessage(RequestType type) {
        sendMessage(type, null, 0);
    }

    /**
     * Lock object to synchronize threads.
     */
    Object broadCastSync = new Object();

    /**
     * Send messages to the handler with a delay.
     * 
     * @param type
     * @param obj
     * @param timeout
     */
    private void sendMessage(RequestType type, Object obj, long timeout) {
        Message message = obtainMessage(type.ordinal(), obj);
        if (timeout != 0) {
            super.sendMessageDelayed(message, timeout);
        } else {
            super.sendMessage(message);
        }
    }

    /**
     * The looper for thread that discovers Google TV devices offering Anymote
     * service on the local network.
     */
    DiscoveryLooper looper = new DiscoveryLooper();

    /**
     * Returns a list of Google TV devices offering Anymote service on the local
     * network.
     * 
     * @return list of TV devices
     */
    public List<TvDevice> discoverTvs() {
        looper.start();
        try {
            synchronized (broadCastSync) {
                broadCastSync.wait();
            }

        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted while scanning for tvs");
            // Return empty array list as this.devices might be unsafe
            // because of background looper, which may still be writing to it.
            return new ArrayList<TvDevice>();
        }
        return devices;
    }

    /**
     * Called when network scan for discovering Google TV devices is completed.
     */
    public void onDeviceScanComplete() {
        stopBroadcast();
    }

    /**
     * Called when a Google TV device is found on local network.
     * 
     * @param dev
     */
    public void onDeviceFound(TvDevice dev) {
        devices.add(dev);
    }

    /**
     * Stops looking for Google TV devices on the network.
     */
    private synchronized void stopBroadcast() {
        if (broadcastClient != null) {
            Log.i(LOG_TAG, "Disabling broadcast");
            broadcastClient.stop();
            broadcastClient = null;
            try {
                broadcastThread.join(1000);
            } catch (InterruptedException e) {
                Log.i(LOG_TAG, "Timeout while waiting for thread execution to complete");
            }
            broadcastThread = null;
            onDeviceScanComplete();
        }
    }

    /**
     * Starts scanning the local network for Google TV devices.
     */
    private synchronized void startBroadcast() {
        Inet4Address broadcastAddress = getBroadcastAddress();
        if (broadcastAddress == null) {
            stopBroadcast();
            return;
        }
        if (broadcastClient == null) {
            Log.i(LOG_TAG, "Enabling broadcast");
            broadcastClient = new BroadcastDiscoveryClient(broadcastAddress, getServiceName());
            broadcastClient.setDeviceDiscoveredListener(new DeviceDiscoveredListener() {
                public void onDeviceDiscovered(BroadcastAdvertisement advert) {
                    TvDevice remoteDevice = getDeviceFromAdvert(advert);
                    Log.i(LOG_TAG, "Found wireless device: " + remoteDevice.getName());
                    onDeviceFound(remoteDevice);
                }
            });

            broadcastThread = new Thread(broadcastClient);
            broadcastThread.start();
            int broadcastTimeout = coreService.getResources().getInteger(
                    R.integer.broadcast_timeout);
            sendMessage(RequestType.BROADCAST_TIMEOUT, null, broadcastTimeout);
        }
    }

    /**
     * Internal Looper thread that does the discovery
     */
    private class DiscoveryLooper extends Thread {
        public Handler mHandler;

        @Override
        public void run() {
            startBroadcast();
            if (getBroadcastAddress() == null) {
                devices = null;
                return;
            }

        }
    }

    public void handleMessage(Message msg) {
        RequestType request = RequestType.values()[msg.what];

        if (request == RequestType.BROADCAST_TIMEOUT) {
            stopBroadcast();
            synchronized (broadCastSync) {
                broadCastSync.notifyAll();
            }
        }
    }

    /**
     * Extracts Device defination from network broadcast.
     * 
     * @param adv network broadcast
     * @return TV device instance
     */
    protected TvDevice getDeviceFromAdvert(BroadcastAdvertisement adv) {
        return new TvDevice(adv.getServiceName(), adv.getServiceAddress(), adv.getServicePort());
    }

    /**
     * Checks if wifi connectivity is available.
     * 
     * @return boolean indicating if wifi is available.
     */
    protected boolean isWifiAvailable() {
        if (wifiManager.isWifiEnabled()) {
            WifiInfo info = wifiManager.getConnectionInfo();
            return (info != null && info.getIpAddress() != 0);
        }
        return false;
    }

    /**
     * Retuns wifi network name.
     * 
     * @return network name.
     */
    protected String getNetworkName() {
        if (!isWifiAvailable()) {
            return null;
        }
        WifiInfo info = wifiManager.getConnectionInfo();
        return (info != null) ? info.getSSID() : null;
    }

    /**
     * Returns the IP address where network broadcasts are sent.
     * 
     * @return IP address for broadcasts.
     */
    protected Inet4Address getBroadcastAddress() {
        Inet4Address broadcastAddress;
        if (!isWifiAvailable()) {
            return null;
        }

        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        if (dhcp == null) {
            return null;
        }

        int broadcast = dhcp.ipAddress | ~dhcp.netmask;
        byte[] broadcastOctets;

        if (java.nio.ByteOrder.nativeOrder() == java.nio.ByteOrder.BIG_ENDIAN) {
            broadcastOctets = new byte[] {
                    (byte) ((broadcast >> 24) & 0xff),
                    (byte) ((broadcast >> 16) & 0xff), (byte) ((broadcast >> 8) & 0xff),
                    (byte) (broadcast & 0xff) };
        } else {
            broadcastOctets = new byte[] {
                    (byte) (broadcast & 0xff),
                    (byte) ((broadcast >> 8) & 0xff), (byte) ((broadcast >> 16) & 0xff),
                    (byte) ((broadcast >> 24) & 0xff) };
        }

        try {
            broadcastAddress = (Inet4Address) InetAddress.getByAddress(broadcastOctets);
        } catch (IOException e) {
            broadcastAddress = null;
        }
        return broadcastAddress;
    }

    /**
     * Returns Anymote service name.
     * 
     * @return Anymote service name.
     */
    protected String getServiceName() {
        return SERVICE_TCP;
    }
}
