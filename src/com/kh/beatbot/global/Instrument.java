package com.kh.beatbot.global;

import java.io.File;

public class Instrument extends BBDirectory {
	private File[] sampleFiles;
	private String[] sampleNames;

	public Instrument(BBDirectory parent, String name, ImageIconSource bbIconSource) {
		super(parent, name, bbIconSource);
		updateFiles();
	}

	public File getSampleFile(int sampleNum) {
		return sampleFiles[sampleNum];
	}

	@Override
	public String[] getChildNames() {
		return sampleNames;
	}

	public String getSampleName(int sampleNum) {
		return sampleNames[sampleNum];
	}

	public void setSampleName(int sampleNum, String name) {
		sampleNames[sampleNum] = name;
		getSampleFile(sampleNum).renameTo(new File(path + name));
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
