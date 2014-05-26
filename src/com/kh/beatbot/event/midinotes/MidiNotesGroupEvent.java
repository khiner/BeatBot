package com.kh.beatbot.event.midinotes;

import java.util.List;

import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.event.Temporal;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNotesGroupEvent implements Stateful, Temporal {
	private List<MidiNote> savedState = null;

	public synchronized final void begin() {
		savedState = TrackManager.copyMidiNotes();
		TrackManager.saveNoteTicks();
	}

	public synchronized final void end() {
		TrackManager.finalizeNoteTicks();
		if (null != savedState && !TrackManager.notesEqual(savedState)) {
			EventManager.eventCompleted(this);
		}
	}

	@Override
	public synchronized void doRedo() {
		restore();
	}

	@Override
	public synchronized void doUndo() {
		restore();
	}

	protected synchronized void restore() {
		List<MidiNote> newSavedState = TrackManager.copyMidiNotes();
		// restore previous midi state
		new MidiNotesDestroyEvent(TrackManager.getMidiNotes()).execute();
		new MidiNotesCreateEvent(savedState).execute();
		savedState = newSavedState;
	}
}
