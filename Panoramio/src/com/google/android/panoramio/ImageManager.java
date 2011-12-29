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

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.Log;

/**
 * This class is responsible for downloading and parsing the search results for
 * a particular area. All of the work is done on a separate thread, and progress
 * is reported back through the DataSetObserver set in
 * {@link #addObserver(DataSetObserver). State is held in memory by in memory
 * maintained by a single instance of the ImageManager class.
 * 
 */
public class ImageManager {
    private static final String TAG = "Panoramio";

    /**
     * Base URL for Panoramio's web API
     */
    private static final String THUMBNAIL_URL = "//www.panoramio.com/map/get_panoramas.php?"
            + "order=popularity" + "&set=public" + "&from=0" + "&to=300" + "&miny=%f" + "&minx=%f"
            + "&maxy=%f" + "&maxx=%f" + "&size=original";

    /**
     * Used to post results back to the UI thread
     */
    private static Handler mHandler = new Handler();

    /**
     * Holds the single instance of a ImageManager that is shared by the
     * process.
     */
    private static ImageManager sInstance;

    /**
     * Holds the images and related data that have been downloaded
     */
    private final ArrayList<PanoramioItem> mImages = new ArrayList<PanoramioItem>();

    private int mCurrentPosition;

    /**
     * Observers interested in changes to the current search results
     */
    private final ArrayList<WeakReference<DataSetObserver>> mObservers = new ArrayList<
            WeakReference<DataSetObserver>>();

    /**
     * True if we are in the process of loading
     */
    private static boolean mLoading;

    private static Context mContext;

    private volatile static String query;

    /**
     * Key for an Intent extra. The value is the zoom level selected by the
     * user.
     */
    public static final String ZOOM_EXTRA = "zoom";

    /**
     * Key for an Intent extra. The value is the latitude of the center of the
     * search area chosen by the user.
     */
    public static final String LATITUDE_E6_EXTRA = "latitudeE6";

    /**
     * Key for an Intent extra. The value is the latitude of the center of the
     * search area chosen by the user.
     */
    public static final String LONGITUDE_E6_EXTRA = "longitudeE6";

    /**
     * Key for an Intent extra. The value is an item to display
     */
    public static final String PANORAMIO_ITEM_EXTRA = "item";

    public static ImageManager getInstance(Context c) {
        if (sInstance == null) {
            sInstance = new ImageManager(c);
        }
        return sInstance;
    }

    private ImageManager(Context c) {
        mContext = c;
    }

    /**
     * @return True if we are still loading content
     */
    public boolean isLoading() {
        return mLoading;
    }

    /**
     * Clear all downloaded content
     */
    public void clear() {
        File cacheDir = mContext.getCacheDir();
        for (File file : cacheDir.listFiles()) {
            file.delete();
        }
        cacheDir.delete();
        for (PanoramioItem item : mImages) {
            item.clear();
            item = null;
        }
        mImages.clear();
        notifyInvalidateObservers();
    }

    public PanoramioItem getNext() {
        if (mCurrentPosition + 1 <= mImages.size() - 1) {
            mCurrentPosition = mCurrentPosition + 1;
            return mImages.get(mCurrentPosition);
        }
        return null;
    }

    public PanoramioItem getPrevious() {
        if (mCurrentPosition - 1 >= 0) {
            mCurrentPosition = mCurrentPosition - 1;
            return mImages.get(mCurrentPosition);
        }
        return null;
    }

    /**
     * Add an item to and notify observers of the change.
     * 
     * @param item The item to add
     */
    private void add(PanoramioItem item) {
        if (item.getLocation() != query) {
            return;
        }
        mImages.add(item);
        notifyObservers();
    }

    /**
     * @return The number of items displayed so far
     */
    public int size() {
        return mImages.size();
    }

    /**
     * Gets the item at the specified position
     */
    public PanoramioItem get(int position) {
        mCurrentPosition = position;
        if (mImages.size() > position) {
            return mImages.get(position);
        }
        return null;
    }

    /**
     * Adds an observer to be notified when the set of items held by this
     * ImageManager changes.
     */
    public void addObserver(DataSetObserver observer) {
        final WeakReference<DataSetObserver> obs = new WeakReference<DataSetObserver>(observer);
        mObservers.add(obs);
    }

    Thread mPrevThread = null;

