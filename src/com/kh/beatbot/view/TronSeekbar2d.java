package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;

import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.bean.MidiViewBean;

public class TronSeekbar2d extends LevelListenable {
	private static final int DRAW_OFFSET = 8;
	private static float minX, maxX, minY, maxY;
	private float selectX = 0, selectY = 0;
	private float borderRadius;
	private static FloatBuffer borderVb = null;
	private static FloatBuffer lineVb = null;

	private float ¹ = (float) Math.PI;

	public TronSeekbar2d(Context c, AttributeSet as) {
		super(c, as);
	}

	public float[] calcRoundedCornerVertices(float width, float height,
			float cornerRadius, int resolution) {
		float[] roundedRect = new float[resolution * 8];
		float theta = 0, addX, addY;
		for (int i = 0; i < roundedRect.length / 2; i++) {
			theta += 4 * ¹ / roundedRect.length;
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
		borderRadius = width / 14;
		minX = borderRadius + 2;
		minY = borderRadius + 2;
		maxX = width - borderRadius - 2;
		maxY = height - borderRadius - 2;
		borderVb = makeFloatBuffer(calcRoundedCornerVertices(width
				- DRAW_OFFSET * 2, height - DRAW_OFFSET * 2, borderRadius, 25));
	}

	public void setViewLevelX(float x) {
		selectX = x * (width - 2 * minX) + minX;
		initLines();
	}

	public void setViewLevelY(float y) {
		// top of screen lowest value in my OpenGl window
		selectY = (1 - y) * (height - minY * 2) + minY;
		initLines();
	}

	@Override
	public void init() {
		super.init();
		initBorderVb();
		initLines();
		setViewLevelX(0.5f);
		setViewLevelY(0.5f);
	}

	@Override
	protected void drawFrame() {
		gl.glClearColor(0, 0, 0, 1); // black bg
		drawBackground();
		drawSelection();
	}

	private void drawBackground() {
		gl.glTranslatef(width / 2, height / 2, 0);
		drawTriangleFan(borderVb, bgColor);
		gl.glTranslatef(-width / 2, -height / 2, 0);
		levelColor[3] = 1; // completely opaque alpha
		drawLines(lineVb, levelColor, 5, GL10.GL_LINES);
		gl.glTranslatef(width / 2, height / 2, 0);
		drawLines(borderVb, MidiViewBean.VOLUME_COLOR, 5, GL10.GL_LINE_LOOP);
		gl.glTranslatef(-width / 2, -height / 2, 0);
	}

	private void initLines() {
		lineVb = makeFloatBuffer(new float[] { DRAW_OFFSET, selectY,
				width - DRAW_OFFSET, selectY, selectX, DRAW_OFFSET, selectX,
				height - DRAW_OFFSET });
	}

	private void drawSelection() {
		setColor(levelColor);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, makeFloatBuffer(new float[] {
				selectX, selectY }));
		gl.glPointSize(borderRadius);
		gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
		levelColor[3] = .4f;
		setColor(levelColor);
		for (float size = borderRadius; size < borderRadius * 1.5; size++) {
			gl.glPointSize(size);
			gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
		}
	}

	private void selectLocation(float x, float y) {
		selectX = x < minX ? minX : (x > maxX ? maxX : x);
		selectY = y < minY ? minY : (y > maxY ? maxY : y);
		initLines();
		for (LevelListener listener : levelListeners) {
			listener.setLevel(this, (selectX - minX) / (width - minX * 2),
					((height - selectY) - minY) / (height - minY * 2));
		}
	}

	@Override
	protected void onMeasure(int width, int height) {
		int dim = Math.min(width, height);
		super.onMeasure(dim, dim);
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		selectLocation(x, y);
		levelColor = MidiViewBean.LEVEL_SELECTED_COLOR.clone();
		super.handleActionDown(id, x, y);
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		selectLocation(e.getX(0), e.getY(0));
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		levelColor = MidiViewBean.VOLUME_COLOR.clone();
		super.handleActionUp(id, x, y);
	}
}
