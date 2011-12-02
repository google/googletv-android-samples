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

package com.google.example.tv.maps;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import java.util.ArrayList;

/**
 * Class to overlay a marker on the map to point the searched location.
 * 
 */
public class MapsMarkerOverlay extends ItemizedOverlay<OverlayItem> {

    private final ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    
    /**
     * Default constructor.
     * 
     * @param defaultMarker Drawable to initialize the class with.
     */
    public MapsMarkerOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
    }

    /**
     * Constructor with Drawable and Context parameters.
     * 
     * @param defaultMarker Drawable to initialize the class with.
     * @param context Context of the activity.
     */
    public MapsMarkerOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
    }

    @Override
    protected OverlayItem createItem(int location) {
        return mOverlays.get(location);
    }

    /**
     * Add an item to overlay list.
     * 
     * @param overlay The overlay item.
     */
    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }

    @Override
    public int size() {
        return mOverlays.size();
    }
}
