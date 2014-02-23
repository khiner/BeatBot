// This is a OpenGL ES 1.0 dynamic font rendering system. It loads actual font
// files, generates a font map (texture) from them, and allows rendering of
// text strings.
//
// NOTE: the rendering portions of this class uses a sprite batcher in order
// provide decent speed rendering. Also, rendering assumes a BOTTOM-LEFT
// origin, and the (x,y) positions are relative to that, as well as the
// bottom-left of the string to render.

package com.kh.beatbot.ui.texture;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.ui.view.GLSurfaceViewBase;

public class FontTextureAtlas {
	private final static int CHAR_START = 32, // First Character (ASCII Code)
			CHAR_END = 126, // Last Character (ASCII Code)
			CHAR_CNT = CHAR_END - CHAR_START + 1, DEFAULT_TEXT_SIZE = 26;
	// Width of Each Character (Actual; Pixels)
	private static final float[] charWidths = new float[CHAR_CNT];
	private static final int[] textureId = new int[1];
	private static int cellWidth = 0, cellHeight = 0;

	private static Bitmap bitmap = null;
	// region of each character (texture coordinates)
	private static TextureRegion[] charRegions = new TextureRegion[CHAR_CNT];

	public static void load(final String fileName) {
		load(fileName, DEFAULT_TEXT_SIZE);
	}

	// this will load the specified font file, create a texture for the defined
	// character range, and setup all required values used to render with it.
	// file - Filename of the font (.ttf, .otf) to use. In 'Assets' folder.
	// size - Requested pixel size of font (height)
	public static void load(final String fileName, final int size) {
		// load the font and setup paint instance for drawing
		Typeface tf = Typeface.createFromAsset(
				BeatBotActivity.mainActivity.getAssets(), fileName);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(size);
		paint.setTypeface(tf);
		Paint.FontMetrics fm = paint.getFontMetrics();

		cellHeight = (int) Math.ceil(Math.abs(fm.bottom) + Math.abs(fm.top));
		float yOffset = cellHeight - (float) Math.ceil(Math.abs(fm.descent))
				- 1;

		final int textureSize = calcTextureSize(cellHeight);
		// create an empty bitmap (alpha only)
		bitmap = Bitmap.createBitmap(textureSize, textureSize,
				Bitmap.Config.ALPHA_8);
		bitmap.eraseColor(0x00000000); // Set Transparent Background (ARGB)
		Canvas canvas = new Canvas(bitmap);

		float[] w = new float[1]; // Working Width Value
		// render each of the characters to the canvas (ie. build the font map)
		float x = 0, y = yOffset;
		for (char c = CHAR_START; c <= CHAR_END; c++) {
			paint.getTextWidths(String.valueOf(c), w);
			charWidths[c - CHAR_START] = w[0];
			cellWidth = (int) Math.max(cellWidth, charWidths[c - CHAR_START]);
			canvas.drawText(String.valueOf(c), x, y, paint);
			charRegions[c - CHAR_START] = new TextureRegion(textureSize,
					textureSize, x, y - yOffset, cellWidth - 1, cellHeight - 1);
			x += cellWidth;
			if (x + cellWidth > textureSize) {
				x = 0; // Set X for New Row
				y += cellHeight; // Move Down a Row
			}
		}
	}

	public static void loadTexture() {
		// load bitmap texture in OpenGL
		GLSurfaceViewBase.loadTexture(bitmap, textureId, 0);
	}

	public static int[] getTextureId() {
		return textureId;
	}

	public static TextureRegion getCharRegion(char chr) {
		return charRegions[chr - CHAR_START];
	}

	public static float getCharWidth(char chr) {
		return charWidths[chr - CHAR_START];
	}

	public static float getTextWidth(String text, float height) {
		float total = 0;
		for (char character : text.toCharArray()) {
			total += getCharWidth(character);
		}
		return total * height / cellHeight;
	}

	public static float getCellWidth() {
		return cellWidth;
	}

	public static float getCellHeight() {
		return cellHeight;
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
