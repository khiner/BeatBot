package com.kh.beatbot.ui.view;

import com.kh.beatbot.Track;
import com.kh.beatbot.listener.ScrollableViewListener;
import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.manager.TrackManager;
import com.kh.beatbot.ui.view.helper.TickWindowHelper;

public class MidiTrackView extends TouchableView implements TrackListener, ScrollableViewListener {

	@Override
	public synchronized void layoutChildren() {
		float yPos = MidiView.Y_OFFSET - TickWindowHelper.getYOffset();
		for (View child : children) {
			child.layout(this, 0, yPos, width, MidiView.trackHeight);
			yPos += MidiView.trackHeight;
		}
	}

	@Override
	public void onCreate(Track track) {
		addChild(track.getButtonRow());
		layoutChildren();
	}

	@Override
	public void onDestroy(Track track) {
		track.getButtonRow().destroy();
		removeChild(track.getButtonRow());
		layoutChildren();
	}

	@Override
	public void onSelect(Track track) {
		track.getButtonRow().instrumentButton.setChecked(true);
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
			// if this track is soloing, set all other solo icons to
			// inactive.
			track.getButtonRow().soloButton.setChecked(true);
			for (int i = 0; i < TrackManager.getNumTracks(); i++) {
				Track otherTrack = TrackManager.getTrack(i);
				if (!track.equals(otherTrack)) {
					otherTrack.getButtonRow().soloButton.setChecked(false);
				}
			}
		}
	}

	@Override
	public void onGlReady(View view) {
		
	}

	@Override
	public void onInitialize(View view) {
		
	}

	@Override
	public void onScrollX(View view) {
		
	}

	@Override
	public void onScrollY(View view) {
		layoutChildren();
	}
}
