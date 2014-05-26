package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;

public class SelectedMidiNotesPinchEvent extends MidiNotesEvent {

	protected long onTickDiff, offTickDiff;

	public SelectedMidiNotesPinchEvent() {
		super();
	}

	public SelectedMidiNotesPinchEvent(long onTickDiff, long offTickDiff) {
		this.onTickDiff = onTickDiff;
		this.offTickDiff = offTickDiff;
	}

	public void execute() {
		TrackManager.pinchSelectedNotes(onTickDiff, offTickDiff);
		MidiManager.handleMidiCollisions();
	}
}
