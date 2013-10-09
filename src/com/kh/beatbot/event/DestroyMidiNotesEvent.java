package com.kh.beatbot.event;

import java.util.List;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;

public class DestroyMidiNotesEvent extends MidiNotesEvent {

	public DestroyMidiNotesEvent(MidiNote midiNote) {
		super(midiNote);
	}

	public DestroyMidiNotesEvent(List<MidiNote> midiNotes) {
		super(midiNotes);
	}

	public void execute() {
		for (MidiNote midiNote : midiNotes) {
			destroyMidiNote(midiNote);
		}
	}

	private void destroyMidiNote(MidiNote midiNote) {
		midiNote.getRectangle().getGroup().remove(midiNote.getRectangle());
		Track track = TrackManager.getTrack(midiNote.getNoteValue());
		track.removeNote(midiNote);
	}
}
