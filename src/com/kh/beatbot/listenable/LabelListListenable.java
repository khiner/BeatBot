package com.kh.beatbot.listenable;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BBIcon;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.view.ClickableSurfaceView;

public class LabelListListenable extends ClickableSurfaceView {
	private static float addTextWidth;

	static enum LabelState {
		EMPTY, OFF, ON
	};

	/*
	 * holds the effect id for native effects and label image id, position,
	 * dragged state
	 */
	private class Label implements Comparable<Label> {
		LabelState state;
		private FloatBuffer backgroundRectVb = null;
		private String text = null;
		private float x, textWidth, labelWidth;
		
		Label(String text, boolean on, float x) {
			this.text = text;
			this.x = x;
			// empty text indicates empty label
			state = text.isEmpty() ? LabelState.EMPTY : (on ? LabelState.ON
					: LabelState.OFF);
			setText(text);
		}

		public void setText(String text) {
			this.text = text;
			if (text.isEmpty())
				state = LabelState.EMPTY;
			this.textWidth = glText.getTextWidth(text, height / 2);
		}

		public void updateSize() {
			labelWidth = state == LabelState.EMPTY ? (width - (labels.size() - 1)
					* GAP_BETWEEN_LABELS)
					/ labels.size()
					: textWidth + 20;
			backgroundRectVb = makeRoundedCornerRectBuffer(labelWidth, height,
					14, 16);
		}

		public boolean isPointInsideLabel(float x) {
			return x > this.x && x < this.x + labelWidth;
		}

		public void draw() {
			gl.glPushMatrix();
			translate(x + labelWidth / 2, height / 2);
			// draw background - different background colors for
			// normal/dragged/on labels
			drawTriangleFan(backgroundRectVb, whichColor());
			gl.glPopMatrix();
			if (state == LabelState.EMPTY) {
				plusIcon.draw(x + labelWidth / 2 - addTextWidth / 2, 0, height,
						height);
				setColor(Colors.WHITE);
				// draw string in center of rect
				glText.draw("ADD", height / 2, x + labelWidth / 2 - addTextWidth / 2
						+ height, TEXT_Y_OFFSET);
			} else {
				setColor(Colors.WHITE);
				// draw string in center of rect
				glText.draw(text, height / 2, x + labelWidth / 2 - textWidth / 2,
						TEXT_Y_OFFSET);
			}
		}

		private float[] whichColor() {
			if (touchedLabel != null && this.equals(touchedLabel)) {
				switch (state) {
				case EMPTY:
					return Colors.LABEL_MED;
				case OFF:
					return Colors.LABEL_VERY_LIGHT;
				case ON:
					return Colors.VOLUME_LIGHT;
				default:
					return Colors.VOLUME;
				}
			} else {
				switch (state) {
				case EMPTY:
					return Colors.LABEL_DARK;
				case OFF:
					return Colors.LABEL_LIGHT;
				case ON:
					return Colors.VOLUME;
				default:
					return Colors.LABEL_LIGHT;
				}
			}
		}

		@Override
		public int compareTo(Label another) {
			float diff = this.x - another.x;
			if (diff == 0)
				return 0;
			else if (diff > 0)
				return 1;
			else
				return -1;
		}
	}

	private static final float GAP_BETWEEN_LABELS = 5;
	private static final float TEXT_Y_OFFSET = 3;
	private static FloatBuffer bgRectVb = null;
	private LabelListListener listener = null;
	private ArrayList<Label> labels = null;
	private static BBIcon plusIcon;
	private int initialTouchedPosition;

	// which label is currently being touched? (null for none)
	private Label touchedLabel = null;

	private float dragOffset = 0;

	public LabelListListenable(Context c, AttributeSet as) {
		super(c, as);
	}

	public void setListener(LabelListListener listener) {
		this.listener = listener;
	}

	public void setLabelOn(int position, boolean on) {
		Label label = labels.get(position);
		if (label != null) {
			label.state = on ? LabelState.ON : LabelState.OFF;
		}
	}

