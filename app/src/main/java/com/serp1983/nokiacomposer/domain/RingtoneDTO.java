package com.serp1983.nokiacomposer.domain;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class RingtoneDTO {
    public String Name;
    public String Code;
    public int Tempo;

    public RingtoneDTO(){}

    public RingtoneDTO(String name, String code, int tempo){
        Name = name;
        Code = code;
        Tempo = tempo;
    }
}
