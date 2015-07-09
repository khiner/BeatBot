package com.kh.beatbot.event.effect;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.BaseTrack;

public abstract class EffectEvent implements Stateful {
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

	@Override
	public void apply() {
		doExecute();
	}

	public void execute() {
		doExecute();
		EventManager.eventCompleted(this);
	}
	
	abstract void doExecute();
}
