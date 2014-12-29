package com.kh.beatbot.event;

import java.io.File;

import com.kh.beatbot.manager.FileManager;

public class SampleRenameEvent implements Executable, Stateful {

	private String originalSampleName, newSampleName;
	private File file;

	public SampleRenameEvent(File file, String sampleName) {
		this.file = file;
		originalSampleName = file.getName();
		newSampleName = sampleName;
	}

	@Override
	public void undo() {
		doExecute(originalSampleName);
	}

	@Override
	public void redo() {
		doExecute(newSampleName);
	}

	@Override
	public void execute() {
		if (doExecute(newSampleName)) {
			EventManager.eventCompleted(this);
		}
	}

	public void doExecute() {
		doExecute(newSampleName);
	}

	private boolean doExecute(String sampleName) {
		if (file == null || sampleName == null || sampleName.isEmpty()
				|| newSampleName.equals(originalSampleName)) {
			return false;
		}

		File newFile = new File(file.getParent() + "/" + sampleName);
		file.renameTo(newFile);
		FileManager.get().onNameChange(file, newFile);
		file = newFile;
		return true;
	}
}
