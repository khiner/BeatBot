package com.kh.beatbot.event;

import java.util.Arrays;
import java.util.List;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;

public class PinchMidiNotesEvent extends MidiNotesEvent {

	protected long onTickDiff, offTickDiff;

	public PinchMidiNotesEvent(MidiNote midiNote, long onTickDiff,
			long offTickDiff) {
		this(Arrays.asList(midiNote), onTickDiff, offTickDiff);
	}

	public PinchMidiNotesEvent(List<MidiNote> midiNotes, long onTickDiff,
			long offTickDiff) {
		super(midiNotes);
		this.onTickDiff = onTickDiff;
		this.offTickDiff = offTickDiff;
	}

	protected void execute() {
		for (MidiNote midiNote : MidiManager.getSelectedNotes()) {
			pinchNote(midiNote);
		}
		MidiManager.handleMidiCollisions();
	}

	private void pinchNote(MidiNote midiNote) {
		float newOnTick = midiNote.getOnTick();
		float newOffTick = midiNote.getOffTick();
		if (midiNote.getOnTick() + onTickDiff >= 0)
			newOnTick += onTickDiff;
		if (midiNote.getOffTick() + offTickDiff <= MidiManager.MAX_TICKS)
			newOffTick += offTickDiff;
		MidiManager.setNoteTicks(midiNote, (long) newOnTick, (long) newOffTick,
				false);
	}
}
