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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.google.tv.anymotelibrary.R;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used to present the discovered Google TV devices to the user. When user
 * selects one of the listed devices from this dialog, connection to the device
 * is initiated.
 */
public class DeviceSelectDialog extends Dialog {
    private DeviceListAdapter mDataAdapter;
    private DeviceSelectListener mListener;
    private TvDiscoveryService mTvDiscoveryService;
    private boolean mInitialized;
    private AsyncTask<Integer, Integer, List<TvDevice>> mDiscoveryTask;
    private ProgressBar progressBar;

    /**
     * Dialog listener
     */
    public interface DeviceSelectListener {
        /**
         * Called whenever user picks up a new device to connect to.
         * 
         * @param device - interface to remote device
         */
        void onDeviceSelected(TvDevice device);

        /**
         * Called when device select dialog is dismissed
         */
        void onDeviceSelectCancelled();
    }

    /**
     * Constructor.
     * 
     * @param context owner of the dialog.
     */
    public DeviceSelectDialog(Context context) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    /*
     * events
     */
    @Override
    protected void onStart() {
        super.onStart();
        startDiscovery();
    }

    @Override
    protected void onStop() {
        stopDiscovery();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ListView deviceView;

        setContentView(R.layout.device_select_layout);

        mDataAdapter = new DeviceListAdapter();
        deviceView = (ListView) findViewById(R.id.dsl_stb_list);
        deviceView.setAdapter(mDataAdapter);

        deviceView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TvDevice TvDevice = (TvDevice) parent.getItemAtPosition(position);
                selectDevice(TvDevice);
            }
        });

        deviceView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(
                    AdapterView<?> parent, View view, int position, long id) {
                TvDevice TvDevice = (TvDevice) parent.getItemAtPosition(position);
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showDevice(TvDevice);
                return true;
            }

        });

        findViewById(R.id.dsl_manual).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showManualIpDialog();
            }
        });

        findViewById(R.id.dsl_rescan).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startDiscovery();
            }
        });

        setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                selectDevice(null);
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.a_progressbar);
        progressBar.setVisibility(View.VISIBLE);

        setCancelable(true);
        super.onCreate(savedInstanceState);
        mInitialized = true;

    }

    /**
     * Represents an entry in the box list.
     */
    private static class ListEntryView extends LinearLayout {

        private Context myContext = null;
        private TvDevice listEntry = null;
        private TextView tvName = null;
        private TextView tvTargetAddr = null;

        public ListEntryView(Context context, AttributeSet attrs) {
            super(context, attrs);
            myContext = context;
        }

        public ListEntryView(Context context) {
            super(context);
            myContext = context;
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            tvName = (TextView) findViewById(R.id.device_select_item_name);
            tvTargetAddr = (TextView) findViewById(R.id.device_select_item_address);
        }

        private void updateContents() {
            if (null != tvName) {
                String txt = myContext.getString(R.string.unkown_tgt_name);
                if ((null != listEntry) && (null != listEntry.getName())) {
                    txt = listEntry.getName();
                }
                tvName.setText(txt);
            }

            if (null != tvTargetAddr) {
                String txt = myContext.getString(R.string.unkown_tgt_addr);
                if ((null != listEntry) && (null != listEntry.getLocation())) {
                    txt = listEntry.getLocation();
                }
                tvTargetAddr.setText(txt);
            }
        }

        public void setListEntry(TvDevice listEntry) {
            this.listEntry = listEntry;
            updateContents();
        }

    }

    /**
     * Internal listview adapter.
     */
    private class DeviceListAdapter extends BaseAdapter {
        private final List<TvDevice> trackedDevices;
        private TvDevice mCurrentDevice;

        public DeviceListAdapter() {
            trackedDevices = new ArrayList<TvDevice>();
            setRecentDevices(null);
        }

        public int getCount() {
            return getTotalSize();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return (position != 0);
        }

        public Object getItem(int position) {
            return getTvDevice(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public boolean add(TvDevice device) {
            if (!trackedDevices.contains(device)) {
                trackedDevices.add(device);
                Collections.sort(trackedDevices);

                notifyDataSetChanged();
                return true;
            }
            return false;
        }

        public void setRecentDevices(TvDevice[] devices) {

            notifyDataSetChanged();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // /*
            // * Find an item and construct a view for it given the position.
            // * Layout / content is as follows: 0 : [ Recently Connected header
            // ]
            // * 1 : [ Recent Device #0 ] n : [ Recent Device #n-1 ] n+1: [
            // * Discovered Devices header ] n+2: [ Discovered Device #0 ] n+m:
            // [
            // * Discovered Device #m-2 ]
            // */
            // if (position == 0) {
            // return getHeaderView(R.string.finder_recently_connected,
            // convertView);
            // }
            // // Skip Recently Connected header.
            // position -= 1;
            //
            // if (position < rememberedDevices.length) {
            // return getDeviceView(rememberedDevices[position], convertView,
            // true);
            // }
            // // Skip Recent Devices.
            // position -= rememberedDevices.length;

            if (position == 0) {
                return getHeaderView(R.string.finder_connect, convertView);
            }
            // Skip Discovered Devices header.
            position -= 1;

            if (position < trackedDevices.size()) {
                return getDeviceView(trackedDevices.get(position), convertView, false);
            }
            // Nothing more to skip, invalid index.
            return null;
        }

        /**
         * Construct titled separator for listview.
         * 
         * @param resource string resource identifying title.
         * @param convertView currently presented view.
         * @return separator view.
         */
        private View getHeaderView(int resource, View convertView) {
            View view = getLayoutInflater().inflate(R.layout.device_select_item_separator_layout,
                    null);
            TextView text = (TextView) view.findViewById(R.id.header_text);
            text.setText(resource);
            return view;
        }

        /**
         * Construct view representing device.
         * 
         * @param device element associated with the view.
         * @param convertView currently presented view.
         * @param isRecent specifies whether device has been recently connected.
         * @return device item.
         */
        private View getDeviceView(TvDevice device, View convertView, boolean isRecent) {
            ListEntryView itemView;
            ImageView image;

            if (convertView == null || !(convertView instanceof ListEntryView)) {
                itemView = (ListEntryView) getLayoutInflater().inflate(
                        R.layout.device_select_item_layout, null);
            } else {
                itemView = (ListEntryView) convertView;
            }

            image = (ImageView) itemView.findViewById(R.id.device_select_item_image);
            if (image != null) {
                if (device.equals(mCurrentDevice)) {
                    image.setImageResource(android.R.drawable.ic_menu_upload_you_tube);
                } else if (isRecent) {
                    image.setImageResource(android.R.drawable.ic_menu_recent_history);
                } else {
                    image.setImageResource(android.R.drawable.ic_menu_search);
                }
            }

            if (device.equals(mCurrentDevice)) {
                itemView.setBackgroundColor(Color.argb(0x80, 0x00, 0x40, 0x20));
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            itemView.setListEntry(device);
            return itemView;
        }

        private int getTotalSize() {
            // return sum of tracked devices and header.
            return trackedDevices.size() + 1;
        }

        private TvDevice getTvDevice(int position) {
            // Skip header.
            position--;
            if (position < 0) {
                return null;
            }

            // and check, if the new position fits into the second group.
            if (position < trackedDevices.size()) {
                return trackedDevices.get(position);
            }

            // bad luck.
            return null;
        }

        public void setCurrentDevice(TvDevice device) {
            mCurrentDevice = device;
            notifyDataSetChanged();
        }
    }

    /**
     * Present dialog allowing manual IP address specification.
     */
    private void showManualIpDialog() {

        progressBar.setVisibility(View.INVISIBLE);
        DeviceManualIPDialog ipDialog = new DeviceManualIPDialog(getContext());
        ipDialog.setManualIPListener(new DeviceManualIPDialog.ManualIPListener() {
            public void onSelect(String name, Inet4Address address, int port) {
                selectDevice(new TvDevice(name, address, port));
            }

            public void onCancel() {
            }
        });
        ipDialog.show();
    }

    private void startDiscovery() {
        if (!mInitialized || (mTvDiscoveryService == null) || (mDiscoveryTask != null)) {
            return;
        }

        // As TvDiscovery.discoverTvs method is blocking, launch an AsyncTask
        // to get a list of TVs.
        mDiscoveryTask = new AsyncTask<Integer, Integer, List<TvDevice>>() {

            /**
             * Start a discovery in background.
             */
                @Override
            protected List<TvDevice> doInBackground(Integer... params) {
                return mTvDiscoveryService.discoverTvs();
            }

            /**
             * Put results into the dialog, or pop a no-wifi dialog, after
             * discovery has finished.
             */
                @Override
            protected void onPostExecute(List<TvDevice> tvs) {

                progressBar.setVisibility(View.INVISIBLE);
                if (tvs != null) {
                    for (TvDevice tv : tvs) {
                        mDataAdapter.add(tv);
                    }
                } else {
                    // For the time being, assume that null result means
                    // wifi connection error, so show wifi config dialog.
                    buildNoWifiDialog().show();
                }
                mDiscoveryTask = null;
            }
        };
        mDiscoveryTask.execute((Integer) null);
    }

    private void stopDiscovery() {
        if (mDiscoveryTask == null) {
            return;
        }
        mDiscoveryTask.cancel(true);
        mDiscoveryTask = null;
    }

    /**
     * Connect to specified device.
     * 
     * @param device target device.
     */
    private void selectDevice(TvDevice device) {
        Toast.makeText(getContext(), "Device selected",
                Toast.LENGTH_LONG);
        if (mListener != null) {
            if (device != null) {

                mListener.onDeviceSelected(device);
            } else {
                mListener.onDeviceSelectCancelled();
            }
        }
    }

    /**
     * Construct and present a dialog describing device.
     * 
     * @param device device to be presented to the user.
     */
    private void showDevice(TvDevice device) {
    }

    /**
     * Construct a no-wifi dialog.
     * 
     * @return AlertDialog asking user to turn on WIFI.
     */
    private AlertDialog buildNoWifiDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.finder_wifi_not_available);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.finder_configure, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
                Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                getContext().startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.finder_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
            }
        });
        return builder.create();
    }

    /**
     * Set object receiving device select events.
     * 
     * @param listener target object.
     */
    public void setDeviceSelectListener(DeviceSelectListener listener) {
        this.mListener = listener;
    }

    /**
     * Set currently connected device to highlight it in the list.
     * 
     * @param device currently connected device.
     */
    public void setCurrentDevice(TvDevice device) {
        if (mInitialized) {
            mDataAdapter.setCurrentDevice(device);
        }
    }

    /**
     * Set list of recently connected TVs.
     * 
     * @param devices list of recently connected devices.
     */
    public void setRecentDevices(TvDevice[] devices) {
        if (mInitialized) {
            mDataAdapter.setRecentDevices(devices);
        }
    }

    /**
     * Set TV discoverer service.
     * 
     * @param discovery service used to find neighboring TVs.
     */
    public void setTvDiscovery(TvDiscoveryService discovery) {
        mTvDiscoveryService = discovery;
        startDiscovery();
    }
}
