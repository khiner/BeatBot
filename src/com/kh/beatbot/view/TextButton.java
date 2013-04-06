package com.kh.beatbot.view;

import com.kh.beatbot.global.ColorSet;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.global.RoundedRectIconSource;

public class TextButton extends ToggleButton {

	private String text = "";
	private float iconOffset = 0, textWidth = 0, textHeight = 0;
	private float textXOffset = 0, textYOffset = 0;
	private ColorSet bgColorSet, strokeColorSet;
	private ToggleButton iconButton;

	private int defaultIconResource, pressedIconResource, selectedIconResource;

	public TextButton(TouchableSurfaceView parent, ColorSet bgColorSet,
			ColorSet strokeColorSet) {
		this(parent, bgColorSet, strokeColorSet, -1, -1, -1);
	}

	public TextButton(TouchableSurfaceView parent, ColorSet bgColorSet,
			ColorSet strokeColorSet, int defaultIconResource,
			int pressedIconResource, int selectedIconResource) {
		super(parent);
		this.bgColorSet = bgColorSet;
		this.strokeColorSet = strokeColorSet;
		this.defaultIconResource = defaultIconResource;
		this.pressedIconResource = pressedIconResource;
		this.selectedIconResource = selectedIconResource;
		if (defaultIconResource != -1 || pressedIconResource != -1
				|| selectedIconResource != -1) {
			iconButton = new ToggleButton((TouchableSurfaceView) root);
		} else {
			iconButton = null;
		}
	}

	@Override
	public void press() {
		super.press();
		if (iconButton != null) {
			iconButton.press();
		}
	}

	@Override
	public void release() {
		super.release();
		if (iconButton != null) {
			iconButton.release();
		}
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);
		if (iconButton != null) {
			iconButton.setChecked(checked);
		}
	}

	@Override
	protected void loadIcons() {
		setIconSource(new RoundedRectIconSource(width, height, bgColorSet,
				strokeColorSet));
		setText(text);

		if (iconButton != null) {
			iconButton.setIconSource(new ImageIconSource(defaultIconResource,
					pressedIconResource, selectedIconResource));
		}
	}

	@Override
	public void init() {
		if (text.isEmpty()) {
			return;
		}
		GLSurfaceViewBase.storeText(text);
		textHeight = 5 * height / 8;
		textWidth = GLSurfaceViewBase.getTextWidth(text, textHeight);
		textXOffset = (iconButton != null ? iconOffset + iconButton.width
				+ (width - iconButton.width - iconOffset) / 2 : width / 2)
				- textWidth / 2;
		textXOffset += 2; // cludgy magic number correction,
						  // but it corrects for something wierd in
						  // GLSurfaceViewBase.getTextWidth
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
		if (iconButton != null) { // draw optional icon
			iconButton.draw();
		}
		if (text != null) { // draw optional text
			float[] textColor = pressed ? strokeColorSet.pressedColor
					: checked ? strokeColorSet.selectedColor
							: strokeColorSet.defaultColor;
			drawText(text, textColor, (int) textHeight, textXOffset,
					textYOffset);
		}
	}

	@Override
	public void layoutChildren() {
		if (iconButton != null) { // layout optional icon
			iconOffset = height / 8;
			iconButton.layout(this, iconOffset, iconOffset, 3 * height / 4,
					3 * height / 4);
		}
	}
}
