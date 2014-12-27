package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNotesMoveEvent implements Stateful, Executable {

	protected long beginOnTick, beginOffTick, endOnTick, endOffTick;
	protected int beginNoteValue, endNoteValue;

	public MidiNotesMoveEvent(int beginNoteValue, long beginOnTick, long beginOffTick,
			int endNoteValue, long endOnTick, long endOffTick) {
		this.beginNoteValue = beginNoteValue;
		this.beginOnTick = beginOnTick;
		this.beginOffTick = beginOffTick;
		this.endNoteValue = endNoteValue;
		this.endOnTick = endOnTick;
		this.endOffTick = endOffTick;
	}

	@Override
	public void undo() {
		new MidiNotesMoveEvent(endNoteValue, endOnTick, endOffTick, beginNoteValue, beginOnTick,
				beginOffTick).doExecute();
	}

	@Override
	public void redo() {
		doExecute();
	}

	public void execute() {
		doExecute();
		MidiNotesEventManager.eventCompleted(this);
	}

	public void doExecute() {
		MidiNote midiNote = MidiManager.findNote(beginNoteValue, beginOnTick);

		if (midiNote != null) {
			TrackManager.saveNoteTicks();

			midiNote.setSelected(true);
			midiNote.setNote(endNoteValue);
			midiNote.setTicks(endOnTick, endOffTick);

			MidiManager.handleMidiCollisions();
			midiNote.setSelected(false);
			TrackManager.finalizeNoteTicks();
		}
	}
}
