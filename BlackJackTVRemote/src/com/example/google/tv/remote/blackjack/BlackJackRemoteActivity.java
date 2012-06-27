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

package com.example.google.tv.remote.blackjack;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.google.tv.anymotelibrary.client.AnymoteClientService;
import com.example.google.tv.anymotelibrary.client.AnymoteClientService.ClientListener;
import com.example.google.tv.anymotelibrary.client.AnymoteSender;

/**
 * The main Activity of the BlackJack remote app. It binds to the
 * AnymoteClientService service from AnymoteLibrary to establish connection with
 * Google TV devices on local network. It implements the
 * AnymoteClientService.ClientListener interface from AnymoteLibrary to receive
 * connection state notifications. It uses AnymoteSender from the AnymoteLibrary
 * to send launch BlackJack app on Google TV and send events to it.
 */
public class BlackJackRemoteActivity extends Activity implements ClientListener {
    /**
     * This manages discovering, pairing and connecting to Google TV devices on
     * network.
     */
    private AnymoteClientService mAnymoteClientService;
    private ProgressBar progressBar;
    private Context mContext;

    /**
     * Handles messages on main UI thread.
     */
    private Handler handler;

    /**
     * The proxy used to send events to the server using Anymote Protocol
     */
    private AnymoteSender anymoteSender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Save reference to the context
        mContext = this;

        // Setup the Activity UI
        setContentView(R.layout.main);
        progressBar = (ProgressBar) findViewById(R.id.a_progressbar);
        progressBar.setVisibility(View.VISIBLE);
        Button hit = (Button) findViewById(R.id.hit);
        Button stand = (Button) findViewById(R.id.stand);
        Button newgame = (Button) findViewById(R.id.newgame);

        // Setup button click listeners.
        hit.setOnClickListener(new OnClickListener() {
                @Override
            public void onClick(View v) {
                // Sends Keycode H for 'Hit'.
                sendKeyEvent(KeyEvent.KEYCODE_H);
            }
        });
        stand.setOnClickListener(new OnClickListener() {
                @Override // Sends Keycode S for 'Stand'.
            public void onClick(View v) {
                sendKeyEvent(KeyEvent.KEYCODE_S);
            }
        });
        newgame.setOnClickListener(new OnClickListener() {
                @Override // Sends Keycode N for starting new game.
            public void onClick(View v) {
                sendKeyEvent(KeyEvent.KEYCODE_N);
            }
        });

        // UI message handler
        handler = new Handler();

        // Bind to the AnymoteClientService
        Intent intent = new Intent(BlackJackRemoteActivity.this, AnymoteClientService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    private void sendKeyEvent(final int keyEvent) {
        // create new Thread to avoid network operations on UI Thread
        if (anymoteSender == null) {
            Toast.makeText(BlackJackRemoteActivity.this, "Waiting for connection",
                    Toast.LENGTH_LONG).show();
            return;
        }
        anymoteSender.sendKeyPress(keyEvent);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        /*
         * ServiceConnection listener methods.
         */
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAnymoteClientService = ((AnymoteClientService.AnymoteClientServiceBinder) service)
                    .getService();
            mAnymoteClientService.attachClientListener(BlackJackRemoteActivity.this);
        }

        public void onServiceDisconnected(ComponentName name) {
            mAnymoteClientService.detachClientListener(BlackJackRemoteActivity.this);
            mAnymoteClientService = null;
        }
    };

    @Override
    public void onConnected(final AnymoteSender anymoteSender) {
        Toast.makeText(
                BlackJackRemoteActivity.this, R.string.pairing_succeeded_toast, Toast.LENGTH_LONG)
                .show();

        this.anymoteSender = anymoteSender;

        // Send Intent to launch BlackJack app on Google TV through Anymote.
        final Intent blackJackTVLaunchIntent = new Intent("android.intent.action.MAIN");
        blackJackTVLaunchIntent.setComponent(new ComponentName(
                "com.google.android.tv.blackjack",
                "com.google.android.tv.blackjack.BlackJackTableActivity"));
        anymoteSender.sendIntent(blackJackTVLaunchIntent);

        // Hide the progressBar once connection to Google TV is established.
        handler.post(new Runnable() {
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(
                BlackJackRemoteActivity.this, R.string.pairing_failed_toast, Toast.LENGTH_LONG)
                .show();
        this.anymoteSender = null;
    }

    @Override
    public void onConnectionError() {
        this.anymoteSender = null;
    }

    @Override
    protected void onDestroy() {
        if (mAnymoteClientService != null) {
            mAnymoteClientService.detachClientListener(this);
        }
        unbindService(mConnection);
        super.onDestroy();
    }
}
