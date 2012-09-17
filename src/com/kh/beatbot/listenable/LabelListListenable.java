package com.kh.beatbot.listenable;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLU;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.R;
import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.view.ClickableSurfaceView;
import com.kh.beatbot.view.bean.MidiViewBean;
import com.kh.beatbot.view.text.GLText;

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
		private int id;
		private int prevPosition;

		Label(int id, String text, boolean on, float x) {
			this.text = text;
			this.id = id;
			this.x = x;
			// empty text indicates empty label
			state = text.isEmpty() ? LabelState.EMPTY : (on ? LabelState.ON : LabelState.OFF);
			setText(text);
		}

		public void setText(String text) {
			this.text = text;
			if (text.isEmpty())
				state = LabelState.EMPTY;
			this.textWidth = glText.getTextWidth(text);
			labelWidth = text.isEmpty() ? (width - 3 * GAP_BETWEEN_LABELS) / 4 : textWidth + 20;
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
				drawTexture(0, x + labelWidth / 2 - addTextWidth / 2, 0, height, height);
				glText.begin(); // Begin Text Rendering
				setColor(GlobalVars.WHITE);
				// draw string in center of rect
				glText.draw("ADD", x + labelWidth / 2 - addTextWidth / 2 + height, TEXT_Y_OFFSET);
				glText.end();
			} else {
				glText.begin(); // Begin Text Rendering
				setColor(GlobalVars.WHITE);
				// draw string in center of rect
				glText.draw(text, x + labelWidth / 2 - textWidth / 2, TEXT_Y_OFFSET);
				glText.end();
			}
		}

		private float[] whichColor() {
			if (touchedLabel != null && this.equals(touchedLabel)) {
				switch (state) {
				case EMPTY:
					return SELECTED_EMPTY_RECT_COLOR;
				case OFF:
					return SELECTED_LABEL_RECT_COLOR;
				case ON:
					return SELECTED_LABEL_RECT_ON_COLOR;
				default:
					return SELECTED_LABEL_RECT_COLOR;
				}
			} else {
				switch (state) {
				case EMPTY:
					return EMPTY_RECT_COLOR;
				case OFF:
					return LABEL_RECT_COLOR;
				case ON:
					return LABEL_RECT_ON_COLOR;
				default:
					return LABEL_RECT_COLOR;
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

	private static final float[] BG_RECT_COLOR = LevelListenable.BG_COLOR
			.clone();
	
	private static final float[] EMPTY_RECT_COLOR = { BG_RECT_COLOR[0] + .1f,
			BG_RECT_COLOR[1] + .1f, BG_RECT_COLOR[2] + .1f, 1 };
	private static final float[] LABEL_RECT_COLOR = { BG_RECT_COLOR[0] + .3f,
			BG_RECT_COLOR[1] + .3f, BG_RECT_COLOR[2] + .3f, 1 };
	private static final float[] LABEL_RECT_ON_COLOR = MidiViewBean.VOLUME_COLOR
			.clone();

	private static final float[] SELECTED_EMPTY_RECT_COLOR = { EMPTY_RECT_COLOR[0] + .2f,
		EMPTY_RECT_COLOR[1] + .2f, EMPTY_RECT_COLOR[2] + .2f, 1 };
	private static final float[] SELECTED_LABEL_RECT_COLOR = { LABEL_RECT_COLOR[0] + .2f,
		LABEL_RECT_COLOR[1] + .2f, LABEL_RECT_COLOR[2] + .2f, 1 };
	private static final float[] SELECTED_LABEL_RECT_ON_COLOR = { LABEL_RECT_ON_COLOR[0] + .1f,
		LABEL_RECT_ON_COLOR[1] + .1f, LABEL_RECT_ON_COLOR[2] + .1f, 1 };

	private static final float GAP_BETWEEN_LABELS = 5;
	private static final float TEXT_Y_OFFSET = 3;
	private FloatBuffer bgRectVb = null;
	private GLText glText = null; // A GLText Instance
	private LabelListListener listener = null;
	private ArrayList<Label> labels = null;

	// which label is currently being touched? (null for none)
	private Label touchedLabel = null;

	private float dragOffset = 0;

	public LabelListListenable(Context c, AttributeSet as) {
		super(c, as);
	}

	public void setListener(LabelListListener listener) {
		this.listener = listener;
	}

	public void setLabelOn(int labelId, boolean on) {
		Label label = findLabel(labelId);
		if (label != null) {
			label.state = on ? LabelState.ON : LabelState.OFF;
		}
	}

	public Label getLabel(int index) {
		return index < labels.size() ? labels.get(index) : null;
	}
	
	/*
	 * Find the label with the given id
	 */
	private Label findLabel(int id) {
		for (Label label : labels) {
			if (label.id == id) {
				return label;
			}
		}
		return null;
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

	public void addLabel(String text, int id, boolean on) {
		labels.add(new Label(id, text, on, Float.MAX_VALUE));
		updateLabelLocations();
	}

	public boolean noLabels() {
		return labels == null || labels.isEmpty();
	}
	
	// callback function for listener to notify when the label text and id is
	// known
	public void setLabelText(int id, String text) {
		Label label = findLabel(id);
		if (label != null) {
			label.setText(text);
		}
		updateLabelLocations();
	}
	
	public void setLabel(int id, String text) {
		Label label = findLabel(id);
		if (label != null) {
			label.setText(text);
		}
		updateLabelLocations();
	}

	private void touchLabel(Label label, float pointerX) {
		touchedLabel = label;
		dragOffset = touchedLabel.x - pointerX;
		setAllLabelPrevPositions();
	}


	private void setAllLabelPrevPositions() {
		for (int i = 0; i < labels.size(); i++) {
			labels.get(i).prevPosition = i;
		}
	}

	// notify listener of the given label's old and new position in list
	private void updateLabelPositions() {
		for (Label label : labels) {
			int newPosition = labels.indexOf(label);
			if (newPosition != label.prevPosition) {
				listener.labelMoved(label.id, label.prevPosition, newPosition);
			}
		}
	}

	private void initBgRectVb() {
		bgRectVb = makeRoundedCornerRectBuffer(width, height, 14, 15);
	}

	private void initGlText() {
		glText = new GLText(gl, this.getContext().getAssets());
		// Load the font from file with no padding, and height of 4/5 total
		// height
		glText.load("REDRING-1969-v03.ttf", height / 2, 0, 0);
	}

	@Override
	protected void init() {
		loadTexture(R.drawable.plus_outline, 0);
		if (labels == null)
			labels = new ArrayList<Label>();
		initGlText();
		addTextWidth = glText.getTextWidth("add") + height;
		if (bgRectVb == null)
			initBgRectVb();
		while (listener == null); // wait for listener
		listener.labelListInitialized(this);
	}

	@Override
	protected void drawFrame() {
		translate(width / 2, height / 2);
		// draw background rounded rect
		drawTriangleFan(bgRectVb, BG_RECT_COLOR);
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
		touchedLabel = null;
		updateLabelPositions();
		updateLabelLocations();
	}

	@Override
	protected void drawFrame(GL10 gl, int w, int h) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		GLU.gluOrtho2D(gl, 0, width, 0, height);
		fillBackground();
		drawFrame();
		GLU.gluOrtho2D(gl, 0, width, height, 0);
	}

	@Override
	protected void longPress(int id, float x, float y) {
		// notify listener that the label has been long-clicked
		if (touchedLabel != null)
			listener.labelLongClicked(touchedLabel.id, labels.indexOf(touchedLabel));
	}

	@Override
	protected void singleTap(int id, float x, float y) {
		// notify listener that the label has been single-clicked (tapped)
		if (touchedLabel != null)
			listener.labelClicked(touchedLabel.text, touchedLabel.id, labels.indexOf(touchedLabel));
	}

	@Override
	protected void doubleTap(int id, float x, float y) {
		// no double tap for this view
	}
}
