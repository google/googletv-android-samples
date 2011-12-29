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

package com.google.android.panoramio;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
//Copyright 2011 Google Inc. All Rights Reserved.

import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The main Activity which displays the grid of photos fetched from
 * Panoramio service.
 */
public class ImageGrid extends Activity implements OnClickListener {
    private static final String DEFAULT_QUERY = "San Francisco";

    ImageManager mImageManager;

    private static boolean isSplashShown = false;

    private String query;

    private Context mContext;

    private TextView textView;

    private ProgressBar progressBar;

    /**
     * Simple Dialog used to show the splash screen.
     */
    protected Dialog mSplashDialog;

    @Override
    protected void onDestroy() {
        super.onStop();
        mImageManager.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        mImageManager = ImageManager.getInstance(mContext);
        try {
            handleIntent(getIntent());
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        } catch (final JSONException e) {
            e.printStackTrace();
        }

        if (!isSplashShown) {
            setContentView(R.layout.splash_screen);
            isSplashShown = true;
            CountDownTimer timer = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    initGridView();
                }
            }.start();
        } else {
            initGridView();
        }
    }

    private GridView gridView;

    private void initGridView() {
        setContentView(R.layout.image_grid);
        gridView = (GridView) findViewById(R.id.gridview);
        final ImageAdapter imageAdapter = new ImageAdapter(mContext);
        gridView.setAdapter(imageAdapter);
        progressBar = (ProgressBar) findViewById(R.id.a_progressbar);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // Create an intent to show a particular item.
                final Intent i = new Intent(ImageGrid.this, ViewImage.class);
                i.putExtra(ImageManager.PANORAMIO_ITEM_EXTRA, position);
                startActivity(i);
            }
        });
        gridView.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
            public void onChildViewAdded(View parent, View child) {
                progressBar.setVisibility(View.INVISIBLE);
                ((ViewGroup) parent).getChildAt(0).setSelected(true);
            }

            public void onChildViewRemoved(View parent, View child) {
            }
        });

        textView = (TextView) findViewById(R.id.place_name);
        textView.setText(query);
        PanoramioLeftNavService.getLeftNavBar(this);
        gridView.requestFocus();
    }

    public void onClick(View view) {
        onSearchRequested();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        progressBar.setVisibility(View.VISIBLE);
        try {
            handleIntent(intent);
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        initGridView();
    }

    private void handleIntent(Intent intent) throws IOException, URISyntaxException, JSONException {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        } else {
            query = intent.getStringExtra("query");
        }

        if (query == null || query.isEmpty()) {
            query = DEFAULT_QUERY;
        }
        // Start downloading
        mImageManager.load(query);
    }
}
