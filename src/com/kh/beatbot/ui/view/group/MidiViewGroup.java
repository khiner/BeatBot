package com.kh.beatbot.ui.view.group;

import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.MidiTrackView;
import com.kh.beatbot.ui.view.MidiView;
import com.kh.beatbot.ui.view.TouchableView;

public class MidiViewGroup extends TouchableView {
	public MidiTrackView midiTrackView;
	public MidiView midiView;

	private float trackControlWidth = 0;

	public float getTrackControlWidth() {
		return trackControlWidth;
	}

	@Override
	protected synchronized void createChildren() {
		midiTrackView = new MidiTrackView();
		midiView = new MidiView();

		TrackManager.addTrackListener(midiView);
		TrackManager.addTrackListener(midiTrackView);

		addChildren(midiView, midiTrackView);
	}

	@Override
	protected synchronized void layoutChildren() {
		MidiView.trackHeight = (height - MidiView.Y_OFFSET) / 5f;
		trackControlWidth = MidiView.trackHeight * 2.5f;

		midiTrackView.layout(this, 0, 0, trackControlWidth, height);
		midiView.layout(this, trackControlWidth, 0, width - trackControlWidth, height);
	}
}
