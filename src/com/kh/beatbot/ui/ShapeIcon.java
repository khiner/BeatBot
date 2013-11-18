package com.kh.beatbot.ui;

import com.kh.beatbot.ui.color.ColorSet;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public abstract class ShapeIcon extends Icon {

	protected ShapeGroup shapeGroup;
	protected boolean shouldDraw;

	protected ShapeIcon(ShapeGroup shapeGroup, Shape.Type type,
			ColorSet bgColorSet) {
		this(shapeGroup, type, bgColorSet, null);
	}

	protected ShapeIcon(ShapeGroup shapeGroup, Shape.Type type,
			ColorSet bgColorSet, ColorSet strokeColorSet) {
		// if there is already a global group, then it will be drawn elsewhere.
		// otherwise, we create a new group to share amongst all icons
		shouldDraw = (shapeGroup == null);
		this.shapeGroup = shouldDraw ? new ShapeGroup() : shapeGroup;
		this.shapeGroup.setStrokeWeight(1);
		Shape defaultShape = Shape.get(type, this.shapeGroup,
				bgColorSet == null ? null : bgColorSet.defaultColor,
				strokeColorSet == null ? null : strokeColorSet.defaultColor);

		Shape pressedShape = Shape.get(type, this.shapeGroup,
				bgColorSet == null ? null : bgColorSet.pressedColor,
				strokeColorSet == null ? null : strokeColorSet.pressedColor);

		Shape selectedShape = Shape.get(type, this.shapeGroup,
				bgColorSet == null ? null : bgColorSet.selectedColor,
				strokeColorSet == null ? null : strokeColorSet.selectedColor);

		this.resource = new IconResource(defaultShape, pressedShape,
				selectedShape, null, null, null);
		setState(IconResource.State.DEFAULT);
	}

	@Override
	public void draw() {
		if (shouldDraw) {
			shapeGroup.draw();
		}
	}

	@Override
	protected void setDrawable(Drawable icon) {
		Shape prevShape = (Shape) currentDrawable;
		super.setDrawable(icon);
		Shape currShape = (Shape) currentDrawable;
		if (currShape == null) {
			if (prevShape != null) {
				prevShape.getGroup().remove(prevShape);
			}
			return;
		} else if (prevShape == null
				|| !prevShape.getGroup().contains(prevShape)) {
			currShape.getGroup().add(currShape);
		} else {
			currShape.getGroup().replace(prevShape, currShape);
		}
	}

	public void setShapeGroup(ShapeGroup shapeGroup) {
		if (currentDrawable != null) {
			((Shape) currentDrawable).setGroup(shapeGroup);
		}
	}

	public void setFillColorSet(ColorSet fillColorSet) {
		Drawable prevIcon = currentDrawable;
		if (resource.defaultDrawable != null) {
			setDrawable(resource.defaultDrawable);
			((Shape) resource.defaultDrawable)
					.setFillColor(fillColorSet.defaultColor);
		}
		if (resource.pressedDrawable != null) {
			setDrawable(resource.pressedDrawable);
			((Shape) resource.pressedDrawable)
					.setFillColor(fillColorSet.pressedColor);
		}
		if (fillColorSet.selectedColor != null) {
			setDrawable(resource.selectedDrawable);
			((Shape) resource.selectedDrawable)
					.setFillColor(fillColorSet.selectedColor);
		}
		setDrawable(prevIcon);
	}

	public void setColors(ColorSet fillColorSet, ColorSet outlineColorSet) {
		Drawable prevIcon = currentDrawable;
		if (resource.defaultDrawable != null) {
			setDrawable(resource.defaultDrawable);
			((Shape) resource.defaultDrawable).setColors(
					fillColorSet.defaultColor, outlineColorSet.defaultColor);
		}
		if (resource.pressedDrawable != null) {
			setDrawable(resource.pressedDrawable);
			((Shape) resource.pressedDrawable).setColors(
					fillColorSet.pressedColor, outlineColorSet.pressedColor);
		}
		if (fillColorSet.selectedColor != null
				&& outlineColorSet.selectedColor != null) {
			setDrawable(resource.selectedDrawable);
			((Shape) resource.selectedDrawable).setColors(
					fillColorSet.selectedColor, outlineColorSet.selectedColor);
		}
		setDrawable(prevIcon);
	}

	public float[] getCurrStrokeColor() {
		if (currentDrawable != null) {
			return ((Shape) currentDrawable).getStrokeColor();
		} else {
			return Colors.GREEN; // TODO this is to make it stand out, to fix
		}
	}

	public float[] getCurrFillColor() {
		if (currentDrawable != null) {
			return ((Shape) currentDrawable).getFillColor();
		} else {
			return Colors.GREEN; // TODO this is to make it stand out, to fix
		}
	}

	public void destroy() {
		if (currentDrawable != null) {
			shapeGroup.remove((Shape) currentDrawable);
		}
	}
}
