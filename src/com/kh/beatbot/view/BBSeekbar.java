package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.listenable.LevelListenable;

public class BBSeekbar extends LevelListenable {
	protected FloatBuffer levelBarVb = null;
	protected int numLevelVertices = 0;
	protected float levelBarHeight = 8;

	public BBSeekbar(Context c, AttributeSet as) {
		super(c, as);
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

	protected void loadIcons() {
		// no icons to load
	}
	
	protected void init() {
		initLevelBarVb();
		updateNumLevelVertices();
		super.init();
	}

	protected void drawBackgroundBar() {
		gl.glPushMatrix();
		translate(levelBarHeight * 2, height / 2);
		drawTriangleStrip(levelBarVb, Colors.VIEW_BG);
		// circle at beginning and end of level for rounded edge
		translate(0, levelBarHeight / 2);
		drawPoint(levelBarHeight, Colors.VIEW_BG, 0);
		drawPoint(levelBarHeight, Colors.VIEW_BG, levelBarVb.capacity() / 2 - 2);
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
		selectColor[3] = .7f;
		drawPoint(levelBarHeight * 3, selectColor, numLevelVertices - 2);
		selectColor[3] = .5f;
		for (int i = (int) (levelBarHeight * 3); i < levelBarHeight * 4; i += 4) {
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
		if (levelBarVb == null)
			return;
		numLevelVertices = (int) (level * (levelBarVb.capacity() / 2));
		// want even number of vertices to avoid jagged ending
		numLevelVertices += numLevelVertices % 2;
		// make sure we don't go have an out of bounds index
		numLevelVertices = numLevelVertices > 2 ? (numLevelVertices < levelBarVb
				.capacity() / 2 ? numLevelVertices : levelBarVb.capacity() / 2)
				: 2;
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
