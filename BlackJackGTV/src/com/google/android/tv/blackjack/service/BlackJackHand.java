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

package com.google.android.tv.blackjack.service;

/**
 * A subclass of the Hand class that represents a hand of cards in the game of
 * Blackjack. To the methods inherited form Hand, it adds the method
 * getBlackjackHand(), which returns the value of the hand for the game of
 * Blackjack.
 */
public class BlackJackHand extends Hand {
    /**
     * Calculates the BlackJack value of the hand.
     */
    public int getBlackjackValue() {
        int val; // The value computed for the hand.
        boolean ace; // This will be set to true if the
                     // hand contains an ace.
        int cards; // Number of cards in the hand.

        val = 0;
        ace = false;
        cards = getCardCount();

        for (int i = 0; i < cards; i++) {
            // Add the value of the i-th card in the hand.
            Card card; // The i-th card;
            int cardVal; // The blackjack value of the i-th card.
            card = getCard(i);
            cardVal = card.getValue(); // The normal value, 1 to 13.
            if (cardVal > 10) {
                cardVal = 10; // For a Jack, Queen, or King.
            }
            if (cardVal == 1) {
                ace = true; // There is at least one ace.
            }
            val = val + cardVal;
        }
        // Now, val is the value of the hand, counting any ace as 1.
        // If there is an ace, and if changing its value from 1 to
        // 11 would leave the score less than or equal to 21,
        // then do so by adding the extra 10 points to val.

        if (ace == true && val + 10 <= 21)
            val = val + 10;

        return val;
    }
}
