package com.serp1983.nokiacomposer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdView;
import com.serp1983.nokiacomposer.util.ActivityHelper;

public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new PagerAdapter(fragmentManager));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
                startActivity(new Intent(MainActivity.this, DetailsActivity.class));
            }
        });

        AdView adView = (AdView) this.findViewById(R.id.adView);
        adView.loadAd(ActivityHelper.getAdBuilder().build());
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
        getMenuInflater().inflate( R.menu.menu_main, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem search = menu.findItem(R.id.action_search);
        search.setVisible(viewPager.getCurrentItem() == 0);
        return true;
    }


    private class PagerAdapter extends FragmentPagerAdapter {
        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return RingtonesFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return  2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0
                    ? App.getAppContext().getResources().getString(R.string.ringtones_label)
                    : App.getAppContext().getResources().getString(R.string.my_ringtones_label) ;
        }
    }

}
