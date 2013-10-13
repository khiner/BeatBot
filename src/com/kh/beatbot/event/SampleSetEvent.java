package com.kh.beatbot.event;

import com.kh.beatbot.SampleFile;
import com.kh.beatbot.Track;
import com.kh.beatbot.ui.view.page.Page;

public class SampleSetEvent implements Executable, Stateful {

	private Track track;
	private SampleFile originalSample, newSample;

	public SampleSetEvent(Track track, SampleFile sample) {
		this.track = track;
		originalSample = track.getCurrSampleFile();
		newSample = sample;
	}

	@Override
	public void doUndo() {
		doExecute(originalSample);
	}

	@Override
	public void doRedo() {
		doExecute(newSample);
	}

	@Override
	public void updateUi() {
		Page.mainPage.notifyTrackUpdated(track);
	}

	@Override
	public void execute() {
		if (doExecute(newSample)) {
			EventManager.eventCompleted(this);
			updateUi();
		}
	}

	public boolean doExecute(SampleFile sample) {
		if (sample == null || track.getCurrSampleFile().equals(sample)) {
			return false;
		}
		track.setSample(sample);
		return true;
	}
}
