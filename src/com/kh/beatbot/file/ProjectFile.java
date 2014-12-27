package com.kh.beatbot.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

import com.kh.beatbot.event.EventJsonFactory;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.listener.StatefulEventListener;

public class ProjectFile implements StatefulEventListener {
	private File workingFile;
	private FileOutputStream workingOutputStream;

	public ProjectFile(String path) {
		workingFile = new File(path);
		try {
			workingOutputStream = new FileOutputStream(workingFile);
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
			workingOutputStream.write(eventJson.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeToFile(File outFile) throws FileNotFoundException, IOException {
		copyFile(workingFile, outFile);
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	@Override
	public void onEventCompleted(Stateful event) {
		writeEvent(event);
	}
}
