package com.kh.beatbot.ui.view.page;

import java.io.File;

import android.annotation.SuppressLint;
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

	@SuppressLint("DefaultLocale")
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
