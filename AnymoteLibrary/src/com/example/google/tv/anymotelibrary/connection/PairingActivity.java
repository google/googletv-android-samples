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
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.google.tv.anymotelibrary.client.AnymoteClientService;

/**
 * This Activity handles Pairing with the remote TV device. It also handles
 * displaying the device selection dialog.
 */
public class PairingActivity extends Activity
        implements DeviceSelectDialog.DeviceSelectListener, AnymoteClientService.PairingListener {
    /**
     * This manages discovering, pairing and connecting to Google TV devices on
     * network.
     */
    private AnymoteClientService mConnectionManager;
    /**
     * This dialog allows the user to select one of the TV devices on network to
     * connect to.
     */
    private DeviceSelectDialog mDeviceSelectDialog;
    private ProgressBar progressBar;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

        // bind to the ConnectionManager
        RelativeLayout layout = new RelativeLayout(this);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        progressBar.setLayoutParams(params);
        layout.addView(progressBar);
        setContentView(layout);
        Intent intent = new Intent(PairingActivity.this, AnymoteClientService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        /*
         * ServiceConnection listener methods.
         */
        public void onServiceConnected(ComponentName name, IBinder service) {
            AnymoteClientService.AnymoteClientServiceBinder binder =
                    (AnymoteClientService.AnymoteClientServiceBinder) service;

            mConnectionManager = binder.getService();
            mConnectionManager.attachPairingListener(PairingActivity.this);
            // progressBar.setVisibility(View.INVISIBLE);
            selectDevice();

        }

        public void onServiceDisconnected(ComponentName name) {
            if (mConnectionManager != null) {
                mConnectionManager.detachPairingListener(PairingActivity.this);
            }
            mConnectionManager = null;
            mDeviceSelectDialog.setTvDiscovery(null);
        }
    };

    /**
     * Shows the device selection dialog.
     */
    public void selectDevice() {
        mDeviceSelectDialog = new DeviceSelectDialog(this);
        mDeviceSelectDialog.setDeviceSelectListener(this);
        mDeviceSelectDialog.setTvDiscovery(mConnectionManager.getTvDiscovery());
        mDeviceSelectDialog.show();
    }

    public void onDeviceSelected(TvDevice device) {

        progressBar.setVisibility(View.VISIBLE);
        mConnectionManager.connect(device, this);
        // dismiss the device selection dialog.
        mDeviceSelectDialog.dismiss();
    }

    public void onDeviceSelectCancelled() {
    }

    public void onPairingCodeRequired(
            final PairingPINDialogBuilder.PinListener pairingPINListener) {
        mHandler.post(new Runnable() {
            public void run() {
                PairingPINDialogBuilder mPinDialogBuilder =
                        new PairingPINDialogBuilder(PairingActivity.this);
                mPinDialogBuilder.setPinListener(pairingPINListener);
                mPinDialogBuilder.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mConnectionManager != null) {
            mConnectionManager.detachPairingListener(this);
        }
        unbindService(mConnection);

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
