package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Dial;
import com.kh.beatbot.ui.view.control.ToggleDial;

public class DialParamControl extends LevelParamControl {
	private TouchableView levelControlView;

	public DialParamControl(View view) {
		super(view);
	}

	public DialParamControl withBeatSync(boolean beatSync) {
		levelControlView = beatSync ? new ToggleDial(this) : new Dial(this);
		if (beatSync) {
			levelControl = ((ToggleDial) levelControlView).getDial();
		} else {
			levelControl = (Dial) levelControlView;
		}

		levelControl.setListener(this);
		return this;
	}

	@Override
	public void setParam(Param param) {
		super.setParam(param);
		if (levelControlView instanceof ToggleDial) {
			final ToggleDial toggleDial = (ToggleDial) levelControlView;
			param.addToggleListener(toggleDial);
			toggleDial.onParamToggle(param);
		}
	}

	@Override
	public synchronized void layoutChildren() {
		label.layout(this, 0, 0, width, height / 5);
		levelControlView.layout(this, 0, height / 5, width, 3 * height / 5);
		valueLabel.layout(this, 0, 5 * height / 6, width, height / 6);
	}
}
