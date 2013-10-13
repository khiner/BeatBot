package com.kh.beatbot.event.midinotes;

import java.util.List;

import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.event.Temporal;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.view.page.Page;

public class MidiNotesGroupEvent implements Stateful, Temporal {

	private List<MidiNote> savedState = null;

	public synchronized final void begin() {
		savedState = MidiManager.copyMidiList(MidiManager.getMidiNotes());
		MidiManager.saveNoteTicks();
	}

	public synchronized final void end() {
		MidiManager.finalizeNoteTicks();
		if (savedState != null && !notesEqual(savedState, MidiManager.getMidiNotes())) {
			EventManager.eventCompleted(this);
		}
		updateUi();
	}

	@Override
	public synchronized void doRedo() {
		restore();
	}

	@Override
	public synchronized void doUndo() {
		restore();
	}

	public void updateUi() {
		Page.mainPage.controlButtonGroup.setEditIconsEnabled(MidiManager
				.anyNoteSelected());
	}

	private synchronized void restore() {
		List<MidiNote> newSavedState = MidiManager.copyMidiList(MidiManager
				.getMidiNotes());
		// restore previous midi state
		new MidiNotesDestroyEvent(MidiManager.getMidiNotes()).execute();
		new MidiNotesCreateEvent(savedState).execute();
		savedState = newSavedState;
	}

	private synchronized boolean notesEqual(List<MidiNote> notes,
			List<MidiNote> otherNotes) {
		if (notes.size() != otherNotes.size()) {
			return false;
		}

		for (int i = 0; i < notes.size(); i++) {
			if (notes.get(i).compareTo(otherNotes.get(i)) != 0) {
				return false;
			}
		}
		return true;
	}
}
