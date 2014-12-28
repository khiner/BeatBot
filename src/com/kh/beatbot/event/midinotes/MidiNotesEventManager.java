package com.kh.beatbot.event.midinotes;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.event.Combinable;
import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNotesEventManager {
	private static List<Stateful> midiEvents = new ArrayList<Stateful>();
	private static boolean inProgress = false;

	public static synchronized void begin() {
		end();
		inProgress = true;
		TrackManager.saveNoteTicks();
	}

	public static synchronized void end() {
		if (inProgress) {
			TrackManager.finalizeNoteTicks();
			for (Stateful event : midiEvents) {
				EventManager.eventCompleted(event);
			}
			midiEvents.clear();
			inProgress = false;
		}
	}

	public static void eventCompleted(Stateful event) {
		if (inProgress && !combineEvent(event)) {
			midiEvents.add(event);
		}
	}

	public static void onMove(int beginNoteValue, long beginOnTick, long beginOffTick,
			int endNoteValue, long endOnTick, long endOffTick) {
		eventCompleted(new MidiNotesMoveEvent(beginNoteValue, beginOnTick, beginOffTick,
				endNoteValue, endOnTick, endOffTick));
	}

	public static void createNote(MidiNote midiNote) {
		if (null == midiNote)
			return;
		new MidiNotesCreateEvent(midiNote).execute();
	}

	public static void createNotes(List<MidiNote> midiNotes) {
		if (midiNotes.isEmpty())
			return;
		if (inProgress) {
			new MidiNotesCreateEvent(midiNotes).execute();
		} else {
			begin();
			new MidiNotesCreateEvent(midiNotes).execute();
			end();
		}
	}

	public static void destroyNote(MidiNote midiNote) {
		if (null == midiNote)
			return;
		new MidiNotesDestroyEvent(midiNote).execute();
	}

	public static void destroyNotes(List<MidiNote> midiNotes) {
		if (midiNotes.isEmpty())
			return;
		if (inProgress) {
			new MidiNotesDestroyEvent(midiNotes).execute();
		} else {
			begin();
			new MidiNotesDestroyEvent(midiNotes).execute();
			end();
		}
	}
	
	private static boolean combineEvent(Stateful event) {
		if (midiEvents.isEmpty())
			return false;
		Stateful latestEvent = midiEvents.get(midiEvents.size() - 1);
		if (event instanceof Combinable && latestEvent instanceof Combinable) {
			((Combinable)latestEvent).combine((Combinable) event);
			return true;
		} else {
			return false;
		}
	}
}
