package com.kh.beatbot.listener;

import com.kh.beatbot.view.LabelList;

public interface LabelListListener {
	void labelListInitialized(LabelList labelList);

	void labelClicked(String text, int position);

	void labelLongClicked(int position);
}
