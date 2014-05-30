package com.kh.beatbot.ui.view;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.view.ViewGroup.LayoutParams;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.icon.IconResource;
import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.icon.IconResourceSet.State;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.mesh.TextMesh;
import com.kh.beatbot.ui.mesh.TextureMesh;
import com.kh.beatbot.ui.shape.Rectangle;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.shape.Shape;
import com.kh.beatbot.ui.texture.TextureAtlas;
import com.kh.beatbot.ui.view.TouchableView.Pointer;
import com.kh.beatbot.ui.view.group.GLSurfaceViewGroup;
import com.kh.beatbot.ui.view.page.MainPage;
import com.kh.beatbot.ui.view.page.effect.EffectPage;

public class View implements Comparable<View> {
	protected static final float ¹ = (float) Math.PI, X_OFFSET = 2;
	protected static float LABEL_HEIGHT = 0, BG_OFFSET = 0;

	public static MainPage mainPage;
	public static EffectPage effectPage;
	public static GLSurfaceViewBase root;
	public static BeatBotActivity context;

	public float absoluteX = 0, absoluteY = 0, x = 0, y = 0, width = 0, height = 0;

	protected int id = -1; // optional
	protected boolean shouldDraw = true, shrinkable = false;
	protected float minX = 0, maxX = 0, minY = 0, maxY = 0, borderWidth = 0, borderHeight = 0;

	protected View parent;
	protected List<View> children = new ArrayList<View>();
	protected List<Shape> shapes = new ArrayList<Shape>();

	protected Shape bgShape;
	protected TextureMesh textureMesh;
	protected RenderGroup renderGroup;
	private TextMesh textMesh;

	protected IconResourceSet icon = new IconResourceSet(IconResourceSets.DEFAULT);
	private boolean shouldClip = false;
	private String text = "";

	private State state = State.DEFAULT;

	public View(View parent) {
		this(parent, null == parent ? null : parent.getRenderGroup());
	}

	public View(View parent, RenderGroup renderGroup) {
		shouldDraw = (null == renderGroup);
		this.renderGroup = shouldDraw ? new RenderGroup() : renderGroup;
		textMesh = new TextMesh(this.renderGroup.getTextGroup());
		textureMesh = new TextureMesh(this.renderGroup.getTextureGroup());
		createChildren();
		if (null != parent) {
			parent.addChild(this);
		}
	}

	public static ViewFlipper init(BeatBotActivity context) {
		View.context = context;
		root = new GLSurfaceViewGroup(context);
		mainPage = new MainPage(null);
		effectPage = new EffectPage(null);

		ViewFlipper rootFlipper = new ViewFlipper(null);
		rootFlipper.addPage(mainPage);
		rootFlipper.addPage(effectPage);
		rootFlipper.setPage(mainPage);

		return rootFlipper;
	}

	public static GL11 getGl() {
		return root.getGl();
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
		if (enabled) {
			enable();
		} else {
			disable();
		}
	}

	public void enable() {
		setState(State.DEFAULT);
	}

	public void disable() {
		setState(State.DISABLED);
	}

	public final boolean isEnabled() {
		return getState() != State.DISABLED;
	}

	public void setText(String text) {
		this.text = text;
		textMesh.setText(text);
		stateChanged();
	}

	public String getText() {
		return text;
	}

	public synchronized View withIcon(IconResourceSet resourceSet) {
		setIcon(resourceSet);
		return this;
	}

	public final synchronized void setIcon(IconResourceSet resourceSet) {
		if (null == resourceSet) {
			textureMesh.hide();
			bgShape.hide();
			return;
		}
		icon.set(resourceSet);
		stateChanged();
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
		child.show();
	}

	public synchronized void removeChild(View child) {
		if (!children.contains(child))
			return;
		child.hide();
		children.remove(child);
	}

	public synchronized void removeChildren(View... children) {
		for (View child : children) {
			removeChild(child);
		}
	}

	public synchronized void addShapes(Shape... shapes) {
		for (Shape shape : shapes) {
			if (!this.shapes.contains(shape)) {
				this.shapes.add(shape);
			}
		}
	}

	public synchronized void removeShape(Shape shape) {
		if (null != shape) {
			shape.hide();
			shapes.remove(shape);
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

	public int indexOf(View child) {
		return children.indexOf(child);
	}

	protected void createChildren() {
	}

	public void layoutChildren() {
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

		if (shrinkable && null != bgShape && bgShape.isVisible()) {
			x += BG_OFFSET;
			y += BG_OFFSET;
			width -= BG_OFFSET * 2;
			height -= BG_OFFSET * 2;
		}

		float bgRectRadius = Math.min(width, height) * .15f;

		if (null != bgShape && bgShape instanceof RoundedRect) {
			((RoundedRect) bgShape).setCornerRadius(bgRectRadius);
		}

		if (shouldShrink()) {
			float shrink = height * 0.1f;
			x += shrink / 2;
			y += shrink / 2;
			width -= shrink;
			height -= shrink;
		}

		if (null != bgShape) {
			bgShape.layout(x, y, width, height);
		}

		textureMesh.layout(x, y, height, height);

		if (textMesh.isVisible()) {
			float textHeight = height + BG_OFFSET;
			float textWidth = TextureAtlas.font.getTextWidth(text, textHeight);

			float nonIconWidth = width - X_OFFSET * 2;
			x += X_OFFSET * 2;
			if (textureMesh.isVisible()) {
				nonIconWidth -= height;
				x += height;
			}
			if (textWidth > nonIconWidth) {
				float scaleRatio = nonIconWidth / textWidth;
				textWidth *= scaleRatio;
				textHeight *= scaleRatio;
			}

			textMesh.layout(x + (nonIconWidth - textWidth) / 2, absoluteY
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

	protected IconResource getIconResource() {
		return icon.getResource(state);
	}

	protected synchronized void stateChanged() {
		if (null != bgShape && null == getFillColor() && null == getStrokeColor()) {
			bgShape.hide();
		} else if (null != bgShape) {
			bgShape.show();
			bgShape.setColors(getFillColor(), getStrokeColor());
		}

		textureMesh.setResource(getResourceId());
		textureMesh.setColor(getIconColor());

		textMesh.setColor(getTextColor());

		layoutShape();
	}

	protected void setShape(Shape shape) {
		this.bgShape = shape;
	}

	protected View withRect() {
		initRect();
		return this;
	}

	protected View withRoundedRect() {
		initRoundedRect();
		return this;
	}

	protected void initRect() {
		if (null == bgShape) {
			bgShape = new Rectangle(renderGroup, getFillColor(), getStrokeColor());
		}
	}

	protected void initRoundedRect() {
		if (null == bgShape) {
			bgShape = new RoundedRect(renderGroup, getFillColor(), getStrokeColor());
		}
	}

	protected synchronized void show() {
		if (null != bgShape) {
			bgShape.show();
		}
		textureMesh.show();
		textMesh.show();

		for (Shape shape : shapes) {
			shape.show();
		}
		for (View child : children) {
			child.show();
		}
		stateChanged();
	}

	protected synchronized void hide() {
		if (null != bgShape) {
			bgShape.hide();
		}
		textureMesh.hide();
		textMesh.hide();

		for (Shape shape : shapes) {
			shape.hide();
		}
		for (View child : children) {
			child.hide();
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

	private final int getResourceId() {
		final IconResource currResource = getIconResource();
		return null == currResource ? -1 : currResource.resourceId;
	}

	private final boolean shouldShrink() {
		return shrinkable ? (getState() == State.PRESSED || getState() == State.SELECTED) : false;
	}
}