	public Label getLabel(int index) {
		return index < labels.size() ? labels.get(index) : null;
	}

	/*
	 * Find the label such that the given coordinates are inside
	 */
	private Label findLabel(float x, float y) {
		for (Label label : labels) {
			if (label.isPointInsideLabel(x)) {
				return label;
			}
		}
		return null;
	}

	private void updateLabelSizes() {
		for (Label label : labels) {
			label.updateSize();
		}
	}

	private void updateLabelLocations() {
		Collections.sort(labels); // sort labels by x value
		float xTotal = 0;
		for (Label label : labels) {
			if (touchedLabel == null || !label.equals(touchedLabel)) {
				label.x = xTotal;
			}
			xTotal += label.labelWidth + GAP_BETWEEN_LABELS;
		}
	}

	public void addLabel(String text, boolean on) {
		labels.add(new Label(text, on, Float.MAX_VALUE));
		updateLabelSizes();
		updateLabelLocations();
	}

	// callback function for listener to notify when the label text and id is
	// known
	public void setLabelText(int position, String text) {
		Label label = labels.get(position);
		if (label != null) {
			label.setText(text);
		}
		updateLabelLocations();
	}

	public void setLabelTextByPosition(int pos, String text) {
		Label label = labels.get(pos);
		if (label != null) {
			label.setText(text);
		}
		updateLabelLocations();
	}
	
	private void touchLabel(Label label, float pointerX) {
		touchedLabel = label;
		dragOffset = touchedLabel.x - pointerX;
		initialTouchedPosition = labels.indexOf(touchedLabel);
	}

	private void initBgRectVb() {
		bgRectVb = makeRoundedCornerRectBuffer(width, height, 14, 15);
	}

	@Override
	protected void loadIcons() {
		plusIcon = new BBIcon(R.drawable.plus_outline);
	}
	
	@Override
	protected void init() {
		if (labels == null)
			labels = new ArrayList<Label>();
		addTextWidth = glText.getTextWidth("add", height / 2) + height;
		glText.storeText("add");
		if (bgRectVb == null)
			initBgRectVb();
		listener.labelListInitialized(this);
	}
	
	public boolean anyLabels() {
		return labels != null && !labels.isEmpty();
	}
	
	@Override
	protected void drawFrame() {
		translate(width / 2, height / 2);
		// draw background rounded rect
		drawTriangleFan(bgRectVb, Colors.VIEW_BG);
		translate(-width / 2, -height / 2);
		// draw all other labels other than touched label first
		for (Label label : labels) {
			if (touchedLabel == null || !touchedLabel.equals(label))
				label.draw();
		}
		if (touchedLabel != null) {
			touchedLabel.draw(); // draw touched label last so it's on top
		}
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		super.handleActionDown(id, x, y);
		// clicking on an existing label will select label.
		Label touched = findLabel(x, y);
		if (touched != null) {
			touchLabel(touched, x);
		}
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		// no pointer for now - only handle one
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		super.handleActionMove(e);
		if (touchedLabel == null) {
			return;
		}
		touchedLabel.x = e.getX(0) + dragOffset;
		updateLabelLocations();
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		// nothing for now
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		super.handleActionUp(id, x, y);
		// notify listener of the touched label's old and new position in list
		int newPosition = labels.indexOf(touchedLabel);
		if (newPosition != initialTouchedPosition) {
			listener.labelMoved(initialTouchedPosition, newPosition);
		}
		touchedLabel = null;
		updateLabelLocations();
	}

	@Override
	protected void longPress(int id, float x, float y) {
		// notify listener that the label has been long-clicked
		if (touchedLabel != null)
			listener.labelLongClicked(labels.indexOf(touchedLabel));
	}

	@Override
	protected void singleTap(int id, float x, float y) {
		// notify listener that the label has been single-clicked (tapped)
		if (touchedLabel != null)
			listener.labelClicked(touchedLabel.text, labels.indexOf(touchedLabel));
	}

	@Override
	protected void doubleTap(int id, float x, float y) {
		// no double tap for this view
	}
}
