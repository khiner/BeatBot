package com.kh.beatbot.ui.view.page.track;

import com.kh.beatbot.effect.Effect;
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

	public void onReverseChange(Track track, boolean reverse) {
	}

	public void onLoopChange(Track track, boolean loop) {
	}

	@Override
	public void onEffectCreate(BaseTrack track, Effect effect) {
	}

	@Override
	public void onEffectDestroy(BaseTrack track, Effect effect) {
	}

	@Override
	public void onEffectOrderChange(BaseTrack track, int initialEffectPosition,
			int endEffectPosition) {
	}
}
