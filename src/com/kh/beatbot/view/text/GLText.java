// This is a OpenGL ES 1.0 dynamic font rendering system. It loads actual font
// files, generates a font map (texture) from them, and allows rendering of
// text strings.
//
// NOTE: the rendering portions of this class uses a sprite batcher in order
// provide decent speed rendering. Also, rendering assumes a BOTTOM-LEFT
// origin, and the (x,y) positions are relative to that, as well as the
// bottom-left of the string to render.

package com.kh.beatbot.view.text;

import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.FloatMath;

import com.kh.beatbot.view.SurfaceViewBase;

public class GLText {

	public final static int CHAR_START = 32; // First Character (ASCII Code)
	public final static int CHAR_END = 126; // Last Character (ASCII Code)
	public final static int CHAR_CNT = CHAR_END - CHAR_START + 2;
	// Character to Use for Unknown (ASCII Code)
	public final static int CHAR_NONE = 32;
	public final static int CHAR_UNKNOWN = (CHAR_CNT - 1);
	public final static int CHAR_BATCH_SIZE = 100;

	private AssetManager assets; // Asset Manager
	private SpriteBatch batch; // Batch Renderer
	// region of each character (texture coordinates)
	private TextureRegion[] charRgn = new TextureRegion[CHAR_CNT];

	private int[] textureIds; // Font Texture ID
	private int textureSize = 0; // Texture Size for Font (Square)
	private int cellWidth = 0, cellHeight = 0; // Character Cell Width/Height
	private float charWidthMax = 0; // Character Width (Maximum; Pixels)
	// Width of Each Character (Actual; Pixels)
	private final float[] charWidths = new float[CHAR_CNT];

	public GLText(GL10 gl, AssetManager assets) {
		this.assets = assets; // Save the Asset Manager Instance
		batch = new SpriteBatch(gl);
	}

	// this will load the specified font file, create a texture for the defined
	// character range, and setup all required values used to render with it.
	// file - Filename of the font (.ttf, .otf) to use. In 'Assets' folder.
	// size - Requested pixel size of font (height)
	public void load(String file, int size) {
		// load the font and setup paint instance for drawing
		Typeface tf = Typeface.createFromAsset(assets, file);
		Paint paint = new Paint(); // Create Android Paint Instance
		paint.setAntiAlias(true); // Enable Anti Alias
		paint.setTextSize(size); // Set Text Size
		paint.setTypeface(tf); // Set Typeface
		Paint.FontMetrics fm = paint.getFontMetrics(); // Get Font Metrics
		cellHeight = (int) (FloatMath.ceil(Math.abs(fm.bottom)
				+ Math.abs(fm.top)));
		charWidthMax = 0;

		char[] s = new char[2]; // Create Character Array
		float[] w = new float[2]; // Working Width Value
		int cnt = 0; // Array Counter
		for (char c = CHAR_START; c <= CHAR_END; c++) { // FOR Each Character
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
		Bitmap bitmap = Bitmap.createBitmap(textureSize, textureSize,
				Bitmap.Config.ALPHA_8); // Create Bitmap
		Canvas canvas = new Canvas(bitmap); // Create Canvas for Rendering to
											// Bitmap
		bitmap.eraseColor(0x00000000); // Set Transparent Background (ARGB)
		// render each of the characters to the canvas (ie. build the font map)
		float x = 0;
		float y = cellHeight - FloatMath.ceil(Math.abs(fm.descent)) - 1;
		for (char c = CHAR_START; c <= CHAR_END; c++) { // FOR Each Character
			s[0] = c; // Set Character to Draw
			canvas.drawText(s, 0, 1, x, y, paint); // Draw Character
			x += cellWidth; // Move to Next Character
			if (x + cellWidth > textureSize) {
				x = 0; // Set X for New Row
				y += cellHeight; // Move Down a Row
			}
		}
		s[0] = CHAR_NONE; // Set Character to Use for NONE
		canvas.drawText(s, 0, 1, x, y, paint); // Draw Character

		// generate a new texture
		textureIds = new int[1]; // Array to Get Texture Id
		// load bitmap texture in OpenGL
		SurfaceViewBase.loadTexture(bitmap, textureIds, 0);

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
	}

	public void init(String text, float x, float y) {

	}

	// D: draw text at the specified x,y position
	// A: text - the string to draw
	// x, y - the x,y position to draw text at (bottom left of text; including
	// descent)
	public void draw(String text, float x, float y) {
		x += cellWidth / 2;
		y += cellHeight / 2;
		batch.beginBatch(textureIds[0]);
		for (char character : text.toCharArray()) {
			int c = (int) character - CHAR_START;
			if (c < 0 || c >= CHAR_CNT) // IF Character Not In Font
				c = CHAR_UNKNOWN; // Set to Unknown Character Index
			batch.drawSprite(x, y, cellWidth, cellHeight, charRgn[c]);
			x += charWidths[c];
		}
		batch.endBatch();
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

	public float getTextWidth(String text) {
		float total = 0;
		for (char character : text.toCharArray()) {
			total += getCharWidth(character);
		}
		return total;
	}
}
