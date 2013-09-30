package com.kh.beatbot.event;

import java.util.List;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.view.page.Page;

public class CreateMidiNotesEvent extends MidiNotesEvent {
	
	public CreateMidiNotesEvent(MidiNote midiNote) {
		super(midiNote);
	}
	
	public CreateMidiNotesEvent(List<MidiNote> midiNotes) {
		super(midiNotes);
	}

	protected void doExecute() {
		MidiManager.deselectAllNotes();
		for (MidiNote midiNote : midiNotes) {
			createNote(midiNote);
		}
		MidiManager.handleMidiCollisions();
		MidiManager.deselectAllNotes();
	}
	
	protected Event opposite() {
		return new DestroyMidiNotesEvent(midiNotes);
	}
	
	private void createNote(MidiNote midiNote) {
		Track track = TrackManager.getTrack(midiNote.getNoteValue());
		track.addNote(midiNote);
		Page.mainPage.midiView.createNoteView(midiNote);
		MidiManager.selectNote(midiNote);
	}
}
