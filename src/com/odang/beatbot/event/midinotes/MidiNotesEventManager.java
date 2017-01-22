package com.odang.beatbot.event.midinotes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.odang.beatbot.effect.Effect.LevelType;
import com.odang.beatbot.manager.MidiManager;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.midi.MidiNote.Levels;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.view.View;

public class MidiNotesEventManager {
	private final MidiManager midiManager;

	private List<MidiNoteDiff> midiNoteDiffs;
	private Map<MidiNote, Levels> originalNoteLevels = new HashMap<MidiNote, Levels>();

	private boolean active = false;

	public MidiNotesEventManager(final MidiManager midiManager) {
		this.midiManager = midiManager;
	}

	public void begin() {
		end();
		activate();
	}

	public void end() {
		if (!isActive())
			return;

		List<MidiNoteDiff> moveDiffs = new ArrayList<MidiNoteDiff>();

		for (Track track : View.context.getTrackManager().getTracks()) {
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

				if (originalNoteLevels.containsKey(note)) {
					Levels originalLevels = originalNoteLevels.get(note);
					Levels newLevels = note.getLevels();
					if (originalLevels.velocity != newLevels.velocity) {
						addDiff(new MidiNoteLevelsDiff(note, LevelType.VOLUME,
								originalLevels.velocity, newLevels.velocity));
					}
					if (originalLevels.pan != newLevels.pan) {
						addDiff(new MidiNoteLevelsDiff(note, LevelType.PAN, originalLevels.pan,
								newLevels.pan));
					}
					if (originalLevels.pitch != newLevels.pitch) {
						addDiff(new MidiNoteLevelsDiff(note, LevelType.PITCH, originalLevels.pitch,
								newLevels.pitch));
					}
				}
			}
		}

		finalizeNoteTicks();
		addDiffs(moveDiffs);

		if (!midiNoteDiffs.isEmpty()) {
			final MidiNotesDiffEvent midiNotesDiffEvent = new MidiNotesDiffEvent(midiNoteDiffs);
			View.context.getEventManager().eventCompleted(midiNotesDiffEvent);
		}

