package com.serp1983.nokiacomposer;

import android.app.Application;
// import android.content.Context;

import com.serp1983.nokiacomposer.logic.DataService;

public class App extends Application {
    //private static Context mContext;

    public void onCreate() {
        super.onCreate();
        //mContext = getApplicationContext();
        DataService.initialize(this);
    }

    //public static Context getAppContext() {
    //    return mContext;
    //}
}
