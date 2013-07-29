package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;

public class KnobParamControl extends ParamControl {
	
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
	public void setParam(Param param) {
		super.setParam(param);
		setBeatSync(param.beatSync);
	}
}
