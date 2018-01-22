package com.odang.beatbot.event;

import com.odang.beatbot.track.Track;
import com.odang.beatbot.ui.view.View;

import java.io.File;

public class SampleRenameEvent extends Executable {
    private int trackId;
    private String originalSampleName, newSampleName;
    private File originalSampleFile;

    public SampleRenameEvent(int trackId, File originalSampleFile, String newSampleName) {
        this.trackId = trackId;
        this.originalSampleFile = originalSampleFile;
        originalSampleName = originalSampleFile.getName();
        this.newSampleName = newSampleName;
    }

    @Override
    public void undo() {
        doExecute(originalSampleName);
    }

    public boolean doExecute() {
        return doExecute(newSampleName);
    }

    private boolean doExecute(String sampleName) {
        if (originalSampleFile == null || sampleName == null || sampleName.isEmpty()
                || newSampleName.equals(originalSampleName)) {
            return false;
        }

        File newFile = new File(originalSampleFile.getParent() + "/" + sampleName);
        originalSampleFile.renameTo(newFile);
        View.context.getFileManager().onNameChange(getTrack(), originalSampleFile, newFile);
        originalSampleFile = newFile;
        return true;
    }

    protected Track getTrack() {
        return View.context.getTrackManager().getTrackById(trackId);
    }
}
