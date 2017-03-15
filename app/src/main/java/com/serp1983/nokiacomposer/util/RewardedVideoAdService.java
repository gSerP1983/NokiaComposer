package com.serp1983.nokiacomposer.util;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.crash.FirebaseCrash;
import com.serp1983.nokiacomposer.R;

public class RewardedVideoAdService implements RewardedVideoAdListener {
    private RewardedVideoAd ad;
    private Context context;

    public RewardedVideoAdService(Context context){
        this.context = context;
        ad = MobileAds.getRewardedVideoAdInstance(context);
        ad.setRewardedVideoAdListener(this);
        ad.loadAd(context.getString(R.string.ads_rewarded_video), ActivityHelper.getAdBuilder().build());
    }

    private void show() {
        if (ad.isLoaded()) {
            ad.show();
        }
    }

    public void tryShow() {
        try{
            show();
        }
        catch(Exception e){
            e.printStackTrace();
            FirebaseCrash.report(e);
        }
    }

    public boolean isLoaded() {
        return ad.isLoaded();
    }

    public void resume() {
        ad.resume(context);
    }

    public void pause() {
        ad.pause(context);
    }

    public void destroy() {
        ad.destroy(context);
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        ad.loadAd(context.getString(R.string.ads_interstitial), ActivityHelper.getAdBuilder().build());
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        // todo disable ads here
        Toast.makeText(context, context.getString(R.string.msg_thank_you), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }
}
