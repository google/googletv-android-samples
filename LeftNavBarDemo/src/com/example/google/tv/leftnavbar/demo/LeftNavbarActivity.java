/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.example.google.tv.leftnavbar.demo;

import com.example.google.tv.leftnavbar.LeftNavBar;
import com.example.google.tv.leftnavbar.LeftNavBarService;
import com.example.google.tv.leftnavbar.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Exercises the LeftNavBar API.
 */
public class LeftNavbarActivity extends BaseActivity {

    private static final ActionBar.TabListener BLANK_LISTENER = new ActionBar.TabListener() {
        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {}

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {}
    };

    private boolean mHasOptionsMenu;
    private boolean mHasShowAlwaysItems;
    private boolean mHasCustomTabs;
    private LeftNavBar mLeftNavBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LeftNavBar bar = (LeftNavBarService.instance()).getLeftNavBar((Activity) this);
        setContentView(R.layout.api_controls);
        bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.leftnav_bar_background_dark));
        setupButtons();
        // Prepare the left navigation bar.
        setupBar();
    }
    
    private LeftNavBar getLeftNavBar() {
        if (mLeftNavBar == null) {
            mLeftNavBar = new LeftNavBar(this);
            mLeftNavBar.setOnClickHomeListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // This is called when the app icon is selected in the left navigation bar
                    // Doing nothing.
                }
            });
        }
        return mLeftNavBar;
    }

    private void setupBar() {
        ActionBar bar = getLeftNavBar();
        bar.setTitle(R.string.title_text);
        setupCustomView();

        // Tabs.
        setupTabs(mHasCustomTabs);

        // Navigation list.
        SpinnerListAdapter adapter = new SpinnerListAdapter(this);
        bar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                postMessage("Item selected: " + itemPosition);
                return true;
            }
        });
    }

    private void setupCustomView() {
        getLeftNavBar().setCustomView(R.layout.custom_view);
        LayoutParams params = new LayoutParams(0);
        params.width = params.height = nextDimension(0);
        params.gravity = nextGravity(nextGravity(0, true), false);
        applyCustomParams(params);
    }

    private LayoutParams getCustomParams() {
        return (LayoutParams) getLeftNavBar().getCustomView().getLayoutParams();
    }

    private void applyCustomParams(LayoutParams params) {
        ActionBar bar = getLeftNavBar();
        bar.setCustomView(bar.getCustomView(), params);
    }

    private static int nextDimension(int dimension) {
		switch (dimension) {
		case 40:
			return 100;
		case 100:
			return LayoutParams.MATCH_PARENT;
		case LayoutParams.MATCH_PARENT:
		default:
			return 40;
		}
    }

    private static int nextGravity(int gravity, boolean horizontal) {
        int hGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        int vGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
		if (horizontal) {
			switch (hGravity) {
			case Gravity.LEFT:
				hGravity = Gravity.CENTER_HORIZONTAL;
				break;
			case Gravity.CENTER_HORIZONTAL:
				hGravity = Gravity.RIGHT;
				break;
			case Gravity.RIGHT:
			default:
				hGravity = Gravity.LEFT;
				break;
			}
		} else {
			switch (vGravity) {
			case Gravity.TOP:
				vGravity = Gravity.CENTER_VERTICAL;
				break;
			case Gravity.CENTER_VERTICAL:
				vGravity = Gravity.BOTTOM;
				break;
			case Gravity.BOTTOM:
			default:
				vGravity = Gravity.TOP;
				break;
			}
		}
        return hGravity | vGravity;
    }

    private void setupTabs(boolean custom) {
        ActionBar bar = getLeftNavBar();
        bar.removeAllTabs();
        if (custom) {
            addCustomTab(bar, R.string.tab_a);
            addCustomTab(bar, R.string.tab_b);
            addCustomTab(bar, R.string.tab_c);
            addCustomTab(bar, R.string.tab_d);
        } else {
            bar.addTab(bar.newTab().setText(R.string.tab_b).setIcon(R.drawable.tab_b)
                    .setTabListener(BLANK_LISTENER), false);
            ActionBar.Tab tab = bar.newTab().setText(R.string.tab_a).setIcon(R.drawable.tab_a)
                    .setTabListener(new ActionBar.TabListener() {

                @Override
                public void onTabUnselected(Tab tab, FragmentTransaction ft) {
                    postMessage("Unselected " + tab);
                }

                @Override
                public void onTabSelected(Tab tab, FragmentTransaction ft) {
                    postMessage("Selected " + tab);
                }

                @Override
                public void onTabReselected(Tab tab, FragmentTransaction ft) {
                    postMessage("Reselected " + tab);
                }
            });
            bar.addTab(tab, 0, true);
            bar.addTab(bar.newTab().setText(R.string.tab_c).setIcon(R.drawable.tab_c)
                    .setTabListener(BLANK_LISTENER), false);
            bar.addTab(bar.newTab().setText(R.string.tab_d).setIcon(R.drawable.tab_d)
                    .setTabListener(BLANK_LISTENER), false);
        }
    }

    private void addCustomTab(ActionBar bar, int title) {
        ActionBar.Tab tab = bar.newTab().setCustomView(R.layout.custom_tab)
                .setTabListener(BLANK_LISTENER);
        ((Button) tab.getCustomView()).setText(title);
        bar.addTab(tab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mHasOptionsMenu) {
            return false;
        }
        getMenuInflater().inflate(R.menu.bar, menu);
        if (mHasShowAlwaysItems) {
            menu.findItem(R.id.option_a).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.option_b).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            postMessage("Going home");
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        ActionBar bar = getLeftNavBar();
        View fakeContent = findViewById(R.id.simulated_background);
        View controls = findViewById(R.id.controls);
        switch (keyCode) {
            case KeyEvent.KEYCODE_ESCAPE:
                setBackground(0 /* controls */);
                return true;

            case KeyEvent.KEYCODE_1:
                setBackground(R.drawable.content_img_1);
                return true;

            case KeyEvent.KEYCODE_2:
                setBackground(R.drawable.content_img_2);
                return true;

            case KeyEvent.KEYCODE_N:
                bar.show();
                return true;

            case KeyEvent.KEYCODE_M:
                bar.hide();
                return true;

            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void setBackground(int image) {
        View fakeContent = findViewById(R.id.simulated_background);
        View controls = findViewById(R.id.controls);
        if (image != 0) {
            fakeContent.setBackgroundResource(image);
            fakeContent.setVisibility(View.VISIBLE);
            controls.setVisibility(View.GONE);
        } else {
            controls.setVisibility(View.VISIBLE);
            fakeContent.setVisibility(View.GONE);
        }
    }

    private void flipOption(int option) {
        ActionBar bar = getLeftNavBar();
        int options = bar.getDisplayOptions();
        boolean hadOption = (options & option) != 0;
        bar.setDisplayOptions(hadOption ? 0 : option, option);
    }

    private void setupButtons() {
        final ActionBar bar = getLeftNavBar();
        findViewById(R.id.button_home_show).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flipOption(ActionBar.DISPLAY_SHOW_HOME);
            }
        });
        findViewById(R.id.button_home_use_logo).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flipOption(ActionBar.DISPLAY_USE_LOGO);
            }
        });
        findViewById(R.id.button_home_as_up).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flipOption(ActionBar.DISPLAY_HOME_AS_UP);
            }
        });
        findViewById(R.id.button_home_logo_expand).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flipOption(LeftNavBar.DISPLAY_USE_LOGO_WHEN_EXPANDED);
            }
        });
        findViewById(R.id.button_title_show).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flipOption(ActionBar.DISPLAY_SHOW_TITLE);
            }
        });
        findViewById(R.id.button_subtitle).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                CharSequence subtitle = bar.getSubtitle();
                bar.setSubtitle(
                        TextUtils.isEmpty(subtitle) ? getString(R.string.subtitle_text) : null);
            }
        });
        findViewById(R.id.button_navigation_standard).setOnClickListener(
                new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }
        });
        findViewById(R.id.button_navigation_tabs).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            }
        });
        findViewById(R.id.button_navigation_list).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            }
        });
        findViewById(R.id.button_options_show).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mHasOptionsMenu = !mHasOptionsMenu;
                invalidateOptionsMenu();
            }
        });
        findViewById(R.id.button_options_always).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mHasShowAlwaysItems = !mHasShowAlwaysItems;
                invalidateOptionsMenu();
            }
        });
        findViewById(R.id.button_behavior_expanded).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flipOption(LeftNavBar.DISPLAY_ALWAYS_EXPANDED);
            }
        });
        findViewById(R.id.button_behavior_focus).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flipOption(LeftNavBar.DISPLAY_AUTO_EXPAND);
            }
        });
        findViewById(R.id.button_misc_background).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
            }
        });
        findViewById(R.id.button_misc_custom_view).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flipOption(ActionBar.DISPLAY_SHOW_CUSTOM);
            }
        });
        findViewById(R.id.button_custom_tabs).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mHasCustomTabs = !mHasCustomTabs;
                setupTabs(mHasCustomTabs);
            }
        });
        findViewById(R.id.button_show_progress).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flipOption(LeftNavBar.DISPLAY_SHOW_INDETERMINATE_PROGRESS);
            }
        });
        findViewById(R.id.button_post_messages).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMessagesEnabled = ((CheckBox) v).isChecked();
            }
        });
        findViewById(R.id.button_cv_size).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LayoutParams params = getCustomParams();
                int dimension = nextDimension(params.width);
                params.width = dimension;
                params.height = dimension;
                applyCustomParams(params);
            }
        });
        findViewById(R.id.button_cv_hgravity).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LayoutParams params = getCustomParams();
                params.gravity = nextGravity(params.gravity, true);
                applyCustomParams(params);
            }
        });
        findViewById(R.id.button_cv_vgravity).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LayoutParams params = getCustomParams();
                params.gravity = nextGravity(params.gravity, false);
                applyCustomParams(params);
            }
        });
    }

    private static final class SpinnerListAdapter extends BaseAdapter {

        private static final int[] NAMES = {
            R.string.list_a, R.string.list_b, R.string.list_c };
        private static final int[] PICTURES = {
            R.drawable.list_a, R.drawable.list_b, R.drawable.list_c };

        private final Context mContext;

        SpinnerListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return NAMES.length;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createView(position, convertView, parent, false);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createView(position, convertView, parent, true);
        }

        private View createView(int position, View convertView, ViewGroup parent, boolean inList) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.spinner_item, parent, false);
            }
            ImageView picture = (ImageView) convertView.findViewById(R.id.picture);
            TextView name = (TextView) convertView.findViewById(R.id.name);
            picture.setImageResource(PICTURES[position]);
            if (inList) {
                picture.setBackgroundResource(R.drawable.spinner_list_item_background);
                name.setVisibility(View.VISIBLE);
                name.setText(NAMES[position]);
            } else {
                picture.setBackgroundResource(R.drawable.spinner_item_background);
                name.setVisibility(View.GONE);
            }
            return convertView;
        }
    }
}
