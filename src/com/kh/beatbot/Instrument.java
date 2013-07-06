package com.kh.beatbot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.ui.IconResource;

public class Instrument extends Directory {

	public Instrument(Directory parent, String name, IconResource iconResource) {
		super(parent, name, iconResource);
	}

	public SampleFile createSampleFile(int sampleNum) {
		return new SampleFile(getSampleFiles().get(sampleNum));
	}

	@Override
	public String[] getChildNames() {
		List<File> sampleFiles = getSampleFiles();
		String[] sampleNames = new String[sampleFiles.size()];
		for (int i = 0; i < sampleFiles.size(); i++) {
			sampleNames[i] = sampleFiles.get(i).getName();
		}
		return sampleNames;
	}

	public String getSampleName(int sampleNum) {
		return getSampleFiles().get(sampleNum).getName();
	}

	public String getBasePath() {
		return path;
	}

	public String getFullPath(int sampleNum) {
		return getSampleFiles().get(sampleNum).getAbsolutePath();
	}

	private List<File> getSampleFiles() {
		List<File> sampleFiles = new ArrayList<File>();
		for (File sampleFile : new File(path).listFiles()) {
			if (!sampleFile.getAbsolutePath().contains(".raw")) {
				sampleFiles.add(sampleFile);
			}
		}
		return sampleFiles;
	}
}
