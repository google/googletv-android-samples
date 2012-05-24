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

package com.example.google.tv.anymotelibrary.connection;

import com.google.polo.exception.PoloException;
import com.google.polo.pairing.ClientPairingSession;
import com.google.polo.pairing.PairingContext;
import com.google.polo.pairing.PairingListener;
import com.google.polo.pairing.PairingSession;
import com.google.polo.pairing.message.EncodingOption;
import com.google.polo.ssl.DummySSLSocketFactory;
import com.google.polo.wire.PoloWireInterface;
import com.google.polo.wire.WireFormat;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import com.example.google.tv.anymotelibrary.client.AnymoteSender;
import com.example.google.tv.anymotelibrary.client.AnymoteClientService.ClientListener;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * This task covers entire connection mechanism, including pairing, when
 * necessary.
 */
public class ConnectingTask extends Thread {
    private static final String REMOTE_NAME = Build.MANUFACTURER + " " + Build.MODEL;
    private static final int RECONNECTION_DELAY_MS = 1000;
    private static final int MAX_CONNECTION_ATTEMPTS = 3;
    private static final String LOG_TAG = "ConnectingActivity";

    private final Object secretSync;
    private final AnymoteSender anymoteProxy;
    private final KeyStoreManager keyStore;

    private TvDevice target;
    private ConnectionListener listener;
    private boolean isCancelled;
    private String secret;
    private SSLSocket sslsock;
    private Context context;

    /**
     * Connection status enumeration.
     */
    public enum ConnectionStatus {
        /**
         * Connection successful.
         */
        SUCCESS,
        /**
         * Error while creating socket or establishing connection.
         */
        ERROR,
        /**
         * Error during SSL handshake.
         */
        NEEDS_PAIRING
    }

    /**
     * Device pairing result.
     */
    public enum PairingStatus {
        /**
         * Pairing successful
         */
        PAIRING_SUCCESS,
        /**
         * Pairing failed due to connection issues
         */
        FAILED_CONNECTION,
        /**
         * Pairing failed due to secret mismatch
         */
        FAILED_SECRET,
        /**
         * User cancelled pairing
         */
        FAILED_CANCELLED,
    }

    /**
     * Connection listener. This listener receives all calls when connection
     * state changes.
     */
    public interface ConnectionListener {
        /**
         * Connection to target device has been established.
         * 
         * @param device the device to which we connected.
         * @param anymoteProxy
         */
        void onConnected(TvDevice device, AnymoteSender anymoteProxy);

        /**
         * Connection to target device failed.
         */
        void onConnectionFailed();

        /**
         * Target device requires secret to continue connecting.
         * 
         * @param pinListener
         */
        void onSecretRequired(PairingPINDialogBuilder.PinListener pinListener);

        /**
         * Called when pairing session is started.
         */
        void onConnectionPairing();

        /**
         * Connection to target device disconnected.
         */
        void onConnectionDisconnected();
    }

    /**
     * Constructor.
     * 
     * @param device target device to which we want to connect to.
     * @param keystoreManager key store manager for maintaining server/client
     *            certificates.
     * @param context context of the foreground Activity which wants to send
     *            events to the server
     */
    public ConnectingTask(TvDevice device, KeyStoreManager keystoreManager, Context context) {
        this.context = context;
        target = device;
        isCancelled = false;
        secretSync = new Object();
        secret = null;
        keyStore = keystoreManager;
        anymoteProxy = new AnymoteSender(this);
    }

    /**
     * Initialize background connection; notify the listener about results.
     */
    @Override
    public void run() {
        Looper.prepare();
        boolean state = connect();
        state = anymoteProxy.attemptToConnect(sslsock);
        if (isCancelled) {
            disconnect();
        } else {
            if (listener != null) {
                if (state) {

                    listener.onConnected(target, anymoteProxy);

                } else {
                    listener.onConnectionFailed();
                }
            }
        }
    }

