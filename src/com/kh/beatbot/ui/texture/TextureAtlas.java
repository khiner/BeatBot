package com.kh.beatbot.ui.texture;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLUtils;

import com.kh.beatbot.ui.view.View;

public abstract class TextureAtlas {

	protected class Config {
		protected int numRegions = 0, regionIdOffset = 0, textureSize = 0, cellWidth = 0,
				cellHeight = 0;
		protected float textureYOffset = 0;
		protected Bitmap.Config bitmapConfig = null;
	}

	public static final FontTextureAtlas font = new FontTextureAtlas();
	public static final ResourceTextureAtlas resource = new ResourceTextureAtlas();

	protected final int[] textureId = new int[1];
	protected TextureRegion[] textureRegions;
	protected Bitmap bitmap = null;
	protected Paint paint = new Paint();
	protected Canvas canvas = null;
	protected Config config;

	public void load(Activity activity) {
		initConfig();
		// create an empty bitmap (alpha only)
		bitmap = Bitmap.createBitmap(config.textureSize, config.textureSize, config.bitmapConfig);
		canvas = new Canvas(bitmap);
		initTextureRegions();
	}

	public void loadTexture() {
		GL11 gl = View.getGl();
		// Generate Texture ID
		gl.glGenTextures(1, textureId, 0);
		// Bind texture id texturing target
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId[0]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		// allow non-power-of-2 images to render with hardware acceleration enabled
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
	}

	public int[] getTextureId() {
		return textureId;
	}

	public float getCellWidth() {
		return config.cellWidth;
	}

	public float getCellHeight() {
		return config.cellHeight;
	}

	public TextureRegion getTextureRegion(int regionId) {
		return textureRegions[regionId - config.regionIdOffset];
	}

	protected void setTextureRegion(int regionId, float x, float y) {
		textureRegions[regionId - config.regionIdOffset] = new TextureRegion(config.textureSize,
				config.textureSize, x, y, config.cellWidth - 1, config.cellHeight - 1);
	}

	protected void initTextureRegions() {
		textureRegions = new TextureRegion[config.numRegions];

		float x = 0, y = config.textureYOffset;
		for (int regionId = config.regionIdOffset; regionId < config.regionIdOffset
				+ config.numRegions; regionId++) {
			drawTextureRegion(regionId, x, y);
			setTextureRegion(regionId, x, y - config.textureYOffset);

			x += config.cellWidth;
			if (x + config.cellWidth > config.textureSize) {
				x = 0; // Set X for New Row
				y += config.cellHeight; // Move Down a Row
			}
		}
	}

	protected abstract void initConfig();

	protected abstract void drawTextureRegion(int regionId, float x, float y);
}
