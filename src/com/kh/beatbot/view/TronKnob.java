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

import com.kh.beatbot.R;
import com.kh.beatbot.listenable.LevelListenable;

public class TronKnob extends LevelListenable {

	private FloatBuffer circleVb = null;
	private FloatBuffer selectCircleVb = null;
	private FloatBuffer selectCircleVb2 = null;
	private float[] bgColor = new float[] {.3f, .3f, .3f , 1};
	private float[] selectColor = {levelColor[0], levelColor[1], levelColor[2], .4f};
	private int[] textureHandlers = new int[2];
	private int[] crop = new int[4];
	private int imageWidth, imageHeight;
	private int currentTexture = 0;
	private long timeDown = 0;
	
	public TronKnob(Context c, AttributeSet as) {
		super(c, as);
	}

	public void loadTexture(final int resourceId, int textureId) {
		// Generate Texture ID
		gl.glGenTextures(1, textureHandlers, textureId);
		// Bind texture id texturing target
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandlers[textureId]); 
		 
		InputStream is = getContext().getResources().openRawResource(resourceId);
		Bitmap bitmap = BitmapFactory.decodeStream(is);
		 
		// Build our crop texture to be the size of the bitmap (ie full texture)
		crop[0] = 0;
		crop[1] = imageHeight = bitmap.getHeight();
		crop[2] = imageWidth = bitmap.getWidth();
		crop[3] = -bitmap.getHeight();
		 
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
	
	private void initCircleVbs() {
		float[] circleVertices = new float[400];
		float[] selectCircleVertices = new float[400];
		float[] selectCircle2Vertices = new float[400];
		float theta;
		for (int i = 0; i < circleVertices.length / 4; i++) {
			theta = (float)(Math.PI / 2) * (i * 13f / (circleVertices.length) + 33f/24);
			circleVertices[i * 4] = FloatMath.cos(theta)*width/2.3f + width/2;
			circleVertices[i * 4 + 1] = FloatMath.sin(theta)*height/2.3f + height/2;
			circleVertices[i * 4 + 2] = FloatMath.cos(theta)*width/3.1f + width/2;
			circleVertices[i * 4 + 3] = FloatMath.sin(theta)*height/3.1f + height/2;
			selectCircleVertices[i * 4] = FloatMath.cos(theta)*width/2.2f + width/2;
			selectCircleVertices[i * 4 + 1] = FloatMath.sin(theta)*height/2.2f + height/2;
			selectCircleVertices[i * 4 + 2] = FloatMath.cos(theta)*width/3.2f + width/2;
			selectCircleVertices[i * 4 + 3] = FloatMath.sin(theta)*height/3.2f + height/2;
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
		loadTexture(R.drawable.clock, 0);
		loadTexture(R.drawable.note_icon, 1);
		gl.glClearColor(0, 0, 0, 1);
		gl.glEnable(GL10.GL_LINE_SMOOTH);
		initCircleVbs();
	}
	
	@Override
	protected void drawFrame() {
		drawTriangleStrip(circleVb, bgColor);
		drawTriangleStrip(circleVb, levelColor ,(int)(circleVb.capacity() * level / 2));
		if (selected) {
			drawTriangleStrip(selectCircleVb2, selectColor ,(int)(circleVb.capacity() * level / 2));
			drawTriangleStrip(selectCircleVb, selectColor ,(int)(circleVb.capacity() * level / 2));
		}
		//drawLines(outerCircleVb, levelColor , 2, GL10.GL_LINE_STRIP, Float.SIZE/2);
		drawTexture();
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
//		if (newLevel - level < -.2f)
//			newLevel = 1;
//		else if (level - newLevel < -.2f)
//			newLevel = 0;
		setLevel(newLevel);
	}
	
	@Override
	protected void handleActionUp(int id, float x, float y) {
		if (System.currentTimeMillis() - timeDown < 300) {
			currentTexture = (currentTexture + 1 ) % 2;
		}
		super.handleActionUp(id, x, y);
	}
	
	private float distanceFromCenter(float x, float y) {
		return FloatMath.sqrt((x - width/2)*(x - width/2) + (y - height/2)*(y - height/2));
	}
	
	private float coordToLevel(float x, float y) {
		float unitX = (x - width/2)/width;
		float unitY = (y - height/2)/height;
		float theta = (float)Math.atan(unitY/unitX);
		float level = theta/(2*(float)Math.PI) + 0.25f;
		if (unitX > 0) level += .5;
		return level;
	}
}
