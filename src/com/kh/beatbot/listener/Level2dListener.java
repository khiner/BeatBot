package com.kh.beatbot.listener;

import com.kh.beatbot.view.control.ControlViewBase;

public interface Level2dListener {
	
	void onLevelChange(ControlViewBase levelListenable, float levelX, float levelY);
	
}
