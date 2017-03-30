package com.serp1983.nokiacomposer.domain;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.annotations.SerializedName;
import com.serp1983.nokiacomposer.BR;
import com.serp1983.nokiacomposer.lib.AsyncAudioTrack;
import com.serp1983.nokiacomposer.lib.PCMConverter;
import com.serp1983.nokiacomposer.lib.ShortArrayList;

public class RingtoneVM extends BaseObservable {
    @SerializedName("Name") private String _name;
    @SerializedName("Tempo") private int _tempo = 120;
    @SerializedName("Code") private String _code;
    public boolean IsMy = false;
    private boolean _isPlaying = false;

    @Bindable
    public String getName() {
        return _name;
    }
    public void setName(String name) {
        _name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public int getTempo() {
        return _tempo;
    }
    public void setTempo(int tempo) {
        _tempo = tempo;
        notifyPropertyChanged(BR.tempo);
    }

    @Bindable
    public boolean isPlaying() {
        return _isPlaying;
    }
    private void setPlaying(boolean playing) {
        _isPlaying = playing;
        notifyPropertyChanged(BR.playing);
    }

    public String getCode() {
        return _code;
    }
    public void setCode(String code) { _code = code; }

    public RingtoneVM(String name, int tempo, String code){
        this._name = name;
        this._code = code;
        this._tempo = tempo;
    }

    @Override
    public String toString() {
        return _name;
    }

    public void play(){
        try {
            if (!isPlaying()) {
                setPlaying(true);
                ShortArrayList pcm = PCMConverter.getInstance().convert(this.getCode(), this.getTempo());
                AsyncAudioTrack.start(PCMConverter.shorts2Bytes(pcm), new AsyncAudioTrack.Callback() {
                    @Override
                    public void onComplete() {
                        setPlaying(false);
                    }
                });
            }
            else
                AsyncAudioTrack.stop();
        }
        catch(Exception e){
            e.printStackTrace();
            FirebaseCrash.report(e);
            setPlaying(false);
        }
    }
}
