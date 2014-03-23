package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.MidiTrackView;
import com.kh.beatbot.ui.view.MidiView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.group.ControlButtonGroup;
import com.kh.beatbot.ui.view.group.PageSelectGroup;
import com.kh.beatbot.ui.view.menu.MainMenu;

public class MainPage extends TouchableView {

	public MidiView midiView;
	public ControlButtonGroup controlButtonGroup;
	public MidiTrackView midiTrackView;
	public PageSelectGroup pageSelectGroup;
	public MainMenu slideMenu;

	public static float controlButtonHeight = 0;
	private float trackControlWidth = 0;

	public float getTrackControlWidth() {
		return trackControlWidth;
	}

	@Override
	protected synchronized void createChildren() {
		midiView = new MidiView();
		controlButtonGroup = new ControlButtonGroup();
		midiTrackView = new MidiTrackView();
		pageSelectGroup = new PageSelectGroup();
		slideMenu = new MainMenu();

		midiView.addScrollListener(midiTrackView);
		TrackManager.addTrackListener(midiView);
		TrackManager.addTrackListener(midiTrackView);
		TrackManager.addTrackListener(pageSelectGroup);
		FileManager.addListener(pageSelectGroup);

		addChildren(controlButtonGroup, midiTrackView, midiView, pageSelectGroup, slideMenu);
	}

	@Override
	public synchronized void layoutChildren() {
		controlButtonHeight = height / 10;
		float midiHeight = 3 * (height - controlButtonHeight) / 5;
		float pageSelectGroupHeight = height - midiHeight - controlButtonHeight;
		MidiView.trackHeight = (midiHeight - MidiView.Y_OFFSET) / 5f;
		View.LABEL_HEIGHT = pageSelectGroupHeight / 5;

		trackControlWidth = MidiView.trackHeight * 2.5f;

		midiTrackView.layout(this, 0, controlButtonHeight, trackControlWidth, midiHeight);
		midiView.layout(this, trackControlWidth, controlButtonHeight, width - trackControlWidth
				- 15, midiHeight);

		controlButtonGroup.layout(this, trackControlWidth, 0, width - trackControlWidth,
				controlButtonHeight);
		pageSelectGroup.layout(this, 0, controlButtonHeight + midiHeight, width,
				pageSelectGroupHeight);

		slideMenu.layout(this, -width, 0, trackControlWidth, height);
	}

	public void expandMenu() {
		slideMenu.expand();
	}
}
