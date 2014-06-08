package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Seekbar;

public class SeekbarParamControl extends LevelParamControl {
	public SeekbarParamControl(View view) {
		this(view, Seekbar.BasePosition.LEFT);
	}

	public SeekbarParamControl(View view, Seekbar.BasePosition basePosition) {
		super(view);
		levelControl = new Seekbar(this, basePosition);
		addChild(levelControl);
	}

	@Override
	public synchronized void layoutChildren() {
		label.layout(this, 0, 0, width / 2, height / 2);
		valueLabel.layout(this, width / 2, 0, width / 2, height / 2);
		levelControl.layout(this, 0, height / 2, width, height / 2);
	}
}
