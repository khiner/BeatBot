package com.kh.beatbot.view;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.kh.beatbot.EffectActivity;
import com.kh.beatbot.view.bean.MidiViewBean;

public class XYView extends SurfaceViewBase {
	private EffectActivity effectActivity;
	
	private float selectX = 0, selectY = 0;
	
	public XYView(Context c, AttributeSet as) {
		super(c, as);
		effectActivity = (EffectActivity)getContext();
	}
	
	public void setSelectXY(float x, float y) {
		Log.d("x, y = ", String.valueOf(x) + ", " + String.valueOf(y));
		selectX = x*(width - 30) + 15;
		selectY = y*(height - 30) + 15;
	}
		
	@Override
	protected void init() {
		setSelectXY(effectActivity.getXValue(), effectActivity.getYValue());
		gl.glEnable(GL10.GL_POINT_SMOOTH);		
	}

	@Override
	protected void drawFrame() {
		gl.glClearColor(.2f, .2f, .2f, 1);
		drawSelectCircle();
	}

	private void drawSelectCircle() {
		gl.glPointSize(30);
		float[] color = MidiViewBean.VOLUME_COLOR;
		FloatBuffer vb = makeFloatBuffer(new float[] {selectX, selectY});
		gl.glColor4f(color[0], color[1], color[2], 1);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vb);
		gl.glDrawArrays(GL10.GL_POINTS, 0, 1);		
	}
	
	private void selectLocation(float x, float y) {
		selectX = x < 15 ? 15 : (x > width - 15 ? width - 15 : x);
		selectY = y < 15 ? 15 : (y > height - 15 ? height - 15 : y);
		effectActivity.setXValue((selectX - 15)/(width - 30));
		effectActivity.setYValue((selectY - 15)/(height - 30));		
	}
	
	@Override
	protected void handleActionDown(int id, float x, float y) {
		effectActivity.setEffectDynamic(true);
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
		effectActivity.setEffectDynamic(false);
	}

	
}
