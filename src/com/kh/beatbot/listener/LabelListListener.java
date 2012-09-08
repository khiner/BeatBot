package com.kh.beatbot.listener;

import com.kh.beatbot.listenable.LabelListListenable;

public interface LabelListListener {
	void labelListInitialized(LabelListListenable labelList);
	void labelMoved(int id, int oldPosition, int newPosition);
	void labelClicked(int id, String text);
	void labelLongClicked(int id);
}
