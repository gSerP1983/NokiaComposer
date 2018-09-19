package com.serp1983.nokiacomposer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.ads.AdView;
import com.serp1983.nokiacomposer.databinding.ActivityComposerBinding;
import com.serp1983.nokiacomposer.domain.ComposerVM;
import com.serp1983.nokiacomposer.domain.Note;
import com.serp1983.nokiacomposer.domain.RingtoneVM;
import com.serp1983.nokiacomposer.lib.PCMConverter;
import com.serp1983.nokiacomposer.logic.DataService;
import com.serp1983.nokiacomposer.logic.SetAsRingtoneService;
import com.serp1983.nokiacomposer.util.ActivityHelper;
import com.serp1983.nokiacomposer.util.DialogHelper;

@BindingMethods({
        @BindingMethod(type = android.widget.ImageView.class,
                attribute = "app:srcCompat",
                method = "setImageDrawable") })
public class ComposerActivity extends AppCompatActivity {

    private FlexboxLayout flexBox;
    private ComposerVM vm;
    private RingtoneVM currentMyRingtone;
    private static int countNew = 0;

    public static Intent getIntent(Context context, RingtoneVM ringtone){
        Intent intent = new Intent(context, ComposerActivity.class);
        intent.putExtra("name", ringtone.getName());
        intent.putExtra("tempo", ringtone.getTempo());
        intent.putExtra("code", ringtone.getCode());
        intent.putExtra("isMy", ringtone.IsMy);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityComposerBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_composer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        flexBox = findViewById(R.id.flexBox);

        String name = "New" + ++countNew;
        int tempo = 120;
        String code = "";
        boolean isMy = false;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            name = bundle.getString("name", null);
            tempo = bundle.getInt("tempo");
            code = bundle.getString("code");
            isMy = bundle.getBoolean("isMy");
            currentMyRingtone = DataService.getInstance().findMyRingtone(tempo, name, code);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        vm = new ComposerVM(name, tempo);
        vm.IsMy = isMy;
        vm.setKeySound(prefs.getBoolean(ComposerVM.keySoundPrefName, true));
        vm.setNotePin(prefs.getBoolean(ComposerVM.notePinPrefName, false));
        binding.contentComposer.setVm(vm);
        vm.Notes.addOnListChangedCallback(getAddOnListChangedCallback());
        if (!"".equals(code))
            vm.setCode(code);

        if (vm.Notes.size() > 0)
            flexBox.findViewWithTag(vm.Notes.get(0)).requestFocus();

        if (bar != null) {
            bar.setSubtitle(vm.getName());
        }
        Toast.makeText(this, vm.getName(), Toast.LENGTH_SHORT).show();

        AdView adView = this.findViewById(R.id.adView);
        adView.loadAd(ActivityHelper.getAdBuilder().build());
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem keyPress = menu.findItem(R.id.action_key_press);
        if (keyPress != null)
            keyPress.setVisible(vm.Notes.size() > 0);

        MenuItem save = menu.findItem(R.id.action_save);
        if (save != null)
            save.setVisible(vm.Notes.size() > 0 && vm.IsMy);

        MenuItem saveInMyRingtones = menu.findItem(R.id.action_save_in_my_ringtones);
        if (saveInMyRingtones != null)
            saveInMyRingtones.setVisible(vm.Notes.size() > 0);

        MenuItem setRingtone = menu.findItem(R.id.action_set_as_ringtone);
        if (setRingtone != null)
            setRingtone.setVisible(vm.Notes.size() > 0);

        MenuItem share = menu.findItem(R.id.action_share);
        if (share != null)
            share.setVisible(vm.Notes.size() > 0);

        MenuItem saveInMusic = menu.findItem(R.id.action_save_in_music);
        if (saveInMusic != null)
            saveInMusic.setVisible(vm.Notes.size() > 0);

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_composer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.action_help) {
            DialogHelper.showAlert(this, getString(R.string.action_help), getString(R.string.msg_help), null);
            return true;
        }

        if (id == R.id.action_tempo) {
            vm.onTempoClick(getWindow().getDecorView());
            return true;
        }

