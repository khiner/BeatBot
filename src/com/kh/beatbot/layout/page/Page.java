package com.kh.beatbot.layout.page;

import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public abstract class Page extends TouchableBBView {
	
	public Page(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public abstract void update();
}
