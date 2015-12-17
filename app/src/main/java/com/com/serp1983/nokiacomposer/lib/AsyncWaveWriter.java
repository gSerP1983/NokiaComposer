package com.com.serp1983.nokiacomposer.lib;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.intervigil.wave.WaveWriter;

public class AsyncWaveWriter implements Runnable {
	
	public static void execute(WaveWriter writer, short[] pcm1stChanel, short[] pcm2ndChanel, Callback callback){
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Runnable worker = new AsyncWaveWriter(writer, pcm1stChanel, pcm2ndChanel, callback);
        executor.execute(worker);
        executor.shutdown();
	}

	WaveWriter _writer;
	short[] pcm1stChanel;
	short[] pcm2ndChanel;
	private Callback _callback;
	public interface Callback {
		void onComplete();
	}

	private AsyncWaveWriter(WaveWriter writer, short[] pcm1stChanel, short[] pcm2ndChanel, Callback callback){
		_writer = writer;
		this.pcm1stChanel = pcm1stChanel;
		this.pcm2ndChanel = pcm2ndChanel;
		_callback = callback;
	}
	
	@Override
	public void run() {
		try {
			_writer.createWaveFile();
			if (pcm2ndChanel == null)
				_writer.write(pcm1stChanel, 0, pcm1stChanel.length);
			else
				_writer.write(pcm1stChanel, pcm2ndChanel, 0, pcm1stChanel.length);
			_writer.closeWaveFile();

			if (_callback != null)
				_callback.onComplete();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
