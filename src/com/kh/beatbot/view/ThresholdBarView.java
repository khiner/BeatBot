package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class ThresholdBarView extends SurfaceViewBase {

	private final float[] LINE_COLOR = {.9f, .9f, .9f, .65f};
	private final float[] LINE_OUTLINE_COLOR = {1, 1, 1, 1};
	
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
	}

	public void setChannelLevel(float channelDb) {
		// map channel DB to range (0, 1)
		channelLevel = dbToUnit(channelDb);
		// Log.d("channel level", String.valueOf(channelLevels[0]));
	}

	private void initChannelBuffers() {
		// only 1/3 of the lines are initialized, so they can be
		// drawn all together as one
		// color. to draw more lines with a different color, simply translate to
		// the right and change the color
		// (numBars/3 lines per channel) X (4 coordinates per line)
		float[] channelCoords = new float[(numBars*4)];
		int y1 = 0;
		int y2 = 3*height / 4;
		for (int x = barWidth/2, i = 0; i < numBars; x += barWidth, i++) {
			channelCoords[i * 4] = x;
			channelCoords[i * 4 + 1] = y1;
			channelCoords[i * 4 + 2] = x;
			channelCoords[i * 4 + 3] = y2;
		}
		// the float buffer for OpenGL to draw the bar coordinates
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
		gl.glClearColor(0, 0, 0, 1);
		barWidth = width / numBars;
		initChannelBuffers();
		initThresholdBar();
	}

	private float dbToUnit(float db) {
		// db range = -60 - 0, need range 0-1
		return db <= -60 ? 0 : db/60 + 1;
	}
	
	private float clipToUnit(float x) {
		if (x < 0)
			return 0;
		else if (x > 1)
			return 1;
		return x;
	}

	@Override
	protected void drawFrame() {
		drawChannels();
		drawThresholdBar();
		initThresholdBar();
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