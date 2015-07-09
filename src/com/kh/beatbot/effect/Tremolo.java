package com.kh.beatbot.effect;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Tremolo extends Effect {
	public static final String NAME = "Tremolo";
	public static final int EFFECT_NUM = 6, NUM_PARAMS = 3;

	public Tremolo() {
		super();
	}

	public Tremolo(int trackId, int position) {
		super(trackId, position);
	}

	public String getName() {
		return NAME;
	}

	public int getId() {
		return EFFECT_NUM;
	}

	@Override
	protected void initParams() {
		params.add(new Param(0, "Rate").withUnits("Hz").logScale().beatSyncable().withLevel(0.5f));
		params.add(new Param(1, "Phase").withLevel(0.5f));
		params.add(new Param(2, "Depth").withLevel(0.5f));
	}

	@Override
	public JsonObject serialize(Gson gson) {
		JsonObject object = super.serialize(gson);
		object.addProperty("rateSync", getParam(0).isBeatSync());
		return object;
	}

	@Override
	public void deserialize(Gson gson, JsonObject jsonObject) {
		super.deserialize(gson, jsonObject);
		getParam(0).setBeatSync(jsonObject.get("rateSync").getAsBoolean());
	}
}
