package com.kh.beatbot.ui.view.helper;

public interface Scrollable {
	public float width();

	public float height();

	public float unscaledHeight();
	
	public void onScrollX();

	public void onScrollY();
	
	public void onScaleX();
}
