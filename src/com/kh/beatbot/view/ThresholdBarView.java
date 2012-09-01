package com.kh.beatbot.view;

import android.content.Context;
import android.util.AttributeSet;

import com.kh.beatbot.global.GlobalVars;

public class ThresholdBarView extends TronSeekbar {
	private static final float[] THRESHOLD_COLOR = { BG_COLOR[0] + .2f, BG_COLOR[1] + .2f, BG_COLOR[2] + .2f, 1};
	private static int maxGreenVertices, maxYellowVertices, maxRedVertices;
	
	private static int currAmpVertex;
	private static float currAmpLevel;
	
	public ThresholdBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private void updateAmpVertex() {
		if (levelBarVb == null)
			return;
		currAmpVertex = (int)(currAmpLevel * levelBarVb.capacity() / 2);
		currAmpVertex += currAmpVertex % 2;
		currAmpVertex = currAmpVertex > 2 ? currAmpVertex : 2;
	}
	
	public void setChannelLevel(float channelDb) {
		// map channel DB to range (0, 1)
		float newChannelLevel = dbToUnit(channelDb);
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
	
	protected void init() {
		super.init();
		level = 0;
		initVerticesLimits();
		updateAmpVertex();
	}
	
	private void drawThresholdLevel() {
		drawTriangleStrip(levelBarVb, THRESHOLD_COLOR, currAmpVertex);
		translate(0, levelBarHeight / 2);
		// circles for rounded rect ends
		drawPoint(levelBarHeight, THRESHOLD_COLOR, 0);
		drawPoint(levelBarHeight, THRESHOLD_COLOR, numLevelVertices - 2);
		// bigger selection point
		drawPoint(levelBarHeight * 4, selectColor, numLevelVertices - 2);
		translate(0, -levelBarHeight / 2);
	}
	
	private void drawDbLevel() {
		drawTriangleStrip(levelBarVb, GlobalVars.GREEN, 0,
				currAmpVertex <= maxGreenVertices ? currAmpVertex : maxGreenVertices);
		if (currAmpVertex >= maxGreenVertices) {
			drawTriangleStrip(levelBarVb, GlobalVars.YELLOW, maxGreenVertices - 2,
					currAmpVertex <= maxYellowVertices ? currAmpVertex : maxYellowVertices);
		}
		if (currAmpVertex >= maxYellowVertices) {
			drawTriangleStrip(levelBarVb, GlobalVars.RED, maxYellowVertices - 2,
					currAmpVertex <= maxRedVertices ? currAmpVertex : maxRedVertices);
		}
		translate(0, levelBarHeight / 2);
		if (currAmpVertex > 0) { // draw circle at beginning
			drawPoint(levelBarHeight, GlobalVars.GREEN, 0);
		}
		if (currAmpVertex >= maxRedVertices) { // draw circle at end
			drawPoint(levelBarHeight, GlobalVars.RED, levelBarVb.capacity() - 2);
		}
		translate(0, -levelBarHeight / 2);
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
	
	private float dbToUnit(float db) {
		// db range = -60 - 0, need range 0-1
		return db <= -60 ? 0 : db/60 + 1;
	}
}