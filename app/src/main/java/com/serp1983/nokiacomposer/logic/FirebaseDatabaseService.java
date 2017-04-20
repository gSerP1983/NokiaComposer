package com.serp1983.nokiacomposer.logic;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.serp1983.nokiacomposer.domain.RingtoneDTO;

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
        if (ringtone == null || ringtone.Code.length() < 50)
            return;
        FirebaseDatabaseService.getReference().push().setValue(ringtone);
    }

}
