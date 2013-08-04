package com.kh.beatbot.ui.view;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.Track;
import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.page.Page;

public class AdsrView extends TouchableView {

	private static final int SNAP_DIST_SQUARED = 1024;
	private static float[] pointVertices = new float[10];
	private static FloatBuffer adsrPointVb = null;
	private static FloatBuffer adsrCurveVb;

	// keep track of which pointer ids are selecting which ADSR points
	// init to -1 to indicate no pointer is selecting
	private int[] adsrSelected = new int[] { -1, -1, -1, -1, -1 };

	public void update() {
		initAdsrVb();
	}

	private void initAdsrVb() {
		Track track = TrackManager.currTrack;
		float attackX = getAttackX(track.adsr);
		float decayX = getDecayX(track.adsr);
		pointVertices[0] = viewX(0);
		pointVertices[1] = viewY(track.adsr.getStart());
		pointVertices[2] = attackX;
		pointVertices[3] = viewY(track.adsr.getPeak());
		pointVertices[4] = decayX;
		pointVertices[5] = viewY(track.adsr.getSustain());
		pointVertices[6] = viewX(2f / 3f); // fixed x for release begin
		pointVertices[7] = viewY(track.adsr.getSustain());
		pointVertices[8] = getReleaseX(track.adsr);
		pointVertices[9] = viewY(0);

		adsrPointVb = makeFloatBuffer(pointVertices);
		ArrayList<Float> curveVertices = new ArrayList<Float>();
		for (int i = 0; i < 4; i++) {
			curveVertices
					.addAll(makeExponentialCurveVertices(pointVertices[i * 2],
							pointVertices[i * 2 + 1],
							pointVertices[(i + 1) * 2],
							pointVertices[(i + 1) * 2 + 1]));
		}
		float[] converted = new float[curveVertices.size()];
		for (int j = 0; j < curveVertices.size(); j++) {
			converted[j] = curveVertices.get(j);
		}
		adsrCurveVb = makeFloatBuffer(converted);
	}

	private float getAttackX(ADSR adsr) {
		return viewX(adsr.getAttack() / 3f);
	}

	private float getDecayX(ADSR adsr) {
		return getAttackX(adsr) + adsr.getDecay() * borderWidth / 3f;
	}

	private float getReleaseX(ADSR adsr) {
		return viewX(2f / 3f + adsr.getRelease() / 3f);
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

	private float getAttackX() {
		return pointVertices[2]; // x coord of 2nd point
	}

	private float xToAttack(float x) {
		return GeneralUtils.clipToUnit(unitX(x) * 3);
	}

	private float xToDecay(float x) {
		return GeneralUtils.clipToUnit(unitX(x - getAttackX()) * 3);
	}

	private float xToRelease(float x) {
		return GeneralUtils.clipToUnit((unitX(x) - 2f / 3f) * 3);
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
					TrackManager.currTrack.adsr.setStart(unitY(y));
					break;
				case 1: // attack point - controls attack time and peak value
					TrackManager.currTrack.adsr.setAttack(xToAttack(x));
					TrackManager.currTrack.adsr.setPeak(unitY(y));
					break;
				case 2: // decay point - controls decay time and sustain level
					TrackManager.currTrack.adsr.setDecay(xToDecay(x));
					TrackManager.currTrack.adsr.setSustain(unitY(y));
					break;
				case 3: // beginning of release - user cannot set.
					break;
				case 4: // release time - y == 0 always.
					TrackManager.currTrack.adsr.setRelease(xToRelease(x));
					break;
				}
				initAdsrVb();
				Page.mainPage.pageSelectGroup.updateAdsrPage();
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
		initAdsrVb();
		ADSR adsr = TrackManager.currTrack.adsr;
		for (int i = 0; i < ADSR.NUM_PARAMS; i++) {
			GLSurfaceViewBase.storeText(adsr.getParam(i).name);
		}
	}

	@Override
	public void draw() {
		drawCircle(getBgRectRadius() / 2, Colors.VOLUME, adsrPointVb.get(0),
				adsrPointVb.get(1));
		drawCircle(getBgRectRadius() / 2, Colors.VOLUME, adsrPointVb.get(2),
				adsrPointVb.get(3));
		drawCircle(getBgRectRadius() / 2, Colors.VOLUME, adsrPointVb.get(4),
				adsrPointVb.get(5));
		drawCircle(getBgRectRadius() / 2, Colors.VOLUME, adsrPointVb.get(8),
				adsrPointVb.get(9));
		for (int i = 0; i < 5; i++) {
			if (adsrSelected[i] != -1) {
				drawCircle(getBgRectRadius(), Colors.VOLUME_SELECTED,
						adsrPointVb.get(i * 2), adsrPointVb.get(i * 2 + 1));
			}
		}
		drawLines(adsrCurveVb, Colors.VOLUME, 3, GL10.GL_LINE_STRIP);
	}

	@Override
	public void handleActionDown(int id, float x, float y) {
		selectAdsrPoint(id, clipX(x), clipY(y));
	}

	@Override
	public void handleActionPointerDown(int id, float x, float y) {
		selectAdsrPoint(id, clipX(x), clipY(y));
	}

	@Override
	public void handleActionMove(int id, float x, float y) {
		moveAdsrPoint(id, clipX(x), clipY(y));
	}

	@Override
	public void handleActionPointerUp(int id, float x, float y) {
		deselectAdsrPoint(id);
	}

	@Override
	public void handleActionUp(int id, float x, float y) {
		clearAdsrSelected();
	}

	@Override
	protected void createChildren() {
		initBgRect(null, Colors.VIEW_BG, Colors.VOLUME);
	}
}
