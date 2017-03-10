package com.serp1983.nokiacomposer.logic;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.google.firebase.crash.FirebaseCrash;
import com.serp1983.nokiacomposer.BR;
import com.serp1983.nokiacomposer.lib.AsyncAudioTrack;
import com.serp1983.nokiacomposer.lib.PCMConverter;
import com.serp1983.nokiacomposer.lib.ShortArrayList;
import com.serp1983.nokiacomposer.util.InterstitialAdService;

import java.util.Arrays;
import java.util.Comparator;

public class RingtoneVM extends BaseObservable {
    public String Name;
    public String Code;
    public int Tempo;
    public boolean IsMy = false;

    private boolean _isPlaying = false;

    @Bindable
    public boolean isPlaying() {
        return _isPlaying;
    }
    private void setPlaying(boolean playing) {
        _isPlaying = playing;
        notifyPropertyChanged(BR.playing);
    }

    public RingtoneVM(String name, int tempo, String code){
        this.Name = name;
        this.Code = code;
        this.Tempo = tempo;
    }

    @Override
    public String toString() {
        return Name;
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


    static void sort(RingtoneVM[] ringtones){
        Arrays.sort(ringtones, new Comparator<RingtoneVM>() {
            @Override
            public int compare(RingtoneVM obj1, RingtoneVM obj2) {
                if (obj1 == obj2) {
                    return 0;
                }
                if (obj1 == null) {
                    return -1;
                }
                if (obj2 == null) {
                    return 1;
                }
                String name1 = obj1.Name;
                if (name1 == null) name1 = "";
                String name2 = obj2.Name;
                if (name2 == null) name2 = "";
                return name1.compareTo(name2);
            }
        });
    }
}
