package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.kh.beatbot.listenable.LevelListenable;
import com.kh.beatbot.listener.LevelListener;

public class TronSeekbar extends LevelListenable {
	private float[] bgColor = new float[] {.3f, .3f, .3f , 1};
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
		super.init();
		initBgBar();
		initLevelBarVB();
	}
		
	private void drawBar(FloatBuffer vb, float[] color) {
		drawLines(vb, color, height/2, GL10.GL_LINES);
		gl.glPointSize(height/4.5f);
		gl.glDrawArrays(GL10.GL_POINTS, 0, 2);
		if (vb.equals(levelBarVb)) {
			gl.glPointSize(height/2);
			gl.glColor4f(color[0], color[1], color[2], .7f);
			gl.glDrawArrays(GL10.GL_POINTS, 1, 1);
		}
	}

	public void setViewLevel(float x) {
		super.setViewLevel(x);
		initLevelBarVB();
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
		super.handleActionDown(id, x, y);
	}

	@Override
	protected void handleActionMove(MotionEvent e) {
		setLevel(xToLevel(e.getX(0)));
	}

}
