package com.odang.beatbot.listener;

import com.odang.beatbot.ui.view.menu.FileMenuItem;

public interface FileMenuItemListener {
    public void onFileMenuItemReleased(FileMenuItem menuItem);

    public void onFileMenuItemLongPressed(FileMenuItem menuItem);
}
