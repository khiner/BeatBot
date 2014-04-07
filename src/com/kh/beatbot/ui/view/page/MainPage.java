package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.group.ControlButtonGroup;
import com.kh.beatbot.ui.view.group.MidiViewGroup;
import com.kh.beatbot.ui.view.group.PageSelectGroup;
import com.kh.beatbot.ui.view.menu.MainMenu;

public class MainPage extends TouchableView {

	public ControlButtonGroup controlButtonGroup;
	public MidiViewGroup midiViewGroup;
	public PageSelectGroup pageSelectGroup;
	public MainMenu slideMenu;

	public static float controlButtonHeight = 0;

	@Override
	protected synchronized void createChildren() {
		controlButtonGroup = new ControlButtonGroup(shapeGroup);
		midiViewGroup = new MidiViewGroup(shapeGroup);
		pageSelectGroup = new PageSelectGroup(shapeGroup);
		slideMenu = new MainMenu();

		TrackManager.addTrackListener(pageSelectGroup);
		FileManager.addListener(pageSelectGroup);

		addChildren(controlButtonGroup, midiViewGroup, pageSelectGroup, slideMenu);
	}

	@Override
	public synchronized void layoutChildren() {
		controlButtonHeight = height / 10;
		float midiHeight = 3 * (height - controlButtonHeight) / 5;
		float pageSelectGroupHeight = height - midiHeight - controlButtonHeight;
		View.LABEL_HEIGHT = pageSelectGroupHeight / 5;

		midiViewGroup.layout(this, 0, controlButtonHeight, width - 15, midiHeight);
		controlButtonGroup.layout(this, midiViewGroup.getTrackControlWidth(), 0, width
				- midiViewGroup.getTrackControlWidth(), controlButtonHeight);
		pageSelectGroup.layout(this, 0, controlButtonHeight + midiHeight, width,
				pageSelectGroupHeight);

		slideMenu.layout(this, -width, 0, midiViewGroup.getTrackControlWidth(), height);
	}

	public void expandMenu() {
		slideMenu.expand();
	}
}
