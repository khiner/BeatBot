package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.Circle;
import com.kh.beatbot.ui.mesh.IntersectingLines;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.Shape.Type;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public class Seekbar2d extends ControlView2dBase {
	private ShapeGroup shapeGroup;
	private IntersectingLines intersectingLines;
	private Circle circle;

	public Seekbar2d() {
		super();
		selectColor = Colors.LABEL_SELECTED;
	}

	public synchronized void createChildren() {
		shapeGroup = new ShapeGroup();
		initBgRect(Type.ROUNDED_RECT, shapeGroup, Colors.VIEW_BG, Colors.VOLUME);
		intersectingLines = (IntersectingLines) Shape.get(
				Type.INTERSECTING_LINES, shapeGroup, null, Colors.VOLUME);
		circle = (Circle) Shape.get(Type.CIRCLE, shapeGroup, Colors.VOLUME,
				null);
	}

	public synchronized void layoutChildren() {
		intersectingLines.layout(BG_OFFSET, BG_OFFSET, width - BG_OFFSET * 2,
				height - BG_OFFSET * 2);
		float circleDiameter = 4 * getBgRectRadius() / 3;
		circle.layout(0, 0, circleDiameter, circleDiameter);
	}

	public void onParamChanged(Param param) {
		float viewX = viewX(params[0].viewLevel);
		float viewY = viewY(params[1].viewLevel);
		circle.setPosition(BG_OFFSET + viewX - circle.width / 2, BG_OFFSET
				+ viewY - circle.height / 2);
		intersectingLines.setIntersect(viewX, viewY);
	}

	@Override
	public void draw() {
		shapeGroup.draw();
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
