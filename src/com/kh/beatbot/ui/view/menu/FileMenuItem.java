package com.kh.beatbot.ui.view.menu;

import java.io.File;
import java.util.Arrays;

import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResource;
import com.kh.beatbot.ui.IconResource.State;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.view.Menu;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class FileMenuItem extends MenuItem {

	private File file;

	public FileMenuItem(Menu menu, MenuItem parent, File file) {
		super(menu, parent, file.isDirectory() ? new ToggleButton()
				: new ImageButton());
		this.file = file;
		setText(file.getName().isEmpty() ? file.getPath() : file.getName());
	}

	@Override
	public void onRelease(Button button) {
		if (file.isDirectory()) {
			expand();
		} else {
			menu.fileItemReleased(this);
		}
		super.onRelease(button);
	}

	private void expand() {
		clearSubMenuItems();
		File[] files = file.listFiles();
		Arrays.sort(files);
		FileMenuItem[] subMenuItems = new FileMenuItem[files.length];
		for (int i = 0; i < files.length; i++) {
			FileMenuItem childFileItem = new FileMenuItem(menu, this, files[i]);
			childFileItem.loadIcons();
			subMenuItems[i] = childFileItem;
		}
		addSubMenuItems(subMenuItems);
	}

	@Override
	public void loadIcons() {
		super.loadIcons();

		if (file.isDirectory()) {
			IconResource resource = IconResources.forDirectory(getText());
			if (resource != null) {
				Icon icon = new Icon(resource);
				icon.lockState(State.PRESSED);
				setIcon(icon);
			}
		}
	}
	
	public File getFile() {
		return file;
	}
}
