package com.serp1983.nokiacomposer;

import android.app.Application;
import android.content.Context;
// import android.content.Context;

import com.serp1983.nokiacomposer.logic.DataService;

public class App extends Application {
    public void onCreate() {
        super.onCreate();
        DataService.initialize(this);
    }
}
