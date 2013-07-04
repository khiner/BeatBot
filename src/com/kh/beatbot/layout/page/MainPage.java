package com.kh.beatbot.layout.page;

import com.kh.beatbot.global.GlobalVars;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.view.MidiTrackView;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.group.ControlButtonGroup;
import com.kh.beatbot.view.group.PageSelectGroup;

public class MainPage extends TouchableBBView {
	
	public MidiView midiView;
	public ControlButtonGroup controlButtonGroup;
	public MidiTrackView midiTrackView;
	public PageSelectGroup pageSelectGroup;
	
	public void trackCreated(int newTrackNum) {
		midiTrackView.notifyTrackCreated(newTrackNum);
		midiView.notifyTrackCreated(newTrackNum);
		pageSelectGroup.notifyTrackChanged();
	}
	
	public void notifyTrackDeleted(int newTrackNum) {
		midiTrackView.notifyTrackDeleted(newTrackNum);
		midiView.notifyTrackDeleted(newTrackNum);
	}
	
	@Override
	public void initAll() {
		super.initAll();
		GlobalVars.mainActivity.setupProject();
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
		midiTrackView = new MidiTrackView();
		pageSelectGroup = new PageSelectGroup();
		
		addChild(controlButtonGroup);
		addChild(midiTrackView);
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
		int numTracks = TrackManager.getNumTracks();
		MidiView.allTracksHeight = midiHeight - MidiView.Y_OFFSET;
		MidiView.trackHeight = MidiView.allTracksHeight / numTracks;
		float trackControlWidth = MidiView.trackHeight * 2.5f;
		
		midiTrackView.layout(this, 0, controlButtonHeight, trackControlWidth, midiHeight);
		midiView.layout(this, trackControlWidth, controlButtonHeight, width - trackControlWidth - 15, midiHeight);
		controlButtonGroup.layout(this, 0, 0, width, controlButtonHeight);
		pageSelectGroup.layout(this, 0, controlButtonHeight + midiHeight, width, height - midiHeight - controlButtonHeight);
	}
}
