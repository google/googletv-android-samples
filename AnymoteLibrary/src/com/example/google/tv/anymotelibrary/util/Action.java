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

import com.google.anymote.Key;
import com.google.anymote.Key.Code;

import com.example.google.tv.anymotelibrary.client.AnymoteSender;

/**
 * Lists common control actions on a Google TV box.
 */
public enum Action {

    BACKSPACE {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_DEL);
        }
    },

    CLICK_DOWN {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKey(Code.BTN_MOUSE, Key.Action.DOWN);
        }
    },

    CLICK_UP {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKey(Code.BTN_MOUSE, Key.Action.UP);
        }
    },

    DPAD_CENTER {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_DPAD_CENTER);
        }
    },

    DPAD_DOWN {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_DPAD_DOWN);
        }
    },

    DPAD_DOWN_PRESSED {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKey(Code.KEYCODE_DPAD_DOWN, Key.Action.DOWN);
        }
    },

    DPAD_DOWN_RELEASED {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKey(Code.KEYCODE_DPAD_DOWN, Key.Action.UP);
        }
    },

    DPAD_LEFT {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_DPAD_LEFT);
        }
    },

    DPAD_LEFT_PRESSED {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKey(Code.KEYCODE_DPAD_LEFT, Key.Action.DOWN);
        }
    },

    DPAD_LEFT_RELEASED {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKey(Code.KEYCODE_DPAD_LEFT, Key.Action.UP);
        }
    },

    DPAD_RIGHT {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_DPAD_RIGHT);
        }
    },

    DPAD_RIGHT_PRESSED {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKey(Code.KEYCODE_DPAD_RIGHT, Key.Action.DOWN);
        }
    },

    DPAD_RIGHT_RELEASED {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKey(Code.KEYCODE_DPAD_RIGHT, Key.Action.UP);
        }
    },

    DPAD_UP {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_DPAD_UP);
        }
    },

    DPAD_UP_PRESSED {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKey(Code.KEYCODE_DPAD_UP, Key.Action.DOWN);
        }
    },

    DPAD_UP_RELEASED {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKey(Code.KEYCODE_DPAD_UP, Key.Action.UP);
        }
    },

    ENTER {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_ENTER);
        }
    },

    ESCAPE {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_ESCAPE);
        }
    },

    GO_TO_DVR {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_DVR);
        }
    },

    GO_TO_GUIDE {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_GUIDE);
        }
    },

    GO_TO_LIVE_TV {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_LIVE);
        }
    },

    NAVBAR {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_SEARCH);
        }
    },

    POWER {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_POWER);
        }
    },

    VOLUME_DOWN {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_VOLUME_DOWN);
        }
    },

    VOLUME_UP {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_VOLUME_UP);
        }
    },

    ZOOM_IN {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_ZOOM_IN);
        }
    },

    ZOOM_OUT {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_ZOOM_OUT);
        }
    },

    COLOR_RED {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_PROG_RED);
        }
    },

    COLOR_GREEN {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_PROG_GREEN);
        }
    },

    COLOR_YELLOW {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_PROG_YELLOW);
        }
    },

    COLOR_BLUE {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_PROG_BLUE);
        }
    },

    POWER_BD {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_BD_POWER);
        }
    },

    INPUT_BD {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_BD_INPUT);
        }
    },

    POWER_AVR {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_AVR_POWER);
        }
    },

    INPUT_AVR {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_AVR_INPUT);
        }
    },

    POWER_TV {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_TV_POWER);
        }
    },

    INPUT_TV {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_TV_INPUT);
        }
    },

    BD_TOP_MENU {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_BD_TOP_MENU);
        }
    },

    BD_MENU {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_BD_POPUP_MENU);
        }
    },

    EJECT {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_EJECT);
        }
    },

    AUDIO {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_AUDIO);
        }
    },

    SETTINGS {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_SETTINGS);
        }
    },

    CAPTIONS {
    @Override
        public void execute(AnymoteSender anymoteProxy) {
            anymoteProxy.sendKeyPress(Code.KEYCODE_INSERT);
        }
    };

    /**
     * Executes the action.
     * 
     * @param anymoteProxy interface to the remote box
     */
    public abstract void execute(AnymoteSender anymoteProxy);
}
