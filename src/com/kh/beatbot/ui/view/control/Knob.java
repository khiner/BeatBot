package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.KnobShape;
import com.kh.beatbot.ui.shape.ShapeGroup;

public class Knob extends ControlView1dBase {
	private KnobShape knobShape;

	public Knob(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	@Override
	public void onParamChanged(Param param) {
		knobShape.setLevel(param.viewLevel);
	}

	@Override
	protected float posToLevel(Pointer pos) {
		float unitX = pos.x / width - .5f;
		float unitY = pos.y / height - .5f;
		float theta = (float) Math.atan(unitY / unitX) + � / 2;
		// atan ranges from 0 to �, and produces symmetric results around the y
		// axis.
		// we need 0 to 2*�, so ad � if right of x axis.
		if (unitX > 0)
			theta += �;
		// convert to level - remember, min theta is �/4, max is 7�/8
		float level = (4 * theta / � - 1) / 6;
		return GeneralUtils.clipToUnit(level);
	}

	@Override
	public synchronized void createChildren() {
		knobShape = new KnobShape(shapeGroup, Color.TRON_BLUE, null);
	}

	@Override
	public synchronized void layoutChildren() {
		knobShape.layout(absoluteX, absoluteY, width, height);
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		knobShape.setFillColor(selectColor);
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		super.handleActionUp(id, pos);
		knobShape.setFillColor(levelColor);
	}
}
