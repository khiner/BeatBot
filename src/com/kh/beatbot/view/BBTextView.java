package com.kh.beatbot.view;

import android.content.Context;
import android.util.AttributeSet;

import com.kh.beatbot.global.Colors;

public class BBTextView extends SurfaceViewBase {
	
	private boolean textLoaded = false;
	
	private String text = null;
	private float textWidth = 0;
	private float textOffset = 0;
	
	public BBTextView(Context c, AttributeSet as) {
		super(c, as);
	}
	
	@Override
	protected void loadIcons() {
		initGlText();
		textLoaded = true;
		if (text != null) {
			setText(text);
		}
	}
	
	@Override
	protected void init() {
		if (text == null)
			return;
		glText.init(text, textOffset, 0);
	}

	public void setText(String text) {
		this.text = text;
		if (textLoaded) {
			textWidth = glText.getTextWidth(this.text);
			textOffset = width / 2 - textWidth / 2;
			glText.init(this.text, textOffset, 0);
		}
	}
	
	@Override
	protected void drawFrame() {
		if (text == null)
			return;
		setColor(Colors.VOLUME_COLOR);
		// draw string in center of rect
		glText.draw();
	}
}
