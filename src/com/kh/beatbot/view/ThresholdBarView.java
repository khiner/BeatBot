package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.kh.beatbot.global.GlobalVars;

public class ThresholdBarView extends SurfaceViewBase {

	private final float[] LINE_COLOR = {.9f, .9f, .9f, .65f};
	private final float[] LINE_OUTLINE_COLOR = GlobalVars.WHITE;
	
	private final int numBars = 75; 
	private final float thumbWidth = 15;
	
	FloatBuffer channelBuffer;
	FloatBuffer thresholdBarBuffer;
	FloatBuffer thresholdLineBuffer;

	private int barWidth;

	private float threshold;
	private short shortThreshold;
	private float channelLevel = 0;

	public ThresholdBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setThreshold(0.5f);
	}

	public float getThreshold() {
		return threshold;
	}

	public short getShortThreshold() {
		return shortThreshold;
	}
	
	public void setThreshold(float threshold) {
		this.threshold = clipToUnit(threshold);
		shortThreshold = dbToShort((this.threshold - 1)*60);
		initThresholdBar();
	}

	public void setChannelLevel(float channelDb) {
		// map channel DB to range (0, 1)
		float newChannelLevel = dbToUnit(channelDb);
		// only see channel level changing if the 'spike' is
		// greater than the current perceived level
		channelLevel = Math.max(channelLevel, newChannelLevel);
	}

	private void dampChannelLevel() {
		// dampen level to emulate physical level meter
		channelLevel *= .95f;
		channelLevel = channelLevel < 0 ? 0 : channelLevel;
	}
	
	private void initChannelBuffers() {
		float[] channelCoords = new float[(numBars*4)];
		int y1 = 0;
		int y2 = 3*height / 4;
		for (int x = barWidth/2, i = 0; i < numBars; x += barWidth, i++) {
			channelCoords[i * 4] = x;
			channelCoords[i * 4 + 1] = y1;
			channelCoords[i * 4 + 2] = x;
			channelCoords[i * 4 + 3] = y2;
		}
		channelBuffer = makeFloatBuffer(channelCoords);
	}

	private void drawChannel(float channelLevel) {
		int buffNum = (int) (channelLevel * numBars) * 2;		
		if (channelLevel < .33) {
			gl.glColor4f(0, 1, 0, 1); // green lines
			gl.glDrawArrays(GL10.GL_LINES, 0, buffNum);
		} else if (channelLevel < .66) {
			gl.glColor4f(0, 1, 0, 1); // green lines
			gl.glDrawArrays(GL10.GL_LINES, 0, channelBuffer.capacity() / 6);
			gl.glColor4f(1, 1, 0, 1); // yellow lines
			gl.glDrawArrays(GL10.GL_LINES, channelBuffer.capacity() / 6,
					buffNum - channelBuffer.capacity() / 6);
		} else {
			gl.glColor4f(0, 1, 0, 1); // green lines
			gl.glDrawArrays(GL10.GL_LINES, 0, channelBuffer.capacity() / 6);
			gl.glColor4f(1, 1, 0, 1); // yellow lines
			gl.glDrawArrays(GL10.GL_LINES, channelBuffer.capacity() / 6, channelBuffer.capacity() / 6);
			gl.glColor4f(1, 0, 0, 1); // red lines
			gl.glDrawArrays(GL10.GL_LINES, channelBuffer.capacity() / 3,
					buffNum - channelBuffer.capacity()/3);
		}
		gl.glColor4f(.6f, .6f, .6f, 1);
		gl.glDrawArrays(GL10.GL_LINES, buffNum, channelBuffer.capacity()/2 - buffNum);		
	}

	private void drawChannels() {
		gl.glLineWidth(barWidth * .75f);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, channelBuffer);
		gl.glPushMatrix();
		// draw channel lines
		gl.glTranslatef(0, 5, 0);
		drawChannel(channelLevel);		
		gl.glPopMatrix();
	}

	private void initThresholdBar() {
		float x1 = threshold*width;
		float x2 = x1 + thumbWidth;
		float y1 = 0;
		float y2 = height;
		thresholdBarBuffer = makeRectFloatBuffer(x1, y1, x2, y2);
		thresholdLineBuffer = makeRectOutlineFloatBuffer(x1, y1, x2, y2);
	}

	private void drawThresholdBar() {
		drawTriangleStrip(thresholdBarBuffer, LINE_COLOR);
		drawLines(thresholdLineBuffer, LINE_OUTLINE_COLOR, 4, GL10.GL_LINE_LOOP);
	}

	@Override
	protected void init() {
		barWidth = width / numBars;
		initChannelBuffers();
		initThresholdBar();
	}

	private float dbToUnit(float db) {
		// db range = -60 - 0, need range 0-1
		return db <= -60 ? 0 : db/60 + 1;
	}
	
	private float clipToUnit(float x) {
		return x >= 0 ? (x <= 1 ? x : 1) : 0;
	}

	@Override
	protected void drawFrame() {
		drawChannels();
		drawThresholdBar();
		dampChannelLevel();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		super.surfaceChanged(holder, format, width, height);
		barWidth = width / numBars;
		initChannelBuffers();
		initThresholdBar();		
	}
	
	private short dbToShort(float db) {
		return (short)(32768*Math.pow(10, db/20));
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		setThreshold(x / width);		
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x, float y) {
		return; // not handled
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		setThreshold(e.getX(0) / width);		
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {		
		return; // not handled
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		return; // not handled
	}	
}