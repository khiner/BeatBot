package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.shape.Circle;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.shape.ShapeGroup;

public class Seekbar extends ControlView1dBase {

	private RoundedRect backgroundRect, foregroundRect;
	private Circle levelCircle;
	protected float levelBarHeight;

	public Seekbar(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	@Override
	protected synchronized void createChildren() {
		backgroundRect = new RoundedRect(shapeGroup, Colors.VIEW_BG, null);
		foregroundRect = new RoundedRect(shapeGroup, Colors.VOLUME, null);
		levelCircle = new Circle(shapeGroup, Colors.VOLUME, null);
	}

	@Override
	public void setLevelColor(float[] newLevelColor, float[] newLevelColorTrans) {
		super.setLevelColor(newLevelColor, newLevelColorTrans);
		foregroundRect.setFillColor(levelColor);
		levelCircle.setFillColor(levelColorTrans);
	}

	@Override
	public synchronized void layoutChildren() {
		levelBarHeight = height / 4;
		foregroundRect.setCornerRadius(levelBarHeight / 2);
		backgroundRect.setCornerRadius(levelBarHeight / 2);

		backgroundRect.layout(absoluteX + levelBarHeight, absoluteY
				+ (height - levelBarHeight) / 2, width - levelBarHeight * 2,
				levelBarHeight);
		foregroundRect.layout(absoluteX + levelBarHeight, absoluteY
				+ (height - levelBarHeight) / 2, width - levelBarHeight * 2,
				levelBarHeight);
		levelCircle.setDimensions(levelBarHeight * 2.5f, levelBarHeight * 2.5f);
	}

	public void onParamChanged(Param param) {
		float w = levelBarHeight + param.viewLevel
				* (width - levelBarHeight * 3);
		foregroundRect.setDimensions(w, levelBarHeight);
		levelCircle.setPosition(absoluteX + w + levelCircle.width / 4,
				absoluteY + height / 2);
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
