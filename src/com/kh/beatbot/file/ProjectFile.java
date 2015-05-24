package com.kh.beatbot.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.track.TrackSerializer;

public class ProjectFile {
	private static final String TEMPO_KEY = "tempo";
	private static final String LOOP_BEGIN_TICK_KEY = "loopBeginTick";
	private static final String LOOP_END_TICK_KEY = "loopEndTick";

	private String path;
	private final static Gson GSON = new GsonBuilder().registerTypeAdapter(BaseTrack.class, new TrackSerializer()).create();
	private final static JsonParser parser = new JsonParser();

	public ProjectFile(String path) {
		this.path = path;
	}

	public void save() throws IOException {
		FileOutputStream outputStream = new FileOutputStream(new File(path));

		// global properties
		JsonObject globalProperties = new JsonObject();
		globalProperties.addProperty(LOOP_BEGIN_TICK_KEY, MidiManager.getLoopBeginTick());
		globalProperties.addProperty(LOOP_END_TICK_KEY, MidiManager.getLoopEndTick());
		globalProperties.addProperty(TEMPO_KEY, MidiManager.getBPM());

		outputStream.write((globalProperties.toString() + "\n").getBytes());

		// tracks
		BaseTrack masterTrack = TrackManager.getMasterTrack();
		outputStream.write((toJson(masterTrack) + "\n").getBytes());
		for (Track track : TrackManager.getTracks()) {
			String trackJson = toJson(track) + "\n";
			outputStream.write(trackJson.getBytes());
		}

		outputStream.close();
	}

	public void load() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));

		// global properties
		JsonObject globalProperties = parser.parse(reader.readLine()).getAsJsonObject();
		MidiManager.setLoopBeginTick(globalProperties.get(LOOP_BEGIN_TICK_KEY).getAsLong());
		MidiManager.setLoopEndTick(globalProperties.get(LOOP_END_TICK_KEY).getAsLong());
		MidiManager.setBPM(globalProperties.get(TEMPO_KEY).getAsFloat());

		// tracks
		String serializedTrack;
		while ((serializedTrack = reader.readLine()) != null) {
			fromJson(serializedTrack);
		}
		reader.close();
	}

	public static String toJson(BaseTrack track) {
		return GSON.toJson(track, BaseTrack.class);
	}
	
	public static BaseTrack fromJson(String serializedTrack) {
		return GSON.fromJson(serializedTrack, BaseTrack.class);
	}
}
