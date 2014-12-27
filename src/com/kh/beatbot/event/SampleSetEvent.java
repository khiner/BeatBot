package com.kh.beatbot.event;

import java.io.File;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.TrackManager;
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
	public void undo() {
		doExecute(originalSample);
	}

	@Override
	public void redo() {
		doExecute(newSample);
	}

	@Override
	public void execute() {
		if (doExecute(newSample)) {
			EventManager.eventCompleted(this);
		}
	}

	public boolean doExecute(File sample) {
		if (sample == null || sample.equals(track.getCurrSampleFile())) {
			return false;
		}
		TrackManager.setSample(track, sample);
		View.mainPage.pageSelectGroup.selectBrowsePage();
		return true;
	}
}
