package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.listener.ControlViewListener;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.TouchableView;

public abstract class ControlViewBase extends TouchableView {

	protected float[] levelColor = Color.TRON_BLUE;
	protected float[] levelColorTrans = new float[] { levelColor[0], levelColor[1], levelColor[2],
			.6f };
	protected static float[] selectColor = Color.LABEL_SELECTED;
	protected static float[] selectColorTrans = Color.LABEL_SELECTED_TRANS;

	protected boolean selected = false;

	protected ControlViewListener listener;

	public void setListener(ControlViewListener listener) {
		this.listener = listener;
	}

	public ControlViewBase(RenderGroup renderGroup) {
		super(renderGroup);
	}

	public void setLevelColor(float[] newLevelColor, float[] newLevelColorTrans) {
		levelColor = newLevelColor;
		levelColorTrans = newLevelColorTrans;
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		selected = true;
		if (listener != null) {
			listener.onPress(this);
		}
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		super.handleActionUp(id, pos);
		selected = false;
		if (listener != null) {
			listener.onRelease(this);
		}
	}
}
