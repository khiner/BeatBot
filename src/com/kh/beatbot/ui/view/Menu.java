package com.kh.beatbot.ui.view;

import java.util.List;

import com.kh.beatbot.ui.view.menu.MenuItem;

public class Menu extends TouchableView {
	protected List<ListView> menuLists;
	protected List<MenuItem> topLevelItems;
	protected MenuItem selectedItem = null;
	protected float columnWidth = 0;

	public List<MenuItem> getTopLevelItems() {
		return topLevelItems;
	}

	public ListView getListAtLevel(final int level) {
		return level < menuLists.size() ? menuLists.get(level) : null;
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

	public synchronized void selectMenuItem(MenuItem menuItem) {
		menuItem.setChecked(true);

		if (!menuItem.equals(selectedItem)) {
			menuItem.select();
			selectedItem = menuItem;
		}
	}
	
	public synchronized void loadIcons() {
		columnWidth = width;
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
}
