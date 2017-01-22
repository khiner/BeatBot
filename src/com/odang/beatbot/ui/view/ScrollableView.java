package com.odang.beatbot.ui.view;

import com.odang.beatbot.midi.util.GeneralUtils;
import com.odang.beatbot.ui.color.Color;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.shape.RoundedRect;
import com.odang.beatbot.ui.transition.ColorTransition;

public abstract class ScrollableView extends TouchableView {
	private static final float DRAG = .9f, THRESH = 0.01f;

	// only offsets are exposed to descendants
	protected float xOffset = 0, yOffset = 0;

	private float xOffsetAnchor = 0, yOffsetAnchor = 0, xVelocity = 0, yVelocity = 0, lastX = 0,
			lastY = 0, childWidth = 0, childHeight = 0;

	private boolean horizontal = false, vertical = false, firstRender = true;

	private RoundedRect horizontalScrollBar, verticalScrollBar;
	private ColorTransition scrollBarColorTrans = new ColorTransition(20, 20, Color.TRANSPARENT,
			new float[] { 0, 0, 0, .7f });

	public ScrollableView(View view) {
		super(view);
	}

	public ScrollableView(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	public void setScrollable(boolean xScrollable, boolean yScrollable) {
		this.horizontal = xScrollable;
		this.vertical = yScrollable;
	}

	@Override
	public void layoutChildren() {
		float prevChildWidth = childWidth;
		float prevChildHeight = childHeight;
		childWidth = getChildWidth();
		childHeight = getChildHeight();

		if (horizontal && childWidth != prevChildWidth) {
			setXOffset(childWidth > width ? width - childWidth : 0);

			if (childWidth < width) {
				removeShape(horizontalScrollBar);
				horizontalScrollBar = null;
			}
		}

		if (vertical && childHeight != prevChildHeight) {
			setYOffset(childHeight > height ? height - childHeight : 0);

			if (childHeight < height) {
				removeShape(verticalScrollBar);
				verticalScrollBar = null;
			}
		}

		float rad = getLabelHeight() / 5;
		if (horizontal && childWidth > width) {
			float x = Math.max(1, -xOffset * width / childWidth);
			float w = Math.max(rad * 2, width * width / childWidth);
			getXScrollBar().setCornerRadius(rad);
			getXScrollBar().layout(absoluteX + x, absoluteY + height - 2.5f * rad, w, 2 * rad);
		}

		if (vertical && childHeight > height) {
			float y = Math.max(1, -yOffset * height / childHeight);
			float h = Math.max(rad * 2, height * height / childHeight);
			getYScrollBar().setCornerRadius(rad);
			getYScrollBar().layout(absoluteX + width - 1.5f * rad, absoluteY + y, 2 * rad, h);
		}
	}

	@Override
	public void draw() {
		if (firstRender) {
			// XXX hack, bad initial state on load after at least on shutdown
			setXOffset(0);
			setYOffset(0);
		}
		firstRender = false;
	}

	@Override
	public synchronized void tick() {
		if ((pointerCount() == 0
				&& ((horizontal && Math.abs(xVelocity) >= THRESH) || (vertical && Math
						.abs(yVelocity) >= THRESH)))) {
			updateOffsets(null);
		}

		if (pointerCount() == 0) {
			xVelocity *= DRAG;
			if (Math.abs(xVelocity) < 1) {
				xVelocity = 0;
			}
			yVelocity *= DRAG;
			if (Math.abs(yVelocity) < 1) {
				yVelocity = 0;
			}
			if (xVelocity == 0 && yVelocity == 0) {
				scrollBarColorTrans.end();
			}
		}
		scrollBarColorTrans.tick();
		if (null != horizontalScrollBar) {
			horizontalScrollBar.setFillColor(scrollBarColorTrans.getColor());
		}
		if (null != verticalScrollBar) {
			verticalScrollBar.setFillColor(scrollBarColorTrans.getColor());
		}
	}

	@Override
	public void handleActionDown(int index, Pointer pos) {
		xOffsetAnchor = xOffset;
		yOffsetAnchor = yOffset;
		lastX = pos.x;
		lastY = pos.y;
		scrollBarColorTrans.begin();
	}

	@Override
	public void handleActionMove(int index, Pointer pos) {
		if (childHeight <= height) {
			return;
		}

		xVelocity = pos.x - lastX;
		lastX = pos.x;

		yVelocity = pos.y - lastY;
		lastY = pos.y;

		updateOffsets(pos);
	}

	private void updateOffsets(Pointer pos) {
		if (horizontal && childWidth > width) {
			float newX;
			if (null == pos) {
				newX = xOffset + xVelocity;
			} else {
				newX = pos.x - pos.downX + xOffsetAnchor;
			}
			setXOffset(GeneralUtils.clipTo(newX, width - childWidth, 0));
		}

		if (vertical && childHeight > height) {
			float newY;
			if (null == pos) {
				newY = yOffset + yVelocity;
			} else {
				newY = pos.y - pos.downY + yOffsetAnchor;
			}

			setYOffset(GeneralUtils.clipTo(newY, height - childHeight, 0));
		}
	}

	private RoundedRect getXScrollBar() {
		if (horizontalScrollBar == null) {
			horizontalScrollBar = new RoundedRect(renderGroup, Color.TRANSPARENT, null);
			addShapes(horizontalScrollBar);
		}
		return horizontalScrollBar;
	}

	private RoundedRect getYScrollBar() {
		if (verticalScrollBar == null) {
			verticalScrollBar = new RoundedRect(renderGroup, Color.TRANSPARENT, null);
			addShapes(verticalScrollBar);
		}
		return verticalScrollBar;
	}

	private void setXOffset(float xOffset) {
		if (this.xOffset == xOffset)
			return;
		this.xOffset = xOffset;

		layoutChildren();
	}

	private void setYOffset(float yOffset) {
		if (this.yOffset == yOffset)
			return;
		this.yOffset = yOffset;

		layoutChildren();
	}
}