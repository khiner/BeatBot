package com.kh.beatbot.event;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.kh.beatbot.event.midinotes.MidiNoteDiff;

final class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
	public JsonElement serialize(T object, Type interfaceType, JsonSerializationContext context) {
		final JsonObject wrapper = new JsonObject();
		wrapper.addProperty("type", object.getClass().getName());
		wrapper.add("data", context.serialize(object));
		return wrapper;
	}

	public T deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context)
			throws JsonParseException {
		final JsonObject wrapper = (JsonObject) elem;
		final JsonElement typeName = get(wrapper, "type");
		final JsonElement data = get(wrapper, "data");
		final Type actualType = typeForName(typeName);
		return context.deserialize(data, actualType);
	}

	private Type typeForName(final JsonElement typeElem) {
		try {
			return Class.forName(typeElem.getAsString());
		} catch (ClassNotFoundException e) {
			throw new JsonParseException(e);
		}
	}

	private JsonElement get(final JsonObject wrapper, String memberName) {
		final JsonElement elem = wrapper.get(memberName);
		if (elem == null)
			throw new JsonParseException("no '" + memberName
					+ "' member found in what was expected to be an interface wrapper");
		return elem;
	}
}

public class EventJsonFactory {
	private static final String CLASS_KEY = "class";

	private static final GsonBuilder gsonBuilder = new GsonBuilder();
	static {
		gsonBuilder.registerTypeAdapter(MidiNoteDiff.class, new InterfaceAdapter<MidiNoteDiff>());
	}
	private static final Gson GSON = gsonBuilder.create();

	public static String toJson(Stateful event) {
		JsonElement json = GSON.toJsonTree(event);
		json.getAsJsonObject().addProperty(CLASS_KEY, event.getClass().getName());

		return GSON.toJson(json);
	}

	public static Stateful fromJson(String json) {
		JsonObject eventJsonObject = new JsonParser().parse(json).getAsJsonObject();
		String eventClassName = eventJsonObject.remove(CLASS_KEY).getAsString();
		Class<Stateful> serializableClass = EventManager.getSerializableClass(eventClassName);
		return serializableClass != null ? GSON.fromJson(json, serializableClass) : null;
	}
}
