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

		public MenuItem(ImageButton button) {
			final MenuItem menuItem = this;
			this.button = button;
			button.setOnReleaseListener(new OnReleaseListener() {
				@Override
				public void onRelease(Button button) {
					menuItem.onRelease(button);
				}
			});
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
				menuItem.parent = this;
			}
		}

		public void layout(View parent, float x, float y, float width,
				float height) {
			button.layout(parent, x, y, width, height);
			layoutSubMenuItems(parent);
		}

		public void layoutSubMenuItems(View parent) {
			for (int i = 0; i < subMenuItems.size(); i++) {
				subMenuItems.get(i).layout(parent, button.x + button.width,
						offset + LABEL_HEIGHT * i, columnWidth * 2 - offset,
						LABEL_HEIGHT);
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

		public void remove() {
			removeChild(button);
			for (MenuItem child : subMenuItems) {
				child.remove();
			}
		}

		public void select() {
			for (MenuItem sibling : getSiblings()) {
				if (sibling.equals(this)) {
					setChecked(true);
					for (MenuItem subMenuItem : subMenuItems) {
						addChild(subMenuItem.button);
						if (subMenuItem.isChecked()) {
							subMenuItem.select();
						}
					}
				} else {
					sibling.setChecked(false);
					for (MenuItem nephew : sibling.subMenuItems) {
						nephew.remove();
					}
				}
			}

			selectedItem = this;
		}

		private List<MenuItem> getSiblings() {
			return parent == null ? topLevelItems : parent.subMenuItems;
		}
	}

	private float offset = 0, columnWidth = 0, iconWidth;

	private MenuItem fileItem, settingsItem, snapToGridItem, midiImportItem,
			midiExportItem, selectedItem = null;

	private List<MenuItem> topLevelItems;

	private OnMidiItemReleaseListener midiItemReleaseListener;

	public synchronized void createChildren() {
		topLevelItems = new ArrayList<MenuItem>();
		midiItemReleaseListener = new OnMidiItemReleaseListener();

		fileItem = new MenuItem(new ToggleButton());
		settingsItem = new MenuItem(new ToggleButton());
		snapToGridItem = new MenuItem(new ToggleButton());
		midiImportItem = new MenuItem(new ToggleButton());
		midiExportItem = new MenuItem(new ImageButton());

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

		for (MenuItem menuItem : topLevelItems) {
			addChild(menuItem.button);
		}

		final String[] fileNames = DirectoryManager.midiDirectory.list();
		for (String fileName : fileNames) {
			MenuItem midiMenuItem = new MenuItem(new ImageButton());
			midiMenuItem.button.setOnReleaseListener(midiItemReleaseListener);
			midiMenuItem.button.setText(fileName);
			midiImportItem.addSubMenuItems(midiMenuItem);
		}
	}

	public synchronized void loadIcons() {
		columnWidth = width;
		offset = width / 6;
		fileItem.setIcon(new Icon(IconResources.FILE));
		settingsItem.setIcon(new Icon(IconResources.SETTINGS));

		snapToGridItem.setIcon(new Icon(IconResources.SNAP_TO_GRID));
		snapToGridItem.setChecked(MidiManager.isSnapToGrid());
		snapToGridItem.setText("SNAP-TO-GRID");

		midiImportItem.setIcon(new Icon(IconResources.MIDI_IMPORT));
		midiExportItem.setIcon(new Icon(IconResources.MIDI_EXPORT));
		midiImportItem.setText("IMPORT MIDI");
		midiExportItem.setText("EXPORT MIDI");

		for (MenuItem menuItem : topLevelItems) {
			menuItem.loadIcons();
		}
	}

	public synchronized void layoutChildren() {
		iconWidth = columnWidth - offset * 2;

		fileItem.layout(this, offset, 0, iconWidth, iconWidth);
		settingsItem.layout(this, offset, iconWidth + offset, iconWidth,
				iconWidth);
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
