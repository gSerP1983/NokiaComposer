package com.serp1983.nokiacomposer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.ads.AdView;
import com.serp1983.nokiacomposer.lib.AsyncAudioTrack;
import com.serp1983.nokiacomposer.lib.PCMConverter;
import com.serp1983.nokiacomposer.lib.SamplingType;
import com.serp1983.nokiacomposer.lib.ShortArrayList;
import com.serp1983.nokiacomposer.util.ActivityHelper;
import com.serp1983.nokiacomposer.util.DialogHelper;

public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;

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
                startActivity(new Intent(MainActivity.this, ComposerActivity.class));
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

        AdView adView = (AdView) this.findViewById(R.id.adView);
        adView.loadAd(ActivityHelper.getAdBuilder().build());
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
        getMenuInflater().inflate( R.menu.menu_main, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (viewPager != null) {
            MenuItem search = menu.findItem(R.id.action_search);
            if (search != null)
                search.setVisible(viewPager.getCurrentItem() == 0 || viewPager.getCurrentItem() == 2);
            MenuItem sort = menu.findItem(R.id.action_sort);
            if (sort != null)
                sort.setVisible(viewPager.getCurrentItem() == 2);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_rate) {
            ActivityHelper.rate(this);
            return true;
        }
        if (id == R.id.action_settings) {
            String title = getString(R.string.action_settings);
            DialogHelper.showSingleChoice(this, title, R.array.sampling_type_array, SamplingType.getSamplingType(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ShortArrayList pcm = PCMConverter.getInstance().convert("8C1 8G1 8C1 8D1", 200, which);
                    AsyncAudioTrack.start(PCMConverter.shorts2Bytes(pcm), null);
                }
            }, null, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ListView lw = ((AlertDialog)dialog).getListView();
                    SamplingType.setSamplingType(lw.getCheckedItemPosition());
                    dialog.dismiss();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            return  3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return context.getResources().getString(R.string.ringtones_label);
            if (position == 1)
                return context.getResources().getString(R.string.my_ringtones_label);
            if (position == 2)
                return context.getResources().getString(R.string.ringtones_cloud_label);
            return "";
        }
    }

}
