package com.kh.beatbot.view;

import java.io.InputStream;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.kh.beatbot.R;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;

public class TronKnob extends LevelListenable {
	public static final float ¹ = (float)Math.PI;
	private static FloatBuffer circleVb = null;
	private static FloatBuffer selectCircleVb = null;
	private static FloatBuffer selectCircleVb2 = null;
	private static int circleWidth = 0, circleHeight = 0;
	
	private int[] textureHandlers = new int[2];
	private int[] crop = null;
	private int currentTexture = 0;
	private int drawIndex = 0;
	private long timeDown = 0;
	private boolean clickable = false;
	
	public TronKnob(Context c, AttributeSet as) {
		super(c, as);
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}
	
	public void loadTexture(final int resourceId, int textureId) {
		// Generate Texture ID
		gl.glGenTextures(1, textureHandlers, textureId);
		// Bind texture id texturing target
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandlers[textureId]); 
		 
		InputStream is = getContext().getResources().openRawResource(resourceId);
		Bitmap bitmap = BitmapFactory.decodeStream(is);
		 
		// Build our crop texture to be the size of the bitmap (ie full texture)
		// we only need to do this once, since both images will have the same width/height
		if (crop == null) {
			crop = new int[] {0, bitmap.getHeight(), bitmap.getWidth(), -bitmap.getHeight()};
		}

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
		
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
	}
	
	private void drawTexture() {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandlers[currentTexture]);
		((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, crop, 0);
		gl.glColor4f(1, 1, 1, 1);
		((GL11Ext)gl).glDrawTexfOES(0, 0, 0, width, height);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
	
	private static void initCircleVbs(float width, float height) {
		float[] circleVertices = new float[128];
		float[] selectCircleVertices = new float[128];
		float[] selectCircle2Vertices = new float[128];
		float theta = 3 * ¹ / 4; // start at 1/8 around the circle
		for (int i = 0; i < circleVertices.length / 4; i++) {
			// theta will range from ¹/4 to 7¹/8,
			// with the ¹/8 gap at the "bottom" of the view
			theta += 6 * ¹ /circleVertices.length; 
			// main circles will show when user is not touching
			circleVertices[i * 4] = FloatMath.cos(theta)*width/2.3f + width/2;
			circleVertices[i * 4 + 1] = FloatMath.sin(theta)*height/2.3f + height/2;
			circleVertices[i * 4 + 2] = FloatMath.cos(theta)*width/3.1f + width/2;
			circleVertices[i * 4 + 3] = FloatMath.sin(theta)*height/3.1f + height/2;
			// two dimmer circles are shown for a "glow" effect when the user touches the view
			// this first one is slightly wider...
			selectCircleVertices[i * 4] = FloatMath.cos(theta)*width/2.2f + width/2;
			selectCircleVertices[i * 4 + 1] = FloatMath.sin(theta)*height/2.2f + height/2;
			selectCircleVertices[i * 4 + 2] = FloatMath.cos(theta)*width/3.2f + width/2;
			selectCircleVertices[i * 4 + 3] = FloatMath.sin(theta)*height/3.2f + height/2;
			// and this one is even wider... use alpha channel to produce glow effect
			selectCircle2Vertices[i * 4] = FloatMath.cos(theta)*width/2.1f + width/2;
			selectCircle2Vertices[i * 4 + 1] = FloatMath.sin(theta)*height/2.1f + height/2;
			selectCircle2Vertices[i * 4 + 2] = FloatMath.cos(theta)*width/3.3f + width/2;
			selectCircle2Vertices[i * 4 + 3] = FloatMath.sin(theta)*height/3.3f + height/2;			
		}
		circleVb = makeFloatBuffer(circleVertices);
		selectCircleVb = makeFloatBuffer(selectCircleVertices);
		selectCircleVb2 = makeFloatBuffer(selectCircle2Vertices);
	}
	
	@Override
	protected void init() {
		super.init();
		if (clickable) {
			loadTexture(R.drawable.clock, 0);
			loadTexture(R.drawable.note_icon, 1);
		}
		gl.glClearColor(0, 0, 0, 1);
		setViewLevel(0.5f);
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		super.surfaceChanged(holder, format, width, height);
		// all knobs share the same circle VBs, and they should only change when width or height changes
		if (width != circleWidth || height != circleHeight) {
			initCircleVbs(width, height);
			circleWidth = width;
			circleHeight = height;
		}
	}
	
	@Override
	protected void drawFrame() {
		// background
		drawTriangleStrip(circleVb, bgColor);
		// main selection
		drawTriangleStrip(circleVb, levelColor, drawIndex);
		if (selected) { // selected glow
			drawTriangleStrip(selectCircleVb2, selectColor, drawIndex);
			drawTriangleStrip(selectCircleVb, selectColor, drawIndex);
		}
		if (clickable)
			drawTexture();
	}

	private void updateDrawIndex() {
		if (circleVb == null)
			return;
		drawIndex = (int)(circleVb.capacity() * level / 2);
		drawIndex += drawIndex % 2;
	}
	
	@Override
	public void setViewLevel(float level) {
		super.setViewLevel(level);
		updateDrawIndex();
	}
	
	@Override
	public void setLevel(float level) {
		super.setLevel(level);
		updateDrawIndex();
	}
	
	@Override
	protected void handleActionDown(int id, float x, float y) {
		if (distanceFromCenter(x, y) > width/4)
			setLevel(coordToLevel(x, y));
		else
			timeDown = System.currentTimeMillis();
		super.handleActionDown(id, x, y);
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		if (distanceFromCenter(e.getX(0), e.getY(0)) < width/4)
			return;
		float newLevel = coordToLevel(e.getX(0), e.getY(0));
		setLevel(newLevel);
	}
	
	@Override
	protected void handleActionUp(int id, float x, float y) {
		if (clickable && System.currentTimeMillis() - timeDown < 300) {
			currentTexture = (currentTexture + 1 ) % 2;
			for (LevelListener listener : levelListeners) {
				listener.notifyClicked(this);
			}
		}
		super.handleActionUp(id, x, y);
	}
	
	public boolean isClickable() {
		return clickable;
	}
	
	private float distanceFromCenter(float x, float y) {
		return FloatMath.sqrt((x - width/2)*(x - width/2) + (y - height/2)*(y - height/2));
	}
	
	private float coordToLevel(float x, float y) {
		float unitX = (x - width/2)/width;
		float unitY = (y - height/2)/height;
		float theta = (float)Math.atan(unitY/unitX) + ¹ / 2;
		// atan ranges from 0 to ¹, and produces symmetric results around the y axis.
		// we need 0 to 2*¹, so ad ¹ if right of x axis.
		if (unitX > 0) theta += ¹;
		// convert to level - remember, min theta is ¹/4, max is 7¹/8
		float level = (4 * theta / ¹ - 1) / 6;
		return level > 0 ? (level < 1 ? level : 1) : 0;
	}
}
