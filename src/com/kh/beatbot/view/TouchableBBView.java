package com.kh.beatbot.view;

import java.util.HashMap;
import java.util.Map;

import android.view.MotionEvent;


public abstract class TouchableBBView extends BBView {

	// map of pointer ID #'s that this window is responsible for to their current
	// position relative to this window
	protected Map<Integer, Position> pointerIdToPos = new HashMap<Integer, Position>();
	
	public final boolean ownsPointer(int id) {
		return pointerIdToPos.containsKey(id);
	}
	
	public final int pointerCount() {
		return pointerIdToPos.size();
	}

	/********************************************************
	 * These are the handlers that implementations should
	 * override to actually handle touch events.  They are
	 * not abstract here because not all TouchableViews may
	 * want to implement each type of touch event (for example,
	 * a view may not handle multi-touch events like pointer
	 * up/down events.
	 ********************************************************/
	public void handleActionDown(int id, float x, float y) {
		
	}

	public void handleActionUp(int id, float x, float y) {
		
	}

	public void handleActionPointerDown(int id, float x, float y) {
		
	}

	public void handleActionPointerUp(int id, float x, float y) {
		
	}

	public void handleActionMove(int id, float x, float y) {
		
	}
	
	/**********************************************************************
	 * Touch events are propogated to children, using coordinates relative 
	 * to child.
	 * 
	 * These methods also "consume" the actions, in case the view wants
	 * to do something with these touch events.
	 **********************************************************************/
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
		TouchableBBView child = whichChildOwnsPointer(id);
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
		TouchableBBView child = whichChildOwnsPointer(id);
		if (child != null) {
			if (child.pointerCount() == 1)
				child.propogateActionUp(e, id, x - child.x, y - child.y);
			else
				child.propogateActionPointerUp(e, id, x - child.x, y - child.y);
		}
	}
	
	public final void propogateActionMove(MotionEvent e, int id, float x, float y) {
		consumeActionMove(id, x, y);
		TouchableBBView child = whichChildOwnsPointer(id);
		if (child != null)
			child.propogateActionMove(e, id, x - child.x, y - child.y);
	}

	/***************************************************************
	 * These "consume" methods are essentially wrappers around
	 * handler methods that also take care of the business of
	 * properly storing the pointers in the "id to position" map.
	 ***************************************************************/
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
	
	/**
	 * Find which child is responsible for touch events from
	 * the pointer with the given id.
	 *  
	 * @param id the pointer id
	 * @return the Touchable view child responsible for
	 * handling actions from the given pointer id, or null
	 * if no such Touchable child view exists
	 */
	private TouchableBBView whichChildOwnsPointer(int id) {
		for (BBView child : children) {
			if (child instanceof TouchableBBView
					&& ((TouchableBBView) child).ownsPointer(id)) {
				return (TouchableBBView) child;
			}
		}
		return null;
	}
}
