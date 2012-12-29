package com.kh.beatbot.global;

public class BBIconSource {
	public BBIcon defaultIcon = null;
	public BBIcon selectedIcon = null;
	public BBIcon listViewIcon = null;

	public BBIconSource() {

	}

	public BBIconSource(int defaultIconResourceId, int selectedIconResourceId) {
		this(new BBIcon(defaultIconResourceId), new BBIcon(
				selectedIconResourceId), null);
	}

	public BBIconSource(int defaultIconResourceId, int selectedIconResourceId,
			int listViewIconResourceId) {
		this(new BBIcon(defaultIconResourceId), new BBIcon(
				selectedIconResourceId), new BBIcon(listViewIconResourceId));
	}

	public BBIconSource(BBIcon defaultIcon, BBIcon selectedIcon,
			BBIcon listViewIcon) {
		this.defaultIcon = defaultIcon;
		this.selectedIcon = selectedIcon;
		this.listViewIcon = listViewIcon;
	}

	public void set(int defaultIconResourceId, int selectedIconResourceId) {
		if (defaultIconResourceId > 0)
			this.defaultIcon = new BBIcon(defaultIconResourceId);
		if (selectedIconResourceId > 0)
			this.selectedIcon = new BBIcon(selectedIconResourceId);
	}

	public void set(int defaultIconResourceId, int selectedIconResourceId,
			int listViewIconResourceId) {
		set(defaultIconResourceId, selectedIconResourceId);
		if (listViewIconResourceId > 0)
			this.listViewIcon = new BBIcon(listViewIconResourceId);
	}
}
