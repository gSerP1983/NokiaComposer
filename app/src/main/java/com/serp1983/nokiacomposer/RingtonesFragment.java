package com.serp1983.nokiacomposer;


import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.serp1983.nokiacomposer.logic.DataService;
import com.serp1983.nokiacomposer.logic.RingtoneVM;
import com.serp1983.nokiacomposer.logic.SetAsRingtoneService;
import com.serp1983.nokiacomposer.util.ActivityHelper;
import com.serp1983.nokiacomposer.util.DialogHelper;
import com.serp1983.nokiacomposer.util.RecyclerBindingAdapter;

import java.util.ArrayList;


public class RingtonesFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private int position;
    private RecyclerBindingAdapter<RingtoneVM> adapter;

    public RingtonesFragment() {
        // Required empty public constructor
    }

    public static RingtonesFragment newInstance(int position) {
        RingtonesFragment fragment = new RingtonesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION, 0);
        }
        // must be here, copy parent activity menu items
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                myActionMenuItem.collapseActionView();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_ringtones, container, false);
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        ArrayList<RingtoneVM> ringtones = position == 0
                ? DataService.getInstance().getAssetRingtones()
                : DataService.getInstance().getMyRingtones();
        adapter = new RecyclerBindingAdapter<>(R.layout.list_item_ringtone, BR.ringtone, ringtones);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                fab.show();
            }
        });

        adapter.setOnItemClickListener(new RecyclerBindingAdapter.OnItemClickListener<RingtoneVM>() {
            @Override
            public void onItemClick(int position, RingtoneVM item) {
                startActivity(DetailsActivity.getIntent(view.getContext(), item));
            }
        });

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.ringtones_recycler);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0)
                    fab.hide();
                else if (dy < 0)
                    fab.show();
            }
        });

        return view;
    }

    public static void showRingtoneMenu(final View view, final RingtoneVM ringtone) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(ringtone.IsMy ? R.menu.menu_my_ringtone : R.menu.menu_ringtone);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        DataService.getInstance().deleteMyRingtone(view.getContext(), ringtone);
                        break;
                    case R.id.action_set_as_ringtone:
                        Activity activity = ActivityHelper.getActivity(view.getContext());
                        if (activity != null)
                            SetAsRingtoneService.setAsRingtone(activity, ringtone);
                        break;
                    case R.id.action_share:
                        DialogHelper.showShareDialog(view.getContext(), ringtone);
                        break;
                }
                return false;
            }
        });

        popup.show();
    }
}
