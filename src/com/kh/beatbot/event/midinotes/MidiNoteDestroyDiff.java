package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNoteDestroyDiff extends MidiNoteDiff {
	private MidiNote midiNote;

	public MidiNoteDestroyDiff(MidiNote midiNote) {
		this.midiNote = midiNote;
	}
	
	@Override
	public void apply() {
		// when restoring from saved file, saved ticks can be different & no Rectangle instance
		TrackManager.getTrack(midiNote).findNoteStarting(midiNote.getOnTick()).destroy();
	}

	@Override
	public MidiNoteCreateDiff opposite() {
		return new MidiNoteCreateDiff(midiNote); 
	}
}
