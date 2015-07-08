package com.kh.beatbot.listener;

import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;

public interface TrackListener {
	void onCreate(Track track);

	void onDestroy(Track track);

	void onSelect(BaseTrack track);

	void onSampleChange(Track track);

	void onMuteChange(Track track, boolean mute);

	void onSoloChange(Track track, boolean solo);

	void onEffectOrderChange(BaseTrack track, int initialEffectPosition, int endEffectPosition);
}
