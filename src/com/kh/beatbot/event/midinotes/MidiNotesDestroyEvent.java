package com.kh.beatbot.event.midinotes;

import java.util.List;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.view.page.Page;

public class MidiNotesDestroyEvent extends MidiNotesEvent {

	public MidiNotesDestroyEvent(MidiNote midiNote) {
		super(midiNote);
	}

	public MidiNotesDestroyEvent(List<MidiNote> midiNotes) {
		super(midiNotes);
	}

	public void execute() {
		for (MidiNote midiNote : midiNotes) {
			destroyMidiNote(midiNote);
		}
		Page.mainPage.controlButtonGroup.notifyMidiChange();
	}

	private void destroyMidiNote(MidiNote midiNote) {
		midiNote.getRectangle().getGroup().remove(midiNote.getRectangle());
		Track track = TrackManager.getTrack(midiNote.getNoteValue());
		track.removeNote(midiNote);
	}
}
