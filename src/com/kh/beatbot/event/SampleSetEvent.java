package com.kh.beatbot.event;

import java.io.File;

import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.Track;

public class SampleSetEvent implements Executable, Stateful {
	private int trackId;
	private File originalSample, newSample;

	public SampleSetEvent(int trackId, File sampleFile) {
		this.trackId = trackId;
		Track track = TrackManager.getTrackById(trackId);
		originalSample = track.getCurrSampleFile();
		newSample = sampleFile;
	}

	@Override
	public void undo() {
		doExecute(originalSample);
	}

	@Override
	public void apply() {
		doExecute(newSample);
	}

	@Override
	public void execute() {
		if (doExecute(newSample)) {
			EventManager.eventCompleted(this);
		}
	}

	public void doExecute() {
		doExecute(newSample);
	}

	public boolean doExecute(File sample) {
		Track track = TrackManager.getTrackById(trackId);
		if (sample == null || sample.equals(track.getCurrSampleFile())) {
			return false;
		}
		TrackManager.setSample(track, sample);
		return true;
	}
}
