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

package com.example.google.tv.anymotelibrary.util;

import java.util.HashMap;
import java.util.Map;

import android.view.KeyEvent;

import com.google.anymote.Key.Code;

/**
 * Serves as a translation mechanism from Anymote key codes to
 * android/application understandable events.
 */

public class KeyEventTranslator {
    private static Map<Integer, Code> mapKeyEvent = new HashMap<Integer, Code>();

    /*
     * Initialize static hash map. This constant initializer serves only the
     * purpose of filling the translation maps.
     */
    static {
        mapKeyEvent.put(KeyEvent.KEYCODE_SOFT_LEFT, Code.KEYCODE_SOFT_LEFT);
        mapKeyEvent.put(KeyEvent.KEYCODE_SOFT_RIGHT, Code.KEYCODE_SOFT_RIGHT);
        mapKeyEvent.put(KeyEvent.KEYCODE_HOME, Code.KEYCODE_HOME);
        mapKeyEvent.put(KeyEvent.KEYCODE_BACK, Code.KEYCODE_BACK);
        mapKeyEvent.put(KeyEvent.KEYCODE_CALL, Code.KEYCODE_CALL);
        mapKeyEvent.put(KeyEvent.KEYCODE_0, Code.KEYCODE_0);
        mapKeyEvent.put(KeyEvent.KEYCODE_1, Code.KEYCODE_1);
        mapKeyEvent.put(KeyEvent.KEYCODE_2, Code.KEYCODE_2);
        mapKeyEvent.put(KeyEvent.KEYCODE_3, Code.KEYCODE_3);
        mapKeyEvent.put(KeyEvent.KEYCODE_4, Code.KEYCODE_4);
        mapKeyEvent.put(KeyEvent.KEYCODE_5, Code.KEYCODE_5);
        mapKeyEvent.put(KeyEvent.KEYCODE_6, Code.KEYCODE_6);
        mapKeyEvent.put(KeyEvent.KEYCODE_7, Code.KEYCODE_7);
        mapKeyEvent.put(KeyEvent.KEYCODE_8, Code.KEYCODE_8);
        mapKeyEvent.put(KeyEvent.KEYCODE_9, Code.KEYCODE_9);
        mapKeyEvent.put(KeyEvent.KEYCODE_STAR, Code.KEYCODE_STAR);
        mapKeyEvent.put(KeyEvent.KEYCODE_POUND, Code.KEYCODE_POUND);
        mapKeyEvent.put(KeyEvent.KEYCODE_DPAD_UP, Code.KEYCODE_DPAD_UP);
        mapKeyEvent.put(KeyEvent.KEYCODE_DPAD_DOWN, Code.KEYCODE_DPAD_DOWN);
        mapKeyEvent.put(KeyEvent.KEYCODE_DPAD_LEFT, Code.KEYCODE_DPAD_LEFT);
        mapKeyEvent.put(KeyEvent.KEYCODE_DPAD_RIGHT, Code.KEYCODE_DPAD_RIGHT);
        mapKeyEvent.put(KeyEvent.KEYCODE_DPAD_CENTER, Code.KEYCODE_DPAD_CENTER);
        mapKeyEvent.put(KeyEvent.KEYCODE_VOLUME_UP, Code.KEYCODE_VOLUME_UP);
        mapKeyEvent.put(KeyEvent.KEYCODE_VOLUME_DOWN, Code.KEYCODE_VOLUME_DOWN);
        mapKeyEvent.put(KeyEvent.KEYCODE_POWER, Code.KEYCODE_POWER);
        mapKeyEvent.put(KeyEvent.KEYCODE_CAMERA, Code.KEYCODE_CAMERA);
        mapKeyEvent.put(KeyEvent.KEYCODE_A, Code.KEYCODE_A);
        mapKeyEvent.put(KeyEvent.KEYCODE_B, Code.KEYCODE_B);
        mapKeyEvent.put(KeyEvent.KEYCODE_C, Code.KEYCODE_C);
        mapKeyEvent.put(KeyEvent.KEYCODE_D, Code.KEYCODE_D);
        mapKeyEvent.put(KeyEvent.KEYCODE_E, Code.KEYCODE_E);
        mapKeyEvent.put(KeyEvent.KEYCODE_F, Code.KEYCODE_F);
        mapKeyEvent.put(KeyEvent.KEYCODE_G, Code.KEYCODE_G);
        mapKeyEvent.put(KeyEvent.KEYCODE_H, Code.KEYCODE_H);
        mapKeyEvent.put(KeyEvent.KEYCODE_I, Code.KEYCODE_I);
        mapKeyEvent.put(KeyEvent.KEYCODE_J, Code.KEYCODE_J);
        mapKeyEvent.put(KeyEvent.KEYCODE_K, Code.KEYCODE_K);
        mapKeyEvent.put(KeyEvent.KEYCODE_L, Code.KEYCODE_L);
        mapKeyEvent.put(KeyEvent.KEYCODE_M, Code.KEYCODE_M);
        mapKeyEvent.put(KeyEvent.KEYCODE_N, Code.KEYCODE_N);
        mapKeyEvent.put(KeyEvent.KEYCODE_O, Code.KEYCODE_O);
        mapKeyEvent.put(KeyEvent.KEYCODE_P, Code.KEYCODE_P);
        mapKeyEvent.put(KeyEvent.KEYCODE_Q, Code.KEYCODE_Q);
        mapKeyEvent.put(KeyEvent.KEYCODE_R, Code.KEYCODE_R);
        mapKeyEvent.put(KeyEvent.KEYCODE_S, Code.KEYCODE_S);
        mapKeyEvent.put(KeyEvent.KEYCODE_T, Code.KEYCODE_T);
        mapKeyEvent.put(KeyEvent.KEYCODE_U, Code.KEYCODE_U);
        mapKeyEvent.put(KeyEvent.KEYCODE_V, Code.KEYCODE_V);
        mapKeyEvent.put(KeyEvent.KEYCODE_W, Code.KEYCODE_W);
        mapKeyEvent.put(KeyEvent.KEYCODE_X, Code.KEYCODE_X);
        mapKeyEvent.put(KeyEvent.KEYCODE_Y, Code.KEYCODE_Y);
        mapKeyEvent.put(KeyEvent.KEYCODE_Z, Code.KEYCODE_Z);
        mapKeyEvent.put(KeyEvent.KEYCODE_COMMA, Code.KEYCODE_COMMA);
        mapKeyEvent.put(KeyEvent.KEYCODE_PERIOD, Code.KEYCODE_PERIOD);
        mapKeyEvent.put(KeyEvent.KEYCODE_ALT_LEFT, Code.KEYCODE_ALT_LEFT);
        mapKeyEvent.put(KeyEvent.KEYCODE_ALT_RIGHT, Code.KEYCODE_ALT_RIGHT);
        mapKeyEvent.put(KeyEvent.KEYCODE_SHIFT_LEFT, Code.KEYCODE_SHIFT_LEFT);
        mapKeyEvent.put(KeyEvent.KEYCODE_SHIFT_RIGHT, Code.KEYCODE_SHIFT_RIGHT);
        mapKeyEvent.put(KeyEvent.KEYCODE_TAB, Code.KEYCODE_TAB);
        mapKeyEvent.put(KeyEvent.KEYCODE_SPACE, Code.KEYCODE_SPACE);
        mapKeyEvent.put(KeyEvent.KEYCODE_EXPLORER, Code.KEYCODE_EXPLORER);
        mapKeyEvent.put(KeyEvent.KEYCODE_ENTER, Code.KEYCODE_ENTER);
        mapKeyEvent.put(KeyEvent.KEYCODE_DEL, Code.KEYCODE_DEL);
        mapKeyEvent.put(KeyEvent.KEYCODE_GRAVE, Code.KEYCODE_GRAVE);
        mapKeyEvent.put(KeyEvent.KEYCODE_MINUS, Code.KEYCODE_MINUS);
        mapKeyEvent.put(KeyEvent.KEYCODE_EQUALS, Code.KEYCODE_EQUALS);
        mapKeyEvent.put(KeyEvent.KEYCODE_LEFT_BRACKET, Code.KEYCODE_LEFT_BRACKET);
        mapKeyEvent.put(KeyEvent.KEYCODE_RIGHT_BRACKET, Code.KEYCODE_RIGHT_BRACKET);
        mapKeyEvent.put(KeyEvent.KEYCODE_BACKSLASH, Code.KEYCODE_BACKSLASH);
        mapKeyEvent.put(KeyEvent.KEYCODE_SEMICOLON, Code.KEYCODE_SEMICOLON);
        mapKeyEvent.put(KeyEvent.KEYCODE_APOSTROPHE, Code.KEYCODE_APOSTROPHE);
        mapKeyEvent.put(KeyEvent.KEYCODE_SLASH, Code.KEYCODE_SLASH);
        mapKeyEvent.put(KeyEvent.KEYCODE_AT, Code.KEYCODE_AT);
        mapKeyEvent.put(KeyEvent.KEYCODE_FOCUS, Code.KEYCODE_FOCUS);
        mapKeyEvent.put(KeyEvent.KEYCODE_PLUS, Code.KEYCODE_PLUS);
        mapKeyEvent.put(KeyEvent.KEYCODE_MENU, Code.KEYCODE_MENU);
        mapKeyEvent.put(KeyEvent.KEYCODE_SEARCH, Code.KEYCODE_SEARCH);
        mapKeyEvent.put(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, Code.KEYCODE_MEDIA_PLAY_PAUSE);
        mapKeyEvent.put(KeyEvent.KEYCODE_MEDIA_STOP, Code.KEYCODE_MEDIA_STOP);
        mapKeyEvent.put(KeyEvent.KEYCODE_MEDIA_NEXT, Code.KEYCODE_MEDIA_NEXT);
        mapKeyEvent.put(KeyEvent.KEYCODE_MEDIA_PREVIOUS, Code.KEYCODE_MEDIA_PREVIOUS);
        mapKeyEvent.put(KeyEvent.KEYCODE_MEDIA_REWIND, Code.KEYCODE_MEDIA_REWIND);
        mapKeyEvent.put(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, Code.KEYCODE_MEDIA_FAST_FORWARD);
        mapKeyEvent.put(KeyEvent.KEYCODE_MUTE, Code.KEYCODE_MUTE);
        mapKeyEvent.put(KeyEvent.KEYCODE_MEDIA_PREVIOUS, Code.KEYCODE_MEDIA_SKIP_BACK);
        mapKeyEvent.put(KeyEvent.KEYCODE_MEDIA_NEXT, Code.KEYCODE_MEDIA_SKIP_FORWARD);
    }

    /**
     * Translate key event to Anymote code.
     * 
     * @param keyEvent android.view.KeyEvent.KEYCODE_ value to translate from.
     * @return Anymote code or null, if translation not found.
     */
    public static Code fromKeyEvent(int keyEvent) {
        return mapKeyEvent.get(keyEvent);
    }
}
