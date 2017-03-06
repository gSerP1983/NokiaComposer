package com.serp1983.nokiacomposer.logic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.firebase.crash.FirebaseCrash;
import com.serp1983.nokiacomposer.lib.AsyncWaveWriter;
import com.serp1983.nokiacomposer.lib.PCMConverter;
import com.serp1983.nokiacomposer.lib.ShortArrayList;
import com.intervigil.wave.WaveWriter;
import com.singlecellsoftware.mp3convert.ConvertActivity;

import java.io.File;

public class ShareHelper {

    public static void shareText(Context context, RingtoneVM ringtone){
        if (ringtone == null)
            return;

        try{
            Intent intentSend = new Intent(Intent.ACTION_SEND);
            intentSend.setType("text/plain");
            intentSend.putExtra(Intent.EXTRA_TEXT, ringtone.Name + ", tempo=" + ringtone.Tempo + ", " + ringtone.Code);
            intentSend.putExtra(Intent.EXTRA_SUBJECT, ringtone.Name);

            Intent intentChooser = Intent.createChooser(intentSend, "Share");
            intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intentChooser);
        }
        catch(Exception e){
            e.printStackTrace();
            FirebaseCrash.log("ShareHelper.shareText(...)");
            FirebaseCrash.report(e);
        }
    }

    public static void shareWav(Context context, RingtoneVM ringtone){
        if (ringtone == null)
            return;

        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null)
            return;

        try{
            String name = ringtone.Name.replaceAll("\\W+", "_");
            File file = new File(externalCacheDir.getPath(), name + ".wav");

            ShortArrayList pcm = PCMConverter.getInstance().convert(ringtone.Code, ringtone.Tempo);
            WaveWriter writer = new WaveWriter(file, 44100, 1, 16);
            AsyncWaveWriter.execute(writer, pcm.toArray(), null, null);

            Intent intentSend = new Intent(Intent.ACTION_SEND);
            intentSend.setType("sound/wav");
            intentSend.putExtra(Intent.EXTRA_SUBJECT, ringtone.Name);
            intentSend.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

            Intent intentChooser = Intent.createChooser(intentSend, "Share *.wav");
            intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intentChooser);
        }
        catch(Exception e){
            e.printStackTrace();
            FirebaseCrash.log("ShareHelper.shareWav(...)");
            FirebaseCrash.report(e);
        }
    }

    public static void shareMp3(Context context, RingtoneVM ringtone){
        if (ringtone == null)
            return;

        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null)
            return;

        try{
            String name = ringtone.Name.replaceAll("\\W+", "_");
            final File fileWav = new File(externalCacheDir.getPath(), name + ".wav");

            ShortArrayList pcm = PCMConverter.getInstance().convert(ringtone.Code, ringtone.Tempo);
            WaveWriter writer = new WaveWriter(fileWav, 44100, 2, 16);
            AsyncWaveWriter.execute(writer, pcm.toArray(), pcm.toArray(), new AsyncWaveWriter.Callback() {
                @Override
                public void onComplete() {
                    ConvertActivity.nativeEncodeMP3(fileWav.getAbsolutePath(), 44100, 1);
                }
            });

            final File fileMp3 = new File(externalCacheDir.getPath(), name + ".mp3");
            Intent intentSend = new Intent(Intent.ACTION_SEND);
            intentSend.setType("sound/mp3");
            intentSend.putExtra(Intent.EXTRA_SUBJECT, ringtone.Name);
            intentSend.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileMp3));

            Intent intentChooser = Intent.createChooser(intentSend, "Share *.mp3");
            intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intentChooser);
        }
        catch(Exception e){
            e.printStackTrace();
            FirebaseCrash.log("ShareHelper.shareMp3(...)");
            FirebaseCrash.report(e);
        }
    }
}
