package com.serp1983.nokiacomposer;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.serp1983.nokiacomposer.logic.DataService;
import com.serp1983.nokiacomposer.logic.RingtoneVM;


public class RingtonesFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private int position;
    private ListView listView;

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
                if (TextUtils.isEmpty(newText)) {
                    listView.clearTextFilter();
                } else {
                    listView.setFilterText(newText);
                }
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ringtones, container, false);

        listView = (ListView) view.findViewById(R.id.listView);
        RingtoneVM[] ringtones = position == 0 ? DataService.getInstance().getAssetRingtones() : DataService.getInstance().getMyRingtones();
        listView.setAdapter(new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, ringtones));
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //if (callback != null)
                //    callback.onComplete((T) parent.getAdapter().getItem(position));
            }
        });

        return view;
    }

}
