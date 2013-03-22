package com.kh.beatbot.view;

import com.kh.beatbot.global.BBIcon;
import com.kh.beatbot.global.BBIconSource;
import com.kh.beatbot.listener.BBOnClickListener;

public class Button extends TouchableBBView {
	BBOnClickListener listener;
	BBIconSource iconSource;
	BBIcon currentIcon;
	
	public Button(TouchableSurfaceView parent) {
		super(parent);
	}

	public BBOnClickListener getOnClickListener() {
		return listener;
	}
	
	public void setOnClickListener(BBOnClickListener listener) {
		this.listener = listener;
	}
	
	public BBIconSource getIconSource() {
		return iconSource;
	}
	
	public void setIconSource(BBIconSource iconSource) {
		this.iconSource = iconSource;
		if (iconSource.disabledIcon != null) {
			currentIcon = iconSource.disabledIcon;
		} else {
			currentIcon = iconSource.defaultIcon;
		}
	}

	protected void touch() {
		currentIcon = iconSource.selectedIcon;
		notifyClicked();
	}

	protected void release(boolean sendEvent) {
		currentIcon = iconSource.defaultIcon;
		notifyClicked();
	}

	public float getIconWidth() {
		return currentIcon.getWidth();
	}

	public float getIconHeight() {
		return currentIcon.getHeight();
	}

	public void setEnabled(boolean enabled) {
		currentIcon = enabled ? iconSource.defaultIcon : (iconSource.disabledIcon != null ?
				iconSource.disabledIcon : null);
	}
	
	public boolean isEnabled() {
		return currentIcon != iconSource.defaultIcon;
	}
	
	public boolean isTouched() {
		return currentIcon.equals(iconSource.selectedIcon);
	}
	
	protected void notifyClicked() {
		if (listener != null)
			listener.onClick(this);
	}
	
	@Override
	public void init() {
		//nothing to do
	}

	@Override
	public void draw() {
		if (currentIcon != null)
			currentIcon.draw(absoluteX, root.getHeight() - absoluteY - height, width, height);
	}

	@Override
	protected void handleActionDown(int id, float x, float y) {
		touch();
	}

	@Override
	protected void handleActionUp(int id, float x, float y) {
		release(x >= 0 && x <= width && y >= 0 && y <= height);
	}

	@Override
	protected void handleActionPointerDown(int id, float x, float y) {
		// only one pointer on button		
	}

	@Override
	protected void handleActionPointerUp(int id, float x, float y) {
		// only one pointer on button
	}

	@Override
	protected void handleActionMove(int id, float x, float y) {
		if (x < 0 || x > width || y < 0 || y > height) {
			release(false);
		}
	}

	@Override
	protected void loadIcons() {
		// TODO add icons eventually
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
