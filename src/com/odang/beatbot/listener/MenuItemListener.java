package com.odang.beatbot.listener;

import com.odang.beatbot.ui.view.menu.MenuItem;

public interface MenuItemListener {
	public void onMenuItemPressed(MenuItem menuItem);

	public void onMenuItemReleased(MenuItem menuItem);
}
