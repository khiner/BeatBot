package com.kh.beatbot.listener;

public interface LabelListListener {
	String getLabelText(int id);
	int labelAdded();
	void labelRemoved(int id);
	void labelMoved(int id, int oldPosition, int newPosition);
	void labelSelected(int id);
}
