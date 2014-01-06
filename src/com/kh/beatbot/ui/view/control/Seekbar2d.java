package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.Circle;
import com.kh.beatbot.ui.mesh.IntersectingLines;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public class Seekbar2d extends ControlView2dBase {
	private IntersectingLines intersectingLines;
	private Circle circle;

	public Seekbar2d(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	public synchronized void createChildren() {
		selectColor = Colors.LABEL_SELECTED;
		initBgRect(true);
		intersectingLines = new IntersectingLines(shapeGroup, null,
				Colors.VOLUME);
		circle = new Circle(shapeGroup, Colors.VOLUME, null);
	}

	public synchronized void layoutChildren() {
		intersectingLines.layout(absoluteX + BG_OFFSET, absoluteY + BG_OFFSET,
				width - BG_OFFSET * 2, height - BG_OFFSET * 2);
		float circleDiameter = 4 * calcBgRectRadius() / 3;
		circle.setDimensions(circleDiameter, circleDiameter);
	}

	public void onParamChanged(Param param) {
		float viewX = viewX(params[0].viewLevel);
		float viewY = viewY(params[1].viewLevel);
		circle.setPosition(absoluteX + BG_OFFSET + viewX, absoluteY + BG_OFFSET
				+ viewY);
		intersectingLines.setIntersect(viewX, viewY);
	}

	protected float xToLevel(float x) {
		return unitX(clipX(x));
	}

	protected float yToLevel(float y) {
		return unitY(clipY(y));
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		params[0].setLevel(xToLevel(x));
		params[1].setLevel(yToLevel(y));
		intersectingLines.setStrokeColor(selectColor);
		circle.setFillColor(selectColor);
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
		intersectingLines.setStrokeColor(levelColor);
		circle.setFillColor(levelColor);
	}
}
