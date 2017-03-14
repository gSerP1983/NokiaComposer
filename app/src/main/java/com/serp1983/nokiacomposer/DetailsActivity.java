package com.serp1983.nokiacomposer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.crash.FirebaseCrash;
import com.serp1983.nokiacomposer.lib.AsyncAudioTrack;
import com.serp1983.nokiacomposer.lib.PCMConverter;
import com.serp1983.nokiacomposer.lib.ShortArrayList;
import com.serp1983.nokiacomposer.logic.DataService;
import com.serp1983.nokiacomposer.logic.RingtoneVM;
import com.serp1983.nokiacomposer.logic.SetAsRingtoneService;
import com.serp1983.nokiacomposer.util.ActivityHelper;
import com.serp1983.nokiacomposer.util.DialogHelper;

public class DetailsActivity extends AppCompatActivity {
    EditText _editTextTempo;
    EditText _editTextCode;
    FloatingActionButton _fabPlayOn;
    FloatingActionButton _fabPlayOff;
    FloatingActionButton _fabSave;
    ViewGroup _root;
    ViewGroup _sceneRoot;

    private Boolean disallowEnableSave = false;
    private RingtoneVM currentRingtone;
    private static int countNew = 0;

    public static Intent getIntent(Context context, RingtoneVM ringtone){
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra("name", ringtone.getName());
        intent.putExtra("tempo", ringtone.Tempo);
        intent.putExtra("code", ringtone.Code);
        intent.putExtra("isMy", ringtone.IsMy);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            // bar.setTitle(getString(R.string.action_help));
            bar.setDisplayHomeAsUpEnabled(true);
        }

        RingtoneVM ringtone = new RingtoneVM("New" + ++countNew, 120, "");
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String name = bundle.getString("name", null);
            int tempo = bundle.getInt("tempo");
            String code = bundle.getString("code");
            // boolean isMy = bundle.getBoolean("isMy");
            if (name != null)
                ringtone = new RingtoneVM(name, tempo, code);
        }

        _root = (ViewGroup) findViewById(R.id.root);
        _sceneRoot = (ViewGroup) findViewById(R.id.scene_root);
        _editTextTempo = (EditText) findViewById(R.id.editTextTempo);
        _editTextCode = (EditText) findViewById(R.id.editTextCode);
        _fabPlayOn = (FloatingActionButton) findViewById(R.id.playOn );
        _fabPlayOff = (FloatingActionButton) findViewById(R.id.playOff);
        _fabSave = (FloatingActionButton) findViewById(R.id.save);

        setRingtone(ringtone);

        _editTextTempo.addTextChangedListener(getTextWatcher());
        _editTextCode.addTextChangedListener(getTextWatcher());
        _editTextCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(_editTextCode, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        _fabPlayOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) return;
                _fabPlayOff.show();
                play();
            }
        });

        _fabPlayOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) return;
                _fabPlayOff.hide();
                AsyncAudioTrack.stop();
            }
        });

        Button btn = (Button) findViewById(R.id.details_button_tones);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.showAlert(DetailsActivity.this, "",
                        PCMConverter.getInstance().convert2Keys(_editTextCode.getText().toString()),
                        null);
            }
        });

        _fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) return;
                _fabSave.hide();
                save();
            }
        });

        AdView adView = (AdView) this.findViewById(R.id.adView);
        adView.loadAd(ActivityHelper.getAdBuilder().build());
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
            save();
            return true;
        }

        if (id == R.id.action_set_as_ringtone) {
            SetAsRingtoneService.setAsRingtone(this, getCurrentRingtone());
            return true;
        }

        if (id == R.id.action_share) {
            DialogHelper.showShareDialog(this, getCurrentRingtone());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void play(){
        if (!validate()) return;

        String codeStr = _editTextCode.getText().toString();
        String tempoStr = _editTextTempo.getText().toString();
        int tempo = Integer.parseInt(tempoStr);

        try {
            ShortArrayList pcm = PCMConverter.getInstance().convert(codeStr, tempo);
            AsyncAudioTrack.start(PCMConverter.shorts2Bytes(pcm), new AsyncAudioTrack.Callback() {
                @Override
                public void onComplete() {
                    safetyPlayOff();
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
    }

    private Boolean validate(){
        String codeStr = _editTextCode.getText().toString();
        String tempoStr = _editTextTempo.getText().toString();

        if (codeStr.isEmpty() || tempoStr.isEmpty() || tempoStr.length() > 4)
            return false;

        try {
            int tempo = Integer.parseInt(tempoStr);
            return tempo > 0  && tempo <= 1000;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void safetyPlayOff(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!AsyncAudioTrack.isRun) {
                    _fabPlayOff.hide();
                }
            }
        });
    }

    private TextWatcher getTextWatcher(){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (disallowEnableSave || !validate()) return;
                _fabSave.show();
            }
        };
    }

    private void setRingtone(RingtoneVM ringtone){
        if (ringtone == null) return;
        Toast.makeText(DetailsActivity.this, ringtone.getName(), Toast.LENGTH_SHORT).show();

        disallowEnableSave = true;
        _editTextTempo.setText(String.valueOf(ringtone.Tempo), TextView.BufferType.EDITABLE);
        _editTextCode.setText(ringtone.Code, TextView.BufferType.EDITABLE);
        disallowEnableSave = false;

        currentRingtone = ringtone;

        _fabSave.hide();
    }

    private void save(){
        final RingtoneVM ringtone = getCurrentRingtone();
        if (ringtone == null)
            return;

        String name = ringtone.getName();
        if (!name.startsWith("(My) "))
            name = "(My) " + name;

        String hint = this.getString(R.string.ringtone_name_label);
        DialogHelper.inputDialog(this, "", hint, name, new DialogHelper.Callback<String>() {
            @Override
            public void onComplete(String input) {
                if (!input.isEmpty()) {
                    ringtone.IsMy = true;
                    ringtone.setName(input);
                    if (DataService.getInstance().addMyRingtone(DetailsActivity.this, ringtone)) {
                        String msg = input + " " + DetailsActivity.this.getString(R.string.msg_saved);
                        Toast.makeText(DetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private RingtoneVM getCurrentRingtone(){
        if (!validate() || currentRingtone == null) return null;
        String codeStr = _editTextCode.getText().toString();
        String tempoStr = _editTextTempo.getText().toString();
        int tempo = Integer.parseInt(tempoStr);
        return new RingtoneVM(currentRingtone.getName(), tempo, codeStr);
    }

}