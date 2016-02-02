package com.broadlink.control;

import android.content.Intent;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.broadlink.control.api.BroadlinkAPI;
import com.broadlink.control.api.DeviceInfo;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mHandler = new Handler();
        initBroadlink();

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recognize();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void initBroadlink() {
        //Initialize Broadlink SDK
        final BroadlinkAPI api = BroadlinkAPI.getInstance(this);
        api.broadlinkInitNetwork();

        JsonObject versionJson = api.broadlinkVersion();
        if (versionJson.has("version") && versionJson.get("version") != null)
            Log.e(this.getClass().getSimpleName(), "BROADLINK VERSION: " + versionJson.get("version"));
        if (versionJson.has("update") && versionJson.get("update") != null)
            Log.e(this.getClass().getSimpleName(), "BROADLINK CHANGE LOG: " + versionJson.get("update"));

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ArrayList<DeviceInfo> deviceInfoArrayList = api.getProbeList();
                if (deviceInfoArrayList != null) {
                    for (DeviceInfo info : deviceInfoArrayList) {
                        JsonObject result = api.addDevice(info);
                        Log.e(this.getClass().getSimpleName(), result.toString());
                    }
                }
            }
        }, 500);
    }

    private void recognize() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if (requestCode == 1) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String query = results.isEmpty() ? null : results.get(0);
            BroadlinkUtil.processFunction(this, query);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new FunctionsFragment();
                case 1: return new ButtonsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Функции";
                case 1:
                    return "Кнопки";
            }
            return null;
        }
    }
}
