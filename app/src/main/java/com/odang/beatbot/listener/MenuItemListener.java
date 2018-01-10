package com.odang.beatbot.listener;

import com.odang.beatbot.ui.view.menu.MenuItem;

public interface MenuItemListener {
    void onMenuItemPressed(MenuItem menuItem);

    void onMenuItemReleased(MenuItem menuItem);
}
