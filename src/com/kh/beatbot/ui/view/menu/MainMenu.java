package com.kh.beatbot.ui.view.menu;

import java.io.File;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.DirectoryManager;
import com.kh.beatbot.manager.MidiFileManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.view.Menu;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class MainMenu extends Menu {

	private MenuItem fileItem, settingsItem, snapToGridItem, midiImportItem,
			midiExportItem;

	protected synchronized void createMenuItems() {
		fileItem = new MenuItem(this, null, new ToggleButton());
		settingsItem = new MenuItem(this, null, new ToggleButton());
		snapToGridItem = new MenuItem(this, settingsItem, new ToggleButton());
		midiImportItem = new FileMenuItem(this, fileItem, new File(
				DirectoryManager.midiDirectory.getPath()));
		midiExportItem = new MenuItem(this, fileItem, new ImageButton());

		topLevelItems.add(fileItem);
		topLevelItems.add(settingsItem);

		snapToGridItem.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				MidiManager.setSnapToGrid(((ToggleButton) button).isChecked());
			}
		});

		midiExportItem.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				midiExportItem.onRelease(button);
				BeatBotActivity.mainActivity
						.showDialog(BeatBotActivity.MIDI_FILE_NAME_EDIT_DIALOG_ID);
			}
		});

		fileItem.addSubMenuItems(midiImportItem, midiExportItem);
		settingsItem.addSubMenuItems(snapToGridItem);

		snapToGridItem.setText("Snap-to-grid");
		midiImportItem.setText("Import MIDI");
		midiExportItem.setText("Export MIDI");
	}

	protected float getWidthForLevel(int level) {
		return level == 0 ? columnWidth : 2 * columnWidth;
	}

	public synchronized void loadIcons() {
		super.loadIcons();
		fileItem.setIcon(new Icon(IconResources.FILE));
		settingsItem.setIcon(new Icon(IconResources.SETTINGS));

		snapToGridItem.setIcon(new Icon(IconResources.SNAP_TO_GRID));
		snapToGridItem.setChecked(MidiManager.isSnapToGrid());

		midiImportItem.setIcon(new Icon(IconResources.MIDI_IMPORT));
		midiExportItem.setIcon(new Icon(IconResources.MIDI_EXPORT));
	}

	public synchronized void adjustWidth() {
		super.adjustWidth();
		View.mainPage.notifyMenuExpanded();
	}

	public void fileItemReleased(FileMenuItem fileItem) {
		MidiFileManager.importMidi(fileItem.getText());
	}
}
