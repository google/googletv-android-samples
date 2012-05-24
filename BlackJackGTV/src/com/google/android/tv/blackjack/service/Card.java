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
 * An object of class card represents one of the 52 cards in a standard deck of
 * playing cards. Each card has a suit and a value.
 */
public class Card {

    public final static int SPADES = 0, // Codes for the 4 suits.
            HEARTS = 1, DIAMONDS = 2, CLUBS = 3;

    public final static int ACE = 1, // Codes for the non-numeric cards.
            JACK = 11, // Cards 2 through 10 have their
            QUEEN = 12, // numerical values for their codes.
            KING = 13;

    private final int suit; // The suit of this card, one of the constants
                            // SPADES, HEARTS, DIAMONDS, CLUBS.

    private final int value; // The value of this card, from 1 to 11.

    private String image; // String name of Image of the card

    public Card(int theValue, int theSuit) {
        // Construct a card with the specified value and suit.
        // Value must be between 1 and 13. Suit must be between
        // 0 and 3. If the parameters are outside these ranges,
        // the constructed card object will be invalid.
        value = theValue;
        suit = theSuit;
        image = value + "_of_" + getSuitAsString();
    }

    public int getSuit() {
        // Return the int that codes for this card's suit.
        return suit;
    }

    public int getValue() {
        // Return the int that codes for this card's value.
        return value;
    }

    public String getSuitAsString() {
        // Return a String representing the card's suit.
        // (If the card's suit is invalid, "??" is returned.)
        switch (suit) {
            case SPADES:
                return "spades";
            case HEARTS:
                return "hearts";
            case DIAMONDS:
                return "diamonds";
            case CLUBS:
                return "clubs";
            default:
                return "??";
        }
    }

    public String getValueAsString() {
        // Return a String representing the card's value.
        // If the card's value is invalid, "??" is returned.
        switch (value) {
            case 1:
                return "ace";
            case 2:
                return "two";
            case 3:
                return "three";
            case 4:
                return "four";
            case 5:
                return "five";
            case 6:
                return "six";
            case 7:
                return "seven";
            case 8:
                return "eight";
            case 9:
                return "nine";
            case 10:
                return "ten";
            case 11:
                return "jack";
            case 12:
                return "queen";
            case 13:
                return "king";
            default:
                return "??";
        }
    }

    public String toString() {
        // Return a String representation of this card, such as
        // "10 of Hearts" or "Queen of Spades".
        return getValueAsString() + "_of_" + getSuitAsString();
    }
}
