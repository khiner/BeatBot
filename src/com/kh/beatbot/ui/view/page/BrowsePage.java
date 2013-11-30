package com.kh.beatbot.ui.view.page;

import java.io.File;

import com.kh.beatbot.manager.DirectoryManager;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.Menu;
import com.kh.beatbot.ui.view.menu.FileMenuItem;

public class BrowsePage extends Menu {

	protected synchronized void createMenuItems() {
		FileMenuItem drumItem = new FileMenuItem(this, null, new File(
				DirectoryManager.drumsDirectory.getPath()));
		topLevelItems.add(drumItem);
	}

	public void fileItemReleased(FileMenuItem fileItem) {
		// new SampleSetEvent(TrackManager.currTrack,
		// ((Instrument) parent).getSample(item)).execute();
	}

	public void draw() {
		drawRectangle(0, 0, width, height, Colors.LABEL_SELECTED);
	}

	protected float getWidthForLevel(int level) {
		return width / 4;
	}

	public synchronized void adjustWidth() {
		// don't adjust width of menu when items are selected
	}
}
