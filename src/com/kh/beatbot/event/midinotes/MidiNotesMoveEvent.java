package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNotesMoveEvent extends MidiNotesEvent {

	protected long tickDiff;
	protected int noteDiff;

	public MidiNotesMoveEvent(MidiNote midiNote, int noteDiff, long tickDiff) {
		super(midiNote);
		this.noteDiff = noteDiff;
		this.tickDiff = tickDiff;
	}

	public MidiNotesMoveEvent(int noteDiff, long tickDiff) {
		this.noteDiff = noteDiff;
		this.tickDiff = tickDiff;
	}

	public void execute() {
		if (null != midiNotes && !midiNotes.isEmpty()) {
			for (MidiNote midiNote : midiNotes) {
				TrackManager.moveNote(midiNote, noteDiff, tickDiff);
			}
		} else {
			TrackManager.moveSelectedNotes(noteDiff, tickDiff);
		}
		
		MidiManager.handleMidiCollisions();
	}
}
