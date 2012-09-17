package com.kh.beatbot.listener;

public interface MidiTrackControlListener {
	public abstract void midiControlIconClicked(int track, int controlNum);
	public abstract void midiControlIconLongPressed(int track, int controlNum);
}
