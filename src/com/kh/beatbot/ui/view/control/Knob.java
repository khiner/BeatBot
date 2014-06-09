package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.KnobShape;
import com.kh.beatbot.ui.view.View;

public class Knob extends ControlView1dBase {
	private KnobShape knobShape;

	public Knob(View view) {
		super(view);
	}

	@Override
	public void onParamChanged(Param param) {
		knobShape.setLevel(param.viewLevel);
	}

	@Override
	protected float posToLevel(Pointer pos) {
		float unitX = pos.x / width - .5f;
		float unitY = pos.y / height - .5f;
		float theta = (float) Math.atan(unitY / unitX) + ¹ / 2;
		// atan ranges from 0 to ¹, and produces symmetric results around the y axis.
		// we need 0 to 2*¹, so ad ¹ if right of x axis.
		if (unitX > 0)
			theta += ¹;
		// convert to level - remember, min theta is ¹/4, max is 7¹/8
		float level = (4 * theta / ¹ - 1) / 6;
		return GeneralUtils.clipToUnit(level);
	}

	@Override
	public synchronized void createChildren() {
		knobShape = new KnobShape(renderGroup, Color.TRON_BLUE, null);
		addShapes(knobShape);
	}

	@Override
	public synchronized void layoutChildren() {
		knobShape.layout(absoluteX, absoluteY, width, height);
	}

	@Override
	public void press() {
		super.press();
		knobShape.setFillColor(selectColor);
	}

	@Override
	public void release() {
		super.release();
		knobShape.setFillColor(levelColor);
	}
}
