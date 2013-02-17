package com.kh.beatbot.layout.page;

import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.window.TouchableViewWindow;

public abstract class Page extends TouchableViewWindow {
	
	public Page(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public abstract void update();
}
