package com.kh.beatbot.listenable;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.listener.LabelListListener;
import com.kh.beatbot.view.SurfaceViewBase;

public class LabelListListenable extends SurfaceViewBase {
	/*
	 * Struct to hold the effect id for native effects and label image id,
	 * position, dragged state
	 */
	private class Label implements Comparable<Label> {
		private static final float width = 80;
		private String text = null;
		private int prevPosition;
		private int id;
		private float x;

		Label(String text, int id, float x) {
			this.text = text;
			this.id = id;
			this.x = x;
		}

		public boolean isPointInsideLabel(float x) {
			return x > this.x && x < this.x + width;
		}
		
		public void draw() {
			if (this.equals(draggedLabel)) {
				// draw lighter background for dragged/selected label
			} else { 
				// draw normal background
			}
			translate(x, 0);
			drawTexture(id, width, height);
			translate(-x, 0);
		}

		@Override
		public int compareTo(Label another) {
			float diff = this.x - another.x;
			if (diff == 0)
				return 0;
			else if (diff > 0)
				return 1;
			else
				return 0;
		}
	}

	LabelListListener listener = null;
	private static ArrayList<Label> labels = new ArrayList<Label>();

	public static final int ADD_LABEL_ID = 0;

	public static final int CHORUS_ID = 2;
	public static final int DECIMATE_ID = 3;
	public static final int DELAY_ID = 4;
	public static final int FILTER_ID = 5;
	public static final int FLANGER_ID = 6;
	public static final int REVERB_ID = 7;
	public static final int TREMELO_ID = 8;
	
	private static long LAST_DOWN_TIME = -1;
	private static float LAST_DOWN_X = -1;
	// which label is currently being dragged?
	// (happens after long-click) (null for none)
	private static Label draggedLabel = null;
	// which label is currently being touched? (null for none)
	private static Label touchedLabel = null;

	private static float dragOffset = 0;
	
	public LabelListListenable(Context c, AttributeSet as) {
		super(c, as);
	}

	public void setListener(LabelListListener listener) {
		this.listener = listener;
	}

	private boolean pointInsideAddButton(float x, float y) {
		return x < height;
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
		Collections.sort(labels);
		for (int i = 0; i < labels.size(); i++) {
			labels.get(i).x = height + i * Label.width;
		}
	}
	
	// notify listener that a new label has been created,
	// and set the returned id
	private void addLabel() {
		int id = listener.labelAdded();
		float xPos = labels.get(labels.size() - 1).x + Label.width + height;
		labels.add(new Label(listener.getLabelText(id), id, xPos));
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
	
	@Override
	protected void init() {

	}

	@Override
	protected void drawFrame() {
		drawTexture(ADD_LABEL_ID, height, height); // for now, just draw plus label
		for (Label label : labels) {
			label.draw();
		}
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
				addLabel();
			} else if (touchedLabel != null) {
				selectLabel(touchedLabel);
			}
			return;
		}
		if (draggedLabel != null) {
			if (labels.contains(draggedLabel)) {
				updateLabelPositions();
			} else {
				// label dragged and dropped, but not in list - remove label
				removeLabel(draggedLabel);
			}
			draggedLabel = touchedLabel = null;
		}
	}
}
