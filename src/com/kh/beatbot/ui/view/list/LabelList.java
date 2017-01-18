package com.kh.beatbot.ui.view.list;

import java.util.Collections;

import com.kh.beatbot.effect.Effect;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.ClickableView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;

public class LabelList extends TouchableView {

	public static enum LabelState {
		ON, OFF, EMPTY
	};

	protected static final float GAP_BETWEEN_LABELS = 5, TEXT_Y_OFFSET = 3;
	protected LabelListListener labelListListener = null;

	public LabelList(View view) {
		super(view);
	}

	public void setListener(LabelListListener listener) {
		this.labelListListener = listener;
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
		Label newLabel = new Label(this, null);
		newLabel.setShrinkable(true);
		// need onPressListener as well as the onReleaseListener to notify
		// when a label becomes touched
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
	protected synchronized void createChildren() {
		initRoundedRect();
	}

	@Override
	public synchronized void layoutChildren() {
		float labelWidth = (width - BG_OFFSET * 3 - (children.size() - 1) * GAP_BETWEEN_LABELS)
				/ children.size();
		Collections.sort(children); // sort children by position
		float xTotal = 3 * BG_OFFSET / 2;
		for (View label : children) {
			if (!((TouchableView) label).isPressed()) {
				label.layout(this, xTotal, BG_OFFSET, labelWidth, height - BG_OFFSET * 2);
			}
			xTotal += labelWidth + GAP_BETWEEN_LABELS;
		}
	}

	@Override
	protected synchronized void drawChildren() {
		for (View label : children) {
			if (!((TouchableView) label).isPressed()) {
				label.drawAll();
			}
		}
		for (View label : children) {
			if (((TouchableView) label).isPressed()) {
				label.drawAll();
			}
		}
	}

	protected class Label extends ClickableView {
		private final static String EMPTY_TEXT = Effect.NEW_EFFECT_LABEL;
		private LabelState state = LabelState.EMPTY;
		private int initialTouchedPosition = -1;

		private float originalX = 0;

		public Label(View view, RenderGroup renderGroup) {
			super(view, renderGroup);
			initRoundedRect();
			setIcon(IconResourceSets.LIST_ITEM_EMPTY);
			setText(EMPTY_TEXT);
		}

		public void setState(LabelState state) {
			this.state = state;
			switch (state) {
			case ON:
				setIcon(IconResourceSets.LIST_ITEM_ON);
				break;
			case OFF:
				setIcon(IconResourceSets.LIST_ITEM_OFF);
				break;
			case EMPTY:
				setIcon(IconResourceSets.LIST_ITEM_EMPTY);
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

		@Override
		protected void singleTap(int id, Pointer pos) {
			// notify listener that the label has been single-clicked (tapped)
			labelListListener.labelClicked(getText(), this.parent.indexOf(this));
		}

		@Override
		protected void doubleTap(int id, Pointer pos) {
		}

		@Override
		protected void longPress(int id, Pointer pos) {
			labelListListener.labelLongClicked(parent.indexOf(this));
		}

		@Override
		public void press() {
			super.press();
			initialTouchedPosition = parent.indexOf(this);
			originalX = x;
		}

		@Override
		public void release() {
			super.release();
			// notify listener of the touched label's old and new position in list
			int newPosition = parent.indexOf(this);
			if (newPosition != initialTouchedPosition) {
				labelListListener.labelMoved(initialTouchedPosition, newPosition);
			}
			parent.layoutChildrenSynchronized();
		}

		@Override
		public void handleActionMove(int id, Pointer pos) {
			super.handleActionMove(id, pos);
			setPosition(x + pos.x - getPointer().downX, y);
			if (Math.abs(x - originalX) > SNAP_DIST)
				releaseLongPress();
			parent.layoutChildrenSynchronized();
		}
	}
}
