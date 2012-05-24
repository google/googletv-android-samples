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

package com.example.google.tv.anymotelibrary.client;

import android.content.Context;
import android.media.AudioManager;
import android.view.MotionEvent;

import com.example.google.tv.anymotelibrary.R;

/**
 * The trackball logic.
 */
public final class TrackballHandler {

    /**
     * Direction of dpad events.
     */
    enum Direction {
        DOWN,
        LEFT,
        RIGHT,
        UP
    }

    /**
     * Modes this handler can be in.
     */
    enum Mode {
        DPAD,
        SCROLL
    }

    /**
     * Sound played on trackball Dpad event.
     */
    private static final int SOUND_TRACKBALL = AudioManager.FX_KEY_CLICK;

    /**
     * Receives translated trackball events.
     */
    interface Listener {

        /**
         * Called when a trackball event should be interpreted as a dpad event.
         * 
         * @param direction represents the direction of the dpad event
         */
        void onDirectionalEvent(Direction direction);

        /**
         * Called when a trackball event should be interpreted as a scrolling
         * event.
         * 
         * @param dx scrolling delta along the horizontal axis
         * @param dy scrolling delta along the vertical axis
         */
        void onScrollEvent(int dx, int dy);

        /**
         * Called when the trackball was clicked.
         */
        void onClick();
    }

    private final Listener listener;

    /**
     * Parameter that controls the threshold for the amount of trackball motion
     * needed to generate a directional event.
     */
    private final float dpadThreshold;

    private final int scrollAmount;

    /**
     * {@code true} if trackball events should be handled.
     */
    private boolean enabled;

    /**
     * Mode this handler is currently in.
     */
    private Mode mode;

    /**
     * Used to store the amounts of trackball motion observed.
     */
    private float dpadAccuX, dpadAccuY;

    /**
     * Plays some sounds on trackball events.
     */
    private AudioManager audioManager;

    TrackballHandler(Listener listener, Context context) {
        this.listener = listener;
        this.mode = Mode.SCROLL;
        dpadThreshold = (float) context.getResources().getInteger(
                R.integer.dpad_threshold) / 100;
        scrollAmount = context.getResources().getInteger(
                R.integer.scroll_amount);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    /**
     * Should be called on every trackball event.
     */
    public boolean onTrackballEvent(MotionEvent event) {
        if (!enabled) {
            return false;
        }
        switch (mode) {
            case DPAD:
                return onDpad(event);

            case SCROLL:
                return onScroll(event);

            default:
                return false;
        }
    }

    /**
     * Called to interpret an event as a scrolling event.
     */
    private boolean onScroll(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int deltaX = (int) (scrollAmount * event.getX()
                    * event.getXPrecision());
            int deltaY = (int) (scrollAmount * event.getY()
                    * event.getYPrecision());
            listener.onScrollEvent(deltaX, deltaY);
            return true;
        }
        return false;
    }

    /**
     * Called to interpret an event as a dpad event.
     */
    private boolean onDpad(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            playSoundOnDPad();
            listener.onClick();
            resetMotionAccumulators();
            return true;
        }
        dpadAccuX += event.getX();
        dpadAccuY += event.getY();
        if (Math.abs(dpadAccuX) > dpadThreshold) {
            playSoundOnDPad();
            listener.onDirectionalEvent(
                    dpadAccuX > 0 ? Direction.RIGHT : Direction.LEFT);
            resetMotionAccumulators();
        }
        if (Math.abs(dpadAccuY) > dpadThreshold) {
            playSoundOnDPad();
            listener.onDirectionalEvent(
                    dpadAccuY > 0 ? Direction.DOWN : Direction.UP);
            resetMotionAccumulators();
        }
        return true;
    }

    private void resetMotionAccumulators() {
        dpadAccuX = 0;
        dpadAccuY = 0;
    }

    /**
     * Plays a sound when a Dpad event is performed.
     */
    private void playSoundOnDPad() {
        if (audioManager != null) {
            audioManager.playSoundEffect(SOUND_TRACKBALL);
        }
    }
}
