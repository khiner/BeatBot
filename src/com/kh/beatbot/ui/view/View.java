package com.kh.beatbot.ui.view;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.midi.util.GeneralUtils;
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
import com.kh.beatbot.ui.view.TouchableView.Pointer;

public class View implements Comparable<View> {
	public static BeatBotActivity context; // XXX

	protected static final float π = (float) Math.PI, X_OFFSET = 2;
	protected static float LABEL_HEIGHT = 0, BG_OFFSET = 0;

	public float absoluteX = 0, absoluteY = 0, x = 0, y = 0, width = 0, height = 0;

	protected int id = -1; // optional
	protected boolean shouldDraw = true, shrinkable = false;
	protected float minX = 0, maxX = 0, minY = 0, maxY = 0, borderWidth = 0, borderHeight = 0,
			cornerRadius = 0;

	protected View parent;
	protected List<View> children = new ArrayList<View>();
	protected List<Shape> shapes = new ArrayList<Shape>();

	protected Shape bgShape;
	protected TextureMesh textureMesh;
	protected RenderGroup renderGroup;
	private TextMesh textMesh;

	protected IconResourceSet icon = new IconResourceSet(IconResourceSets.DEFAULT);
	protected String text = "";

	private boolean shouldClip = false;
	private State state = State.DEFAULT;

	public View(View parent) {
		this(parent, parent == null ? null : parent.getRenderGroup());
	}

