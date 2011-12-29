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

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * This class provides Geocoding services through the Google Maps APIs.
 */
public class GeoCoderTask extends AsyncTask<String, Void, GeoResponse> {
    // URL prefix to the geocoder
    private static final String
            GEOCODER_REQUEST_PREFIX_FOR_JSON = "//maps.googleapis.com/maps/api/geocode/json";

    private static final String TAG = null;

    public GeoResponse geocode(String address)
            throws IOException, URISyntaxException, JSONException {

        // prepare a URL to the geocoder
        String url = GEOCODER_REQUEST_PREFIX_FOR_JSON + "?address="
                + URLEncoder.encode(address, "UTF-8") + "&sensor=false";

        // prepare an HTTP connection to the geocoder
        URI uri = new URI("http", url, null);
        HttpGet get = new HttpGet(uri);

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(get);
        HttpEntity entity = response.getEntity();
        String str = Utilities.convertStreamToString(entity.getContent());
        JSONObject json = new JSONObject(str);
        return parse(json);

    }

    private GeoResponse parse(JSONObject json) {
        GeoResponse geoResponse = null;
        try {
            JSONArray array = json.getJSONArray("results");
            if (array.length() > 0) {

                JSONObject obj = array.getJSONObject(0);
                JSONObject viewport = obj.getJSONObject("geometry").getJSONObject("viewport");
                double minLat = viewport.getJSONObject("southwest").getDouble("lat");
                double minLng = viewport.getJSONObject("southwest").getDouble("lng");
                double maxLat = viewport.getJSONObject("northeast").getDouble("lat");
                double maxLng = viewport.getJSONObject("northeast").getDouble("lng");
                geoResponse = new GeoResponse(minLat, minLng, maxLat, maxLng);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        return geoResponse;
    }

    /**
     * The system calls this to perform work in a worker thread and delivers it
     * the parameters given to AsyncTask.execute()
     */
    protected GeoResponse doInBackground(String... address) {
        try {
            return geocode(address[0]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
