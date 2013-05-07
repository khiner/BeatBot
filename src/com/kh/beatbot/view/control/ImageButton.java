package com.kh.beatbot.view.control;

import com.kh.beatbot.global.IconSource;

public class ImageButton extends Button {

	protected final int BACKGROUND_ICON_INDEX = 0;
	protected final int FOREGROUND_ICON_INDEX = 1;
	protected float iconOffset = 0, iconW = 0, iconH = 0;

	// two icon sources - foreground and background
	protected IconSource[] iconSources;

	public ImageButton() {
		super();
		iconSources = new IconSource[2];
	}
	
	public IconSource getIconSource() {
		return iconSources[FOREGROUND_ICON_INDEX];
	}

	public void setIconSource(IconSource iconSource) {
		iconSources[FOREGROUND_ICON_INDEX] = iconSource;
		layoutIcons();
	}

	public void setBgIconSource(IconSource bgIconSource) {
		iconSources[BACKGROUND_ICON_INDEX] = bgIconSource;
		layoutIcons();
	}

	@Override
	public void press() {
		super.press();
		for (IconSource iconSource : iconSources) {
			if (iconSource != null) {
				iconSource.setState(IconSource.State.PRESSED);
			}
		}
	}

	@Override
	public void release() {
		super.release();
		for (IconSource iconSource : iconSources) {
			if (iconSource != null) {
				iconSource.setState(IconSource.State.DEFAULT);
			}
		}
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (IconSource iconSource : iconSources) {
			if (iconSource != null) {
				iconSource.setState(enabled ? IconSource.State.DEFAULT
						: IconSource.State.DISABLED);
			}
		}
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public void draw() {
		IconSource bgIconSource = iconSources[BACKGROUND_ICON_INDEX];
		IconSource foregroundIconSource = iconSources[FOREGROUND_ICON_INDEX];
		if (bgIconSource != null) {
			bgIconSource.draw();
		}
		if (foregroundIconSource != null) {
			foregroundIconSource.draw(absoluteX + iconOffset,
					root.getHeight() - absoluteY - height + iconOffset, iconW,
					iconH);
		}
	}

	@Override
	protected void loadIcons() {
		// icon is set from elsewhere.
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
		IconSource bgIconSource = iconSources[BACKGROUND_ICON_INDEX];
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
}
