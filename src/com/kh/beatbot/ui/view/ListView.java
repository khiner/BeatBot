package com.kh.beatbot.ui.view;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.RoundedRect;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.transition.ColorTransition;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class ListView extends TouchableView implements OnPressListener {

	public static final float[] END_TRANS_COLOR = new float[] { 0, 0, 0, .8f };
	public static final float DRAG = .9f, THRESH = 0.01f;

	private float yOffset = 0, yOffsetAnchor = 0, childHeight = 0, velocity = 0, lastY = 0;
	private RoundedRect scrollBar = null;
	private ColorTransition tabColorTransition = new ColorTransition(20, 20, Color.TRANSPARENT,
			END_TRANS_COLOR);

	private Button selectedButton = null;

	public ListView(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	@Override
	public synchronized void tick() {
		if (pointerCount() == 0 && Math.abs(velocity) > THRESH) {
			updateYOffset();
			layoutChildren();
		}

		tabColorTransition.tick();
		if (null != scrollBar) {
			scrollBar.setFillColor(tabColorTransition.getColor());
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
		float y = yOffset;
		for (View child : children) {
			float height = ((TouchableView) child).getText().isEmpty() ? width - 2 * LABEL_HEIGHT
					/ 3 : LABEL_HEIGHT;
			child.layout(this, LABEL_HEIGHT / 3, y, width - 2 * LABEL_HEIGHT / 3, height);
			y += height;
		}
		childHeight = y - yOffset;
		layoutScrollTab();
	}

	@Override
	public void handleActionDown(int index, Position pos) {
		yOffsetAnchor = yOffset;
		lastY = pos.y;
		tabColorTransition.begin();
	}

	@Override
	public void handleActionUp(int index, Position pos) {
		if (Math.abs(velocity) < 3) {
			velocity = 0;
		}
		tabColorTransition.end();
		shouldPropagateTouchEvents = true;
	}

	@Override
	public void handleActionMove(int index, Position pos) {
		if (childHeight <= height) {
			return;
		}

		velocity = pos.y - lastY;
		updateYOffset(pos);
		layoutChildren();
		if (null != selectedButton
				&& selectedButton.isPressed()
				&& !(selectedButton instanceof ToggleButton && ((ToggleButton) selectedButton)
						.isChecked()) && Math.abs(y - pos.downY) > LABEL_HEIGHT / 2) {
			// scrolling, release the pressed button
			selectedButton.release();
			shouldPropagateTouchEvents = false;
		}
		lastY = pos.y;
	}

	private RoundedRect getScrollTab() {
		if (scrollBar == null) {
			scrollBar = new RoundedRect(shapeGroup, Color.TRANSPARENT, null);
		}
		return scrollBar;
	}

	private void layoutScrollTab() {
		if (childHeight > height) {
			float rad = LABEL_HEIGHT / 5;
			float y = Math.max(1, -yOffset * height / childHeight);
			float h = Math.max(rad * 2, height * height / childHeight);
			getScrollTab().setCornerRadius(rad);
			getScrollTab().layout(absoluteX + width - 2.5f * rad, absoluteY + y, 2 * rad, h);
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
		updateYOffset(null);
	}

	private synchronized void updateYOffset(Position pos) {
		float newY;
		if (null == pos) {
			newY = yOffset + velocity;
			velocity *= DRAG;
		} else {
			newY = pos.y - pos.downY + yOffsetAnchor;
		}

		yOffset = GeneralUtils.clipTo(newY, height - childHeight, 0);
	}

	private synchronized void resetScrollState() {
		yOffset = 0;
		velocity = 0;
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