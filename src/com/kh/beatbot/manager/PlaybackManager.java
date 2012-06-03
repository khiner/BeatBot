package com.kh.beatbot.manager;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioTrack;

public class PlaybackManager {
	
	private static PlaybackManager singletonInstance = null;
	private State state;
		
	private String[] sampleNames;
	
	private static final int SAMPLE_RATE = 41000;
	private static final int MIN_BUF_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE,
			AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);				;
	private static byte[] zeroBytes = new byte[MIN_BUF_SIZE];
	
	static {
		for (int i = 0; i < zeroBytes.length; i++) {
			zeroBytes[i] = 0;
		}
	}
	
	public enum State {
		PLAYING, STOPPED
	}
	
	public static PlaybackManager getInstance(Context context, String[] sampleNames) {
		if (singletonInstance == null) {
			singletonInstance = new PlaybackManager(context, sampleNames);
		}
		return singletonInstance;			
	}
	
	private PlaybackManager(Context context, String[] sampleNames) {
		this.sampleNames = sampleNames;
		//audioTracks = new AudioTrack[sampleNames.length];
		//streams = new Stream[sampleNames.length];
		state = State.STOPPED;
	}
	
	public State getState() {
		return state;
	}
	
	public void play() {
		state = State.PLAYING;
		openSlPlay();
	}

	public void stop() {
		state = State.STOPPED;
		openSlStop();
	}
	
	public void playTrack(int trackNum, int velocity, int pan, int pitch) {
		if (trackNum >= 0 && trackNum < sampleNames.length) {
			float normVelocity = velocity/127f;
			float normPan = pan/127f;
			float normPitch = 1.5f*pitch/127f + .5f;
			// highlight the icon to indicate playing
			//context.activateIcon(sampleNum);
			playTrack(trackNum, normVelocity, normPan, normPitch);
		}
	}
	
	public void stopAllTracks() {
		for (int sampleNum = 0; sampleNum < sampleNames.length; sampleNum++) {
			stopTrack(sampleNum);
		}
	}

	public native void openSlPlay();
	public native void openSlStop();
	public native void playTrack(int sampleNum, float volume, float pan, float pitch);
	public native void stopTrack(int sampleNum);
	public native void muteTrack(int sampleNum);
	public native void unmuteTrack(int sampleNum);
	public native void soloTrack(int sampleNum);
}