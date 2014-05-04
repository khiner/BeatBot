package com.kh.beatbot.ui.shape;

import com.kh.beatbot.ui.mesh.Mesh2D;

public abstract class Shape {
	public static final float ¹ = (float) Math.PI;
	protected Mesh2D fillMesh, strokeMesh;
	protected float[] fillColor, strokeColor;

	public float x, y, width, height;

	public Shape(RenderGroup group, float[] fillColor, float[] strokeColor, int numFillVertices,
			int numStrokeVertices) {
		this(group, fillColor, strokeColor, null, null, numFillVertices, numStrokeVertices);
	}

	public Shape(RenderGroup group, float[] fillColor, float[] strokeColor, short[] fillIndices,
			short[] strokeIndices, int numFillVertices, int numStrokeVertices) {
		RenderGroup myGroup = null == group ? new RenderGroup() : group;

		if (fillColor != null) {
			fillMesh = new Mesh2D(myGroup.getFillGroup(), numFillVertices, fillIndices);
			this.fillColor = fillColor;
		}
		if (strokeColor != null) {
			strokeMesh = new Mesh2D(myGroup.getStrokeGroup(), numStrokeVertices, strokeIndices);
			this.strokeColor = strokeColor;
		}
		show();
	}

	protected abstract void updateVertices();

	protected float getFillVertexX(int i) {
		return fillMesh.getGroup().getVertexX(i);
	}

	protected float getFillVertexY(int i) {
		return fillMesh.getGroup().getVertexY(i);
	}

	protected synchronized void fillVertex(float x, float y) {
		if (null != fillMesh) {
			fillMesh.vertex(x, y, fillColor);
		}
	}

	protected synchronized void strokeVertex(float x, float y) {
		if (null != strokeMesh) {
			strokeMesh.vertex(x, y, strokeColor);
		}
	}

	protected synchronized void resetIndices() {
		if (null != fillMesh)
			fillMesh.reset();
		if (null != strokeMesh)
			strokeMesh.reset();
	}

	public synchronized void update() {
		resetIndices();
		updateVertices();
	}

	protected synchronized void setFillColor(int vertexIndex, float[] fillColor) {
		if (fillMesh != null) {
			fillMesh.setColor(vertexIndex, fillColor);
		}
	}

	public synchronized void setFillColor(float[] fillColor) {
		this.fillColor = fillColor;
		if (fillMesh != null) {
			fillMesh.setColor(fillColor);
		}
	}

	public synchronized void setStrokeColor(float[] strokeColor) {
		this.strokeColor = strokeColor;
		if (null == strokeMesh) {
			return;
		}
		if (null != strokeColor) {
			strokeMesh.setColor(strokeColor);
		} else {
			strokeMesh.hide();
			strokeMesh = null;
		}
	}

	public synchronized void setColors(float[] fillColor, float[] strokeColor) {
		setFillColor(fillColor);
		setStrokeColor(strokeColor);
	}

	public Mesh2D getFillMesh() {
		return fillMesh;
	}

	public Mesh2D getStrokeMesh() {
		return strokeMesh;
	}

	public float[] getFillColor() {
		return fillColor;
	}

	public float[] getStrokeColor() {
		return strokeColor;
	}

	// set "z-index" of this shape to the top of the stack
	public void bringToTop() {
		if (null != fillMesh)
			fillMesh.push();
		if (null != strokeMesh)
			strokeMesh.push();
	}

	public synchronized void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
		if (fillMesh != null) {
			fillMesh.setPosition(x, y);
		}
		if (strokeMesh != null) {
			strokeMesh.setPosition(x, y);
		}
	}

	public synchronized void setDimensions(float width, float height) {
		this.width = width;
		this.height = height;
		if (null != fillMesh)
			fillMesh.setDimensions(width, height);
		if (null != strokeMesh)
			strokeMesh.setDimensions(width, height);
		update();
	}

	public synchronized void layout(float x, float y, float width, float height) {
		setPosition(x, y);
		setDimensions(width, height);
	}

	public void hide() {
		if (null != fillMesh)
			fillMesh.hide();
		if (null != strokeMesh)
			strokeMesh.hide();
	}

	public void show() {
		if (null != fillMesh)
			fillMesh.show();
		if (null != strokeMesh)
			strokeMesh.show();
		update();
	}

	public boolean isVisible() {
		return (null != fillMesh && fillMesh.isVisible())
				|| (null != strokeMesh && strokeMesh.isVisible());
	}
}
