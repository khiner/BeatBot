package com.kh.beatbot.global;

import java.io.File;

public class Instrument {
	private String name;
	private BeatBotIconSource bbIconSource;
	private File[] sampleFiles;
	private String[] sampleNames;
	private int iconSource;

	public Instrument(String instrumentName, BeatBotIconSource instrumentIcon) {
		this.name = instrumentName;
		this.bbIconSource = instrumentIcon;
		File dir = new File(GlobalVars.appDirectory + instrumentName);
		sampleFiles = dir.listFiles();
		sampleNames = dir.list();
	}

	public String getName() {
		return name;
	}
	
	public BeatBotIconSource getBBIconSource() {
		return bbIconSource;
	}

	public int getIconSource() {
		return iconSource;
	}

	public void setIconResources(int iconSource, int defaultIconResource,
			int selectedIconResource, int listViewIconResource) {
		this.iconSource = iconSource;
		bbIconSource.set(defaultIconResource, selectedIconResource,
				listViewIconResource);
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
		return GlobalVars.appDirectory + name + "/" + getSampleName(sampleNum);
	}
	
	public long getNumSamples(int sampleNum) {
		return getSampleFile(sampleNum).length() / 8 - 44;
	}
}
