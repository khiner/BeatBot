package com.kh.beatbot.ui.view.menu;

import java.util.ArrayList;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.DirectoryManager;
import com.kh.beatbot.manager.MidiFileManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.view.ListView;
import com.kh.beatbot.ui.view.Menu;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class MainMenu extends Menu {

	private class OnMidiItemReleaseListener implements OnReleaseListener {
		@Override
		public void onRelease(Button button) {
			MidiFileManager.importMidi(button.getText());
		}
	}

	private MenuItem fileItem, settingsItem, snapToGridItem, midiImportItem,
			midiExportItem;

	private OnMidiItemReleaseListener midiItemReleaseListener;

	public synchronized void createChildren() {
		midiItemReleaseListener = new OnMidiItemReleaseListener();

		menuLists = new ArrayList<ListView>();
		for (int i = 0; i < 3; i++) {
			menuLists.add(new ListView());
		}

		topLevelItems = new ArrayList<MenuItem>();

		fileItem = new MenuItem(this, null, new ToggleButton());
		settingsItem = new MenuItem(this, null, new ToggleButton());
		snapToGridItem = new MenuItem(this, settingsItem, new ToggleButton());
		midiImportItem = new MenuItem(this, fileItem, new ToggleButton());
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

		addChild(menuLists.get(0));

		fileItem.show();
		settingsItem.show();
		updateMidiList();
	}

	public void updateMidiList() {
		midiImportItem.clearSubMenuItems();
		final String[] fileNames = DirectoryManager.midiDirectory.list();
		for (String fileName : fileNames) {
			MenuItem midiMenuItem = new MenuItem(this, midiImportItem,
					new ImageButton());
			midiMenuItem.setOnReleaseListener(midiItemReleaseListener);
			midiMenuItem.setText(fileName);
			midiImportItem.addSubMenuItems(midiMenuItem);
		}
		if (initialized) {
			midiImportItem.loadIcons();
		}
	}

	public synchronized void loadIcons() {
		super.loadIcons();
		fileItem.setIcon(new Icon(IconResources.FILE));
		settingsItem.setIcon(new Icon(IconResources.SETTINGS));

		snapToGridItem.setIcon(new Icon(IconResources.SNAP_TO_GRID));
		snapToGridItem.setChecked(MidiManager.isSnapToGrid());
		snapToGridItem.setText("Snap-to-grid");

		midiImportItem.setIcon(new Icon(IconResources.MIDI_IMPORT));
		midiExportItem.setIcon(new Icon(IconResources.MIDI_EXPORT));
		midiImportItem.setText("Import MIDI");
		midiExportItem.setText("Export MIDI");
	}
}
