package com.kh.beatbot.view.control;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GeneralUtils;
import com.kh.beatbot.view.TouchableSurfaceView;

public class ThresholdBarView extends Seekbar {
	private static final float[] THRESHOLD_COLOR = { Colors.VIEW_BG[0] + .2f,
		Colors.VIEW_BG[1] + .2f, Colors.VIEW_BG[2] + .2f, 1 };
	private static int maxGreenVertices, maxYellowVertices, maxRedVertices;

	private static int currAmpVertex = 0;
	private static float currAmpLevel = 0;

	public ThresholdBarView(TouchableSurfaceView parent) {
		super(parent);
	}
	
	private void updateAmpVertex() {
		if (levelBarVb == null)
			return;
		currAmpVertex = (int) (currAmpLevel * levelBarVb.capacity() / 2);
		currAmpVertex += currAmpVertex % 2;
		currAmpVertex = currAmpVertex > 2 ? currAmpVertex : 2;
	}

	public void setChannelLevel(float channelDb) {
		// map channel DB to range (0, 1)
		float newChannelLevel = GeneralUtils.dbToUnit(channelDb);
		// only see channel level changing if the 'spike' is
		// greater than the current perceived level
		currAmpLevel = Math.max(currAmpLevel, newChannelLevel);
		updateAmpVertex();
	}

	private void dampLevel() {
		// dampen level to emulate physical level meter
		currAmpLevel -= .01f;
		currAmpLevel = currAmpLevel < 0 ? 0 : currAmpLevel;
		updateAmpVertex();
	}

	private void initVerticesLimits() {
		maxGreenVertices = (int) (.33f * (levelBarVb.capacity() / 2));
		maxGreenVertices += maxGreenVertices % 2;
		maxYellowVertices = (int) (.66f * (levelBarVb.capacity() / 2));
		maxYellowVertices += maxYellowVertices % 2;
		maxRedVertices = (int) (levelBarVb.capacity() / 2);
	}

	public void init() {
		super.init();
		setLevel(0.8f);
		initVerticesLimits();
	}

	private void drawThresholdLevel() {
		drawTriangleStrip(levelBarVb, THRESHOLD_COLOR, numLevelVertices);
		// circles for rounded rect ends
		drawPoint(levelBarHeight, THRESHOLD_COLOR, levelBarVb.get(0), levelBarHeight / 2);
		drawPoint(levelBarHeight, THRESHOLD_COLOR, levelBarVb.get(numLevelVertices * 2 - 2), levelBarHeight / 2);
		// bigger selection point
		drawPoint(levelBarHeight * 4, selectColor, levelBarVb.get(numLevelVertices * 2 - 2), levelBarHeight / 2);
	}

	private void drawDbLevel() {
		drawTriangleStrip(levelBarVb, Colors.GREEN, 0,
				currAmpVertex <= maxGreenVertices ? currAmpVertex
						: maxGreenVertices);
		if (currAmpVertex >= maxGreenVertices) {
			drawTriangleStrip(levelBarVb, Colors.YELLOW, maxGreenVertices - 2,
					currAmpVertex <= maxYellowVertices ? currAmpVertex
							: maxYellowVertices);
		}
		if (currAmpVertex >= maxYellowVertices) {
			drawTriangleStrip(levelBarVb, Colors.RED, maxYellowVertices - 2,
					currAmpVertex <= maxRedVertices ? currAmpVertex
							: maxRedVertices);
		}
		if (currAmpVertex > 0) { // draw circle at beginning
			drawPoint(levelBarHeight, Colors.GREEN, levelBarVb.get(0), levelBarHeight / 2);
		}
		if (currAmpVertex >= maxRedVertices) { // draw circle at end
			drawPoint(levelBarHeight, Colors.RED, levelBarVb.get(levelBarVb.capacity() * 2 - 2), levelBarHeight / 2);
		}
	}

	@Override
	protected void drawLevel() {
		gl.glPushMatrix();
		translate(levelBarHeight * 2, height / 2);
		drawThresholdLevel();
		drawDbLevel();
		dampLevel();
		gl.glPopMatrix();
	}
}