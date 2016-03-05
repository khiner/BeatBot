package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.midi.event.NoteOff;
import com.kh.beatbot.midi.event.NoteOn;
import com.kh.beatbot.midi.util.GeneralUtils;
import com.kh.beatbot.ui.view.View;

public class MidiNoteCreateDiff extends MidiNoteDiff {
	int noteValue;
	long onTick, offTick;
	MidiNote note;

	public MidiNoteCreateDiff(int noteValue, long onTick, long offTick) {
		this.noteValue = noteValue;
		this.onTick = onTick;
		this.offTick = offTick;
	}

	public MidiNoteCreateDiff(MidiNote note) {
		this(note.getNoteValue(), note.getOnTick(), note.getOffTick());
		this.note = note;
	}

	@Override
	public void apply() {
		if (note == null) {
			byte velocity = GeneralUtils.HALF_BYTE;
			byte pan = GeneralUtils.HALF_BYTE;
			byte pitch = GeneralUtils.HALF_BYTE;

			NoteOn on = new NoteOn(onTick, 0, noteValue, velocity, pan, pitch);
			NoteOff off = new NoteOff(offTick, 0, noteValue, velocity, pan, pitch);
			new MidiNote(on, off).create();
		} else {
			note.create();
		}
	}

	@Override
	public MidiNoteDestroyDiff opposite() {
		final MidiNote note = View.context.getMidiManager().findNote(noteValue, onTick);
		return new MidiNoteDestroyDiff(note);
	}
}
