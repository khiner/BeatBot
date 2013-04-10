package com.kh.beatbot.global;

import com.kh.beatbot.view.GLSurfaceViewBase;

public class Image implements Drawable {

	protected int resourceId;
	protected int[] textureHandlers = new int[1];
	protected int[] crop = new int[4];
	
	public Image(int resourceId) {
		this.resourceId = resourceId;
		GLSurfaceViewBase.loadTexture(resourceId, textureHandlers, 0, crop);
	}
	
	public void draw() {
		draw(0, 0);
	}
	
	public void draw(float x, float y) {
		draw(x, y, getWidth(), getHeight());
	}

	public void draw(float x, float y, float width, float height) {
		GLSurfaceViewBase.drawTexture(0, textureHandlers, crop, x, y, width,
				height);
	}

	public float getX() {
		return 0;
	}
	
	public float getY() {
		return 0;
	}
	
	public float getWidth() {
		return crop[2];
	}

	public float getHeight() {
		return crop[1];
	}
}
