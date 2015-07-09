package com.kh.beatbot.event.effect;

import com.google.gson.JsonObject;
import com.kh.beatbot.effect.EffectSerializer;
import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.event.Temporal;
import com.kh.beatbot.manager.TrackManager;

public class EffectParamsChangeEvent extends EffectEvent implements Stateful, Temporal {
	private JsonObject beginSerializedParams, endSerializedParams;

	public EffectParamsChangeEvent(int trackId, int effectPosition) {
		super(trackId, effectPosition);
	}

	@Override
	public void begin() {
		beginSerializedParams = getEffect().serialize(EffectSerializer.GSON);
	}

	@Override
	public void end() {
		endSerializedParams = getEffect().serialize(EffectSerializer.GSON);
		if (!endSerializedParams.equals(beginSerializedParams)) {
			EventManager.eventCompleted(this);
		}
	}

	@Override
	public void doExecute() {
		apply();
	}

	@Override
	public void undo() {
		getEffect().deserialize(EffectSerializer.GSON, beginSerializedParams);
		TrackManager.get().onEffectCreate(getTrack(), getEffect());
//		View.mainPage.effectPage.updateParams();
	}

	@Override
	public void apply() {
		getEffect().deserialize(EffectSerializer.GSON, endSerializedParams);
		TrackManager.get().onEffectCreate(getTrack(), getEffect());
//		View.mainPage.effectPage.updateParams();
	}
}
