package com.serp1983.nokiacomposer.logic;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.intervigil.wave.WaveWriter;
import com.serp1983.nokiacomposer.R;
import com.serp1983.nokiacomposer.lib.AsyncWaveWriter;
import com.serp1983.nokiacomposer.lib.FileUtils;
import com.serp1983.nokiacomposer.lib.PCMConverter;
import com.serp1983.nokiacomposer.lib.ShortArrayList;
import com.serp1983.nokiacomposer.util.DialogHelper;
import com.singlecellsoftware.mp3convert.ConvertActivity;

import java.io.File;
import java.io.IOException;

public class SetAsRingtoneService {
    static final int FILE_KIND_ALARM = 0;
    static final int FILE_KIND_NOTIFICATION = 1;
    static final int FILE_KIND_RINGTONE = 2;

    public static void setAsRingtone(final Context context, final RingtoneVM ringtone){
        if (ringtone == null)
            return;

        // java.lang.SecurityException: com.serp1983.nokiacomposer
        // was not granted  this permission: android.permission.WRITE_SETTINGS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                String msg = context.getString(R.string.msg_modify_system_settings);
                DialogHelper.showAlert(context, null, msg, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        @SuppressLint("InlinedApi")
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        context.startActivity(intent);
                    }
                });

                return;
            }
        }

        String title = context.getString(R.string.title_set_as_ringtone);
        DialogHelper.showSingleChoice(context, title, R.array.ringtone_type_array, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                saveRingtone(context, ringtone, item);
                dialog.dismiss();
            }
        }, null);
    }

    private static void saveRingtone(final Context context, final RingtoneVM ringtone, int fileKind) {
        if (ringtone == null)
            return;

        String title = ringtone.Name;
        switch(fileKind) {
            default:
            case SetAsRingtoneService.FILE_KIND_ALARM:
                title += " alarm";
                break;
            case SetAsRingtoneService.FILE_KIND_NOTIFICATION:
                title += " notify";
                break;
            case SetAsRingtoneService.FILE_KIND_RINGTONE:
                title += " ringtone";
                break;
        }

        int i = 0;
        String newTitle = title;
        while (findRingtone(context, MediaStore.Audio.Media.INTERNAL_CONTENT_URI, newTitle) ||
                findRingtone(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, newTitle)){
            newTitle = title + " " + ++i;
        }
        title = newTitle;

        final String outPath = RingtoneSaver.makeRingtoneFilename(title, ".mp3", fileKind);
        if (outPath == null) {
            Toast.makeText(context, android.R.string.cancel, Toast.LENGTH_SHORT).show();
            FirebaseCrash.log("SetAsRingtoneService.saveRingtone(...): Unable to find unique filename!");
            return;
        }

        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null)
            return;

        final File outFile = new File(outPath);
        try {
            final File fileWav = new File(externalCacheDir.getPath(), "nokiacomposer.wav");
            final File fileMp3 = new File(externalCacheDir.getPath(), "nokiacomposer.mp3");
            ShortArrayList pcm = PCMConverter.getInstance().convert(ringtone.Code, ringtone.Tempo);
            WaveWriter writer = new WaveWriter(fileWav, 44100, 2, 16);
            AsyncWaveWriter.execute(writer, pcm.toArray(), pcm.toArray(), new AsyncWaveWriter.Callback() {
                @Override
                public void onComplete() {
                    try {
                        ConvertActivity.nativeEncodeMP3(fileWav.getAbsolutePath(), 44100, 1);
                        FileUtils.copy(fileMp3, outFile);
                    } catch (IOException e) {
                        FirebaseCrash.log("SetAsRingtoneService.saveRingtone(...)");
                        FirebaseCrash.report(e);
                    }
                }
            });

            afterSavingRingtone(context, title, outFile, fileKind);
        }
        catch(Exception e){
            Toast.makeText(context, android.R.string.cancel, Toast.LENGTH_SHORT).show();
            FirebaseCrash.log("SetAsRingtoneService.saveRingtone(...)");
            FirebaseCrash.report(e);
        }
    }

    private static Boolean findRingtone(final Context context, Uri uri, CharSequence title){
        try {
            Cursor c = context.getContentResolver().query(
                    uri,
                    new String[]{MediaStore.Audio.Media.TITLE},
                    null, null, null);
            if (c == null)
                return false;

            String titleString = String.valueOf(title);
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                if (titleString.toLowerCase().equals(c.getString(0).toLowerCase())) {
                    return true;
                }
            }
            c.close();
        }
        catch(Exception e){
            FirebaseCrash.log("SetAsRingtoneService.findRingtone(...)");
            FirebaseCrash.report(e);
        }
        return false;
    }

    private static void afterSavingRingtone(final Context context, CharSequence title, File outFile, int fileKind) {
        long fileSize = outFile.length();
        String mimeType = "audio/mpeg";

        String artist = "" + context.getResources().getText(R.string.app_name);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, outFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, title.toString());
        values.put(MediaStore.MediaColumns.SIZE, fileSize);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

        values.put(MediaStore.Audio.Media.ARTIST, artist);
        values.put(MediaStore.Audio.Media.DURATION, 44100);

        values.put(MediaStore.Audio.Media.IS_RINGTONE, fileKind == FILE_KIND_RINGTONE);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, fileKind == FILE_KIND_NOTIFICATION);
        values.put(MediaStore.Audio.Media.IS_ALARM, fileKind == FILE_KIND_ALARM);

        // Insert it into the database
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(outFile.getAbsolutePath());
        final Uri newUri = context.getContentResolver().insert(uri, values);
        //setResult(RESULT_OK, new Intent().setData(newUri));

        // alarm
        if (fileKind == FILE_KIND_ALARM) {
            setAsDefaultRingtone(context, RingtoneManager.TYPE_ALARM, newUri);
            return;
        }

        // notification
        if (fileKind == FILE_KIND_NOTIFICATION) {
            setAsDefaultRingtone(context, RingtoneManager.TYPE_NOTIFICATION, newUri);
            return;
        }

        // ringtone.
        if (fileKind == FILE_KIND_RINGTONE) {
            setAsDefaultRingtone(context, RingtoneManager.TYPE_RINGTONE, newUri);
        }
    }

    private static void setAsDefaultRingtone(final Context context, final int type, final Uri newUri) {
        RingtoneManager.setActualDefaultRingtoneUri(context, type, newUri);
        Toast.makeText(context, R.string.alert_title_success, Toast.LENGTH_SHORT).show();
    }
}
