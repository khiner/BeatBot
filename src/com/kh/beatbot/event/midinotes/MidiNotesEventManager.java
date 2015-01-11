package com.kh.beatbot.event.midinotes;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.track.Track;

public class MidiNotesEventManager {
	private static List<MidiNoteDiff> midiNoteDiffs;
	private static List<MidiNote> beginNotes;

	public static synchronized void begin() {
		end();
		TrackManager.saveNoteTicks();
		midiNoteDiffs = new ArrayList<MidiNoteDiff>();
		beginNotes = TrackManager.copyMidiNotes();
	}

	public static synchronized void end() {
		if (beginNotes == null)
			return;

		if (!midiNoteDiffs.isEmpty()) {
			final MidiNotesDiffEvent midiNotesDiffEvent = new MidiNotesDiffEvent(midiNoteDiffs);
			EventManager.eventCompleted(midiNotesDiffEvent);
		}
		beginNotes = null;
	}

	public static MidiNote createNote(int note, long onTick, long offTick) {
		MidiNoteCreateDiff createDiff = new MidiNoteCreateDiff(note, onTick, offTick);
		midiNoteDiffs.add(createDiff);
		new MidiNotesDiffEvent(createDiff).apply();
		return createDiff.getNote();
	}

	public static MidiNote createNote(MidiNote note) {
		MidiNoteCreateDiff createDiff = new MidiNoteCreateDiff(note);
		midiNoteDiffs.add(createDiff);
		new MidiNotesDiffEvent(createDiff).apply();
		return createDiff.getNote();
	}

	public static void createNotes(List<MidiNote> notes) {
		begin();
		List<MidiNoteDiff> createDiffs = new ArrayList<MidiNoteDiff>(notes.size());
		for (MidiNote note : notes) {
			createDiffs.add(new MidiNoteCreateDiff(note));
		}
		midiNoteDiffs.addAll(createDiffs);
		new MidiNotesDiffEvent(createDiffs).apply();
		end();
	}

	public static void destroyNote(MidiNote note) {
		MidiNoteDestroyDiff destroyDiff = new MidiNoteDestroyDiff(note);
		midiNoteDiffs.add(destroyDiff);
		destroyDiff.apply();
	}

	public static void destroyNotes(List<MidiNote> notes) {
		begin();
		for (MidiNote note : notes) {
			destroyNote(note);
		}
		end();
	}

