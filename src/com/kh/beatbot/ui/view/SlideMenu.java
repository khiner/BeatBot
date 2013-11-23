package com.kh.beatbot.ui.view;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.DirectoryManager;
import com.kh.beatbot.manager.MidiFileManager;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.page.Page;

public class SlideMenu extends TouchableView {

	private class OnMidiItemReleaseListener implements OnReleaseListener {
		@Override
		public void onRelease(Button button) {
			MidiFileManager.importMidi(button.getText());
		}
	}

	private class MenuItem {
		private MenuItem parent = null;
		private ImageButton button;
		private List<MenuItem> subMenuItems = new ArrayList<MenuItem>();
		private ListView container = null;
		private int level = 0;

		public MenuItem(MenuItem parent, ImageButton button) {
			this.parent = parent;
			this.level = parent == null ? 0 : parent.level + 1;
			final MenuItem menuItem = this;
			this.button = button;
			button.setOnReleaseListener(new OnReleaseListener() {
				@Override
				public void onRelease(Button button) {
					menuItem.onRelease(button);
				}
			});

			container = menuLists.get(level);
		}

		public void onRelease(Button button) {
			selectMenuItem(this);
			layoutChildren();
			adjustWidth();
			Page.mainPage.notifyMenuExpanded();
		}

		public void addSubMenuItems(final MenuItem... subMenuItems) {
			for (MenuItem menuItem : subMenuItems) {
				this.subMenuItems.add(menuItem);
			}
		}

		public void loadIcons() {
			if (parent != null) {
				button.setBgIcon(new RoundedRectIcon(
						null,
						button instanceof ToggleButton ? Colors.menuToggleFillColorSet
								: Colors.menuItemFillColorSet));
				button.setStrokeColor(Colors.BLACK);
			}

			for (MenuItem subMenuItem : subMenuItems) {
				subMenuItem.loadIcons();
			}
		}

		public void setIcon(final Icon icon) {
			button.setIcon(icon);
		}

		public void setText(final String text) {
			button.setText(text);
		}

		public void setChecked(final boolean checked) {
			if (button instanceof ToggleButton) {
				((ToggleButton) button).setChecked(checked);
			}
		}

		public boolean isChecked() {
			if (button instanceof ToggleButton) {
				return ((ToggleButton) button).isChecked();
			}

			return false;
		}

		public void show() {
			container.add(button);
			if (!children.contains(container)) {
				addChild(container);
			}
		}

		public void hide() {
			container.remove(button);
			for (MenuItem child : subMenuItems) {
				child.hide();
			}
			if (container.children.isEmpty()) {
				removeChild(container);
			}
		}

		public void clearSubMenuItems() {
			for (MenuItem child : subMenuItems) {
				child.hide();
			}
			subMenuItems.clear();
		}

		public void select() {
			for (MenuItem sibling : getSiblings()) {
				if (sibling.equals(this)) {
					show();
					setChecked(true);
					for (MenuItem subMenuItem : subMenuItems) {
						subMenuItem.show();
						if (subMenuItem.isChecked()) {
							subMenuItem.select();
						}
					}
				} else {
					sibling.setChecked(false);
					for (MenuItem nephew : sibling.subMenuItems) {
						nephew.hide();
					}
				}
			}

			selectedItem = this;
		}

		private List<MenuItem> getSiblings() {
			return parent == null ? topLevelItems : parent.subMenuItems;
		}
	}

	private float columnWidth = 0;

	private MenuItem fileItem, settingsItem, snapToGridItem, midiImportItem,
			midiExportItem, selectedItem = null;

	private List<ListView> menuLists;
	private List<MenuItem> topLevelItems;

	private OnMidiItemReleaseListener midiItemReleaseListener;

	public synchronized void createChildren() {
		midiItemReleaseListener = new OnMidiItemReleaseListener();

		menuLists = new ArrayList<ListView>();
		for (int i = 0; i < 3; i++) {
			menuLists.add(new ListView());
		}

		topLevelItems = new ArrayList<MenuItem>();

		fileItem = new MenuItem(null, new ToggleButton());
		settingsItem = new MenuItem(null, new ToggleButton());
		snapToGridItem = new MenuItem(settingsItem, new ToggleButton());
		midiImportItem = new MenuItem(fileItem, new ToggleButton());
		midiExportItem = new MenuItem(fileItem, new ImageButton());

		topLevelItems.add(fileItem);
		topLevelItems.add(settingsItem);

		snapToGridItem.button.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				MidiManager
						.setSnapToGrid(((ToggleButton) snapToGridItem.button)
								.isChecked());
			}
		});

		midiExportItem.button.setOnReleaseListener(new OnReleaseListener() {
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
			MenuItem midiMenuItem = new MenuItem(midiImportItem,
					new ImageButton());
			midiMenuItem.button.setOnReleaseListener(midiItemReleaseListener);
			midiMenuItem.button.setText(fileName);
			midiImportItem.addSubMenuItems(midiMenuItem);
		}
		if (initialized) {
			midiImportItem.loadIcons();
		}
	}

	public synchronized void loadIcons() {
		columnWidth = width;
		fileItem.setIcon(new Icon(IconResources.FILE));
		settingsItem.setIcon(new Icon(IconResources.SETTINGS));

		snapToGridItem.setIcon(new Icon(IconResources.SNAP_TO_GRID));
		snapToGridItem.setChecked(MidiManager.isSnapToGrid());
		snapToGridItem.setText("Snap-to-grid");

		midiImportItem.setIcon(new Icon(IconResources.MIDI_IMPORT));
		midiExportItem.setIcon(new Icon(IconResources.MIDI_EXPORT));
		midiImportItem.setText("Import MIDI");
		midiExportItem.setText("Export MIDI");

		for (MenuItem menuItem : topLevelItems) {
			menuItem.loadIcons();
		}
	}

	public synchronized void layoutChildren() {
		float yOffset = LABEL_HEIGHT / 3;
		ListView.displayOffset = yOffset;
		float displayHeight = height - 2 * yOffset;
		menuLists.get(0).layout(this, 0, yOffset, columnWidth, displayHeight);
		menuLists.get(1).layout(this, columnWidth, yOffset, 2 * columnWidth,
				displayHeight);
		menuLists.get(2).layout(this, 3 * columnWidth, yOffset,
				2 * columnWidth, displayHeight);
	}

	private synchronized void selectMenuItem(MenuItem menuItem) {
		menuItem.setChecked(true);

		if (!menuItem.equals(selectedItem)) {
			menuItem.select();
		}
	}

	// adjust width of this view to fit all children
	private synchronized void adjustWidth() {
		float maxX = 0;
		for (View child : children) {
			if (child.absoluteX + child.width > maxX) {
				maxX = child.absoluteX + child.width;
			}
		}
		width = maxX;
	}
}
