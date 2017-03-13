package com.serp1983.nokiacomposer.logic;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.annotations.SerializedName;
import com.serp1983.nokiacomposer.BR;
import com.serp1983.nokiacomposer.lib.AsyncAudioTrack;
import com.serp1983.nokiacomposer.lib.PCMConverter;
import com.serp1983.nokiacomposer.lib.ShortArrayList;
import com.serp1983.nokiacomposer.util.InterstitialAdService;


public class RingtoneVM extends BaseObservable {
    @SerializedName("Name") private String _name;
    public String Code;
    public int Tempo;
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
    public boolean isPlaying() {
        return _isPlaying;
    }
    private void setPlaying(boolean playing) {
        _isPlaying = playing;
        notifyPropertyChanged(BR.playing);
    }

    public RingtoneVM(String name, int tempo, String code){
        this._name = name;
        this.Code = code;
        this.Tempo = tempo;
    }

    @Override
    public String toString() {
        return _name;
    }

    public void play(){
        InterstitialAdService.getInstance().tryShow();

        try {
            if (!_isPlaying) {
                setPlaying(true);
                ShortArrayList pcm = PCMConverter.getInstance().convert(this.Code, this.Tempo);
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
