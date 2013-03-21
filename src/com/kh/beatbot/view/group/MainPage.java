package com.kh.beatbot.view.group;

import com.kh.beatbot.manager.Managers;
import com.kh.beatbot.view.MidiTrackView;
import com.kh.beatbot.view.MidiView;
import com.kh.beatbot.view.TouchableBBView;
import com.kh.beatbot.view.TouchableSurfaceView;

public class MainPage extends TouchableBBView {
	
	public MidiView midiView;
	public MidiTrackView midiTrackControl;
	public PageSelectGroup pageSelectGroup;
	
	public MainPage(TouchableSurfaceView parent) {
		super(parent);
	}
	
	public void trackAdded(int newTrackNum) {
		midiTrackControl.trackAdded(newTrackNum);
		midiView.trackAdded(newTrackNum);
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
		midiTrackControl = new MidiTrackView((TouchableSurfaceView)root);
		pageSelectGroup = new PageSelectGroup((TouchableSurfaceView)root);
		
		addChild(midiView);
		addChild(midiTrackControl);
		addChild(pageSelectGroup);
	}

	@Override
	public void layoutChildren() {
		float midiHeight = 2 * height / 3;
		int numTracks = Managers.trackManager.getNumTracks();
		MidiView.allTracksHeight = midiHeight - MidiView.Y_OFFSET;
		MidiView.trackHeight = MidiView.allTracksHeight / numTracks;
		
		float trackControlWidth = MidiView.trackHeight * 2.5f;
		
		midiTrackControl.layout(this, 0, 0, trackControlWidth, midiHeight);
		midiView.layout(this, trackControlWidth, 0, width - trackControlWidth, midiHeight);
		pageSelectGroup.layout(this, 0, midiHeight, width, height - midiHeight);
	}

	@Override
	protected void loadIcons() {
		// parent group
	}
}
