package com.kh.beatbot.ui.view.page;

import java.io.File;

import com.kh.beatbot.event.SampleSetEvent;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.Menu;
import com.kh.beatbot.ui.view.menu.FileMenuItem;

public class BrowsePage extends Menu {

	protected synchronized void createMenuItems() {
		File[] topLevelDirs = new File[] { FileManager.drumsDirectory,
				FileManager.recordDirectory, FileManager.rootDirectory };

		for (File topLevelDir : topLevelDirs) {
			topLevelItems.add(new FileMenuItem(this, null, topLevelDir));
		}

		initBgRect(null, Colors.LABEL_SELECTED, null);
	}

	public void fileItemReleased(FileMenuItem fileItem) {
		new SampleSetEvent(TrackManager.currTrack, fileItem.getFile())
				.execute();
	}

	protected float getWidthForLevel(int level) {
		return width / 4;
	}
}
