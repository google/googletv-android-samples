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

package com.example.google.tv.anymotelibrary.touch;

import android.os.Build;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Factory of {@link ScaleGestureDetector} implementation. Creates gesture
 * detector that depends on available APIs. For API levels that support
 * multitouch, creates implementation that detects two finger gestures.
 */
public class ScaleGestureDetectorFactory {
    private static final int MIN_API_LEVEL_MULTITOUCH = 5;

    private ScaleGestureDetectorFactory() {
        // prevents instantiation
        throw new IllegalStateException();
    }

    public static ScaleGestureDetector createScaleGestureDetector(View view,
            ScaleGestureDetector.OnScaleGestureListener listener) {
        ScaleGestureDetector result = null;
        if (Build.VERSION.SDK_INT >= MIN_API_LEVEL_MULTITOUCH) {
            result = createScaleGestureDetectorImpl(view, listener);
        }
        return result;
    }

    private static ScaleGestureDetector createScaleGestureDetectorImpl(View view,
            ScaleGestureDetector.OnScaleGestureListener listener) {
        try {
            Class<?> clazz = Class.forName(
                    "com.google.android.apps.tvremote.backport.ScaleGestureDetectorImpl");
            Constructor<?> constructor = clazz.getConstructor(View.class,
                    ScaleGestureDetector.OnScaleGestureListener.class);
            return (ScaleGestureDetector) constructor.newInstance(view, listener);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
