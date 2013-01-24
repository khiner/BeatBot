package com.kh.beatbot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public abstract class TouchableSurfaceView extends SurfaceViewBase {

	public TouchableSurfaceView(Context c) {
		super(c);
	}

	public TouchableSurfaceView(Context c, AttributeSet as) {
		super(c, as);
	}

	protected abstract void handleActionDown(MotionEvent e, int id, float x,
			float y);

	protected abstract void handleActionUp(MotionEvent e, int id, float x,
			float y);

	protected abstract void handleActionPointerDown(MotionEvent e, int id,
			float x, float y);

	protected abstract void handleActionPointerUp(MotionEvent e, int id,
			float x, float y);

	protected abstract void handleActionMove(MotionEvent e, int id, float x,
			float y);

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		int index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		int id = e.getPointerId(index);
		float x = e.getX(index);
		float y = e.getY(index);
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_CANCEL:
			return false;
		case MotionEvent.ACTION_DOWN:
			handleActionDown(e, id, x, y);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			handleActionPointerDown(e, id, x, y);
			break;
		case MotionEvent.ACTION_MOVE:
			handleActionMove(e, id, x, y);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			handleActionPointerUp(e, id, x, y);
			break;
		case MotionEvent.ACTION_UP:
			handleActionUp(e, id, x, y);
			break;
		}
		return true;
	}
}
