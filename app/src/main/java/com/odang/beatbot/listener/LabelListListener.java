package com.odang.beatbot.listener;

public interface LabelListListener {
	void labelClicked(String text, int position);

	void labelLongClicked(int position);
	
	void labelMoved(int oldPosition, int newPosition);
}
