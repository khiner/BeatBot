package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.listener.MidiNoteListener;
import com.kh.beatbot.manager.FileManager;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.midi.MidiNote;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;
import com.kh.beatbot.ui.view.group.ControlButtonGroup;
import com.kh.beatbot.ui.view.group.MidiViewGroup;
import com.kh.beatbot.ui.view.group.PageSelectGroup;
import com.kh.beatbot.ui.view.menu.MainMenu;

public class MainPage extends TouchableView implements MidiNoteListener {
	public ControlButtonGroup controlButtonGroup;
	public MidiViewGroup midiViewGroup;
	public PageSelectGroup pageSelectGroup;
	public MainMenu slideMenu;

	public MainPage(View view) {
		super(view);
	}

	@Override
	protected synchronized void createChildren() {
		controlButtonGroup = new ControlButtonGroup(this);
		midiViewGroup = new MidiViewGroup(this);
		pageSelectGroup = new PageSelectGroup(this);
		slideMenu = new MainMenu(this, null);

		TrackManager.addTrackListener(pageSelectGroup);
		FileManager.addListener(pageSelectGroup);
	}

	@Override
	public synchronized void layoutChildren() {
		float controlButtonHeight = height / 10;
		float midiHeight = 3 * (height - controlButtonHeight) / 5;
		float pageSelectGroupHeight = height - midiHeight - controlButtonHeight;
		View.LABEL_HEIGHT = pageSelectGroupHeight / 5;
		View.BG_OFFSET = height / 180;
		midiViewGroup.layout(this, 0, controlButtonHeight, width - 15, midiHeight);
		controlButtonGroup.layout(this, midiViewGroup.getTrackControlWidth(), 0, width
				- midiViewGroup.getTrackControlWidth(), controlButtonHeight);
		pageSelectGroup.layout(this, 0, controlButtonHeight + midiHeight, width,
				pageSelectGroupHeight);

		slideMenu.layout(this, -width, 0, midiViewGroup.getTrackControlWidth(), height);
	}

	@Override
	public synchronized void drawAll() {
		controlButtonGroup.drawAll();
		midiViewGroup.drawAll();
		renderGroup.draw();
		pageSelectGroup.drawAll();
		slideMenu.drawAll();
	}

	public void expandMenu() {
		slideMenu.expand();
	}

	@Override
	public void onCreate(MidiNote note) {
		controlButtonGroup.onCreate(note);
		midiViewGroup.midiView.onCreate(note);
	}

	@Override
	public void onDestroy(MidiNote note) {
		controlButtonGroup.onDestroy(note);
		midiViewGroup.midiView.onDestroy(note);
	}

	@Override
	public void onMove(MidiNote note) {
		midiViewGroup.midiView.onMove(note);
	}

	@Override
	public void onSelectStateChange(MidiNote note) {
		controlButtonGroup.onSelectStateChange(note);
		midiViewGroup.midiView.onSelectStateChange(note);
	}
}
