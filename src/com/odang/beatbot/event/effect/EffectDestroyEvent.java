package com.odang.beatbot.event.effect;

import com.odang.beatbot.file.ProjectFile;

public class EffectDestroyEvent extends EffectEvent {
	final String serializedEffect;

	public EffectDestroyEvent(int trackId, int effectPosition) {
		super(trackId, effectPosition);
		this.serializedEffect = ProjectFile.effectToJson(getEffect());
	}

	@Override
	public void undo() {
		ProjectFile.effectFromJson(serializedEffect);
	}

	@Override
	public boolean doExecute() {
		getEffect().destroy();
		return true;
	}
}
