package com.kh.beatbot.global;

import java.io.File;

public class Instrument extends BBDirectory {
	private File[] sampleFiles;
	private String[] sampleNames;

	public Instrument(BBDirectory parent, String name, BeatBotIconSource bbIconSource) {
		super(parent, name, bbIconSource);
		updateFiles();
	}

	public BeatBotIconSource getBBIconSource() {
		if (bbIconSource == null)
			return parent.bbIconSource;
		return bbIconSource;
	}

	public File getSampleFile(int sampleNum) {
		return sampleFiles[sampleNum];
	}

	public String[] getSampleNames() {
		return sampleNames;
	}

	public String getSampleName(int sampleNum) {
		return sampleNames[sampleNum];
	}
	
	public String getSamplePath(int sampleNum) {
		return path + getSampleName(sampleNum);
	}
	
	public long getNumSamples(int sampleNum) {
		return getSampleFile(sampleNum).length() / 8 - 44;
	}
	
	public void updateFiles() {
		File dir = new File(path);
		sampleFiles = dir.listFiles();
		sampleNames = dir.list();
	}
}
