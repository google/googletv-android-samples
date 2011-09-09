/*
 * Copyright (C) 2011 Google Inc.
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

package com.example.google.android.tv.channelchanging;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

/**
 * This activity retrieves a list of channels from the Channel Listing Content
 * Provider and displays them in a ListView. If a channel is selected, an intent
 * is sent to the Channel Changing Activity. To use this code, be sure to add
 * the READ_CHANNELS permission to your manifest. 
 * <uses-permission android:name="com.google.android.tv.permission.READ_CHANNELS" />
 *
 * @author Paul Saxman
 */
public class ChannelChangingActivity extends ListActivity {
    private static final String LOG_TAG = "ChannelChangingActivity";

    // Constants for accessing the channel listing content provider.
    private static final String AUTHORITY = "com.google.android.tv.provider";

    private static final String CHANNEL_LISTING_PATH = "channel_listing";

    private static final Uri CHANNEL_LISTING_URI = Uri.parse(
            "content://" + AUTHORITY).buildUpon().appendPath(
            CHANNEL_LISTING_PATH).build();
    
    // All available channel listing content provider columns.
    private static final String ID = "_id";
    
    private static final String CHANNEL_URI = "channel_uri";
    // This column stores the abbreviated channel name. 
    private static final String CHANNEL_NAME = "channel_name";
    // This column stores the abbreviated channel number. 
    private static final String CHANNEL_NUMBER = "channel_number";
    // This column stores the abbreviated channel name.
    private static final String CHANNEL_CALLSIGN = "callsign";

    // Retrieve specified columns (faster) or all columns (PROJECTION = null)
    // Column order specified here so that we can use the SimpleCursorAdapter
    // column-to-view binding.
    private static final String[] PROJECTION = new String[] {
            CHANNEL_CALLSIGN, CHANNEL_NUMBER, CHANNEL_NAME, CHANNEL_URI, ID };

    // Retrieve a specified channel
    // private static final String SELECTION = CHANNEL_CALLSIGN + " = ?";
    // private static final String[] ARGS = {"FLIX"};

    // Retrieve all related channels
    // private static final String SELECTION = CHANNEL_CALLSIGN + " LIKE ?";
    // private static final String[] ARGS = {"HBO%"};
    // (HBO followed by % means anything that starts with HBO).

    // Retrieve all channels
    private static final String SELECTION = null;
    private static final String[] ARGS = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the channels from the channel listing content provider.
        // In production code, the query should be executed in an AsyncTask.
        ContentResolver mResolver = getContentResolver();
        Cursor mCursor = mResolver.query(
                CHANNEL_LISTING_URI, PROJECTION, SELECTION, ARGS, null);

        if (mCursor.isAfterLast()) {
            Log.w(LOG_TAG, "No channels found.");
        }

        // Add the channel cursor to a CursorAdapter to displaying the channels
        // in the ListActivity.
        final ListAdapter mAdapter = new SimpleCursorAdapter(this,
                R.layout.list_entry, mCursor, PROJECTION,
                new int[] { R.id.channel_callsign, R.id.channel_number,
                        R.id.channel_name });
        this.setListAdapter(mAdapter);

        // Add an onclick listener to change the channel when one is selected.
        this.getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(
                    AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the cursor for the selected channel, then extract
                // the channel URI.
                Cursor rowCursor = (Cursor) mAdapter.getItem(position);
                int columnIndex = rowCursor.getColumnIndex(CHANNEL_URI);
                Uri channelUri = Uri.parse(rowCursor.getString(columnIndex));

                // Start the channel changing activity using the channel URI.
                startActivity(new Intent(Intent.ACTION_VIEW, channelUri));
            }
        });
    }
}
