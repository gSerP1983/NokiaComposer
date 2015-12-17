package com.singlecellsoftware.mp3convert;

/**
 * Created by Serp on 23.11.2015.
 */
public class ConvertActivity {
    static
    {
        System.loadLibrary("mp3lame");
    }

    public static native void nativeEncodeMP3(String s, int i, int j);
    public static native void cancelEncoding();
}
