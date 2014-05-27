package com.kh.beatbot.ui.view;

import android.util.SparseArray;
import android.view.MotionEvent;

import com.kh.beatbot.ui.icon.IconResourceSet.State;
import com.kh.beatbot.ui.shape.RenderGroup;

public class TouchableView extends View {
	// map of pointer ID #'s that this window is responsible for to their current position relative
	// to this window
	protected SparseArray<Pointer> pointersById = new SparseArray<Pointer>();

	protected boolean shouldPropagateTouchEvents = true, deselectOnPointerExit = true;

	public TouchableView(View view) {
		super(view);
	}

	public TouchableView(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	public final Pointer getPointer() {
		return pointersById.valueAt(0);
	}

	public final boolean ownsPointer(int id) {
		return pointersById.get(id) != null;
	}

	public final int pointerCount() {
		return pointersById.size();
	}

	/********************************************************
	 * These are the handlers that implementations should override to actually handle touch events.
	 * They are not abstract here because not all TouchableViews may want to implement each type of
	 * touch event (for example, a view may not handle multi-touch events like pointer up/down
	 * events.
	 ********************************************************/
	public void handleActionDown(int id, Pointer pos) {
		press();
	}

	public void handleActionUp(int id, Pointer pos) {
		release();
	}

	public void handleActionPointerDown(int id, Pointer pos) {

	}

	public void handleActionPointerUp(int id, Pointer pos) {

	}

	public void handleActionMove(int id, Pointer pos) {

	}

	/**********************************************************************
	 * Touch events are propagated to children, using coordinates relative to child.
	 * 
	 * These methods also "consume" the actions, in case the view wants to do something with these
	 * touch events.
	 **********************************************************************/
	public final void propagateActionDown(MotionEvent e, int id, float x, float y) {
		consumeActionDown(id, x, y);
		if (!shouldPropagateTouchEvents) {
			return;
		}
		x += getXTouchTransform();
		y += getYTouchTransform();
		View child = findChildAt(x, y);
		if (child instanceof TouchableView) {
			((TouchableView) child).propagateActionDown(e, id, x - child.x, y - child.y);
		}
	}

	public final void propagateActionUp(MotionEvent e, int id, float x, float y) {
		consumeActionUp(id, x, y);
		if (!shouldPropagateTouchEvents) {
			return;
		}
		TouchableView child = whichChildOwnsPointer(id);
		if (child != null)
			child.propagateActionUp(e, id, x - child.x, y - child.y);
	}

	public final void propagateActionPointerDown(MotionEvent e, int id, float x, float y) {
		consumeActionPointerDown(id, x, y);
		if (!shouldPropagateTouchEvents) {
			return;
		}
		x += getXTouchTransform();
		y += getYTouchTransform();
		View child = findChildAt(x, y);
		if (child instanceof TouchableView) {
			TouchableView touchableChild = (TouchableView) child;
			if (touchableChild.pointerCount() == 0)
				touchableChild.propagateActionDown(e, id, x - child.x, y - child.y);
			else
				touchableChild.propagateActionPointerDown(e, id, x - child.x, y - child.y);
		}
	}

	public final void propagateActionPointerUp(MotionEvent e, int id, float x, float y) {
		consumeActionPointerUp(id, x, y);
		if (!shouldPropagateTouchEvents) {
			return;
		}
		TouchableView child = whichChildOwnsPointer(id);
		if (child != null) {
			if (child.pointerCount() == 1)
				child.propagateActionUp(e, id, x - child.x, y - child.y);
			else
				child.propagateActionPointerUp(e, id, x - child.x, y - child.y);
		}
	}

	public final void propagateActionMove(MotionEvent e, int id, float x, float y) {
		consumeActionMove(id, x, y);
		if (!shouldPropagateTouchEvents) {
			return;
		}
		TouchableView child = whichChildOwnsPointer(id);
		if (child != null) {
			x += getXTouchTransform();
			y += getYTouchTransform();
			child.propagateActionMove(e, id, x - child.x, y - child.y);
		}
	}

	/***************************************************************
	 * These "consume" methods are essentially wrappers around handler methods that also take care
	 * of the business of properly storing the pointers in the "id to position" map.
	 ***************************************************************/
	private final void consumeActionDown(int id, float x, float y) {
		Pointer pos = new Pointer(id, x, y);
		pointersById.put(id, pos);
		handleActionDown(id, pos);
	}

	private final void consumeActionUp(int id, float x, float y) {
		Pointer pos = pointersById.get(id);
		if (null != pos) {
			handleActionUp(id, pos);
		}
		pointersById.clear();
	}

	private final void consumeActionPointerDown(int id, float x, float y) {
		Pointer pos = new Pointer(id, x, y);
		pointersById.put(id, pos);
		handleActionPointerDown(id, pos);
	}

	private final void consumeActionPointerUp(int id, float x, float y) {
		Pointer pos = pointersById.get(id);
		handleActionPointerUp(id, pos);
		pointersById.remove(id);
	}

	private final void consumeActionMove(int id, float x, float y) {
		Pointer pos = pointersById.get(id);
		if (null != pos) {
			pos.set(x, y);
			handleActionMove(id, pos);
		}
	}

	/**
	 * Find which child is responsible for touch events from the pointer with the given id.
	 * 
	 * @param id
	 *            the pointer id
	 * @return the Touchable view child responsible for handling actions from the given pointer id,
	 *         or null if no such Touchable child view exists
	 */
	protected TouchableView whichChildOwnsPointer(int id) {
		for (View child : children) {
			if (child instanceof TouchableView && ((TouchableView) child).ownsPointer(id)) {
				return (TouchableView) child;
			}
		}
		return null;
	}

	protected void press() {
		if (isEnabled()) {
			setState(State.PRESSED);
		}
	}

	protected void release() {
		if (isEnabled()) {
			setState(State.DEFAULT);
		}
	}

	/* Views can override this for different behavior when dragging away than lifting up */
	protected void dragRelease() {
		release();
	}

	public boolean isPressed() {
		return getState() == State.PRESSED;
	}

	protected void checkPointerExit(int id, Pointer pos) {
		if (!(isEnabled() && deselectOnPointerExit))
			return;
		// x / y are relative to this view but containsPoint is absolute
		if (!containsPoint(this.x + pos.x, this.y + pos.y)) {
			if (isPressed()) { // pointer dragged away from button - signal release
				dragRelease();
			}
		} else { // pointer inside button
			if (!isPressed()) { // pointer was dragged away and back IN to button
				press();
			}
		}
	}

	protected float getXTouchTransform() {
		return 0;
	}

	protected float getYTouchTransform() {
		return 0;
	}

	public class Pointer {
		public int id;
		public float downX, downY;
		public float x, y;

		public Pointer(int id, float x, float y) {
			this.id = id;
			downX = x;
			downY = y;
			set(x, y);
		}

		public void set(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public boolean equals(Pointer another) {
			if (null == another) {
				return false;
			} else {
				return this.id == another.id;
			}
		}
	}
}
