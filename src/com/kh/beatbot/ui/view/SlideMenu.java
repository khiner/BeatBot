package com.kh.beatbot.ui.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kh.beatbot.listener.OnReleaseListener;
import com.kh.beatbot.manager.MidiManager;
import com.kh.beatbot.ui.Icon;
import com.kh.beatbot.ui.IconResources;
import com.kh.beatbot.ui.RoundedRectIcon;
import com.kh.beatbot.ui.color.Colors;
import com.kh.beatbot.ui.mesh.ShapeGroup;
import com.kh.beatbot.ui.view.control.Button;
import com.kh.beatbot.ui.view.control.ImageButton;
import com.kh.beatbot.ui.view.control.ToggleButton;
import com.kh.beatbot.ui.view.page.Page;

public class SlideMenu extends TouchableView {

	private float offset = 0, columnWidth = 0, iconWidth, textHeight;

	private ShapeGroup shapeGroup;

	private ToggleButton settingsButton, snapToGridButton,
			selectedMenuItem = null;

	private Map<ImageButton, List<ImageButton>> menuHeirarchy;

	public void createChildren() {
		shapeGroup = new ShapeGroup();

		settingsButton = new ToggleButton();
		snapToGridButton = new ToggleButton();

		menuHeirarchy = new HashMap<ImageButton, List<ImageButton>>();

		settingsButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				selectMenuItem(settingsButton);
				width = columnWidth * 3;
				Page.mainPage.notifyMenuExpanded();
			}
		});

		snapToGridButton.setOnReleaseListener(new OnReleaseListener() {
			@Override
			public void onRelease(Button button) {
				MidiManager.setSnapToGrid(snapToGridButton.isChecked());
			}
		});

		menuHeirarchy.put(settingsButton,
				Arrays.asList((ImageButton) snapToGridButton));

		addChild(settingsButton);
		addChild(snapToGridButton);
	}

	public void loadIcons() {
		columnWidth = width;
		offset = width / 6;
		settingsButton.setIcon(new Icon(IconResources.SETTINGS));
		snapToGridButton.setBgIcon(new RoundedRectIcon(shapeGroup,
				Colors.menuItemFillColorSet));
		snapToGridButton.setStrokeColor(Colors.BLACK);
		snapToGridButton.setIcon(new Icon(IconResources.SNAP_TO_GRID));
		snapToGridButton.setChecked(MidiManager.isSnapToGrid());
		snapToGridButton.setText("SNAP-TO-GRID");
	}

	@Override
	public void draw() {
		if (selectedMenuItem != null) {
			shapeGroup.draw(this, 1);
		}
	}

	@Override
	public void drawChildren() {
		for (ImageButton button : menuHeirarchy.keySet()) {
			drawChild(button);
		}
		if (selectedMenuItem != null) {
			// TODO draw separating bar for each heirarchy level
			for (ImageButton button : menuHeirarchy.get(selectedMenuItem)) {
				drawChild(button);
			}
		}
	}

	public void layoutChildren() {
		iconWidth = columnWidth - offset * 2;
		textHeight = columnWidth / 4;
		settingsButton.layout(this, offset, 0, iconWidth, iconWidth);
		layoutSubMenuItems(settingsButton);
	}

	private void selectMenuItem(ToggleButton menuItem) {
		selectedMenuItem = menuItem;
		selectedMenuItem.setChecked(true);
	}

	private void layoutSubMenuItems(ToggleButton parentMenuItem) {
		List<ImageButton> subMenuItems = menuHeirarchy.get(parentMenuItem);
		for (int i = 0; i < subMenuItems.size(); i++) {
			subMenuItems.get(i).layout(this, columnWidth,
					offset + textHeight * i, columnWidth * 2 - offset,
					textHeight);
		}
	}
}
