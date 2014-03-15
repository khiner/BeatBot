package com.kh.beatbot.ui.view.menu;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.icon.IconResourceSet;
import com.kh.beatbot.ui.icon.IconResourceSets;
import com.kh.beatbot.ui.view.ListView;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class MenuItem implements OnPressListener, OnReleaseListener {
	protected Menu menu = null;
	private MenuItem parent = null;
	protected Button button;
	protected List<MenuItem> subMenuItems = new ArrayList<MenuItem>();
	private ListView container = null;
	private int level = 0;

	public MenuItem(Menu menu, MenuItem parent, Button button) {
		this.menu = menu;
		this.parent = parent;
		this.level = parent == null ? 0 : parent.level + 1;
		this.button = button;

		button.setIcon(IconResourceSets.MENU_ITEM);
		button.setOnPressListener(this);
		button.setOnReleaseListener(this);

		container = menu.getListAtLevel(this, level);
	}

	public void onPress(Button button) {
		menu.onMenuItemPressed(this);
		container.onPress(button);
	}

	public void onRelease(Button button) {
		select();
		menu.onMenuItemReleased(this);
	}

	public void trigger() {
		setChecked(true);
		onRelease(button);
	}

	public void addSubMenuItems(final MenuItem... subMenuItems) {
		for (MenuItem menuItem : subMenuItems) {
			this.subMenuItems.add(menuItem);
		}
	}

	public List<MenuItem> getSubMenuItems() {
		return subMenuItems;
	}

	public void setOnReleaseListener(OnReleaseListener listener) {
		button.setOnReleaseListener(listener);
	}

	public void setIcon(IconResourceSet icon) {
		button.setIcon(icon);
	}

	public void setResourceId(final IconResourceSet icon) {
		button.setResourceId(icon);
	}

	public void setText(final String text) {
		button.setText(text);
	}

	public String getText() {
		return button.getText();
	}

	public void setChecked(final boolean checked) {
		if (button instanceof ToggleButton) {
			((ToggleButton) button).setChecked(checked);
		}
	}

	public boolean isChecked() {
		if (button instanceof ToggleButton) {
			return ((ToggleButton) button).isChecked();
		}

		return false;
	}

	public void show() {
		menu.addChild(container);
		container.add(button);
	}

	public void hide() {
		container.remove(button);
		for (MenuItem child : subMenuItems) {
			child.hide();
		}
		if (!container.hasChildren()) {
			menu.removeChild(container);
		}
	}

	public void clearSubMenuItems() {
		for (MenuItem child : subMenuItems) {
			child.hide();
		}
		subMenuItems.clear();
	}

	public void select() {
		for (MenuItem sibling : getSiblings()) {
			if (sibling.equals(this)) {
				for (MenuItem subMenuItem : subMenuItems) {
					subMenuItem.show();
					if (subMenuItem.isChecked()) {
						subMenuItem.select();
					}
				}
			} else {
				sibling.setChecked(false);
				for (MenuItem nephew : sibling.subMenuItems) {
					nephew.hide();
				}
			}
		}
	}

	private List<MenuItem> getSiblings() {
		return parent == null ? menu.getTopLevelItems() : parent.subMenuItems;
	}
}
