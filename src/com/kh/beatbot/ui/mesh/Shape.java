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

	public void update(float x, float y, float width, float height,
			float[] fillColor) {
		update(x, y, width, height, fillColor, null);
	}

	public void update(float x, float y, float width, float height,
			float[] fillColor, float[] outlineColor) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		if (fillMesh != null) {
			fillMesh.color = fillColor;
		}
		if (strokeMesh != null) {
			strokeMesh.color = outlineColor;
		}
		update();
	}

	protected abstract int getNumFillVertices();

	protected abstract int getNumStrokeVertices();

	protected void fillVertex(float x, float y) {
		if (fillMesh != null) {
			fillMesh.vertex(x, y);
		}
	}

	protected void strokeVertex(float x, float y) {
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

	protected abstract void updateVertices();

	protected void update() {
		if (fillMesh != null) {
			fillMesh.index = 0;
		}
		if (strokeMesh != null) {
			strokeMesh.index = 0;
		}
		updateVertices();
		if (fillMesh != null) {
			fillMesh.setColor(fillMesh.color);
		}
		if (strokeMesh != null) {
			strokeMesh.setColor(strokeMesh.color);
		}
		if (shouldDraw && !group.contains(this)) {
			group.add(this);
		} else {
			group.update(this);
		}
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
			group.draw();
		}
	}
}
