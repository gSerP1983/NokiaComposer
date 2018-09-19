package com.serp1983.nokiacomposer.logic;

import android.databinding.ObservableArrayList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.serp1983.nokiacomposer.domain.RingtoneDTO;
import com.serp1983.nokiacomposer.domain.RingtoneVM;
import com.serp1983.nokiacomposer.util.AppLog;

import java.util.ArrayList;
import java.util.Collections;

public class FirebaseDatabaseService {
    public static ObservableArrayList<RingtoneVM> data = new ObservableArrayList<>();

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
                AppLog.Error(databaseError.toException());
            }
        });
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
            AppLog.Error(e);
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
            AppLog.Error(e);
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
            AppLog.Error(e);
        }
    }
}
