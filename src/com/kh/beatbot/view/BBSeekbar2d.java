package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.listener.LevelListener;

public class BBSeekbar2d extends LevelViewBase {
	private float selectX = 0, selectY = 0;
	private static ViewRect viewRect;
	private static FloatBuffer lineVb = null;

	public BBSeekbar2d(TouchableSurfaceView parent) {
		super(parent);
	}

	
	public void setViewLevelX(float x) {
		selectX = viewRect.viewX(x);
		initLines();
	}

	public void setViewLevelY(float y) {
		// top of screen lowest value in my OpenGl window
		selectY = viewRect.viewY(y);
		initLines();
	}

	protected void loadIcons() {
		// no icons to load
	}
	
	@Override
	public void init() {
		super.init();
		viewRect = new ViewRect(width, height, 0.08f, 8);
		initLines();
	}

	@Override
	public void draw() {
		viewRect.drawRoundedBg();
		levelColor[3] = 1; // completely opaque alpha
		drawLines(lineVb, levelColor, 5, GL10.GL_LINES);
		viewRect.drawRoundedBgOutline();
		drawSelection();
	}

	private void initLines() {
		lineVb = makeFloatBuffer(new float[] { viewRect.drawOffset, selectY,
				width - viewRect.drawOffset, selectY, selectX, viewRect.drawOffset, selectX,
				height - viewRect.drawOffset});
		requestRender();
	}

	private void drawSelection() {
		setColor(levelColor);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, makeFloatBuffer(new float[] {
				selectX, selectY }));
		gl.glPointSize(viewRect.borderRadius);
		gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
		levelColor[3] = .4f;
		setColor(levelColor);
		for (float size = viewRect.borderRadius; size < viewRect.borderRadius * 1.5; size++) {
			gl.glPointSize(size);
			gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
		}
	}

	private void selectLocation(float x, float y) {
		selectX = viewRect.clipX(x);
		selectY = viewRect.clipY(y);
		initLines();
		for (LevelListener listener : levelListeners) {
			listener.setLevel(this, viewRect.unitX(selectX), viewRect.unitY(selectY));
		}
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		selectLocation(x, y);
		levelColor = Colors.LEVEL_SELECTED.clone();
		super.handleActionDown(id, x, y);
	}

	@Override
	protected void handleActionMove(int id, float x, float y) {
		if (id != 0)
			return;  // no multitouch for level - one pointer drags one level!
		selectLocation(x, y);
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		levelColor = Colors.VOLUME.clone();
		super.handleActionUp(id, x, y);
	}

	@Override
	protected void createChildren() {
		// leaf child
	}

	@Override
	public void layoutChildren() {
		// leaf child
	}
}
