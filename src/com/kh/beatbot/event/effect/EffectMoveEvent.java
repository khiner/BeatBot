package com.kh.beatbot.event.effect;

import com.kh.beatbot.event.Stateful;


public class EffectMoveEvent extends EffectEvent implements Stateful {
	private int finalPosition;

	public EffectMoveEvent(int trackId, int initialPosition, int finalPosition) {
		super(trackId, initialPosition);
		this.finalPosition = finalPosition;
	}

	@Override
	public void apply() {
		getTrack().moveEffect(effectPosition, finalPosition);
	}

	@Override
	public void undo() {
		getTrack().moveEffect(finalPosition, effectPosition);
	}
}
