// This is a OpenGL ES 1.0 dynamic font rendering system. It loads actual font
// files, generates a font map (texture) from them, and allows rendering of
// text strings.
//
// NOTE: the rendering portions of this class uses a sprite batcher in order
// provide decent speed rendering. Also, rendering assumes a BOTTOM-LEFT
// origin, and the (x,y) positions are relative to that, as well as the
// bottom-left of the string to render.

package com.kh.beatbot.ui.view.text;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.FloatMath;

import com.kh.beatbot.GlobalVars;
import com.kh.beatbot.ui.view.GLSurfaceViewBase;
import com.kh.beatbot.ui.view.View;

public class GLText {

	public final static int CHAR_START = 32; // First Character (ASCII Code)
	public final static int CHAR_END = 126; // Last Character (ASCII Code)
	public final static int CHAR_CNT = CHAR_END - CHAR_START + 2;
	// Character to Use for Unknown (ASCII Code)
	public final static int CHAR_NONE = 32;
	public final static int CHAR_UNKNOWN = (CHAR_CNT - 1);
	public final static int CHAR_BATCH_SIZE = 100;

	private static Bitmap bitmap = null;

	private static Map<String, SpriteBatch> batches;
	private static SpriteBatch genericBatch;
	// region of each character (texture coordinates)
	private static TextureRegion[] charRgn = new TextureRegion[CHAR_CNT];

	private static int size = 0;

	private static int[] textureIds; // Font Texture ID
	private static int textureSize = 0; // Texture Size for Font (Square)
	private static int cellWidth = 0, cellHeight = 0; // Character Cell
														// Width/Height
	private static float charWidthMax = 0; // Character Width (Maximum; Pixels)
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
		batches = new HashMap<String, SpriteBatch>();
		genericBatch = new SpriteBatch();
		load(file, size);
	}

	// this will load the specified font file, create a texture for the defined
	// character range, and setup all required values used to render with it.
	// file - Filename of the font (.ttf, .otf) to use. In 'Assets' folder.
	// size - Requested pixel size of font (height)
	public void load(String file, int size) {
		GLText.size = size;
		// load the font and setup paint instance for drawing
		Typeface tf = Typeface.createFromAsset(
				GlobalVars.mainActivity.getAssets(), file);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(size);
		paint.setTypeface(tf);
		Paint.FontMetrics fm = paint.getFontMetrics();
		cellHeight = (int) (FloatMath.ceil(Math.abs(fm.bottom)
				+ Math.abs(fm.top)));
		charWidthMax = 0;

		char[] s = new char[2]; // Create Character Array
		float[] w = new float[2]; // Working Width Value
		int cnt = 0; // Array Counter
		for (char c = CHAR_START; c <= CHAR_END; c++) {
			s[0] = c; // Set Character
			paint.getTextWidths(s, 0, 1, w); // Get Character Bounds
			charWidths[cnt] = w[0]; // Get Width
			if (charWidths[cnt] > charWidthMax)
				charWidthMax = charWidths[cnt];
			cnt++;
		}
		s[0] = CHAR_NONE;
		paint.getTextWidths(s, 0, 1, w);
		charWidths[cnt] = w[0]; // Get Width
		if (charWidths[cnt] > charWidthMax)
			charWidthMax = charWidths[cnt]; // Save New Max Width
		cnt++;

		// find the maximum size, validate, and setup cell sizes
		cellWidth = (int) charWidthMax;
		int maxSize = Math.max(cellWidth, cellHeight);
		// set texture size based on max font size (width or height)
		// NOTE: these values are fixed, based on the defined characters. when
		// changing start/end characters (CHAR_START/CHAR_END) this will need
		// adjustment too!
		if (maxSize <= 24)
			textureSize = 256;
		else if (maxSize <= 40)
			textureSize = 512;
		else if (maxSize <= 80)
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
		float y = cellHeight - FloatMath.ceil(Math.abs(fm.descent)) - 1;
		for (char c = CHAR_START; c <= CHAR_END; c++) {
			s[0] = c;
			canvas.drawText(s, 0, 1, x, y, paint);
			x += cellWidth;
			if (x + cellWidth > textureSize) {
				x = 0; // Set X for New Row
				y += cellHeight; // Move Down a Row
			}
		}
		s[0] = CHAR_NONE;
		canvas.drawText(s, 0, 1, x, y, paint);

		x = 0;
		y = 0;
		// setup the array of character texture regions
		for (int c = 0; c < CHAR_CNT; c++) {
			charRgn[c] = new TextureRegion(textureSize, textureSize, x, y,
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

	public void loadTexture() {
		// load bitmap texture in OpenGL
		GLSurfaceViewBase.loadTexture(bitmap, textureIds, 0);
	}

	// D: draw text at the specified x,y position
	// A: text - the string to draw
	// x, y - the x,y position to draw text at (bottom left of text; including
	// descent)
	private void initTextInBatch(String text, SpriteBatch batch) {
		float x = cellWidth / 2;
		float y = cellHeight / 2;
		batch.beginBatch();
		for (char character : text.toCharArray()) {
			int c = (int) character - CHAR_START;
			batch.initSprite(x, y, cellWidth, cellHeight, charRgn[c]);
			x += charWidths[c];
		}
		batch.complete();
	}

	public void storeText(String text) {
		if (batches.containsKey(text))
			return;
		SpriteBatch batch = new SpriteBatch();
		initTextInBatch(text, batch);
		batches.put(text, batch);
	}
	
	// D: draw text at the specified x,y position
	// A: text - the string to draw
	// x, y - the x,y position to draw text at (bottom left of text; including
	// descent)
	public void draw(String text, int height, float x, float y) {
		View.push();
		View.translate(x, y);
		float scale = (float) height / size;
		View.scale(scale, scale);
		if (batches.containsKey(text)) {
			batches.get(text).endBatch(textureIds[0]);
		} else {
			initTextInBatch(text, genericBatch);
			genericBatch.endBatch(textureIds[0]);
		}
		View.pop();
	}

	// D: return the width/height of a character, or max character width
	// A: chr - the character to get width for
	// R: the requested character size (scaled)
	public float getCharWidth(char chr) {
		return charWidths[chr - CHAR_START];
	}

	public float getCharWidthMax() {
		return charWidthMax;
	}

	public float getTextWidth(String text, float height) {
		float total = 0;
		for (char character : text.toCharArray()) {
			total += getCharWidth(character);
		}
		return total * height / size;
	}
}