	public static void destroyAllNotes() {
		begin();
		for (Track track : TrackManager.getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				destroyNote(note);
			}
		}
		end();
	}

	public static void deleteSelectedNotes() {
		destroyNotes(TrackManager.getSelectedNotes());
	}

	public static void moveNote(MidiNote note, int noteDiff, long tickDiff) {
		Track track = TrackManager.getTrack(note);
		if (tickDiff != 0) {
			setNoteTicks(track, note, note.getOnTick() + tickDiff, note.getOffTick() + tickDiff,
					true);
		}
		if (noteDiff != 0) {
			note.setNote(note.getNoteValue() + noteDiff);
		}
		// handleMidiCollisions();
	}

	public static void moveSelectedNotes(int noteDiff, long tickDiff) {
		for (Track track : TrackManager.getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				if (note.isSelected()) {
					moveNote(note, noteDiff, tickDiff);
				}
			}
		}
		// XXX move to this class?
		for (Track track : TrackManager.getTracks()) {
			track.resetSelectedNotes();
		}

		// handleMidiCollisions();
	}

	public static void pinchSelectedNotes(long onTickDiff, long offTickDiff) {
		for (Track track : TrackManager.getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				if (note.isSelected()) {
					pinchNote(note, onTickDiff, offTickDiff);
				}
			}
		}
		// handleMidiCollisions();
	}

	/*
	 * Translate all midi notes to their on-ticks' nearest major ticks given the provided beat
	 * division
	 */
	public static void quantize(int beatDivision) {
		for (Track track : TrackManager.getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				long diff = (long) MidiManager.getMajorTickNearestTo(note.getOnTick())
						- note.getOnTick();
				setNoteTicks(note, note.getOnTick() + diff, note.getOffTick() + diff, true);
			}
		}
	}

	public static void handleNoteCollisions() {
		for (Track track : TrackManager.getTracks()) {
			List<MidiNote> notes = track.getMidiNotes();
			for (int i = 0; i < notes.size(); i++) {
				MidiNote note = notes.get(i);
				long newOnTick = note.isSelected() ? note.getOnTick() : note.getSavedOnTick();
				long newOffTick = note.isSelected() ? note.getOffTick() : note.getSavedOffTick();
				for (int j = 0; j < notes.size(); j++) {
					MidiNote otherNote = notes.get(j);
					if (note.equals(otherNote) || !otherNote.isSelected()) {
						continue;
					}
					// if a selected note begins in the middle of another note,
					// clip the covered note
					if (otherNote.getOnTick() > newOnTick && otherNote.getOnTick() - 1 < newOffTick) {
						newOffTick = otherNote.getOnTick() - 1;
						// otherwise, if a selected note overlaps with the beginning
						// of another note, delete the note
						// (CAN NEVER DELETE SELECTED NOTES THIS WAY!)
					} else if (!note.isSelected() && otherNote.getOnTick() <= newOnTick
							&& otherNote.getOffTick() > newOnTick) {
						// we 'delete' the note temporarily by moving
						// it offscreen, so it won't ever be played or drawn
						newOnTick = (long) MidiManager.MAX_TICKS * 2;
						newOffTick = (long) MidiManager.MAX_TICKS * 2 + 100;
						break;
					}
				}
				setNoteTicks(note, newOnTick, newOffTick, false);
			}
		}
	}

	public static void setNoteLevel(MidiNote note, LevelType levelType, byte level) {
		byte beginVelocity = note.getVelocity();
		byte beginPan = note.getPan();
		byte beginPitch = note.getPitch();

		note.setLevel(levelType, level);

		byte endVelocity = note.getVelocity();
		byte endPan = note.getPan();
		byte endPitch = note.getPitch();

		if (beginVelocity != endVelocity || beginPan != endPan || beginPitch != endPitch) {
			midiNoteDiffs.add(new MidiNoteLevelsDiff(note, beginVelocity, beginPan, beginPitch,
					endVelocity, endPan, endPitch));
		}
	}

	private static void setNoteTicks(MidiNote midiNote, long onTick, long offTick,
			boolean maintainNoteLength) {
		Track track = TrackManager.getTrack(midiNote.getNoteValue());
		setNoteTicks(track, midiNote, onTick, offTick, maintainNoteLength);
	}

	private static void setNoteTicks(Track track, MidiNote note, long onTick, long offTick,
			boolean maintainNoteLength) {
		if ((note.getOnTick() == onTick && note.getOffTick() == offTick)
				|| !track.containsNote(note)) {
			return;
		}

		if (offTick <= onTick)
			offTick = onTick + 4;
		if (MidiManager.isSnapToGrid()) {
			onTick = (long) MidiManager.getMajorTickNearestTo(onTick);
			offTick = (long) MidiManager.getMajorTickNearestTo(offTick) - 1;
			if (offTick == onTick - 1) {
				offTick += MidiManager.getTicksPerBeat();
			}
		}
		if (maintainNoteLength) {
			offTick = note.getOffTick() + onTick - note.getOnTick();
		}

		long beginOnTick = note.getOnTick();
		long beginOffTick = note.getOffTick();

		note.setTicks(onTick, offTick);
		midiNoteDiffs.add(new MidiNoteMoveDiff(note.getNoteValue(), beginOnTick, beginOffTick, note
				.getNoteValue(), note.getOnTick(), note.getOffTick()));
	}

	private static void pinchNote(MidiNote midiNote, long onTickDiff, long offTickDiff) {
		float newOnTick = midiNote.getOnTick();
		float newOffTick = midiNote.getOffTick();
		if (midiNote.getOnTick() + onTickDiff >= 0)
			newOnTick += onTickDiff;
		if (midiNote.getOffTick() + offTickDiff <= MidiManager.MAX_TICKS)
			newOffTick += offTickDiff;
		setNoteTicks(midiNote, (long) newOnTick, (long) newOffTick, false);
	}
}
