package com.kh.beatbot.event.midinotes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.util.Log;

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
		activate();
	}

	public static synchronized void end() {
		if (!isActive())
			return;

		List<MidiNoteDiff> moveDiffs = new ArrayList<MidiNoteDiff>();

		for (Track track : TrackManager.getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				if (!note.isFinalized()
						&& !note.isMarkedForDeletion()
						&& (note.getNoteValue() != note.getSavedNoteValue()
								|| note.getOnTick() != note.getSavedOnTick() || note.getOffTick() != note
								.getSavedOffTick())) {
					// TODO combine into MidiNotesMoveDiff for multiple note moves with same diff
					moveDiffs.add(new MidiNoteMoveDiff(note.getSavedNoteValue(), note
							.getSavedOnTick(), note.getSavedOffTick(), note.getNoteValue(), note
							.getOnTick(), note.getOffTick()));
				}
			}
		}

		finalizeNoteTicks();
		addDiffs(moveDiffs);

		if (!midiNoteDiffs.isEmpty()) {
			final MidiNotesDiffEvent midiNotesDiffEvent = new MidiNotesDiffEvent(midiNoteDiffs);
			EventManager.eventCompleted(midiNotesDiffEvent);
		}

		deactivate();
	}

	public static MidiNote createNote(int note, long onTick, long offTick) {
		MidiNoteCreateDiff createDiff = new MidiNoteCreateDiff(note, onTick, offTick);
		addDiff(createDiff);
		new MidiNotesDiffEvent(createDiff).apply();
		return createDiff.getNote();
	}

	public static MidiNote createNote(MidiNote note) {
		MidiNoteCreateDiff createDiff = new MidiNoteCreateDiff(note);
		addDiff(createDiff);
		new MidiNotesDiffEvent(createDiff).apply();
		return createDiff.getNote();
	}

	public static void createNotes(List<MidiNote> notes) {
		begin();
		List<MidiNoteDiff> createDiffs = new ArrayList<MidiNoteDiff>(notes.size());
		for (MidiNote note : notes) {
			createDiffs.add(new MidiNoteCreateDiff(note));
		}
		addDiffs(createDiffs);
		new MidiNotesDiffEvent(createDiffs).apply();
		end();
	}

	public static void destroyNote(MidiNote note) {
		MidiNoteDestroyDiff destroyDiff = new MidiNoteDestroyDiff(note);
		addDiff(destroyDiff);
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
		boolean noteChanged = false;
		if (tickDiff != 0) {
			noteChanged = setNoteTicks(note, note.getOnTick() + tickDiff, note.getOffTick()
					+ tickDiff, true);
		}
		if (noteDiff != 0) {
			note.setNote(note.getNoteValue() + noteDiff);
			noteChanged = true;
		}

		if (noteChanged) {
			handleNoteCollisions();
		}
	}

	public static void moveSelectedNotes(int noteDiff, long tickDiff) {
		if (noteDiff == 0 && tickDiff == 0)
			return;

		// need to keep a temp list since we can be moving notes to another track
		List<MidiNote> selectedNotes = new ArrayList<MidiNote>();

		for (Track track : TrackManager.getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				if (note.isSelected()) {
					selectedNotes.add(note);
				}
			}
		}

		boolean noteChanged = false;

		for (MidiNote note : selectedNotes) {
			Track track = TrackManager.getTrack(note);
			if (tickDiff != 0) {
				if (setNoteTicks(note, note.getOnTick() + tickDiff, note.getOffTick() + tickDiff,
						true)) {
					noteChanged = true;
				}
			}
			if (noteDiff != 0) {
				track.removeNote(note);
				note.setNote(note.getNoteValue() + noteDiff);
				TrackManager.getTrack(note.getNoteValue()).addNote(note);
				noteChanged = true;
			}
		}

		if (noteChanged) {
			handleNoteCollisions();
		}
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
					MidiNote selectedNote = notes.get(j);
					if (!selectedNote.isSelected() || note.totallyEquals(selectedNote))
						continue;
					// if a selected note begins in the middle of another note,
					// clip the covered note
					if (selectedNote.getOnTick() > newOnTick
							&& selectedNote.getOnTick() - 1 < newOffTick) {
						newOffTick = selectedNote.getOnTick() - 1;
						// otherwise, if a selected note overlaps with the beginning
						// of another note, delete the note
						// (CAN NEVER DELETE SELECTED NOTES THIS WAY!)
					} else if (!note.isSelected() && selectedNote.getOnTick() <= newOnTick
							&& selectedNote.getOffTick() > newOnTick) {
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

	// called after release of touch event - this
	// finalizes the note on/off ticks of all notes
	public static void finalizeNoteTicks() {
		List<MidiNote> notesToDestroy = new ArrayList<MidiNote>();
		for (Track track : TrackManager.getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				if (note.isMarkedForDeletion()) {
					notesToDestroy.add(note);
				} else {
					note.finalizeTicks();
				}
			}
		}

		for (MidiNote note : notesToDestroy) {
			Log.d("Track", "Destroying note in finalize!");
			destroyNote(note);
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
			addDiff(new MidiNoteLevelsDiff(note, beginVelocity, beginPan, beginPitch, endVelocity,
					endPan, endPitch));
		}
	}

	private static boolean setNoteTicks(MidiNote note, long onTick, long offTick,
			boolean maintainNoteLength) {
		if (note.getOnTick() == onTick && note.getOffTick() == offTick) {
			return false;
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

		return note.setTicks(onTick, offTick);
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

	private static void addDiff(MidiNoteDiff diff) {
		if (isActive()) {
			// only add diffs to list if they're coming from a begin/end session (usually touch
			// events). This way, if we are undoing/redoing, the diff will only be applied,
			// not added to the list
			midiNoteDiffs.add(diff);
		}
	}

	private static void addDiffs(Collection<MidiNoteDiff> diffs) {
		if (isActive()) {
			midiNoteDiffs.addAll(diffs);
		}
	}

	private static void activate() {
		TrackManager.saveNoteTicks();
		midiNoteDiffs = new ArrayList<MidiNoteDiff>();
		beginNotes = TrackManager.copyMidiNotes();
	}

	private static void deactivate() {
		beginNotes = null;
	}

	private static boolean isActive() {
		return beginNotes != null;
	}
}
