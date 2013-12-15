package com.kh.beatbot.event;

import java.io.File;

import com.kh.beatbot.Track;
import com.kh.beatbot.ui.view.View;

public class SampleSetEvent implements Executable, Stateful {

	private Track track;
	private File originalSample, newSample;

	public SampleSetEvent(Track track, File sampleFile) {
		this.track = track;
		originalSample = track.getCurrSampleFile();
		newSample = sampleFile;
	}

	@Override
	public void doUndo() {
		doExecute(originalSample);
		updateUi();
	}

	@Override
	public void doRedo() {
		doExecute(newSample);
		updateUi();
	}

	@Override
	public void updateUi() {
		View.mainPage.notifyTrackUpdated(track);
	}

	@Override
	public void execute() {
		if (doExecute(newSample)) {
			EventManager.eventCompleted(this);
			updateUi();
		}
	}

	public boolean doExecute(File sample) {
		if (sample == null || sample.equals(track.getCurrSampleFile())) {
			return false;
		}
		track.setSample(sample);
		return true;
	}
}
