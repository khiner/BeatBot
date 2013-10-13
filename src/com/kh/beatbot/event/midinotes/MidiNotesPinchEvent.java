package com.kh.beatbot.event.midinotes;

import java.util.Arrays;
import java.util.List;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNotesPinchEvent extends MidiNotesEvent {

	protected long onTickDiff, offTickDiff;

	public MidiNotesPinchEvent(MidiNote midiNote, long onTickDiff,
			long offTickDiff) {
		this(Arrays.asList(midiNote), onTickDiff, offTickDiff);
	}

	public MidiNotesPinchEvent(List<MidiNote> midiNotes, long onTickDiff,
			long offTickDiff) {
		super(midiNotes);
		this.onTickDiff = onTickDiff;
		this.offTickDiff = offTickDiff;
	}

	public void execute() {
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
