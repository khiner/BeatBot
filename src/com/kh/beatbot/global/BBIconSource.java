package com.kh.beatbot.global;

public class BBIconSource {
	public BBIcon defaultIcon = null;
	public BBIcon selectedIcon = null;
	public int iconSource;
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
	
	public BBIconSource(int iconSource, int defaultIconResourceId, int selectedIconResourceId) {
		this(iconSource, defaultIconResourceId, selectedIconResourceId, -1);
	}

	public BBIconSource(int iconSource, int defaultIconResourceId, int selectedIconResourceId,
			int listViewIconResourceId) {
		this(iconSource, new BBIcon(defaultIconResourceId), new BBIcon(
				selectedIconResourceId), listViewIconResourceId, -1);
	}

	public BBIconSource(int iconSource, int defaultIconResourceId, int selectedIconResourceId,
			int listViewIconResourceId, int listTitleIconResourceId) {
		this(iconSource, new BBIcon(defaultIconResourceId), new BBIcon(
				selectedIconResourceId), listViewIconResourceId, listTitleIconResourceId);
	}
	
	public BBIconSource(int iconSource, BBIcon defaultIcon, BBIcon selectedIcon,
			int listViewIconResource, int listTitleIconResource) {
		this.defaultIcon = defaultIcon;
		this.selectedIcon = selectedIcon;
		this.iconSource = iconSource;
		this.listViewIconResource = listViewIconResource;
		this.listTitleIconResource = listTitleIconResource;
	}

	public void set(int iconSource, int defaultIconResourceId, int selectedIconResourceId) {
		this.iconSource = iconSource;
		if (defaultIconResourceId > 0)
			this.defaultIcon = new BBIcon(defaultIconResourceId);
		if (selectedIconResourceId > 0)
			this.selectedIcon = new BBIcon(selectedIconResourceId);
	}

	public void set(int iconSource, int defaultIconResourceId, int selectedIconResourceId,
			int listViewIconResourceId) {
		set(iconSource, defaultIconResourceId, selectedIconResourceId);
		this.listViewIconResource = listViewIconResourceId;
	}
	
	public void set(int iconSource, int defaultIconResourceId, int selectedIconResourceId,
			int listViewIconResourceId, int listTitleIconResourceId) {
		set(iconSource, defaultIconResourceId, selectedIconResourceId, listViewIconResourceId);
		this.listTitleIconResource = listTitleIconResourceId;
	}
}
