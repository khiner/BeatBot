package com.kh.beatbot.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Instrument {
	private String name;
	private BeatBotIconSource icon;
	private File[] sampleFiles;
	private String[] sampleNames;
	
	public Instrument(String instrumentName, BeatBotIconSource instrumentIcon) {
		this.name = instrumentName;
		this.icon = instrumentIcon;
		File dir = new File(GlobalVars.appDirectory + instrumentName);
		sampleFiles = dir.listFiles();
		sampleNames = dir.list();
	}
	
	public byte[] getSampleBytes(int sampleNum) {
		byte[] bytes = null;
		try {
			File sampleFile = sampleFiles[sampleNum];
			FileInputStream in = new FileInputStream(sampleFile);
			bytes = new byte[(int)sampleFile.length()];
			in.read(bytes);
			in.close();
			in = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}
	
	public String getName() {
		return name;
	}

	public BeatBotIconSource getIcon() {
		return icon;
	}

	public void setIconResources(int defaultIconResourceId, int selectedIconResourceId) {
		this.icon.set(defaultIconResourceId, selectedIconResourceId);
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
}
