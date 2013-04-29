package com.kh.beatbot.view.control;

import com.kh.beatbot.global.ColorSet;
import com.kh.beatbot.global.Colors;
import com.kh.beatbot.global.IconSource;
import com.kh.beatbot.global.ImageIconSource;
import com.kh.beatbot.global.RoundedRectIconSource;
import com.kh.beatbot.view.GLSurfaceViewBase;
import com.kh.beatbot.view.mesh.ShapeGroup;

public class TextButton extends ToggleButton {

	private ShapeGroup globalGroup;

	private String text = "";
	private float iconOffset = 0, textWidth = 0, textHeight = 0;
	private float textXOffset = 0, textYOffset = 0;
	private ColorSet bgColorSet, strokeColorSet;
	private ToggleButton iconButton;

	private int defaultIconResource, pressedIconResource, selectedIconResource;

	private boolean iconEnabled = true;
	
	public TextButton() {
		strokeColorSet = Colors.defaultStrokeColorSet;
		globalGroup = null;
		bgColorSet = null;
		iconButton = null;
	}

	public TextButton(ShapeGroup globalGroup, ColorSet bgColorSet,
			ColorSet strokeColorSet) {
		this(globalGroup, bgColorSet, strokeColorSet, -1, -1, -1);
	}

	public TextButton(ShapeGroup globalGroup, ColorSet bgColorSet,
			ColorSet strokeColorSet, int defaultIconResource,
			int pressedIconResource, int selectedIconResource) {
		super();
		this.globalGroup = globalGroup;
		this.bgColorSet = bgColorSet;
		this.strokeColorSet = strokeColorSet;
		this.defaultIconResource = defaultIconResource;
		this.pressedIconResource = pressedIconResource;
		this.selectedIconResource = selectedIconResource;
		if (defaultIconResource != -1 || pressedIconResource != -1
				|| selectedIconResource != -1) {
			iconButton = new ToggleButton();
		} else {
			iconButton = null;
		}
	}

	public void setForegroundIconSource(IconSource iconSource) {
		if (iconButton == null) {
			iconButton = new ToggleButton();
			layoutChildren();
		}
		iconButton.setIconSource(iconSource);
	}

	public void setIconEnabled(boolean iconEnabled) {
		this.iconEnabled = iconEnabled;
		init();
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
		setText(text);

		if (bgColorSet != null && strokeColorSet != null) {
			setIconSource(new RoundedRectIconSource(globalGroup,
					globalGroup == null ? 0 : absoluteX,
					globalGroup == null ? 0 : absoluteY, width, height,
					bgColorSet, strokeColorSet));
		}

		if (iconButton != null && iconButton.getIconSource() == null) {
			iconButton.setIconSource(new ImageIconSource(defaultIconResource,
					pressedIconResource, selectedIconResource));
		}
	}

	@Override
	public void init() {
		if (text.isEmpty() || !GLSurfaceViewBase.isInitialized()) {
			return;
		}
		GLSurfaceViewBase.storeText(text);
		textHeight = 5 * height / 8;
		textWidth = GLSurfaceViewBase.getTextWidth(text, textHeight);
		textXOffset = (iconButton != null && iconEnabled ? iconOffset + iconButton.width
				+ (width - iconButton.width - iconOffset) / 2 : width / 2)
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
	
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		if (iconSource != null) {
			iconSource.setPosition(x, y);
		}
	}
	
	public void setDimensions(float width, float height) {
		super.setDimensions(width, height);
		init();
		if (iconSource != null) {
			iconSource.setDimensions(width, height);
		}
	}
	
	@Override
	public void draw() {
		super.draw();
		if (iconButton != null && iconEnabled) { // draw optional icon
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
		if (iconButton != null && iconEnabled) { // layout optional icon
			iconOffset = height / 10;
			iconButton.layout(this, iconOffset, iconOffset, 4 * height / 5,
					4 * height / 5);
		}
	}
}
