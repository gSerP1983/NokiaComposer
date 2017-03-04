package com.serp1983.nokiacomposer.logic;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.databinding.ObservableArrayList;

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

    private ArrayList<RingtoneVM> assetRingtones;
    private ArrayList<RingtoneVM> myRingtones;

    private DataService(ContextWrapper context) {
        // pre-installed ringtones
        RingtoneVM[] assetRingtonesArray = null;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream ims = assetManager.open("Ringtones.json");
            Reader reader = new InputStreamReader(ims);
            assetRingtonesArray = new Gson().fromJson(reader, RingtoneVM[].class);
        }catch(Exception e) {
            e.printStackTrace();
        }
        if (assetRingtonesArray == null)
            assetRingtonesArray = new RingtoneVM[]{};
        else
            RingtoneVM.sort(assetRingtonesArray);
        assetRingtones = new ArrayList<>(Arrays.asList(assetRingtonesArray));

        // my ringtones
        RingtoneVM[] myRingtonesArray = null;
        try {
            File file = getMyRingtonesFile(context);
            if (file != null && file.exists()) {
                FileReader reader = new FileReader(file);
                myRingtonesArray = new Gson().fromJson(reader, RingtoneVM[].class);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        if (myRingtonesArray == null)
            myRingtonesArray = new RingtoneVM[]{};
        myRingtones = new ObservableArrayList<>();
        myRingtones.addAll(Arrays.asList(myRingtonesArray));
    }

    public ArrayList<RingtoneVM> getAssetRingtones(){return assetRingtones;}

    public ArrayList<RingtoneVM> getMyRingtones(){
        return myRingtones;
    }

    public Boolean deleteMyRingtone(Context context, RingtoneVM ringtone){
        if (!ringtone.IsMy)
            return false;

        try {
            myRingtones.remove(ringtone);
            saveMyRingtones(context, myRingtones);
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Boolean addMyRingtone(Context context, RingtoneVM ringtone){
        if (!ringtone.IsMy)
            return false;

        try{
            myRingtones.add(ringtone);
            saveMyRingtones(context, myRingtones);
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void saveMyRingtones(Context context, ArrayList<RingtoneVM> rigtones) throws IOException {
        RingtoneVM[] array = rigtones.toArray(new RingtoneVM[0]);
        File file = getMyRingtonesFile(context);
        if (file == null)
            return;

        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(new Gson().toJson(array).getBytes());
        outputStream.close();
    }

    private static File getMyRingtonesFile(Context context) throws IOException {
        File outputDir = context.getExternalCacheDir();
        if (outputDir == null)
            return null;
        return new File(outputDir.getPath(), "ringtones.json");
    }
}
