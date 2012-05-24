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

import android.os.Build;

import java.net.Inet4Address;

/**
 * This class represents the Tv Device to connect to.
 */
public class TvDevice implements Comparable<TvDevice> {
    private static final String LOG_TAG = "TvDevice";
    private static final String REMOTE_NAME = Build.MANUFACTURER + " " + Build.MODEL;
    private static final String PREF_NAME = "Name";
    private static final String PREF_ADDRESS = "Address";
    private static final String PREF_PORT = "Port";

    private final String name;
    private final int port;
    private final Inet4Address address;

    /**
     * Constructor
     * 
     * @param name Device name
     * @param address Device IP Address
     * @param port The port where Anymote service is running.
     */
    public TvDevice(final String name, final Inet4Address address, final Integer port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    /**
     * Gets device name.
     * 
     * @return device name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets port where Anymote service is running.
     * 
     * @return port number
     */
    protected int getPort() {
        return port;
    }

    /**
     * Gets IP address of the device
     * 
     * @return device IP Address.
     */
    protected Inet4Address getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return String.format("Secure: %s [%s:%d]", getName(), getAddress(), getPort());
    }

    /**
     * Compare self against another instance of TvDevice. Single device may
     * expose multiple interfaces. If it does, we will sort devices by the name.
     */
    @Override
    public int compareTo(TvDevice another) {
        int result = getName().compareTo(another.getName());

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TvDevice)) {
            return false;
        }
        TvDevice another = (TvDevice) o;
        return compareTo(another) == 0;
    }

    /**
     * Gets the string representing the device address.
     * 
     * @return device location string.
     */
    public String getLocation() {
        return "SSL:" + address;
    }
}
