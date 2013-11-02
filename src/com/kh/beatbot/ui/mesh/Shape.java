package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.Drawable;

public abstract class Shape extends Drawable {
	public static final float � = (float) Math.PI;
	
	protected ShapeGroup group;
	protected Mesh2D fillMesh, strokeMesh;
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
	
	public Shape(ShapeGroup group, Mesh2D fillMesh, Mesh2D strokeMesh) {
		this(group);
		this.fillMesh = fillMesh;
		this.strokeMesh = strokeMesh;
	}
	
	public void setBorderWeight(float borderWeight) {
		this.borderWeight = (int) borderWeight;
	}
	
	protected abstract void createVertices(float[] fillColor);
	protected abstract void createVertices(float[] fillColor, float[] strokeColor);

	protected void update() {
		fillMesh.index = 0;
		if (strokeMesh != null) {
			strokeMesh.index = 0;
		}
		if (strokeMesh != null) {
			createVertices(fillMesh.color, strokeMesh.color);
		} else {
			createVertices(fillMesh.color);
		}
		if (shouldDraw && !group.contains(this)) {
			this.group.add(this);
		}
		group.update(this);
	}
	
	public void setFillColor(float[] fillColor) {
		fillMesh.color = fillColor;
		update();
	}
	
	public void setColors(float[] fillColor, float[] strokeColor) {
		fillMesh.color = fillColor;
		strokeMesh.color = strokeColor;
		update();
	}
	
	public Mesh2D getFillMesh() {
		return fillMesh;
	}
	
	public Mesh2D getStrokeMesh() {
		return strokeMesh;
	}

	public float[] getStrokeColor() {
		return strokeMesh != null ? strokeMesh.color : null;
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
