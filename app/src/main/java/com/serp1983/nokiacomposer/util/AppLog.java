package com.serp1983.nokiacomposer.util;

public class AppLog {
    public static void Error(Throwable e){
        // FirebaseCrash.report(e);
        e.printStackTrace();
    }
}
