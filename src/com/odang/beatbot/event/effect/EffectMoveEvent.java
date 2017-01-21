package com.odang.beatbot.event.effect;

public class EffectMoveEvent extends EffectEvent {
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
	public boolean doExecute() {
		getTrack().moveEffect(effectPosition, finalPosition);
		return true;
	}
}
