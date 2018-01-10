package com.odang.beatbot.listener;

import com.odang.beatbot.track.BaseTrack;
import com.odang.beatbot.track.Track;

public interface TrackLevelsEventListener {
    void onTrackLevelsChange(BaseTrack track);

    void onSampleLoopWindowChange(Track track);
}
