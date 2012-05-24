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

import com.google.anymote.Key.Action;
import com.google.anymote.Key.Code;
import com.google.anymote.Messages.DataItem;
import com.google.anymote.Messages.DataList;
import com.google.anymote.Messages.FlingResult;
import com.google.anymote.common.AnymoteFactory;
import com.google.anymote.common.ConnectInfo;
import com.google.anymote.common.ErrorListener;
import com.google.anymote.device.DeviceAdapter;
import com.google.anymote.device.MessageReceiver;

import android.content.Intent;
import android.util.Log;

import com.example.google.tv.anymotelibrary.connection.AckManager;
import com.example.google.tv.anymotelibrary.connection.AckManager.Listener;
import com.example.google.tv.anymotelibrary.connection.ConnectingTask;
import com.example.google.tv.anymotelibrary.util.KeyEventTranslator;

import java.io.IOException;

import javax.net.ssl.SSLSocket;

/**
 * A proxy class that sends messages to the Anymote server using Anymote
 * protocol.
 */
public final class AnymoteSender implements MessageReceiver {

    private static final String LOG_TAG = AnymoteSender.class.getSimpleName();

    /** Data type used to send a string in a data message. */
    public static final String DATA_TYPE_STRING = "com.google.tv.string";

    /** Device name used upon connection. */
    public static final String DEVICE_NAME = "android";

    /** Manages connection to the server */
    private final ConnectingTask connectingTask;

    /** Error listener for Anymote protocol */
    private final ErrorListener errorListener;

    /** Sender for Anymote protocol */
    private DeviceAdapter deviceAdapter;

    /** ACK manager (ping etc) */
    private AckManager ackManager;

    /** Remote device protocol version number */
    private int deviceVersion;

    /**
     * Constructor
     * 
     * @param task The Thread that handles connectivity to the TV device.
     */
    public AnymoteSender(ConnectingTask task) {
        connectingTask = task;
        errorListener = new ErrorListener() {
            public void onIoError(String message, Throwable exception) {
                Log.d(LOG_TAG, "IoError: " + message, exception);
                onConnectionError();
            }
        };
        ackManager = new AckManager(new Listener() {
            public void onTimeout() {
                ackManager.stop();
                onConnectionError();
            }
        }, this);
    }

    /**
     * Attempts to establish connection with the Anymote service socket.
     * 
     * @param sslSocket TV device socket.
     * @return boolean indicating if connection to socket was successful.
     */
    public boolean attemptToConnect(final SSLSocket sslSocket) {
        if (sslSocket == null) {
            throw new NullPointerException("null socket");
        }
        return instantiateProtocol(sslSocket);
    }

    private boolean instantiateProtocol(SSLSocket sslSocket) {
        disconnect();

        try {
            deviceAdapter = AnymoteFactory.getDeviceAdapter(
                    this, sslSocket.getInputStream(), sslSocket.getOutputStream(), errorListener);
        } catch (IOException e) {
            Log.d(LOG_TAG, "Unable to create sender", e);
            deviceAdapter = null;
            return false;
        }

        sendConnect();
        ackManager.start();
        return true;
    }

    /**
     * Disconnects from Anymote service.
     * 
     * @return boolean indicating if the device was successfully disconnected.
     */
    public synchronized boolean disconnect() {

        ackManager.stop();
        if (deviceAdapter != null) {
            deviceAdapter.stop();
            deviceAdapter = null;
            return true;
        }
        return false;
    }

    /**
     * Destroys the connection to anymote service.
     */
    public void destroy() {
        disconnect();
        ackManager.quit();
    }

    private void onConnectionError() {

        if (disconnect()) {
            connectingTask.onConnectionDisconnected();
        }
    }

    /**
     * Sends click event to Anymote service.
     * 
     * @param action
     */
    public void sendClick(final Action action) {
        if (deviceAdapter != null) {
            deviceAdapter.sendKeyEvent(Code.BTN_MOUSE, action);
        }
    }

