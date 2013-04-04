package com.kh.beatbot.global;

public abstract class IconSource {
	public Icon defaultIcon, selectedIcon, disabledIcon, pressedIcon;
	
	public IconSource() {}
	
	public IconSource(Icon defaultIcon, Icon pressedIcon, Icon selectedIcon, Icon disabledIcon) {
		this.defaultIcon = defaultIcon;
		this.pressedIcon = pressedIcon;
		this.selectedIcon = selectedIcon;
		this.disabledIcon = disabledIcon;
	}
	
	public float getWidth() {
		return defaultIcon.getWidth();
	}
	
	public float getHeight() {
		return defaultIcon.getHeight();
	}
}
