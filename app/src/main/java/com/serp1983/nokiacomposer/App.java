package com.serp1983.nokiacomposer;

import android.app.Application;
import com.serp1983.nokiacomposer.logic.DataService;
import com.serp1983.nokiacomposer.util.InterstitialAdService;
import com.serp1983.nokiacomposer.util.RewardedVideoAdService;

public class App extends Application {
    public void onCreate() {
        super.onCreate();
        DataService.initialize(this);
    }
}
