package com.kh.beatbot.global;

import com.kh.beatbot.listener.BBOnClickListener;
import com.kh.beatbot.view.TouchableSurfaceView;
import com.kh.beatbot.view.window.TouchableViewWindow;

public class BBButton extends TouchableViewWindow {
	BBOnClickListener listener;
	
	BBIcon defaultIcon = null;
	BBIcon selectedIcon = null;
	BBIcon currentIcon = null;
	
	public BBButton(TouchableSurfaceView parent) {
		super(parent);
	}

	public void setOnClickListener(BBOnClickListener listener) {
		this.listener = listener;
	}
	
	public void setIconSource(BBIconSource iconSource) {
		defaultIcon = iconSource.defaultIcon;
		selectedIcon = iconSource.selectedIcon;
		currentIcon = defaultIcon;
	}

	protected void touch() {
		currentIcon = selectedIcon;
		notifyClicked();
		requestRender();
	}

	protected void release(boolean sendEvent) {
		currentIcon = defaultIcon;
		notifyClicked();
		requestRender();
	}

	public float getIconWidth() {
		return currentIcon.getWidth();
	}

	public float getIconHeight() {
		return currentIcon.getHeight();
	}

	public boolean isTouched() {
		return currentIcon.equals(selectedIcon);
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
		currentIcon.draw(absoluteX, parent.getHeight() - absoluteY - height, width, height);
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
	protected void layoutChildren() {
		// no children
	}
}
