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
	int borderResolution = 25;
	private float selectX = 0, selectY = 0;
	private float selectionSide, selectionCornerRadius, borderRadius,
			borderSide;
	private static float minCoord;
	private static FloatBuffer borderVb = null;
	private static FloatBuffer lineVb = null;
	private static FloatBuffer selectionVb = null;

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
		borderSide = width - DRAW_OFFSET;
		borderRadius = borderSide / 8;
		borderVb = makeFloatBuffer(calcRoundedCornerVertices(borderSide,
				borderSide, borderRadius, borderResolution));
		minCoord = borderVb.get((int)(borderResolution * 2.5)) + width / 2;
	}

	private void initSelectionVb() {
		int resolution = 8;
		selectionSide = width / 12;
		selectionCornerRadius = selectionSide / 4;
		selectionVb = makeFloatBuffer(calcRoundedCornerVertices(selectionSide,
				selectionSide, selectionCornerRadius, resolution));
	}
	
	public void setViewLevelX(float x) {
		float minX = min(selectY) + selectionSide / 2;
		selectX = x * (width - 2 * minX) + minX;
		initLines();
	}

	public void setViewLevelY(float y) {
		float minY = min(selectX) + selectionSide / 2;
		// top of screen lowest value in my	OpenGl window
		selectY = (1 - y) * (height - minY * 2) + minY;
		initLines();
	}

	// input = x view location or y view location
	// output = the minimum allowable view location for the opposite dimension
	// such that the entire selection marker is within the rounded square border
	private float min(float coord) {
		if (coord > DRAW_OFFSET + borderRadius && coord < borderSide - borderRadius)
			return DRAW_OFFSET;
		if (coord < DRAW_OFFSET || coord > borderSide)
			return minCoord;
		if (coord > borderSide - borderRadius)
			coord = width - coord;
		float percent = coord / borderRadius;
		int index = (int)(borderResolution * 2 * (1f + percent));
		index += index % 2; // make sure we're grabbing x coord
		return borderVb.get(index) + width / 2;
	}
	
	@Override
	public void init() {
		super.init();
		initBorderVb();
		initLines();
		initSelectionVb();
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
		float xMin = min(selectY) - 2;
		float yMin = min(selectX) - 2;
		float xMax = width - xMin;
		float yMax = height - yMin;
		lineVb = makeFloatBuffer(new float[] {xMin, selectY, xMax, selectY, selectX, yMin, selectX, yMax});
	}

	private void drawSelection() {
		gl.glPushMatrix();
		gl.glTranslatef(selectX, selectY, 0);
		drawTriangleFan(selectionVb, levelColor);
		for (int i = 0; i < 3; i++) {
			levelColor[3] = .5f; // fade alpha
			gl.glScalef(1.05f, 1.05f, 1);
			drawTriangleFan(selectionVb, levelColor);
		}
		gl.glPopMatrix();
	}

	private void selectLocation(float x, float y) {
		float minX = min(y) + selectionSide / 2;
		float maxX = width - minX;
		selectX = x < minX ? minX : (x > maxX ? maxX : x);
		float minY = min(selectX) + selectionSide / 2;
		float maxY = height - minY;
		selectY = y < minY ? minY : (y > maxY ? maxY : y);
		initLines();
		for (LevelListener listener : levelListeners) {
			listener.setLevel(this, (selectX - minX) / (width - minX * 2),
					((height - selectY) - minY) / (height - minY * 2));
		}
	}

	// constrain 2d Seekbar to square proportions
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int side = Math.min(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(side, side);
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
