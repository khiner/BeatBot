package com.kh.beatbot.view.group;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public class GLSurfaceViewGroup extends TouchableSurfaceView {

	protected BBView renderer;

	public GLSurfaceViewGroup(Context context) {
		super(context);
	}

	public GLSurfaceViewGroup(Context context, AttributeSet attr) {
		super(context, attr);
	}

	public void setBBRenderer(BBView renderer) {
		this.renderer = renderer;
	}
	
	@Override
	protected void init() {
		renderer.initAll();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		super.surfaceChanged(holder, format, w, h);
		renderer.layout(null, 0, 0, w, h);
	}

	public void initGl(GL10 gl) {
		super.initGl(gl);
		renderer.initGl(gl);
	}
	
	@Override
	protected void draw() {
		renderer.drawAll();
	}

	/*
	 * Touch events are delegated to children, using coordinates relative to
	 * child.
	 */
	protected void handleActionDown(MotionEvent e, int id, float x, float y) {
		if (renderer instanceof TouchableBBView) {
			((TouchableBBView) renderer).propogateActionDown(e, id, x, y);
		}
	}

	protected void handleActionUp(MotionEvent e, int id, float x, float y) {
		if (renderer instanceof TouchableBBView) {
			((TouchableBBView) renderer).propogateActionUp(e, id, x, y);
		}
	}

	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		if (renderer instanceof TouchableBBView) {
			((TouchableBBView) renderer).propogateActionPointerDown(e, id, x, y);
		}
	}

	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		if (renderer instanceof TouchableBBView) {
			((TouchableBBView) renderer).propogateActionPointerUp(e, id, x, y);
		}
	}
	
	protected void handleActionMove(MotionEvent e, int id, float x, float y) {
		if (renderer instanceof TouchableBBView) {
			((TouchableBBView) renderer).propogateActionMove(e, id, x, y);
		}
	}
}