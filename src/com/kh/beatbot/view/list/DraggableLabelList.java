package com.kh.beatbot.view.list;

import com.kh.beatbot.listener.DraggableLabelListListener;
import com.kh.beatbot.view.TouchableSurfaceView;

public class DraggableLabelList extends LabelList {
	private int initialTouchedPosition;
	private float dragOffset = 0;
	
	public DraggableLabelList(TouchableSurfaceView parent) {
		super(parent);
	}

	@Override
	protected void touchLabel(Label label, float pointerX) {
		super.touchLabel(label, pointerX);
		dragOffset = touchedLabel.x - pointerX;
		initialTouchedPosition = labels.indexOf(touchedLabel);
	}
	
	@Override
	protected void handleActionMove(int id, float x, float y) {
		super.handleActionMove(id, x, y);
		if (touchedLabel == null || id != 0) {
			return;
		}
		touchedLabel.x = x + dragOffset;
		updateLabelLocations();
	}
	
	@Override
	protected void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
		// notify listener of the touched label's old and new position in list
		int newPosition = labels.indexOf(touchedLabel);
		if (newPosition != initialTouchedPosition) {
			((DraggableLabelListListener)listener).labelMoved(initialTouchedPosition, newPosition);
		}
		touchedLabel = null;
		updateLabelLocations();
	}
}
