// This is a OpenGL ES 1.0 dynamic font rendering system. It loads actual font
// files, generates a font map (texture) from them, and allows rendering of
// text strings.
//
// NOTE: the rendering portions of this class uses a sprite batcher in order
// provide decent speed rendering. Also, rendering assumes a BOTTOM-LEFT
// origin, and the (x,y) positions are relative to that, as well as the
// bottom-left of the string to render.

package com.kh.beatbot.ui.mesh;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.ui.view.GLSurfaceViewBase;

public class GLText {

	public final static int CHAR_START = 32, // First Character (ASCII Code)
			CHAR_END = 126, // Last Character (ASCII Code)
			CHAR_CNT = CHAR_END - CHAR_START + 2,
			CHAR_NONE = 32,
			CHAR_UNKNOWN = (CHAR_CNT - 1), CHAR_BATCH_SIZE = 100;

	private static Bitmap bitmap = null;

	// region of each character (texture coordinates)
	private static TextureRegion[] charRegions = new TextureRegion[CHAR_CNT];

	private static int[] textureIds; // Font Texture ID
	private static int textureSize = 0, // Texture Size for Font (Square)
			cellWidth = 0, cellHeight = 0;

	// Width of Each Character (Actual; Pixels)
	private static final float[] charWidths = new float[CHAR_CNT];

	private static GLText singletonInstance = null;

	public static GLText getInstance(String file, int size) {
		if (singletonInstance == null) {
			singletonInstance = new GLText(file, size);
		}
		return singletonInstance;
	}

	private GLText(String file, int size) {
		load(file, size);
	}

	// this will load the specified font file, create a texture for the defined
	// character range, and setup all required values used to render with it.
	// file - Filename of the font (.ttf, .otf) to use. In 'Assets' folder.
	// size - Requested pixel size of font (height)
	public void load(String file, int size) {
		// load the font and setup paint instance for drawing
		Typeface tf = Typeface.createFromAsset(
				BeatBotActivity.mainActivity.getAssets(), file);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(size);
		paint.setTypeface(tf);
		Paint.FontMetrics fm = paint.getFontMetrics();
		cellHeight = (int) (Math.ceil(Math.abs(fm.bottom) + Math.abs(fm.top)));

		float[] w = new float[1]; // Working Width Value

		for (int i = 0; i <= CHAR_END - CHAR_START; i++) {
			paint.getTextWidths(String.valueOf((char) (i + CHAR_START)), w);
			charWidths[i] = w[0];
			cellWidth = (int) Math.max((int) cellWidth, charWidths[i]);
		}

		// NOTE: these values are fixed, based on the defined characters. when
		// changing start/end characters (CHAR_START/CHAR_END) this will need
		// adjustment too!
		if (cellHeight <= 24)
			textureSize = 256;
		else if (cellHeight <= 40)
			textureSize = 512;
		else if (cellHeight <= 80)
			textureSize = 1024;
		else
			textureSize = 2048;

		// create an empty bitmap (alpha only)
		bitmap = Bitmap.createBitmap(textureSize, textureSize,
				Bitmap.Config.ALPHA_8);
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(0x00000000); // Set Transparent Background (ARGB)
		// render each of the characters to the canvas (ie. build the font map)
		float x = 0;
		float y = cellHeight - (float) Math.ceil(Math.abs(fm.descent)) - 1;
		for (char c = CHAR_START; c <= CHAR_END; c++) {
			canvas.drawText(String.valueOf(c), x, y, paint);
			x += cellWidth;
			if (x + cellWidth > textureSize) {
				x = 0; // Set X for New Row
				y += cellHeight; // Move Down a Row
			}
		}

		x = y = 0;
		// setup the array of character texture regions
		for (int c = 0; c < CHAR_CNT; c++) {
			charRegions[c] = new TextureRegion(textureSize, textureSize, x, y,
					cellWidth - 1, cellHeight - 1);
			x += cellWidth;
			if (x + cellWidth > textureSize) {
				x = 0; // Reset X Position to Start
				y += cellHeight; // Move to Next Row (Cell)
			}
		}
		// generate a new texture
		textureIds = new int[1];
	}

	public static void loadTexture() {
		// load bitmap texture in OpenGL
		GLSurfaceViewBase.loadTexture(bitmap, textureIds, 0);
	}

	public static int getTextureId() {
		return textureIds[0];
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
}
