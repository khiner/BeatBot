package com.kh.beatbot.event;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kh.beatbot.event.midinotes.MidiNotesCreateEvent;
import com.kh.beatbot.event.midinotes.MidiNotesDestroyEvent;
import com.kh.beatbot.event.midinotes.MidiNotesLevelsSetEvent;
import com.kh.beatbot.event.midinotes.MidiNotesMoveEvent;

public class EventJsonFactory {
	private static Gson gson = new GsonBuilder().create();

	public static String toJson(Stateful event) {
		JsonElement json = gson.toJsonTree(event);
		json.getAsJsonObject().addProperty("class", event.getClass().getName());
		return gson.toJson(json);
	}
	
	public static Stateful fromJson(String json) {
		JsonObject o = new JsonParser().parse(json).getAsJsonObject();
		String className = o.remove("class").getAsString();

		List<Class> eventClasses = new ArrayList<Class>() {{
			add(MidiNotesCreateEvent.class);
			add(MidiNotesDestroyEvent.class);
			add(MidiNotesLevelsSetEvent.class);
			add(MidiNotesMoveEvent.class);
		}};
		
		for (Class<Stateful> eventClass : eventClasses) {
			if (className.equals(eventClass.getName())) {
				return gson.fromJson(json, eventClass);
			}
		}

		return null;
	}
}
