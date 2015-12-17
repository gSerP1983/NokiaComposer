package com.serp1983.nokiacomposer;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Serp on 08.11.2015.
 */
public class RingtoneVM {
    public String Name;
    public String Code;
    public int Tempo;
    public Boolean IsMy = false;

    public RingtoneVM(String name, int tempo, String code){
        this.Name = name;
        this.Code = code;
        this.Tempo = tempo;
    }

    @Override
    public String toString() {
        return Name;
    }

    public static void sort(RingtoneVM[] ringtones){
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
