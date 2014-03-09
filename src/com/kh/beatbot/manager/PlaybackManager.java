package com.kh.beatbot.manager;

public class PlaybackManager {

	public static enum State {
		PLAYING, STOPPED
	}

	public static final int SAMPLE_RATE = 44100;

	private static PlaybackManager singletonInstance = null;
	private static State state = State.STOPPED;

	public static State getState() {
		return state;
	}

	public static void play() {
		state = State.PLAYING;
		playNative();
	}

	public static void stop() {
		state = State.STOPPED;
		stopNative();
	}

	public static native void playNative();

	public static native void stopNative();
}