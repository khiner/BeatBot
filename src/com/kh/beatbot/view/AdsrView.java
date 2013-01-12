package com.kh.beatbot.view;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GeneralUtils;
import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.manager.TrackManager;

public class AdsrView extends TouchableSurfaceView {
	private static final int DRAW_OFFSET = 6;
	private static final int SNAP_DIST_SQUARED = 1024;
	private static final float[] ADSR_COLOR = Colors.VOLUME_COLOR.clone();
	private static final float[] ADSR_SELECTED_COLOR = { ADSR_COLOR[0],
			ADSR_COLOR[1], ADSR_COLOR[2], .6f };
	private static final float ADSR_POINT_RADIUS = 5;
	private static FloatBuffer adsrPointVb = null;
	private static FloatBuffer[] adsrCurveVb = new FloatBuffer[4];
	private static ViewRect viewRect;
	private static FloatBuffer borderVb = null;
	// keep track of which pointer ids are selecting which ADSR points
	// init to -1 to indicate no pointer is selecting
	private int[] adsrSelected = new int[] { -1, -1, -1, -1, -1 };
	
	public AdsrView(Context c, AttributeSet as) {
		super(c, as);
	}

	private void initBorderVb() {
		viewRect = new ViewRect(width, height, 0.18f);
		borderVb = makeRoundedCornerRectBuffer(width - DRAW_OFFSET * 2, height
				- DRAW_OFFSET * 2, viewRect.borderRadius, 25);
	}
	