    /**
     * Loops to connect to the server until connection is established or max
     * allowed attempts are made.
     * 
     * @return true, if connection succeeded.
     */
    protected boolean connect() {
        PairingStatus pairingStatus = attemptToPair(new PairingListenerImpl());
        if (pairingStatus != PairingStatus.PAIRING_SUCCESS) {
            Log.i(LOG_TAG, "Pairing failed");
            return false;
        }
        for (int connectionAttempt = 0; connectionAttempt < MAX_CONNECTION_ATTEMPTS;) {
            /*
             * wait on every next iteration; placed here so we don't wait after
             * final one
             */
            try {
                if (connectionAttempt > 0) {
                    // Give server time to accept connection if we just paired
                    Thread.sleep(RECONNECTION_DELAY_MS);
                }
            } catch (InterruptedException e) {
                return false;
            }
            if (isCancelled) {
                return false;
            }
            if (attemptToConnect() == ConnectionStatus.SUCCESS) {
                Log.i(LOG_TAG, "Connected to " + target.toString());
                return true;
            }
            connectionAttempt++;
        }
        Log.i(LOG_TAG, "Connection failed");
        return false;
    }

    /**
     * Attempts to establish pairing with the server.
     * 
     * @param listener Listener for device pairing state.
     * @return PairingStatus device pairing result.
     */
    public PairingStatus attemptToPair(final PairingListenerImpl listener) {
        PairingStatus result = PairingStatus.FAILED_CONNECTION;
        SSLSocketFactory socketFactory;
        SSLSocket socket;
        PairingContext context;

        try {
            try {
                socketFactory = DummySSLSocketFactory.fromKeyManagers(keyStore.getKeyManagers());
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Cannot build socket factory", e);
            }

            Socket s =
                    new java.net.Socket(target.getAddress().getHostAddress(), target.getPort() + 1);
            socket = (SSLSocket) socketFactory.createSocket(
                    s, target.getAddress().getHostAddress(), target.getPort() + 1, true);

            context = PairingContext.fromSslSocket(socket, false);

            PoloWireInterface protocol = WireFormat.PROTOCOL_BUFFERS.getWireInterface(context);
            ClientPairingSession pairingSession =
                    new ClientPairingSession(protocol, context, "AnyMote", REMOTE_NAME);

            EncodingOption hexEnc =
                    new EncodingOption(EncodingOption.EncodingType.ENCODING_HEXADECIMAL, 4);
            pairingSession.addInputEncoding(hexEnc);
            pairingSession.addOutputEncoding(hexEnc);

            boolean ret = pairingSession.doPair(listener);
            if (ret) {
                keyStore.storeCertificate(context.getServerCertificate());
                result = PairingStatus.PAIRING_SUCCESS;
            } else {
                if (listener.isFailedSecret()) {
                    result = PairingStatus.FAILED_SECRET;
                } else {
                    result = PairingStatus.FAILED_CANCELLED;
                }
            }
        } catch (UnknownHostException e) {
            Log.e(LOG_TAG, "Unknown host. Failed to connect", e);
            result = PairingStatus.FAILED_CONNECTION;
        } catch (PoloException e) {
            Log.e(LOG_TAG, "Polo exception", e);
            result = PairingStatus.FAILED_CONNECTION;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to connect", e);
            result = PairingStatus.FAILED_CONNECTION;
        }
        return result;
    }

    /**
     * Cancel current connection.
     */
    public void cancel() {
        disconnect();
        // Interrupt thread in case it's pending on pairing code.
        synchronized (this) {
            this.interrupt();
        }
        isCancelled = true;
    }

    /**
     * Attach the connection listener to this task.
     * 
     * @param listener the listener to be called.
     */
    public void setConnectionListener(ConnectionListener listener) {
        this.listener = listener;
    }

    /**
     * Set secret (PIN, passphrase) which is required for pairing devices. This
     * method is called when the user enters secret code in the
     * PairingPinDialog.
     * 
     * @param secret the secret passphrase provided by user.
     */
    public void setSecret(String secret) {
        this.secret = secret;

        synchronized (secretSync) {
            secretSync.notify();
        }
    }

    /**
     * Service lost existing connection.
     */
    public void onConnectionDisconnected() {

        disconnect();

        if (listener != null) {
            listener.onConnectionDisconnected();
        }

    }

    /**
     * Attempts to establish connection the Anymote server.
     * 
     * @return result of connection attempt.
     */
    public ConnectionStatus attemptToConnect() {
        ConnectionStatus status = ConnectionStatus.ERROR;

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyStore.getKeyManagers(), keyStore.getTrustManagers(), null);
            SSLSocketFactory factory = sslContext.getSocketFactory();
            sslsock = (SSLSocket) factory.createSocket(
                    target.getAddress().getHostAddress(), target.getPort());
            sslsock.setUseClientMode(true);
            sslsock.setKeepAlive(true);
            sslsock.setTcpNoDelay(true);
            sslsock.startHandshake();

