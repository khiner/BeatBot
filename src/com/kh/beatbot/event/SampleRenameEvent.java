package com.kh.beatbot.event;

import java.io.File;

import com.kh.beatbot.manager.FileManager;

public class SampleRenameEvent extends Executable {
	private transient FileManager fileManager;
	private String originalSampleName, newSampleName;
	private File file;

	public SampleRenameEvent(FileManager fileManager, File file, String sampleName) {
		this.fileManager = fileManager;
		this.file = file;
		originalSampleName = file.getName();
		newSampleName = sampleName;
	}

	@Override
	public void undo() {
		doExecute(originalSampleName);
	}

	public boolean doExecute() {
		return doExecute(newSampleName);
	}

	private boolean doExecute(String sampleName) {
		if (file == null || sampleName == null || sampleName.isEmpty()
				|| newSampleName.equals(originalSampleName)) {
			return false;
		}

		File newFile = new File(file.getParent() + "/" + sampleName);
		file.renameTo(newFile);
		fileManager.onNameChange(file, newFile);
		file = newFile;
		return true;
	}
}
