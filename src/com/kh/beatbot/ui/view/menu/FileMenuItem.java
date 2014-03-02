package com.kh.beatbot.ui.view.menu;

import java.io.File;
import java.util.Arrays;

import com.kh.beatbot.listener.OnLongPressListener;
import com.kh.beatbot.ui.IconResourceSet;
import com.kh.beatbot.ui.IconResourceSet.State;
import com.kh.beatbot.ui.IconResourceSets;
import com.kh.beatbot.ui.view.Menu;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class FileMenuItem extends MenuItem implements OnLongPressListener {

	private File file;

	public FileMenuItem(Menu menu, MenuItem parent, File file) {
		super(menu, parent, file.isDirectory() ? new ToggleButton(menu.getShapeGroup(), false, false)
				: new Button(menu.getShapeGroup(), false));
		this.file = file;
		if (file.isFile()) {
			button.setOnLongPressListener(this);
		}
	}

	@Override
	public void onRelease(Button button) {
		if (file.isDirectory()) {
			expand();
		} else {
			menu.onFileMenuItemReleased(this);
		}
		super.onRelease(button);
	}

	@Override
	public void onLongPress(Button button) {
		menu.onFileMenuItemLongPressed(this);
	}

	private void expand() {
		clearSubMenuItems();
		File[] files = file.listFiles(menu);
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

		setText(file.getName().isEmpty() ? file.getPath() : file.getName());
		if (file.isDirectory()) {
			IconResourceSet icon = IconResourceSets.forDirectory(getText());
			button.setIcon(icon);
			button.lockState(State.PRESSED);
		}
	}

	public File getFile() {
		return file;
	}
}
