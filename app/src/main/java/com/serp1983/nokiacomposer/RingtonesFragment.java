package com.serp1983.nokiacomposer;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.serp1983.nokiacomposer.logic.DataService;
import com.serp1983.nokiacomposer.domain.RingtoneVM;
import com.serp1983.nokiacomposer.logic.FirebaseDatabaseService;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort){
            String title = getString(R.string.action_sort);
            DialogHelper.showSingleChoice(this.getContext(), title, R.array.sort_mode, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    if (i == 0)
                        adapter.sort(RingtoneVM.COMPARE_BY_NEW);
                    if (i == 1)
                        adapter.sort(RingtoneVM.COMPARE_BY_Name);
                    dialog.dismiss();
                }
            }, null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_ringtones, container, false);
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

        ArrayList<RingtoneVM> ringtones = DataService.getInstance().getAssetRingtones();
        if (position == 1)
            ringtones = DataService.getInstance().getMyRingtones();
        if (position == 2)
            ringtones = FirebaseDatabaseService.data;

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
                startActivity(ComposerActivity.getIntent(view.getContext(), item));
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

        final Boolean isCloudModerator = App.isModerator && ringtone.getKey() != null && !ringtone.getKey().isEmpty();
        if (isCloudModerator)
            popup.inflate(R.menu.menu_cloud_ringtone);
        else if (ringtone.IsMy )
            popup.inflate(R.menu.menu_my_ringtone);
        else
            popup.inflate(R.menu.menu_ringtone);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final Context context = view.getContext();
                Activity activity = ActivityHelper.getActivity(context);

                switch (item.getItemId()) {
                    case R.id.action_rename:
                        String hint = context.getString(R.string.ringtone_name_label);
                        DialogHelper.inputDialog(context, null, hint, ringtone.getName(), new DialogHelper.Callback<String>() {
                            @Override
                            public void onComplete(String input) {
                                if (input == null || input.isEmpty() || input.equals(ringtone.getName()))
                                    return;
                                ringtone.setName(input);
                                if (ringtone.IsMy)
                                    DataService.getInstance().saveMyRingtones(context);
                                else if (isCloudModerator)
                                    FirebaseDatabaseService.setName(ringtone);
                            }
                        });
                        break;
                    case R.id.action_delete:
                        if (ringtone.IsMy)
                            DataService.getInstance().deleteMyRingtone(context, ringtone);
                        else if (isCloudModerator)
                            FirebaseDatabaseService.delete(ringtone);
                        break;
                    case R.id.action_set_as_ringtone:
                        if (activity != null)
                            SetAsRingtoneService.setAsRingtone(activity, ringtone);
                        break;
                    case R.id.action_share:
                        DialogHelper.showShareDialog(context, ringtone);
                        break;
                    case R.id.action_save_in_music:
                        if (activity != null)
                            SetAsRingtoneService.saveInMusic(activity, ringtone);
                        break;
                    case R.id.action_open:
                        context.startActivity(ComposerActivity.getIntent(context, ringtone));
                        break;
                    case R.id.action_save_in_cloud:
                        FirebaseDatabaseService.add(ringtone.getRingtoneDTO());
                        Toast.makeText(activity, activity.getString(R.string.msg_to_moderation), Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });

        popup.show();
    }
}
