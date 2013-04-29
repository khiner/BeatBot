package com.kh.beatbot.global;

public abstract class IconSource implements Drawable {
	public enum State {DEFAULT, PRESSED, SELECTED, DISABLED}
	
	protected Drawable currentIcon, defaultIcon, selectedIcon, disabledIcon, pressedIcon;

	public IconSource() {};
	
	public IconSource(Drawable defaultIcon, Drawable pressedIcon,
			Drawable selectedIcon, Drawable disabledIcon) {
		this.defaultIcon = defaultIcon;
		this.pressedIcon = pressedIcon;
		this.selectedIcon = selectedIcon;
		this.disabledIcon = disabledIcon;
		setState(State.DISABLED);
	}
	
	public float getWidth() {
		return defaultIcon.getWidth();
	}

	public float getHeight() {
		return defaultIcon.getHeight();
	}
	
	public void setDimensions(float width, float height) {
		if (defaultIcon != null) {
			defaultIcon.setDimensions(width, height);
		}
		if (selectedIcon != null) {
			selectedIcon.setDimensions(width, height);
		}
		if (disabledIcon != null) {
			disabledIcon.setDimensions(width, height);
		}
		if (pressedIcon != null) {
			pressedIcon.setDimensions(width, height);
		}
	}
	
	public void setPosition(float x, float y) {
		// TODO
	}
	
	public void setState(State state) {
		setIcon(whichIcon(state));
	}
	
	protected void setIcon(Drawable icon) {
		if (icon != null) {
			currentIcon = icon;
		} else {
			currentIcon = defaultIcon;
		}
	}
	
	private Drawable whichIcon(State state) {
		switch (state) {
		case DEFAULT: return defaultIcon;
		case PRESSED: return pressedIcon;
		case SELECTED: return selectedIcon;
		case DISABLED: return disabledIcon;
		default: return defaultIcon;
		}
	}
	
	public float getX() {
		return defaultIcon.getX();
	}
	
	public float getY() {
		return defaultIcon.getX();
	}
	
	public void draw() {
		draw(getWidth(), getHeight());
	}
	
	public void draw(float width, float height) {
		draw(getX(), getY(), width, height);
	}
	
	public void draw(float x, float y, float width, float height) {
		if (currentIcon != null) {
			currentIcon.draw(x, y, width, height);
		}
	}
}
