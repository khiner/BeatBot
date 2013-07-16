package com.kh.beatbot.ui.view.control;

import java.nio.FloatBuffer;

import com.kh.beatbot.ui.color.Colors;

public class Seekbar extends ControlView1dBase {

	protected FloatBuffer levelBarVb = null;
	protected int numLevelVertices = 0;
	protected float levelBarHeight = 8;
	private float middleY = 0;
	
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
	
	public void init() {
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
	public void draw() {
		super.draw();
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

	protected float posToLevel(float x, float y) {
		if (x > width - levelBarHeight)
			return 1;
		float level = (x - levelBarHeight / 2) / (width - levelBarHeight * 4);
		return level < 0 ? 0 : (level > 1 ? 1 : level);
	}
}
