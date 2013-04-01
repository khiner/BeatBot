package com.kh.beatbot.global;

public abstract class IconSource {
	public Icon defaultIcon, selectedIcon, disabledIcon, pressedIcon;
	
	public IconSource() {}
	
	public IconSource(Icon defaultIcon, Icon selectedIcon) {
		this.defaultIcon = defaultIcon;
		this.selectedIcon = selectedIcon;
	}

	public void setDisabledIcon(Icon disabledIcon) {
		this.disabledIcon = disabledIcon;
	}
	
	public void setPressedIcon(Icon pressedIcon) {
		this.pressedIcon = pressedIcon;
	}
	
	public float getWidth() {
		return defaultIcon.getWidth();
	}
	
	public float getHeight() {
		return defaultIcon.getHeight();
	}
}
