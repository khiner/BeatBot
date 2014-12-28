package com.kh.beatbot.listener;

import com.kh.beatbot.track.BaseTrack;
import com.kh.beatbot.track.Track;

public interface TrackLevelsEventListener {
	public void onTrackLevelsChange(BaseTrack track);

	public void onNoteLevelsChange(Track track);

	public void onSampleLoopWindowChange(Track track);
}
