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
	
	public void notifyActionDown(int id, float x, float y) {
		pointerIdToPos.put(id, new Position(x, y));
		handleActionDown(id, x, y);
	}

	public void notifyActionUp(int id, float x, float y) {
		handleActionUp(id, x, y);
		pointerIdToPos.clear();
	}

	public void notifyActionPointerDown(int id, float x, float y) {
		pointerIdToPos.put(id, new Position(x, y));
		handleActionPointerDown(id, x, y);
	}

	public void notifyActionPointerUp(int id, float x, float y) {
		handleActionPointerUp(id, x, y);
		pointerIdToPos.remove(id);
	}

	public void notifyActionMove(int id, float x, float y) {
		if (!ownsPointer(id))
			return;
		pointerIdToPos.get(id).set(x, y);
		handleActionMove(id, x, y);
	}
	
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
	public final void handleActionDown(MotionEvent e, int id, float x, float y) {
		notifyActionDown(id, x, y);
		BBView child = findChildAt(x, y);
		if (child instanceof TouchableBBView) {
			((TouchableBBView) child).handleActionDown(e, id, x - child.x, y
					- child.y);
		}
	}

	public final void handleActionUp(MotionEvent e, int id, float x, float y) {
		notifyActionUp(id, x, y);
		TouchableBBView child = findChildOwningPointer(id);
		if (child != null)
			child.handleActionUp(e, id, x - child.x, y - child.y);
	}

	public final void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		notifyActionPointerDown(id, x, y);
		BBView child = findChildAt(x, y);
		if (child instanceof TouchableBBView) {
			TouchableBBView touchableChild = (TouchableBBView)child;
			if (touchableChild.pointerCount() == 0)
				touchableChild.handleActionDown(e, id, x - child.x, y - child.y);
			else
				touchableChild.notifyActionPointerDown(id, x - child.x, y - child.y);
		}
	}

	public final void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		notifyActionPointerUp(id, x, y);
		TouchableBBView child = findChildOwningPointer(id);
		if (child != null) {
			if (child.pointerCount() == 1)
				child.handleActionUp(e, id, x - child.x, y - child.y);
			else
				child.handleActionPointerUp(e, id, x - child.x, y - child.y);
		}
	}
	
	public final void handleActionMove(MotionEvent e, int id, float x, float y) {
		notifyActionMove(id, x, y);
		TouchableBBView child = findChildOwningPointer(id);
		if (child != null)
			child.handleActionMove(e, id, x - child.x, y - child.y);
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
