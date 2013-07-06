package com.kh.beatbot.ui.view.control;

import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.ShapeIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.GLSurfaceViewBase;
import com.kh.beatbot.ui.view.View;

public class ImageButton extends Button {

	protected final int BACKGROUND_ICON_INDEX = 0;
	protected final int FOREGROUND_ICON_INDEX = 1;
	protected float iconOffset = 0, iconW = 0, iconH = 0;

	private String text = "";
	private float textWidth = 0, textHeight = 0;
	private float textXOffset = 0, textYOffset = 0;

	// two icon sources - foreground and background
	protected Icon[] icons;

	public ImageButton() {
		super();
		icons = new Icon[2];
	}

	public Icon getIcon() {
		return icons[FOREGROUND_ICON_INDEX];
	}

	public Icon getBgIcon() {
		return icons[BACKGROUND_ICON_INDEX];
	}

	public void setIcon(Icon icon) {
		icons[FOREGROUND_ICON_INDEX] = icon;
		layoutIcons();
		init();
	}

	public void setBgIcon(Icon bgIcon) {
		icons[BACKGROUND_ICON_INDEX] = bgIcon;
		layoutIcons();
	}

	@Override
	public void press() {
		super.press();
		for (Icon iconSource : icons) {
			if (iconSource != null) {
				iconSource.setState(IconResource.State.PRESSED);
			}
		}
	}

	@Override
	public void release() {
		super.release();
		for (Icon iconSource : icons) {
			if (iconSource != null) {
				iconSource.setState(IconResource.State.DEFAULT);
			}
		}
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (Icon iconSource : icons) {
			if (iconSource != null) {
				iconSource.setState(enabled ? IconResource.State.DEFAULT
						: IconResource.State.DISABLED);
			}
		}
	}

	@Override
	public void draw() {
		Icon foregroundIconSource = getIcon();
		Icon bgIconSource = getBgIcon();

		if (bgIconSource != null) {
			bgIconSource.draw();
		}
		if (foregroundIconSource != null) {
			foregroundIconSource.draw(absoluteX + iconOffset, root.getHeight()
					- absoluteY - height + iconOffset, iconW, iconH);
		}

		if (text != null) { // draw optional text
			float[] textColor = calcStrokeColor();
			drawText(text, textColor, (int) textHeight, textXOffset,
					textYOffset);
		}
	}

	@Override
	protected void loadIcons() {
		setText(text);
		init();
	}

	@Override
	protected void createChildren() {
		// no children
	}

	@Override
	public void layoutChildren() {
		layoutIcons();
	}

	protected void layoutIcons() {
		Icon bgIconSource = getBgIcon();
		if (bgIconSource != null) {
			// if there is a bg shape, we shrink the icon a bit to avoid overlap
			iconOffset = height / 10;
			iconW = 4 * height / 5;
			iconH = 4 * height / 5;
		} else {
			iconOffset = 0;
			iconW = width;
			iconH = height;
		}
		if (bgIconSource != null) {
			bgIconSource.layout(absoluteX, absoluteY, width, height);
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
		textXOffset = (getIcon() != null ? iconOffset + iconW
				+ (width - iconW - iconOffset) / 2 : width / 2)
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

	public void layout(View parent, float x, float y, float width,
			float height) {
		super.layout(parent, x, y, width, height);
		init();
	}

	private float[] calcStrokeColor() {
		Icon bgIconSource = getBgIcon();
		if (bgIconSource != null && bgIconSource instanceof ShapeIcon) {
			float[] color = ((ShapeIcon) bgIconSource).getCurrStrokeColor();
			return color != null ? color : Colors.WHITE;
		} else {
			if (pressed) {
				return Colors.defaultStrokeColorSet.pressedColor;
			} else {
				return Colors.defaultStrokeColorSet.defaultColor;
			}
		}
	}
}
