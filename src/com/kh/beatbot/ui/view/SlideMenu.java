package com.kh.beatbot.ui.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.MidiFileManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.page.Page;

public class SlideMenu extends TouchableView {

	private class MenuItemReleaseListener implements OnReleaseListener {
		@Override
		public void onRelease(Button button) {
			selectMenuItem((ToggleButton) button);
			width = columnWidth * 3;
			Page.mainPage.notifyMenuExpanded();
		}
	}

	private float offset = 0, columnWidth = 0, iconWidth, textHeight;

	private ShapeGroup shapeGroup;

	private ToggleButton fileButton, settingsButton, snapToGridButton,
			selectedMenuItem = null;

	private ImageButton midiImportButton, midiExportButton;

	private Map<ToggleButton, List<ImageButton>> menuHeirarchy;

	private MenuItemReleaseListener menuItemReleaseListener;

	public synchronized void createChildren() {
		menuItemReleaseListener = new MenuItemReleaseListener();
		shapeGroup = new ShapeGroup();

		fileButton = new ToggleButton();
		settingsButton = new ToggleButton();
		snapToGridButton = new ToggleButton();
		midiImportButton = new ImageButton();
		midiExportButton = new ImageButton();

		menuHeirarchy = new HashMap<ToggleButton, List<ImageButton>>();

		fileButton.setOnReleaseListener(menuItemReleaseListener);
		settingsButton.setOnReleaseListener(menuItemReleaseListener);

		snapToGridButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				MidiManager.setSnapToGrid(snapToGridButton.isChecked());
			}
		});

		midiImportButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				MidiFileManager.chooseMidiFile();
			}
		});

		midiExportButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				BeatBotActivity.mainActivity
						.showDialog(BeatBotActivity.MIDI_FILE_NAME_EDIT_DIALOG_ID);
			}
		});

		menuHeirarchy.put(fileButton,
				Arrays.asList(midiImportButton, midiExportButton));
		menuHeirarchy.put(settingsButton,
				Arrays.asList((ImageButton) snapToGridButton));

		for (ToggleButton menuButton : menuHeirarchy.keySet()) {
			addChild(menuButton);
		}
	}

	public synchronized void loadIcons() {
		columnWidth = width;
		offset = width / 6;
		fileButton.setIcon(new Icon(IconResources.FILE));
		settingsButton.setIcon(new Icon(IconResources.SETTINGS));

		snapToGridButton.setIcon(new Icon(IconResources.SNAP_TO_GRID));
		snapToGridButton.setChecked(MidiManager.isSnapToGrid());
		snapToGridButton.setText("SNAP-TO-GRID");

		midiImportButton.setIcon(new Icon(IconResources.MIDI_IMPORT));
		midiExportButton.setIcon(new Icon(IconResources.MIDI_EXPORT));
		midiImportButton.setText("IMPORT MIDI");
		midiExportButton.setText("EXPORT MIDI");

		for (ToggleButton menuButton : menuHeirarchy.keySet()) {
			addChild(menuButton);
			for (ImageButton subMenuButton : menuHeirarchy.get(menuButton)) {
				subMenuButton.setBgIcon(new RoundedRectIcon(shapeGroup,
						Colors.menuItemFillColorSet));
				subMenuButton.setStrokeColor(Colors.BLACK);
				subMenuButton.destroy(); // remove it from its ShapeGroup
			}
		}

	}

	@Override
	public synchronized void draw() {
		shapeGroup.draw(this, -1);
	}

	public synchronized void layoutChildren() {
		iconWidth = columnWidth - offset * 2;
		textHeight = columnWidth / 4;
		fileButton.layout(this, offset, 0, iconWidth, iconWidth);
		settingsButton.layout(this, offset, iconWidth + offset, iconWidth,
				iconWidth);
		if (selectedMenuItem != null) {
			layoutSubMenuItems(selectedMenuItem);
		}
	}

	private synchronized void selectMenuItem(ToggleButton menuItem) {
		menuItem.setChecked(true);

		if (menuItem.equals(selectedMenuItem))
			return;

		for (ToggleButton menuButton : menuHeirarchy.keySet()) {
			if (!menuButton.equals(menuItem)) {
				menuButton.setChecked(false);
			}
			for (ImageButton subMenuButton : menuHeirarchy.get(menuButton)) {
				removeChild(subMenuButton);
			}
		}

		for (ImageButton subMenuButton : menuHeirarchy.get(menuItem)) {
			addChild(subMenuButton);
			if (subMenuButton instanceof ToggleButton) {
				((ToggleButton) subMenuButton)
						.setChecked(((ToggleButton) subMenuButton).isChecked());
			}
		}
		selectedMenuItem = menuItem;
	}

	private synchronized void layoutSubMenuItems(ToggleButton parentMenuItem) {
		List<ImageButton> subMenuItems = menuHeirarchy.get(parentMenuItem);
		for (int i = 0; i < subMenuItems.size(); i++) {
			subMenuItems.get(i).layout(this, columnWidth,
					offset + textHeight * i, columnWidth * 2 - offset,
					textHeight);
		}
	}
}
