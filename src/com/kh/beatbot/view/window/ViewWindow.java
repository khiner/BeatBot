package com.kh.beatbot.view.window;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;

import com.kh.beatbot.view.GLSurfaceViewBase;

public abstract class ViewWindow {
	class Position {
		float x, y;

		Position(float x, float y) {
			set(x, y);
		}

		public void set(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	protected List<ViewWindow> children = new ArrayList<ViewWindow>();
	
	protected GLSurfaceViewBase parent = null;
	protected GL10 gl = null;
	public float x = 0, y = 0;
	public float width = 0, height = 0;

	public ViewWindow(GLSurfaceViewBase parent) {
		this.parent = parent;
		createChildren();
	}

	public void addChild(ViewWindow child) {
		children.add(child);
	}
	
	public boolean containsPoint(float x, float y) {
		return x > this.x && x < this.x + width && y > this.y
				&& y < this.y + height;
	}

	protected abstract void loadIcons();
	
	public abstract void init();

	public abstract void draw();
	
	public void initAll() {
		init();
		for (ViewWindow child : children) {
			child.init();
		}
	}
	
	public void drawAll() {
		draw();
		for (ViewWindow child : children) {
			push();
			translate(child.x, child.y);
			child.drawAll();
			pop();
		}
	}

	protected abstract void createChildren();

	protected abstract void layoutChildren();
	
	public void loadAllIcons() {
		loadIcons();
		for (ViewWindow child : children) {
			child.loadAllIcons();
		}
	}
	
	protected void requestRender() {
		parent.requestRender();
	}
	
	public void layout(GL10 gl, float x, float y, float width, float height) {
		this.gl = gl;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		layoutChildren();
	}

	protected ViewWindow findChildAt(float x, float y) {
		for (ViewWindow child : children) {
			if (child.containsPoint(x, y)) {
				return child;
			}
		}
		return null;
	}
	
	protected static final FloatBuffer makeFloatBuffer(float[] arr) {
		return GLSurfaceViewBase.makeFloatBuffer(arr);
	}

	protected static final FloatBuffer makeFloatBuffer(float[] arr, int position) {
		return GLSurfaceViewBase.makeFloatBuffer(arr, position);
	}

	protected static final void translate(float x, float y) {
		GLSurfaceViewBase.translate(x, y);
	}

	protected static final void scale(float x, float y) {
		GLSurfaceViewBase.scale(x, y);
	}

	protected static void push() {
		GLSurfaceViewBase.push();
	}

	protected static final void pop() {
		GLSurfaceViewBase.pop();
	}

	protected static final FloatBuffer makeRectFloatBuffer(float x1, float y1,
			float x2, float y2) {
		return GLSurfaceViewBase.makeRectFloatBuffer(x1, y1, x2, y2);
	}

	protected static final FloatBuffer makeRectOutlineFloatBuffer(float x1,
			float y1, float x2, float y2) {
		return GLSurfaceViewBase.makeRectOutlineFloatBuffer(x1, y1, x2, y2);
	}

	protected static final FloatBuffer makeRoundedCornerRectBuffer(float width,
			float height, float cornerRadius, int resolution) {
		return GLSurfaceViewBase.makeRoundedCornerRectBuffer(width, height,
				cornerRadius, resolution);
	}

	protected static final void drawRectangle(float x1, float y1, float x2,
			float y2, float[] color) {
		GLSurfaceViewBase.drawRectangle(x1, y1, x2, y2, color);
	}

	protected static void drawRectangleOutline(float x1, float y1, float x2,
			float y2, float[] color, float width) {
		GLSurfaceViewBase.drawRectangleOutline(x1, y1, x2, y2, color, width);
	}

	protected static final void drawTriangleStrip(FloatBuffer vb,
			float[] color, int numVertices) {
		GLSurfaceViewBase.drawTriangleStrip(vb, color, numVertices);
	}

	protected static final void drawTriangleStrip(FloatBuffer vb, float[] color) {
		GLSurfaceViewBase.drawTriangleStrip(vb, color);
	}

	protected static final void drawTriangleStrip(FloatBuffer vb,
			float[] color, int beginVertex, int endVertex) {
		GLSurfaceViewBase.drawTriangleStrip(vb, color, beginVertex, endVertex);
	}

	protected static final void drawTriangleFan(FloatBuffer vb, float[] color) {
		GLSurfaceViewBase.drawTriangleFan(vb, color);
	}

	protected static final void drawLines(FloatBuffer vb, float[] color,
			float width, int type, int stride) {
		GLSurfaceViewBase.drawLines(vb, color, width, type, stride);
	}

	protected static final void drawLines(FloatBuffer vb, float[] color,
			float width, int type) {
		GLSurfaceViewBase.drawLines(vb, color, width, type);
	}

	protected static final void drawPoint(float pointSize, float[] color,
			float x, float y) {
		GLSurfaceViewBase.drawPoint(pointSize, color, x, y);
	}

	protected final float distanceFromCenterSquared(float x, float y) {
		return (x - width / 2) * (x - width / 2) + (y - height / 2)
				* (y - height / 2);
	}

	protected final static void setColor(float[] color) {
		GLSurfaceViewBase.setColor(color);
	}
	
	protected final void setBackgroundColor(float[] color) {
		parent.setBackgroundColor(color);
	}

	protected final static void loadTexture(Bitmap bitmap,
			int[] textureHandlers, int textureId) {
		GLSurfaceViewBase.loadTexture(bitmap, textureHandlers, textureId);
	}

	protected final static void loadTexture(int resourceId,
			int[] textureHandlers, int textureId, int[] crop) {
		GLSurfaceViewBase.loadTexture(resourceId, textureHandlers, textureId,
				crop);
	}

	protected final static void drawTexture(int textureId,
			int[] textureHandlers, int[] crop, float x, float y, float width,
			float height) {
		GLSurfaceViewBase.drawTexture(textureId, textureHandlers, crop, x, y,
				width, height);
	}
}
