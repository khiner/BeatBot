package com.odang.beatbot.event.midinotes;

import com.odang.beatbot.effect.Effect.LevelType;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.ui.view.View;

public class MidiNoteLevelsDiff extends MidiNoteDiff {
	// identifying info for note
	int noteValue;
	long onTick;

	LevelType type;
	byte beginLevel, endLevel;

	public MidiNoteLevelsDiff(MidiNote midiNote, LevelType type, byte beginLevel, byte endLevel) {
		this(midiNote.getNoteValue(), midiNote.getOnTick(), type, beginLevel, endLevel);
	}

	public MidiNoteLevelsDiff(int noteValue, long onTick, LevelType type, byte beginLevel,
			byte endLevel) {
		this.noteValue = noteValue;
		this.onTick = onTick;
		this.type = type;

		this.beginLevel = beginLevel;
		this.endLevel = endLevel;
	}

	@Override
	public void apply() {
		// when restoring from saved file, saved ticks can be different
		final MidiNote note = View.context.getMidiManager().findNote(noteValue, onTick);
		note.setLevel(type, endLevel);
	}

	@Override
	public MidiNoteLevelsDiff opposite() {
		return new MidiNoteLevelsDiff(noteValue, onTick, type, endLevel, beginLevel);
	}
}
