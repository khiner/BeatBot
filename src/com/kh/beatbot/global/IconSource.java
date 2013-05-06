package com.kh.beatbot.global;

public abstract class IconSource extends Drawable {
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
	
	@Override
	public void layout(float x, float y, float width, float height) {
		super.layout(x, y, width, height);
		if (defaultIcon != null) {
			defaultIcon.layout(x, y, width, height);
		}
		if (selectedIcon != null) {
			selectedIcon.layout(x, y, width, height);
		}
		if (disabledIcon != null) {
			disabledIcon.layout(x, y, width, height);
		}
		if (pressedIcon != null) {
			pressedIcon.layout(x, y, width, height);
		}
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

	public void draw() {
		if (currentIcon != null) {
			currentIcon.draw();
		}
	}
}
