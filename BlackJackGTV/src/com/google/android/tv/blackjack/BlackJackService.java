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

import com.google.android.tv.blackjack.service.Dealer;
import com.google.android.tv.blackjack.service.Deck;
import com.google.android.tv.blackjack.service.Player;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * This service handles the BlackJack game logic.
 */
public class BlackJackService extends Service {

    private NotificationManager mNM;
    private Deck deck;
    private Dealer dealer;
    private Player player;
    private int numBets;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.i("TAg", "\n\nService created");
        deck = new Deck();
        dealer = new Dealer();
        player = new Player();
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        BlackJackService getService() {
            return BlackJackService.this;
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    public void start() {
        // start dealing cards.
        player.prepareForDeal();
        dealPlayer(player);
        deal(dealer);
    }

    public void dealPlayer(Player player) {
        player.addCard(deck.dealCard());
        player.addCard(deck.dealCard());
        final Intent drawPlayerCardsIntent = new Intent(CustomIntent.DRAW_PLAYER_CARDS_INTENT);
        sendBroadcast(drawPlayerCardsIntent);
    }

    public void deal(Dealer dealer) {
        dealer.addCard(deck.dealCard());
        dealer.addCard(deck.dealCard());
        final Intent drawDealerCardsIntent = new Intent(CustomIntent.DRAW_DEALER_CARDS_INTENT);
        sendBroadcast(drawDealerCardsIntent);
    }

    public void hitPlayer() {
        player.addCard(deck.dealCard());
        final Intent drawPlayerCardsIntent = new Intent(CustomIntent.DRAW_PLAYER_CARDS_INTENT);
        sendBroadcast(drawPlayerCardsIntent);
        if (player.getHand().getBlackjackValue() > 21) {
            final Intent dealerWinsIntent = new Intent(CustomIntent.DEALER_WINS_INTENT);
            sendBroadcast(dealerWinsIntent);
        }
    }

    public void standPlayer() {
        showDealerCards();
        boolean playerWins = false;
        Intent intent = null;
        if (dealer.getHand().getBlackjackValue() != 21) {
            while (dealer.getHand().getBlackjackValue() < 17) {
                hitDealer();
                if (dealer.getHand().getBlackjackValue() > 21) {
                    playerWins = true;
                    break;
                }
            }
            if (player.getHand().getBlackjackValue() > dealer.getHand().getBlackjackValue()) {
                playerWins = true;
            } else {
                playerWins = false;
            }
        }
        if (playerWins) {
            intent = new Intent(CustomIntent.PLAYER_WINS_INTENT);
        } else {
            intent = new Intent(CustomIntent.DEALER_WINS_INTENT);
        }
        sendBroadcast(intent);
    }

    public void showDealerCards() {
        final Intent showDealerCardsIntent = new Intent(CustomIntent.SHOW_DEALER_CARDS_INTENT);
        sendBroadcast(showDealerCardsIntent);
    }

    public void hitDealer() {
        dealer.addCard(deck.dealCard());
        if (dealer.getHand().getBlackjackValue() > 21) {
            final Intent playerLostsIntent = new Intent(CustomIntent.DEALER_WINS_INTENT);
            sendBroadcast(playerLostsIntent);
        }
    }

    public Deck getDeck() {
        return deck;
    }

    public Player getPlayer() {
        return player;
    }

    public Dealer getDealer() {
        return dealer;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    public void resetGame() {
        deck = new Deck();
        dealer = new Dealer();
        player = new Player();
    }
}
