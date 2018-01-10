package com.odang.beatbot.event.track;

import com.odang.beatbot.event.Stateful;
import com.odang.beatbot.event.Temporal;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.view.View;

public class TrackLoopWindowSetEvent implements Stateful, Temporal {
    private int trackId;
    private float initialBeginLevel = 0, initialEndLevel = 0, finalBeginLevel = 0,
            finalEndLevel = 0;

    public TrackLoopWindowSetEvent(int trackId) {
        this.trackId = trackId;
    }

    @Override
    public void begin() {
        Track track = View.context.getTrackManager().getTrackById(trackId);
        if (track == null || track.getLoopBeginParam() == null)
            return;
        initialBeginLevel = track.getLoopBeginParam().viewLevel;
        initialEndLevel = track.getLoopEndParam().viewLevel;
    }

    @Override
    public void end() {
        Track track = View.context.getTrackManager().getTrackById(trackId);
        if (track == null || track.getLoopBeginParam() == null)
            return;

        finalBeginLevel = track.getLoopBeginParam().viewLevel;
        finalEndLevel = track.getLoopEndParam().viewLevel;

        if (initialBeginLevel != finalBeginLevel || initialEndLevel != finalEndLevel) {
            View.context.getEventManager().eventCompleted(this);
        }
    }

    @Override
    public void undo() {
        Track track = View.context.getTrackManager().getTrackById(trackId);
        track.setSampleLoopWindow(initialBeginLevel, initialEndLevel);
    }

    @Override
    public void apply() {
        Track track = View.context.getTrackManager().getTrackById(trackId);
        track.setSampleLoopWindow(finalBeginLevel, finalEndLevel);
    }
}
