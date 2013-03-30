package com.kh.beatbot.view.control;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public abstract class ControlViewBase extends TouchableBBView {

	protected float[] levelColor = Colors.VOLUME.clone();
	protected float[] selectColor = { levelColor[0], levelColor[1],
			levelColor[2], .5f };

	protected boolean selected = false;

	public ControlViewBase(TouchableSurfaceView parent) {
		super(parent);
	}
	
	@Override
	public void init() {
		// nothing to do
	}

	public void setLevelColor(float[] newLevelColor) {
		levelColor = newLevelColor;
		selectColor = new float[] { levelColor[0], levelColor[1],
				levelColor[2], .5f };
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		selected = true;
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		selected = false;
	}
}
