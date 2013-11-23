package com.kh.beatbot.ui.view;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.RoundedRect;
import com.kh.beatbot.ui.mesh.Shape;
import com.kh.beatbot.ui.mesh.Shape.Type;
import com.kh.beatbot.ui.transition.ColorTransition;
import com.kh.beatbot.ui.transition.Transition;
import com.kh.beatbot.ui.view.control.Button;

public class ListView extends TouchableView implements OnPressListener {

	public static final float[] END_TRANS_COLOR = new float[] { 0, 0, 0, .8f };

	public static float displayOffset = 0;

	private float yAnchor = 0, yOffset = 0, childHeight = 0;
	private RoundedRect scrollTab = null;
	private ColorTransition tabColorTransition = new ColorTransition(20, 20,
			Colors.TRANSPARANT, END_TRANS_COLOR);

	private Button pressedButton = null;

	public void draw() {
		tabColorTransition.tick();
		if (scrollTab != null
				&& tabColorTransition.getState() != Transition.State.OFF) {
			scrollTab.setFillColor(tabColorTransition.getColor());
			scrollTab.draw();
		}
	}

	public void add(TouchableView view) {
		addChild(view);
		if (view instanceof Button) {
			((Button) view).setOnPressListener(this);
		}
	}

	public void remove(TouchableView view) {
		removeChild(view);
	}

	public synchronized void layoutChildren() {
		float y = yOffset;
		for (View child : children) {
			float height = ((TouchableView) child).getText().isEmpty() ? width
					- displayOffset * 2 : LABEL_HEIGHT;
			child.layout(this, displayOffset, y, width - displayOffset * 2,
					height);
			y += height;
		}
		childHeight = y - yOffset;
		layoutScrollTab();
	}

	public void handleActionDown(int index, float x, float y) {
		yAnchor = y - yOffset;
		tabColorTransition.begin();
	}

	public void handleActionUp(int index, float x, float y) {
		tabColorTransition.end();
		shouldPropogateTouchEvents = true;
	}

	public void handleActionMove(int index, float x, float y) {
		if (childHeight <= height) {
			return;
		}
		yOffset = GeneralUtils.clipTo(y - yAnchor, height - childHeight, 0);
		layoutChildren();
		if (Math.abs(y - yAnchor) > LABEL_HEIGHT / 2 && pressedButton != null) {
			pressedButton.release();
			pressedButton = null;
			shouldPropogateTouchEvents = false;
		}
	}

	private RoundedRect getScrollTab() {
		if (scrollTab == null) {
			scrollTab = (RoundedRect) Shape.get(Type.ROUNDED_RECT, null,
					Colors.TRANSPARANT, null);
		}
		return scrollTab;
	}

	private void layoutScrollTab() {
		if (childHeight > height) {
			float y = Math.max(1, -yOffset * height / childHeight);
			float rad = displayOffset * .6f;
			getScrollTab().setCornerRadius(rad);
			getScrollTab().layout(width - 3 * rad, y, 2 * rad,
					height * height / childHeight);
		}
	}

	@Override
	public void onPress(Button button) {
		pressedButton = button;
	}
}