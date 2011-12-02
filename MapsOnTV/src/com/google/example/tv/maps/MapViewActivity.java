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

package com.google.example.tv.maps;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import java.io.IOException;
import java.util.List;

/**
 * Activity to show the map and all the controls on screen and handles all the 
 * user events.
 * 
 */
public class MapViewActivity extends MapActivity {

    /**
     * Left scrolling speed of map while panning through controls.
     */
    private static final int PAN_LEFT_SCROLL_SPEED = -50;
    /**
     * Right scrolling speed of map while panning through controls.
     */
    private static final int PAN_RIGHT_SCROLL_SPEED = 50;
    /**
     * Downward scrolling speed of map while panning through controls.
     */
    private static final int PAN_DOWN_SCROLL_SPEED = 50;
    /**
     * Upward scrolling speed of map while panning through controls.
     */
    private static final int PAN_UP_SCROLL_SPEED = -50;
    /**
     * Index for fetching the first address from list.
     */
    private static final int ZERO_INDEX = 0;
    /**
     * Initial zoom level.
     */
    private static final int MAP_ZOOM_LEVEL = 7;
    /**
     * Zoom level when maps shows a particular location.
     */
    private static final int LOCATION_ZOOM_LEVEL = 14;
    /**
     * Key for search input stored in preference.
     */
    private static final String SEARCH_INPUT_KEY = "search input";
    /**
     * Key for help dialog.
     */
    private static final String HELP_DIALOG_KEY = "show help";
    /**
     * Constant for preference file name.
     */
    private static final String PREF_FILE_NAME = "maps_tv_pref_file";
    /**
     * A view which display a Map.
     */
    private MapView mMapView;
    /**
     * Control panel layout for all the map controls.
     */
    private RelativeLayout mControlPanel;
    /**
     * Search Bar Layout.
     */
    private LinearLayout mSearchbar;
    /**
     * Map controller to manager panning and zoom.
     */
    private MapController mMapController;
    /**
     * EditText for search location text.
     */
    private EditText mSearchEditText;
    /**
     * Imageview to toggle map, satellite and traffic mode.
     */
    private ImageView mMapModeView, mSatelliteModeView, mTrafficModeView;
    /**
     * Shared preference to store the last search address. 
     */
    private SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mMapView = (MapView) findViewById(R.id.mapview);
        mSearchEditText = (EditText) findViewById(R.id.searchtext);
        mMapController = mMapView.getController();
        mControlPanel = (RelativeLayout) findViewById(R.id.control_panel);
        mMapModeView = (ImageView) findViewById(R.id.map_mode);
        mSatelliteModeView = (ImageView) findViewById(R.id.sat_mode);
        mTrafficModeView = (ImageView) findViewById(R.id.traffic_mode);
        mSearchbar = (LinearLayout) findViewById(R.id.searchbar);
        //Pre-load the tiles so that maps can get loaded faster.
        mMapView.preLoad();
        //Turn on the map mode and set the zoom level.
        mMapController.setZoom(MAP_ZOOM_LEVEL);
        mMapView.setSatellite(false);
        mMapView.setBuiltInZoomControls(true);
        mSearchEditText.setFocusable(true);
        setSearchEditorActionListner();
    }

    /**
     * Set the listener for the search bar to perform search when user presses
     * enter/ok key.
     */
    private void setSearchEditorActionListner() {
        mSearchEditText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //When user puts in the address and press enter on search bar
                //then search that location and zoom in.
                if (actionId == EditorInfo.IME_NULL) {
                    goToLocation(mSearchEditText.getText().toString());
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPreferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        Boolean shouldShowHelp = mPreferences.getBoolean(HELP_DIALOG_KEY, true);
        //Shows the help dialog first time user installs the application.
        if (shouldShowHelp) {
            showHelpDialog();
            // Store that help screen is already shown to user in
            // SharedPreferences.
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean(HELP_DIALOG_KEY, false);
            editor.commit();
        }
        //Check if there is previous location stored in preferences and
        //pan/zoom to that location on re-launch of that activity.
        String lastAddress = mPreferences.getString(SEARCH_INPUT_KEY, "");
        if (!TextUtils.isEmpty(lastAddress)) {
            mSearchEditText.setText(lastAddress);
            goToLocation(lastAddress);
        }
    }

    /**
     * Shows the help dialog first time user installs the application.
     * 
     */
    private void showHelpDialog() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getText(R.string.dialog_help_text));
        builder.setTitle(getResources().getText(R.string.dialog_title));
        builder.setIcon(android.R.drawable.ic_menu_help);
        builder.setPositiveButton(getResources().getText(R.string.dialog_button_ok),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing.
                    }
                });
        AlertDialog helpDialog = builder.create();
        helpDialog.show();
    }

    @Override
    protected void onStop() {
        // Store the current location in preferences for re-launch before
        // exiting the application.
        String prevAddress = mSearchEditText.getText().toString();
        mPreferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SEARCH_INPUT_KEY, prevAddress);
        editor.commit();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu items from the menu layout.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map_controls, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handles the menu item selection.
        switch (item.getItemId()) {
        case R.id.menu_satellite:
            mMapView.setSatellite(true);
            mMapView.setTraffic(false);
            return true;
        case R.id.menu_map:
            mMapView.setSatellite(false);
            return true;
        case R.id.menu_traffic:
            if (mMapView.isTraffic()) {
                mMapView.setTraffic(false);
            } else {
                mMapView.setTraffic(true);
                mMapView.setSatellite(false);
            }
            return true;
        case R.id.menu_help:
            showHelpDialog();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * When satellite mode is clicked by the user.
     * 
     * @param view The satellite view.
     */
    public void onSatModeClicked(View view) {
        mMapView.setSatellite(true);
        mMapView.setTraffic(false);
        view.setEnabled(false);
        mMapModeView.setEnabled(true);
        mTrafficModeView.setEnabled(true);
    }

    /**
     * When map mode is clicked by the user.
     * 
     * @param view The map view.
     */
    public void onMapModeClicked(View view) {
        mMapView.setSatellite(false);
        view.setEnabled(false);
        mSatelliteModeView.setEnabled(true);
        mTrafficModeView.setEnabled(true);
    }

    /**
     * When traffic mode is clicked by the user.
     * 
     * @param view The traffic view.
     */
    public void onTrafficModeClicked(View view) {
        if (mMapView.isTraffic()) {
            mMapView.setTraffic(false);
            mMapModeView.setEnabled(false);
            mSatelliteModeView.setEnabled(true);
        } else {
            // If Traffic mode is selected, show the traffic in Map Mode only.
            // If Map is already on satellite mode than automatically change it
            // to Map Mode and enable the traffic.
            mMapView.setTraffic(true);
            mMapView.setSatellite(false);
            mMapModeView.setEnabled(false);
            mTrafficModeView.setEnabled(false);
            mSatelliteModeView.setEnabled(true);
        }
    }

    /**
     * On up arrow pan the map upwards.
     * 
     * @param view The up arrow view.
     */
    public void onUpArrowClicked(View view) {
        mMapController.scrollBy(ZERO_INDEX, PAN_UP_SCROLL_SPEED);
    }

    /**
     * On down arrow pan the map downwards
     * 
     * @param view The down arrow view.
     */
    public void onDownArrowClicked(View view) {
        mMapController.scrollBy(ZERO_INDEX, PAN_DOWN_SCROLL_SPEED);
    }

    /**
     * On left arrow pan the map to the left.
     * 
     * @param view The left arrow view.
     */
    public void onLeftArrowClicked(View view) {
        mMapController.scrollBy(PAN_LEFT_SCROLL_SPEED, ZERO_INDEX);
    }

    /**
     * On right arrow pan the map to the right.
     * 
     * @param view The right arrow view.
     */
    public void onRightArrowClicked(View view) {
        mMapController.scrollBy(PAN_RIGHT_SCROLL_SPEED, ZERO_INDEX);
    }

    /**
     * When clicked display the map in full screen.
     * 
     * @param view The full screen view.
     */
    public void onFullScreenClicked(View view) {
        //Checks if search bar is visible or not and toggle the search bar
        // mode accordingly.
        mSearchbar.setVisibility(View.GONE);
        mControlPanel.setVisibility(View.GONE);
    }

    /**
     * Event to handle when user press Go button to search a location(geocode).
     * 
     * @param view The search icon view.
     */
    public void onGoSearchButtonClicked(View view) {
        goToLocation(mSearchEditText.getText().toString());
    }

    /**
     * Search the geo-location and zoom the map to that point.
     * 
     * @param addressEntered The address entered by the user to search.
     */
    private void goToLocation(final String addressEntered) {
        if (!TextUtils.isEmpty(addressEntered.trim())) {
            Geocoder geoCode = new Geocoder(this);
            try {
                //Get top 5 matched address for this location.
                final List<Address> foundAdresses = geoCode.getFromLocationName(addressEntered, 5);
                if (foundAdresses.isEmpty()) {
                    //If no address found, show error through toast.
                    Toast.makeText(this, getResources().getText(R.string.error_message),
                            Toast.LENGTH_LONG).show();
                } else { // Else display address on map and 
                         // store results as longitude and latitude.
                    Address address = foundAdresses.get(ZERO_INDEX);
                    Double latitude = address.getLatitude() * 1E6;
                    Double longitude = address.getLongitude() * 1E6;
                    GeoPoint point = new GeoPoint(latitude.intValue(), longitude.intValue());
                    List<Overlay> mapOverlays = mMapView.getOverlays();
                    Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
                    MapsMarkerOverlay itemizedoverlay = new MapsMarkerOverlay(drawable,
                            getApplicationContext());
                    OverlayItem overlayitem = new OverlayItem(point, null, null);
                    itemizedoverlay.addOverlay(overlayitem);
                    if (mapOverlays.size() > 0) {
                        mapOverlays.remove(0);
                    }
                    mapOverlays.add(itemizedoverlay);
                    mMapView.preLoad();
                    //Show the location in the center of the map. 
                    mMapController.setCenter(point);
                    mMapController.animateTo(point);
                    mMapController.setZoom(LOCATION_ZOOM_LEVEL);
                    mMapView.invalidate();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
          case KeyEvent.KEYCODE_ZOOM_IN:
              mMapView.getController().zoomIn();
              break;
          case KeyEvent.KEYCODE_ZOOM_OUT:
              mMapView.getController().zoomOut();
              break;
          case KeyEvent.KEYCODE_BACK: {
              //Checks if search bar is visible or not and toggle the search bar
              //mode accordingly.
              if (mSearchbar.getVisibility() != View.VISIBLE) {
                  mSearchbar.setVisibility(View.VISIBLE);
                  mControlPanel.setVisibility(View.VISIBLE);
                  mSearchEditText.requestFocus();
                  return true;
              }
              break;
          }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //Stop panning once user releases the DPad control.
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            mMapController.stopPanning();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
