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

// BACKPORTED FROM ANDROID PUBLIC REPOSITORY

package com.example.google.tv.anymotelibrary.touch;

import android.view.MotionEvent;

/**
 * Detects transformation gestures involving more than one pointer
 * ("multitouch") using the supplied {@link MotionEvent}s. The
 * {@link OnScaleGestureListener} callback will notify users when a particular
 * gesture event has occurred. This class should only be used with
 * {@link MotionEvent}s reported via touch. To use this class:
 * <ul>
 * <li>Create an instance of the {@code ScaleGestureDetector} for your
 * {@link View}
 * <li>In the {@link View#onTouchEvent(MotionEvent)} method ensure you call
 * {@link #onTouchEvent(MotionEvent)}. The methods defined in your callback will
 * be executed when the events occur.
 * </ul>
 */
public interface ScaleGestureDetector {
    /**
     * The listener for receiving notifications when gestures occur. If you want
     * to listen for all the different gestures then implement this interface.
     * If you only want to listen for a subset it might be easier to extend
     * {@link SimpleOnScaleGestureListener}. An application will receive events
     * in the following order:
     * <ul>
     * <li>One {@link OnScaleGestureListener#onScaleBegin(ScaleGestureDetector)}
     * <li>Zero or more
     * {@link OnScaleGestureListener#onScale(ScaleGestureDetector)}
     * <li>One {@link OnScaleGestureListener#onScaleEnd(ScaleGestureDetector)}
     * </ul>
     */
    public interface OnScaleGestureListener {
        /**
         * Responds to scaling events for a gesture in progress. Reported by
         * pointer motion.
         * 
         * @param detector The detector reporting the event - use this to
         *            retrieve extended info about event state.
         * @return Whether or not the detector should consider this event as
         *         handled. If an event was not handled, the detector will
         *         continue to accumulate movement until an event is handled.
         *         This can be useful if an application, for example, only wants
         *         to update scaling factors if the change is greater than 0.01.
         */
        public boolean onScale(ScaleGestureDetector detector);

        /**
         * Responds to the beginning of a scaling gesture. Reported by new
         * pointers going down.
         * 
         * @param detector The detector reporting the event - use this to
         *            retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing this
         *         gesture. For example, if a gesture is beginning with a focal
         *         point outside of a region where it makes sense,
         *         onScaleBegin() may return false to ignore the rest of the
         *         gesture.
         */
        public boolean onScaleBegin(ScaleGestureDetector detector);

        /**
         * Responds to the end of a scale gesture. Reported by existing pointers
         * going up. Once a scale has ended,
         * {@link ScaleGestureDetector#getFocusX()} and
         * {@link ScaleGestureDetector#getFocusY()} will return the location of
         * the pointer remaining on the screen.
         * 
         * @param detector The detector reporting the event - use this to
         *            retrieve extended info about event state.
         */
        public void onScaleEnd(ScaleGestureDetector detector);
    }

    /**
     * A convenience class to extend when you only want to listen for a subset
     * of scaling-related events. This implements all methods in
     * {@link OnScaleGestureListener} but does nothing.
     * {@link OnScaleGestureListener#onScale(ScaleGestureDetector)} returns
     * {@code false} so that a subclass can retrieve the accumulated scale
     * factor in an overridden onScaleEnd.
     * {@link OnScaleGestureListener#onScaleBegin(ScaleGestureDetector)} returns
     * {@code true}.
     */
    public static class SimpleOnScaleGestureListener
            implements OnScaleGestureListener {

        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            // Intentionally empty
        }
    }

    /**
     * Returns {@code true} if a two-finger scale gesture is in progress.
     * 
     * @return {@code true} if a scale gesture is in progress, {@code false}
     *         otherwise.
     */
    public boolean isInProgress();

    /**
     * Get the X coordinate of the current gesture's focal point. If a gesture
     * is in progress, the focal point is directly between the two pointers
     * forming the gesture. If a gesture is ending, the focal point is the
     * location of the remaining pointer on the screen. If
     * {@link #isInProgress()} would return false, the result of this function
     * is undefined.
     * 
     * @return X coordinate of the focal point in pixels.
     */
    public float getFocusX();

    /**
     * Get the Y coordinate of the current gesture's focal point. If a gesture
     * is in progress, the focal point is directly between the two pointers
     * forming the gesture. If a gesture is ending, the focal point is the
     * location of the remaining pointer on the screen. If
     * {@link #isInProgress()} would return false, the result of this function
     * is undefined.
     * 
     * @return Y coordinate of the focal point in pixels.
     */
    public float getFocusY();

    /**
     * Return the current distance between the two pointers forming the gesture
     * in progress.
     * 
     * @return Distance between pointers in pixels.
     */
    public float getCurrentSpan();

    /**
     * Return the previous distance between the two pointers forming the gesture
     * in progress.
     * 
     * @return Previous distance between pointers in pixels.
     */
    public float getPreviousSpan();

    /**
     * Return the scaling factor from the previous scale event to the current
     * event. This value is defined as ({@link #getCurrentSpan()} /
     * {@link #getPreviousSpan()}).
     * 
     * @return The current scaling factor.
     */
    public float getScaleFactor();

    /**
     * Return the time difference in milliseconds between the previous accepted
     * scaling event and the current scaling event.
     * 
     * @return Time difference since the last scaling event in milliseconds.
     */
    public long getTimeDelta();

    /**
     * Return the event time of the current event being processed.
     * 
     * @return Current event time in milliseconds.
     */
    public long getEventTime();

    public boolean onTouchEvent(MotionEvent event);
}
