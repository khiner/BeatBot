package com.odang.beatbot.listener;

import java.io.File;

public interface FileListener {
    public void onNameChange(File file, File newFile);
}
