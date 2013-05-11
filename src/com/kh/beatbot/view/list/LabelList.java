package com.kh.beatbot.view.list;

import java.nio.FloatBuffer;
import java.util.Collections;

import com.kh.beatbot.R;
import com.kh.beatbot.global.ColorSet;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.global.RoundedRectIconSource;
import com.kh.beatbot.global.ShapeIconSource;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.ClickableBBView;
import com.kh.beatbot.view.GLSurfaceViewBase;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.ImageButton;

public class LabelList extends ClickableBBView implements OnPressListener,
		OnReleaseListener {

	static enum LabelState { ON, OFF, EMPTY };
	
	protected static class Label extends ImageButton {
		private LabelState state = LabelState.EMPTY;
		
		private static final ColorSet onColorSet = new ColorSet(Colors.VOLUME, Colors.VOLUME_LIGHT);
		private static final ColorSet offColorSet = new ColorSet(Colors.LABEL_LIGHT, Colors.LABEL_VERY_LIGHT);
		private static final ColorSet emptyColorSet = new ColorSet(Colors.LABEL_DARK, Colors.LABEL_MED);
		private static final ColorSet outlineColorSet = new ColorSet(Colors.WHITE, Colors.WHITE);
		
		public Label() {
			super();
			setIconSource(plusIconSource);
			setBgIconSource(new RoundedRectIconSource(null, emptyColorSet, outlineColorSet));
			setText("ADD");
		}
		
		public void setState(LabelState state) {
			ShapeIconSource bgShape = ((ShapeIconSource)this.getBgIconSource()); 
			this.state = state;
			switch (state) {
			case ON: bgShape.setColors(onColorSet, outlineColorSet);
			break;
			case OFF: bgShape.setColors(offColorSet, outlineColorSet);
			break;
			case EMPTY: bgShape.setColors(emptyColorSet, outlineColorSet);
			break;
			}
		}
		
		public LabelState getState() {
			return state;
		}
		
		// we don't want labels to 'snap' back into place
		// when dragging out of parent view after touching
		@Override
		public boolean containsPoint(float x, float y) {
			return x > this.x && x < this.x + width;
		}
	}
	
	protected static final float GAP_BETWEEN_LABELS = 5;
	protected static final float TEXT_Y_OFFSET = 3;
	protected static FloatBuffer bgRectVb = null;
	protected static ImageIconSource plusIconSource;
	protected LabelListListener listener = null;

	protected Label touchedLabel = null;

	
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
		addChild(newLabel);
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
			label.setText("ADD");
			label.setIconSource(plusIconSource);
			label.setState(LabelState.EMPTY);
		} else {
			label.setText(text);
			label.setIconSource(null);
		}
	}

	protected void initBgRectVb() {
		bgRectVb = makeRoundedCornerRectBuffer(width, height, 14, 15);
	}

	@Override
	protected void loadIcons() {
		plusIconSource = new ImageIconSource(R.drawable.plus_outline, -1, -1);
	}

	@Override
	public void init() {
		GLSurfaceViewBase.storeText("ADD");
		if (bgRectVb == null) {
			initBgRectVb();
		}
		listener.labelListInitialized(this);
	}

	@Override
	public void draw() {
		// draw background rounded rect
		push();
		translate(width / 2, height / 2);
		drawTriangleFan(bgRectVb, Colors.VIEW_BG);
		pop();
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
		// leaf
	}

	@Override
	public void layoutChildren() {
		float labelW = (width - (children.size() - 1) * GAP_BETWEEN_LABELS)
				/ children.size();

		Collections.sort(children); // sort labels by position
		float xTotal = 0;
		for (BBView label : children) {
			xTotal += label.width + GAP_BETWEEN_LABELS;
			label.layout(this, xTotal, 0, labelW, height);
		}
	}

	@Override
	public void drawAll() {
		draw();
		for (BBView label : children) {
			// not using foreach to avoid concurrent modification
			if (touchedLabel == null || !label.equals(touchedLabel)) {
				push();
				translate(label.x, label.y);
				label.drawAll();
				pop();
			}
		}
		if (touchedLabel != null) { // draw touched last
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
