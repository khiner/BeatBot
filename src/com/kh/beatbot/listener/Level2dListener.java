package com.kh.beatbot.listener;

import com.kh.beatbot.view.TronSeekbar2d;

public interface Level2dListener {
	void notifyInit(TronSeekbar2d level2d);
	void notifyChecked(TronSeekbar2d level2d, boolean checked);	
	void setLevel(TronSeekbar2d level2d, float levelX, float levelY);
}
