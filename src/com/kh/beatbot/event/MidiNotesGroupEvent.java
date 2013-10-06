package com.kh.beatbot.event;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.view.page.Page;

public class MidiNotesGroupEvent extends Event {

	private List<MidiNote> savedState = null;
	private List<MidiNotesEvent> subEvents = new ArrayList<MidiNotesEvent>();

	protected final void execute() {
		begin();
		doExecute();
		end();
	}

	public synchronized final void begin() {
		savedState = MidiManager.copyMidiList(MidiManager.getMidiNotes());
		MidiManager.saveNoteTicks();
	}

	public synchronized final void end() {
		MidiManager.finalizeNoteTicks();
		if (!subEvents.isEmpty()) {
			eventCompleted(this);
		}
		updateUi();
	}

	public synchronized final void executeEvent(MidiNotesEvent event) {
		if (savedState == null) {
			begin();
		}
		event.doExecute();
		if (subEvents.isEmpty()
				|| !subEvents.get(subEvents.size() - 1).merge(event)) {
			subEvents.add(event);
		}
		MidiNotesEvent lastEvent = subEvents.get(subEvents.size() - 1);
		if (!lastEvent.hasEffect()) {
			subEvents.remove(lastEvent);
		}
	}

	@Override
	protected synchronized void doExecute() {
		for (MidiNotesEvent event : subEvents) {
			event.doExecute();
		}
	}

	@Override
	protected synchronized void doUndo() {
		// restore previous midi state
		new DestroyMidiNotesEvent(MidiManager.getMidiNotes()).doExecute();
		new CreateMidiNotesEvent(savedState).doExecute();
		updateUi();
	}

	@Override
	protected Event opposite() {
		return null;
	}

	private void updateUi() {
		Page.mainPage.controlButtonGroup.setEditIconsEnabled(MidiManager
				.anyNoteSelected());
	}
}
