package com.serp1983.nokiacomposer.domain;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.serp1983.nokiacomposer.BR;

public class Note extends BaseObservable {
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

    @Override
    public String toString() {
        return mDuration
                + (mDottedDuration ? "." : "")
                + mNote
                + (mOctave == null ? "" : mOctave);
    }

}
