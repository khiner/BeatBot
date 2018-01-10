package com.odang.beatbot.event.effect;

import com.odang.beatbot.effect.Effect;
import com.odang.beatbot.ui.view.View;

public class EffectToggleEvent extends EffectEvent {
	public EffectToggleEvent(int trackId, int effectPosition) {
		super(trackId, effectPosition);
	}

	@Override
	public void undo() {
		doExecute();
	}

	@Override
	public boolean doExecute() {
		Effect effect = getEffect();
		effect.setOn(!effect.isOn());
		View.context.getTrackManager().onEffectCreate(getTrack(), getEffect());
		return true;
	}
}
