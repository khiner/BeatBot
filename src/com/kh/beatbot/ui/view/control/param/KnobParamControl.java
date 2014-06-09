package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Knob;
import com.kh.beatbot.ui.view.control.ToggleKnob;

public class KnobParamControl extends LevelParamControl {

	public KnobParamControl(View view) {
		super(view);
	}

	public KnobParamControl withBeatSync(boolean beatSync) {
		levelControl = beatSync ? new ToggleKnob(this) : new Knob(this);
		levelControl.setListener(this);
		return this;
	}

	@Override
	public synchronized void layoutChildren() {
		label.layout(this, 0, 0, width, height / 5);
		levelControl.layout(this, 0, height / 5, width, 3 * height / 5);
		// a little shorter
		valueLabel.layout(this, 0, 5 * height / 6, width, height / 6);
	}
}
