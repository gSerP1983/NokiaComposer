package com.serp1983.nokiacomposer.logic;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.serp1983.nokiacomposer.domain.RingtoneDTO;
import com.serp1983.nokiacomposer.domain.RingtoneVM;

public class FirebaseDatabaseService {
    public static void initialize(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        DatabaseReference databaseRef = database.getReference();
        databaseRef.keepSynced(true);
    }

    public static DatabaseReference getReference(){
        return FirebaseDatabase.getInstance().getReference();
    }

    public static void add(RingtoneDTO ringtone){
        if (ringtone == null || ringtone.Code.length() < 40)
            return;
        if (ringtone.Name.startsWith("(My)"))
            return;
        String code = ringtone.Code.substring(10, 40).replaceAll("\\s", "").toUpperCase();
        for(RingtoneVM assetRingtone : DataService.getInstance().getAssetRingtones()) {
            String assetCode = assetRingtone.getCode().replaceAll("\\s", "").toUpperCase();
            if (assetCode.contains(code))
                return;
        }

        FirebaseDatabaseService.getReference().push().setValue(ringtone);
    }

}
