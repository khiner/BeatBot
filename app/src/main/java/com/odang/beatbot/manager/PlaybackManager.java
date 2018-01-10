package com.odang.beatbot.manager;

public class PlaybackManager {
    public static final int SAMPLE_RATE = 44100;

    public enum State {
        PLAYING, STOPPED
    }

    private State state = State.STOPPED;

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

    private native void playNative();

    private native void stopNative();
}