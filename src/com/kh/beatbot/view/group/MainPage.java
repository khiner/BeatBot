package com.kh.beatbot.view.group;

import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.MidiTrackView;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public class MainPage extends TouchableBBView {
	
	public MidiView midiView;
	public ControlButtonGroup controlButtonGroup;
	public MidiTrackView midiTrackControl;
	public PageSelectGroup pageSelectGroup;
	
	public MainPage(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public void trackAdded(int newTrackNum) {
		midiTrackControl.trackAdded(newTrackNum);
		midiView.trackAdded(newTrackNum);
		pageSelectGroup.notifyTrackChanged();
	}
	
	@Override
	public void init() {
		// nothing to do.
	}

	@Override
	public void draw() {
		// parent group
	}

	@Override
	protected void createChildren() {
		midiView = new MidiView((TouchableSurfaceView)root);
		controlButtonGroup = new ControlButtonGroup((TouchableSurfaceView)root);
		midiTrackControl = new MidiTrackView((TouchableSurfaceView)root);
		pageSelectGroup = new PageSelectGroup((TouchableSurfaceView)root);
		
		addChild(controlButtonGroup);
		addChild(midiTrackControl);
		addChild(midiView);
		addChild(pageSelectGroup);
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
		midiView.layout(this, trackControlWidth, controlButtonHeight, width - trackControlWidth, midiHeight);
		pageSelectGroup.layout(this, 0, controlButtonHeight + midiHeight, width, height - midiHeight - controlButtonHeight);
	}

	@Override
	protected void loadIcons() {
		// parent group
	}
}
