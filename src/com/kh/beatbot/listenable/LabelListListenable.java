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
import com.kh.beatbot.view.SurfaceViewBase;
import com.kh.beatbot.view.bean.MidiViewBean;
import com.kh.beatbot.view.text.GLText;

public class LabelListListenable extends SurfaceViewBase {
	/*
	 * Struct to hold the effect id for native effects and label image id,
	 * position, dragged state
	 */
	private class Label implements Comparable<Label> {
		private FloatBuffer backgroundRectVb = null;
		private String text = null;
		private float x, textWidth, width;
		private int id;
		private int prevPosition;
		private boolean on = false;

		Label(String text, int id, float x) {
			this.text = text;
			this.id = id;
			this.x = x;
			textWidth = glText.getTextWidth(text);
			width = textWidth + 20;
			backgroundRectVb = makeRoundedCornerRectBuffer(this.width, height, 10, 16);
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
			drawTriangleFan(backgroundRectVb,
					this.equals(draggedLabel) ? LABEL_RECT_SELECTED_COLOR :
						(on ? LABEL_RECT_ON_COLOR : LABEL_RECT_COLOR));
			gl.glPopMatrix();
			gl.glEnable(GL10.GL_TEXTURE_2D);
			setColor(GlobalVars.WHITE);
			glText.draw(text, x + 10, 0); // draw string
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
	
	private static final float[] BG_RECT_COLOR = LevelListenable.BG_COLOR.clone();
	private static final float[] LABEL_RECT_COLOR = {BG_RECT_COLOR[0] + .2f,
													 BG_RECT_COLOR[1] + .2f,
													 BG_RECT_COLOR[2] + .2f, 1};
	private static final float[] LABEL_RECT_SELECTED_COLOR = {BG_RECT_COLOR[0] + .4f,
															BG_RECT_COLOR[1] + .4f,
															BG_RECT_COLOR[2] + .4f, 1};
	private static final float[] LABEL_RECT_ON_COLOR = MidiViewBean.VOLUME_COLOR.clone();
	
	private static final float GAP_BETWEEN_LABELS = 5;
	
	private FloatBuffer bgRectVb = null;  
	private GLText glText; // A GLText Instance
	private LabelListListener listener = null;
	private ArrayList<Label> labels = new ArrayList<Label>();

	public static final int ADD_LABEL_ID = 0;
	
	private long LAST_DOWN_TIME = -1;
	private float LAST_DOWN_X = -1;
	
	// which label is currently being dragged?
	// (happens after long-click) (null for none)
	private Label draggedLabel = null;
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
			label.on = on;
		}
	}
	
	private boolean pointInsideAddButton(float x, float y) {
		return x < height;
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
		float xTotal = height + GAP_BETWEEN_LABELS;
		for (Label label : labels) {
			if (draggedLabel == null || !label.equals(draggedLabel)) {
				label.x = xTotal;
			}
			xTotal += label.width + GAP_BETWEEN_LABELS;
		}
	}
	
	// notify listener that a new label has been created,
	// and set the returned id
	private void labelAdded() {
		listener.labelAdded(labels.size());
	}
	
	// callback function for listener to notify when the label text and id is known
	public void addLabel(String text, int id) {
		labels.add(new Label(text, id, Float.MAX_VALUE));
		updateLabelLocations();
	}
	// notify listener that the label has been single-tapped
	private void selectLabel(Label label) {
		listener.labelSelected(label.id);
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

	// notify listener the the given label has been removed form the list
	private void removeLabel(Label label) {
		listener.labelRemoved(label.id);
	}
	
	private void initBgRectVb() {
		bgRectVb = makeRoundedCornerRectBuffer(width, height, 14, 15);
	}
	
	private void initGlText() {
		glText = new GLText(gl, this.getContext().getAssets());
		// Load the font from file with no padding, and height of 4/5 total height
		glText.load("Roboto-Regular.ttf", 4 * height / 5, 0, 0);
	}
	
	@Override
	protected void init() {
		listener.labelListInitialized(this);
		initBgRectVb();
		initGlText();
		loadTexture(R.drawable.plus_icon, ADD_LABEL_ID);
	}

	@Override
	protected void drawFrame() {
		gl.glDisable(GL10.GL_TEXTURE_2D);
		translate(width / 2, height / 2);
		drawTriangleFan(bgRectVb, BG_RECT_COLOR); // draw background rounded rect
		translate(-width / 2, -height / 2);
		drawTexture(ADD_LABEL_ID, 0, 0, height, height); // draw 'add label' icon
		gl.glEnable(GL10.GL_TEXTURE_2D);
		glText.begin(); // Begin Text Rendering
		for (int i = 0; i < labels.size(); i++) {
			// using iterator to avoid concurrent mod exceptions.
			// label could be removed in the middle of the iteration
			if (i < labels.size())
				labels.get(i).draw();
		}
		glText.end(); // Begin Text Rendering
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		LAST_DOWN_TIME = System.currentTimeMillis();
		LAST_DOWN_X = x;
		// clicking on an existing label will select label.
		touchedLabel = findLabel(x, y);
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		// no pointer for now - only handle one
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		if (Math.abs(e.getX(0) - LAST_DOWN_X) < 25) {
			if (touchedLabel != null && 
					System.currentTimeMillis() - LAST_DOWN_TIME >
						GlobalVars.LONG_CLICK_TIME) {
				draggedLabel = touchedLabel;
				dragOffset = draggedLabel.x - e.getX(0);
				setAllLabelPrevPositions();
			}
		} else {
			LAST_DOWN_TIME = Long.MAX_VALUE;
		}
		if (draggedLabel == null) {
			return;
		}
		draggedLabel.x = e.getX(0) + dragOffset;
		if (e.getY(0) < -10 || e.getY(0) > height + 10) {
			// if dragging away from the view, remove the note from the list
			labels.remove(draggedLabel);
		} else if (!labels.contains(draggedLabel)) {
			// if the label has been dragged away, it is now dragged back in to list
			labels.add(draggedLabel);
		}
		updateLabelLocations();
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		// nothing for now
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		if (System.currentTimeMillis() - LAST_DOWN_TIME < GlobalVars.SINGLE_TAP_TIME) {
			if (pointInsideAddButton(x, y)) {
				labelAdded();
			} else if (touchedLabel != null) {
				selectLabel(touchedLabel);
			}
		} else if (draggedLabel != null) {
			if (labels.contains(draggedLabel)) {
				updateLabelPositions();
			} else {
				// label dragged and dropped, but not in list - remove label
				removeLabel(draggedLabel);
			}
		}
		draggedLabel = touchedLabel = null;
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
