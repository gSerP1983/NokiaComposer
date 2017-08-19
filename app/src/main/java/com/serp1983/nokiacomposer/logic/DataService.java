package com.serp1983.nokiacomposer.logic;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.databinding.ObservableArrayList;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.serp1983.nokiacomposer.domain.RingtoneVM;
import com.serp1983.nokiacomposer.lib.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;

public class DataService {
    private static DataService ourInstance;
    public static DataService getInstance() {
        return ourInstance;
    }

    public static void initialize(ContextWrapper context){
        // my ringtones store in private dir now
        try {
            File oldFile = getMyRingtonesFileOld(context);
            File newFile = getMyRingtonesFile(context);
            if (oldFile != null && oldFile.exists()) {
                FileUtils.copy(oldFile, newFile);
                oldFile.delete();
            }
        }
        catch(Exception ignore){
            // ignore
        }

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
            FirebaseCrash.report(e);
        }
        if (assetRingtonesArray == null)
            assetRingtonesArray = new RingtoneVM[]{};
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
            FirebaseCrash.report(e);
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
            FirebaseCrash.report(e);
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
            FirebaseCrash.report(e);
            return false;
        }
        return true;
    }

    public void saveMyRingtones(Context context){
        try{
            saveMyRingtones(context, myRingtones);
        }
        catch(Exception e){
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
    }

    public RingtoneVM findMyRingtone(int tempo, String name, String code){
        for(RingtoneVM ring : myRingtones)
            if (ring.getTempo() == tempo && ring.getName().equals(name) && ring.getCode().equals(code))
                return ring;
        return null;
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

    private static File getMyRingtonesFileOld(Context context) throws IOException {
        File outputDir = context.getExternalCacheDir();
        if (outputDir == null)
            return null;
        return new File(outputDir.getPath(), "ringtones.json");
    }

    private static File getMyRingtonesFile(Context context) throws IOException {
        File outputDir = context.getDir("data", Context.MODE_PRIVATE);
        if (outputDir == null)
            return null;
        return new File(outputDir.getPath(), "ringtones.json");
    }
}
