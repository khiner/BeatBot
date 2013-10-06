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

	public void doExecute() {
		for (MidiNote midiNote : midiNotes) {
			destroyMidiNote(midiNote);
		}
	}
	
	protected Event opposite() {
		return new CreateMidiNotesEvent(midiNotes);
	}
	
	private void destroyMidiNote(MidiNote midiNote) {
		midiNote.getRectangle().getGroup().remove(midiNote.getRectangle());
		Track track = TrackManager.getTrack(midiNote.getNoteValue());
		track.removeNote(midiNote);
	}

	@Override
	protected boolean merge(MidiNotesEvent other) {
		return false;
	}

	@Override
	protected boolean hasEffect() {
		return true;
	}
}
