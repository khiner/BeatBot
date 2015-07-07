package com.kh.beatbot.ui.view.page.track;

import java.io.File;
import java.util.List;

import com.kh.beatbot.event.SampleSetEvent;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.menu.FileMenuItem;
import com.kh.beatbot.ui.view.menu.Menu;
import com.kh.beatbot.ui.view.menu.MenuItem;

public class BrowsePage extends Menu implements TrackListener {
	private Track currTrack;

	public BrowsePage(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
		setScrollable(true, false);
	}

	protected synchronized void createMenuItems() {
		setIcon(IconResourceSets.BROWSE_PAGE);
		initRoundedRect();

		File[] topLevelDirs = new File[] { FileManager.drumsDirectory, FileManager.recordDirectory,
				FileManager.rootDirectory };

		for (File topLevelDir : topLevelDirs) {
			topLevelItems.add(new FileMenuItem(this, null, topLevelDir));
		}
	}

	@Override
	public void onSelect(BaseTrack track) {
		if (null == track || track.equals(currTrack)) {
			return;
		}

		this.currTrack = (Track) track;
		update();
	}

	public void update() {
		// recursively select file menu items based on current sample
		File currSampleFile = currTrack.getCurrSampleFile();
		if (null == currSampleFile)
			return;
		String currSamplePath = currSampleFile.getAbsolutePath();

		FileMenuItem match = null;
		while ((match = findMatchingChild(match, currSamplePath)) != null) {
			match.trigger();
		}
	}

	private FileMenuItem findMatchingChild(FileMenuItem parent, String path) {
		List<MenuItem> children = parent == null ? topLevelItems : parent.getSubMenuItems();
		for (MenuItem child : children) {
			FileMenuItem fileChild = (FileMenuItem) child;
			if (path.startsWith(fileChild.getFile().getAbsolutePath())) {
				return fileChild;
			}
		}
		return null;
	}

	@Override
	public void onFileMenuItemReleased(FileMenuItem fileItem) {
		new SampleSetEvent(TrackManager.getCurrTrack().getId(), fileItem.getFile()).execute();
	}

	@Override
	public void onFileMenuItemLongPressed(FileMenuItem fileItem) {
		context.editFileName(fileItem.getFile());
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

	public void onCreate(Track track) {
	}

	public void onDestroy(Track track) {
	}

	public void onSampleChange(Track track) {
		update();
	}

	public void onMuteChange(Track track, boolean mute) {
	}

	public void onSoloChange(Track track, boolean solo) {
	}
}
