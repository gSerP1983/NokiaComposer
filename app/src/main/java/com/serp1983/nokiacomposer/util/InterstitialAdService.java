package com.serp1983.nokiacomposer.util;

import android.content.Context;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.serp1983.nokiacomposer.R;

public class InterstitialAdService {
    private InterstitialAd interstitial;
    private static InterstitialAdService instance;
    public static InterstitialAdService getInstance(){
        return instance;
    }

    private int count = 0;
    public static void initialize(Context context){
        instance = new InterstitialAdService(context);
    }

    private InterstitialAdService(Context context){
        interstitial = new InterstitialAd(context);
        interstitial.setAdUnitId(context.getString(R.string.ads_interstitial));
        interstitial.loadAd(ActivityHelper.getAdBuilder().build());

        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                interstitial.loadAd(ActivityHelper.getAdBuilder().build());
            }
        });
    }

    public void show() {
        if ((++count) % 26 == 0 && interstitial.isLoaded()) {
            interstitial.show();
        }
    }

}
