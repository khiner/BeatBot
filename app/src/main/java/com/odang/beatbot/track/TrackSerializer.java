package com.odang.beatbot.track;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.effect.EffectSerializer;
import com.odang.beatbot.manager.TrackManager;
import com.odang.beatbot.midi.MidiNote;
import com.odang.beatbot.ui.view.View;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TrackSerializer implements JsonSerializer<BaseTrack>, JsonDeserializer<BaseTrack> {
    private final static Gson GSON = new GsonBuilder().registerTypeAdapter(Effect.class,
            new EffectSerializer()).create();

    private Type noteListType = new TypeToken<ArrayList<MidiNote>>() {
    }.getType();
    private Type effectListType = new TypeToken<ArrayList<Effect>>() {
    }.getType();

    @Override
    public BaseTrack deserialize(JsonElement trackJson, Type type,
                                 JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = trackJson.getAsJsonObject();
        int id = object.get("id").getAsInt();
        int position = object.get("position").getAsInt();
        float volume = object.get("volume").getAsFloat();
        float pan = object.get("pan").getAsFloat();
        float pitch = object.get("pitch").getAsFloat();
        float pitchCent = object.get("pitchCent").getAsFloat();

        boolean isMaster = id == TrackManager.MASTER_TRACK_ID;
        BaseTrack track = isMaster ? View.context.getTrackManager().getMasterTrack() : View.context
                .getTrackManager().createTrack(id, position);

        track.getVolumeParam().setLevel(volume);
        track.getPanParam().setLevel(pan);
        track.getPitchParam().setLevel(pitch);
        track.getPitchCentParam().setLevel(pitchCent);

        // effects are added to the track in the Effect constructor called by EffectSerializer
        GSON.fromJson(object.get("effects"), effectListType);

        if (isMaster)
            return track;

        Track t = (Track) track;
        if (object.has("samplePath")) {
            try {
                t.setSample(new File(object.get("samplePath").getAsString()));
            } catch (Exception e) {
                Log.e(getClass().getName(), e.toString());
            }

            t.getLoopBeginParam().setLevelWithoutNotify(object.get("sampleLoopBegin").getAsFloat());
            t.getLoopEndParam().setLevelWithoutNotify(object.get("sampleLoopEnd").getAsFloat());
            t.getGainParam().setLevelWithoutNotify(object.get("gain").getAsFloat());
            t.setReverseWithoutNotify(object.get("reverse").getAsBoolean());
        }

        List<MidiNote> notes = GSON.fromJson(object.get("notes"), noteListType);
        for (MidiNote note : notes) {
            note.create();
        }
        
        t.mute(object.get("muted").getAsBoolean());
        t.solo(object.get("soloing").getAsBoolean());

        return t;
    }

    @Override
    public JsonElement serialize(BaseTrack track, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.addProperty("id", track.getId());
        object.addProperty("position", View.context.getTrackManager().getTracks().indexOf(track));

        object.addProperty("volume", track.getVolumeParam().viewLevel);
        object.addProperty("pan", track.getPanParam().viewLevel);
        object.addProperty("pitch", track.getPitchParam().viewLevel);
        object.addProperty("pitchCent", track.getPitchCentParam().viewLevel);

        object.add("effects", GSON.toJsonTree(track.getEffects(), effectListType).getAsJsonArray());

        if (track instanceof Track) {
            Track t = (Track) track;
            object.addProperty("class", Track.class.getName());
            object.addProperty("muted", t.isMuted());
            object.addProperty("soloing", t.isSoloing());

            if (t.getCurrSampleFile() != null) {
                object.addProperty("samplePath", t.getCurrSampleFile().getAbsolutePath());
                object.addProperty("sampleLoopBegin", t.getLoopBeginParam().viewLevel);
                object.addProperty("sampleLoopEnd", t.getLoopEndParam().viewLevel);
                object.addProperty("gain", t.getGainParam().viewLevel);
                object.addProperty("reverse", t.isReverse());
            }

            object.add("notes", GSON.toJsonTree(t.getMidiNotes()).getAsJsonArray());
        } else {
            object.addProperty("class", BaseTrack.class.getName());
        }

        return object;
    }
}
