package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.Circle;
import com.kh.beatbot.ui.mesh.RoundedRect;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.Shape.Type;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public class Seekbar extends ControlView1dBase {

	private ShapeGroup shapeGroup;
	private RoundedRect backgroundRect, foregroundRect;
	private Circle levelCircle;
	protected float levelBarHeight;

	@Override
	protected synchronized void createChildren() {
		shapeGroup = new ShapeGroup();
		backgroundRect = (RoundedRect) Shape.get(Type.ROUNDED_RECT, shapeGroup,
				Colors.VIEW_BG, null);
		foregroundRect = (RoundedRect) Shape.get(Type.ROUNDED_RECT, shapeGroup,
				Colors.VOLUME, null);
		levelCircle = (Circle) Shape.get(Type.CIRCLE, shapeGroup,
				Colors.VOLUME, null);
	}

	@Override
	public void setLevelColor(float[] newLevelColor) {
		super.setLevelColor(newLevelColor);
		foregroundRect.setFillColor(levelColor);
		levelCircle.setFillColor(levelColorTrans);
	}

	@Override
	public void draw() {
		shapeGroup.draw();
	}

	@Override
	public synchronized void layoutChildren() {
		levelBarHeight = height / 4;
		foregroundRect.setCornerRadius(levelBarHeight / 2);
		backgroundRect.setCornerRadius(levelBarHeight / 2);

		backgroundRect.layout(levelBarHeight, (height - levelBarHeight) / 2,
				width - levelBarHeight * 2, levelBarHeight);
		foregroundRect.layout(levelBarHeight, (height - levelBarHeight) / 2,
				width - levelBarHeight * 2, levelBarHeight);
		levelCircle.layout(0, 0, levelBarHeight * 2.5f, levelBarHeight * 2.5f);
	}

	public void onParamChanged(Param param) {
		float w = levelBarHeight + param.viewLevel
				* (width - levelBarHeight * 3);
		foregroundRect.setDimensions(w, levelBarHeight);
		levelCircle.setPosition(w + levelCircle.width / 4, height / 2);
	}

	protected float posToLevel(float x, float y) {
		if (x > width - levelBarHeight)
			return 1;
		float level = (x - levelBarHeight / 2) / (width - levelBarHeight * 4);
		return level < 0 ? 0 : (level > 1 ? 1 : level);
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		foregroundRect.setFillColor(selectColor);
		levelCircle.setFillColor(selectColorTrans);
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
		foregroundRect.setFillColor(levelColor);
		levelCircle.setFillColor(levelColorTrans);
	}
}
