package com.kh.beatbot.view;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.listener.LevelListener;
import com.kh.beatbot.view.bean.MidiViewBean;

public class TronSeekbar extends SurfaceViewBase {
	ArrayList<LevelListener> levelListeners = new ArrayList<LevelListener>();
	
	private float level = .5f;
	private float[] levelColor = MidiViewBean.VOLUME_COLOR;
	private float[] bgColor = new float[] {.3f, .3f, .3f };
	private FloatBuffer bgBarVb = null;
	private FloatBuffer levelBarVb = null;

	public TronSeekbar(Context c, AttributeSet as) {
		super(c, as);
	}

	public void addLevelListener(LevelListener levelListener) {		
		levelListeners.add(levelListener);
	}
	
	private void initBgBar() {
		float[] vertices = new float[] { height/2, height/2, width - height/2, height/2 };
		bgBarVb = makeFloatBuffer(vertices);		
	}
	
	private void initLevelBarVB() {
		float[] vertices = new float[] { height/2, height/2, levelToX(level), height/2 };
		levelBarVb = makeFloatBuffer(vertices);
	}
	
	@Override
	protected void init() {
		gl.glEnable(GL10.GL_POINT_SMOOTH);
		initBgBar();
		initLevelBarVB();
		for (LevelListener levelListener : levelListeners) {
			levelListener.notifyInit(this);
		}
	}
		
	private void drawBar(FloatBuffer vb, float[] color) {
		gl.glColor4f(color[0], color[1], color[2], 1);		
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vb);
		gl.glLineWidth(height/2);
		gl.glDrawArrays(GL10.GL_LINES, 0, 2);
		gl.glPointSize(height/4.5f);
		gl.glDrawArrays(GL10.GL_POINTS, 0, 2);
		if (vb.equals(levelBarVb)) {
			gl.glPointSize(height/2);
			gl.glColor4f(color[0], color[1], color[2], .7f);
			gl.glDrawArrays(GL10.GL_POINTS, 1, 1);
		}
	}
	
	public float getLevel() {
		return level;
	}

	public void setViewLevel(float x) {
		this.level = x;
		initLevelBarVB();
	}
	
	/* level should be from 0 to 1 */
	public void setLevel(float level) {
		setViewLevel(level);
		for (LevelListener levelListener : levelListeners)
			levelListener.setLevel(this, level);
	}
	
	public void setLevelColor(float[] levelColor) {
		this.levelColor = levelColor;
	}
	
	@Override
	protected void drawFrame() {
		gl.glClearColor(0, 0, 0, 1);
		drawBar(bgBarVb, bgColor);
		drawBar(levelBarVb, levelColor);
	}

	private float xToLevel(float x) {
		float level = (x - height/2)/(width - height);
		level = level < 0 ? 0 : (level > 1 ? 1 : level);
		return level;
	}
	
	private float levelToX(float level) {
		return height/2 + level*(width - height);
	}
	
	@Override
	protected void handleActionDown(int id, float x, float y) {
		setLevel(xToLevel(x));
		for (LevelListener levelListener : levelListeners)
			levelListener.notifyChecked(this, true);
	}

	@Override
	protected void handleActionPointerDown(MotionEvent e, int id, float x,
			float y) {
		// no multitouch for this seekbar
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		setLevel(xToLevel(e.getX(0)));
	}

	@Override
	protected void handleActionPointerUp(MotionEvent e, int id, float x, float y) {
		// no multitouch for this seekbar		
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		for (LevelListener levelListener : levelListeners)
			levelListener.notifyChecked(this, false);
	}
	
}
