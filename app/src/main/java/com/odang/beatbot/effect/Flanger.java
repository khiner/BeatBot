package com.odang.beatbot.effect;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Flanger extends Effect {
    public static final String NAME = "Flanger";
    public static final int EFFECT_NUM = 4;

    public Flanger() {
        super();
    }

    public Flanger(int trackId, int position) {
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
        params.add(new Param(0, "Time").withUnits("ms").logScale().withLevel(0.5f));
        params.add(new Param(1, "Feedback").withLevel(0.5f));
        params.add(new Param(2, "Wet").withLevel(0.5f));
        params.add(new Param(3, "Mod Rate").withUnits("Hz").logScale().beatSyncable()
                .withLevel(0.5f));
        params.add(new Param(4, "Mod Amt").withLevel(0.5f));
        params.add(new Param(5, "Phase").withLevel(0.5f));
    }

    @Override
    public JsonObject serialize(Gson gson) {
        JsonObject object = super.serialize(gson);
        object.addProperty("modRateSync", getParam(3).isBeatSync());
        return object;
    }

    @Override
    public void deserialize(Gson gson, JsonObject jsonObject) {
        super.deserialize(gson, jsonObject);
        getParam(3).setBeatSync(jsonObject.get("modRateSync").getAsBoolean());
    }
}
