package com.kh.beatbot.event.midinotes;

import java.util.Arrays;
import java.util.List;

import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.midi.MidiNote;

public abstract class MidiNotesEvent implements Stateful, Executable {

	protected final List<MidiNote> midiNotes;

	public MidiNotesEvent(MidiNote midiNote) {
		this.midiNotes = Arrays.asList(midiNote);
	}

	public MidiNotesEvent(List<MidiNote> midiNotes) {
		this.midiNotes = midiNotes;
	}
}
