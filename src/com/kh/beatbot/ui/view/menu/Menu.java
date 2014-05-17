package com.kh.beatbot.ui.view.menu;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.listener.FileMenuItemListener;
import com.kh.beatbot.listener.MenuItemListener;
import com.kh.beatbot.ui.shape.RenderGroup;
import com.kh.beatbot.ui.view.ListView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;

public abstract class Menu extends TouchableView implements MenuItemListener, FileMenuItemListener,
		FileFilter {
	protected List<ListView> menuLists;
	protected List<MenuItem> topLevelItems;
	protected float columnWidth = 0;

	protected FilenameFilter filenameFilter;

	protected abstract void createMenuItems();

	protected abstract float getWidthForLevel(int level);

	public Menu(View view) {
		super(view);
	}

	public Menu(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	public List<MenuItem> getTopLevelItems() {
		return topLevelItems;
	}

	public ListView getListAtLevel(final MenuItem item, final int level) {
		while (level >= menuLists.size()) {
			ListView menuList = new ListView(this);
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

	public synchronized void layoutChildren() {
		if (columnWidth <= 0) {
			columnWidth = width;
		}
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
