package com.kh.beatbot.listener;

import com.kh.beatbot.ui.view.TouchableView;

public interface TouchableViewListener {
	void onPress(TouchableView view);

	void onRelease(TouchableView view);
}