	public View(View parent, RenderGroup renderGroup) {
		shouldDraw = (renderGroup == null);
		this.renderGroup = shouldDraw ? new RenderGroup() : renderGroup;
		textMesh = new TextMesh(this.renderGroup.getTextGroup());
		textureMesh = new TextureMesh(this.renderGroup.getTextureGroup());
		createChildren();
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public float unscaledHeight() {
		return height;
	}

	public float getTotalHeight() {
		return context.getRoot().getHeight();
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

	public void press() {
		if (isEnabled()) {
			setState(State.PRESSED);
		}
	}

	public void release() {
		if (isEnabled()) {
			setState(State.DEFAULT);
		}
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

	public View withIcon(IconResourceSet resourceSet) {
		setIcon(resourceSet);
		return this;
	}

	public final synchronized void setIcon(IconResourceSet resourceSet) {
		if (resourceSet == null) {
			textureMesh.hide();
			bgShape.hide();
			return;
		}
		icon.set(resourceSet);
		stateChanged();
	}

	public IconResourceSet getIcon() {
		return icon;
	}

	public final void setResourceId(IconResourceSet resourceSet) {
		icon.setResourceId(resourceSet);
		stateChanged();
	}

	public final synchronized void setColors(IconResourceSet resourceSet) {
		icon.setFillColors(resourceSet);
		icon.setStrokeColors(resourceSet);
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
		if (shape != null) {
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

		if (parent != null) {
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

	protected float getChildWidth() {
		float totalChildWidth = 0;
		for (View child : children) {
			totalChildWidth += child.width;
		}
		return totalChildWidth;
	}

	protected float getChildHeight() {
		float totalChildHeight = 0;
		for (View child : children) {
			totalChildHeight += child.height;
		}
		return totalChildHeight;
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
		if (parent == null)
			return;
		// scissor ensures that each view can only draw within its rect
		context.getGl().glEnable(GL10.GL_SCISSOR_TEST);
		int xClip = clipX ? (int) absoluteX : 0;
		int yClip = clipY ? (int) (getTotalHeight() - absoluteY - height) : 0;
		int clipW = clipX ? (int) width : Integer.MAX_VALUE;
		int clipH = clipY ? (int) height : Integer.MAX_VALUE;

		context.getGl().glScissor(xClip, yClip, clipW, clipH);
	}

	public void endClip() {
		context.getGl().glDisable(GL10.GL_SCISSOR_TEST);
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

	public View getChild(int index) {
		return children.get(index);
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

	public View withCornerRadius(float cornerRadius) {
		this.cornerRadius = cornerRadius;
		return this;
	}

	public float getCornerRadius() {
		return cornerRadius;
	}

	protected synchronized void layoutShape() {
		float x = absoluteX, y = absoluteY;
		float width = this.width, height = this.height;

		if (width <= 0 || height <= 0)
			return;

		if (shrinkable && bgShape != null && bgShape.isVisible()) {
			x += BG_OFFSET;
			y += BG_OFFSET;
			width -= BG_OFFSET * 2;
			height -= BG_OFFSET * 2;
		}

		if (bgShape != null && bgShape instanceof RoundedRect) {
			if (getCornerRadius() == 0)
				withCornerRadius(Math.min(width, height) * .15f);
			((RoundedRect) bgShape).setCornerRadius(getCornerRadius());
		}

		if (shouldShrink()) {
			float shrink = height * 0.1f;
			x += shrink / 2;
			y += shrink / 2;
			width -= shrink;
			height -= shrink;
		}

		if (bgShape != null) {
			bgShape.layout(x, y, width, height);
		}

		textureMesh.layout(x, y, height, height);

		if (textMesh.isVisible()) {
			float textHeight = height + BG_OFFSET;
			if (context.getFontTextureAtlas().hasDescent(text))
				textHeight *= .9f;
			float textWidth = context.getFontTextureAtlas().getTextWidth(text, textHeight);
			float nonIconWidth = width - X_OFFSET * 2;
			if (textureMesh.isVisible()) {
				nonIconWidth -= height;
				x += height;
			}
			if (textWidth > nonIconWidth) {
				float scale = nonIconWidth / textWidth;
				textWidth *= scale;
				textHeight *= scale;
			}

			float maxTextHeight = context.getMainPage().height / 10;
			if (textHeight > maxTextHeight) { // text should only be so big, I mean c'mon
				float scale = maxTextHeight / textHeight;
				textWidth *= scale;
				textHeight *= scale;
			}

			textMesh.layout(2 * X_OFFSET + x + (nonIconWidth - textWidth) / 2, absoluteY
					+ (this.height - textHeight) / 2, textHeight);
		}
		minX = minY = getCornerRadius() - BG_OFFSET;
		maxX = this.width - getCornerRadius() - BG_OFFSET;
		maxY = this.height - getCornerRadius() - BG_OFFSET;
		borderWidth = this.width - 2 * minX;
		borderHeight = this.height - 2 * minY;
	}

	protected synchronized View findChildAt(float x, float y) {
		// reverse order to respect z-index (children are drawn in position order
		for (int i = children.size() - 1; i >= 0; i--) {
			final View child = children.get(i);
			if (child.containsPoint(x, y)) {
				return child;
			}
		}
		return null;
	}

	protected static final void translate(float x, float y) {
		context.getGl().glTranslatef(x, y, 0);
	}

	protected static final void scale(float x, float y) {
		context.getGl().glScalef(x, y, 1);
	}

	protected static final void push() {
		context.getGl().glPushMatrix();
	}

	protected static final void pop() {
		context.getGl().glPopMatrix();
	}

	protected final float distanceFromCenterSquared(Pointer pos) {
		return distanceFromCenterSquared(pos.x, pos.y);
	}

	protected final float distanceFromCenterSquared(float x, float y) {
		return (x - width / 2) * (x - width / 2) + (y - height / 2) * (y - height / 2);
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
		if (bgShape != null) {
			bgShape.setColors(getFillColor(), getStrokeColor());
		}

		textureMesh.setResource(getResourceId());
		textureMesh.setColor(getIconColor());

		textMesh.setColor(getTextColor());

		if (bgShape != null && (textureMesh.isVisible() || textMesh.isVisible()))
			bgShape.show();
		layoutShape();
	}

	protected void setShape(Shape shape) {
		this.bgShape = shape;
	}

	public View withRect() {
		initRect();
		return this;
	}

	public View withRoundedRect() {
		initRoundedRect();
		return this;
	}

	protected void initRect() {
		if (bgShape == null) {
			bgShape = new Rectangle(renderGroup, getFillColor(), getStrokeColor());
		}
	}

	protected void initRoundedRect() {
		if (bgShape == null) {
			bgShape = new RoundedRect(renderGroup, getFillColor(), getStrokeColor());
		}
	}

	public synchronized void show() {
		if (bgShape != null) {
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

	public synchronized void hide() {
		if (bgShape != null) {
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
		return currResource == null ? null : currResource.fillColor;
	}

	private final float[] getStrokeColor() {
		final IconResource currResource = getIconResource();
		return currResource == null ? null : currResource.strokeColor;
	}

	private final float[] getTextColor() {
		final IconResource currResource = getIconResource();
		if (currResource == null
				|| (currResource.textColor == null && currResource.strokeColor == null)) {
			return Color.BLACK;
		}
		return currResource.textColor == null ? currResource.strokeColor : currResource.textColor;
	}

	private final float[] getIconColor() {
		final IconResource currResource = getIconResource();
		return (currResource == null || currResource.iconColor == null) ? getTextColor()
				: currResource.iconColor;
	}

	private final int getResourceId() {
		final IconResource currResource = getIconResource();
		return currResource == null ? -1 : currResource.resourceId;
	}

	private final boolean shouldShrink() {
		return shrinkable ? (getState() == State.PRESSED) : false;
	}
}
