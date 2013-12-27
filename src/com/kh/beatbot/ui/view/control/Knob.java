package com.kh.beatbot.ui.view.control;

import java.nio.FloatBuffer;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.Colors;

public class Knob extends ControlView1dBase {

	private FloatBuffer circleVb;

	private int drawIndex = 0;

	private void initCircleVbs(float width, float height) {
		float[] circleVertices = new float[128];
		float theta = 3 * ¹ / 4; // start at 1/8 around the circle
		for (int i = 0; i < circleVertices.length / 4; i++) {
			// theta will range from ¹/4 to 7¹/8,
			// with the ¹/8 gap at the "bottom" of the view
			theta += 6 * ¹ / circleVertices.length;
			// main circles will show when user is not touching
			circleVertices[i * 4] = (float) Math.cos(theta) * width / 2.3f
					+ width / 2;
			circleVertices[i * 4 + 1] = (float) Math.sin(theta) * width / 2.3f
					+ width / 2;
			circleVertices[i * 4 + 2] = (float) Math.cos(theta) * width / 3.3f
					+ width / 2;
			circleVertices[i * 4 + 3] = (float) Math.sin(theta) * width / 3.3f
					+ width / 2;
		}
		circleVb = makeFloatBuffer(circleVertices);
	}

	@Override
	public void draw() {
		// level background
		drawTriangleStrip(circleVb, Colors.VIEW_BG);
		// main selection
		drawTriangleStrip(circleVb, selected ? Colors.LABEL_SELECTED
				: levelColor, drawIndex);
	}

	@Override
	public void onParamChanged(Param param) {
		if (circleVb == null)
			return;
		drawIndex = (int) (circleVb.capacity() * param.viewLevel / 2);
		drawIndex += drawIndex % 2;
	}

	@Override
	protected float posToLevel(float x, float y) {
		float unitX = x / width - .5f;
		float unitY = y / height - .5f;
		float theta = (float) Math.atan(unitY / unitX) + ¹ / 2;
		// atan ranges from 0 to ¹, and produces symmetric results around the y
		// axis.
		// we need 0 to 2*¹, so ad ¹ if right of x axis.
		if (unitX > 0)
			theta += ¹;
		// convert to level - remember, min theta is ¹/4, max is 7¹/8
		float level = (4 * theta / ¹ - 1) / 6;
		return GeneralUtils.clipToUnit(level);
	}

	@Override
	public synchronized void layoutChildren() {
		initCircleVbs(width, height);
	}
}
