package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.TouchableView;

public abstract class ControlViewBase extends TouchableView {

	protected float[] levelColor = Colors.VOLUME;
	protected float[] levelColorTrans = new float[] { levelColor[0],
			levelColor[1], levelColor[2], .6f };
	protected static float[] selectColor = Colors.LABEL_SELECTED;
	protected static float[] selectColorTrans = Colors.LABEL_SELECTED_TRANS;

	protected boolean selected = false;

	public void setLevelColor(float[] newLevelColor) {
		levelColor = newLevelColor;
		levelColorTrans = new float[] { levelColor[0],
				levelColor[1], levelColor[2], .6f };
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		selected = true;
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
		selected = false;
	}
}
