package com.serp1983.nokiacomposer.util;

import com.google.android.gms.ads.AdRequest;

public class ActivityHelper {
    public static AdRequest.Builder getAdBuilder(){
        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("719114FF69D7B57C496D44E00E4D2324")  // lenovo p7800
                .addTestDevice("75BA8C6263CDF31C6E0BD9C3C90C7B4C")  // sony e2033
                ;
    }
}
