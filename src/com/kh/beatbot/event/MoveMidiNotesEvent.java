package com.kh.beatbot.event;

import java.util.Arrays;
import java.util.List;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;

public class MoveMidiNotesEvent extends MidiNotesEvent {

	protected long tickDiff;
	protected int noteDiff;

	public MoveMidiNotesEvent(MidiNote midiNote, long tickDiff, int noteDiff,
			boolean snapToGrid) {
		this(Arrays.asList(midiNote), tickDiff, noteDiff);
	}

	public MoveMidiNotesEvent(List<MidiNote> midiNotes, long tickDiff,
			int noteDiff) {
		super(midiNotes);
		this.tickDiff = tickDiff;
		this.noteDiff = noteDiff;
	}

	protected void execute() {
		for (MidiNote midiNote : midiNotes) {
			moveMidiNote(midiNote);
		}
		MidiManager.handleMidiCollisions();
	}

	private void moveMidiNote(MidiNote midiNote) {
		if (tickDiff != 0) {
			MidiManager.setNoteTicks(midiNote, midiNote.getOnTick()
					+ tickDiff, midiNote.getOffTick() + tickDiff, true);
		}
		if (noteDiff != 0) {
			MidiManager.setNoteValue(midiNote, midiNote.getNoteValue()
					+ noteDiff);
		}
	}
}
