package com.kh.beatbot.event.effect;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.ui.view.View;

public abstract class EffectEvent extends Executable {
	protected int trackId, effectPosition;

	public EffectEvent() {
	}

	public EffectEvent(int trackId, int effectPosition) {
		this.trackId = trackId;
		this.effectPosition = effectPosition;
	}

	protected BaseTrack getTrack() {
		return View.context.getTrackManager().getBaseTrackById(trackId);
	}

	protected Effect getEffect() {
		return getTrack().findEffectByPosition(effectPosition);
	}
}
