package com.kh.beatbot.listenable;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLU;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.view.SurfaceViewBase;
import com.kh.beatbot.view.bean.MidiViewBean;
import com.kh.beatbot.view.text.GLText;

public class LabelListListenable extends SurfaceViewBase {
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
		private float x, textWidth, width;
		private int id;
		private int prevPosition;

		Label(int id, String text, float x) {
			this.text = text;
			this.id = id;
			this.x = x;
			setText(text);
		}

		public void setText(String text) {
			this.text = text;
			// empty text indicates empty label, start with on state otherwise
			state = text.isEmpty() ? LabelState.EMPTY : LabelState.ON;
			this.textWidth = glText.getTextWidth(text);
			this.width = text.isEmpty() ? 200 : textWidth + 20;
			backgroundRectVb = makeRoundedCornerRectBuffer(this.width, height,
					10, 16);
		}

		public boolean isPointInsideLabel(float x) {
			return x > this.x && x < this.x + width;
		}

		public void draw() {
			gl.glDisable(GL10.GL_TEXTURE_2D);
			gl.glPushMatrix();
			translate(x + this.width / 2, height / 2);
			// draw background - different background colors for
			// normal/dragged/on labels
			drawTriangleFan(backgroundRectVb, whichColor());
			gl.glPopMatrix();
			gl.glEnable(GL10.GL_TEXTURE_2D);
			setColor(GlobalVars.WHITE);
			// draw string in center of rect
			glText.draw(text, x + width / 2 - textWidth / 2, 0);
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

	private FloatBuffer bgRectVb = null;
	private GLText glText = null; // A GLText Instance
	private LabelListListener listener = null;
	private ArrayList<Label> labels = null;

	private long LAST_DOWN_TIME = -1;
	private float LAST_DOWN_X = -1;

	// which label is currently being touched? (null for none)
	private Label touchedLabel = null;

	private float dragOffset = 0;

	public LabelListListenable(Context c, AttributeSet as) {
		super(c, as);
	}

	public void setListener(LabelListListener listener) {
		this.listener = listener;
	}

	public void setLabelOn(String labelText, boolean on) {
		Label label = findLabel(labelText);
		if (label != null) {
			label.state = LabelState.ON;
		}
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
	 * Find the label with the given text
	 */
	private Label findLabel(String text) {
		for (Label label : labels) {
			if (label.text.equals(text)) {
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
		float xTotal = GAP_BETWEEN_LABELS;
		for (Label label : labels) {
			if (touchedLabel == null || !label.equals(touchedLabel)) {
				label.x = xTotal;
			}
			xTotal += label.width + GAP_BETWEEN_LABELS;
		}
	}

	private void initLabels() {
		labels = new ArrayList<Label>();
		for (int i = 0; i < 4; i++) {
			addLabel("");
		}
	}

	public void addLabel(String text) {
		labels.add(new Label(labels.size(), text, Float.MAX_VALUE));
		updateLabelLocations();
	}

	public void removeLabel(String text) {
		Label label = findLabel(text);
		if (label != null) {
			labels.remove(label);
		}
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

	private void touchLabel(Label label, float pointerX) {
		touchedLabel = label;
		dragOffset = touchedLabel.x - pointerX;
		setAllLabelPrevPositions();
	}

	// notify listener that the label has been single-clicked (tapped)
	private void clickLabel(Label label) {
		listener.labelClicked(label.id, label.text);
	}

	// notify listener that the label has been long-clicked
	private void longClickLabel(Label label) {
		listener.labelLongClicked(label.id);
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
		glText.load("Roboto-Regular.ttf", 4 * height / 5, 0, 0);
	}

	@Override
	protected void init() {
		initGlText();
		if (labels == null)
			initLabels();
		if (bgRectVb == null)
			initBgRectVb();
		listener.labelListInitialized(this);
	}

	@Override
	protected void drawFrame() {
		gl.glDisable(GL10.GL_TEXTURE_2D);
		translate(width / 2, height / 2);
		// draw background rounded rect
		drawTriangleFan(bgRectVb, BG_RECT_COLOR);
		translate(-width / 2, -height / 2);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		glText.begin(); // Begin Text Rendering
		for (Label label : labels) {
			label.draw();
		}
		glText.end(); // Begin Text Rendering
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		LAST_DOWN_TIME = System.currentTimeMillis();
		LAST_DOWN_X = x;
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
		if (Math.abs(e.getX(0) - LAST_DOWN_X) < 25) {
			if (touchedLabel != null
					&& System.currentTimeMillis() - LAST_DOWN_TIME > GlobalVars.LONG_CLICK_TIME) {
				longClickLabel(touchedLabel);
			}
		} else {
			LAST_DOWN_TIME = Long.MAX_VALUE;
		}
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
		if (touchedLabel != null) {
			if (Math.abs(System.currentTimeMillis() - LAST_DOWN_TIME) < GlobalVars.SINGLE_TAP_TIME) {
				clickLabel(touchedLabel);
			} else {
				updateLabelPositions();
			}
		}
		touchedLabel = null;
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
}
