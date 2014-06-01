package com.kh.beatbot.ui.view;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.transition.ColorTransition;

public abstract class ScrollableView extends TouchableView {
	// only offsets are exposed to descendants
	protected float xOffset = 0, yOffset = 0;

	private static final float DRAG = .9f, THRESH = 0.01f;
	private float xOffsetAnchor = 0, yOffsetAnchor = 0, xVelocity = 0, yVelocity = 0, lastX = 0,
			lastY = 0;

	private boolean horizontal = false, vertical = false;

	private RoundedRect horizontalScrollBar, verticalScrollBar;
	private ColorTransition tabColorTransition = new ColorTransition(20, 20, Color.TRANSPARENT,
			new float[] { 0, 0, 0, .7f });

	public ScrollableView(View view) {
		super(view);
	}

	public void setScrollable(boolean xScrollable, boolean yScrollable) {
		this.horizontal = xScrollable;
		this.vertical = yScrollable;
	}

	@Override
	public synchronized void layoutChildren() {
		layoutScrollBars();
	}

	@Override
	public synchronized void tick() {
		if (pointerCount() == 0
				&& ((horizontal && Math.abs(xVelocity) > THRESH) || (vertical && Math
						.abs(yVelocity) > THRESH))) {
			updateOffsets(null);
		}

		tabColorTransition.tick();
		if (null != horizontalScrollBar) {
			horizontalScrollBar.setFillColor(tabColorTransition.getColor());
		}
		if (null != verticalScrollBar) {
			verticalScrollBar.setFillColor(tabColorTransition.getColor());
		}
	}

	@Override
	public synchronized void addChild(View view) {
		super.addChild(view);
		resetScrollState();
	}

	@Override
	public synchronized void removeChild(View view) {
		super.removeChild(view);
		resetScrollState();
	}

	@Override
	public void handleActionDown(int index, Pointer pos) {
		xOffsetAnchor = xOffset;
		yOffsetAnchor = yOffset;
		lastX = pos.x;
		lastY = pos.y;
		tabColorTransition.begin();
	}

	@Override
	public void handleActionUp(int index, Pointer pos) {
		if (Math.abs(xVelocity) < 3) {
			xVelocity = 0;
		}
		if (Math.abs(yVelocity) < 3) {
			yVelocity = 0;
		}
		tabColorTransition.end();
	}

	@Override
	public void handleActionMove(int index, Pointer pos) {
		if (getChildHeight() <= height) {
			return;
		}

		xVelocity = pos.x - lastX;
		lastX = pos.x;

		yVelocity = pos.y - lastY;
		lastY = pos.y;

		updateOffsets(pos);
	}

	private synchronized void resetScrollState() {
		xOffset = yOffset = xVelocity = yVelocity = 0;
	}

	private synchronized void updateOffsets(Pointer pos) {
		if (horizontal) {
			float newX;
			if (null == pos) {
				newX = xOffset + xVelocity;
				xVelocity *= DRAG;
			} else {
				newX = pos.x - pos.downX + xOffsetAnchor;
			}

			xOffset = GeneralUtils.clipTo(newX, width - getChildWidth(), 0);
		}

		if (vertical) {
			float newY;
			if (null == pos) {
				newY = yOffset + yVelocity;
				yVelocity *= DRAG;
			} else {
				newY = pos.y - pos.downY + yOffsetAnchor;
			}

			yOffset = GeneralUtils.clipTo(newY, height - getChildHeight(), 0);
		}
		layoutChildren();
	}

	private void layoutScrollBars() {
		if (horizontal) {
			float childWidth = getChildWidth();
			if (childWidth > width) {
				float rad = LABEL_HEIGHT / 5;
				float x = Math.max(1, -xOffset * width / childWidth);
				float w = Math.max(rad * 2, width * width / childWidth);
				getXScrollBar().setCornerRadius(rad);
				getXScrollBar().layout(absoluteX + x, absoluteY + height - 2.5f * rad, w, 2 * rad);
			}
		}

		if (vertical) {
			float childHeight = getChildHeight();
			if (childHeight > height) {
				float rad = LABEL_HEIGHT / 5;
				float y = Math.max(1, -yOffset * height / childHeight);
				float h = Math.max(rad * 2, height * height / childHeight);
				getYScrollBar().setCornerRadius(rad);
				getYScrollBar().layout(absoluteX + width - 2.5f * rad, absoluteY + y, 2 * rad, h);
			}
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
}