    /**
     * Load a new set of search results for the specified area.
     * 
     * @param minLong The minimum longitude for the search area
     * @param maxLong The maximum longitude for the search area
     * @param minLat The minimum latitude for the search area
     * @param maxLat The minimum latitude for the search area
     * @throws JSONException
     * @throws URISyntaxException
     * @throws IOException
     */
    public void load(String query) throws IOException, URISyntaxException, JSONException {
        this.query = query;
        clear();
        mLoading = true;

        GeoResponse location = null;
        try {
            location = new GeoCoderTask().execute(query).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (location != null) {

            mPrevThread = new ImageLoader(location.getMinLatitude(), location.getMinLongitude(),
                    location.getMaxLatitude(), location.getMaxLongitude(), query);
            mPrevThread.start();
        } else {
            Log.e(TAG, "Geocoder returned no location");
        }
    }

    /**
     * Called when something changes in our data set. Cleans up any weak
     * references that are no longer valid along the way.
     */
    private void notifyObservers() {
        final ArrayList<WeakReference<DataSetObserver>> observers = mObservers;
        final int count = observers.size();
        for (int i = count - 1; i >= 0; i--) {
            final WeakReference<DataSetObserver> weak = observers.get(i);
            final DataSetObserver obs = weak.get();
            if (obs != null) {
                obs.onChanged();
            } else {
                observers.remove(i);
            }
        }
    }

    /**
     * Called when something changes in our data set. Cleans up any weak
     * references that are no longer valid along the way.
     */
    private void notifyInvalidateObservers() {
        final ArrayList<WeakReference<DataSetObserver>> observers = mObservers;
        final int count = observers.size();
        for (int i = count - 1; i >= 0; i--) {
            final WeakReference<DataSetObserver> weak = observers.get(i);
            final DataSetObserver obs = weak.get();
            if (obs != null) {
                obs.onInvalidated();
            } else {
                observers.remove(i);
            }
        }
    }

    /**
     * This thread does the actual work of fetching and parsing Panoramio JSON
     * response data.
     */
    private static class ImageLoader extends Thread {
        double mMinLong;

        double mMaxLong;

        double mMinLat;

        double mMaxLat;

        String mQuery;

        public ImageLoader(double minLatitude, double minLongitude, double maxLatitude,
                double maxLongitude, String query) {
            mMinLong = minLongitude;
            mMaxLong = maxLongitude;
            mMinLat = minLatitude;
            mMaxLat = maxLatitude;
            mQuery = query;
        }

        @Override
        public void run() {
            String url = THUMBNAIL_URL;
            url = String.format(url, mMinLat, mMinLong, mMaxLat, mMaxLong);
            try {
                final URI uri = new URI("http", url, null);
                final HttpGet get = new HttpGet(uri);
                final HttpClient client = new DefaultHttpClient();
                final HttpResponse response = client.execute(get);
                final HttpEntity entity = response.getEntity();
                final String str = Utilities.convertStreamToString(entity.getContent());
                final JSONObject json = new JSONObject(str);
                parse(json);
            } catch (final Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        private void parse(JSONObject json) {
            try {
                final JSONArray array = json.getJSONArray("photos");
                final int count = array.length();

                for (int i = 0; i < count; i++) {
                    if (!mQuery.equals(query)) {
                        break;
                    }
                    final boolean done = i == count - 1;
                    final JSONObject obj = array.getJSONObject(i);

                    final long id = obj.getLong("photo_id");
                    String title = obj.getString("photo_title");
                    final String owner = obj.getString("owner_name");
                    final String thumb = obj.getString("photo_file_url");
                    final String ownerUrl = obj.getString("owner_url");
                    final String photoUrl = obj.getString("photo_url");
                    final double latitude = obj.getDouble("latitude");
                    final double longitude = obj.getDouble("longitude");
                    final double width = obj.getDouble("width");
                    final double height = obj.getDouble("height");
                    if (title == null) {
                        title = mContext.getString(R.string.untitled);
                    }
                    // ignore small pictures, they appear pixelated on Google TV
                    // screen.2000 is a randomly chosen number.
                    if (width < 2000 || height < 2000) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                sInstance.mLoading = !done;
                                sInstance.notifyObservers();
                            }
                        });
                        continue;
                    }
                    final PanoramioItem item = new PanoramioItem(mContext, id, thumb,
                            (int) (latitude), (int) (longitude), title, owner, ownerUrl, photoUrl,
                            mQuery, mHandler);
                    item.loadLargeBitmap();
                    mHandler.post(new Runnable() {
                        public void run() {
                            sInstance.mLoading = !done;
                            sInstance.add(item);
                        }
                    });
                }

            } catch (final JSONException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
