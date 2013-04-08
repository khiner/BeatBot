package com.kh.beatbot.view;

import com.kh.beatbot.global.Colors;

public class TextView extends BBView {

	private String text = null;
	private float textWidth = 0;
	private float textOffset = 0;
	
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
	}
	
	@Override
	public void draw() {
		if (text == null)
			return;
		// draw string in center of rect
		drawText(text, Colors.BLACK, (int)(height / 2), textOffset, 0);
	}

	@Override
	protected void createChildren() {
		// leaf child
	}

	@Override
	public void layoutChildren() {
		// leaf child
	}
}
