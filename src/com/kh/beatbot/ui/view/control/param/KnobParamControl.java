package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.view.control.Knob;
import com.kh.beatbot.ui.view.control.ToggleKnob;

public class KnobParamControl extends LevelParamControl {
	
	public KnobParamControl(boolean beatSync) {
		super();
		levelControl = beatSync ? new ToggleKnob() : new Knob();
		addChild(levelControl);
	}
	
	public void setParam(Param param) {
		super.setParam(param);
	}
	
	@Override
	public synchronized void layoutChildren() {
		label.layout(this, 0, 0, width, height / 5);
		levelControl.layout(this, 0, height / 5, width, 3 * height / 5);
		valueLabel.layout(this, 0, 5 * height / 6, width, height / 6); // a little shorter
	}
}
