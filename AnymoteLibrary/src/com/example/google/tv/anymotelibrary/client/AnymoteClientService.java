/*
 * Copyright (C) 2012 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.google.tv.anymotelibrary.client;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.google.tv.anymotelibrary.connection.ConnectingTask;
import com.example.google.tv.anymotelibrary.connection.KeyStoreManager;
import com.example.google.tv.anymotelibrary.connection.PairingActivity;
import com.example.google.tv.anymotelibrary.connection.PairingPINDialogBuilder;
import com.example.google.tv.anymotelibrary.connection.TvDevice;
import com.example.google.tv.anymotelibrary.connection.TvDiscoveryService;
import com.example.google.tv.anymotelibrary.connection.ConnectingTask.ConnectionListener;
import com.example.google.tv.anymotelibrary.connection.PairingPINDialogBuilder.PinListener;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * The central point to connect to Anymote serivce running on a Google TV device
 * and send commands. The clients of this library should bind to this service
 * and implement the ClientListener interface provided in this service.
 */
public class AnymoteClientService extends Service implements ConnectionListener {
    private static final String LOG_TAG = "AnymoteConnectionService";
    private List<ClientListener> clientListeners;
    private List<PairingListener> pairingListeners;

    private ConnectingTask connectingTask;

    private Context context;
    private TvDiscoveryService tvDiscovery;
    private TvDevice target;
    private KeyStoreManager mKeyStoreManager;
    private static AnymoteSender anymoteSender;

    /**
     * All client applications should implement this listener. It provides
     * callbacks when the state of connection to the Anymote service running on
     * Google TV device changes.
     */
    public interface ClientListener {
        /**
         * This callback method is called when connection to Anymote service has
         * been established.
         * 
         * @param anymoteSender The proxy to send Anymote messages.
         */
        public void onConnected(AnymoteSender anymoteSender);

        /**
         * This callback method is called when connection to Anymote service is
         * lost.
         */
        public void onDisconnected();

        /**
         * This callback method is called when there was a error in establishing
         * connection to the Anymote service.
         */
        public void onConnectionFailed();

    }

    /**
     * The Listener for Pairing stage.
     */
    public interface PairingListener {

        /**
         * This callback method is called when the user is required to enter the
         * secret paring code.
         * 
         * @param pairingListener
         */
        public void onPairingCodeRequired(PairingPINDialogBuilder.PinListener pairingListener);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    /*
     * (non-Javadoc)
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (connectingTask != null) {
            connectingTask.disconnect();
        }
        tvDiscovery = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new AnymoteClientServiceBinder();
    }

    /**
     * @author mjoshi@google.com (Megha Joshi)
     */
    public class AnymoteClientServiceBinder extends Binder {
        /**
         * Local binder to the service.
         * 
         * @return binder to the service.
         */
        public AnymoteClientService getService() {
            return AnymoteClientService.this;
        }
    }

    private void initialize() {
        clientListeners = new ArrayList<ClientListener>();
        pairingListeners = new ArrayList<PairingListener>();

        try {
            mKeyStoreManager = new KeyStoreManager();
            mKeyStoreManager.initialize(this);
        } catch (GeneralSecurityException e) {
            Log.e(LOG_TAG, "Security exception during initialization! Aborting", e);
            stopSelf();
            return;
        }

        Intent intent2 = new Intent();
        intent2.setComponent(new ComponentName(
                getApplicationContext(),
                "com.example.google.tv.anymotelibrary.connection.PairingActivity"));
        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        getApplication().startActivity(intent2);
    }

    /**
     * Service lost existing connection.
     */
    @Override
    public void onConnectionDisconnected() {
        this.anymoteSender = null;
        if (target != null) {
            for (ClientListener listener : clientListeners) {
                listener.onDisconnected();
            }
            target = null;
        }
    }

    /**
     * Initiate new connection to specified TV device.
     * 
     * @param device the device to connect to.
     * @param activity which uses the connection.
     * @return {@code true} if already connected to the specified device.
     */
    public boolean connect(TvDevice device, Context activity) {
        if (target != null && target.equals(device)) {
            return true;
        }
        this.context = activity;

        if (connectingTask != null) {
            connectingTask.cancel();
            connectingTask = null;
        }

        target = null;
        connectingTask = new ConnectingTask(device, mKeyStoreManager, activity);
        connectingTask.setConnectionListener(this);
        connectingTask.start();
        return false;
    }

    /**
     * Re-establish connection to current target.
     */
    public void reconnect() {
        TvDevice device = target;

        if (device != null) {
            connect(device, context);
        }
    }

    /**
     * The TV device that is connected.
     * 
     * @return connected TV device.
     */
    public TvDevice getCurrentDevice() {
        return target;
    }

    /**
     * Adds client listeners.
     * 
     * @param listener client listener.
     */
    public void attachClientListener(ClientListener listener) {
        clientListeners.add(listener);
    }

    /**
     * Removes client listener.
     * 
     * @param listener client listener.
     */
    public void detachClientListener(ClientListener listener) {
        clientListeners.remove(listener);
    }

    /**
     * Adds pairing listeners.
     * 
     * @param listener pairing listener.
     */
    public void attachPairingListener(PairingListener listener) {
        pairingListeners.add(listener);
    }

    /**
     * Removes pairing listeners.
     * 
     * @param listener
     */
    public void detachPairingListener(PairingListener listener) {
        pairingListeners.remove(listener);
    }

    /**
     * Called by anybody who wants to cancel pending connection.
     */
    public void cancelConnection() {
        if (connectingTask != null) {
            connectingTask.cancel();
            connectingTask = null;
        }
    }

    /**
     * Called by Pairing PIN Dialog when user provides secret.
     * 
     * @param secret The secret entered by the user.
     */
    public void setPairingSecret(String secret) {
        if (connectingTask != null) {
            connectingTask.setSecret(secret);
        }
    }

    /**
     * Called when connecting task successfully established connection.
     */
    public void onConnected(TvDevice device, AnymoteSender anymoteSender) {
        target = device;
        this.anymoteSender = anymoteSender;
        // Broadcast new connection.
        for (ClientListener listener : clientListeners) {
            listener.onConnected(anymoteSender);
        }
    }

    public static AnymoteSender getAnymoteSender() {
        return anymoteSender;
    }

    /**
     * Returns instance of TV discovery service, creates new instance if one
     * does not already exist.
     * 
     * @return instance of TV discovery service.
     */
    public synchronized TvDiscoveryService getTvDiscovery() {
        if (tvDiscovery == null) {
            tvDiscovery = new TvDiscoveryService(this);
        }
        return tvDiscovery;
    }

    @Override
    public void onSecretRequired(PinListener pinListener) {
        for (PairingListener listener : pairingListeners) {
            listener.onPairingCodeRequired(pinListener);
        }
    }

    @Override
    public void onConnectionFailed() {
        this.anymoteSender = null;
        for (ClientListener listener : clientListeners) {
            listener.onConnectionFailed();
        }
    }

    @Override
    public void onConnectionPairing() {
    }

}
