package com.kh.beatbot.ui.mesh;

import com.kh.beatbot.ui.Drawable;

public abstract class Shape extends Drawable {
	public static enum Type {
		RECTANGLE, ROUNDED_RECT, SLIDE_TAB
	};

	public static final float ¹ = (float) Math.PI;

	private Mesh2D fillMesh, strokeMesh;
	protected ShapeGroup group;
	protected boolean shouldDraw;

	public static Shape get(Type type, ShapeGroup group, float[] fillColor,
			float[] strokeColor) {
		if (fillColor == null && strokeColor == null) {
			return null;
		} else {
			Shape shape = create(type, group);
			if (fillColor != null) {
				shape.fillMesh = new Mesh2D(shape.getNumFillVertices(),
						fillColor);
			}
			if (strokeColor != null) {
				shape.strokeMesh = new Mesh2D(shape.getNumStrokeVertices(),
						strokeColor);
				
			}
			shape.updateGroup();
			return shape;
		}
	}

	protected static Shape create(Type type, ShapeGroup group) {
		switch (type) {
		case RECTANGLE:
			return new Rectangle(group);
		case ROUNDED_RECT:
			return new RoundedRect(group);
		case SLIDE_TAB:
			return new SlideTab(group);

		default:
			return null;
		}
	}

	public static WaveformShape createWaveform(ShapeGroup group, float width,
			float[] fillColor, float[] strokeColor) {
		Shape waveform = new WaveformShape(group, width);
		waveform.fillMesh = new Mesh2D(waveform.getNumFillVertices(), fillColor);
		waveform.strokeMesh = new Mesh2D(waveform.getNumStrokeVertices(),
				strokeColor);
		return (WaveformShape) waveform;
	}

	protected abstract int getNumFillVertices();

	protected abstract int getNumStrokeVertices();

	protected abstract void updateVertices();

	protected synchronized void fillVertex(float x, float y) {
		if (fillMesh != null) {
			fillMesh.vertex(x, y);
		}
	}

	protected synchronized void strokeVertex(float x, float y) {
		if (strokeMesh != null) {
			strokeMesh.vertex(x, y);
		}
	}

	protected Shape(ShapeGroup group) {
		// must draw via some parent group. if one is given, use that,
		// otherwise create a new group and render upon request using that group
		shouldDraw = group == null;
		this.group = group != null ? group : new ShapeGroup();
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
			group.update(this);
		}
	}

	protected synchronized void update() {
		resetIndices();
		updateVertices();
		updateGroup();
	}

	public synchronized void setFillColor(float[] fillColor) {
		if (fillMesh != null) {
			fillMesh.setColor(fillColor);
		}
		updateGroup();
	}

	public synchronized void setStrokeColor(float[] strokeColor) {
		if (strokeMesh != null) {
			strokeMesh.setColor(strokeColor);
		}
		updateGroup();
	}

	public synchronized void setColors(float[] fillColor, float[] strokeColor) {
		if (fillMesh != null) {
			fillMesh.setColor(fillColor);
		}
		if (strokeMesh != null) {
			strokeMesh.setColor(strokeColor);
		}
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

	public synchronized float[] getStrokeColor() {
		return strokeMesh != null ? strokeMesh.getColor() : null;
	}

	public synchronized float[] getFillColor() {
		return fillMesh != null ? fillMesh.getColor() : null;
	}

	public synchronized void setGroup(ShapeGroup group) {
		if (this.group == group) {
			return; // already a member of this group
		}
		if (this.group != null) {
			this.group.remove(this);
		}
		this.group = group;
		group.add(this);
	}

	public synchronized ShapeGroup getGroup() {
		return group;
	}

	public synchronized void setPosition(float x, float y) {
		if (fillMesh != null) {
			fillMesh.translate(x - this.x, y - this.y);
		}
		if (strokeMesh != null) {
			strokeMesh.translate(x - this.x, y - this.y);
		}
		super.setPosition(x, y);
		updateGroup();
	}

	public synchronized void layout(float x, float y, float width, float height) {
		if (width != this.width || height != this.height) {
			super.layout(x, y, width, height);
			update();
		} else {
			setPosition(x, y);
		}
	}

	@Override
	public synchronized void draw(float x, float y, float width, float height) {
		if (shouldDraw) {
			group.draw();
		}
	}
}
