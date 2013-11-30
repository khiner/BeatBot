package com.kh.beatbot.ui.view;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.ui.view.menu.FileMenuItem;
import com.kh.beatbot.ui.view.menu.MenuItem;
import com.kh.beatbot.ui.view.menu.MenuItemListener;

public abstract class Menu extends TouchableView implements MenuItemListener {
	protected List<ListView> menuLists;
	protected List<MenuItem> topLevelItems;
	protected float columnWidth = 0;

	protected abstract void createMenuItems();

	protected abstract float getWidthForLevel(int level);

	public abstract void fileItemReleased(FileMenuItem fileItem);

	public List<MenuItem> getTopLevelItems() {
		return topLevelItems;
	}

	public ListView getListAtLevel(final MenuItem item, final int level) {
		while (level >= menuLists.size()) {
			ListView menuList = new ListView();
			if (initialized) {
				menuList.loadIcons();
			}
			menuLists.add(menuList);
		}

		return menuLists.get(level);
	}

	public synchronized void createChildren() {
		menuLists = new ArrayList<ListView>();
		topLevelItems = new ArrayList<MenuItem>();
		createMenuItems();
		for (MenuItem topLevelItem : topLevelItems) {
			topLevelItem.show();
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
		float displayHeight = height - 2 * yOffset;
		float x = 0;
		for (int i = 0; i < menuLists.size(); i++) {
			ListView list = menuLists.get(i);
			list.layout(this, x, yOffset, getWidthForLevel(i), displayHeight);
			x += list.width;
		}
	}

	@Override
	public void onMenuItemPressed(MenuItem menuItem) {
	}

	@Override
	public void onMenuItemReleased(MenuItem menuItem) {
		layoutChildren();
	}
}
