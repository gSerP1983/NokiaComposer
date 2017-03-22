package com.serp1983.nokiacomposer;

import android.content.Context;
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
import com.serp1983.nokiacomposer.util.RewardedVideoAdService;

public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;
    RewardedVideoAdService adService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.main_view_pager);
        viewPager.setAdapter(new PagerAdapter(this, getSupportFragmentManager()));

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(1);
                startActivity(new Intent(MainActivity.this, DetailsActivity.class));
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(MainActivity.this, ComposerActivity.class));
                return false;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                fab.show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        adService = new RewardedVideoAdService(this);

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
        if (search != null && viewPager != null)
            search.setVisible(viewPager.getCurrentItem() == 0);

        MenuItem helpProject = menu.findItem(R.id.action_help_project);
        if (helpProject != null && adService != null)
            helpProject.setVisible(adService.isLoaded());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_rate) {
            ActivityHelper.rate(this);
            return true;
        }
        if (id == R.id.action_help_project) {
            adService.tryShow();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        adService.resume();
        super.onResume();
    }

    @Override
    public void onPause() {
        adService.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        adService.destroy();
        super.onDestroy();
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        private Context context;
        PagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
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
                    ? context.getResources().getString(R.string.ringtones_label)
                    : context.getResources().getString(R.string.my_ringtones_label) ;
        }
    }

}
