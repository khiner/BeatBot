package com.kh.beatbot.ui;

import com.kh.beatbot.ui.view.GLSurfaceViewBase;

public class Image extends Drawable {

	public int resourceId;
	protected int[] textureHandlers = new int[1];
	protected int[] crop = new int[4];
	
	public Image(final int resourceId) {
		this.resourceId = resourceId;
		GLSurfaceViewBase.loadTexture(resourceId, textureHandlers, 0, crop);
	}

	public void draw(float x, float y, float width, float height) {
		GLSurfaceViewBase.drawTexture(0, textureHandlers, crop, x, y, width,
				height);
	}
}
