package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;

public class TronSeekbar extends LevelListenable {
	private static FloatBuffer levelBarVb = null;
	private int numLevelVertices = 0;

	public TronSeekbar(Context c, AttributeSet as) {
		super(c, as);
	}

	public void addLevelListener(LevelListener levelListener) {		
		levelListeners.add(levelListener);
	}
	
	private void initLevelBarVB() {
		if (levelBarVb != null)
			return; // only need one VB for all level bars to share
		float[] vertices = new float[800];
		for (int i = 0; i < vertices.length / 4; i++) {
			vertices[i * 4] = ((float) i / (vertices.length / 4)) * (width - 10) + 5;
			vertices[i * 4 + 1] = -4;
			vertices[i * 4 + 2] = vertices[i * 4];
			vertices[i * 4 + 3] = 4;
		}
		levelBarVb = makeFloatBuffer(vertices);
	}
	
	@Override
	protected void init() {
		super.init();
		initLevelBarVB();
	}
		
	private void drawBar() {
		gl.glPushMatrix();
		// draw background rect
		gl.glTranslatef(0,  height/2, 0);
		drawTriangleStrip(levelBarVb, bgColor);
		
		// draw level
		drawTriangleStrip(levelBarVb, levelColor, numLevelVertices);
		
		if (selected) {
			gl.glPushMatrix();
			selectColor[3] = .2f;
			gl.glTranslatef(-6f, 0, 0);
			gl.glScalef(1.01f, 1, 1);
			for (int i = 0; i < 5; i++) {
				gl.glScalef(1, 1.2f, 1);
				drawTriangleStrip(levelBarVb, selectColor, numLevelVertices);
			}
			gl.glPopMatrix();
			selectColor[3] = .5f;
		}
		
		// draw circle at the end of background rect
		gl.glPointSize(8);
		gl.glTranslatef(0, 4, 0);
		setColor(bgColor);
		gl.glDrawArrays(GL10.GL_POINTS, levelBarVb.capacity() / 2 - 2, 1);
		// draw level-colored circle at beginning and end of level
		setColor(levelColor);
		gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
		gl.glDrawArrays(GL10.GL_POINTS, numLevelVertices - 2, 1);
		setColor(selectColor);
		// draw bigger, translucent 'selection' circle at end
		if (selected) {
			gl.glPointSize(18);
			setColor(levelColor);
			gl.glDrawArrays(GL10.GL_POINTS, numLevelVertices - 2, 1);
			setColor(selectColor);
			for (int i = 20; i < 32; i+=4) {
				gl.glPointSize(i);
				gl.glDrawArrays(GL10.GL_POINTS, numLevelVertices - 2, 1);
			}
		} else {
			gl.glPointSize(20);
			gl.glDrawArrays(GL10.GL_POINTS, numLevelVertices - 2, 1);
		}
			
		gl.glPopMatrix();
	}

	public void setViewLevel(float x) {
		super.setViewLevel(x);
		initLevelBarVB();
	}

	@Override
	protected void drawFrame() {
		gl.glClearColor(0, 0, 0, 1);
		drawBar();
	}

	public void setLevel(float level) {
		super.setLevel(level);
		updateNumLevelVertices();
	}
	
	private void updateNumLevelVertices() {
		numLevelVertices = (int)(level*(levelBarVb.capacity() / 2));
		// want even number of vertices to avoid jagged ending
		numLevelVertices += numLevelVertices % 2;
		// make sure we don't go have an out of bounds index
		numLevelVertices = Math.min(numLevelVertices, levelBarVb.capacity() / 2);
	}
	
	private float xToLevel(float x) {
		float level = (x - height/2)/(width - height);
		level = level < 0 ? 0 : (level > 1 ? 1 : level);
		return level;
	}
	
	private float levelToX(float level) {
		return height/2 + level*(width - height);
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
