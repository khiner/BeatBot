package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.midi.MidiNote;

public class MidiNoteDestroyDiff extends MidiNoteDiff {
	private MidiNote midiNote;

	public MidiNoteDestroyDiff(MidiNote midiNote) {
		this.midiNote = midiNote;
	}
	
	@Override
	public void apply() {
		midiNote.destroy();
	}

	@Override
	public MidiNoteCreateDiff opposite() {
		return new MidiNoteCreateDiff(midiNote); 
	}
}
