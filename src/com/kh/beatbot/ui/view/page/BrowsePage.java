package com.kh.beatbot.ui.view.page;

import java.io.File;

import com.kh.beatbot.event.SampleSetEvent;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.color.ColorSet;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.Menu;
import com.kh.beatbot.ui.view.menu.FileMenuItem;

public class BrowsePage extends Menu {

	private static ColorSet bgColorSet = new ColorSet(Colors.LABEL_SELECTED);

	protected synchronized void createMenuItems() {
		File[] topLevelDirs = new File[] { FileManager.drumsDirectory,
				FileManager.recordDirectory, FileManager.rootDirectory };

		for (File topLevelDir : topLevelDirs) {
			topLevelItems.add(new FileMenuItem(this, null, topLevelDir));
		}

		initBgRect(true, null, bgColorSet, null);
	}

	public void fileItemReleased(FileMenuItem fileItem) {
		new SampleSetEvent(TrackManager.currTrack, fileItem.getFile())
				.execute();
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			return children != null && children.length > 0;
		} else {
			for (String extension : FileManager.SUPPORTED_EXTENSIONS) {
				if (file.getName().toLowerCase().endsWith(extension)) {
					return true;
				}
			}
			return false;
		}
	}

	protected float getWidthForLevel(int level) {
		return width / 4;
	}
}
