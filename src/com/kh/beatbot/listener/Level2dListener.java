package com.kh.beatbot.listener;

import com.kh.beatbot.view.XYView;

public interface Level2dListener {
	void notifyInit(XYView level2d);
	void notifyChecked(XYView level2d, boolean checked);	
	void setLevel(XYView level2d, float levelX, float levelY);
}
