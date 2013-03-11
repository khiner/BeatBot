package com.kh.beatbot.listener;

import com.kh.beatbot.view.control.ControlViewBase;

public interface LevelListener {
	void notifyInit(ControlViewBase levelListenable);

	void notifyPressed(ControlViewBase levelListenable, boolean pressed);

	void notifyClicked(ControlViewBase levelListenable);

	void setLevel(ControlViewBase levelListenable, float level);

	void setLevel(ControlViewBase levelListenable, float levelX, float levelY);
}
