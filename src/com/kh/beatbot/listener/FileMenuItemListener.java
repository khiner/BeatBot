package com.kh.beatbot.listener;

import com.kh.beatbot.ui.view.menu.FileMenuItem;

public interface FileMenuItemListener {
	public void onFileMenuItemReleased(FileMenuItem menuItem);

	public void onFileMenuItemLongPressed(FileMenuItem menuItem);
}
