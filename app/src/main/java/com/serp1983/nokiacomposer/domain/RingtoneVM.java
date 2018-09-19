package com.serp1983.nokiacomposer.domain;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import com.google.gson.annotations.SerializedName;
import com.serp1983.nokiacomposer.BR;
import com.serp1983.nokiacomposer.lib.AsyncAudioTrack;
import com.serp1983.nokiacomposer.lib.PCMConverter;
import com.serp1983.nokiacomposer.lib.ShortArrayList;
import com.serp1983.nokiacomposer.util.AppLog;

import java.util.Comparator;

public class RingtoneVM extends BaseObservable {
    @SerializedName("Name") private String _name;
    @SerializedName("Tempo") private int _tempo = 120;
    @SerializedName("Code") private String _code;
    public boolean IsMy = false;
    private boolean _isPlaying = false;
    private String _key  = "";

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

    public String getKey() {
        return _key;
    }

    public RingtoneVM(String name, int tempo, String code){
        this._name = name;
        this._code = code;
        this._tempo = tempo;
    }

    public RingtoneVM(String key, RingtoneDTO dto){
        this._key = key;
        this._name = dto.Name;
        this._code = dto.Code;
        this._tempo = dto.Tempo;
    }

    @Override
    public String toString() {
        return _name;
    }

    protected void play(String code, int tempo){
        try {
            if (!isPlaying()) {
                setPlaying(true);
                ShortArrayList pcm = PCMConverter.getInstance().convert(code, tempo);
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
            AppLog.Error(e);
            setPlaying(false);
        }
    }

    public void play(){
        play(this.getCode(), this.getTempo());
    }

    public RingtoneDTO getRingtoneDTO(){
        return new RingtoneDTO(this.getName(), this.getCode(), this.getTempo());
    }

    public static Comparator<RingtoneVM> COMPARE_BY_NEW = new Comparator<RingtoneVM>() {
        public int compare(RingtoneVM one, RingtoneVM other) {
            return -(one.getKey().compareTo(other.getKey()));
        }
    };

    public static Comparator<RingtoneVM> COMPARE_BY_Name = new Comparator<RingtoneVM>() {
        public int compare(RingtoneVM one, RingtoneVM other) {
            return one.getName().toUpperCase().compareTo(other.getName().toUpperCase());
        }
    };
}
