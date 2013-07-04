package com.kh.beatbot.global;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.mesh.Shape;
import com.kh.beatbot.view.mesh.ShapeGroup;

public abstract class ShapeIconSource extends IconSource {

	private Shape prevShape;
	protected ShapeGroup shapeGroup;
	protected boolean shouldDraw;
	
	public ShapeIconSource(ShapeGroup shapeGroup, ColorSet bgColorSet) {
		this(shapeGroup, bgColorSet, null);
	}
	
	public ShapeIconSource(ShapeGroup shapeGroup, ColorSet bgColorSet,
			ColorSet borderColorSet) {
		// if there is already a global group, then it will be drawn elsewhere.
		// otherwise, we create a new group to share amongst all icons
		shouldDraw = (shapeGroup == null);
		this.shapeGroup = shouldDraw ? new ShapeGroup() : shapeGroup;
	}
		
	@Override
	public void draw() {
		if (shouldDraw) {
			shapeGroup.draw((GL11) BBView.gl, 1);
		}
	}
	
	@Override
	protected void setIcon(Drawable icon) {
		prevShape = (Shape) currentIcon;
		super.setIcon(icon);
		if (prevShape == null) {
			((Shape) currentIcon).getGroup().add((Shape)currentIcon);
		} else {
			((Shape) currentIcon).getGroup().replace(prevShape, (Shape)currentIcon);
		}
	}
	
	public void setShapeGroup(ShapeGroup shapeGroup) {
		((Shape) currentIcon).setGroup(shapeGroup);
	}
	
	public void setFillColorSet(ColorSet fillColorSet) {
		Drawable prevIcon = currentIcon;
		setIcon(defaultIcon);
		((Shape)defaultIcon).setFillColor(fillColorSet.defaultColor);
		setIcon(pressedIcon);
		((Shape)pressedIcon).setFillColor(fillColorSet.pressedColor);
		if (fillColorSet.selectedColor != null) {
			setIcon(selectedIcon);
			((Shape)selectedIcon).setFillColor(fillColorSet.selectedColor);
		}
		setIcon(prevIcon);
	}
	
	public void setColors(ColorSet fillColorSet, ColorSet outlineColorSet) {
		Drawable prevIcon = currentIcon;
		setIcon(defaultIcon);
		((Shape)defaultIcon).setColors(fillColorSet.defaultColor, outlineColorSet.defaultColor);
		setIcon(pressedIcon);
		((Shape)pressedIcon).setColors(fillColorSet.pressedColor, outlineColorSet.pressedColor);
		if (fillColorSet.selectedColor != null && outlineColorSet.selectedColor != null) {
			setIcon(selectedIcon);
			((Shape)selectedIcon).setColors(fillColorSet.selectedColor, outlineColorSet.selectedColor);
		}
		setIcon(prevIcon);
	}
	
	public float[] getCurrStrokeColor() {
		return ((Shape)currentIcon).getStrokeColor();
	}
	
	public float[] getCurrFillColor() {
		return ((Shape)currentIcon).getFillColor();
	}
	
	public void destroy() {
		shapeGroup.remove((Shape)currentIcon);
	}
}
