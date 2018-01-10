package com.odang.beatbot.event.track;

import com.odang.beatbot.event.Executable;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.view.View;

public class TrackMuteEvent extends Executable {
    private int trackId;

    public TrackMuteEvent(Track track) {
        this.trackId = track.getId();
    }

    @Override
    public void undo() {
        doExecute();
    }

    @Override
    public boolean doExecute() {
        final Track track = View.context.getTrackManager().getTrackById(trackId);
        track.mute(!track.isMuted());
        return true;
    }
}
