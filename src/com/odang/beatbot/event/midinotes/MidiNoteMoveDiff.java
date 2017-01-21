package com.odang.beatbot.event.midinotes;

import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.ui.view.View;

public class MidiNoteMoveDiff extends MidiNoteDiff {
	long beginOnTick, beginOffTick, endOnTick, endOffTick;
	int beginNoteValue, endNoteValue;

	public MidiNoteMoveDiff(int beginNoteValue, long beginOnTick, long beginOffTick,
			int endNoteValue, long endOnTick, long endOffTick) {
		this.beginNoteValue = beginNoteValue;
		this.beginOnTick = beginOnTick;
		this.beginOffTick = beginOffTick;
		this.endNoteValue = endNoteValue;
		this.endOnTick = endOnTick;
		this.endOffTick = endOffTick;
	}

	@Override
	public void apply() {
		final MidiNote midiNote = View.context.getMidiManager().findNote(beginNoteValue,
				beginOnTick);

		if (midiNote != null) { // TODO throw exception? Big deal if the state's not right
			midiNote.setSelected(true);
			midiNote.setNote(endNoteValue);
			midiNote.setTicks(endOnTick, endOffTick);
		}
	}

	@Override
	public MidiNoteMoveDiff opposite() {
		return new MidiNoteMoveDiff(endNoteValue, endOnTick, endOffTick, beginNoteValue,
				beginOnTick, beginOffTick);
	}
}
