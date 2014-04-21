package com.kh.beatbot.ui.view;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.Track;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.shape.RenderGroup;

public class MidiTrackView extends TouchableView implements TrackListener {
	public MidiTrackView(RenderGroup renderGroup) {
		super(renderGroup);
	}

	@Override
	public synchronized void layoutChildren() {
		float yPos = 0; // yOffset
		for (View child : children) {
			child.layout(this, 0, yPos, width, MidiView.trackHeight);
			yPos += MidiView.trackHeight;
		}
	}

	@Override
	public void onCreate(Track track) {
		TrackButtonRow buttonRow = new TrackButtonRow(renderGroup, track);
		track.setButtonRow(buttonRow);
		addChild(buttonRow);
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
		track.checkInstrumentButton();
		for (int i = 0; i < TrackManager.getNumTracks(); i++) {
			Track otherTrack = TrackManager.getTrack(i);
			if (!track.equals(otherTrack)) {
				otherTrack.getButtonRow().instrumentButton.setChecked(false);
			}
		}
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
		if (solo) {
			// if this track is soloing, set all other solo icons to inactive.
			for (Track otherTrack : TrackManager.getTracks()) {
				if (!track.equals(otherTrack)) {
					otherTrack.getButtonRow().soloButton.setChecked(false);
				}
			}
		}
	}
}
