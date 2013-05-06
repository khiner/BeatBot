package com.kh.beatbot.view.mesh;

import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.global.Drawable;
import com.kh.beatbot.view.BBView;

public abstract class Shape extends Drawable {
	public static final float ¹ = (float) Math.PI;
	
	protected ShapeGroup group;
	protected Mesh2D fillMesh, outlineMesh;
	protected boolean shouldDraw;
	
	public Shape(ShapeGroup group) {
		// must draw via some parent group.  if one is given, use that, 
		// otherwise create a new group and render upon request using that group
		shouldDraw = group == null;
		this.group = group != null ? group : new ShapeGroup();
	}
	
	public Shape(ShapeGroup group, Mesh2D fillMesh, Mesh2D outlineMesh) {
		this(group);
		this.fillMesh = fillMesh;
		this.outlineMesh = outlineMesh;
	}
	
	protected abstract void createVertices(float[] fillColor, float[] outlineColor);

	protected void update() {
		fillMesh.index = 0;
		outlineMesh.index = 0;
		createVertices(fillMesh.color, outlineMesh.color);
		group.update(this);
	}
	
	public void setColors(float[] fillColor, float[] outlineColor) {
		fillMesh.color = fillColor;
		outlineMesh.color = outlineColor;
		update();
	}
	
	public float[] getStrokeColor() {
		return outlineMesh.color;
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
	
	public Mesh2D getFillMesh() {
		return fillMesh;
	}
	
	public Mesh2D getOutlineMesh() {
		return outlineMesh;
	}

	public ShapeGroup getGroup() {
		return group;
	}
	
	public void layout(float x, float y, float width, float height) {
		super.layout(x, y, width, height);
		update();
	}
	
	@Override
	public void draw() {
		if (shouldDraw) {
			group.draw((GL11)BBView.gl, 1);
		}
	}
}
