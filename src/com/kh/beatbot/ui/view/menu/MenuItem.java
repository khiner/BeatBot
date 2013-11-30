package com.kh.beatbot.ui.view.menu;

import java.util.ArrayList;
import java.util.List;

import com.kh.beatbot.listener.OnPressListener;
import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.view.ListView;
import com.kh.beatbot.ui.view.Menu;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;

public class MenuItem {
	protected Menu menu = null;
	private MenuItem parent = null;
	protected ImageButton button;
	protected List<MenuItem> subMenuItems = new ArrayList<MenuItem>();
	private ListView container = null;
	private int level = 0;

	public MenuItem(Menu menu, MenuItem parent, ImageButton button) {
		this.menu = menu;
		this.parent = parent;
		this.level = parent == null ? 0 : parent.level + 1;
		final MenuItem menuItem = this;
		this.button = button;

		button.setOnPressListener(new OnPressListener() {
			@Override
			public void onPress(Button button) {
				menuItem.onPress(button);
			}
		});

		button.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				menuItem.onRelease(button);
			}
		});

		container = menu.getListAtLevel(this, level);
	}

	public void onPress(Button button) {
		updateIcon();
		menu.onMenuItemPressed(this);
		container.onPress(button);
	}

	public void onRelease(Button button) {
		setChecked(true);
		select();
		menu.onMenuItemReleased(this);
	}

	public void addSubMenuItems(final MenuItem... subMenuItems) {
		for (MenuItem menuItem : subMenuItems) {
			this.subMenuItems.add(menuItem);
		}
	}

	public void loadIcons() {
		if (!button.getText().isEmpty()) {
			button.setStrokeColor(Colors.BLACK);
		}

		for (MenuItem subMenuItem : subMenuItems) {
			subMenuItem.loadIcons();
		}
	}

	public void setOnReleaseListener(OnReleaseListener listener) {
		button.setOnReleaseListener(listener);
	}

	public void setIcon(final Icon icon) {
		button.setIcon(icon);
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
		updateIcon();
	}

	public boolean isChecked() {
		if (button instanceof ToggleButton) {
			return ((ToggleButton) button).isChecked();
		}

		return false;
	}

	public void show() {
		container.add(button);
		menu.addChild(container);
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
				setChecked(true);
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

	private void updateIcon() {
		if ((button.isPressed() || isChecked()) && !button.getText().isEmpty()) {
			if (((ImageButton) button).getBgIcon() == null) {
				if (button instanceof ToggleButton) {
					((ToggleButton) button).setBgIcon(new RoundedRectIcon(null,
							Colors.menuToggleFillColorSet));
				} else if (button instanceof ImageButton) {
					((ImageButton) button).setBgIcon(new RoundedRectIcon(null,
							Colors.menuItemFillColorSet));
				}
			}
		} else {
			button.setBgIcon(null);
		}
	}
}
