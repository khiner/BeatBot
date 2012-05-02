package com.kh.beatbot;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class PlaybackManager {
	private static PlaybackManager singletonInstance = null;
	
	private State state;
	private int[] sampleIDs;
	private int[] streamIDs;
	private SoundPool soundPool;

	public enum State {
		PLAYING, STOPPED
	}
	
	public static PlaybackManager getInstance(Context context, int[] sampleRawResources) {
		if (singletonInstance == null) {
			singletonInstance = new PlaybackManager(context, sampleRawResources);
		}
		return singletonInstance;			
	}
	
	private PlaybackManager(Context context, int[] sampleRawResources) {
		state = State.STOPPED;
		sampleIDs = new int[sampleRawResources.length];
		streamIDs = new int[sampleRawResources.length];
		soundPool = new SoundPool(sampleRawResources.length, AudioManager.STREAM_MUSIC, 0);
		for (int i = 0; i < sampleRawResources.length; i++)
			sampleIDs[i] = soundPool.load(context, sampleRawResources[i], 1);		
	}
		
	public State getState() {
		return state;
	}
	
	public void play() {
		state = State.PLAYING;
	}

	public void stop() {
		state = State.STOPPED;
	}
	
	public void release() {
		state = State.STOPPED;
		soundPool.release();
	}
	
	public void playSample(int sampleNum, int velocity) {
		if (sampleNum >= 0 && sampleNum < sampleIDs.length) {
			float normVelocity = velocity/100f;
			streamIDs[sampleNum] = soundPool.play(sampleIDs[sampleNum], normVelocity, normVelocity, 1, 0, 1f);
			// highlight the icon to indicate playing
			//context.activateIcon(sampleNum);
		}
	}
	
	public void stopSample(int sampleNum) {
		if (sampleNum >= 0 && sampleNum < sampleIDs.length) {
			soundPool.stop(streamIDs[sampleNum]);
			// set the icon back to normal after playing is done			
			//context.deactivateIcon(sampleNum);
		}
	}
	
	public void stopAllSamples() {
		for (int sampleID = 0; sampleID < sampleIDs.length; sampleID++) {
			soundPool.stop(streamIDs[sampleID]);
		}
	}
}
