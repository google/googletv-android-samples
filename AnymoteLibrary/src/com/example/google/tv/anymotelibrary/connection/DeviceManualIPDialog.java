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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.google.tv.anymotelibrary.R;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Used to manually specify IP address of remote device user wants to connect
 * to.
 */
public class DeviceManualIPDialog extends AlertDialog {
    private static final char[] ACCEPTED_CHARS = "0123456789.:".toCharArray();
    private final Context context;
    private ManualIPListener listener;

    /**
     * The listener for Manual IP Dialog.
     */
    public interface ManualIPListener {

        /**
         * Called when user makes a valid IP or host name selection.
         * 
         * @param name hostname of device to connect to.
         * @param address IP address of device to connect to.
         * @param port port of the service to connect to.
         */
        public void onSelect(String name, Inet4Address address, int port);

        /**
         * Called when user selects invalid hostname/ip or cancels dialog.
         */
        public void onCancel();
    }

    /**
     * Constructor.
     * 
     * @param context Context of the Activity that owns the dialog.
     */
    public DeviceManualIPDialog(Context context) {
        super(context, android.R.style.Theme_InputMethod);
        this.context = context;
    }

    /**
     * Set listener that will receive information about entered host and port.
     * 
     * @param listener the interface which will be called.
     */
    public void setManualIPListener(ManualIPListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_select_manual_ip, null);

        final EditText ipEditText = (EditText) view.findViewById(R.id.manual_ip_entry);

        ipEditText.setFilters(new InputFilter[] {
        new NumberKeyListener() {
                            @Override
            protected char[] getAcceptedChars() {
                return ACCEPTED_CHARS;
                }

            public int getInputType() {
                return InputType.TYPE_CLASS_NUMBER;
                }
        } });

        setButton(BUTTON_POSITIVE, context.getString(R.string.manual_ip_connect),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!parseInputAddress(ipEditText.getText().toString())) {
                            Toast.makeText(context, "Host not found", Toast.LENGTH_LONG).show();
                        }
                        hide();
                    }
                });

        setButton(BUTTON_NEGATIVE, context.getString(R.string.manual_ip_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onCancel();
                        }
                        hide();
                    }
                });

        setCancelable(true);
        setTitle(R.string.manual_ip_label);
        setView(view);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showKeyboard();
    }

    /**
     * Parse and validate address:port input.
     * 
     * @param text string containing address:port specification of the remote
     *            host.
     * @return true, if string contains valid address:port tuple and points to
     *         existing host.
     */
    private boolean parseInputAddress(String text) {
        String[] ipPort = text.split(":");
        int port = context.getResources().getInteger(R.integer.manual_default_port);

        if (listener == null) {
            return false;
        }

        if (ipPort.length == 2) {
            try {
                port = Integer.parseInt(ipPort[1]);
            } catch (NumberFormatException e) {
                listener.onCancel();
                return false;
            }
        } else if (ipPort.length != 1) {
            listener.onCancel();
            return false;
        }

        try {
            Inet4Address address = (Inet4Address) InetAddress.getByName(ipPort[0]);

            listener.onSelect(
                    context.getString(R.string.manual_ip_default_box_name), address, port);
            return true;
        } catch (UnknownHostException e) {
            listener.onCancel();
        }
        return false;
    }

    /**
     * Show software keyboard.
     */
    private void showKeyboard() {
        InputMethodManager manager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
}
