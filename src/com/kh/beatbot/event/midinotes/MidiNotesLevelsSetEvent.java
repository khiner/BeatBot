package com.kh.beatbot.event.midinotes;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.TrackManager;

public class MidiNotesLevelsSetEvent extends MidiNotesGroupEvent {
	private Track track;

	public MidiNotesLevelsSetEvent(Track track) {
		this.track = track;
	}

	public synchronized void restore() {
		super.restore();
		TrackManager.notifyNoteLevelsSetEvent(track);
	}
}
