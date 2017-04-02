package com.serp1983.nokiacomposer.domain;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.serp1983.nokiacomposer.BR;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Note extends BaseObservable {

    private static final Pattern regexPattern;
    static{
        String pattern = "^(\\d{1,2})[.]?(#?[A-G]|[A-G]#?)(\\d)$";
        regexPattern = Pattern.compile(pattern);
    }

    @Bindable
    public int getDuration() {
        return mDuration;
    }
    public void setDuration(int mDuration) {
        this.mDuration = mDuration;
        notifyPropertyChanged(BR.duration);
    }

    @Bindable
    public String getNote() {
        return mNote;
    }
    public void setNote(String mNote) {
        this.mNote = mNote;
        notifyPropertyChanged(BR.note);
    }

    @Bindable
    public Integer getOctave() {
        return mOctave;
    }
    public void setOctave(Integer mOctave) {
        this.mOctave = mOctave;
        notifyPropertyChanged(BR.octave);
    }

    private int mDuration;
    private String mNote;
    private Integer mOctave;
    private boolean mDottedDuration;

    public Note(int duration, boolean dottedDuration, String note, Integer octave) {
        this.mDuration = duration;
        this.mNote = note;
        this.mOctave = octave;
        this.mDottedDuration = dottedDuration;
    }

    public Note(String token){
        int duration = 4;
        int octave = 1;
        String note = "-";

        if (token.endsWith("-")){
            String str = token.substring(0, token.length() - 1);
            if (str.endsWith("."))
                str = str.substring(0, str.length() - 1);

            if (isNumeric(str))
                duration = Integer.parseInt(str);
        }
        else{
            Matcher m = regexPattern.matcher(token);
            if (m.matches()){
                duration = Integer.parseInt(m.group(1));
                note = m.group(2);
                if (note.length() == 2 && note.charAt(1) == '#')
                    note = "#" + note.charAt(0);
                octave = Integer.parseInt(m.group(3));
            }
        }

        this.mDuration = duration;
        this.mNote = note;
        this.mOctave = ("-".equals(note) ? null :  octave);
        this.mDottedDuration = token.contains(".");
    }

    private static boolean isNumeric(String str)
    {
        for (char c : str.toCharArray())
            if (!Character.isDigit(c))
                return false;

        return true;
    }

    @Override
    public String toString() {
        return mDuration
                + (mDottedDuration ? "." : "")
                + mNote
                + (mOctave == null ? "" : mOctave);
    }

    public static String[] getTokens(String nokiaCodes){
        return nokiaCodes
                .replaceAll("\\s+", " ")
                .toUpperCase().split(" ");
    }

}
