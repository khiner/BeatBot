package com.kh.beatbot.event.effect;

import com.kh.beatbot.event.Executable;
import com.kh.beatbot.event.Stateful;
import com.kh.beatbot.file.ProjectFile;

public class EffectDestroyEvent extends EffectEvent implements Stateful, Executable {
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
	public void doExecute() {
		getEffect().destroy();
	}
}
