package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.ThresholdBar;

public class ThresholdParamControl extends LevelParamControl {
	public ThresholdParamControl(View view) {
		super(view);
		levelControl = new ThresholdBar(this);
		levelControl.setListener(this);
		addChild(levelControl);
	}

	@Override
	public synchronized void layoutChildren() {
		label.layout(this, 0, 0, height * 2, height);
		levelControl.layout(this, height * 2, 0, width - height * 4, height);
		valueLabel.layout(this, width - height * 2, 0, height * 2, height);
	}

	public void setLevel(float level) {
		((ThresholdBar) levelControl).setLevelNormalized(level);
	}
}
