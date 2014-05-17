package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.Circle;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.view.View;

public class Seekbar extends ControlView1dBase {

	private RoundedRect backgroundRect, foregroundRect;
	private Circle levelCircle;
	protected float levelBarHeight;

	public Seekbar(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	@Override
	protected synchronized void createChildren() {
		backgroundRect = new RoundedRect(renderGroup, Color.VIEW_BG, null);
		foregroundRect = new RoundedRect(renderGroup, Color.TRON_BLUE, null);
		levelCircle = new Circle(renderGroup, Color.TRON_BLUE_TRANS, null);

		addShapes(backgroundRect, foregroundRect, levelCircle);
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

		backgroundRect.layout(absoluteX + levelBarHeight,
				absoluteY + (height - levelBarHeight) / 2, width - levelBarHeight * 2,
				levelBarHeight);
		foregroundRect.layout(absoluteX + levelBarHeight,
				absoluteY + (height - levelBarHeight) / 2, width - levelBarHeight * 2,
				levelBarHeight);
		levelCircle.setDimensions(levelBarHeight * 2.5f, levelBarHeight * 2.5f);
	}

	public void onParamChanged(Param param) {
		float w = levelBarHeight + param.viewLevel * (width - levelBarHeight * 3);
		foregroundRect.setDimensions(w, levelBarHeight);
		levelCircle.setPosition(absoluteX + w + levelCircle.width / 4, absoluteY + height / 2);
	}

	protected float posToLevel(Pointer pos) {
		if (pos.x > width - levelBarHeight)
			return 1;
		float level = (pos.x - levelBarHeight / 2) / (width - levelBarHeight * 4);
		return level < 0 ? 0 : (level > 1 ? 1 : level);
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		foregroundRect.setFillColor(selectColor);
		levelCircle.setFillColor(selectColorTrans);
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		super.handleActionUp(id, pos);
		foregroundRect.setFillColor(levelColor);
		levelCircle.setFillColor(levelColorTrans);
	}
}
