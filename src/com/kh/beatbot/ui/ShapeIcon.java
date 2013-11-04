package com.kh.beatbot.ui;

import com.kh.beatbot.ui.color.ColorSet;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public abstract class ShapeIcon extends Icon {

	private Shape prevShape;
	protected ShapeGroup shapeGroup;
	protected boolean shouldDraw;

	public ShapeIcon(ShapeGroup shapeGroup, ColorSet bgColorSet) {
		this(shapeGroup, bgColorSet, null);
	}

	public ShapeIcon(ShapeGroup shapeGroup, ColorSet bgColorSet,
			ColorSet borderColorSet) {
		// if there is already a global group, then it will be drawn elsewhere.
		// otherwise, we create a new group to share amongst all icons
		shouldDraw = (shapeGroup == null);
		this.shapeGroup = shouldDraw ? new ShapeGroup() : shapeGroup;
	}

	@Override
	public void draw() {
		if (shouldDraw) {
			shapeGroup.draw(1);
		}
	}

	@Override
	protected void setDrawable(Drawable icon) {
		prevShape = (Shape) currentDrawable;
		super.setDrawable(icon);
		if (prevShape == null || !prevShape.getGroup().contains(prevShape)) {
			((Shape) currentDrawable).getGroup().add((Shape) currentDrawable);
		} else {
			((Shape) currentDrawable).getGroup().replace(prevShape,
					(Shape) currentDrawable);
		}
	}

	public void setShapeGroup(ShapeGroup shapeGroup) {
		((Shape) currentDrawable).setGroup(shapeGroup);
	}

	public void setFillColorSet(ColorSet fillColorSet) {
		Drawable prevIcon = currentDrawable;
		setDrawable(resource.defaultDrawable);
		((Shape) resource.defaultDrawable)
				.setFillColor(fillColorSet.defaultColor);
		setDrawable(resource.pressedDrawable);
		((Shape) resource.pressedDrawable)
				.setFillColor(fillColorSet.pressedColor);
		if (fillColorSet.selectedColor != null) {
			setDrawable(resource.selectedDrawable);
			((Shape) resource.selectedDrawable)
					.setFillColor(fillColorSet.selectedColor);
		}
		setDrawable(prevIcon);
	}

	public void setColors(ColorSet fillColorSet, ColorSet outlineColorSet) {
		Drawable prevIcon = currentDrawable;
		setDrawable(resource.defaultDrawable);
		((Shape) resource.defaultDrawable).setColors(fillColorSet.defaultColor,
				outlineColorSet.defaultColor);
		setDrawable(resource.pressedDrawable);
		((Shape) resource.pressedDrawable).setColors(fillColorSet.pressedColor,
				outlineColorSet.pressedColor);
		if (fillColorSet.selectedColor != null
				&& outlineColorSet.selectedColor != null) {
			setDrawable(resource.selectedDrawable);
			((Shape) resource.selectedDrawable).setColors(
					fillColorSet.selectedColor, outlineColorSet.selectedColor);
		}
		setDrawable(prevIcon);
	}

	public float[] getCurrStrokeColor() {
		return ((Shape) currentDrawable).getStrokeColor();
	}

	public float[] getCurrFillColor() {
		return ((Shape) currentDrawable).getFillColor();
	}

	public void destroy() {
		if (currentDrawable != null) {
			shapeGroup.remove((Shape) currentDrawable);
		}
	}
}
