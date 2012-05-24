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

import java.util.Vector;

/**
 * This class represents a hand of cards.
 */
public class Hand {

    private Vector hand; // The cards in the hand.

    public Hand() {
        // Create a Hand object that is initially empty.
        hand = new Vector();
    }

    public void clear() {
        // Discard all the cards from the hand.
        hand.removeAllElements();
    }

    public void addCard(Card c) {
        // Add the card c to the hand. c should be non-null. (If c is
        // null, nothing is added to the hand.)
        if (c != null)
            hand.addElement(c);
    }

    public void removeCard(Card c) {
        // If the specified card is in the hand, it is removed.
        hand.removeElement(c);
    }

    public void removeCard(int position) {
        // If the specified position is a valid position in the hand,
        // then the card in that position is removed.
        if (position >= 0 && position < hand.size())
            hand.removeElementAt(position);
    }

    public int getCardCount() {
        // Return the number of cards in the hand.
        return hand.size();
    }

    public Card getCard(int position) {
        // Get the card from the hand in given position, where positions
        // are numbered starting from 0. If the specified position is
        // not the position number of a card in the hand, then null
        // is returned.
        if (position >= 0 && position < hand.size())
            return (Card) hand.elementAt(position);
        else
            return null;
    }
}
