package com.com.serp1983.nokiacomposer.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AsyncAudioTrack implements Runnable {
	private static AsyncAudioTrack instance;
	public static  Boolean isRun;

	byte[] _buffer;
	AudioTrack _audioTrack;
	private Callback _callback;
	public interface Callback {
		void onComplete();
	}

	private AsyncAudioTrack(byte[] buffer, Callback callback){
		_buffer = buffer;
		_callback = callback;
	}
	
	@Override
	public void run() {
		isRun = true;

		int bufferSize = AudioTrack.getMinBufferSize(
				44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		
		_audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC, 44100,
				AudioFormat.CHANNEL_OUT_MONO, 
				AudioFormat.ENCODING_PCM_16BIT, 
				bufferSize, AudioTrack.MODE_STREAM);
		
		_audioTrack.play();
		_audioTrack.write(_buffer, 0, _buffer.length);

		if (_callback != null)
			_callback.onComplete();

		isRun = false;
	}

	public void release(){
		if (_audioTrack != null) _audioTrack.stop();
	}
	
	public static void start(byte[] buffer, Callback callback){
		stop();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        instance = new AsyncAudioTrack(buffer, callback);
        executor.execute(instance);
        executor.shutdown();
	}
	
	public static void stop(){
		if (instance != null) instance.release();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
