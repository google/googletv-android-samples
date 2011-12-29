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
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a full-screen photo and a carousel to browse through
 * all photos. It also enables the user to view a slideshow of all photos.
 */
public class ViewImage extends Activity implements Animation.AnimationListener {

    /**
     * A toast that shows status messages. We have to keep it in order to be
     * able to substitute existing messages instead of queuing them.
     */
    private Toast statusToast;

    private static String AUTHOR = "Photo by ";

    private static int count;

    private ImageView mImage1;

    private ImageView mImage2;

    private static final String TAG = null;

    private boolean mPlaying;

    private int visibleItemIndex;

    private static Context mContext;

    private ImageManager mImageManager;

    private TextView mTitle1;

    private TextView mTitle2;

    private TextView mFooter1;

    private TextView mFooter2;

    private static CarouselFragment fragment;

    private String title1;

    private String title2;

    private String footer1;

    private String footer2;

    private ProgressBar progressBar;

    private AlphaAnimation animation;

    private AlphaAnimation animation2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image);
        progressBar = (ProgressBar) findViewById(R.id.a_progressbar);
        final Intent i = getIntent();
        count = i.getIntExtra(ImageManager.PANORAMIO_ITEM_EXTRA, 0);

        mImage1 = (ImageView) findViewById(R.id.image_1);
        mImage2 = (ImageView) findViewById(R.id.image_2);

        mTitle1 = (TextView) findViewById(R.id.title_1);
        mTitle2 = (TextView) findViewById(R.id.title_2);
        mFooter1 = (TextView) findViewById(R.id.footer_1);
        mFooter2 = (TextView) findViewById(R.id.footer_2);
        mImageManager = ImageManager.getInstance(ViewImage.this);

        statusToast = Toast.makeText(ViewImage.this, "", Toast.LENGTH_SHORT);

        showStatusToast(R.string.slideshow_play_hint);

        if (mImageManager.size() > 0) {

        }
        final PanoramioItem item = mImageManager.get(count++);
        mTitle1.setText(item.getLocation() + " : " + item.getTitle());
        mFooter1.setText(AUTHOR + item.getOwner());
        setFooterClickListener(mFooter1, item);
        setImage(mImage1, item);
        if (mImageManager.size() > 1) {
            final PanoramioItem item2 = mImageManager.get(count++);
            title2 = item2.getLocation() + " : " + item2.getTitle();
            footer2 = AUTHOR + item2.getOwner();
            setImage(mImage2, item2);
            mImage2.setVisibility(View.INVISIBLE);
        }
    }

    private void setImage(ImageView view, PanoramioItem item) {
        class IntializeViewImageTask extends AsyncTask<Object, Void, Bitmap> {

            private ImageView view;

           @Override
            protected Bitmap doInBackground(Object... params) {
                view = (ImageView) params[0];
                final PanoramioItem item = (PanoramioItem) params[1];
                view.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        String url = "http://www.panoramio.com/photo/" + item.getId();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
                return item.getLargeBitmap();
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                
            }

            @Override
            protected void onPostExecute(Bitmap result) {
            	super.onPostExecute(result);
                view.setImageBitmap(result);
                view.requestFocus();
                progressBar.setVisibility(View.INVISIBLE);
            }
        }

        new IntializeViewImageTask().execute(view, item);
    }

    private void showImage() {
    	visibleItemIndex = count;
    	progressBar.setVisibility(View.VISIBLE);
        final PanoramioItem item = mImageManager.get(count);
        mTitle1.setText(item.getLocation() + " : " + item.getTitle());
        mTitle2.setText("");
        mFooter2.setText("");
        mFooter1.setText(AUTHOR + item.getOwner());
        setFooterClickListener(mFooter1, item);
        setImage(mImage1, item);

        mImage1.setVisibility(View.VISIBLE);
        mImage2.setVisibility(View.INVISIBLE);

    }

    private void setFooterClickListener(TextView view, final PanoramioItem item) {
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String url = item.getOwnerUrl();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

    /**
     * A helper method that shows a quick toast with a status message,
     * overwriting the previous message if it's not yet hidden.
     * 
     * @param messageId ID of the message to show.
     */
    private void showStatusToast(int messageId) {
        statusToast.setText(messageId);
        statusToast.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_MEDIA_REWIND:
        case KeyEvent.KEYCODE_DPAD_LEFT: {
        	stopSlideShow();
            fragment = CarouselFragment.newInstance(visibleItemIndex - 1);

            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.carousel, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();

            break;
        }
        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
        case KeyEvent.KEYCODE_DPAD_RIGHT: {
           
            fragment = CarouselFragment.newInstance(visibleItemIndex + 1);

            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.carousel, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();

            break;
        }
        case KeyEvent.KEYCODE_DPAD_DOWN: {
        	stopSlideShow();
            
            showStatusToast(R.string.hide_thumbnails_hint);
            fragment = CarouselFragment.newInstance(visibleItemIndex);

            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.carousel, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();

            break;
        }
        case KeyEvent.KEYCODE_DPAD_UP: {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.hide(fragment);
            ft.commit();
            break;
        }
        case KeyEvent.KEYCODE_MEDIA_PLAY: {
            if (!mPlaying) {
                startSlideShow();
            }
            // hide the carousel.
            if (fragment != null) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.hide(fragment);
                ft.commit();
            }
            mPlaying = true;
            break;
        }
        case KeyEvent.KEYCODE_MEDIA_PAUSE: {
            stopSlideShow();
            showStatusToast(R.string.slideshow_paused);
            break;
        }
        }
        return super.onKeyDown(keyCode, event);
    }

	private void stopSlideShow() {
		mPlaying = false;
		if (animation != null && animation2 != null) {
			animation.cancel();
			animation2.cancel();
		}

	}
    private void startSlideShow() {
        mPlaying = true;
        showStatusToast(R.string.slideshow_started);
        doit();
    }

    public static class CarouselFragment extends Fragment {
        static int mCurCheckPosition = 0;
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }

        /**
         * Create a new instance of DetailsFragment, initialized to show the
         * text at 'index'.
         */
        public static CarouselFragment newInstance(int index) {
            CarouselFragment f = new CarouselFragment();
            mCurCheckPosition = index;
            return f;
        }

        @Override
        public View onCreateView(
                LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            Gallery gallery = (Gallery) inflater.inflate(R.layout.image_gallery, null);

            // In dual-pane mode, the list view highlights the selected item.
            gallery.setAdapter(new GalleryAdapter(mContext));
            gallery.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> l, View v, int position, long id) {
                    count = position;
                    ((ViewImage) mContext).showImage();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.hide(fragment);
                    ft.commit();
                }
            });

            gallery.requestFocus();
            gallery.setSelection(mCurCheckPosition);

            return gallery;
        }
    }

    private void doit() {
        if ((count % 2) == 0) {
            mImage1.setVisibility(View.VISIBLE);
            animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setStartOffset(3000);
            animation.setDuration(3000);
            animation.setFillAfter(false);
            animation.setInterpolator(new LinearInterpolator());
            if (mPlaying) {
                mImage1.startAnimation(animation);
            }
            animation2 = new AlphaAnimation(0.0f, 1.0f);
            animation2.setStartOffset(3000);
            animation2.setDuration(3000);
            animation2.setAnimationListener(this);
            animation2.setInterpolator(new LinearInterpolator());
            if (mPlaying) {
                mImage2.startAnimation(animation2);
            }
        } else {
            mImage2.setVisibility(View.VISIBLE);
            animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setStartOffset(3000);
            animation.setDuration(3000);
            animation.setFillAfter(false);
            animation.setInterpolator(new LinearInterpolator());
            if (mPlaying) {
                mImage2.startAnimation(animation);
            }
            animation2 = new AlphaAnimation(0.0f, 1.0f);
            animation2.setStartOffset(3000);
            animation2.setDuration(3000);
            animation2.setAnimationListener(this);
            animation2.setInterpolator(new LinearInterpolator());
            if (mPlaying) {
                mImage1.startAnimation(animation2);
            }
        }
    }

    public void onAnimationEnd(Animation animation) {
    	visibleItemIndex = count;
        final PanoramioItem item = mImageManager.get(count++ % (mImageManager.size()));
        if ((count % 2) == 0) {
            title2 = item.getLocation() + " : " + item.getTitle();
            footer2 = AUTHOR + item.getOwner();
            setFooterClickListener(mFooter2, item);
            setImage(mImage2, item);
            mImage2.setVisibility(View.INVISIBLE);
        } else {
            title1 = item.getLocation() + " : " + item.getTitle();
            footer1 = AUTHOR + item.getOwner();
            setFooterClickListener(mFooter1, item);
            setImage(mImage1, item);
            mImage1.setVisibility(View.INVISIBLE);
        }
        this.doit();
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {
        if (mImage1.getVisibility() < 2) {
            mTitle2.setText("");
            mTitle1.setText(title1);
            mFooter2.setText("");
            mFooter1.setText(footer1);
        } else {
            mTitle1.setText("");
            mTitle2.setText(title2);
            mFooter1.setText("");
            mFooter2.setText(footer2);
        }
    }
}
