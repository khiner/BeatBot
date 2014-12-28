package com.kh.beatbot.ui.view.page;

import com.kh.beatbot.listener.TrackListener;
import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;
import com.kh.beatbot.ui.view.TouchableView;
import com.kh.beatbot.ui.view.View;

public class TrackPage extends TouchableView implements TrackListener {

	public TrackPage(View view) {
		super(view);
	}

	@Override
	public void onCreate(Track track) {
	}

	@Override
	public void onDestroy(Track track) {
	}

	@Override
	public void onSelect(BaseTrack track) {
	}

	@Override
	public void onSampleChange(Track track) {
	}

	@Override
	public void onMuteChange(Track track, boolean mute) {
	}

	@Override
	public void onSoloChange(Track track, boolean solo) {
	}
}
