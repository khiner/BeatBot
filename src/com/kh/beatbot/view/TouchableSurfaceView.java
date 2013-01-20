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
	
	protected abstract void handleActionDown(int id, float x, float y);

	protected abstract void handleActionPointerDown(MotionEvent e, int id,
			float x, float y);

	protected abstract void handleActionMove(MotionEvent e);

	protected abstract void handleActionPointerUp(MotionEvent e, int id,
			float x, float y);

	protected abstract void handleActionUp(int id, float x, float y);

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_CANCEL:
			return false;
		case MotionEvent.ACTION_DOWN:
			handleActionDown(e.getPointerId(0), e.getX(0), e.getY(0));
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			int index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			handleActionPointerDown(e, e.getPointerId(index), e.getX(index),
					e.getY(index));
			break;
		case MotionEvent.ACTION_MOVE:
			index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			handleActionMove(e);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			index = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			handleActionPointerUp(e, e.getPointerId(index), e.getX(index),
					e.getY(index));
			break;
		case MotionEvent.ACTION_UP:
			handleActionUp(e.getPointerId(0), e.getX(0), e.getY(0));
			break;
		}
		return true;
	}
}
