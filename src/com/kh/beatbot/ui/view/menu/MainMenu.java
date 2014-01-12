package com.kh.beatbot.ui.view.menu;

import java.io.File;

import android.annotation.SuppressLint;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.FileManager;
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
		fileItem = new MenuItem(this, null, new ToggleButton(false));
		settingsItem = new MenuItem(this, null, new ToggleButton(false));
		snapToGridItem = new MenuItem(this, settingsItem, new ToggleButton(false));
		midiImportItem = new FileMenuItem(this, fileItem, new File(
				FileManager.midiDirectory.getPath()));
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

	public synchronized void initIcons() {
		super.initIcons();
		fileItem.setIcon(new Icon(IconResources.FILE));
		settingsItem.setIcon(new Icon(IconResources.SETTINGS));

		snapToGridItem.setIcon(new Icon(IconResources.SNAP_TO_GRID));
		snapToGridItem.setChecked(MidiManager.isSnapToGrid());

		midiImportItem.setIcon(new Icon(IconResources.MIDI_IMPORT));
		midiExportItem.setIcon(new Icon(IconResources.MIDI_EXPORT));
	}

	// adjust width of this view to fit all children
	public synchronized void adjustWidth() {
		float maxX = 0;
		for (View child : children) {
			if (child.absoluteX + child.width > maxX) {
				maxX = child.absoluteX + child.width;
			}
		}
		width = maxX;
	}

	public void fileItemReleased(FileMenuItem fileItem) {
		MidiFileManager.importMidi(fileItem.getText());
	}

	@Override
	public void onMenuItemReleased(MenuItem menuItem) {
		super.onMenuItemReleased(menuItem);
		adjustWidth();
		View.mainPage.notifyMenuExpanded();
	}

	@SuppressLint("DefaultLocale")
	@Override
	public boolean accept(File file) {
		return file.getName().toLowerCase().endsWith(".midi");
	}
}
