package com.kh.beatbot.view.group;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.window.TouchableViewWindow;
import com.kh.beatbot.view.window.ViewWindow;

public abstract class GLSurfaceViewGroup extends TouchableSurfaceView {

	protected List<ViewWindow> children = new ArrayList<ViewWindow>();

	public GLSurfaceViewGroup(Context context) {
		super(context);
		createChildren();
	}

	public GLSurfaceViewGroup(Context context, AttributeSet attr) {
		super(context, attr);
		createChildren();
	}

	public void addChild(ViewWindow child) {
		children.add(child);
	}
	
	protected abstract void createChildren();

	protected abstract void layoutChildren();
	
	@Override
	protected void loadIcons() {
		for (ViewWindow child : children) {
			child.loadIcons();
		}
	}
	
	@Override
	protected void init() {
		for (ViewWindow child : children) {
			child.init();
		}
	}

	public abstract void update();
	
	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		super.onSurfaceChanged(gl, w, h);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		super.surfaceChanged(holder, format, w, h);
		layoutChildren();
	}

	@Override
	protected void draw() {
		for (ViewWindow child : children) {
			push();
			translate(child.x, child.y);
			child.draw();
			pop();
		}
	}

	/*
	 * Touch events are delegated to children, using coordinates relative to
	 * child.
	 */
	protected void handleActionDown(MotionEvent e, int id, float x, float y) {
		ViewWindow child = findChildAt(x, y);
		if (child instanceof TouchableViewWindow) {
			((TouchableViewWindow) child).notifyActionDown(id, x - child.x, y
					- child.y);
		}
	}

	protected void handleActionUp(MotionEvent e, int id, float x, float y) {
		TouchableViewWindow child = findChildOwningPointer(id);
		if (child != null)
			child.notifyActionUp(id, x - child.x, y - child.y);
	}

	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		ViewWindow child = findChildAt(x, y);
		if (child instanceof TouchableViewWindow) {
			TouchableViewWindow touchableChild = (TouchableViewWindow)child;
			if (touchableChild.pointerCount() == 0)
				touchableChild.notifyActionDown(id, x - child.x, y - child.y);
			else
				touchableChild.notifyActionPointerDown(id, x - child.x, y - child.y);
		}
	}

	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		TouchableViewWindow child = findChildOwningPointer(id);
		if (child != null) {
			if (child.pointerCount() == 1)
				child.notifyActionUp(id, x - child.x, y - child.y);
			else
				child.notifyActionPointerUp(id, x - child.x, y - child.y);
		}
	}
	
	protected void handleActionMove(MotionEvent e, int id, float x, float y) {
		TouchableViewWindow child = findChildOwningPointer(id);
		if (child != null)
			child.notifyActionMove(id, x - child.x, y - child.y);
	}
	
	private ViewWindow findChildAt(float x, float y) {
		for (ViewWindow child : children) {
			if (child.containsPoint(x, y)) {
				return child;
			}
		}
		return null;
	}

	private TouchableViewWindow findChildOwningPointer(int id) {
		for (ViewWindow child : children) {
			if (child instanceof TouchableViewWindow
					&& ((TouchableViewWindow) child).ownsPointer(id)) {
				return (TouchableViewWindow) child;
			}
		}
		return null;
	}
}