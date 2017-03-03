package com.serp1983.nokiacomposer.logic;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class DataService {
    private static DataService ourInstance;
    public static DataService getInstance() {
        return ourInstance;
    }

    public static void initialize(ContextWrapper context){
        ourInstance = new DataService(context);
    }

    private RingtoneVM[] assetRingtones;
    private RingtoneVM[] myRingtones;

    private DataService(ContextWrapper context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream ims = assetManager.open("Ringtones.json");
            Reader reader = new InputStreamReader(ims);
            assetRingtones = new Gson().fromJson(reader, RingtoneVM[].class);
        }catch(Exception e) {
            e.printStackTrace();
        }
        if (assetRingtones == null)
            assetRingtones = new RingtoneVM[]{};
        else
            RingtoneVM.sort(assetRingtones);

        try {
            File file = getMyRingtonesFile(context);
            if (file != null && file.exists()) {
                FileReader reader = new FileReader(file);
                myRingtones = new Gson().fromJson(reader, RingtoneVM[].class);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        if (myRingtones == null)
            myRingtones = new RingtoneVM[]{};
        else
            RingtoneVM.sort(myRingtones);
    }

    public RingtoneVM[] getAssetRingtones(){
        return assetRingtones;
    }

    public RingtoneVM[] getMyRingtones(){
        return myRingtones;
    }

    public Boolean deleteMyRingtone(Context context, RingtoneVM ringtone){
        if (!ringtone.IsMy)
            return false;

        try {
            RingtoneVM[] rigtones = delete(myRingtones, ringtone);
            saveMyRingtones(context, rigtones);
            myRingtones = rigtones;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Boolean saveMyRingtone(Context context, RingtoneVM ringtone){
        try{
            RingtoneVM[] rigtones = append(myRingtones, ringtone);
            saveMyRingtones(context, rigtones);
            myRingtones = rigtones;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void saveMyRingtones(Context context, RingtoneVM[] rigtones) throws IOException {
        File file = getMyRingtonesFile(context);
        if (file == null)
            return;

        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(new Gson().toJson(rigtones).getBytes());
        outputStream.close();
    }

    private static File getMyRingtonesFile(Context context) throws IOException {
        File outputDir = context.getExternalCacheDir();
        if (outputDir == null)
            return null;
        return new File(outputDir.getPath(), "ringtones.json");
    }

    private static <T> T[] append(T[] array, T value) {
        T[] result = Arrays.copyOf(array, array.length + 1);
        result[result.length - 1] = value;
        return result;
    }

    private static <T> T[] delete(T[] array, T value) {
        ArrayList<T> x = new ArrayList<>(Arrays.asList(array));
        x.remove(value);
        return x.toArray((T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1));
    }
}
