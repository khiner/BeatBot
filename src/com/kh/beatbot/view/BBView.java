package com.kh.beatbot.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GeneralUtils;

public abstract class BBView {
	public class Position {
		public float x, y;

		Position(float x, float y) {
			set(x, y);
		}

		public void set(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	public static final float ¹ = (float) Math.PI;

	protected List<BBView> children = new ArrayList<BBView>();
	
	protected GLSurfaceViewBase root;
	protected BBView parent;
	public static GL10 gl;
	public float absoluteX = 0, absoluteY = 0;
	public float x = 0, y = 0;
	public float width = 0, height = 0;

	protected float[] backgroundColor = Colors.BG_COLOR;
	protected float[] clearColor = Colors.BG_COLOR;
	
	private int id = -1; // optional
	
	private static FloatBuffer circleVb = null;
	private static final float CIRCLE_RADIUS = 100;

	protected boolean initialized = false;
	
	static { // init circle
		float theta = 0;
		float coords[] = new float[128];
		for (int i = 0; i < 128; i += 2) {
			coords[i] = (float) Math.cos(theta) * CIRCLE_RADIUS;
			coords[i + 1] = (float) Math.sin(theta) * CIRCLE_RADIUS;
			theta += 2 * Math.PI / 64;
		}
		circleVb = makeFloatBuffer(coords);
	}
	
	public BBView() {}
	
	public BBView(GLSurfaceViewBase root) {
		this.root = root;
		createChildren();
	}

	public void addChild(BBView child) {
		children.add(child);
		if (initialized)
			child.initAll();
	}
	
	public boolean containsPoint(float x, float y) {
		return x > this.x && x < this.x + width && y > this.y
				&& y < this.y + height;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public abstract void init();

	public abstract void draw();
	
	protected abstract void createChildren();
	
	public abstract void layoutChildren();
	
	protected abstract void loadIcons();
	
	public void initAll() {
		initBackgroundColor();
		init();
		for (BBView child : children) {
			child.initAll();
		}
		initialized = true;
	}
	
	public void drawAll() {
		// scissor ensures that each view can only draw within its rect
		gl.glEnable(GL10.GL_SCISSOR_TEST);
		gl.glScissor((int)absoluteX, (int)(root.getHeight() - absoluteY - height), (int)width, (int)height);
		draw();
		for (int i = 0; i < children.size(); i++) {
			// not using foreach to avoid concurrent modification
			BBView child = children.get(i);
			push();
			//translate(child.x, child.y);
			child.drawAll();
			pop();
		}
		gl.glDisable(GL10.GL_SCISSOR_TEST);
	}
	
	public void initGl(GL10 _gl) {
		gl = _gl;
		loadAllIcons();
	}
	
	public void loadAllIcons() {
		loadIcons();
		for (BBView child : children) {
			child.loadAllIcons();
		}
	}
	
	public void layout(BBView parent, float x, float y, float width, float height) {
		if (parent != null) {
			this.absoluteX = parent.absoluteX + x;
			this.absoluteY = parent.absoluteY + y;
		}
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		layoutChildren();
	}

	protected BBView findChildAt(float x, float y) {
		for (BBView child : children) {
			if (child.containsPoint(x, y)) {
				return child;
			}
		}
		return null;
	}
	
	public static final FloatBuffer makeFloatBuffer(List<Float> vertices) {
		return makeFloatBuffer(GeneralUtils.floatListToArray(vertices));
	}
	
	public static final FloatBuffer makeFloatBuffer(float[] vertices) {
		return makeFloatBuffer(vertices, 0);
	}

	public static final FloatBuffer makeFloatBuffer(float[] vertices, int position) {
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(vertices);
		fb.position(position);
		return fb;
	}

	public static final FloatBuffer makeRectFloatBuffer(float x1, float y1, float x2,
			float y2) {
		return makeFloatBuffer(new float[] { x1, y1, x1, y2, x2, y2, x2, y1 });
	}

	public static final void translate(float x, float y) {
		gl.glTranslatef(x, y, 0);
	}

	public static final void scale(float x, float y) {
		gl.glScalef(x, y, 1);
	}

	public static final void push() {
		gl.glPushMatrix();
	}

	public static final void pop() {
		gl.glPopMatrix();
	}

	public static final FloatBuffer makeRoundedCornerRectBuffer(float width,
			float height, float cornerRadius, int resolution) {
		float[] roundedRect = new float[resolution * 8];
		float theta = 0, addX, addY;
		for (int i = 0; i < roundedRect.length / 2; i++) {
			theta += 4 * ¹ / roundedRect.length;
			if (theta < ¹ / 2) { // lower right
				addX = width / 2 - cornerRadius;
				addY = height / 2 - cornerRadius;
			} else if (theta < ¹) { // lower left
				addX = -width / 2 + cornerRadius;
				addY = height / 2 - cornerRadius;
			} else if (theta < 3 * ¹ / 2) { // upper left
				addX = -width / 2 + cornerRadius;
				addY = -height / 2 + cornerRadius;
			} else { // upper right
				addX = width / 2 - cornerRadius;
				addY = -height / 2 + cornerRadius;
			}
			roundedRect[i * 2] = (float) Math.cos(theta) * cornerRadius + addX;
			roundedRect[i * 2 + 1] = (float) Math.sin(theta) * cornerRadius + addY;
		}
		return makeFloatBuffer(roundedRect);
	}

	public static final void drawText(String text, float[] color, int height, float x, float y)  {
		setColor(color);
		GLSurfaceViewBase.drawText(text, height, x, y);
	}
	
	public static final void drawRectangle(float x1, float y1, float x2, float y2,
			float[] color) {
		drawTriangleFan(makeRectFloatBuffer(x1, y1, x2, y2), color);
	}

	public static void drawRectangleOutline(float x1, float y1, float x2,
			float y2, float[] color, float width) {
		drawLines(makeRectFloatBuffer(x1, y1, x2, y2), color, width,
				GL10.GL_LINE_LOOP);
	}

	public static final void drawTriangleStrip(FloatBuffer vb, float[] color,
			int numVertices) {
		drawTriangleStrip(vb, color, 0, numVertices);
	}

	public static final void drawTriangleStrip(FloatBuffer vb, float[] color) {
		if (vb == null)
			return;
		drawTriangleStrip(vb, color, vb.capacity() / 2);
	}

	public static final void drawTriangleStrip(FloatBuffer vb, float[] color,
			int beginVertex, int endVertex) {
		if (vb == null)
			return;
		setColor(color);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vb);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, beginVertex, endVertex
				- beginVertex);
	}

