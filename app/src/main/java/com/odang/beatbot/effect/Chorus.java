package com.odang.beatbot.effect;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Chorus extends Effect {
    public static final String NAME = "Chorus";
    public static final int EFFECT_NUM = 0;

    public Chorus() {
        super();
    }

    public Chorus(int trackId, int position) {
        super(trackId, position);
    }

    public int getId() {
        return EFFECT_NUM;
    }

    public String getName() {
        return NAME;
    }

    @Override
    protected void initParams() {
        params.add(new Param(0, "Mod Rate").withUnits("Hz").logScale().beatSyncable().withLevel(0.5f));
        params.add(new Param(1, "Mod Amt").withLevel(0.5f));
        params.add(new Param(2, "Time").withUnits("ms").logScale().withLevel(0.5f));
        params.add(new Param(3, "Feedback").withLevel(0.5f));
        params.add(new Param(4, "Wet").withLevel(0.5f));
    }

    @Override
    public JsonObject serialize(Gson gson) {
        JsonObject object = super.serialize(gson);
        object.addProperty("modRateSync", getParam(0).isBeatSync());
        return object;
    }

    @Override
    public void deserialize(Gson gson, JsonObject jsonObject) {
        super.deserialize(gson, jsonObject);
        getParam(0).setBeatSync(jsonObject.get("modRateSync").getAsBoolean());
    }
}
