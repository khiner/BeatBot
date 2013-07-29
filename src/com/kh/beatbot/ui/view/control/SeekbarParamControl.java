package com.kh.beatbot.ui.view.control;

public class SeekbarParamControl extends ParamControl {

	public SeekbarParamControl() {
		super();
		levelControl = new Seekbar();
		addChild(levelControl);
		addLevelListener(this);
	}
	
	@Override
	public void layoutChildren() {
		label.layout(this, 0, 0, width / 2, height / 2);
		valueLabel.layout(this, width / 2, 0, width / 2, height / 2);
		levelControl.layout(this, 0, height / 2, width, height / 2);
	}
}
