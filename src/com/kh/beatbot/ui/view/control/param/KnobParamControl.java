package com.kh.beatbot.ui.view.control.param;

import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.control.Knob;
import com.kh.beatbot.ui.view.control.ToggleKnob;

public class KnobParamControl extends LevelParamControl {

	private boolean beatSync;

	public KnobParamControl(ShapeGroup shapeGroup, boolean beatSync) {
		this.beatSync = beatSync;
		shouldDraw = (null == shapeGroup);
		this.shapeGroup = shouldDraw ? new ShapeGroup() : shapeGroup;
		createChildren();
	}

	@Override
	public synchronized void createChildren() {
		super.createChildren();
		levelControl = beatSync ? new ToggleKnob(shapeGroup) : new Knob(
				shapeGroup);
		addChild(levelControl);
	}

	@Override
	public synchronized void layoutChildren() {
		label.layout(this, 0, 0, width, height / 5);
		levelControl.layout(this, 0, height / 5, width, 3 * height / 5);
		// a little shorter
		valueLabel.layout(this, 0, 5 * height / 6, width, height / 6);
	}
}
