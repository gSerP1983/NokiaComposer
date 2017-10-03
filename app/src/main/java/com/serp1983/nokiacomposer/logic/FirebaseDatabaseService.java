package com.serp1983.nokiacomposer.logic;

import android.content.ContentResolver;
import android.databinding.ObservableArrayList;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.serp1983.nokiacomposer.App;
import com.serp1983.nokiacomposer.domain.RingtoneDTO;
import com.serp1983.nokiacomposer.domain.RingtoneVM;

import java.util.ArrayList;
import java.util.Collections;

public class FirebaseDatabaseService {
    public static ObservableArrayList<RingtoneVM> data = new ObservableArrayList<>();
    public static boolean isModerator = false;

    public static void initialize(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        DatabaseReference databaseRef = database.getReference();
        databaseRef.keepSynced(true);

        FirebaseDatabaseService.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<RingtoneVM> tempData = new ArrayList<>();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    RingtoneDTO dto = postSnapshot.getValue(RingtoneDTO.class);
                    tempData.add(new RingtoneVM(postSnapshot.getKey(), dto));
                }
                Collections.sort(tempData, RingtoneVM.COMPARE_BY_NEW);
                data.addAll(tempData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Throwable e = databaseError.toException();
                FirebaseCrash.report(e);
                e.printStackTrace();
            }
        });

        final FirebaseRemoteConfig cfg = FirebaseRemoteConfig.getInstance();
        cfg.fetch(60 * 60)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            cfg.activateFetched();

                            String moderators = cfg.getString("moderators");
                            if (moderators != null && moderators.contains(getDeviceUniqueID()))
                                isModerator = true;
                        }
                    }
                });
    }

    public static String getDeviceUniqueID(){
        ContentResolver cr = App.getAppContext().getContentResolver();
        return Settings.Secure.getString(cr, Settings.Secure.ANDROID_ID);
    }

    private static DatabaseReference getReference(){
        return FirebaseDatabase.getInstance().getReference();
    }

    public static void add(RingtoneDTO ringtone){
        if (ringtone == null || ringtone.Name.length() < 3 || ringtone.Code.length() < 40)
            return;
        if (ringtone.Name.contains("(My)"))
            return;

        String code = ringtone.Code.substring(10, 40).replaceAll("\\s", "").toUpperCase();
        for(RingtoneVM assetRingtone : DataService.getInstance().getAssetRingtones()) {
            String assetCode = assetRingtone.getCode().replaceAll("\\s", "").toUpperCase();
            if (assetCode.contains(code))
                return;
        }
        if (data != null){
            for(RingtoneVM cloudRingtone : data) {
                String cloudCode = cloudRingtone.getCode().replaceAll("\\s", "").toUpperCase();
                if (cloudCode.contains(code))
                    return;
            }
        }

        ringtone.Name = ringtone.Name.trim();
        try {
            FirebaseDatabaseService.getReference().push().setValue(ringtone);
        }
        catch(Exception e){
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
    }

    public static void delete(RingtoneVM ringtone){
        if (data == null || ringtone == null || ringtone.getKey() == null || ringtone.getKey().isEmpty())
            return;

        data.remove(ringtone);
        try {
            FirebaseDatabaseService.getReference().child(ringtone.getKey()).removeValue();
        }
        catch(Exception e){
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
    }

    public static void setName(RingtoneVM ringtone){
        if (data == null || ringtone == null || ringtone.getKey() == null || ringtone.getKey().isEmpty())
            return;

        try {
            FirebaseDatabaseService.getReference()
                    .child(ringtone.getKey())
                    .child("Name")
                    .setValue(ringtone.getName());
        }
        catch(Exception e){
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
    }
}
