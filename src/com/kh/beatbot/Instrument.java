package com.kh.beatbot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.ui.IconResource;

public class Instrument extends Directory {

	private List<SampleFile> samples = new ArrayList<SampleFile>();

	public Instrument(Directory parent, String name, IconResource iconResource) {
		super(parent, name, iconResource);
		initSampleFiles();
	}

	public SampleFile getSample(int sampleNum) {
		return samples.get(sampleNum);
	}

	@Override
	public String[] getChildNames() {
		String[] sampleNames = new String[samples.size()];
		for (int i = 0; i < samples.size(); i++) {
			sampleNames[i] = samples.get(i).getName();
		}
		return sampleNames;
	}

	public String getSampleName(int sampleNum) {
		return samples.get(sampleNum).getName();
	}

	public String getBasePath() {
		return path;
	}

	private void initSampleFiles() {
		for (File sampleFile : new File(path).listFiles()) {
			if (!sampleFile.getAbsolutePath().contains(".raw")) {
				samples.add(new SampleFile(this, sampleFile));
			}
		}
	}
}
