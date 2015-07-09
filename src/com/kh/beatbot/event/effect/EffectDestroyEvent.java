package com.kh.beatbot.event.effect;

import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;

public class EffectDestroyEvent extends EffectEvent implements Stateful, Executable {
	private String effectName;

	public EffectDestroyEvent(int trackId, int effectPosition) {
		super(trackId, effectPosition);
		this.effectName = getEffect().getName();
	}

	@Override
	public void undo() {
		new EffectCreateEvent(trackId, effectPosition, effectName).apply();
	}

	@Override
	public void doExecute() {
		getEffect().destroy();
	}
}
