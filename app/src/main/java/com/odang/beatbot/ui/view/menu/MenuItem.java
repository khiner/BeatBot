package com.odang.beatbot.ui.view.menu;

import com.odang.beatbot.listener.OnPressListener;
import com.odang.beatbot.listener.OnReleaseListener;
import com.odang.beatbot.ui.icon.IconResourceSet;
import com.odang.beatbot.ui.icon.IconResourceSets;
import com.odang.beatbot.ui.view.ListView;
import com.odang.beatbot.ui.view.control.Button;
import com.odang.beatbot.ui.view.control.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuItem implements OnPressListener, OnReleaseListener {
    protected Menu menu = null;
    private MenuItem parent = null;
    protected Button button;
    protected List<MenuItem> subMenuItems = new ArrayList<>();
    private ListView container = null;
    private int level = 0;

    public MenuItem(Menu menu, MenuItem parent, boolean toggle) {
        this.menu = menu;
        this.parent = parent;
        this.level = null == parent ? 0 : parent.level + 1;
        container = menu.getListAtLevel(level);

        this.button = toggle ? new ToggleButton(container) : new Button(container);
        this.button = button.withIcon(IconResourceSets.MENU_ITEM).withRoundedRect();
        button.setOnPressListener(this);
        button.setOnReleaseListener(this);

        hide();
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
        Collections.addAll(this.subMenuItems, subMenuItems);
    }

    public List<MenuItem> getSubMenuItems() {
        return subMenuItems;
    }

    public void setOnReleaseListener(OnReleaseListener listener) {
        button.setOnReleaseListener(listener);
    }

    public IconResourceSet getIcon() {
        return button.getIcon();
    }

    public void setIcon(IconResourceSet icon) {
        button.setIcon(icon);
    }

    public void setIconColors(IconResourceSet icon) {
        button.setColors(icon);
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
        return button instanceof ToggleButton && ((ToggleButton) button).isChecked();

    }

    public void show() {
        menu.addChild(container);
        container.addChild(button);
    }

    public void hide() {
        container.removeChild(button);
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

    public void scrollTo() {
        menu.scrollToMenuItem(this);
    }

    public int getLevel() {
        return level;
    }

    public Button getButton() {
        return button;
    }

    private List<MenuItem> getSiblings() {
        return parent == null ? menu.getTopLevelItems() : parent.subMenuItems;
    }
}
