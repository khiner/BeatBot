package com.kh.beatbot.ui.view;

import android.util.SparseArray;
import android.view.MotionEvent;

import com.kh.beatbot.listener.TouchableViewListener;
import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.icon.IconResourceSet.State;
import com.kh.beatbot.ui.shape.RenderGroup;

public class TouchableView extends View {
	// map of pointer ID #'s that this window is responsible for to their current position relative
	// to this window
	protected SparseArray<Pointer> pointerById = new SparseArray<Pointer>();

	protected boolean shouldPropagateMoveEvents = true, deselectOnPointerExit = true;

	protected TouchableViewListener listener;

	public TouchableView(View view) {
		super(view);
	}

	public TouchableView(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	public TouchableView withRoundedRect() {
		return (TouchableView) super.withRoundedRect();
	}

	public TouchableView withIcon(IconResourceSet resourceSet) {
		return (TouchableView) super.withIcon(resourceSet);
	}

	public void setListener(TouchableViewListener listener) {
		this.listener = listener;
	}

	public final Pointer getPointer() {
		return pointerById.valueAt(0);
	}

	public final boolean ownsPointer(int id) {
		return pointerById.get(id) != null;
	}

	public final int pointerCount() {
		return pointerById.size();
	}

	/********************************************************
	 * These are the handlers that implementations should override to actually handle touch events.
	 * They are not abstract here because not all TouchableViews may want to implement each type of
	 * touch event (for example, a view may not handle multi-touch events like pointer up/down
	 * events.
	 ********************************************************/
	public void handleActionDown(int id, Pointer pos) {
		if (!isEnabled())
			return;
		press();
		if (listener != null) {
			listener.onPress(this);
		}
	}

	public void handleActionUp(int id, Pointer pos) {
		if (!isEnabled())
			return;
		release();
		if (listener != null) {
			listener.onRelease(this);
		}
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
		x += getXTouchTranslate();
		y += getYTouchTranslate();
		View child = findChildAt(x, y);
		if (child instanceof TouchableView) {
			((TouchableView) child).propagateActionDown(e, id, x - child.x, y - child.y);
		}
	}

	public final void propagateActionUp(MotionEvent e, int id, float x, float y) {
		TouchableView child = whichChildOwnsPointer(id);
		if (child != null)
			child.propagateActionUp(e, id, x - child.x, y - child.y);
		consumeActionUp(id, x, y);
	}

	public final void propagateActionPointerDown(MotionEvent e, int id, float x, float y) {
		consumeActionPointerDown(id, x, y);
		x += getXTouchTranslate();
		y += getYTouchTranslate();
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
		TouchableView child = whichChildOwnsPointer(id);
		if (child != null) {
			if (child.pointerCount() == 1)
				child.propagateActionUp(e, id, x - child.x, y - child.y);
			else
				child.propagateActionPointerUp(e, id, x - child.x, y - child.y);
		}

		consumeActionPointerUp(id, x, y);
	}

	public final void propagateActionMove(MotionEvent e, int id, float x, float y) {
		consumeActionMove(id, x, y);
		if (!shouldPropagateMoveEvents) {
			return;
		}
		TouchableView child = whichChildOwnsPointer(id);
		if (child != null) {
			x += getXTouchTranslate();
			y += getYTouchTranslate();
			child.propagateActionMove(e, id, x - child.x, y - child.y);
		}
	}

	/***************************************************************
	 * These "consume" methods are essentially wrappers around handler methods that also take care
	 * of the business of properly storing the pointers in the "id to position" map.
	 ***************************************************************/
	private final void consumeActionDown(int id, float x, float y) {
		Pointer pos = new Pointer(id, x, y);
		pointerById.put(id, pos);
		handleActionDown(id, pos);
	}

	private final void consumeActionUp(int id, float x, float y) {
		Pointer pos = pointerById.get(id);
		if (null != pos) {
			handleActionUp(id, pos);
		}
		pointerById.clear();
	}

	private final void consumeActionPointerDown(int id, float x, float y) {
		Pointer pos = new Pointer(id, x, y);
		pointerById.put(id, pos);
		handleActionPointerDown(id, pos);
	}

	private final void consumeActionPointerUp(int id, float x, float y) {
		Pointer pos = pointerById.get(id);
		handleActionPointerUp(id, pos);
		pointerById.remove(id);
	}

	private final void consumeActionMove(int id, float x, float y) {
		Pointer pos = pointerById.get(id);
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

	protected float getXTouchTranslate() {
		return 0;
	}

	protected float getYTouchTranslate() {
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
