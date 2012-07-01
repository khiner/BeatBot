package com.kh.beatbot.view;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.listener.Level2dListener;
import com.kh.beatbot.view.bean.MidiViewBean;

public class TronSeekbar2d extends SurfaceViewBase {
	private ArrayList<Level2dListener> levelListeners = new ArrayList<Level2dListener>();
	private float[] color = MidiViewBean.VOLUME_COLOR;
	private float selectX = 0, selectY = 0;
	
	public TronSeekbar2d(Context c, AttributeSet as) {
		super(c, as);
	}
	
	public void setViewLevelX(float x) {
		selectX = x*(width - 50) + 25;
	}
	
	public void setViewLevelY(float y) {
		selectY = y*(height - 50) + 25;
	}
		
	public void addLevelListener(Level2dListener levelListener) {
		levelListeners.add(levelListener);
	}
	
	@Override
	protected void init() {
		gl.glEnable(GL10.GL_POINT_SMOOTH);
		for (Level2dListener listener : levelListeners) {
			listener.notifyInit(this);
		}
	}

	@Override
	protected void drawFrame() {
		gl.glClearColor(.3f, .3f, .3f, 1);
		drawSelectCircle();
	}

	private void drawSelectCircle() {
		FloatBuffer vb = makeFloatBuffer(new float[] {selectX, selectY});
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vb);
		for (int i = 15; i > 10; i--) {
			float alpha = (16 - i)/6f;
			gl.glPointSize(i*3);
			gl.glColor4f(color[0], color[1], color[2], alpha);
			gl.glDrawArrays(GL10.GL_POINTS, 0, 1);
		}
	}
	
	private void selectLocation(float x, float y) {
		selectX = x < 25 ? 25 : (x > width - 25 ? width - 25 : x);
		selectY = y < 25 ? 25 : (y > height - 25 ? height - 25 : y);
		for (Level2dListener listener : levelListeners) {
			listener.setLevel(this, (selectX - 25)/(width - 50), (selectY - 25)/(height - 50));
		}
	}
	
	@Override
	protected void handleActionDown(int id, float x, float y) {
		color = MidiViewBean.SELECTED_COLOR;
		for (Level2dListener listener : levelListeners) {
			listener.notifyChecked(this, true);
		}
		selectLocation(x, y);
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		return; // only one selection
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		selectLocation(e.getX(0), e.getY(0));
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		return; // only one selection		
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		color = MidiViewBean.VOLUME_COLOR;
		for (Level2dListener listener : levelListeners) {
			listener.notifyChecked(this, false);
		}
	}	
}
