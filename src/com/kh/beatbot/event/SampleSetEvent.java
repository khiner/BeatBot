package com.kh.beatbot.event;

import java.io.File;

import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.Track;
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
		if (sample == null || sample.equals(track.getCurrSampleFile())) {
			return false;
		}
		TrackManager.setSample(track, sample);
		View.mainPage.pageSelectGroup.selectBrowsePage();
		return true;
	}
}
