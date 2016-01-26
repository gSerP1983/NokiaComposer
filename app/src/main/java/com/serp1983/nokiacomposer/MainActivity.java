package com.serp1983.nokiacomposer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.com.serp1983.nokiacomposer.lib.AsyncAudioTrack;
import com.com.serp1983.nokiacomposer.lib.AsyncWaveWriter;
import com.com.serp1983.nokiacomposer.lib.PCMConverter;
import com.com.serp1983.nokiacomposer.lib.ShortArrayList;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.intervigil.wave.WaveWriter;
import com.singlecellsoftware.mp3convert.ConvertActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    EditText _editTextTempo;
    EditText _editTextCode;
    FloatingActionButton _fabPlayOn;
    FloatingActionButton _fabPlayOff;
    FloatingActionButton _fabSave;
    ViewGroup _root;
    ViewGroup _sceneRoot;

    private Boolean disallowEnableSave = false;
    private RingtoneVM currentRingtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        DataService.initialize(this);

        _root = (ViewGroup) findViewById(R.id.root);
        _sceneRoot = (ViewGroup) findViewById(R.id.scene_root);
        _editTextTempo = (EditText) findViewById(R.id.editTextTempo);
        _editTextCode = (EditText) findViewById(R.id.editTextCode);
        _fabPlayOn = (FloatingActionButton) findViewById(R.id.playOn );
        _fabPlayOff = (FloatingActionButton) findViewById(R.id.playOff);
        _fabSave = (FloatingActionButton) findViewById(R.id.save);

        setRingtone(new RingtoneVM("Coca Cola", 125, "8#f2 8#f2 8#f2 8#f2 4g2 8#f2 4e2 8e2 8a2 4#f2 4d2 2-"));

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
                _fabPlayOff.setVisibility(View.VISIBLE);
                animatePlayOff(true);
                play();
            }
        });

        _fabPlayOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) return;
                animatePlayOff(false);
                _fabPlayOff.setVisibility(View.INVISIBLE);
                AsyncAudioTrack.stop();
            }
        });

        _fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) return;
                animateSave(false);
                _fabSave.setVisibility(View.INVISIBLE);
                save();
            }
        });

        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest.Builder adBuilder = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(Constants.testDeviceId);
        adView.loadAd(adBuilder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        boolean flag = currentRingtone != null && currentRingtone.IsMy != null && currentRingtone.IsMy;
        menu.findItem(R.id.action_delete).setEnabled(flag);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open) {
            open();
            return true;
        }

        if (id == R.id.action_new) {
            setRingtone(new RingtoneVM("Noname", 120, ""));
            return true;
        }

        if (id == R.id.action_delete) {
            if (DataService.getInstance().deleteMyRingtone(currentRingtone)){
                Toast.makeText(MainActivity.this, currentRingtone.Name + " deleted...", Toast.LENGTH_SHORT).show();
                currentRingtone.IsMy = false;
            }
            return true;
        }

        if (id == R.id.action_save) {
            save();
            return true;
        }

        if (id == R.id.action_share) {
            final AppCompatActivity activity = this;
            DialogHelper.shareDialog(this, new DialogHelper.Callback<String>() {
                @Override
                public void onComplete(String input) {
                    if (input == activity.getString(R.string.action_share_text))
                        shareText(getCurrentRingtone());
                    if (input == activity.getString(R.string.action_share_wav))
                        shareWav(getCurrentRingtone());
                    if (input == activity.getString(R.string.action_share_mp3))
                        shareMp3(getCurrentRingtone());
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void animatePlayOff(Boolean on){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(_root);
        }
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) _fabPlayOff.getLayoutParams();
        params.bottomMargin = this.getResources().getDimensionPixelSize(R.dimen.fab_marginBottom) +
                (on ? this.getResources().getDimensionPixelSize(R.dimen.fabAddon_margin) : 0);
        _fabPlayOff.setLayoutParams(params);
    }

    private void animateSave(Boolean on){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.root));
        }
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) _fabSave.getLayoutParams();
        params.rightMargin = this.getResources().getDimensionPixelSize(R.dimen.fab_marginRight) +
                (on ? this.getResources().getDimensionPixelSize(R.dimen.fabAddon_margin) : 0);
        _fabSave.setLayoutParams(params);
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
        }
    }

    private Boolean validate(){
        String codeStr = _editTextCode.getText().toString();
        String tempoStr = _editTextTempo.getText().toString();

        if (codeStr.isEmpty() || tempoStr.isEmpty())
            return false;

        int tempo = Integer.parseInt(tempoStr);
        if (tempo < 0 || tempo > 1000)
            return false;

        return true;
    }

    private void safetyPlayOff(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!AsyncAudioTrack.isRun) {
                    animatePlayOff(false);
                    _fabPlayOff.setVisibility(View.INVISIBLE);
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
                _fabSave.setVisibility(View.VISIBLE);
                animateSave(true);
            }
        };
    }

    private void setRingtone(RingtoneVM ringtone){
        if (ringtone == null) return;
        Toast.makeText(MainActivity.this, ringtone.Name, Toast.LENGTH_SHORT).show();

        disallowEnableSave = true;
        _editTextTempo.setText("" + ringtone.Tempo, TextView.BufferType.EDITABLE);
        _editTextCode.setText(ringtone.Code, TextView.BufferType.EDITABLE);
        disallowEnableSave = false;

        currentRingtone = ringtone;

        animateSave(false);
        _fabSave.setVisibility(View.INVISIBLE);
    }

    private void save(){
        final RingtoneVM ringtone = getCurrentRingtone();
        if (ringtone == null) return;
        String name = ringtone.Name;
        if (!name.startsWith("(My) ")) name = "(My) " + name;
        DialogHelper.inputDialog(this, "", "Name", name, new DialogHelper.Callback<String>() {
            @Override
            public void onComplete(String input) {
                if (!input.isEmpty()) {
                    ringtone.IsMy = true;
                    ringtone.Name = input;
                    if (DataService.getInstance().saveMyRingtone(ringtone))
                        Toast.makeText(MainActivity.this, input + " saved...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private RingtoneVM getCurrentRingtone(){
        if (!validate() || currentRingtone == null) return null;
        String codeStr = _editTextCode.getText().toString();
        String tempoStr = _editTextTempo.getText().toString();
        int tempo = Integer.parseInt(tempoStr);
        return new RingtoneVM(currentRingtone.Name, tempo, codeStr);
    }

    private void open(){
        DialogHelper.selectRingtoneDialog(this, DataService.getInstance().getAll(), new DialogHelper.Callback<RingtoneVM>() {
            @Override
            public void onComplete(RingtoneVM input) {
                if (input != null) {
                    setRingtone(input);
                }
            }
        });
    }

    public void shareText(RingtoneVM ringtone){
        if (ringtone == null) return;
        try{
            Intent intentSend = new Intent(Intent.ACTION_SEND);
            intentSend.setType("text/plain");
            intentSend.putExtra(Intent.EXTRA_TEXT, ringtone.Name + ", tempo=" + ringtone.Tempo + ", " + ringtone.Code);
            intentSend.putExtra(Intent.EXTRA_SUBJECT, ringtone.Name);

            Intent intentChooser = Intent.createChooser(intentSend, "Share");
            intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            this.startActivity(intentChooser);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void shareWav(RingtoneVM ringtone){
        if (ringtone == null) return;

        try{
            File file = new File(getExternalCacheDir().getPath(), "2015nokiacomposer.wav");
            ConvertActivity.nativeEncodeMP3(file.getAbsolutePath(), 44100, 0);

            ShortArrayList pcm = PCMConverter.getInstance().convert(ringtone.Code, ringtone.Tempo);
            WaveWriter writer = new WaveWriter(file, 44100, 1, 16);
            AsyncWaveWriter.execute(writer, pcm.toArray(), null, null);

            Intent intentSend = new Intent(Intent.ACTION_SEND);
            intentSend.setType("sound/wav");
            intentSend.putExtra(Intent.EXTRA_SUBJECT, ringtone.Name);
            intentSend.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

            Intent intentChooser = Intent.createChooser(intentSend, "Share *.wav");
            intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            this.startActivity(intentChooser);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void shareMp3(RingtoneVM ringtone){
        if (ringtone == null) return;

        try{
            final File fileWav = new File(getExternalCacheDir().getPath(), "2015nokiacomposer.wav");

            ShortArrayList pcm = PCMConverter.getInstance().convert(ringtone.Code, ringtone.Tempo);
            WaveWriter writer = new WaveWriter(fileWav, 44100, 2, 16);
            AsyncWaveWriter.execute(writer, pcm.toArray(), pcm.toArray(), new AsyncWaveWriter.Callback() {
                @Override
                public void onComplete() {
                    ConvertActivity.nativeEncodeMP3(fileWav.getAbsolutePath(), 44000, 1);
                }
            });

            final File fileMp3 = new File(getExternalCacheDir().getPath(), "2015nokiacomposer.mp3");
            Intent intentSend = new Intent(Intent.ACTION_SEND);
            intentSend.setType("sound/mp3");
            intentSend.putExtra(Intent.EXTRA_SUBJECT, ringtone.Name);
            intentSend.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileMp3));

            Intent intentChooser = Intent.createChooser(intentSend, "Share *.mp3");
            intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            this.startActivity(intentChooser);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}