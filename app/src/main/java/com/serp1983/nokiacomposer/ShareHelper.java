package com.serp1983.nokiacomposer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.com.serp1983.nokiacomposer.lib.AsyncWaveWriter;
import com.com.serp1983.nokiacomposer.lib.PCMConverter;
import com.com.serp1983.nokiacomposer.lib.ShortArrayList;
import com.intervigil.wave.WaveWriter;
import com.singlecellsoftware.mp3convert.ConvertActivity;

import java.io.File;

/**
 * Created by Serp on 09.03.2016.
 */
public class ShareHelper {

    public static void shareText(Activity activity, RingtoneVM ringtone){
        if (ringtone == null) return;
        try{
            Intent intentSend = new Intent(Intent.ACTION_SEND);
            intentSend.setType("text/plain");
            intentSend.putExtra(Intent.EXTRA_TEXT, ringtone.Name + ", tempo=" + ringtone.Tempo + ", " + ringtone.Code);
            intentSend.putExtra(Intent.EXTRA_SUBJECT, ringtone.Name);

            Intent intentChooser = Intent.createChooser(intentSend, "Share");
            intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            activity.startActivity(intentChooser);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void shareWav(Activity activity, RingtoneVM ringtone){
        if (ringtone == null) return;

        try{
            File file = new File(activity.getExternalCacheDir().getPath(), "2015nokiacomposer.wav");

            ShortArrayList pcm = PCMConverter.getInstance().convert(ringtone.Code, ringtone.Tempo);
            WaveWriter writer = new WaveWriter(file, 44100, 1, 16);
            AsyncWaveWriter.execute(writer, pcm.toArray(), null, null);

            Intent intentSend = new Intent(Intent.ACTION_SEND);
            intentSend.setType("sound/wav");
            intentSend.putExtra(Intent.EXTRA_SUBJECT, ringtone.Name);
            intentSend.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

            Intent intentChooser = Intent.createChooser(intentSend, "Share *.wav");
            intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            activity.startActivity(intentChooser);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void shareMp3(Activity activity, RingtoneVM ringtone){
        if (ringtone == null) return;

        try{
            final File fileWav = new File(activity.getExternalCacheDir().getPath(), "2015nokiacomposer.wav");

            ShortArrayList pcm = PCMConverter.getInstance().convert(ringtone.Code, ringtone.Tempo);
            WaveWriter writer = new WaveWriter(fileWav, 44100, 2, 16);
            AsyncWaveWriter.execute(writer, pcm.toArray(), pcm.toArray(), new AsyncWaveWriter.Callback() {
                @Override
                public void onComplete() {
                    ConvertActivity.nativeEncodeMP3(fileWav.getAbsolutePath(), 44100, 1);
                }
            });

            final File fileMp3 = new File(activity.getExternalCacheDir().getPath(), "2015nokiacomposer.mp3");
            Intent intentSend = new Intent(Intent.ACTION_SEND);
            intentSend.setType("sound/mp3");
            intentSend.putExtra(Intent.EXTRA_SUBJECT, ringtone.Name);
            intentSend.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileMp3));

            Intent intentChooser = Intent.createChooser(intentSend, "Share *.mp3");
            intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            activity.startActivity(intentChooser);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
