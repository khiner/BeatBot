package com.kh.beatbot.ui.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.listener.ScrollableViewListener;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResource;
import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.icon.IconResourceSet.State;
import com.kh.beatbot.ui.mesh.TextMesh;
import com.kh.beatbot.ui.mesh.TextureMesh;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.shape.Shape;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.texture.TextureAtlas;
import com.kh.beatbot.ui.view.page.MainPage;
import com.kh.beatbot.ui.view.page.effect.EffectPage;

public class View implements Comparable<View> {
	public static final float ¹ = (float) Math.PI, BG_OFFSET = 3;

	public static MainPage mainPage;
	public static EffectPage effectPage;
	public static GLSurfaceViewBase root;

	// where is the view currently clipped to?
	// used to keep track of SCISSOR clipping of parent views,
	// so child views don't draw outside of any parent (granparent, etc)
	// this should be reset every frame by the parent using resetClipWindow()
	public int currClipX = Integer.MIN_VALUE, currClipY = Integer.MIN_VALUE,
			currClipW = Integer.MAX_VALUE, currClipH = Integer.MAX_VALUE;

	public int id = -1; // optional

	protected static float X_OFFSET = 2, LABEL_HEIGHT = 0;

	public float absoluteX = 0, absoluteY = 0, x = 0, y = 0, width = 0, height = 0;
	protected float minX = 0, maxX = 0, minY = 0, maxY = 0, borderWidth = 0, borderHeight = 0;

	protected View parent;
	protected List<View> children = new ArrayList<View>();

	private boolean initialized = false;
	protected boolean shouldClip = true, shouldDraw = true;
	private String text = "";
	protected Set<ScrollableViewListener> listeners = new HashSet<ScrollableViewListener>();

	protected Shape shape;
	protected TextMesh textMesh;
	protected TextureMesh textureMesh;
	protected ShapeGroup shapeGroup;

	private IconResourceSet icon = new IconResourceSet(IconResourceSets.DEFAULT);
	private State state = State.DEFAULT;

	public View() {
		this(null);
	}

	public View(ShapeGroup shapeGroup) {
		shouldDraw = (null == shapeGroup);
		this.shapeGroup = shouldDraw ? new ShapeGroup() : shapeGroup;
		createChildren();
	}

	public static GL11 getGl() {
		return GLSurfaceViewBase.gl;
	}

	public void addScrollListener(ScrollableViewListener listener) {
		listeners.add(listener);
	}

	public ShapeGroup getShapeGroup() {
		return shapeGroup;
	}

	protected void setShape(Shape shape) {
		this.shape = shape;
	}

	public final void setState(State state) {
		this.state = state;
		stateChanged();
	}

	public State getState() {
		return state;
	}

	public void initRoundedRect() {
		if (null == shape) {
			shape = new RoundedRect(shapeGroup, icon.getResource(state).fillColor,
					icon.getResource(state).strokeColor);
		}
	}

	protected void initRect() {
		shape = new Rectangle(shapeGroup, icon.getResource(state).fillColor,
				icon.getResource(state).strokeColor);
	}

	public void setText(String text) {
		this.text = text;
		setState(state);
	}

	public String getText() {
		return text;
	}

	public final synchronized void setIcon(IconResourceSet resourceSet) {
		if (null == resourceSet) {
			destroyTexture();
			destroyShape();
			return;
		}
		icon = new IconResourceSet(resourceSet);

		setState(state);
	}

	public final void setResourceId(IconResourceSet resourceSet) {
		icon.setResourceId(resourceSet);
		stateChanged();
	}

	public final void setFillColors(IconResourceSet resourceSet) {
		icon.setFillColors(resourceSet);
		stateChanged();
	}

	protected float calcBgRectRadius() {
		return Math.min(width, height) * .15f;
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
			child.stateChanged();
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
		destroyTexture();
		destroyShape();
		destroyText();

		for (View child : children) {
			child.destroy();
		}
	}

	public synchronized void destroyShape() {
		if (null != shape) {
			shape.destroy();
			shape = null;
		}
	}

	private synchronized void destroyTexture() {
		if (null != textureMesh) {
			textureMesh.destroy();
			textureMesh = null;
		}
	}

