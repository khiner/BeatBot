package com.kh.beatbot.view.list;

import java.nio.FloatBuffer;
import java.util.Collections;

import com.kh.beatbot.R;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.ShapeIconSource;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.ClickableBBView;
import com.kh.beatbot.view.GLSurfaceViewBase;
import com.kh.beatbot.view.control.Button;
import com.kh.beatbot.view.control.TextButton;

public class LabelList extends ClickableBBView implements OnPressListener,
		OnReleaseListener {

	protected static final float GAP_BETWEEN_LABELS = 5;
	protected static final float TEXT_Y_OFFSET = 3;
	protected static FloatBuffer bgRectVb = null;
	protected LabelListListener listener = null;

	protected TextButton touchedLabel = null;

	public void setListener(LabelListListener listener) {
		this.listener = listener;
	}

	public void checkLabel(int position, boolean checked) {
		TextButton label = (TextButton) children.get(position);
		if (label != null) {
			label.setChecked(checked);
		}
	}

	public TextButton addLabel(String text, boolean on) {
		TextButton newLabel = new TextButton(null,
				Colors.effectLabelBgColorSet, Colors.effectLabelStrokeColorSet,
				R.drawable.plus_outline, -1, -1);
		// need onPressListener as well as the onReleaseListener to notify
		// when a label becomes touched
		newLabel.setOnPressListener(this);
		newLabel.setOnReleaseListener(this);
		addChild(newLabel);
		layoutChildren();
		newLabel.setText("ADD");
		newLabel.loadAllIcons();
		return newLabel;
	}

	// callback function for listener to notify when the label text and id is
	// known
	public void setLabelText(int position, String text) {
		TextButton label = (TextButton) children.get(position);
		if (label == null) {
			return;
		}
		if (text.isEmpty()) {
			label.setText("ADD");
			label.setIconEnabled(true);
			label.setChecked(false);
		} else {
			label.setText(text);
			label.setIconEnabled(false);
		}
	}

	protected void initBgRectVb() {
		bgRectVb = makeRoundedCornerRectBuffer(width, height, 14, 15);
	}

	@Override
	protected void loadIcons() {
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
		if (touchedLabel != null)
			listener.labelLongClicked(children.indexOf(touchedLabel));

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
		// leaf child
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
		touchedLabel = (TextButton) button;
		//((ShapeIconSource)((TextButton)touchedLabel).getIconSource()).setColors(Colors.effectLabelTouchedBgColorSet, Colors.effectLabelTouchedStrokeColorSet);
	}

	@Override
	public void onRelease(Button button) {
		//((ShapeIconSource)((TextButton)touchedLabel).getIconSource()).setColors(Colors.effectLabelBgColorSet, Colors.effectLabelStrokeColorSet);
		touchedLabel = null;
	}
}
