package com.serp1983.nokiacomposer;

import android.app.Application;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.serp1983.nokiacomposer.domain.RingtoneDTO;
import com.serp1983.nokiacomposer.logic.DataService;


public class App extends Application {
    public void onCreate() {
        super.onCreate();
        DataService.initialize(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        DatabaseReference databaseRef = database.getReference();
        databaseRef.keepSynced(true);

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    RingtoneDTO ringtone = postSnapshot.getValue(RingtoneDTO.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
    }
}
