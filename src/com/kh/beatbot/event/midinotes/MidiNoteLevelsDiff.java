package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNoteLevelsDiff extends MidiNoteDiff {
	// identifying info for note
	int noteValue;
	long onTick;

	byte beginVelocity, beginPan, beginPitch, endVelocity, endPan, endPitch;

	public MidiNoteLevelsDiff(MidiNote midiNote, byte beginVelocity, byte beginPan,
			byte beginPitch, byte endVelocity, byte endPan, byte endPitch) {
		this(midiNote.getNoteValue(), midiNote.getOnTick(), beginVelocity, beginPan, beginPitch,
				endVelocity, endPan, endPitch);
	}

	public MidiNoteLevelsDiff(int noteValue, long onTick, byte beginVelocity, byte beginPan,
			byte beginPitch, byte endVelocity, byte endPan, byte endPitch) {
		this.noteValue = noteValue;
		this.onTick = onTick;

		this.beginVelocity = beginVelocity;
		this.beginPan = beginPan;
		this.beginPitch = beginPitch;

		this.endVelocity = endVelocity;
		this.endPan = endPan;
		this.endPitch = endPitch;
	}

	@Override
	public void apply() {
		// when restoring from saved file, saved ticks can be different
		MidiNote note = MidiManager.findNote(noteValue, onTick);
		note.setLevel(LevelType.VOLUME, endVelocity);
		note.setLevel(LevelType.PAN, endPan);
		note.setLevel(LevelType.PITCH, endPitch);
	}

	@Override
	public MidiNoteLevelsDiff opposite() {
		return new MidiNoteLevelsDiff(noteValue, onTick, endVelocity, endPan, endPitch,
				beginVelocity, beginPan, beginPitch);
	}
}
