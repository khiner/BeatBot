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

	public Shape(ShapeGroup group, float[] fillColor, float[] strokeColor) {
		this(group);
		if (fillColor != null) {
			fillMesh = new Mesh2D(this.group.fillGroup, getNumFillVertices());
			this.fillColor = fillColor;
		}
		if (strokeColor != null) {
			strokeMesh = new Mesh2D(this.group.strokeGroup,
					getNumStrokeVertices());
			this.strokeColor = strokeColor;
		}
		this.group.add(this);
	}

	public static WaveformShape createWaveform(ShapeGroup group, float width,
			float[] fillColor, float[] strokeColor) {
		Shape waveform = new WaveformShape(group, width);
		waveform.fillMesh = new Mesh2D(waveform.group.fillGroup,
				waveform.getNumFillVertices());
		waveform.strokeMesh = new Mesh2D(waveform.group.strokeGroup,
				waveform.getNumStrokeVertices());
		waveform.setColors(fillColor, strokeColor);
		return (WaveformShape) waveform;
	}

	protected abstract int getNumFillVertices();

	protected abstract int getNumStrokeVertices();

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

	protected synchronized void updateGroup() {
		if (!group.contains(this)) {
			group.add(this);
		} else {
			update();
		}
	}

	public synchronized void update() {
		if (width > 0 && height > 0) {
			resetIndices();
			updateVertices();
		}
	}

	public synchronized void setFillColor(float[] fillColor) {
		if (fillColor != null && fillColor.equals(this.fillColor)) {
			return;
		}
		this.fillColor = fillColor;
		updateGroup();
	}

	public synchronized void setStrokeColor(float[] strokeColor) {
		if (strokeColor != null && strokeColor.equals(this.strokeColor)) {
			return;
		}
		this.strokeColor = strokeColor;
		updateGroup();
	}

	public synchronized void setColors(float[] fillColor, float[] strokeColor) {
		this.fillColor = fillColor;
		this.strokeColor = strokeColor;
		updateGroup();
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
