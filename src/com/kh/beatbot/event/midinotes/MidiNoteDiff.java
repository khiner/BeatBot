package com.kh.beatbot.event.midinotes;

public abstract class MidiNoteDiff {
	public abstract void apply();
	public abstract MidiNoteDiff opposite();
}
