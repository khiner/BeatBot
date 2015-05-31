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
import com.kh.beatbot.ui.view.View;

public class ProjectFile {
	private static final String TEMPO = "tempo";
	private static final String SNAP_TO_GRID = "snapToGrid";
	private static final String LOOP_BEGIN_TICK = "loopBeginTick";
	private static final String LOOP_END_TICK = "loopEndTick";
	private static final String CURR_TRACK_ID = "currTrackId";
	private static final String CURR_PAGE_INDEX = "currPageIndex";

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
		globalProperties.addProperty(LOOP_BEGIN_TICK, MidiManager.getLoopBeginTick());
		globalProperties.addProperty(LOOP_END_TICK, MidiManager.getLoopEndTick());
		globalProperties.addProperty(TEMPO, MidiManager.getBPM());
		globalProperties.addProperty(SNAP_TO_GRID, MidiManager.isSnapToGrid());
		globalProperties.addProperty(CURR_TRACK_ID, TrackManager.getCurrTrack().getId());
		globalProperties.addProperty(CURR_PAGE_INDEX, View.mainPage.pageSelectGroup.getCurrPageIndex());

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
		MidiManager.setLoopBeginTick(globalProperties.get(LOOP_BEGIN_TICK).getAsLong());
		MidiManager.setLoopEndTick(globalProperties.get(LOOP_END_TICK).getAsLong());
		MidiManager.setBPM(globalProperties.get(TEMPO).getAsFloat());
		MidiManager.setSnapToGrid(globalProperties.get(SNAP_TO_GRID).getAsBoolean());

		// tracks
		String serializedTrack;
		while ((serializedTrack = reader.readLine()) != null) {
			fromJson(serializedTrack);
		}

		TrackManager.getBaseTrackById(globalProperties.get(CURR_TRACK_ID).getAsInt()).select();
		View.mainPage.pageSelectGroup.selectPage(globalProperties.get(CURR_PAGE_INDEX).getAsInt());

		reader.close();
	}

	public static String toJson(BaseTrack track) {
		return GSON.toJson(track, BaseTrack.class);
	}
	
	public static BaseTrack fromJson(String serializedTrack) {
		return GSON.fromJson(serializedTrack, BaseTrack.class);
	}
}
