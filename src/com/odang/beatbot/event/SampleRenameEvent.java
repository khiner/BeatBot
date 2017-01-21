package com.odang.beatbot.event;

import java.io.File;

import com.odang.beatbot.ui.view.View;

public class SampleRenameEvent extends Executable {
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
		View.context.getFileManager().onNameChange(file, newFile);
		file = newFile;
		return true;
	}
}
