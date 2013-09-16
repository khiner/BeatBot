package com.kh.beatbot;

import java.io.File;

public class SampleFile {
	private File file;

	public SampleFile(File file) {
		this.file = file;
	}

	public void renameTo(String name) {
		File newFile = new File(name);
		file.renameTo(newFile);
		file = newFile;
	}

	public String getName() {
		return file.getName();
	}
	
	public String getFullPath() {
		return file.getAbsolutePath();
	}
}
