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
import android.app.ListFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * This Activity displays list of cool places grouped by continents. Users can
 * choose any place from the list to view photos for that place.
 */
public class CoolPlacesActivity extends Activity {
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.cool_places);
        PanoramioLeftNavService.getLeftNavBar(this);
    }

    public void onClick(View view) {
        onSearchRequested();
    }

    static ListView continents;

    public static class ContinentsFragment extends ListFragment {

        int mCurCheckPosition = 0;

        int mShownCheckPosition = 0;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.large_list_item,
                    getResources().getStringArray(R.array.continents)));

            if (savedInstanceState != null) {
                // Restore last state for checked position.
                mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
            }
            continents = getListView();
            // In dual-pane mode, the list view highlights the selected item.
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            getListView().setDividerHeight(0);
            getListView().setSelector(R.drawable.continents_background);

            getListView().setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> l, View v, int position, long id) {
                    showDetails(position);
                    mCurCheckPosition = position;
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("curChoice", mCurCheckPosition);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            showDetails(position);
            mCurCheckPosition = position;
        }

        /**
         * Helper function to show the details of a selected item, by displaying
         * a fragment in-place.
         */
        void showDetails(int index) {
            Log.v("TAG", "showDetails" + index);
            mCurCheckPosition = index;

            // We can display everything in-place with fragments, so update
            // the list to highlight the selected item and show the data.
            getListView().setItemChecked(index, true);

            CoolPlacesNamesFragment df = CoolPlacesNamesFragment.newInstance(index);

            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.details, df);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            mShownCheckPosition = index;

        }
    }

    /** This is the secondary fragment displaying the names of the places **/
    public static class CoolPlacesNamesFragment extends ListFragment {
        int mCurCheckPosition = 0;

        int mShownCheckPosition = -1;

        /**
         * Create a new instance of DetailsFragment, initialized to show the
         * text at 'index'.
         */
        public static CoolPlacesNamesFragment newInstance(int index) {
            CoolPlacesNamesFragment f = new CoolPlacesNamesFragment();
            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putInt("index", index);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            int arrayId = -1;

            switch ((Integer) getArguments().get("index")) {
            case 0:
                arrayId = R.array.asia_places;
                break;
            case 1:
                arrayId = R.array.americas_places;
                break;
            case 2:
                arrayId = R.array.africa_places;
                break;
            case 3:
                arrayId = R.array.europe_places;
                break;
            case 4:
                arrayId = R.array.australia_places;
                break;
            case 5:
                arrayId = R.array.antartica_places;
                break;

            }

            setListAdapter(new ArrayAdapter<String>(
                    getActivity(), R.layout.list_item, getResources().getStringArray(arrayId)));

            if (savedInstanceState != null) {
                // Restore last state for checked position.
                mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
                mShownCheckPosition = savedInstanceState.getInt("shownChoice", -1);
            }

            getListView().setDividerHeight(0);
            getListView().setSelector(R.drawable.image_background);
            continents.setSelection((Integer) getArguments().get("index"));

        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Intent intent = new Intent();
            intent.setClass(mContext, ImageGrid.class);
            int arrayId = -1;

            switch ((Integer) getArguments().get("index")) {
            case 0:
                arrayId = R.array.asia_places;
                break;
            case 1:
                arrayId = R.array.americas_places;
                break;
            case 2:
                arrayId = R.array.africa_places;
                break;
            case 3:
                arrayId = R.array.europe_places;
                break;
            case 4:
                arrayId = R.array.australia_places;
                break;
            case 5:
                arrayId = R.array.antartica_places;
                break;

            }
            final String query = getResources().getStringArray(arrayId)[position].replace("'", "");
            intent.putExtra("query", query);
            mContext.startActivity(intent);
        }

    }

    private void handleIntent(Intent intent) {
        String query = "";
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            Intent intent2 = new Intent();
            intent2.setClass(mContext, ImageGrid.class);
            intent2.putExtra("query", query);
            mContext.startActivity(intent2);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
}
