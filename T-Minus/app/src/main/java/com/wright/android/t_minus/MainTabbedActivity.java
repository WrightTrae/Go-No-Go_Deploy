package com.wright.android.t_minus;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wright.android.t_minus.main_tabs.launchpad.LaunchPadFragment;
import com.wright.android.t_minus.main_tabs.manifest.ManifestFragment;
import com.wright.android.t_minus.main_tabs.photos.PhotosFragment;
import com.wright.android.t_minus.objects.LaunchPad;
import com.wright.android.t_minus.objects.Manifest;
import com.wright.android.t_minus.objects.PadLocation;
import com.wright.android.t_minus.settings.PreferencesActivity;
import com.wright.android.t_minus.network_connection.GetManifestsFromAPI;
import com.wright.android.t_minus.network_connection.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class MainTabbedActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener{
    private LaunchPadFragment launchPadFragment;
    private ManifestFragment manifestFragment;
    private PhotosFragment photosFragment;
    private ViewPager mMainViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tabbed);
        mMainViewPager = findViewById(R.id.container);

        //MobileAds.initialize(this, "ca-app-pub-9162158259171878~9059604120");

//        AdView mAdView = findViewById(R.id.adView);
//
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                // Code to be executed when an ad finishes loading.
//                mMainViewPager.setPadding(0,0,0, mAdView.getHeight());
//            }
//
//            @Override
//            public void onAdFailedToLoad(int errorCode) {
//                // Code to be executed when an ad request fails.
//                mMainViewPager.setPadding(0,0,0, 0);
//            }
//
//            @Override
//            public void onAdOpened() {
//                // Code to be executed when an ad opens an overlay that
//                // covers the screen.
//            }
//
//            @Override
//            public void onAdLeftApplication() {
//                // Code to be executed when the user has left the app.
//            }
//
//            @Override
//            public void onAdClosed() {
//                // Code to be executed when when the user is about to return
//                // to the app after tapping on an ad.
//            }
//        });
//        mAdView.loadAd(adRequest);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mMainViewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        mMainViewPager.setOffscreenPageLimit(2);
        TabLayout.Tab originalTab = tabLayout.getTabAt(0);
        if(originalTab != null && originalTab.getIcon() != null){
            originalTab.getIcon().setColorFilter(getColor(R.color.selectedTabColor), PorterDuff.Mode.SRC_IN);
        }
        for (int i = 1;i < tabLayout.getTabCount();i++){
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if(tab != null && tab.getIcon() != null) {
                tab.getIcon().setColorFilter(getColor(R.color.unselectedTabColor), PorterDuff.Mode.SRC_IN);
            }
        }
        mMainViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(this);
        manifestFragment = ManifestFragment.newInstance();
        launchPadFragment = LaunchPadFragment.newInstance();
        photosFragment = PhotosFragment.newInstance();

    }

//    public int dpToPx(int dp) {
//        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
//        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {/////////////////Setup UI\\\\\\\\\\\\\\\\\\\\
        getMenuInflater().inflate(R.menu.menu_main_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, PreferencesActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mMainViewPager.setCurrentItem(tab.getPosition());
        int tabIconColor = ContextCompat.getColor(this, R.color.selectedTabColor);
        if(tab.getIcon()!=null) {
            tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
        }
        switch (tab.getPosition()){
            case 1:
                launchPadFragment.onTabClick();
                break;
            case 2:
                photosFragment.onTabClick();
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        int tabIconColor = ContextCompat.getColor(this, R.color.unselectedTabColor);
        if(tab.getIcon()!=null)
            tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return manifestFragment;
                case 1:
                    return launchPadFragment;
                case 2:
                    return photosFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
