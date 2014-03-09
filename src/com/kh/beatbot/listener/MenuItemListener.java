package com.kh.beatbot.listener;

import com.kh.beatbot.ui.view.menu.MenuItem;

public interface MenuItemListener {
	public void onMenuItemPressed(MenuItem menuItem);

	public void onMenuItemReleased(MenuItem menuItem);
}
