package com.kh.beatbot.listener;

import com.kh.beatbot.listenable.LabelListListenable;

public interface LabelListListener {
	int labelAdded();
	void labelListInitialized(LabelListListenable labelList);
	void labelMoved(int id, int oldPosition, int newPosition);
	void labelRemoved(int id);
	void labelSelected(int id);
}
