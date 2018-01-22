package com.odang.beatbot.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.effect.EffectSerializer;
import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.track.TrackSerializer;
import com.odang.beatbot.ui.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProjectFile {
    private static final String TEMPO = "tempo";
    private static final String SNAP_TO_GRID = "snapToGrid";
    private static final String LOOP_BEGIN_TICK = "loopBeginTick";
    private static final String LOOP_END_TICK = "loopEndTick";
    private static final String CURR_TRACK_ID = "currTrackId";
    private static final String CURR_PAGE_INDEX = "currPageIndex";

    private String path;
    private final static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BaseTrack.class, new TrackSerializer())
            .registerTypeAdapter(Effect.class, new EffectSerializer()).create();
    private final static JsonParser parser = new JsonParser();

    public ProjectFile(String path) {
        this.path = path;
    }

    public void save() throws IOException {
        FileOutputStream outputStream = new FileOutputStream(new File(path));

        // global properties
        JsonObject globalProperties = new JsonObject();
        globalProperties.addProperty(LOOP_BEGIN_TICK, View.context.getMidiManager()
                .getLoopBeginTick());
        globalProperties.addProperty(LOOP_END_TICK, View.context.getMidiManager().getLoopEndTick());
        globalProperties.addProperty(TEMPO, View.context.getMidiManager().getBpm());
        globalProperties.addProperty(SNAP_TO_GRID, View.context.getMidiManager().isSnapToGrid());
        globalProperties.addProperty(CURR_TRACK_ID, View.context.getTrackManager().getCurrTrack()
                .getId());
        globalProperties.addProperty(CURR_PAGE_INDEX, View.context.getPageSelectGroup()
                .getCurrPageIndex());

        outputStream.write((globalProperties.toString() + "\n").getBytes());

        // tracks
        BaseTrack masterTrack = View.context.getTrackManager().getMasterTrack();
        outputStream.write((trackToJson(masterTrack) + "\n").getBytes());
        for (Track track : View.context.getTrackManager().getTracks()) {
            String trackJson = trackToJson(track) + "\n";
            outputStream.write(trackJson.getBytes());
        }

        outputStream.close();
    }

    public void load() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));

        // global properties
        JsonObject globalProperties = parser.parse(reader.readLine()).getAsJsonObject();
        View.context.getMidiManager().setLoopTicks(
                globalProperties.get(LOOP_BEGIN_TICK).getAsLong(),
                globalProperties.get(LOOP_END_TICK).getAsLong());
        View.context.getMidiManager().setBpm(globalProperties.get(TEMPO).getAsFloat());
        View.context.getMidiManager().setSnapToGrid(
                globalProperties.get(SNAP_TO_GRID).getAsBoolean());

        // Tracks are added to the project in during deserialization in the TrackSerializer class
        String serializedTrack;
        while ((serializedTrack = reader.readLine()) != null) {
            trackFromJson(serializedTrack);
        }

        View.context.getTrackManager().deselectAllNotes();
        View.context.getTrackManager().getBaseTrackById(
                globalProperties.get(CURR_TRACK_ID).getAsInt()).select();
        View.context.getPageSelectGroup().selectPage(
                globalProperties.get(CURR_PAGE_INDEX).getAsInt());

        reader.close();
    }

    public static String trackToJson(BaseTrack track) {
        return GSON.toJson(track, BaseTrack.class);
    }

    public static BaseTrack trackFromJson(String serializedTrack) {
        return GSON.fromJson(serializedTrack, BaseTrack.class);
    }

    public static String effectToJson(Effect effect) {
        return GSON.toJson(effect, Effect.class);
    }

    public static Effect effectFromJson(String serializedEffect) {
        return GSON.fromJson(serializedEffect, Effect.class);
    }
}
