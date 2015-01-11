package com.kh.beatbot.event.midinotes;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.manager.TrackManager;

public class MidiNotesDiffEvent implements Stateful {
	private final List<MidiNoteDiff> midiNoteDiffs;

	public MidiNotesDiffEvent(MidiNoteDiff midiNoteDiff) {
		midiNoteDiffs = new ArrayList<MidiNoteDiff>(1);
		midiNoteDiffs.add(midiNoteDiff);
	}

	public MidiNotesDiffEvent(final List<MidiNoteDiff> midiNoteDiffs) {
		this.midiNoteDiffs = midiNoteDiffs;
	}

	@Override
	public void undo() {
		TrackManager.saveNoteTicks();
		TrackManager.deselectAllNotes();

		// apply opposites in reverse order
		for (int i = midiNoteDiffs.size() - 1; i >= 0; i--) {
			midiNoteDiffs.get(i).opposite().apply();
		}

		MidiNotesEventManager.handleNoteCollisions();
		TrackManager.deselectAllNotes();
		TrackManager.finalizeNoteTicks();
	}

	@Override
	public void redo() {
		TrackManager.saveNoteTicks();
		TrackManager.deselectAllNotes();

		for (MidiNoteDiff midiNoteDiff : midiNoteDiffs) {
			midiNoteDiff.apply();
		}

		MidiNotesEventManager.handleNoteCollisions();
		TrackManager.deselectAllNotes();
		TrackManager.finalizeNoteTicks();
	}
	
	public void apply() {
		redo();
	}
}
