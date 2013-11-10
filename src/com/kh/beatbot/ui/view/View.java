package com.kh.beatbot.ui.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Typeface;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.RoundedRect;
import com.kh.beatbot.ui.mesh.ShapeGroup;

public abstract class View implements Comparable<View> {
	public class Position {
		public float x, y;

		public Position(float x, float y) {
			set(x, y);
		}

		public void set(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	public static final float ¹ = (float) Math.PI, CIRCLE_RADIUS = 100;
	public static GL11 gl;
	public static GLSurfaceViewBase root;
	public static Typeface font;

	// where is the view currently clipped to?
	// used to keep track of SCISSOR clipping of parent views,
	// so child views don't draw outside of any parent (granparent, etc)
	// this should be reset every frame by the parent using resetClipWindow()
	public int currClipX = Integer.MIN_VALUE, currClipY = Integer.MIN_VALUE,
			currClipW = Integer.MAX_VALUE, currClipH = Integer.MAX_VALUE;

	public int id = -1; // optional

	public float absoluteX = 0, absoluteY = 0, x = 0, y = 0, width = 0,
			height = 0;

	protected List<View> children = new ArrayList<View>();
	protected View parent;
	protected RoundedRect bgRect = null;

	protected float[] backgroundColor = Colors.BG_COLOR,
			clearColor = Colors.BG_COLOR, strokeColor = Colors.WHITE;

	protected boolean initialized = false;
	
	protected boolean shouldClip = true;
	
	protected float minX = 0, maxX = 0, minY = 0, maxY = 0, borderWidth = 0,
			borderHeight = 0, borderOffset = 0;

	// just scale and translate this circle to draw any circle for efficiency
	private static FloatBuffer circleVb = null;

	static { // init circle
		float theta = 0;
		float coords[] = new float[128];
		for (int i = 0; i < coords.length; i += 2) {
			coords[i] = (float) Math.cos(theta) * CIRCLE_RADIUS;
			coords[i + 1] = (float) Math.sin(theta) * CIRCLE_RADIUS;
			theta += 4 * Math.PI / coords.length;
		}
		circleVb = makeFloatBuffer(coords);
	}

	public View() {
		createChildren();
	}

	public void initBgRect(ShapeGroup group, float[] fillColor,
			float[] borderColor) {
		bgRect = new RoundedRect(group, fillColor, borderColor);
	}

	public float getBgRectRadius() {
		return bgRect.cornerRadius;
	}

	public synchronized void addChild(View child) {
		if (children.contains(child)) {
			return;
		}

		children.add(child);
		if (initialized) {
			child.initAll();
		}
	}

	public synchronized void removeChild(View child) {
		if (!children.contains(child))
			return;
		child.destroy();
		children.remove(child);
	}

	public synchronized int numChildren() {
		return children.size();
	}

	public void setDimensions(float width, float height) {
		this.width = width;
		this.height = height;
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
		if (parent != null) {
			this.absoluteX = parent.absoluteX + x;
			this.absoluteY = parent.absoluteY + y;
		}
		layoutChildren();
	}

	public void setClip(boolean shouldClip) {
		this.shouldClip = shouldClip;
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

	public abstract void destroy();

	public abstract void draw();

	protected abstract void createChildren();

	public abstract void layoutChildren();

	protected abstract void loadIcons();

	public void clipWindow(int parentClipX, int parentClipY, int parentClipW,
			int parentClipH) {
		currClipX = (int) absoluteX;
		currClipY = (int) (root.getHeight() - absoluteY - height);
		currClipW = (int) width;
		currClipH = (int) height;
		if (currClipX < parentClipX) {
			currClipW -= parentClipX - currClipX;
			currClipX = parentClipX;
		}
		float parentMaxX = parentClipX + parentClipW;
		if (parentMaxX > 1 && currClipX + currClipW > parentMaxX) {
			currClipW = parentClipW + parentClipW - currClipW;
		}
		if (currClipY < parentClipY) {
			currClipH -= parentClipY - currClipY;
			currClipY = parentClipY;
		}
		float parentMaxY = parentClipY + parentClipH;
		if (parentMaxY > 1 && currClipY + currClipH > parentMaxY) {
			currClipH = parentClipY + parentClipH - currClipY;
		}

		gl.glScissor(currClipX, currClipY, currClipW, currClipH);
	}

	public void initAll() {
		initBackgroundColor();
		init();
		for (View child : children) {
			child.initAll();
		}
		initialized = true;
	}

	public void drawAll() {
		if (!initialized)
			return;
		// scissor ensures that each view can only draw within its rect
		if (shouldClip) {
			gl.glEnable(GL10.GL_SCISSOR_TEST);
			if (parent != null) {
				clipWindow(parent.currClipX, parent.currClipY,
						parent.currClipW, parent.currClipH);
			} else {
				clipWindow(Integer.MIN_VALUE, Integer.MIN_VALUE,
						Integer.MAX_VALUE, Integer.MAX_VALUE);
			}
			if (bgRect != null) {
				bgRect.draw();
			}
		}
		draw();
		drawChildren();
		gl.glDisable(GL10.GL_SCISSOR_TEST);
	}

	protected synchronized void drawChildren() {
		for (int i = 0; i < children.size(); i++) {
			// not using foreach to avoid concurrent modification
			drawChild(children.get(i));
		}
	}

	protected final void drawChild(View child) {
		push();
		translate(child.x, child.y);
		child.drawAll();
		pop();
	}

	public void initGl(GL11 _gl) {
		gl = _gl;
		loadAllIcons();
	}

	public synchronized void loadAllIcons() {
		loadIcons();
		for (View child : children) {
			child.loadAllIcons();
		}
	}

	private void layoutBgRect() {
		if (bgRect == null)
			return;
		float borderWeight = 2;
		float borderRadius = Math.max(height / 9, 10);
		borderOffset = borderWeight * 2;
		bgRect.setBorderWeight(borderWeight);
		bgRect.setCornerRadius(borderRadius);
		bgRect.layout(borderOffset, borderOffset, width - borderOffset * 2,
				height - borderOffset * 2);
		minX = minY = bgRect.cornerRadius + borderOffset;
		maxX = width - bgRect.cornerRadius - borderOffset;
		maxY = height - bgRect.cornerRadius - borderOffset;
		borderWidth = width - 2 * minX;
		borderHeight = height - 2 * minY;
	}

	public void layout(View parent, float x, float y, float width, float height) {
		this.parent = parent;
		setDimensions(width, height);
		setPosition(x, y);
		layoutBgRect();
	}

	protected synchronized View findChildAt(float x, float y) {
		// reverse order to respect z-index (children are drawn in position
		// order
		for (int i = children.size() - 1; i >= 0; i--) {
			View child = children.get(i);
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

	public static final FloatBuffer makeFloatBuffer(float[] vertices,
			int position) {
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(vertices);
		fb.position(position);
		return fb;
	}

	public static final FloatBuffer makeRectFloatBuffer(float x1, float y1,
			float x2, float y2) {
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

	public static final void drawText(String text, float[] color, float height,
			float x, float y) {
		setColor(color);
		GLSurfaceViewBase.drawText(text, height, x, y);
	}

	public static final void drawRectangle(float x1, float y1, float x2,
			float y2, float[] color) {
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

	public static final void drawLines(FloatBuffer vb, float[] color,
			float width, int type, int stride) {
		setColor(color);
		gl.glLineWidth(width);
		gl.glVertexPointer(2, GL10.GL_FLOAT, stride, vb);
		gl.glDrawArrays(type, 0, vb.capacity() / (2 + stride / 8));
	}

	public static final void drawLines(FloatBuffer vb, float[] color,
			float width, int type) {
		drawLines(vb, color, width, type, 0);
	}

	public static final void drawCircle(float pointSize, float[] color,
			float x, float y) {
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
		initBackgroundColor();
	}

	public static final void setColor(float[] color) {
		gl.glColor4f(color[0], color[1], color[2], color[3]);
	}

	protected void initBackgroundColor() {
		gl.glClearColor(backgroundColor[0], backgroundColor[1],
				backgroundColor[2], backgroundColor[3]);
	}

	public float viewX(float x) {
		return x * borderWidth + minX;
	}

	public float viewY(float y) {
		return (1 - y) * borderHeight + minY;
	}

	public float unitX(float viewX) {
		return (viewX - minX) / borderWidth;
	}

	public float unitY(float viewY) {
		// bottom == height in pixels == 0 in value
		// top == 0 in pixels == 1 in value
		return (height - viewY - minY) / borderHeight;
	}

	public float clipX(float x) {
		return x < minX ? minX : (x > maxX ? maxX : x);
	}

	public float clipY(float y) {
		return y < minY ? minY : (y > maxY ? maxY : y);
	}

	public void setStrokeColor(float[] strokeColor) {
		this.strokeColor = strokeColor;
	}

	public float[] getStrokeColor() {
		return strokeColor;
	}

	@Override
	public int compareTo(View another) {
		float diff = this.x - another.x;
		if (diff == 0)
			return 0;
		else if (diff > 0)
			return 1;
		else
			return -1;
	}
}
