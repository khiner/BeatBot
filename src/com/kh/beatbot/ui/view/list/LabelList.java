package com.kh.beatbot.ui.view.list;

import java.util.Collections;

import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.ShapeIcon;
import com.kh.beatbot.ui.color.ColorSet;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.ClickableView;
import com.kh.beatbot.ui.view.GLSurfaceViewBase;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;

public class LabelList extends ClickableView implements OnPressListener,
		OnReleaseListener {

	static enum LabelState {
		ON, OFF, EMPTY
	};

	private static final ColorSet onColorSet = new ColorSet(Colors.VOLUME,
			Colors.VOLUME_LIGHT);
	private static final ColorSet offColorSet = new ColorSet(
			Colors.LABEL_LIGHT, Colors.LABEL_VERY_LIGHT);
	private static final ColorSet emptyColorSet = new ColorSet(
			Colors.LABEL_DARK, Colors.LABEL_MED);

	protected class Label extends ImageButton {
		private LabelState state = LabelState.EMPTY;

		public Label() {
			super();
			setIcon(addIcon);
			setBgIcon(new RoundedRectIcon(null, emptyColorSet));
			setText("ADD");
		}

		public void setState(LabelState state) {
			ShapeIcon bgShape = ((ShapeIcon) getBgIcon());
			this.state = state;
			switch (state) {
			case ON:
				bgShape.setFillColorSet(onColorSet);
				break;
			case OFF:
				bgShape.setFillColorSet(offColorSet);
				break;
			case EMPTY:
				bgShape.setFillColorSet(emptyColorSet);
				break;
			}
		}

		public LabelState getState() {
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
	protected static Icon addIcon;
	protected Label touchedLabel = null;
	protected LabelListListener listener = null;

	public void setListener(LabelListListener listener) {
		this.listener = listener;
	}

	public Label getLabel(int position) {
		return (Label) children.get(position);
	}

	public void setLabelOn(int position, boolean on) {
		Label label = getLabel(position);
		if (label != null) {
			label.setState(on ? LabelState.ON : LabelState.OFF);
		}
	}

	public Label addLabel(String text, boolean on) {
		Label newLabel = new Label();
		// need onPressListener as well as the onReleaseListener to notify
		// when a label becomes touched
		newLabel.setOnPressListener(this);
		newLabel.setOnReleaseListener(this);
		children.add(newLabel);
		layoutChildren();
		newLabel.loadAllIcons();
		return newLabel;
	}

	// callback function for listener to notify when the label text and id is
	// known
	public void setLabelText(int position, String text) {
		Label label = getLabel(position);
		if (label == null) {
			return;
		}
		if (text.isEmpty()) {
			label.setIcon(addIcon);
			label.setText("ADD");
			label.setState(LabelState.EMPTY);
		} else {
			label.setIcon(null);
			label.setText(text);
		}
	}

	@Override
	protected void loadIcons() {
		addIcon = new Icon(IconResources.ADD);
	}

	@Override
	public void init() {
		GLSurfaceViewBase.storeText("ADD");
		layoutBgRect(2, height / 6);
		listener.labelListInitialized(this);
	}

	@Override
	public void draw() {

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
			listener.labelClicked(touchedLabel.getText(),
					children.indexOf(touchedLabel));
		}
	}

	@Override
	protected void doubleTap(int id, float x, float y) {
		// no double tap for this view
	}

	@Override
	protected void createChildren() {
		initBgRect(Colors.VIEW_BG, Colors.VOLUME);
	}

	@Override
	public void layoutChildren() {
		float labelW = (width - borderOffset * 3 - (children.size() - 1)
				* GAP_BETWEEN_LABELS)
				/ children.size();

		Collections.sort(children); // sort children by position
		float xTotal = 3 * borderOffset / 2;
		for (View label : children) {
			if (touchedLabel == null || !label.equals(touchedLabel)) {
				label.layout(this, xTotal, 3 * borderOffset / 2, labelW, height
						- borderOffset * 3);
			}
			xTotal += labelW + GAP_BETWEEN_LABELS;
		}
	}

	protected synchronized void drawChildren() {
		for (View label : children) {
			if (label.equals(touchedLabel))
				continue;
			push();
			translate(label.x, label.y);
			label.drawAll();
			pop();
		}
		// draw touched label last (ontop of others)
		if (touchedLabel != null) {
			push();
			translate(touchedLabel.x, touchedLabel.y);
			touchedLabel.drawAll();
			pop();
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
