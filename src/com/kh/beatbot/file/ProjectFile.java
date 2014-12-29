package com.kh.beatbot.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.kh.beatbot.event.EventJsonFactory;
import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;

public class ProjectFile {
	private String path;

	public ProjectFile(String path) {
		this.path = path;
	}

	public void load() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		String serializedEvent;
		while ((serializedEvent = reader.readLine()) != null) {
			try {
				Integer eventIndex = Integer.valueOf(serializedEvent);
				EventManager.jumpTo(eventIndex);
			} catch (NumberFormatException e) { // this is an event, not the event index
				final Stateful event = EventJsonFactory.fromJson(serializedEvent);
				// XXX not using simple execute() since midi events won't make it to the
				// EventManager if there's no batch midi event
				((Executable) event).doExecute();
				EventManager.eventCompleted(event);
			}
		}
		reader.close();
	}

	public void save() throws IOException {
		FileOutputStream outputStream = new FileOutputStream(new File(path));
		for (Stateful event : EventManager.getEvents()) {
			String eventJson = EventJsonFactory.toJson(event) + "\n";
			outputStream.write(eventJson.getBytes());
		}

		outputStream.write(String.valueOf(EventManager.getCurrentEventIndex()).getBytes());
		outputStream.close();
	}
}
