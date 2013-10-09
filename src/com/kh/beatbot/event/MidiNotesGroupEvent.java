package com.kh.beatbot.event;

import java.util.List;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.view.page.Page;

public class MidiNotesGroupEvent extends Event {

	private List<MidiNote> savedState = null;

	public synchronized final void begin() {
		savedState = MidiManager.copyMidiList(MidiManager.getMidiNotes());
		MidiManager.saveNoteTicks();
	}

	public synchronized final void end() {
		MidiManager.finalizeNoteTicks();
		if (savedState != null && !notesEqual(savedState, MidiManager.getMidiNotes())) {
			eventCompleted(this);
		}
		updateUi();
	}

	public synchronized final void executeEvent(MidiNotesEvent event) {
		if (savedState == null) {
			begin();
		}
		event.execute();
	}

	@Override
	protected synchronized void execute() {
	}

	@Override
	protected synchronized void doRedo() {
		restore();
	}

	@Override
	protected synchronized void doUndo() {
		restore();
	}

	private synchronized void restore() {
		List<MidiNote> newSavedState = MidiManager.copyMidiList(MidiManager
				.getMidiNotes());
		// restore previous midi state
		new DestroyMidiNotesEvent(MidiManager.getMidiNotes()).execute();
		new CreateMidiNotesEvent(savedState).execute();
		updateUi();
		savedState = newSavedState;
	}

	private static void updateUi() {
		Page.mainPage.controlButtonGroup.setEditIconsEnabled(MidiManager
				.anyNoteSelected());
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
