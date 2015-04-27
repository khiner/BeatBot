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
		MidiNote noteToDestroy = MidiManager.findNote(note.getNoteValue(), note.getOnTick());
		if (noteToDestroy != null)
			noteToDestroy.destroy();
	}

	@Override
	public MidiNoteCreateDiff opposite() {
		return new MidiNoteCreateDiff(note);
	}
}
