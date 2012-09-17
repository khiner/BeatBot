package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.manager.MidiManager;

public class BpmView extends SurfaceViewBase {
	private static final float[] SEGMENT_ON_COLOR = {1, 0, 0, 1};
	private static final float[] SEGMENT_OFF_COLOR = {1, 0, 0, .3f};
	private static final float[] SEGMENT_ON_TOUCHED_COLOR = {1, .3f, .25f, 1};
	private static final float[] SEGMENT_OFF_TOUCHED_COLOR = {1, .3f, .25f, .3f};
	private static final float INC_BPM_THRESH = 15;
	private static boolean[][] segments = new boolean[3][7];
	private static boolean touched = false;
	private static FloatBuffer longSegmentVB = null;
	private static FloatBuffer shortSegmentVB = null;

	private static float lastFrameXLoc = -1;
	private static float lastFrameYLoc = -1;
	private static float currXDragTotal = 0;
	private static float currYDragTotal = 0;
	
	public BpmView(Context c, AttributeSet as) {
		super(c, as);
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

	public static void setText(String text) {
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

	private static float[] calculateColor(boolean on) {
		if (on) {
			return touched ? SEGMENT_ON_TOUCHED_COLOR : SEGMENT_ON_COLOR;
		} else {
			return touched ? SEGMENT_OFF_TOUCHED_COLOR : SEGMENT_OFF_COLOR;
		}
	}

	@Override
	protected void init() {
		initSegmentVBs();
	}

	private void drawSegments() {
		gl.glPushMatrix();
		for (int i = 0; i < 3; i++) {
			gl.glPushMatrix();
			// long segments
			gl.glTranslatef(4, 4, 0);
			drawTriangleStrip(longSegmentVB, calculateColor(segments[i][0]));
			gl.glPushMatrix();
			gl.glTranslatef(0, (height - 9) / 2, 0);
			drawTriangleStrip(longSegmentVB, calculateColor(segments[i][1]));
			gl.glTranslatef((width - 10) / 3 - 10, -(height - 9) / 2, 0);
			drawTriangleStrip(longSegmentVB, calculateColor(segments[i][2]));
			gl.glTranslatef(0, (height - 9) / 2, 0);
			drawTriangleStrip(longSegmentVB, calculateColor(segments[i][3]));
			// short segments
			gl.glPopMatrix();
			gl.glTranslatef(1, 0, 0);
			drawTriangleStrip(shortSegmentVB, calculateColor(segments[i][4]));
			gl.glTranslatef(0, (height - 9) / 2 - 1, 0);
			drawTriangleStrip(shortSegmentVB, calculateColor(segments[i][5]));
			gl.glTranslatef(0, (height - 9) / 2, 0);
			drawTriangleStrip(shortSegmentVB, calculateColor(segments[i][6]));
			gl.glPopMatrix();
			// translate for next digit
			gl.glTranslatef((width - 8) / 3, 0, 0);
		}
		gl.glPopMatrix();
	}

	@Override
	protected void drawFrame() {
		drawSegments();
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		touched = true;
		lastFrameXLoc = x;
		lastFrameYLoc = y;
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		return; // no pointer down events
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		float x = e.getX(0);
		float y = e.getY(0);
		currXDragTotal += x - lastFrameXLoc;
		currYDragTotal += lastFrameYLoc - y;
		lastFrameXLoc = x;
		lastFrameYLoc = y;
		if (Math.abs(currYDragTotal) > INC_BPM_THRESH) {
			float newBPM = currYDragTotal < 0 ? MidiManager.getBPM() - 1 :
				MidiManager.getBPM() + 1;
			MidiManager.setBPM(newBPM);
			setText(String.valueOf((int)newBPM));
			currYDragTotal %= INC_BPM_THRESH;
		} else if (Math.abs(currXDragTotal) > INC_BPM_THRESH) {
			float newBPM = currXDragTotal < 0 ? MidiManager.getBPM() - 1 :
				MidiManager.getBPM() + 1;
			MidiManager.setBPM(newBPM);
			setText(String.valueOf((int)newBPM));
			currXDragTotal %= INC_BPM_THRESH;
		}
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		return; // no pointer up events
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		touched = false;
	}
}
