package com.serp1983.nokiacomposer;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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
import com.com.serp1983.nokiacomposer.lib.FileUtils;
import com.com.serp1983.nokiacomposer.lib.PCMConverter;
import com.com.serp1983.nokiacomposer.lib.ShortArrayList;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.intervigil.wave.WaveWriter;
import com.singlecellsoftware.mp3convert.ConvertActivity;

import java.io.File;
import java.io.IOException;

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

        if (id == R.id.action_save_as) {
            saveAs();
            return true;
        }

        if (id == R.id.action_share) {
            final AppCompatActivity activity = this;
            DialogHelper.shareDialog(this, new DialogHelper.Callback<String>() {
                @Override
                public void onComplete(String input) {
                    if (input == activity.getString(R.string.action_share_text))
                        ShareHelper.shareText(activity, getCurrentRingtone());
                    if (input == activity.getString(R.string.action_share_wav))
                        ShareHelper.shareWav(activity, getCurrentRingtone());
                    if (input == activity.getString(R.string.action_share_mp3))
                        ShareHelper.shareMp3(activity, getCurrentRingtone());
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveAs(){
        final RingtoneVM ringtone = getCurrentRingtone();
        if (ringtone == null) return;
        final Handler handler = new Handler() {
            public void handleMessage(Message response) {
                CharSequence newTitle = (CharSequence)response.obj;
                saveRingtone(newTitle, response.arg1);
            }
        };
        Message message = Message.obtain(handler);
        FileSaveDialog dlg = new FileSaveDialog(this, getResources(), ringtone.Name, message);
        dlg.show();
    }

    private void saveRingtone(final CharSequence title, int fileKind) {
        final RingtoneVM ringtone = getCurrentRingtone();
        if (ringtone == null) return;

        final String outPath = RingtoneSaver.makeRingtoneFilename(title, ".mp3", fileKind);
        if (outPath == null) {
            showFinalAlert(getString(R.string.no_unique_filename));
            return;
        }

        final File outFile = new File(outPath);
        try {
            final File fileWav = new File(getExternalCacheDir().getPath(), "2015nokiacomposer.wav");
            final File fileMp3 = new File(getExternalCacheDir().getPath(), "2015nokiacomposer.mp3");
            ShortArrayList pcm = PCMConverter.getInstance().convert(ringtone.Code, ringtone.Tempo);
            WaveWriter writer = new WaveWriter(fileWav, 44100, 2, 16);
            AsyncWaveWriter.execute(writer, pcm.toArray(), pcm.toArray(), new AsyncWaveWriter.Callback() {
                @Override
                public void onComplete() {
                    try {
                        ConvertActivity.nativeEncodeMP3(fileWav.getAbsolutePath(), 44100, 1);
                        FileUtils.copy(fileMp3, outFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            afterSavingRingtone(title, outFile, fileKind);
        }
        catch(Exception x){
            x.printStackTrace();
        }
    }

    private void afterSavingRingtone(CharSequence title, File outFile, int fileKind) {
        long fileSize = outFile.length();
        String mimeType = "audio/mpeg";

        String artist = "" + getResources().getText(R.string.app_name);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, outFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, title.toString());
        values.put(MediaStore.MediaColumns.SIZE, fileSize);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

        values.put(MediaStore.Audio.Media.ARTIST, artist);
        values.put(MediaStore.Audio.Media.DURATION, 44100);

        values.put(MediaStore.Audio.Media.IS_RINGTONE,
                fileKind == FileSaveDialog.FILE_KIND_RINGTONE);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION,
                fileKind == FileSaveDialog.FILE_KIND_NOTIFICATION);
        values.put(MediaStore.Audio.Media.IS_ALARM,
                fileKind == FileSaveDialog.FILE_KIND_ALARM);
        values.put(MediaStore.Audio.Media.IS_MUSIC,
                fileKind == FileSaveDialog.FILE_KIND_MUSIC);

        // Insert it into the database
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(outFile.getAbsolutePath());
        final Uri newUri = getContentResolver().insert(uri, values);
        setResult(RESULT_OK, new Intent().setData(newUri));

        // There's nothing more to do with music. Show a
        // success message and then quit.
        if (fileKind == FileSaveDialog.FILE_KIND_MUSIC) {
            Toast.makeText(this, R.string.save_success_message, Toast.LENGTH_SHORT).show();
            return;
        }

        // alarm
        if (fileKind == FileSaveDialog.FILE_KIND_ALARM) {
            askSetDefault(RingtoneManager.TYPE_ALARM, R.string.set_default_alarm,
                    R.string.default_alarm_success_message, newUri);
            return;
        }

        // If it's a notification, give the user the option of making
        // this their default notification.  If they say no, we're finished.
        if (fileKind == FileSaveDialog.FILE_KIND_NOTIFICATION) {
            askSetDefault(RingtoneManager.TYPE_NOTIFICATION, R.string.set_default_notification,
                    R.string.default_notification_success_message, newUri);
            return;
        }

        // If we get here, that means the type is a ringtone.
        if (fileKind == FileSaveDialog.FILE_KIND_RINGTONE) {
            askSetDefault(RingtoneManager.TYPE_RINGTONE, R.string.set_default_ringtone,
                    R.string.default_ringtone_success_message, newUri);
            return;
        }
    }

    private void askSetDefault(final int type, final int questionId,
                                  final int successMsgId, final Uri newUri){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.alert_title_success)
                .setMessage(questionId)
                .setPositiveButton(R.string.alert_yes_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                RingtoneManager.setActualDefaultRingtoneUri(MainActivity.this,
                                        type,newUri);
                                Toast.makeText(MainActivity.this, successMsgId, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                .setNegativeButton(R.string.alert_no_button, null)
                .setCancelable(false)
                .show();
    }

    private void showFinalAlert(CharSequence message) {
        CharSequence title = getResources().getText(R.string.alert_title_success);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                        R.string.alert_ok_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                finish();
                            }
                        })
                .setCancelable(false)
                .show();
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
}