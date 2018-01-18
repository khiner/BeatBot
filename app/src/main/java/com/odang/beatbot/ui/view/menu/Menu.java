package com.odang.beatbot.ui.view.menu;

import com.odang.beatbot.listener.FileMenuItemListener;
import com.odang.beatbot.listener.MenuItemListener;
import com.odang.beatbot.ui.shape.RenderGroup;
import com.odang.beatbot.ui.view.ListView;
import com.odang.beatbot.ui.view.ScrollableView;
import com.odang.beatbot.ui.view.View;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public abstract class Menu extends ScrollableView implements MenuItemListener,
        FileMenuItemListener, FileFilter {
    protected List<ListView> menuLists;
    protected List<MenuItem> topLevelItems;
    protected float columnWidth = 0;

    protected abstract void createMenuItems();

    protected abstract float getWidthForLevel(int level);

    public Menu(View view, RenderGroup renderGroup) {
        super(view, renderGroup);
    }

    public List<MenuItem> getTopLevelItems() {
        return topLevelItems;
    }

    public ListView getListAtLevel(final int level) {
        while (level >= menuLists.size()) {
            ListView menuList = new ListView(this);
            menuLists.add(menuList);
        }

        return menuLists.get(level);
    }

    public void createChildren() {
        menuLists = new ArrayList<>();
        topLevelItems = new ArrayList<>();
        createMenuItems();
        for (MenuItem topLevelItem : topLevelItems) {
            topLevelItem.show();
        }
    }

    public synchronized void layoutChildren() {
        if (columnWidth <= 0) {
            columnWidth = width;
        }

        float displayHeight = height - 2 * BG_OFFSET;
        float x = xOffset;
        for (int i = 0; i < menuLists.size(); i++) {
            ListView list = menuLists.get(i);
            list.layout(this, x, BG_OFFSET, getWidthForLevel(i), displayHeight);
            x += list.width;
        }
        super.layoutChildren();
    }

    public synchronized void scrollToMenuItem(final MenuItem menuItem) {
        final int level = menuItem.getLevel();
        if (level < menuLists.size()) {
            menuLists.get(level).scrollToChild(menuItem.button);
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
