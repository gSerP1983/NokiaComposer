package com.singlecellsoftware.mp3convert;

public class ConvertActivity {
    static
    {
        System.loadLibrary("mp3lame");
    }

    public static native void nativeEncodeMP3(String s, int i, int j);
    public static native void cancelEncoding();
}
