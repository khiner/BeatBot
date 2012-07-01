package com.kh.beatbot.listener;

import com.kh.beatbot.view.TronSeekbar;

public interface LevelListener {
	void notifyInit(TronSeekbar levelBar);
	void notifyChecked(TronSeekbar levelBar, boolean checked);
	void setLevel(TronSeekbar levelBar, float level);
}
