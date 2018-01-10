package com.odang.beatbot.event.track;

import com.odang.beatbot.event.Executable;
import com.odang.beatbot.file.ProjectFile;
import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.view.View;

public class TrackCreateEvent extends Executable {
    private int trackId;
    private String serializedTrack = null;

    public TrackCreateEvent() {
    }

    public TrackCreateEvent(Track track) {
        this.trackId = track.getId();
        this.serializedTrack = ProjectFile.trackToJson(track);
    }

    @Override
    public void undo() {
        new TrackDestroyEvent(trackId).apply();
    }

    @Override
    public boolean doExecute() {
        if (serializedTrack == null) {
            final Track track = View.context.getTrackManager().createTrack();
            trackId = track.getId();
        } else {
            ProjectFile.trackFromJson(serializedTrack);
        }
        return true;
    }
}
