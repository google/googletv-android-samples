/*
 * Copyright (C) 2010 The Android Open Source Project 
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

package com.example.google.tv.notifier;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RemoteViews;

/**
 * Sample demo App for Google TV notification
 */
public class NotifierActivity extends Activity {
    /** Called when the activity is first created. */
    private NotificationManager mNotificationManager;
    private static final int NOTFICATION_ID = 0;
    private Button mButton1, mButton2, mButton3;

    RemoteViews contentView;
    Notification.Builder mBuilder, mBuilder2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mButton1 = (Button) findViewById(R.id.btnNotify1);
        mButton2 = (Button) findViewById(R.id.btnNotify2);
        mButton3 = (Button) findViewById(R.id.btnNotify3);
        mBuilder = new Notification.Builder(this);

        Intent notificationIntent = new Intent(NotifierActivity.this,
                NotifierActivity.class);
        final PendingIntent contentIntent = PendingIntent.getActivity(
                NotifierActivity.this, 0, notificationIntent, 0);

        // Default notification
        mButton1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mBuilder.setTicker(getText(R.string.ticker_text))
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(getString(R.string.content_title))
                        .setContentText(getText(R.string.content_text))
                        .setContentIntent(contentIntent);
                mNotificationManager.notify(NOTFICATION_ID,
                        mBuilder.getNotification());
            }
        });

        // Setting a custom view for the notification
        mButton2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                contentView = new RemoteViews(getPackageName(),
                        R.layout.notify_text);
                contentView.setImageViewResource(R.id.image_custom,
                        R.drawable.android);
                contentView.setTextViewText(R.id.text,
                        getText(R.string.custom_notification));
                mBuilder2 = new Notification.Builder(NotifierActivity.this)
                        .setSmallIcon(R.drawable.icon)
                        .setTicker(getText(R.string.ticker_text1))
                        .setContent(contentView)
                        .setContentIntent(contentIntent);
                mNotificationManager.notify(NOTFICATION_ID + 1,
                        mBuilder2.getNotification());
            }

        });

        // Ongoing notifications can't clear using 'X' button or "Clear all"
        mButton3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mBuilder.setTicker(getText(R.string.ticker_text2))
                        .setContentTitle(
                                getString(R.string.ongoing_content_title))
                        .setContentText(getText(R.string.ongoing_content_text))
                        .setContentIntent(contentIntent).setOngoing(true);
                mNotificationManager.notify(NOTFICATION_ID + 2,
                        mBuilder.getNotification());
            }
        });

    }

}
