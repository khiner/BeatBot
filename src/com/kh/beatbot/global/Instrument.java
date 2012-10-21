package com.kh.beatbot.global;

import java.io.File;

public class Instrument {
	private String name;
	private BeatBotIconSource bbIconSource;
	private File[] sampleFiles;
	private String[] sampleNames;
	private int currSampleNum;
	private int iconSource;

	public Instrument(String instrumentName, BeatBotIconSource instrumentIcon) {
		this.name = instrumentName;
		this.bbIconSource = instrumentIcon;
		File dir = new File(GlobalVars.appDirectory + instrumentName);
		sampleFiles = dir.listFiles();
		sampleNames = dir.list();
		currSampleNum = 0;
	}

	public void setCurrSampleNum(int currSampleNum) {
		this.currSampleNum = currSampleNum;
	}

	public int getCurrSampleNum() {
		return currSampleNum;
	}

	public String getName() {
		return name;
	}

	public String getCurrSampleName() {
		return getSampleName(currSampleNum);
	}

	public File getCurrSampleFile() {
		return sampleFiles[currSampleNum];
	}
	
	public String getCurrSamplePath() {
		return GlobalVars.appDirectory + name + "/" + getCurrSampleName();
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
}
