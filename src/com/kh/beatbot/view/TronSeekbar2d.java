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
	private float selectX = 0, selectY = 0;
	private static FloatBuffer borderVb;
	private float ¹ = (float)Math.PI;
	public TronSeekbar2d(Context c, AttributeSet as) {
		super(c, as);
	}
	
	private void initBorderVb(float cornerRadius, int resolution) {
		float[] circle = new float[resolution * 8 + 2];
		float offset = 4; // we don't want any vertices to be directly on the edge
		circle[0] = width / 2;
		circle[1] = height / 2;
		
		float theta, addX, addY;
		for (int i = 0; i < circle.length / 2; i++) {
			theta =  i * 4 * ¹ / circle.length;
			if (theta < ¹ / 2) { // lower right
				addX = width - cornerRadius - offset;
				addY = height - cornerRadius - offset;
			} else if (theta < ¹) { // lower left
				addX = cornerRadius + offset;
				addY = height - cornerRadius - offset;
			} else if (theta < 3 * ¹ / 2) { // upper left
				addX = cornerRadius + offset;
				addY = cornerRadius + offset;
			} else { // upper right
				addX = width - cornerRadius - offset;
				addY = cornerRadius + offset;
			}
			circle[i * 2] = FloatMath.cos(theta) * cornerRadius + addX;
			circle[i * 2 + 1] = FloatMath.sin(theta) * cornerRadius + addY;
		}
		borderVb = makeFloatBuffer(circle);
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
		initBorderVb(width / 8, 50);
	}

	@Override
	protected void drawFrame() {
		gl.glClearColor(0, 0, 0, 1); // black bg
		drawBorder();
		drawSelectCircle();
	}

	private void drawBorder() {
		drawTriangleFan(borderVb, bgColor);
		drawLines(borderVb, MidiViewBean.VOLUME_COLOR, 5, GL10.GL_LINE_LOOP);
	}
	
	private void drawSelectCircle() {
		FloatBuffer vb = makeFloatBuffer(new float[] {selectX, selectY});
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vb);
		float alpha;
		for (int i = 15; i > 10; i--) {
			alpha = (16 - i)/6f;
			gl.glPointSize(i*3);
			gl.glColor4f(levelColor[0], levelColor[1], levelColor[2], alpha);
			gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
		}
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
		levelColor = MidiViewBean.LEVEL_SELECTED_COLOR;
		selectLocation(x, y);
		super.handleActionDown(id, x,  y);
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		selectLocation(e.getX(0), e.getY(0));
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		levelColor = MidiViewBean.VOLUME_COLOR;
		super.handleActionUp(id,  x, y);
	}	
}
