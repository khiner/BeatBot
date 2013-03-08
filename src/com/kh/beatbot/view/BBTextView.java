package com.kh.beatbot.view;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.view.window.ViewWindow;

public class BBTextView extends ViewWindow {

	private String text = null;
	private float textWidth = 0;
	private float textOffset = 0;

	public BBTextView(GLSurfaceViewBase parent) {
		super(parent);
	}
	
	@Override
	protected void loadIcons() {
		if (text != null) {
			setText(text);
		}
	}
	
	@Override
	public void init() {
		if (text == null)
			return;
		GLSurfaceViewBase.storeText(text);
	}

	public void setText(String text) {
		this.text = text;
		textWidth = GLSurfaceViewBase.getTextWidth(text, height / 2);
		textOffset = width / 2 - textWidth / 2;
		//GLSurfaceViewBase.storeText(text);
		requestRender();
	}
	
	@Override
	public void draw() {
		if (text == null)
			return;
		setColor(Colors.VOLUME);
		// draw string in center of rect
		GLSurfaceViewBase.drawText(this, text, (int)(height / 2), textOffset, 0);
	}

	@Override
	protected void createChildren() {
		// leaf child
	}

	@Override
	protected void layoutChildren() {
		// leaf child
	}
}