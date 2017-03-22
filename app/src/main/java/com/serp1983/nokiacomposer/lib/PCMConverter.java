package com.serp1983.nokiacomposer.lib;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PCMConverter {
	
	private static PCMConverter instance;	
	public static PCMConverter getInstance(){	
		if (instance == null) return instance = new PCMConverter();
		return instance;
	}
	
	public static byte[] shorts2Bytes(ShortArrayList pcm){	
		byte[] array = new byte[pcm.size() * 2];
		int i = 0;
		for(short data : pcm.toArray()){
			array[i++] = (byte) (data & 0xff);
			array[i++] = (byte) ((data >>> 8) & 0xff);
		}
		return array;
	}

	private final Map<String, Float> notes = new HashMap<>();
	private final Map<String, String> tones = new HashMap<>();
	private final Pattern regexPattern;

	private PCMConverter(){
		notes.put("-", 0f);
		notes.put("C", 261.626f); 
		notes.put("#C", 277.183f); 
		notes.put("D", 293.665f); 
		notes.put("#D", 311.127f); 
		notes.put("E", 329.628f); 
		notes.put("#E", 349.228f); 
		notes.put("F", 349.228f); 
		notes.put("#F", 369.994f); 
		notes.put("G", 391.995f); 
		notes.put("#G", 415.305f); 
		notes.put("A", 440.000f); 
		notes.put("#A", 466.164f); 
		notes.put("B", 493.883f); 
		notes.put("#B", 523.251f);

		tones.put("-", "0");
		tones.put("C", "1");
		tones.put("#C", "1#");
		tones.put("D", "2");
		tones.put("#D", "2#");
		tones.put("E", "3");
		tones.put("#E", "3#");
		tones.put("F", "4");
		tones.put("#F", "4#");
		tones.put("G", "5");
		tones.put("#G", "5#");
		tones.put("A", "6");
		tones.put("#A", "6#");
		tones.put("B", "7");
		tones.put("#B", "7#");

		String pattern = "^(\\d{1,2})[.]?(#?[A-G]|[A-G]#?)(\\d)$";
		regexPattern = Pattern.compile(pattern);
	}
	
	private void appendNote(ShortArrayList pcm, float volume, int time, String note, int octave){
		float FREQ = (float) (notes.get(note) * Math.pow(2, octave-1));
		double KFREQ = 0;
		short value;
		int i;
		
		for(i = 0; i < 44100 * time / 1000; i++){			
			KFREQ = Math.sin(6.28f * FREQ * i / 44100f);
			value = (short) (32765f * volume * KFREQ);
			pcm.add(value);
		}
		
		// making clear sound
		if (Math.abs(KFREQ)>0.1f){
			while (Math.abs(KFREQ)>0.1f){				
				KFREQ = Math.sin(6.28f * FREQ * i / 44100f);
				value = (short) (32765f * volume * KFREQ);
				pcm.add(value);	
				i++;
			}
		}
	}
	
	public ShortArrayList convert(String nokiaCodes, float tempo /*120*/){
		return convert(nokiaCodes, tempo, 1f);
	}
	
	@SuppressLint("DefaultLocale")
	private ShortArrayList convert(String nokiaCodes, float tempo /*120*/, float volume /*1*/){
		ShortArrayList pcm = new ShortArrayList();		
		String[] tokens = nokiaCodes.toUpperCase().split(" ");
		
		for(String token : tokens){
			float duration = 0f;
			String note = "-";
			int octave = 1;
			
			if (token.endsWith("-")){
				String str = token.substring(0, token.length() - 1);
				if (str.endsWith("."))
					str = str.substring(0, str.length() - 1);
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
			
			if (token.contains("."))
				duration = duration / 1.5f;
			
			float time = 32f / duration; 			
			appendNote(pcm, volume, (int) (time * 1000f * 7.5f / tempo), note, octave);
		}
		
		appendNote(pcm, 0, (int) (250 * 7.5 / tempo), "-", 1);
		
		return pcm;
	}

	public String convert2Keys(String nokiaCodes){
		String[] tokens = nokiaCodes.toUpperCase().split(" ");
		List<String> result = new ArrayList<>();

		int prevDuration = 4;
		int prevOctave = 1;
		for(String token : tokens){
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

			String keyForNote= tones.get(note);
			if (token.contains("."))
				keyForNote = "(" + keyForNote + ")";

			result.add(keyForNote
					+ getKeysForDuration(prevDuration, duration)
					+ getKeysForOctave(note, prevOctave, octave)
			);

			if (!"-".equals(note)) {
				prevDuration = duration;
				prevOctave = octave;
			}
		}

		StringBuilder strBuilder = new StringBuilder();
		for (String token : result) {
			strBuilder.append(token);
			strBuilder.append(", ");
		}

		return strBuilder.toString();
	}

    public static boolean isNumeric(String str)
    {
        for (char c : str.toCharArray())
            if (!Character.isDigit(c))
                return false;

        return true;
    }

	private static String getKeysForDuration(int prevDuration, int duration){
		if (prevDuration == duration)
			return "";

		int divDuration = duration > prevDuration ? duration / prevDuration : prevDuration / duration;
		int log2Duration = 1;
		if (divDuration == 2) log2Duration = 1;
		if (divDuration == 4) log2Duration = 2;
		if (divDuration == 8) log2Duration = 3;
		if (divDuration == 16) log2Duration = 4;
		if (divDuration == 32) log2Duration = 5;

		String tone = duration > prevDuration ? "8" : "9";
		String res = "";
		for (int i = 0; i < log2Duration; i++)
			res += tone;

		return res;
	}

	private static String getKeysForOctave(String note, int prevOctave, int octave) {
		if ("-".equals(note) || octave == prevOctave)
			return "";

		int count = 0;
		if (prevOctave == 1 && octave == 2) count = 1;
		if (prevOctave == 1 && octave == 3) count = 2;
		if (prevOctave == 2 && octave == 1) count = 2;
		if (prevOctave == 2 && octave == 3) count = 1;
		if (prevOctave == 3 && octave == 1) count = 1;
		if (prevOctave == 3 && octave == 2) count = 2;

		String res = "";
		for (int i = 0; i < count; i++)
			res += "*";

		return res;
	}
}
