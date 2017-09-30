package com.serp1983.nokiacomposer;

import android.app.Application;
import android.content.Context;

import com.serp1983.nokiacomposer.logic.DataService;
import com.serp1983.nokiacomposer.logic.FirebaseDatabaseService;


public class App extends Application {

    private static Context context;
    public static Context getAppContext() {
        return context;
    }

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        DataService.initialize(this);
        FirebaseDatabaseService.initialize();
    }
}
