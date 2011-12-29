/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.android.panoramio;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.example.google.tv.leftnavbar.LeftNavBar;
import com.example.google.tv.leftnavbar.LeftNavBarService;

/**
 * This class helps with setting the left-side navigation bar in an Activity's layout.
 */
public class PanoramioLeftNavService {

    private static Context mContext;

    public static LeftNavBar getLeftNavBar(Context context) {
        LeftNavBar bar = (LeftNavBarService.instance()).getLeftNavBar((Activity) context);
        mContext = context;
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayUseLogoEnabled(false);
        bar.removeAllTabs();

        bar.addTab(bar.newTab().setText(R.string.search).setIcon(R.drawable.search)
                .setTabListener(searchTabListener), false);
        bar.setNavigationMode(LeftNavBar.NAVIGATION_MODE_TABS);
        // bar.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.red_gradient));
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
        bar.setDisplayShowHomeEnabled(true);

        bar.addTab(bar.newTab().setText(R.string.cool_places).setIcon(R.drawable.places)
                .setTabListener(placesTabListener), false);
        bar.showOptionsMenu(false);
        bar.setDisplayUseLogoEnabled(true);
        // bar.setDisplayOptions(LeftNavBar.DISPLAY_AUTO_EXPAND);
        bar.setShowHideAnimationEnabled(true);
        return bar;
    }

    private static LeftNavBar.TabListener searchTabListener = new LeftNavBar.TabListener() {
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            ((Activity) mContext).onSearchRequested();
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    };

    private static LeftNavBar.TabListener placesTabListener = new LeftNavBar.TabListener() {
        public void onTabSelected(Tab tab, FragmentTransaction ft) {

            Intent intent = new Intent();
            intent.setClass(mContext, CoolPlacesActivity.class);
            intent.putExtra("index", 0);
            mContext.startActivity(intent);
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    };
}
