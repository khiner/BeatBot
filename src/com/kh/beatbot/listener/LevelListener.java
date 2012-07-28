package com.kh.beatbot.listener;

import com.kh.beatbot.listenable.LevelListenable;

public interface LevelListener {
	void notifyInit(LevelListenable levelListenable);
	void notifyChecked(LevelListenable levelListenable, boolean checked);
	void setLevel(LevelListenable levelListenable, float level);
	void setLevel(LevelListenable levelListenable, float levelX, float levelY);
}
