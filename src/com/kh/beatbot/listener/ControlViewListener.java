package com.kh.beatbot.listener;

import com.kh.beatbot.ui.view.control.ControlViewBase;

public interface ControlViewListener {
	void onPress(ControlViewBase control);

	void onRelease(ControlViewBase control);
}
