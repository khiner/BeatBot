package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.midi.MidiNote;

public class MidiNoteLevelsDiff extends MidiNoteDiff {
	MidiNote midiNote;
	byte beginVelocity, beginPan, beginPitch, endVelocity, endPan, endPitch;

	public MidiNoteLevelsDiff(MidiNote midiNote, byte beginVelocity, byte beginPan,
			byte beginPitch, byte endVelocity, byte endPan, byte endPitch) {
		this.midiNote = midiNote;

		this.beginVelocity = beginVelocity;
		this.beginPan = beginPan;
		this.beginPitch = beginPitch;

		this.endVelocity = endVelocity;
		this.endPan = endPan;
		this.endPitch = endPitch;
	}

	@Override
	public void apply() {
		midiNote.setLevel(LevelType.VOLUME, endVelocity);
		midiNote.setLevel(LevelType.PAN, endPan);
		midiNote.setLevel(LevelType.PITCH, endPitch);
	}

	@Override
	public MidiNoteLevelsDiff opposite() {
		return new MidiNoteLevelsDiff(midiNote, endVelocity, endPan, endPitch, beginVelocity,
				beginPan, beginPitch);
	}
}
