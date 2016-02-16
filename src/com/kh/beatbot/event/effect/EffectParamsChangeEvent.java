package com.kh.beatbot.event.effect;

import com.google.gson.JsonObject;
import com.kh.beatbot.effect.EffectSerializer;
import com.kh.beatbot.event.Temporal;
import com.kh.beatbot.ui.view.View;

public class EffectParamsChangeEvent extends EffectEvent implements Temporal {
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
			View.context.getEventManager().eventCompleted(this);
		}
	}

	@Override
	public boolean doExecute() {
		getEffect().deserialize(EffectSerializer.GSON, endSerializedParams);
		View.context.getTrackManager().onEffectCreate(getTrack(), getEffect());
		return true;
	}

	@Override
	public void undo() {
		getEffect().deserialize(EffectSerializer.GSON, beginSerializedParams);
		View.context.getTrackManager().onEffectCreate(getTrack(), getEffect());
	}
}
