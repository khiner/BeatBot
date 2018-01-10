package com.odang.beatbot.effect;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.odang.beatbot.manager.PlaybackManager;

public class Filter extends Effect {
    public static final String NAME = "Filter";
    public static final int EFFECT_NUM = 3, NUM_PARAMS = 4;

    private int mode = 0;

    public Filter() {
        super();
    }

    public Filter(int trackId, int position) {
        super(trackId, position);
    }

    public int getId() {
        return EFFECT_NUM;
    }

    public String getName() {
        return NAME;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
        setEffectParam(trackId, position, 4, mode);
    }

    @Override
    protected void initParams() {
        params.add(new Param(0, "Freq").withUnits("Hz").scale(PlaybackManager.SAMPLE_RATE / 2)
                .logScale().withLevel(0.5f));
        params.add(new Param(1, "Res").withLevel(0.5f));
        params.add(new Param(2, "Mod Rate").withUnits("Hz").logScale().beatSyncable()
                .withLevel(0.5f));
        params.add(new Param(3, "Mod Amt").withLevel(0.5f));
        params.get(0).hz = false;
    }

    @Override
    public JsonObject serialize(Gson gson) {
        JsonObject object = super.serialize(gson);
        object.addProperty("modRateSync", getParam(2).isBeatSync());
        object.addProperty("mode", mode);
        return object;
    }

    @Override
    public void deserialize(Gson gson, JsonObject jsonObject) {
        super.deserialize(gson, jsonObject);
        getParam(2).setBeatSync(jsonObject.get("modRateSync").getAsBoolean());
        setMode(jsonObject.get("mode").getAsInt());
    }
}
