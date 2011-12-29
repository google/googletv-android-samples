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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utilities for loading a bitmap from a URL. All network transactions and 
 * image scaling is done on non-UI thread using AsyncTask.
 */
public class BitmapUtilsTask extends AsyncTask<Object, Void, Bitmap> {

    private static final String TAG = "Panoramio";

    /**
     * Loads a bitmap from the specified url.
     * 
     * @param url The location of the bitmap asset
     * @return The bitmap, or null if it could not be loaded
     * @throws IOException
     * @throws MalformedURLException
     */
    public Bitmap getBitmap(final String string, Object fileObj) throws MalformedURLException, IOException {
        File file = (File)fileObj;        
        // Get the source image's dimensions
        int desiredWidth = 1000;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        if (!file.isFile()) {
            InputStream is = (InputStream) new URL(string).getContent();
            BitmapFactory.decodeStream(is, null, options);
            is.close();

        } else {
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }
        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;

        // Only scale if the source is big enough. This code is just trying
        // to fit a image into a certain width.
        if (desiredWidth > srcWidth)
            desiredWidth = srcWidth;

        // Calculate the correct inSampleSize/scale value. This helps reduce
        // memory use. It should be a power of 2
        int inSampleSize = 1;
        while (srcWidth / 2 > desiredWidth) {
            srcWidth /= 2;
            srcHeight /= 2;
            inSampleSize *= 2;
        }
        // Decode with inSampleSize
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inSampleSize = inSampleSize;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inPurgeable = true;
        Bitmap sampledSrcBitmap;
        if (!file.isFile()) {
            InputStream is = (InputStream) new URL(string).getContent();
            sampledSrcBitmap = BitmapFactory.decodeStream(is, null, options);
            is.close();
        } else {
            sampledSrcBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }
        return sampledSrcBitmap;

    }

    private void loadBitmap(String url, Object fileObj) {
        File file = (File)fileObj;
        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = client.execute(request);
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Header[] headers = response.getHeaders("Location");

                if (headers != null && headers.length != 0) {
                    String newUrl = headers[headers.length - 1].getValue();
                    // call again with new URL
                    loadBitmap(newUrl, file);
                    return;
                }
            }
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                FileOutputStream out = new FileOutputStream(file);
                try {
                    inputStream = entity.getContent();
                    byte buf[] = new byte[16384];
                    do {
                        int numread = inputStream.read(buf);
                        if (numread <= 0)
                            break;
                        out.write(buf, 0, numread);
                    } while (true);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                        out.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            request.abort();
        } finally {
            if (client != null) {
                client.close();
            }
        }

    }

    /**
     * Loads a bitmap from the specified url.
     * 
     * @param url The location of the bitmap asset
     * @return The bitmap, or null if it could not be loaded
     */
    public Bitmap loadThumbnail(String string) {
        Bitmap bitmap = null;

        InputStream is;
        try {
            is = (InputStream) new URL(string).getContent();
            BitmapFactory.Options optsDownSample = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeStream(is, null, optsDownSample);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * The system calls this to perform work in a worker thread and delivers
     * it the parameters given to AsyncTask.execute()
     */
    @Override
    protected Bitmap doInBackground(Object... item) {

        try {
            if (item[1].toString().equals("thumb"))
                return loadThumbnail(item[0].toString());
            else if (item[1].toString().equals("load")) {
                loadBitmap(item[0].toString(), item[2]);
                return null;
            } else
                return getBitmap(item[0].toString(), item[2]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

