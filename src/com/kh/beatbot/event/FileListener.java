package com.kh.beatbot.event;

import java.io.File;

public interface FileListener {
	public void onNameChange(File file, File newFile);
}
