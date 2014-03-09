package com.kh.beatbot.event.midinotes;

import java.util.List;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.view.View;

public class MidiNotesCreateEvent extends MidiNotesEvent {

	public MidiNotesCreateEvent(MidiNote midiNote) {
		super(midiNote);
	}

	public MidiNotesCreateEvent(List<MidiNote> midiNotes) {
		super(midiNotes);
	}

	public void execute() {
		MidiManager.deselectAllNotes();
		for (MidiNote midiNote : midiNotes) {
			createNote(midiNote);
		}
		MidiManager.handleMidiCollisions();
		MidiManager.deselectAllNotes();
		View.mainPage.controlButtonGroup.notifyMidiChange();
	}

	private void createNote(MidiNote midiNote) {
		Track track = TrackManager.getTrack(midiNote.getNoteValue());
		track.addNote(midiNote);
		View.mainPage.midiView.createNoteView(midiNote);
		midiNote.setSelected(true);
	}
}
