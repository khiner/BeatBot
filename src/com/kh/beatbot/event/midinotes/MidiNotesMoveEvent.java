package com.kh.beatbot.event.midinotes;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.event.Combinable;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNotesMoveEvent implements Stateful, Executable, Combinable {
	public static class Move {
		long beginOnTick, beginOffTick, endOnTick, endOffTick;
		int beginNoteValue, endNoteValue;

		public Move(int beginNoteValue, long beginOnTick, long beginOffTick, int endNoteValue,
				long endOnTick, long endOffTick) {
			this.beginNoteValue = beginNoteValue;
			this.beginOnTick = beginOnTick;
			this.beginOffTick = beginOffTick;
			this.endNoteValue = endNoteValue;
			this.endOnTick = endOnTick;
			this.endOffTick = endOffTick;
		}

		public boolean combineWith(Move otherMove) {
			if (combinableWith(otherMove)) {
				endNoteValue = otherMove.endNoteValue;
				endOnTick = otherMove.endOnTick;
				endOffTick = otherMove.endOffTick;
				return true;
			} else {
				return false;
			}
		}

		private boolean combinableWith(Move otherMove) {
			return endNoteValue == otherMove.beginNoteValue && endOnTick == otherMove.beginOnTick
					&& endOffTick == otherMove.beginOffTick;
		}
	}

	private List<Move> moves = new ArrayList<Move>();

	public MidiNotesMoveEvent(int beginNoteValue, long beginOnTick, long beginOffTick,
			int endNoteValue, long endOnTick, long endOffTick) {
		moves.add(new Move(beginNoteValue, beginOnTick, beginOffTick, endNoteValue, endOnTick,
				endOffTick));
	}

	@Override
	public void undo() {
		for (Move move : moves) {
			MidiNote midiNote = MidiManager.findNote(move.endNoteValue, move.endOnTick);

			if (midiNote != null) {
				midiNote.setSelected(true);
				midiNote.setNote(move.beginNoteValue);
				midiNote.setTicks(move.beginOnTick, move.beginOffTick);

				MidiManager.handleMidiCollisions();
				midiNote.setSelected(false);
			}
		}
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
		for (Move move : moves) {
			MidiNote midiNote = MidiManager.findNote(move.beginNoteValue, move.beginOnTick);

			if (midiNote != null) {
				midiNote.setSelected(true);
				midiNote.setNote(move.endNoteValue);
				midiNote.setTicks(move.endOnTick, move.endOffTick);

				MidiManager.handleMidiCollisions();
				midiNote.setSelected(false);
			}
		}
	}

	// Not actually used. combineMove is used directly for efficiency
	public void combine(Combinable other) {
		if (!(other instanceof MidiNotesMoveEvent))
			return;
		MidiNotesMoveEvent otherMoveEvent = (MidiNotesMoveEvent) other;
		// try combining each event
		for (Move otherMove : otherMoveEvent.moves) {
			combineMove(otherMove);
		}
	}

	public void combineMove(Move otherMove) {
		boolean combined = false;
		for (Move move : moves) {
			if (move.combineWith(otherMove)) {
				combined = true;
			}
		}

		if (!combined) {
			moves.add(otherMove);
		}
	}
}
