package com.kh.beatbot.global;

public class BeatBotIconSource {
	BeatBotIcon defaultIcon = null;
	BeatBotIcon selectedIcon = null;
	
	public BeatBotIconSource() {
		
	}
	
	public BeatBotIconSource(int defaultIconResourceId, int selectedIconResourceId) {
		this(new BeatBotIcon(defaultIconResourceId), new BeatBotIcon(selectedIconResourceId));
	}
	
	public BeatBotIconSource(BeatBotIcon defaultIcon, BeatBotIcon selectedIcon) {
		this.defaultIcon = defaultIcon;
		this.selectedIcon = selectedIcon;
	}
	
	public void set(int defaultIconResourceId, int selectedIconResourceId) {
		this.defaultIcon = new BeatBotIcon(defaultIconResourceId);
		this.selectedIcon = new BeatBotIcon(selectedIconResourceId);
	}
}
