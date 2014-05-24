package com.kh.beatbot.event.midinotes;

import java.util.List;

import com.kh.beatbot.midi.MidiNote;

public class MidiNotesDestroyEvent extends MidiNotesEvent {

	public MidiNotesDestroyEvent(MidiNote midiNote) {
		super(midiNote);
	}

	public MidiNotesDestroyEvent(List<MidiNote> midiNotes) {
		super(midiNotes);
	}

	public void execute() {
		for (MidiNote midiNote : midiNotes) {
			midiNote.destroy();
		}
	}
}
