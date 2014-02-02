package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.Drawable;

public abstract class Shape extends Drawable {
	public static final float ¹ = (float) Math.PI;
	protected Mesh2D fillMesh, strokeMesh;
	private float[] fillColor, strokeColor;

	protected ShapeGroup group;
	private boolean shouldDraw;

	protected Shape(ShapeGroup group) {
		// must draw via some parent group. if one is given, use that,
		// otherwise create a new group and render upon request using that group
		shouldDraw = group == null;
		this.group = group != null ? group : new ShapeGroup();
	}

	public Shape(ShapeGroup group, float[] fillColor, float[] strokeColor, int numFillVertices, int numStrokeVertices) {
		this(group, fillColor, strokeColor, null, null, numFillVertices, numStrokeVertices);
	}

	public Shape(ShapeGroup group, float[] fillColor, float[] strokeColor,
			short[] fillIndices, short[] strokeIndices, int numFillVertices,
			int numStrokeVertices) {
		this(group);
		if (fillColor != null) {
			fillMesh = new Mesh2D(this.group.fillGroup, numFillVertices,
					fillIndices);
			this.fillColor = fillColor;
		}
		if (strokeColor != null) {
			strokeMesh = new Mesh2D(this.group.strokeGroup, numStrokeVertices,
					strokeIndices);
			this.strokeColor = strokeColor;
		}
		this.group.add(this);
	}

	protected abstract void updateVertices();

	protected float getFillVertexX(int i) {
		return fillMesh.group.getVertexX(i);
	}

	protected float getFillVertexY(int i) {
		return fillMesh.group.getVertexY(i);
	}

	protected synchronized void fillVertex(float x, float y) {
		if (fillMesh != null) {
			fillMesh.vertex(x, y, fillColor);
		}
	}

	protected synchronized void fillVertex(float x, float y, float[] color) {
		if (fillMesh != null) {
			fillMesh.vertex(x, y, color);
		}
	}

	protected synchronized void strokeVertex(float x, float y) {
		if (strokeMesh != null) {
			strokeMesh.vertex(x, y, strokeColor);
		}
	}

	protected synchronized void strokeVertex(float x, float y, float[] color) {
		if (strokeMesh != null) {
			strokeMesh.vertex(x, y, color);
		}
	}

	protected synchronized void resetIndices() {
		if (fillMesh != null) {
			fillMesh.index = 0;
		}
		if (strokeMesh != null) {
			strokeMesh.index = 0;
		}
	}

	public synchronized void update() {
		if (width > 0 && height > 0) {
			resetIndices();
			updateVertices();
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
		if (strokeMesh != null) {
			strokeMesh.setColor(strokeColor);
		}
	}

	public synchronized void setColors(float[] fillColor, float[] strokeColor) {
		setFillColor(fillColor);
		setStrokeColor(strokeColor);
	}

	public synchronized void setStrokeWeight(int strokeWeight) {
		group.setStrokeWeight(strokeWeight);
	}

	public synchronized Mesh2D getFillMesh() {
		return fillMesh;
	}

	public synchronized Mesh2D getStrokeMesh() {
		return strokeMesh;
	}

	public synchronized float[] getFillColor() {
		return fillColor;
	}

	public synchronized float[] getStrokeColor() {
		return strokeColor;
	}

	// set "z-index" of this shape to the top of the stack
	public void bringToTop() {
		group.push(this);
	}

	@Override
	public synchronized void setPosition(float x, float y) {
		if (fillMesh != null) {
			fillMesh.translate(x - this.x, y - this.y);
		}
		if (strokeMesh != null) {
			strokeMesh.translate(x - this.x, y - this.y);
		}
		super.setPosition(x, y);
	}

	@Override
	public synchronized void setDimensions(float width, float height) {
		boolean dimChanged = width != this.width || height != this.height;
		if (width <= 0 || height <= 0 || !dimChanged)
			return;
		super.setDimensions(width, height);
		update();
	}

	@Override
	public synchronized void layout(float x, float y, float width, float height) {
		if (width <= 0 || height <= 0)
			return;
		boolean dimChanged = width != this.width || height != this.height;
		boolean posChanged = x != this.x || y != this.y;
		if (dimChanged && posChanged) {
			super.layout(x, y, width, height);
			update();
		} else if (dimChanged) {
			setDimensions(width, height);
		} else if (posChanged) {
			setPosition(x, y);
		}
	}

	@Override
	public synchronized void draw(float x, float y, float width, float height) {
		if (shouldDraw) {
			group.draw();
		}
	}

	public void destroy() {
		group.remove(this);
	}
}
