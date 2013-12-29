package com.kh.beatbot.ui;

import com.kh.beatbot.ui.IconResource.State;
import com.kh.beatbot.ui.color.ColorSet;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public abstract class ShapeIcon extends Icon {

	protected final float OFFSET = 2;

	protected ShapeGroup shapeGroup;
	protected ColorSet fillColorSet, strokeColorSet;
	protected boolean shouldDraw;

	protected float pressedX, pressedY, pressedWidth, pressedHeight;

	protected ShapeIcon(ShapeGroup shapeGroup, Shape.Type type,
			ColorSet bgColorSet) {
		this(shapeGroup, type, bgColorSet, null);
	}

	protected ShapeIcon(ShapeGroup shapeGroup, Shape.Type type,
			ColorSet fillColorSet, ColorSet strokeColorSet) {
		// if there is already a global group, then it will be drawn elsewhere.
		// otherwise, we create a new group to share amongst all icons
		shouldDraw = (shapeGroup == null);
		this.shapeGroup = shouldDraw ? new ShapeGroup() : shapeGroup;
		this.shapeGroup.setStrokeWeight(1);
		this.fillColorSet = fillColorSet;
		this.strokeColorSet = strokeColorSet;
		currentDrawable = Shape.get(type, this.shapeGroup,
				fillColorSet == null ? null : Colors.TRANSPARENT,
				strokeColorSet == null ? null : Colors.TRANSPARENT);
	}

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

		switch (this.state) {
		case DEFAULT:
		case DISABLED:
			currentDrawable.layout(x + OFFSET, y + OFFSET, width - OFFSET * 2,
					height - OFFSET * 2);
			break;
		case PRESSED:
		case SELECTED:
			currentDrawable.layout(pressedX, pressedY, pressedWidth,
					pressedHeight);
			break;
		}

		((Shape) currentDrawable).setColors(
				fillColor == null ? Colors.TRANSPARENT : fillColor,
				strokeColor == null ? Colors.TRANSPARENT : strokeColor);
	}

	@Override
	public void layout(float x, float y, float width, float height) {
		this.x = shouldDraw ? 0 : x;
		this.y = shouldDraw ? 0 : y;
		this.width = width;
		this.height = height;

		float pressedScale = Math.min(width, height) * .1f;
		pressedWidth = width - OFFSET * 2 - pressedScale;
		pressedHeight = height - OFFSET * 2 - pressedScale;
		pressedX = this.x + width / 2 - pressedWidth / 2;
		pressedY = this.y + height / 2 - pressedHeight / 2;

		setState(state);
	}

	@Override
	public void draw() {
		if (shouldDraw) {
			shapeGroup.draw();
		}
	}

	public void setFillColorSet(ColorSet fillColorSet) {
		this.fillColorSet = fillColorSet;
	}

	public void destroy() {
		if (currentDrawable != null) {
			((Shape) currentDrawable).destroy();
		}
	}

	public float[] getCurrFillColor() {
		return getFillColor(state);
	}

	public float[] getCurrStrokeColor() {
		return getStrokeColor(state);
	}

	private float[] getFillColor(State state) {
		return fillColorSet == null ? null : fillColorSet.getColor(state);
	}

	private float[] getStrokeColor(State state) {
		return strokeColorSet == null ? null : strokeColorSet.getColor(state);
	}
}
