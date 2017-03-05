package com.serp1983.nokiacomposer.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.google.android.gms.ads.AdRequest;

public class ActivityHelper {
    public static AdRequest.Builder getAdBuilder(){
        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("719114FF69D7B57C496D44E00E4D2324")  // lenovo p7800
                .addTestDevice("75BA8C6263CDF31C6E0BD9C3C90C7B4C")  // sony e2033
                ;
    }

    public static void rate(Context context) {
        if (!startInMarket(context))
            startInBrowser(context);
    }

    private static boolean startInMarket(Context context){
        return startActivity(context, Uri.parse("market://details?id=" + context.getPackageName()));
    }

    private static boolean startInBrowser(Context context){
        return startActivity(context, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName()));
    }

    private static boolean startActivity(Context context, Uri uri){
        boolean result = true;

        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        catch(Exception e){
            result = false;
        }

        return result;
    }

}