	private void initAdsrVb() {
		float[] pointVertices = new float[10];
		
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 2; j++) {
				pointVertices[i * 2 + j] = j == 1 ? adsrToY(TrackManager.currTrack.getAdsrY(i))
						: adsrToX(TrackManager.currTrack.getAdsrX(i));
			}
		}
		adsrPointVb = makeFloatBuffer(pointVertices);
		for (int i = 0; i < 4; i++) {
			ArrayList<Float> curveVertices = new ArrayList<Float>();
			curveVertices
					.addAll(makeExponentialCurveVertices(pointVertices[i * 2],
							pointVertices[i * 2 + 1],
							pointVertices[(i + 1) * 2],
							pointVertices[(i + 1) * 2 + 1]));
			float[] converted = new float[curveVertices.size()];
			for (int j = 0; j < curveVertices.size(); j++) {
				converted[j] = curveVertices.get(j);
			}
			adsrCurveVb[i] = makeFloatBuffer(converted);
		}
	}
	
	private ArrayList<Float> makeExponentialCurveVertices(float x1, float y1,
			float x2, float y2) {
		ArrayList<Float> vertices = new ArrayList<Float>();
		// fake it w/ Bezier curve
		for (float t = 0; t <= 1; t += 0.05) {
			float bezierX = x1;
			float bezierY = y2;
			vertices.add((1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * bezierX + t
					* t * x2);
			vertices.add((1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * bezierY + t
					* t * y2);
		}
		vertices.add(x2);
		vertices.add(y2);
		return vertices;
	}
	
	private void drawAdsr() {
		setColor(ADSR_COLOR);
		gl.glPointSize(ADSR_POINT_RADIUS * 2);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, adsrPointVb);
		gl.glDrawArrays(GL10.GL_POINTS, 0, 3);
		gl.glDrawArrays(GL10.GL_POINTS, 4, 1);
		for (int i = 0; i < 5; i++) {
			if (adsrSelected[i] != -1) {
				gl.glPointSize(ADSR_POINT_RADIUS * 4);
				setColor(ADSR_SELECTED_COLOR);
				gl.glDrawArrays(GL10.GL_POINTS, i, 1);
			}
		}
		for (int i = 0; i < adsrCurveVb.length; i++) {
			drawLines(adsrCurveVb[i], ADSR_COLOR, 3, GL10.GL_LINE_STRIP);
		}
	}

	private float adsrToX(float adsr) {
		return adsr * width;
	}
	
	private float adsrToY(float adsr) {
		return -(adsr - 1) * (height - 2 * ADSR_POINT_RADIUS)
				+ ADSR_POINT_RADIUS;
	}

	private float xToAdsr(float x) {
		return x / width;
	}

	private float yToAdsr(float y) {
		// clip y to half an adsr circle above 0 and half a circle below height
		y = y > ADSR_POINT_RADIUS ? (y < height - ADSR_POINT_RADIUS ? y
				: height - ADSR_POINT_RADIUS) : ADSR_POINT_RADIUS;
		return 1 - (y - ADSR_POINT_RADIUS) / (height - 2 * ADSR_POINT_RADIUS);
	}
	
	private void deselectAdsrPoint(int id) {
		for (int i = 0; i < adsrSelected.length; i++) {
			if (adsrSelected[i] == id)
				adsrSelected[i] = -1;
		}
	}

	private void clearAdsrSelected() {
		for (int i = 0; i < adsrSelected.length; i++) {
			adsrSelected[i] = -1;
		}
	}

	private boolean moveAdsrPoint(int id, float x, float y) {
		for (int i = 0; i < adsrSelected.length; i++) {
			if (adsrSelected[i] == id) {
				float adsrX = xToAdsr(x);
				float adsrY = yToAdsr(y);
				float prevX = i >= 2 ? TrackManager.currTrack.getAdsrX(i - 1)
						: 0;
				float nextX = i <= 3 ? TrackManager.currTrack.getAdsrX(i + 1)
						: 1;
				if (i == 0)
					adsrX = 0;
				// ADSR samples cannot go past the next ADSR sample or before
				// the previous sample
				TrackManager.currTrack.setAdsrX(i, adsrX > prevX ? (adsrX < nextX ? adsrX
						: nextX )
						: prevX);
				if (i != 3) // can only change the x coord of the cutoff point
					TrackManager.currTrack.setAdsrY(i, adsrY > 0 ? (adsrY < 1 ? adsrY
							: 1)
							: 0);
				// points 2 and 3 must have the same y value, since these are
				// the two ends
				// of the sustain level, which must be linear.
				// ie. adjusting either 2 or 3 will adjust both points' y values
				if (i == 2)
					TrackManager.currTrack.setAdsrY(3, TrackManager.currTrack.getAdsrY(2));
				initAdsrVb();
				return true;
			}
		}
		return false;
	}
	
	private boolean selectAdsrPoint(int id, float x, float y) {
		for (int i = 0; i < 5; i++) {
			if (i == 3)
				continue; // release point not user changeable
			if (GeneralUtils.distanceFromPointSquared(
					adsrToX(TrackManager.currTrack.getAdsrX(i)),
					adsrToY(TrackManager.currTrack.getAdsrY(i)), x, y) < SNAP_DIST_SQUARED) {
				adsrSelected[i] = id;
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void init() {
		initBorderVb();
		initAdsrVb();
	}

	@Override
	protected void loadIcons() {
		
	}
	
	private void drawRoundedBg() {
		gl.glTranslatef(width / 2, height / 2, 0);
		drawTriangleFan(borderVb, LevelListenable.BG_COLOR);
		drawLines(borderVb, Colors.VOLUME_COLOR, 5, GL10.GL_LINE_LOOP);
		gl.glTranslatef(-width / 2, -height / 2, 0);
	}
	
	@Override
	protected void drawFrame() {
		drawRoundedBg();
		drawAdsr();
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		selectAdsrPoint(id, x, y);
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		selectAdsrPoint(id, x, y);
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		for (int i = 0; i < e.getPointerCount(); i++) {
			int id = e.getPointerId(i);
			if (moveAdsrPoint(id, e.getX(i), e.getY(i))) {
				initAdsrVb();
			}
		}
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		deselectAdsrPoint(id);
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		clearAdsrSelected();
	}
}
