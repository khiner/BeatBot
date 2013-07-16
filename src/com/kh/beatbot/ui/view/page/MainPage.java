package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.Track;
import com.kh.beatbot.activity.BeatBotActivity;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.MidiTrackView;
import com.kh.beatbot.ui.view.MidiView;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.group.ControlButtonGroup;
import com.kh.beatbot.ui.view.group.PageSelectGroup;

public class MainPage extends TouchableView {
	
	public MidiView midiView;
	public ControlButtonGroup controlButtonGroup;
	public MidiTrackView midiTrackView;
	public PageSelectGroup pageSelectGroup;
	
	public void notifyTrackCreated(Track track) {
		midiTrackView.notifyTrackCreated(track);
		midiView.notifyTrackCreated(track);
		pageSelectGroup.updateAll();
	}
	
	public void notifyTrackChanged(Track track) {
		midiView.notifyTrackChanged(track.getId());
		pageSelectGroup.updateAll();
	}
	
	public void notifyTrackUpdated(Track track) {
		midiView.notifyTrackChanged(track.getId());
		midiTrackView.notifyTrackUpdated(track);
		pageSelectGroup.updateAll();
	}
	
	public void notifyTrackDeleted(Track track) {
		midiTrackView.notifyTrackDeleted(track);
		midiView.notifyTrackDeleted(track);
	}
	
	@Override
	public void initAll() {
		super.initAll();
		BeatBotActivity.mainActivity.setupProject();
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
