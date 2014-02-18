package com.kh.beatbot.listener;

import com.kh.beatbot.BaseTrack;
import com.kh.beatbot.Track;

public interface TrackListener {
	void onCreate(Track track);
	void onDestroy(Track track);
	void onSelect(BaseTrack track);
	void onSampleChange(Track track);
	void onMuteChange(Track track, boolean mute);
	void onSoloChange(Track track, boolean solo);
}
