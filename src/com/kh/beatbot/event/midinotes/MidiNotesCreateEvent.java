package com.kh.beatbot.event.midinotes;

import java.util.List;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNotesCreateEvent extends MidiNotesEvent {

	public MidiNotesCreateEvent(MidiNote midiNote) {
		super(midiNote);
	}

	public MidiNotesCreateEvent(List<MidiNote> midiNotes) {
		super(midiNotes);
	}

	public void execute() {
		MidiManager.deselectAllNotes();
		for (MidiNote midiNote : midiNotes) {
			midiNote.create();
		}
		MidiManager.handleMidiCollisions();
		MidiManager.deselectAllNotes();
	}
}
