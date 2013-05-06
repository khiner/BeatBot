package com.kh.beatbot.view.control;

import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.IconSource;
import com.kh.beatbot.global.ShapeIconSource;
import com.kh.beatbot.view.BBView;
import com.kh.beatbot.view.GLSurfaceViewBase;

public class TextButton extends ToggleButton {

	private String text = "";
	private float textWidth = 0, textHeight = 0;
	private float textXOffset = 0, textYOffset = 0;

	@Override
	protected void loadIcons() {
		setText(text);
		init();
		// globalGroup == null ? 0 : absoluteX,
		// globalGroup == null ? 0 : absoluteY, width, height,
		// bgColorSet, strokeColorSet));
	}

	@Override
	public void init() {
		if (text.isEmpty() || !GLSurfaceViewBase.isInitialized()) {
			return;
		}
		GLSurfaceViewBase.storeText(text);
		textHeight = 5 * height / 8;
		textWidth = GLSurfaceViewBase.getTextWidth(text, textHeight);
		textXOffset = (iconSources[FOREGROUND_ICON_INDEX] != null ? iconOffset
				+ iconW + (width - iconW - iconOffset) / 2 : width / 2)
				- textWidth / 2;
		textXOffset += 2; // kludgey magic number correction,
							// but it corrects for something weird in
							// GLSurfaceViewBase.getTextWidth
		textYOffset = 0;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		init();
	}

	public void layout(BBView parent, float x, float y, float width, float height) {
		super.layout(parent, x, y, width, height);
		init();
	}

	@Override
	public void draw() {
		super.draw();
		if (text != null) { // draw optional text
			float[] textColor = calcStrokeColor();
			drawText(text, textColor, (int) textHeight, textXOffset,
					textYOffset);
		}
	}

	private float[] calcStrokeColor() {
		IconSource bgIconSource = iconSources[BACKGROUND_ICON_INDEX]; 
		if (bgIconSource != null && bgIconSource instanceof ShapeIconSource) {
			return ((ShapeIconSource) bgIconSource).getCurrStrokeColor();
		} else {
			if (pressed) {
				return Colors.defaultStrokeColorSet.pressedColor;
			} else if (checked) {
				return Colors.defaultStrokeColorSet.selectedColor;
			} else {
				return Colors.defaultStrokeColorSet.defaultColor;
			}
		}
	}
}
