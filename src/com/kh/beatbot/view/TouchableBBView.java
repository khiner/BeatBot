package com.kh.beatbot.view;

import java.util.HashMap;
import java.util.Map;

import android.view.MotionEvent;


public abstract class TouchableBBView extends BBView {

	// map of pointer ID #'s that this window is responsible for to their current
	// position relative to this window
	protected Map<Integer, Position> pointerIdToPos = new HashMap<Integer, Position>();
	
	public TouchableBBView() {}
	
	public TouchableBBView(TouchableSurfaceView parent) {
		super(parent);
	}

	public final boolean ownsPointer(int id) {
		return pointerIdToPos.containsKey(id);
	}
	
	public final int pointerCount() {
		return pointerIdToPos.size();
	}
	
	private final void consumeActionDown(int id, float x, float y) {
		pointerIdToPos.put(id, new Position(x, y));
		handleActionDown(id, x, y);
	}

	private final void consumeActionUp(int id, float x, float y) {
		handleActionUp(id, x, y);
		pointerIdToPos.clear();
	}

	private final void consumeActionPointerDown(int id, float x, float y) {
		pointerIdToPos.put(id, new Position(x, y));
		handleActionPointerDown(id, x, y);
	}

	private final void consumeActionPointerUp(int id, float x, float y) {
		handleActionPointerUp(id, x, y);
		pointerIdToPos.remove(id);
	}

	private final void consumeActionMove(int id, float x, float y) {
		if (!ownsPointer(id))
			return;
		pointerIdToPos.get(id).set(x, y);
		handleActionMove(id, x, y);
	}
	
	
	/********************************************************
	 * These are the handlers that implementations should
	 * override to actually handle touch events.  They are
	 * not abstract here because not all TouchableViews may
	 * want to implement each type of touch event (for example,
	 * a view may not handle multi-touch events like pointer
	 * up/down events.
	 ********************************************************/
	protected void handleActionDown(int id, float x, float y) {
		
	}

	protected void handleActionUp(int id, float x, float y) {
		
	}

	protected void handleActionPointerDown(int id, float x, float y) {
		
	}

	protected void handleActionPointerUp(int id, float x, float y) {
		
	}

	protected void handleActionMove(int id, float x, float y) {
		
	}
	
	
	/*
	 * Touch events are delegated to children, using coordinates relative to
	 * child.
	 */
	public final void propogateActionDown(MotionEvent e, int id, float x, float y) {
		consumeActionDown(id, x, y);
		BBView child = findChildAt(x, y);
		if (child instanceof TouchableBBView) {
			((TouchableBBView) child).propogateActionDown(e, id, x - child.x, y
					- child.y);
		}
	}

	public final void propogateActionUp(MotionEvent e, int id, float x, float y) {
		consumeActionUp(id, x, y);
		TouchableBBView child = findChildOwningPointer(id);
		if (child != null)
			child.propogateActionUp(e, id, x - child.x, y - child.y);
	}

	public final void propogateActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		consumeActionPointerDown(id, x, y);
		BBView child = findChildAt(x, y);
		if (child instanceof TouchableBBView) {
			TouchableBBView touchableChild = (TouchableBBView)child;
			if (touchableChild.pointerCount() == 0)
				touchableChild.propogateActionDown(e, id, x - child.x, y - child.y);
			else
				touchableChild.propogateActionPointerDown(e, id, x - child.x, y - child.y);
		}
	}

	public final void propogateActionPointerUp(MotionEvent e, int id, float x, float y) {
		consumeActionPointerUp(id, x, y);
		TouchableBBView child = findChildOwningPointer(id);
		if (child != null) {
			if (child.pointerCount() == 1)
				child.propogateActionUp(e, id, x - child.x, y - child.y);
			else
				child.propogateActionPointerUp(e, id, x - child.x, y - child.y);
		}
	}
	
	public final void propogateActionMove(MotionEvent e, int id, float x, float y) {
		consumeActionMove(id, x, y);
		TouchableBBView child = findChildOwningPointer(id);
		if (child != null)
			child.propogateActionMove(e, id, x - child.x, y - child.y);
	}

	private TouchableBBView findChildOwningPointer(int id) {
		for (BBView child : children) {
			if (child instanceof TouchableBBView
					&& ((TouchableBBView) child).ownsPointer(id)) {
				return (TouchableBBView) child;
			}
		}
		return null;
	}
}
