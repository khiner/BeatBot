package com.kh.beatbot.global;

import com.kh.beatbot.view.SurfaceViewBase;

public class BeatBotIcon {
	protected int[] textureHandlers = new int[1];
	protected int[] crop = new int[4];
	
	public BeatBotIcon(int resourceId) {
		SurfaceViewBase.loadTexture(resourceId, 0, textureHandlers, crop);
	}
	
	public void draw(float x, float y) {
		draw(x, y, getWidth(), getHeight());
	}
	
	public void draw(float x, float y, float width, float height) {
		SurfaceViewBase.drawTexture(0, textureHandlers, crop, x, y, width, height);
	}
	
	public float getWidth() {
		return crop[2];
	}
	
	public float getHeight() {
		return crop[1];
	}
}
