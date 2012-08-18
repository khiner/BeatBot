package com.kh.beatbot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.global.GlobalVars;

public class ThresholdBarView extends TronSeekbar {

	private float dbThreshold;
	private short shortThreshold;
	private int thresholdVertex;
	private int maxGreenVertices, maxYellowVertices, maxRedVertices;

	public ThresholdBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setThreshold(0.5f);
	}

	public float getThreshold() {
		return dbThreshold;
	}

	public short getShortThreshold() {
		return shortThreshold;
	}
	
	private void updateThresholdVertex() {
		if (levelBarVb == null)
			return;
		thresholdVertex = (int)(dbThreshold * levelBarVb.capacity() / 2);
		thresholdVertex += thresholdVertex % 2;
		thresholdVertex = thresholdVertex > levelBarVb.capacity() / 2 - 2? levelBarVb.capacity() / 2 - 2: thresholdVertex;
	}
	
	public void setThreshold(float threshold) {
		this.dbThreshold = clipToUnit(threshold);
		shortThreshold = dbToShort((this.dbThreshold - 1)*60);
		updateThresholdVertex();
	}

	public void setChannelLevel(float channelDb) {
		// map channel DB to range (0, 1)
		float newChannelLevel = dbToUnit(channelDb);
		// only see channel level changing if the 'spike' is
		// greater than the current perceived level
		setViewLevel(Math.max(level, newChannelLevel));
	}

	private void dampLevel() {
		// dampen level to emulate physical level meter
		level -= .008f;
		level = level < 0 ? 0 : level;
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
		initVerticesLimits();
		updateThresholdVertex();
	}
	
	@Override
	protected void drawLevel() {
		setColor(GlobalVars.GREEN);
		gl.glPushMatrix();
		translate(levelBarHeight * 2, height / 2);
		drawTriangleStrip(levelBarVb, GlobalVars.GREEN, 0,
				numLevelVertices <= maxGreenVertices ? numLevelVertices : maxGreenVertices);
		if (numLevelVertices >= maxGreenVertices) {
			drawTriangleStrip(levelBarVb, GlobalVars.YELLOW, maxGreenVertices - 2,
					numLevelVertices <= maxYellowVertices ? numLevelVertices : maxYellowVertices);
		}
		if (numLevelVertices >= maxYellowVertices) {
			drawTriangleStrip(levelBarVb, GlobalVars.RED, maxYellowVertices - 2,
					numLevelVertices <= maxRedVertices ? numLevelVertices : maxRedVertices);
		}
		translate(0, levelBarHeight / 2);
		if (numLevelVertices > 0) { // draw circle at beginning
			drawPoint(levelBarHeight, GlobalVars.GREEN, 0);
		}
		if (numLevelVertices >= maxRedVertices) { // draw circle at end
			drawPoint(levelBarHeight, GlobalVars.RED, levelBarVb.capacity() - 2);
		}
		drawPoint(levelBarHeight * 4, selectColor, thresholdVertex);
		gl.glPopMatrix();
		dampLevel();
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		setThreshold(xToLevel(x));
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		setThreshold(xToLevel(e.getX(0)));
	}
	
	private float dbToUnit(float db) {
		// db range = -60 - 0, need range 0-1
		return db <= -60 ? 0 : db/60 + 1;
	}
	
	private float clipToUnit(float x) {
		return x >= 0 ? (x <= 0.99f ? x : 0.99f) : 0;
	}
	
	private short dbToShort(float db) {
		return (short)(32768*Math.pow(10, db/20));
	}
}