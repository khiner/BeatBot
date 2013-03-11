package com.kh.beatbot.listenable;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;

import com.kh.beatbot.R;
import com.kh.beatbot.global.BBIcon;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.view.GLSurfaceViewBase;
import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.window.ClickableViewWindow;
import com.kh.beatbot.view.window.ViewWindow;

public class LabelList extends ClickableViewWindow {

	private static float addTextWidth;

	static enum LabelState {
		EMPTY, OFF, ON
	};

	protected class Label implements Comparable<Label> {
		private String text = null;
		
		public float x, textWidth, labelWidth;
		public LabelState state;
		public FloatBuffer backgroundRectVb = null;
		public ViewWindow view;
		
		Label(ViewWindow view, String text, boolean on, float x) {
			this.view = view;
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
			this.textWidth = GLSurfaceViewBase.getTextWidth(text, height / 2);
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
				plusIcon.draw(absoluteX + x + labelWidth / 2 - addTextWidth / 2, root.getHeight() - absoluteY - height, height,
						height);
				setColor(Colors.WHITE);
				// draw string in center of rect
				GLSurfaceViewBase.drawText(view, "ADD", (int)(height / 2), x + labelWidth / 2 - addTextWidth / 2
						+ height, TEXT_Y_OFFSET);
			} else {
				setColor(Colors.WHITE);
				// draw string in center of rect
				GLSurfaceViewBase.drawText(view, text, (int)(height / 2), x + labelWidth / 2 - textWidth / 2,
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

	protected static final float GAP_BETWEEN_LABELS = 5;
	protected static final float TEXT_Y_OFFSET = 3;
	protected static FloatBuffer bgRectVb = null;
	protected LabelListListener listener = null;
	protected ArrayList<Label> labels = null;
	protected static BBIcon plusIcon;

	// which label is currently being touched? (null for none)
	protected Label touchedLabel = null;

	public LabelList(TouchableSurfaceView parent) {
		super(parent);
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
	protected Label findLabel(float x, float y) {
		for (Label label : labels) {
			if (label.isPointInsideLabel(x)) {
				return label;
			}
		}
		return null;
	}

	protected void updateLabelSizes() {
		for (Label label : labels) {
			label.updateSize();
		}
	}

	protected void updateLabelLocations() {
		Collections.sort(labels); // sort labels by x value
		float xTotal = 0;
		for (Label label : labels) {
			if (touchedLabel == null || !label.equals(touchedLabel)) {
				label.x = xTotal;
			}
			xTotal += label.labelWidth + GAP_BETWEEN_LABELS;
		}
		requestRender();
	}

	public void addLabel(String text, boolean on) {
		labels.add(new Label(this, text, on, Float.MAX_VALUE));
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

	protected void touchLabel(Label label, float pointerX) {
		touchedLabel = label;
	}

	protected void initBgRectVb() {
		bgRectVb = makeRoundedCornerRectBuffer(width, height, 14, 15);
	}

	@Override
	protected void loadIcons() {
		plusIcon = new BBIcon(R.drawable.plus_outline);
	}
	
	@Override
	public void init() {
		if (labels == null)
			labels = new ArrayList<Label>();
		addTextWidth = GLSurfaceViewBase.getTextWidth("add", height / 2) + height;
		GLSurfaceViewBase.storeText("add");
		if (bgRectVb == null)
			initBgRectVb();
		listener.labelListInitialized(this);
	}
	
	public boolean anyLabels() {
		return labels != null && !labels.isEmpty();
	}
	
	@Override
	public void draw() {
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
		requestRender();
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

	@Override
	protected void createChildren() {
		// leaf child
	}

	@Override
	public void layoutChildren() {
		// leaf child
	}
}
