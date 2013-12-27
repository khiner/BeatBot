package com.kh.beatbot.ui.view.control;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.Shape.Type;

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

	public void onParamChanged(Param param) {
		lineVb = makeFloatBuffer(new float[] { BG_OFFSET,
				viewY(params[1].viewLevel), width - BG_OFFSET,
				viewY(params[1].viewLevel), viewX(params[0].viewLevel),
				BG_OFFSET, viewX(params[0].viewLevel), height - BG_OFFSET });
	}

	@Override
	public void draw() {
		float[] color = selected ? selectColor : levelColor;
		drawLines(lineVb, color, 5, GL10.GL_LINES);
		drawCircle(2 * getBgRectRadius() / 3, color,
				viewX(params[0].viewLevel), viewY(params[1].viewLevel));
	}

	@Override
	protected synchronized void createChildren() {
		initBgRect(Type.ROUNDED_RECT, null, Colors.VIEW_BG, Colors.VOLUME);
	}
}
