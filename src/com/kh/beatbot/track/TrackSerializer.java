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

public class TrackSerializer implements JsonSerializer<BaseTrack>, JsonDeserializer<BaseTrack> {
	private static final Gson GSON = new GsonBuilder().create();
	private Type noteListType = new TypeToken<ArrayList<MidiNote>>() {
	}.getType();

	@Override
	public BaseTrack deserialize(JsonElement trackJson, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject object = trackJson.getAsJsonObject();
		int id = object.get("id").getAsInt();
		int position = object.get("position").getAsInt();
		float volume = object.get("volume").getAsFloat();
		float pan = object.get("pan").getAsFloat();
		float pitch = object.get("pitch").getAsFloat();
		float pitchCent = object.get("pitchCent").getAsFloat();

		boolean isMaster = id == TrackManager.MASTER_TRACK_ID;
		BaseTrack track = isMaster ? TrackManager.getMasterTrack() : TrackManager.createTrack(id, position);

		track.getVolumeParam().setLevel(volume);
		track.getPanParam().setLevel(pan);
		track.getPitchParam().setLevel(pitch);
		track.getPitchCentParam().setLevel(pitchCent);

		if (isMaster) return track;

		Track t = (Track) track;
		try {
			t.setSample(new File(object.get("samplePath").getAsString()));
		} catch (Exception e) {
			Log.e(getClass().getName(), e.toString());
		}
		
		t.setSampleLoopWindow(object.get("sample_loop_begin").getAsFloat(), object.get("sample_loop_end").getAsFloat());
		List<MidiNote> notes = GSON.fromJson(object.get("notes"), noteListType);
		for (MidiNote note : notes) {
			note.create();
		}

		ADSR adsr = t.getAdsr();
		adsr.setStart(object.get("adsrStart").getAsFloat());
		adsr.setAttack(object.get("adsrAttack").getAsFloat());
		adsr.setDecay(object.get("adsrDecay").getAsFloat());
		adsr.setSustain(object.get("adsrSustain").getAsFloat());
		adsr.setRelease(object.get("adsrRelease").getAsFloat());
		adsr.setPeak(object.get("adsrPeak").getAsFloat());

		return t;
	}

	@Override
	public JsonElement serialize(BaseTrack track, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();

		object.addProperty("id", track.getId());
		object.addProperty("position", TrackManager.getTracks().indexOf(track));
		
		object.addProperty("volume", track.getVolumeParam().viewLevel);
		object.addProperty("pan", track.getPanParam().viewLevel);
		object.addProperty("pitch", track.getPitchParam().viewLevel);
		object.addProperty("pitchCent", track.getPitchCentParam().viewLevel);
		
		if (track instanceof Track) {
			Track t = (Track) track;
			object.addProperty("class", Track.class.getName());

			object.addProperty("samplePath", t.getCurrSampleFile().getAbsolutePath());

			object.addProperty("sample_loop_begin", t.getLoopBeginParam().viewLevel);
			object.addProperty("sample_loop_end", t.getLoopEndParam().viewLevel);

			object.add("notes", GSON.toJsonTree(t.getMidiNotes()).getAsJsonArray());

			ADSR adsr = t.getAdsr();
			object.addProperty("adsrStart", adsr.getStart());
			object.addProperty("adsrAttack", adsr.getAttack());
			object.addProperty("adsrDecay", adsr.getDecay());
			object.addProperty("adsrSustain", adsr.getSustain());
			object.addProperty("adsrRelease", adsr.getRelease());
			object.addProperty("adsrPeak", adsr.getPeak());

		} else {
			object.addProperty("class", BaseTrack.class.getName());			
		}

		return object;
	}
}
