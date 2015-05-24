package com.kh.beatbot.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.track.TrackSerializer;

public class ProjectFile {
	private String path;
	private final static Gson GSON = new GsonBuilder().registerTypeAdapter(BaseTrack.class, new TrackSerializer()).create();

	public ProjectFile(String path) {
		this.path = path;
	}

	public void load() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		String serializedTrack;
		while ((serializedTrack = reader.readLine()) != null) {
			final BaseTrack track = (BaseTrack) fromJson(serializedTrack); 
			//TrackManager.createTrack(track);
		}
		reader.close();
	}

	public void save() throws IOException {
		FileOutputStream outputStream = new FileOutputStream(new File(path));
		BaseTrack masterTrack = TrackManager.getMasterTrack();
		outputStream.write((toJson(masterTrack) + "\n").getBytes());
		for (Track track : TrackManager.getTracks()) {
			String trackJson = toJson(track) + "\n";
			outputStream.write(trackJson.getBytes());
		}

		outputStream.close();
	}

	public static String toJson(BaseTrack track) {
		return GSON.toJson(track, BaseTrack.class);
	}
	
	public static BaseTrack fromJson(String serializedTrack) {
		return GSON.fromJson(serializedTrack, BaseTrack.class);
	}
}
