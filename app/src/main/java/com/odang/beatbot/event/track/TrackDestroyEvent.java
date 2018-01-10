package com.odang.beatbot.event.track;

import com.odang.beatbot.event.Executable;
import com.odang.beatbot.file.ProjectFile;
import com.odang.beatbot.ui.view.View;

public class TrackDestroyEvent extends Executable {
    private int trackId;
    private String serializedTrack = null;

    public TrackDestroyEvent(int trackId) {
        this.trackId = trackId;
        this.serializedTrack = ProjectFile.trackToJson(View.context.getTrackManager().getTrackById(trackId));
    }

    @Override
    public void undo() {
        ProjectFile.trackFromJson(serializedTrack);
    }

    public boolean doExecute() {
        if (View.context.getTrackManager().getNumTracks() > 1) {
            View.context.getTrackManager().getTrackById(trackId).destroy();
            return true;
        } else {
            return false;
        }
    }
}
