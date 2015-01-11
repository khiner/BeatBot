package com.kh.beatbot.event;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kh.beatbot.event.midinotes.MidiNotesDiffEvent;

public class EventJsonFactory {
	private static final String CLASS_KEY = "class";
	private static final List<Class> eventClasses = new ArrayList<Class>() {
		{
			add(MidiNotesDiffEvent.class);
		}
	};

	private static final Gson GSON = new GsonBuilder().create();

	public static String toJson(Stateful event) {
		JsonElement json = GSON.toJsonTree(event);
		json.getAsJsonObject().addProperty(CLASS_KEY, event.getClass().getName());
		return GSON.toJson(json);
	}

	public static Stateful fromJson(String json) {
		JsonObject eventJsonObject = new JsonParser().parse(json).getAsJsonObject();
		String eventClassName = eventJsonObject.remove(CLASS_KEY).getAsString();
		for (Class<Stateful> eventClass : eventClasses) {
			if (eventClassName.equals(eventClass.getName())) {
				return GSON.fromJson(json, eventClass);
			}
		}

		return null;
	}
}
