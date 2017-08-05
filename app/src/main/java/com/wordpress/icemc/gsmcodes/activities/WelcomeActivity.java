package com.wordpress.icemc.gsmcodes.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wordpress.icemc.gsmcodes.R;
import com.wordpress.icemc.gsmcodes.listeners.GetCodesListener;
import com.wordpress.icemc.gsmcodes.utilities.AppStart;
import com.wordpress.icemc.gsmcodes.utilities.AppStartStatus;
import com.wordpress.icemc.gsmcodes.utilities.ApplicationConstants;
import com.wordpress.icemc.gsmcodes.utilities.JsonUtils;
import com.wordpress.icemc.gsmcodes.utilities.LocaleHelper;

import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity implements GetCodesListener{

    private static final String TAG = WelcomeActivity.class.getSimpleName();
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    private AppStartStatus status;
    private ProgressDialog pDialog;
    private SharedPreferences sharedPreferences;
    private String selectedAppLanguage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking for first time launch - before calling setContentView()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        switch (status = AppStart.checkAppStartStatus(this, sharedPreferences)) {
            case NORMAL:
                String operator = sharedPreferences.getString(ApplicationConstants.LAST_OPERATOR_USED, "");
                if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
                    Log.d(TAG, "Closing application");
                    finish();
                } else if(!operator.equals("")) {
                    //Launch HomeActivity
                    Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    loadDataIntoDatabase();
                }
                break;
            case FIRST_TIME_VERSION:
                //Add any version specific stuff here
                break;
            case FIRST_TIME:
                break;
            default:
                break;
        }

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_welcome);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnSkip = (Button) findViewById(R.id.btn_skip);
        btnNext = (Button) findViewById(R.id.btn_next);


        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.welcome_slide1,
                R.layout.welcome_slide2,
                R.layout.welcome_slide3,
                R.layout.welcome_slide4};

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentLanguage = new LocaleHelper().getLocaleString();
                int index = -1;

                for(int i = 0; i < ApplicationConstants.TRANSLATIONS_AVAILABLE.length; i++) {
                    if((currentLanguage).equals(
                            new Locale(ApplicationConstants.TRANSLATIONS_AVAILABLE[i]).getLanguage())) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    //Default to english
                    index = 0;
                }

                final int currentIndex = index;
                new AlertDialog.Builder(WelcomeActivity.this, R.style.MaterialDialog)
                        .setTitle(R.string.select_language)
                        .setSingleChoiceItems(ApplicationConstants.LANGUAGES, currentIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sharedPreferences.edit().putString(ApplicationConstants.APP_LANGUAGE, ApplicationConstants.TRANSLATIONS_AVAILABLE[which]).apply();
                                selectedAppLanguage = ApplicationConstants.TRANSLATIONS_AVAILABLE[which];
                                loadDataIntoDatabase();
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sharedPreferences.edit().putString(ApplicationConstants.APP_LANGUAGE, ApplicationConstants.TRANSLATIONS_AVAILABLE[currentIndex]).apply();
                                selectedAppLanguage = ApplicationConstants.TRANSLATIONS_AVAILABLE[currentIndex];
                                loadDataIntoDatabase();
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    String currentLanguage = new LocaleHelper().getLocaleString();
                    int index = -1;

                    for(int i = 0; i < ApplicationConstants.TRANSLATIONS_AVAILABLE.length; i++) {
                        if((currentLanguage).equals(
                                new Locale(ApplicationConstants.TRANSLATIONS_AVAILABLE[i]).getLanguage())) {
                            index = i;
                            break;
                        }
                    }
                    if (index == -1) {
                        //Default to english
                        index = 0;
                    }

                    final int currentIndex = index;
                    new AlertDialog.Builder(WelcomeActivity.this, R.style.MaterialDialog)
                            .setTitle(R.string.select_language)
                            .setSingleChoiceItems(ApplicationConstants.LANGUAGES, currentIndex, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sharedPreferences.edit().putString(ApplicationConstants.APP_LANGUAGE, ApplicationConstants.TRANSLATIONS_AVAILABLE[which]).apply();
                                    selectedAppLanguage = ApplicationConstants.TRANSLATIONS_AVAILABLE[which];
                                    loadDataIntoDatabase();
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sharedPreferences.edit().putString(ApplicationConstants.APP_LANGUAGE, ApplicationConstants.TRANSLATIONS_AVAILABLE[currentIndex]).apply();
                                    selectedAppLanguage = ApplicationConstants.TRANSLATIONS_AVAILABLE[currentIndex];
                                    loadDataIntoDatabase();
                                    dialog.dismiss();
                        }
                    }).show();

                }
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void loadDataIntoDatabase() {
        if(status == AppStartStatus.FIRST_TIME) {
            new LoadDataIntoDatabase(this).execute();
        } else {
            if(!sharedPreferences.getBoolean(ApplicationConstants.IS_DATABASE_DATA_CORRECT, false)) {
                new LoadDataIntoDatabase(this).execute();
            }
        }
    }

    //	viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.start));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onCodeLoaderFinished() {
        // Dismiss the progress dialog
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
        sharedPreferences.edit().putBoolean(ApplicationConstants.IS_DATABASE_DATA_CORRECT, true).apply();
        startActivity(new Intent(WelcomeActivity.this, OperatorsActivity.class));
    }

    @Override
    public void onCodeLoaderStart() {
        // Showing progress dialog
        pDialog = new ProgressDialog(WelcomeActivity.this);
        pDialog.setMessage(getResources().getString(R.string.saving));
        pDialog.setCancelable(false);
        pDialog.show();
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    /**
     * Async task to store json data into database if app is running for the first time
     */
    private class LoadDataIntoDatabase extends AsyncTask<Void, Void, Void> {
        private GetCodesListener listener;
        private String path;

        public LoadDataIntoDatabase(GetCodesListener listener) {
            super();
            this.listener = listener;
            if (selectedAppLanguage.equals(ApplicationConstants.TRANSLATIONS_AVAILABLE[1])) {
                path = "fr/";
            } else {
                path = "en/";
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listener.onCodeLoaderStart();
        }

        @Override
        protected Void doInBackground(Void... params) {
            JsonUtils.saveJSONOperatorsToDatabase(WelcomeActivity.this, path + "operators.json");
            JsonUtils.saveJSONCodesToDatabase(WelcomeActivity.this,  path + "codes.json");
            JsonUtils.saveJSONTagsToDatabase(WelcomeActivity.this, path + "tags.json");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            listener.onCodeLoaderFinished();
        }
    }
}
