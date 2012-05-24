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

package com.google.android.tv.blackjack;

/**
 * Custom Intents used by the BlackJackService to communicate with the
 * BlackJackActivity.
 */
public class CustomIntent {
    public static String DRAW_PLAYER_INTENT = "draw_player";
    public static String DRAW_PLAYER_CARDS_INTENT = "draw_player_cards";
    public static String DRAW_DEALER_CARDS_INTENT = "draw_dealer_cards";
    public static String SHOW_DEALER_CARDS_INTENT = "show_dealer_cards";
    public static String PLAYER_WINS_INTENT = "player_wins";
    public static String DEALER_WINS_INTENT = "dealer_wins";
}
