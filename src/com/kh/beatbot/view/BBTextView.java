package com.kh.beatbot.view;

import android.content.Context;
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
		// nothing to do
	}

	public void setText(String text) {
		this.text = text;
		textWidth = glText.getTextWidth(this.text);
		textOffset = width / 2 - textWidth / 2;
		glText.init(this.text, textOffset, 0);
	}
	
	@Override
	protected void drawFrame() {
		setColor(Colors.VOLUME_COLOR);
		// draw string in center of rect
		glText.draw();
	}
}
