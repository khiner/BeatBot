package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.bean.MidiViewBean;

public class TronSeekbar2d extends LevelListenable {
	private static final int DRAW_OFFSET = 8;
	private float selectX = 0, selectY = 0;
	private static FloatBuffer borderVb = null;
	private static FloatBuffer selectionVb = null;
	private static FloatBuffer horizontalVb = null;
	private static FloatBuffer verticalVb = null;
	
	private float ¹ = (float)Math.PI;
	public TronSeekbar2d(Context c, AttributeSet as) {
		super(c, as);
	}
	
	public float[] calcRoundedCornerVertices(float width, float height, float cornerRadius, int resolution) {
		float[] roundedRect = new float[resolution * 8];
		float theta, addX, addY;
		for (int i = 0; i < roundedRect.length / 2; i++) {
			theta =  i * 4 * ¹ / roundedRect.length;
			if (theta < ¹ / 2) { // lower right
				addX = width / 2 - cornerRadius;
				addY = height / 2 - cornerRadius;
			} else if (theta < ¹) { // lower left
				addX = -width / 2 + cornerRadius;
				addY = height / 2 - cornerRadius;
			} else if (theta < 3 * ¹ / 2) { // upper left
				addX = -width / 2 + cornerRadius;
				addY = -height / 2 + cornerRadius;
			} else { // upper right
				addX = width / 2 - cornerRadius;
				addY = -height / 2 + cornerRadius;
			}
			roundedRect[i * 2] = FloatMath.cos(theta) * cornerRadius + addX;
			roundedRect[i * 2 + 1] = FloatMath.sin(theta) * cornerRadius + addY;
		}
		return roundedRect;
	}
	
	private void initBorderVb() {
		float rectWidth = width - DRAW_OFFSET;
		float rectHeight = height - DRAW_OFFSET;
		float cornerRadius = rectWidth/8;
		int resolution = 25;
		borderVb = makeFloatBuffer(calcRoundedCornerVertices(rectWidth, rectHeight, cornerRadius, resolution));
	}

	private void initSelectionVb() {
		float rectWidth = width / 10;
		float rectHeight = height / 10;
		float cornerRadius = rectWidth / 4;
		int resolution = 10;
		selectionVb = makeFloatBuffer(calcRoundedCornerVertices(rectWidth, rectHeight, cornerRadius, resolution));
	}
	
	private void initLineVbs() {
		horizontalVb = makeRectFloatBuffer(DRAW_OFFSET / 2, -3, width - DRAW_OFFSET / 2, 3);
		verticalVb = makeRectFloatBuffer(-3, DRAW_OFFSET / 2, 3, height - DRAW_OFFSET / 2);
	}
	
	public void setViewLevelX(float x) {
		selectX = x*(width - 50) + 25;
	}
	
	public void setViewLevelY(float y) {
		selectY = (1 - y)*(height - 50) + 25; // top of screen has lowest value in my OpenGl window
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		super.surfaceChanged(holder, format, width, height);
		initBorderVb();
		initLineVbs();
		initSelectionVb();
	}

	@Override
	protected void drawFrame() {
		gl.glClearColor(0, 0, 0, 1); // black bg
		drawBackground();
		drawLines();
		drawSelection();
		drawBorder();
	}

	private void drawBackground() {
		gl.glPushMatrix();
		gl.glTranslatef(width / 2, height / 2, 0);
		drawTriangleFan(borderVb, bgColor);
		gl.glPopMatrix();
	}
	private void drawBorder() {
		gl.glPushMatrix();
		gl.glTranslatef(width / 2, height / 2, 0);
		drawLines(borderVb, MidiViewBean.VOLUME_COLOR, 5, GL10.GL_LINE_LOOP);
		gl.glPopMatrix();
	}
	
	private void drawLines() {
		gl.glPushMatrix();
		gl.glTranslatef(selectX, 0, 0);
		drawTriangleStrip(verticalVb, levelColor);
		if (selected) {
			gl.glPushMatrix();
			for (int i = 0; i < 5; i++) {
				gl.glScalef(1.1f, 1, 1);
				drawTriangleStrip(verticalVb, selectColor);
			}
			gl.glPopMatrix();
		}			
		gl.glTranslatef(-selectX, selectY, 0);
		drawTriangleStrip(horizontalVb, levelColor);
		if (selected) {
			gl.glPushMatrix();
			for (int i = 0; i < 5; i++) {
				gl.glScalef(1, 1.1f, 1);
				drawTriangleStrip(horizontalVb, selectColor);
			}
			gl.glPopMatrix();			
		}
		gl.glPopMatrix();
	}
	
	private void drawSelection() {
		gl.glPushMatrix();
		gl.glTranslatef(selectX, selectY, 0);
		drawTriangleFan(selectionVb, levelColor);
		if (selected) {
			for (int i = 0; i < 5; i++) {
				gl.glScalef(1.05f, 1.05f, 1);
				drawTriangleFan(selectionVb, selectColor);
			}
		}
		gl.glPopMatrix();
	}
	
	private void selectLocation(float x, float y) {
		selectX = x < 25 ? 25 : (x > width - 25 ? width - 25 : x);
		selectY = y < 25 ? 25 : (y > height - 25 ? height - 25 : y);
		for (LevelListener listener : levelListeners) {
			listener.setLevel(this, (selectX - 25)/(width - 50), ((height - selectY) - 25)/(height - 50));
		}
	}
	
	@Override
	protected void handleActionDown(int id, float x, float y) {
		selectLocation(x, y);
		super.handleActionDown(id, x,  y);
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		selectLocation(e.getX(0), e.getY(0));
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id,  x, y);
	}	
}
