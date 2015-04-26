package com.kh.beatbot.ui.view;

import com.kh.beatbot.effect.ADSR;
import com.kh.beatbot.effect.Param;
import com.kh.beatbot.listener.ParamListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.util.GeneralUtils;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.color.Color;
import com.kh.beatbot.ui.shape.AdsrShape;

public class AdsrView extends TouchableView implements ParamListener {

	private static final int NUM_VERTICES = 5;
	private static final int SNAP_DIST_SQUARED = 1024;
	private static final float[] POINT_VERTICES = new float[NUM_VERTICES * 2];

	// keep track of which pointer ids are selecting which ADSR points
	// init to -1 to indicate no pointer is selecting
	private int[] adsrSelected = new int[] { -1, -1, -1, -1 };

	private AdsrShape adsrShape;

	public AdsrView(View view) {
		super(view);
	}

	public synchronized void onSelect(Track track) {
		for (int i = 0; i < ADSR.NUM_PARAMS; i++) {
			track.getAdsrParam(i).removeListener(this);
			track.getAdsrParam(i).addListener(this);
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
		deselectAdsrPoint(id);
	}

	@Override
	public void onParamChanged(Param param) {
		initAdsrVb();
	}

	@Override
	protected synchronized void createChildren() {
		initRoundedRect();
		adsrShape = new AdsrShape(renderGroup, Color.TRON_BLUE, Color.TRON_BLUE_TRANS,
				Color.TRON_BLUE);

		addShapes(adsrShape);
	}

	@Override
	public synchronized void layoutChildren() {
		adsrShape.layout(absoluteX, absoluteY, width, height);
	}

	private void initAdsrVb() {
		Track track = TrackManager.currTrack;
		float attackX = getAttackX(track.getAdsr());
		float decayX = getDecayX(track.getAdsr());
		POINT_VERTICES[0] = viewX(0);
		POINT_VERTICES[1] = viewY(track.getAdsr().getStart());
		POINT_VERTICES[2] = attackX;
		POINT_VERTICES[3] = viewY(track.getAdsr().getPeak());
		POINT_VERTICES[4] = decayX;
		POINT_VERTICES[5] = viewY(track.getAdsr().getSustain());
		POINT_VERTICES[6] = viewX(2f / 3f); // fixed x for release begin
		POINT_VERTICES[7] = viewY(track.getAdsr().getSustain());
		POINT_VERTICES[8] = getReleaseX(track.getAdsr());
		POINT_VERTICES[9] = viewY(0);

		adsrShape.update(POINT_VERTICES);
	}

	private float getAttackX(ADSR adsr) {
		return viewX(adsr.getAttack() / 3f);
	}

	private float getDecayX(ADSR adsr) {
		return getAttackX(adsr) + viewX(adsr.getDecay() / 3f);
	}

	private float getReleaseX(ADSR adsr) {
		return viewX(2f / 3f + adsr.getRelease() / 3f);
	}

	private float xToAttack(float x) {
		return GeneralUtils.clipToUnit(unitX(x) * 3);
	}

	private float xToDecay(float x) {
		return GeneralUtils.clipToUnit(unitX(x - POINT_VERTICES[2]) * 3);
	}

	private float xToRelease(float x) {
		return GeneralUtils.clipToUnit((unitX(x) - 2f / 3f) * 3);
	}

	private void selectAdsrPoint(int id, float x, float y) {
		for (int i = 0; i < NUM_VERTICES; i++) {
			if (i == 3)
				// cannot be set by user (beginning of release / end of sustain)
				continue;
			if (GeneralUtils.distanceFromPointSquared(POINT_VERTICES[i * 2],
					POINT_VERTICES[i * 2 + 1], x, y) < SNAP_DIST_SQUARED) {
				int selectIndex = vertexIndexToSelectIndex(i);
				adsrSelected[selectIndex] = id;
				adsrShape.select(selectIndex);
				return;
			}
		}
	}

	private void deselectAdsrPoint(int id) {
		for (int i = 0; i < adsrSelected.length; i++) {
			if (adsrSelected[i] == id) {
				adsrSelected[i] = -1;
				adsrShape.deselect(i);
				return;
			}
		}
	}

	private void moveAdsrPoint(int id, float x, float y) {
		for (int i = 0; i < adsrSelected.length; i++) {
			ADSR adsr = TrackManager.currTrack.getAdsr();
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
				case 3: // release time - y == 0 always.
					adsr.setRelease(xToRelease(x));
					break;
				}
				return;
			}
		}
	}

	private int vertexIndexToSelectIndex(int vertexIndex) {
		return vertexIndex > 3 ? vertexIndex - 1 : vertexIndex;
	}
}
