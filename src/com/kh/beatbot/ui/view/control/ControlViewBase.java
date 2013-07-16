package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.TouchableView;

public abstract class ControlViewBase extends TouchableView {

	protected float[] levelColor = Colors.VOLUME.clone();
	protected float[] selectColor = { levelColor[0], levelColor[1],
			levelColor[2], .5f };

	protected boolean selected = false;
	
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
