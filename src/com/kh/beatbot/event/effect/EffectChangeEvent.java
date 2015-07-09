package com.kh.beatbot.event.effect;

import com.kh.beatbot.event.EventManager;
import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;

public class EffectChangeEvent extends EffectEvent implements Stateful, Executable {
	final String initialEffectName, endEffectName;

	public EffectChangeEvent(int trackId, int effectPosition, String initialEffectName,
			String endEffectName) {
		super(trackId, effectPosition);
		this.initialEffectName = initialEffectName;
		this.endEffectName = endEffectName;
	}

	@Override
	public void undo() {
		new EffectChangeEvent(trackId, effectPosition, endEffectName, initialEffectName).apply();
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
		new EffectDestroyEvent(trackId, effectPosition).apply();
		new EffectCreateEvent(trackId, effectPosition, endEffectName).apply();
	}
}
