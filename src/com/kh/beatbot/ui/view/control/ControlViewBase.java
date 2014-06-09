package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;

public abstract class ControlViewBase extends TouchableView {

	protected float[] levelColor = Color.TRON_BLUE;
	protected float[] levelColorTrans = new float[] { levelColor[0], levelColor[1], levelColor[2],
			.6f };
	protected static float[] selectColor = Color.LABEL_SELECTED;
	protected static float[] selectColorTrans = Color.LABEL_SELECTED_TRANS;

	public ControlViewBase(View view) {
		super(view);
	}
	
	public ControlViewBase(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	public void setLevelColor(float[] newLevelColor, float[] newLevelColorTrans) {
		levelColor = newLevelColor;
		levelColorTrans = newLevelColorTrans;
	}
}
