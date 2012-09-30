package com.kh.beatbot.manager;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Track;

public class PlaybackManager {
	
	private static PlaybackManager singletonInstance = null;
	private State state;
		
	public static final int SAMPLE_RATE = 44100;
	
	public enum State {
		PLAYING, STOPPED
	}
	
	public static PlaybackManager getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new PlaybackManager();
		}
		return singletonInstance;			
	}
	
	private PlaybackManager() {
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
		
	public static void stopAllTracks() {
		for (Track track : GlobalVars.tracks) {
			track.stop();
		}
	}

	public static native void armAllTracks();
	public static native void disarmAllTracks();
}