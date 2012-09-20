package com.kh.beatbot.listener;

public interface MidiTrackControlListener {
	public abstract void muteToggled(int track, boolean mute);
	public abstract void soloToggled(int track, boolean solo);
	public abstract void trackClicked(int track);
	public abstract void trackLongPressed(int track);
}
