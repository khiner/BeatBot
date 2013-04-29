package com.kh.beatbot.view.list;

import java.util.Collections;

import com.kh.beatbot.listener.DraggableLabelListListener;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.ToggleButton;

public class DraggableLabelList extends LabelList {
	
	private int initialTouchedPosition = -1;
	private float dragOffset = 0;
	private boolean wasTouchedLabelChecked = false;
	
	@Override
	public void handleActionMove(int id, float x, float y) {
		super.handleActionMove(id, x, y);
		if (touchedLabel == null || id != 0) {
			return;
		}
		touchedLabel.setPosition(x + dragOffset, touchedLabel.y);
		updateLabelPositions();
	}
	
	@Override
	public void onPress(Button button) {
		wasTouchedLabelChecked = ((ToggleButton)button).isChecked();
		super.onPress(button);
		initialTouchedPosition = children.indexOf(button);
		dragOffset = touchedLabel.x - this.pointerIdToPos.get(0).x;
	}
	
	@Override
	public void onRelease(Button button) {
		// notify listener of the touched label's old and new position in list
		int newPosition = children.indexOf(button);
		if (newPosition != initialTouchedPosition) {
			((DraggableLabelListListener)listener).labelMoved(initialTouchedPosition, newPosition);
		}
		touchedLabel.setChecked(wasTouchedLabelChecked);
		touchedLabel = null;
		updateLabelPositions();
	}

	protected void updateLabelPositions() {
		Collections.sort(children); // sort labels by x value
		float xTotal = 0;
		for (BBView label : children) {
			if (touchedLabel == null || !label.equals(touchedLabel)) {
				label.setPosition(xTotal, label.y);
			}
			xTotal += label.width + GAP_BETWEEN_LABELS;
		}
	}
	
	@Override
	public void layoutChildren() {
		float labelW = (width - (children.size() - 1)
				* GAP_BETWEEN_LABELS) / children.size();
		
		Collections.sort(children); // sort labels by position
		float xTotal = 0;
		for (BBView label : children) {
			if (touchedLabel == null || !label.equals(touchedLabel)) {
				label.layout(this, xTotal, 0, labelW, height);
			}
			xTotal += labelW + GAP_BETWEEN_LABELS;
		}
	}
}
