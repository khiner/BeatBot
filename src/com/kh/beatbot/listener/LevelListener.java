package com.kh.beatbot.listener;

import com.kh.beatbot.view.TronSeekbar;

public interface LevelListener {
	void setLevel(TronSeekbar levelBar, float level);
	void setLevelChecked(TronSeekbar levelBar, boolean checked);
}
