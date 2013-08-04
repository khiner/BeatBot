package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.EffectParam;
import com.kh.beatbot.ui.view.control.Knob;
import com.kh.beatbot.ui.view.control.ToggleKnob;

public class KnobParamControl extends LevelParamControl {
	
	public KnobParamControl(boolean beatSync) {
		super();
		levelControl = beatSync ? new ToggleKnob() : new Knob();
		addChild(levelControl);
		addLevelListener(this);
	}
	
	public void setBeatSync(boolean beatSync) {
		if (levelControl instanceof ToggleKnob) {
			((ToggleKnob) levelControl).setBeatSync(beatSync);
		}
	}
	
	public boolean isBeatSync() {
		if (levelControl instanceof ToggleKnob) {
			return ((ToggleKnob) levelControl).isBeatSync();
		}
		return false;
	}
	
	@Override
	public void setParam(EffectParam param) {
		super.setParam(param);
		setBeatSync(param.beatSync);
	}
}
