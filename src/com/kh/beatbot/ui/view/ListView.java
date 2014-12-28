package com.kh.beatbot.ui.view;

import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class ListView extends ScrollableView implements OnPressListener {
	private Button selectedButton;

	public ListView(View view) {
		super(view);
		setScrollable(false, true);
	}

	@Override
	public synchronized void layoutChildren() {
		float y = yOffset;
		for (View child : children) {
			float height = ((TouchableView) child).getText().isEmpty() ? width - 2 * LABEL_HEIGHT
					/ 3 : LABEL_HEIGHT;
			child.layout(this, LABEL_HEIGHT / 3, y, width - 2 * LABEL_HEIGHT / 3, height);
			y += height;
		}
		super.layoutChildren();
	}

	@Override
	public void handleActionUp(int index, Pointer pos) {
		super.handleActionUp(index, pos);
		shouldPropagateTouchEvents = true;
	}

	@Override
	public void handleActionMove(int index, Pointer pos) {
		super.handleActionMove(index, pos);
		if (null != selectedButton
				&& selectedButton.isPressed()
				&& !(selectedButton instanceof ToggleButton && ((ToggleButton) selectedButton)
						.isChecked()) && Math.abs(pos.y - pos.downY) > LABEL_HEIGHT / 2) {
			// scrolling, release the pressed button
			selectedButton.release();
			shouldPropagateTouchEvents = false;
		}
	}

	@Override
	public void onPress(Button button) {
		selectedButton = button;
	}
}