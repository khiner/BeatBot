package com.kh.beatbot.listener;

import com.kh.beatbot.ui.view.View;

public interface ViewListener {
	void onGlReady(View view);
	void onInitialize(View view);
}
