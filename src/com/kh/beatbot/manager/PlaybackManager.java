package com.kh.beatbot.manager;

import android.content.Context;

public class PlaybackManager {
	
	private static PlaybackManager singletonInstance = null;
	private State state;
		
	private String[] sampleNames;
	
	private static final int SAMPLE_RATE = 41000;
	
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
		armAllTracks();
	}

	public void stop() {
		state = State.STOPPED;
		disarmAllTracks();
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

	public native void armAllTracks();
	public native void armTrack(int trackNum);
	public native void disarmAllTracks();
	public native void disarmTrack(int trackNum);
	public native void playTrack(int trackNum, float volume, float pan, float pitch);
	public native void stopTrack(int trackNum);
	public native void muteTrack(int trackNum);
	public native void unmuteTrack(int trackNum);
	public native void soloTrack(int trackNum);
	public native void toggleLooping(int trackNum);
	public native boolean isLooping(int trackNum); 
	public native void setLoopWindow(int sampleNum, int loopBegin, int loopEnd);
}