package com.kh.beatbot.event;

import java.io.File;

import com.kh.beatbot.Track;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.View;

public class SampleRenameEvent implements Executable, Stateful {

	private String originalSampleName, newSampleName;
	private File file;

	public SampleRenameEvent(File file, String sampleName) {
		this.file = file;
		originalSampleName = file.getName();
		newSampleName = sampleName;
	}

	@Override
	public void doUndo() {
		doExecute(originalSampleName);
		updateUi();
	}

	@Override
	public void doRedo() {
		doExecute(newSampleName);
		updateUi();
	}

	@Override
	public void updateUi() {
		View.mainPage.pageSelectGroup.update();
		View.mainPage.pageSelectGroup.updateBrowsePage();
	}

	@Override
	public void execute() {
		if (doExecute(newSampleName)) {
			updateUi();
			EventManager.eventCompleted(this);
		}
	}

	private boolean doExecute(String sampleName) {
		if (file == null || sampleName == null || sampleName.isEmpty()
				|| newSampleName.equals(originalSampleName)) {
			return false;
		}

		File newFile = new File(file.getParent() + "/" + sampleName);
		file.renameTo(newFile);
		for (Track track : TrackManager.getTracks()) {
			if (track.getCurrSampleFile().equals(file)) {
				track.updateSampleFile(newFile);
			}
		}
		file = newFile;
		return true;
	}
}
