package com.kh.beatbot.ui.view;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.Track;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.ui.shape.RenderGroup;

public class MidiTrackView extends TouchableView implements TrackListener {
	public MidiTrackView(View view, RenderGroup renderGroup) {
		super(view, renderGroup);
	}

	@Override
	public synchronized void layoutChildren() {
		for (int i = 0; i < children.size(); i++) {
			children.get(i).layout(this, 0, MidiView.trackHeight * i, width, MidiView.trackHeight);
		}
	}

	@Override
	public void onCreate(Track track) {
		TrackButtonRow buttonRow = new TrackButtonRow(this, track);
		track.setButtonRow(buttonRow);
		layoutChildren();
	}

	@Override
	public void onDestroy(Track track) {
		TrackButtonRow buttonRow = track.getButtonRow();
		removeChild(buttonRow);
		layoutChildren();
		track.setButtonRow(null);
	}

	@Override
	public void onSelect(BaseTrack track) {
	}

	@Override
	public void onSampleChange(Track track) {
		track.getButtonRow().updateInstrumentIcon();
	}

	@Override
	public void onMuteChange(Track track, boolean mute) {
	}

	@Override
	public void onSoloChange(Track track, boolean solo) {
	}

	@Override
	public float getYTouchTransform() {
		return mainPage.midiViewGroup.midiView.getYOffset();
	}
}
