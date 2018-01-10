package com.odang.beatbot.effect;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.odang.beatbot.event.effect.EffectCreateEvent;
import com.odang.beatbot.ui.view.View;

public class EffectSerializer implements JsonSerializer<Effect>, JsonDeserializer<Effect> {
	public static final Gson GSON = new GsonBuilder().create();

	@Override
	public Effect deserialize(JsonElement effectJson, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject object = effectJson.getAsJsonObject();
		String name = object.get("name").getAsString();
		int trackId = object.get("trackId").getAsInt();
		int position = object.get("position").getAsInt();
		new EffectCreateEvent(trackId, position, name).apply();
		Effect effect = View.context.getTrackManager().getTrackById(trackId).getEffectByPosition(position);
		effect.deserialize(GSON, object);

		return effect;
	}

	@Override
	public JsonElement serialize(Effect effect, Type type, JsonSerializationContext context) {
		return effect.serialize(GSON);
	}
}
