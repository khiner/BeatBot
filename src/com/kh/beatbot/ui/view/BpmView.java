package com.kh.beatbot.ui.view;

import java.nio.FloatBuffer;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.color.Colors;

public class BpmView extends ClickableView {

	private static final float INC_BPM_THRESH = 15;
	private static boolean[][] segments = new boolean[3][7];
	private static FloatBuffer longSegmentVB = null;
	private static FloatBuffer shortSegmentVB = null;

	private static float lastFrameY = -1;
	private static float currYDragTotal = 0;

	private long lastTapTime = 0;
	
	public void setBPM(float bpm) {
		setText(String.valueOf((int) MidiManager.setBPM(bpm)));
	}
	
	@Override
	public synchronized void init() {
		initSegmentVBs();
	}

	@Override
	public void draw() {
		drawSegments();
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		lastFrameY = y;
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		super.handleActionMove(id, x, y);
		if (id != 0)
			return; // only one pointer drags bpm
		currYDragTotal += lastFrameY - y;
		lastFrameY = y;
		if (Math.abs(currYDragTotal) > INC_BPM_THRESH) {
			if (currYDragTotal <= 0) {
				setBPM(MidiManager.getBPM() - 1);
			} else {
				setBPM(MidiManager.getBPM() + 1);
			}
			currYDragTotal %= INC_BPM_THRESH;
		}
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
	}
	
	@Override
	protected void singleTap(int id, float x, float y) {
		long tapTime = System.currentTimeMillis();
		float millisElapsed = tapTime - lastTapTime;
		lastTapTime = tapTime;
		float bpm = 60000 / millisElapsed;
		if (bpm <= MidiManager.MAX_BPM + 20 && bpm >= MidiManager.MIN_BPM - 20) {
			// if we are far outside of the range, don't change the tempo.
			// otherwise, midiManager will take care of clipping the result
			setBPM(bpm);
		}
	}

	@Override
	protected void doubleTap(int id, float x, float y) {
		singleTap(id, x, y);
	}

	@Override
	protected void longPress(int id, float x, float y) {
		BeatBotActivity.mainActivity.showDialog(BeatBotActivity.BPM_DIALOG_ID);
	}
	
	private void initSegmentVBs() {
		// for use with GL_TRIANGLE_FAN - first is middle, the rest are edges
		float[] longSegmentBuf = new float[] { 0, 0, -4, 4, 4, 4, -4,
				(height - 2) / 2 - 10, 4, (height - 2) / 2 - 10, 0,
				(height - 2) / 2 - 5 };
		float[] shortSegmentBuf = new float[] { 0, 0, 4, -4, 4, 4,
				(width - 8 * 5) / 3 - 7, -4, (width - 8 * 5) / 3 - 7, 4,
				(width - 8 * 5) / 3 - 2, 0 };
		longSegmentVB = makeFloatBuffer(longSegmentBuf);
		shortSegmentVB = makeFloatBuffer(shortSegmentBuf);
	}

	public void setText(String text) {
		if (text.length() > 3)
			return;
		for (int i = 0; i < 3 - text.length(); i++) {
			setSegments(i, 0); // pad with zeros
		}
		for (int i = 3 - text.length(), j = 0; i < 3; i++, j++) {
			setSegments(i, Character.digit(text.charAt(j), 10));
		}
	}

	private static void setSegments(int position, int digit) {
		switch (digit) {
		case 0:
			segments[position][0] = true;
			segments[position][1] = true;
			segments[position][2] = true;
			segments[position][3] = true;
			segments[position][4] = true;
			segments[position][5] = false;
			segments[position][6] = true;
			break;
		case 1:
			segments[position][0] = false;
			segments[position][1] = false;
			segments[position][2] = true;
			segments[position][3] = true;
			segments[position][4] = false;
			segments[position][5] = false;
			segments[position][6] = false;
			break;
		case 2:
			segments[position][0] = false;
			segments[position][1] = true;
			segments[position][2] = true;
			segments[position][3] = false;
			segments[position][4] = true;
			segments[position][5] = true;
			segments[position][6] = true;
			break;
		case 3:
			segments[position][0] = false;
			segments[position][1] = false;
			segments[position][2] = true;
			segments[position][3] = true;
			segments[position][4] = true;
			segments[position][5] = true;
			segments[position][6] = true;
			break;
		case 4:
			segments[position][0] = true;
			segments[position][1] = false;
			segments[position][2] = true;
			segments[position][3] = true;
			segments[position][4] = false;
			segments[position][5] = true;
			segments[position][6] = false;
			break;
		case 5:
			segments[position][0] = true;
			segments[position][1] = false;
			segments[position][2] = false;
			segments[position][3] = true;
			segments[position][4] = true;
			segments[position][5] = true;
			segments[position][6] = true;
			break;
		case 6:
			segments[position][0] = true;
			segments[position][1] = true;
			segments[position][2] = false;
			segments[position][3] = true;
			segments[position][4] = true;
			segments[position][5] = true;
			segments[position][6] = true;
			break;
		case 7:
			segments[position][0] = false;
			segments[position][1] = false;
			segments[position][2] = true;
			segments[position][3] = true;
			segments[position][4] = true;
			segments[position][5] = false;
			segments[position][6] = false;
			break;
		case 8:
			segments[position][0] = true;
			segments[position][1] = true;
			segments[position][2] = true;
			segments[position][3] = true;
			segments[position][4] = true;
			segments[position][5] = true;
			segments[position][6] = true;
			break;
		case 9:
			segments[position][0] = true;
			segments[position][1] = false;
			segments[position][2] = true;
			segments[position][3] = true;
			segments[position][4] = true;
			segments[position][5] = true;
			segments[position][6] = false;
			break;
		}
	}

	private void drawSegments() {
		gl.glPushMatrix();
		for (int i = 0; i < 3; i++) {
			gl.glPushMatrix();
			// long segments
			gl.glTranslatef(4, 4, 0);
			drawTriangleStrip(longSegmentVB, calculateSegmentColor(segments[i][0]));
			gl.glPushMatrix();
			gl.glTranslatef(0, (height - 9) / 2, 0);
			drawTriangleStrip(longSegmentVB, calculateSegmentColor(segments[i][1]));
			gl.glTranslatef((width - 10) / 3 - 10, -(height - 9) / 2, 0);
			drawTriangleStrip(longSegmentVB, calculateSegmentColor(segments[i][2]));
			gl.glTranslatef(0, (height - 9) / 2, 0);
			drawTriangleStrip(longSegmentVB, calculateSegmentColor(segments[i][3]));
			// short segments
			gl.glPopMatrix();
			gl.glTranslatef(1, 0, 0);
			drawTriangleStrip(shortSegmentVB, calculateSegmentColor(segments[i][4]));
			gl.glTranslatef(0, (height - 9) / 2 - 1, 0);
			drawTriangleStrip(shortSegmentVB, calculateSegmentColor(segments[i][5]));
			gl.glTranslatef(0, (height - 9) / 2, 0);
			drawTriangleStrip(shortSegmentVB, calculateSegmentColor(segments[i][6]));
			gl.glPopMatrix();
			// translate for next digit
			gl.glTranslatef((width - 8) / 3, 0, 0);
		}
		gl.glPopMatrix();
	}
	
	private float[] calculateSegmentColor(boolean on) {
		return pointerCount() > 0 ? on ? Colors.BPM_ON_SELECTED : Colors.BPM_OFF_SELECTED : on ? Colors.BPM_ON : Colors.BPM_OFF;
	}
}