    /**
     * Sends Url to Anymote service. This is url is a serialzed representation
     * of Intent.
     * 
     * @param url
     */
    public void sendUrl(final String url) {
        if (deviceAdapter != null) {
            deviceAdapter.sendFling(url, 0);
        }
    }

    /**
     * Converts Intent to String and sends it to deviceAdapter.
     * 
     * @param intent The Intent to be sent to Anymote service.
     */
    public void sendIntent(Intent intent) {
        sendUrl(intent.toUri(Intent.URI_INTENT_SCHEME));
    }

    /**
     * Converts Android KeyEvent.KeyCode to Anymote Key.Code and sends it to
     * deviceAdapter.
     * 
     * @param keyEvent The key event to be sent to Anymote service.
     */
    public void sendKeyPress(int keyEvent) {
        sendKeyPress(KeyEventTranslator.fromKeyEvent(keyEvent));
    }

    /**
     * Sends key to Anymote service.
     * 
     * @param keycode The keycode of the key to be sent.
     * @param action The key up/down action.
     */
    public void sendKey(final Code keycode, final Action action) {
        if (deviceAdapter != null) {
            deviceAdapter.sendKeyEvent(keycode, action);
        }
    }

    /**
     * Sends key press event to Anymote service.
     * 
     * @param key code of the key that was pressed.
     */
    public void sendKeyPress(final Code key) {
        if (deviceAdapter != null) {
            deviceAdapter.sendKeyEvent(key, Action.DOWN);
            deviceAdapter.sendKeyEvent(key, Action.UP);
        }
    }

    /**
     * Sends relative mouse move event to Anymote service.
     * 
     * @param deltaX the delta between intial and final x positions of the the
     *            mouse movement.
     * @param deltaY the delta between intial and final y positions of the the
     *            mouse movement.
     */
    public void sendMoveRelative(final int deltaX, final int deltaY) {
        if (deviceAdapter != null) {
            deviceAdapter.sendMouseMove(deltaX, deltaY);
        }
    }

    /**
     * Sends scroll event to Anymote service.
     * 
     * @param deltaX the delta between intial and final x positions of the the
     *            scroll movement.
     * @param deltaY the delta between intial and final y positions of the the
     *            scroll movement.
     */
    public void sendScroll(final int deltaX, final int deltaY) {
        if (deviceAdapter != null) {
            deviceAdapter.sendMouseWheel(deltaX, deltaY);
        }
    }

    /**
     * Sends ping to Anymote service to monitor connection state.
     */
    public void sendPing() {
        if (deviceAdapter != null) {
            deviceAdapter.sendPing();
        }
    }

    private void sendConnect() {
        if (deviceAdapter != null) {
            deviceAdapter.sendConnect(
                    new ConnectInfo(DEVICE_NAME, connectingTask.getVersionCode()));
        }
    }

    public void onAck() {
        ackManager.onAck();
    }

    public void onData(String type, String data) {
        Log.d(LOG_TAG, "onData: " + type + " / " + data);
    }

    public void onDataList(DataList dataList) {
        Log.d(LOG_TAG, "onDataList: " + dataList.getType());
        for (DataItem dataItem : dataList.getItemList()) {
            for (String string : dataItem.getStringFieldList()) {
                Log.d(LOG_TAG, " data item [string]: " + string);
            }
            for (int i : dataItem.getIntFieldList()) {
                Log.d(LOG_TAG, " data item [int]: " + i);
            }
        }
    }

    /**
     * Called when connection to Anymote service is established.
     * 
     * @param connectInfo The Anymote connection info.
     */
    public void onConnect(final ConnectInfo connectInfo) {
        Log.d(LOG_TAG, "onConnect: " + connectInfo.toString() + " "
                + connectInfo.getVersionNumber());
        deviceVersion = connectInfo.getVersionNumber();

        ackManager.start();
    }

    public void onFlingResult(FlingResult flingResult, Integer sequenceNumber) {
        Log.d(LOG_TAG, "onFlingResult: " + sequenceNumber);
    }
}
