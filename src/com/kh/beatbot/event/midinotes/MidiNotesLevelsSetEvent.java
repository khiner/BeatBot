package com.kh.beatbot.event.midinotes;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.Track;
import com.kh.beatbot.effect.Effect.LevelType;
import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Temporal;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;

public class MidiNotesLevelsSetEvent extends MidiNotesEvent implements Temporal {
	private class MidiNoteLevelsDiff {
		byte beginVelocity, endVelocity, beginPan, endPan, beginPitch, endPitch;
	}

	private final Track track;
	private final List<MidiNoteLevelsDiff> levelsDiffs;

	public MidiNotesLevelsSetEvent(Track track) {
		super(track.getMidiNotes());
		this.track = track;

		levelsDiffs = new ArrayList<MidiNoteLevelsDiff>(midiNotes.size());
	}

	@Override
	public void undo() {
		for (int i = 0; i < midiNotes.size(); i++) {
			MidiNote note = midiNotes.get(i);
			MidiNoteLevelsDiff diff = levelsDiffs.get(i);
			note.setLevel(LevelType.VOLUME, diff.beginVelocity);
			note.setLevel(LevelType.PAN, diff.beginPan);
			note.setLevel(LevelType.PITCH, diff.beginPitch);
		}
	}

	@Override
	public void redo() {
		doExecute();
	}

	@Override
	public void execute() {
		doExecute();
		EventManager.eventCompleted(this);
		TrackManager.notifyNoteLevelsSetEvent(track);
	}

	public void doExecute() {
		for (int i = 0; i < midiNotes.size(); i++) {
			MidiNote note = midiNotes.get(i);
			MidiNoteLevelsDiff diff = levelsDiffs.get(i);
			note.setLevel(LevelType.VOLUME, diff.endVelocity);
			note.setLevel(LevelType.PAN, diff.endPan);
			note.setLevel(LevelType.PITCH, diff.endPitch);
		}
	}

	@Override
	public void begin() {
		for (int i = 0; i < midiNotes.size(); i++) {
			MidiNote note = midiNotes.get(i);
			MidiNoteLevelsDiff diff = levelsDiffs.get(i);
			diff.beginVelocity = note.getVelocity();
			diff.beginPan = note.getPan();
			diff.beginPitch = note.getPitch();
		}
	}

	@Override
	public void end() {
		for (int i = 0; i < midiNotes.size(); i++) {
			MidiNote note = midiNotes.get(i);
			MidiNoteLevelsDiff diff = levelsDiffs.get(i);
			diff.endVelocity = note.getVelocity();
			diff.endPan = note.getPan();
			diff.endPitch = note.getPitch();
		}
	}
}
