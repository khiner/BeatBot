package com.kh.beatbot.global;

import com.kh.beatbot.view.GLSurfaceViewBase;

public class Image extends Drawable {

	protected int resourceId;
	protected int[] textureHandlers = new int[1];
	protected int[] crop = new int[4];
	
	public Image(int resourceId) {
		this.resourceId = resourceId;
		GLSurfaceViewBase.loadTexture(resourceId, textureHandlers, 0, crop);
	}

	public void draw(float x, float y, float width, float height) {
		GLSurfaceViewBase.drawTexture(0, textureHandlers, crop, x, y, width,
				height);
	}
}
