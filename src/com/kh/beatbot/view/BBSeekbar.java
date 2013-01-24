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
	private float middleY = 0;
	
	public BBSeekbar(Context c, AttributeSet as) {
		super(c, as);
	}

	protected void initLevelBarVb() {
		float[] vertices = new float[800];
		for (int i = 0; i < vertices.length / 4; i++) {
			vertices[i * 4] = ((float) i / (vertices.length / 4))
					* (width - levelBarHeight * 4) + levelBarHeight * 2;
			vertices[i * 4 + 1] = -levelBarHeight / 2 + height / 2;
			vertices[i * 4 + 2] = vertices[i * 4];
			vertices[i * 4 + 3] = levelBarHeight / 2 + height / 2;
		}
		levelBarVb = makeFloatBuffer(vertices);
		middleY = levelBarVb.get(1) + levelBarHeight / 2;
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
		drawTriangleStrip(levelBarVb, Colors.VIEW_BG);
		// circle at end of level for rounded edge
		drawPoint(levelBarHeight / 2, Colors.VIEW_BG, levelBarVb.get(levelBarVb.capacity() - 2), middleY);
	}

	protected void drawLevel() {
		drawTriangleStrip(levelBarVb, levelColor, numLevelVertices);

		// draw level-colored circle at beginning and end of level
		drawPoint(levelBarHeight / 2, levelColor, levelBarVb.get(0), middleY);
		drawPoint(levelBarHeight / 2, levelColor, levelBarVb.get(numLevelVertices * 2 - 2), middleY);

		drawLevelSelectionCircle();
	}

	protected void drawTouchedLevelSelectionCircle() {
		selectColor[3] = .7f;
		drawPoint(3 * levelBarHeight / 2, selectColor, levelBarVb.get(numLevelVertices * 2 - 2), middleY);
		selectColor[3] = .5f;
		for (int i = (int) (5 * levelBarHeight / 4); i < levelBarHeight * 2; i += 2) {
			drawPoint(i, selectColor, levelBarVb.get(numLevelVertices * 2 - 2), middleY);
		}
	}

	protected void drawLevelSelectionCircle() {
		// draw bigger, translucent 'selection' circle at end of level
		if (selected) {
			drawTouchedLevelSelectionCircle();
		} else {
			selectColor[3] = .5f;
			drawPoint(5 * levelBarHeight / 4, selectColor, levelBarVb.get(numLevelVertices * 2 - 2), middleY);
		}
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
		requestRender();
	}

	protected float xToLevel(float x) {
		if (x > width - levelBarHeight)
			return 1;
		float level = (x - levelBarHeight / 2) / (width - levelBarHeight * 4);
		level = level < 0 ? 0 : (level > 1 ? 1 : level);
		return level;
	}

	@Override
	protected void handleActionDown(MotionEvent e, int id, float x, float y) {
		setLevel(xToLevel(x));
		super.handleActionDown(e, id, x, y);
	}

	@Override
	protected void handleActionMove(MotionEvent e, int id, float x, float y) {
		if (id != 0)
			return; // no multitouch for level - one pointer drags one level!
		setLevel(xToLevel(x));
	}
}
