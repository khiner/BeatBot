package com.kh.beatbot.ui.view.control;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.ui.color.Colors;

public class Seekbar2d extends ControlView2dBase {
	private static FloatBuffer lineVb;

	public Seekbar2d() {
		super();
		selectColor = Colors.LABEL_SELECTED;
	}

	protected float xToLevel(float x) {
		return unitX(clipX(x));
	}

	protected float yToLevel(float y) {
		return unitY(clipY(y));
	}

	public void setViewLevelX(float x) {
		super.setViewLevelX(x);
		initLines();
	}

	public void setViewLevelY(float y) {
		super.setViewLevelY(y);
		initLines();
	}

	public void setViewLevel(float x, float y) {
		super.setViewLevel(x, y);
		initLines();
	}


	@Override
	public void init() {
		super.init();
		initLines();
	}

	@Override
	public void draw() {
		float[] color = selected ? selectColor : levelColor;
		drawLines(lineVb, color, 5, GL10.GL_LINES);
		drawCircle(2 * getBgRectRadius() / 3, color, viewX(xLevel), viewY(yLevel));
	}

	private void initLines() {
		lineVb = makeFloatBuffer(new float[] { borderOffset, viewY(yLevel),
				width - borderOffset, viewY(yLevel), viewX(xLevel),
				borderOffset, viewX(xLevel), height - borderOffset });
	}

	@Override
	protected void createChildren() {
		initBgRect(null, Colors.VIEW_BG, Colors.VOLUME);
	}
}
