package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNoteDestroyDiff extends MidiNoteDiff {
	MidiNote note;

	public MidiNoteDestroyDiff(MidiNote note) {
		this.note = note;
	}

	@Override
	public void apply() {
		// when restoring from saved file, saved ticks can be different & no Rectangle instance
		MidiManager.findNote(note.getNoteValue(), note.getOnTick()).destroy();
	}

	@Override
	public MidiNoteCreateDiff opposite() {
		return new MidiNoteCreateDiff(note);
	}
}
