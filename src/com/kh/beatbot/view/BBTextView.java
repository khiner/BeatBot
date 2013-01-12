package com.kh.beatbot.view;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLU;
import android.util.AttributeSet;

import com.kh.beatbot.global.Colors;

public class BBTextView extends SurfaceViewBase {
	
	private String text = "";
	private float textWidth = 0;
	private float textOffset = 0;
	
	public BBTextView(Context c, AttributeSet as) {
		super(c, as);
	}
	
	@Override
	protected void loadIcons() {
		initGlText();
	}
	
	@Override
	protected void init() {
		
	}

	public void setText(String text) {
		this.text = text;
		textWidth = glText.getTextWidth(text);
		textOffset = width / 2 - textWidth / 2;
	}
	
	@Override
	protected void drawFrame() {
		setColor(Colors.VOLUME_COLOR);
		// draw string in center of rect
		glText.draw(text, textOffset, 0);
	}
	
	// TODO get rid of this garbage (also in labellistlistenable
	@Override
	protected void drawFrame(GL10 gl, int w, int h) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		GLU.gluOrtho2D(gl, 0, width, 0, height);
		fillBackground();
		drawFrame();
		GLU.gluOrtho2D(gl, 0, width, height, 0);
	}
}
