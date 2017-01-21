package com.odang.beatbot.ui.view.control;

import com.odang.beatbot.effect.Param;
import com.odang.beatbot.ui.color.Color;
import com.odang.beatbot.ui.shape.Circle;
import com.odang.beatbot.ui.shape.RoundedRect;
import com.odang.beatbot.ui.view.View;

public class Seekbar extends ControlView1dBase {
	public enum BasePosition {
		LEFT, CENTER
	};

	private final BasePosition basePosition;
	private float levelBarHeight;

	private RoundedRect backgroundRect, foregroundRect;
	private Circle levelCircle;

	public Seekbar(View view, BasePosition basePosition) {
		super(view);
		this.basePosition = basePosition;
	}

	@Override
	protected void createChildren() {
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
	public void layoutChildren() {
		levelBarHeight = height / 4;
		foregroundRect.setCornerRadius(levelBarHeight / 2);
		backgroundRect.setCornerRadius(levelBarHeight / 2);

		backgroundRect.layout(absoluteX + levelBarHeight,
				absoluteY + (height - levelBarHeight) / 2, width - levelBarHeight * 2,
				levelBarHeight);
		foregroundRect.layout(backgroundRect.x, backgroundRect.y, backgroundRect.width,
				backgroundRect.height);
		levelCircle.setDimensions(levelBarHeight * 2.5f, levelBarHeight * 2.5f);
		if (null != param) {
			onParamChange(param);
		}
	}

	public void onParamChange(Param param) {
		float levelPos = levelToX(param.viewLevel);
		levelCircle.setPosition(absoluteX + levelPos + levelCircle.width / 4, absoluteY + height
				/ 2);
		if (basePosition == BasePosition.LEFT) {
			foregroundRect.setDimensions(levelPos, levelBarHeight);
		} else if (basePosition == BasePosition.CENTER) {
			float middleX = levelToX(.5f);
			if (param.viewLevel >= .5f) {
				foregroundRect.layout(absoluteX + middleX, foregroundRect.y, levelPos - middleX
						+ levelBarHeight, levelBarHeight);
			} else {
				foregroundRect.layout(absoluteX + levelPos, foregroundRect.y, middleX - levelPos
						+ levelBarHeight, levelBarHeight);
			}
		}
	}

	protected float posToLevel(Pointer pos) {
		if (pos.x > width - levelBarHeight)
			return 1;
		float level = (pos.x - levelBarHeight * 2) / (width - levelBarHeight * 4);
		return level < 0 ? 0 : (level > 1 ? 1 : level);
	}

	private float levelToX(float viewLevel) {
		return levelBarHeight + viewLevel * (width - levelBarHeight * 3);
	}

	@Override
	public void press() {
		super.press();
		foregroundRect.setFillColor(selectColor);
		levelCircle.setFillColor(selectColorTrans);
	}

	@Override
	public void release() {
		super.release();
		foregroundRect.setFillColor(levelColor);
		levelCircle.setFillColor(levelColorTrans);		
	}
}
