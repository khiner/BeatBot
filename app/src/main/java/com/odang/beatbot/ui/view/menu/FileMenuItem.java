package com.odang.beatbot.ui.view.menu;

import com.odang.beatbot.listener.OnLongPressListener;
import com.odang.beatbot.manager.FileManager;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.control.Button;
import com.odang.beatbot.ui.view.control.ToggleButton;

import java.io.File;
import java.util.Arrays;

public class FileMenuItem extends MenuItem implements OnLongPressListener {
    private File file;
    private boolean ignoreRelease = false;

    public FileMenuItem(Menu menu, MenuItem parent, File file) {
        super(menu, parent, true);
        this.file = file;
        if (file.isFile()) {
            button.setOnLongPressListener(this);
        }
        if (parent != null) {
            setIconColors(parent.getIcon());
        }
    }

    @Override
    public void onPress(Button button) {
        super.onPress(button);
        // ignore if the button is already checked
        ignoreRelease = button instanceof ToggleButton && ((ToggleButton) button).isChecked();
    }
    
    @Override
    public void onRelease(Button button) {
        if (ignoreRelease)
            return;
        if (file.isDirectory()) {
            expand();
        } else {
            menu.onFileMenuItemReleased(this);
        }
        super.onRelease(button);
        if (file.isDirectory()) {
            menu.onDirectoryMenuItemReleased(this);
        }
    }

    @Override
    public void onLongPress(Button button) {
        menu.onFileMenuItemLongPressed(this);
    }

    private void expand() {
        clearSubMenuItems();
        File[] files = file.listFiles(menu);
        Arrays.sort(files);
        int numFiles = Math.min(files.length, 100);
        for (int i = 0; i < numFiles; i++) {
            subMenuItems.add(new FileMenuItem(menu, this, files[i]));
        }
    }

    @Override
    public void select() {
        super.select();
        if (file.isDirectory()) {
            for (MenuItem subMenuItem : subMenuItems) {
                ((FileMenuItem) subMenuItem).loadIcons();
            }
        }
    }

    @Override
    public void show() {
        super.show();
        loadIcons();
    }

    public File getFile() {
        return file;
    }

    private void loadIcons() {
        if (button.getText().isEmpty()) {
            setText(file.getName().isEmpty() ? file.getPath() : FileManager.formatSampleName(file.getName()));
            if (file.isDirectory()) {
                setResourceId(IconResourceSets.forDirectory(getText()));
            }
        }
    }
}
