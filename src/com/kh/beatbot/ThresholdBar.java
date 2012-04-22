package com.kh.beatbot;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLU;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class ThresholdBar extends SurfaceViewBase {

	private final int numBars = 75; 
	private final float thumbWidth = 15;
	
	FloatBuffer channelBuffer;
	FloatBuffer thresholdBarBuffer;
	FloatBuffer thresholdLineBuffer;

	private int barWidth;

	private float threshold = 0.35f;
	private float[] channelLevels = new float[] { 0 , 0 };

	public ThresholdBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public float getThreshold() {
		return threshold;
	}

	public void setThreshold(float threshold) {
		this.threshold = clipToUnit(threshold);
	}

	public void setChannelLevels(float channel1Lvl, float channel2Lvl) {
		// clip the channels to range (0, 1)
		channelLevels[0] = clipToUnit(channel1Lvl);
		channelLevels[1] = clipToUnit(channel2Lvl);
	}

	private void initChannelBuffers() {
		// only 1/3 of the lines are initialized, so they can be
		// drawn all together as one
		// color. to draw more lines with a different color, simply translate to
		// the right and change the color
		// (numBars/3 lines per channel) X (4 coordinates per line)
		float[] channelCoords = new float[(numBars*4)];
		int y1 = 0;
		int y2 = height / 4;
		for (int x = 0, i = 0; i < numBars; x += barWidth, i++) {
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
		gl.glColor4f(.5f, .5f, .5f, .7f);
		gl.glDrawArrays(GL10.GL_LINES, buffNum, channelBuffer.capacity()/2 - buffNum);		
	}

	private void drawChannels() {
		gl.glLineWidth(barWidth * .75f);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, channelBuffer);

		gl.glPushMatrix();
		
		// draw channel 0		
		gl.glTranslatef(0, 5, 0);
		drawChannel(channelLevels[0]);
		// draw channel 1
		gl.glTranslatef(0, height / 4 + 5, 0);
		drawChannel(channelLevels[1]);
		
		gl.glPopMatrix();
	}

	private void initThresholdBar() {
		float x1 = threshold*width;
		float x2 = x1 + thumbWidth;
		float y1 = 0;
		float y2 = height;
		thresholdBarBuffer = makeFloatBuffer(new float[] { x1, y1, x2, y1, x1, y2, x2,
				y2 });
		thresholdLineBuffer = makeFloatBuffer(new float[] { x1, y1, x2, y1, x2, y2, x1, y2, x1, y2 });		
	}

	private void drawThresholdBar() {
		gl.glColor4f(.9f, .9f, .9f, .65f);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, thresholdBarBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		gl.glColor4f(1, 1, 1, .9f);
		gl.glLineWidth(4);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, thresholdLineBuffer);
		gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, thresholdLineBuffer.capacity()/2);
	}

	@Override
	protected void init() {
		gl.glClearColor(0, 0, 0, 1);
		barWidth = width / numBars;
		initChannelBuffers();
		initThresholdBar();
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
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glViewport(0, 0, width, height);
		gl.glLoadIdentity();
		GLU.gluOrtho2D(gl, 0, width, height, 0);
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

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			setThreshold(e.getX() / width);
			break;
		case MotionEvent.ACTION_MOVE:
			setThreshold(e.getX() / width);
			break;
		}
		return true;
	}
}