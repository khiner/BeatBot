package com.kh.beatbot.ui.view;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResource;
import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.icon.IconResourceSet.State;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.mesh.TextMesh;
import com.kh.beatbot.ui.mesh.TextureMesh;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.shape.Shape;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.texture.TextureAtlas;
import com.kh.beatbot.ui.view.TouchableView.Pointer;
import com.kh.beatbot.ui.view.page.MainPage;
import com.kh.beatbot.ui.view.page.effect.EffectPage;

public class View implements Comparable<View> {
	public static MainPage mainPage;
	public static EffectPage effectPage;
	public static GLSurfaceViewBase root;

	public float absoluteX = 0, absoluteY = 0, x = 0, y = 0, width = 0, height = 0;

	protected static final float � = (float) Math.PI, BG_OFFSET = 3, X_OFFSET = 2;
	protected static float LABEL_HEIGHT = 0;

	protected int id = -1; // optional
	protected boolean shouldDraw = true, shrinkable = false;
	protected float minX = 0, maxX = 0, minY = 0, maxY = 0, borderWidth = 0, borderHeight = 0;

	protected View parent;
	protected List<View> children = new ArrayList<View>();

	protected Shape shape;
	protected TextMesh textMesh;
	protected TextureMesh textureMesh;
	protected RenderGroup renderGroup;

	private boolean shouldClip = false;
	private String text = "";
	private IconResourceSet icon = new IconResourceSet(IconResourceSets.DEFAULT);
	private State state = State.DEFAULT;

	public View() {
		this(null);
	}

	public View(RenderGroup renderGroup) {
		shouldDraw = (null == renderGroup);
		this.renderGroup = shouldDraw ? new RenderGroup() : renderGroup;
		createChildren();
	}

	public static GL11 getGl() {
		return GLSurfaceViewBase.gl;
	}

	public float unscaledHeight() {
		return height;
	}

	public RenderGroup getRenderGroup() {
		return renderGroup;
	}

	public State getState() {
		return state;
	}

	public final void setState(State state) {
		this.state = state;
		stateChanged();
	}

	public void setEnabled(boolean enabled) {
		setState(enabled ? State.DEFAULT : State.DISABLED);
	}

	public final boolean isEnabled() {
		return getState() != State.DISABLED;
	}

