package com.kh.beatbot.ui.mesh;

public abstract class Shape {
	public static final float ¹ = (float) Math.PI;
	protected Mesh2D fillMesh, strokeMesh;
	protected float[] fillColor, strokeColor;

	protected ShapeGroup group;
	private boolean shouldDraw = false;

	public float x, y, width, height;

	protected Shape(ShapeGroup group) {
		// must draw via some parent group. if one is given, use that,
		// otherwise create a new group and render upon request using that group
		shouldDraw = group == null;
		this.group = group != null ? group : new ShapeGroup();
	}

	public Shape(ShapeGroup group, float[] fillColor, float[] strokeColor,
			int numFillVertices, int numStrokeVertices) {
		this(group, fillColor, strokeColor, null, null, numFillVertices,
				numStrokeVertices);
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

	protected synchronized void strokeVertex(float x, float y) {
		if (strokeMesh != null) {
			strokeMesh.vertex(x, y, strokeColor);
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
		boolean fillChanged = fillMesh != null
				&& fillMesh.setDimensions(width, height);
		boolean strokeChanged = strokeMesh != null
				&& strokeMesh.setDimensions(width, height);
		if (fillChanged || strokeChanged) {
			update();
		}
	}

	public synchronized void layout(float x, float y, float width, float height) {
		if (width <= 0 || height <= 0)
			return;
		setPosition(x, y);
		setDimensions(width, height);
	}

	public void draw() {
		if (shouldDraw) {
			group.draw();
		}
	}

	public void destroy() {
		group.remove(this);
	}
}
