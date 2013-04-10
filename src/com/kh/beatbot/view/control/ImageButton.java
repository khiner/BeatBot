package com.kh.beatbot.view.control;

import com.kh.beatbot.global.IconSource;

public class ImageButton extends Button {

	protected IconSource iconSource;
	
	public IconSource getIconSource() {
		return iconSource;
	}
	
	public void setIconSource(IconSource iconSource) {
		this.iconSource = iconSource;
	}

	@Override
	public void press() {
		super.press();
		iconSource.setState(IconSource.State.PRESSED);
	}
	
	@Override
	public void release() {
		super.release();
		iconSource.setState(IconSource.State.DEFAULT);
	}
	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		iconSource.setState(enabled ? IconSource.State.DEFAULT : IconSource.State.DISABLED);
	}

	public float getIconWidth() {
		return iconSource.getWidth();
	}

	public float getIconHeight() {
		return iconSource.getWidth();
	}
	
	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public void draw() {
		if (iconSource != null) {
			iconSource.draw(absoluteX, root.getHeight() - absoluteY - height, width, height);
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
