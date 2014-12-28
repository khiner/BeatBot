package com.kh.beatbot.event.midinotes;

import java.util.List;

import com.kh.beatbot.event.Combinable;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNotesCreateEvent extends MidiNotesEvent implements Combinable {

	public MidiNotesCreateEvent(MidiNote midiNote) {
		super(midiNote);
	}

	public MidiNotesCreateEvent(List<MidiNote> midiNotes) {
		super(midiNotes);
	}

	@Override
	public void undo() {
		new MidiNotesDestroyEvent(midiNotes).doExecute();
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
		TrackManager.saveNoteTicks();
		TrackManager.deselectAllNotes();
		for (MidiNote midiNote : midiNotes) {
			midiNote.create();
		}
		MidiManager.handleMidiCollisions();
		TrackManager.deselectAllNotes();
		TrackManager.finalizeNoteTicks();
	}
	
	public void combine(Combinable other) {
		if (!(other instanceof MidiNotesCreateEvent))
			return;
		for (MidiNote otherNote : ((MidiNotesCreateEvent)other).midiNotes) {
			midiNotes.add(otherNote);
		}
	}
}
