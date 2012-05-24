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

import com.google.android.tv.blackjack.service.BlackJackHand;
import com.google.android.tv.blackjack.service.Card;
import com.google.android.tv.blackjack.service.Dealer;
import com.google.android.tv.blackjack.service.Player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Display;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * The main BlackJack Activity on TV, which receives events from the client app
 * through Anymote.
 */
public class BlackJackTableActivity extends Activity {
    private FrameLayout table;

    private boolean mIsBound;
    private BlackJackService mBoundService;
    private BlackJackIntentReceiver receiver;
    private int screenWidth;
    private int screenHeight;
    private int cardWidth;
    private int cardHeight;
    private boolean gameOver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        table = (FrameLayout) findViewById(R.id.table);
        doBindService();
        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        cardWidth = 150;
        cardHeight = 300;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (gameOver && (keyCode == KeyEvent.KEYCODE_H || keyCode == KeyEvent.KEYCODE_S)) {
            Toast.makeText(this, "This game is over. Start a new game.", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_H) {
            mBoundService.hitPlayer();
        }
        if (keyCode == KeyEvent.KEYCODE_S) {
            mBoundService.standPlayer();
        }
        if (keyCode == KeyEvent.KEYCODE_N) {
            mBoundService.resetGame();
            gameOver = false;
            Toast.makeText(BlackJackTableActivity.this, "Starting new game", Toast.LENGTH_LONG)
                    .show();
            table.removeAllViews();
            mBoundService.start();
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onDestroy();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    private void drawPlayerCards(Player player) {
        int baseMarginLeft = screenWidth / 2 - cardWidth / 2;
        int baseMarginTop = screenHeight * 3 / 4 - cardHeight / 2;
        BlackJackHand hand = player.getHand();
        for (int i = 0; i < hand.getCardCount(); i++) {
            drawCard(i, hand.getCard(i), false, baseMarginTop, baseMarginLeft);
        }
    }

    private void drawDealerCards(Dealer dealer) {
        int baseMarginLeft = screenWidth / 2 - cardWidth / 2;
        int baseMarginTop = 0;
        boolean hide = false;
        BlackJackHand hand = dealer.getHand();
        for (int i = 0; i < hand.getCardCount(); i++) {
            // hide dealer's second card before show
            if (i == 0 && hand.getCardCount() <= 2) {
                hide = true;
            } else {
                hide = false;
            }
            drawCard(i, hand.getCard(i), hide, baseMarginTop, baseMarginLeft);
        }

    }

    private void showDealerCards(Dealer dealer) {
        int baseMarginLeft = screenWidth / 2 - cardWidth / 2;
        int baseMarginTop = 0;
        BlackJackHand hand = dealer.getHand();
        for (int i = 0; i < hand.getCardCount(); i++) {
            drawCard(i, hand.getCard(i), false, baseMarginTop, baseMarginLeft);
        }
    }

    private void drawCard(int cardIndex, Card card, boolean hide, int baseMarginTop,
            int baseMarginLeft) {
        int marginLeft = (baseMarginLeft + (cardIndex * 30));
        int marginTop = (baseMarginTop + (cardIndex * 20));

        int resID;
        if (!hide) {
            resID = getResources().getIdentifier(
                    "com.google.android.tv.blackjack:drawable/" + card.toString(), null, null);
        } else {
            resID = getResources().getIdentifier(
                    "com.google.android.tv.blackjack:drawable/card_back", null, null);
        }
        ImageView cardView = new ImageView(this);

        cardView.setImageResource(resID);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(150, 300);
        layoutParams.setMargins(marginLeft, marginTop, 0, 0);
        table.addView(cardView, layoutParams);
    }

    /** The receiver to receive communication from {@code BlackJackService} **/
    class BlackJackIntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CustomIntent.DRAW_PLAYER_INTENT)) {
                Player player = mBoundService.getPlayer();
            }
            if (intent.getAction().equals(CustomIntent.DRAW_PLAYER_CARDS_INTENT)) {
                Player player = mBoundService.getPlayer();
                drawPlayerCards(player);
            }
            if (intent.getAction().equals(CustomIntent.DRAW_DEALER_CARDS_INTENT)) {
                Dealer dealer = mBoundService.getDealer();
                drawDealerCards(dealer);
            }
            if (intent.getAction().equals(CustomIntent.SHOW_DEALER_CARDS_INTENT)) {
                Dealer dealer = mBoundService.getDealer();
                showDealerCards(dealer);
            }
            if (intent.getAction().equals(CustomIntent.PLAYER_WINS_INTENT)) {
                Toast.makeText(BlackJackTableActivity.this, "Player wins!", Toast.LENGTH_LONG)
                        .show();
                mBoundService.resetGame();
                gameOver = true;

            }
            if (intent.getAction().equals(CustomIntent.DEALER_WINS_INTENT)) {
                Toast.makeText(BlackJackTableActivity.this, "Dealer wins!", Toast.LENGTH_LONG)
                        .show();
                mBoundService.resetGame();
                gameOver = true;

            }
        }

    }

    /** This is used to send communication to the {@code BlackJackService} **/
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((BlackJackService.LocalBinder) service).getService();
            IntentFilter filter = new IntentFilter(CustomIntent.DRAW_PLAYER_INTENT);
            filter.addAction(CustomIntent.DRAW_PLAYER_CARDS_INTENT);
            filter.addAction(CustomIntent.DRAW_DEALER_CARDS_INTENT);
            filter.addAction(CustomIntent.SHOW_DEALER_CARDS_INTENT);
            filter.addAction(CustomIntent.PLAYER_WINS_INTENT);
            filter.addAction(CustomIntent.DEALER_WINS_INTENT);
            receiver = new BlackJackIntentReceiver();
            BlackJackTableActivity.this.registerReceiver(receiver, filter);
            mBoundService.start();

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };

    void doBindService() {
        // Establish a connection with the service. We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(BlackJackTableActivity.this, BlackJackService.class), mConnection,
                Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
}
