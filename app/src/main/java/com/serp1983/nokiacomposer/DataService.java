package com.serp1983.nokiacomposer;

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
import java.util.Collections;

/**
 * Created by Serp on 12.11.15.
 */
public class DataService {
    private static DataService ourInstance;
    public static DataService getInstance() {
        return ourInstance;
    }

    public static void initialize(ContextWrapper context){
        ourInstance = new DataService(context);
    }

    private ContextWrapper context;
    private RingtoneVM[] assetRingtones;
    private RingtoneVM[] myRingtones;

    private DataService(ContextWrapper context) {
        this.context = context;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream ims = assetManager.open("Ringtones.json");
            Reader reader = new InputStreamReader(ims);
            assetRingtones = new Gson().fromJson(reader, RingtoneVM[].class);
        }catch(Exception e) {
            e.printStackTrace();
        }
        if (assetRingtones == null) assetRingtones = new RingtoneVM[]{};

        try {
            File file = getMyRingtonesFile(context);
            if (file != null && file.exists()) {
                FileReader reader = new FileReader(file);
                myRingtones = new Gson().fromJson(reader, RingtoneVM[].class);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        if (myRingtones == null) myRingtones = new RingtoneVM[]{};
    }

    public RingtoneVM[] getAll(){
        RingtoneVM[] allRingtones = concat(assetRingtones, myRingtones);
        RingtoneVM.sort(allRingtones);
        return allRingtones;
    }

    public Boolean deleteMyRingtone(RingtoneVM ringtone){
        if (ringtone.IsMy == null || !ringtone.IsMy) return false;
        try {
            RingtoneVM[] rigtones = delete(myRingtones, ringtone);
            saveMyRingtones(rigtones);
            myRingtones = rigtones;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Boolean saveMyRingtone(RingtoneVM ringtone){
        try{
            RingtoneVM[] rigtones = append(myRingtones, ringtone);
            saveMyRingtones(rigtones);
            myRingtones = rigtones;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void saveMyRingtones(RingtoneVM[] rigtones) throws IOException {
        File file = getMyRingtonesFile(context);
        if (file == null)
            return;

        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(new Gson().toJson(rigtones).getBytes());
        outputStream.close();
    }

    private static File getMyRingtonesFile(Context context) throws IOException {
        File outputDir = context.getExternalCacheDir();
        return new File(outputDir.getPath(), "ringtones.json");
    }

    private static <T> T[] concat(T[]... arrays) {
        ArrayList<T> al = new ArrayList<>();
        for (T[] one : arrays)
            Collections.addAll(al, one);
        return al.toArray(arrays[0].clone());
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
