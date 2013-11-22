package com.kh.beatbot.ui.view;

public class ListView extends TouchableView {

	private static float offset = 0;

	public synchronized void init() {
		offset = LABEL_HEIGHT / 3;
	}

	public void add(TouchableView view) {
		addChild(view);
	}

	public void remove(TouchableView view) {
		removeChild(view);
	}

	public synchronized void layoutChildren() {
		float y = offset;
		for (View child : children) {
			float height = ((TouchableView) child).getText().isEmpty() ? width
					- offset * 2 : LABEL_HEIGHT;
			child.layout(this, offset, y, width - offset * 2, height);
			y += height;
		}
	}
}