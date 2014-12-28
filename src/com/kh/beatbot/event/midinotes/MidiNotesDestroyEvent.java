package com.kh.beatbot.event.midinotes;

import java.util.List;

import com.kh.beatbot.event.Combinable;
import com.kh.beatbot.midi.MidiNote;

public class MidiNotesDestroyEvent extends MidiNotesEvent implements Combinable {

	public MidiNotesDestroyEvent(MidiNote midiNote) {
		super(midiNote);
	}

	public MidiNotesDestroyEvent(List<MidiNote> midiNotes) {
		super(midiNotes);
	}

	@Override
	public void undo() {
		new MidiNotesCreateEvent(midiNotes).doExecute();
	}

	@Override
	public void redo() {
		doExecute();
	}

	public void execute() {
		doExecute();
		MidiNotesEventManager.eventCompleted(this);
	}
	
	public void doExecute() {
		for (MidiNote midiNote : midiNotes) {
			midiNote.destroy();
		}
	}

	public void combine(Combinable other) {
		if (!(other instanceof MidiNotesDestroyEvent))
			return;
		for (MidiNote otherNote : ((MidiNotesDestroyEvent)other).midiNotes) {
			midiNotes.add(otherNote);
		}
	}
}
