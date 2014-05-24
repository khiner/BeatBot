package com.kh.beatbot.listener;

import com.kh.beatbot.midi.MidiNote;

public interface MidiNoteListener {
	public void onCreate(MidiNote note);
	public void onDestroy(MidiNote note);
	public void onMove(MidiNote note);
	public void onSelectStateChange(MidiNote note);
}