	public void setText(String text) {
		this.text = text;
		setState(state);
		if (text.isEmpty()) {
			destroyText();
		}
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

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public int numChildren() {
		return children.size();
	}

	public synchronized void addChild(View child) {
		if (children.contains(child))
			return;

		children.add(child);
		child.stateChanged();
	}

	public synchronized void removeChild(View child) {
		if (!children.contains(child))
			return;
		child.destroy();
		children.remove(child);
	}

	public synchronized void addChildren(View... children) {
		for (View child : children) {
			addChild(child);
		}
	}

	public synchronized void removeChildren(View... children) {
		for (View child : children) {
			removeChild(child);
		}
	}

	public void setShrinkable(boolean shrinkable) {
		this.shrinkable = shrinkable;
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
		layoutShape();
		layoutChildren();
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

	public void layout(View parent, float x, float y, float width, float height) {
		this.parent = parent;
		setDimensions(width, height);
		setPosition(x, y);
	}

	public synchronized void tickAll() {
		tick();
		tickChildren();
	}

	public synchronized void drawAll() {
		if (shouldClip)
			startClip(true, true);

		if (shouldDraw)
			renderGroup.draw();

		draw();
		drawChildren();

		if (shouldClip)
			endClip();
	}

	public void startClip(boolean clipX, boolean clipY) {
		if (null == parent)
			return;
		// scissor ensures that each view can only draw within its rect
		getGl().glEnable(GL10.GL_SCISSOR_TEST);
		int xClip = clipX ? (int) absoluteX : 0;
		int yClip = clipY ? (int) (root.getHeight() - absoluteY - height) : 0;
		int clipW = clipX ? (int) width : Integer.MAX_VALUE;
		int clipH = clipY ? (int) height : Integer.MAX_VALUE;

		getGl().glScissor(xClip, yClip, clipW, clipH);
	}

	public void endClip() {
		getGl().glDisable(GL10.GL_SCISSOR_TEST);
	}

	public void initGl() {
		getGl().glLineWidth(1);
		getGl().glClearColor(Color.BG[0], Color.BG[1], Color.BG[2], Color.BG[3]);
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

	protected void createChildren() {
	}

	protected void layoutChildren() {
	}

	protected void tick() {
	}

	protected synchronized void tickChildren() {
		for (View child : children) {
			child.tickAll();
		}
	}

	protected void draw() {
	}

	protected synchronized void drawChildren() {
		for (View child : children) {
			child.drawAll();
		}
	}

	protected synchronized void layoutShape() {
		float x = absoluteX, y = absoluteY;
		float width = this.width, height = this.height;

		if (width <= 0 || height <= 0)
			return;

		if (null != shape && shrinkable) {
			x += BG_OFFSET;
			y += BG_OFFSET;
			width -= BG_OFFSET * 2;
			height -= BG_OFFSET * 2;
		}

		float bgRectRadius = Math.min(width, height) * .15f;

		if (null != shape && shape instanceof RoundedRect) {
			((RoundedRect) shape).setCornerRadius(bgRectRadius);
		}

		if (shouldShrink()) {
			float shrink = height * 0.1f;
			x += shrink / 2;
			y += shrink / 2;
			width -= shrink;
			height -= shrink;
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

	protected synchronized View findChildAt(float x, float y) {
		// reverse order to respect z-index (children are drawn in position
		// order
		for (int i = children.size() - 1; i >= 0; i--) {
			final View child = children.get(i);
			if (child.containsPoint(x, y)) {
				return child;
			}
		}
		return null;
	}

	protected static final void translate(float x, float y) {
		getGl().glTranslatef(x, y, 0);
	}

	protected static final void scale(float x, float y) {
		getGl().glScalef(x, y, 1);
	}

	protected static final void push() {
		getGl().glPushMatrix();
	}

	protected static final void pop() {
		getGl().glPopMatrix();
	}

	protected final float distanceFromCenterSquared(Pointer pos) {
		return (pos.x - width / 2) * (pos.x - width / 2) + (pos.y - height / 2)
				* (pos.y - height / 2);
	}

	protected float viewX(float x) {
		return x * borderWidth + minX;
	}

	protected float viewY(float y) {
		return (1 - y) * borderHeight + minY;
	}

	protected float unitX(float viewX) {
		return (viewX - minX) / borderWidth;
	}

	protected float unitY(float viewY) {
		// bottom == height in pixels == 0 in value
		// top == 0 in pixels == 1 in value
		return (height - viewY - minY) / borderHeight;
	}

	protected float clipX(float x) {
		return GeneralUtils.clipTo(x, minX, maxX);
	}

	protected float clipY(float y) {
		return GeneralUtils.clipTo(y, minY, maxY);
	}

	protected final IconResource getIconResource() {
		return icon.getResource(state);
	}

	protected synchronized void stateChanged() {
		final IconResource currResource = getIconResource();

		if (null == getFillColor() && null == getStrokeColor()) {
			destroyShape();
		} else if (null != shape) {
			shape.setColors(getFillColor(), getStrokeColor());
		}

		if (null == textMesh && !text.isEmpty()) {
			textMesh = new TextMesh(renderGroup.getTextGroup(), text);
		}
		if (null == textureMesh && null != currResource && currResource.resourceId != -1) {
			textureMesh = new TextureMesh(renderGroup.getTextureGroup());
		}
		if (null != textureMesh) {
			int resourceId = null == currResource ? -1 : currResource.resourceId;
			if (resourceId == -1) {
				destroyTexture();
			} else {
				textureMesh.setResource(resourceId);
				textureMesh.setColor(getIconColor());
			}
		}

		layoutShape();

		if (null != textMesh) {
			textMesh.setColor(getTextColor());
		}
	}

	protected void setShape(Shape shape) {
		this.shape = shape;
	}

	protected void initRect() {
		if (null == shape) {
			shape = new Rectangle(renderGroup, getFillColor(), getStrokeColor());
		}
	}

	protected void initRoundedRect() {
		if (null == shape) {
			shape = new RoundedRect(renderGroup, getFillColor(), getStrokeColor());
		}
	}

	protected synchronized void destroy() {
		destroyTexture();
		destroyShape();
		destroyText();

		for (View child : children) {
			child.destroy();
		}
	}

	protected synchronized void destroyShape() {
		if (null != shape) {
			shape.destroy();
			shape = null;
		}
	}

	protected synchronized void destroyTexture() {
		if (null != textureMesh) {
			textureMesh.destroy();
			textureMesh = null;
		}
	}

	protected synchronized void destroyText() {
		if (null != textMesh) {
			textMesh.destroy();
			textMesh = null;
		}
	}

	private float[] getFillColor() {
		final IconResource currResource = getIconResource();
		return null == currResource ? null : currResource.fillColor;
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

	private final float[] getIconColor() {
		final IconResource currResource = getIconResource();
		return (null == currResource || null == currResource.iconColor) ? getTextColor()
				: currResource.iconColor;
	}

	private final boolean shouldShrink() {
		return shrinkable ? (getState() == State.PRESSED || getState() == State.SELECTED) : false;
	}
}