	public synchronized void destroyText() {
		if (null != textMesh) {
			textMesh.destroy();
			textMesh = null;
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
		this.x = absoluteX = x;
		this.y = absoluteY = y;

		if (null != parent) {
			absoluteX += parent.absoluteX;
			absoluteY += parent.absoluteY;
		}
		layoutChildren();
		layoutShape();
	}

	public void setClip(boolean shouldClip) {
		this.shouldClip = shouldClip;
	}

	public boolean containsPoint(float x, float y) {
		return x > this.x && x < this.x + width && y > this.y && y < this.y + height;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	protected void init() {
	}

	public void update() {
	}

	public void draw() {
	}

	protected void createChildren() {
	}

	protected void layoutChildren() {
	}

	public void clipWindow(int parentClipX, int parentClipY, int parentClipW, int parentClipH) {
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

		getGl().glScissor(currClipX, currClipY, currClipW, currClipH);
	}

	public void initAll() {
		getGl().glClearColor(Color.BG[0], Color.BG[1], Color.BG[2], Color.BG[3]);
		init();
		for (View child : children) {
			child.initAll();
		}
		initialized = true;
	}

	public synchronized void drawAll() {
		// scissor ensures that each view can only draw within its rect
		if (shouldClip) {
			getGl().glEnable(GL10.GL_SCISSOR_TEST);
			if (null != parent) {
				clipWindow(parent.currClipX, parent.currClipY, parent.currClipW, parent.currClipH);
			} else {
				getGl().glDisable(GL10.GL_SCISSOR_TEST);
			}
		}

		if (shouldDraw) {
			shapeGroup.draw();
		}
		draw();
		drawChildren();
		getGl().glDisable(GL10.GL_SCISSOR_TEST);
	}

	protected synchronized void drawChildren() {
		for (View child : children) {
			child.drawAll();
		}
	}

	public void initGl() {
		getGl().glLineWidth(1);
		initAll();
	}

	protected synchronized void layoutShape() {
		float bgRectRadius = calcBgRectRadius();
		float x = absoluteX, y = absoluteY;
		float width = this.width, height = this.height;

		if (width <= 0 || height <= 0)
			return;

		if (null != shape) {
			x += BG_OFFSET;
			y += BG_OFFSET;
			width -= BG_OFFSET * 2;
			height -= BG_OFFSET * 2;
			if (shape instanceof RoundedRect) {
				((RoundedRect) shape).setCornerRadius(bgRectRadius);
			}
		}

		if (shouldShrink()) {
			x += width * .04f;
			y += height * .04f;
			width *= .92f;
			height *= .92f;
		}

		if (null != shape) {
			shape.layout(x, y, width, height);
		}
		if (null != textureMesh) {
			textureMesh.layout(x, y, height, height);
		}
		if (null != textMesh) {
			float textHeight = height + BG_OFFSET;
			float textWidth = TextureAtlas.font.getTextWidth(text, textHeight);

			float nonIconWidth = width - X_OFFSET * 2;
			x += X_OFFSET * 2;
			if (null != textureMesh) {
				nonIconWidth -= height;
				x += height;
			}
			if (textWidth > nonIconWidth) {
				float scaleRatio = nonIconWidth / textWidth;
				textWidth *= scaleRatio;
				textHeight *= scaleRatio;
			}

			textMesh.setText(text, x + (nonIconWidth - textWidth) / 2, absoluteY
					+ (this.height - textHeight) / 2, textHeight);
		}
		minX = minY = bgRectRadius + BG_OFFSET;
		maxX = this.width - bgRectRadius - BG_OFFSET;
		maxY = this.height - bgRectRadius - BG_OFFSET;
		borderWidth = this.width - 2 * minX;
		borderHeight = this.height - 2 * minY;
	}

	protected boolean shouldShrink() {
		return false;
	}

	public void layout(View parent, float x, float y, float width, float height) {
		this.parent = parent;
		setDimensions(width, height);
		setPosition(x, y);
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
		getGl().glTranslatef(x, y, 0);
	}

	public static final void scale(float x, float y) {
		getGl().glScalef(x, y, 1);
	}

	public static final void push() {
		getGl().glPushMatrix();
	}

	public static final void pop() {
		getGl().glPopMatrix();
	}

	protected final float distanceFromCenterSquared(float x, float y) {
		return (x - width / 2) * (x - width / 2) + (y - height / 2) * (y - height / 2);
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
		return GeneralUtils.clipTo(x, minX, maxX);
	}

	public float clipY(float y) {
		return GeneralUtils.clipTo(y, minY, maxY);
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
		for (ScrollableViewListener listener : listeners) {
			if (listener instanceof ScrollableViewListener) {
				((ScrollableViewListener) listener).onScrollX(this);
			}
		}
	}

	public void notifyScrollY() {
		for (ScrollableViewListener listener : listeners) {
			if (listener instanceof ScrollableViewListener) {
				((ScrollableViewListener) listener).onScrollY(this);
			}
		}
	}

	protected final IconResource getIconResource() {
		return icon.getResource(state);
	}

	protected synchronized void stateChanged() {
		IconResource currResource = getIconResource();
		float[] strokeColor = getStrokeColor();

		if (null == currResource || null == currResource.fillColor) {
			destroyShape();
		} else if (null != shape) {
			shape.setColors(currResource.fillColor, strokeColor);
		}

		if (null == textureMesh && null != currResource && currResource.resourceId != -1) {
			textureMesh = new TextureMesh(shapeGroup.getTextureGroup());
		}
		if (null == textMesh && !text.isEmpty()) {
			textMesh = new TextMesh(shapeGroup.getTextGroup(), text);
		}
		if (null != textureMesh) {
			int resourceId = null == currResource ? -1 : currResource.resourceId;
			if (-1 == resourceId) {
				destroyTexture();
			} else {
				textureMesh.setResource(resourceId);
				textureMesh.setColor(getTextColor());
			}
		}
		if (null != textMesh) {
			textMesh.setColor(getTextColor());
		}

		layoutShape();
	}

	private final float[] getStrokeColor() {
		final IconResource currResource = getIconResource();
		return null == currResource ? null : currResource.strokeColor;
	}

	private final float[] getTextColor() {
		final IconResource currResource = getIconResource();
		if (null == currResource
				|| (null == currResource.textColor && null == currResource.strokeColor)) {
			return Color.BLACK;
		}
		return null == currResource.textColor ? currResource.strokeColor : currResource.textColor;
	}
}