        if (id == R.id.action_quick_edit) {
            DialogHelper.multilineInputDialog(this, null, null, vm.getCode(), new DialogHelper.Callback<String>() {
                @Override
                public void onComplete(String input) {
                    vm.setCode(input);
                }
            });
            return true;
        }

        if (id == R.id.action_key_press) {
            DialogHelper.showAlert(this, "", PCMConverter.getInstance().convert2Keys(vm.getCode()),null);
            return true;
        }

        if (id == R.id.action_save) {
            save();
            return true;
        }

        if (id == R.id.action_save_in_my_ringtones) {
            saveInMyRingtones();
            return true;
        }

        if (id == R.id.action_set_as_ringtone) {
            SetAsRingtoneService.setAsRingtone(this, vm);
            return true;
        }

        if (id == R.id.action_share) {
            DialogHelper.showShareDialog(this, vm);
            return true;
        }

        if (id == R.id.action_save_in_music) {
            SetAsRingtoneService.saveInMusic(this, vm);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void save() {
        if (currentMyRingtone == null)
            return;

        currentMyRingtone.setTempo(vm.getTempo());
        currentMyRingtone.setCode(vm.getCode());
        DataService.getInstance().saveMyRingtones(this);

        String msg = currentMyRingtone.getName() + " " + ComposerActivity.this.getString(R.string.msg_saved);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void saveInMyRingtones(){
        String name = vm.getName();
        if (!name.startsWith("(My) "))
            name = "(My) " + name;

        String hint = this.getString(R.string.ringtone_name_label);
        DialogHelper.inputDialog(this, "", hint, name, new DialogHelper.Callback<String>() {
            @Override
            public void onComplete(String input) {
                if (input.isEmpty()) {
                    Toast.makeText(ComposerActivity.this, R.string.err_empty_name, Toast.LENGTH_SHORT).show();
                    return;
                }

                // checking unique name
                String name = input.replaceAll("\\s", "").toUpperCase();
                for(RingtoneVM myRingtone : DataService.getInstance().getMyRingtones()) {
                    String myRingtoneName = myRingtone.getName().replaceAll("\\s", "").toUpperCase();
                    if (myRingtoneName.equals(name)) {
                        Toast.makeText(ComposerActivity.this, R.string.err_double_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                vm.IsMy = true;
                vm.setName(input.trim());

                if (DataService.getInstance().addMyRingtone(ComposerActivity.this, vm)) {
                    String msg = input + " " + ComposerActivity.this.getString(R.string.msg_saved);
                    Toast.makeText(ComposerActivity.this, msg, Toast.LENGTH_SHORT).show();
                    // FirebaseDatabaseService.add(vm.getRingtoneDTO());
                }
            }
        });
    }

    private AppCompatTextView CreateTextView(Note note){
        AppCompatTextView textView = new AppCompatTextView(this, null, android.R.attr.editTextStyle);
        textView.setTag(note);
        textView.setPadding(8,8,8,8);
        textView.setText(note.toString());
        textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    vm.CurrentNote = (Note) v.getTag();
            }
        });
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                vm.CurrentNote = (Note) v.getTag();
                vm.playCurrentNote();
                return false;
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
                TextView tv = flexBox.findViewWithTag(note);
                tv.setText(note.toString());
            }

            @Override
            public void onItemRangeInserted(ObservableList<Note> notes, int index, int count) {
                TextView tv = ComposerActivity.this.CreateTextView(notes.get(index));
                flexBox.addView(tv, index);
                tv.requestFocus();

                if (vm != null && "4E1 4C1 4F1 4C1 2G1 2D1 2A1".equals(vm.getCode())) {
                    App.isModerator = true;
                    Toast.makeText(ComposerActivity.this, "Moderator", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemRangeMoved(ObservableList<Note> notes, int index, int i1, int i2) {
            }

            @Override
            public void onItemRangeRemoved(ObservableList<Note> notes, int index, int count) {
                if (notes.size() == 0){
                    flexBox.removeAllViews();
                }
                else {
                    flexBox.removeViewAt(index);
                    if (index != 0)
                        flexBox.getChildAt(index - 1).requestFocus();
                }
            }
        };
    }

}
