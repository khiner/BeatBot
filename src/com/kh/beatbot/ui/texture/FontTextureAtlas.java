// This is a OpenGL ES 1.0 dynamic font rendering system. It loads actual font
// files, generates a font map (texture) from them, and allows rendering of
// text strings.
//
// NOTE: the rendering portions of this class uses a sprite batcher in order
// provide decent speed rendering. Also, rendering assumes a BOTTOM-LEFT
// origin, and the (x,y) positions are relative to that, as well as the
// bottom-left of the string to render.

package com.kh.beatbot.ui.texture;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;

public class FontTextureAtlas extends TextureAtlas {
	private final static int CHAR_START = 32, // First Character (ASCII Code)
			CHAR_CNT = 126 - CHAR_START + 1, DEFAULT_TEXT_SIZE = 26;
	// Width of Each Character (Actual; Pixels)
	private final static float[] CHAR_WIDTHS = new float[CHAR_CNT];
	private final float[] W = new float[1]; // Working Width Value
	private Typeface tf = null;

	public void initConfig() {
		paint.setAntiAlias(true);
		paint.setTextSize(DEFAULT_TEXT_SIZE);
		paint.setTypeface(tf);
		Paint.FontMetrics fm = paint.getFontMetrics();

		config = new Config();
		config.numRegions = CHAR_CNT;
		config.regionIdOffset = CHAR_START;
		config.bitmapConfig = Bitmap.Config.ALPHA_8;
		config.cellHeight = (int) Math.ceil(Math.abs(fm.bottom) + Math.abs(fm.top));
		config.textureYOffset = config.cellHeight - (float) Math.ceil(Math.abs(fm.descent)) - 1;
		config.textureSize = calcTextureSize(config.cellHeight);
	}

	// this will load the specified font file, create a texture for the defined
	// character range, and setup all required values used to render with it.
	// file - Filename of the font (.ttf, .otf) to use. In 'Assets' folder.
	public void load(Activity activity, String fontFilePath) {
		// load the font and setup paint instance for drawing
		tf = Typeface.createFromAsset(activity.getAssets(), fontFilePath);
		super.load(activity);
	}

	public static float getCharWidth(char chr) {
		return CHAR_WIDTHS[chr - CHAR_START];
	}

	public float getTextWidth(String text, float height) {
		float total = 0;
		for (char character : text.toCharArray()) {
			total += getCharWidth(character);
		}
		return total * height / config.cellHeight;
	}

	@Override
	protected void drawTextureRegion(int regionId, float x, float y) {
		paint.getTextWidths(String.valueOf((char) regionId), W);
		CHAR_WIDTHS[regionId - CHAR_START] = W[0];
		config.cellWidth = (int) Math.max(config.cellWidth, CHAR_WIDTHS[regionId - CHAR_START]);
		canvas.drawText(String.valueOf((char) regionId), x, y, paint);
	}

	// Texture Size for Font (Square)
	private static int calcTextureSize(final float cellHeight) {
		// NOTE: these values are fixed, based on the defined characters. when
		// changing start/end characters (CHAR_START/CHAR_END) this will need
		// adjustment too!
		if (cellHeight <= 24)
			return 256;
		else if (cellHeight <= 40)
			return 512;
		else if (cellHeight <= 80)
			return 1024;
		else
			return 2048;
	}
}
