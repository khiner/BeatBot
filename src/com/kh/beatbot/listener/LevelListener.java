package com.kh.beatbot.listener;

import com.kh.beatbot.listenable.LevelListenable;

public interface LevelListener {
	void notifyInit(LevelListenable levelListenable);
	void notifyPressed(LevelListenable levelListenable, boolean pressed);
	void notifyClicked(LevelListenable levelListenable);
	void setLevel(LevelListenable levelListenable, float level);
	void setLevel(LevelListenable levelListenable, float levelX, float levelY);
}
