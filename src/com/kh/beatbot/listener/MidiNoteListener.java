package com.kh.beatbot.listener;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.midi.MidiNote;

public interface MidiNoteListener {
	public void onCreate(MidiNote note);

	public void onDestroy(MidiNote note);

	public void onMove(MidiNote note, int beginNoteValue, long beginOnTick, long beginOffTick,
			int endNoteValue, long endOnTick, long endOffTick);

	public void onSelectStateChange(MidiNote note);
	
	public void onLevelChanged(MidiNote note, LevelType type);
}
