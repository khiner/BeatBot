package com.kh.beatbot.view;

import com.kh.beatbot.global.GlobalVars;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class EffectControlView extends SurfaceViewBase {
	/* Struct to hold the effect id for native effects and label image id,
	 * position, dragged state
	 */
	private class EffectLabel {
		private int effectId, imageId;
		private float x, y, width, height;
		
		EffectLabel(int effectId, float x, float y) {
			imageId = EMPTY_EFFECT_LABEL_ID; // 
			this.imageId = imageId;
			this.x = x;
			this.y = y;
		}
		
		public boolean isPointInsideLabel(float x, float y) {
			return x > this.x && x < this.x + width && y > this.y && y < this.y + height;
		}
		
		public void setLocation(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}
	
	/*
	 * Find the effect such that the given coordinates are inside
	 */
	private EffectLabel findEffectLabel(float x, float y) {
		for (EffectLabel effectLabel : effectLabels) {
			if (effectLabel.isPointInsideLabel(x, y)) {
				return effectLabel;
			}
		}
		return null;
	}
	
	private static final int EMPTY_EFFECT_LABEL_ID = 0;
	private static long LAST_DOWN_TIME = -1;
	// which label is currently being dragged? (null for none)
	private static EffectLabel draggedEffectLabel = null;
	
	// 4 effect labels on the screen 
	private static EffectLabel[] effectLabels = new EffectLabel[4];
	
	public EffectControlView(Context c, AttributeSet as) {
		super(c, as);
	}
	

	@Override
	protected void init() {
		// TODO initialize effect labels to default positions
	}

	@Override
	protected void drawFrame() {
		// TODO draw each effect + optional dragged effect in its current position
		
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		LAST_DOWN_TIME = System.currentTimeMillis();
		// single tap will select the effect slot
		// clicking on an existing effect will select effect.
		for (EffectLabel effectLabel : effectLabels) {
			if (effectLabel.isPointInsideLabel(x, y)) {
				draggedEffectLabel = effectLabel;
			}
		}
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		// no pointer for now - only handle one
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		// if effect being dragged, update position
		if (draggedEffectLabel == null) {
			return;
		}
		draggedEffectLabel.setLocation(e.getX(0), e.getY(0));
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		// nothing for now
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		// if single tap:
		//		open effect label view activity
		// if effect being dragged:
		//      if new loc is in effect bin:
		//    		drop in the bin.
		//		else:
		//      	remove effect
		if (System.currentTimeMillis() - LAST_DOWN_TIME < GlobalVars.SINGLE_TAP_TIME) {
			// TODO launch effect label activity
			return;
		}
		if (draggedEffectLabel != null) {
			EffectLabel droppedInLabel = findEffectLabel(x, y);
			if (droppedInLabel != null) { // drop the label in the bin
				droppedInLabel.effectId = draggedEffectLabel.effectId;
				droppedInLabel.imageId = draggedEffectLabel.imageId;
				// TODO call native effect ordering method
			} else {
				// label dragged and dropped, but not in bin - remove effect
				// TODO call native effect remove method
			}
			draggedEffectLabel = null;
		}
	}
}
