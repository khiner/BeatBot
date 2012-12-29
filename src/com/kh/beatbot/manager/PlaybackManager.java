package com.kh.beatbot.manager;

public class PlaybackManager {

	private static PlaybackManager singletonInstance = null;
	private static State state;

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
		// audioTracks = new AudioTrack[sampleNames.length];
		// streams = new Stream[sampleNames.length];
		state = State.STOPPED;
	}

	public State getState() {
		return state;
	}

	public void play() {
		state = State.PLAYING;
		playNative();
	}

	public void stop() {
		state = State.STOPPED;
		stopNative();
	}

	public void reset() {
		playNative();
	}

	public static native void playNative();

	public static native void stopNative();
}