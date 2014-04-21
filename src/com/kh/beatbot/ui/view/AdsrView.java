package com.kh.beatbot.ui.view;

import com.kh.beatbot.GeneralUtils;
import com.kh.beatbot.Track;
import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.AdsrShape;
import com.kh.beatbot.ui.shape.RenderGroup;

public class AdsrView extends TouchableView implements ParamListener {

	private static final int SNAP_DIST_SQUARED = 1024;
	private static float[] pointVertices = new float[10];

	// keep track of which pointer ids are selecting which ADSR points
	// init to -1 to indicate no pointer is selecting
	private int[] adsrSelected = new int[] { -1, -1, -1, -1, -1 };

	private AdsrShape adsrShape;

	public AdsrView(RenderGroup renderGroup) {
		super(renderGroup);
	}

	public synchronized void onSelect(Track track) {
		for (int i = 0; i < ADSR.NUM_PARAMS; i++) {
			track.adsr.getParam(i).removeListener(this);
			track.adsr.getParam(i).addListener(this);
		}
		initAdsrVb();
	}

	@Override
	public void handleActionDown(int id, Pointer pos) {
		super.handleActionDown(id, pos);
		selectAdsrPoint(id, clipX(pos.x), clipY(pos.y));
	}

	@Override
	public void handleActionPointerDown(int id, Pointer pos) {
		selectAdsrPoint(id, clipX(pos.x), clipY(pos.y));
	}

	@Override
	public void handleActionMove(int id, Pointer pos) {
		moveAdsrPoint(id, clipX(pos.x), clipY(pos.y));
	}

	@Override
	public void handleActionPointerUp(int id, Pointer pos) {
		deselectAdsrPoint(id);
	}

	@Override
	public void handleActionUp(int id, Pointer pos) {
		super.handleActionUp(id, pos);
		clearAdsrSelected();
	}

	@Override
	public void onParamChanged(Param param) {
		initAdsrVb();
	}

	@Override
	protected synchronized void createChildren() {
		initRoundedRect();
		adsrShape = new AdsrShape(renderGroup, Color.TRON_BLUE, Color.TRON_BLUE);
	}

	@Override
	public synchronized void layoutChildren() {
		adsrShape.layout(absoluteX, absoluteY, width, height);
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

		adsrShape.update(pointVertices);
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

	private float xToAttack(float x) {
		return GeneralUtils.clipToUnit(unitX(x) * 3);
	}

	private float xToDecay(float x) {
		return GeneralUtils.clipToUnit(unitX(x - pointVertices[2]) * 3);
	}

	private float xToRelease(float x) {
		return GeneralUtils.clipToUnit((unitX(x) - 2f / 3f) * 3);
	}

	private void deselectAdsrPoint(int id) {
		for (int i = 0; i < adsrSelected.length; i++) {
			if (adsrSelected[i] == id) {
				adsrSelected[i] = -1;
				return;
			}
		}
	}

	private void clearAdsrSelected() {
		for (int i = 0; i < adsrSelected.length; i++) {
			adsrSelected[i] = -1;
		}
	}

	private void moveAdsrPoint(int id, float x, float y) {
		for (int i = 0; i < adsrSelected.length; i++) {
			ADSR adsr = TrackManager.currTrack.adsr;
			if (adsrSelected[i] == id) {
				switch (i) {
				case 0: // start level - only moves along y axis, always x == 0
					adsr.setStart(unitY(y));
					break;
				case 1: // attack point - controls attack time and peak value
					adsr.setAttack(xToAttack(x));
					adsr.setPeak(unitY(y));
					break;
				case 2: // decay point - controls decay time and sustain level
					adsr.setDecay(xToDecay(x));
					adsr.setSustain(unitY(y));
					break;
				case 3: // beginning of release - user cannot set.
					break;
				case 4: // release time - y == 0 always.
					adsr.setRelease(xToRelease(x));
					break;
				}
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
}
