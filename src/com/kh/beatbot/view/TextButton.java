package com.kh.beatbot.view;

import com.kh.beatbot.global.ColorSet;
import com.kh.beatbot.global.RoundedRectIconSource;

public class TextButton extends ToggleButton {

	private String text = "";
	private float textWidth = 0, textHeight = 0;
	private float textXOffset = 0, textYOffset = 0;
	private ColorSet bgColorSet, strokeColorSet;
	
	public TextButton(TouchableSurfaceView parent, ColorSet bgColorSet, ColorSet strokeColorSet) {
		super(parent);
		this.bgColorSet = bgColorSet;
		this.strokeColorSet = strokeColorSet;
	}
	
	@Override
	protected void loadIcons() {
		setIconSource(new RoundedRectIconSource(width, height, bgColorSet, strokeColorSet));
		setText(text);
	}

	@Override
	public void init() {
		GLSurfaceViewBase.storeText(text);
		textHeight = height - 13;
		textWidth = GLSurfaceViewBase.getTextWidth(text, textHeight);
		textXOffset = width / 2 - textWidth / 2;
		textYOffset = 0;
	}
	
	public void setText(String text) {
		this.text = text;
		if (initialized)
			init();
	}
	
	@Override
	public void draw() {
		super.draw();
		if (text == null)
			return;
		// draw string in center of rect
		float[] textColor = touched ? strokeColorSet.pressedColor : checked ? strokeColorSet.selectedColor : strokeColorSet.defaultColor;
		drawText(text, textColor, (int)textHeight, textXOffset, textYOffset);
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
