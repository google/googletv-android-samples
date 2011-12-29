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

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Holds one item returned from the Panoramio server. This includes the bitmap
 * along with other meta info.
 */
public class PanoramioItem implements Parcelable {
    private long mId;

    private String mTitle;

    private String mOwner;

    private String mThumbUrl;

    private String mOwnerUrl;

    private String mPhotoUrl;

    private Context mContext;

    private boolean isLoaded;

    private String mLocation;

    private Bitmap mBitmap;

    private File bitmapFile;

    public PanoramioItem(Context context, long id, String thumbUrl, int latitudeE6, int longitudeE6,
            String title, String owner, String ownerUrl, String photoUrl, String location,
            Handler handler) {
        mTitle = title;
        mOwner = owner;
        mThumbUrl = thumbUrl;
        mOwnerUrl = ownerUrl;
        mPhotoUrl = photoUrl;
        mId = id;
        mContext = context;
        mLocation = location;
        mBitmap = getBitmap();
        final File cacheDir = mContext.getCacheDir();
        bitmapFile = new File(cacheDir, mId + ".jpg");
    }

    public long getId() {
        return mId;
    }

    public String getLocation() {
        return mLocation;
    }

    public Bitmap getBitmap() {
        if (mBitmap != null)
            return mBitmap;

        try {
            String url = "http://mw2.google.com/mw-panoramio/photos/small/" + mId + ".jpg";
            mBitmap = new BitmapUtilsTask().execute(url, "thumb").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return mBitmap;
    }

    public Bitmap getLargeBitmap() {
        Bitmap b = null;
        try {
            b = new BitmapUtilsTask().execute(mThumbUrl, "large",bitmapFile).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return b;

    }

    public void loadLargeBitmap() {
        if (!isLoaded) {
            new BitmapUtilsTask().execute(mThumbUrl, "load",bitmapFile);
            isLoaded = true;
        }
    }

    public String getTitle() {
        return mTitle;
    }

    public String getOwner() {
        return mOwner;
    }

    public String getThumbUrl() {
        return mThumbUrl;
    }

    public String getOwnerUrl() {
        return mOwnerUrl;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mId);
        parcel.writeString(mTitle);
        parcel.writeString(mOwner);
        parcel.writeString(mThumbUrl);
        parcel.writeString(mOwnerUrl);
        parcel.writeString(mPhotoUrl);
    }

   
    public void clear() {
        if (mBitmap != null)
            mBitmap.recycle();
        mBitmap = null;
    }
}
