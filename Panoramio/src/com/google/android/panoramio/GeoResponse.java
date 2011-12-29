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

/**
 * This data class encapsulates the response received from Google Maps API geocoding request.
 * It is used by GeoCoderTask.
 */
public class GeoResponse {

    private double mMaxLatitude;

    private double mMinLatitude;

    private double mMaxLongitude;

    private double mMinLongitude;

    public GeoResponse(
            double minLat, double minLng, double maxLat, double maxLng) {
        super();
        mMinLatitude = minLat;
        mMinLongitude = minLng;
        mMaxLatitude = maxLat;
        mMaxLongitude = maxLng;
    }

    public double getMaxLatitude() {
        return mMaxLatitude;
    }

    public double getMinLatitude() {
        return mMinLatitude;
    }

    public double getMaxLongitude() {
        return mMaxLongitude;
    }

    public double getMinLongitude() {
        return mMinLongitude;
    }
}
