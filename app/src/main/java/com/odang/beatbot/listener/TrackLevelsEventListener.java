package com.odang.beatbot.listener;

import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;

public interface TrackLevelsEventListener {
    public void onTrackLevelsChange(BaseTrack track);

    public void onSampleLoopWindowChange(Track track);
}
