package com.kh.beatbot.ui.texture;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.kh.beatbot.ui.view.GLSurfaceViewBase;

public abstract class TextureAtlas {

	protected class Config {
		protected int numRegions = 0, regionIdOffset = 0, textureSize = 0,
				cellWidth = 0, cellHeight = 0;
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
		initBitmap();
		canvas = new Canvas(bitmap);
		initTextureRegions();
	}

	public void loadTexture() {
		// load bitmap texture in OpenGL
		GLSurfaceViewBase.loadTexture(bitmap, textureId, 0);
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
		textureRegions[regionId - config.regionIdOffset] = new TextureRegion(
				config.textureSize, config.textureSize, x, y,
				config.cellWidth - 1, config.cellHeight - 1);
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

	protected void initBitmap() {
		// create an empty bitmap (alpha only)
		bitmap = Bitmap.createBitmap(config.textureSize, config.textureSize,
				config.bitmapConfig);
		bitmap.eraseColor(0x00000000); // Set Transparent Background (ARGB)
	}

	protected abstract void initConfig();

	protected abstract void drawTextureRegion(int regionId, float x, float y);
}
