package com.kh.beatbot.event;

import com.kh.beatbot.Track;
import com.kh.beatbot.ui.view.page.Page;

public class SampleRenameEvent implements Executable, Stateful {

	private String originalSampleName, newSampleName;
	private Track track;

	public SampleRenameEvent(Track track, String sampleName) {
		this.track = track;
		originalSampleName = track.getCurrSampleName();
		newSampleName = sampleName;
	}

	@Override
	public void doUndo() {
		doExecute(originalSampleName);
	}

	@Override
	public void doRedo() {
		doExecute(newSampleName);
	}

	@Override
	public void updateUi() {
		Page.mainPage.pageSelectGroup.update();
	}

	@Override
	public void execute() {
		if (doExecute(newSampleName)) {
			updateUi();
			EventManager.eventCompleted(this);
		}
	}

	private boolean doExecute(String sampleName) {
		if (track == null || sampleName == null || sampleName.isEmpty()
				|| newSampleName.equals(originalSampleName)) {
			return false;
		}
		track.setCurrSampleName(sampleName);
		return true;
	}
}
