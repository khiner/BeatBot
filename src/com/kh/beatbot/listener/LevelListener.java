package com.kh.beatbot.listener;

import com.kh.beatbot.view.LevelViewBase;

public interface LevelListener {
	void notifyInit(LevelViewBase levelListenable);

	void notifyPressed(LevelViewBase levelListenable, boolean pressed);

	void notifyClicked(LevelViewBase levelListenable);

	void setLevel(LevelViewBase levelListenable, float level);

	void setLevel(LevelViewBase levelListenable, float levelX, float levelY);
}
