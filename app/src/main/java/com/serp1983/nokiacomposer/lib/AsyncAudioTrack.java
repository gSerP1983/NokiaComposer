package com.serp1983.nokiacomposer.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.google.firebase.crash.FirebaseCrash;

public class AsyncAudioTrack implements Runnable {
	private static AsyncAudioTrack instance;
	public static  Boolean isRun;

	private byte[] _buffer;
	private AudioTrack _audioTrack;
	private Callback _callback;
	private int _bufferSize;
	public interface Callback {
		void onComplete();
	}

	private AsyncAudioTrack(byte[] buffer, Callback callback){
		_buffer = buffer;
		_callback = callback;

		try {
			_bufferSize = AudioTrack.getMinBufferSize(44100,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
		}
		catch(Exception e){
			e.printStackTrace();
			_bufferSize = 3528;
		}

		if (_bufferSize == -1)
			_bufferSize = 3528;
	}
	
	@Override
	public void run() {
		isRun = true;

		_audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC, 44100,
				AudioFormat.CHANNEL_OUT_MONO, 
				AudioFormat.ENCODING_PCM_16BIT, 
				_bufferSize, AudioTrack.MODE_STREAM);
		
		_audioTrack.play();
		_audioTrack.write(_buffer, 0, _buffer.length);

		if (_callback != null)
			_callback.onComplete();

		isRun = false;
	}

	private void release(){
		if (_audioTrack != null) {
			_audioTrack.release();
			_audioTrack = null;
		}
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
