package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.effect.Param;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.Circle;
import com.kh.beatbot.ui.shape.IntersectingLines;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.view.View;

public class Seekbar2d extends ControlView2dBase {
	private IntersectingLines intersectingLines;
	private Circle circle;

	public Seekbar2d(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	public synchronized void createChildren() {
		selectColor = Color.LABEL_SELECTED;
		initRoundedRect();
		intersectingLines = new IntersectingLines(renderGroup, null, Color.TRON_BLUE);
		circle = new Circle(renderGroup, Color.TRON_BLUE, null);
		addShapes(intersectingLines, circle);
	}

	public synchronized void layoutChildren() {
		intersectingLines.layout(absoluteX + BG_OFFSET, absoluteY + BG_OFFSET, width - BG_OFFSET
				* 2, height - BG_OFFSET * 2);

		float circleDiameter = ((RoundedRect) bgShape).cornerRadius;
		circle.setDimensions(circleDiameter, circleDiameter);
	}

	public void onParamChanged(Param param) {
		float viewX = viewX(params[0].viewLevel);
		float viewY = viewY(params[1].viewLevel);
		circle.setPosition(absoluteX + BG_OFFSET + viewX, absoluteY + BG_OFFSET + viewY);
		intersectingLines.setIntersect(viewX, viewY);
	}

	protected float xToLevel(float x) {
		return unitX(clipX(x));
	}

	protected float yToLevel(float y) {
		return unitY(clipY(y));
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		params[0].setLevel(xToLevel(pos.x));
		params[1].setLevel(yToLevel(pos.y));
		intersectingLines.setStrokeColor(selectColor);
		circle.setFillColor(selectColor);
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		super.handleActionUp(id, pos);
		intersectingLines.setStrokeColor(levelColor);
		circle.setFillColor(levelColor);
	}
}
