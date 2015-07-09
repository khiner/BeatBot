package com.kh.beatbot.event.effect;

import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;


public class EffectMoveEvent extends EffectEvent implements Stateful, Executable {
	private int finalPosition;

	public EffectMoveEvent(int trackId, int initialPosition, int finalPosition) {
		super(trackId, initialPosition);
		this.finalPosition = finalPosition;
	}

	@Override
	public void undo() {
		new EffectMoveEvent(trackId, finalPosition, effectPosition).apply();
	}

	@Override
	public void doExecute() {
		getTrack().moveEffect(effectPosition, finalPosition);
	}
}
