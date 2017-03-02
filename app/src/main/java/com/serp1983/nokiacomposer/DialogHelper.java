package com.serp1983.nokiacomposer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

class DialogHelper {
    interface Callback<T> {
        void onComplete(T input);
    }
    static void inputDialog(Context context, String title, String hint, String defValue,
                                   final Callback callback){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (!title.isEmpty()) builder.setTitle(title);

        final EditText input = new EditText(context);
        if (!defValue.isEmpty()) input.setText(defValue, TextView.BufferType.EDITABLE);
        if (!hint.isEmpty()) input.setHint(hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (callback != null) callback.onComplete(input.getText().toString());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    static <T> void selectRingtoneDialog(Activity activity, T[] ringtones, final Callback<T> callback){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select ringtone:");
        LayoutInflater inflater = activity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.content_list, null);
        builder.setView(convertView);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();

        final ListView listView = (ListView) convertView.findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, ringtones));
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (callback != null)
                    callback.onComplete((T) parent.getAdapter().getItem(position));
                dialog.dismiss();
            }
        });

        SearchView search = (SearchView) convertView.findViewById(R.id.search_view);
        search.setIconifiedByDefault(false);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
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

        dialog.show();
    }

    static <T> void shareDialog(Activity activity, final Callback<T> callback){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.share_list, null);
        builder.setView(convertView);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();

        final ListView listView = (ListView) convertView.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (callback != null)
                    callback.onComplete((T) parent.getAdapter().getItem(position));
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
