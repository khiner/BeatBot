package com.kh.beatbot.ui.view;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.transition.ColorTransition;

public abstract class ScrollableView extends TouchableView {
	protected float yOffset = 0; // only yOffset is exposed to descendants 

	private static final float DRAG = .9f, THRESH = 0.01f;
	private float yOffsetAnchor = 0, velocity = 0, lastY = 0;

	private RoundedRect scrollBar;
	private ColorTransition tabColorTransition = new ColorTransition(20, 20, Color.TRANSPARENT,
			new float[] { 0, 0, 0, .7f });

	public ScrollableView(View view) {
		super(view);
	}

	@Override
	public synchronized void layoutChildren() {
		layoutScrollTab();
	}

	@Override
	public synchronized void tick() {
		if (pointerCount() == 0 && Math.abs(velocity) > THRESH) {
			updateYOffset();
		}
		tabColorTransition.tick();
		if (null != scrollBar) {
			scrollBar.setFillColor(tabColorTransition.getColor());
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
		yOffsetAnchor = yOffset;
		lastY = pos.y;
		tabColorTransition.begin();
	}

	@Override
	public void handleActionUp(int index, Pointer pos) {
		if (Math.abs(velocity) < 3) {
			velocity = 0;
		}
		tabColorTransition.end();
	}

	@Override
	public void handleActionMove(int index, Pointer pos) {
		if (getChildHeight() <= height) {
			return;
		}
		velocity = pos.y - lastY;
		lastY = pos.y;

		updateYOffset(pos);
	}

	private synchronized void resetScrollState() {
		yOffset = 0;
		velocity = 0;
	}

	private synchronized void updateYOffset() {
		updateYOffset(null);
	}

	private synchronized void updateYOffset(Pointer pos) {
		float newY;
		if (null == pos) {
			newY = yOffset + velocity;
			velocity *= DRAG;
		} else {
			newY = pos.y - pos.downY + yOffsetAnchor;
		}

		yOffset = GeneralUtils.clipTo(newY, height - getChildHeight(), 0);

		layoutChildren();
	}

	private RoundedRect getScrollTab() {
		if (scrollBar == null) {
			scrollBar = new RoundedRect(renderGroup, Color.TRANSPARENT, null);
			addShapes(scrollBar);
		}
		return scrollBar;
	}

	private void layoutScrollTab() {
		float childHeight = getChildHeight();
		if (childHeight > height) {
			float rad = LABEL_HEIGHT / 5;
			float y = Math.max(1, -yOffset * height / childHeight);
			float h = Math.max(rad * 2, height * height / childHeight);
			getScrollTab().setCornerRadius(rad);
			getScrollTab().layout(absoluteX + width - 2.5f * rad, absoluteY + y, 2 * rad, h);
		}
	}
}
