package com.kh.beatbot.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProjectFile {
	public ProjectFile() {
	}

	public ProjectFile(InputStream rawIn) throws IOException {
		BufferedInputStream in = new BufferedInputStream(rawIn);
		//TODO
	}
	
	public void writeToFile(File outFile) throws FileNotFoundException, IOException {

		FileOutputStream fout = new FileOutputStream(outFile);

		fout.write("project".getBytes()); // TODO

		fout.flush();
		fout.close();
	}
}
