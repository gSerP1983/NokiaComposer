package com.serp1983.nokiacomposer.lib;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.serp1983.nokiacomposer.App;

public class SamplingType {
    private static String SamplingTypePrefName = "SamplingTypePref";
    static int Default = -1;

    private static double sin(float freq, int i){
        return Math.sin(2 * Math.PI * freq * i / PCMConverter.SAMPLING_FREQUENCY);
    }

    private static double meander(float freq, int i){
        double wt = 2 * Math.PI * freq * i / PCMConverter.SAMPLING_FREQUENCY;
        return 2 * (Math.sin(wt) + Math.sin(3 * wt) / 3f + Math.sin(5 * wt) / 5f
                //+ Math.sin(7 * wt) / 7f + Math.sin(9 * wt) / 9f
        ) / Math.PI
                ;
    }

    private static double saw(float freq, int i){
        double wt = 2 * Math.PI * freq * i / PCMConverter.SAMPLING_FREQUENCY;
        return 2 * (Math.sin(wt) - Math.sin(2 * wt) / 2f + Math.sin(3 * wt) / 3f
               // - Math.sin(4 * wt) / 4f + Math.sin(5 * wt) / 5f
        ) / Math.PI
                ;
    }

    static double getValByTime(int samplingType, float freq, int i) {
        if (samplingType == -1)
            samplingType = getSamplingType();

        if (samplingType == -1 || samplingType == 0)
            return sin(freq, i);

        if (samplingType == 1)
            return meander(freq, i);

        if (samplingType == 2)
            return saw(freq, i);

        return sin(freq, i);
    }

    public static int getSamplingType() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        return prefs.getInt(SamplingTypePrefName, 2);
    }

    public static void setSamplingType(int samplingType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
        prefs.edit().putInt(SamplingTypePrefName, samplingType).apply();
    }
}
