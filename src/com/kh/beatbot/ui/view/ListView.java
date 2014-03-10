package com.kh.beatbot.ui.view;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.ui.Color;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.transition.ColorTransition;
import com.kh.beatbot.ui.transition.Transition;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class ListView extends TouchableView implements OnPressListener {

	public static final float[] END_TRANS_COLOR = new float[] { 0, 0, 0, .8f };
	public static final float DRAG = .9f, THRESH = 0.01f;
	public static float displayOffset = 0;

	private float yAnchor = 0, yOffset = 0, yOffsetAnchor = 0, childHeight = 0, velocity = 0,
			lastY = 0;
	private RoundedRect scrollBar = null;
	private ColorTransition tabColorTransition = new ColorTransition(20, 20, Color.TRANSPARENT,
			END_TRANS_COLOR);

	private Button selectedButton = null;

	public ListView(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	public synchronized void drawChildren() {
		if (pointerCount() == 0 && Math.abs(velocity) > THRESH) {
			updateYOffset();
			layoutChildren();
		}
		super.drawChildren();

		tabColorTransition.tick();
		if (shouldDrawScrollBar()) {
			scrollBar.setFillColor(tabColorTransition.getColor());
			scrollBar.draw();
		}
	}

	public synchronized void add(TouchableView view) {
		addChild(view);
		resetScrollState();
	}

	public synchronized void remove(TouchableView view) {
		removeChild(view);
		resetScrollState();
	}

	public synchronized void layoutChildren() {
		displayOffset = LABEL_HEIGHT / 3;
		float y = yOffset;
		for (View child : children) {
			float height = ((TouchableView) child).getText().isEmpty() ? width - displayOffset * 2
					: LABEL_HEIGHT;
			child.layout(this, displayOffset, y, width - displayOffset * 2, height);
			y += height;
		}
		childHeight = y - yOffset;
		layoutScrollTab();
	}

	public void handleActionDown(int index, float x, float y) {
		yAnchor = y;
		yOffsetAnchor = yOffset;
		lastY = y;
		tabColorTransition.begin();
	}

	public void handleActionUp(int index, float x, float y) {
		if (Math.abs(velocity) < 3) {
			velocity = 0;
		}
		tabColorTransition.end();
		shouldPropagateTouchEvents = true;
	}

	public void handleActionMove(int index, float x, float y) {
		if (childHeight <= height) {
			return;
		}

		velocity = y - lastY;
		updateYOffset(y);
		layoutChildren();
		if (null != selectedButton
				&& selectedButton.isPressed()
				&& !(selectedButton instanceof ToggleButton && ((ToggleButton) selectedButton)
						.isChecked()) && Math.abs(y - yAnchor) > LABEL_HEIGHT / 2) {
			// scrolling, release the pressed button
			selectedButton.release();
			shouldPropagateTouchEvents = false;
		}
		lastY = y;
	}

	private RoundedRect getScrollTab() {
		if (scrollBar == null) {
			scrollBar = new RoundedRect(shapeGroup, Color.TRANSPARENT, null);
		}
		return scrollBar;
	}

	private void layoutScrollTab() {
		if (childHeight > height) {
			float rad = displayOffset * .6f;
			float y = Math.max(1, -yOffset * height / childHeight);
			float h = Math.max(rad * 2, height * height / childHeight);
			getScrollTab().setCornerRadius(rad);
			getScrollTab().layout(absoluteX + width - 3 * rad, absoluteY + y, 2 * rad, h);
		}
	}

	@Override
	public void onPress(Button button) {
		if (button.equals(selectedButton)) {
			return;
		}
		selectedButton = button;
	}

	private synchronized void updateYOffset() {
		updateYOffset(-1);
	}

	private synchronized void updateYOffset(float pointerY) {
		float newY;
		if (pointerY == -1) {
			newY = yOffset + velocity;
			velocity *= DRAG;
		} else {
			newY = pointerY - yAnchor + yOffsetAnchor;
		}

		yOffset = GeneralUtils.clipTo(newY, height - childHeight, 0);
	}

	private synchronized void resetScrollState() {
		yOffset = 0;
		velocity = 0;
	}

	private boolean shouldDrawScrollBar() {
		return scrollBar != null && childHeight > height
				&& tabColorTransition.getState() != Transition.State.OFF;
	}

	@Override
	protected synchronized void destroy() {
		super.destroy();
		if (null != scrollBar) {
			scrollBar.destroy();
			scrollBar = null;
		}
	}
}