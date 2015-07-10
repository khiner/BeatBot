package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Knob;
import com.kh.beatbot.ui.view.control.ToggleKnob;

public class KnobParamControl extends LevelParamControl {
	private TouchableView levelControlView;

	public KnobParamControl(View view) {
		super(view);
	}

	public KnobParamControl withBeatSync(boolean beatSync) {
		levelControlView = beatSync ? new ToggleKnob(this) : new Knob(this);
		if (beatSync) {
			levelControl = ((ToggleKnob) levelControlView).getKnob();
		} else {
			levelControl = (Knob) levelControlView;
		}

		levelControl.setListener(this);
		return this;
	}

	@Override
	public void setParam(Param param) {
		super.setParam(param);
		if (levelControlView instanceof ToggleKnob) {
			ToggleKnob toggleKnob = (ToggleKnob) levelControlView;
			param.addToggleListener(toggleKnob);
			toggleKnob.onParamToggle(param);
		}
	}

	@Override
	public synchronized void layoutChildren() {
		label.layout(this, 0, 0, width, height / 5);
		levelControlView.layout(this, 0, height / 5, width, 3 * height / 5);
		valueLabel.layout(this, 0, 5 * height / 6, width, height / 6);
	}
}
