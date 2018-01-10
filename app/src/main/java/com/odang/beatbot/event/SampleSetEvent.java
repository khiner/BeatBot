package com.odang.beatbot.event;

import java.io.File;

import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.view.View;

public class SampleSetEvent extends Executable {
	private int trackId;
	private File originalSample, newSample;

	public SampleSetEvent(int trackId, File sampleFile) {
		this.trackId = trackId;
		Track track = View.context.getTrackManager().getTrackById(trackId);
		originalSample = track.getCurrSampleFile();
		newSample = sampleFile;
	}

	@Override
	public void undo() {
		doExecute(originalSample);
	}

	public boolean doExecute() {
		return doExecute(newSample);
	}

	private boolean doExecute(File sample) {
		Track track = View.context.getTrackManager().getTrackById(trackId);
		if (sample == null || sample.equals(track.getCurrSampleFile())) {
			return false;
		}
		View.context.getTrackManager().setSample(track, sample);
		return true;
	}
}
