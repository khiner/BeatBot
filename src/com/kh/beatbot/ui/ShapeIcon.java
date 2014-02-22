package com.kh.beatbot.ui;

import com.kh.beatbot.ui.IconResource.State;
import com.kh.beatbot.ui.color.ColorSet;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public abstract class ShapeIcon extends Icon {

	protected final float OFFSET = 2;

	protected ShapeGroup shapeGroup;
	protected Shape currentShape;
	protected boolean shouldDraw;

	protected ShapeIcon(ShapeGroup shapeGroup, ColorSet bgColorSet) {
		this(shapeGroup, bgColorSet, null);
	}

	protected ShapeIcon(ShapeGroup shapeGroup, ColorSet fillColorSet,
			ColorSet strokeColorSet) {
		// if there is already a global group, then it will be drawn elsewhere.
		// otherwise, we create a new group to share amongst all icons
		shouldDraw = (shapeGroup == null);
		this.shapeGroup = shouldDraw ? new ShapeGroup() : shapeGroup;
		this.shapeGroup.setStrokeWeight(1);
		this.fillColorSet = fillColorSet;
		this.strokeColorSet = strokeColorSet;
		currentShape = initShape();
	}

	protected abstract Shape initShape();

	@Override
	public void setState(IconResource.State state) {
		this.state = lockedState != null ? lockedState : state;

		float[] fillColor = getCurrFillColor();
		float[] strokeColor = getCurrStrokeColor();
		if (null == fillColor && null == strokeColor
				&& this.state == State.PRESSED) {
			fillColor = getFillColor(State.SELECTED);
			strokeColor = getStrokeColor(State.SELECTED);
		}
		if (null == fillColor && null == strokeColor) {
			fillColor = getFillColor(State.DEFAULT);
			strokeColor = getStrokeColor(State.DEFAULT);
		}

		currentShape.layout(x + OFFSET, y + OFFSET, width - OFFSET * 2,
				height - OFFSET * 2);

		((Shape) currentShape).setColors(
				fillColor == null ? Colors.TRANSPARENT : fillColor,
				strokeColor == null ? Colors.TRANSPARENT : strokeColor);
	}

	@Override
	public void layout(float x, float y, float width, float height) {
		this.x = shouldDraw ? 0 : x;
		this.y = shouldDraw ? 0 : y;
		this.width = width;
		this.height = height;

		setState(state);
	}

	public void setFillColorSet(ColorSet fillColorSet) {
		this.fillColorSet = fillColorSet;
		setState(state);
	}

	public void bringToTop() {
		((Shape) currentShape).bringToTop();
	}

	public void destroy() {
		if (currentShape != null) {
			((Shape) currentShape).destroy();
		}
	}
}
