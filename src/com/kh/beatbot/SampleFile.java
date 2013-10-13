package com.kh.beatbot;

import java.io.File;

public class SampleFile {
	private Instrument instrument;
	private File file;

	public SampleFile(Instrument instrument, File file) {
		this.instrument = instrument;
		this.file = file;
	}

	public void renameTo(String name) {
		File newFile = new File(instrument.getBasePath() + name);
		file.renameTo(newFile);
		file = newFile;
	}

	public String getName() {
		return file.getName();
	}
	
	public String getFullPath() {
		return file.getAbsolutePath();
	}
	
	public Instrument getInstrument() {
		return instrument;
	}
}
