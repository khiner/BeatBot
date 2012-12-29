package com.kh.beatbot.global;

public class BeatBotIconSource {
	public BeatBotIcon defaultIcon = null;
	public BeatBotIcon selectedIcon = null;
	public BeatBotIcon listViewIcon = null;
	
	public BeatBotIconSource() {
		
	}
	
	public BeatBotIconSource(int defaultIconResourceId, int selectedIconResourceId) {
		this(new BeatBotIcon(defaultIconResourceId), new BeatBotIcon(selectedIconResourceId), null);
	}
	
	public BeatBotIconSource(int defaultIconResourceId, int selectedIconResourceId, int listViewIconResourceId) {
		this(new BeatBotIcon(defaultIconResourceId), new BeatBotIcon(selectedIconResourceId), new BeatBotIcon(listViewIconResourceId));
	}
	
	public BeatBotIconSource(BeatBotIcon defaultIcon, BeatBotIcon selectedIcon, BeatBotIcon listViewIcon) {
		this.defaultIcon = defaultIcon;
		this.selectedIcon = selectedIcon;
		this.listViewIcon = listViewIcon;
	}
	
	public void set(int defaultIconResourceId, int selectedIconResourceId) {
		if (defaultIconResourceId > 0)
			this.defaultIcon = new BeatBotIcon(defaultIconResourceId);
		if (selectedIconResourceId > 0)
			this.selectedIcon = new BeatBotIcon(selectedIconResourceId);
	}
	
	public void set(int defaultIconResourceId, int selectedIconResourceId, int listViewIconResourceId) {
		set(defaultIconResourceId, selectedIconResourceId);
		if (listViewIconResourceId > 0)
			this.listViewIcon = new BeatBotIcon(listViewIconResourceId);
	}
}
