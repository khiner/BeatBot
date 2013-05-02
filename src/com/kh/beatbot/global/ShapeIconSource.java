package com.kh.beatbot.global;

import com.kh.beatbot.view.mesh.RoundedRect;
import com.kh.beatbot.view.mesh.Shape;
import com.kh.beatbot.view.mesh.ShapeGroup;

public abstract class ShapeIconSource extends IconSource {

	private Shape prevShape;
	protected ShapeGroup shapeGroup;
	protected boolean shouldDraw;

	public ShapeIconSource(ShapeGroup shapeGroup, float x, float y,
			float width, float height, float cornerRadius, ColorSet bgColorSet,
			ColorSet borderColorSet) {
		// if there is already a global group, then it will be drawn elsewhere.
		// otherwise, we create a new group to share amongst all icons
		shouldDraw = shapeGroup == null;
		this.shapeGroup = shouldDraw ? new ShapeGroup() : shapeGroup;
	}
		
	@Override
	protected void setIcon(Drawable icon) {
		prevShape = (Shape) currentIcon;
		super.setIcon(icon);
		if (prevShape == null) {
			((RoundedRect) currentIcon).getGroup().add((Shape)currentIcon);
		} else {
			((RoundedRect) currentIcon).getGroup().replace(prevShape, (Shape)currentIcon);
		}
	}
	
	public void setShapeGroup(ShapeGroup shapeGroup) {
		((RoundedRect) currentIcon).setGroup(shapeGroup);
	}
	
	public void setColors(ColorSet fillColorSet, ColorSet outlineColorSet) {
		((Shape)defaultIcon).setColors(fillColorSet.defaultColor, outlineColorSet.defaultColor);
		((Shape)pressedIcon).setColors(fillColorSet.pressedColor, outlineColorSet.pressedColor);
		((Shape)selectedIcon).setColors(fillColorSet.selectedColor, outlineColorSet.selectedColor);
	}
}
