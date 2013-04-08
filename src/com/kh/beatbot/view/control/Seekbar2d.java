package com.kh.beatbot.view.control;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.global.Colors;

public class Seekbar2d extends ControlView2dBase {
	private static ViewRect viewRect;
	private static FloatBuffer lineVb;

	public Seekbar2d() {
		super();
		selectColor = Colors.RED;
	}

	protected float xToLevel(float x) {
		return viewRect.unitX(viewRect.clipX(x));
	}
	
	protected float yToLevel(float y) {
		return viewRect.unitY(viewRect.clipY(y));
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
	
	protected void loadIcons() {
		// no icons to load
	}
	
	@Override
	public void init() {
		super.init();
		viewRect = new ViewRect(width, height, 0.08f, 8);
		initLines();
	}

	@Override
	public void draw() {
		viewRect.drawRoundedBg();
		viewRect.drawRoundedBgOutline();
		float[] color = selected ? selectColor : levelColor;
		drawLines(lineVb, color, 5, GL10.GL_LINES);
		drawPoint(2 * viewRect.borderRadius / 3, color, viewRect.viewX(xLevel), viewRect.viewY(yLevel));
	}

	private void initLines() {
		lineVb = makeFloatBuffer(new float[] { viewRect.drawOffset, viewRect.viewY(yLevel),
				width - viewRect.drawOffset, viewRect.viewY(yLevel), viewRect.viewX(xLevel), viewRect.drawOffset, viewRect.viewX(xLevel),
				height - viewRect.drawOffset});
	}

	@Override
	protected void createChildren() {
		// leaf child
	}

	@Override
	public void layoutChildren() {
		// leaf child
	}
}
