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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.google.tv.anymotelibrary.client.AnymoteSender;

/**
 * This class manages the requests for acknowledgments that are sent to the
 * Anymote server to monitor the connection state.
 */
public final class AckManager {

    /**
     * The tag used for debug logging.
     */
    private static final String LOG_TAG = "AckManager";

    /**
     * The flag which controls whether debug output should be generated.
     */
    private static final boolean DEBUG = false;

    /**
     * The listener which listens for lost connection events.
     */
    private final Listener connectionListener;

    /**
     * Inner class that handles start / stop / ping / ack / timeout messages.
     */
    private final AckHandler handler;

    /**
     * The proxy for sending Anymote events.
     */
    private final AnymoteSender sender;

    /**
     * Interface used when the connection is lost.
     */
    public interface Listener {
        /**
         * Called on connection timeout.
         */
        public void onTimeout();
    }

    /**
     * Constructor.
     * 
     * @param listener Listens for lost connection events.
     * @param sender Sends Anymote events to server.
     */
    public AckManager(final Listener listener, final AnymoteSender sender) {
        HandlerThread handlerThread = new HandlerThread("AckHandlerThread");
        handlerThread.start();
        handler = new AckHandler(handlerThread.getLooper());
        connectionListener = listener;
        this.sender = sender;
    }

    /**
     * Notifies the AckManager that a acknowledgment message has been received.
     */
    public void onAck() {
        handler.sendEmptyMessage(Action.ACK.ordinal());
    }

    /**
     * Starts monitoring connection to Anymote server.
     */
    public void start() {
        handler.sendEmptyMessage(Action.START.ordinal());
    }

    /**
     * Stops monitoring connection to Anymote server.
     */
    public void stop() {
        handler.removeMessages(Action.PING, Action.START);
    }

    /**
     * Stop the ACK handling thread.
     */
    public void quit() {
        handler.getLooper().quit();
    }

    /**
     * Notifies the listener that the connection to Anymote server has been
     * lost.
     */
    private void connectionTimeout() {
        connectionListener.onTimeout();
    }

    /**
     * Enum defining action for messages sent to AckHandler.
     */
    private enum Action {
        START, PING, ACK,
    }

    /**
     * Inner class that handles start / stop / ping / ack / timeout messages
     * from multiple threads, and serializes their execution.
     */
    private final class AckHandler extends Handler {
        /**
         * Duration between two ack requests.
         */
        private static final int PING_PERIOD = 3 * 1000;
        private int lostAcks;

        /**
         * Max number of missing requests in a row that indicade conneciton lost
         * this is more robust and only fails if server stops responding
         */
        private static final int MAX_LOST_ACKS = 3;

        AckHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Action action = actionValueOf(msg.what);
            if (DEBUG) {
                Log.d(LOG_TAG, "action=" + action + " : msg=" + msg + " @ "
                        + System.currentTimeMillis());
            }
            switch (action) {
                case START:
                    handleStart();
                    break;

                case PING:
                    handlePing();
                    break;

                case ACK:
                    handleAck();
                    break;
            }
        }

        private void handlePing() {
            sender.sendPing();
            sendMessageDelayed(obtainMessage(Action.PING), PING_PERIOD);
            ++lostAcks;
            if (lostAcks > MAX_LOST_ACKS) {
                handleTimeout();
            }
        }

        private void handleStart() {
            lostAcks = 0;
            handlePing();
        }

        private void handleTimeout() {
            removeMessages(Action.PING, Action.ACK);
            connectionTimeout();
        }

        private void handleAck() {
            lostAcks = 0;
        }

        private void removeMessages(Action... actions) {
            for (Action action : actions) {
                removeMessages(action.ordinal());
            }
        }

        private Message obtainMessage(Action action) {
            return obtainMessage(action.ordinal());
        }

        private Action actionValueOf(int what) {
            return Action.values()[what];
        }
    }
}
