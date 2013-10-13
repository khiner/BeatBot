package com.kh.beatbot.event;

import java.util.Arrays;
import java.util.List;

import com.kh.beatbot.midi.MidiNote;

public abstract class MidiNotesEvent implements Executable {

	protected List<MidiNote> midiNotes;

	public MidiNotesEvent(MidiNote midiNote) {
		this.midiNotes = Arrays.asList(midiNote);
	}

	public MidiNotesEvent(List<MidiNote> midiNotes) {
		this.midiNotes = midiNotes;
	}
}