            if (sslsock.isConnected()) {
                status = ConnectionStatus.SUCCESS;
            }
        } catch (NoSuchAlgorithmException e) {
            status = ConnectionStatus.ERROR;
        } catch (KeyManagementException e) {
            status = ConnectionStatus.ERROR;
        } catch (SSLException e) {
            Log.e(LOG_TAG, "(SSL) Could not create socket to " + target.getName(), e);
            status = ConnectionStatus.NEEDS_PAIRING;
        } catch (ConnectException e) {
            Log.e(LOG_TAG, "(IOE) Could not create socket to " + target.getName(), e);
            status = ConnectionStatus.ERROR;
        } catch (IOException e) {
            if (e.getMessage().startsWith("SSL handshake")) {
                Log.e(LOG_TAG, "(IOE) SSL handshake failed while connecting to " + target.getName(),
                        e);
                status = ConnectionStatus.NEEDS_PAIRING;
            } else {
                Log.e(LOG_TAG, "(IOE) Could not create socket to " + target.getName(), e);
                status = ConnectionStatus.ERROR;
            }
        }

        if (status != ConnectionStatus.SUCCESS) {
            if (sslsock != null && sslsock.isConnected()) {
                try {
                    sslsock.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "(IOE) Could not close socket", e);
                }
            }
            sslsock = null;
        }
        return status;
    }

    /**
     * Disconnect from the Anymote server.
     */
    public void disconnect() {
        new Thread(new Runnable() {
                @Override
            public void run() {
                if (anymoteProxy != null) {
                    anymoteProxy.destroy();
                }
                try {
                    if (sslsock != null) {
                        sslsock.close();
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "(IOE) Failed to close socket", e);
                }
                sslsock = null;
            }

        }).start();
    }

    /**
     * Listens for events sent during the pairing session. pairing listener
     */
    private class PairingListenerImpl
            implements PairingListener, PairingPINDialogBuilder.PinListener {
        boolean failedSecret;
        private String secret;
        private static final int SECRET_WAIT_TIMEOUT_MS = 60 * 1000;
        private final Object secretSync;

        public PairingListenerImpl() {
            secretSync = new Object();
            failedSecret = false;
        }

        public boolean isFailedSecret() {
            return failedSecret;
        }

        public void onSessionEnded(PairingSession session) {
            Log.d(LOG_TAG, "onSessionEnded: " + session);
        }

        public void onSessionCreated(PairingSession session) {
            Log.d(LOG_TAG, "onSessionCreated: " + session);
        }

        public void onPerformOutputDeviceRole(PairingSession session, byte[] gamma) {
            Log.d(LOG_TAG, "onPerformOutputDeviceRole: " + session + ", "
                    + session.getEncoder().encodeToString(gamma));
        }

        public void onPerformInputDeviceRole(PairingSession session) {

            Looper.prepare();
            // this listener is implemented by the main Activity which
            // shows Pairing PIN dialog to the user to enter secret code.
            listener.onSecretRequired(this);
            // wait for user to enter secret code.
            synchronized (secretSync) {
                try {
                    secretSync.wait(SECRET_WAIT_TIMEOUT_MS);
                } catch (InterruptedException e) {
                    // secret is already null.
                }
            }
            // check if the secret was entered correctly.
            Log.d(LOG_TAG, "Got: " + secret);
            if (secret != null && secret.length() > 0) {
                try {
                    byte[] secretBytes = session.getEncoder().decodeToBytes(secret);
                    session.setSecret(secretBytes);
                    failedSecret = !session.hasSucceeded();
                } catch (IllegalArgumentException exception) {
                    Log.d(LOG_TAG, "Exception while decoding secret: ", exception);
                    session.teardown();
                } catch (IllegalStateException exception) {
                    // ISE may be thrown when session is currently terminating
                    Log.d(LOG_TAG, "Exception while setting secret: ", exception);
                    session.teardown();
                }
            } else {
                session.teardown();
            }
        }

        public void onLogMessage(LogLevel level, String message) {
            Log.d(LOG_TAG, "Log: " + message + " (" + level + ")");
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onSecretEntered(String secret) {
            this.secret = secret;

            synchronized (secretSync) {
                secretSync.notify();
            }
        }
    }

    /**
     * Returns the version number as defined in Android manifest.
     * {@code versionCode}
     * 
     * @return versionCode
     */
    public int getVersionCode() {
        try {
            PackageInfo info =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            Log.d(LOG_TAG, "cannot retrieve version number, package name not found");
        }
        return -1;
    }

}
