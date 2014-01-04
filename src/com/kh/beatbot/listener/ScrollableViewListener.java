package com.kh.beatbot.listener;

import com.kh.beatbot.ui.view.View;

public interface ScrollableViewListener extends ViewListener {
	public void onScrollX(View view);
	public void onScrollY(View view);
}