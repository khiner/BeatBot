package com.kh.beatbot.view;

import android.content.Context;
import android.util.AttributeSet;

import com.kh.beatbot.global.Colors;

public class BBTextView extends GLSurfaceViewBase {
	
	private String text = null;
	private float textWidth = 0;
	private float textOffset = 0;
	
	public BBTextView(Context c, AttributeSet as) {
		super(c, as);
	}
	
	@Override
	protected void loadIcons() {
		if (text != null) {
			setText(text);
		}
	}
	
	@Override
	protected void init() {
		if (text == null)
			return;
		glText.storeText(text);
	}

	public void setText(String text) {
		this.text = text;
		if (glText != null) {
			textWidth = glText.getTextWidth(text, height / 2);
			textOffset = width / 2 - textWidth / 2;
			//glText.storeText(text);
		}
		requestRender();
	}
	
	@Override
	protected void drawFrame() {
		if (text == null)
			return;
		setColor(Colors.VOLUME);
		// draw string in center of rect
		glText.draw(text, height / 2, textOffset, 0);
	}
}
