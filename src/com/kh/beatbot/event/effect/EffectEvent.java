package com.kh.beatbot.event.effect;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.BaseTrack;

public class EffectEvent {
	protected int trackId, effectPosition;

	public EffectEvent(int trackId, int effectPosition) {
		this.trackId = trackId;
		this.effectPosition = effectPosition;
	}

	protected BaseTrack getTrack() {
		return TrackManager.getBaseTrackById(trackId);
	}

	protected Effect getEffect() {
		return getTrack().findEffectByPosition(effectPosition);
	}
}