	public static final void drawTriangleFan(FloatBuffer vb, float[] color) {
		setColor(color);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vb);
		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, vb.capacity() / 2);
	}

	public static final void drawLines(FloatBuffer vb, float[] color, float width,
			int type, int stride) {
		setColor(color);
		gl.glLineWidth(width);
		gl.glVertexPointer(2, GL10.GL_FLOAT, stride, vb);
		gl.glDrawArrays(type, 0, vb.capacity() / (2 + stride / 8));
	}

	public static final void drawLines(FloatBuffer vb, float[] color, float width,
			int type) {
		drawLines(vb, color, width, type, 0);
	}
	
	public static final void drawPoint(float pointSize, float[] color, float x,
			float y) {
		push();
		translate(x, y);
		float scale = pointSize / CIRCLE_RADIUS;
		scale(scale, scale);
		drawTriangleFan(circleVb, color);
		pop();
	}
	
	protected final float distanceFromCenterSquared(float x, float y) {
		return (x - width / 2) * (x - width / 2) + (y - height / 2)
				* (y - height / 2);
	}

	public final void setBackgroundColor(float[] color) {
		backgroundColor = color;
	}

	public static final void setColor(float[] color) {
		gl.glColor4f(color[0], color[1], color[2], color[3]);
	}
	
	protected void initBackgroundColor() {
		gl.glClearColor(backgroundColor[0], backgroundColor[1],
				backgroundColor[2], backgroundColor[3]);
	}
	
	public class ViewRect {
		public int drawOffset;
		public float parentWidth, parentHeight, minX, maxX, minY, maxY, width,
				height, borderRadius;

		private FloatBuffer borderVb = null;

		// radiusScale determines the size of the radius of the rounded border.
		// radius will be the given percentage of the shortest side of the view
		// rect.
		public ViewRect(float parentWidth, float parentHeight, float radiusScale,
				int drawOffset) {
			this.parentWidth = parentWidth;
			this.parentHeight = parentHeight;
			this.drawOffset = drawOffset;
			borderRadius = Math.min(parentWidth, parentHeight) * radiusScale;
			minX = borderRadius;
			minY = borderRadius;
			maxX = parentWidth - borderRadius;
			maxY = parentHeight - borderRadius;
			width = parentWidth - 2 * minX;
			height = parentHeight - 2 * minY;
			borderVb = makeRoundedCornerRectBuffer(
					parentWidth - drawOffset * 2,
					parentHeight - drawOffset * 2, borderRadius, 25);
		}

		public float viewX(float x) {
			return x * width + minX;
		}

		public float viewY(float y) {
			return (1 - y) * height + minY;
		}

		public float unitX(float viewX) {
			return (viewX - minX) / width;
		}

		public float unitY(float viewY) {
			// bottom == height in pixels == 0 in value
			// top == 0 in pixels == 1 in value
			return (parentHeight - viewY - minY) / height;
		}

		public float clipX(float x) {
			return x < minX ? minX : (x > maxX ? maxX : x);
		}

		public float clipY(float y) {
			return y < minY ? minY : (y > maxY ? maxY : y);
		}

		public void drawRoundedBg() {
			gl.glTranslatef(parentWidth / 2, parentHeight / 2, 0);
			drawTriangleFan(borderVb, Colors.VIEW_BG);
			gl.glTranslatef(-parentWidth / 2, -parentHeight / 2, 0);
		}

		public void drawRoundedBgOutline() {
			gl.glTranslatef(parentWidth / 2, parentHeight / 2, 0);
			drawLines(borderVb, Colors.VOLUME, drawOffset / 2,
					GL10.GL_LINE_LOOP);
			gl.glTranslatef(-parentWidth / 2, -parentHeight / 2, 0);
		}
	}
}
