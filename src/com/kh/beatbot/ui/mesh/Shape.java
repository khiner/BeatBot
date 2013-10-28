package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.Drawable;

public abstract class Shape extends Drawable {
	public static final float ¹ = (float) Math.PI;
	
	protected ShapeGroup group;
	protected Mesh2D fillMesh, outlineMesh;
	protected boolean shouldDraw;
	protected int borderWeight = 2;
	
	public Shape(ShapeGroup group) {
		// must draw via some parent group.  if one is given, use that, 
		// otherwise create a new group and render upon request using that group
		shouldDraw = group == null;
		this.group = group != null ? group : new ShapeGroup();
	}
	
	public Shape(ShapeGroup group, Mesh2D fillMesh) {
		this(group, fillMesh, null);
	}
	
	public Shape(ShapeGroup group, Mesh2D fillMesh, Mesh2D outlineMesh) {
		this(group);
		this.fillMesh = fillMesh;
		this.outlineMesh = outlineMesh;
	}
	
	public void setBorderWeight(float borderWeight) {
		this.borderWeight = (int) borderWeight;
	}
	
	protected abstract void createVertices(float[] fillColor);
	protected abstract void createVertices(float[] fillColor, float[] outlineColor);

	protected void update() {
		fillMesh.index = 0;
		if (outlineMesh != null) {
			outlineMesh.index = 0;
		}
		if (outlineMesh != null) {
			createVertices(fillMesh.color, outlineMesh.color);
		} else {
			createVertices(fillMesh.color);
		}
		if (!group.contains(this)) {
			this.group.add(this);
		}
		group.update(this);
	}
	
	public void setFillColor(float[] fillColor) {
		fillMesh.color = fillColor;
		update();
	}
	
	public void setColors(float[] fillColor, float[] outlineColor) {
		fillMesh.color = fillColor;
		outlineMesh.color = outlineColor;
		update();
	}
	
	public float[] getStrokeColor() {
		return outlineMesh != null ? outlineMesh.color : null;
	}
	
	public float[] getFillColor() {
		return fillMesh.color;
	}
	
	public void setGroup(ShapeGroup group) {
		if (this.group == group) {
			return; // already a member of this group
		}
		if (this.group != null) {
			this.group.remove(this);
		}
		this.group = group;
		group.add(this);
	}

	public ShapeGroup getGroup() {
		return group;
	}
	
	public void layout(float x, float y, float width, float height) {
		super.layout(x, y, width, height);
		update();
	}
	
	@Override
	public void draw(float x, float y, float width, float height) {
		if (shouldDraw) {
			group.draw(borderWeight);
		}
	}
}
