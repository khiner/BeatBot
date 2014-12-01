package com.kh.beatbot.listener;

import java.io.File;

public interface RecordStateListener {
	void onListenStart();
	void onListenStop();
	void onRecordStart();
	void onRecordStop(File recordedSampleFile);
}
