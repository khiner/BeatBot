package com.kh.beatbot.global;

public class BBIconSource {
	public BBIcon defaultIcon, selectedIcon, disabledIcon, pressedIcon;
	
	public int listViewIconResource = -1;
	public int listTitleIconResource = -1;

	public BBIconSource() {
		
	}

	public float getWidth() {
		return defaultIcon.getWidth();
	}
	
	public float getHeight() {
		return defaultIcon.getHeight();
	}
	
	public BBIconSource(int defaultIconResourceId, int selectedIconResourceId) {
		this(defaultIconResourceId, selectedIconResourceId, -1);
	}

	public BBIconSource(int defaultIconResourceId, int selectedIconResourceId,
			int listViewIconResourceId) {
		this(defaultIconResourceId, selectedIconResourceId, listViewIconResourceId, -1);
	}

	public BBIconSource(int defaultIconResourceId, int selectedIconResourceId,
			int listViewIconResourceId, int listTitleIconResourceId) {
		this.defaultIcon = defaultIconResourceId == -1 ? null : new BBIcon(defaultIconResourceId);
		this.selectedIcon = selectedIconResourceId == -1 ? null : new BBIcon(selectedIconResourceId);
		this.listViewIconResource = listViewIconResourceId;
		this.listTitleIconResource = listTitleIconResourceId;
	}

	public void setDisabledIcon(int disabledResourceId) {
		this.disabledIcon = new BBIcon(disabledResourceId);
	}
	
	public void setPressedIcon(int pressedResourceId) {
		this.pressedIcon = new BBIcon(pressedResourceId);
	}
}
