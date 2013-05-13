package com.kh.beatbot.view.list;

import com.kh.beatbot.listener.DraggableLabelListListener;
import com.kh.beatbot.view.control.Button;

public class DraggableLabelList extends LabelList {
	
	private int initialTouchedPosition = -1;
	private float dragOffset = 0;
	private LabelState prevTouchedLabelState = LabelState.EMPTY;
	
	@Override
	public void handleActionMove(int id, float x, float y) {
		super.handleActionMove(id, x, y);
		if (touchedLabel == null || id != 0) {
			return;
		}
		touchedLabel.setPosition(x + dragOffset, touchedLabel.y);
		layoutChildren();
	}
	
	@Override
	public void onPress(Button button) {
		prevTouchedLabelState = ((Label)button).getState();
		super.onPress(button);
		initialTouchedPosition = children.indexOf(button);
		dragOffset = touchedLabel.x - this.pointerIdToPos.get(0).x;
	}
	
	@Override
	public synchronized void onRelease(Button button) {
		// notify listener of the touched label's old and new position in list
		int newPosition = children.indexOf(button);
		if (newPosition != initialTouchedPosition) {
			((DraggableLabelListListener)listener).labelMoved(initialTouchedPosition, newPosition);
		}
		touchedLabel.setState(prevTouchedLabelState);
		touchedLabel = null;
		layoutChildren();
	}
}
