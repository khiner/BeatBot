package com.kh.beatbot.ui.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.graphics.Typeface;

import com.kh.beatbot.listener.ScrollableViewListener;
import com.kh.beatbot.listener.ViewListener;
import com.kh.beatbot.ui.color.ColorSet;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.Rectangle;
import com.kh.beatbot.ui.mesh.RoundedRect;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.mesh.TextMesh;
import com.kh.beatbot.ui.view.page.MainPage;
import com.kh.beatbot.ui.view.page.effect.EffectPage;

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

	public static MainPage mainPage;
	public static EffectPage effectPage;

	public static final float ¹ = (float) Math.PI;
	public static final float BG_OFFSET = 4;

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

	protected static float LABEL_HEIGHT = 0;

	protected List<View> children = new ArrayList<View>();
	protected View parent;
	protected Shape bgRect = null;

	protected float[] backgroundColor = Colors.BG, clearColor = Colors.BG,
			strokeColor = Colors.WHITE;

	private boolean initialized = false;
	protected boolean shouldClip = true, shouldDraw = true;

	protected float minX = 0, maxX = 0, minY = 0, maxY = 0, borderWidth = 0,
			borderHeight = 0;

	protected ColorSet bgFillColorSet, bgStrokeColorSet;

	protected Set<ViewListener> listeners = new HashSet<ViewListener>();

	protected ShapeGroup shapeGroup;

	protected TextMesh textMesh; // TODO this assumes only one string per view

	public View() {
		this(null);
	}

	public View(ShapeGroup shapeGroup) {
		shouldDraw = (null == shapeGroup);
		this.shapeGroup = shouldDraw ? new ShapeGroup() : shapeGroup;
		createChildren();
	}

	public void addListener(ViewListener listener) {
		listeners.add(listener);
	}

	public ShapeGroup getShapeGroup() {
		return shapeGroup;
	}

	protected void initBgRect(boolean rounded) {
		initBgRect(rounded, shapeGroup, Colors.defaultBgFillColorSet,
				Colors.defaultBgStrokeColorSet);
	}

	protected void initBgRect(boolean rounded, ColorSet fillColorSet) {
		initBgRect(rounded, shapeGroup, fillColorSet,
				Colors.defaultBgStrokeColorSet);
	}

	protected void initBgRect(boolean rounded, ShapeGroup group,
			ColorSet fillColorSet, ColorSet strokeColorSet) {
		this.bgFillColorSet = fillColorSet;
		this.bgStrokeColorSet = strokeColorSet;
		bgRect = rounded ? new RoundedRect(group, fillColorSet == null ? null
				: fillColorSet.defaultColor, strokeColorSet == null ? null
				: strokeColorSet.defaultColor) : new Rectangle(group,
				fillColorSet == null ? null : fillColorSet.defaultColor,
				strokeColorSet == null ? null : strokeColorSet.defaultColor);
	}

	protected float calcBgRectRadius() {
		return Math.max(height / 9, 10);
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public synchronized void addChild(View child) {
		if (children.contains(child))
			return;

		children.add(child);
		if (initialized) {
			child.initAll();
			child.initAllIcons();
		}
	}

	protected synchronized void addChildren(View... children) {
		for (View child : children) {
			addChild(child);
		}
	}

	public synchronized void removeChild(View child) {
		if (!children.contains(child))
			return;
		child.destroy();
		children.remove(child);
	}

	protected synchronized void destroy() {
		if (textMesh != null) {
			textMesh.destroy();
			textMesh = null;
		}
		
		for (View child : children) {
			child.destroy();
		}
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
		} else {
			this.absoluteX = x;
			this.absoluteY = y;
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

	protected abstract void init();

	public abstract void update();

	public abstract void draw();

	protected abstract void createChildren();

	protected abstract void layoutChildren();

	protected abstract void initIcons();

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
		gl.glClearColor(backgroundColor[0], backgroundColor[1],
				backgroundColor[2], backgroundColor[3]);
		init();
		for (View child : children) {
			child.initAll();
		}
		initialized = true;
	}

	public synchronized void drawAll() {
		// scissor ensures that each view can only draw within its rect
		if (shouldClip) {
			gl.glEnable(GL10.GL_SCISSOR_TEST);
			if (null != parent) {
				clipWindow(parent.currClipX, parent.currClipY,
						parent.currClipW, parent.currClipH);
			} else {
				gl.glDisable(GL10.GL_SCISSOR_TEST);
			}
		}

		if (shouldDraw) {
			shapeGroup.draw(this);
		}
		draw();
		drawChildren();
		gl.glDisable(GL10.GL_SCISSOR_TEST);
	}

	protected synchronized void drawChildren() {
		for (View child : children) {
			drawChild(child);
		}
	}

	protected final void drawChild(View child) {
		if (child.parent != null) {
			push();
			translate(child.x, child.y);
			child.drawAll();
			pop();
		} else {
			child.drawAll();
		}
	}

	public void initGl(GL11 _gl) {
		gl = _gl;
		for (ViewListener listener : listeners) {
			listener.onGlReady(this);
		}
		initAllIcons();
		initAll();
		for (ViewListener listener : listeners) {
			listener.onInitialize(this);
		}
	}

	public synchronized void initAllIcons() {
		initIcons();
		for (View child : children) {
			child.initAllIcons();
		}
	}

	private void layoutBgRect() {
		if (bgRect == null)
			return;
		float bgRectRadius = 0;
		if (bgRect instanceof RoundedRect) {
			bgRectRadius = calcBgRectRadius();
			((RoundedRect) bgRect).setCornerRadius(bgRectRadius);
		}
		float x = BG_OFFSET + this.absoluteX, y = BG_OFFSET + this.absoluteY;

		bgRect.layout(x, y, width - BG_OFFSET * 2, height - BG_OFFSET * 2);
		minX = minY = bgRectRadius + BG_OFFSET;
		maxX = width - bgRectRadius - BG_OFFSET;
		maxY = height - bgRectRadius - BG_OFFSET;
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

	public final void setText(String text, float[] color, float x, float y,
			float height) {
		if (textMesh == null) {
			textMesh = new TextMesh(shapeGroup.getTextGroup(), text);
		}

		x += this.absoluteX;
		y += this.absoluteY;
		textMesh.setText(text, x, y, height, color);
	}

	protected final float distanceFromCenterSquared(float x, float y) {
		return (x - width / 2) * (x - width / 2) + (y - height / 2)
				* (y - height / 2);
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
		if (null != textMesh) {
			textMesh.setColor(strokeColor);
		}
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

	public void notifyScrollX() {
		for (ViewListener listener : listeners) {
			if (listener instanceof ScrollableViewListener) {
				((ScrollableViewListener) listener).onScrollX(this);
			}
		}
	}

	public void notifyScrollY() {
		for (ViewListener listener : listeners) {
			if (listener instanceof ScrollableViewListener) {
				((ScrollableViewListener) listener).onScrollY(this);
			}
		}
	}
}
