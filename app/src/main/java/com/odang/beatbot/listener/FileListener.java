package com.odang.beatbot.listener;

import com.odang.beatbot.track.Track;

import java.io.File;

public interface FileListener {
    void onNameChange(Track track, File file, File newFile);
}
