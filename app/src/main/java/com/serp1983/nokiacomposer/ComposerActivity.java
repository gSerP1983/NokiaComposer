package com.serp1983.nokiacomposer;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.serp1983.nokiacomposer.databinding.ActivityComposerBinding;
import com.serp1983.nokiacomposer.domain.ComposerVM;
import com.serp1983.nokiacomposer.domain.Note;
import com.serp1983.nokiacomposer.domain.RingtoneVM;
import com.serp1983.nokiacomposer.logic.SetAsRingtoneService;
import com.serp1983.nokiacomposer.util.DialogHelper;

public class ComposerActivity extends AppCompatActivity {

    private FlexboxLayout flexBox;
    private ComposerVM vm;

    public static Intent getIntent(Context context, RingtoneVM ringtone){
        Intent intent = new Intent(context, ComposerActivity.class);
        intent.putExtra("name", ringtone.getName());
        intent.putExtra("tempo", ringtone.Tempo);
        intent.putExtra("code", ringtone.Code);
        intent.putExtra("isMy", ringtone.IsMy);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityComposerBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_composer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        flexBox = (FlexboxLayout) findViewById(R.id.flexBox);

        vm = new ComposerVM();
        binding.contentComposer.setVm(vm);
        vm.Notes.addOnListChangedCallback(getAddOnListChangedCallback());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.action_save) {
            //save();
            return true;
        }

        if (id == R.id.action_set_as_ringtone) {
            //SetAsRingtoneService.setAsRingtone(this, getCurrentRingtone());
            return true;
        }

        if (id == R.id.action_share) {
            //DialogHelper.showShareDialog(this, getCurrentRingtone());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private AppCompatTextView CreateTextView(Note note){
        AppCompatTextView textView = new AppCompatTextView(this, null, android.R.attr.editTextStyle);
        textView.setTag(note);
        //textView.setTextSize(20);
        textView.setPadding(8,8,8,8);
        textView.setText(note.toString());
        textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    vm.CurrentNote = (Note) v.getTag();
            }
        });
        return textView;
    }

    private ObservableList.OnListChangedCallback<ObservableList<Note>> getAddOnListChangedCallback(){
        return new ObservableList.OnListChangedCallback<ObservableList<Note>>() {
            @Override
            public void onChanged(ObservableList<Note> notes) {
            }

            @Override
            public void onItemRangeChanged(ObservableList<Note> notes, int index, int count) {
                Note note = notes.get(index);
                TextView tv = (TextView) flexBox.findViewWithTag(note);
                tv.setText(note.toString());
            }

            @Override
            public void onItemRangeInserted(ObservableList<Note> notes, int index, int count) {
                TextView tv = ComposerActivity.this.CreateTextView(notes.get(index));
                flexBox.addView(tv, index);
                tv.requestFocus();
            }

            @Override
            public void onItemRangeMoved(ObservableList<Note> notes, int index, int i1, int i2) {
            }

            @Override
            public void onItemRangeRemoved(ObservableList<Note> notes, int index, int count) {
                flexBox.removeViewAt(index);
                if (index == notes.size() && index != 0)
                    flexBox.getChildAt(index - 1).requestFocus();
                if (index < notes.size())
                    flexBox.getChildAt(index).requestFocus();
            }
        };
    }

}
