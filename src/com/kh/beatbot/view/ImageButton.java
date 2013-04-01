package com.kh.beatbot.view;

import com.kh.beatbot.global.Icon;
import com.kh.beatbot.global.IconSource;

public class ImageButton extends Button {

	protected IconSource iconSource;
	protected Icon currentIcon;
	
	public ImageButton(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public IconSource getIconSource() {
		return iconSource;
	}
	
	public void setIconSource(IconSource iconSource) {
		this.iconSource = iconSource;
		if (iconSource.disabledIcon != null) {
			currentIcon = iconSource.disabledIcon;
		} else {
			currentIcon = iconSource.defaultIcon;
		}
	}

	@Override
	protected void touch() {
		currentIcon = iconSource.selectedIcon;
		super.touch();
	}
	
	@Override
	protected void release(boolean sendEvent) {
		currentIcon = iconSource.defaultIcon;
		super.release(sendEvent);
	}
	
	public void setEnabled(boolean enabled) {
		currentIcon = enabled ? iconSource.defaultIcon : (iconSource.disabledIcon != null ?
				iconSource.disabledIcon : null);
		super.setEnabled(enabled);
	}

	public float getIconWidth() {
		return currentIcon.getWidth();
	}

	public float getIconHeight() {
		return currentIcon.getHeight();
	}
	
	@Override
	public void init() {
		//nothing to do
	}

	@Override
	public void draw() {
		if (currentIcon != null) {
			currentIcon.draw(absoluteX, root.getHeight() - absoluteY - height, width, height);
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
		// no children
	}
}