		deactivate();
	}

	public MidiNote createNote(int noteValue, long onTick, long offTick) {
		MidiNoteCreateDiff createDiff = new MidiNoteCreateDiff(noteValue, onTick, offTick);
		addDiff(createDiff);
		new MidiNotesDiffEvent(createDiff).apply();

		return midiManager.findNote(noteValue, onTick);
	}

	public void createNotes(List<MidiNote> notes) {
		begin();
		List<MidiNoteDiff> createDiffs = new ArrayList<MidiNoteDiff>(notes.size());
		for (MidiNote note : notes) {
			createDiffs.add(new MidiNoteCreateDiff(note));
		}
		addDiffs(createDiffs);
		new MidiNotesDiffEvent(createDiffs).apply();
		end();
	}

	public void destroyNote(MidiNote note) {
		MidiNoteDestroyDiff destroyDiff = new MidiNoteDestroyDiff(note);
		addDiff(destroyDiff);
		destroyDiff.apply();
	}

	public void destroyNotes(List<MidiNote> notes) {
		begin();
		for (MidiNote note : notes) {
			destroyNote(note);
		}
		end();
	}

	public void destroyAllNotes() {
		begin();
		for (Track track : View.context.getTrackManager().getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				destroyNote(note);
			}
		}
		end();
	}

	public void deleteSelectedNotes() {
		destroyNotes(View.context.getTrackManager().getSelectedNotes());
	}

	public void moveNote(MidiNote note, int noteDiff, long tickDiff) {
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

	public void moveSelectedNotes(int noteDiff, long tickDiff) {
		if (noteDiff == 0 && tickDiff == 0)
			return;

		// need to keep a temp list since we can be moving notes to another track
		List<MidiNote> selectedNotes = new ArrayList<MidiNote>();

		for (Track track : View.context.getTrackManager().getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				if (note.isSelected()) {
					selectedNotes.add(note);
				}
			}
		}

		if (selectedNotes.isEmpty())
			return;

		if (midiManager.isSnapToGrid()) {
			final MidiNote firstSelectedNote = selectedNotes.get(0);
			tickDiff = midiManager.getMajorTickNearestTo(firstSelectedNote.getOnTick() + tickDiff) - firstSelectedNote.getOnTick();
		}

		boolean noteChanged = false;
		for (MidiNote note : selectedNotes) {
			if (tickDiff != 0) {
				if (note.setTicks(note.getOnTick() + tickDiff, note.getOffTick() + tickDiff)) {
					noteChanged = true;
				}
			}
			Track track = View.context.getTrackManager().getTrack(note);
			if (noteDiff != 0) {
				track.removeNote(note);
				note.setNote(note.getNoteValue() + noteDiff);
				View.context.getTrackManager().getTrack(note).addNote(note);
				noteChanged = true;
			}
		}

		if (noteChanged) {
			handleNoteCollisions();
		}
	}

	public void pinchSelectedNotes(long onTickDiff, long offTickDiff) {
		for (Track track : View.context.getTrackManager().getTracks()) {
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
	public void quantize(int beatDivision) {
		for (Track track : View.context.getTrackManager().getTracks()) {
			for (MidiNote note : track.getMidiNotes()) {
				long diff = midiManager.getMajorTickNearestTo(note.getOnTick()) - note.getOnTick();
				setNoteTicks(note, note.getOnTick() + diff, note.getOffTick() + diff, true);
			}
		}
	}

	// called after release of touch event - this
	// finalizes the note on/off ticks of all notes
	public void finalizeNoteTicks() {
		List<MidiNote> notesToDestroy = new ArrayList<MidiNote>();
		for (Track track : View.context.getTrackManager().getTracks()) {
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

	public void beforeLevelChange(MidiNote note) {
		if (!originalNoteLevels.containsKey(note)) {
			originalNoteLevels.put(note, note.getLevels());
		}
	}

	public void handleNoteCollisions() {
		for (Track track : View.context.getTrackManager().getTracks()) {
			List<MidiNote> notes = track.getMidiNotes();
			for (int i = 0; i < notes.size(); i++) {
				MidiNote note = notes.get(i);
				long newOnTick = note.isSelected() ? note.getOnTick() : note.getSavedOnTick();
				long newOffTick = note.isSelected() ? note.getOffTick() : note.getSavedOffTick();
				for (int j = 0; j < notes.size(); j++) {
					MidiNote selectedNote = notes.get(j);
					if (!selectedNote.isSelected() || note.equals(selectedNote))
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
						newOffTick = newOnTick + 100;
						break;
					}
				}
				setNoteTicks(note, newOnTick, newOffTick, false);
			}
		}
	}

	private boolean setNoteTicks(MidiNote note, long onTick, long offTick,
			boolean maintainNoteLength) {
		if (note.getOnTick() == onTick && note.getOffTick() == offTick) {
			return false;
		}

		if (offTick <= onTick)
			offTick = onTick + 4;
		if (midiManager.isSnapToGrid()) {
			onTick = (long) midiManager.getMajorTickNearestTo(onTick);
			offTick = (long) midiManager.getMajorTickNearestTo(offTick) - 1;
			if (offTick == onTick - 1) {
				offTick += midiManager.getTicksPerBeat();
			}
		}
		if (maintainNoteLength) {
			offTick = note.getOffTick() + onTick - note.getOnTick();
		}

		return note.setTicks(onTick, offTick);
	}

	private void pinchNote(MidiNote midiNote, long onTickDiff, long offTickDiff) {
		long newOnTick = midiNote.getOnTick();
		long newOffTick = midiNote.getOffTick();
		if (midiNote.getOnTick() + onTickDiff >= 0)
			newOnTick += onTickDiff;
		if (midiNote.getOffTick() + offTickDiff <= MidiManager.MAX_TICKS)
			newOffTick += offTickDiff;
		setNoteTicks(midiNote, newOnTick, newOffTick, false);
	}

	private void addDiff(MidiNoteDiff diff) {
		if (isActive()) {
			// only add diffs to list if they're coming from a begin/end session (usually touch
			// events). This way, if we are undoing/redoing, the diff will only be applied,
			// not added to the list
			midiNoteDiffs.add(diff);
		}
	}

	private void addDiffs(Collection<MidiNoteDiff> diffs) {
		if (isActive()) {
			midiNoteDiffs.addAll(diffs);
		}
	}

	private void activate() {
		View.context.getTrackManager().saveNoteTicks();
		midiNoteDiffs = new ArrayList<MidiNoteDiff>();
		originalNoteLevels.clear();
		active = true;
	}

	private void deactivate() {
		active = false;
	}

	private boolean isActive() {
		return active;
	}
}
