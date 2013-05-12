package com.kh.beatbot.layout.page;

import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.MidiTrackView;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.group.ControlButtonGroup;
import com.kh.beatbot.view.group.PageSelectGroup;

public class MainPage extends TouchableBBView {
	
	public MidiView midiView;
	public ControlButtonGroup controlButtonGroup;
	public MidiTrackView midiTrackControl;
	public PageSelectGroup pageSelectGroup;
	
	public void trackAdded(int newTrackNum) {
		midiTrackControl.notifyTrackAdded(newTrackNum);
		midiView.notifyTrackAdded(newTrackNum);
		pageSelectGroup.notifyTrackChanged();
	}
	
	@Override
	public void init() {
		// nothing to do.
	}

	@Override
	public void draw() {
		// parent view
	}

	@Override
	protected void createChildren() {
		midiView = new MidiView();
		controlButtonGroup = new ControlButtonGroup();
		midiTrackControl = new MidiTrackView();
		pageSelectGroup = new PageSelectGroup();
		
		addChild(controlButtonGroup);
		addChild(midiTrackControl);
		addChild(midiView);
		addChild(pageSelectGroup);
	}

	@Override
	protected void loadIcons() {
		// parent
	}
	
	@Override
	public void layoutChildren() {
		float controlButtonHeight = height / 10;
		float midiHeight = 3 * (height - controlButtonHeight) / 5;
		int numTracks = Managers.trackManager.getNumTracks();
		MidiView.allTracksHeight = midiHeight - MidiView.Y_OFFSET;
		MidiView.trackHeight = MidiView.allTracksHeight / numTracks;
		float trackControlWidth = MidiView.trackHeight * 2.5f;
		
		controlButtonGroup.layout(this, 0, 0, width, controlButtonHeight);
		midiTrackControl.layout(this, 0, controlButtonHeight, trackControlWidth, midiHeight);
		midiView.layout(this, trackControlWidth, controlButtonHeight, width - trackControlWidth - 15, midiHeight);
		pageSelectGroup.layout(this, 0, controlButtonHeight + midiHeight, width, height - midiHeight - controlButtonHeight);
	}
}
