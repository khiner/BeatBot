package com.kh.beatbot.event.effect;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;

public class EffectToggleEvent extends EffectEvent implements Stateful, Executable {
	public EffectToggleEvent(int trackId, int effectPosition) {
		super(trackId, effectPosition);
	}

	@Override
	public void undo() {
		doExecute();
	}

	@Override
	public void apply() {
		doExecute();
	}

	@Override
	public void execute() {
		doExecute();
		EventManager.eventCompleted(this);
	}

	@Override
	public void doExecute() {
		Effect effect = getEffect();
		effect.setOn(!effect.isOn());
	}
}
