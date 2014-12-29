package com.kh.beatbot.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.kh.beatbot.event.EventJsonFactory;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;

public class ProjectFile {
	private File projectFile;
	private FileOutputStream outputStream;

	public ProjectFile(String path) {
		projectFile = new File(path);
		try {
			outputStream = new FileOutputStream(projectFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public ProjectFile(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String serializedEvent;
        while ((serializedEvent = reader.readLine()) != null) {
            final Stateful event = EventJsonFactory.fromJson(serializedEvent);
            ((Executable)event).execute();
        }
	}

	public void writeEvent(Stateful event) {
		String eventJson = EventJsonFactory.toJson(event) + "\n";
		try {
			outputStream.write(eventJson.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		outputStream.close();
	}
}
