package com.odang.beatbot.listener;

import com.odang.beatbot.ui.view.menu.FileMenuItem;

public interface FileMenuItemListener {
    void onFileMenuItemReleased(FileMenuItem menuItem);

    void onFileMenuItemLongPressed(FileMenuItem menuItem);
}
