package com.kh.beatbot.track;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;

public class TrackSerializer implements JsonSerializer<Track>, JsonDeserializer<Track> {
	private static final Gson GSON = new GsonBuilder().create();
	private Type noteListType = new TypeToken<ArrayList<MidiNote>>() {
	}.getType();

	@Override
	public Track deserialize(JsonElement trackJson, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject object = trackJson.getAsJsonObject();
		int id = object.get("id").getAsInt();
		int position = object.get("position").getAsInt();
		Track track = TrackManager.createTrack(id, position);

		try {
			track.setSample(new File(object.get("samplePath").getAsString()));
		} catch (Exception e) {
			Log.e(getClass().getName(), e.toString());
		}
		
		List<MidiNote> notes = GSON.fromJson(object.get("notes"), noteListType);
		for (MidiNote note : notes) {
			note.create();
		}

		track.getVolumeParam().setLevel(object.get("volume").getAsFloat());
		track.getPanParam().setLevel(object.get("pan").getAsFloat());
		track.getPitchParam().setLevel(object.get("pitch").getAsFloat());
		track.getPitchCentParam().setLevel(object.get("pitchCent").getAsFloat());

		ADSR adsr = track.getAdsr();
		adsr.setStart(object.get("adsrStart").getAsFloat());
		adsr.setAttack(object.get("adsrAttack").getAsFloat());
		adsr.setDecay(object.get("adsrDecay").getAsFloat());
		adsr.setSustain(object.get("adsrSustain").getAsFloat());
		adsr.setRelease(object.get("adsrRelease").getAsFloat());
		adsr.setPeak(object.get("adsrPeak").getAsFloat());
		return track;
	}

	@Override
	public JsonElement serialize(Track track, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("class", Track.class.getName());

		object.addProperty("id", track.getId());
		object.addProperty("position", TrackManager.getTracks().indexOf(track));

		object.addProperty("samplePath", track.getCurrSampleFile().getAbsolutePath());

		object.add("notes", GSON.toJsonTree(track.getMidiNotes()).getAsJsonArray());
		
		object.addProperty("volume", track.getVolumeParam().viewLevel);
		object.addProperty("pan", track.getPanParam().viewLevel);
		object.addProperty("pitch", track.getPitchParam().viewLevel);
		object.addProperty("pitchCent", track.getPitchCentParam().viewLevel);

		ADSR adsr = track.getAdsr();
		object.addProperty("adsrStart", adsr.getStart());
		object.addProperty("adsrAttack", adsr.getAttack());
		object.addProperty("adsrDecay", adsr.getDecay());
		object.addProperty("adsrSustain", adsr.getSustain());
		object.addProperty("adsrRelease", adsr.getRelease());
		object.addProperty("adsrPeak", adsr.getPeak());

		return object;
	}
}
