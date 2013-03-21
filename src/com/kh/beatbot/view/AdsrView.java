package com.kh.beatbot.view;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.GeneralUtils;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.global.Track;
import com.kh.beatbot.manager.TrackManager;

public class AdsrView extends TouchableBBView {

	private static final int SNAP_DIST_SQUARED = 1024;
	private static float[] pointVertices = new float[10];
	private static FloatBuffer adsrPointVb = null;
	private static FloatBuffer[] adsrCurveVb = new FloatBuffer[4];
	private static ViewRect viewRect;
	// keep track of which pointer ids are selecting which ADSR points
	// init to -1 to indicate no pointer is selecting
	private int[] adsrSelected = new int[] { -1, -1, -1, -1, -1 };

	public AdsrView(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public void update() {
		initAdsrVb();
	}

	private void initAdsrVb() {
		if (viewRect == null)
			return;
		Track track = TrackManager.currTrack;
		float attackX = getAttackX(track.adsr);
		float decayX = getDecayX(track.adsr);
		pointVertices[0] = viewRect.viewX(0);
		pointVertices[1] = viewRect.viewY(track.adsr.getStart());
		pointVertices[2] = attackX;
		pointVertices[3] = viewRect.viewY(track.adsr.getPeak());
		pointVertices[4] = decayX;
		pointVertices[5] = viewRect.viewY(track.adsr.getSustain());
		pointVertices[6] = viewRect.viewX(2f / 3f); // fixed x for release begin
		pointVertices[7] = viewRect.viewY(track.adsr.getSustain());
		pointVertices[8] = getReleaseX(track.adsr);
		pointVertices[9] = viewRect.viewY(0);

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

	private float getAttackX(ADSR adsr) {
		return viewRect.viewX(adsr.getAttack() / 3f);
	}

	private float getDecayX(ADSR adsr) {
		return getAttackX(adsr) + adsr.getDecay() * viewRect.width / 3f;
	}

	private float getReleaseX(ADSR adsr) {
		return viewRect.viewX(2f / 3f + adsr.getRelease() / 3f);
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
		drawPoint(viewRect.borderRadius / 2, Colors.VOLUME, adsrPointVb.get(0),
				adsrPointVb.get(1));
		drawPoint(viewRect.borderRadius / 2, Colors.VOLUME, adsrPointVb.get(2),
				adsrPointVb.get(3));
		drawPoint(viewRect.borderRadius / 2, Colors.VOLUME, adsrPointVb.get(4),
				adsrPointVb.get(5));
		drawPoint(viewRect.borderRadius / 2, Colors.VOLUME, adsrPointVb.get(8),
				adsrPointVb.get(9));
		for (int i = 0; i < 5; i++) {
			if (adsrSelected[i] != -1) {
				drawPoint(viewRect.borderRadius, Colors.VOLUME_SELECTED,
						adsrPointVb.get(i * 2), adsrPointVb.get(i * 2 + 1));
			}
		}
		for (int i = 0; i < adsrCurveVb.length; i++) {
			drawLines(adsrCurveVb[i], Colors.VOLUME, 3, GL10.GL_LINE_STRIP);
		}
	}

	private float getAttackX() {
		return pointVertices[2]; // x coord of 2nd point
	}

	private float xToAttack(float x) {
		float unitX = viewRect.unitX(x) * 3;
		return unitX > 0 ? (unitX < 1 ? unitX : 1) : 0;
	}

	private float xToDecay(float x) {
		float decay = viewRect.unitX(x - getAttackX()) * 3;
		return decay > 0 ? (decay < 1 ? decay : 1) : 0;
	}

	private float xToRelease(float x) {
		float unitX = (viewRect.unitX(x) - 2f / 3f) * 3;
		return unitX > 0 ? (unitX < 1 ? unitX : 1) : 0;
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

	private void moveAdsrPoint(int id, float x, float y) {
		for (int i = 0; i < adsrSelected.length; i++) {
			if (adsrSelected[i] == id) {
				switch (i) {
				case 0: // start level - only moves along y axis, always x == 0
					TrackManager.currTrack.adsr.setStart(viewRect.unitY(y));
					break;
				case 1: // attack point - controls attack time and peak value
					TrackManager.currTrack.adsr.setAttack(xToAttack(x));
					TrackManager.currTrack.adsr.setPeak(viewRect.unitY(y));
					break;
				case 2: // decay point - controls decay time and sustain level
					TrackManager.currTrack.adsr.setDecay(xToDecay(x));
					TrackManager.currTrack.adsr.setSustain(viewRect.unitY(y));
					break;
				case 3: // beginning of release - user cannot set.
					break;
				case 4: // release time - y == 0 always.
					TrackManager.currTrack.adsr.setRelease(xToRelease(x));
					break;
				}
				initAdsrVb();
				GlobalVars.mainPage.pageSelectGroup.updateAdsrPage();
				return;
			}
		}
	}

	private void selectAdsrPoint(int id, float x, float y) {
		for (int i = 0; i < 5; i++) {
			if (i == 3)
				// cannot be set by user (beginning of release / end of sustain)
				continue;
			if (GeneralUtils.distanceFromPointSquared(pointVertices[i * 2],
					pointVertices[i * 2 + 1], x, y) < SNAP_DIST_SQUARED) {
				adsrSelected[i] = id;
				return;
			}
		}
	}

	@Override
	public void init() {
		viewRect = new ViewRect(width, height, 0.12f, 6);
		initAdsrVb();
		ADSR adsr = TrackManager.currTrack.adsr;
		for (int i = 0; i < adsr.getNumParams(); i++) {
			GLSurfaceViewBase.storeText(adsr.getParam(i).name);
		}
	}

	@Override
	protected void loadIcons() {

	}

	@Override
	public void draw() {
		viewRect.drawRoundedBg();
		viewRect.drawRoundedBgOutline();
		drawAdsr();
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		selectAdsrPoint(id, viewRect.clipX(x), viewRect.clipY(y));
	}

	@Override
	protected void handleActionPointerDown(int id, float x, float y) {
		selectAdsrPoint(id, viewRect.clipX(x), viewRect.clipY(y));
	}

	@Override
	protected void handleActionMove(int id, float x, float y) {
		moveAdsrPoint(id, viewRect.clipX(x), viewRect.clipY(y));
	}

	@Override
	protected void handleActionPointerUp(int id, float x, float y) {
		deselectAdsrPoint(id);
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		clearAdsrSelected();
	}

	@Override
	protected void createChildren() {
		// leaf child
	}

	@Override
	public void layoutChildren() {
		// leaf child
	}
}
