package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.MidiNote.Levels;

public class MidiNoteLevelsDiff extends MidiNoteDiff {
	// identifying info for note
	int noteValue;
	long onTick;

	Levels beginLevels, endLevels;

	public MidiNoteLevelsDiff(MidiNote midiNote, Levels beginLevels, Levels endLevels) {
		this(midiNote.getNoteValue(), midiNote.getOnTick(), beginLevels, endLevels);
	}

	public MidiNoteLevelsDiff(int noteValue, long onTick, Levels beginLevels, Levels endLevels) {
		this.noteValue = noteValue;
		this.onTick = onTick;

		this.beginLevels = beginLevels;
		this.endLevels = endLevels;
	}

	@Override
	public void apply() {
		// when restoring from saved file, saved ticks can be different
		MidiNote note = MidiManager.findNote(noteValue, onTick);
		note.setLevels(endLevels);
	}

	@Override
	public MidiNoteLevelsDiff opposite() {
		return new MidiNoteLevelsDiff(noteValue, onTick, endLevels, beginLevels);
	}
}
