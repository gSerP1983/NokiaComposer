package com.serp1983.nokiacomposer;

import android.app.Application;
import com.serp1983.nokiacomposer.logic.DataService;
import com.serp1983.nokiacomposer.logic.FirebaseDatabaseService;


public class App extends Application {
    public void onCreate() {
        super.onCreate();
        DataService.initialize(this);
        FirebaseDatabaseService.initialize();

        /*FirebaseDatabaseService.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
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
        });*/
    }
}
