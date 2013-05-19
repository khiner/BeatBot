package com.kh.beatbot.global;

import java.io.File;
import java.io.IOException;

public class Instrument extends BBDirectory {
	private WavFile[] sampleFiles;
	private String[] sampleNames;

	public Instrument(BBDirectory parent, String name,
			ImageIconSource bbIconSource) {
		super(parent, name, bbIconSource);
		updateFiles();
	}

	public WavFile getWavFile(int sampleNum) {
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
		sampleFiles[sampleNum].renameTo(path + name);
	}

	public String getSamplePath(int sampleNum) {
		return path + getSampleName(sampleNum);
	}

	public long getNumSamples(int sampleNum) {
		return sampleFiles[sampleNum].getNumSamples();
	}

	public void updateFiles() {
		File dir = new File(path);
		sampleNames = dir.list();
		sampleFiles = new WavFile[dir.list().length];
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			sampleFiles[i] = new WavFile(files[i]);
		}
	}
}
