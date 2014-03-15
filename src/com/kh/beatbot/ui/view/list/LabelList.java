package com.kh.beatbot.ui.view.list;

import java.util.Collections;

import com.kh.beatbot.R;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.Color;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.IconResourceSet;
import com.kh.beatbot.ui.IconResourceSets;
import com.kh.beatbot.ui.shape.ShapeGroup;
import com.kh.beatbot.ui.view.ClickableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;

public class LabelList extends ClickableView implements OnPressListener, OnReleaseListener {

	public static enum LabelState {
		ON, OFF, EMPTY
	};

	private static final IconResourceSet onIcon = new IconResourceSet(new IconResource(-1,
			Color.TRON_BLUE, null), new IconResource(-1, Color.TRON_BLUE_LIGHT, Color.WHITE));

	private static final IconResourceSet offIcon = new IconResourceSet(new IconResource(-1,
			Color.LABEL_LIGHT, null), new IconResource(-1, Color.LABEL_VERY_LIGHT, Color.WHITE));

	private static final IconResourceSet emptyIcon = new IconResourceSet(new IconResource(
			R.drawable.plus_outline, Color.LABEL_DARK, Color.WHITE), new IconResource(
			R.drawable.plus_outline, Color.LABEL_MED, Color.WHITE));

	protected class Label extends Button {
		private final static String EMPTY_TEXT = "ADD";
		private LabelState state = LabelState.EMPTY;

		public Label(ShapeGroup shapeGroup) {
			super(shapeGroup);
			setResourceId(IconResourceSets.ADD);
			setFillColors(emptyIcon);
			setText(EMPTY_TEXT);
			shouldClip = false;
		}

		public void setState(LabelState state) {
			this.state = state;
			switch (state) {
			case ON:
				setIcon(onIcon);
				break;
			case OFF:
				setIcon(offIcon);
				break;
			case EMPTY:
				setIcon(emptyIcon);
				break;
			}
		}

		public LabelState getLabelState() {
			return state;
		}

		// we don't want children to 'snap' back into place
		// when dragging out of parent view after touching
		@Override
		public boolean containsPoint(float x, float y) {
			return x > this.x && x < this.x + width;
		}
	}

	protected static final float GAP_BETWEEN_LABELS = 5;
	protected static final float TEXT_Y_OFFSET = 3;
	protected float labelWidth = 0;
	protected Label touchedLabel = null;
	protected LabelListListener listener = null;

	public LabelList(ShapeGroup shapeGroup) {
		super(shapeGroup);
	}

	public void setListener(LabelListListener listener) {
		this.listener = listener;
	}

	public Label getLabel(int position) {
		return (Label) children.get(position);
	}

	public void setLabelState(int position, LabelState labelState) {
		Label label = getLabel(position);
		if (label != null) {
			label.setState(labelState);
		}
	}

	public Label addLabel(String text, boolean on) {
		Label newLabel = new Label(null);
		// need onPressListener as well as the onReleaseListener to notify
		// when a label becomes touched
		newLabel.setOnPressListener(this);
		newLabel.setOnReleaseListener(this);
		addChild(newLabel);
		labelWidth = (width - BG_OFFSET * 3 - (children.size() - 1) * GAP_BETWEEN_LABELS)
				/ children.size();
		layoutChildren();
		return newLabel;
	}

	// callback function for listener to notify when the label text and id is
	// known
	public void setLabelText(int position, String text) {
		Label label = getLabel(position);
		if (label == null)
			return;
		if (text.isEmpty()) {
			label.setState(LabelState.EMPTY);
			label.setText(Label.EMPTY_TEXT);
		} else {
			label.setText(text);
		}
	}

	@Override
	public synchronized void init() {
		super.init();
		listener.labelListInitialized(this);
	}

	@Override
	protected void longPress(int id, float x, float y) {
		// notify listener that the label has been long-clicked
		if (touchedLabel != null) {
			listener.labelLongClicked(children.indexOf(touchedLabel));
		}
	}

	@Override
	protected void singleTap(int id, float x, float y) {
		// notify listener that the label has been single-clicked (tapped)
		if (touchedLabel != null) {
			listener.labelClicked(touchedLabel.getText(), children.indexOf(touchedLabel));
		}
	}

	@Override
	protected void doubleTap(int id, float x, float y) {
		// no double tap for this view
	}

	@Override
	protected synchronized void createChildren() {
		initRoundedRect();
	}

	@Override
	public synchronized void layoutChildren() {
		Collections.sort(children); // sort children by position
		float xTotal = 3 * BG_OFFSET / 2;
		for (View label : children) {
			if (touchedLabel == null || !label.equals(touchedLabel)) {
				label.layout(this, xTotal, 3 * BG_OFFSET / 2, labelWidth, height - BG_OFFSET * 3);
			}
			xTotal += labelWidth + GAP_BETWEEN_LABELS;
		}
	}

	@Override
	protected synchronized void drawChildren() {
		for (View label : children) {
			if (!label.equals(touchedLabel)) {
				label.drawAll();
			}
		}
		// draw touched label last (ontop of others)
		if (touchedLabel != null) {
			touchedLabel.drawAll();
		}
	}

	@Override
	public void onPress(Button button) {
		touchedLabel = (Label) button;
	}

	@Override
	public void onRelease(Button button) {
		touchedLabel = null;
	}
}
