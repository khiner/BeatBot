package com.odang.beatbot.effect;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Delay extends Effect {
	public static final String NAME = "Delay";
	public static final int EFFECT_NUM = 2, NUM_PARAMS = 4;

	// keep track of what right channel was before linking
	// so we can go back after disabling link
	// by default, channels are linked, so no memory is needed
	public float rightChannelLevelMemory = -1;
	public boolean rightChannelBeatSyncMemory = true;

	private boolean paramsLinked;

	public Delay() {
		super();
	}

	public Delay(int trackId, int position) {
		super(trackId, position);
		// since left/right delay times are linked by default,
		// xy view is set to x = left channel, y = feedback
		xParamIndex = 0;
		yParamIndex = 2;
		paramsLinked = true;
	}

	public int getId() {
		return EFFECT_NUM;
	}

	public String getName() {
		return NAME;
	}

	public Param getLeftTimeParam() {
		return params.get(0);
	}
	
	public Param getRightTimeParam() {
		return params.get(1);
	}

	public boolean paramsLinked() {
		return paramsLinked;
	}

	public void setParamsLinked(boolean linked) {
		paramsLinked = linked;
		// y = feedback when linked / right delay time when not linked
		yParamIndex = linked ? 2 : 1;
	}

	@Override
	protected void initParams() {
		params.add(new Param(0, "Left").withUnits("ms").logScale().beatSyncable().withLevel(0.5f));
		params.add(new Param(1, "Right").withUnits("ms").logScale().beatSyncable().withLevel(0.5f));
		params.add(new Param(2, "Feedback").withLevel(0.5f));
		params.add(new Param(3, "Wet").withLevel(0.5f));
	}
	
	@Override
	public JsonObject serialize(Gson gson) {
		JsonObject object = super.serialize(gson);
		object.addProperty("paramsLinked", paramsLinked());
		object.addProperty("leftChannelBeatSync", getParam(0).isBeatSync());
		object.addProperty("rightChannelBeatSync", getParam(1).isBeatSync());
		object.addProperty("rightChannelLevelMemory", rightChannelLevelMemory);
		object.addProperty("rightChannelBeatSyncMemory", rightChannelBeatSyncMemory);
		return object;
	}

	public void deserialize(Gson gson, JsonObject jsonObject) {
		super.deserialize(gson, jsonObject);
		getLeftTimeParam().setBeatSync(jsonObject.get("leftChannelBeatSync").getAsBoolean());
		getRightTimeParam().setBeatSync(jsonObject.get("rightChannelBeatSync").getAsBoolean());
		setParamsLinked(jsonObject.get("paramsLinked").getAsBoolean());
		rightChannelBeatSyncMemory = jsonObject.get("rightChannelBeatSyncMemory").getAsBoolean();
		rightChannelLevelMemory = jsonObject.get("rightChannelLevelMemory").getAsFloat();
	}
}
