package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;

public class TronSeekbar extends LevelListenable {
	protected static FloatBuffer levelBarVb = null;
	protected int numLevelVertices = 0;
	protected float levelBarHeight = 8;
	protected static float currWidth = 0;
	
	public TronSeekbar(Context c, AttributeSet as) {
		super(c, as);
	}

	public void addLevelListener(LevelListener levelListener) {
		levelListeners.add(levelListener);
	}

	protected void initLevelBarVb() {
		float[] vertices = new float[800];
		for (int i = 0; i < vertices.length / 4; i++) {
			vertices[i * 4] = ((float) i / (vertices.length / 4))
					* (width - levelBarHeight * 4);
			vertices[i * 4 + 1] = -levelBarHeight / 2;
			vertices[i * 4 + 2] = vertices[i * 4];
			vertices[i * 4 + 3] = levelBarHeight / 2;
		}
		levelBarVb = makeFloatBuffer(vertices);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		super.surfaceChanged(holder, format, width, height);
		// all knobs share the same circle VBs, and they should only change when width or height changes
		if (width != currWidth) {
			initLevelBarVb();
			currWidth = width;
		}
	}
	
	protected void drawBackgroundBar() {
		gl.glPushMatrix();
		translate(levelBarHeight * 2, height / 2);
		drawTriangleStrip(levelBarVb, BG_COLOR);
		// circle at beginning and end of level for rounded edge
		translate(0, levelBarHeight / 2);
		drawPoint(levelBarHeight, BG_COLOR, 0);
		drawPoint(levelBarHeight, BG_COLOR, levelBarVb.capacity() / 2 - 2);
		gl.glPopMatrix();
	}

	protected void drawLevel() {
		gl.glPushMatrix();
		translate(levelBarHeight * 2, height / 2);
		drawTriangleStrip(levelBarVb, levelColor, numLevelVertices);
		if (selected) {
			drawSelectedLevel();
		}
		
		translate(0, levelBarHeight / 2);
		// draw level-colored circle at beginning and end of level
		drawPoint(levelBarHeight, levelColor, 0);
		drawPoint(levelBarHeight, levelColor, numLevelVertices - 2);
		
		drawLevelSelectionCircle();
		gl.glPopMatrix();
	}

	protected void drawTouchedLevelSelectionCircle() {
		drawPoint(levelBarHeight * 3, levelColor, numLevelVertices - 2);
		for (int i = (int)(levelBarHeight * 3); i < levelBarHeight * 4; i += 4) {
			drawPoint(i, selectColor, numLevelVertices - 2);
		}
	}
	
	protected void drawLevelSelectionCircle() {
		// draw bigger, translucent 'selection' circle at end of level
		if (selected) {
			drawTouchedLevelSelectionCircle();
		} else {
			selectColor[3] = .5f;
			drawPoint(levelBarHeight * 2.5f, selectColor, numLevelVertices - 2);
		}
	}
	
	protected void drawSelectedLevel() {
		gl.glPushMatrix();
		selectColor[3] = .2f;
		translate(-6f, 0);
		gl.glScalef(1.01f, 1, 1);
		for (int i = 0; i < 5; i++) {
			gl.glScalef(1, 1.2f, 1);
			drawTriangleStrip(levelBarVb, selectColor, numLevelVertices);
		}
		gl.glPopMatrix();
	}

	protected void drawBar() {
		drawBackgroundBar();
		drawLevel();
	}

	@Override
	protected void drawFrame() {
		drawBar();
	}

	public void setViewLevel(float level) {
		super.setViewLevel(level);
		updateNumLevelVertices();
	}

	protected void updateNumLevelVertices() {
		numLevelVertices = (int) (level * (levelBarVb.capacity() / 2));
		// want even number of vertices to avoid jagged ending
		numLevelVertices += numLevelVertices % 2;
		// make sure we don't go have an out of bounds index
		numLevelVertices = numLevelVertices > 2 ? (numLevelVertices < levelBarVb.capacity() / 2?
				numLevelVertices : levelBarVb.capacity() / 2) : 2;
	}

	protected float xToLevel(float x) {
		if (x > width - levelBarHeight)
			 return 1;
		float level = (x - levelBarHeight / 2) / (width - levelBarHeight * 4);
		level = level < 0 ? 0 : (level > 1 ? 1 : level);
		return level;
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		setLevel(xToLevel(x));
		super.handleActionDown(id, x, y);
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		setLevel(xToLevel(e.getX(0)));
	}
}
