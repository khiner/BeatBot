package com.kh.beatbot.views;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;


import android.content.Context;
import android.util.AttributeSet;

public class BpmView extends SurfaceViewBase {
	private boolean[][] segments = new boolean[3][7];
	
	FloatBuffer longSegmentVB = null;
	FloatBuffer shortSegmentVB = null;

	public BpmView(Context c, AttributeSet as) {
		super(c, as);
	}

	private void initSegmentVBs() {
		// for use with GL_TRIANGLE_FAN - first is middle, the rest are edges
		float[] longSegmentBuf = new float[] {0, 0, -4, 4, 4, 4, -4, (height - 2)/2 - 10, 4, (height - 2)/2 - 10, 0, (height - 2)/2 - 5};
		float[] shortSegmentBuf = new float[] {0, 0, 4, -4, 4, 4, (width - 8*5)/3 - 7, -4, (width - 8*5)/3 - 7, 4, (width - 8*5)/3 - 2, 0};
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
	
	private void setSegments(int position, int digit) {
		switch(digit) {
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
	
	private void setColor(boolean on) {
		if (on)
			gl.glColor4f(1, 0, 0, 1);
		else
			gl.glColor4f(1, 0, 0, .3f);
	}
	
	@Override
	protected void init() {
		gl.glClearColor(0, 0, 0, 1);		
		initSegmentVBs();		
	}

	private void drawSegments() {
		gl.glPushMatrix();
		for (int i = 0; i < 3; i++) {
			gl.glPushMatrix();
			gl.glTranslatef(4, 4, 0);
			setColor(segments[i][0]);
			drawLongSegment();
			gl.glPushMatrix();
			gl.glTranslatef(0, (height - 9)/2, 0);
			setColor(segments[i][1]);
			drawLongSegment();
			gl.glTranslatef((width - 10)/3 - 10, -(height - 9)/2, 0);
			setColor(segments[i][2]);
			drawLongSegment();
			gl.glTranslatef(0, (height - 9)/2, 0);
			setColor(segments[i][3]);
			drawLongSegment();
			gl.glPopMatrix();
			gl.glTranslatef(1, 0, 0);
			setColor(segments[i][4]);
			drawShortSegment();
			gl.glTranslatef(0, (height - 9)/2 - 1, 0);
			setColor(segments[i][5]);
			drawShortSegment();
			gl.glTranslatef(0, (height - 9)/2, 0);
			setColor(segments[i][6]);
			drawShortSegment();	
			gl.glPopMatrix();
			// translate for next digit
			gl.glTranslatef((width - 8)/3, 0, 0);			
		}
		gl.glPopMatrix();		
	}

	private void drawLongSegment() {
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, longSegmentVB);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 6);		
	}

	private void drawShortSegment() {
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, shortSegmentVB);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 6);
	}
	
	@Override
	protected void drawFrame() {		
		drawSegments();
	}

}
